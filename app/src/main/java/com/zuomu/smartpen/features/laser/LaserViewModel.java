package com.zuomu.smartpen.features.laser;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class LaserViewModel extends ViewModel {
    
    // 光标大小 (1-20)
    private final MutableLiveData<Integer> cursorSize = new MutableLiveData<>(14);
    
    // 光标颜色 (0:红色, 1:绿色, 2:蓝色)
    private final MutableLiveData<Integer> cursorColor = new MutableLiveData<>(0);
    
    // 是否启用
    private final MutableLiveData<Boolean> isEnabled = new MutableLiveData<>(false);
    
    // 获取光标大小
    public LiveData<Integer> getCursorSize() {
        return cursorSize;
    }
    
    // 设置光标大小
    public void setCursorSize(int size) {
        if (size >= 1 && size <= 20) {
            cursorSize.setValue(size);
            updateLaserSettings();
        }
    }
    
    // 获取光标颜色
    public LiveData<Integer> getCursorColor() {
        return cursorColor;
    }
    
    // 设置光标颜色
    public void setCursorColor(int color) {
        if (color >= 0 && color <= 2) {
            cursorColor.setValue(color);
            updateLaserSettings();
        }
    }
    
    // 获取启用状态
    public LiveData<Boolean> getIsEnabled() {
        return isEnabled;
    }
    
    // 设置启用状态
    public void setEnabled(boolean enabled) {
        isEnabled.setValue(enabled);
        updateLaserSettings();
    }
    
    // 更新激光笔设置
    private void updateLaserSettings() {
        Integer size = cursorSize.getValue();
        Integer color = cursorColor.getValue();
        Boolean enabled = isEnabled.getValue();
        
        if (size != null && color != null && enabled != null && enabled) {
            // 发送命令到设备
            byte[] command = new byte[8];
            command[0] = (byte) 0xAB;
            command[1] = (byte) 0xCD;
            command[2] = 0x00;
            
            // 设置光圈大小和颜色
            if (color == 0) { // 红色
                command[3] = (byte) (size & 0xFF);
            } else if (color == 1) { // 绿色
                command[3] = (byte) ((size | 0x10) & 0xFF);
            }
            
            // TODO: 通过SmartPenConnection发送命令
        }
    }
    
    // 保存设置
    public void saveSettings() {
        // TODO: 实现设置保存
    }
    
    // 重置设置
    public void resetSettings() {
        cursorSize.setValue(14);
        cursorColor.setValue(0);
        isEnabled.setValue(false);
    }
}
