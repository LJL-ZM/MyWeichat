package com.example.myweixin_client;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myweixin_client.databinding.ActivityChatBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {
    ActivityChatBinding binding;
    int chatId;
    String chatName;
    int chatType;
    List<ChatMsgBean> msgList = new ArrayList<>();
    ChatMsgAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        chatId = getIntent().getIntExtra("chat_id", 0);
        chatName = getIntent().getStringExtra("chat_name");
        chatType = getIntent().getIntExtra("chat_type", 1);
        binding.toolbar.setTitle(chatName);

        adapter = new ChatMsgAdapter(msgList);
        binding.rvChat.setLayoutManager(new LinearLayoutManager(this));
        binding.rvChat.setAdapter(adapter);

        loadLocalMsg();
        pullHistoryMsg();

        binding.btnSend.setOnClickListener(v -> {
            String content = binding.etInput.getText().toString().trim();
            if (TextUtils.isEmpty(content)) return;
            sendMessage(content);
            binding.etInput.setText("");
        });

        SocketClientManager.getInstance().setCurrentChatActivity(this, chatId);
        LocalDBHelper.getInstance(this).clearUnreadCount(chatId);
    }

    private void loadLocalMsg() {
        new Thread(() -> {
            List<ChatMsgBean> local = LocalDBHelper.getInstance(this).getChatMsgList(chatId);
            runOnUiThread(() -> {
                msgList.clear();
                msgList.addAll(local);
                adapter.notifyDataSetChanged();
                scrollToBottom();
            });
        }).start();
    }

    private void pullHistoryMsg() {
        String json = "{\"chatId\":" + chatId + ",\"page\":1,\"pageSize\":50}";
        HttpUtil.postJson("/get_history_msg", json, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(ChatActivity.this, "加载历史消息失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String body = response.body().string();
                BaseHttpResp resp = GsonUtil.GSON.fromJson(body, BaseHttpResp.class);
                if (resp.getCode() == 0) {
                    ChatMsgBean[] array = GsonUtil.GSON.fromJson(GsonUtil.GSON.toJson(resp.getData()), ChatMsgBean[].class);
                    new Thread(() -> {
                        for (ChatMsgBean msg : array) {
                            msg.isSendByMe = msg.fromUid == SpUtil.getUid();
                            LocalDBHelper.getInstance(ChatActivity.this).insertChatMsg(msg);
                        }
                        loadLocalMsg();
                    }).start();
                }
            }
        });
    }

    private void sendMessage(String content) {
        ChatMsgBean localMsg = new ChatMsgBean();
        localMsg.chatId = chatId;
        localMsg.fromUid = SpUtil.getUid();
        localMsg.fromNickname = SpUtil.getNickname();
        localMsg.content = content;
        localMsg.sendTime = System.currentTimeMillis();
        localMsg.isSendByMe = true;
        localMsg.msgId = 0; // 临时ID，服务器返回前不落库

        msgList.add(localMsg);
        adapter.notifyItemInserted(msgList.size() - 1);
        scrollToBottom();

        ChatMsgBean req = new ChatMsgBean();
        req.chatId = chatId;
        req.fromUid = SpUtil.getUid();
        req.content = content;
        String json = GsonUtil.toJson(req);

        HttpUtil.postJson("/send_msg", json, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(ChatActivity.this, "发送失败", Toast.LENGTH_SHORT).show());
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String body = response.body().string();
                try {
                    BaseHttpResp resp = GsonUtil.GSON.fromJson(body, BaseHttpResp.class);
                    if (resp.getCode() == 0) {
                        // 服务器返回带真实 msgId/sendTime 的消息体
                        ChatMsgBean serverMsg = GsonUtil.GSON.fromJson(
                                GsonUtil.GSON.toJson(resp.getData()), ChatMsgBean.class);
                        // 用服务器真实数据回填本地乐观消息
                        localMsg.msgId = serverMsg.msgId;
                        localMsg.sendTime = serverMsg.sendTime;
                        localMsg.fromNickname = serverMsg.fromNickname;
                        // 落库（按真实 msgId 去重）
                        LocalDBHelper.getInstance(ChatActivity.this).insertChatMsg(localMsg);
                        // 更新会话最后一条消息，保留未读数
                        ChatSessionBean session = new ChatSessionBean();
                        session.chatId = chatId;
                        session.chatType = chatType;
                        session.chatName = chatName;
                        session.lastMsgContent = content;
                        session.lastMsgTime = serverMsg.sendTime;
                        LocalDBHelper.getInstance(ChatActivity.this).upsertSessionKeepUnread(session);
                    } else {
                        runOnUiThread(() -> Toast.makeText(ChatActivity.this,
                                resp.getMsg() == null ? "发送失败" : resp.getMsg(), Toast.LENGTH_SHORT).show());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(ChatActivity.this, "发送失败", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    public void onNewMessage(ChatMsgBean msg) {
        // 去重：同一 msgId 已存在则不重复加入UI（socket推送与历史拉取可能重叠）
        for (ChatMsgBean m : msgList) {
            if (m.msgId != 0 && m.msgId == msg.msgId) {
                return;
            }
        }
        msgList.add(msg);
        adapter.notifyItemInserted(msgList.size() - 1);
        scrollToBottom();
    }

    private void scrollToBottom() {
        if (msgList.size() > 0) {
            binding.rvChat.scrollToPosition(msgList.size() - 1);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SocketClientManager.getInstance().clearCurrentChatActivity();
    }
}