package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import org.jetbrains.annotations.NotNull;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity
{
    TextInputEditText input_username,input_password;
    TextView login_text_registered,login_text_password;
    Button btn_login;
    SharedPreferences userdata;
    Internet internet = new Internet();
    String id,email,password,name,phone;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    String Collection = "UserData";
    ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        userdata = getSharedPreferences("UserData",MODE_PRIVATE);
        input_username = findViewById(R.id.login_user);
        input_password = findViewById(R.id.login_password);
        btn_login = findViewById(R.id.login_button_login);
        login_text_registered = findViewById(R.id.login_text_signup);
        login_text_password = findViewById(R.id.login_text_password);
        progress = findViewById(R.id.progress);

        //監聽如果輸入完密碼按Enter的話呼叫登入按鈕的click事件
        input_password.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE))
                {
                    btn_login.callOnClick();
                }
                return true;
            }
        });
        login_text_registered.setOnClickListener(click); //前往註冊
        login_text_password.setOnClickListener(click); //忘記密碼
        btn_login.setOnClickListener(click); //登入按鈕
    }

    public View.OnClickListener click = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if(v == login_text_registered) //前往註冊
            {
                Intent intent = new Intent(LoginActivity.this,SignupActivity.class);
                startActivity(intent);
            }
            else if(v == btn_login) //登入按鈕
            {
                email = String.valueOf(input_username.getText());
                password = String.valueOf(input_password.getText());
                if(email.equals(""))
                {
                    Toast.makeText(getApplicationContext(),"請輸入信箱",Toast.LENGTH_SHORT).show();
                }
                else if(password.equals(""))
                {
                    Toast.makeText(getApplicationContext(),"請輸入密碼",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    if(internet.CheckInternet(LoginActivity.this))
                    {
                        Login();
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),"請確認網路連線",Toast.LENGTH_SHORT).show();
                    }
                }
            }
            else if(v == login_text_password) //忘記密碼
            {
                email = String.valueOf(input_username.getText());
                if(email.equals(""))
                {
                    Toast.makeText(getApplicationContext(),"請輸入信箱",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    if(internet.CheckInternet(LoginActivity.this))
                    {
                        SendResetPasssword();
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),"請確認網路連線",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    };

    //登入
    public void Login()
    {
        progress.setVisibility(View.VISIBLE);
        firebaseAuth.signInWithEmailAndPassword(email,password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>()
                {
                    @Override
                    public void onSuccess(AuthResult authResult)
                    {
                        if(firebaseAuth.getCurrentUser().isEmailVerified())
                        {
                            id = firebaseAuth.getUid();
                            UpdateEmail();
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(),"請先到完成信箱認證",Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this,VerificationActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        progress.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e)
                    {
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
                        progress.setVisibility(View.GONE);
                    }
                });
    }

    //取得使用者資料
    public void GetUserData()
    {
        firebaseFirestore.collection(Collection)
                .document(id)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
                {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot)
                    {
                        name = documentSnapshot.getString("name");
                        phone = documentSnapshot.getString("phone");
                        SaveUserData();
                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Toast.makeText(getApplicationContext(),"系統錯誤",Toast.LENGTH_SHORT).show();
                        progress.setVisibility(View.GONE);
                    }
                });
    }

    //將使用者資料存到SharePreference
    public void SaveUserData()
    {
        userdata.edit()
                .putString("id",id)
                .putString("name",name)
                .putString("phone",phone)
                .putString("email",email)
                .commit();
        progress.setVisibility(View.GONE);
        Toast.makeText(getApplicationContext(),"登入成功",Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(LoginActivity.this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    //寄送重設密碼信件
    public void SendResetPasssword()
    {
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(new OnSuccessListener<Void>()
                {
                    @Override
                    public void onSuccess(Void unused)
                    {
                        Toast.makeText(getApplicationContext(),"已寄送重設密碼信件",Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e)
                    {
                        Toast.makeText(getApplicationContext(),"寄送重設密碼信件失敗",Toast.LENGTH_SHORT).show();
                        Log.d("QQQ",e.getMessage());
                    }
                });
    }

    public void UpdateEmail()
    {
        Map map = new HashMap();
        map.put("email",email);
        firebaseFirestore.collection(Collection).document(id).update(map)
                .addOnSuccessListener(new OnSuccessListener()
                {
                    @Override
                    public void onSuccess(Object o)
                    {
                        GetUserData();
                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e)
                    {
                        progress.setVisibility(View.GONE);
                        Log.d("QQQ",e.getMessage());
                        Toast.makeText(getApplicationContext(),"系統錯誤",Toast.LENGTH_SHORT).show();
                    }
                });
    }
}