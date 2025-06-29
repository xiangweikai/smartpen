package com.zuomu.smartpen.connection;

import static android.app.PendingIntent.FLAG_IMMUTABLE;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.util.HashMap;

public class SmartPenConnection {
    private static SmartPenConnection instance;
    private static final String TAG = "SmartPenConnection";
    private static final String ACTION_USB_PERMISSION = "com.zuomu.smartpen.USB_PERMISSION";
    
    // USB设备标识
    private static final int VENDOR_ID = 0x363C;
    private static final int PRODUCT_ID = 0x0001;
    
    // 数据包固定长度
    private static final int PACKET_LENGTH = 8;
    
    private static Context appContext;
    private final UsbManager usbManager;
    private UsbDevice mDevice;
    private UsbInterface mInterface;
    private UsbEndpoint mEndpoint;
    private UsbDeviceConnection mConnection;
    
    private final Handler mHandler;
    private final HandlerThread mHandlerThread;
    private boolean isConnected = false;
    private OnPenConnectionListener listener;
    
    // 数据包模式定义
    public static class PenMode {
        // 电子激光模式
        public static final byte[] LASER_MODE_HEADER = {(byte)0xAB, (byte)0xCD};
        // 放大镜模式
        public static final byte[] MAGNIFIER_MODE_HEADER = {(byte)0xA1, (byte)0xB1};
        // 聚光灯模式
        public static final byte[] SPOTLIGHT_MODE_HEADER = {(byte)0xA2, (byte)0xB2};
        // 正常模式
        public static final byte[] NORMAL_MODE_HEADER = {(byte)0xA0, (byte)0xB0};
        // 批注模式
        public static final byte[] ANNOTATION_MODE_HEADER = {(byte)0xA1, (byte)0xC1};
        // 橡皮擦模式
        public static final byte[] ERASER_MODE_HEADER = {(byte)0xA2, (byte)0xC2};
        // 语音模式
        public static final byte[] VOICE_MODE_HEADER = {(byte)0xC1, (byte)0xC2};
    }

    public interface OnPenConnectionListener {
        void onConnected();
        void onFailed(String message);
        void onDisconnected();
        void onDataReceived(byte[] data);
    }

    public static synchronized SmartPenConnection getInstance() {
        if (instance == null) {
            if (appContext == null) {
                try {
                    Class<?> activityThread = Class.forName("android.app.ActivityThread");
                    Object currentActivityThread = activityThread.getMethod("currentActivityThread").invoke(null);
                    appContext = (Context) activityThread.getMethod("getApplication").invoke(currentActivityThread);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to get application context", e);
                    throw new RuntimeException("Failed to get application context", e);
                }
            }
            instance = new SmartPenConnection();
        }
        return instance;
    }

