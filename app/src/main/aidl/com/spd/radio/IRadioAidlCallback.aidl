package com.spd.radio;

import com.spd.radio.entity.aidl.RadioFreqInfo;
import com.spd.radio.entity.aidl.RadioStatus;

interface IRadioAidlCallback {
    void onRadioBandChanged(String band);
    void onRadioCurrentStatusChanged(in RadioStatus status);
    void onRadioFreqChanged(int freq);
    void onRadioListChanged(String listType, in List<RadioFreqInfo> list, int focusIndex);
    void onRadioRdsPSChanged(String ps);
    void onRadioRdsRTChanged(String rt);
}

