package com.example.project;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.example.project.databinding.ActivityMainBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback
{
    private SharedPreferences userdata;
    private View headerView;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private TextView name,main_text_sum,main_text_today;
    private CardView main_cardview;
    private ImageView avatar;
    private Internet internet = new Internet();
    // 初始化陣列來儲存資料
    private JSONArray earthquakes = new JSONArray();
    private ActivityMainBinding binding;
    private GoogleMap mMap;
    //private StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        navigationView = findViewById(R.id.nav_view);
        headerView = navigationView.getHeaderView(0);
        name = headerView.findViewById(R.id.menu_text_user);
        avatar = headerView.findViewById(R.id.menu_img_avatar);
        main_text_sum = findViewById(R.id.main_text_sum);
        main_text_today = findViewById(R.id.main_text_today);
        main_cardview = findViewById(R.id.main_cardview);
        toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawer_main);

        requestPermission();
        SetToolbar();

        //設定抽屜DrawerLayout
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.setDrawerListener(drawerToggle);

        //設定導航欄NavigationView的點選事件
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener()
        {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem)
            {
                switch (menuItem.getItemId())
                {
                    case R.id.menu_account://個人資料
                        if(userdata.getString("name","未登入").equals("未登入"))
                        {
                            Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                            startActivity(intent);
                        }
                        else
                        {
                            Intent intent = new Intent(MainActivity.this, AccountActivity.class);
                            startActivity(intent);
                        }
                        break;
                    case R.id.menu_add://新增足跡
                        if(userdata.getString("name","未登入").equals("未登入"))
                        {
                            Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                            startActivity(intent);
                        }
                        else
                        {
                            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                            startActivity(intent);
                        }
                        break;
                    case R.id.menu_select://查詢足跡
                        if(userdata.getString("name","未登入").equals("未登入"))
                        {
                            Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                            startActivity(intent);
                        }
                        else
                        {
                            Intent intent = new Intent(MainActivity.this, SelectActivity.class);
                            startActivity(intent);
                        }
                        break;
                    case R.id.menu_logout://登出
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("登出")
                                .setMessage("確定要登出嗎？")
                                .setPositiveButton("確定", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i)
                                    {
                                        userdata.edit().clear().commit();
                                        firebaseAuth.signOut();
                                        SetHeaderview();
                                        //SetAvatar();
                                        Toast.makeText(getApplicationContext(),"您已登出",Toast.LENGTH_SHORT).show();
                                        onPrepareOptionsMenu(navigationView.getMenu());
                                    }
                                })
                                .setNegativeButton("取消",null)
                                .show();
                        break;
                    case R.id.menu_login://登入
                        Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                        startActivity(intent);
                        break;
                }
                drawerLayout.closeDrawers();//關閉抽屜
                return true;
            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        userdata = getSharedPreferences("UserData",MODE_PRIVATE);
        SetHeaderview();
        //SetAvatar();
        onPrepareOptionsMenu(navigationView.getMenu());
    }

    //設定ToolBar
    public void SetToolbar()
    {
        setSupportActionBar(toolbar);
        toolbar.setTitle("臺灣近期有感地震區域分布圖");
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_menu));
    }

    //設定選單的使用者介面
    public void SetHeaderview()
    {
        name.setText(userdata.getString("name","未登入"));
        avatar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(userdata.getString("name","未登入").equals("未登入"))
                {
                    Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                    startActivity(intent);
                }
                else
                {
                    final String[] list = {"從相簿選擇","開啟相機"};
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("更換頭像")
                            .setItems(list, new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    switch(which)
                                    {
                                        case 0:
                                            Intent photo = new Intent();
                                            photo.setType("image/*");
                                            photo.setAction(Intent.ACTION_GET_CONTENT);
                                            startActivityForResult(photo, 0);
                                            break;
                                        case 1:
                                            Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                            startActivityForResult(camera,1);
                                            break;
                                    }
                                }
                            })
                            .show();
                }
            }
        });

        avatar.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {
                if(userdata.getString("name","未登入").equals("未登入"))
                {
                    Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                    startActivity(intent);
                }
                else
                {
                    if(avatar.getTag().equals("null"))
                    {
                        Toast.makeText(getApplicationContext(),"無設置頭像",Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("刪除頭像")
                                .setMessage("是否真的要刪除頭像？")
                                .setPositiveButton("確定", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i)
                                    {
                                        //DeleteAvatar();
                                    }
                                })
                                .setNegativeButton("取消",null)
                                .show();
                    }
                }
                return true;
            }
        });
    }

    //Menu選單Item控制
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        userdata = getSharedPreferences("UserData",MODE_PRIVATE);
        navigationView = findViewById(R.id.nav_view);
        navigationView.getMenu().removeItem(R.id.menu_login);
        navigationView.getMenu().removeItem(R.id.menu_logout);
        if(userdata.getString("name","未登入").equals("未登入"))
        {
            navigationView.getMenu().removeItem(R.id.menu_logout);
            navigationView.getMenu().add(0,R.id.menu_login,100,R.string.menu_login).setIcon(R.drawable.ic_menu_power);
        }
        else
        {
            navigationView.getMenu().removeItem(R.id.menu_login);
            navigationView.getMenu().add(0,R.id.menu_logout,100,R.string.menu_logout).setIcon(R.drawable.ic_menu_power);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    //監聽返回鍵
    @Override
    public void onBackPressed()
    {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage("確定要離開嗎？")
                .setPositiveButton("確定", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        finish();
                    }
                })
                .setNegativeButton("取消",null)
                .show();
    }

    //要求目前位置權限
    public void requestPermission()
    {
        if(Build.VERSION.SDK_INT >= 23) //Android 6 以上
        {
            int hasPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            if(hasPermission != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            }
        }
    }

    //取得權限回傳值
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if(requestCode == 0)
        {
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this,"未取得授權",Toast.LENGTH_LONG).show();
            }
            else
            {
                //已取得授權
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    //Google地圖
    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true); //顯示放大縮小的圖示
        SetSnippet();
        try
        {
            LatLng taiwan = new LatLng(23.816345340850653, 121.03288592100365);
            mMap.clear();
            CameraPosition cameraPosition = new CameraPosition.Builder().target(taiwan).zoom(7.8f).build();
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            GetData();
        }
        catch(Exception e)
        {
            Log.d("QQQ",e.toString());
        }
    }

    //設定地圖Marker
    public void SetMap()
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Log.d("QQQ",earthquakes.toString());
                    // 遍歷地震報告資料
                    for (int i = 0; i < earthquakes.length(); i++)
                    {
                        JSONObject earthquake = earthquakes.getJSONObject(i);
                        JSONObject earthquakeInfo = earthquake.getJSONObject("EarthquakeInfo");

                        // 取得日期、震度和座標資訊
                        String originTime = earthquakeInfo.getString("OriginTime");
                        double focalDepth = earthquakeInfo.getDouble("FocalDepth");
                        JSONObject epicenter = earthquakeInfo.getJSONObject("Epicenter");
                        String location = epicenter.getString("Location");
                        double latitude = epicenter.getDouble("EpicenterLatitude");
                        double longitude = epicenter.getDouble("EpicenterLongitude");
                        double magnitudeValue = earthquakeInfo.getJSONObject("EarthquakeMagnitude").getDouble("MagnitudeValue");

                        System.out.println(originTime);
                        LatLng select = new LatLng(latitude, longitude);
                        mMap.addMarker(new MarkerOptions()
                                .position(select)
                                .title(location)
                                .snippet("發生時間：" + originTime + "\n震源深度：" + focalDepth + "\n芮氏規模：" + magnitudeValue)
                                .icon(BitmapDescriptorFactory.fromBitmap(setTextToImg(String.valueOf(magnitudeValue)))));
                    }
                }
                catch(Exception e)
                {
                    Log.d("QQQ",e.toString());
                }
            }
        });
    }

    //設定Marker圖片
    public Bitmap setTextToImg(String text)
    {
        // 取得圖片資源，將其轉換為 BitmapDrawable 並提取 Bitmap
        BitmapDrawable icon = (BitmapDrawable) getResources().getDrawable(R.drawable.number);

        // 複製 Bitmap，並設置為可編輯 (mutable)，配置為 ARGB_8888 格式
        Bitmap bitmap = icon.getBitmap().copy(Bitmap.Config.ARGB_8888, true);

        // 建立一個畫布，並將剛剛創建的 Bitmap 作為畫布的背景
        Canvas canvas = new Canvas(bitmap);

        // 設定繪製文字的 Paint 對象屬性
        Paint paint = new Paint();
        paint.setAntiAlias(true);  // 防鋸齒
        paint.setDither(true);  // 防抖動
        paint.setTextAlign(Paint.Align.CENTER);  // 設置文字對齊方式為居中
        paint.setTextSize(40);  // 設定文字大小
        paint.setColor(Color.parseColor("#FF3232")); // 設定文字顏色為紅色

        // 在原來的圖片上繪製文字，位置為圖片的中心偏下
        canvas.drawText(text, (bitmap.getWidth()/2), (bitmap.getHeight()/1.7f), paint);

        // 返回繪製後的 Bitmap
        return bitmap;
    }

    //設定座標標籤格式
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
                LinearLayout info = new LinearLayout(MainActivity.this);
                info.setOrientation(LinearLayout.VERTICAL);
                TextView title = new TextView(MainActivity.this);
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());
                TextView snippet = new TextView(MainActivity.this);
                snippet.setTextColor(Color.rgb(72,72,72));
                snippet.setText(marker.getSnippet());
                info.addView(title);
                info.addView(snippet);
                return info;
            }
        };
        mMap.setInfoWindowAdapter(infoWindowAdapter);
    }

    //取得資料
    public void GetData()
    {
        if(internet.CheckInternet(MainActivity.this))
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        // API URL
                        String urlString = "https://opendata.cwa.gov.tw/api/v1/rest/datastore/E-A0015-001?Authorization=CWA-FC729085-D91C-4FD5-8B8E-1D3B774D6262&limit=10&format=JSON&AreaName=";
                        URL url = new URL(urlString);

                        // 打開連接
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("GET");

                        // 檢查回應碼
                        int responseCode = conn.getResponseCode();
                        if (responseCode == HttpURLConnection.HTTP_OK)
                        {
                            // 讀取回應
                            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                            String inputLine;
                            StringBuilder content = new StringBuilder();

                            while ((inputLine = in.readLine()) != null)
                            {
                                content.append(inputLine);
                            }

                            // 關閉讀取器和連接
                            in.close();
                            conn.disconnect();

                            // 解析JSON
                            JSONObject jsonObject = new JSONObject(content.toString());
                            JSONObject records = jsonObject.getJSONObject("records");
                            earthquakes = records.getJSONArray("Earthquake");
                            SetMap();

                        }
                        else
                        {
                            Log.d("HTTP GET ERROR","HTTP GET請求失敗，回應碼: " + responseCode);
                        }
                    }
                    catch(Exception e)
                    {
                        Log.d("Error",e.toString());
                    }
                }
            }).start();
        }
        else
        {
            Toast.makeText(getApplicationContext(),"請確認網路連線",Toast.LENGTH_SHORT).show();
        }
    }

