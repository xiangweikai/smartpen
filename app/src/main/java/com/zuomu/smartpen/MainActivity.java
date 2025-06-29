package com.zuomu.smartpen;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.zuomu.smartpen.features.annotation.AnnotationFragment;
import com.zuomu.smartpen.features.laser.LaserFragment;
import com.zuomu.smartpen.features.magnifier.MagnifierFragment;
import com.zuomu.smartpen.features.recording.RecordingFragment;
import com.zuomu.smartpen.features.spotlight.SpotlightFragment;
import com.zuomu.smartpen.features.timer.TimerFragment;
import com.zuomu.smartpen.features.voice.VoiceFragment;
import android.util.Log;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, MouseEventCallback {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private FragmentManager fragmentManager;
    
    // 定义触发截图的按键代码（这里使用F12作为示例，您可以根据需要修改）
    private static final int TRIGGER_KEY_CODE = 88; // F12键的代码
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupToolbar();
        setupNavigation();
        
        // 默认显示激光模块
        loadFragment(new LaserFragment());
    }

    @Override
    protected void onResume() {
        super.onResume();
        NativeUtils.startReadingMouseEvents(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        NativeUtils.stopReadingMouseEvents();
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        toolbar = findViewById(R.id.toolbar);
        fragmentManager = getSupportFragmentManager();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void setupNavigation() {
        navigationView.setNavigationItemSelectedListener(this);
        
        // 设置默认选中项
        navigationView.setCheckedItem(R.id.nav_laser);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        String title = "";

        int itemId = item.getItemId();
        if (itemId == R.id.nav_laser) {
            fragment = new LaserFragment();
            title = getString(R.string.laser_title);
        } else if (itemId == R.id.nav_spotlight) {
            fragment = new SpotlightFragment();
            title = getString(R.string.spotlight_title);
        } else if (itemId == R.id.nav_magnifier) {
            fragment = new MagnifierFragment();
            title = getString(R.string.magnifier_title);
        } else if (itemId == R.id.nav_annotation) {
            fragment = new AnnotationFragment();
            title = getString(R.string.annotation_title);
        } else if (itemId == R.id.nav_recording) {
            fragment = new RecordingFragment();
            title = getString(R.string.recording_title);
        } else if (itemId == R.id.nav_timer) {
            fragment = new TimerFragment();
            title = getString(R.string.timer_title);
        } else if (itemId == R.id.nav_voice) {
            fragment = new VoiceFragment();
            title = getString(R.string.voice_title);
        }

        if (fragment != null) {
            loadFragment(fragment);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(title);
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    @Override
    public void onMouseEvent(MouseEvent event) {
        // Handle the mouse event on the UI thread
        runOnUiThread(() -> {
            Log.d("MainActivity", "收到鼠标事件: " + event.toString());
            
            // 检查是否是触发截图的按键事件
            if (event.code == TRIGGER_KEY_CODE && event.value == 1) { // 按键按下
                Log.d("MainActivity", "检测到触发截图按键被按下，开始截图流程...");
                Log.d("MainActivity", "按键代码: " + event.code + ", 按键值: " + event.value);
                takeScreenshotAndLaunchActivity();
            }
        });
    }
    
    private void takeScreenshotAndLaunchActivity() {
        Log.d("MainActivity", "开始调用截图功能...");
        ScreenshotUtils.takeScreenshot(this, new ScreenshotUtils.ScreenshotCallback() {
            @Override
            public void onScreenshotTaken(String filePath) {
                Log.d("MainActivity", "截图成功，文件路径: " + filePath);
                
                try {
                    // 启动显示Activity，传递文件路径
                    Intent intent = new Intent(MainActivity.this, ScreenshotDisplayActivity.class);
                    intent.putExtra("screenshot_path", filePath);
                    Log.d("MainActivity", "启动ScreenshotDisplayActivity...");
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e("MainActivity", "处理截图失败: " + e.getMessage(), e);
                }
            }

            @Override
            public void onScreenshotError(String error) {
                Log.e("MainActivity", "截图失败: " + error);
                // 可以在这里显示错误提示
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("MainActivity", "onActivityResult被调用 - requestCode: " + requestCode + ", resultCode: " + resultCode);
        // 处理截图权限请求结果
        ScreenshotUtils.handleActivityResult(requestCode, resultCode, data, this);
    }
} 