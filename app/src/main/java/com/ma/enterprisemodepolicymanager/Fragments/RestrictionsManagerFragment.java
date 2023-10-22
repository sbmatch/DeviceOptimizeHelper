package com.ma.enterprisemodepolicymanager.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.RemoteException;

import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;

import com.ma.enterprisemodepolicymanager.IDeviceOptService;
import com.ma.enterprisemodepolicymanager.ViewModels.FragmentShareIBinder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.ma.enterprisemodepolicymanager.Utils.AnyRestrictPolicyUtils;

import rikka.preference.SimpleMenuPreference;

public class RestrictionsManagerFragment extends PreferenceFragmentCompat {
    private PreferenceScreen preferenceScreen;
    private IDeviceOptService deviceOptService;
    private SharedPreferences sharedPreferences;
    private FragmentShareIBinder shareIBinder;

    public RestrictionsManagerFragment(){

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        shareIBinder = new ViewModelProvider(this).get(FragmentShareIBinder.class);
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {

        // 获取根布局，如果不存在则创建一个
        if (preferenceScreen == null) {
            preferenceScreen = getPreferenceManager().createPreferenceScreen(requireContext());
        }
        preferenceScreen.removeAll();

        deviceOptService = shareIBinder.getDeviceOptService();

        sharedPreferences = requireContext().getSharedPreferences("main_sharePreference", Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean("restrictFragment",true).apply();

        PreferenceCategory restrictionSystemFeatureCategory = new PreferenceCategory(requireContext());
        restrictionSystemFeatureCategory.setIconSpaceReserved(false);
        restrictionSystemFeatureCategory.setTitle("系统功能管控");
        preferenceScreen.addPreference(restrictionSystemFeatureCategory);

        for (String pkg : AnyRestrictPolicyUtils.getDisallowsFieldReflect()) {
            SwitchPreferenceCompat switchPreferenceCompat = new SwitchPreferenceCompat(requireContext());
            switchPreferenceCompat.setKey(pkg);
            switchPreferenceCompat.setIconSpaceReserved(true);
            switchPreferenceCompat.setTitle(AnyRestrictPolicyUtils.sEntRestrictionArray.get(pkg));
            switchPreferenceCompat.setDefaultValue(AnyRestrictPolicyUtils.hasRestriction(pkg));
            // 添加开关变化监听器
            switchPreferenceCompat.setOnPreferenceChangeListener((preference, newValue) -> {
                if ((deviceOptService != null) && sharedPreferences.getBoolean("remoteProcessBinder", false)) {
                    try {
                        deviceOptService.setEntRestrict(preference.getKey(), (Boolean) newValue);
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                }
                return deviceOptService != null;
            });

            restrictionSystemFeatureCategory.addPreference(switchPreferenceCompat);
        }

        PreferenceCategory systemSwitcherStatusCategory = new PreferenceCategory(requireContext());
        systemSwitcherStatusCategory.setIconSpaceReserved(false);
        systemSwitcherStatusCategory.setTitle("系统开关状态管控");
        preferenceScreen.addPreference(systemSwitcherStatusCategory);
        ArrayMap<Integer,String> sSystemSwitchStatusArray = AnyRestrictPolicyUtils.sSystemSwitchStatusArray;
        List<String> value = new ArrayList<>(sSystemSwitchStatusArray.values());
        // 使用 Stream API 将整数列表转换字符串列表
        List<String> items = new ArrayList<>(sSystemSwitchStatusArray.keySet().stream().map(String::valueOf).collect(Collectors.toList()));

        for (String key : AnyRestrictPolicyUtils.getControlStatusFieldReflect()){
            SimpleMenuPreference sysSwitcherStatus = new SimpleMenuPreference(requireContext());
            sysSwitcherStatus.setKey(key);
            sysSwitcherStatus.setTitle(AnyRestrictPolicyUtils.sRestrictionStateArray.get(key));
            sysSwitcherStatus.setEntries(value.toArray(new CharSequence[value.size()]));
            sysSwitcherStatus.setEntryValues(items.toArray(new CharSequence[items.size()]));
            sysSwitcherStatus.setDefaultValue(AnyRestrictPolicyUtils.getControlStatus(key));
            sysSwitcherStatus.setOnPreferenceChangeListener((preference, newValue) -> {
                if ((deviceOptService != null) && sharedPreferences.getBoolean("remoteProcessBinder", false)) {
                    try {
                        deviceOptService.setControlStatus(key, Integer.parseInt((String) newValue));
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                }
                return deviceOptService != null;
            });
            sysSwitcherStatus.setSummaryProvider(preference -> AnyRestrictPolicyUtils.sSystemSwitchStatusArray.get(Integer.parseInt(sysSwitcherStatus.getValue())));
            systemSwitcherStatusCategory.addPreference(sysSwitcherStatus);
        }

        setPreferenceScreen(preferenceScreen);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}