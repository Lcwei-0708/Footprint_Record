package com.example.project;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

public class WelcomeActivity extends AppCompatActivity
{
    private SharedPreferences userdata;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        userdata = getSharedPreferences("UserData",MODE_PRIVATE);
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                if(userdata.getString("name","未登入").equals("未登入"))
                {
                    Intent intent = new Intent(WelcomeActivity.this,LoginActivity.class);
                    startActivity(intent);
                }
                else
                {
                    Intent intent = new Intent(WelcomeActivity.this,MainActivity.class);
                    startActivity(intent);
                }
                finish();
            }
        }, 1000);
    }
}