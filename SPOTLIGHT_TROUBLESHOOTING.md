# 聚光灯问题排查指南

## 最新修改记录 (2024-01-XX)

### 强制使用ARGB_8888配置
- 修改`setupWorkingBitmap()`方法，强制使用`Bitmap.Config.ARGB_8888`
- 添加Bitmap配置日志输出
- 确保所有透明度操作都能正常工作

### 使用CLEAR模式
- 在`showSpotlight()`方法中使用`PorterDuff.Mode.CLEAR`
- 添加详细的绘制过程日志
- 优化绘制顺序：原图 → 批注 → 遮罩 → 清除区域 → 边框

### 添加测试按钮
- 在Activity中添加"测试聚光灯"按钮
- 点击按钮在屏幕中心显示聚光灯效果
- 方便调试和验证功能

### 当前实现代码
```java
private void showSpotlight(float x, float y) {
    // 1. 重新设置工作Bitmap（强制ARGB_8888）
    setupWorkingBitmap();
    
    // 2. 绘制所有批注路径
    for (DrawPath drawPath : drawPaths) {
        workingCanvas.drawPath(drawPath.path, drawPath.paint);
    }
    
    // 3. 绘制半透明黑色遮罩
    Paint spotlightPaint = new Paint();
    spotlightPaint.setAntiAlias(true);
    int alpha = (int)(255 * spotlightDarkness / 100f);
    spotlightPaint.setColor(Color.argb(alpha, 0, 0, 0));
    workingCanvas.drawRect(0, 0, workingBitmap.getWidth(), workingBitmap.getHeight(), spotlightPaint);
    
    // 4. 使用CLEAR模式清除聚光灯区域
    spotlightPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    workingCanvas.drawCircle(x, y, spotlightRadius, spotlightPaint);
    
    // 5. 绘制白色边框
    spotlightPaint.setXfermode(null);
    spotlightPaint.setStyle(Paint.Style.STROKE);
    spotlightPaint.setColor(Color.WHITE);
    spotlightPaint.setStrokeWidth(2);
    workingCanvas.drawCircle(x, y, spotlightRadius, spotlightPaint);
    
    // 6. 更新ImageView
    screenshotImageView.setImageBitmap(workingBitmap);
}
```

## 问题描述
聚光灯圆形区域显示黑色，预期应该显示截图内容。

## 可能原因分析

### 1. Bitmap配置不支持透明度
**问题**: `workingBitmap`使用`RGB_565`配置，不支持透明度
**解决方案**: 已修复`setupWorkingBitmap()`方法，确保使用`ARGB_8888`配置

### 2. PorterDuff.Mode.CLEAR不工作
**问题**: 混合模式没有正确应用
**可能原因**:
- Bitmap配置不支持透明度
- 绘制顺序错误
- Paint对象配置问题

### 3. 配置参数错误
**问题**: 聚光灯半径或暗度配置不正确
**检查点**:
- `spotlightRadius`值是否合理
- `spotlightDarkness`值是否在有效范围

## 修复措施

### 1. 修复Bitmap配置
```java
private void setupWorkingBitmap() {
    if (screenshotBitmap != null) {
        // 确保使用支持透明度的Bitmap配置
        Bitmap.Config config = screenshotBitmap.getConfig();
        if (config == Bitmap.Config.RGB_565) {
            config = Bitmap.Config.ARGB_8888; // RGB_565不支持透明度
        }
        workingBitmap = screenshotBitmap.copy(config, true);
        workingCanvas = new Canvas(workingBitmap);
    }
}
```

### 2. 添加调试日志
```java
private void showSpotlight(float x, float y) {
    Log.d("ScreenshotDisplayActivity", "显示聚光灯 - 位置: (" + x + ", " + y + "), 半径: " + spotlightRadius + ", 暗度: " + spotlightDarkness);
    
    // ... 绘制逻辑 ...
    
    Log.d("ScreenshotDisplayActivity", "绘制黑色遮罩 - 透明度: " + alpha);
    Log.d("ScreenshotDisplayActivity", "清除聚光灯区域 - 使用CLEAR模式");
    Log.d("ScreenshotDisplayActivity", "聚光灯绘制完成");
}
```

