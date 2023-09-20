package ma.DeviceOptimizeHelper.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class CommandExecutor {

    private static CommandExecutor instance;

    private CommandExecutor() {
        // 私有构造函数，以防止类的实例化
    }

    public static CommandExecutor getInstance() {
        // 如果instance为空，则创建一个新的CommandExecutor实例
        if (instance == null) {
            instance = new CommandExecutor();
        }
        // 返回CommandExecutor实例
        return instance;
    }


    public interface CommandResultListener {
        void onSuccess(String output);

        void onError(String error, Exception e);
    }

    public void executeCommand(final String command, final CommandResultListener listener, final boolean useRoot, final boolean switchToSystem) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 切换权限
                    Process process;
                    // 这里先切换权限
                    if (useRoot) {
                        if (switchToSystem) {
                            process = Runtime.getRuntime().exec(new String[]{"su","system"});
                        } else {
                            process = Runtime.getRuntime().exec(new String[]{"su"});
                        }
                    } else {
                        process = Runtime.getRuntime().exec(new String[]{"sh"});
                    }
                    // 这里跑命令
                    OutputStream outputStream = process.getOutputStream();
                    outputStream.write((command+"\n").getBytes());
                    outputStream.flush();
                    outputStream.close();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    StringBuilder output = new StringBuilder();
                    StringBuilder errorOutput = new StringBuilder();
                    String line;

                    while ((line = reader.readLine())!= null) {
                        output.append(line).append("\n");
                    }

                    while ((line = errorReader.readLine())!= null) {
                        errorOutput.append(line).append("\n");
                    }

                    int exitCode = process.waitFor();

                    // 如果结果为0，则调用listener的onSuccess方法
                    if (exitCode == 0) {
                        listener.onSuccess(output.toString());
                    } else {
                        // 如果结果不为0，则调用listener的onError方法
                        Exception exception = new Exception("Command execution error");
                        listener.onError(errorOutput.toString(), exception);
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                    listener.onError(e.getMessage(), e);
                }
            }
        }).start();
    }}
