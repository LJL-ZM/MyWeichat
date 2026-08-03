package com.example.myweixin_client;
public class FriendLocalBean {
    private int friendUid;
    private String nickname;
    private String remark;

    public FriendLocalBean(int friendUid, String nickname) {
        this.friendUid = friendUid;
        this.nickname = nickname;
    }

    public int getFriendUid() {
        return friendUid;
    }
    public void setFriendUid(int friendUid) {
        this.friendUid = friendUid;
    }
    public String getNickname() {
        return nickname;
    }
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    public String getRemark() {
        return remark;
    }
    public void setRemark(String remark) {
        this.remark = remark;
    }
}