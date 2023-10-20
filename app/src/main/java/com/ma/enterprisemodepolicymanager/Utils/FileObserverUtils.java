package com.ma.enterprisemodepolicymanager.Utils;

import android.os.FileObserver;

public class FileObserverUtils extends FileObserver {
    private String mFolderPath;
    private OnFileChangeListener mListener;

    public FileObserverUtils(String folderPath, OnFileChangeListener listener) {
        super(folderPath, FileObserver.CREATE | FileObserver.DELETE);
        mFolderPath = folderPath;
        mListener = listener;
    }

    @Override
    public void onEvent(int event, String path) {
        if (event == FileObserver.CREATE) {
            mListener.onFileCreated(mFolderPath, path);
        } else if (event == FileObserver.DELETE) {
            mListener.onFileDeleted(mFolderPath, path);
        }
    }

    public interface OnFileChangeListener {
        void onFileCreated(String folderPath, String fileName);
        void onFileDeleted(String folderPath, String fileName);
    }
}