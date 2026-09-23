package com.spd.radio;

import com.spd.radio.IRadioAidlCallback;
import com.spd.radio.entity.aidl.RadioFactorySettings;
import com.spd.radio.entity.aidl.RadioFreqInfo;
import com.spd.radio.entity.aidl.RadioRdsSettings;
import com.spd.radio.entity.aidl.RadioStatus;

interface IRadioAidlInterface {
    void registerCallback(String type, IRadioAidlCallback callback);
    void unregisterCallback(String type);
    void enterSource(String sourcePackageName);
    void exitSource(String sourcePackageName);

    String[] getBands();
    RadioFreqInfo getFreqInfo();
    RadioFreqInfo getFreqInfoByBand(String band);

    String getRadioRdsPS();
    String getRadioRdsRT();
    RadioStatus getRadioStatus();

    RadioRdsSettings getRdsSettings();
    void setRdsSettings(in RadioRdsSettings settings);

    RadioFactorySettings getFactorySettings();
    void setFactorySettings(in RadioFactorySettings settings);

    String[] getRadioAllRegion();
    String getRadioCurrentRegion();

    List<RadioFreqInfo> getList(String listType);
    int getListIndex(String listType);
    void saveList(String listType, int index, in RadioFreqInfo freqInfo);

    void setCommand(int cmd, int arg0, int arg1, in Bundle arg2);
}

