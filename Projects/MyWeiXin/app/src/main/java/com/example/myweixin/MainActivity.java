package com.example.myweixin;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<Msg> msgList = new ArrayList<>();
    private EditText inputText;
    private Button send;
    private RecyclerView msgRecyclerView;
    private MsgAdapter adapter;

    private ChatService chatService;
    private boolean isServiceBound = false;

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            try {
                ChatService.ChatBinder binder = (ChatService.ChatBinder) service;
                chatService = binder.getService();
                isServiceBound = true;

                chatService.setOnMessageReceivedListener(new ChatService.OnMessageReceivedListener() {
                    @Override
                    public void onMessageReceived(String message) {
                        msgList.add(new Msg(message, Msg.TYPE_RECEIVED));
                        adapter.notifyItemInserted(msgList.size() - 1);
                        msgRecyclerView.scrollToPosition(msgList.size() - 1);
                    }
                });

                chatService.setOnConnectionStatusListener(new ChatService.OnConnectionStatusListener() {
                    @Override
                    public void onConnected() {
                        Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onDisconnected() {
                        Toast.makeText(MainActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onReconnecting() {
                        Toast.makeText(MainActivity.this, "Reconnecting...", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isServiceBound = false;
            chatService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        androidInit();

        send.setOnClickListener(v -> {
            String content = inputText.getText().toString().trim();
            if (!content.isEmpty()) {
                if (isServiceBound && chatService != null) {
                    if (chatService.isConnected()) {
                        chatService.sendMessage(content);
                    } else {
                        Toast.makeText(MainActivity.this, "Not connected", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Service not ready", Toast.LENGTH_SHORT).show();
                }
                msgList.add(new Msg(content, Msg.TYPE_SENT));
                adapter.notifyItemInserted(msgList.size() - 1);
                msgRecyclerView.scrollToPosition(msgList.size() - 1);
                inputText.setText("");
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    Intent intent = new Intent(MainActivity.this, ChatService.class);
                    bindService(intent, connection, Context.BIND_AUTO_CREATE);
                    startService(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Failed to start service", Toast.LENGTH_SHORT).show();
                }
            }
        }, 1000);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isServiceBound) {
            try {
                unbindService(connection);
            } catch (Exception e) {
                e.printStackTrace();
            }
            isServiceBound = false;
        }
    }

    private void androidInit() {
        inputText = findViewById(R.id.input_edit);
        send = findViewById(R.id.send);
        msgRecyclerView = findViewById(R.id.msg_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        adapter = new MsgAdapter(msgList);
        msgRecyclerView.setLayoutManager(layoutManager);
        msgRecyclerView.setAdapter(adapter);
    }
}
