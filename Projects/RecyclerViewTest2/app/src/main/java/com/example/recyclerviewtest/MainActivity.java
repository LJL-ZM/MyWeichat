package com.example.recyclerviewtest;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;import androidx.recyclerview.widget.LinearLayoutManager;import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.util.ArrayList;import java.util.List;

public class MainActivity extends AppCompatActivity {
    private List<MengFuFu> mengList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
//        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(
                2, StaggeredGridLayoutManager.VERTICAL);
        MengFuFuAdapter mengFuFuAdapter = new MengFuFuAdapter(mengList);
        recyclerView.setAdapter(mengFuFuAdapter);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
    }

    private void init(){
        for(int i = 0; i < 4; i++){
            MengFuFu furry = new MengFuFu("福瑞mengFuFu,你爱了吗？", R.drawable.p1);
            mengList.add(furry);
            MengFuFu nuo = new MengFuFu("糯叽mengFuFu，我说白了，这谁能不爱？？？", R.drawable.p2);
            mengList.add(nuo);
            MengFuFu sun = new MengFuFu("晴空mengFuFu，也是一个不错的选择！！！！！！！！！", R.drawable.p3);
            mengList.add(sun);
            MengFuFu ear = new MengFuFu("兽耳mengFuFu，没人能拒绝？！？！？！？！？！", R.drawable.p4);
            mengList.add(ear);
            MengFuFu black = new MengFuFu("暗黑mengFuFu，太威严了吧？？？？？？？？？？？？", R.drawable.p5);
            mengList.add(black);
        }
    }
}