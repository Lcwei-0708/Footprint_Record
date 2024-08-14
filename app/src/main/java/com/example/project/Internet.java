package com.example.project;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import static android.content.Context.CONNECTIVITY_SERVICE;

public class Internet
{
    public boolean CheckInternet(Context context)
    {
        //判斷網路
        ConnectivityManager conManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);//先取得此service
        NetworkInfo networInfo = conManager.getActiveNetworkInfo();       //在取得相關資訊

        if (networInfo == null || !networInfo.isAvailable())
        {
            return false;
        }
        else
        {
            return true; 
        }
    }
}
