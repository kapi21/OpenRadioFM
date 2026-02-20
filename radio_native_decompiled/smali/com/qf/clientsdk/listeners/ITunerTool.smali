.class public interface abstract Lcom/qf/clientsdk/listeners/ITunerTool;
.super Ljava/lang/Object;
.source "ITunerTool.java"


# virtual methods
.method public abstract onCurrentFrequencyPICodeChange(I)V
.end method

.method public abstract onDABSignalFound(Z)V
.end method

.method public abstract onRds_TA_PlayStateChange(Z)V
.end method

.method public abstract onReceiveCurrentFrequencyRdsInfo(BIILjava/lang/String;[BLjava/lang/String;)V
.end method

.method public abstract onReceiveTunerData([B)V
.end method

.method public abstract onRequestSendCurrentFrequencyRdsInfo(BII[B)V
.end method

.method public abstract onRequestSendTunerData([B)V
.end method

.method public abstract onRequestSetDABPowerSupply(Z)V
.end method

.method public abstract onRequestSetDABReset(Z)V
.end method

.method public abstract onSetDABAntennaSupply(Z)V
.end method

.method public abstract onSetTunerAntennaSupply(I)V
.end method

.method public abstract onTuneRdsIndicateInfo([B)V
.end method

.method public abstract onTuneRdsInfo([B)V
.end method

.method public abstract onTuneRdsPSInfo([B)V
.end method

.method public abstract onTuneRdsPtyTypeInfo([B)V
.end method

.method public abstract onTuneRdsRTInfo([B)V
.end method

.method public abstract onTunerInfoChanged([B)V
.end method

.method public abstract onTunerPresetListChanged([B)V
.end method

.method public abstract onTunerRangInfoChanged([B)V
.end method

.method public abstract onTunerRdsPSPresetListInfo([B)V
.end method

.method public abstract rdsSwitcherChange(B)V
.end method

.method public abstract rds_AFInfoChange(BI)V
.end method

.method public abstract rds_AFSwitcherChange(B)V
.end method

.method public abstract rds_PTYInfoChange(BIB)V
.end method

.method public abstract rds_PTYSwitcherChange(B)V
.end method

.method public abstract rds_ProgramServiceInfoChange(BILjava/lang/String;)V
.end method

.method public abstract rds_RegionSwitcherChange(B)V
.end method

.method public abstract rds_TASwitcherChange(B)V
.end method

.method public abstract rds_isStereoPlayStation(Z)V
.end method

.method public abstract rds_isTAState(Z)V
.end method

.method public abstract rds_isTPStation(Z)V
.end method

.method public abstract rds_stationNameChange(Ljava/lang/String;)V
.end method

.method public abstract rds_stationRawTextChange(Ljava/lang/String;)V
.end method
