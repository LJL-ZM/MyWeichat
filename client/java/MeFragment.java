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

        int uid = SpUtil.getUid();
        String nickName = SpUtil.getNickname();

        binding.tvNick.setText(nickName);
        binding.tvUid.setText("UID：" + uid);

        if (!nickName.isEmpty()) {
            binding.tvAvatar.setText(String.valueOf(nickName.charAt(0)));
        } else {
            binding.tvAvatar.setText("?");
        }

        binding.btnLogout.setOnClickListener(v -> {
            LocalDBHelper.getInstance(requireContext()).clearAll();
            SpUtil.clearLogin();
            SocketClientManager.getInstance().disconnect();
            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });

        return binding.getRoot();
    }
}