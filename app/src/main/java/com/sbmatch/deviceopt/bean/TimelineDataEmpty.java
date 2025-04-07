package com.sbmatch.deviceopt.bean;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class TimelineDataEmpty implements Parcelable {
    private String title;
    private String description;
    private String date;

    public TimelineDataEmpty(String title, String description, String date) {
        this.title = title;
        this.description = description;
        this.date = date;
    }

    protected TimelineDataEmpty(Parcel in) {
        title = in.readString();
        description = in.readString();
        date = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(date);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TimelineDataEmpty> CREATOR = new Creator<TimelineDataEmpty>() {
        @Override
        public TimelineDataEmpty createFromParcel(Parcel in) {
            return new TimelineDataEmpty(in);
        }

        @Override
        public TimelineDataEmpty[] newArray(int size) {
            return new TimelineDataEmpty[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getDate() {
        return date;
    }

    @NonNull
    @Override
    public String toString() {
        return "TimelineDataEmpty{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", date='" + date + '\'' +
                '}';
    }

}
