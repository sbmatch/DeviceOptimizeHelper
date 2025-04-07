package com.sbmatch.deviceopt.bean;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class UpdateInfo implements Parcelable {
    public String id;
    public String version;
    public String size;
    public String update_time;
    public String name_all;
    public String update_desc;

    public UpdateInfo() {

    }
    protected UpdateInfo(Parcel in) {
        id = in.readString();
        version = in.readString();
        size = in.readString();
        update_time = in.readString();
        name_all = in.readString();
        update_desc = in.readString();
    }

    public static final Creator<UpdateInfo> CREATOR = new Creator<UpdateInfo>() {
        @Override
        public UpdateInfo createFromParcel(Parcel in) {
            return new UpdateInfo(in);
        }

        @Override
        public UpdateInfo[] newArray(int size) {
            return new UpdateInfo[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UpdateInfo that)) return false;

        if (!id.equals(that.id)) return false;
        if (!version.equals(that.version)) return false;
        if (!size.equals(that.size)) return false;
        if (!update_time.equals(that.update_time)) return false;
        if (!name_all.equals(that.name_all)) return false;
        return update_desc.equals(that.update_desc);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + version.hashCode();
        result = 31 * result + size.hashCode();
        result = 31 * result + update_time.hashCode();
        result = 31 * result + name_all.hashCode();
        result = 31 * result + update_desc.hashCode();
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return "UpdateInfo{" +
                "id='" + id + '\'' +
                ", version='" + version + '\'' +
                ", size='" + size + '\'' +
                ", update_time='" + update_time + '\'' +
                ", name_all='" + name_all + '\'' +
                ", update_desc='" + update_desc + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {

        dest.writeString(id);
        dest.writeString(version);
        dest.writeString(size);
        dest.writeString(update_time);
        dest.writeString(name_all);
        dest.writeString(update_desc);
    }
}
