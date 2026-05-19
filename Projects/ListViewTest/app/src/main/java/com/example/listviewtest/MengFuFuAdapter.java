package com.example.listviewtest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

public class MengFuFuAdapter extends ArrayAdapter<MengFuFu> {
    private int resourceId;

    public MengFuFuAdapter(@NonNull Context context, int resourceId, @NonNull List<MengFuFu> objects) {
        super(context, resourceId, objects);
        this.resourceId = resourceId;
    }

    public View getView(int position, View convertView, ViewGroup parent){
        MengFuFu mengfufu = getItem(position);
        View view;
        ViewHolder viewHolder;
        if(convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.imageView = (ImageView) view.findViewById(R.id.mengfufu_image);
            viewHolder.textView = (TextView) view.findViewById(R.id.mengfufu_name);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.textView.setText(mengfufu.getName());
        viewHolder.imageView.setImageResource(mengfufu.getImageId());
        return view;
    }
    class ViewHolder{
        ImageView imageView;
        TextView textView;
    }
}
