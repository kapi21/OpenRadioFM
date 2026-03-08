package com.ts.main.common;
interface ITsCommon {
	void EnterMode(int nMode);
	void Mute();
	void VolInc();
	void VolDec();
	void VolSet(int nVol);
	void SendMcuKey(int nKey);
	void GotoEq();
	int  GetReverState();
	int  GetBrakeState();
	int  nGetWorkMode();
	int  IsHaveDisc();
	boolean  BtIsConnect();
	boolean  IsMute();
	boolean  IsNightMode();
	boolean  IsTconAdj();
	boolean  IsLastMemory();
	void     TconDvdSetShow();
	void     TconVideoSetShow();
	void     TconSetHide();
	void     TconSet(int nMode);
	void TsVolumeShow();
	void BklTurn();
	float  GetSpeed();
	String  GetTemp();
	float  GetCog();
	

	int GetCurTime();
	int GetTotalTime();
	int GetPlayState();		
	String GetSongName();
	
	String GetId3Album();

	String GetId3Artist();

	String GetId3Title();

	String GetBand();	
	String GetFreq();	
	
	// GDUCK MOD BEGIN NOT INCLUDED IN FACTORY VERSION!
	String GetPsName();
	String GetPtyStr();
	String GetCategory();
	// GDUCK MOD END
	
	int GetSDCard();
	
	void EnterActivity(int nMode);
	
	Map GetListBt();
	
	Map GetListMedia();
	
	void PlayByPath(String path);
	
	void BtDail(String Number);
	
	void PopMuteSet(int nMode);	
	
	void PopMuteClear(int nMode);
	
	int GetRadio_N_Step();
	
	int GetRadio_T_Step();
	
	int StepToFreq(int nStep);
	
	int nGetKey();
	

	int getRepeatMode();

	int getShuffleMode();
	
	boolean GetRadioSTState();		
	
	boolean GetRadioSTSwitch();		
	
	String GetIMEI();				
	
	String GetIMSI();					
	
	String GetDeviceID();		
	
	int GetMcuPowerState();			
	
	
	IBinder getSpecialBinder(String name);
	
	boolean bNaviAutoStart();
	
	String  GetNaviPackage();
	
	int GetadaptanyEnable();
	
	int GetadaptanyW();
	
	int GetadaptanyH();
	
	int GetMusicVol();
	

	boolean isRadioAvailable();
	boolean isRadioMute();
	void toggleRadioMute(); 
	
	boolean bCarplayConnected();
	
	boolean bAutoConnected();
	
	void EnterAutoLink();
	
	
	
}
