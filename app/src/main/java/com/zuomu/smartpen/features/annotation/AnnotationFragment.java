package com.zuomu.smartpen.features.annotation;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.tabs.TabLayout;
import com.zuomu.smartpen.R;
import com.zuomu.smartpen.ScreenshotDisplayActivity;

public class AnnotationFragment extends Fragment {

    private AnnotationViewModel viewModel;
    private TabLayout tabTools;
    private ChipGroup chipGroupColors;
    private SeekBar seekBarThickness;
    private TextView textThickness;
    private TextView textEraserSizeTitle;
    private View layoutEraserSize;
    private SeekBar seekBarEraserSize;
    private TextView textEraserSize;
    private View previewArea;
    private MaterialButton btnClear;
    private MaterialButton btnApply;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_annotation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("AnnotationFragment", "AnnotationFragment onViewCreated被调用");
        viewModel = new ViewModelProvider(this).get(AnnotationViewModel.class);
        initViews(view);
        setupListeners();
        observeViewModel();
        
        // 从Activity获取当前配置
        loadCurrentConfig();
    }

    private void initViews(View view) {
        tabTools = view.findViewById(R.id.tab_tools);
        chipGroupColors = view.findViewById(R.id.chip_group_colors);
        seekBarThickness = view.findViewById(R.id.seekbar_thickness);
        textThickness = view.findViewById(R.id.text_thickness);
        textEraserSizeTitle = view.findViewById(R.id.text_eraser_size_title);
        layoutEraserSize = view.findViewById(R.id.layout_eraser_size);
        seekBarEraserSize = view.findViewById(R.id.seekbar_eraser_size);
        textEraserSize = view.findViewById(R.id.text_eraser_size);
        previewArea = view.findViewById(R.id.preview_area);
        btnClear = view.findViewById(R.id.btn_clear);
        btnApply = view.findViewById(R.id.btn_apply);
        
        // 为每个Chip添加点击监听器
        setupChipListeners(view);
    }

    private void setupChipListeners(View view) {
        // 为每个颜色Chip添加点击监听器
        view.findViewById(R.id.chip_black).setOnClickListener(v -> {
            Log.d("AnnotationFragment", "黑色Chip被点击");
            chipGroupColors.check(R.id.chip_black);
            viewModel.setAnnotationColor(0);
            updateActivityConfig();
        });
        
        view.findViewById(R.id.chip_red).setOnClickListener(v -> {
            Log.d("AnnotationFragment", "红色Chip被点击");
            chipGroupColors.check(R.id.chip_red);
            viewModel.setAnnotationColor(1);
            updateActivityConfig();
        });
        
        view.findViewById(R.id.chip_yellow).setOnClickListener(v -> {
            Log.d("AnnotationFragment", "黄色Chip被点击");
            chipGroupColors.check(R.id.chip_yellow);
            viewModel.setAnnotationColor(2);
            updateActivityConfig();
        });
        
        view.findViewById(R.id.chip_green).setOnClickListener(v -> {
            Log.d("AnnotationFragment", "绿色Chip被点击");
            chipGroupColors.check(R.id.chip_green);
            viewModel.setAnnotationColor(3);
            updateActivityConfig();
        });
        
        view.findViewById(R.id.chip_blue).setOnClickListener(v -> {
            Log.d("AnnotationFragment", "蓝色Chip被点击");
            chipGroupColors.check(R.id.chip_blue);
            viewModel.setAnnotationColor(4);
            updateActivityConfig();
        });
        
        // 添加TouchListener作为备选方案
        setupChipTouchListeners(view);
    }
    
    private void setupChipTouchListeners(View view) {
        // 为每个Chip添加TouchListener，确保能捕获到触摸事件
        view.findViewById(R.id.chip_black).setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                Log.d("AnnotationFragment", "黑色Chip被触摸");
            }
            return false; // 不消费事件，让ClickListener处理
        });
        
        view.findViewById(R.id.chip_red).setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                Log.d("AnnotationFragment", "红色Chip被触摸");
            }
            return false;
        });
        
        view.findViewById(R.id.chip_yellow).setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                Log.d("AnnotationFragment", "黄色Chip被触摸");
            }
            return false;
        });
        
        view.findViewById(R.id.chip_green).setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                Log.d("AnnotationFragment", "绿色Chip被触摸");
            }
            return false;
        });
        
        view.findViewById(R.id.chip_blue).setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                Log.d("AnnotationFragment", "蓝色Chip被触摸");
            }
            return false;
        });
    }

    private void setupListeners() {
        tabTools.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                boolean isEraser = tab.getPosition() == 1;
                textEraserSizeTitle.setVisibility(isEraser ? View.VISIBLE : View.GONE);
                layoutEraserSize.setVisibility(isEraser ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        seekBarThickness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textThickness.setText(String.valueOf(progress));
                viewModel.setLineThickness(progress);
                if (fromUser) {
                    updateActivityConfig();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        seekBarEraserSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textEraserSize.setText(String.valueOf(progress));
                viewModel.setEraserSize(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnClear.setOnClickListener(v -> {
            Log.d("AnnotationFragment", "清除批注按钮被点击");
            if (getActivity() instanceof ScreenshotDisplayActivity) {
                ScreenshotDisplayActivity activity = (ScreenshotDisplayActivity) getActivity();
                activity.clearAllDrawings();
            }
        });

        btnApply.setOnClickListener(v -> {
            Log.d("AnnotationFragment", "应用设置按钮被点击");
            viewModel.saveSettings();
            if (getActivity() instanceof ScreenshotDisplayActivity) {
                ScreenshotDisplayActivity activity = (ScreenshotDisplayActivity) getActivity();
                activity.hideFragment();
            }
        });
    }

    private void updateActivityConfig() {
        if (getActivity() instanceof ScreenshotDisplayActivity) {
            ScreenshotDisplayActivity activity = (ScreenshotDisplayActivity) getActivity();
            Integer color = viewModel.getAnnotationColor().getValue();
            Integer thickness = viewModel.getLineThickness().getValue();
            Log.d("AnnotationFragment", "更新Activity配置 - 颜色: " + color + ", 粗细: " + thickness);

            if (color != null && thickness != null) {
                int actualColor = getColorFromIndex(color);
                activity.updateAnnotationConfig(actualColor, thickness);
            }
        }
    }

    private int getColorFromIndex(int colorIndex) {
        int color;
        switch (colorIndex) {
            case 0: color = 0xFF000000; break; // 黑色
            case 1: color = 0xFFFF0000; break; // 红色
            case 2: color = 0xFFFFFF00; break; // 黄色
            case 3: color = 0xFF00FF00; break; // 绿色
            case 4: color = 0xFF0000FF; break; // 蓝色
            default: color = 0xFFFF0000; break; // 默认红色
        }
        Log.d("AnnotationFragment", "颜色索引 " + colorIndex + " 转换为颜色值: " + String.format("0x%08X", color));
        return color;
    }

    private void observeViewModel() {
        viewModel.getAnnotationColor().observe(getViewLifecycleOwner(), color -> {
            int checkedId = R.id.chip_black; // 默认选中黑色
            if (color == 0) {
                checkedId = R.id.chip_black;
            } else if (color == 1) {
                checkedId = R.id.chip_red;
            } else if (color == 2) {
                checkedId = R.id.chip_yellow;
            } else if (color == 3) {
                checkedId = R.id.chip_green;
            } else if (color == 4) {
                checkedId = R.id.chip_blue;
            }
            chipGroupColors.check(checkedId);
        });

        viewModel.getLineThickness().observe(getViewLifecycleOwner(), thickness -> {
            seekBarThickness.setProgress(thickness);
            textThickness.setText(String.valueOf(thickness));
        });

        viewModel.getEraserSize().observe(getViewLifecycleOwner(), size -> {
            seekBarEraserSize.setProgress(size);
            textEraserSize.setText(String.valueOf(size));
        });
    }

    private void loadCurrentConfig() {
        if (getActivity() instanceof ScreenshotDisplayActivity) {
            ScreenshotDisplayActivity activity = (ScreenshotDisplayActivity) getActivity();
            // 从Activity获取当前配置
            int color = activity.getAnnotationColor();
            int thickness = activity.getAnnotationThickness();
            
            // 将颜色转换为索引
            int colorIndex = getColorIndexFromColor(color);
            
            // 设置到ViewModel
            viewModel.setAnnotationColor(colorIndex);
            viewModel.setLineThickness(thickness);
            
            Log.d("AnnotationFragment", "加载当前配置 - 颜色索引: " + colorIndex + ", 粗细: " + thickness);
        }
    }

    private int getColorIndexFromColor(int color) {
        int colorIndex;
        switch (color) {
            case 0xFF000000: colorIndex = 0; break; // 黑色
            case 0xFFFF0000: colorIndex = 1; break; // 红色
            case 0xFFFFFF00: colorIndex = 2; break; // 黄色
            case 0xFF00FF00: colorIndex = 3; break; // 绿色
            case 0xFF0000FF: colorIndex = 4; break; // 蓝色
            default: colorIndex = 1; break; // 默认红色
        }
        Log.d("AnnotationFragment", "颜色值 " + String.format("0x%08X", color) + " 转换为索引: " + colorIndex);
        return colorIndex;
    }
} 