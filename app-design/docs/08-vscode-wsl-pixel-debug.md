# VS Code + WSL(Arch) + Pixel 7 Pro 轻量调试教程

本教程目标：不用 Android Studio 作为日常 IDE，只用 VS Code + WSL 完成构建、安装、启动和日志调试。

## 1. 方案说明

推荐分工：
- WSL(Arch)：负责代码编辑、Gradle 构建
- Pixel 7 Pro：真机运行
- ADB：优先使用 Windows 的 `adb.exe`（USB 最稳）

这样做的优点是轻量、启动快、真机行为更接近真实用户环境。

## 2. 前置准备

### 2.1 手机端（Pixel 7 Pro）

1. 设置 -> 关于手机 -> 连点版本号 7 次，开启开发者选项
2. 设置 -> 系统 -> 开发者选项：
   - 开启 `USB 调试`
   - 首次连接电脑时同意 RSA 授权

### 2.2 Windows 端（提供 adb）

确保已安装 Android SDK Platform-Tools，并能使用：

```powershell
adb version
```

若未安装，可在 Android Studio 的 SDK Manager 安装 `Android SDK Platform-Tools`。

### 2.3 WSL(Arch) 端（构建环境）

```bash
sudo pacman -S --needed git unzip zip jdk17-openjdk
java -version
```

若 `java -version` 不是 17，请设置 `JAVA_HOME` 到 JDK17。

## 3. 在 WSL 使用 Windows adb

在 WSL 中将 `adb` 指向 Windows 的 `adb.exe`。  
把下面内容加入 `~/.zshrc`（按你的 Windows 用户名改路径）：

```bash
export ADB_WIN="/mnt/c/Users/<YourWindowsUser>/AppData/Local/Android/Sdk/platform-tools/adb.exe"
alias adb="$ADB_WIN"
```

重新加载：

```bash
source ~/.zshrc
adb version
```

## 4. 日常调试流程（项目内）

在仓库根目录执行：

```bash
./gradlew :app:assembleDebug
./gradlew :app:installDebug
adb shell am start -n com.example.android16demo/.MainActivity
```

查看日志：

```bash
adb logcat | rg "AndroidRuntime|com.example.android16demo|E/"
```

只看当前进程日志（更干净）：

```bash
PID=$(adb shell pidof -s com.example.android16demo)
adb logcat --pid="$PID"
```

## 5. 常用命令速查

```bash
# 设备列表
adb devices

# 重启 adb
adb kill-server && adb start-server

# 卸载 app（调签名/安装问题时常用）
adb uninstall com.example.android16demo

# 查看已安装包
adb shell pm list packages | rg android16demo
```

## 6. 无线调试（可选）

USB 首次授权后可切无线（同一局域网）：

```bash
adb tcpip 5555
adb shell ip route
# 记下手机 IP，例如 192.168.1.23
adb connect 192.168.1.23:5555
adb devices
```

之后可拔掉 USB（网络稳定性不如 USB）。

## 7. 常见问题

### 7.1 `device unauthorized`

处理：
1. 手机弹窗点允许
2. 重新插线
3. `adb kill-server && adb start-server`

### 7.2 `no devices/emulators found`

处理：
1. 检查 USB 数据线是否支持数据传输
2. 手机 USB 模式切为文件传输
3. 执行 `adb devices` 确认是否识别

### 7.3 `INSTALL_FAILED_VERSION_DOWNGRADE`

手机上已安装更高版本，先卸载：

```bash
adb uninstall com.example.android16demo
./gradlew :app:installDebug
```

### 7.4 WSL 找不到 `adb`

检查 `ADB_WIN` 路径是否正确，确认 Windows 路径存在 `adb.exe`。

## 8. 建议工作流

1. VS Code 写代码
2. `./gradlew :app:installDebug`
3. `adb shell am start -n com.example.android16demo/.MainActivity`
4. `adb logcat` 看错误
5. 修复后重复步骤 2-4

只有在需要 Compose Preview、Layout Inspector、Profiler 时再打开 Android Studio。
