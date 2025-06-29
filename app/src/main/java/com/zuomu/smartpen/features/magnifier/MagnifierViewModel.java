package com.zuomu.smartpen.features.magnifier;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MagnifierViewModel extends ViewModel {

    // 放大区域大小 (1-5)
    private final MutableLiveData<Integer> magnifierSize = new MutableLiveData<>(4);

    // 放大倍数 (2x, 3x, 4x, 5x)
    private final MutableLiveData<Integer> zoomLevel = new MutableLiveData<>(2);

    // 放大镜形状 (0=圆形, 1=方形)
    private final MutableLiveData<Integer> magnifierShape = new MutableLiveData<>(0);

    // 是否启用
    private final MutableLiveData<Boolean> isEnabled = new MutableLiveData<>(false);

    // 获取放大区域大小
    public LiveData<Integer> getMagnifierSize() {
        return magnifierSize;
    }

    // 设置放大区域大小
    public void setMagnifierSize(int size) {
        if (size >= 1 && size <= 5) {
            magnifierSize.setValue(size);
            updateMagnifierSettings();
        }
    }

    // 获取放大倍数
    public LiveData<Integer> getZoomLevel() {
        return zoomLevel;
    }

    // 设置放大倍数
    public void setZoomLevel(int level) {
        if (level >= 2 && level <= 5) {
            zoomLevel.setValue(level);
            updateMagnifierSettings();
        }
    }

    // 获取放大镜形状
    public LiveData<Integer> getMagnifierShape() {
        return magnifierShape;
    }

    // 设置放大镜形状
    public void setMagnifierShape(int shape) {
        if (shape >= 0 && shape <= 1) {
            magnifierShape.setValue(shape);
            updateMagnifierSettings();
        }
    }

    // 获取启用状态
    public LiveData<Boolean> getIsEnabled() {
        return isEnabled;
    }

    // 设置启用状态
    public void setEnabled(boolean enabled) {
        isEnabled.setValue(enabled);
        updateMagnifierSettings();
    }

    // 更新放大镜设置
    private void updateMagnifierSettings() {
        Integer size = magnifierSize.getValue();
        Integer zoom = zoomLevel.getValue();
        Integer shape = magnifierShape.getValue();
        Boolean enabled = isEnabled.getValue();

        if (size != null && zoom != null && shape != null && enabled != null && enabled) {
            // TODO: 发送命令到设备
        }
    }

    // 保存设置
    public void saveSettings() {
        // TODO: 实现设置保存
    }

    // 重置设置
    public void resetSettings() {
        magnifierSize.setValue(4);
        zoomLevel.setValue(2);
        magnifierShape.setValue(0);
        isEnabled.setValue(false);
    }
} 