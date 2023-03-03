package com.example.chatbot;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;


public class splashActivity extends Activity {
ProgressBar progressBar;
    Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        progressBar=  findViewById(R.id.progressbar);
        progressBar.setMax(100);
        progressBar.setProgress(30);


        handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
           progressBar.setProgress(70);

            }
        },3000);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent=new Intent(splashActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
                progressBar.setProgress(80);
//                progressBar.setProgress(100);

            }
        },3000);

    }
}