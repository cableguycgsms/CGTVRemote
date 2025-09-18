package com.cableguy.remotesdk;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.cgutman.adblib.AdbBase64;
import com.cgutman.adblib.AdbConnection;
import com.cgutman.adblib.AdbCrypto;
import com.cgutman.adblib.AdbStream;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

public class AdbClient {
    private static final String TAG = "AdbClient";

    private final Context context;
    private AdbConnection adbConnection;
    private AdbStream shellStream;
    private boolean connected = false;
    private final List<String> commandQueue = new ArrayList<>();

    public AdbClient(Context context) {
        this.context = context;
    }

    public boolean isConnected() {
        return connected && shellStream != null && !shellStream.isClosed();
    }

    public void connect(String host, int port) {
        new Thread(() -> {
            try {
                // 1. Load or generate keypair
                AdbCrypto crypto = setupCrypto("pub.key", "priv.key");
                Log.d(TAG, "Keypair ready");

                // 2. Connect to device
                Socket socket = new Socket(host, port);
                Log.d(TAG, "Socket connected: " + host + ":" + port);

                // 3. Create ADB connection
                adbConnection = AdbConnection.create(socket, crypto);
                adbConnection.connect();
                connected = true;

                // 4. Open shell stream
                shellStream = adbConnection.open("shell:");
                Log.d(TAG, "ADB connected and shell opened");

                // 5. Flush queued commands
                flushCommandQueue();

                // 6. Start reading shell output
                new Thread(() -> {
                    try {
                        while (!shellStream.isClosed()) {
                            byte[] data = shellStream.read();
                            if (data != null) {
                                String output = new String(data, StandardCharsets.UTF_8);
                                Log.d(TAG, "ADB Shell: " + output);
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Shell read error", e);
                    }
                }).start();

            } catch (Exception e) {
                Log.e(TAG, "ADB connection error", e);
                connected = false;
            }
        }).start();
    }

    public void executeCommand(String command) {
        if (!connected || adbConnection == null) {
            Log.e(TAG, "ADB not ready, queuing: " + command);
            commandQueue.add(command);
            return;
        }

        new Thread(() -> {
            try {
                // Open a one-time exec stream for this command
                AdbStream execStream = adbConnection.open("exec:" + command);
                Log.d(TAG, "Command sent: " + command);

                // Optionally read response (not strictly needed for keyevents)
                byte[] response = execStream.read();
                if (response != null) {
                    String output = new String(response, StandardCharsets.UTF_8);
                    Log.d(TAG, "Command output: " + output);
                }

                execStream.close(); // Close after execution
            } catch (IOException | InterruptedException e) {
                Log.e(TAG, "Command error", e);
            }
        }).start();
    }

    private void flushCommandQueue() {
        for (String command : commandQueue) {
            executeCommand(command);
        }
        commandQueue.clear();
    }

    private AdbCrypto setupCrypto(String pubKeyFile, String privKeyFile)
            throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {

        File pub = new File(context.getFilesDir(), pubKeyFile);
        File priv = new File(context.getFilesDir(), privKeyFile);
        AdbCrypto crypto = null;

        if (pub.exists() && priv.exists()) {
            try {
                crypto = AdbCrypto.loadAdbKeyPair(getBase64Impl(), priv, pub);
                Log.d(TAG, "Loaded existing keypair");
            } catch (Exception e) {
                Log.w(TAG, "Key load failed, regenerating");
            }
        }

        if (crypto == null) {
            crypto = AdbCrypto.generateAdbKeyPair(getBase64Impl());
            crypto.saveAdbKeyPair(priv, pub);
            Log.d(TAG, "Generated new keypair");
        }

        return crypto;
    }

    private static AdbBase64 getBase64Impl() {
        return arg0 -> Base64.encodeToString(arg0, Base64.NO_WRAP);
    }
}
