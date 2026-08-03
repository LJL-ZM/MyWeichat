package http;

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