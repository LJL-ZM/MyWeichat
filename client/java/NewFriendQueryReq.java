package com.example.myweixin_client;

public class NewFriendQueryReq {
    String nameOrId;
    public NewFriendQueryReq(String nameOrId){
        this.nameOrId = nameOrId;
    }

    public String getNameOrId() {
        return nameOrId;
    }

    public void setNameOrId(String nameOrId) {
        this.nameOrId = nameOrId;
    }
}