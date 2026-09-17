package com.spd.radio.entity.aidl;

import android.os.Parcel;
import android.os.Parcelable;

public class RadioRdsSettings implements Parcelable {
    public int rdsType;
    public boolean rdsAF;
    public boolean rdsTA;
    public boolean rdsCT;
    public boolean rdsREG;
    public boolean rdsEON;
    public int rdsAFLevel;

    public RadioRdsSettings() {}

    public RadioRdsSettings(int rdsType, boolean rdsAF, boolean rdsTA, boolean rdsCT,
                            boolean rdsREG, boolean rdsEON, int rdsAFLevel) {
        this.rdsType = rdsType;
        this.rdsAF = rdsAF;
        this.rdsTA = rdsTA;
        this.rdsCT = rdsCT;
        this.rdsREG = rdsREG;
        this.rdsEON = rdsEON;
        this.rdsAFLevel = rdsAFLevel;
    }

    protected RadioRdsSettings(Parcel in) {
        rdsType = in.readInt();
        rdsAF = in.readInt() != 0;
        rdsTA = in.readInt() != 0;
        rdsCT = in.readInt() != 0;
        rdsREG = in.readInt() != 0;
        rdsEON = in.readInt() != 0;
        rdsAFLevel = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(rdsType);
        dest.writeInt(rdsAF ? 1 : 0);
        dest.writeInt(rdsTA ? 1 : 0);
        dest.writeInt(rdsCT ? 1 : 0);
        dest.writeInt(rdsREG ? 1 : 0);
        dest.writeInt(rdsEON ? 1 : 0);
        dest.writeInt(rdsAFLevel);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<RadioRdsSettings> CREATOR = new Creator<RadioRdsSettings>() {
        @Override
        public RadioRdsSettings createFromParcel(Parcel in) {
            return new RadioRdsSettings(in);
        }

        @Override
        public RadioRdsSettings[] newArray(int size) {
            return new RadioRdsSettings[size];
        }
    };
}

