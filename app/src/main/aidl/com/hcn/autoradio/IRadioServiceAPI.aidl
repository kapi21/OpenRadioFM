package com.hcn.autoradio;

import com.hcn.autoradio.IRadioCallBack;
import android.os.IBinder;

interface IRadioServiceAPI {
    // Transaction ID: 1 (0x1)
    void registerRadioClientBinder(IBinder binder);
    
    // Transaction ID: 2 (0x2)
    void unRegisterRadioClientBinder();
    
    // Transaction ID: 3 (0x3)
    void registerRadioCallback(IRadioCallBack cb);
    
    // Transaction ID: 4 (0x4)
    void unRegisterRadioCallback(IRadioCallBack cb);
    
    // Transaction ID: 5 (0x5)
    void onBandEvent();
    
    // Transaction ID: 6 (0x6)
    void onASEvent();
    
    // Transaction ID: 7 (0x7)
    void onPSEvent();
    
    // Transaction ID: 8 (0x8)
    void onLocDxEvent();
    
    // Transaction ID: 9 (0x9)
    void onSeekDownEvent();
    
    // Transaction ID: 10 (0xa)
    void onSeekUpEvent();
    
    // Transaction ID: 11 (0xb)
    void onManualUpEvent();
    
    // Transaction ID: 12 (0xc)
    void onManualDownEvent();
    
    // Transaction ID: 13 (0xd)
    void onScanEvent();
    
    // Transaction ID: 14 (0xe)
    void gotoFreq(int freq);
    
    // Transaction ID: 15 (0xf)
    void gotoFreq2(String freq);
    
    // Transaction ID: 16 (0x10)
    void gotoFreqIndex(int index);
    
    // Transaction ID: 17 (0x11)
    int getCurrentBand();
    
    // Transaction ID: 18 (0x12)
    int getCurrentFreq();
    
    // Transaction ID: 19 (0x13)
    boolean IsAS();
    
    // Transaction ID: 20 (0x14)
    boolean IsPS();
    
    // Transaction ID: 21 (0x15)
    boolean IsScan();
    
    // Transaction ID: 22 (0x16)
    boolean IsSeek();
    
    // Transaction ID: 23 (0x17)
    boolean IsStereo();
    
    // Transaction ID: 24 (0x18)
    boolean IsDxLocal();
    
    // Transaction ID: 25 (0x19)
    boolean requestPlayAudio();
}