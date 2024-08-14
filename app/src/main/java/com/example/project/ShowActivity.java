package com.example.project;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.project.databinding.ActivityShowBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ShowActivity extends AppCompatActivity implements OnMapReadyCallback
{
    private GoogleMap mMap;
    private ActivityShowBinding binding;
    private String address, datetime, stay, remark;
    private TextView toolbar_edit;
    private int year,month,day,hour,minute;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityShowBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent intent = getIntent();
        address = intent.getStringExtra("address");
        datetime = intent.getStringExtra("datetime");
        stay = intent.getStringExtra("stay");
        remark = intent.getStringExtra("remark");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //設定ToolBar
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(SetTitle());
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

        toolbar_edit = findViewById(R.id.toolbar_edit);
        toolbar_edit.setVisibility(View.VISIBLE);
        //編輯按鈕
        toolbar_edit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(ShowActivity.this,EditlocationActivity.class);
                intent.putExtra("address",address);
                intent.putExtra("datetime",datetime);
                intent.putExtra("stay",stay);
                intent.putExtra("remark",remark);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true); //顯示放大縮小的圖示
        SetSnippet();
        try
        {
            Geocoder geocoder = new Geocoder(ShowActivity.this, Locale.TAIWAN);
            List<Address> list = geocoder.getFromLocationName(address,1);
            Double lng = list.get(0).getLatitude(); //經度
            Double lat = list.get(0).getLongitude(); //緯度
            LatLng select = new LatLng(lng,lat);
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(select).title(address)
                    .snippet("時間：" + datetime + "\n停留時間：" + stay + "分鐘\n備註：" + remark));
            CameraPosition cameraPosition = new CameraPosition.Builder().target(select).zoom(18).build();
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
        catch(IndexOutOfBoundsException indexOutOfBoundsException)
        {
            Toast.makeText(getApplicationContext(),"太多地點",Toast.LENGTH_SHORT).show();
        }
        catch (Exception e)
        {
            Log.d("QQQ",e.toString());
        }
    }

    public void SetSnippet()
    {
        GoogleMap.InfoWindowAdapter infoWindowAdapter = new GoogleMap.InfoWindowAdapter()
        {
            @Nullable
            @org.jetbrains.annotations.Nullable
            @Override
            public View getInfoWindow(@NonNull @NotNull Marker marker)
            {
                return null;
            }
            @Nullable
            @org.jetbrains.annotations.Nullable
            @Override
            public View getInfoContents(@NonNull @NotNull Marker marker)
            {
                LinearLayout info = new LinearLayout(ShowActivity.this);
                info.setOrientation(LinearLayout.VERTICAL);
                TextView title = new TextView(ShowActivity.this);
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());
                TextView snippet = new TextView(ShowActivity.this);
                snippet.setTextColor(Color.rgb(72,72,72));
                snippet.setText(marker.getSnippet());
                info.addView(title);
                info.addView(snippet);
                return info;
            }
        };
        mMap.setInfoWindowAdapter(infoWindowAdapter);
    }

    public String SetTitle()
    {
        String[] dt = datetime.split(" ");
        String[] d = dt[0].split("-");
        String[] t = dt[1].split(":");
        year = Integer.parseInt(d[0]);
        month = Integer.parseInt(d[1])-1;
        day = Integer.parseInt(d[2]);
        hour = Integer.parseInt(t[0]);
        minute = Integer.parseInt(t[1]);
        calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR,year);
        calendar.set(Calendar.MONTH,month);
        calendar.set(Calendar.DAY_OF_MONTH,day);
        calendar.set(Calendar.HOUR_OF_DAY,hour);
        calendar.set(Calendar.MINUTE,minute);
        calendar.set(Calendar.SECOND,0);
        String DateTime = android.text.format.DateFormat.format("yyyy年MM月dd日 HH點mm分", calendar.getTimeInMillis()).toString();
        return DateTime;
    }
}