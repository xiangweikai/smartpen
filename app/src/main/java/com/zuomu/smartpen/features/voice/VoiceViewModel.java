package com.zuomu.smartpen.features.voice;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.zuomu.smartpen.connection.SmartPenConnection;
import java.util.ArrayList;
import java.util.List;

public class VoiceViewModel extends ViewModel {
    private static final String PREF_NAME = "voice_settings";
    private static final String KEY_IS_ENABLED = "is_enabled";
    private static final String KEY_VOICE_FEEDBACK = "voice_feedback";
    private static final String KEY_VOLUME = "volume";
    private static final String KEY_MIC_SENSITIVITY = "mic_sensitivity";
    private static final String KEY_CUSTOM_COMMANDS = "custom_commands";

    private final MutableLiveData<Boolean> isEnabled = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> voiceFeedback = new MutableLiveData<>(true);
    private final MutableLiveData<Integer> volume = new MutableLiveData<>(70);
    private final MutableLiveData<Integer> micSensitivity = new MutableLiveData<>(50);
    private final MutableLiveData<List<VoiceCommand>> customCommands = new MutableLiveData<>(new ArrayList<>());

    public LiveData<Boolean> getIsEnabled() {
        return isEnabled;
    }

    public LiveData<Boolean> getVoiceFeedback() {
        return voiceFeedback;
    }

    public LiveData<Integer> getVolume() {
        return volume;
    }

    public LiveData<Integer> getMicSensitivity() {
        return micSensitivity;
    }

    public LiveData<List<VoiceCommand>> getCustomCommands() {
        return customCommands;
    }

    public void setEnabled(boolean enabled) {
        isEnabled.setValue(enabled);
        updateVoiceSettings();
    }

    public void setVoiceFeedback(boolean enabled) {
        voiceFeedback.setValue(enabled);
        updateVoiceSettings();
    }

    public void setVolume(int value) {
        volume.setValue(value);
        updateVoiceSettings();
    }

    public void setMicSensitivity(int value) {
        micSensitivity.setValue(value);
        updateVoiceSettings();
    }

    public void addCustomCommand(String command, String target) {
        List<VoiceCommand> commands = customCommands.getValue();
        if (commands != null) {
            commands.add(new VoiceCommand(command, target));
            customCommands.setValue(commands);
            updateVoiceSettings();
        }
    }

    public void removeCustomCommand(int position) {
        List<VoiceCommand> commands = customCommands.getValue();
        if (commands != null && position >= 0 && position < commands.size()) {
            commands.remove(position);
            customCommands.setValue(commands);
            updateVoiceSettings();
        }
    }

    public void updateVoiceSettings() {
        Integer volumeValue = volume.getValue();
        Integer sensitivityValue = micSensitivity.getValue();
        if (volumeValue != null && sensitivityValue != null) {
            byte[] command = new byte[8];
            command[0] = (byte) 0xAB;
            command[1] = (byte) 0xCD;
            command[2] = 0x02;
            command[3] = (byte) (volumeValue & 0xFF);
            command[4] = (byte) (sensitivityValue & 0xFF);
            SmartPenConnection.getInstance().sendCommand(command);
        }
    }

    public void saveSettings(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
            .putBoolean(KEY_IS_ENABLED, isEnabled.getValue())
            .putBoolean(KEY_VOICE_FEEDBACK, voiceFeedback.getValue())
            .putInt(KEY_VOLUME, volume.getValue())
            .putInt(KEY_MIC_SENSITIVITY, micSensitivity.getValue())
            .apply();
    }

    public void loadSettings(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        isEnabled.setValue(prefs.getBoolean(KEY_IS_ENABLED, false));
        voiceFeedback.setValue(prefs.getBoolean(KEY_VOICE_FEEDBACK, true));
        volume.setValue(prefs.getInt(KEY_VOLUME, 70));
        micSensitivity.setValue(prefs.getInt(KEY_MIC_SENSITIVITY, 50));
    }

    public void resetSettings() {
        isEnabled.setValue(false);
        voiceFeedback.setValue(true);
        volume.setValue(70);
        micSensitivity.setValue(50);
        customCommands.setValue(new ArrayList<>());
        updateVoiceSettings();
    }

    public static class VoiceCommand {
        private final String command;
        private final String target;

        public VoiceCommand(String command, String target) {
            this.command = command;
            this.target = target;
        }

        public String getCommand() {
            return command;
        }

        public String getTarget() {
            return target;
        }
    }
} 