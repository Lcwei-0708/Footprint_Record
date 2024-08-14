package com.example.project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.project.databinding.ActivityAddBinding;
import java.util.List;
import java.util.Locale;

public class AddActivity extends AppCompatActivity implements OnMapReadyCallback
{
    private GoogleMap mMap;
    private ActivityAddBinding binding;
    private TextView toolbar_next;
    private String address;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent intent = getIntent();
        address = intent.getStringExtra("address");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //設定ToolBar
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("新增足跡");
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
        toolbar_next = findViewById(R.id.toolbar_next);
        toolbar_next.setVisibility(View.VISIBLE);
        toolbar_next.setOnClickListener(click); //下一步
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true); //顯示放大縮小的圖示
        try
        {
            Geocoder geocoder = new Geocoder(AddActivity.this, Locale.TAIWAN);
            List<Address> list = geocoder.getFromLocationName(address,1);
            Double lng = list.get(0).getLatitude(); //經度
            Double lat = list.get(0).getLongitude(); //緯度
            LatLng select = new LatLng(lng,lat);
            mMap.addMarker(new MarkerOptions().position(select).title(address).snippet(lat + " " + lng));
            CameraPosition cameraPosition = new CameraPosition.Builder().target(select).zoom(18).build();
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition)); //動畫方式移至對應地點
            mMap.getUiSettings().setZoomControlsEnabled(true); //顯示放大縮小的圖示
        }
        catch(IndexOutOfBoundsException indexOutOfBoundsException)
        {
            Toast.makeText(getApplicationContext(),"找不到此地點",Toast.LENGTH_SHORT).show();
        }
        catch (Exception e)
        {
            Log.d("QQQ",e.toString());
        }
    }

    public View.OnClickListener click = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if(v == toolbar_next) //下一步
            {
                Intent intent = new Intent(AddActivity.this,TimeActivity.class);
                intent.putExtra("address",address);
                startActivity(intent);
            }
        }
    };
}