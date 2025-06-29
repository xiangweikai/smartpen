package com.zuomu.smartpen;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.projection.MediaProjectionManager;
import android.util.Log;

public class ScreenshotUtils {
    
    public interface ScreenshotCallback {
        void onScreenshotTaken(String filePath);
        void onScreenshotError(String error);
    }
    
    private static final int REQUEST_MEDIA_PROJECTION = 1001;
    public static ScreenshotCallback pendingCallback;
    private static MediaProjectionManager mediaProjectionManager;
    
    public static void takeScreenshot(Context context, ScreenshotCallback callback) {
        Log.d("ScreenshotUtils", "takeScreenshot被调用");
        
        if (mediaProjectionManager == null) {
            Log.d("ScreenshotUtils", "初始化MediaProjectionManager");
            mediaProjectionManager = (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        }
        
        pendingCallback = callback;
        Log.d("ScreenshotUtils", "设置pendingCallback");
        
        if (context instanceof Activity) {
            Log.d("ScreenshotUtils", "Context是Activity类型，创建截图权限请求Intent");
            Intent intent = mediaProjectionManager.createScreenCaptureIntent();
            Log.d("ScreenshotUtils", "启动权限请求Activity");
            ((Activity) context).startActivityForResult(intent, REQUEST_MEDIA_PROJECTION);
        } else {
            Log.e("ScreenshotUtils", "Context不是Activity类型，无法请求权限");
            if (callback != null) {
                callback.onScreenshotError("Context必须是Activity类型");
            }
        }
    }
    
    public static void handleActivityResult(int requestCode, int resultCode, Intent data, Context context) {
        Log.d("ScreenshotUtils", "handleActivityResult被调用 - requestCode: " + requestCode + ", resultCode: " + resultCode);
        Log.d("ScreenshotUtils", "Activity.RESULT_OK: " + Activity.RESULT_OK + ", Activity.RESULT_CANCELED: " + Activity.RESULT_CANCELED);
        
        if (requestCode == REQUEST_MEDIA_PROJECTION && resultCode == Activity.RESULT_OK) {
            // 用户授权了截图权限，启动前台服务进行截图
            Log.d("ScreenshotUtils", "用户授权截图权限，准备启动前台服务");
            Log.d("ScreenshotUtils", "data不为空: " + (data != null));
            startScreenshotService(context, resultCode, data);
        } else if (requestCode == REQUEST_MEDIA_PROJECTION && resultCode == Activity.RESULT_CANCELED) {
            Log.d("ScreenshotUtils", "用户取消了截图权限");
            if (pendingCallback != null) {
                pendingCallback.onScreenshotError("用户取消了截图权限");
            }
        } else {
            Log.w("ScreenshotUtils", "未知的ActivityResult - requestCode: " + requestCode + ", resultCode: " + resultCode);
        }
    }
    
    private static void startScreenshotService(Context context, int resultCode, Intent data) {
        try {
            Log.d("ScreenshotUtils", "开始创建服务Intent");
            Intent serviceIntent = new Intent(context, ScreenshotService.class);
            serviceIntent.putExtra("resultCode", resultCode);
            serviceIntent.putExtra("data", data);
            
            Log.d("ScreenshotUtils", "启动前台服务，resultCode: " + resultCode + ", Android版本: " + android.os.Build.VERSION.SDK_INT);
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                Log.d("ScreenshotUtils", "使用startForegroundService启动服务");
                context.startForegroundService(serviceIntent);
            } else {
                Log.d("ScreenshotUtils", "使用startService启动服务");
                context.startService(serviceIntent);
            }
            Log.d("ScreenshotUtils", "服务启动命令已发送");
        } catch (Exception e) {
            Log.e("ScreenshotUtils", "启动前台服务失败: " + e.getMessage(), e);
            if (pendingCallback != null) {
                pendingCallback.onScreenshotError("启动截图服务失败: " + e.getMessage());
            }
        }
    }
} 