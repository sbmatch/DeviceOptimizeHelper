package ma.DeviceOptimizeHelper.Utils;

import android.os.AsyncTask;
import android.util.Log;

// TODO 找个时间换个实现方法

public class CheckRootPermissionTask extends AsyncTask<Void, Void, Boolean> {

    private OnRootPermissionCheckedListener listener;

    // 构造函数接收回调监听器
    public CheckRootPermissionTask(OnRootPermissionCheckedListener listener) {
        this.listener = listener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"su"});
            process.getOutputStream().close();
            int exitCode = process.waitFor(); // 等待命令执行完成
            Log.i("CheckRootPermissionTask", "exitCode: "+exitCode);
            return exitCode == 0; // 如果退出状态码为0，表示有 root 权限
        } catch (Exception e) {
            e.printStackTrace();
            return false; // 发生异常或没有 root 权限
        }
    }

    @Override
    protected void onPostExecute(Boolean hasRootPermission) {
        // 任务完成后，调用回调方法将结果传递给调用者
        if (listener != null) {
            listener.onRootPermissionChecked(hasRootPermission);
        }
    }
}
