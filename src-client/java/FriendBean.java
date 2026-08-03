package com.example.myweixin_client;

public class FriendBean {
    public int type;
    public int uid;
    public String name;

    public FriendBean(int type, int uid, String name) {
        this.type = type;
        this.uid = uid;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}