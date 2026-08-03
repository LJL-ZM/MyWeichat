package com.example.myweixin_client;

import android.content.Context;
import android.content.SharedPreferences;

public class SpUtil {
    private static final String SP_NAME = "user_login_info";
    private static SharedPreferences sp;

    public static void init(Context context) {
        if (sp == null) {
            sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        }
    }

    public static void saveLoginInfo(int uid, String nickname) {
        sp.edit()
                .putInt("uid", uid)
                .putString("nickname", nickname)
                .putBoolean("is_login", true)
                .commit();
    }

    public static int getUid() {
        return sp.getInt("uid", 0);
    }

    public static String getNickname() {
        return sp.getString("nickname", "");
    }

    public static boolean isLogin() {
        return sp.getBoolean("is_login", false);
    }

    public static void clearLogin() {
        sp.edit()
                .remove("uid")
                .remove("nickname")
                .remove("is_login")
                .apply();
    }
}