package com.example.listviewtest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private List<MengFuFu> mengList = new ArrayList<MengFuFu>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        MengFuFuAdapter ad = new MengFuFuAdapter(MainActivity.this, R.layout.mengfufu_item, mengList);
        ListView listView = (ListView) findViewById(R.id.list_view);
        listView.setAdapter(ad);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                MengFuFu mengFuFu = mengList.get(position);
//                setContentView(R.layout.mengfufu_image);
//                ImageView imageView = (ImageView) findViewById(R.id.image_id);
//                imageView.setImageResource(mengFuFu.getImageId());
                MengFuFu mengFuFu = mengList.get(position);
                Intent intent = new Intent(MainActivity.this, ItemViewActivity.class);
                intent.putExtra("imageId", mengFuFu.getImageId());
                intent.putExtra("name", mengFuFu.getName());
                startActivity(intent);
            }
        });
    }
    private void init(){
        for(int i = 0; i < 4; i++){
            MengFuFu furry = new MengFuFu("福瑞", R.drawable.p1);
            mengList.add(furry);
            MengFuFu nuo = new MengFuFu("糯叽", R.drawable.p2);
            mengList.add(nuo);
            MengFuFu sun = new MengFuFu("晴空", R.drawable.p3);
            mengList.add(sun);
            MengFuFu ear = new MengFuFu("兽耳", R.drawable.p4);
            mengList.add(ear);
            MengFuFu black = new MengFuFu("暗黑", R.drawable.p5);
            mengList.add(black);
        }
    }
}