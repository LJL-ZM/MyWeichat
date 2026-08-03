package http;

public class LoginReapData {
    public LoginReapData(int uid, String nickname){
        this.nickname = nickname;
        this.uid = uid;
    }
    private int uid;
    private String nickname;
    public int getUid() { return uid; }    
    public void setUid(int uid) { this.uid = uid; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
}