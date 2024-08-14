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
import android.widget.TimePicker;
import android.widget.Toast;
import com.example.project.databinding.ActivityEditlocationBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import org.jetbrains.annotations.NotNull;
import java.util.Calendar;

public class EditlocationActivity extends AppCompatActivity
{
    ActivityEditlocationBinding binding;
    String datetime, date, time, id, stay, address, remark;
    int year, month, day, hour, minute;
    Calendar calendar;
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    SharedPreferences userdata;
    Internet internet = new Internet();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityEditlocationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userdata = getSharedPreferences("UserData",MODE_PRIVATE);
        id = userdata.getString("id","");
        Intent intent = getIntent();
        address = intent.getStringExtra("address");
        datetime = intent.getStringExtra("datetime");
        stay = intent.getStringExtra("stay");
        remark = intent.getStringExtra("remark");

        GetData();
        String[] dt = datetime.split(" ");
        String[] d = dt[0].split("-");
        String[] t = dt[1].split(":");
        year = Integer.parseInt(d[0]);
        month = Integer.parseInt(d[1])-1;
        day = Integer.parseInt(d[2]);
        hour = Integer.parseInt(t[0]);
        minute = Integer.parseInt(t[1]);

        binding.editlocationTextDate.setText(dt[0]);
        binding.editlocationTextTime.setText(dt[1]);
        binding.editlocationInputMinute.setText(stay);
        binding.editlocationInputRemark.setText(remark);
        binding.editlocationBtnDate.setOnClickListener(click);
        binding.editlocationBtnTime.setOnClickListener(click);
        binding.editlocationBtnCancel.setOnClickListener(click);
        binding.editlocationBtnSave.setOnClickListener(click);

        //設定ToolBar
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("編輯足跡");
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
    }

    public View.OnClickListener click = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if(v == binding.editlocationBtnDate)
            {
                SetTime();
                new DatePickerDialog(EditlocationActivity.this, new DatePickerDialog.OnDateSetListener()
                {
                    @Override
                    public void onDateSet(DatePicker view, int chooseyear, int choosemonth, int chooseday)
                    {
                        year = chooseyear;
                        month = choosemonth;
                        day = chooseday;
                        SetTime();
                        date = android.text.format.DateFormat.format("yyyy-MM-dd", calendar.getTimeInMillis()).toString();
                        binding.editlocationTextDate.setText(date);
                    }
                }, year, month, day).show();
            }
            else if(v == binding.editlocationBtnTime)
            {
                SetTime();
                new TimePickerDialog(EditlocationActivity.this, new TimePickerDialog.OnTimeSetListener()
                {
                    @Override
                    public void onTimeSet(TimePicker view, int choosehour, int chooseminute)
                    {
                        hour = choosehour;
                        minute = chooseminute;
                        SetTime();
                        time = android.text.format.DateFormat.format("HH:mm", calendar.getTimeInMillis()).toString();
                        binding.editlocationTextTime.setText(time);
                    }
                }, hour, minute, false).show();
            }
            else if(v == binding.editlocationBtnCancel)
            {
                finish();
            }
            else if(v == binding.editlocationBtnSave)
            {
                if(internet.CheckInternet(EditlocationActivity.this))
                {
                    stay = binding.editlocationInputMinute.getText().toString();
                    remark = binding.editlocationInputRemark.getText().toString();
                    date = binding.editlocationTextDate.getText().toString();
                    time = binding.editlocationTextTime.getText().toString();
                    if(stay.isEmpty())
                    {
                        Toast.makeText(getApplicationContext(),"請輸入停留時間",Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        if(remark.isEmpty())
                        {
                            remark = "無";
                        }
                        DeleteData(getIntent().getStringExtra("datetime"));
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
        calendar.set(Calendar.YEAR,year);
        calendar.set(Calendar.MONTH,month);
        calendar.set(Calendar.DAY_OF_MONTH,day);
        calendar.set(Calendar.HOUR_OF_DAY,hour);
        calendar.set(Calendar.MINUTE,minute);
        calendar.set(Calendar.SECOND,0);
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
                        Toast.makeText(getApplicationContext(),"更改成功",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(EditlocationActivity.this,MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Toast.makeText(getApplicationContext(),"更改失敗",Toast.LENGTH_SHORT).show();
                        Log.d("QQQ",e.getMessage());
                    }
                });
    }

    //刪除舊資料
    public void DeleteData(String datetime)
    {
        firebaseFirestore.collection("UserData").document(id)
                .collection("Location").document(datetime).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>()
                {
                    @Override
                    public void onSuccess(Void unused)
                    {
                        String datetime = date + " " + time;
                        Timestamp timestamp = new Timestamp(calendar.getTime());
                        AddData(address, timestamp, stay, remark, datetime);
                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e)
                    {
                        Toast.makeText(getApplicationContext(),"更改失敗",Toast.LENGTH_SHORT).show();
                        Log.d("QQQ",e.getMessage());
                    }
                });
    }

    //取得資料
    public void GetData()
    {
        firebaseFirestore.collection("UserData")
                .document(id)
                .collection("Location")
                .document(datetime)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
                {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot)
                    {
                        calendar = Calendar.getInstance();
                        calendar.setTime(documentSnapshot.getTimestamp("datetime").toDate());
                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Toast.makeText(getApplicationContext(),"查詢失敗",Toast.LENGTH_SHORT).show();
                    }
                });
    }
}