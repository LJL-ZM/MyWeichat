package socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import myweixin.ChatDao;
import myweixin.ChatMessage;
import myweixin.FriendRequestDao;
import myweixin.GsonUtil;
import myweixin.OnlineUserManager;

public class ClientHandler implements Runnable{
    private final Socket clientSocket;
    private int currentUid = -1;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
            String line;
            while((line = reader.readLine()) != null) {
                try{
                    StringBuilder sbLog1 = new StringBuilder();
                    sbLog1.append("收到原始报文：");
                    sbLog1.append(line);
                    System.out.println(sbLog1.toString());

                    Message<?> baseMsg = GsonUtil.fromJson(line, Message.class);
                    if(baseMsg == null || baseMsg.getType() == null){
                        continue;
                    }
                    String type = baseMsg.getType();
                    switch (type) {
                        case "SOCKET_LOGIN":
                            LoginRequest req = GsonUtil.fromJson(GsonUtil.toJson(baseMsg.getData()), LoginRequest.class);
                            int loginUid = req.getUid();
                            handleLogin(loginUid);
                            break;
                        case "heartbeat":
                            sendMsg("heartbeat_ack", null);
                            break;
                        default:
                            break;
                    }
                }catch (Exception e){
                    System.out.println("【单条报文解析失败，跳过】");
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            cleanUp();
        }
    }

    public void handleLogin(int uid){
        if(uid > 0) {
            Socket oldSocket = OnlineUserManager.getInstance().getSocket(uid);
            if(oldSocket != null) {
                try{
                    sendToSocket(oldSocket, "kick", new CommonResponse(-1, "账号在其它设备登录"));
                    oldSocket.close();
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
            this.currentUid = uid;
            try {
                OnlineUserManager.getInstance().userOnline(uid, clientSocket);
                StringBuilder sbLog2 = new StringBuilder();
                sbLog2.append("✅ 用户上线成功 uid=");
                sbLog2.append(uid);
                System.out.println(sbLog2.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            sendMsg("login_ack", new CommonResponse(0, "连接登陆成功"));
            ChatDao chatDao = new ChatDao();
            List<Map<String, Object>> offlineMsgs = chatDao.getOfflineMessages(uid);
            if (offlineMsgs != null && !offlineMsgs.isEmpty()) {
                for (Map<String, Object> map : offlineMsgs) {
                    ChatMessage msg = myweixin.GsonUtil.fromJson(
                            myweixin.GsonUtil.toJson(map), ChatMessage.class);
                    Message<ChatMessage> pushMsg = new Message<>("NEW_CHAT_MSG", msg);
                    OnlineUserManager.getInstance().pushMessage(uid, pushMsg);
                }
                chatDao.updateLastReadMsgIdToMax(uid);
            }
            pushMsgWhenOffline(uid);
        } else {
            sendMsg("login_ack", new CommonResponse(-1 , "身份校验失败"));
            cleanUp();
        }
    }

    private void pushMsgWhenOffline(int uid){
        try {
            StringBuilder sbLogUid = new StringBuilder();
            sbLogUid.append("【离线推送】用户上线 uid=");
            sbLogUid.append(uid);
            System.out.println(sbLogUid.toString());

            List<FriendRequestPushMsg> requestList = FriendRequestDao.queryUnPushRequest(uid);

            StringBuilder sbLogSize = new StringBuilder();
            sbLogSize.append("【离线推送】查到待推送申请数量：");
            sbLogSize.append(requestList.size());
            System.out.println(sbLogSize.toString());

            if(requestList == null || requestList.isEmpty()){
                System.out.println("【离线推送】无好友申请，直接返回");
                return;
            }

            for(FriendRequestPushMsg msg : requestList){
                StringBuilder sbLogId = new StringBuilder();
                sbLogId.append("【离线推送】正在推送申请 id=");
                sbLogId.append(msg.getRequestId());
                System.out.println(sbLogId.toString());

                sendMsg("NEW_FRIEND_APPLY", msg);
                FriendRequestDao.markPushed(msg.getRequestId());
            }
        }catch (Exception e){
            System.out.println("【离线推送】处理发生异常");
            e.printStackTrace();
        }
    }

    public <T> void sendMsg(String type, T data) {
        sendToSocket(clientSocket, type, data);
    }

    private static <T> void sendToSocket(Socket socket, String type, T data) {
        try {
            Message<T> msg = new Message<>(type, data);
            String jsonStr = GsonUtil.toJson(msg);
            StringBuilder sb = new StringBuilder();
            sb.append(jsonStr);
            sb.append("\n");
            byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
            socket.getOutputStream().write(bytes);
            socket.getOutputStream().flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void cleanUp() {
        if (currentUid != -1) {
            OnlineUserManager.getInstance().userOffline(currentUid);
            currentUid = -1;
        }
        try {
            if (!clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}