package com.nwd.radio.service;

import com.nwd.radio.service.data.Frequency;
import com.nwd.radio.service.data.RadioPoint;

interface RadioCallback {
    // Orden ESTRICTO para igualar los TRANSACTION ID de Android IPC
    // ID 1
    void notifyState(byte state);
    // ID 2
    void notifyCurrentFrequency(byte bandType, int frequency, String psName, int prefabIndex);
    // ID 3
    void notifyNearOn(boolean isOn);
    // ID 4
    void notifyStereo(boolean isStereo);
    // ID 5
    void notifyStereoOn(boolean isOn);
    // ID 6
    void notifyRDSStateChange();
    // ID 7
    void notifyCurrentPTYType(byte ptyType);
    // ID 8
    void notifyPrefabFrequency(in Frequency[] frequencys);
    // ID 9
    void notifyPrefabPTYType(byte ptyType);
    // ID 10
    void notifyRadioPoint(in RadioPoint[] radioPoints);
    // ID 11
    void notifyCurrentIsTA(boolean isTA);
    // ID 12
    void notifyRdsShowState(boolean isShow);
    // ID 13
    void notifyRtMessage(String rtMessage);
    // ID 14
    void notifyRadioScanState(int state);
}
