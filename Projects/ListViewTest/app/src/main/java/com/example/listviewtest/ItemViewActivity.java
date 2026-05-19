package com.example.listviewtest;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ItemViewActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mengfufu_image);
        Intent intent = getIntent();
        int imageId = intent.getIntExtra("imageId", 0);
        ImageView imageView = (ImageView) findViewById(R.id.image_id);
        imageView.setImageResource(imageId);
        String name = intent.getStringExtra("name") + "mengfufu";
        Toast.makeText(ItemViewActivity.this, name, Toast.LENGTH_SHORT).show();
    }
}