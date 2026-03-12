# CarView3D

CarView3D 是一个原生 Android 3D 车模展示示例项目，提供两种交互模式：序列图片 360 展示和视频 360 展示。用户可通过左右滑动控制车模旋转，并支持自动播放、触摸中断、延迟恢复等交互行为。

## GitHub 首页展示版简介

一个基于 Android 原生开发的 3D 车模展示 Demo，支持图片序列与视频两种旋转方案，具备左右滑动跟手控制、方向记忆和自动播放恢复能力。

## 项目来源与致谢

本项目基于开源项目进行学习与二次开发：

- 原项目：`ZhaoKaiQiang/car3d`
- 原项目地址：`https://github.com/ZhaoKaiQiang/car3d`

在原项目基础上，本仓库做了以下扩展与调整：

- 增加首页模式入口（图片模式 / 视频模式）
- 新增视频 3D 展示页面与滑动联动交互
- 增加自动播放、触摸暂停、延迟恢复、方向记忆
- 统一标题栏样式（主题色、白色标题与图标、居中标题）
- 针对展示比例、手势灵敏度和自动播放稳定性做了优化

感谢原作者提供的开源实现与思路。

## 项目介绍

本项目用于模拟汽车配置器（Car Configurator）中常见的 360 车模浏览体验，重点演示：

- 高响应的左右滑动旋转
- 自动旋转与手势操作切换
- 视频进度头尾无缝衔接
- 多页面结构与统一 UI 风格

适合作为 Android 交互练习、车模展示原型或产品展示页基础模板。

![demo](https://lois-pictures.oss-cn-hangzhou.aliyuncs.com/picture/GIF%202026-3-12%2018-46-11.gif)

## 功能特性

- 首页模式选择：
  - `PictureCarActivity`：基于 52 张序列图（`p1 ~ p52`）的 360 旋转
  - `VideoCarActivity`：基于 `res/raw/car_view2.mp4` 的视频旋转
- 手势交互：
  - 向左滑：向后旋转
  - 向右滑：向前旋转
- 自动播放策略：
  - 页面进入后自动播放
  - 用户触摸时暂停自动播放
  - 用户停止滑动后延迟恢复，并按最后滑动方向继续
- 界面样式：
  - 三个页面统一标题栏
  - 标题居中、白色文字/图标、主题色背景

## 技术栈

- 语言：Java 11
- Android Gradle Plugin：`8.8.0`
- compileSdk / targetSdk：`35`
- minSdk：`24`
- 主要依赖：
  - AndroidX AppCompat `1.7.1`
  - Material Components `1.13.0`
  - AndroidX Activity `1.9.3`
  - ConstraintLayout `2.2.1`

## 目录结构

```text
CarView3D/
├── app/
│   ├── src/main/java/com/example/carview3d/
│   │   ├── MainActivity.java
│   │   ├── PictureCarActivity.java
│   │   └── VideoCarActivity.java
│   ├── src/main/res/layout/
│   │   ├── activity_main.xml
│   │   ├── activity_picture_car.xml
│   │   └── activity_video_car.xml
│   └── src/main/res/raw/
│       ├── car_view.mp4
│       └── car_view2.mp4
├── gradle/
├── build.gradle
├── settings.gradle
└── gradlew / gradlew.bat
```

## 快速开始

### 环境要求

- Android Studio（建议最新稳定版）
- JDK 11
- Android SDK 35

### 克隆项目

```bash
git clone <你的仓库地址>
cd CarView3D
```

### 构建

```bash
./gradlew assembleDebug
```

Windows：

```bash
gradlew.bat assembleDebug
```

### 运行

- 使用 Android Studio 打开项目
- 选择设备或模拟器
- 运行 `app` 配置

## 常用命令

- 构建 Debug 包：

```bash
./gradlew assembleDebug
```

- 运行全部单元测试：

```bash
./gradlew test
```

- 运行单个测试类：

```bash
./gradlew testDebugUnitTest --tests "com.example.carview3d.ExampleUnitTest"
```

- 运行单个测试方法：

```bash
./gradlew testDebugUnitTest --tests "com.example.carview3d.ExampleUnitTest.addition_isCorrect"
```

- 执行 Lint：

```bash
./gradlew lint
```

## 注意事项

- 模拟器图形后端不同，视频渲染表现可能有差异，建议同时在真机验证。
- 交互手感可通过活动类中的参数调节（滑动阈值、自动播放间隔、恢复延迟等）。
- 标题栏与状态栏颜色由主题统一控制，保证视觉一致性。

## License

本项目用于学习与演示。发布到生产环境前，请补充你的开源许可证，并遵循上游项目 License 的相关要求进行使用与署名。
