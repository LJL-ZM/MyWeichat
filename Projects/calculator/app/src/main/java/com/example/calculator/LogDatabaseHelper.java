package com.example.calculator;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LogDatabaseHelper extends SQLiteOpenHelper {

    // 全局单例对象 & 数据库实例
    private static LogDatabaseHelper mHelper;
    private static SQLiteDatabase mDb;

    // 表名、字段常量
    private static final String TABLE_NAME = "Item";
    private static final String COL_DATE = "date";
    private static final String COL_EXPRESSION = "expression";
    private static final String COL_RESULT = "result";
    private static final String COL_IS_HEADER = "is_date_header"; // 1=日期头  0=普通记录

    // 初始化（全局调用一次）
    public static void init(Context context) {
        if (mHelper == null) {
            mHelper = new LogDatabaseHelper(context.getApplicationContext(), "item.db", null, 1);
            mDb = mHelper.getWritableDatabase();
        }
    }

    // 静态插入方法
    public static void insert(String date, String expression, String result, boolean isDateHeader) {
        ContentValues values = new ContentValues();
        values.put(COL_DATE, date);
        values.put(COL_EXPRESSION, expression);
        values.put(COL_RESULT, result);
        values.put(COL_IS_HEADER, isDateHeader ? 1 : 0);
        mDb.insert(TABLE_NAME, null, values);
    }

    // 静态查询所有数据
    @SuppressLint("Range")
    public static List<HistoryItem> selectAllData() {
        List<HistoryItem> list = new ArrayList<>();
        Cursor cursor = mDb.query(TABLE_NAME, null, null, null, null, null, COL_DATE + " DESC");
        if (cursor.moveToFirst()) {
            do {
                String date = cursor.getString(cursor.getColumnIndex(COL_DATE));
                String exp = cursor.getString(cursor.getColumnIndex(COL_EXPRESSION));
                String res = cursor.getString(cursor.getColumnIndex(COL_RESULT));
                int isHeader = cursor.getInt(cursor.getColumnIndex(COL_IS_HEADER));

                HistoryItem item;
                if (isHeader == 1) {
                    item = new HistoryItem(date);
                } else {
                    item = new HistoryItem(date, exp, res);
                }
                list.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    // 静态清空全部数据
    public static void clearAll() {
        mDb.delete(TABLE_NAME, null, null);
    }

    // 私有构造，禁止外部 new
    private LogDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 建表语句，包含类型标记字段
        String sql = "CREATE TABLE " + TABLE_NAME + " ("
                + COL_DATE + " TEXT, "
                + COL_EXPRESSION + " TEXT, "
                + COL_RESULT + " TEXT, "
                + COL_IS_HEADER + " INTEGER)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    // 获取当前日期字符串 (格式: yyyy年MM月dd日)
    public static String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault());
        return sdf.format(new Date());
    }

    // 判断是否需要添加日期头
    public static boolean needAddDateHeader() {
        // 如果缓存为空，需要从数据库读取
        if (HistoryCache.isEmpty()) {
            List<HistoryItem> dbList = selectAllData();
            if (dbList == null || dbList.isEmpty()) {
                return true; // 没有任何记录，需要添加日期头
            }
            // 找到最后一条日期头记录
            for (int i = dbList.size() - 1; i >= 0; i--) {
                HistoryItem item = dbList.get(i);
                if (item.isDateHeader) {
                    return !item.dateText.equals(getCurrentDate());
                }
            }
            return true; // 没有日期头记录，需要添加
        } else {
            // 从缓存中查找
            List<HistoryItem> cacheList = HistoryCache.getHistoryItems();
            for (int i = cacheList.size() - 1; i >= 0; i--) {
                HistoryItem item = cacheList.get(i);
                if (item.isDateHeader) {
                    return !item.dateText.equals(getCurrentDate());
                }
            }
            return true; // 没有日期头记录，需要添加
        }
    }

    // 保存计算记录（自动判断是否需要添加日期头）
    public static void saveRecord(String expression, String result) {
        String currentDate = getCurrentDate();
        
        // 先判断是否需要添加日期头
        if (needAddDateHeader()) {
            // 添加日期头记录
            LogDatabaseHelper.insert(currentDate, "", "", true);
            HistoryCache.insert(new HistoryItem(currentDate));
        }
        
        // 添加计算记录
        LogDatabaseHelper.insert(currentDate, expression, result, false);
        HistoryCache.insert(new HistoryItem(currentDate, expression, result));
    }

    // 数据模型
    public static class HistoryItem {
        public boolean isDateHeader;
        public String dateText;
        public String expression;
        public String result;

        // 日期头部构造
        public HistoryItem(String dateText) {
            this.isDateHeader = true;
            this.dateText = dateText;
        }

        // 普通计算记录构造
        public HistoryItem(String dateText, String expression, String result) {
            this.isDateHeader = false;
            this.dateText = dateText;
            this.expression = expression;
            this.result = result;
        }
    }
}