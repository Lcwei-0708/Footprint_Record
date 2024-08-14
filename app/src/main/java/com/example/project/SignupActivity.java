package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import org.jetbrains.annotations.NotNull;

public class SignupActivity extends AppCompatActivity
{
    TextView signup_text_login;
    TextInputEditText input_user,input_email,input_password,input_phone;
    Button btn_registered;
    Internet internet = new Internet();
    String username,email,password,phone;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    String Collection = "UserData";
    ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        signup_text_login = findViewById(R.id.signup_text_login);
        input_user = findViewById(R.id.signup_user);
        input_email = findViewById(R.id.signup_email);
        input_password = findViewById(R.id.signup_password);
        input_phone = findViewById(R.id.signup_phone);
        btn_registered = findViewById(R.id.signup_btn_signup);
        progress = findViewById(R.id.progress);

        //監聽如果輸入完密碼後按下Enter，就呼叫註冊按鈕的click事件
        input_password.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE))
                {
                    btn_registered.callOnClick();
                }
                return false;
            }
        });

        btn_registered.setOnClickListener(click); //註冊按鈕
        signup_text_login.setOnClickListener(click); //返回登入
    }

    public View.OnClickListener click = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if(v == btn_registered) //註冊按鈕
            {
                username = input_user.getText().toString();
                email = input_email.getText().toString();
                password = input_password.getText().toString();
                phone = input_phone.getText().toString();
                if(!username.equals("") && !email.equals("") && !password.equals("") && !phone.equals(""))
                {
                    if(email.contains("@"))
                    {
                        if(password.length() < 1)
                        {
                            Toast.makeText(getApplicationContext(),"密碼至少要6個字",Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            if(internet.CheckInternet(SignupActivity.this))
                            {
                                SignUp();
                            }
                            else
                            {
                                Toast.makeText(getApplicationContext(),"請確認網路連線",Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),"信箱格式不正確",Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"需填寫所有欄位",Toast.LENGTH_SHORT).show();
                }
            }
            else if(v == signup_text_login) //返回登入
            {
                finish();
            }
        }
    };

    public void SetUserData() //設定使用者資料
    {
        DataField.UserData data = new DataField.UserData();
        data.id = firebaseAuth.getUid();
        data.email = email;
        data.name = username;
        data.phone = phone;

        firebaseFirestore.collection(Collection)
                .document(firebaseAuth.getUid())
                .set(data)
                .addOnSuccessListener(new OnSuccessListener()
                {
                    @Override
                    public void onSuccess(Object o)
                    {
                        progress.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(),"註冊成功",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SignupActivity.this,VerificationActivity.class);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        progress.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(),"註冊失敗",Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void SignUp() //註冊
    {
        progress.setVisibility(View.VISIBLE);
        firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>()
                {
                    @Override
                    public void onSuccess(AuthResult authResult)
                    {
                        SetUserData();
                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e)
                    {
                        String worning = e.getMessage();
                        if(worning.equals("The email address is already in use by another account."))
                        {
                            Toast.makeText(getApplicationContext(),"此信箱已註冊過",Toast.LENGTH_SHORT).show();
                        }
                        else if(worning.equals("The email address is badly formatted."))
                        {
                            Toast.makeText(getApplicationContext(),"信箱格式不正確",Toast.LENGTH_SHORT).show();
                        }
                        else if(worning.contains("The given password is invalid."))
                        {
                            Toast.makeText(getApplicationContext(),"密碼強度太弱",Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(),"註冊失敗",Toast.LENGTH_SHORT).show();
                            Log.d("QQQ",e.getMessage());
                        }
                        progress.setVisibility(View.GONE);
                    }
                });
    }
}