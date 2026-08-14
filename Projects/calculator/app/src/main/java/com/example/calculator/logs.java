package com.example.calculator;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.calculator.databinding.ActivityLogsBinding;

import java.util.ArrayList;
import java.util.List;

// 类名改为大驼峰 LogsActivity
public class logs extends AppCompatActivity {

    public static final String EXTRA_IS_SCIENTIFIC = "is_scientific";
    
    ActivityLogsBinding binding;
    HistoryAdapter adapter;
    List<LogDatabaseHelper.HistoryItem> historyItemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 根据传入的参数设置屏幕方向
        Intent intent = getIntent();
        boolean isScientific = intent.getBooleanExtra(EXTRA_IS_SCIENTIFIC, false);
        if (isScientific) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        
        binding = ActivityLogsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 关键：给 RecyclerView 设置布局管理器
        binding.reHistory.setLayoutManager(new LinearLayoutManager(this));

        // 返回按钮
        binding.ivBack.setOnClickListener(v -> finish());

        // 清空记录按钮
        binding.ivDelete.setOnClickListener(v -> {
            LogDatabaseHelper.clearAll();
            HistoryCache.clear();
            // 使用可变空集合，不用 List.of()
            historyItemList = new ArrayList<>();
            adapter = new HistoryAdapter(historyItemList);
            binding.reHistory.setAdapter(adapter);
            Toast.makeText(this, "已清空所有记录", Toast.LENGTH_SHORT).show();
        });

        // 读取数据，判空防止 NPE
        if (!HistoryCache.isEmpty()) {
            historyItemList = HistoryCache.getHistoryItems();
        } else {
            List<LogDatabaseHelper.HistoryItem> dbList = LogDatabaseHelper.selectAllData();
            // 数据库返回null则新建空集合
            historyItemList = dbList == null ? new ArrayList<>() : dbList;
            HistoryCache.setHistory(historyItemList);
        }

        // 初始化适配器
        adapter = new HistoryAdapter(historyItemList);
        binding.reHistory.setAdapter(adapter);

        // 无数据提示
        if (historyItemList.isEmpty()) {
            Toast.makeText(this, "无历史记录", Toast.LENGTH_SHORT).show();
        }
    }
}