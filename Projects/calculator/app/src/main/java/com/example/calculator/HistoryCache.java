package com.example.calculator;

import java.util.ArrayList;
import java.util.List;

public class HistoryCache {
    private static List<LogDatabaseHelper.HistoryItem> historyItems = new ArrayList<>();
    private HistoryCache(){}

    public static void setHistory(List<LogDatabaseHelper.HistoryItem> list){
        historyItems = list;
    }

    public static List<LogDatabaseHelper.HistoryItem> getHistoryItems() {
        return historyItems;
    }

    public static void insert(LogDatabaseHelper.HistoryItem historyItem){
        historyItems.add(historyItem);
    }

    public static void clear(){
        historyItems.clear();
    }

    public static boolean isEmpty(){
        return historyItems.isEmpty();
    }
}
