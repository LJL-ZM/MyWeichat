package com.example.myweixin_client;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myweixin_client.databinding.ActivitySearchFriendBinding;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class SearchActivity extends AppCompatActivity {
    ActivitySearchFriendBinding binding;
    SearchFriendAdapter adapter;
    List<SearchItemBean> list = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchFriendBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        //initList();
        adapter = new SearchFriendAdapter(list);
        // 设置监听 适配你的适配器
        adapter.setOnItemListener(new SearchItemClickListener());
        binding.rvSearchResult.setAdapter(adapter);
        binding.rvSearchResult.setLayoutManager(new LinearLayoutManager(this));

        binding.btnSearch.setOnClickListener(v -> onClickSearch());
    }

    private void initList() {
        list.add(new SearchItemBean("我的好友"));
        list.add(new SearchItemBean(SearchItemBean.TYPE_MY_FRIEND, 1, "张三"));
        list.add(new SearchItemBean(SearchItemBean.TYPE_MY_FRIEND, 2, "李四"));
        list.add(new SearchItemBean(SearchItemBean.TYPE_MY_FRIEND, 3, "王五"));
        //list.add(0, new SearchItemBean(SearchItemBean.TYPE_NEW_FRIEND, 999, "测试用户"));
    }

    private void onClickSearch() {
        String nameOrId = binding.etSearch.getText().toString().trim();
        if (nameOrId.isEmpty()) {
            Toast.makeText(this, "请输入搜索内容", Toast.LENGTH_SHORT).show();
            return;
        }

        NewFriendQueryReq req = new NewFriendQueryReq(nameOrId);
        String json = GsonUtil.toJson(req);

        HttpUtil.postJson("/searchNewFriend", json, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(SearchActivity.this, "网络错误", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try (ResponseBody body = response.body()) {
                    if (body == null || !response.isSuccessful()) {
                        runOnUiThread(() -> Toast.makeText(SearchActivity.this, "请求失败", Toast.LENGTH_SHORT).show());
                        return;
                    }
                    String jsonStr = body.string();
                    BaseHttpResp resp = GsonUtil.fromJson(jsonStr, BaseHttpResp.class);
                    if (resp.getCode() != 0) {
                        runOnUiThread(() -> Toast.makeText(SearchActivity.this, resp.getMsg(), Toast.LENGTH_SHORT).show());
                        return;
                    }

                    Object o = resp.getData();
                    NewFriendQueryRespData data = GsonUtil.fromJson(GsonUtil.toJson(o), NewFriendQueryRespData.class);
                    List<NewFriendQueryRespData.userData> resultList = data.getList();

                    // 先删除旧数据 统计数量
                    int delCount = 0;
                    Iterator<SearchItemBean> iterator = list.iterator();
                    while (iterator.hasNext()) {
                        SearchItemBean item = iterator.next();
                        if (item.itemType == SearchItemBean.TYPE_NEW_FRIEND) {
                            iterator.remove();
                            delCount++;
                        }
                    }

                    // 添加新数据
                    int selfUid = SpUtil.getUid();
                    int added = 0;
                    for (NewFriendQueryRespData.userData user : resultList) {
                        // 修复：过滤掉自己，避免给自己发送好友申请
                        if (user.uid == selfUid) {
                            continue;
                        }
                        list.add(0, new SearchItemBean(SearchItemBean.TYPE_NEW_FRIEND, user.uid, user.nickName));
                        added++;
                    }
                    final int removeSize = delCount;
                    final int addSize = added;

                    runOnUiThread(() -> {
                        if (removeSize > 0) {
                            adapter.notifyItemRangeRemoved(0, removeSize);
                        }
                        if (addSize > 0) {
                            adapter.notifyItemRangeInserted(0, addSize);
                        }
                    });

                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(SearchActivity.this, "数据解析异常", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    // 打招呼弹窗
    private void showSayHelloDialog(int uid) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_ay_hello, null);
        EditText etMsg = view.findViewById(R.id.et_say_msg);
        etMsg.setText("你好，加个好友吧~");

        new AlertDialog.Builder(this)
                .setTitle("给新朋友打个招呼")
                .setView(view)
                .setPositiveButton("发送请求", (dialog, which) -> {
                    String content = etMsg.getText().toString().trim();
                    sendAddFriendRequest(uid, content);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    // 发送好友请求完整逻辑
    private void sendAddFriendRequest(int friendId, String msg) {
        AddFriendReq req = new AddFriendReq(SpUtil.getUid(), friendId, msg, SpUtil.getNickname());
        String json = GsonUtil.toJson(req);

        HttpUtil.postJson("/addFriend", json, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(SearchActivity.this, "发送失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try (ResponseBody body = response.body()) {
                    if (body == null) return;
                    BaseHttpResp resp = GsonUtil.fromJson(body.string(), BaseHttpResp.class);
                    runOnUiThread(() -> {
                        if (resp.getCode() == 0) {
                            Toast.makeText(SearchActivity.this, "好友请求发送成功", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SearchActivity.this, resp.getMsg(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    // 内部监听类 无static 直接调用外部方法
    class SearchItemClickListener implements SearchFriendAdapter.OnItemClickListener {
        @Override
        public void onMsgClick(SearchItemBean bean) {
            showSayHelloDialog(bean.uid);
        }

        @Override
        public void onFriendClick(SearchItemBean bean) {

        }
    }
}