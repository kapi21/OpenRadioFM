package com.nwd.radio.service.data;

import android.os.Parcel;
import android.os.Parcelable;

public class Frequency implements Parcelable {
    private byte mBandType;
    private int mFrequency;
    private String mPSName;

    public Frequency(byte bandType, int frequency, String psName) {
        this.mBandType = bandType;
        this.mFrequency = frequency;
        this.mPSName = psName;
    }

    protected Frequency(Parcel in) {
        mBandType = in.readByte();
        mPSName = in.readString();
        mFrequency = in.readInt();
    }

    public static final Creator<Frequency> CREATOR = new Creator<Frequency>() {
        @Override
        public Frequency createFromParcel(Parcel in) {
            return new Frequency(in);
        }

        @Override
        public Frequency[] newArray(int size) {
            return new Frequency[size];
        }
    };

    public byte getBandType() {
        return mBandType;
    }

    public void setBandType(byte bandType) {
        this.mBandType = bandType;
    }

    public int getFrequency() {
        return mFrequency;
    }

    public void setFrequency(int frequency) {
        this.mFrequency = frequency;
    }

    public String getPSName() {
        return mPSName;
    }

    public void setPSName(String psName) {
        this.mPSName = psName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(mBandType);
        dest.writeString(mPSName);
        dest.writeInt(mFrequency);
    }
}
