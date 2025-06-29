package com.zuomu.smartpen;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ScreenshotTestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screenshot_test);

        Button btnTestScreenshot = findViewById(R.id.btn_test_screenshot);
        btnTestScreenshot.setOnClickListener(v -> testScreenshot());
    }

    private void testScreenshot() {
        ScreenshotUtils.takeScreenshot(this, new ScreenshotUtils.ScreenshotCallback() {
            @Override
            public void onScreenshotTaken(String filePath) {
                Toast.makeText(ScreenshotTestActivity.this, 
                    getString(R.string.screenshot_success) + ": " + filePath, Toast.LENGTH_SHORT).show();
                
                // 启动截图显示Activity
                Intent intent = new Intent(ScreenshotTestActivity.this, ScreenshotDisplayActivity.class);
                intent.putExtra("screenshot_path", filePath);
                startActivity(intent);
            }

            @Override
            public void onScreenshotError(String error) {
                Toast.makeText(ScreenshotTestActivity.this, 
                    getString(R.string.screenshot_failed) + ": " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ScreenshotUtils.handleActivityResult(requestCode, resultCode, data, this);
    }
} 