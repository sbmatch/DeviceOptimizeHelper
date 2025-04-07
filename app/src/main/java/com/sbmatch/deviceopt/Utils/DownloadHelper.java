package com.sbmatch.deviceopt.utils;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;

import com.sbmatch.deviceopt.AppGlobals;

public class DownloadHelper {

    public static DownloadHelper get(){
        return new DownloadHelper();
    }

    private DownloadManager getDownloadMgr(){
        return AppGlobals.getSystemService(Context.DOWNLOAD_SERVICE);
    }

    /**
     * 获取下载状态
     *
     * @param downloadId an ID for the download, unique across the system.
     *                   This ID is used to make future calls related to this download.
     * @return int
     * @see DownloadManager#STATUS_PENDING  　　 下载等待开始时
     * @see DownloadManager#STATUS_PAUSED   　　 下载暂停
     * @see DownloadManager#STATUS_RUNNING　     正在下载中　
     * @see DownloadManager#STATUS_SUCCESSFUL   下载成功
     * @see DownloadManager#STATUS_FAILED       下载失败
     */
    public int getDownloadStatus(long downloadId) {
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
        Cursor c = getDownloadMgr().query(query);
        if (c != null) {
            try (c) {
                if (c.moveToFirst()) {
                    return c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                }
            }
        }
        return -1;
    }

}
