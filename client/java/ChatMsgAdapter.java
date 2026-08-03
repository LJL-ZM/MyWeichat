package com.example.myweixin_client;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatMsgAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_SEND = 1;
    private static final int TYPE_RECEIVE = 2;
    List<ChatMsgBean> list;

    public ChatMsgAdapter(List<ChatMsgBean> list) {
        this.list = list;
    }

    @Override
    public int getItemViewType(int position) {
        return list.get(position).isSendByMe ? TYPE_SEND : TYPE_RECEIVE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SEND) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_msg_send, parent, false);
            return new SendViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_msg_receive, parent, false);
            return new ReceiveViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMsgBean bean = list.get(position);
        if (holder instanceof SendViewHolder) {
            ((SendViewHolder) holder).tvContent.setText(bean.content);
        } else {
            ((ReceiveViewHolder) holder).tvNick.setText(bean.fromNickname);
            ((ReceiveViewHolder) holder).tvContent.setText(bean.content);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class SendViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent;
        public SendViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tv_content);
        }
    }

    static class ReceiveViewHolder extends RecyclerView.ViewHolder {
        TextView tvNick;
        TextView tvContent;
        public ReceiveViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNick = itemView.findViewById(R.id.tv_nick);
            tvContent = itemView.findViewById(R.id.tv_content);
        }
    }
}