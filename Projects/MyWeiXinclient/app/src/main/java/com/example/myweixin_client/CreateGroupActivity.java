package com.example.myweixin_client;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myweixin_client.databinding.ActivityCreateGroupBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class CreateGroupActivity extends AppCompatActivity {
    ActivityCreateGroupBinding binding;
    List<FriendLocalBean> friendList = new ArrayList<>();
    List<FriendLocalBean> selectedList = new ArrayList<>();
    CreateGroupAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.toolbar.setTitle("创建群聊");

        // 加载好友列表
        friendList.addAll(LocalDBHelper.getInstance(this).getAllFriend());
        adapter = new CreateGroupAdapter(friendList);
        binding.rvFriends.setLayoutManager(new LinearLayoutManager(this));
        binding.rvFriends.setAdapter(adapter);

        adapter.setOnItemClickListener((position, bean, isChecked) -> {
            if (isChecked) {
                selectedList.add(bean);
            } else {
                selectedList.remove(bean);
            }
        });

        binding.btnCreate.setOnClickListener(v -> {
            String groupName = binding.etGroupName.getText().toString().trim();
            if (TextUtils.isEmpty(groupName)) {
                Toast.makeText(this, "请输入群名称", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedList.isEmpty()) {
                Toast.makeText(this, "请至少选择一个好友", Toast.LENGTH_SHORT).show();
                return;
            }
            createGroup(groupName);
        });
    }

    private void createGroup(String groupName) {
        // 构造请求体
        StringBuilder membersJson = new StringBuilder("[");
        for (int i = 0; i < selectedList.size(); i++) {
            FriendLocalBean f = selectedList.get(i);
            membersJson.append("{\"uid\":").append(f.getFriendUid())
                    .append(",\"nickname\":\"").append(f.getNickname()).append("\"}");
            if (i < selectedList.size() - 1) membersJson.append(",");
        }
        membersJson.append("]");

        String json = "{\"groupName\":\"" + groupName + "\"," +
                "\"ownerUid\":" + SpUtil.getUid() + "," +
                "\"ownerNick\":\"" + SpUtil.getNickname() + "\"," +
                "\"members\":" + membersJson + "}";

        HttpUtil.postJson("/create_group_chat", json, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(CreateGroupActivity.this, "创建失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String body = response.body().string();
                BaseHttpResp resp = GsonUtil.GSON.fromJson(body, BaseHttpResp.class);
                runOnUiThread(() -> {
                    if (resp.getCode() == 0) {
                        int chatId = ((Number) resp.getData()).intValue();
                        // 创建成功，跳转到聊天页
                        Intent intent = new Intent(CreateGroupActivity.this, ChatActivity.class);
                        intent.putExtra("chat_id", chatId);
                        intent.putExtra("chat_name", groupName);
                        intent.putExtra("chat_type", 2);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(CreateGroupActivity.this, resp.getMsg(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}