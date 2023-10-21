package com.ma.enterprisemodepolicymanager.Model;

import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class BinderParcel implements Parcelable {
    private IBinder binder;
    public BinderParcel(IBinder binder){
        this.binder = binder;
    }
    public IBinder getBinder(){
        return binder;
    }
    protected BinderParcel(Parcel in) {
        binder = in.readStrongBinder();
    }

    public static final Creator<BinderParcel> CREATOR = new Creator<BinderParcel>() {
        @Override
        public BinderParcel createFromParcel(Parcel in) {
            return new BinderParcel(in);
        }

        @Override
        public BinderParcel[] newArray(int size) {
            return new BinderParcel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeStrongBinder(binder);
    }
}
