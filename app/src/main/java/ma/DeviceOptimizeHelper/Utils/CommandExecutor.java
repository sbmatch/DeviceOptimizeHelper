package ma.DeviceOptimizeHelper.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CommandExecutor {

    private static CommandExecutor instance;

    private CommandExecutor() {
        // 私有构造函数，以防止类的实例化
    }

    public static CommandExecutor getInstance() {
        if (instance == null) {
            instance = new CommandExecutor();
        }
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
                    Process process;
                    if (useRoot) {
                        if (switchToSystem) {
                            process = Runtime.getRuntime().exec("su -c 'setprop service.adb.root 1; setprop ro.debuggable 1; setprop ro.secure 0; exec " + command + "'");
                        } else {
                            process = Runtime.getRuntime().exec("su -c " + command);
                        }
                    } else {
                        process = Runtime.getRuntime().exec(command);
                    }

                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    StringBuilder output = new StringBuilder();
                    StringBuilder errorOutput = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                    }

                    while ((line = errorReader.readLine()) != null) {
                        errorOutput.append(line).append("\n");
                    }

                    int exitCode = process.waitFor();

                    if (exitCode == 0) {
                        listener.onSuccess(output.toString());
                    } else {
                        Exception exception = new Exception("Command execution error");
                        listener.onError(errorOutput.toString(), exception);
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                    listener.onError(e.getMessage(), e);
                }
            }
        }).start();
    }

}
