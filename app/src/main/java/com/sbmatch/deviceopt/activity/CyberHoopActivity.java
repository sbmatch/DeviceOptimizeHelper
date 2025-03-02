package com.sbmatch.deviceopt.activity;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.core.app.NavUtils;
import androidx.core.content.PermissionChecker;

import com.kongzue.baseframework.BaseActivity;
import com.kongzue.baseframework.interfaces.Layout;
import com.kongzue.baseframework.util.JumpParameter;
import com.sbmatch.deviceopt.Interface.AbstractIUserServiceFactory;
import com.sbmatch.deviceopt.Interface.OnBinderCallbackListener;
import com.sbmatch.deviceopt.Interface.ShizukuUserServiceFactory;
import com.sbmatch.deviceopt.Utils.ToastUtils;
import com.sbmatch.deviceopt.fragment.CyberHoopFragment;

import ma.DeviceOptimizeHelper.R;
import rikka.shizuku.Shizuku;


@Layout(R.layout.cyberhoop_activity)
public class CyberHoopActivity extends BaseActivity implements Shizuku.OnRequestPermissionResultListener, OnBinderCallbackListener {

    private final static String TAG = CyberHoopActivity.class.getSimpleName();
    private final static Handler mMainHandler = new Handler(Looper.getMainLooper());
    private final Bundle bundle = new Bundle();
    private final Shizuku.OnRequestPermissionResultListener REQUEST_PERMISSION_RESULT_LISTENER = this;
    private final AbstractIUserServiceFactory shizukuUserServiceFactory = ShizukuUserServiceFactory.get();

    public CyberHoopActivity(){

    }

    @Override
    public void initViews() {

    }

    @Override
    public void initDatas(JumpParameter parameter) {

    }

    @Override
    public void setEvents() {

        setupActionBar();

        Shizuku.addRequestPermissionResultListener(REQUEST_PERMISSION_RESULT_LISTENER);

        requestShizukuPermission();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Navigate up using NavUtils, should return to parent Activity
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onRequestPermissionResult(int requestCode, int grantResult) {
        if (grantResult == PermissionChecker.PERMISSION_GRANTED) {
            switch (requestCode) {
                case 4718:
                    shizukuUserServiceFactory.bindUserService(this);
                    ToastUtils.toast("Shizuku 已授权");
                    break;
            }
        }
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(null);
        }
    }
    private void requestShizukuPermission() {
        try {
            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                Shizuku.requestPermission(4718);
            } else {
                shizukuUserServiceFactory.bindUserService(this);
            }
        } catch (Exception e) {
            ToastUtils.toast("未知错误: " + e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        shizukuUserServiceFactory.unbindUserService();
    }

    @Override
    public void onUserServiceReady(IBinder service, String ImplClass) {

        if (ImplClass.toLowerCase().contains("shizuku")){
            bundle.putBinder("serviceBinder_S", service);
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.user_restrict_policy, CyberHoopFragment.class, bundle, "cyberHoop")
                .commit();
    }

    @Override
    public void onUserServiceDisconnected(String ImplClass) throws RemoteException {

    }

}