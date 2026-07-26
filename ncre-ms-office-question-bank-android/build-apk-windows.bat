@echo off
chcp 65001 >nul
setlocal
cd /d %~dp0

echo [1/4] 检查 Java...
where java >nul 2>nul
if errorlevel 1 (
  echo 未检测到 Java。请安装 Android Studio，它会自带/配置 JDK。
  pause
  exit /b 1
)

echo [2/4] 检查 Android SDK...
if "%ANDROID_HOME%"=="" if "%ANDROID_SDK_ROOT%"=="" (
  echo 未检测到 ANDROID_HOME 或 ANDROID_SDK_ROOT。
  echo 请先安装 Android Studio，并在 SDK Manager 安装 Android SDK Platform。
  pause
  exit /b 1
)

echo [3/4] 开始构建 Debug APK...
if exist gradlew.bat (
  call gradlew.bat assembleDebug
) else (
  where gradle >nul 2>nul
  if errorlevel 1 (
    echo 未检测到 Gradle。建议用 Android Studio 打开本项目后点击 Build APK。
    pause
    exit /b 1
  )
  call gradle assembleDebug
)

if errorlevel 1 (
  echo 构建失败，请把上方错误截图发给助手。
  pause
  exit /b 1
)

echo [4/4] 构建完成。
echo APK 位置：app\build\outputs\apk\debug\app-debug.apk
pause
