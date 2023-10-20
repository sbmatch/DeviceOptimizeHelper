package com.ma.enterprisemodepolicymanager.Fragments;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Environment;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.RemoteException;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.ma.enterprisemodepolicymanager.IDeviceOptService;
import com.ma.enterprisemodepolicymanager.ViewModels.FragmentShareIBinder;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.miui.enterprise.sdk.IEpDeletePackageObserver;
import com.miui.enterprise.sdk.IEpInstallPackageObserver;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.ma.enterprisemodepolicymanager.Utils.AnyRestrictPolicyUtils;
import com.ma.enterprisemodepolicymanager.Utils.Enterprise.ApplicationManager;
import com.ma.enterprisemodepolicymanager.Utils.Enterprise.EnterpriseManager;
import com.ma.enterprisemodepolicymanager.Utils.PackageManager;
import com.ma.enterprisemodepolicymanager.Utils.ServiceManager;

import rikka.preference.SimpleMenuPreference;

public class ApplicationManagerFragment extends PreferenceFragmentCompat {
    private PreferenceScreen preferenceScreen;
    private IDeviceOptService deviceOptService;
    PackageManager packageManager = ServiceManager.getPackageManager();
    SharedPreferences sharedPreferences;
    FragmentShareIBinder shareIBinder;
    EnterpriseManager enterpriseManager;
    ApplicationManager applicationManager;
    LinearLayoutCompat layoutCompat;
    CompoundButton.OnCheckedChangeListener changeListener = (buttonView, isChecked) -> {
        int switchId = buttonView.getId();
        if (switchId == 0 && isChecked) {
            // 如果是 id 为 0 的开关被打开，关闭其他开关
            for (int v = 0; v < layoutCompat.getChildCount(); v++){
                View vv = layoutCompat.getChildAt(v);
                if (vv.getId() != View.NO_ID && vv.getId() != 0){
                    if (vv instanceof SwitchMaterial){
                        if (((SwitchMaterial) vv).isChecked()) {
                            ((SwitchMaterial) vv).setChecked(false);
                       }
                    }
                }
            }

        } else if (switchId != 0 && isChecked){
            // 如果是其他开关被打开，关闭 id 为 0 的开关
            SwitchMaterial sm2 = layoutCompat.findViewById(0);
            if (sm2.isChecked()) sm2.setChecked(false);
        }
    };;