//    //設定頭像
//    public void SetAvatar()
//    {
//        if(userdata.getString("name","未登入").equals("未登入"))
//        {
//            avatar.setImageResource(R.mipmap.logo_round);
//            avatar.setTag("null");
//        }
//        else
//        {
//            try
//            {
//                storageReference.child(userdata.getString("id","") + "/avatar")
//                        .getDownloadUrl()
//                        .addOnSuccessListener(new OnSuccessListener<Uri>()
//                        {
//                            @Override
//                            public void onSuccess(Uri uri)
//                            {
//                                Glide.with(MainActivity.this).load(uri).into(avatar);
//                                avatar.setTag("");
//                            }
//                        })
//                        .addOnFailureListener(new OnFailureListener()
//                        {
//                            @Override
//                            public void onFailure(@NonNull @org.jetbrains.annotations.NotNull Exception e)
//                            {
//                                avatar.setImageResource(R.mipmap.logo_round);
//                                avatar.setTag("null");
//                            }
//                        });
//            }
//            catch(Exception e)
//            {
//                Log.d("QQQ", e.getMessage());
//            }
//        }
//    }
//
//    //上傳頭像(Uri)
//    public void UploadAvatar(Uri uri)
//    {
//        storageReference.child(userdata.getString("id","") + "/avatar").putFile(uri)
//                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
//                {
//                    @Override
//                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
//                    {
//                        SetAvatar();
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener()
//                {
//                    @Override
//                    public void onFailure(@NonNull @NotNull Exception e)
//                    {
//                        Toast.makeText(getApplicationContext(),"上傳頭像失敗",Toast.LENGTH_SHORT).show();
//                        Log.d("QQQ",e.toString());
//                    }
//                });
//    }
//
//    //上傳頭像(Bitmap)
//    public void UploadAvatar(Bitmap bitmap)
//    {
//        storageReference.child(userdata.getString("id","") + "/avatar")
//                .putBytes(BitmapToByte(bitmap))
//                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
//                {
//                    @Override
//                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
//                    {
//                        SetAvatar();
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener()
//                {
//                    @Override
//                    public void onFailure(@NonNull @NotNull Exception e)
//                    {
//                        Toast.makeText(getApplicationContext(),"上傳頭像失敗",Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }
//
//    //刪除頭像
//    public void DeleteAvatar()
//    {
//        storageReference.child(userdata.getString("id","") + "/avatar")
//                .delete()
//                .addOnSuccessListener(new OnSuccessListener<Void>()
//                {
//                    @Override
//                    public void onSuccess(Void unused)
//                    {
//                        avatar.setImageResource(R.mipmap.logo_round);
//                        avatar.setTag("null");
//                        Toast.makeText(getApplicationContext(),"刪除頭像成功",Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener()
//                {
//                    @Override
//                    public void onFailure(@NonNull @NotNull Exception e)
//                    {
//                        Toast.makeText(getApplicationContext(),"刪除頭像失敗",Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data)
//    {
//        if(resultCode == RESULT_OK)
//        {
//            switch(requestCode)
//            {
//                case 0:
//                    Uri uri = data.getData();
//                    UploadAvatar(uri);
//                    break;
//                case 1:
//                    Bitmap bitmap = (Bitmap) data.getExtras().get("data");
//                    UploadAvatar(bitmap);
//                    break;
//            }
//        }
//        super.onActivityResult(requestCode, resultCode, data);
//    }
//
//    public byte[] BitmapToByte(Bitmap bitmap)
//    {
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.PNG,100,byteArrayOutputStream);
//        byte[] data = byteArrayOutputStream.toByteArray();
//        return data;
//    }
}