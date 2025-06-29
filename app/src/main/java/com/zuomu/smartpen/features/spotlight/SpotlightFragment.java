package com.zuomu.smartpen.features.spotlight;

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
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.zuomu.smartpen.R;
import com.zuomu.smartpen.ScreenshotDisplayActivity;

public class SpotlightFragment extends Fragment {

    private SpotlightViewModel viewModel;
    private SwitchMaterial switchSpotlight;
    private SeekBar seekBarSpotlightSize;
    private TextView textSpotlightSize;
    private SeekBar seekBarDarkness;
    private TextView textDarkness;
    private View spotlightPreview;
    private ChipGroup chipGroupPresets;
    private MaterialButton btnApply;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_spotlight, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("SpotlightFragment", "SpotlightFragment onViewCreated被调用");
        viewModel = new ViewModelProvider(this).get(SpotlightViewModel.class);
        initViews(view);
        setupListeners();
        observeViewModel();
        
        // 从Activity获取当前配置
        loadCurrentConfig();
    }

    private void initViews(View view) {
        switchSpotlight = view.findViewById(R.id.switch_spotlight);
        seekBarSpotlightSize = view.findViewById(R.id.seekbar_spotlight_size);
        textSpotlightSize = view.findViewById(R.id.text_spotlight_size);
        seekBarDarkness = view.findViewById(R.id.seekbar_darkness);
        textDarkness = view.findViewById(R.id.text_darkness);
        spotlightPreview = view.findViewById(R.id.spotlight_preview);
        chipGroupPresets = view.findViewById(R.id.chip_group_presets);
        btnApply = view.findViewById(R.id.btn_apply);
    }

    private void setupListeners() {
        switchSpotlight.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setEnabled(isChecked);
        });

        seekBarSpotlightSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textSpotlightSize.setText(String.valueOf(progress));
                viewModel.setSpotlightSize(progress);
                if (fromUser) {
                    updateActivityConfig();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        seekBarDarkness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textDarkness.setText(progress + "%");
                viewModel.setDarkness(progress);
                if (fromUser) {
                    updateActivityConfig();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        chipGroupPresets.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chip_small) {
                viewModel.setSpotlightSize(2);
            } else if (checkedId == R.id.chip_medium) {
                viewModel.setSpotlightSize(3);
            } else if (checkedId == R.id.chip_large) {
                viewModel.setSpotlightSize(4);
            }
            updateActivityConfig();
        });

        btnApply.setOnClickListener(v -> {
            Log.d("SpotlightFragment", "应用设置按钮被点击");
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
            Integer size = viewModel.getSpotlightSize().getValue();
            Integer darkness = viewModel.getDarkness().getValue();
            
            if (size != null && darkness != null) {
                Log.d("SpotlightFragment", "更新Activity配置 - 大小: " + size + ", 暗度: " + darkness);
                activity.updateSpotlightConfig(size, darkness);
            }
        }
    }

    private void observeViewModel() {
        viewModel.getSpotlightSize().observe(getViewLifecycleOwner(), size -> {
            seekBarSpotlightSize.setProgress(size);
            textSpotlightSize.setText(String.valueOf(size));
            updateSpotlightPreview();
        });

        viewModel.getDarkness().observe(getViewLifecycleOwner(), dark -> {
            seekBarDarkness.setProgress(dark);
            textDarkness.setText(dark + "%");
            updateSpotlightPreview();
        });

        viewModel.getIsEnabled().observe(getViewLifecycleOwner(), enabled -> {
            switchSpotlight.setChecked(enabled);
            updateSpotlightPreview();
        });
    }

    private void updateSpotlightPreview() {
        Integer size = viewModel.getSpotlightSize().getValue();
        Integer dark = viewModel.getDarkness().getValue();
        Boolean enabled = viewModel.getIsEnabled().getValue();

        if (size != null && dark != null && enabled != null) {
            // TODO: 实现聚光灯效果预览
        }
    }

    private void loadCurrentConfig() {
        if (getActivity() instanceof ScreenshotDisplayActivity) {
            ScreenshotDisplayActivity activity = (ScreenshotDisplayActivity) getActivity();
            // 从Activity获取当前配置
            int size = activity.getSpotlightSize();
            int darkness = activity.getSpotlightDarkness();
            
            // 设置到ViewModel
            viewModel.setSpotlightSize(size);
            viewModel.setDarkness(darkness);
            
            Log.d("SpotlightFragment", "加载当前配置 - 大小: " + size + ", 暗度: " + darkness);
        }
    }
} 