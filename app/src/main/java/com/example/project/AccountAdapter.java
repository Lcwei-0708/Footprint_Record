package com.example.project;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.ViewHolder>
{
    private Context Context;
    private String[] title;
    private String[] data;

    public AccountAdapter(Context Context, String[] title, String[] data)
    {
        this.Context = Context;
        this.title = title;
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(Context);
        View v = inflater.inflate(R.layout.accountlayout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position)
    {
        holder.account_text_title.setText(title[position] + "：");
        holder.account_text_data.setText(data[position]);
        holder.itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                switch(position)
                {
                    case 0:
                        Intent name = new Intent(Context,NameActivity.class);
                        Context.startActivity(name);
                        break;
                    case 1:
                        Intent phone = new Intent(Context,PhoneActivity.class);
                        Context.startActivity(phone);
                        break;
                    case 2:
                        Intent email = new Intent(Context,EmailActivity.class);
                        Context.startActivity(email);
                        break;
                    case 3:
                        Intent password = new Intent(Context,PasswordActivity.class);
                        Context.startActivity(password);
                        break;
                }
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return title.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        public TextView account_text_title;
        public TextView account_text_data;
        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            account_text_title = itemView.findViewById(R.id.account_text_title);
            account_text_data = itemView.findViewById(R.id.account_text_data);
        }
    }
}
