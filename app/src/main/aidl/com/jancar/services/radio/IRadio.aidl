// Jancar IVI — orden de métodos = códigos de transacción del Stub en firmware (ivi-radio / ivi-services).
package com.jancar.services.radio;

import com.jancar.services.radio.IRadioCallback;

interface IRadio {
    void open(IRadioCallback cb, String packageName);
    void close();
    void setFreq(int freq);
    int getFreq();
    void setBand(int band);
    int getBand();
    void setLocation(int location);
    void scanUp(int n);
    void scanDown(int n);
    void scanAll();
    boolean scanStop();
    void step(int direction);
    int getId();
    void selectRdsTa(boolean on);
    void selectRdsAf(boolean on);
    void selectRdsPty(int pty);
    void selectRdsTp(boolean on);
    void setStationDisplayName(String name, boolean persist);
    void setZone(int zone);
    int getZone();
    void openInZone(IRadioCallback cb, String packageName, int zone);
    String getPSText(int arg);
    int getScanAction();
    void setFMScanCondition(float a, float b, float c, float d, float e, float f);
    float[] getFMScanConditions();
    void setAMScanCondition(float a, float b, float c, float d);
    float[] getAMScanConditions();
    void mute();
    void unMute();
    int getfreqValid(int freq);
    void setStereo(boolean on);
    boolean isStereo();
    void setDistanceMode(boolean on);
    boolean getDistanceMode();
    void send(int cmd, in int[] ints, in float[] floats, in String[] strings);
    int[] getI(int key);
    String[] getS(int key);
}
