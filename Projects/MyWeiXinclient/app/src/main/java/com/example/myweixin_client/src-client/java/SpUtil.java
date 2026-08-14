package com.example.myweixin_client;

import android.content.Context;
import android.content.SharedPreferences;

public class SpUtil {
    private static final String SP_NAME = "user_login_info";
    private static SharedPreferences sp;

    // 初始化，在Application或MainActivity先调用一次
    public static void init(Context context) {
        if (sp == null) {
            sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        }
    }

    // 保存登录全部信息
    public static void saveLoginInfo(int uid, String nickname) {
        sp.edit()
                .putInt("uid", uid)
                .putString("nickname", nickname)
                .putBoolean("is_login", true)
                .commit();
    }

    // 获取uid，未登录返回0
    public static int getUid() {
        return sp.getInt("uid", 0);
    }

    // 获取昵称
    public static String getNickname() {
        return sp.getString("nickname", "");
    }

    // 判断是否登录
    public static boolean isLogin() {
        return sp.getBoolean("is_login", false);
    }

    // 清空登录信息（退出登录调用）
    public static void clearLogin() {
        sp.edit()
                .remove("uid")
                .remove("nickname")
                .remove("is_login")
                .apply();
    }
}
