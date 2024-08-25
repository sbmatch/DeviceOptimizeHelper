# DeviceOptimizeHelper

DeviceOptimizeHelper是一个 Android APP，它允许管理员配置和管理 Android 设备上的用户限制政策。该工具提供了一组功能和实用工具，允许管理员配置和管理 Android 设备上的用户权限，类似于 Windows 系统中的组策略

## 特点

- **用户限制政策**：管理员可以通过从 `/data/local/tmp/` 目录加载预编译的 Dex 文件来配置各种用户限制政策，例如防止恢复出厂设置、控制应用程序（卸载、禁用、清除数据、强制停止、清除默认应用程序）等限制政策。

- **其他限制类型**：要了解有关 `UserManager` 的源代码以及 Android 系统中可用的限制类型的更多信息，请查看[UserManager 源代码](https://cs.android.com/android/platform/superproject/main/+/main:frameworks/base/core/java/android/os/UserManager.java;l=256?q=UserManager&ss=android%2Fplatform%2Fsuperproject%2Fmain)。


## 许可证

DeviceOptimizeHelper 使用 [MIT 许可证](/LICENSE)。

