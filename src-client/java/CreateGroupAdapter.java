package com.example.myweixin_client;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CreateGroupAdapter extends RecyclerView.Adapter<CreateGroupAdapter.VH> {
    List<FriendLocalBean> list;
    OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position, FriendLocalBean bean, boolean isChecked);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public CreateGroupAdapter(List<FriendLocalBean> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_create_group, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        FriendLocalBean bean = list.get(position);
        holder.tvName.setText(bean.getNickname());
        holder.itemView.setOnClickListener(v -> {
            holder.checkBox.setChecked(!holder.checkBox.isChecked());
            if (listener != null) {
                listener.onItemClick(position, bean, holder.checkBox.isChecked());
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName;
        CheckBox checkBox;
        public VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            checkBox = itemView.findViewById(R.id.checkbox);
        }
    }
}