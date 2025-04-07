package com.sbmatch.deviceopt.utils;

import android.util.Log;

import com.kongzue.dialogx.dialogs.PopTip;
import com.sbmatch.deviceopt.AppGlobals;
import com.tencent.mmkv.MMKV;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class CommandExecutor {
    private ProcessBuilder processBuilder;
    private Process process;

    private CommandExecutor() {

    }

    public static MMKV getMMkv(){
        return MMKV.mmkvWithID("CommandExecutor", MMKV.MULTI_PROCESS_MODE);
    }

    public static CommandExecutor get() {
        return new CommandExecutor();
    }


    public interface CommandResultListener {
        void onSuccess(String output);

        void onError(String error);
    }

    public void executeCommand(String command, CommandResultListener listener, boolean useRoot) {

        processBuilder = useRoot ? new ProcessBuilder("su", "-c", command) : new ProcessBuilder("sh", "-c", command);

        StringBuilder output = new StringBuilder();
        StringBuilder errorOutput = new StringBuilder();

        Thread h1 = new Thread(() -> {

            try {

                process = processBuilder.start();
                OutputStream outputStream = process.getOutputStream();
                outputStream.write(("""

                        exit
                        """).getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
                outputStream.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

                new Thread(() -> {
                    String line;
                    try {
                        while ((line = reader.readLine()) != null) {
                            output.append(line).append("\n");
                        }
                        getMMkv().encode("command_success", String.valueOf(output));
                        reader.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).start();

                new Thread(() -> {
                    String errorLine;
                    try {
                        while ((errorLine = errorReader.readLine()) != null) {
                            errorOutput.append(errorLine).append("\n");
                        }
                        getMMkv().encode("command_failure", String.valueOf(errorOutput));
                        errorReader.close();
                    }catch (IOException e){
                        throw new RuntimeException(e);
                    }
                }).start();

                // Wait for the process to finish
                int exitCode = process.waitFor();

                getMMkv().encode("execStatusCode", exitCode);

                // 如果结果为0，则调用listener的onSuccess方法
                if (exitCode == 0 ){
                    if (listener != null) {
                        listener.onSuccess(output.toString());
                    }
                }

                // 如果结果不为0，则调用listener的onError方法
                if (exitCode != 0){
                    if (listener != null){
                        listener.onError(errorOutput.toString());
                    }
                }

            } catch (IOException | InterruptedException e) {
                getMMkv().encode("hasException", Log.getStackTraceString(e));
                AppGlobals.sMainHandler.post(() -> {
                    PopTip.show(getMMkv().decodeString("hasException")).showLong();
                });
            }
        });

        h1.start();

    }
}
