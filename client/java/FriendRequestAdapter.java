package com.example.myweixin_client;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.ViewHolder> {
    List<FriendRequestLocalBean> list;
    OnRequestListener listener;

    public interface OnRequestListener{
        void onAgree(int position, FriendRequestLocalBean bean);
        void onRefuse(int position, FriendRequestLocalBean bean);
    }

    public void setOnRequestListener(OnRequestListener listener) {
        this.listener = listener;
    }

    public FriendRequestAdapter(List<FriendRequestLocalBean> list){
        this.list = list;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNickname;
        TextView tvMsg;
        TextView tvAgree;
        TextView tvRefuse;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNickname = itemView.findViewById(R.id.tv_nickname);
            tvMsg = itemView.findViewById(R.id.tv_msg);
            tvAgree = itemView.findViewById(R.id.tv_agree);
            tvRefuse = itemView.findViewById(R.id.tv_refuse);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_request, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FriendRequestLocalBean bean = list.get(position);
        holder.tvNickname.setText(bean.fromNickname);
        holder.tvMsg.setText(bean.reqMsg);

        if (bean.status == 0) {
            holder.tvAgree.setVisibility(View.VISIBLE);
            holder.tvRefuse.setVisibility(View.VISIBLE);
        } else {
            holder.tvAgree.setVisibility(View.GONE);
            holder.tvRefuse.setVisibility(View.GONE);
        }

        holder.tvAgree.setOnClickListener(v -> {
            if (listener != null) listener.onAgree(position, bean);
        });
        holder.tvRefuse.setOnClickListener(v -> {
            if (listener != null) listener.onRefuse(position, bean);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}