// Jancar IVI — orden de métodos = códigos de transacción del Stub en firmware (ivi-radio / ivi-services).
package com.jancar.services.radio;

interface IRadioCallback {
    void onSetFreq(int freq);
    void onPowerOn();
    void onPowerOff();
    void onFreqChanged(int freq);
    void onScanResult(int freq, int signal);
    void onScanStart(boolean scanAll);
    void onScanEnd(boolean scanAll);
    void onScanAbort(boolean scanAll);
    void onSignalUpdate(int freq, int strength);
    void suspend();
    void resume();
    void pause();
    void play();
    void playPause();
    void stop();
    void next();
    void prev();
    void quitApp();
    void select(int index);
    void setFavour(boolean favour);
    void onRdsPsChanged(int pi, int freq, String text);
    void onRdsRtChanged(int pi, int freq, String text);
    void onRdsMaskChanged(int pi, int freq, int pty, int tp, int ta);
    void onTuneRotate(boolean clockwise);
    void scanUp();
    void scanDown();
    void scanAll();
    void setNumberkey(int key);
    void onStereo(int freq, boolean stereo);
    void onCMDServiceToApp(int cmd, in int[] ints, in float[] floats, in String[] strings);
}
