package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import org.jetbrains.annotations.NotNull;

public class PasswordActivity extends AppCompatActivity
{
    EditText password_input_old,password_input_new;
    Button password_btn_cancel,password_btn_save;
    ProgressBar progress;
    SharedPreferences userdata;
    String id,password_old,password_new;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    Internet internet = new Internet();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        userdata = getSharedPreferences("UserData",MODE_PRIVATE);
        password_input_old = findViewById(R.id.password_input_old);
        password_input_new = findViewById(R.id.password_input_new);
        password_btn_cancel = findViewById(R.id.password_btn_cancel);
        password_btn_save = findViewById(R.id.password_btn_save);
        progress = findViewById(R.id.progress);

        id = userdata.getString("id","");

        password_btn_cancel.setOnClickListener(click);
        password_btn_save.setOnClickListener(click);

        //設定ToolBar
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("更改密碼");
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

        //監聽如果輸入完密碼按Enter的話呼叫登入按鈕的click事件
        password_input_new.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE))
                {
                    password_btn_save.callOnClick();
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
            if(v == password_btn_cancel)
            {
                finish();
            }
            else if(v == password_btn_save)
            {
                if(internet.CheckInternet(PasswordActivity.this))
                {
                    password_old = password_input_old.getText().toString();
                    password_new = password_input_new.getText().toString();
                    if(password_old.isEmpty())
                    {
                        Toast.makeText(getApplicationContext(),"請輸入舊密碼",Toast.LENGTH_SHORT).show();
                    }
                    else if(password_new.isEmpty())
                    {
                        Toast.makeText(getApplicationContext(),"請輸入新密碼",Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        progress.setVisibility(View.VISIBLE);
                        Login();
                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"請確認網路連線",Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    //確認使用者身分
    public void Login()
    {
        firebaseAuth.signInWithEmailAndPassword(firebaseAuth.getCurrentUser().getEmail(),password_old)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>()
                {
                    @Override
                    public void onSuccess(AuthResult authResult)
                    {
                        UpdatePassword();
                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e)
                    {
                        progress.setVisibility(View.GONE);
                        String worning = e.getMessage();
                        if(worning.equals("There is no user record corresponding to this identifier. The user may have been deleted."))
                        {
                            Toast.makeText(getApplicationContext(),"此帳號尚未註冊",Toast.LENGTH_SHORT).show();
                        }
                        else if(worning.equals("The password is invalid or the user does not have a password."))
                        {
                            Toast.makeText(getApplicationContext(),"帳號密碼不正確",Toast.LENGTH_SHORT).show();
                        }
                        else if(worning.equals("The email address is badly formatted."))
                        {
                            Toast.makeText(getApplicationContext(),"信箱格式不正確",Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(),"登入失敗",Toast.LENGTH_SHORT).show();
                            Log.d("QQQ",e.getMessage());
                        }
                    }
                });
    }

    //更改密碼
    public void UpdatePassword()
    {
        firebaseAuth.getCurrentUser().updatePassword(password_new)
                .addOnSuccessListener(new OnSuccessListener<Void>()
                {
                    @Override
                    public void onSuccess(Void unused)
                    {
                        progress.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(),"更改成功",Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e)
                    {
                        progress.setVisibility(View.GONE);
                        Log.d("QQQ",e.getMessage());
                    }
                });
    }
}