package com.zuomu.smartpen.features.annotation;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AnnotationViewModel extends ViewModel {

    // 批注颜色 (0:黑色, 1:红色, 2:黄色, 3:绿色, 4:蓝色)
    private final MutableLiveData<Integer> annotationColor = new MutableLiveData<>(0);

    // 线条粗细 (1-5)
    private final MutableLiveData<Integer> lineThickness = new MutableLiveData<>(2);

    // 橡皮擦大小 (1-5)
    private final MutableLiveData<Integer> eraserSize = new MutableLiveData<>(4);

    // 获取批注颜色
    public LiveData<Integer> getAnnotationColor() {
        return annotationColor;
    }

    // 设置批注颜色
    public void setAnnotationColor(int color) {
        if (color >= 0 && color <= 4) {
            annotationColor.setValue(color);
            Log.i("TAG","set annotation Color" + color);
            updateAnnotationSettings();
        }
    }

    // 获取线条粗细
    public LiveData<Integer> getLineThickness() {
        return lineThickness;
    }

    // 设置线条粗细
    public void setLineThickness(int thickness) {
        if (thickness >= 1 && thickness <= 5) {
            lineThickness.setValue(thickness);
            updateAnnotationSettings();
        }
    }

    // 获取橡皮擦大小
    public LiveData<Integer> getEraserSize() {
        return eraserSize;
    }

    // 设置橡皮擦大小
    public void setEraserSize(int size) {
        if (size >= 1 && size <= 5) {
            eraserSize.setValue(size);
            updateAnnotationSettings();
        }
    }

    // 更新批注设置
    private void updateAnnotationSettings() {
        Integer color = annotationColor.getValue();
        Integer thickness = lineThickness.getValue();
        Integer eraser = eraserSize.getValue();

        if (color != null && thickness != null && eraser != null) {
            // TODO: 发送命令到设备
        }
    }

    // 保存设置
    public void saveSettings() {
        // TODO: 实现设置保存
    }

    // 重置设置
    public void resetSettings() {
        annotationColor.setValue(0);
        lineThickness.setValue(2);
        eraserSize.setValue(4);
    }
} 