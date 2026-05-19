package com.example.myweixin;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private List<Msg> msgList = new ArrayList<>();
    private EditText inputText;
    private Button send;
    private RecyclerView msgRecyclerView;
    private MsgAdapter adapter;

    private Scanner scannerNet;
    private PrintWriter writerNet;
    private Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        androidInit();
        new Thread(this::socketInit).start();
        send.setOnClickListener(v -> {
            String content = inputText.getText().toString().trim();
            if (!content.isEmpty()) {
                new Thread(() -> sendMsg(content)).start();
                msgList.add(new Msg(content, Msg.TYPE_SENT));
                adapter.notifyItemInserted(msgList.size() - 1);
                msgRecyclerView.scrollToPosition(msgList.size() - 1);
                inputText.setText("");
            }
        });
    }

    private void sendMsg(String content) {
        if (writerNet != null) {
            writerNet.println(content);
        }
    }

    private void socketInit() {
        try {
            socket = new Socket("43.138.32.230", 8666);
            scannerNet = new Scanner(socket.getInputStream());
            writerNet = new PrintWriter(socket.getOutputStream(), true);

            readInit();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void readInit() {
        new Thread(() -> {
            while (scannerNet.hasNextLine()) {
                String info = scannerNet.nextLine();
                runOnUiThread(() -> {
                    msgList.add(new Msg(info, Msg.TYPE_RECEIVED));
                    adapter.notifyItemInserted(msgList.size() - 1);
                    msgRecyclerView.scrollToPosition(msgList.size() - 1);
                });
            }
        }).start();
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