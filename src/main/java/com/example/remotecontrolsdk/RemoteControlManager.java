package com.example.remotecontrolsdk;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

public class RemoteControlManager {

    private static final String TAG = "RemoteControlManager";
    private static RemoteControlManager instance;
    private Socket mSocket;
    private Activity currentActivity;

    public static RemoteControlManager getInstance() {
        if (instance == null) {
            instance = new RemoteControlManager();
        }
        return instance;
    }

    public void attach(Activity activity) {
        this.currentActivity = activity;
        if (mSocket == null || !mSocket.connected()) {
            initSocket(activity);
        }
    }

    private void initSocket(Activity activity) {
        try {
            mSocket = IO.socket("http://192.168.1.64:8695"); // Replace with your server IP
            mSocket.connect();

            mSocket.on(Socket.EVENT_CONNECT, args -> {
                Log.d(TAG, "Socket connected");

                String androidId = Settings.Secure.getString(
                        activity.getContentResolver(), Settings.Secure.ANDROID_ID
                );
                String socketId = mSocket.id();

                Log.d(TAG, "Device ID: " + androidId);
                Log.d(TAG, "Socket ID: " + socketId);

                try {
                    JSONObject auth = new JSONObject();
                    auth.put("device_id", androidId);

                    String encrypted = AESCrypto.encrypt(auth.toString()); // Ensure AESCrypto class is correct
                    mSocket.emit("auth", encrypted);

                    Log.d(TAG, "Sent encrypted auth: " + encrypted);
                } catch (Exception e) {
                    Log.e(TAG, "Auth error", e);
                }
            });

            mSocket.on("remote", args -> {
                if (args.length > 0) {
                    try {
                        String encrypted = args[0].toString();
                        Log.d(TAG, "Encrypted remote command received: " + encrypted);

                        String decrypted = AESCrypto.decrypt(encrypted); // Decrypt the payload
                        Log.d(TAG, "Decrypted command: " + decrypted);

                        handleCommand(decrypted);

                    } catch (Exception e) {
                        Log.e(TAG, "Error decrypting remote command", e);
                    }
                }
            });

        } catch (URISyntaxException e) {
            Log.e(TAG, "Socket URI error: " + e.getMessage());
        }
    }





    private void handleCommand(String decryptedMessage) {
        try {
            JSONObject json = new JSONObject(decryptedMessage);
            String msg = json.optString("message", "").toLowerCase();
            AudioManager audioManager = (AudioManager) currentActivity.getSystemService(Context.AUDIO_SERVICE);

            Log.d(TAG, "sajibcfskdjb: " +msg);
            String command = "";

            if (msg.contains("volume_up")) command = "volume_up";
            else if (msg.contains("volume_down")) command = "volume_down";
            else if (msg.contains("left")) command = "left";
            else if (msg.contains("right")) command = "right";
            else if (msg.contains("up")) command = "up";
            else if (msg.contains("down")) command = "down";
            else if (msg.contains("ok") || msg.contains("center")) command = "ok";
            else if (msg.contains("back")) command = "back";

            String finalCommand = command;

            if (!command.isEmpty() && currentActivity != null) {
                currentActivity.runOnUiThread(() -> {
                    Log.d(TAG, "OOOOP "+finalCommand);
                    switch (finalCommand) {
                        case "left":
                        case "right":
                        case "up":
                        case "down": {
                            View focusedView = currentActivity.getCurrentFocus();
                            if (focusedView == null) return;

                            View nextFocus = null;
                            switch (finalCommand) {
                                case "left":
                                    nextFocus = focusedView.focusSearch(View.FOCUS_LEFT);
                                    break;
                                case "right":
                                    nextFocus = focusedView.focusSearch(View.FOCUS_RIGHT);
                                    break;
                                case "up":
                                    nextFocus = focusedView.focusSearch(View.FOCUS_UP);
                                    break;
                                case "down":
                                    nextFocus = focusedView.focusSearch(View.FOCUS_DOWN);
                                    break;
                            }

                            if (nextFocus != null) nextFocus.requestFocus();
                            break;
                        }

                        case "ok": {
                            View focusedView = currentActivity.getCurrentFocus();
                            if (focusedView != null) {
                                focusedView.performClick();
                            }
                            break;
                        }

                        case "back":
                            currentActivity.onBackPressed(); // Handle back action
                            break;

                        case "volume_up":
                            if (audioManager != null) {
                                int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                                int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                                if (currentVolume < maxVolume) {
                                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume + 1, AudioManager.FLAG_SHOW_UI);
                                }
                            }
                            break;

                        case "volume_down":
                            if (audioManager != null) {
                                int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                                if (currentVolume > 0) {
                                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume - 1, AudioManager.FLAG_SHOW_UI);
                                }
                            }
                            break;
                    }
                });
            }

        } catch (Exception e) {
            Log.e(TAG, "Command parse error", e);
        }
    }

}
