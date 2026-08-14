package com.example.myweixin;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ChatService extends Service {
    private static final String TAG = "ChatService";
    private static final String SERVER_IP = "43.138.32.230";
    private static final int SERVER_PORT = 8666;
    private static final int RECONNECT_DELAY = 5000;

    private Socket socket;
    private Scanner scannerNet;
    private PrintWriter writerNet;
    private Handler handler;
    private boolean isRunning = true;
    private boolean isReconnecting = false;

    private OnMessageReceivedListener messageListener;
    private OnConnectionStatusListener connectionListener;

    public interface OnMessageReceivedListener {
        void onMessageReceived(String message);
    }

    public interface OnConnectionStatusListener {
        void onConnected();
        void onDisconnected();
        void onReconnecting();
    }

    public class ChatBinder extends Binder {
        public ChatService getService() {
            return ChatService.this;
        }
    }

    private final IBinder binder = new ChatBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());
        new Thread(new Runnable() {
            @Override
            public void run() {
                connect();
            }
        }).start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        disconnect();
        super.onDestroy();
    }

    private void connect() {
        while (isRunning) {
            try {
                if (socket != null && socket.isConnected()) {
                    return;
                }

                if (!isReconnecting) {
                    isReconnecting = true;
                    notifyReconnecting();
                }

                Log.d(TAG, "Connecting to server...");
                socket = new Socket(SERVER_IP, SERVER_PORT);
                scannerNet = new Scanner(socket.getInputStream());
                writerNet = new PrintWriter(socket.getOutputStream(), true);

                isReconnecting = false;
                notifyConnected();
                Log.d(TAG, "Connected to server");

                listenForMessages();

            } catch (IOException e) {
                Log.e(TAG, "Connection failed: " + e.getMessage());
                disconnect();

                if (isRunning) {
                    try {
                        Thread.sleep(RECONNECT_DELAY);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }

    private void listenForMessages() {
        while (isRunning && socket != null && socket.isConnected()) {
            try {
                if (scannerNet != null && scannerNet.hasNextLine()) {
                    String message = scannerNet.nextLine();
                    notifyMessageReceived(message);
                } else {
                    Thread.sleep(100);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error reading message: " + e.getMessage());
                break;
            }
        }

        if (isRunning) {
            notifyDisconnected();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    connect();
                }
            }).start();
        }
    }

    private void disconnect() {
        try {
            if (scannerNet != null) {
                scannerNet.close();
                scannerNet = null;
            }
            if (writerNet != null) {
                writerNet.close();
                writerNet = null;
            }
            if (socket != null) {
                socket.close();
                socket = null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Error disconnecting: " + e.getMessage());
        }
    }

    public void sendMessage(final String message) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (writerNet != null && socket != null && !socket.isClosed()) {
                        writerNet.println(message);
                        writerNet.flush();
                        Log.d(TAG, "Message sent: " + message);
                    } else {
                        Log.e(TAG, "Cannot send message - not connected");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error sending message: " + e.getMessage());
                    disconnect();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            connect();
                        }
                    }).start();
                }
            }
        }).start();
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    public void setOnMessageReceivedListener(OnMessageReceivedListener listener) {
        this.messageListener = listener;
    }

    public void setOnConnectionStatusListener(OnConnectionStatusListener listener) {
        this.connectionListener = listener;
    }

    private void notifyMessageReceived(final String message) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (messageListener != null) {
                    messageListener.onMessageReceived(message);
                }
            }
        });
    }

    private void notifyConnected() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (connectionListener != null) {
                    connectionListener.onConnected();
                }
            }
        });
    }

    private void notifyDisconnected() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (connectionListener != null) {
                    connectionListener.onDisconnected();
                }
            }
        });
    }

    private void notifyReconnecting() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (connectionListener != null) {
                    connectionListener.onReconnecting();
                }
            }
        });
    }
}
