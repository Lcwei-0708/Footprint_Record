package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import org.jetbrains.annotations.NotNull;

public class AccountActivity extends AppCompatActivity
{
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    String username,email,phone;
    String Collection = "UserData";
    SharedPreferences userdata;
    RecyclerView account_recyclerview;
    String[] title = new String[]{"姓名", "電話", "信箱", "密碼"};
    String[] data = new String[title.length];

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        userdata = getSharedPreferences("UserData",MODE_PRIVATE);
        account_recyclerview = findViewById(R.id.account_recyclerview);

        //設定ToolBar
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("個人資料");
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

    @Override
    protected void onResume()
    {
        super.onResume();
        try
        {
            //判斷登入身分是否失效
            firebaseAuth.getCurrentUser().reload()
                    .addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<Void> task)
                        {
                            if(firebaseAuth.getCurrentUser() != null)
                            {
                                GetUserData();
                            }
                            else
                            {
                                userdata.edit().clear().commit();
                                Intent intent = new Intent(AccountActivity.this,LoginActivity.class);
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
            Intent intent = new Intent(AccountActivity.this,LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }

    //取得使用者資料
    public void GetUserData()
    {
        firebaseFirestore.collection(Collection)
                .document(firebaseAuth.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
                {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot)
                    {
                        username = documentSnapshot.getString("name");
                        email = firebaseAuth.getCurrentUser().getEmail();
                        phone = documentSnapshot.getString("phone");
                        SaveUserPhone();
                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Log.d("QQQ",e.getMessage());
                        Toast.makeText(getApplicationContext(),"系統錯誤",Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //將使用者資料存到SharePreference
    public void SaveUserPhone()
    {
        userdata.edit()
                .putString("name",username)
                .putString("email",firebaseAuth.getCurrentUser().getEmail())
                .putString("phone",phone)
                .commit();
        SetRecyclerView();
    }

    //設定RecyclerView
    public void SetRecyclerView()
    {
        data[0] = username;
        data[1] = phone;
        data[2] = email;
        data[3] = "**********";
        AccountAdapter accountAdapter = new AccountAdapter(AccountActivity.this, title, data);
        account_recyclerview.setLayoutManager(new LinearLayoutManager(AccountActivity.this));
        account_recyclerview.setAdapter(accountAdapter);
        account_recyclerview.setItemAnimator(new DefaultItemAnimator());
    }
}