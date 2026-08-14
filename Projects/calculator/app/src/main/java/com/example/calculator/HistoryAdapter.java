package com.example.calculator;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calculator.databinding.ItemDateHeaderBinding;
import com.example.calculator.databinding.ItemHistoryRecordBinding;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_DATE = 0;
    private static final int TYPE_RECORD = 1;
    private List<LogDatabaseHelper.HistoryItem> dataList;
    public HistoryAdapter(List<LogDatabaseHelper.HistoryItem> dataList) {
        this.dataList = dataList;
    }

    @Override
    public int getItemViewType(int position) {
        return dataList.get(position).isDateHeader ? TYPE_DATE : TYPE_RECORD;
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == TYPE_DATE){
            ItemDateHeaderBinding binding = ItemDateHeaderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new DateHolder(binding);
        } else {
            ItemHistoryRecordBinding binding = ItemHistoryRecordBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new RecordHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        LogDatabaseHelper.HistoryItem item = dataList.get(position);
        if(holder instanceof DateHolder){
            DateHolder dateHolder = (DateHolder) holder;
            dateHolder.setDate(item.dateText);
        } else {
            RecordHolder recordHolder = (RecordHolder) holder;
            recordHolder.setHistory(item.expression, item.result);
        }
    }


    static class DateHolder extends RecyclerView.ViewHolder{
        ItemDateHeaderBinding binding;
        public DateHolder(ItemDateHeaderBinding binding){
            super(binding.getRoot());
            this.binding = binding;
        }
        public void setDate(String date){
            binding.tvDate.setText(date);
        }
    }

    static class RecordHolder extends RecyclerView.ViewHolder{
        ItemHistoryRecordBinding binding;
        public RecordHolder(ItemHistoryRecordBinding binding){
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setHistory(String expression, String result){
            binding.tvResult.setText("=" + result);
            binding.tvExpression.setText(expression);
        }
    }
}
