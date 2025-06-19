package com.zuomu.smartpen.features.timer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.zuomu.smartpen.connection.SmartPenConnection;

public class TimerViewModel extends ViewModel {
    private static final String PREF_NAME = "timer_settings";
    private static final String KEY_PRESET_DURATION = "preset_duration";
    private static final String KEY_ENABLED = "enabled";

    private final MutableLiveData<Integer> presetDuration = new MutableLiveData<>(30);
    private final MutableLiveData<Boolean> enabled = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isRunning = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> remainingTime = new MutableLiveData<>(0);

    private CountDownTimer countDownTimer;

    public MutableLiveData<Integer> getPresetDuration() {
        return presetDuration;
    }

    public MutableLiveData<Boolean> getEnabled() {
        return enabled;
    }

    public MutableLiveData<Boolean> getIsRunning() {
        return isRunning;
    }

    public MutableLiveData<Integer> getRemainingTime() {
        return remainingTime;
    }

    public void setPresetDuration(int duration) {
        presetDuration.setValue(duration);
    }

    public void setEnabled(boolean enabled) {
        this.enabled.setValue(enabled);
    }

    public void startTimer() {
        if (enabled.getValue() != null && enabled.getValue()) {
            isRunning.setValue(true);
            updateTimerSettings();
        }
    }

    public void stopTimer() {
        isRunning.setValue(false);
        updateTimerSettings();
    }

    public void updateTimerSettings() {
        if (enabled.getValue() != null && enabled.getValue()) {
            Integer duration = presetDuration.getValue();
            if (duration != null) {
                byte[] command = new byte[8];
                command[0] = (byte) 0xAB;
                command[1] = (byte) 0xCD;
                command[2] = 0x03;
                command[3] = (byte) (duration & 0xFF);
                SmartPenConnection.getInstance().sendCommand(command);
            }
        }
    }

    public void saveSettings(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        Integer duration = presetDuration.getValue();
        Boolean enabledValue = enabled.getValue();
        
        if (duration != null) {
            editor.putInt(KEY_PRESET_DURATION, duration);
        }
        if (enabledValue != null) {
            editor.putBoolean(KEY_ENABLED, enabledValue);
        }
        
        editor.apply();
    }

    public void loadSettings(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        presetDuration.setValue(prefs.getInt(KEY_PRESET_DURATION, 30));
        enabled.setValue(prefs.getBoolean(KEY_ENABLED, false));
    }

    public void resetSettings() {
        presetDuration.setValue(30);
        enabled.setValue(false);
        isRunning.setValue(false);
        remainingTime.setValue(0);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }
} 