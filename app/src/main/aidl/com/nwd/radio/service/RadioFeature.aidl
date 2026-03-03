package com.nwd.radio.service;

import com.nwd.radio.service.RadioCallback;
import com.nwd.radio.service.data.Frequency;
import com.nwd.radio.service.data.RadioPoint;

interface RadioFeature {
    // Orden ESTRICTO para igualar los TRANSACTION ID de Android IPC
    // ID 1 (0x1)
    void setCurrentFrequency(int frequency, byte bandType, int prefebIndex);
    // ID 2 (0x2)
    Frequency getCurrentFrequency();
    // ID 3 (0x3)
    void seek(boolean isIncrease);
    // ID 4 (0x4)
    void search(boolean isIncrease);
    // ID 5 (0x5)
    void changeBand();
    // ID 6 (0x6)
    void AMS();
    // ID 7 (0x7)
    void INTRO();
    // ID 8 (0x8)
    void setNearOn(boolean isOn);
    // ID 9 (0x9)
    boolean isNearOn();
    // ID 10 (0xa)
    boolean isHasStrero();
    // ID 11 (0xb)
    void setStreroOn(boolean isOn);
    // ID 12 (0xc)
    boolean isStreroOn();
    // ID 13 (0xd)
    void setRadioBackServiceOn(boolean isOn);
    // ID 14 (0xe)
    boolean isRadioBackServiceOn();
    // ID 15 (0xf)
    void setRDSState(byte rdsType, boolean isOn);
    // ID 16 (0x10)
    boolean getRDSState(int rdsType);
    // ID 17 (0x11)
    void setPTYType(byte ptyType);
    // ID 18 (0x12)
    byte getPTYType();
    // ID 19 (0x13)
    byte getPrefabPTYType();
    // ID 20 (0x14)
    void saveCurrentFrequency(byte index);
    // ID 21 (0x15)
    Frequency[] getPrefabFrequency();
    // ID 22 (0x16)
    RadioPoint[] getRadioPoint();
    // ID 23 (0x17)
    byte getRadioState();
    // ID 24 (0x18)
    void registCallback(RadioCallback callback);
    // ID 25 (0x19)
    void unRegistCallback(RadioCallback callback);
    // ID 26 (0x1a)
    void prefeb(boolean isNext);
    // ID 27 (0x1b)
    void sendRadioCommand(byte data0, byte data1);
    // ID 28 (0x1c)
    String getRtMessage();
    // ID 29 (0x1d)
    int getRadioType();
    // ID 30 (0x1e)
    int getCurrentScanState();
}
