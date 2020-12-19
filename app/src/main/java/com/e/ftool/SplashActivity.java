package com.e.ftool;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.EventLogTags;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import java.util.ConcurrentModificationException;
import java.util.Objects;

public class SplashActivity extends AppCompatActivity {

    TextView name,lines,tag;
    ImageView gif;
    NetworkInfo netInfo;
    ConnectivityManager conMgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Objects.requireNonNull(this.getSupportActionBar()).hide();

        gif = findViewById(R.id.gif);
        name = findViewById(R.id.name);
        lines = findViewById(R.id.lines);
        tag = findViewById(R.id.tag);

        conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        netInfo = conMgr.getActiveNetworkInfo();

        Animation anim1 = AnimationUtils.loadAnimation(this,android.R.anim.slide_in_left);
        anim1.setDuration(3000);
        name.startAnimation(anim1);
        lines.startAnimation(anim1);

        Animation anim2 = AnimationUtils.loadAnimation(this,android.R.anim.slide_out_right);
        anim2.setDuration(3000);
        tag.startAnimation(anim2);

        Glide.with(this).load(R.drawable.tractor_gif).into(gif);
    }

    @Override
    protected void onResume() {
        super.onResume();

        new Handler().postDelayed(() -> {

            conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            netInfo = conMgr.getActiveNetworkInfo();

            if(netInfo == null){

                Snackbar.make(tag,"No Internet Connection", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Turn ON Data", view -> {
                            Intent intent = new Intent();
                            intent.setComponent(new ComponentName("com.android.settings",
                                    "com.android.settings.Settings$DataUsageSummaryActivity"));
                            startActivity(intent);
                        })
                        .show();

            }else {

                Intent mainIntent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(mainIntent);
                finish();
            }
        }, 3000);
    }
}