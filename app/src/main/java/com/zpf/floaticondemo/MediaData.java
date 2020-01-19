package com.zpf.floaticondemo;

import android.os.Parcel;
import android.os.Parcelable;

public class MediaData implements Parcelable {
    private int progress;

    MediaData() {
        progress = 0;
    }

    protected MediaData(Parcel in) {
        progress = in.readInt();
    }

    public static final Creator<MediaData> CREATOR = new Creator<MediaData>() {
        @Override
        public MediaData createFromParcel(Parcel in) {
            return new MediaData(in);
        }

        @Override
        public MediaData[] newArray(int size) {
            return new MediaData[size];
        }
    };

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(progress);
    }

}
