package com.zuomu.smartpen;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.app.Activity;

import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

public class ScreenshotService extends Service {

    private static final String CHANNEL_ID = "screenshot_service_channel";
    private static final int NOTIFICATION_ID = 1001;
    
    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;
    private ImageReader imageReader;
    
    private int screenWidth;
    private int screenHeight;
    private int screenDensity;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("ScreenshotService", "ScreenshotService onCreate被调用");
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("ScreenshotService", "ScreenshotService onStartCommand被调用 - startId: " + startId);
        
        // 立即启动前台服务，避免超时异常
        Log.d("ScreenshotService", "立即启动前台服务...");
        startForeground(NOTIFICATION_ID, createNotification());
        Log.d("ScreenshotService", "前台服务启动成功");
        
        if (intent != null) {
            int resultCode = intent.getIntExtra("resultCode", -1);
            Intent data = intent.getParcelableExtra("data");
            
            Log.d("ScreenshotService", "从Intent获取数据 - resultCode: " + resultCode + ", data不为空: " + (data != null));
            Log.d("ScreenshotService", "Activity.RESULT_OK: " + Activity.RESULT_OK + ", Activity.RESULT_CANCELED: " + Activity.RESULT_CANCELED);
            
            if (resultCode == Activity.RESULT_OK && data != null) {
                // 在后台线程中处理截图，避免阻塞主线程
                Log.d("ScreenshotService", "开始准备截图，在主线程中启动截图流程");
                new Handler(Looper.getMainLooper()).post(() -> {
                    startScreenCapture(resultCode, data);
                });
            } else {
                // 如果没有有效数据，立即停止服务
                Log.w("ScreenshotService", "无效的Intent数据，停止服务 - resultCode: " + resultCode + ", data: " + data);
                stopSelf();
            }
        } else {
            // 如果没有intent，立即停止服务
            Log.w("ScreenshotService", "Intent为空，停止服务");
            stopSelf();
        }
        
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        Log.d("ScreenshotService", "创建通知渠道");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "截图服务",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("用于支持屏幕截图功能");
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
                Log.d("ScreenshotService", "通知渠道创建成功");
            } else {
                Log.e("ScreenshotService", "无法获取NotificationManager");
            }
        }
    }

    private Notification createNotification() {
        Log.d("ScreenshotService", "创建通知");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("截图服务")
            .setContentText("正在运行截图功能")
            .setSmallIcon(R.drawable.ic_smart_pen)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true);

        return builder.build();
    }

    private void startScreenCapture(int resultCode, Intent data) {
        Log.d("ScreenshotService", "开始屏幕截图流程");
        try {
            // 获取屏幕尺寸
            Log.d("ScreenshotService", "获取屏幕尺寸");
            WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics metrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(metrics);
            
            screenWidth = metrics.widthPixels;
            screenHeight = metrics.heightPixels;
            screenDensity = metrics.densityDpi;
            
            Log.d("ScreenshotService", "屏幕尺寸: " + screenWidth + "x" + screenHeight + ", 密度: " + screenDensity);
            
            // 创建ImageReader
            Log.d("ScreenshotService", "创建ImageReader");
            imageReader = ImageReader.newInstance(screenWidth, screenHeight, PixelFormat.RGBA_8888, 1);
            
            // 创建MediaProjection
            Log.d("ScreenshotService", "创建MediaProjection");
            MediaProjectionManager mediaProjectionManager = 
                (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);
            
            if (mediaProjection == null) {
                Log.e("ScreenshotService", "MediaProjection创建失败");
                if (ScreenshotUtils.pendingCallback != null) {
                    ScreenshotUtils.pendingCallback.onScreenshotError("MediaProjection创建失败");
                }
                stopSelf();
                return;
            }
            
            // 注册MediaProjection回调
            Log.d("ScreenshotService", "注册MediaProjection回调");
            mediaProjection.registerCallback(new MediaProjection.Callback() {
                @Override
                public void onStop() {
                    Log.d("ScreenshotService", "MediaProjection停止");
                    cleanup();
                }
            }, new Handler(Looper.getMainLooper()));
            
            // 创建VirtualDisplay
            Log.d("ScreenshotService", "创建VirtualDisplay");
            virtualDisplay = mediaProjection.createVirtualDisplay(
                "Screenshot",
                screenWidth, screenHeight, screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader.getSurface(), null, null);
            
            if (virtualDisplay == null) {
                Log.e("ScreenshotService", "VirtualDisplay创建失败");
                if (ScreenshotUtils.pendingCallback != null) {
                    ScreenshotUtils.pendingCallback.onScreenshotError("VirtualDisplay创建失败");
                }
                stopSelf();
                return;
            }
            
            Log.d("ScreenshotService", "设置ImageReader监听器");
            // 监听图像
            imageReader.setOnImageAvailableListener(reader -> {
                Log.d("ScreenshotService", "ImageReader有可用图像");
                Image image = null;
                Bitmap bitmap = null;
                
                try {
                    image = reader.acquireLatestImage();
                    if (image != null) {
                        Log.d("ScreenshotService", "获取到图像，开始处理");
                        Image.Plane[] planes = image.getPlanes();
                        ByteBuffer buffer = planes[0].getBuffer();
                        int pixelStride = planes[0].getPixelStride();
                        int rowStride = planes[0].getRowStride();
                        int rowPadding = rowStride - pixelStride * screenWidth;
                        
                        Log.d("ScreenshotService", "图像参数 - pixelStride: " + pixelStride + ", rowStride: " + rowStride + ", rowPadding: " + rowPadding);
                        
                        bitmap = Bitmap.createBitmap(screenWidth + rowPadding / pixelStride, screenHeight, Bitmap.Config.ARGB_8888);
                        bitmap.copyPixelsFromBuffer(buffer);
                        
                        // 裁剪到正确的尺寸
                        if (bitmap.getWidth() > screenWidth) {
                            Log.d("ScreenshotService", "裁剪bitmap到正确尺寸");
                            bitmap = Bitmap.createBitmap(bitmap, 0, 0, screenWidth, screenHeight);
                        }
                        
                        Log.d("ScreenshotService", "截图处理完成，bitmap大小: " + bitmap.getWidth() + "x" + bitmap.getHeight());
                        
                        // 压缩Bitmap以避免Parcel传输问题
                        Log.d("ScreenshotService", "开始压缩Bitmap");
                        Bitmap compressedBitmap = compressBitmap(bitmap);
                        Log.d("ScreenshotService", "压缩后bitmap大小: " + compressedBitmap.getWidth() + "x" + compressedBitmap.getHeight());
                        
                        // 保存Bitmap到文件
                        String filePath = saveBitmapToFile(compressedBitmap);
                        if (filePath != null) {
                            Log.d("ScreenshotService", "Bitmap已保存到文件: " + filePath);
                            
                            // 在主线程回调，传递文件路径
                            new Handler(Looper.getMainLooper()).post(() -> {
                                Log.d("ScreenshotService", "在主线程中回调截图结果");
                                // 使用ScreenshotUtils中的静态回调
                                if (ScreenshotUtils.pendingCallback != null) {
                                    ScreenshotUtils.pendingCallback.onScreenshotTaken(filePath);
                                    Log.d("ScreenshotService", "截图回调成功");
                                } else {
                                    Log.w("ScreenshotService", "pendingCallback为空");
                                }
                            });
                        } else {
                            Log.e("ScreenshotService", "保存Bitmap到文件失败");
                            if (ScreenshotUtils.pendingCallback != null) {
                                ScreenshotUtils.pendingCallback.onScreenshotError("保存截图文件失败");
                            }
                        }
                        
                        // 截图完成后停止服务
                        Log.d("ScreenshotService", "截图完成，准备停止服务");
                        stopSelf();
                    } else {
                        Log.w("ScreenshotService", "获取到的图像为空");
                    }
                } catch (Exception e) {
                    Log.e("ScreenshotService", "截图处理失败: " + e.getMessage(), e);
                    if (ScreenshotUtils.pendingCallback != null) {
                        ScreenshotUtils.pendingCallback.onScreenshotError("截图处理失败: " + e.getMessage());
                    }
                    stopSelf();
                } finally {
                    if (image != null) {
                        image.close();
                        Log.d("ScreenshotService", "图像资源已释放");
                    }
                    cleanup();
                }
            }, null);
            
            Log.d("ScreenshotService", "截图流程初始化完成");
            
        } catch (Exception e) {
            Log.e("ScreenshotService", "截图初始化失败: " + e.getMessage(), e);
            if (ScreenshotUtils.pendingCallback != null) {
                ScreenshotUtils.pendingCallback.onScreenshotError("截图初始化失败: " + e.getMessage());
            }
            stopSelf();
        }
    }

    private String saveBitmapToFile(Bitmap bitmap) {
        try {
            // 创建临时文件
            File cacheDir = getCacheDir();
            File screenshotFile = new File(cacheDir, "screenshot_" + System.currentTimeMillis() + ".jpg");
            
            // 将Bitmap保存为JPEG文件
            FileOutputStream out = new FileOutputStream(screenshotFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            
            Log.d("ScreenshotService", "Bitmap已保存到: " + screenshotFile.getAbsolutePath());
            return screenshotFile.getAbsolutePath();
        } catch (Exception e) {
            Log.e("ScreenshotService", "保存Bitmap失败: " + e.getMessage(), e);
            return null;
        }
    }

    private Bitmap compressBitmap(Bitmap originalBitmap) {
        try {
            // 计算压缩比例，确保bitmap不会太大
            int maxSize = 1024; // 最大尺寸
            int width = originalBitmap.getWidth();
            int height = originalBitmap.getHeight();
            
            float scale = 1.0f;
            if (width > maxSize || height > maxSize) {
                scale = Math.min((float) maxSize / width, (float) maxSize / height);
            }
            
            int newWidth = Math.round(width * scale);
            int newHeight = Math.round(height * scale);
            
            Log.d("ScreenshotService", "压缩比例: " + scale + ", 新尺寸: " + newWidth + "x" + newHeight);
            
            // 创建压缩后的bitmap
            Bitmap compressedBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);
            
            // 如果创建了新的bitmap，释放原始bitmap
            if (compressedBitmap != originalBitmap) {
                originalBitmap.recycle();
            }
            
            return compressedBitmap;
        } catch (Exception e) {
            Log.e("ScreenshotService", "压缩Bitmap失败: " + e.getMessage(), e);
            return originalBitmap;
        }
    }

    private void cleanup() {
        Log.d("ScreenshotService", "开始清理资源");
        if (virtualDisplay != null) {
            virtualDisplay.release();
            virtualDisplay = null;
            Log.d("ScreenshotService", "VirtualDisplay已释放");
        }
        if (mediaProjection != null) {
            mediaProjection.stop();
            mediaProjection = null;
            Log.d("ScreenshotService", "MediaProjection已停止");
        }
        if (imageReader != null) {
            imageReader.close();
            imageReader = null;
            Log.d("ScreenshotService", "ImageReader已关闭");
        }
        Log.d("ScreenshotService", "资源清理完成");
    }

    @Override
    public void onDestroy() {
        Log.d("ScreenshotService", "ScreenshotService onDestroy被调用");
        cleanup();
        super.onDestroy();
    }
} 