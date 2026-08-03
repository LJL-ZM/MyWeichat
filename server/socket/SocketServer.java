package socket;

import java.net.ServerSocket;
import java.net.Socket;

public class SocketServer {
    private static final int PORT = 8085;
    private volatile boolean isRunning = true;
    public void start(){
        try(ServerSocket serverSocket = new ServerSocket(PORT)){
            System.out.println("socket服务器启动");
            while(isRunning){
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}