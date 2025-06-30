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
            Log.d(TAG, "getInstance: Creating new SmartPenConnection instance.");
            if (appContext == null) {
                Log.e(TAG, "getInstance: Application context is null when trying to get instance.");
                try {
                    Class<?> activityThread = Class.forName("android.app.ActivityThread");
                    Object currentActivityThread = activityThread.getMethod("currentActivityThread").invoke(null);
                    appContext = (Context) activityThread.getMethod("getApplication").invoke(currentActivityThread);
                    Log.d(TAG, "getInstance: Successfully obtained application context via reflection.");
                } catch (Exception e) {
                    Log.e(TAG, "Failed to get application context", e);
                    throw new RuntimeException("Failed to get application context", e);
                }
            }
            instance = new SmartPenConnection();
        } else {
            Log.d(TAG, "getInstance: Returning existing SmartPenConnection instance.");
        }
        return instance;
    }

    private SmartPenConnection() {
        Log.d(TAG, "SmartPenConnection: Constructor called.");
        this.usbManager = (UsbManager) appContext.getSystemService(Context.USB_SERVICE);
        Log.d(TAG, "SmartPenConnection: UsbManager obtained.");
        
        // 初始化Handler线程
        mHandlerThread = new HandlerThread("UsbThread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
        Log.d(TAG, "SmartPenConnection: HandlerThread started.");
        
        // 注册USB权限广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        appContext.registerReceiver(usbReceiver, filter, Context.RECEIVER_EXPORTED);
        Log.d(TAG, "SmartPenConnection: USB permission broadcast receiver registered.");
    }

    // 初始化方法，用于在 Application 中调用
    public static void init(Context context) {
        if (appContext == null) {
            appContext = context.getApplicationContext();
            Log.d(TAG, "init: Application context set.");
        } else {
            Log.d(TAG, "init: Application context already set.");
        }
    }

    // USB权限广播接收器
    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "usbReceiver.onReceive: Action received: " + action);
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            Log.d(TAG, "usbReceiver: USB permission granted for device: " + device.getDeviceName());
                            setupConnection(device);
                        } else {
                            Log.w(TAG, "usbReceiver: USB permission granted but device is null.");
                        }
                    } else {
                        Log.w(TAG, "usbReceiver: USB permission denied for device: " + (device != null ? device.getDeviceName() : "null"));
                        if (listener != null) {
                            listener.onFailed("Permission denied");
                        }
                    }
                }
            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                Log.d(TAG, "usbReceiver: USB device attached.");
                findDevice();
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                Log.d(TAG, "usbReceiver: USB device detached: " + (device != null ? device.getDeviceName() : "null"));
                disconnect();
            }
        }
    };

    // 查找设备
    public void findDevice() {
        Log.d(TAG, "findDevice: Searching for SmartPen device (VID: " + String.format("0x%04X", VENDOR_ID) + ", PID: " + String.format("0x%04X", PRODUCT_ID) + ").");
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        if (deviceList.isEmpty()) {
            Log.d(TAG, "findDevice: No USB devices found.");
        }
        boolean deviceFound = false;
        for (UsbDevice device : deviceList.values()) {
            Log.d(TAG, "findDevice: Found USB device: " + device.getDeviceName() + " (VID: " + String.format("0x%04X", device.getVendorId()) + ", PID: " + String.format("0x%04X", device.getProductId()) + ").");
//            if (device.getVendorId() == VENDOR_ID && device.getProductId() == PRODUCT_ID) {
                Log.d(TAG, "findDevice: SmartPen device found! Requesting permission.");
                requestPermission(device);
                deviceFound = true;
                break;
//            }
        }
        if (!deviceFound) {
            Log.d(TAG, "findDevice: SmartPen device not found.");
            if (listener != null) {
                listener.onFailed("SmartPen device not found.");
            }
        }
    }

    // 请求USB权限
    private void requestPermission(UsbDevice device) {
        Log.d(TAG, "requestPermission: Checking USB permission for device: " + device.getDeviceName());
        if (!usbManager.hasPermission(device)) {
            Log.d(TAG, "requestPermission: Permission not granted, requesting now.");
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
            Log.d(TAG, "requestPermission: Permission already granted. Setting up connection.");
            setupConnection(device);
        }
    }

    // 建立连接
    private void setupConnection(UsbDevice device) {
        Log.d(TAG, "setupConnection: Attempting to set up connection for device: " + device.getDeviceName());
        mDevice = device;
        if (mDevice == null) {
            Log.e(TAG, "setupConnection: Device is null.");
            if (listener != null) {
                listener.onFailed("Device not found");
            }
            return;
        }

        // 获取接口
        mInterface = mDevice.getInterface(0);
        Log.d(TAG, "setupConnection: Obtained UsbInterface: " + (mInterface != null ? mInterface.getId() : "null"));
        
        // 获取端点
        mEndpoint = null;
        for (int i = 0; i < mInterface.getEndpointCount(); i++) {
            UsbEndpoint endpoint = mInterface.getEndpoint(i);
            Log.d(TAG, "setupConnection: Checking endpoint: " + endpoint.getAddress() + ", Direction: " + endpoint.getDirection() + ", Number: " + endpoint.getEndpointNumber());
            if (endpoint.getDirection() == UsbConstants.USB_DIR_IN &&
                endpoint.getEndpointNumber() == 2) {
                mEndpoint = endpoint;
                Log.d(TAG, "setupConnection: Found IN endpoint 2: " + mEndpoint.getAddress());
                break;
            }
        }

//        if (mEndpoint == null) {
//            Log.e(TAG, "setupConnection: IN endpoint 2 not found.");
//            if (listener != null) {
//                listener.onFailed("IN endpoint 2 not found.");
//            }
//            return;
//        }

        // 打开连接
        mConnection = usbManager.openDevice(mDevice);
        if (mConnection != null) {
            Log.d(TAG, "setupConnection: UsbDeviceConnection opened.");
            if (mConnection.claimInterface(mInterface, true)) {
                isConnected = true;
                Log.d(TAG, "setupConnection: Interface claimed successfully. Connection established.");
                if (listener != null) {
                    listener.onConnected();
                }
                startReading();
            } else {
                Log.e(TAG, "setupConnection: Failed to claim interface.");
                mConnection.close();
                if (listener != null) {
                    listener.onFailed("Failed to claim interface");
                }
            }
        } else {
            Log.e(TAG, "setupConnection: Failed to open device connection.");
            if (listener != null) {
                listener.onFailed("Could not open device connection");
            }
        }
    }

    // 开始读取数据
    private void startReading() {
        Log.d(TAG, "startReading: Starting data reading thread.");
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Reading thread: Running. isConnected: " + isConnected);
                while (isConnected) {
                    byte[] buffer = new byte[PACKET_LENGTH];
                    int bytesRead = mConnection.bulkTransfer(mEndpoint, buffer, 
                        buffer.length, 1000); // 1000ms timeout
                    if (bytesRead > 0) {
                        Log.d(TAG, "Reading thread: Received " + bytesRead + " bytes: " + bytesToHex(buffer));
                        if (listener != null) {
                            listener.onDataReceived(buffer);
                        }
                    } else if (bytesRead == 0) {
                        // Timeout, no data received within 1000ms
                        Log.d(TAG, "Reading thread: No data received (timeout).");
                    } else {
                        // Error occurred
                        Log.e(TAG, "Reading thread: bulkTransfer error: " + bytesRead);
                        // Consider breaking loop or handling specific errors
                    }
                }
                Log.d(TAG, "Reading thread: Exited loop. isConnected: " + isConnected);
            }
        });
    }

    // 发送指令方法
    public boolean sendCommand(byte[] command) {
        Log.d(TAG, "sendCommand: Attempting to send command: " + bytesToHex(command));
        if (!isConnected) {
            Log.w(TAG, "sendCommand: Not connected. Command not sent.");
            return false;
        }
        if (command.length != PACKET_LENGTH) {
            Log.e(TAG, "sendCommand: Invalid command length. Expected " + PACKET_LENGTH + ", got " + command.length);
            return false;
        }
        
        int result = mConnection.bulkTransfer(mEndpoint, command, command.length, 1000);
        if (result >= 0) {
            Log.d(TAG, "sendCommand: Command sent successfully. Bytes written: " + result);
        } else {
            Log.e(TAG, "sendCommand: Failed to send command. Result: " + result);
        }
        return result >= 0;
    }

    // 设置激光模式
    public boolean setLaserMode(int aperture, boolean isRed) {
        Log.d(TAG, "setLaserMode: aperture=" + aperture + ", isRed=" + isRed);
        byte[] command = new byte[PACKET_LENGTH];
        command[0] = PenMode.LASER_MODE_HEADER[0];
        command[1] = PenMode.LASER_MODE_HEADER[1];
        command[2] = 0x00;
        command[3] = (byte)(isRed ? aperture : (aperture | 0x10));
        return sendCommand(command);
    }

    // 设置放大镜模式
    public boolean setMagnifierMode(int aperture) {
        Log.d(TAG, "setMagnifierMode: aperture=" + aperture);
        byte[] command = new byte[PACKET_LENGTH];
        command[0] = PenMode.MAGNIFIER_MODE_HEADER[0];
        command[1] = PenMode.MAGNIFIER_MODE_HEADER[1];
        command[2] = 0x00;
        command[3] = (byte)aperture;
        return sendCommand(command);
    }

    // 设置注释模式
    public boolean setAnnotationMode(boolean enable, int thickness, int r, int g, int b) {
        Log.d(TAG, "setAnnotationMode: enable=" + enable + ", thickness=" + thickness + ", r=" + r + ", g=" + g + ", b=" + b);
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
        Log.d(TAG, "setEraserMode: enable=" + enable + ", isPixelEraser=" + isPixelEraser + ", size=" + size);
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
        Log.d(TAG, "setVoiceMode: enable=" + enable);
        byte[] command = new byte[PACKET_LENGTH];
        command[0] = PenMode.VOICE_MODE_HEADER[0];
        command[1] = PenMode.VOICE_MODE_HEADER[1];
        command[2] = (byte)(enable ? 0x01 : 0x00);
        return sendCommand(command);
    }

    // 断开连接
    public void disconnect() {
        Log.d(TAG, "disconnect: Disconnecting from device.");
        isConnected = false;
        if (mConnection != null) {
            mConnection.releaseInterface(mInterface);
            Log.d(TAG, "disconnect: Interface released.");
            mConnection.close();
            Log.d(TAG, "disconnect: Connection closed.");
        } else {
            Log.d(TAG, "disconnect: No active connection to disconnect.");
        }
        if (listener != null) {
            listener.onDisconnected();
        }
    }

    // 设置监听器
    public void setOnPenConnectionListener(OnPenConnectionListener listener) {
        this.listener = listener;
        Log.d(TAG, "setOnPenConnectionListener: Listener set.");
    }

    // 释放资源
    public void release() {
        Log.d(TAG, "release: Releasing resources.");
        disconnect();
        try {
            appContext.unregisterReceiver(usbReceiver);
            Log.d(TAG, "release: USB receiver unregistered.");
        } catch (Exception e) {
            Log.e(TAG, "Error unregistering receiver", e);
        }
        mHandlerThread.quit();
        Log.d(TAG, "release: HandlerThread quit.");
    }

    // 检查是否已连接
    public boolean isConnected() {
        Log.d(TAG, "isConnected: " + isConnected);
        return isConnected;
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString();
    }
}
