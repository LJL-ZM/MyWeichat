package http;

public class ReplyReq {
    public static final int AGREE = 1;
    public static final int REFUSE = 2;

    public int requestId;
    public int uid;
    public int statusCode;

    public ReplyReq() {
    }

    private ReplyReq(int requestId, int uid, int status) {
        this.requestId = requestId;
        this.uid = uid;
        this.statusCode = status;
    }

    public static ReplyReq getAgreeInstance(int requestId, int uid) {
        return new ReplyReq(requestId, uid, AGREE);
    }

    public static ReplyReq getRefuseInstance(int requestId, int uid) {
        return new ReplyReq(requestId, uid, REFUSE);
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
}