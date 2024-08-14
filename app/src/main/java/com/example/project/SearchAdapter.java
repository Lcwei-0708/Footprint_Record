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

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder>
{
    private Context Context;
    private String[] address;

    public SearchAdapter(Context Context, String[] address)
    {
        this.Context = Context;
        this.address = address;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(Context);
        View v = inflater.inflate(R.layout.searchlayout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position)
    {
        holder.search_text_address.setText(address[position]);
        holder.search_imgs.setImageResource(R.drawable.ic_location);
        holder.itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(Context,AddActivity.class);
                intent.putExtra("address",address[position]);
                Context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return address.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        public TextView search_text_address;
        public ImageView search_imgs;
        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            search_text_address = itemView.findViewById(R.id.search_text_address);
            search_imgs = itemView.findViewById(R.id.search_imgs);
        }
    }
}
