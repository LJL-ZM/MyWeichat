package com.example.myweixin_client;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.myweixin_client.databinding.ActivityFriendRequestBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class FriendRequestActivity extends AppCompatActivity {

    ActivityFriendRequestBinding binding;
    List<FriendRequestLocalBean> requestList;
    FriendRequestAdapter adapter;
    LocalDBHelper dbHelper;
    private int selfUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFriendRequestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = LocalDBHelper.getInstance(this);
        selfUid = SpUtil.getUid();
        requestList = dbHelper.getAllRequest();

        adapter = new FriendRequestAdapter(requestList);
        binding.rvRequest.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRequest.setAdapter(adapter);

        //下拉刷新监听
        binding.swipeRefresh.setOnRefreshListener(this::pullRefreshRequest);

        adapter.setOnRequestListener(new FriendRequestAdapter.OnRequestListener() {
            @Override
            public void onAgree(int position, FriendRequestLocalBean bean) {
                int myUid = SpUtil.getUid();
                ReplyReq handleBean = ReplyReq.getAgreeInstance(bean.requestId, myUid);
                System.out.println("同意申请，发送uid = " + myUid);
                HttpUtil.postJson("/agree_friend", GsonUtil.toJson(handleBean), new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        runOnUiThread(() -> Toast.makeText(FriendRequestActivity.this, "网络异常", Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        String respStr;
                        try {
                            respStr = response.body().string();
                            BaseHttpResp resp = GsonUtil.GSON.fromJson(respStr, BaseHttpResp.class);
                            runOnUiThread(() -> {
                                try {
                                    if (resp.getCode() == 0) {
                                        dbHelper.updateStatus(bean.requestId, ReplyReq.AGREE);
                                        if (!dbHelper.isFriend(bean.fromUid)) {
                                            FriendLocalBean friend = new FriendLocalBean(bean.fromUid, bean.fromNickname);
                                            friend.setRemark("");
                                            dbHelper.addFriend(friend);
                                        }
                                        bean.status = ReplyReq.AGREE;
                                        adapter.notifyItemChanged(position);
                                        Toast.makeText(FriendRequestActivity.this, "已同意" + bean.fromNickname + "的申请", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(FriendRequestActivity.this, resp.getMsg(), Toast.LENGTH_SHORT).show();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(FriendRequestActivity.this, "本地处理异常：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        } finally {
                            response.close();
                        }
                    }
                });
            }

            @Override
            public void onRefuse(int position, FriendRequestLocalBean bean) {
                int myUid = SpUtil.getUid();
                ReplyReq handleBean = ReplyReq.getRefuseInstance(bean.requestId, myUid);
                System.out.println("拒绝申请，发送uid = " + myUid);
                HttpUtil.postJson("/refuse_friend", GsonUtil.toJson(handleBean), new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        runOnUiThread(() -> Toast.makeText(FriendRequestActivity.this, "网络异常", Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        String respStr;
                        try {
                            respStr = response.body().string();
                            BaseHttpResp resp = GsonUtil.GSON.fromJson(respStr, BaseHttpResp.class);
                            runOnUiThread(() -> {
                                try {
                                    if (resp.getCode() == 0) {
                                        dbHelper.updateStatus(bean.requestId, ReplyReq.REFUSE);
                                        bean.status = ReplyReq.REFUSE;
                                        adapter.notifyItemChanged(position);
                                        Toast.makeText(FriendRequestActivity.this, "已拒绝" + bean.fromNickname + "的申请", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(FriendRequestActivity.this, resp.getMsg(), Toast.LENGTH_SHORT).show();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(FriendRequestActivity.this, "本地处理异常：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        } finally {
                            response.close();
                        }
                    }
                });
            }
        });

        binding.ivBack.setOnClickListener(v -> finish());
    }

    //下拉刷新：同步未处理好友申请【修复版本，禁止全清】
    private void pullRefreshRequest() {
        String reqJson = GsonUtil.toJson(new GetPendingReq(selfUid));
        HttpUtil.postJson("/getPendingRequest", reqJson, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    binding.swipeRefresh.setRefreshing(false);
                    Toast.makeText(FriendRequestActivity.this, "刷新失败", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String body = response.body().string();
                BaseHttpResp resp = GsonUtil.GSON.fromJson(body, BaseHttpResp.class);

                new Thread(() -> {
                    try {
                        List<Integer> serverIdList = new ArrayList<>();
                        if (resp != null && resp.getCode() == 0 && resp.getData() != null) {
                            FriendRequestRemoteBean[] array = GsonUtil.GSON.fromJson(
                                    GsonUtil.GSON.toJson(resp.getData()), FriendRequestRemoteBean[].class);
                            // 1. 更新/新增后端下发申请
                            if (array != null) {
                                for (FriendRequestRemoteBean item : array) {
                                    serverIdList.add(item.requestId);
                                    FriendRequestLocalBean localBean = new FriendRequestLocalBean();
                                    localBean.requestId = item.requestId;
                                    localBean.fromUid = item.fromUid;
                                    localBean.fromNickname = item.fromNickname;
                                    localBean.reqMsg = item.reqMsg;
                                    localBean.status = 0;
                                    dbHelper.addRequest(localBean);
                                }
                            }
                            // 2. 将本地存在、后端消失的待处理申请标记失效，不删除历史
                            dbHelper.invalidateLostRequest(serverIdList);
                        }

                        runOnUiThread(() -> {
                            binding.swipeRefresh.setRefreshing(false);
                            initData();
                            if (resp != null && resp.getCode() == 0) {
                                Toast.makeText(FriendRequestActivity.this, "刷新完成", Toast.LENGTH_SHORT).show();
                            } else if (resp != null) {
                                Toast.makeText(FriendRequestActivity.this, resp.getMsg(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            binding.swipeRefresh.setRefreshing(false);
                            Toast.makeText(FriendRequestActivity.this, "刷新异常", Toast.LENGTH_SHORT).show();
                        });
                    }
                }).start();
            }
        });
    }

    private void initData() {
        requestList.clear();
        requestList.addAll(dbHelper.getAllRequest());
        adapter.notifyDataSetChanged();
    }

    public static void showFriendApplyNotification(Context context, String nickName) {
        String channelId = "friend_apply_channel";
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "好友申请通知",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(context, FriendRequestActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                1001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("收到好友申请")
                .setContentText(nickName + "想要添加你为好友")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    public static void showAgreeApplyNotification(Context context, String nickName) {
        String channelId = "friend_apply_channel";
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "好友申请通知",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(context, FriendRequestActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                2002,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("好友申请已通过")
                .setContentText(nickName + "同意了你的好友申请！")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }
}