package com.example.project;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Calendar;

public class SelectAdapter extends RecyclerView.Adapter<SelectAdapter.ViewHolder>
{
    private Context Context;
    private ArrayList<String> address = new ArrayList<>();
    private ArrayList<String> datetime = new ArrayList<>();
    private ArrayList<String> stay = new ArrayList<>();
    private ArrayList<String> remark = new ArrayList<>();
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private String Collection = "UserData";
    private String id;

    public SelectAdapter(Context Context, ArrayList<String> address, ArrayList<String> datetime, ArrayList<String> stay, ArrayList<String> remark, String id)
    {
        this.Context = Context;
        this.address = address;
        this.datetime = datetime;
        this.stay = stay;
        this.remark = remark;
        this.id = id;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(Context);
        View v = inflater.inflate(R.layout.selectlayout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position)
    {
        holder.select_text_address.setText("地址：" + address.get(position));
        holder.select_text_datetime.setText("日期：" + datetime.get(position));
        holder.select_text_stay.setText("停留時間：" + stay.get(position) + "分鐘");
        holder.select_text_remark.setText("備註：" + remark.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(Context,ShowActivity.class);
                intent.putExtra("address",address.get(position));
                intent.putExtra("datetime",datetime.get(position));
                intent.putExtra("stay",stay.get(position));
                intent.putExtra("remark",remark.get(position));
                Context.startActivity(intent);
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {
                String[] delete = new String[]{"刪除"};
                new AlertDialog.Builder(Context)
                        .setItems(delete, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                DeleteData(position);
                            }
                        })
                        .show();
                return true;
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return address.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        public TextView select_text_address, select_text_datetime, select_text_stay, select_text_remark;
        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            select_text_address = itemView.findViewById(R.id.select_text_address);
            select_text_datetime = itemView.findViewById(R.id.select_text_datetime);
            select_text_stay = itemView.findViewById(R.id.select_text_stay);
            select_text_remark = itemView.findViewById(R.id.select_text_remark);
        }
    }

    public void DeleteData(int position)
    {
        firebaseFirestore.collection(Collection).document(id)
                .collection("Location").document(datetime.get(position)).delete()
                .addOnSuccessListener(new OnSuccessListener()
                {
                    @Override
                    public void onSuccess(Object o)
                    {
                        DeleteList(position);
                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Toast.makeText(Context,"刪除失敗",Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void DeleteList(int position)
    {
        address.remove(position);
        datetime.remove(position);
        stay.remove(position);
        remark.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position,getItemCount()-position);
        Toast.makeText(Context,"刪除成功",Toast.LENGTH_SHORT).show();
    }
}
