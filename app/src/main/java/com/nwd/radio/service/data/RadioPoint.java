package com.nwd.radio.service.data;

import android.os.Parcel;
import android.os.Parcelable;

public class RadioPoint implements Parcelable {
    private int mFrequencyMax;
    private int mFrequencyMin;
    private int mFrequencyStep;

    public RadioPoint(int frequencyMin, int frequencyMax, int frequencyStep) {
        this.mFrequencyMax = frequencyMax;
        this.mFrequencyMin = frequencyMin;
        this.mFrequencyStep = frequencyStep;
    }

    protected RadioPoint(Parcel in) {
        mFrequencyMin = in.readInt();
        mFrequencyMax = in.readInt();
        mFrequencyStep = in.readInt();
    }

    public static final Creator<RadioPoint> CREATOR = new Creator<RadioPoint>() {
        @Override
        public RadioPoint createFromParcel(Parcel in) {
            return new RadioPoint(in);
        }

        @Override
        public RadioPoint[] newArray(int size) {
            return new RadioPoint[size];
        }
    };

    public int getFrequencyMax() {
        return mFrequencyMax;
    }

    public int getFrequencyMin() {
        return mFrequencyMin;
    }

    public int getFrequencyStep() {
        return mFrequencyStep;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mFrequencyMin);
        dest.writeInt(mFrequencyMax);
        dest.writeInt(mFrequencyStep);
    }
}
