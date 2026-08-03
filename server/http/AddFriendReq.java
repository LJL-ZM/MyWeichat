package http;

public class AddFriendReq {
    private int friendId;
    private int selfId;
    private String helloMsg;
    private String nickName;

    public AddFriendReq(int selfId, int friendId, String helloMsg, String nickName) {
        this.friendId = friendId;
        this.selfId = selfId;
        this.helloMsg = helloMsg;
        this.nickName = nickName;
    }

    public int getFriendId() {
        return friendId;
    }

    public void setFriendId(int friendId) {
        this.friendId = friendId;
    }

    public String getHelloMsg() {
        return helloMsg;
    }

    public void setHelloMsg(String helloMsg) {
        this.helloMsg = helloMsg;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public int getSelfId() {
        return selfId;
    }

    public void setSelfId(int selfId) {
        this.selfId = selfId;
    }
}