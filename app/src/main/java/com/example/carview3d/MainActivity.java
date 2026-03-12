package com.example.carview3d;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

/**
 * 3D车模展示入口
 * @author Lty
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.app_name);

        Button pictureButton = findViewById(R.id.btnPictureMode);
        Button videoButton = findViewById(R.id.btnVideoMode);

        pictureButton.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, PictureCarActivity.class)));
        videoButton.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, VideoCarActivity.class)));
    }
}
