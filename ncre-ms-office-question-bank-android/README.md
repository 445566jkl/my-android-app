# 全国计算机等级考试一级 MS Office 选择题 App（Android）

这是根据用户上传的《【无忧考吧】计算机一级必考选择题400道.docx》制作的 Android 离线刷题 App 项目。

## 当前内容

- 题库数量：400 道选择题
- 题库位置：`app/src/main/assets/questions.json`
- 主程序：`app/src/main/java/com/example/ncremsbank/MainActivity.java`
- 最低 Android 版本：Android 6.0，API 23
- 目标 SDK：Android 35

## 已实现功能

- 离线刷题，不需要联网
- 上一题 / 下一题
- 点击选项自动判断对错
- 显示 / 隐藏答案和解析
- 错题本
- 收藏题
- 已做题进度统计
- 输入题号跳转
- 关键词搜索题目
- 随机练习
- 50 题模拟考试模式
- 本地自动保存进度、错题、收藏

## 如何生成 APK

### 方法一：Android Studio

1. 安装 Android Studio。
2. 解压本项目压缩包。
3. Android Studio 选择 `Open`。
4. 打开本文件夹：`ncre-ms-office-question-bank-android`。
5. 等待 Gradle Sync 完成。
6. 点击菜单：

```text
Build > Build Bundle(s) / APK(s) > Build APK(s)
```

7. 生成位置一般是：

```text
app/build/outputs/apk/debug/app-debug.apk
```

### 方法二：Windows 脚本

如果你的电脑已经配置好 Java、Android SDK、Gradle，可以双击：

```text
build-apk-windows.bat
```

如果缺少环境，脚本会提示缺什么。

## 说明

当前运行环境没有 Java、Gradle、Android SDK，所以助手侧无法直接编译出 APK 文件；本项目已补齐源码、题库、配置和构建脚本，可在 Android Studio 中生成 APK。

## 后续可继续增强

- 增加夜间模式
- 增加考试倒计时
- 增加成绩页
- 增加题目分类
- 增加导出错题
