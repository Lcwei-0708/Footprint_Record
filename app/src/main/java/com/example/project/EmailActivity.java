package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import org.jetbrains.annotations.NotNull;
import java.util.HashMap;
import java.util.Map;

public class EmailActivity extends AppCompatActivity
{
    EditText email_input_old,email_input_new,email_input_password;
    Button email_btn_cancel,email_btn_save;
    ProgressBar progress;
    SharedPreferences userdata;
    String id,email,password;
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    String Collection = "UserData";
    Internet internet = new Internet();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email);

        userdata = getSharedPreferences("UserData",MODE_PRIVATE);
        email_input_old = findViewById(R.id.email_input_old);
        email_input_new = findViewById(R.id.email_input_new);
        email_input_password = findViewById(R.id.email_input_password);
        email_btn_cancel = findViewById(R.id.email_btn_cancel);
        email_btn_save = findViewById(R.id.email_btn_save);
        progress = findViewById(R.id.progress);

        email_input_old.setText(userdata.getString("email",""));
        id = userdata.getString("id","");

        email_btn_cancel.setOnClickListener(click);
        email_btn_save.setOnClickListener(click);

        //設定ToolBar
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("更改信箱");
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
            if(v == email_btn_cancel)
            {
                finish();
            }
            else if(v == email_btn_save)
            {
                if(internet.CheckInternet(EmailActivity.this))
                {
                    email = email_input_new.getText().toString();
                    password = email_input_password.getText().toString();
                    if(email.isEmpty())
                    {
                        Toast.makeText(getApplicationContext(),"請輸入信箱",Toast.LENGTH_SHORT).show();
                    }
                    else if(password.isEmpty())
                    {
                        Toast.makeText(getApplicationContext(),"請輸入密碼",Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        new AlertDialog.Builder(EmailActivity.this)
                                .setTitle("警告")
                                .setMessage("更改信箱需要重新登入，確定要更改嗎？")
                                .setPositiveButton("確定", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i)
                                    {
                                        Login();
                                    }
                                })
                                .setNegativeButton("取消",null)
                                .show();
                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(),"請確認網路連線",Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    //確認是否為本人
    public void Login()
    {
        progress.setVisibility(View.VISIBLE);
        firebaseAuth.signInWithEmailAndPassword(userdata.getString("email",""),password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>()
                {
                    @Override
                    public void onSuccess(AuthResult authResult)
                    {
                        UpdateUserEmail();
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

    //更改使用者信箱
    public void UpdateUserEmail()
    {
        firebaseAuth.getCurrentUser().updateEmail(email)
                .addOnSuccessListener(new OnSuccessListener<Void>()
                {
                    @Override
                    public void onSuccess(Void unused)
                    {
                        Map map = new HashMap();
                        map.put("email",email);
                        firebaseFirestore.collection(Collection)
                                .document(id)
                                .update(map)
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
                                {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot)
                                    {
                                        LogOut();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener()
                                {
                                    @Override
                                    public void onFailure(@NonNull Exception e)
                                    {
                                        progress.setVisibility(View.GONE);
                                        String worning = e.getMessage();
                                        if (worning.equals("The email address is already in use by another account."))
                                        {
                                            Toast.makeText(getApplicationContext(), "此信箱已被註冊", Toast.LENGTH_SHORT).show();
                                        }
                                        else
                                        {
                                            Log.d("QQQ", e.getMessage());
                                            Toast.makeText(getApplicationContext(), "更改失敗", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e)
                    {
                        progress.setVisibility(View.GONE);
                        String worning = e.getMessage();
                        if (worning.equals("The email address is already in use by another account."))
                        {
                            Toast.makeText(getApplicationContext(), "此信箱已被註冊", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Log.d("QQQ", e.getMessage());
                            Toast.makeText(getApplicationContext(), "更改失敗", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    //登出帳號
    public void LogOut()
    {
        userdata.edit().clear().commit();
        firebaseAuth.signOut();
        progress.setVisibility(View.GONE);
        Toast.makeText(getApplicationContext(),"更改成功，請重新登入帳號",Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(EmailActivity.this,LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}