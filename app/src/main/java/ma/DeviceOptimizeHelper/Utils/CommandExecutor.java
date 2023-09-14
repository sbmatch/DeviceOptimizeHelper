package ma.DeviceOptimizeHelper.Utils;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CommandExecutor {

    public static void executeCommand(String command, boolean useSystem, CommandCallback callback) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                StringBuilder output = new StringBuilder();
                Process process = null;
                try {
                    if (useSystem) {
                        // 使用root权限切换到system执行命令
                        process = Runtime.getRuntime().exec(new String[]{"su", "system", "-c", command});
                    } else {
                        // 普通用户权限执行命令
                        process = Runtime.getRuntime().exec(command);
                    }

                    // 获取标准输出流
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                } finally {
                    if (process != null) {
                        process.destroy();
                    }
                }
                return output.toString().trim();
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    callback.onSuccess(result);
                } else {
                    callback.onError(new RuntimeException("快给我太极急支糖浆"));
                }
            }
        }.execute();
    }

    public interface CommandCallback {
        void onSuccess(String output);
        void onError(Exception e);
    }

}
