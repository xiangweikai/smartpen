# Smart Pen

智能笔应用，支持多种功能模块，包括激光笔、聚光灯、放大镜、批注、录音、倒计时、语音自定义等。

## 功能特性

### 核心功能
- **数码激光**: 激光笔功能
- **聚光灯**: 高亮显示特定区域
- **放大镜**: 放大查看内容
- **批注**: 在内容上添加批注
- **录音设置**: 会议录音功能
- **倒计时**: 自定义倒计时功能
- **语音自定义**: 语音反馈和指令

### 新增截图功能
- **按键触发截图**: 通过F12键（可配置）触发屏幕截图
- **全屏显示**: 截图内容在全屏Activity中显示
- **iOS风格菜单**: 底部有批注、激光笔、放大镜、聚光灯四个工具选项
- **前台服务支持**: 使用前台服务支持MediaProjection API，符合Android 8.0+要求

## 技术架构

### 主要组件
- **MainActivity**: 主界面，包含导航抽屉和功能模块
- **ScreenshotUtils**: 截图工具类，处理权限请求和服务启动
- **ScreenshotService**: 前台服务，支持MediaProjection API截图
- **ScreenshotDisplayActivity**: 截图显示Activity，全屏显示截图内容
- **NativeUtils**: 原生库接口，处理按键事件监听

### 技术特点
- 使用MediaProjection API进行高质量截图
- 前台服务支持，符合Android 8.0+要求
- 异步处理，性能优化
- 全屏沉浸式体验
- 响应式布局设计

## 安装和使用

### 系统要求
- Android 5.0（API 21）或更高版本
- 支持MediaProjection API的设备
- 前台服务权限（Android 8.0+）
- FOREGROUND_SERVICE_MEDIA_PROJECTION权限（Android 14+）
- 足够的系统资源用于截图处理

### 权限要求
- `SYSTEM_ALERT_WINDOW`: 允许应用显示在其他应用之上
- `FOREGROUND_SERVICE`: 前台服务权限
- `FOREGROUND_SERVICE_MEDIA_PROJECTION`: Android 14+的mediaProjection前台服务权限
- MediaProjection权限（运行时请求）

### 使用方法

#### 1. 基本功能
- 启动应用后，使用左侧导航抽屉选择不同功能模块
- 各模块提供相应的设置和操作界面

#### 2. 截图功能
- **按键触发**: 运行应用后按F12键触发截图
- **权限授权**: 首次使用需要授权MediaProjection权限
- **服务启动**: 系统会自动启动前台服务进行截图
- **工具使用**: 在截图界面使用底部菜单选择不同工具
  - 批注：在截图上添加文字和图形批注
  - 激光笔：在截图上进行激光笔标注
  - 放大镜：放大查看截图细节
  - 聚光灯：高亮显示特定区域

#### 3. 测试功能
- 启动ScreenshotTestActivity进行功能测试
- 点击"测试截图功能"按钮验证截图功能

## 项目结构

```
app/src/main/java/com/zuomu/smartpen/
├── MainActivity.java                    # 主Activity
├── NativeUtils.java                     # 原生库接口
├── MouseEvent.java                      # 鼠标事件类
├── MouseEventCallback.java              # 鼠标事件回调接口
├── ScreenshotUtils.java                 # 截图工具类
├── ScreenshotService.java               # 截图前台服务
├── ScreenshotDisplayActivity.java       # 截图显示Activity
├── ScreenshotTestActivity.java          # 截图测试Activity
└── features/                            # 功能模块
    ├── annotation/                      # 批注功能
    ├── laser/                          # 激光笔功能
    ├── magnifier/                      # 放大镜功能
    ├── recording/                      # 录音功能
    ├── spotlight/                      # 聚光灯功能
    ├── timer/                          # 倒计时功能
    └── voice/                          # 语音功能
```

## 自定义配置

### 修改触发按键
在 `MainActivity.java` 中修改 `TRIGGER_KEY_CODE` 的值：
```java
private static final int TRIGGER_KEY_CODE = 88; // 修改为其他按键代码
```

### 修改菜单项
在 `screenshot_bottom_menu.xml` 中添加或修改菜单项。

### 修改主题样式
在 `themes.xml` 中修改 `Theme.SmartPen.Fullscreen` 的样式属性。

## 注意事项

1. **权限要求**: 首次使用截图功能时，系统会请求MediaProjection权限，用户必须授权才能使用。

2. **前台服务**: Android 8.0（API 26）及以上版本要求MediaProjection必须在前台服务中使用。

3. **Android 14+权限**: Android 14（API 34）及以上版本需要额外的`FOREGROUND_SERVICE_MEDIA_PROJECTION`权限。

4. **通知显示**: 前台服务会显示一个持续的通知，这是系统要求，无法隐藏。

5. **性能考虑**: 截图功能会消耗一定的系统资源，建议在性能较好的设备上使用。

6. **兼容性**: 该功能需要Android 5.0（API 21）或更高版本。

7. **按键映射**: 不同设备的按键代码可能不同，需要根据实际情况调整触发按键。

## 故障排除

### 截图失败
- 检查是否已授权MediaProjection权限
- 确认设备支持截图功能
- 查看日志中的错误信息
- 确认前台服务正常启动

### 按键无响应
- 确认NativeUtils正确加载
- 检查按键代码是否正确
- 验证鼠标事件监听是否正常工作

### 界面显示问题
- 检查布局文件是否正确
- 确认主题样式是否应用
- 验证Fragment是否正确加载

### 服务相关问题
- 确认前台服务权限已授予
- 检查通知渠道是否正确创建
- 验证服务生命周期管理

## 开发说明

### 编译环境
- Android Studio
- Gradle
- Android SDK

### 依赖库
- Material Design Components
- AndroidX
- Android Support Library

### 原生库
- native-lib: 处理按键事件监听

## 许可证

本项目采用MIT许可证。
