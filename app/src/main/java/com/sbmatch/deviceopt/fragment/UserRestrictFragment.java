package com.sbmatch.deviceopt.fragment;

import android.app.ActivityThread;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;

import com.hchen.himiuix.MiuiAlertDialog;
import com.hchen.himiuix.MiuiPreferenceCategory;
import com.hchen.himiuix.MiuiSwitchPreference;
import com.kongzue.dialogx.dialogs.PopNotification;
import com.kongzue.dialogx.dialogs.PopTip;
import com.sbmatch.deviceopt.AppGlobals;
import com.sbmatch.deviceopt.Service;
import com.sbmatch.deviceopt.utils.CommandExecutor;
import com.sbmatch.deviceopt.utils.ReflectUtils;
import com.sbmatch.deviceopt.utils.ResourceUtils;
import com.sbmatch.deviceopt.utils.SystemServiceWrapper.DevicePolicyManager;
import com.sbmatch.deviceopt.utils.SystemServiceWrapper.UserManager;
import com.sbmatch.deviceopt.ViewModel.ViewModelUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import timber.log.Timber;

public class UserRestrictFragment extends PreferenceFragmentCompat implements Handler.Callback, Filterable, Preference.OnPreferenceChangeListener{
    private final Handler mHandle = new Handler(Looper.getMainLooper(), this);
    private final DevicePolicyManager dpm = DevicePolicyManager.get();;
    private PreferenceScreen preferenceScreen;
    private final UserManager userManager = UserManager.get();
    private final AtomicBoolean switchNewValue = new AtomicBoolean(false);
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 获取根布局，如果不存在则创建一个
        if (preferenceScreen == null) preferenceScreen = getPreferenceManager().createPreferenceScreen(requireContext());

        ViewModelUtils viewModelU = new ViewModelProvider(requireActivity()).get(ViewModelUtils.class);

        viewModelU.getBooleanMutableLiveData().observe(getViewLifecycleOwner(), aBoolean -> {
            // 遍历分类中的所有子项并根据形参设置值
            int count = ((PreferenceGroup) findPreference("user_restrict_group")).getPreferenceCount();
            for (int i = 0; i < count; i++) {
                Preference preference = ((PreferenceGroup) findPreference("user_restrict_group")).getPreference(i);
                Log.i(getTag(),preference.getKey() +" want set to "+ aBoolean.toString());
                try {
                    if (aBoolean){
                        dpm.addUserRestriction(preference.getKey());
                    }else{
                        dpm.clearUserRestriction(preference.getKey());
                    }
                }catch (Throwable e){
                    AppGlobals.getLogger(getTag()).severe("已跳过此策略， 由于: "+e.getMessage());
                    PopTip.show("已跳过此策略， 由于: "+e.getMessage()).iconError();
                    continue;
                }
                mHandle.post(() ->  ((MiuiSwitchPreference)preference).setChecked(userManager.hasUserRestriction(preference.getKey())));
            }
        });

        // 创建首选项分类
        MiuiPreferenceCategory preferenceCategory = new MiuiPreferenceCategory(requireContext(), null);
        preferenceCategory.setKey("user_restrict_group");
        preferenceCategory.setIconSpaceReserved(false);
        preferenceCategory.setTitle("* 注: 限制策略受Android版本及OEM厂商的影响");
        preferenceCategory.setSingleLineTitle(true);
        // 将动态生成的分类添加进首选项的根布局中
        preferenceScreen.addPreference(preferenceCategory);

        // 动态创建SwitchPreference
        for (Object key : UserManager.getALLUserRestrictionsByReflect()) {
            if (key.equals("disallow_biometric")) continue;
            MiuiSwitchPreference switchPreferenceCompat = new MiuiSwitchPreference(requireContext());
            switchPreferenceCompat.setKey(key.toString());
            switchPreferenceCompat.setTitle(ReflectUtils.getResIdReflect((String) key) == -1 ? (CharSequence) key : ResourceUtils.getString(ReflectUtils.getResIdReflect((String) key)));
            switchPreferenceCompat.setSummary((CharSequence) key);
            switchPreferenceCompat.setDefaultValue(userManager.hasUserRestriction(key.toString()));
            switchPreferenceCompat.setOnPreferenceChangeListener(this);
            if (Build.VERSION.SDK_INT >= 30) {
                switch ((String) key) {
                    case "no_add_clone_profile", "no_add_managed_profile" -> {
                        switchPreferenceCompat.setSummaryOn("此策略不保证可用，由于在受管理的设备上添加管理和克隆配置文件的功能已从平台中删除。");
                    }
                }
            }

            // 添加动态生成的SwitchPreference
            preferenceCategory.addPreference(switchPreferenceCompat);
        }

