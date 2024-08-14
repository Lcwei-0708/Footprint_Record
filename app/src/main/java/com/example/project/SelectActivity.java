package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;
import com.example.project.databinding.ActivitySelectBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

public class SelectActivity extends AppCompatActivity
{
    private ActivitySelectBinding binding;
    private SharedPreferences userdata;
    private String id;
    private Calendar choose;
    private ArrayList<String> address = new ArrayList<>();
    private ArrayList<String> datetime = new ArrayList<>();
    private ArrayList<String> stay = new ArrayList<>();
    private ArrayList<String> remark = new ArrayList<>();
    private int year, month, day;
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivitySelectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        userdata = getSharedPreferences("UserData", MODE_PRIVATE);
        id = userdata.getString("id", "");
        //設定ToolBar
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("查詢足跡");
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

        binding.selectBtnDate.setOnClickListener(click);
        binding.selectBtnSelect.setOnClickListener(click);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        try
        {
            firebaseAuth.getCurrentUser().reload()
                    .addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<Void> task)
                        {
                            if(firebaseAuth.getCurrentUser() == null)
                            {
                                userdata.edit().clear().commit();
                                Intent intent = new Intent(SelectActivity.this,LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                finish();
                            }
                        }
                    });
        }
        catch(Exception e)
        {
            userdata.edit().clear().commit();
            Intent intent = new Intent(SelectActivity.this,LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }

    //取得資料
    public void GetData(Timestamp min, Timestamp max)
    {
        firebaseFirestore.collection("UserData")
                .document(id)
                .collection("Location")
                .whereGreaterThanOrEqualTo("datetime", min)
                .whereLessThanOrEqualTo("datetime",max)
                .orderBy("datetime")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>()
                {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task)
                    {
                        if(task.isSuccessful())
                        {
                            if(task.getResult().size() > 0)
                            {
                                for(QueryDocumentSnapshot doc:task.getResult())
                                {
                                    Map<String ,Object> m = doc.getData();
                                    Calendar timedata = Calendar.getInstance();
                                    timedata.setTime(doc.getTimestamp("datetime").toDate());
                                    String DateTime = android.text.format.DateFormat.format("yyyy-MM-dd HH:mm", timedata.getTimeInMillis()).toString();
                                    datetime.add(DateTime);
                                    address.add(m.get("address").toString());
                                    stay.add(m.get("stay").toString());
                                    remark.add(m.get("remark").toString());
                                    SetAdapter();
                                }
                            }
                            else
                            {
                                Toast.makeText(getApplicationContext(),binding.selectBtnDate.getText().toString() + "沒有足跡紀錄",Toast.LENGTH_SHORT).show();
                            }
                        }
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

    public View.OnClickListener click = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if(v == binding.selectBtnDate)
            {
                SetTime();
                new DatePickerDialog(SelectActivity.this, new DatePickerDialog.OnDateSetListener()
                {
                    @Override
                    public void onDateSet(DatePicker view, int chooseyear, int choosemonth, int chooseday)
                    {
                        year = chooseyear;
                        month = choosemonth;
                        day = chooseday;
                        SetTime();
                        String Date = android.text.format.DateFormat.format("yyyy年MM月dd日", choose.getTimeInMillis()).toString();
                        binding.selectBtnDate.setText(Date);
                    }
                }, year, month, day).show();
            }
            else if(v == binding.selectBtnSelect)
            {
                datetime.clear();
                address.clear();
                stay.clear();
                remark.clear();
                SetAdapter();
                if(binding.selectBtnDate.getText().toString().equals("選擇日期"))
                {
                    Toast.makeText(getApplicationContext(),"請選擇日期",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    SetTime();
                    GetData(MinTime(choose),MaxTime(choose));
                }
            }
        }
    };

    //設定查詢條件最小值
    public Timestamp MinTime(Calendar min)
    {
        min.set(Calendar.HOUR_OF_DAY,0);
        min.set(Calendar.MINUTE,0);
        min.set(Calendar.SECOND,0);
        min.set(Calendar.MILLISECOND,0);
        Timestamp timestamp = new Timestamp(min.getTime());
        return timestamp;
    }

    //設定查詢條件最大值
    public Timestamp MaxTime(Calendar max)
    {
        max.set(Calendar.HOUR_OF_DAY,23);
        max.set(Calendar.MINUTE,59);
        max.set(Calendar.SECOND,59);
        max.set(Calendar.MILLISECOND,99);
        Timestamp timestamp = new Timestamp(max.getTime());
        return timestamp;
    }

    //設定時間
    public void SetTime()
    {
        choose = Calendar.getInstance();
        if(year == 0 && month == 0 && day == 0)
        {
            year = choose.get(Calendar.YEAR);
            month = choose.get(Calendar.MONTH);
            day = choose.get(Calendar.DAY_OF_MONTH);
        }
        else
        {
            choose.set(Calendar.YEAR,year);
            choose.set(Calendar.MONTH,month);
            choose.set(Calendar.DAY_OF_MONTH,day);
        }
    }

    //設定RecyclerView
    public void SetAdapter()
    {
        SelectAdapter selectAdapter = new SelectAdapter(SelectActivity.this, address, datetime, stay, remark, id);
        binding.selectRecyclerview.setLayoutManager(new LinearLayoutManager(SelectActivity.this));
        binding.selectRecyclerview.setAdapter(selectAdapter);
        binding.selectRecyclerview.setItemAnimator(new DefaultItemAnimator());
    }
}