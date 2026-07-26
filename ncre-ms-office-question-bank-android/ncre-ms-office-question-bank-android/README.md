# 全国计算机等级考试一级MS刷题助手（Android）

这是一个用于一级 MS Office 选择题练习的 Android 项目，内置 400 道选择题，支持考试模式、背题模式和错题记录。

## 当前功能

### 1. 考试模式

- 每次随机抽取 20 道题
- 每题 1 分，总分 20 分
- 支持 A / B / C / D 选择
- 答题过程中不提示对错，模拟正式考试体验
- 支持上一题、下一题切换
- 最后一题可点击“提交评分”
- 提交后显示得分
- 提交后列出本次做错的题和正确答案
- 做错的题会自动加入错题记录

### 2. 背题模式

- 按题库顺序练习
- 直接显示正确答案
- 支持上一题、下一题
- 适合快速记忆答案和复习知识点

### 3. 错题记录

- 考试模式中做错的题会保存到本机
- 首页可进入错题记录
- 错题记录以背题方式展示
- 可反复复习历史错题

## 项目结构

```text
app/src/main/java/com/example/ncremsbank/MainActivity.java   主界面和刷题逻辑
app/src/main/assets/questions.json                           题库文件
app/src/main/res/values/strings.xml                          App 名称
app/src/main/AndroidManifest.xml                             Android 应用配置
.github/workflows/build-apk.yml                              GitHub Actions 云端构建配置
```

## App 名称

```text
全国计算机等级考试一级MS刷题助手
```

## 云端生成 APK

上传到 GitHub 后，进入：

```text
Actions > Build Android APK > Run workflow
```

构建成功后，在 Artifacts / 工件 中下载 APK。

## 说明

本项目是原创学习工具示例，不隶属于任何第三方教育机构。题库内容请在公开发布前自行确认来源和授权。
