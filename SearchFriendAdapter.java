package com.example.myweixin_client;

import android.view.LayoutInflater;
import android.view.SearchEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SearchFriendAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<SearchItemBean> list;
    private OnItemClickListener listener;
    public interface OnItemClickListener {
        //void onAddClick(SearchItemBean bean);
        void onMsgClick(SearchItemBean bean);
        void onFriendClick(SearchItemBean bean);
    }
    public void setOnItemListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    public SearchFriendAdapter(List<SearchItemBean> list) {
        this.list = list;
    }
    @Override
    public int getItemViewType(int position) {
        return list.get(position).itemType;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == SearchItemBean.TYPE_NEW_FRIEND){
            return new NewFriendHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_new_friend, parent, false));
        } else if(viewType == SearchItemBean.TYPE_MY_FRIEND){
            return new MyFriendHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_my_friend, parent, false));
        } else {
            return new DividerHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_divider, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SearchItemBean bean = list.get(position);
        if (holder instanceof NewFriendHolder) {
            NewFriendHolder h = (NewFriendHolder) holder;
            h.tvName.setText(bean.name);
//            h.btnAdd.setOnClickListener(v -> {
//                if (listener != null) listener.onAddClick(bean);
//            });
            h.btnMsg.setOnClickListener(v -> {
                if (listener != null) listener.onMsgClick(bean);
            });
        } else if (holder instanceof MyFriendHolder) {
            MyFriendHolder h = (MyFriendHolder) holder;
            h.tvName.setText(bean.name);
            h.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onFriendClick(bean);
            });
        } else if (holder instanceof DividerHolder) {
            DividerHolder h = (DividerHolder) holder;
            h.tvTitle.setText(bean.dividerTitle);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class NewFriendHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        Button btnAdd;
        Button btnMsg;
        public NewFriendHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            //btnAdd = itemView.findViewById(R.id.btn_add);
            btnMsg = itemView.findViewById(R.id.btn_msg);
        }

    }
    static class MyFriendHolder extends RecyclerView.ViewHolder{
        TextView tvName;
        TextView btnChat;
        public MyFriendHolder(View view) {
            super(view);
            tvName = view.findViewById(R.id.tv_name);
            btnChat = view.findViewById(R.id.btn_chat);
        }
    }
    static class DividerHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        public DividerHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_divider_title);
        }
    }
}
