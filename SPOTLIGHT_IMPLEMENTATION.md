# 聚光灯功能实现过程详解

## 功能概述
聚光灯功能允许用户在截图上创建一个圆形区域，该区域保持原图内容清晰可见，而其他区域则被半透明黑色遮罩覆盖，形成聚光灯效果。

## 实现架构

### 1. 核心组件
- **SpotlightFragment**: 配置界面，提供大小和暗度调节
- **SpotlightViewModel**: 数据管理，处理配置保存和加载
- **ScreenshotDisplayActivity**: 主要绘制逻辑，实现聚光灯效果

### 2. 配置参数
- **聚光区域大小** (spotlightRadius): 控制聚光灯圆形区域的半径
- **暗度** (spotlightDarkness): 控制非聚光区域的黑色遮罩透明度

## 详细实现过程

### 第一阶段：UI界面设计

#### 1.1 布局文件 (fragment_spotlight.xml)
```xml
<!-- 聚光灯开关 -->
<SwitchMaterial android:id="@+id/switch_spotlight" />

<!-- 聚光区域大小控制 -->
<SeekBar android:id="@+id/seekbar_spotlight_size" />

<!-- 暗度控制 -->
<SeekBar android:id="@+id/seekbar_darkness" />

<!-- 快速预设 -->
<ChipGroup android:id="@+id/chip_group_presets">
    <Chip android:id="@+id/chip_small" android:text="小聚光" />
    <Chip android:id="@+id/chip_medium" android:text="中聚光" />
    <Chip android:id="@+id/chip_large" android:text="大聚光" />
</ChipGroup>
```

#### 1.2 Fragment实现 (SpotlightFragment.java)
- 初始化UI组件
- 设置监听器处理用户交互
- 与ViewModel通信更新配置
- 与Activity同步配置数据

### 第二阶段：数据管理

#### 2.1 ViewModel (SpotlightViewModel.java)
```java
public class SpotlightViewModel extends ViewModel {
    private MutableLiveData<Integer> spotlightSize = new MutableLiveData<>(3);
    private MutableLiveData<Integer> darkness = new MutableLiveData<>(80);
    private MutableLiveData<Boolean> isEnabled = new MutableLiveData<>(true);
    
    // 保存配置到SharedPreferences
    public void saveSettings() {
        // 实现配置持久化
    }
}
```

#### 2.2 配置持久化
- 使用SharedPreferences保存用户配置
- 应用启动时自动加载上次的配置
- 支持配置的实时更新和保存

### 第三阶段：核心绘制逻辑

#### 3.1 绘制流程 (showSpotlight方法)
```java
private void showSpotlight(float x, float y) {
    // 1. 清除之前的绘制，重新从原图开始
    setupWorkingBitmap();
    
    // 2. 先绘制所有已保存的批注路径
    for (DrawPath drawPath : drawPaths) {
        workingCanvas.drawPath(drawPath.path, drawPath.paint);
    }
    
    // 3. 创建聚光灯效果
    Paint spotlightPaint = new Paint();
    spotlightPaint.setAntiAlias(true);
    
    // 4. 创建半透明黑色遮罩，覆盖整个区域
    int alpha = (int)(255 * spotlightDarkness / 100f);
    spotlightPaint.setColor(Color.argb(alpha, 0, 0, 0));
    workingCanvas.drawRect(0, 0, workingBitmap.getWidth(), workingBitmap.getHeight(), spotlightPaint);
    
    // 5. 清除聚光灯区域，显示原图内容（包括批注）
    spotlightPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    workingCanvas.drawCircle(x, y, spotlightRadius, spotlightPaint);
    
    // 6. 绘制聚光灯边框
    spotlightPaint.setXfermode(null);
    spotlightPaint.setStyle(Paint.Style.STROKE);
    spotlightPaint.setColor(Color.WHITE);
    spotlightPaint.setStrokeWidth(2);
    workingCanvas.drawCircle(x, y, spotlightRadius, spotlightPaint);
    
    // 7. 更新显示
    screenshotImageView.setImageBitmap(workingBitmap);
}
```

