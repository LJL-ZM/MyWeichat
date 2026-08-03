package com.example.myweixin_client;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myweixin_client.databinding.ActivityRegisterBinding;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class RegisterActivity extends AppCompatActivity {
    ActivityRegisterBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        binding.btnRegister.setOnClickListener(v -> register());
        binding.tvBackLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void register() {
        String username = binding.etUsername.getText().toString().trim();
        String nick = binding.etNick.getText().toString().trim();
        String pwd = binding.etPwd.getText().toString().trim();
        if (username.isEmpty() || nick.isEmpty() || pwd.isEmpty()) {
            Toast.makeText(this, "账号、昵称、密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        RegisterReq req = new RegisterReq(username, pwd, nick);
        String json = GsonUtil.toJson(req);
        HttpUtil.postJson("/register", json, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(RegisterActivity.this, "网络请求失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody body = response.body()) {
                    if (body == null || !response.isSuccessful()) {
                        new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(RegisterActivity.this, "服务异常", Toast.LENGTH_SHORT).show());
                        return;
                    }
                    String respJson = body.string();
                    BaseHttpResp resp = GsonUtil.GSON.fromJson(respJson, BaseHttpResp.class);
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (resp.getCode() == 0) {
                            Toast.makeText(RegisterActivity.this, "注册成功，请登录", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                            finish();
                        } else {
                            Toast.makeText(RegisterActivity.this, resp.getMsg(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }
}