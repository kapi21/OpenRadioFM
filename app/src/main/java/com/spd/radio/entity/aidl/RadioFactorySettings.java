package com.spd.radio.entity.aidl;

import android.os.Parcel;
import android.os.Parcelable;

public class RadioFactorySettings implements Parcelable {
    public int FMThresholdDistance;
    public int FMThresholdLocal;
    public int AMThresholdDistance;
    public int AMThresholdLocal;
    public int parameter;

    public RadioFactorySettings() {}

    public RadioFactorySettings(int fmDistance, int fmLocal, int amDistance, int amLocal, int parameter) {
        this.FMThresholdDistance = fmDistance;
        this.FMThresholdLocal = fmLocal;
        this.AMThresholdDistance = amDistance;
        this.AMThresholdLocal = amLocal;
        this.parameter = parameter;
    }

    protected RadioFactorySettings(Parcel in) {
        FMThresholdDistance = in.readInt();
        FMThresholdLocal = in.readInt();
        AMThresholdDistance = in.readInt();
        AMThresholdLocal = in.readInt();
        parameter = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(FMThresholdDistance);
        dest.writeInt(FMThresholdLocal);
        dest.writeInt(AMThresholdDistance);
        dest.writeInt(AMThresholdLocal);
        dest.writeInt(parameter);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<RadioFactorySettings> CREATOR = new Creator<RadioFactorySettings>() {
        @Override
        public RadioFactorySettings createFromParcel(Parcel in) {
            return new RadioFactorySettings(in);
        }

        @Override
        public RadioFactorySettings[] newArray(int size) {
            return new RadioFactorySettings[size];
        }
    };
}

