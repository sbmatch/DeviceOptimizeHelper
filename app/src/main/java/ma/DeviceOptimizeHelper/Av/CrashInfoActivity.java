package ma.DeviceOptimizeHelper.Av;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

import ma.DeviceOptimizeHelper.R;


public class CrashInfoActivity extends AppCompatActivity {

    AppCompatTextView appCompatAutoCompleteTextView;
    ActionBar actionBar;
    String crashInfo;
    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash_info);

        if (actionBar == null) actionBar = getSupportActionBar();

        actionBar.setTitle("发给开发者问问他怎么个事？");
        actionBar.setBackgroundDrawable(null);

        appCompatAutoCompleteTextView = findViewById(R.id.appCrashInfoTextView);

        // 获取系统信息
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        String version = Build.VERSION.RELEASE;
        int sdkVersion = Build.VERSION.SDK_INT;
        // 创建包含系统信息的日志字符串
        String systemInfo =  "The manufacturer:  "+manufacturer + "\nModel: " + model + "\nAndroid Version: " + version + "\nSDK Version: " + sdkVersion+"\n\n";

        crashInfo = systemInfo+getIntent().getStringExtra(Intent.EXTRA_TEXT);

        appCompatAutoCompleteTextView.setPadding(25, 0, 25, 10);
        appCompatAutoCompleteTextView.setTextIsSelectable(true);

        if (savedInstanceState == null) {
            appCompatAutoCompleteTextView.setText(crashInfo);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }
}