#### 3.2 关键技术点

**1. 混合模式 (PorterDuff.Mode.CLEAR)**
- 使用`PorterDuff.Mode.CLEAR`模式在黑色遮罩上"挖洞"
- 清除操作会移除指定区域的所有像素，露出下层内容
- 这是实现聚光灯效果的核心技术

**2. 坐标转换**
- 触摸坐标需要转换为Bitmap坐标
- 确保聚光灯位置与用户触摸位置精确对应

**3. 绘制顺序**
- 先绘制批注内容
- 再绘制黑色遮罩
- 最后清除聚光灯区域
- 确保批注内容在聚光灯区域内可见

### 第四阶段：交互处理

#### 4.1 触摸事件处理
```java
case MotionEvent.ACTION_MOVE:
    if (currentTool == Tool.SPOTLIGHT) {
        showSpotlight(bitmapX, bitmapY);
    }
    break;
```

#### 4.2 实时更新
- 鼠标移动时实时更新聚光灯位置
- 配置改变时立即应用新设置
- 支持平滑的交互体验

### 第五阶段：配置管理

#### 5.1 配置更新
```java
public void updateSpotlightConfig(int size, int darkness) {
    spotlightRadius = size * 50f; // 将配置值转换为实际半径
    spotlightDarkness = darkness;
    saveConfigToPreferences();
}
```

#### 5.2 配置加载
```java
private void loadConfigFromPreferences() {
    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    int spotlightSize = prefs.getInt(KEY_SPOTLIGHT_SIZE, 3);
    spotlightDarkness = prefs.getInt(KEY_SPOTLIGHT_DARKNESS, 80);
    spotlightRadius = spotlightSize * 50f;
}
```

## 技术难点与解决方案

### 1. 绘制性能优化
**问题**: 频繁重绘可能导致性能问题
**解决方案**: 
- 使用`setupWorkingBitmap()`重新创建Canvas
- 避免不必要的对象创建
- 优化绘制顺序

### 2. 坐标精度
**问题**: 触摸坐标与Bitmap坐标不匹配
**解决方案**:
- 实现精确的坐标转换算法
- 考虑ImageView的缩放和偏移
- 确保坐标在有效范围内

### 3. 批注内容保护
**问题**: 聚光灯效果可能覆盖批注内容
**解决方案**:
- 先绘制批注，再应用聚光灯效果
- 使用混合模式确保批注在聚光灯区域内可见

## 用户体验优化

### 1. 实时预览
- 配置改变时立即更新效果
- 提供直观的预览界面

### 2. 快速预设
- 提供小、中、大三种预设
- 简化用户操作流程

### 3. 配置持久化
- 记住用户的偏好设置
- 下次使用时自动应用

## 测试要点

### 1. 功能测试
- 聚光灯位置跟随鼠标移动
- 大小调节功能正常
- 暗度调节效果明显

### 2. 性能测试
- 移动鼠标时无卡顿
- 内存使用合理
- 绘制效果流畅

### 3. 兼容性测试
- 不同屏幕尺寸适配
- 不同Android版本兼容
- 各种分辨率支持

## 扩展功能

### 1. 形状扩展
- 支持方形聚光灯
- 支持椭圆形聚光灯
- 支持自定义形状

### 2. 效果增强
- 添加聚光灯边缘渐变
- 支持多个聚光灯同时存在
- 添加聚光灯动画效果

### 3. 交互优化
- 支持键盘快捷键
- 支持手势操作
- 添加撤销/重做功能

## 总结

聚光灯功能的实现涉及多个技术领域：
1. **UI设计**: 提供直观的配置界面
2. **数据管理**: 使用ViewModel和SharedPreferences
3. **图形绘制**: 使用Canvas和PorterDuff混合模式
4. **交互处理**: 处理触摸事件和坐标转换
5. **性能优化**: 确保流畅的用户体验

通过合理的架构设计和关键技术点的解决，实现了功能完整、性能良好的聚光灯效果。 