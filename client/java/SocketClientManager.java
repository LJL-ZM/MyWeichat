package com.example.myweixin_client;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class SocketClientManager {
    private static SocketClientManager instance;
    private Socket socket;
    private BufferedReader reader;
    private OutputStreamWriter writer;
    private Context context;
    private ChatActivity currentChatActivity;
    private int currentChatId = -1;
    private volatile boolean running = false;
    private int connectUid;
    private String connectIp;
    private int connectPort;
    private Runnable sessionChangedListener;

    private SocketClientManager(){}
    public static SocketClientManager getInstance(){
        if(instance == null){
            instance = new SocketClientManager();
        }
        return instance;
    }

    public void setSessionChangedListener(Runnable listener) {
        this.sessionChangedListener = listener;
    }

    public void connect(Context ctx, int uid, String ip, int port){
        this.context = ctx.getApplicationContext();
        this.connectUid = uid;
        this.connectIp = ip;
        this.connectPort = port;
        if (running) return;
        running = true;
        new Thread(() -> {
            while (running) {
                try {
                    socket = new Socket(connectIp, connectPort);
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                    writer = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");

                    Message<LoginRequest> loginMsg = new Message<>();
                    loginMsg.setType("SOCKET_LOGIN");
                    LoginRequest loginData = new LoginRequest();
                    loginData.setUid(connectUid);
                    loginMsg.setData(loginData);
                    String loginJson = GsonUtil.toJson(loginMsg);
                    Log.d("SOCKET_DEBUG", "准备发送登录报文：" + loginJson);
                    sendMsg(loginJson);

                    String line;
                    while ((line = reader.readLine()) != null){
                        Log.d("SOCKET_DEBUG","收到服务端报文："+line);
                        Message<?> msgEntity = GsonUtil.GSON.fromJson(line, Message.class);
                        String type = msgEntity.getType();
                        handleMessage(type, msgEntity);
                    }
                    if (!running) break;
                }catch (Exception e){
                    Log.e("SOCKET_DEBUG","连接异常",e);
                }
                if (!running) break;
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ie) {
                    break;
                }
            }
        }).start();
    }

    private void handleMessage(String type, Message<?> msgEntity) {
        if ("NEW_FRIEND_APPLY".equals(type)) {
            String innerJson = GsonUtil.toJson(msgEntity.getData());
            FriendRequestLocalBean bean = GsonUtil.GSON.fromJson(innerJson, FriendRequestLocalBean.class);
            bean.status = 0;
            LocalDBHelper db = LocalDBHelper.getInstance(context);
            db.insertRequest(bean);
            new Handler(Looper.getMainLooper()).post(()->{
                FriendRequestActivity.showFriendApplyNotification(context, bean.fromNickname);
            });
        } else if("FRIEND_APPROVE".equals(type)){
            String innerJson = GsonUtil.toJson(msgEntity.getData());
            PushMsgData data = GsonUtil.GSON.fromJson(innerJson, PushMsgData.class);

            FriendLocalBean friend = new FriendLocalBean(data.otherUid, data.nickname);
            LocalDBHelper.getInstance(context).addFriend(friend);

            new Handler(Looper.getMainLooper()).post(()->{
                FriendRequestActivity.showAgreeApplyNotification(context, data.nickname);
            });
        } else if ("NEW_CHAT_MSG".equals(type)) {
            String innerJson = GsonUtil.toJson(msgEntity.getData());
            ChatMsgBean msg = GsonUtil.GSON.fromJson(innerJson, ChatMsgBean.class);
            msg.isSendByMe = msg.fromUid == SpUtil.getUid();

            LocalDBHelper.getInstance(context).insertChatMsg(msg);

            ChatSessionBean session = new ChatSessionBean();
            session.chatId = msg.chatId;
            session.chatType = 1;
            session.chatName = msg.fromNickname;
            session.lastMsgContent = msg.content;
            session.lastMsgTime = msg.sendTime;
            LocalDBHelper.getInstance(context).upsertSessionKeepUnread(session);

            if (currentChatActivity != null && currentChatId == msg.chatId) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    currentChatActivity.onNewMessage(msg);
                });
                LocalDBHelper.getInstance(context).clearUnreadCount(msg.chatId);
            } else {
                LocalDBHelper.getInstance(context).addUnreadCount(msg.chatId);
                showMsgNotification(context, msg.fromNickname, msg.content);
            }
            if (sessionChangedListener != null) {
                new Handler(Looper.getMainLooper()).post(sessionChangedListener);
            }
        }
    }

    public void sendMsg(String json){
        try {
            writer.write(json + "\n");
            writer.flush();
            Log.d("SOCKET_DEBUG","发送报文："+json);
        } catch (Exception e) {
            Log.e("SOCKET_DEBUG","发送失败",e);
        }
    }

    public void setCurrentChatActivity(ChatActivity activity, int chatId) {
        this.currentChatActivity = activity;
        this.currentChatId = chatId;
    }

    public void clearCurrentChatActivity() {
        this.currentChatActivity = null;
        this.currentChatId = -1;
    }

    public void disconnect(){
        running = false;
        try {
            if(socket != null && !socket.isClosed()){
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        socket = null;
        reader = null;
        writer = null;
    }

    private void showMsgNotification(Context ctx, String title, String content) {
        try {
            NotificationManager manager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        "chat_msg", "聊天消息", NotificationManager.IMPORTANCE_HIGH);
                manager.createNotificationChannel(channel);
            }
            Notification notification = new NotificationCompat.Builder(ctx, "chat_msg")
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setAutoCancel(true)
                    .build();
            manager.notify((int) (Math.random() * 10000), notification);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}