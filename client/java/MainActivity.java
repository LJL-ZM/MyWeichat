package com.example.myweixin_client;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.PermissionChecker;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.myweixin_client.databinding.ActivityMainBinding;
import com.example.myweixin_client.databinding.ActivityRegisterBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding = null;
    private ChatFragment chatFragment;
    private FriendFragment friendFragment;
    private MeFragment meFragment;
    private Fragment currentFragment;
    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                } else {
                }
            });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SpUtil.init(this);
        if(!SpUtil.isLogin()){
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        SocketClientManager.getInstance().connect(this, SpUtil.getUid(), "YOUR_SERVER_IP", 8085);
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        binding.ivSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });
        initFragments();
        binding.bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_chat) {
                switchFragment(chatFragment);
                binding.toolbar.setTitle("聊天");
            } else if (id == R.id.nav_friend) {
                switchFragment(friendFragment);
                binding.toolbar.setTitle("好友");
            } else if(id == R.id.nav_me) {
                switchFragment(meFragment);
                binding.toolbar.setTitle("我的");
            } else {
                Toast.makeText(this, "开发中", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
        binding.bottomNav.setSelectedItemId(R.id.nav_chat);

        requestNotificationPermission();
    }
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            int state = PermissionChecker.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS);
            if (state != PermissionChecker.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
    private void initFragments(){
        chatFragment = new ChatFragment();
        friendFragment = new FriendFragment();
        meFragment = new MeFragment();
        binding.toolbar.setTitle("聊天");
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fl_container, chatFragment)
                .add(R.id.fl_container, friendFragment)
                .add(R.id.fl_container, meFragment)
                .hide(friendFragment)
                .hide(meFragment)
                .show(chatFragment)
                .commit();
        currentFragment = chatFragment;
    }
    private void switchFragment(Fragment target) {
        if(target != currentFragment) {
            getSupportFragmentManager().beginTransaction().hide(currentFragment).show(target).commit();
            currentFragment = target;
        }
    }

    public void refreshChatList(){
        if(chatFragment != null){
            chatFragment.refreshSessionList();
        }
    }
}