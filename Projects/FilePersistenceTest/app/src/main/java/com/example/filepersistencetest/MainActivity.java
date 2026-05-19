package com.example.filepersistencetest;

import android.content.Context;
import android.os.Bundle;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity {

    private EditText edit = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        edit = (EditText) findViewById(R.id.edit);
        String data = load();
        if(data != null && !data.equals("")){
            edit.setText(data);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        String inputText = edit.getText().toString();
        save(inputText);
    }

    private void save(String inputText) {
        // 修正：去掉 name:，用标准 Java 语法
        try (FileOutputStream out = openFileOutput("data", Context.MODE_PRIVATE);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out))) {

            // 写操作必须放在 try 块里面
            writer.write(inputText);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String load(){
        StringBuilder sb = new StringBuilder();
        try(
                FileInputStream in = openFileInput("data");
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        ){
            String buffer;
            while((buffer = reader.readLine()) != null){
                sb.append(buffer);
            }

        } catch (IOException e) {
            // 捕获异常，直接返回空字符串，不崩溃！
            e.printStackTrace();
            return ""; // 👈 关键在这里
        }
        return sb.toString();
    }
}