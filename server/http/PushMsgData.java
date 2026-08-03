package http;

public class PushMsgData {
    public int otherUid;
    public String nickname;

    public PushMsgData(int otherUid, String nickname) {
        this.otherUid = otherUid;
        this.nickname = nickname;
    }

    public int getOtherUid() {
        return otherUid;
    }

    public String getNickname() {
        return nickname;
    }
}