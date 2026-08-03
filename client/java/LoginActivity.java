package com.example.myweixin_client;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myweixin_client.databinding.ActivityLoginBinding;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class LoginActivity extends AppCompatActivity {
    ActivityLoginBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(LayoutInflater.from(getBaseContext()));
        setContentView(binding.getRoot());

        binding.tvToRegister.setOnClickListener(v->{
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });

        binding.btnLogin.setOnClickListener(v -> {
            String username = binding.etUsername.getText().toString();
            String password = binding.etPwd.getText().toString();
            if(username.isEmpty() || password.isEmpty()){
                Toast.makeText(LoginActivity.this, "账号或者密码不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            if(username.length() >= 15 || password.length() >= 15){
                Toast.makeText(LoginActivity.this, "账号或密码过长", Toast.LENGTH_SHORT).show();
                return;
            }

            HttpUtil.postJson("/login", GsonUtil.toJson(new LoginReq(username, password)), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    new Handler(Looper.getMainLooper()).post(()->{
                        Toast.makeText(LoginActivity.this, "网络请求失败", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try (ResponseBody body = response.body()) {
                        if (body == null) {
                            new Handler(Looper.getMainLooper()).post(() ->
                                    Toast.makeText(LoginActivity.this, "无返回数据", Toast.LENGTH_SHORT).show());
                            return;
                        }
                        if (!response.isSuccessful()) {
                            new Handler(Looper.getMainLooper()).post(() ->
                                    Toast.makeText(LoginActivity.this, "请求失败", Toast.LENGTH_SHORT).show());
                            return;
                        }

                        String json = body.string();
                        BaseHttpResp resp = GsonUtil.GSON.fromJson(json, BaseHttpResp.class);
                        int code = resp.getCode();
                        if (code != 0) {
                            new Handler(Looper.getMainLooper()).post(() ->
                                    Toast.makeText(LoginActivity.this, resp.getMsg(), Toast.LENGTH_SHORT).show()
                            );
                            return;
                        }

                        Object dataObj = resp.getData();
                        LoginReapData loginData = GsonUtil.GSON.fromJson(GsonUtil.toJson(dataObj), LoginReapData.class);
                        int loginUid = loginData.getUid();
                        String loginNick = loginData.getNickname();

                        System.out.println("登录接口返回uid = " + loginUid);
                        SpUtil.saveLoginInfo(loginUid, loginNick);
                        int checkUid = SpUtil.getUid();
                        System.out.println("保存后即时读取uid = " + checkUid);

                        new Handler(Looper.getMainLooper()).post(() -> {
                            Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        });
                    }
                }
            });
        });
    }
}