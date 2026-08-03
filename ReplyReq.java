package com.example.myweixin_client;

public class ReplyReq {
    public static final int AGREE = 1;
    public static final int REFUSE = 2;

    public int requestId;
    public int statusCode;
    public int uid; // 当前登录用户uid

    public ReplyReq() {

    }

    private ReplyReq(int requestId, int status, int uid) {
        this.requestId = requestId;
        this.statusCode = status;
        this.uid = uid;
    }

    public static ReplyReq getAgreeInstance(int requestId, int uid) {
        return new ReplyReq(requestId, AGREE, uid);
    }

    public static ReplyReq getRefuseInstance(int requestId, int uid) {
        return new ReplyReq(requestId, REFUSE, uid);
    }
}