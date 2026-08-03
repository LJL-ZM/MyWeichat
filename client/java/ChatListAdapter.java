package com.example.myweixin_client;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.VH> {
    List<ChatSessionBean> list;
    OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position, ChatSessionBean bean);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public ChatListAdapter(List<ChatSessionBean> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_session, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        ChatSessionBean bean = list.get(position);
        holder.tvName.setText(bean.chatName);
        holder.tvLastMsg.setText(bean.lastMsgContent == null ? "" : bean.lastMsgContent);
        if (bean.unreadCount > 0) {
            holder.tvUnread.setVisibility(View.VISIBLE);
            holder.tvUnread.setText(String.valueOf(bean.unreadCount));
        } else {
            holder.tvUnread.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(position, bean);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvLastMsg;
        TextView tvUnread;

        public VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvLastMsg = itemView.findViewById(R.id.tv_last_msg);
            tvUnread = itemView.findViewById(R.id.tv_unread);
        }
    }
}