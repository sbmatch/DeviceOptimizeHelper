package ma.DeviceOptimizeHelper.Utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.SpannableString;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class CommandExecutor {
    public static Handler mHandler = new Handler(msg -> {
        //throw new RuntimeException(msg.obj+"");
        return false;
    });
    public static void executeCommand(String command, boolean useSystem, CommandCallback callback) throws RuntimeException {

        StringBuilder output = new StringBuilder();
        Thread h1;
        h1 = new Thread(new Runnable() {
            @Override
            public void run() {
                Process process = null;
                try {
                    if (useSystem) { // 使用root权限切换到system执行命令
                        process = Runtime.getRuntime().exec(new String[]{"su", "system"});
                    }

                    // 获取标准输入流
                    OutputStream outputStream = process.getOutputStream();
                    outputStream.write((command+"\n").getBytes());
                    outputStream.flush();
                    outputStream.close();

                    Process finalProcess = process;
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                BufferedReader reader = new BufferedReader(new InputStreamReader(finalProcess.getErrorStream()));
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    Message message = Message.obtain();
                                    message.what = 4;
                                    message.obj = new SpannableString(line+"\n");
                                    mHandler.sendMessage(message);
                                }
                                reader.close();
                            }catch (Exception i){
                                i.printStackTrace();
                            }
                        }
                    });

                    thread.start();

                    // 获取标准输出流
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                    Thread outputThread = new Thread(() -> {
                        String line;
                        try {
                            while ((line = reader.readLine()) != null) {
                                output.append(line).append("\n");
                            }
                            reader.close();
                        } catch (IOException ig) {
                            ig.printStackTrace();
                        }
                    });

                    // 启动线程
                    outputThread.start();
                    // 等待命令执行完毕
                    process.waitFor();
                    // 等待读取线程执行完毕
                    outputThread.join();

                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                } finally {
                    if (process != null) {
                        process.destroy();
                    }
                }
            }
        });

        h1.start();

        callback.onSuccess(output.toString());
    }


    public interface CommandCallback {
        void onSuccess(String output);
    }

}
