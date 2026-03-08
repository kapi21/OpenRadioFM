package com.ts.main.common;
interface ITsRadioCommon {
	

	String GetBand();		
	String GetFreq();		
	

	int getBand();		
	int getSelectedMem();
	boolean isNeedUpdate();
	
	List getMemList(); 
	int GetRadio_N_Step();
	
	int GetRadio_T_Step();
	
	int StepToFreq(int nStep);
	
	void Raido_TuneMsetEx(int id);
	
	void Raido_TuneMsaveEx(int id);
	
	void Radio_VolWinShow();
	
	void Radio_TuneStset();
	
	void Radio_TuneBandFm();
	
	void Radio_TuneBandAm();
	
	void Radio_TuneInt();
	
	void Radio_TuneAms();
	
	void Radio_TurnToEq();
	
	void Radio_TuneBand();
	
	void Radio_TuneSearch(int arg0);
	
	void Radio_TuneStep(int arg0);
	
	void Radio_RdsAf();
	
	void Radio_RdsTa();
	
	void Radio_TuneMset(int id);
	
	int Radio_GetDisp(int arg0);
	
	int Radio_GetDispUpdate();
	
	int Radio_GetDispFlag();
	
	void Radio_TuneMsave(int id);
	
	void Evc_evol_workmode_set(int newmode);
	
	String Radio_GetPsName();
	
	int Radio_GetMemListToResult(int arg0);
	
	String Radio_GetMemList(int arg0);
	
	void Radio_RdsPty(int arg0);
	
	void Radio_TuneFset(int arg0);
	
	String Radio_GetPtyStr(int arg0);
	
	int FtSet_Init();
	
	int FtSet_GetRDSen();
	
	int StSet_SetInit();
	
	String Radio_GetMemPsName(int arg0);
	
	
	
	
}
