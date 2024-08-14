package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import org.jetbrains.annotations.NotNull;

public class VerificationActivity extends AppCompatActivity
{
    Button verification_btn_confirm, verification_btn_resend;
    TextView verification_text_description;
    Internet internet = new Internet();
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);
        verification_btn_confirm = findViewById(R.id.verification_btn_confirm);
        verification_btn_resend = findViewById(R.id.verification_btn_resend);
        verification_text_description = findViewById(R.id.verification_text_description);

        verification_btn_confirm.setOnClickListener(click);
        verification_btn_resend.setOnClickListener(click);
        verification_text_description.setText("請到<" + firebaseAuth.getCurrentUser().getEmail() +  ">信箱中接收認證信，並點選信中連結來完成認證。");

        SendMail();
    }

    public View.OnClickListener click = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if(v == verification_btn_confirm) //完成認證
            {
                CheckVerification();
            }
            else if(v == verification_btn_resend) //重新寄送認證信
            {
                SendMail();
            }
        }
    };

    //寄送認證信
    public void SendMail()
    {
        if(internet.CheckInternet(VerificationActivity.this))
        {
            firebaseAuth.getCurrentUser().sendEmailVerification()
                    .addOnSuccessListener(new OnSuccessListener<Void>()
                    {
                        @Override
                        public void onSuccess(Void unused)
                        {
                            Toast.makeText(getApplicationContext(),"已寄送認證信",Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener()
                    {
                        @Override
                        public void onFailure(@NonNull @NotNull Exception e)
                        {
                            Log.d("QQQ",e.getMessage());
                        }
                    });
        }
        else
        {
            Toast.makeText(getApplicationContext(),"請確認網路連線",Toast.LENGTH_SHORT).show();
        }
    }

    //確認是否通過認證
    public void CheckVerification()
    {
        firebaseAuth.getCurrentUser()
                .reload()
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task)
                    {
                        if(firebaseAuth.getCurrentUser().isEmailVerified())
                        {
                            Toast.makeText(getApplicationContext(),"已成功完成認證",Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(VerificationActivity.this,LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(),"請先完成信箱認證",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}