package com.zuomu.smartpen.features.eraser;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.zuomu.smartpen.R;
import com.zuomu.smartpen.ScreenshotDisplayActivity;

public class EraserFragment extends Fragment {

    private static final String PREFS_NAME = "ScreenshotConfig";
    private static final String KEY_ERASER_SIZE = "eraser_size";

    private SeekBar sizeSeekBar;
    private TextView sizeTextView;
    private Button applyButton;
    private int eraserSize = 20; // 默认橡皮擦大小

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("EraserFragment", "创建橡皮擦Fragment视图");
        View view = inflater.inflate(R.layout.fragment_eraser, container, false);
        
        initViews(view);
        loadConfig();
        setupListeners();
        
        return view;
    }

    private void initViews(View view) {
        sizeSeekBar = view.findViewById(R.id.eraser_size_seekbar);
        sizeTextView = view.findViewById(R.id.eraser_size_text);
        applyButton = view.findViewById(R.id.apply_eraser_button);
        
        Log.d("EraserFragment", "橡皮擦Fragment视图初始化完成");
    }

    private void loadConfig() {
        if (getActivity() instanceof ScreenshotDisplayActivity) {
            ScreenshotDisplayActivity activity = (ScreenshotDisplayActivity) getActivity();
            eraserSize = activity.getEraserSize();
            
            // 更新UI
            sizeSeekBar.setProgress(eraserSize);
            updateSizeText();
            
            Log.d("EraserFragment", "加载橡皮擦配置 - 大小: " + eraserSize);
        }
    }

    private void setupListeners() {
        // 橡皮擦大小调节
        sizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                eraserSize = progress;
                updateSizeText();
                Log.d("EraserFragment", "橡皮擦大小改变: " + eraserSize);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // 应用按钮
        applyButton.setOnClickListener(v -> {
            if (getActivity() instanceof ScreenshotDisplayActivity) {
                ScreenshotDisplayActivity activity = (ScreenshotDisplayActivity) getActivity();
                activity.updateEraserConfig(eraserSize);
                activity.hideFragment();
                Log.d("EraserFragment", "应用橡皮擦配置 - 大小: " + eraserSize);
            }
        });
    }

    private void updateSizeText() {
        sizeTextView.setText("橡皮擦大小: " + eraserSize);
    }
} 