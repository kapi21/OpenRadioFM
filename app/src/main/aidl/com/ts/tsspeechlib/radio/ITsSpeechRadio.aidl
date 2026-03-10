package com.ts.tsspeechlib.radio;
//收音机aidl
interface ITsSpeechRadio {

    //上一个频道
    void onPrevFreq();
    
    //下一个频道
    void onNextFreq();
    
    //切换频道
    void onSelectedFreq(int number);
    
    // *****2019.1.17
    //打开收音机
    void openRadio();
    
    //关闭收音机
    void closeRadio();
    
    //调频FM
    void onRadioFM();
    
    //调幅AM
    void onRadioAM();
    // 2019.1.17*****
    
    // *****2019.3.13
    //获取收音机状态
    int getRadioState();
    // 2019.3.13*****
    
    // *****2020.3.23
    /**
	 * 设置混音大小
	 * @param size 音量值  0-100 非实时生效，需要切换调用一次setSoundCoexistence(0)（快速切换可能会不生效，最好中间有间隔时间以便跑完流程）
	 */
    void setMixVolumeSize(int size);
    
    /**
	 * 设置应用声音和FM声音共存 (收音声音会变小)
	 * @param state 0不共存，1共存
	 */
    void setSoundCoexistence(int state);
    // 2020.3.23*****
    
    // *****2020.4.08
    //获取收音机FM还是AM return AM:>=4  OT:=3 FM:<3
    int getRadioBand();
    // 2020.4.08*****
    
    
    //2020.05.23
    void SeekUp();
    
    
    void SeekDn();
    //2020.06.03
    
    void OpenRadioCh();
    
    void CloseRadioCh();
    
     //切换波段和频率 nBand: 0  FM   4  AM
    void TurnBandAndFq(int nBand,int fq);
    
    
    //是否打开了RDS
    boolean bRdsOn();
    
    int GetRaidoFreq();
    
    
}