package http;


import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import myweixin.FriendDao;
import myweixin.FriendRequestDao;
import myweixin.GsonUtil;
import myweixin.OnlineUserManager;
import myweixin.SqlManager;
import myweixin.FriendRemoteBeanServer;
import myweixin.FriendRequestRemoteBeanServer;
import myweixin.GetFriendReqServer;
import myweixin.GetPendingReqServer;
import socket.FriendRequestPushMsg;
import socket.Message;
import myweixin.ChatDao;
import myweixin.ChatMessage;

import socket.SocketServer;

public class HttpServer {
    private final int port;
    private ServerSocket serverSocket;
    private volatile boolean running = false;

    public HttpServer(int port){
        this.port = port;
    }

    public void start(){
        new Thread(this::runServer, "Http-Server-Thread").start();
    }
    public int getPort(){
        return port;
    }

    private void runServer(){
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            while(running){
                Socket client = serverSocket.accept();
                new Thread(() -> handleSingleRequest(client)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally{
            stop();
        }
    }

    public void stop(){
        running = false;
        try {
            if(serverSocket != null){
                serverSocket.close();
                serverSocket = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleSingleRequest(Socket client){
        byte[] fullBuffer = new byte[4096];
        try (
            java.io.InputStream rawIn = client.getInputStream();
            java.io.OutputStream out = client.getOutputStream()
        ) {
            int totalRead = rawIn.read(fullBuffer);
            String fullText = new String(fullBuffer, 0, totalRead, StandardCharsets.UTF_8);
            String[] lines = fullText.split("\r\n");
            String firstLine = lines[0];
            if(firstLine == null){
                return;
            }
            String[] lineArr = firstLine.split(" ");
            String method = lineArr[0];
            String path = lineArr[1];
    
            int contentLength = 0;
            int headerEndIndex = 0;
            for(int i=0;i<lines.length;i++){
                String line = lines[i];
                if(line.isEmpty()){
                    headerEndIndex = i;
                    break;
                }
                if(line.startsWith("Content-Length")){
                    contentLength = Integer.parseInt(line.split(":")[1].trim());
                }
            }
    
            String jsonBody = "";
            if(contentLength > 0 && headerEndIndex + 1 < lines.length){
                StringBuilder bodySb = new StringBuilder();
                for(int i = headerEndIndex + 1; i < lines.length; i++){
                    bodySb.append(lines[i]);
                }
                jsonBody = bodySb.toString();
            }
    
            String respJson = dispatchApi(path, method, jsonBody);
            byte[] respBytes = buildHttpResponse(respJson);
            out.write(respBytes);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String dispatchApi(String path, String method, String jsonBody){
        System.out.println("【路由】path="+path + " body=["+jsonBody+"]");
        if(path == null || method == null || jsonBody == null){
            return GsonUtil.toJson(BaseHttpResp.onFail("请求不合法"));
        }

        if("/login".equals(path) && "POST".equalsIgnoreCase(method)){
            return loginHandle(jsonBody);
        } else if("/register".equals(path) && "POST".equalsIgnoreCase(method)){
            return registerHandle(jsonBody);
        } else if("/searchNewFriend".equals(path) && "POST".equalsIgnoreCase(method)) { 
            return searchNewFriendHandle(jsonBody);
        } else if("/addFriend".equals(path) && "POST".equalsIgnoreCase(method))  {
            return sendNewFriendReqHandle(jsonBody);
        } else if("/agree_friend".equals(path) && "POST".equalsIgnoreCase(method)) {
            return friendRequestAgreeHandle(jsonBody);
        } else if("/refuse_friend".equals(path) && "POST".equalsIgnoreCase(method)) {
            return friendRequestRefuseHandle(jsonBody);
        } else if("/getAllFriend".equals(path) && "POST".equalsIgnoreCase(method)){
            return getAllFriendHandle(jsonBody);
        }else if("/getPendingRequest".equals(path) && "POST".equalsIgnoreCase(method)){
            return getPendingRequestHandle(jsonBody);
        } else if("/get_my_session".equals(path) && "POST".equalsIgnoreCase(method)){
            return getMySessionHandle(jsonBody);
        } else if("/get_history_msg".equals(path) && "POST".equalsIgnoreCase(method)){
            return getHistoryMsgHandle(jsonBody);
        } else if("/send_msg".equals(path) && "POST".equalsIgnoreCase(method)){
            return sendMsgHandle(jsonBody);
        } else if("/create_single_chat".equals(path) && "POST".equalsIgnoreCase(method)){
            return createSingleChatHandle(jsonBody);
        } else if("/create_group_chat".equals(path) && "POST".equalsIgnoreCase(method)){
            return createGroupChatHandle(jsonBody);
        } else if("/get_group_members".equals(path) && "POST".equalsIgnoreCase(method)){
            return getGroupMembersHandle(jsonBody);
        } else {
            return GsonUtil.toJson(BaseHttpResp.onFail("没有对应接口"));
        }
    }

    private byte[] buildHttpResponse(String respJson){
        String httpResp = "HTTP/1.1 200 OK\r\n" +
        "Content-Type: application/json;charset=UTF-8\r\n" +
        "Content-Length: " + respJson.getBytes(StandardCharsets.UTF_8).length + "\r\n" +
        "\r\n" +
        respJson;
        return httpResp.getBytes(StandardCharsets.UTF_8);
    }

    private String loginHandle(String jsonBody){
        try {
            LoginReq req = GsonUtil.fromJson(jsonBody, LoginReq.class);
            String username = req.getUsername();
            String password = req.getPassword();
            if(username == null || password == null){
                return GsonUtil.toJson(BaseHttpResp.onFail("账号或密码不能为空"));
            }
            List<Map<String, Object>> list = SqlManager.listByCondition("user", "username='" + username + "'");
            if(list.isEmpty()){
                return GsonUtil.toJson(BaseHttpResp.onFail("用户不存在"));
            }
            Map<String, Object> column = list.get(0);
            String usernameInSql = (String)column.get("username");
            String passwordInSql = (String)column.get("password");
            if(username.equals(usernameInSql) && password.equals(passwordInSql)){
                int uid = (Integer) column.get("uid");
                String nickname = (String) column.get("nickname");
                return GsonUtil.toJson(BaseHttpResp.onSuccess(new LoginReapData(uid, nickname)));
            }else{
                return GsonUtil.toJson(BaseHttpResp.onFail("密码错误"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return GsonUtil.toJson(BaseHttpResp.onFail("服务器异常"));
        }
    }

    private String registerHandle(String jsonBody){
        try {
            RegisterReq req = GsonUtil.fromJson(jsonBody, RegisterReq.class);
            String username = req.getUsername();
            String password = req.getPassword();
            String nickname = req.getNickname();
            if(username == null || password == null || nickname == null){
                return GsonUtil.toJson(BaseHttpResp.onFail("信息不能为空"));
            }
            List<Map<String, Object>> list = SqlManager.listByCondition("user", "username='" + username + "'");
            if(!list.isEmpty()){
                return GsonUtil.toJson(BaseHttpResp.onFail("用户已存在"));
            }
            SqlManager.insert("INSERT INTO user(username,nickname,password) " +
            "VALUES('" + username + "','" + nickname + "','" + password + "')");
            return GsonUtil.toJson(BaseHttpResp.onSuccess(null));
        } catch (Exception e) {
            e.printStackTrace();
            return GsonUtil.toJson(BaseHttpResp.onFail("服务器异常"));
        }
    }
    private String searchNewFriendHandle(String jsonBody) {
        try{
            NewFriendQueryReq req = GsonUtil.fromJson(jsonBody, NewFriendQueryReq.class);
            String keyword = req.getNameOrId();
            if(keyword == null || keyword.trim().isEmpty()) {
                return GsonUtil.toJson(BaseHttpResp.onFail("关键词不能为空"));
            }
            keyword = keyword.trim();
            StringBuilder condition = new StringBuilder();
            condition.append("nickname like '%").append(keyword).append("%'")
                .append(" or username like '%").append(keyword).append("%'");
            try{
                int uid = Integer.parseInt(keyword);
                condition.append(" or uid = ").append(uid);
            } catch(NumberFormatException ignored) {}
            String sqlCondition = condition.toString() + "limit 20";
            List<Map<String, Object>> userList = SqlManager.listByCondition("user", sqlCondition);
            NewFriendQueryRespData respData = new NewFriendQueryRespData();
            List<NewFriendQueryRespData.userData> dataList = new java.util.ArrayList<>();
            for (Map<String, Object> user : userList) {
                int uid = (Integer) user.get("uid");
                String username = (String) user.get("username");
                String nickname = (String) user.get("nickname");
                dataList.add(new NewFriendQueryRespData.userData(nickname, username, uid));
            }
            respData.setList(dataList);
    
            return GsonUtil.toJson(BaseHttpResp.onSuccess(respData));    
        } catch(Exception e) {
            e.printStackTrace();
            return GsonUtil.toJson(BaseHttpResp.onFail("搜索失败，服务器异常"));
        }
    }
    public String sendNewFriendReqHandle(String body){
        
        AddFriendReq reqData = GsonUtil.GSON.fromJson(body, AddFriendReq.class);
        int selfId = reqData.getSelfId();
        int targetUid = reqData.getFriendId();
        String helloMsg = reqData.getHelloMsg();
        String nickName = reqData.getNickName();
        System.out.println("[好友申请推送]目标uid:"+targetUid+" ,是否在线:"+OnlineUserManager.getInstance().isOnline(targetUid));
        FriendRequestDao dao = new FriendRequestDao();
        long insertId = dao.sendRequest(selfId, targetUid, helloMsg, nickName);

        if (insertId > 0) {
            FriendRequestPushMsg pushBean = new FriendRequestPushMsg();
            pushBean.setRequestId((int) insertId);
            pushBean.setFromUid(selfId);
            pushBean.setFromNickname(nickName);
            pushBean.setReqMsg(helloMsg);

            Message<FriendRequestPushMsg> socketMsg = new Message<>("NEW_FRIEND_APPLY", pushBean);
            OnlineUserManager onlineManager = OnlineUserManager.getInstance();
            if (onlineManager.isOnline(targetUid)) {
                boolean sendSuccess = onlineManager.pushMessage(targetUid, socketMsg);
                if (sendSuccess) {
                    dao.markPushed((int) insertId);
                }
            }
            return GsonUtil.toJson(BaseHttpResp.onSuccess("好友申请已发送"));
        }else{
            return GsonUtil.toJson(BaseHttpResp.onFail("发送失败"));
        }
    }

    private String friendRequestAgreeHandle(String jsonBody) {
        try {
            ReplyReq req = GsonUtil.GSON.fromJson(jsonBody, ReplyReq.class);
            int requestId = req.getRequestId();
            int myUid = req.getUid();
            System.out.println("[同意申请调试] requestId=" + requestId + " myUid=" + myUid);
    
            FriendRequestDao requestDao = new FriendRequestDao();
            Map<String, Object> requestRecord = requestDao.getById(requestId);
            if (requestRecord == null) {
                System.out.println("[调试] 数据库查不到这条requestId");
                return GsonUtil.toJson(BaseHttpResp.onFail("该好友申请不存在"));
            }
            int status = ((Number) requestRecord.get("status")).intValue();
            int fromUid = ((Number) requestRecord.get("from_uid")).intValue();
            int toUid = ((Number) requestRecord.get("to_uid")).intValue();
            System.out.println("[调试] 记录toUid=" + toUid + " status=" + status);
    
            if (toUid != myUid || status != 0) {
                System.out.println("[调试] 条件不满足！toUid!=myUid:" + (toUid != myUid) + " status!=0:" + (status != 0));
                return GsonUtil.toJson(BaseHttpResp.onFail("申请已处理或无权限操作"));
            }
    
            requestDao.updateStatus(requestId, 1);
    
            List<Map<String, Object>> friendUserInfo = SqlManager.listByCondition("user", "uid=" + fromUid);
            Map<String, Object> friendInfo = friendUserInfo.get(0);
            String friendNick = (String) friendInfo.get("nickname");
    
            List<Map<String, Object>> userInfoList = SqlManager.listByCondition("user", "uid=" + myUid);
            Map<String, Object> selfInfo = userInfoList.get(0);
            String myNick = (String) selfInfo.get("nickname");
    
            FriendDao friendDao = new FriendDao();
            friendDao.addFriend(myUid, fromUid, myNick, friendNick);
    
            PushMsgData pushData = new PushMsgData(myUid, myNick);
            Message<PushMsgData> socketMsg = new Message<>("FRIEND_APPROVE", pushData);
            OnlineUserManager onlineManager = OnlineUserManager.getInstance();
            if (onlineManager.isOnline(fromUid)) {
                onlineManager.pushMessage(fromUid, socketMsg);
            }
    
            return GsonUtil.toJson(BaseHttpResp.onSuccess("添加好友成功"));
        } catch (Exception e) {
            e.printStackTrace();
            return GsonUtil.toJson(BaseHttpResp.onFail("服务器异常"));
        }
    }

    private String friendRequestRefuseHandle(String jsonBody){
        try {
            ReplyReq req = GsonUtil.GSON.fromJson(jsonBody, ReplyReq.class);
            int requestId = req.getRequestId();
            int myUid = req.getUid();
            System.out.println("[拒绝申请调试] requestId=" + requestId + " myUid=" + myUid);
    
            FriendRequestDao dao = new FriendRequestDao();
            Map<String,Object> record = dao.getById(requestId);
            if(record == null){
                System.out.println("[调试] 数据库查不到这条requestId");
                return GsonUtil.toJson(BaseHttpResp.onFail("申请不存在"));
            }
            int toUid = ((Number)record.get("to_uid")).intValue();
            int status = ((Number)record.get("status")).intValue();
            System.out.println("[调试] 记录toUid=" + toUid + " status=" + status);
    
            if(toUid != myUid || status != 0){
                System.out.println("[调试] 条件不满足！toUid!=myUid:" + (toUid != myUid) + " status!=0:" + (status != 0));
                return GsonUtil.toJson(BaseHttpResp.onFail("无法操作该申请"));
            }
            dao.updateStatus(requestId,2);
            return GsonUtil.toJson(BaseHttpResp.onSuccess("已拒绝好友申请"));
        }catch (Exception e){
            e.printStackTrace();
            return GsonUtil.toJson(BaseHttpResp.onFail("服务器异常"));
        }
    }

    private String getAllFriendHandle(String jsonBody){
        try {
            GetFriendReqServer req = GsonUtil.fromJson(jsonBody, GetFriendReqServer.class);
            int uid = req.uid;
            System.out.println("得到一条好友列表刷新申请来自"+uid);
            FriendDao dao = new FriendDao();
            List<Map<String,Object>> friendList = dao.getMyAllFriend(uid);
            List<FriendRemoteBeanServer> result = new java.util.ArrayList<>();
            for(Map<String,Object> map : friendList){
                FriendRemoteBeanServer bean = new FriendRemoteBeanServer();
                bean.friendUid = ((Number)map.get("friend_uid")).intValue();
                bean.nickname = (String) map.get("friend_nickname");
                result.add(bean);
            }
            return GsonUtil.toJson(BaseHttpResp.onSuccess(result));
        }catch (Exception e){
            e.printStackTrace();
            return GsonUtil.toJson(BaseHttpResp.onFail("服务器异常"));
        }
    }

    private String getPendingRequestHandle(String jsonBody){
        try{
            GetPendingReqServer req = GsonUtil.fromJson(jsonBody, GetPendingReqServer.class);
            int toUid = req.toUid;
            FriendRequestDao dao = new FriendRequestDao();
            List<Map<String,Object>> list = dao.getUnHandledRequest(toUid);
            List<FriendRequestRemoteBeanServer> resList = new java.util.ArrayList<>();
            for(Map<String,Object> map : list){
                FriendRequestRemoteBeanServer bean = new FriendRequestRemoteBeanServer();
                bean.requestId = ((Number)map.get("id")).intValue();
                bean.fromUid = ((Number)map.get("from_uid")).intValue();
                bean.fromNickname = (String) map.get("from_nickname");
                bean.reqMsg = (String) map.get("req_msg");
                resList.add(bean);
            }
            return GsonUtil.toJson(BaseHttpResp.onSuccess(resList));
        }catch (Exception e){
            e.printStackTrace();
            return GsonUtil.toJson(BaseHttpResp.onFail("服务器异常"));
        }
    }
    private String getMySessionHandle(String jsonBody){
        try {
            GetFriendReqServer req = GsonUtil.fromJson(jsonBody, GetFriendReqServer.class);
            ChatDao dao = new ChatDao();
            List<Map<String,Object>> list = dao.getUserSessionList(req.uid);
            for (Map<String,Object> row : list) {
                Object t = row.get("chatType");
                int chatType = (t instanceof Number) ? ((Number) t).intValue() : 0;
                if (chatType == 1) {
                    Object name = row.get("chatName");
                    if (name == null || name.toString().isEmpty()) {
                        Object peer = row.get("peerNickname");
                        if (peer != null && !peer.toString().isEmpty()) {
                            row.put("chatName", peer.toString());
                        }
                    }
                }
                row.remove("peerNickname");
            }
            return GsonUtil.toJson(BaseHttpResp.onSuccess(list));
        } catch (Exception e) {
            e.printStackTrace();
            return GsonUtil.toJson(BaseHttpResp.onFail("获取会话列表失败"));
        }
    }

    private String getHistoryMsgHandle(String jsonBody){
        try {
            Map<String, Object> req = GsonUtil.fromJson(jsonBody, Map.class);
            int chatId = ((Number) req.get("chatId")).intValue();
            int page = req.get("page") == null ? 1 : ((Number) req.get("page")).intValue();
            int pageSize = req.get("pageSize") == null ? 20 : ((Number) req.get("pageSize")).intValue();
            ChatDao dao = new ChatDao();
            List<Map<String,Object>> list = dao.getHistoryMessage(chatId, page, pageSize);
            return GsonUtil.toJson(BaseHttpResp.onSuccess(list));
        } catch (Exception e) {
            e.printStackTrace();
            return GsonUtil.toJson(BaseHttpResp.onFail("获取历史消息失败"));
        }
    }

    private String sendMsgHandle(String jsonBody){
        try {
            ChatMessage req = GsonUtil.fromJson(jsonBody, ChatMessage.class);
            List<Map<String, Object>> userList = SqlManager.listByCondition("user", "uid=" + req.fromUid);
            String nick = userList.isEmpty() ? "" : (String) userList.get(0).get("nickname");
    
            ChatMessage msg = new ChatMessage();
            msg.chatId = req.chatId;
            msg.fromUid = req.fromUid;
            msg.fromNickname = nick;
            msg.msgType = 1;
            msg.content = req.content;
    
            ChatDao dao = new ChatDao();
            long msgId = dao.insertMessage(msg);
            msg.msgId = msgId;
            msg.sendTime = System.currentTimeMillis();
    
            dao.updateSessionLastMsg(req.chatId, req.content, msgId);
    
            List<Integer> memberUids = dao.getSessionMemberUids(req.chatId, req.fromUid);
            OnlineUserManager onlineManager = OnlineUserManager.getInstance();
            socket.Message<ChatMessage> socketMsg = new socket.Message<>("NEW_CHAT_MSG", msg);
            for (Integer uid : memberUids) {
                if (onlineManager.isOnline(uid)) {
                    boolean ok = onlineManager.pushMessage(uid, socketMsg);
                    if (ok) {
                        dao.updateLastReadMsgId(req.chatId, uid, msgId);
                    }
                }
            }
    
            return GsonUtil.toJson(BaseHttpResp.onSuccess(msg));
        } catch (Exception e) {
            e.printStackTrace();
            return GsonUtil.toJson(BaseHttpResp.onFail("发送消息失败"));
        }
    }

    private String createSingleChatHandle(String jsonBody){
        try {
            Map<String, Object> req = GsonUtil.fromJson(jsonBody, Map.class);
            int uidA = ((Number) req.get("uidA")).intValue();
            int uidB = ((Number) req.get("uidB")).intValue();
            String nickA = (String) req.get("nickA");
            String nickB = (String) req.get("nickB");
            ChatDao dao = new ChatDao();
            int chatId = dao.createSingleChat(uidA, uidB, nickA, nickB);
            return GsonUtil.toJson(BaseHttpResp.onSuccess(chatId));
        } catch (Exception e) {
            e.printStackTrace();
            return GsonUtil.toJson(BaseHttpResp.onFail("创建会话失败"));
        }
    }

    private String createGroupChatHandle(String jsonBody){
        try {
            Map<String, Object> req = GsonUtil.fromJson(jsonBody, Map.class);
            String groupName = (String) req.get("groupName");
            int ownerUid = ((Number) req.get("ownerUid")).intValue();
            String ownerNick = (String) req.get("ownerNick");
            List<Map<String, Object>> members = (List<Map<String, Object>>) req.get("members");
    
            List<Integer> uidList = new java.util.ArrayList<>();
            List<String> nickList = new java.util.ArrayList<>();
            for (Map<String, Object> m : members) {
                uidList.add(((Number) m.get("uid")).intValue());
                nickList.add((String) m.get("nickname"));
            }
    
            ChatDao dao = new ChatDao();
            int chatId = dao.createGroupChat(groupName, ownerUid, ownerNick, uidList, nickList);
            return GsonUtil.toJson(BaseHttpResp.onSuccess(chatId));
        } catch (Exception e) {
            e.printStackTrace();
            return GsonUtil.toJson(BaseHttpResp.onFail("创建群聊失败"));
        }
    }

    private String getGroupMembersHandle(String jsonBody){
        try {
            Map<String, Object> req = GsonUtil.fromJson(jsonBody, Map.class);
            int chatId = ((Number) req.get("chatId")).intValue();
            ChatDao dao = new ChatDao();
            List<Map<String,Object>> list = dao.getGroupMembers(chatId);
            return GsonUtil.toJson(BaseHttpResp.onSuccess(list));
        } catch (Exception e) {
            e.printStackTrace();
            return GsonUtil.toJson(BaseHttpResp.onFail("获取群成员失败"));
        }
    }

    public static void main(String[] args){
        new Thread(() -> {
            new HttpServer(8082).start();
        }).start();
        new Thread(() -> {
            new SocketServer().start();
        }).start();
    }
}