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

import com.example.myweixin_client.databinding.FragmentFriendBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class FriendFragment extends Fragment {
    FragmentFriendBinding binding;
    List<FriendBean> friendList = new ArrayList<>();
    FriendListAdapter adapter;
    LocalDBHelper db;
    private int selfUid;
    private boolean isRequesting = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFriendBinding.inflate(inflater, container, false);
        db = LocalDBHelper.getInstance(getContext());
        selfUid = SpUtil.getUid();

        adapter = new FriendListAdapter(friendList);
        binding.rvFriend.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.rvFriend.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(this::pullRefreshFriend);

        adapter.setOnItemClickListener((position, bean) -> {
            if (bean.type == 0) {
                Intent intent = new Intent(getContext(), FriendRequestActivity.class);
                startActivity(intent);
            } else if (bean.type == 2) {
                // 发起群聊 → 跳转创建群聊页
                Intent intent = new Intent(getContext(), CreateGroupActivity.class);
                startActivity(intent);
            } else {
                // 点击好友 → 创建单聊 → 跳转聊天页
                createSingleChat(bean.getUid(), bean.getName());
            }
        });

        loadFriendData();
        return binding.getRoot();
    }

    private void loadFriendData() {
        new Thread(() -> {
            List<FriendLocalBean> localBeanList = db.getAllFriend();
            List<FriendBean> convertList = new ArrayList<>();
            convertList.add(new FriendBean(0, 0, "新的朋友"));
            convertList.add(new FriendBean(2, 0, "发起群聊"));
            for (FriendLocalBean local : localBeanList) {
                convertList.add(new FriendBean(1, local.getFriendUid(), local.getNickname()));
            }

            requireActivity().runOnUiThread(() -> {
                friendList.clear();
                friendList.addAll(convertList);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }

    private void pullRefreshFriend() {
        if (isRequesting) {
            return;
        }
        isRequesting = true;
        String json = GsonUtil.toJson(new GetFriendReq(selfUid));
        HttpUtil.postJson("/getAllFriend", json, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                isRequesting = false;
                requireActivity().runOnUiThread(() -> {
                    binding.swipeRefresh.setRefreshing(false);
                    Toast.makeText(getContext(), "网络刷新失败", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    String body = response.body().string();
                    BaseHttpResp resp = GsonUtil.GSON.fromJson(body, BaseHttpResp.class);
                    if (resp.getCode() == 0) {
                        FriendRemoteBean[] remoteArr = GsonUtil.GSON.fromJson(GsonUtil.GSON.toJson(resp.getData()), FriendRemoteBean[].class);
                        new Thread(() -> {
                            db.clearAllFriend();
                            for (FriendRemoteBean remote : remoteArr) {
                                FriendLocalBean local = new FriendLocalBean(remote.friendUid, remote.nickname);
                                local.setRemark("");
                                db.addFriend(local);
                            }
                            requireActivity().runOnUiThread(() -> {
                                loadFriendData();
                                Toast.makeText(getContext(), "好友列表已同步", Toast.LENGTH_SHORT).show();
                            });
                        }).start();
                    } else {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), resp.getMsg(), Toast.LENGTH_SHORT).show());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "数据解析异常", Toast.LENGTH_SHORT).show());
                } finally {
                    isRequesting = false;
                    requireActivity().runOnUiThread(() -> binding.swipeRefresh.setRefreshing(false));
                }
            }
        });
    }

    // 创建单聊会话并跳转聊天页
    private void createSingleChat(int friendUid, String friendNick) {
        if (friendUid <= 0) {
            requireActivity().runOnUiThread(()->
                    Toast.makeText(getContext(), "好友UID无效", Toast.LENGTH_SHORT).show());
            return;
        }
        // ✅ 关键：创建final临时变量给lambda使用，规避编译报错
        final int targetUid = selfUid;
        final String myNick = SpUtil.getNickname() == null ? "" : SpUtil.getNickname();
        final String targetNick = friendNick == null ? "" : friendNick;

        String json = "{\"uidA\":" + targetUid + ",\"uidB\":" + friendUid +
                ",\"nickA\":\"" + myNick + "\",\"nickB\":\"" + targetNick + "\"}";

        HttpUtil.postJson("/create_single_chat", json, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "创建会话失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String body = response.body().string();
                BaseHttpResp resp = GsonUtil.GSON.fromJson(body, BaseHttpResp.class);
                requireActivity().runOnUiThread(() -> {
                    if (resp.getCode() == 0) {
                        int chatId = ((Number) resp.getData()).intValue();
                        Intent intent = new Intent(getContext(), ChatActivity.class);
                        intent.putExtra("chat_id", chatId);
                        intent.putExtra("chat_name", targetNick);
                        intent.putExtra("chat_type", 1);
                        startActivity(intent);
                    } else {
                        Toast.makeText(getContext(), resp.getMsg(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}