package com.example.myweixin_client;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myweixin_client.databinding.FragmentMeBinding;

public class MeFragment extends Fragment {
    FragmentMeBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMeBinding.inflate(inflater, container, false);

        //读取本地SP数据
        int uid = SpUtil.getUid();
        String nickName = SpUtil.getNickname();

        //设置昵称、UID
        binding.tvNick.setText(nickName);
        binding.tvUid.setText("UID：" + uid);

        //头像：昵称首字符，空值容错
        if (!nickName.isEmpty()) {
            binding.tvAvatar.setText(String.valueOf(nickName.charAt(0)));
        } else {
            binding.tvAvatar.setText("?");
        }

        //退出登录点击事件
        binding.btnLogout.setOnClickListener(v -> {
            //清空本地数据库（会话、消息、好友等）
            LocalDBHelper.getInstance(requireContext()).clearAll();
            //清除登录信息
            SpUtil.clearLogin();
            //断开Socket长连接
            SocketClientManager.getInstance().disconnect();
            //跳转登录页面
            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });

        return binding.getRoot();
    }
}