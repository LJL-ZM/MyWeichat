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
        // 1.好友申请表 local_friend_request
        String sqlReq = "CREATE TABLE local_friend_request(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "request_id INTEGER UNIQUE," +
                "from_uid INTEGER," +
                "from_nickname TEXT," +
                "req_msg TEXT," +
                "status INTEGER DEFAULT 0," +
                "create_time TEXT)";
        db.execSQL(sqlReq);

        // 2.好友表 friend_local（带上 remark 备注字段）
        String sqlFriend = "CREATE TABLE friend_local(" +
                "friend_uid INTEGER PRIMARY KEY," +
                "nickname TEXT," +
                "remark TEXT)";
        // 会话表
        db.execSQL("CREATE TABLE IF NOT EXISTS local_session (" +
                "chat_id INTEGER PRIMARY KEY," +
                "chat_type INTEGER," +
                "chat_name TEXT," +
                "last_msg_content TEXT," +
                "last_msg_time INTEGER," +
                "unread_count INTEGER DEFAULT 0)");

        // 消息表
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

    // 插入消息，冲突自动替换（去重）
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

    // 查询某个会话的所有消息，按时间正序
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

    // 保存/更新会话
    // 注意：CONFLICT_REPLACE 会用 bean.unreadCount(默认0) 覆盖已有未读数，
    // 因此仅适合"新建会话"。收到新消息/刷新会话列表时请用 upsertSessionKeepUnread。
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

    /**
     * 更新会话最后一条消息/名称/类型，但【保留本地已有未读数】。
     * 用于：1) 收到 NEW_CHAT_MSG 时更新会话；2) 下拉刷新合并服务器会话列表。
     * 若该会话本地不存在，则按 unread_count=0 新建。
     */
    public void upsertSessionKeepUnread(ChatSessionBean bean) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("chat_type", bean.chatType);
        values.put("chat_name", bean.chatName);
        values.put("last_msg_content", bean.lastMsgContent);
        values.put("last_msg_time", bean.lastMsgTime);
        int rows = db.update("local_session", values, "chat_id=?", new String[]{String.valueOf(bean.chatId)});
        if (rows == 0) {
            // 本地不存在该会话，新建，未读数默认0
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

    // 获取所有会话列表
    // 修复：优先用本地消息表(local_chat_msg)里该会话的最新消息内容/时间作为预览，
    // 避免服务端 session.last_msg_content 残留（如测试消息）或 last_msg_time 为 NULL
    // 导致预览错误和排序失效。本地无消息时回退到 session 表自身字段。
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

    // 增加未读数
    public void addUnreadCount(int chatId) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE local_session SET unread_count = unread_count + 1 WHERE chat_id=?", new Object[]{chatId});
    }

    // 清空未读数
    public void clearUnreadCount(int chatId) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE local_session SET unread_count = 0 WHERE chat_id=?", new Object[]{chatId});
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    // ===================== 好友申请相关方法 =====================
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

    // ===================== 好友表相关方法 =====================
    // 添加好友（外部统一调用这个）
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

    // 查询全部好友（好友碎片页面加载）
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

    // 判断是否已经是好友（推荐统一使用这个）
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
    /**
     * 清空本地全部好友（好友列表下拉刷新使用，全量覆盖逻辑）
     */
    public void clearAllFriend() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM friend_local");
    }

    /**
     * 清空所有本地好友申请
     * 注意：【不推荐直接全清】只是补齐你调用的方法，后面按之前方案改成增量更新
     */
    public void clearAllRequest() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM local_friend_request");
    }

    public void invalidateLostRequest(List<Integer> serverRequestIdList) {
        SQLiteDatabase db = getWritableDatabase();
        if(serverRequestIdList.isEmpty()){
            //后端没有任何待处理申请，所有本地未处理全部失效
            db.execSQL("UPDATE local_friend_request SET status=-1 WHERE status=0");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for(Integer id : serverRequestIdList){
            sb.append(id).append(",");
        }
        sb.deleteCharAt(sb.length()-1);
        // status=-1代表申请已失效（对方处理/撤回），UI中可以隐藏或者灰色展示
        // 修复：列名 request_id（snake_case），原 requestId 导致 SQLiteException 闪退
        String sql = "UPDATE local_friend_request SET status=-1 WHERE status=0 AND request_id NOT IN ("+sb+")";
        db.execSQL(sql);
    }
    /**
     * 新增/更新单条好友申请
     * @param bean 本地申请实体
     */
    public void addRequest(FriendRequestLocalBean bean) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        // 修复：列名必须与建表语句(snake_case)一致，原驼峰名导致 SQLiteException 闪退
        values.put("request_id", bean.requestId);
        values.put("from_uid", bean.fromUid);
        values.put("from_nickname", bean.fromNickname);
        values.put("req_msg", bean.reqMsg);
        values.put("status", bean.status);

        // INSERT OR REPLACE → request_id存在就更新，不存在新增（天然去重！）
        db.insertWithOnConflict("local_friend_request", null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }
}