# 不安装 Android Studio，云端生成 APK

如果 Android Studio、JDK、Android SDK 下载很慢，可以用 GitHub Actions 在云端编译 APK。

## 你需要准备

- 一个 GitHub 账号；
- 当前项目文件夹：`ncre-ms-office-question-bank-android`。

## 方法一：网页上传项目，自动生成 APK

### 第 1 步：新建 GitHub 仓库

1. 打开 GitHub；
2. 点击右上角 `+`；
3. 选择 `New repository`；
4. Repository name 填：

```text
ncre-ms-office-question-bank-android
```

5. 选择 `Public` 或 `Private` 都可以；
6. 点击 `Create repository`。

### 第 2 步：上传项目文件

1. 进入刚创建的仓库；
2. 点击 `uploading an existing file`；
3. 把解压后的 `ncre-ms-office-question-bank-android` 文件夹里的所有内容拖进去；
4. 注意：要上传的是文件夹里面的内容，不是外层 zip 文件；
5. 点击底部绿色按钮 `Commit changes`。

### 第 3 步：运行云端编译

1. 进入仓库页面；
2. 点击顶部 `Actions`；
3. 左侧选择 `Build Android APK`；
4. 点击 `Run workflow`；
5. 再点绿色 `Run workflow`。

### 第 4 步：下载 APK

1. 等待任务变成绿色对勾；
2. 点进最新一次运行记录；
3. 页面底部找到 `Artifacts`；
4. 下载：

```text
ncre-ms-office-question-bank-debug-apk
```

5. 解压后得到：

```text
app-debug.apk
```

这个就是可以安装到安卓手机上的 APK。

## 如果 Actions 报错

把报错页面截图发给我，我继续帮你改。

## 当前云端构建配置文件

```text
.github/workflows/build-apk.yml
```

这个文件已经放进项目里，上传到 GitHub 后会自动识别。
