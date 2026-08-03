package com.example.myweixin_client;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class LocalDBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "weixin_local.db";
    private static final int VERSION = 1;
    private static LocalDBHelper instance;

    public static synchronized LocalDBHelper getInstance(Context context) {
        if (instance == null) {
            instance = new LocalDBHelper(context.getApplicationContext());
        }
        return instance;
    }

    private LocalDBHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sqlReq = "CREATE TABLE local_friend_request(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "request_id INTEGER UNIQUE," +
                "from_uid INTEGER," +
                "from_nickname TEXT," +
                "req_msg TEXT," +
                "status INTEGER DEFAULT 0," +
                "create_time TEXT)";
        db.execSQL(sqlReq);

        String sqlFriend = "CREATE TABLE friend_local(" +
                "friend_uid INTEGER PRIMARY KEY," +
                "nickname TEXT," +
                "remark TEXT)";
        db.execSQL("CREATE TABLE IF NOT EXISTS local_session (" +
                "chat_id INTEGER PRIMARY KEY," +
                "chat_type INTEGER," +
                "chat_name TEXT," +
                "last_msg_content TEXT," +
                "last_msg_time INTEGER," +
                "unread_count INTEGER DEFAULT 0)");

        db.execSQL("CREATE TABLE IF NOT EXISTS local_chat_msg (" +
                "msg_id INTEGER PRIMARY KEY," +
                "chat_id INTEGER," +
                "from_uid INTEGER," +
                "from_nickname TEXT," +
                "content TEXT," +
                "send_time INTEGER," +
                "is_send_by_me INTEGER)");
        db.execSQL(sqlFriend);
    }

    public void insertChatMsg(ChatMsgBean bean) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("msg_id", bean.msgId);
        values.put("chat_id", bean.chatId);
        values.put("from_uid", bean.fromUid);
        values.put("from_nickname", bean.fromNickname);
        values.put("content", bean.content);
        values.put("send_time", bean.sendTime);
        values.put("is_send_by_me", bean.isSendByMe ? 1 : 0);
        db.insertWithOnConflict("local_chat_msg", null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public List<ChatMsgBean> getChatMsgList(int chatId) {
        List<ChatMsgBean> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM local_chat_msg WHERE chat_id=? ORDER BY send_time ASC", new String[]{String.valueOf(chatId)});
        while (cursor.moveToNext()) {
            ChatMsgBean bean = new ChatMsgBean();
            bean.msgId = cursor.getLong(cursor.getColumnIndexOrThrow("msg_id"));
            bean.chatId = cursor.getInt(cursor.getColumnIndexOrThrow("chat_id"));
            bean.fromUid = cursor.getInt(cursor.getColumnIndexOrThrow("from_uid"));
            bean.fromNickname = cursor.getString(cursor.getColumnIndexOrThrow("from_nickname"));
            bean.content = cursor.getString(cursor.getColumnIndexOrThrow("content"));
            bean.sendTime = cursor.getLong(cursor.getColumnIndexOrThrow("send_time"));
            bean.isSendByMe = cursor.getInt(cursor.getColumnIndexOrThrow("is_send_by_me")) == 1;
            list.add(bean);
        }
        cursor.close();
        return list;
    }

    public void saveSession(ChatSessionBean bean) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("chat_id", bean.chatId);
        values.put("chat_type", bean.chatType);
        values.put("chat_name", bean.chatName);
        values.put("last_msg_content", bean.lastMsgContent);
        values.put("last_msg_time", bean.lastMsgTime);
        values.put("unread_count", bean.unreadCount);
        db.insertWithOnConflict("local_session", null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void upsertSessionKeepUnread(ChatSessionBean bean) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("chat_type", bean.chatType);
        values.put("chat_name", bean.chatName);
        values.put("last_msg_content", bean.lastMsgContent);
        values.put("last_msg_time", bean.lastMsgTime);
        int rows = db.update("local_session", values, "chat_id=?", new String[]{String.valueOf(bean.chatId)});
        if (rows == 0) {
            ContentValues insert = new ContentValues();
            insert.put("chat_id", bean.chatId);
            insert.put("chat_type", bean.chatType);
            insert.put("chat_name", bean.chatName);
            insert.put("last_msg_content", bean.lastMsgContent);
            insert.put("last_msg_time", bean.lastMsgTime);
            insert.put("unread_count", 0);
            db.insertWithOnConflict("local_session", null, insert, SQLiteDatabase.CONFLICT_IGNORE);
        }
    }

    public List<ChatSessionBean> getAllSession() {
        List<ChatSessionBean> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT s.chat_id, s.chat_type, s.chat_name, " +
                "COALESCE((SELECT content FROM local_chat_msg WHERE chat_id=s.chat_id ORDER BY send_time DESC LIMIT 1), s.last_msg_content) AS last_msg_content, " +
                "COALESCE((SELECT send_time FROM local_chat_msg WHERE chat_id=s.chat_id ORDER BY send_time DESC LIMIT 1), s.last_msg_time) AS last_msg_time, " +
                "s.unread_count " +
                "FROM local_session s ORDER BY last_msg_time DESC";
        Cursor cursor = db.rawQuery(sql, null);
        while (cursor.moveToNext()) {
            ChatSessionBean bean = new ChatSessionBean();
            bean.chatId = cursor.getInt(cursor.getColumnIndexOrThrow("chat_id"));
            bean.chatType = cursor.getInt(cursor.getColumnIndexOrThrow("chat_type"));
            bean.chatName = cursor.getString(cursor.getColumnIndexOrThrow("chat_name"));
            bean.lastMsgContent = cursor.getString(cursor.getColumnIndexOrThrow("last_msg_content"));
            bean.lastMsgTime = cursor.getLong(cursor.getColumnIndexOrThrow("last_msg_time"));
            bean.unreadCount = cursor.getInt(cursor.getColumnIndexOrThrow("unread_count"));
            list.add(bean);
        }
        cursor.close();
        return list;
    }

    public void addUnreadCount(int chatId) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE local_session SET unread_count = unread_count + 1 WHERE chat_id=?", new Object[]{chatId});
    }

    public void clearUnreadCount(int chatId) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE local_session SET unread_count = 0 WHERE chat_id=?", new Object[]{chatId});
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public long insertRequest(FriendRequestLocalBean bean) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("request_id", bean.requestId);
        cv.put("from_uid", bean.fromUid);
        cv.put("from_nickname", bean.fromNickname);
        cv.put("req_msg", bean.reqMsg);
        cv.put("status", bean.status);
        cv.put("create_time", bean.createTime);
        long res = db.insertWithOnConflict("local_friend_request", null, cv, SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
        return res;
    }

    @SuppressLint("Range")
    public List<FriendRequestLocalBean> getAllRequest() {
        List<FriendRequestLocalBean> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query("local_friend_request", null, null, null, null, null, "create_time DESC");
        while (cursor.moveToNext()) {
            FriendRequestLocalBean bean = new FriendRequestLocalBean();
            bean.requestId = cursor.getInt(cursor.getColumnIndex("request_id"));
            bean.fromUid = cursor.getInt(cursor.getColumnIndex("from_uid"));
            bean.fromNickname = cursor.getString(cursor.getColumnIndex("from_nickname"));
            bean.reqMsg = cursor.getString(cursor.getColumnIndex("req_msg"));
            bean.status = cursor.getInt(cursor.getColumnIndex("status"));
            bean.createTime = cursor.getString(cursor.getColumnIndex("create_time"));
            list.add(bean);
        }
        cursor.close();
        db.close();
        return list;
    }

    public int updateStatus(int requestId, int newStatus) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("status", newStatus);
        int affect = db.update("local_friend_request", cv, "request_id=?", new String[]{String.valueOf(requestId)});
        db.close();
        return affect;
    }

    public boolean addFriend(FriendLocalBean bean) {
        if (isFriend(bean.getFriendUid())) {
            return false;
        }
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("friend_uid", bean.getFriendUid());
        cv.put("nickname", bean.getNickname());
        cv.put("remark", bean.getRemark());
        long row = db.insert("friend_local", null, cv);
        db.close();
        return row != -1;
    }

    @SuppressLint("Range")
    public List<FriendLocalBean> getAllFriend() {
        List<FriendLocalBean> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query("friend_local", null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            int uid = cursor.getInt(cursor.getColumnIndex("friend_uid"));
            String name = cursor.getString(cursor.getColumnIndex("nickname"));
            String remark = cursor.getString(cursor.getColumnIndex("remark"));
            FriendLocalBean bean = new FriendLocalBean(uid, name);
            bean.setRemark(remark);
            list.add(bean);
        }
        cursor.close();
        db.close();
        return list;
    }

    public boolean isFriend(int targetUid) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query("friend_local",
                new String[]{"friend_uid"},
                "friend_uid=?",
                new String[]{String.valueOf(targetUid)},
                null, null, null);
        boolean exist = cursor.moveToFirst();
        cursor.close();
        db.close();
        return exist;
    }

    public void clearAllFriend() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM friend_local");
    }

    public void clearAllRequest() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM local_friend_request");
    }

    public void invalidateLostRequest(List<Integer> serverRequestIdList) {
        SQLiteDatabase db = getWritableDatabase();
        if(serverRequestIdList.isEmpty()){
            db.execSQL("UPDATE local_friend_request SET status=-1 WHERE status=0");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for(Integer id : serverRequestIdList){
            sb.append(id).append(",");
        }
        sb.deleteCharAt(sb.length()-1);
        String sql = "UPDATE local_friend_request SET status=-1 WHERE status=0 AND request_id NOT IN ("+sb+")";
        db.execSQL(sql);
    }

    public void addRequest(FriendRequestLocalBean bean) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("request_id", bean.requestId);
        values.put("from_uid", bean.fromUid);
        values.put("from_nickname", bean.fromNickname);
        values.put("req_msg", bean.reqMsg);
        values.put("status", bean.status);

        db.insertWithOnConflict("local_friend_request", null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void clearAll() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM local_session");
        db.execSQL("DELETE FROM local_chat_msg");
        db.execSQL("DELETE FROM local_friend_request");
        db.execSQL("DELETE FROM friend_local");
    }
}