        setPreferenceScreen(preferenceScreen); // 将这些都显示出来

    }

    @Override
    public boolean handleMessage(@NonNull Message msg) {
        // 获取限制策略的键
        String key = (String) msg.obj;

        switch (key) {
            case "no_content_capture" -> {
                dpm.setScreenCaptureDisabled(switchNewValue.get());
                return dpm.getScreenCaptureDisabled();
            }
            case "no_camera" -> {
                dpm.setCameraDisabled(switchNewValue.get());
                return dpm.getCameraDisabled();
            }

        }

        if (switchNewValue.get()) {

            try {
                //启用指定的限制策略
                dpm.addUserRestriction(key);
                if (!userManager.hasUserRestriction(key)) {
                    ((MiuiSwitchPreference)findPreference(key)).setChecked(false);
                }
            } catch (Throwable e2) {
                new MiuiAlertDialog(requireContext())
                        .setTitle(ResourceUtils.getString(ReflectUtils.getResIdReflect(key)) == null ? key : ResourceUtils.getString(ReflectUtils.getResIdReflect(key)) + " 启用失败")
                        .setMessage("原因：" + e2.getMessage())
                        .setCanceledOnTouchOutside(false)
                        .setPositiveButton("Root重试", (dialog, v) -> exec_userRestrictByRunShell(key, true))
                        .setNegativeButton("取消", (dialog, v) -> ((MiuiSwitchPreference) findPreference(key)).setChecked(userManager.hasUserRestriction(key)))
                        .show();
            }
        } else {

            try {
                dpm.clearUserRestriction(key);
                if (userManager.hasUserRestriction(key)) {
                    //findPreference(key).setSummary(key+ " 禁用策略失败");
                    ((MiuiSwitchPreference)findPreference(key)).setChecked(true);
                    new MiuiAlertDialog(requireContext())
                            .setTitle("禁用失败")
                            .setMessage("使用deviceOwner权限尝试禁用后以失败告终, 如果您想继续尝试，我们提供Root备选方案，要继续吗?")
                            .setCanceledOnTouchOutside(false)
                            .setPositiveButton("Root重试", (dialog, v) -> exec_userRestrictByRunShell(key, false))
                            .setNegativeButton("取消", (dialog, v) -> ((MiuiSwitchPreference) findPreference(key)).setChecked(userManager.hasUserRestriction(key)))
                            .show();
                }

            } catch (Throwable e1) {
                new MiuiAlertDialog(requireContext())
                        .setTitle(ResourceUtils.getString(ReflectUtils.getResIdReflect(key)) == null ? key : ResourceUtils.getString(ReflectUtils.getResIdReflect(key))+" 禁用失败")
                        .setMessage("原因："+e1.getMessage())
                        .setCanceledOnTouchOutside(false)
                        .setPositiveButton("Root重试", (dialog, v) -> exec_userRestrictByRunShell(key, false))
                        .setNegativeButton("取消", (dialog, v) -> ((MiuiSwitchPreference) findPreference(key)).setChecked(userManager.hasUserRestriction(key)))
                        .show();
            }
        }

        Timber.i("key: " + key +
                ", 尝试设置状态: " + switchNewValue.get() +
                ", 系统报告状态: " + userManager.hasUserRestriction(key));

        return userManager.hasUserRestriction(key);
    }

    private void exec_userRestrictByRunShell(String key, boolean newValue){
        PopTip.show("正在尝试, 请稍后...");
        CommandExecutor.get().executeCommand("app_process -Djava.class.path=\"" + ActivityThread.currentApplication().getPackageResourcePath() + "\" /  --nice-name=deviceopt_server " + Service.class.getName() +" --user_restrict " + key + " " + newValue, null, true);
        mHandle.postDelayed(() -> {
            ((MiuiSwitchPreference)findPreference(key)).setChecked(userManager.hasUserRestriction(key));
            if (newValue != userManager.hasUserRestriction(key)) {
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(Service.class.getName()+"_command_failure");
                intentFilter.setPriority(Integer.MAX_VALUE);
                Intent remoteAppProcessBroadcast = requireContext().registerReceiver(null, intentFilter);

                try {
                    Throwable t = remoteAppProcessBroadcast.getSerializableExtra("command_failure", Throwable.class);
                    if (t != null) PopNotification.show("很抱歉, 依旧失败", Log.getStackTraceString(t)).showLong();
                }catch (Throwable ignored){}

            }else {
                PopNotification.show("执行结果","使用root权限设置成功");
                findPreference(key).setSummary(key);
            }

        }, 3500);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                // 获取根布局
                if (isAdded() && (preferenceScreen != null || getPreferenceScreen() != null)) {

                    // 创建一个新的 List 来存储过滤后的结果
                    List<Preference> filteredPreferences = new ArrayList<>();
                    // 遍历分类中的所有子项
                    for (int i = 0; i < ((PreferenceGroup)preferenceScreen.findPreference("user_restrict_group")).getPreferenceCount(); i++) {
                        Preference preference = ((PreferenceGroup)preferenceScreen.findPreference("user_restrict_group")).getPreference(i);
                        if (preference.getKey().contains(constraint) || preference.getTitle().toString().contains(constraint)) {
                            filteredPreferences.add(preference);
                        }
                    }
                    // 将过滤结果保存到 results
                    results.count = filteredPreferences.size();
                    results.values = filteredPreferences;
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                PreferenceGroup filterCategory = getPreferenceScreen().findPreference("user_restrict_group");
                // 遍历分类中的所有子项并隐藏
                for (int i = 0; i < filterCategory.getPreferenceCount(); i++) {
                    filterCategory.getPreference(i).setVisible(false);
                }

                ((List<Preference>) results.values).forEach(preference -> {
                    //显示符合条件的preference
                    preference.setVisible(true);
                });
            }
        };
    }

    @Override
    public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
        switchNewValue.set((Boolean) newValue);
        Message message = Message.obtain();
        message.obj = preference.getKey();
        mHandle.sendMessage(message); // 发送消息
        return true;
    }
}