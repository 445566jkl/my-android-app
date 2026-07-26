# Office 考证通（Android）

这是一个原创风格的全国计算机等级考试一级 MS Office 离线刷题 App 项目。功能形态参考常见刷题软件：题库练习、自动判题、进度统计、错题巩固等；名称、Logo、配色与界面均为原创设计，不使用“未来教育”等第三方品牌名称、商标或界面素材。

## 当前内容

- App 名称：Office 考证通
- 题库数量：400 道选择题
- 题库位置：`app/src/main/assets/questions.json`
- 主程序：`app/src/main/java/com/example/ncremsbank/MainActivity.java`
- 原创 Logo：`app/src/main/res/drawable/ic_app_logo.xml`
- 最低 Android 版本：Android 6.0，API 23
- 目标 SDK：Android 35

## 已实现/保留功能

- 离线刷题，不需要联网
- 点击选项自动判断对错
- 题目进度显示
- 卡片式题目展示
- 原创蓝紫渐变品牌头图
- 原创 App Logo
- GitHub Actions 云端构建 APK

## 如何用 GitHub Actions 生成 APK

1. 把本项目上传到 GitHub 仓库。
2. 打开仓库的 `Actions` 页面。
3. 运行 `Build Android APK` 工作流。
4. 成功后在 `Artifacts/工件` 下载：

```text
ncre-ms-office-question-bank-debug-apk
```

5. 解压后得到：

```text
app-debug.apk
```

## 如何用 Android Studio 生成 APK

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

## 版权与品牌说明

本项目是原创学习工具示例，不隶属于“未来教育”等第三方机构，也不复刻其商标、Logo、UI 图片或受保护题库内容。若用于公开发布，请自行确认题库来源和授权情况。
