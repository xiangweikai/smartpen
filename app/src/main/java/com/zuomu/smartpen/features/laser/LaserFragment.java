package com.zuomu.smartpen.features.laser;

import android.os.Bundle;
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
        viewModel = new ViewModelProvider(this).get(LaserViewModel.class);
        initViews(view);
        setupListeners();
        observeViewModel();
    }

    private void initViews(View view) {
        seekBarCursorSize = view.findViewById(R.id.seekbar_cursor_size);
        textCursorSize = view.findViewById(R.id.text_cursor_size);
        radioGroupColor = view.findViewById(R.id.radio_group_color);
        cursorPreview = view.findViewById(R.id.cursor_preview);
        btnApply = view.findViewById(R.id.btn_apply);
    }

    private void setupListeners() {
        seekBarCursorSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textCursorSize.setText(String.valueOf(progress));
                viewModel.setCursorSize(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        radioGroupColor.setOnCheckedChangeListener((group, checkedId) -> {
            int color = 0;
            if (checkedId == R.id.radio_red) {
                color = 0;
            } else if (checkedId == R.id.radio_green) {
                color = 1;
            } else if (checkedId == R.id.radio_blue) {
                color = 2;
            }
            viewModel.setCursorColor(color);
        });

        btnApply.setOnClickListener(v -> viewModel.saveSettings());
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
}
