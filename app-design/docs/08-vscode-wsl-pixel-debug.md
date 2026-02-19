# VS Code + WSL(Arch) + Android 设备调试

目标：使用 VS Code + WSL 做日常开发，Android Studio 仅在必要时使用。

## 1. 环境

### WSL (Arch)

```bash
sudo pacman -S --needed git unzip zip jdk17-openjdk
java -version
```

### Windows

安装 Android SDK Platform-Tools，确保有 `adb.exe`。

### 手机

开启开发者选项与 USB 调试。

## 2. 在 WSL 复用 Windows adb

`~/.zshrc` 增加：

```bash
export ADB_WIN="/mnt/c/Users/<YourWindowsUser>/AppData/Local/Android/Sdk/platform-tools/adb.exe"
alias adb="$ADB_WIN"
```

重新加载：

```bash
source ~/.zshrc
adb version
```

## 3. 常用调试命令

仓库根目录执行：

```bash
./gradlew :app:assembleDebug
./gradlew :app:installDebug
adb shell am start -n com.example.android16demo/.MainActivity
```

查看日志：

```bash
adb logcat | rg "AndroidRuntime|com.example.android16demo|E/"
```

仅看当前进程：

```bash
PID=$(adb shell pidof -s com.example.android16demo)
adb logcat --pid="$PID"
```

## 4. 常见问题

### SDK location not found

在项目根目录创建 `local.properties`：

```properties
sdk.dir=C:\\Users\\<YourWindowsUser>\\AppData\\Local\\Android\\Sdk
```

### `device unauthorized`

重新插线并在手机上允许调试授权；必要时执行：

```bash
adb kill-server && adb start-server
```

### `INSTALL_FAILED_VERSION_DOWNGRADE`

```bash
adb uninstall com.example.android16demo
./gradlew :app:installDebug
```
