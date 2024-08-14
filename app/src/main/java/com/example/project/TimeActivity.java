package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import com.example.project.databinding.ActivityTimeBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Calendar;

public class TimeActivity extends AppCompatActivity
{
    private ActivityTimeBinding binding;
    private TextView toolbar_add;
    private String datetime, date, time, id, stay, address, remark;
    private int year, month, day, hour, minute;
    private Calendar calendar;
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private SharedPreferences userdata;
    private Internet internet = new Internet();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityTimeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        userdata = getSharedPreferences("UserData",MODE_PRIVATE);
        id = userdata.getString("id","");
        Intent intent = getIntent();
        address = intent.getStringExtra("address");

        //設定ToolBar
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("設定時間");
        //設定返回鍵
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_back));
        toolbar.setNavigationOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });

        toolbar_add = findViewById(R.id.toolbar_add);
        toolbar_add.setVisibility(View.VISIBLE);
        toolbar_add.setOnClickListener(click); //新增
        binding.timeBtnDate.setOnClickListener(click); //選擇日期按鈕
        binding.timeBtnTime.setOnClickListener(click); //選擇時間按鈕
    }

    public View.OnClickListener click = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if(v == binding.timeBtnDate)
            {
                SetTime();
                new DatePickerDialog(TimeActivity.this, new DatePickerDialog.OnDateSetListener()
                {
                    @Override
                    public void onDateSet(DatePicker view, int chooseyear, int choosemonth, int chooseday)
                    {
                        year = chooseyear;
                        month = choosemonth;
                        day = chooseday;
                        SetTime();
                        date = android.text.format.DateFormat.format("yyyy-MM-dd", calendar.getTimeInMillis()).toString();
                        binding.timeTextDate.setText(date);
                    }
                }, year, month, day).show();
            }
            else if(v == binding.timeBtnTime)
            {
                SetTime();
                new TimePickerDialog(TimeActivity.this, new TimePickerDialog.OnTimeSetListener()
                {
                    @Override
                    public void onTimeSet(TimePicker view, int choosehour, int chooseminute)
                    {
                        hour = choosehour;
                        minute = chooseminute;
                        SetTime();
                        time = android.text.format.DateFormat.format("HH:mm", calendar.getTimeInMillis()).toString();
                        binding.timeTextTime.setText(time);
                    }
                }, hour, minute, false).show();
            }
            else if(v == toolbar_add)
            {
                if(internet.CheckInternet(TimeActivity.this))
                {
                    stay = binding.timeInputMinute.getText().toString();
                    remark = binding.timeInputRemark.getText().toString();
                    if(binding.timeTextDate.getText().toString().isEmpty())
                    {
                        Toast.makeText(getApplicationContext(),"請選擇日期",Toast.LENGTH_SHORT).show();
                    }
                    else if(binding.timeTextTime.getText().toString().isEmpty())
                    {
                        Toast.makeText(getApplicationContext(),"請選擇時間",Toast.LENGTH_SHORT).show();
                    }
                    else if(stay.isEmpty())
                    {
                        Toast.makeText(getApplicationContext(),"請輸入停留時間",Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        datetime = date + " " + time;
                        Timestamp timestamp = new Timestamp(calendar.getTime());
                        if(remark.isEmpty())
                        {
                            remark = "無";
                        }
                        AddData(address, timestamp, stay, remark, datetime);
                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"請確認網路連線",Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    //設定時間
    public void SetTime()
    {
        calendar = Calendar.getInstance();
        if(year == 0 && month == 0 && day == 0)
        {
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            day = calendar.get(Calendar.DAY_OF_MONTH);
        }
        else
        {
            calendar.set(Calendar.YEAR,year);
            calendar.set(Calendar.MONTH,month);
            calendar.set(Calendar.DAY_OF_MONTH,day);
            calendar.set(Calendar.HOUR_OF_DAY,hour);
            calendar.set(Calendar.MINUTE,minute);
            calendar.set(Calendar.SECOND,0);
        }

        if(hour == 0 && minute == 0)
        {
            hour = calendar.get(Calendar.HOUR_OF_DAY);
            minute = calendar.get(Calendar.MINUTE);
        }
        else
        {
            calendar.set(Calendar.YEAR,year);
            calendar.set(Calendar.MONTH,month);
            calendar.set(Calendar.DAY_OF_MONTH,day);
            calendar.set(Calendar.HOUR_OF_DAY,hour);
            calendar.set(Calendar.MINUTE,minute);
            calendar.set(Calendar.SECOND,0);
        }
    }

    //新增資料
    public void AddData(String address, Timestamp time, String stay, String remark, String datetime)
    {
        DataField.LocationData data = new DataField.LocationData();
        data.address = address;
        data.datetime = time;
        data.stay = stay;
        data.remark = remark;
        firebaseFirestore.collection("UserData").document(id)
                .collection("Location").document(datetime).set(data)
                .addOnSuccessListener(new OnSuccessListener()
                {
                    @Override
                    public void onSuccess(Object o)
                    {
                        Toast.makeText(getApplicationContext(),"新增成功",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(TimeActivity.this,MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Toast.makeText(getApplicationContext(),"新增失敗",Toast.LENGTH_SHORT).show();
                        Log.d("QQQ",e.getMessage());
                    }
                });
    }
}