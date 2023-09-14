package ma.DeviceOptimizeHelper.Utils;

import android.os.Handler;
import android.os.Looper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CommandExecutor {

    public static String executeCommand(String command, boolean useSystem) {
        StringBuilder output = new StringBuilder();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Process process;

                    if (useSystem) {
                        // 使用root权限切换到system执行命令
                        process = Runtime.getRuntime().exec(new String[] { "su", "system", "-c", command });
                    } else {
                        // 普通用户权限执行命令
                        process = Runtime.getRuntime().exec(command);
                    }

                    // 获取标准输出流
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                    // 创建一个线程来读取输出流
                    Thread outputThread = new Thread(() -> {
                        String line;
                        try {
                            while ((line = reader.readLine()) != null) {
                                output.append(line).append("\n");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

                    // 启动线程
                    outputThread.start();

                    // 等待命令执行完毕
                    process.waitFor();

                    // 等待读取线程执行完毕
                    outputThread.join();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        return output.toString().trim();
    }
}
