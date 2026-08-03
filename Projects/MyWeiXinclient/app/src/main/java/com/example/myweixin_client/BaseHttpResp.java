package com.example.myweixin_client;


public class BaseHttpResp<T> {
    private int code;
    private String msg;
    private T val;
    public BaseHttpResp(){};
    //构建成功消息
    public static <T> BaseHttpResp<T> onSuccess(T val){
        BaseHttpResp<T> resp = new BaseHttpResp<>();
        resp.code = 0;
        resp.msg = "success";
        resp.val = val;
        return resp;
    }

    //构建错误消息
    public static <T> BaseHttpResp<T> onFail(String msg){
        BaseHttpResp<T> resp = new BaseHttpResp<>();
        resp.code = 1;
        resp.msg = msg;
        resp.val = null;
        return resp;
    }

    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }
    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }
    public T getData() { return val; }
    public void setData(T data) { this.val = data; }
}

