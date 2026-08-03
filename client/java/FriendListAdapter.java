package com.example.myweixin_client;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.ViewHolder> {
    List<FriendBean> list;
    OnItemClickListener listener;
    public interface OnItemClickListener {
        void onItemClick(int position, FriendBean bean);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }
    public FriendListAdapter(List<FriendBean> list){
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        FriendBean fb = list.get(position);
        holder.tvName.setText(fb.name);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null){
                    listener.onItemClick(position, fb);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        public ViewHolder(View itemView){
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
        }
    }
}