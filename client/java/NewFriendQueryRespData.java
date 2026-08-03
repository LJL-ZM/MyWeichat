package com.example.myweixin_client;

import java.util.List;

public class NewFriendQueryRespData {
    public static class userData{
        String nickName;
        String name;
        int uid;

        userData(String nickName, String name, int uid) {
            this.uid = uid;
            this.name = name;
            this.nickName = nickName;
        }
    }
    List<userData> list;

    public List<userData> getList() {
        return list;
    }

    public void setList(List<userData> list) {
        this.list = list;
    }
}