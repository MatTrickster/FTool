package com.e.ftool;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.Objects;

public class SplashActivity extends AppCompatActivity {

    TextView name,lines,tag;
    ImageView gif;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Objects.requireNonNull(this.getSupportActionBar()).hide();

        gif = findViewById(R.id.gif);
        name = findViewById(R.id.name);
        lines = findViewById(R.id.lines);
        tag = findViewById(R.id.tag);

        new Handler().postDelayed(() -> {
            Intent mainIntent = new Intent(SplashActivity.this,LoginActivity.class);
            startActivity(mainIntent);
            finish();
        }, 5000);

        Animation anim1 = AnimationUtils.loadAnimation(this,android.R.anim.slide_in_left);
        anim1.setDuration(4000);
        name.startAnimation(anim1);
        lines.startAnimation(anim1);

        Animation anim2 = AnimationUtils.loadAnimation(this,android.R.anim.slide_out_right);
        anim2.setDuration(4000);
        tag.startAnimation(anim2);

        Glide.with(this).load(R.drawable.tractor_gif).into(gif);
    }
}