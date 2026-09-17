package com.spd.radio.entity.aidl;

import android.os.Parcel;
import android.os.Parcelable;

public class RadioFreqInfo implements Parcelable {
    public String band;
    public int freq;
    public int min;
    public int max;
    public int step;
    public int pi;
    public int signal;
    public String ps;

    public RadioFreqInfo() {}

    protected RadioFreqInfo(Parcel in) {
        band = in.readString();
        freq = in.readInt();
        min = in.readInt();
        max = in.readInt();
        step = in.readInt();
        pi = in.readInt();
        signal = in.readInt();
        ps = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(band);
        dest.writeInt(freq);
        dest.writeInt(min);
        dest.writeInt(max);
        dest.writeInt(step);
        dest.writeInt(pi);
        dest.writeInt(signal);
        dest.writeString(ps);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<RadioFreqInfo> CREATOR = new Creator<RadioFreqInfo>() {
        @Override
        public RadioFreqInfo createFromParcel(Parcel in) {
            return new RadioFreqInfo(in);
        }

        @Override
        public RadioFreqInfo[] newArray(int size) {
            return new RadioFreqInfo[size];
        }
    };
}

