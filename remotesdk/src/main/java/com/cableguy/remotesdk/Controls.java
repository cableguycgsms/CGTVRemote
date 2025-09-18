package com.cableguy.remotesdk;

import android.app.Activity;
import android.content.Context;

import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.cableguy.remotesdk.utils.DeviceUtils;
import java.util.HashMap;
import java.util.Map;

public class Controls {

    public interface OnNavigate {
        void execute(String direction);
    }

    public interface OnOk {
        void execute();
    }

    public interface OnBack {
        void execute();
    }

    public interface OnVolumeUp {
        void execute();
    }

    public interface OnVolumeDown {
        void execute();
    }

    public interface OnMute {
        void execute();
    }

    public interface OnNumberInput {
        void execute(String digit);
    }

    public interface OnTextInput {
        void execute(String text);
    }

    public interface OnSpace {
        void execute();
    }

    public interface OnRemove {
        void execute();
    }

    public interface ActivityLauncher {
        void launch();
    }

    public OnNavigate onNavigate;
    public OnOk onOk;
    public OnBack onBack;
    public OnVolumeUp onVolumeUp;
    public OnVolumeDown onVolumeDown;
    public OnMute onMute;
    public OnNumberInput onNumberInput;
    public OnTextInput onTextInput;
    public OnSpace onSpace;
    public OnRemove onRemove;

    private final AdbClient adbClient;

    public Controls(Context context) {
        adbClient = new AdbClient(context);
    }

    public AdbClient getAdbClient() {
        return adbClient;
    }

//    private static Map<String, Class<?>> activityClassMap = new HashMap<>();
    private final Map<String, ActivityLauncher> activityLauncherMap = new HashMap<>();
    private static Map<String, Context> packageMap = new HashMap<>();

    public static final String LIVETV = "livetv";
    public static final String HOME = "home";

    public void registerActivity(String key, ActivityLauncher launcher) {
        activityLauncherMap.put(key, launcher);
    }

    public void registerPackage(String key, Context context) {
        packageMap.put(key, context);
    }

//    @Nullable
//    public Class<?> getActivityClass(String key) {
//        return activityClassMap.get(key);
//    }

    @Nullable
    public Context getPackage(String key) {
        return packageMap.get(key);
    }

    private void sendCommand(String cmd) {
        if (!adbClient.isConnected()) {
            Log.e("Controls", "ADB not ready, cannot execute: " + cmd);
            return;
        }
        adbClient.executeCommand(cmd);
    }

    public void handleNavigate(String direction) {
        Map<String, Integer> keyMap = new HashMap<>();
        keyMap.put("left", KeyEvent.KEYCODE_DPAD_LEFT);
        keyMap.put("right", KeyEvent.KEYCODE_DPAD_RIGHT);
        keyMap.put("up", KeyEvent.KEYCODE_DPAD_UP);
        keyMap.put("down", KeyEvent.KEYCODE_DPAD_DOWN);

        Integer keyCode = keyMap.get(direction.toLowerCase());
        if (keyCode != null) sendCommand("input keyevent " + keyCode);

        if (onNavigate != null) onNavigate.execute(direction);
    }

    public void handleOk() {
        sendCommand("input keyevent " + KeyEvent.KEYCODE_DPAD_CENTER);
        if (onOk != null) onOk.execute();
    }

    public void handleBack() {
        sendCommand("input keyevent " + KeyEvent.KEYCODE_BACK);
        if (onBack != null) onBack.execute();
    }

    public void handleVolumeUp() {
        sendCommand("input keyevent " + KeyEvent.KEYCODE_VOLUME_UP);
        if (onVolumeUp != null) onVolumeUp.execute();
    }

    public void handleVolumeDown() {
        sendCommand("input keyevent " + KeyEvent.KEYCODE_VOLUME_DOWN);
        if (onVolumeDown != null) onVolumeDown.execute();
    }

    public void handleMute() {
        sendCommand("input keyevent " + KeyEvent.KEYCODE_VOLUME_MUTE);
        if (onMute != null) onMute.execute();
    }

    public void handleNumberInput(String digit) {
        try {
            int num = Integer.parseInt(digit);
            Log.d("RemoteControls", String.valueOf(num));
            if (num < 0 || num > 9) return;

            int keyCode = KeyEvent.KEYCODE_0 + num;
            sendCommand("input keyevent " + keyCode);

            if (onNumberInput != null) onNumberInput.execute(digit);
        } catch (NumberFormatException e) {
            Log.d("RemoteControls", String.valueOf(digit));
            Log.e("Input", "Invalid digit: " + digit);
        }
    }

    public void handleTextInput(String text) {
        if (text == null || text.isEmpty()) return;
        char c = text.charAt(0);
        if (Character.isLetter(c)) {
            int offset = Character.toUpperCase(c) - 'A';
            if (offset < 0 || offset > 25) return;

            int keyCode = KeyEvent.KEYCODE_A + offset;
            sendCommand("input keyevent " + keyCode);

            if (onTextInput != null) onTextInput.execute(text);
        }
    }

    public void handleSpace() {
        sendCommand("input keyevent " + KeyEvent.KEYCODE_SPACE);
        if (onSpace != null) onSpace.execute();
    }

    public void handleRemove() {
        sendCommand("input keyevent " + KeyEvent.KEYCODE_DEL);
        if (onRemove != null) onRemove.execute();
    }

    public void handleActivityByKey(String key, Activity currentActivity) {
        ActivityLauncher launcher = activityLauncherMap.get(key);

        Log.d("TAG", "handleActivityByKey: " + key + " : " + (launcher == null));
        if (launcher != null) {
            launcher.launch();
        }
    }


    public void handlePackageByKey(String key, Context context) {
//        Context targetContext = packageMap.get(key);
        if (context != null) {
            DeviceUtils.launchAppActivity(key, context);
        }
    }
}