## 测试步骤

### 1. 检查配置加载
在Logcat中搜索以下日志：
```
ScreenshotDisplayActivity: 配置加载完成 - 聚光灯大小: X, 聚光灯暗度: Y, 聚光灯半径: Z
```

### 2. 检查绘制过程
在Logcat中搜索以下日志：
```
ScreenshotDisplayActivity: 显示聚光灯 - 位置: (x, y), 半径: R, 暗度: D
ScreenshotDisplayActivity: 绘制黑色遮罩 - 透明度: A
ScreenshotDisplayActivity: 清除聚光灯区域 - 使用CLEAR模式
ScreenshotDisplayActivity: 聚光灯绘制完成
```

### 3. 验证效果
- 聚光灯区域应该显示原图内容
- 非聚光灯区域应该被半透明黑色遮罩覆盖
- 聚光灯边缘应该有白色边框

## 预期结果

### 正常行为
1. 聚光灯圆形区域显示截图内容（包括批注）
2. 其他区域被半透明黑色遮罩覆盖
3. 聚光灯边缘有白色边框
4. 鼠标移动时聚光灯跟随移动

### 调试日志示例
```
ScreenshotDisplayActivity: 配置加载完成 - 聚光灯大小: 3, 聚光灯暗度: 80, 聚光灯半径: 150.0
ScreenshotDisplayActivity: 显示聚光灯 - 位置: (100.0, 200.0), 半径: 150.0, 暗度: 80
ScreenshotDisplayActivity: 绘制黑色遮罩 - 透明度: 204
ScreenshotDisplayActivity: 清除聚光灯区域 - 使用CLEAR模式
ScreenshotDisplayActivity: 聚光灯绘制完成
```

## 如果问题仍然存在

### 1. 检查Bitmap配置
```java
Log.d("ScreenshotDisplayActivity", "原始Bitmap配置: " + screenshotBitmap.getConfig());
Log.d("ScreenshotDisplayActivity", "工作Bitmap配置: " + workingBitmap.getConfig());
```

### 2. 尝试不同的混合模式
```java
// 如果CLEAR模式不工作，尝试其他模式
spotlightPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
```

### 3. 检查绘制顺序
确保绘制顺序为：
1. 原图
2. 批注内容
3. 黑色遮罩
4. 清除聚光灯区域
5. 绘制边框

### 4. 验证坐标转换
确保触摸坐标正确转换为Bitmap坐标：
```java
Log.d("ScreenshotDisplayActivity", "触摸坐标: (" + touchX + ", " + touchY + ") -> Bitmap坐标: (" + bitmapX + ", " + bitmapY + ")");
```

## 常见问题

### Q1: 聚光灯区域完全透明
**原因**: `PorterDuff.Mode.CLEAR`清除了所有内容
**解决**: 确保在绘制黑色遮罩后再使用CLEAR模式

### Q2: 聚光灯区域显示黑色
**原因**: Bitmap配置不支持透明度
**解决**: 使用`ARGB_8888`配置

### Q3: 聚光灯位置不准确
**原因**: 坐标转换错误
**解决**: 检查`convertTouchToBitmapCoordinates`方法

### Q4: 聚光灯大小不正确
**原因**: 配置参数错误
**解决**: 检查`spotlightRadius`计算逻辑

## 测试建议

1. **在真机上测试**: 模拟器可能有图形渲染问题
2. **使用简单图片**: 先用纯色图片测试
3. **逐步调试**: 先确保基本功能正常，再添加复杂效果
4. **检查内存**: 确保Bitmap操作不会导致内存问题 