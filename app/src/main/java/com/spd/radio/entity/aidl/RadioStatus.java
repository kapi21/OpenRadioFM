package com.spd.radio.entity.aidl;

import android.os.Parcel;
import android.os.Parcelable;

public class RadioStatus implements Parcelable {
    public int signalLevel;
    public boolean stereo;
    public boolean rdsTPInfo;
    public boolean rdsTAInfo;
    public boolean isSeeking;
    public boolean isPreviewScaning;
    public boolean isAutoSearching;
    /** 1=local, 0=dx (según OEM). */
    public int local;
    /** PTY as int, -1 if unknown. */
    public int rdsPTY;
    public int playState;
    public int pi;

    public RadioStatus() {}

    protected RadioStatus(Parcel in) {
        signalLevel = in.readInt();
        stereo = in.readInt() != 0;
        rdsTPInfo = in.readInt() != 0;
        rdsTAInfo = in.readInt() != 0;
        isSeeking = in.readInt() != 0;
        isPreviewScaning = in.readInt() != 0;
        isAutoSearching = in.readInt() != 0;
        local = in.readInt();
        rdsPTY = in.readInt();
        playState = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(signalLevel);
        dest.writeInt(stereo ? 1 : 0);
        dest.writeInt(rdsTPInfo ? 1 : 0);
        dest.writeInt(rdsTAInfo ? 1 : 0);
        dest.writeInt(isSeeking ? 1 : 0);
        dest.writeInt(isPreviewScaning ? 1 : 0);
        dest.writeInt(isAutoSearching ? 1 : 0);
        dest.writeInt(local);
        dest.writeInt(rdsPTY);
        dest.writeInt(playState);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<RadioStatus> CREATOR = new Creator<RadioStatus>() {
        @Override
        public RadioStatus createFromParcel(Parcel in) {
            return new RadioStatus(in);
        }

        @Override
        public RadioStatus[] newArray(int size) {
            return new RadioStatus[size];
        }
    };
}

