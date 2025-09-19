package com.cableguy.remotesdk;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.cableguy.remotesdk.utils.DeviceUtils;
import com.cableguy.remotesdk.utils.DialogTracker;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Controls {

    public interface OnNavigate {
        void execute(String direction);
    }

    public interface OnOk {
        void execute();
    }

    public interface OnBack {
        void execute(String direction);
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

    public void handleNavigate(String direction, Activity activity) {
        int keyCode = -1;
        int focusDirection = -1;

        switch (direction) {
            case "left":
                keyCode = KeyEvent.KEYCODE_DPAD_LEFT;
                focusDirection = View.FOCUS_LEFT;
                break;
            case "right":
                keyCode = KeyEvent.KEYCODE_DPAD_RIGHT;
                focusDirection = View.FOCUS_RIGHT;
                break;
            case "up":
                keyCode = KeyEvent.KEYCODE_DPAD_UP;
                focusDirection = View.FOCUS_UP;
                break;
            case "down":
                keyCode = KeyEvent.KEYCODE_DPAD_DOWN;
                focusDirection = View.FOCUS_DOWN;
                break;
        }

        if (keyCode != -1 && focusDirection != -1) {
            Dialog dialog = DialogTracker.currentDialog;

            if (dialog != null && dialog.isShowing()) {
                View focused = dialog.getCurrentFocus();
                if (focused != null) {
                    boolean handled = focused.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
                    focused.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyCode));
                    if (!handled) {
                        View next = focused.focusSearch(focusDirection);
                        if (next != null) next.requestFocus();
                    }
                }
            } else {
                View focused = activity.getCurrentFocus();
                if (focused != null) {
                    View next = focused.focusSearch(focusDirection);
                    if (next != null) next.requestFocus();
                }
                dispatchKey(activity, keyCode);
            }
        }

        if (onNavigate != null) onNavigate.execute(direction);
    }


    public void handleOk(Activity activity) {
        dispatchKey(activity, KeyEvent.KEYCODE_DPAD_CENTER);

        if (onOk != null) onOk.execute();
    }

    private boolean isKeyboardVisible(Activity activity) {
        View rootView = activity.findViewById(android.R.id.content);
        int heightDiff = rootView.getRootView().getHeight() - rootView.getHeight();
        return heightDiff > dpToPx(activity, 100);  // heuristic: keyboard is likely visible
    }

    private int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }


    public void handleBack(String command, Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        switch (command) {
            case "back":
                if (isKeyboardVisible(activity)) {
                    View currentFocus = activity.getCurrentFocus();
                    if (currentFocus != null) {
                        imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
                        currentFocus.clearFocus();
                    }
                } else {
                    dispatchKey(activity, KeyEvent.KEYCODE_BACK);
                }
                break;
            case "close_keyback":
                View currentFocus = activity.getCurrentFocus();
                if (currentFocus != null) {
                    imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
                    currentFocus.clearFocus();
                }
                break;
        }
        if (onBack != null) onBack.execute(command);
    }

    private void dispatchKey(Activity activity, int keyCode) {
        KeyEvent down = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
        KeyEvent up = new KeyEvent(KeyEvent.ACTION_UP, keyCode);

        Dialog dialog = DialogTracker.currentDialog;

        if (dialog != null && dialog.isShowing()) {
            View focused = dialog.getCurrentFocus();
            if (focused != null) {
                focused.dispatchKeyEvent(down);
                focused.dispatchKeyEvent(up);
                return;
            }
        }

        View focused = activity.getCurrentFocus();
        if (focused == null) {
            focused = activity.getWindow().getDecorView();
        }
        Window focusedWindow = getWindowFromView(focused);

        if (focusedWindow != null) {
            focusedWindow.getDecorView().dispatchKeyEvent(down);
            focusedWindow.getDecorView().dispatchKeyEvent(up);
        } else {
            activity.getWindow().getDecorView().dispatchKeyEvent(down);
            activity.getWindow().getDecorView().dispatchKeyEvent(up);
        }
    }

    private Window getWindowFromView(View view) {
        view.getWindowId();
        return null;
    }
    public void handleActivityByKey(String key, Activity currentActivity) {
        ActivityLauncher launcher = activityLauncherMap.get(key);

        Log.d("TAG", "handleActivityByKey: " + key + " : " + (launcher == null));
        if (launcher != null) {
            launcher.launch();
        }
    }


    public void handlePackageByKey(String key, Activity currentActivity) {
        Context targetContext = packageMap.get(key);
        if (targetContext != null) {
            DeviceUtils.launchAppActivity(key, targetContext);
        } else {
            Toast.makeText(currentActivity, "No package found for key: " + key, Toast.LENGTH_SHORT).show();
        }
    }

    public void handleVolumeUp(Activity activity) {
        AudioManager audio = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        int vol = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        int max = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        if (vol < max) {
            audio.setStreamVolume(AudioManager.STREAM_MUSIC, vol + 1, AudioManager.FLAG_SHOW_UI);
        }
        if (onVolumeUp != null) onVolumeUp.execute();
    }

    public void handleVolumeDown(Activity activity) {
        AudioManager audio = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        int vol = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (vol > 0) {
            audio.setStreamVolume(AudioManager.STREAM_MUSIC, vol - 1, AudioManager.FLAG_SHOW_UI);
        }
        if (onVolumeDown != null) onVolumeDown.execute();
    }

    public void handleMute(Activity activity) {
        AudioManager audio = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        if (audio != null) {
            boolean isMuted = audio.isStreamMute(AudioManager.STREAM_MUSIC);
            audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                    isMuted ? AudioManager.ADJUST_UNMUTE : AudioManager.ADJUST_MUTE,
                    AudioManager.FLAG_SHOW_UI);
        }

        if (onMute != null) onMute.execute();
    }

    public void handleNumberInput(String digit, Activity activity) {
        int keyCode = KeyEvent.KEYCODE_0 + Integer.parseInt(digit);
        dispatchKey(activity, keyCode);
        if (onNumberInput != null) onNumberInput.execute(digit);
    }

    public void handleTextInput(String text, Activity activity) {
        if (text == null || text.isEmpty()) return;

        char c = text.charAt(0);
        if (Character.isLetter(c)) {
            int keyCode = KeyEvent.KEYCODE_A + (Character.toUpperCase(c) - 'A');
            dispatchKey(activity, keyCode);
        } else {
            return;
        }

        if (onTextInput != null) onTextInput.execute(text);
    }

    public void handleSpace(Activity activity) {
        dispatchKey(activity, KeyEvent.KEYCODE_SPACE);
        if (onSpace != null) onSpace.execute();
    }

    public void handleRemove(Activity activity) {
        dispatchKey(activity, KeyEvent.KEYCODE_DEL);
        if (onRemove != null) onRemove.execute();
    }


}
