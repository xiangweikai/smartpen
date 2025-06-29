package com.zuomu.smartpen.features.magnifier;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.zuomu.smartpen.R;
import com.zuomu.smartpen.ScreenshotDisplayActivity;

public class MagnifierFragment extends Fragment {

    private MagnifierViewModel viewModel;
    private SwitchMaterial switchMagnifier;
    private SeekBar seekBarMagnifierSize;
    private TextView textMagnifierSize;
    private ChipGroup chipGroupZoom;
    private RadioGroup radioGroupShape;
    private View magnifierPreview;
    private MaterialButton btnApply;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_magnifier, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("MagnifierFragment", "MagnifierFragment onViewCreated被调用");
        viewModel = new ViewModelProvider(this).get(MagnifierViewModel.class);
        initViews(view);
        setupListeners();
        observeViewModel();
        
        // 从Activity获取当前配置
        loadCurrentConfig();
    }

    private void initViews(View view) {
        switchMagnifier = view.findViewById(R.id.switch_magnifier);
        seekBarMagnifierSize = view.findViewById(R.id.seekbar_magnifier_size);
        textMagnifierSize = view.findViewById(R.id.text_magnifier_size);
        chipGroupZoom = view.findViewById(R.id.chip_group_zoom);
        radioGroupShape = view.findViewById(R.id.radio_group_shape);
        magnifierPreview = view.findViewById(R.id.magnifier_preview);
        btnApply = view.findViewById(R.id.btn_apply);
        
        // 为Chip和RadioButton添加点击监听器
        setupChipAndRadioListeners(view);
    }

    private void setupChipAndRadioListeners(View view) {
        // 为放大倍数Chip添加点击监听器
        view.findViewById(R.id.chip_2x).setOnClickListener(v -> {
            Log.d("MagnifierFragment", "2x Chip被点击");
            chipGroupZoom.check(R.id.chip_2x);
            viewModel.setZoomLevel(2);
            updateActivityConfig();
        });
        
        view.findViewById(R.id.chip_3x).setOnClickListener(v -> {
            Log.d("MagnifierFragment", "3x Chip被点击");
            chipGroupZoom.check(R.id.chip_3x);
            viewModel.setZoomLevel(3);
            updateActivityConfig();
        });
        
        view.findViewById(R.id.chip_4x).setOnClickListener(v -> {
            Log.d("MagnifierFragment", "4x Chip被点击");
            chipGroupZoom.check(R.id.chip_4x);
            viewModel.setZoomLevel(4);
            updateActivityConfig();
        });
        
        view.findViewById(R.id.chip_5x).setOnClickListener(v -> {
            Log.d("MagnifierFragment", "5x Chip被点击");
            chipGroupZoom.check(R.id.chip_5x);
            viewModel.setZoomLevel(5);
            updateActivityConfig();
        });
        
        // 为形状RadioButton添加点击监听器
        view.findViewById(R.id.radio_circle).setOnClickListener(v -> {
            Log.d("MagnifierFragment", "圆形RadioButton被点击");
            radioGroupShape.check(R.id.radio_circle);
            viewModel.setMagnifierShape(0);
            updateActivityConfig();
        });
        
        view.findViewById(R.id.radio_square).setOnClickListener(v -> {
            Log.d("MagnifierFragment", "方形RadioButton被点击");
            radioGroupShape.check(R.id.radio_square);
            viewModel.setMagnifierShape(1);
            updateActivityConfig();
        });
        
        // 添加TouchListener作为备选方案
        setupTouchListeners(view);
    }
    
    private void setupTouchListeners(View view) {
        // 为Chip添加TouchListener
        view.findViewById(R.id.chip_2x).setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                Log.d("MagnifierFragment", "2x Chip被触摸");
            }
            return false;
        });
        
        view.findViewById(R.id.chip_3x).setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                Log.d("MagnifierFragment", "3x Chip被触摸");
            }
            return false;
        });
        
        view.findViewById(R.id.chip_4x).setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                Log.d("MagnifierFragment", "4x Chip被触摸");
            }
            return false;
        });
        
        view.findViewById(R.id.chip_5x).setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                Log.d("MagnifierFragment", "5x Chip被触摸");
            }
            return false;
        });
        
        // 为RadioButton添加TouchListener
        view.findViewById(R.id.radio_circle).setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                Log.d("MagnifierFragment", "圆形RadioButton被触摸");
            }
            return false;
        });
        
        view.findViewById(R.id.radio_square).setOnTouchListener((v, event) -> {
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                Log.d("MagnifierFragment", "方形RadioButton被触摸");
            }
            return false;
        });
    }

    private void setupListeners() {
        switchMagnifier.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setEnabled(isChecked);
        });

        seekBarMagnifierSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textMagnifierSize.setText(String.valueOf(progress));
                viewModel.setMagnifierSize(progress);
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
            Log.d("MagnifierFragment", "应用设置按钮被点击");
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
            Integer size = viewModel.getMagnifierSize().getValue();
            Integer zoom = viewModel.getZoomLevel().getValue();
            Integer shape = viewModel.getMagnifierShape().getValue();
            
            if (size != null && zoom != null && shape != null) {
                Log.d("MagnifierFragment", "更新Activity配置 - 大小: " + size + ", 缩放: " + zoom + ", 形状: " + shape);
                activity.updateMagnifierConfig(size, zoom, shape);
            }
        }
    }

    private void observeViewModel() {
        viewModel.getMagnifierSize().observe(getViewLifecycleOwner(), size -> {
            seekBarMagnifierSize.setProgress(size);
            textMagnifierSize.setText(String.valueOf(size));
            updateMagnifierPreview();
        });

        viewModel.getZoomLevel().observe(getViewLifecycleOwner(), zoom -> {
            int checkedId = R.id.chip_2x;
            if (zoom == 3) {
                checkedId = R.id.chip_3x;
            } else if (zoom == 4) {
                checkedId = R.id.chip_4x;
            } else if (zoom == 5) {
                checkedId = R.id.chip_5x;
            }
            chipGroupZoom.check(checkedId);
            updateMagnifierPreview();
        });

        viewModel.getIsEnabled().observe(getViewLifecycleOwner(), enabled -> {
            switchMagnifier.setChecked(enabled);
            updateMagnifierPreview();
        });

        viewModel.getMagnifierShape().observe(getViewLifecycleOwner(), shape -> {
            int checkedId = R.id.radio_circle;
            if (shape == 1) {
                checkedId = R.id.radio_square;
            }
            radioGroupShape.check(checkedId);
            updateMagnifierPreview();
        });
    }

    private void updateMagnifierPreview() {
        Integer size = viewModel.getMagnifierSize().getValue();
        Integer zoom = viewModel.getZoomLevel().getValue();
        Boolean enabled = viewModel.getIsEnabled().getValue();

        if (size != null && zoom != null && enabled != null) {
            // TODO: 实现放大镜效果预览
        }
    }

    private void loadCurrentConfig() {
        if (getActivity() instanceof ScreenshotDisplayActivity) {
            ScreenshotDisplayActivity activity = (ScreenshotDisplayActivity) getActivity();
            // 从Activity获取当前配置
            int size = activity.getMagnifierSize();
            int zoom = activity.getMagnifierZoom();
            int shape = activity.getMagnifierShape();
            
            // 设置到ViewModel
            viewModel.setMagnifierSize(size);
            viewModel.setZoomLevel(zoom);
            viewModel.setMagnifierShape(shape);
            
            Log.d("MagnifierFragment", "加载当前配置 - 大小: " + size + ", 缩放: " + zoom + ", 形状: " + shape);
        }
    }
} 