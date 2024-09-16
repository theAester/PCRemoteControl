package io.github.theaester.pcremotecontrol.comms;

import android.content.Context;
import android.util.Log;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.os.Handler;

import io.github.theaester.pcremotecontrol.R;


public class ConnectionManager {
    private static ConnectionManager instance;
    private static final int DEFAULT_PORT = 12345;
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private Encryptor encryptor;
    private RecvCallback recvCallback;
    private final Context context;
    private boolean keepAlive = true;
    private long keepAliveTime;
    private Handler handler;
    private static final long delay = 180_000;
    private String addr;
    private String key;

    private ConnectionManager(Context context){
        this.context = context;
        this.handler = new Handler(context.getMainLooper());
    }

    public static synchronized ConnectionManager getInstance(){
        if(instance == null){
            throw new IllegalStateException("Class is not initialized");
        }
        return instance;
    }
    public static void init(Context context){
        if(instance == null){
            instance = new ConnectionManager(context);
        }
    }

    public void connect(String address, String key, ConnectionCallback callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                String[] parts = address.split(":");
                String ip = parts[0];
                int port = parts.length > 1 ? Integer.parseInt(parts[1]) : DEFAULT_PORT;

                socket = new Socket(ip, port);
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

                encryptor = new Encryptor(key);

                // Perform handshake
                output.println(encryptor.encrypt("HANDSHAKE"));
                Log.d("ConnMan", "handshake sent");
                String resp = input.readLine();
                String response = encryptor.decrypt(resp);
                Log.d("ConnMan", "Handshake recvd");
                if ("HANDSHAKE".equals(response)) {
                    callback.onSuccess();
                    keepAliveTime = System.currentTimeMillis();
                    startKeepAliveSequence();
                    this.addr = address;
                    this.key = key;
                } else {
                    callback.onFailure("Handshake failed");
                }

                // Start listening for incoming messages
                listenForMessages();

            } catch (Exception e) {
                callback.onError(e);
                e.printStackTrace();
            }
        });
    }

    private void startKeepAliveSequence() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                long ts = System.currentTimeMillis();
                if(ts - keepAliveTime > delay / 2){
                    send("{\"type\":\"nop\"}");
                    handler.postDelayed(this, delay);
                }
            }
        }, delay);
    }

    public void disconnect(ConnectionCallback callback) {
        try {
            if (socket != null) {
                stopKeepAliveSequence();
                socket.close();
                socket = null;
            }
            callback.onSuccess();
        } catch (IOException e) {
            callback.onError(e);
            e.printStackTrace();
        }
    }

    private void stopKeepAliveSequence() {
        handler.removeCallbacksAndMessages(null);
    }

    public void send(String message) {
        Log.d("ConnMan", "call");
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Log.d("ConnMan", "into");
            try {
                Log.d("ConnMan", "into > try");
                if (socket != null && !socket.isClosed()) {
                    keepAliveTime = System.currentTimeMillis();
                    Log.d("ConnMan", "sending message: " + message);
                    output.println(encryptor.encrypt(message));
                } else {
                    Log.d("ConnMan", "MAYDAYMAYDAYMAYDAYMAYDAY");
                }
            } catch (Exception e) {
                Log.d("ConnMan", "into > catch");
                e.printStackTrace();
            }
        });
    }

    public void setRecvCallback(RecvCallback callback) {
        recvCallback = callback;
    }

    private void listenForMessages() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                String message;
                while ((message = input.readLine()) != null) {
                    String decryptedMessage = encryptor.decrypt(message);
                    if (recvCallback != null) {
                        // Handle the received message (you can extend this to pass the message to the callback)
                        recvCallback.onMessage(decryptedMessage);
                    }
                }
            } catch (Exception e) {
                if(recvCallback != null)
                    recvCallback.onError(e);
                if(e instanceof SocketException){
                    stopKeepAliveSequence();
                    try {
                        socket.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    socket = null;
                }
                e.printStackTrace();
            }
        });
    }

    public void retryConnection(String addr, String key) {
        Log.d("ConnMan", "might retry: " + addr + ", " + key);
        if(socket != null && socket.isConnected()) return;
        Log.d("ConnMan", "retrying");
        connect(addr, key, new ConnectionCallback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(String message) {
            }

            @Override
            public void onError(Exception e) {
            }
        });
    }

    public String getConnectedAddress() {
        return this.addr;
    }
    public String getConnectionKey(){
        return this.key;
    }
}
