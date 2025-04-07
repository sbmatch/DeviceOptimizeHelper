package com.sbmatch.deviceopt.fragment;

import android.os.Bundle;
import android.os.IBinder;
import android.os.IUserManager;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.hchen.himiuix.DialogInterface;
import com.hchen.himiuix.MiuiAlertDialog;
import com.hchen.himiuix.MiuiPreference;
import com.hchen.himiuix.MiuiPreferenceCategory;
import com.hchen.himiuix.widget.MiuiTextView;
import com.kongzue.dialogx.dialogs.MessageDialog;
import com.kongzue.dialogx.dialogs.PopTip;
import com.rosan.dhizuku.api.Dhizuku;
import com.sbmatch.deviceopt.AppGlobals;
import com.sbmatch.deviceopt.Interface.DhizukuUserServiceFactory;
import com.sbmatch.deviceopt.Interface.OnBinderCallbackListener;
import com.sbmatch.deviceopt.R;
import com.sbmatch.deviceopt.utils.AppUpdateWithLanzouManager;
import com.sbmatch.deviceopt.utils.SystemServiceWrapper.DevicePolicyManager;
import com.sbmatch.deviceopt.utils.preference.Preference;
import com.tencent.mmkv.MMKV;

import java.util.concurrent.Executors;

import io.noties.markwon.Markwon;
import ma.DeviceOptimizeHelper.IUserService;