    ActivityResultLauncher<String> getApkFilePath = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
        @SuppressLint("ResourceType")
        @Override
        public void onActivityResult(Uri o) {
            if (o != null) {
                String filePath = getPathFromUri(o);
                LinearLayoutCompat.LayoutParams lp_4 = new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp_4.leftMargin = 60;
                lp_4.rightMargin = 60;

                layoutCompat = new LinearLayoutCompat(requireContext());
                layoutCompat.setOrientation(LinearLayoutCompat.VERTICAL);

                SwitchMaterial grantRuntime = new SwitchMaterial(requireContext());
                grantRuntime.setUseMaterialThemeColors(true);
                grantRuntime.setId(16);
                grantRuntime.setText("同时授权所有运行时权限");
                grantRuntime.setOnCheckedChangeListener(changeListener);
                layoutCompat.addView(grantRuntime,lp_4);

                SwitchMaterial grantKeepLive = new SwitchMaterial(requireContext());
                grantKeepLive.setUseMaterialThemeColors(true);
                grantKeepLive.setId(1);
                grantKeepLive.setOnCheckedChangeListener(changeListener);
                grantKeepLive.setText("同时授权保活权限");
                layoutCompat.addView(grantKeepLive, lp_4);

                SwitchMaterial grantAutostart = new SwitchMaterial(requireContext());
                grantAutostart.setUseMaterialThemeColors(true);
                grantAutostart.setOnCheckedChangeListener(changeListener);
                grantAutostart.setId(8);
                grantAutostart.setText("同时授权自启动权限");
                layoutCompat.addView(grantAutostart, lp_4);

                SwitchMaterial grantUninstall = new SwitchMaterial(requireContext());
                grantUninstall.setUseMaterialThemeColors(true);
                grantUninstall.setId(4);
                grantUninstall.setText("同时授权防卸载权限");
                grantUninstall.setOnCheckedChangeListener(changeListener);
                layoutCompat.addView(grantUninstall,lp_4);

                SwitchMaterial priv_default = new SwitchMaterial(requireContext());
                priv_default.setUseMaterialThemeColors(true);
                priv_default.setId(0);
                priv_default.setOnCheckedChangeListener(changeListener);
                priv_default.setText(AnyRestrictPolicyUtils.sAppPrivilegeArray.get(priv_default.getId()));
                layoutCompat.addView(priv_default,lp_4);

                showDialog(filePath.contains("不要") ? null : "即将安装...", filePath, layoutCompat, filePath.contains("不要") ? null : (dialog, which) -> {
                    applicationManager.installPackage(filePath.trim(),  new IEpInstallPackageObserver.Stub() {
                        @Override
                        public void onPackageInstalled(String s, int i, String s1, Bundle bundle) {
                            Looper.prepare();
                            if (i == 1){
                                System.out.println("Info: 安装完成... "+s);

                                if (grantRuntime.isChecked()){
                                    applicationManager.setApplicationSettings(s, com.miui.enterprise.sdk.ApplicationManager.FLAG_GRANT_ALL_RUNTIME_PERMISSION);
                                    Toast.makeText(requireContext(), "已授权"+AnyRestrictPolicyUtils.sAppPrivilegeArray.get(grantRuntime.getId())+"(用户无法更改)", Toast.LENGTH_SHORT).show();
                                }

                                if (grantAutostart.isChecked()) {
                                    applicationManager.setApplicationSettings(s, com.miui.enterprise.sdk.ApplicationManager.FLAG_ALLOW_AUTOSTART);
                                    Toast.makeText(requireContext(), "已授权"+AnyRestrictPolicyUtils.sAppPrivilegeArray.get(grantAutostart.getId()), Toast.LENGTH_SHORT).show();
                                }

                                if (grantKeepLive.isChecked()){
                                    applicationManager.setApplicationSettings(s, com.miui.enterprise.sdk.ApplicationManager.FLAG_KEEP_ALIVE);
                                    Toast.makeText(requireContext(), "已授权"+AnyRestrictPolicyUtils.sAppPrivilegeArray.get(grantKeepLive.getId())+" (用户无法更改)", Toast.LENGTH_SHORT).show();

                                }

                                if (grantUninstall.isChecked()){
                                    applicationManager.setApplicationSettings(s, com.miui.enterprise.sdk.ApplicationManager.FLAG_PREVENT_UNINSTALLATION);
                                    Toast.makeText(requireContext(), "已授权"+AnyRestrictPolicyUtils.sAppPrivilegeArray.get(grantUninstall.getId())+" (用户无法更改)", Toast.LENGTH_SHORT).show();
                                }

                                if (priv_default.isChecked()){
                                    applicationManager.setApplicationSettings(s, com.miui.enterprise.sdk.ApplicationManager.FLAG_DEFAULT);
                                    Toast.makeText(requireContext(), ""+AnyRestrictPolicyUtils.sAppPrivilegeArray.get(priv_default.getId()), Toast.LENGTH_SHORT).show();
                                }

                            }else {
                                System.err.println("Error: 安装失败... 返回代码:"+i);
                                Toast.makeText(requireContext(), "安装失败... 返回代码:"+i, Toast.LENGTH_SHORT).show();
                            }
                            Toast.makeText(requireContext(), "安装完成", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                    });
                });

            }
        }
    });

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        sharedPreferences = requireContext().getSharedPreferences("main_sharePreference", Context.MODE_PRIVATE);
        shareIBinder = new ViewModelProvider(this).get(FragmentShareIBinder.class);
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {

        deviceOptService = shareIBinder.getDeviceOptService();
        try {
            applicationManager = new ApplicationManager(getService(shareIBinder.getEnterpriseManager().getService(EnterpriseManager.APPLICATION_MANAGER),"com.miui.enterprise.IApplicationManager"));
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

        // 获取根布局，如果不存在则创建一个
        if (preferenceScreen == null) {
            preferenceScreen = getPreferenceManager().createPreferenceScreen(requireContext());
        }
        preferenceScreen.removeAll();

        PreferenceCategory appRestrictionCategory = new PreferenceCategory(requireContext());
        appRestrictionCategory.setIconSpaceReserved(false);
        appRestrictionCategory.setTitle("应用管控");
        preferenceScreen.addPreference(appRestrictionCategory);

        PreferenceCategory apkSilentModeInstallOrUninstallCategory = new PreferenceCategory(requireContext());
        apkSilentModeInstallOrUninstallCategory.setIconSpaceReserved(false);
        apkSilentModeInstallOrUninstallCategory.setTitle("静默卸载安装与特殊权限管控");
        appRestrictionCategory.addPreference(apkSilentModeInstallOrUninstallCategory);

        Preference entInstall = new Preference(requireContext());
        entInstall.setTitle("静默安装应用");
        entInstall.setIconSpaceReserved(false);
        entInstall.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(@NonNull Preference preference) {
                getApkFilePath.launch("application/vnd.android.package-archive");
                return deviceOptService != null;
            }
        });
        apkSilentModeInstallOrUninstallCategory.addPreference(entInstall);

