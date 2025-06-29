package com.zuomu.smartpen.features.laser;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.button.MaterialButton;
import com.zuomu.smartpen.R;
import com.zuomu.smartpen.ScreenshotDisplayActivity;

public class LaserFragment extends Fragment {

    private LaserViewModel viewModel;
    private SeekBar seekBarCursorSize;
    private TextView textCursorSize;
    private RadioGroup radioGroupColor;
    private View cursorPreview;
    private MaterialButton btnApply;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_laser, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("LaserFragment", "LaserFragment onViewCreated被调用");
        viewModel = new ViewModelProvider(this).get(LaserViewModel.class);
        initViews(view);
        setupListeners();
        observeViewModel();
        
        // 从Activity获取当前配置
        loadCurrentConfig();
    }

    private void initViews(View view) {
        seekBarCursorSize = view.findViewById(R.id.seekbar_cursor_size);
        textCursorSize = view.findViewById(R.id.text_cursor_size);
        radioGroupColor = view.findViewById(R.id.radio_group_color);
        cursorPreview = view.findViewById(R.id.cursor_preview);
        btnApply = view.findViewById(R.id.btn_apply);
        
        // 为每个RadioButton添加点击监听器
        setupRadioButtonListeners(view);
    }

    private void setupRadioButtonListeners(View view) {
        // 为每个颜色RadioButton添加点击监听器
        view.findViewById(R.id.radio_red).setOnClickListener(v -> {
            Log.d("LaserFragment", "红色RadioButton被点击");
            radioGroupColor.check(R.id.radio_red);
            viewModel.setCursorColor(0);
            updateActivityConfig();
        });
        
        view.findViewById(R.id.radio_green).setOnClickListener(v -> {
            Log.d("LaserFragment", "绿色RadioButton被点击");
            radioGroupColor.check(R.id.radio_green);
            viewModel.setCursorColor(1);
            updateActivityConfig();
        });
        
        view.findViewById(R.id.radio_blue).setOnClickListener(v -> {
            Log.d("LaserFragment", "蓝色RadioButton被点击");
            radioGroupColor.check(R.id.radio_blue);
            viewModel.setCursorColor(2);
            updateActivityConfig();
        });
        
        // 添加TouchListener作为备选方案
        setupRadioButtonTouchListeners(view);
    }
    
    private void setupRadioButtonTouchListeners(View view) {
        // 为每个RadioButton添加TouchListener，确保能捕获到触摸事件
        view.findViewById(R.id.radio_red).setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                Log.d("LaserFragment", "红色RadioButton被触摸");
            }
            return false; // 不消费事件，让ClickListener处理
        });
        
        view.findViewById(R.id.radio_green).setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                Log.d("LaserFragment", "绿色RadioButton被触摸");
            }
            return false;
        });
        
        view.findViewById(R.id.radio_blue).setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                Log.d("LaserFragment", "蓝色RadioButton被触摸");
            }
            return false;
        });
    }

    private void setupListeners() {
        seekBarCursorSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textCursorSize.setText(String.valueOf(progress));
                viewModel.setCursorSize(progress);
                if (fromUser) {
                    updateActivityConfig();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnApply.setOnClickListener(v -> {
            Log.d("LaserFragment", "应用设置按钮被点击");
            viewModel.saveSettings();
            // 应用配置后隐藏Fragment
            if (getActivity() instanceof ScreenshotDisplayActivity) {
                ScreenshotDisplayActivity activity = (ScreenshotDisplayActivity) getActivity();
                activity.hideFragment();
            }
        });
    }

    private void updateActivityConfig() {
        if (getActivity() instanceof ScreenshotDisplayActivity) {
            ScreenshotDisplayActivity activity = (ScreenshotDisplayActivity) getActivity();
            Integer color = viewModel.getCursorColor().getValue();
            Integer size = viewModel.getCursorSize().getValue();
            
            if (color != null && size != null) {
                int actualColor = getColorFromIndex(color);
                Log.d("LaserFragment", "更新Activity配置 - 颜色: " + actualColor + ", 大小: " + size);
                activity.updateLaserConfig(actualColor, size);
            }
        }
    }

    private int getColorFromIndex(int colorIndex) {
        int color;
        switch (colorIndex) {
            case 0: color = 0xFFFF0000; break; // 红色
            case 1: color = 0xFF00FF00; break; // 绿色
            case 2: color = 0xFF0000FF; break; // 蓝色
            default: color = 0xFFFF0000; break; // 默认红色
        }
        Log.d("LaserFragment", "颜色索引 " + colorIndex + " 转换为颜色值: " + String.format("0x%08X", color));
        return color;
    }

    private void observeViewModel() {
        viewModel.getCursorSize().observe(getViewLifecycleOwner(), size -> {
            seekBarCursorSize.setProgress(size);
            textCursorSize.setText(String.valueOf(size));
            updateCursorPreview();
        });

        viewModel.getCursorColor().observe(getViewLifecycleOwner(), color -> {
            int checkedId = R.id.radio_red;
            if (color == 1) {
                checkedId = R.id.radio_green;
            } else if (color == 2) {
                checkedId = R.id.radio_blue;
            }
            radioGroupColor.check(checkedId);
            updateCursorPreview();
        });
    }

    private void updateCursorPreview() {
        Integer size = viewModel.getCursorSize().getValue();
        Integer color = viewModel.getCursorColor().getValue();
        if (size != null && color != null) {
            ViewGroup.LayoutParams params = cursorPreview.getLayoutParams();
            params.width = size * 10;
            params.height = size * 10;
            cursorPreview.setLayoutParams(params);
            int colorRes;
            switch (color) {
                case 1:
                    colorRes = android.R.color.holo_green_light;
                    break;
                case 2:
                    colorRes = android.R.color.holo_blue_light;
                    break;
                default:
                    colorRes = android.R.color.holo_red_light;
            }
            cursorPreview.setBackgroundResource(colorRes);
        }
    }

    private void loadCurrentConfig() {
        if (getActivity() instanceof ScreenshotDisplayActivity) {
            ScreenshotDisplayActivity activity = (ScreenshotDisplayActivity) getActivity();
            // 从Activity获取当前配置
            int color = activity.getLaserColor();
            int size = activity.getLaserSize();
            
            // 将颜色转换为索引
            int colorIndex = getColorIndexFromColor(color);
            
            // 设置到ViewModel
            viewModel.setCursorColor(colorIndex);
            viewModel.setCursorSize(size);
            
            Log.d("LaserFragment", "加载当前配置 - 颜色索引: " + colorIndex + ", 大小: " + size);
        }
    }

    private int getColorIndexFromColor(int color) {
        int colorIndex;
        switch (color) {
            case 0xFFFF0000: colorIndex = 0; break; // 红色
            case 0xFF00FF00: colorIndex = 1; break; // 绿色
            case 0xFF0000FF: colorIndex = 2; break; // 蓝色
            default: colorIndex = 0; break; // 默认红色
        }
        Log.d("LaserFragment", "颜色值 " + String.format("0x%08X", color) + " 转换为索引: " + colorIndex);
        return colorIndex;
    }
}
