package com.sbmatch.deviceopt.ViewModel;

import android.os.IBinder;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.preference.Preference;

public class ViewModelUtils extends ViewModel {

    private final MutableLiveData<IBinder> binderLiveData = new MutableLiveData<>();
    private final MutableLiveData<Preference> preferenceLivedata = new MutableLiveData<>();
    private final MutableLiveData<Boolean> booleanMutableLiveData = new MutableLiveData<>();

    public void savePreferenceObject(Preference p){
        preferenceLivedata.postValue(p);
    }

    public void savePreferenceSwitchNewStatus(boolean newStatus){
        booleanMutableLiveData.postValue(newStatus);
    }

    public LiveData<Boolean> getBooleanMutableLiveData() {
        return booleanMutableLiveData;
    }

    public LiveData<Preference> getPreferenceLiveData() { return preferenceLivedata; }

    public LiveData<IBinder> getBinder() {
        return binderLiveData;
    }

    public void saveBinder(IBinder binder) {
        binderLiveData.postValue(binder);
    }
}