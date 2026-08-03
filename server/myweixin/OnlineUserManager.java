package myweixin;

import socket.Message;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class OnlineUserManager {
    private static final OnlineUserManager INSTANCE = new OnlineUserManager();
    private final ConcurrentHashMap<Integer, UserConnection> onlineMap = new ConcurrentHashMap<>();

    private OnlineUserManager() {}

    public static OnlineUserManager getInstance() {
        return INSTANCE;
    }

    public static class UserConnection {
        public final Socket socket;
        public final PrintWriter writer;

        public UserConnection(Socket socket) throws IOException {
            this.socket = socket;
            this.writer = new PrintWriter(socket.getOutputStream(), true);
        }
    }

    public void userOnline(int uid, Socket socket) throws IOException {
        UserConnection connection = new UserConnection(socket);
        onlineMap.put(uid, connection);
    }

    public void userOffline(int uid) {
        onlineMap.remove(uid);
    }

    public boolean isOnline(int uid) {
        return onlineMap.containsKey(uid);
    }

    public UserConnection getConnection(int uid) {
        return onlineMap.get(uid);
    }

    public PrintWriter getWriter(int uid) {
        UserConnection conn = getConnection(uid);
        if (conn == null) {
            return null;
        }
        return conn.writer;
    }
    public Socket getSocket(int uid){
        UserConnection conn = onlineMap.get(uid);
        if(conn == null){
            return null;
        }
        return conn.socket;
    }

    public boolean pushMessage(int targetUid, Message<?> msg) {
        UserConnection conn = getConnection(targetUid);
        if (conn == null) {
            return false;
        }
        try {
            String json = GsonUtil.toJson(msg);
            conn.writer.println(json);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            userOffline(targetUid);
            try {
                conn.socket.close();
            } catch (IOException ignored) {}
            return false;
        }
    }
}