        List<String> key = new ArrayList<>(AnyRestrictPolicyUtils.getInstalledApplicationsArray.keySet());
        List<String> appName = new ArrayList<>(AnyRestrictPolicyUtils.getInstalledApplicationsArray.values());

        SimpleMenuPreference entUninstall = new SimpleMenuPreference(requireContext());
        entUninstall.setTitle("静默卸载应用");
        entUninstall.setIconSpaceReserved(false);
        entUninstall.setEntries(appName.toArray(new CharSequence[0]));
        entUninstall.setEntryValues(key.toArray(new CharSequence[0]));
        entUninstall.setOnPreferenceChangeListener((preference, newValue) -> {

            Toast.makeText(requireContext(), "静默卸载 “"+AnyRestrictPolicyUtils.getInstalledApplicationsArray.get(newValue)+"”.... ", Toast.LENGTH_SHORT).show();
            applicationManager.deletePackage((String) newValue, packageManager.isSystemApp((String) newValue) ? 4 : 2,new IEpDeletePackageObserver.Stub() {
                @Override
                public void onPackageDeleted(String packageName, int returnCode) {

                    System.out.println("卸载应用:" + packageName + (returnCode != 1 ? " 成功!" : " 错误, 返回代码:" + returnCode));
                    Looper.prepare();
                    Toast.makeText(requireContext(), "怎么样了? :" + (returnCode == 1 ? " 成功!" : " 错误, 返回代码:" + returnCode), Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            });
            return deviceOptService != null;
        });
        apkSilentModeInstallOrUninstallCategory.addPreference(entUninstall);

        PreferenceCategory appRunnerBlackWhiteListCategory = new PreferenceCategory(requireContext());
        appRunnerBlackWhiteListCategory.setIconSpaceReserved(false);
        appRunnerBlackWhiteListCategory.setTitle("应用运行黑白名单管控");
        appRestrictionCategory.addPreference(appRunnerBlackWhiteListCategory);



        setPreferenceScreen(preferenceScreen);
    }

    private void showDialog(String title, String msg, View view, DialogInterface.OnClickListener positive){
        new MaterialAlertDialogBuilder(requireContext()).setTitle(title).setMessage(msg).setView(view)
                .setPositiveButton("确定", positive).setNegativeButton("取消", null).create().show();
    }

    private static IInterface getService(IBinder binder, String type){
        try {
            Method asInterfaceMethod = Class.forName(type + "$Stub").getMethod("asInterface", IBinder.class);
            return (IInterface) asInterfaceMethod.invoke(null, binder);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
    private String getPathFromUri(Uri uri) {
        String filePath = null;
        if (DocumentsContract.isDocumentUri(requireContext(), uri)) {
            // 如果是Document类型的URI，使用DocumentFile访问
            DocumentFile documentFile = DocumentFile.fromSingleUri(requireContext(), uri);
            if (documentFile != null) {
                String path = documentFile.getUri().getPath();
                if (path.contains("primary:")){
                    filePath = path.replace("/document/primary:", Environment.getExternalStorageDirectory()+"/");
                } else if (path.startsWith("content://")) {
                    if (path.contains("com.android.fileexplorer.myprovider")) filePath = path.replace("content://com.android.fileexplorer.myprovider/external_files", Environment.getExternalStorageDirectory()+"");
                }else if (path.startsWith("/document")){
                    filePath = "不要在 “最近文件” 中选择文件, 暂未适配";
                }else {
                    // 获取文件的真实绝对路径
                    ContentResolver resolver = requireContext().getContentResolver();
                    Cursor cursor = resolver.query(uri, null, null, null, null, new CancellationSignal());
                    cursor.moveToFirst();
                    String display_name = cursor.getString(cursor.getColumnIndexOrThrow("_display_name"));
                    String document_id = cursor.getString(cursor.getColumnIndexOrThrow("document_id"));
                    String mime_type = cursor.getString(cursor.getColumnIndexOrThrow("mime_type"));
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                       try {
                           Bundle documenBundle = DocumentsContract.getDocumentMetadata(resolver,uri);
                           if (documenBundle != null) {
                               for (String s : documenBundle.keySet()){
                                   System.out.println("key:"+s+" value:"+documenBundle.get(s));
                               }
                           }else {
                               System.err.println("来！ 骗！ 来! 偷袭! 我 22 岁小同志 不讲武德");
                               String command = "find /sdcard -type d -name Android -prune -o -type f -name " + display_name;
                               //filePath = deviceOptService.execCommand(command);
                           }
                       }catch (Throwable e){
                           throw new RuntimeException(e);
                       }
                    }
                    cursor.close();
                }
            }
        } else {
            // 否则，使用普通的URI解析
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = requireActivity().getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    filePath = cursor.getString(columnIndex);
                }
                cursor.close();
            }
        }
        System.out.println(filePath);
        return filePath;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
