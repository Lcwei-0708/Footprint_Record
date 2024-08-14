package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class PhoneActivity extends AppCompatActivity
{
    EditText phone_input_old,phone_input_new;
    Button phone_btn_cancel,phone_btn_save;
    ProgressBar progress;
    SharedPreferences userdata;
    String id,phone;
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    String Collection = "UserData";
    Internet internet = new Internet();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);

        userdata = getSharedPreferences("UserData",MODE_PRIVATE);
        phone_input_old = findViewById(R.id.phone_input_old);
        phone_input_new = findViewById(R.id.phone_input_new);
        phone_btn_cancel = findViewById(R.id.phone_btn_cancel);
        phone_btn_save = findViewById(R.id.phone_btn_save);
        progress = findViewById(R.id.progress);

        phone_input_old.setText(userdata.getString("phone",""));
        id = userdata.getString("id","");

        phone_btn_cancel.setOnClickListener(click);
        phone_btn_save.setOnClickListener(click);

        //設定ToolBar
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("更改電話");
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

        //監聽如果輸入完電話按Enter的話呼叫登入按鈕的click事件
        phone_input_new.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE))
                {
                    phone_btn_save.callOnClick();
                }
                return true;
            }
        });
    }

    public View.OnClickListener click = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if(v == phone_btn_cancel)
            {
                finish();
            }
            else if(v == phone_btn_save)
            {
                if(internet.CheckInternet(PhoneActivity.this))
                {
                    phone = phone_input_new.getText().toString();
                    if(phone.isEmpty())
                    {
                        Toast.makeText(getApplicationContext(),"請輸入電話",Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        UpdateUserPhone();
                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"請確認網路連線",Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    //更改使用者電話
    public void UpdateUserPhone()
    {
        progress.setVisibility(View.VISIBLE);
        Map map = new HashMap();
        map.put("phone",phone);
        firebaseFirestore.collection(Collection)
                .document(id)
                .update(map)
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
                {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot)
                    {
                        SaveUserPhone();
                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Toast.makeText(getApplicationContext(),"更改失敗",Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //將使用者電話存到SharePreference
    public void SaveUserPhone()
    {
        userdata.edit()
                .putString("phone",phone)
                .commit();
        progress.setVisibility(View.GONE);
        Toast.makeText(getApplicationContext(),"更改成功",Toast.LENGTH_SHORT).show();
        finish();
    }
}