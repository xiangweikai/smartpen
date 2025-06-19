package com.zuomu.smartpen;

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

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private FragmentManager fragmentManager;

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
} 