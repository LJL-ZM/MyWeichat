package com.example.myweixin_client;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.myweixin_client.databinding.FragmentChatBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChatFragment extends Fragment {
    FragmentChatBinding binding;
    List<ChatSessionBean> sessionList = new ArrayList<>();
    ChatListAdapter adapter;
    LocalDBHelper db;
    private int selfUid;
    private boolean isRequesting = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChatBinding.inflate(inflater, container, false);
        db = LocalDBHelper.getInstance(getContext());
        selfUid = SpUtil.getUid();

        adapter = new ChatListAdapter(sessionList);
        binding.rvChat.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.rvChat.setAdapter(adapter);

        // 下拉刷新
        binding.swipeRefresh.setOnRefreshListener(this::pullRefreshSession);

        // 点击会话跳转聊天页
        adapter.setOnItemClickListener((position, bean) -> {
            Intent intent = new Intent(getContext(), ChatActivity.class);
            intent.putExtra("chat_id", bean.chatId);
            intent.putExtra("chat_name", bean.chatName);
            intent.putExtra("chat_type", bean.chatType);
            startActivity(intent);
        });

        // 注册会话变更监听：收到新消息时 SocketClientManager 回调刷新会话列表
        SocketClientManager.getInstance().setSessionChangedListener(this::loadLocalSession);

        loadLocalSession();
        pullRefreshSession();

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        // 从聊天页返回时刷新会话列表（最后一条消息/未读数可能已变化）
        loadLocalSession();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 清除监听，避免 Fragment 销毁后回调造成泄漏/崩溃
        SocketClientManager.getInstance().setSessionChangedListener(null);
    }

    // 加载本地会话
    private void loadLocalSession() {
        new Thread(() -> {
            List<ChatSessionBean> local = db.getAllSession();
            // 修复：对单聊(chatType=1)按对方昵称(chatName)去重，
            // 避免服务端 create_single_chat 不幂等导致同一对方出现多个会话。
            // 保留 lastMsgTime 最大的一条。
            List<ChatSessionBean> deduped = dedupSingleChat(local);
            requireActivity().runOnUiThread(() -> {
                sessionList.clear();
                sessionList.addAll(deduped);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }

    /**
     * 单聊去重：相同 chatName(对方昵称) 的单聊会话只保留 lastMsgTime 最大的一条。
     * 群聊(chatType!=1)不去重。
     */
    private List<ChatSessionBean> dedupSingleChat(List<ChatSessionBean> src) {
        Map<String, ChatSessionBean> best = new HashMap<>();
        List<ChatSessionBean> groupList = new ArrayList<>();
        for (ChatSessionBean s : src) {
            if (s.chatType != 1) {
                groupList.add(s);
                continue;
            }
            String key = s.chatName == null ? "" : s.chatName;
            ChatSessionBean exist = best.get(key);
            if (exist == null || s.lastMsgTime > exist.lastMsgTime) {
                best.put(key, s);
            }
        }
        List<ChatSessionBean> result = new ArrayList<>(best.values());
        result.addAll(groupList);
        return result;
    }

    // 下拉刷新会话列表
    private void pullRefreshSession() {
        if (isRequesting) return;
        isRequesting = true;

        String json = "{\"uid\":" + selfUid + "}";
        HttpUtil.postJson("/get_my_session", json, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                isRequesting = false;
                requireActivity().runOnUiThread(() -> {
                    binding.swipeRefresh.setRefreshing(false);
                    Toast.makeText(getContext(), "刷新失败", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    String body = response.body().string();
                    BaseHttpResp resp = GsonUtil.GSON.fromJson(body, BaseHttpResp.class);
                    if (resp.getCode() == 0) {
                        ChatSessionBean[] array = GsonUtil.GSON.fromJson(
                                GsonUtil.GSON.toJson(resp.getData()),
                                ChatSessionBean[].class
                        );
                        new Thread(() -> {
                            // 修复：原 saveSession 用 CONFLICT_REPLACE 会把 unread_count 覆盖成 0，
                            // 每次下拉刷新都清空未读数。改用 upsertSessionKeepUnread 保留本地未读，
                            // 仅更新会话名称/类型/最后一条消息。
                            // 同时对单聊按对方昵称去重，避免服务端重复会话污染本地。
                            List<ChatSessionBean> serverList = new ArrayList<>();
                            for (ChatSessionBean s : array) {
                                serverList.add(s);
                            }
                            List<ChatSessionBean> deduped = dedupSingleChat(serverList);
                            for (ChatSessionBean s : deduped) {
                                db.upsertSessionKeepUnread(s);
                            }
                            loadLocalSession();
                        }).start();
                    } else {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), resp.getMsg(), Toast.LENGTH_SHORT).show());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    isRequesting = false;
                    requireActivity().runOnUiThread(() -> binding.swipeRefresh.setRefreshing(false));
                }
            }
        });
    }

    // 收到新消息时外部调用刷新
    public void refreshSessionList() {
        if (binding != null) {
            loadLocalSession();
        }
    }
}