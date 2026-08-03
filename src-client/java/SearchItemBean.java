package com.example.myweixin_client;

public class SearchItemBean {
    // 类型常量：新朋友/我的好友/分割线
    public static final int TYPE_NEW_FRIEND = 0;
    public static final int TYPE_MY_FRIEND = 1;
    public static final int TYPE_DIVIDER = 2;

    public int itemType;
    public int uid;
    public String name;
    public String dividerTitle;

    // 新朋友/我的好友构造
    public SearchItemBean(int itemType, int uid, String name) {
        this.itemType = itemType;
        this.uid = uid;
        this.name = name;
    }

    // 分割线构造
    public SearchItemBean(String dividerTitle) {
        this.itemType = TYPE_DIVIDER;
        this.dividerTitle = dividerTitle;
    }
}