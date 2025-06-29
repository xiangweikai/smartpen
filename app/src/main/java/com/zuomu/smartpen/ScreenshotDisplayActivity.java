package com.zuomu.smartpen;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.zuomu.smartpen.features.annotation.AnnotationFragment;
import com.zuomu.smartpen.features.eraser.EraserFragment;
import com.zuomu.smartpen.features.laser.LaserFragment;
import com.zuomu.smartpen.features.magnifier.MagnifierFragment;
import com.zuomu.smartpen.features.spotlight.SpotlightFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ScreenshotDisplayActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "ScreenshotConfig";
    private static final String KEY_ANNOTATION_COLOR = "annotation_color";
    private static final String KEY_ANNOTATION_THICKNESS = "annotation_thickness";
    private static final String KEY_LASER_COLOR = "laser_color";
    private static final String KEY_LASER_SIZE = "laser_size";
    private static final String KEY_MAGNIFIER_SIZE = "magnifier_size";
    private static final String KEY_MAGNIFIER_ZOOM = "magnifier_zoom";
    private static final String KEY_MAGNIFIER_SHAPE = "magnifier_shape";
    private static final String KEY_SPOTLIGHT_SIZE = "spotlight_size";
    private static final String KEY_SPOTLIGHT_DARKNESS = "spotlight_darkness";
    private static final String KEY_ERASER_SIZE = "eraser_size";

    private ImageView screenshotImageView;
    private BottomNavigationView bottomNavigationView;
    private FragmentManager fragmentManager;
    private Bitmap screenshotBitmap;
    private Bitmap workingBitmap; // 用于绘制的bitmap
    private Canvas workingCanvas;
    private Paint paint;
    private Path path;
    
    // 当前选择的工具
    private Tool currentTool = Tool.ANNOTATION; // Tracks the currently selected tool
    private boolean isConfigFragmentVisible = false; // Tracks if a config fragment is currently visible
    private boolean isInitialSetup = true; // Flag for initial setup
    
    // 绘制相关
    private float lastX, lastY;
    private List<DrawPath> drawPaths = new ArrayList<>();
    private DrawPath currentPath;
    
    // 放大镜相关
    private float magnifierRadius = 100f;
    private float magnifierScale = 2.0f;
    private int magnifierShape = 0; // 0=圆形, 1=方形
    
    // 聚光灯相关
    private float spotlightRadius = 150f;
    private int spotlightDarkness = 80; // 暗度百分比
    private boolean spotlightActive = false; // Tracks if spotlight is currently active
    private float lastSpotlightX = -1f;
    private float lastSpotlightY = -1f;
    
    // 激光笔相关
    private float laserRadius = 10f;
    
    // 工具配置
    private int annotationColor = Color.RED;
    private int annotationThickness = 5;
    private int laserColor = Color.RED;
    private int laserSize = 10;
    private int eraserSize = 20;

    public enum Tool {
        ANNOTATION, ERASER, LASER, MAGNIFIER, SPOTLIGHT
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("ScreenshotDisplayActivity", "Activity创建开始");
        setContentView(R.layout.activity_screenshot_display);

        initViews();
        loadConfigFromPreferences();
        setupBottomNavigation();
        setupDrawing();
        
        // 获取传递过来的截图文件路径
        if (getIntent().hasExtra("screenshot_path")) {
            Log.d("ScreenshotDisplayActivity", "从Intent中获取截图文件路径");
            String screenshotPath = getIntent().getStringExtra("screenshot_path");
            if (screenshotPath != null) {
                Log.d("ScreenshotDisplayActivity", "截图文件路径: " + screenshotPath);
                screenshotBitmap = loadBitmapFromFile(screenshotPath);
                if (screenshotBitmap != null) {
                    Log.d("ScreenshotDisplayActivity", "截图加载成功，大小: " + screenshotBitmap.getWidth() + "x" + screenshotBitmap.getHeight());
                    setupWorkingBitmap(); // Initialize workingBitmap once
                    screenshotImageView.setImageBitmap(workingBitmap);
                    
                    // 添加测试按钮
                    addTestButton();
                    
                    Log.d("ScreenshotDisplayActivity", "截图显示设置完成");
                } else {
                    Log.w("ScreenshotDisplayActivity", "截图加载失败");
                }
            } else {
                Log.w("ScreenshotDisplayActivity", "截图文件路径为空");
            }
        } else {
            Log.w("ScreenshotDisplayActivity", "Intent中没有截图文件路径");
        }
        
        // 默认不显示Fragment
        Log.d("ScreenshotDisplayActivity", "默认不显示配置Fragment");
    }

    private void loadConfigFromPreferences() {
        Log.d("ScreenshotDisplayActivity", "从SharedPreferences加载配置");
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        // 加载批注配置
        annotationColor = prefs.getInt(KEY_ANNOTATION_COLOR, Color.RED);
        annotationThickness = prefs.getInt(KEY_ANNOTATION_THICKNESS, 5);
        
        // 加载激光笔配置
        laserColor = prefs.getInt(KEY_LASER_COLOR, Color.RED);
        laserSize = prefs.getInt(KEY_LASER_SIZE, 10);
        
        // 加载橡皮擦配置
        eraserSize = prefs.getInt(KEY_ERASER_SIZE, 20);
        
        // 加载放大镜配置
        int magnifierSize = prefs.getInt(KEY_MAGNIFIER_SIZE, 5);
        int magnifierZoom = prefs.getInt(KEY_MAGNIFIER_ZOOM, 2);
        magnifierShape = prefs.getInt(KEY_MAGNIFIER_SHAPE, 0);
        magnifierRadius = magnifierSize * 20f;
        magnifierScale = magnifierZoom;
        
        // 加载聚光灯配置
        int spotlightSize = prefs.getInt(KEY_SPOTLIGHT_SIZE, 3);
        spotlightDarkness = prefs.getInt(KEY_SPOTLIGHT_DARKNESS, 80);
        spotlightRadius = spotlightSize * 50f;
        
        Log.d("ScreenshotDisplayActivity", "配置加载完成 - 批注颜色: " + String.format("0x%08X", annotationColor) + 
              ", 批注粗细: " + annotationThickness + 
              ", 激光颜色: " + String.format("0x%08X", laserColor) + 
              ", 激光大小: " + laserSize + 
              ", 聚光灯大小: " + spotlightSize + 
              ", 聚光灯暗度: " + spotlightDarkness + 
              ", 聚光灯半径: " + spotlightRadius);
    }

    private void saveConfigToPreferences() {
        Log.d("ScreenshotDisplayActivity", "保存配置到SharedPreferences");
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        // 保存批注配置
        editor.putInt(KEY_ANNOTATION_COLOR, annotationColor);
        editor.putInt(KEY_ANNOTATION_THICKNESS, annotationThickness);
        
        // 保存激光笔配置
        editor.putInt(KEY_LASER_COLOR, laserColor);
        editor.putInt(KEY_LASER_SIZE, laserSize);
        
        // 保存橡皮擦配置
        editor.putInt(KEY_ERASER_SIZE, eraserSize);
        
        // 保存放大镜配置
        editor.putInt(KEY_MAGNIFIER_SIZE, (int)(magnifierRadius / 20f));
        editor.putInt(KEY_MAGNIFIER_ZOOM, (int)magnifierScale);
        editor.putInt(KEY_MAGNIFIER_SHAPE, magnifierShape);
        
        // 保存聚光灯配置
        editor.putInt(KEY_SPOTLIGHT_SIZE, (int)(spotlightRadius / 50f));
        editor.putInt(KEY_SPOTLIGHT_DARKNESS, spotlightDarkness);
        
        editor.apply();
        Log.d("ScreenshotDisplayActivity", "配置保存完成");
    }

    private void setupWorkingBitmap() {
        if (screenshotBitmap != null) {
            // 强制使用ARGB_8888配置以确保透明度支持
            workingBitmap = screenshotBitmap.copy(Bitmap.Config.ARGB_8888, true);
            workingCanvas = new Canvas(workingBitmap);
            Log.d("ScreenshotDisplayActivity", "工作Bitmap创建完成 - 配置: " + workingBitmap.getConfig() + ", 尺寸: " + workingBitmap.getWidth() + "x" + workingBitmap.getHeight());
        }
    }

    private void setupDrawing() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        
        path = new Path();
    }

    private Bitmap loadBitmapFromFile(String filePath) {
        try {
            Log.d("ScreenshotDisplayActivity", "开始从文件加载Bitmap: " + filePath);
            File file = new File(filePath);
            if (file.exists()) {
                Log.d("ScreenshotDisplayActivity", "文件存在，开始加载");
                Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                if (bitmap != null) {
                    Log.d("ScreenshotDisplayActivity", "Bitmap加载成功");
                    return bitmap;
                } else {
                    Log.e("ScreenshotDisplayActivity", "BitmapFactory.decodeFile返回null");
                }
            } else {
                Log.e("ScreenshotDisplayActivity", "文件不存在: " + filePath);
            }
        } catch (Exception e) {
            Log.e("ScreenshotDisplayActivity", "加载Bitmap失败: " + e.getMessage(), e);
        }
        return null;
    }

    private void initViews() {
        Log.d("ScreenshotDisplayActivity", "初始化视图");
        screenshotImageView = findViewById(R.id.screenshot_image_view);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fragmentManager = getSupportFragmentManager();
        
        // 设置触摸监听
        screenshotImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return handleTouch(event);
            }
        });
        
        Log.d("ScreenshotDisplayActivity", "视图初始化完成");
    }

    private boolean handleTouch(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        
        // 转换触摸坐标到Bitmap坐标
        float[] bitmapCoords = convertTouchToBitmapCoordinates(x, y);
        float bitmapX = bitmapCoords[0];
        float bitmapY = bitmapCoords[1];
        
        Log.d("ScreenshotDisplayActivity", "触摸坐标: (" + x + ", " + y + ") -> Bitmap坐标: (" + bitmapX + ", " + bitmapY + ")");
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = bitmapX;
                lastY = bitmapY;
                if (currentTool == Tool.ANNOTATION) {
                    startAnnotation(bitmapX, bitmapY);
                } else if (currentTool == Tool.ERASER) {
                    startEraser(bitmapX, bitmapY);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (currentTool == Tool.ANNOTATION) {
                    continueAnnotation(bitmapX, bitmapY);
                } else if (currentTool == Tool.ERASER) {
                    performEraserOperationAtPoint(bitmapX, bitmapY);
                    updateEraserDisplay(bitmapX, bitmapY);
                    lastX = bitmapX;
                    lastY = bitmapY;
                } else if (currentTool == Tool.MAGNIFIER) {
                    showMagnifier(bitmapX, bitmapY);
                } else if (currentTool == Tool.SPOTLIGHT) {
                    showSpotlight(bitmapX, bitmapY);
                } else if (currentTool == Tool.LASER) {
                    showLaser(bitmapX, bitmapY);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (currentTool == Tool.ANNOTATION) {
                    endAnnotation();
                } else if (currentTool == Tool.ERASER) {
                    endEraser();
                } else if (currentTool == Tool.MAGNIFIER || currentTool == Tool.SPOTLIGHT || currentTool == Tool.LASER) {
                    // For temporary tools, the effect should persist until tool change
                }
                break;
        }
        return true;
    }

    private float[] convertTouchToBitmapCoordinates(float touchX, float touchY) {
        if (screenshotBitmap == null || screenshotImageView == null) {
            Log.w("ScreenshotDisplayActivity", "screenshotBitmap或screenshotImageView为null，返回原始坐标");
            return new float[]{touchX, touchY};
        }
        
        // 获取ImageView的边界
        int imageViewWidth = screenshotImageView.getWidth();
        int imageViewHeight = screenshotImageView.getHeight();
        
        // 获取Bitmap的尺寸
        int bitmapWidth = screenshotBitmap.getWidth();
        int bitmapHeight = screenshotBitmap.getHeight();
        
        Log.d("ScreenshotDisplayActivity", "ImageView尺寸: " + imageViewWidth + "x" + imageViewHeight);
        Log.d("ScreenshotDisplayActivity", "Bitmap尺寸: " + bitmapWidth + "x" + bitmapHeight);
        
        // 如果ImageView还没有测量完成，返回原始坐标
        if (imageViewWidth <= 0 || imageViewHeight <= 0) {
            Log.w("ScreenshotDisplayActivity", "ImageView尺寸无效，返回原始坐标");
            return new float[]{touchX, touchY};
        }
        
        // 计算ImageView中Bitmap的实际显示区域
        float scaleX = (float) imageViewWidth / bitmapWidth;
        float scaleY = (float) imageViewHeight / bitmapHeight;
        float scale = Math.min(scaleX, scaleY); // 保持宽高比
        
        // 计算Bitmap在ImageView中的实际显示尺寸
        float displayWidth = bitmapWidth * scale;
        float displayHeight = bitmapHeight * scale;
        
        // 计算Bitmap在ImageView中的偏移量（居中显示）
        float offsetX = (imageViewWidth - displayWidth) / 2f;
        float offsetY = (imageViewHeight - displayHeight) / 2f;
        
        Log.d("ScreenshotDisplayActivity", "缩放比例: " + scale + ", 显示尺寸: " + displayWidth + "x" + displayHeight);
        Log.d("ScreenshotDisplayActivity", "偏移量: (" + offsetX + ", " + offsetY + ")");
        
        // 将触摸坐标转换为Bitmap坐标
        float bitmapX = (touchX - offsetX) / scale;
        float bitmapY = (touchY - offsetY) / scale;
        
        // 确保坐标在Bitmap范围内
        bitmapX = Math.max(0, Math.min(bitmapX, bitmapWidth));
        bitmapY = Math.max(0, Math.min(bitmapY, bitmapHeight));
        
        Log.d("ScreenshotDisplayActivity", "转换后坐标: (" + bitmapX + ", " + bitmapY + ")");
        
        return new float[]{bitmapX, bitmapY};
    }

    private void startAnnotation(float x, float y) {
        Log.d("ScreenshotDisplayActivity", "开始批注绘制，起始点: (" + x + ", " + y + ")");
        Log.d("ScreenshotDisplayActivity", "批注颜色: " + String.format("0x%08X", annotationColor) + ", 粗细: " + annotationThickness);
        
        // Create a new Paint for this annotation stroke
        Paint annotationPaint = new Paint(paint);
        annotationPaint.setColor(annotationColor);
        annotationPaint.setStrokeWidth(annotationThickness);
        
        // Start a new path for this stroke
        Path annotationPath = new Path();
        annotationPath.moveTo(x, y);
        
        // Store the path and its paint
        currentPath = new DrawPath();
        currentPath.segments = new ArrayList<>();
        currentPath.paint = annotationPaint;
        
        lastX = x;
        lastY = y;
    }

    private void continueAnnotation(float x, float y) {
        if (currentPath != null) {
            Log.d("ScreenshotDisplayActivity", "继续批注绘制，当前点: (" + x + ", " + y + ")");
            // Create a line segment and add to current path
            currentPath.segments.add(new LineSegment(lastX, lastY, x, y));
            
            // Draw the current segment onto the workingCanvas
            workingCanvas.drawLine(lastX, lastY, x, y, currentPath.paint);
            lastX = x;
            lastY = y;
            
            // Update the ImageView (only for annotation drawing feedback)
            screenshotImageView.setImageBitmap(workingBitmap);
        }
    }

    private void endAnnotation() {
        if (currentPath != null) {
            Log.d("ScreenshotDisplayActivity", "结束批注绘制");
            drawPaths.add(currentPath);
            currentPath = null;
            redrawBitmap(); // Update ImageView with final annotation
        }
    }

    private void startEraser(float x, float y) {
        Log.d("ScreenshotDisplayActivity", "开始橡皮擦，起始点: (" + x + ", " + y + "), 大小: " + eraserSize);
        lastX = x;
        lastY = y;
        updateEraserDisplay(x, y);
    }

    private void continueEraser(float x, float y) {
        // This method is no longer needed as erasing is handled in ACTION_MOVE directly
    }

    private void endEraser() {
        Log.d("ScreenshotDisplayActivity", "结束橡皮擦");
        // Clear the eraser display and redraw the final result
        redrawBitmap();
    }

    private void updateEraserDisplay(float x, float y) {
        // Create a temporary bitmap to draw the eraser display on
        Bitmap tempBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas tempCanvas = new Canvas(tempBitmap);
        
        // Draw eraser area (semi-transparent red circle)
        Paint eraserPaint = new Paint();
        eraserPaint.setAntiAlias(true);
        eraserPaint.setColor(Color.argb(100, 255, 0, 0)); // Semi-transparent red
        eraserPaint.setStyle(Paint.Style.FILL);
        tempCanvas.drawCircle(x, y, eraserSize / 2f, eraserPaint);
        
        // Draw eraser border
        eraserPaint.setStyle(Paint.Style.STROKE);
        eraserPaint.setColor(Color.RED);
        eraserPaint.setStrokeWidth(2);
        tempCanvas.drawCircle(x, y, eraserSize / 2f, eraserPaint);
        
        screenshotImageView.setImageBitmap(tempBitmap);
    }

    private void performEraserOperationAtPoint(float x, float y) {
        float eraserRadius = eraserSize / 2f;
        List<DrawPath> newDrawPaths = new ArrayList<>();

        for (DrawPath drawPath : drawPaths) {
            List<LineSegment> remainingSegments = new ArrayList<>();
            for (LineSegment segment : drawPath.segments) {
                if (!segment.intersectsCircle(x, y, eraserRadius)) {
                    remainingSegments.add(segment);
                }
            }

            if (!remainingSegments.isEmpty()) {
                DrawPath newDrawPath = new DrawPath();
                newDrawPath.segments = remainingSegments;
                newDrawPath.paint = new Paint(drawPath.paint);
                newDrawPaths.add(newDrawPath);
            }
        }
        drawPaths.clear();
        drawPaths.addAll(newDrawPaths);

        // Redraw the entire workingBitmap to reflect the changes
        redrawBitmap();
    }

    

    

    private void showMagnifier(float x, float y) {
        // Create a temporary bitmap to draw the magnifier effect on
        Bitmap tempBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas tempCanvas = new Canvas(tempBitmap);

        // Draw magnifier effect
        Paint magnifierPaint = new Paint();
        magnifierPaint.setAntiAlias(true);

        // Calculate magnified region
        float left = x - magnifierRadius;
        float top = y - magnifierRadius;
        float right = x + magnifierRadius;
        float bottom = y + magnifierRadius;

        // Ensure bounds are within the bitmap
        left = Math.max(0, left);
        top = Math.max(0, top);
        right = Math.min(tempBitmap.getWidth(), right);
        bottom = Math.min(tempBitmap.getHeight(), bottom);

        // Draw border based on shape
        magnifierPaint.setStyle(Paint.Style.STROKE);
        magnifierPaint.setColor(Color.WHITE);
        magnifierPaint.setStrokeWidth(3);

        if (magnifierShape == 0) {
            // Circular
            tempCanvas.drawCircle(x, y, magnifierRadius, magnifierPaint);
        } else {
            // Rectangular
            tempCanvas.drawRect(left, top, right, bottom, magnifierPaint);
        }

        // Create magnified effect
        try {
            // Extract source region from the original workingBitmap
            Bitmap sourceRegion = Bitmap.createBitmap(workingBitmap,
                (int)left, (int)top,
                (int)(right - left), (int)(bottom - top));

            // Create scaled region
            int scaledWidth = (int)((right - left) * magnifierScale);
            int scaledHeight = (int)((bottom - top) * magnifierScale);
            Bitmap scaledRegion = Bitmap.createScaledBitmap(sourceRegion, scaledWidth, scaledHeight, true);

            // Calculate draw position to center magnified region at touch point
            float drawLeft = x - scaledWidth / 2f;
            float drawTop = y - scaledHeight / 2f;

            if (magnifierShape == 0) {
                // Circular magnifier
                Bitmap circularMagnifiedBitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888);
                Canvas circularCanvas = new Canvas(circularMagnifiedBitmap);

                // 1. Draw the mask shape (a solid circle) onto the temporary bitmap (this is our Dst)
                Paint maskShapePaint = new Paint();
                maskShapePaint.setAntiAlias(true);
                maskShapePaint.setColor(Color.BLACK); // Any opaque color will do for the mask
                circularCanvas.drawCircle(scaledWidth / 2f, scaledHeight / 2f, Math.min(scaledWidth, scaledHeight) / 2f, maskShapePaint);

                // 2. Draw the scaled content (our Src) onto the same canvas with SRC_IN xfermode
                Paint contentPaint = new Paint();
                contentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
                circularCanvas.drawBitmap(scaledRegion, 0, 0, contentPaint);

                // Draw the cropped circular magnified content onto the temporary canvas
                tempCanvas.drawBitmap(circularMagnifiedBitmap, drawLeft, drawTop, null);
                circularMagnifiedBitmap.recycle();
            } else {
                // Rectangular magnifier
                tempCanvas.drawBitmap(scaledRegion, drawLeft, drawTop, null);
            }

            // Clean up temporary bitmaps
            sourceRegion.recycle();
            scaledRegion.recycle();

        } catch (Exception e) {
            Log.e("ScreenshotDisplayActivity", "Magnifier effect creation failed: " + e.getMessage(), e);
            // If magnification fails, draw a simple highlight
            magnifierPaint.setStyle(Paint.Style.FILL);
            magnifierPaint.setColor(Color.argb(100, 255, 255, 255));
            if (magnifierShape == 0) {
                tempCanvas.drawCircle(x, y, magnifierRadius - 5, magnifierPaint);
            } else {
                tempCanvas.drawRect(left + 5, top + 5, right - 5, bottom - 5, magnifierPaint);
            }
        }

        screenshotImageView.setImageBitmap(tempBitmap);
    }

    private void showSpotlight(float x, float y) {
        Log.d("ScreenshotDisplayActivity", "显示聚光灯 - 位置: (" + x + ", " + y + "), 半径: " + spotlightRadius + ", 暗度: " + spotlightDarkness);

        // Create a temporary bitmap to draw the spotlight effect on
        Bitmap tempBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas tempCanvas = new Canvas(tempBitmap);

        // Calculate the darkness color
        int darknessAlpha = spotlightDarkness;
        int darknessColor = Color.argb(darknessAlpha, 0, 0, 0);

        // Create a temporary mask bitmap
        Bitmap maskBitmap = Bitmap.createBitmap(tempBitmap.getWidth(), tempBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas maskCanvas = new Canvas(maskBitmap);

        // Draw dark overlay on maskBitmap
        Paint darkPaint = new Paint();
        darkPaint.setColor(darknessColor);
        maskCanvas.drawRect(0, 0, maskBitmap.getWidth(), maskBitmap.getHeight(), darkPaint);
        Log.d("ScreenshotDisplayActivity", "绘制黑色遮罩到maskBitmap - 透明度: " + darknessAlpha);

        // Punch hole in maskBitmap using PorterDuff.Mode.CLEAR
        Paint clearPaint = new Paint();
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        clearPaint.setAntiAlias(true);
        maskCanvas.drawCircle(x, y, spotlightRadius, clearPaint);
        Log.d("ScreenshotDisplayActivity", "在maskBitmap上清除聚光灯区域");

        // Apply maskBitmap to tempCanvas
        tempCanvas.drawBitmap(maskBitmap, 0, 0, null);
        Log.d("ScreenshotDisplayActivity", "将maskBitmap绘制到tempCanvas");

        // Draw the spotlight border
        Paint borderPaint = new Paint();
        borderPaint.setAntiAlias(true);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(Color.WHITE);
        borderPaint.setStrokeWidth(2);
        tempCanvas.drawCircle(x, y, spotlightRadius, borderPaint);
        Log.d("ScreenshotDisplayActivity", "绘制聚光灯边框");

        // Update ImageView
        screenshotImageView.setImageBitmap(tempBitmap);
        Log.d("ScreenshotDisplayActivity", "聚光灯绘制完成");

        // Recycle temporary bitmaps
        maskBitmap.recycle();
    }

    private void showLaser(float x, float y) {
        // Create a temporary bitmap to draw the laser effect on
        Bitmap tempBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas tempCanvas = new Canvas(tempBitmap);

        // Draw laser dot
        Paint laserPaint = new Paint();
        laserPaint.setAntiAlias(true);
        laserPaint.setColor(laserColor);
        laserPaint.setStyle(Paint.Style.FILL);

        Log.d("ScreenshotDisplayActivity", "绘制激光点 - 位置: (" + x + ", " + y + "), 颜色: " + String.format("0x%08X", laserColor) + ", 大小: " + laserSize);
        tempCanvas.drawCircle(x, y, laserSize, laserPaint);

        screenshotImageView.setImageBitmap(tempBitmap);
    }

    private void redrawBitmap() {
        if (workingBitmap != null && workingCanvas != null) {
            // Clear the canvas with the original screenshot content
            workingCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR); // Clear with transparency
            workingCanvas.drawBitmap(screenshotBitmap, 0, 0, null); // Draw original screenshot

            // Redraw all saved annotation paths (now segments)
            for (DrawPath drawPath : drawPaths) {
                for (LineSegment segment : drawPath.segments) {
                    workingCanvas.drawLine(segment.startX, segment.startY, segment.endX, segment.endY, drawPath.paint);
                }
            }

            // Force ImageView to redraw with the updated workingBitmap
            screenshotImageView.setImageBitmap(workingBitmap);
        }
    }

    private void setupBottomNavigation() {
        Log.d("ScreenshotDisplayActivity", "设置底部导航");
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            Log.d("ScreenshotDisplayActivity", "底部导航项被选中: " + itemId);
            
            Tool selectedTool = null;
            Fragment fragment = null;
            
            if (itemId == R.id.nav_annotation) {
                selectedTool = Tool.ANNOTATION;
                fragment = new AnnotationFragment();
                Log.d("ScreenshotDisplayActivity", "选择批注功能");
            } else if (itemId == R.id.nav_eraser) {
                selectedTool = Tool.ERASER;
                fragment = new EraserFragment();
                Log.d("ScreenshotDisplayActivity", "选择橡皮擦功能");
            } else if (itemId == R.id.nav_laser) {
                selectedTool = Tool.LASER;
                fragment = new LaserFragment();
                Log.d("ScreenshotDisplayActivity", "选择激光笔功能");
            } else if (itemId == R.id.nav_magnifier) {
                selectedTool = Tool.MAGNIFIER;
                fragment = new MagnifierFragment();
                Log.d("ScreenshotDisplayActivity", "选择放大镜功能");
            } else if (itemId == R.id.nav_spotlight) {
                selectedTool = Tool.SPOTLIGHT;
                fragment = new SpotlightFragment();
                Log.d("ScreenshotDisplayActivity", "选择聚光灯功能");
            }
            
            if (selectedTool != null) {
                if (isInitialSetup) {
                    // Initial selection, just set the tool, don't show fragment
                    currentTool = selectedTool;
                    isInitialSetup = false;
                    hideFragment(); // Ensure no fragment is shown initially
                    Log.d("ScreenshotDisplayActivity", "初始工具选择: " + currentTool.name() + ", 不显示配置Fragment");
                } else if (selectedTool == currentTool) {
                    // Same tool clicked again, toggle fragment visibility
                    if (isConfigFragmentVisible) {
                        Log.d("ScreenshotDisplayActivity", "再次点击相同工具，隐藏配置Fragment");
                        hideFragment();
                    } else {
                        Log.d("ScreenshotDisplayActivity", "再次点击相同工具，显示配置Fragment");
                        loadFragment(fragment);
                    }
                    isConfigFragmentVisible = !isConfigFragmentVisible;
                } else {
                    // Different tool selected
                    Log.d("ScreenshotDisplayActivity", "切换到不同工具: " + selectedTool.name() + ", 隐藏当前Fragment");
                    hideFragment();
                    currentTool = selectedTool;
                    isConfigFragmentVisible = false; // New tool, fragment is not visible yet
                    redrawBitmap(); // Clear any previous tool effects
                }
            }
            
            return true;
        });
        
        // 设置默认选中批注
        Log.d("ScreenshotDisplayActivity", "设置默认选中批注");
        bottomNavigationView.setSelectedItemId(R.id.nav_annotation);
    }

    private void loadFragment(Fragment fragment) {
        Log.d("ScreenshotDisplayActivity", "加载Fragment: " + fragment.getClass().getSimpleName());
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
        Log.d("ScreenshotDisplayActivity", "Fragment加载完成");
    }

    public void hideFragment() {
        Log.d("ScreenshotDisplayActivity", "隐藏配置Fragment");
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);
        if (currentFragment != null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.remove(currentFragment);
            transaction.commit();
            Log.d("ScreenshotDisplayActivity", "Fragment隐藏完成");
        }
    }

    // 更新工具配置的方法
    public void updateAnnotationConfig(int color, int thickness) {
        Log.d("ScreenshotDisplayActivity", "更新批注配置 - 原始颜色值: " + String.format("0x%08X", color) + ", 粗细: " + thickness);
        annotationColor = color;
        annotationThickness = thickness;
        Log.d("ScreenshotDisplayActivity", "批注配置已更新 - 颜色: " + String.format("0x%08X", annotationColor) + ", 粗细: " + annotationThickness);
        saveConfigToPreferences();
    }

    public void updateLaserConfig(int color, int size) {
        Log.d("ScreenshotDisplayActivity", "更新激光笔配置 - 原始颜色值: " + String.format("0x%08X", color) + ", 大小: " + size);
        laserColor = color;
        laserSize = size;
        Log.d("ScreenshotDisplayActivity", "激光笔配置已更新 - 颜色: " + String.format("0x%08X", laserColor) + ", 大小: " + laserSize);
        saveConfigToPreferences();
    }

    public void updateMagnifierConfig(int size, int zoom, int shape) {
        magnifierRadius = size * 20f; // 将配置值转换为实际半径
        magnifierScale = zoom;
        magnifierShape = shape;
        Log.d("ScreenshotDisplayActivity", "更新放大镜配置 - 大小: " + size + ", 缩放: " + zoom + ", 形状: " + shape);
        saveConfigToPreferences();
    }

    public void updateSpotlightConfig(int size, int darkness) {
        spotlightRadius = size * 50f; // 将配置值转换为实际半径
        spotlightDarkness = darkness;
        Log.d("ScreenshotDisplayActivity", "更新聚光灯配置 - 大小: " + size + ", 暗度: " + darkness);
        saveConfigToPreferences();
    }

    public void updateEraserConfig(int size) {
        eraserSize = size;
        Log.d("ScreenshotDisplayActivity", "更新橡皮擦配置 - 大小: " + size);
        saveConfigToPreferences();
    }

    public void clearAllDrawings() {
        Log.d("ScreenshotDisplayActivity", "清除所有绘制内容");
        drawPaths.clear();
        currentPath = null;
        redrawBitmap(); // Redraw to clear all annotations
    }

    @Override
    public void onBackPressed() {
        Log.d("ScreenshotDisplayActivity", "返回键被按下，关闭Activity");
        // 返回时关闭Activity
        finish();
    }

    // 绘制路径类
    private static class DrawPath {
        List<LineSegment> segments; // Changed from Path path;
        Paint paint;
    }

    // 线段类，用于更精细的擦除
    private static class LineSegment {
        float startX, startY, endX, endY;

        public LineSegment(float startX, float startY, float endX, float endY) {
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
        }

        // 检查线段是否与圆相交
        public boolean intersectsCircle(float circleX, float circleY, float circleRadius) {
            // 检查线段的两个端点是否在圆内
            if (distance(startX, startY, circleX, circleY) <= circleRadius ||
                distance(endX, endY, circleX, circleY) <= circleRadius) {
                return true;
            }

            // 检查圆心到线段的最短距离是否小于圆的半径
            float dx = endX - startX;
            float dy = endY - startY;
            float lenSq = dx * dx + dy * dy;
            float t = ((circleX - startX) * dx + (circleY - startY) * dy) / lenSq;

            if (t < 0) t = 0;
            if (t > 1) t = 1;

            float closestX = startX + t * dx;
            float closestY = startY + t * dy;

            return distance(closestX, closestY, circleX, circleY) <= circleRadius;
        }

        private float distance(float x1, float y1, float x2, float y2) {
            float dx = x2 - x1;
            float dy = y2 - y1;
            return (float) Math.sqrt(dx * dx + dy * dy);
        }
    }

    // 获取当前配置的方法
    public int getAnnotationColor() {
        return annotationColor;
    }

    public int getAnnotationThickness() {
        return annotationThickness;
    }

    public int getLaserColor() {
        return laserColor;
    }

    public int getLaserSize() {
        return laserSize;
    }

    public int getMagnifierSize() {
        return (int)(magnifierRadius / 20f);
    }

    public int getMagnifierZoom() {
        return (int)magnifierScale;
    }

    public int getMagnifierShape() {
        return magnifierShape;
    }

    public int getSpotlightSize() {
        return (int)(spotlightRadius / 50f);
    }

    public int getSpotlightDarkness() {
        return spotlightDarkness;
    }

    public int getEraserSize() {
        return eraserSize;
    }

    private void addTestButton() {
        // 创建一个测试按钮
        Button testButton = new Button(this);
        testButton.setText("测试聚光灯");
        testButton.setOnClickListener(v -> {
            Log.d("ScreenshotDisplayActivity", "测试聚光灯按钮被点击");
            // 在屏幕中心测试聚光灯
            float centerX = workingBitmap.getWidth() / 2f;
            float centerY = workingBitmap.getHeight() / 2f;
            showSpotlight(centerX, centerY);
        });
        
        // 将按钮添加到布局中
        ViewGroup rootView = findViewById(android.R.id.content);
        rootView.addView(testButton);
        
        // 设置按钮位置 - 使用正确的LayoutParams类型
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = 100;
        params.leftMargin = 50;
        testButton.setLayoutParams(params);
        
        Log.d("ScreenshotDisplayActivity", "测试按钮已添加");
    }
} 