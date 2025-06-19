package com.zuomu.smartpen.features.spotlight;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SpotlightViewModel extends ViewModel {

    // 聚光区域大小 (1-5)
    private final MutableLiveData<Integer> spotlightSize = new MutableLiveData<>(3);

    // 暗处透明度 (0-255)
    private final MutableLiveData<Integer> darkness = new MutableLiveData<>(180);

    // 是否启用
    private final MutableLiveData<Boolean> isEnabled = new MutableLiveData<>(false);

    // 获取聚光区域大小
    public LiveData<Integer> getSpotlightSize() {
        return spotlightSize;
    }

    // 设置聚光区域大小
    public void setSpotlightSize(int size) {
        if (size >= 1 && size <= 5) {
            spotlightSize.setValue(size);
            updateSpotlightSettings();
        }
    }

    // 获取暗处透明度
    public LiveData<Integer> getDarkness() {
        return darkness;
    }

    // 设置暗处透明度
    public void setDarkness(int value) {
        if (value >= 0 && value <= 255) {
            darkness.setValue(value);
            updateSpotlightSettings();
        }
    }

    // 获取启用状态
    public LiveData<Boolean> getIsEnabled() {
        return isEnabled;
    }

    // 设置启用状态
    public void setEnabled(boolean enabled) {
        isEnabled.setValue(enabled);
        updateSpotlightSettings();
    }

    // 更新聚光灯设置
    private void updateSpotlightSettings() {
        Integer size = spotlightSize.getValue();
        Integer dark = darkness.getValue();
        Boolean enabled = isEnabled.getValue();

        if (size != null && dark != null && enabled != null && enabled) {
            // TODO: 发送命令到设备
        }
    }

    // 保存设置
    public void saveSettings() {
        // TODO: 实现设置保存
    }

    // 重置设置
    public void resetSettings() {
        spotlightSize.setValue(3);
        darkness.setValue(180);
        isEnabled.setValue(false);
    }
} 