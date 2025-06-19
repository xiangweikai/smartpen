package com.zuomu.smartpen.features.recording;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.zuomu.smartpen.connection.SmartPenConnection;

public class RecordingViewModel extends ViewModel {

    private static final String PREFS_NAME = "RecordingSettings";
    private static final String KEY_SAVE_PATH = "save_path";
    private static final String KEY_MEETING_RECORDING = "meeting_recording";

    // 录音保存路径
    private final MutableLiveData<String> savePath = new MutableLiveData<>("C:\\Users\\admin\\Documents\\TpenApp");

    // 是否启用会议录音
    private final MutableLiveData<Boolean> isMeetingRecordingEnabled = new MutableLiveData<>(false);

    // 获取录音保存路径
    public LiveData<String> getSavePath() {
        return savePath;
    }

    // 设置录音保存路径
    public void setSavePath(String path) {
        savePath.setValue(path);
        updateRecordingSettings();
    }

    // 获取会议录音启用状态
    public LiveData<Boolean> getIsMeetingRecordingEnabled() {
        return isMeetingRecordingEnabled;
    }

    // 设置会议录音启用状态
    public void setMeetingRecordingEnabled(boolean enabled) {
        isMeetingRecordingEnabled.setValue(enabled);
        updateRecordingSettings();
    }

    // 更新录音设置
    private void updateRecordingSettings() {
        String path = savePath.getValue();
        Boolean enabled = isMeetingRecordingEnabled.getValue();

        if (path != null && enabled != null) {
            // 发送命令到设备
            byte[] command = new byte[8];
            command[0] = (byte) 0xAB;
            command[1] = (byte) 0xCD;
            command[2] = 0x01;
            command[3] = (byte) (enabled ? 0x01 : 0x00);
            SmartPenConnection.getInstance().sendCommand(command);
        }
    }

    // 保存设置
    public void saveSettings(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(KEY_SAVE_PATH, savePath.getValue());
        editor.putBoolean(KEY_MEETING_RECORDING, isMeetingRecordingEnabled.getValue());
        editor.apply();
    }

    // 重置设置
    public void resetSettings() {
        savePath.setValue("C:\\Users\\admin\\Documents\\TpenApp");
        isMeetingRecordingEnabled.setValue(false);
    }
} 