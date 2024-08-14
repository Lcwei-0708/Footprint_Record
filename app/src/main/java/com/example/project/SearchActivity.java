package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.Locale;

public class SearchActivity extends AppCompatActivity
{
    RecyclerView search_recyclerview;
    EditText search_input_address;
    Button search_btn_search;
    String location;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    SharedPreferences userdata;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        search_recyclerview = findViewById(R.id.search_recyclerview);
        search_input_address = findViewById(R.id.search_input_address);
        search_btn_search = findViewById(R.id.search_btn_search);
        userdata = getSharedPreferences("UserData",MODE_PRIVATE);

        //設定ToolBar
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("搜尋地點");
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

        search_btn_search.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                location = search_input_address.getText().toString();
                if(location.isEmpty())
                {
                    Toast.makeText(getApplicationContext(),"請輸入地名或地址",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    SearchLocation(location);
                }
            }
        });

        //監聽如果輸入完地址按Enter的話呼叫搜尋按鈕的click事件
        search_input_address.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_SEARCH))
                {
                    search_btn_search.callOnClick();
                }
                return true;
            }
        });
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
                                Intent intent = new Intent(SearchActivity.this,LoginActivity.class);
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
            Intent intent = new Intent(SearchActivity.this,LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }

    //搜尋地點
    public void SearchLocation(String location)
    {
        try
        {
            Geocoder geocoder = new Geocoder(SearchActivity.this, Locale.TAIWAN);
            List<Address> list = geocoder.getFromLocationName(location,100);
            if(list.size() == 0)
            {
                Toast.makeText(getApplicationContext(),"找不到地點",Toast.LENGTH_SHORT).show();
            }
            else
            {
                String[] address = new String[list.size()];
                for(int i = 0;i < list.size();i++)
                {
                    address[i] = list.get(i).getAddressLine(0);
                }
                SearchAdapter searchAdapter = new SearchAdapter(SearchActivity.this, address);
                search_recyclerview.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
                search_recyclerview.setAdapter(searchAdapter);
                search_recyclerview.setItemAnimator(new DefaultItemAnimator());
            }
        }
        catch(IndexOutOfBoundsException indexOutOfBoundsException)
        {
            Toast.makeText(getApplicationContext(),"請輸入更詳細的地址或地名",Toast.LENGTH_SHORT).show();
        }
        catch(Exception exception)
        {
            Toast.makeText(getApplicationContext(),"系統錯誤",Toast.LENGTH_SHORT).show();
            Log.d("QQQ",exception.toString());
        }
    }
}