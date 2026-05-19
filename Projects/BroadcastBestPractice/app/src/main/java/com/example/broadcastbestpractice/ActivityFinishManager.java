package com.example.broadcastbestpractice;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

public class ActivityFinishManager {
    private static List<Activity> list = new ArrayList<>();
    public static void add(Activity activity){
        list.add(activity);
    }

    public static void remove(Activity activity){
        list.remove(activity);
    }

    public static void finishAll(){
        for(Activity activity : list){
            activity.finish();
        }
    }
}
