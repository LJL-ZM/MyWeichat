package com.example.recyclerviewtest;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MengFuFuAdapter extends RecyclerView.Adapter<MengFuFuAdapter.ViewHolder> {
    private List<MengFuFu> mMengList;
    static class ViewHolder extends RecyclerView.ViewHolder{
        ImageView mengImage;
        TextView mengText;

        public ViewHolder(View view){
            super(view);
            mengImage = (ImageView)view.findViewById(R.id.mengfufu_image);
            mengText = (TextView) view.findViewById(R.id.mengfufu_name);
        }
    }

    public MengFuFuAdapter(List<MengFuFu> list){
        mMengList = list;
    }
    @Override
    public int getItemCount() {
        return mMengList.size();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MengFuFu mengFuFu = mMengList.get(position);
        holder.mengText.setText(mengFuFu.getName());
        holder.mengImage.setImageResource(mengFuFu.getImageId());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mengfufu_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }
}