public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener {
    private static final String WEB_CLIENT_ID = "777306314377-taq8bsjojjf578spcq2g60rc9rfv97ea.apps.googleusercontent.com";
    private final DevicePolicyManager dpm = DevicePolicyManager.get();
    private androidx.credentials.CredentialManager credentialManager;
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        credentialManager = CredentialManager.create(requireContext());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        PreferenceScreen preferenceScreen = getPreferenceManager().createPreferenceScreen(requireContext());
        MiuiPreferenceCategory settingsGroup = new MiuiPreferenceCategory(requireContext());
        settingsGroup.setKey("settings_group");
        MiuiPreferenceCategory aboutGroup = new MiuiPreferenceCategory(requireContext());
        aboutGroup.setKey("about_group");
        preferenceScreen.addPreference(settingsGroup);
        preferenceScreen.addPreference(aboutGroup);

        MiuiPreference loginWithGoogle = new MiuiPreference(requireContext());
        loginWithGoogle.setKey("login_with_google");
        loginWithGoogle.setTitle("使用 Google 登录");
        loginWithGoogle.setTipText("未登录");
        loginWithGoogle.setIcon(R.drawable.icons_google);
        loginWithGoogle.setOnPreferenceClickListener(this);
        //settingsGroup.addPreference(loginWithGoogle);

        MiuiPreference custom_orgName = new MiuiPreference(requireContext());
        custom_orgName.setKey("custom_orgName_custom_org_name");
        custom_orgName.setTitle("设置个性组织名");
        if (DevicePolicyManager.getDeviceOwnerComponent() == null || !MMKV.defaultMMKV().getBoolean("_isDhizukuPermissionGranted", false)){
            custom_orgName.setEnabled(false);
            custom_orgName.setTipText("此功能暂不可用");
        }else {
            custom_orgName.setTipText(String.valueOf(dpm.getOrganizationName()).equals("null") ? "" : String.valueOf(dpm.getOrganizationName()));
        }
        custom_orgName.setOnPreferenceClickListener(this);
        settingsGroup.addPreference(custom_orgName);

        MiuiPreference checkUpdate = new MiuiPreference(requireContext());
        checkUpdate.setKey("check_update");
        checkUpdate.setTitle("检查更新");
        checkUpdate.setOnPreferenceClickListener(this);
        aboutGroup.addPreference(checkUpdate);

        MiuiPreferenceCategory otherGroup = new MiuiPreferenceCategory(requireContext());
        otherGroup.setKey("other_group");
        preferenceScreen.addPreference(otherGroup);

        MiuiPreference openSourceContributors = new MiuiPreference(requireContext());
        openSourceContributors.setTitle("特别鸣谢");
        openSourceContributors.setKey("openSourceContributors");
        openSourceContributors.setSummary("感谢所有对本应用提供帮助的用户及开源项目");
        openSourceContributors.setOnPreferenceClickListener(this);
        otherGroup.addPreference(openSourceContributors);

        MiuiPreference testPreference = new MiuiPreference(requireContext());
        testPreference.setTitle("版本更新历史");
        testPreference.setKey("version_update_history");
        testPreference.setTipText("");
        testPreference.setOnPreferenceClickListener(preference -> {
            requireArguments().putString("title_", "版本更新历史");
            getParentFragmentManager()
                    .beginTransaction()
                    .hide(this)
                    .add(R.id.settings, TimeLineFragment.class, requireArguments(), "admob")
                    .addToBackStack(null)
                    .commit();

            return true;
        });
        otherGroup.addPreference(testPreference);
        setPreferenceScreen(preferenceScreen);

    }

    @Override
    public boolean onPreferenceClick(@NonNull androidx.preference.Preference preference) {
        switch (preference.getKey()){
            case "login_with_google" -> {

                GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId(WEB_CLIENT_ID)
                        .setAutoSelectEnabled(true)
                        //.setNonce("nonce")
                        .build();

                GetCredentialRequest request = new GetCredentialRequest.Builder()
                        .addCredentialOption(googleIdOption)
                        .build();

                android.os.CancellationSignal cancellationSignal = new android.os.CancellationSignal();
                cancellationSignal.setOnCancelListener(() -> {
                    AppGlobals.getLogger("googleLogin").info("Preparing credentials with Google was cancelled.");
                });

                credentialManager.getCredentialAsync(
                        requireContext(),
                        request,
                        cancellationSignal,
                        Executors.newSingleThreadExecutor(),
                        new CredentialManagerCallback<>() {
                            @Override
                            public void onResult(GetCredentialResponse getCredentialResponse) {
                                AppGlobals.getLogger("googleLogin").info("Received credentials with Google."+ getCredentialResponse.getCredential().getData());
                            }

                            @Override
                            public void onError(@NonNull GetCredentialException e) {
                                AppGlobals.getLogger("googleLogin").severe("Google login error: " + Log.getStackTraceString(e)); // 添加错误日志
                                MessageDialog.show("Google 登录失败", Log.getStackTraceString(e)).setOkButton("我知道了");
                            }
                        });

            }

            case "check_update" -> {
                PopTip.show("正在检查更新...");
                String verMsg = AppUpdateWithLanzouManager.hasUpdate() ? "有新版本可用" : "已是最新版本";
                if (AppUpdateWithLanzouManager.hasUpdate()){
                    new MiuiAlertDialog(requireContext())
                            .setTitle("有新版本啦！")
                            .setMessage(AppUpdateWithLanzouManager.getUpdateInfo().update_desc + "\n\n版本号: "+ AppUpdateWithLanzouManager.getUpdateInfo().version+"  大小: " + AppUpdateWithLanzouManager.getUpdateInfo().size)
                            .setNegativeButton("我知道了", (dialog, v) -> {

                            }).show();

                }
                AppGlobals.sMainHandler.postDelayed(() -> {
                    PopTip.show(verMsg);
                    ((MiuiPreference)findPreference(preference.getKey())).setTipText(verMsg);
                }, 2000);
            }

            case "custom_orgName_custom_org_name" -> {
                new MiuiAlertDialog(requireContext())
                        .setTitle(findPreference(preference.getKey()).getTitle())
                        .setEditTextHint("自定义的组织名")
                        .setEditText(((MiuiPreference)findPreference(preference.getKey())).getTipText() != null ? ((MiuiPreference)findPreference(preference.getKey())).getTipText() : "", (dialog, s) -> {
                            if (s != null && s.length() > 0){
                                dpm.setOrganizationName((String) s);
                                PopTip.show("已修改为 "+ s);
                            }else {
                                dpm.setOrganizationName((String) s);
                                PopTip.show("已清除自定义 组织名");
                            }
                            ((MiuiPreference)findPreference(preference.getKey())).setTipText((String) dpm.getOrganizationName());
                        })
                        .setEditTextAutoKeyboard(true)
                        .setEnableEditTextView(true)
                        .setPositiveButton("确定", null)
                        .show();
            }
            case "openSourceContributors" -> {
                MiuiTextView textView = new MiuiTextView(requireContext());
                textView.setId(View.generateViewId());
                textView.setHeight(1080);
                Markwon markwon = Markwon.create(requireContext());
                markwon.setMarkdown(textView, """
                        感谢所有对本应用提供帮助的用户及开源项目

                        ### 开源项目

                        > **[HiMiuiX](https://github.com/HChenX/HiMiuiX)**
                        > 仿 MiuiX 的 Preference Ui，xml式布局！
                                                    
                        > **[BaseFramework](https://github.com/kongzue/BaseFramework)**
                        > 是一款基础适配框架，包含沉浸式适配、对 Activity、Fragment 以及 Adapter 的封装，并提供了一些诸如权限申请、跳转、延时操作、提示、日志输出等小工具，以方便快速构建 Android App。
                                                    
                        > **[DialogX](https://github.com/kongzue/DialogX)**
                        > DialogX dialog box component library, easy to use, more customizable, more scalable, easy to achieve a variety of dialog boxes.
                                                    
                        > **[Markwon](https://github.com/noties/Markwon)**
                        > Android markdown library (no WebView)
                                                    
                        > **[MMKV](https://github.com/Tencent/MMKV)**
                        > An efficient, small mobile key-value storage framework developed by WeChat. Works on Android, iOS, macOS, Windows, and POSIX.
                                                    
                        > **[AndroidX](https://developer.android.com/jetpack)**
                        > Android Jetpack is a set of libraries, tools and architectural guidance to help make it quick and easy to build great Android apps.
                                                    
                        > **[commons-net](https://commons.apache.org/proper/commons-net/)**
                        > Apache Commons Net™ library implements the client side of many basic Internet protocols.
                                     
                        > **[Dhizuku](https://github.com/iamr0s/Dhizuku)**
                        > A Android Application for share DeviceOwner.
                                       
                        > **[jsoup](https://jsoup.org/)**
                        > jsoup is a Java library that simplifies working with real-world HTML and XML.
                                                    
                        > **[okhttp](https://square.github.io/okhttp/)**
                        > An HTTP+HTTP/2 client for Android and Java applications.
                                                    
                        > **[Shizuku](https://shizuku.rikka.app/)**
                        > Using system APIs directly with adb/root privileges from normal apps through a Java process started with app_process.
                                                    
                        > **[Timeline-View](https://github.com/vipulasri/Timeline-View)**
                        > Timeline View is a simple and highly customizable android library for creating shipment/order tracking, step progress indicators, etc.
                                                    
                                                    
                        """
                );

                new MiuiAlertDialog(requireContext())
                        .setTitle("特别鸣谢")
                        .setEnableCustomView(true)
                        .setCustomView(textView, new DialogInterface.OnBindView() {
                            @Override
                            public void onBindView(ViewGroup root, View view) {

                            }
                        })
                        .setPositiveButton("我知道了", null)
                        .show();

            }
        }
        return true;
    }
}