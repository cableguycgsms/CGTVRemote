package com.cableguy.remotesdk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.cableguy.remotesdk.utils.AESUtils;
import com.cableguy.remotesdk.utils.DeviceUtils;
import com.cableguy.remotesdk.utils.RemoteCallback;

import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import io.socket.client.IO;
import io.socket.client.Socket;
import okhttp3.OkHttpClient;

public class RemoteController {

    private static final String TAG = "RemoteController";

    @SuppressLint("StaticFieldLeak")
    private static RemoteController instance;
    private AdbClient adbClient;

    private Socket mSocket;
    private Context appContext;    // Store Application context
    private Activity currentActivity;  // Track top Activity for focus, nav, etc
    public Controls remoteControls; // callbacks

    public static RemoteController getInstance() {
        if (instance == null) {
            instance = new RemoteController();
        }
        return instance;
    }

    // Initialize socket ONCE with Application context
    public void initSocket(Context context) {
        this.appContext = context.getApplicationContext();

        if (remoteControls == null) {
            remoteControls = new Controls(appContext);
        }

        if (adbClient == null) {
            remoteControls = new Controls(appContext);
            adbClient = remoteControls.getAdbClient();
            adbClient.connect(DeviceUtils.getDeviceIp(appContext), 5555);
        }

        if (mSocket == null || !mSocket.connected()) {
            connectSocket();
        }
    }

    // Keep latest Activity for nav + UI actions
    public void setCurrentActivity(Activity activity) {
        this.currentActivity = activity;
    }

    public void setRemoteControls(Controls controls) {
        this.remoteControls = controls;
    }

    private void connectSocket() {
        try {
            IO.Options opts = new IO.Options();
            opts.callFactory = getUnsafeOkHttpClient();
            opts.webSocketFactory = getUnsafeOkHttpClient();

//            mSocket = IO.socket("https://remotesetu.in:4678", opts);
            try {
                mSocket = IO.socket(new URI(RemoteCallback.getRemoteSocketURL()), opts);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            Log.d("RemoteData", "RemoteController: "+RemoteCallback.getRemoteSocketURL()+opts);

            mSocket.connect();

            mSocket.on(Socket.EVENT_CONNECT, args -> {
                Log.d(TAG, "Socket connected");

                // Use safe App context for ID
                String androidId = DeviceUtils.getDeviceId(appContext);
                String socketId = mSocket.id();
                Log.d(TAG, "Device ID: " + androidId);
                Log.d(TAG, "Socket ID: " + socketId);

                try {
                    JSONObject auth = new JSONObject();
                    auth.put("device_id", androidId);
                    String encrypted = AESUtils.encrypt(auth.toString());
                    mSocket.emit("auth", encrypted);

                } catch (Exception e) {
                    Log.e(TAG, "Auth error", e);
                }
            });

            mSocket.on(Socket.EVENT_DISCONNECT, args -> {
                Log.e(TAG, " Socket disconnected! Reason: " + Arrays.toString(args));
            });

            mSocket.on(Socket.EVENT_CONNECT_ERROR, args -> {
                Log.e(TAG, "Socket connect error: " + Arrays.toString(args));
            });

            mSocket.on("remote", args -> {
                if (args.length > 0) {
                    try {
                        String encrypted = args[0].toString();
                        String decrypted = AESUtils.decrypt(encrypted);
                        Log.d(TAG, "Decrypted command: " + decrypted);
                        handleCommand(decrypted);
                    } catch (Exception e) {
                        Log.e(TAG, "Error decrypting remote command", e);
                    }
                }
            });

            mSocket.on("login-status", args -> {
                if (args.length > 0) {
                    try {
                        Log.d(TAG, "Login Status" + args[0].toString());
                    } catch (Exception e) {
                        Log.e(TAG, "Error login status", e);
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Socket URI error: " + e.getMessage());
        }
    }

    private void handleCommand(String decryptedMessage) {
        try {
            JSONObject json = new JSONObject(decryptedMessage);
            String msg = json.optString("message", "");
            if (msg.contains("intent$")) {
                remoteControls.handleActivityByKey(msg.split("\\$")[1].toLowerCase(), currentActivity);
                return;
            } else if (msg.contains("package$")) {
                remoteControls.handlePackageByKey(msg.split("\\$")[1], appContext);
                return;
            }
            String command = getCommand(msg.toLowerCase());
            if (!command.isEmpty() && currentActivity != null) {
                currentActivity.runOnUiThread(() -> {
                    switch (command) {
                        case "left":
                        case "right":
                        case "up":
                        case "down":
                        case "channel_down":
                        case "channel_up":
                            remoteControls.handleNavigate(command);
                            break;
                        case "ok":
                            remoteControls.handleOk();
                            break;
                        case "back":
                            remoteControls.handleBack();
                            break;
                        case "volume_up":
                            remoteControls.handleVolumeUp();
                            break;
                        case "volume_down":
                            remoteControls.handleVolumeDown();
                            break;
                        case "mute":
                            remoteControls.handleMute();
                            break;
                        case "number_input":
                            String digit = msg.toLowerCase().replace("number button clicked: ", "").trim();
                            Log.d("RemoteControls", digit);
                            remoteControls.handleNumberInput(digit);
                            break;
                        case "text_input":
                            String text = msg.toLowerCase().replace("text button clicked: ", "").trim();
                            remoteControls.handleTextInput(text);
                            break;
                        case "space":
                            remoteControls.handleSpace();
                            break;
                        case "remove":
                            remoteControls.handleRemove();
                            break;
                        case "no_key":
                            break;
                    }
                });
            }

        } catch (Exception e) {
            Log.e(TAG, "Command parse error", e);
        }
    }

    @NonNull
    private static String getCommand(String msg) {
        String command = "";
        if (msg.contains("volume_up")) command = "volume_up";
        else if (msg.contains("volume_down")) command = "volume_down";
        else if (msg.contains("mute")) command = "mute";
        else if (msg.contains("left")) command = "left";
        else if (msg.contains("right")) command = "right";
        else if (msg.contains("up")) command = "up";
        else if (msg.contains("down")) command = "down";
        else if (msg.contains("ok") || msg.contains("center") || msg.contains("enter"))
            command = "ok";
        else if (msg.contains("channel_up")) command = "channel_up";
        else if (msg.contains("channel_down")) command = "channel_down";
        else if (msg.contains("close_keyback")) command = "close_keyback";
        else if (msg.contains("back")) command = "back";
        else if (msg.contains("remove")) command = "remove";
        else if (msg.contains("btn arrow") || msg.contains("btn keypad")) command = "no_key";
        else if (msg.contains("space")) command = "space";
        else if (msg.contains("number button clicked")) command = "number_input";
        else if (msg.contains("text button clicked")) command = "text_input";

        return command;
    }

    public void disconnect() {
        if (mSocket != null && mSocket.connected()) {
            mSocket.disconnect();
        }
    }

    private OkHttpClient getUnsafeOkHttpClient() {
        try {
            // Install a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier((hostname, session) -> true);

            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