    private SmartPenConnection() {
        this.usbManager = (UsbManager) appContext.getSystemService(Context.USB_SERVICE);
        
        // 初始化Handler线程
        mHandlerThread = new HandlerThread("UsbThread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
        
        // 注册USB权限广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        appContext.registerReceiver(usbReceiver, filter, Context.RECEIVER_EXPORTED);
    }

    // 初始化方法，用于在 Application 中调用
    public static void init(Context context) {
        if (appContext == null) {
            appContext = context.getApplicationContext();
        }
    }

    // USB权限广播接收器
    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            setupConnection(device);
                        }
                    } else {
                        if (listener != null) {
                            listener.onFailed("Permission denied");
                        }
                    }
                }
            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                findDevice();
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                disconnect();
            }
        }
    };

    // 查找设备
    public void findDevice() {
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        for (UsbDevice device : deviceList.values()) {
            if (device.getVendorId() == VENDOR_ID && device.getProductId() == PRODUCT_ID) {
                requestPermission(device);
                break;
            }
        }
    }

    // 请求USB权限
    private void requestPermission(UsbDevice device) {
        if (!usbManager.hasPermission(device)) {
            PendingIntent permissionIntent;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                permissionIntent = PendingIntent.getBroadcast(appContext, 0, 
                    new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE);
            } else {
                permissionIntent = PendingIntent.getBroadcast(appContext, 0, 
                    new Intent(ACTION_USB_PERMISSION), FLAG_IMMUTABLE);
            }
            usbManager.requestPermission(device, permissionIntent);
        } else {
            setupConnection(device);
        }
    }

    // 建立连接
    private void setupConnection(UsbDevice device) {
        mDevice = device;
        if (mDevice == null) {
            if (listener != null) {
                listener.onFailed("Device not found");
            }
            return;
        }

        // 获取接口
        mInterface = mDevice.getInterface(0);
        // 获取端点
        for (int i = 0; i < mInterface.getEndpointCount(); i++) {
            UsbEndpoint endpoint = mInterface.getEndpoint(i);
            if (endpoint.getDirection() == UsbConstants.USB_DIR_IN && 
                endpoint.getEndpointNumber() == 2) {
                mEndpoint = endpoint;
                break;
            }
        }

        // 打开连接
        mConnection = usbManager.openDevice(mDevice);
        if (mConnection != null && mConnection.claimInterface(mInterface, true)) {
            isConnected = true;
            if (listener != null) {
                listener.onConnected();
            }
            startReading();
        } else {
            if (listener != null) {
                listener.onFailed("Could not open device connection");
            }
        }
    }

    // 开始读取数据
    private void startReading() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                while (isConnected) {
                    byte[] buffer = new byte[PACKET_LENGTH];
                    int bytesRead = mConnection.bulkTransfer(mEndpoint, buffer, 
                        buffer.length, 1000);
                    if (bytesRead > 0 && listener != null) {
                        listener.onDataReceived(buffer);
                    }
                }
            }
        });
    }

    // 发送指令方法
    public boolean sendCommand(byte[] command) {
        if (!isConnected || command.length != PACKET_LENGTH) {
            return false;
        }
        
        int result = mConnection.bulkTransfer(mEndpoint, command, command.length, 1000);
        return result >= 0;
    }

    // 设置激光模式
    public boolean setLaserMode(int aperture, boolean isRed) {
        byte[] command = new byte[PACKET_LENGTH];
        command[0] = PenMode.LASER_MODE_HEADER[0];
        command[1] = PenMode.LASER_MODE_HEADER[1];
        command[2] = 0x00;
        command[3] = (byte)(isRed ? aperture : (aperture | 0x10));
        return sendCommand(command);
    }

    // 设置放大镜模式
    public boolean setMagnifierMode(int aperture) {
        byte[] command = new byte[PACKET_LENGTH];
        command[0] = PenMode.MAGNIFIER_MODE_HEADER[0];
        command[1] = PenMode.MAGNIFIER_MODE_HEADER[1];
        command[2] = 0x00;
        command[3] = (byte)aperture;
        return sendCommand(command);
    }

    // 设置注释模式
    public boolean setAnnotationMode(boolean enable, int thickness, int r, int g, int b) {
        byte[] command = new byte[PACKET_LENGTH];
        command[0] = PenMode.ANNOTATION_MODE_HEADER[0];
        command[1] = PenMode.ANNOTATION_MODE_HEADER[1];
        command[2] = (byte)(enable ? 0x01 : 0x00);
        command[3] = 0x00;
        command[4] = (byte)thickness;
        command[5] = (byte)r;
        command[6] = (byte)g;
        command[7] = (byte)b;
        return sendCommand(command);
    }

    // 设置橡皮擦模式
    public boolean setEraserMode(boolean enable, boolean isPixelEraser, int size) {
        byte[] command = new byte[PACKET_LENGTH];
        command[0] = PenMode.ERASER_MODE_HEADER[0];
        command[1] = PenMode.ERASER_MODE_HEADER[1];
        command[2] = (byte)(enable ? 0x01 : 0x00);
        command[3] = (byte)(isPixelEraser ? 0x01 : 0x02);
        command[4] = (byte)size;
        return sendCommand(command);
    }

    // 设置语音模式
    public boolean setVoiceMode(boolean enable) {
        byte[] command = new byte[PACKET_LENGTH];
        command[0] = PenMode.VOICE_MODE_HEADER[0];
        command[1] = PenMode.VOICE_MODE_HEADER[1];
        command[2] = (byte)(enable ? 0x01 : 0x00);
        return sendCommand(command);
    }

    // 断开连接
    public void disconnect() {
        isConnected = false;
        if (mConnection != null) {
            mConnection.releaseInterface(mInterface);
            mConnection.close();
        }
        if (listener != null) {
            listener.onDisconnected();
        }
    }

    // 设置监听器
    public void setOnPenConnectionListener(OnPenConnectionListener listener) {
        this.listener = listener;
    }

    // 释放资源
    public void release() {
        disconnect();
        try {
            appContext.unregisterReceiver(usbReceiver);
        } catch (Exception e) {
            Log.e(TAG, "Error unregistering receiver", e);
        }
        mHandlerThread.quit();
    }

    // 检查是否已连接
    public boolean isConnected() {
        return isConnected;
    }
}
