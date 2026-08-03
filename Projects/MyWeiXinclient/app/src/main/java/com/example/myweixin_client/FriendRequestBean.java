package com.example.myweixin_client;

public class FriendRequestBean {
    public static final int UNPROCESS = 0;
    public static final int AGREE = 1;
    public static final int REJECT = 2;
    public int id;
    public int fromUid;
    public String fromNickName;
    public String msg;
    public int status;
    public FriendRequestBean(int id, int fromUid, String fromNickName, String msg, int status) {
        this.id = id;
        this.fromUid = fromUid;
        this.fromNickName = fromNickName;
        this.msg = msg;
        this.status = status;
    }

}
