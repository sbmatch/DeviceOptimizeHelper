# DeviceOptimizeHelper

DeviceOptimizeHelper是一个 Android APP，它允许管理员配置和管理 Android 设备上的用户限制政策。该工具提供了一组功能和实用工具，允许管理员配置和管理 Android 设备上的用户权限，类似于 Windows 系统中的组策略

# DeviceOptimizeHelper-Main
DeviceOptimizeHelper-Main 是一个 Android 上的可执行文件，以 root 权限执行 `app_process` 命令，从 `/data/local/tmp/` 目录加载预编译的 Dex 文件，用于配置和管理 Android 设备上的用户限制政策。该工具提供了一组功能和实用工具，允许管理员配置和管理 Android 设备上的用户权限，类似于 Windows 系统中的组策略功能。

## 特点

- **用户限制政策**：管理员可以通过从 `/data/local/tmp/` 目录加载预编译的 Dex 文件来配置各种用户限制政策，例如防止恢复出厂设置、控制应用程序（卸载、禁用、清除数据、强制停止、清除默认应用程序）等限制政策。

- **其他限制类型**：要了解有关 `UserManager` 的源代码以及 Android 系统中可用的限制类型的更多信息，请查看[UserManager 源代码](https://cs.android.com/android/platform/superproject/main/+/main:frameworks/base/core/java/android/os/UserManager.java;l=256?q=UserManager&ss=android%2Fplatform%2Fsuperproject%2Fmain)。

## 使用方法

要使用 DeviceOptimizeHelper，请按照以下步骤在您的 Android 设备上操作：

1. **将预编译的 Dex 文件复制到设备**：将生成的 Dex 文件，例如 `classes.dex`，复制到 Android 设备上的 `/data/local/tmp/` 目录中。这是因为某些情况下，由于权限原因，`system` 权限可能无法访问 `/sdcard` 目录。请确保将 Dex 文件放置在 `/data/local/tmp/` 目录以顺利加载。

2. **打开终端**：连接到您的 Android 设备并打开终端窗口。

3. **以 su 权限运行应用程序**：在终端中，使用以下命令以 `su` 权限运行 `classes.dex`：

```bash
su system -c "app_process -Djava.class.path=/data/local/tmp/classes.dex / ma.DeviceOptimizeHelper.Main"
```
## 自行编译

如果您希望自行编译 `DeviceOptimizeHelper` 项目并调用 `setUserRestrictionReflect` 方法来自定义用户限制政策，可以按照以下步骤操作：

1. **克隆项目**：首先需要使用以下命令将`DeviceOptimizeHelper` 项目克隆到本地计算机。：
```
git clone https://github.com/sbmatch/DeviceOptimizeHelper.git
```
2. **导入项目**：然后，他们可以使用 Android Studio 或其他适当的 IDE 导入项目。

3. **编辑 `Main.java` 文件**：打开位于项目的 `app/src/main/java/ma/DeviceOptimizeHelper/` 目录下的`Main.java`文件。在这个文件中找到 setUserRestrictionReflect 方法。

4. **自定义用户限制政策**：通过调用 setUserRestrictionReflect 方法，他们可以传递相关参数来自定义用户限制政策。根据方法的实现，他们可以添加、删除或修改不同的用户限制。

5. **编译项目**：在编辑完 Main.java 文件后，运行`Build`编译整个项目。

6. **生成 Dex 文件**：编译成功后，在命令行中运行以下命令以生成 Dex 文件。：

```powershell
d8 app\build\intermediates\javac\debug\classes\android\accounts\*.class app\build\intermediates\javac\debug\classes\ma\DeviceOptimizeHelper\*.class --lib "D:\envTools\android-sdk-windows\platforms\android-33\android.jar"
```
- `D:\envTools\android-sdk-windows\platforms\android-33\android.jar` 应替换为您实际的 android.jar 文件路径。




## 许可证

DeviceOptimizeHelper 使用 [MIT 许可证](/LICENSE)。

