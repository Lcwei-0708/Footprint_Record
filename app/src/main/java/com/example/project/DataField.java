package com.example.project;

import com.google.firebase.Timestamp;

public class DataField
{
    public static class UserData
    {
        public String id;
        public String name;
        public String email;
        public String phone;
    }

    public static class LocationData
    {
        public String address;
        public Timestamp datetime;
        public String stay;
        public String remark;
    }
}
