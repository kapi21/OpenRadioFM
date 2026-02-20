.class public Lcom/qf/clientsdk/QFTunerManager;
.super Lcom/qf/clientsdk/IBaseManager;
.source "QFTunerManager.java"


# static fields
.field private static mManager:Lcom/qf/clientsdk/QFTunerManager;


# instance fields
.field private mITunerToolList:Ljava/util/concurrent/CopyOnWriteArrayList;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Ljava/util/concurrent/CopyOnWriteArrayList<",
            "Lcom/qf/clientsdk/listeners/ITunerTool;",
            ">;"
        }
    .end annotation
.end field

.field private mTunerPresenter:Lcom/qf/clientsdk/presenter/TunerPresenter;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Lcom/qf/clientsdk/presenter/TunerPresenter<",
            "Lcom/qf/clientsdk/listeners/ITunerTool;",
            ">;"
        }
    .end annotation
.end field

.field private volatile tunerScanning:Z


# direct methods
.method private constructor <init>()V
    .locals 2

    .line 23
    invoke-direct {p0}, Lcom/qf/clientsdk/IBaseManager;-><init>()V

    const/4 v0, 0x0

    .line 17
    iput-boolean v0, p0, Lcom/qf/clientsdk/QFTunerManager;->tunerScanning:Z

    .line 21
    new-instance v0, Ljava/util/concurrent/CopyOnWriteArrayList;

    invoke-direct {v0}, Ljava/util/concurrent/CopyOnWriteArrayList;-><init>()V

    iput-object v0, p0, Lcom/qf/clientsdk/QFTunerManager;->mITunerToolList:Ljava/util/concurrent/CopyOnWriteArrayList;

    .line 24
    new-instance v0, Ljava/util/concurrent/CopyOnWriteArrayList;

    invoke-direct {v0}, Ljava/util/concurrent/CopyOnWriteArrayList;-><init>()V

    iput-object v0, p0, Lcom/qf/clientsdk/QFTunerManager;->mITunerToolList:Ljava/util/concurrent/CopyOnWriteArrayList;

    .line 25
    new-instance v0, Lcom/qf/clientsdk/presenter/TunerPresenter;

    iget-object v1, p0, Lcom/qf/clientsdk/QFTunerManager;->mITunerToolList:Ljava/util/concurrent/CopyOnWriteArrayList;

    invoke-direct {v0, v1}, Lcom/qf/clientsdk/presenter/TunerPresenter;-><init>(Ljava/util/concurrent/CopyOnWriteArrayList;)V

    iput-object v0, p0, Lcom/qf/clientsdk/QFTunerManager;->mTunerPresenter:Lcom/qf/clientsdk/presenter/TunerPresenter;

    return-void
.end method

.method public static getInstance()Lcom/qf/clientsdk/QFTunerManager;
    .locals 2

    .line 30
    sget-object v0, Lcom/qf/clientsdk/QFTunerManager;->mManager:Lcom/qf/clientsdk/QFTunerManager;

    if-nez v0, :cond_0

    .line 31
    const-class v0, Lcom/qf/clientsdk/QFTunerManager;

    monitor-enter v0

    .line 32
    :try_start_0
    new-instance v1, Lcom/qf/clientsdk/QFTunerManager;

    invoke-direct {v1}, Lcom/qf/clientsdk/QFTunerManager;-><init>()V

    sput-object v1, Lcom/qf/clientsdk/QFTunerManager;->mManager:Lcom/qf/clientsdk/QFTunerManager;

    .line 33
    monitor-exit v0

    goto :goto_0

    :catchall_0
    move-exception v1

    monitor-exit v0
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    throw v1

    .line 35
    :cond_0
    :goto_0
    sget-object v0, Lcom/qf/clientsdk/QFTunerManager;->mManager:Lcom/qf/clientsdk/QFTunerManager;

    return-object v0
.end method

.method private getServer()Lcom/qf/clientsdk/tuner/ITuner;
    .locals 1

    .line 383
    invoke-static {}, Lcom/qf/clientsdk/tuner/TunerCmdFactory;->getInstance()Lcom/qf/clientsdk/tuner/TunerCmdFactory;

    move-result-object v0

    return-object v0
.end method

.method private sendCmds([B)V
    .locals 5

    if-eqz p1, :cond_0

    .line 387
    array-length v0, p1

    if-lez v0, :cond_0

    .line 389
    :try_start_0
    invoke-static {}, Lcom/qf/clientsdk/QFCoreManager;->getInstance()Lcom/qf/clientsdk/QFCoreManager;

    move-result-object v0

    invoke-virtual {v0}, Lcom/qf/clientsdk/QFCoreManager;->getCoreParam()Lcom/qf/clientsdk/ClientSdkParam;

    move-result-object v0

    invoke-virtual {v0}, Lcom/qf/clientsdk/ClientSdkParam;->getContext()Landroid/content/Context;

    move-result-object v0

    const-string v1, "mcu_service"

    .line 390
    invoke-virtual {v0, v1}, Landroid/content/Context;->getSystemService(Ljava/lang/String;)Ljava/lang/Object;

    move-result-object v0

    check-cast v0, Landroid/qf/mcu/McuManager;

    .line 391
    array-length v1, p1

    const/4 v2, 0x1

    sub-int/2addr v1, v2

    new-array v1, v1, [B

    .line 392
    array-length v3, v1

    const/4 v4, 0x0

    invoke-static {p1, v2, v1, v4, v3}, Ljava/lang/System;->arraycopy(Ljava/lang/Object;ILjava/lang/Object;II)V

    .line 393
    aget-byte p1, p1, v4

    array-length v2, v1

    invoke-virtual {v0, p1, v1, v2}, Landroid/qf/mcu/McuManager;->RPC_SendMcuMsgData(B[BI)V
    :try_end_0
    .catch Ljava/lang/Exception; {:try_start_0 .. :try_end_0} :catch_0

    goto :goto_0

    :catch_0
    move-exception p1

    .line 395
    invoke-virtual {p1}, Ljava/lang/Exception;->printStackTrace()V

    :cond_0
    :goto_0
    return-void
.end method


# virtual methods
.method public autoScan()V
    .locals 1

    .line 129
    invoke-direct {p0}, Lcom/qf/clientsdk/QFTunerManager;->getServer()Lcom/qf/clientsdk/tuner/ITuner;

    move-result-object v0

    if-eqz v0, :cond_0

    .line 130
    invoke-direct {p0}, Lcom/qf/clientsdk/QFTunerManager;->getServer()Lcom/qf/clientsdk/tuner/ITuner;

    move-result-object v0

    invoke-interface {v0}, Lcom/qf/clientsdk/tuner/ITuner;->autoScan()[B

    move-result-object v0

    invoke-direct {p0, v0}, Lcom/qf/clientsdk/QFTunerManager;->sendCmds([B)V

    :cond_0
    return-void
.end method

.method public getTunerPresenter()Lcom/qf/clientsdk/presenter/TunerPresenter;
    .locals 1
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "()",
            "Lcom/qf/clientsdk/presenter/TunerPresenter<",
            "Lcom/qf/clientsdk/listeners/ITunerTool;",
            ">;"
        }
    .end annotation

    .line 51
    iget-object v0, p0, Lcom/qf/clientsdk/QFTunerManager;->mTunerPresenter:Lcom/qf/clientsdk/presenter/TunerPresenter;

    return-object v0
.end method

.method public isMuted()Z
    .locals 1

    .line 272
    invoke-direct {p0}, Lcom/qf/clientsdk/QFTunerManager;->getServer()Lcom/qf/clientsdk/tuner/ITuner;

    const/4 v0, 0x0

    return v0
.end method

.method public isTunerScanning()Z
    .locals 1

    .line 401
    iget-boolean v0, p0, Lcom/qf/clientsdk/QFTunerManager;->tunerScanning:Z

    return v0
.end method

.method public mute()Z
    .locals 1

    .line 248
    invoke-direct {p0}, Lcom/qf/clientsdk/QFTunerManager;->getServer()Lcom/qf/clientsdk/tuner/ITuner;

    const/4 v0, 0x0

    return v0
.end method

.method public onBand(B)V
    .locals 1

    .line 98
    invoke-direct {p0}, Lcom/qf/clientsdk/QFTunerManager;->getServer()Lcom/qf/clientsdk/tuner/ITuner;

    move-result-object v0

    if-eqz v0, :cond_0

    .line 99
    invoke-direct {p0}, Lcom/qf/clientsdk/QFTunerManager;->getServer()Lcom/qf/clientsdk/tuner/ITuner;

    move-result-object v0

    invoke-interface {v0, p1}, Lcom/qf/clientsdk/tuner/ITuner;->onBand(B)[B

    move-result-object p1

    invoke-direct {p0, p1}, Lcom/qf/clientsdk/QFTunerManager;->sendCmds([B)V

    :cond_0
    return-void
.end method

.method public onFine(Z)V
    .locals 1

    .line 162
    invoke-direct {p0}, Lcom/qf/clientsdk/QFTunerManager;->getServer()Lcom/qf/clientsdk/tuner/ITuner;

    move-result-object v0

    if-eqz v0, :cond_0

    .line 163
    invoke-direct {p0}, Lcom/qf/clientsdk/QFTunerManager;->getServer()Lcom/qf/clientsdk/tuner/ITuner;

    move-result-object v0

    invoke-interface {v0, p1}, Lcom/qf/clientsdk/tuner/ITuner;->onFine(Z)[B

    move-result-object p1

    invoke-direct {p0, p1}, Lcom/qf/clientsdk/QFTunerManager;->sendCmds([B)V

    :cond_0
    return-void
.end method

.method public onLoc(B)V
    .locals 1

    .line 173
    invoke-direct {p0}, Lcom/qf/clientsdk/QFTunerManager;->getServer()Lcom/qf/clientsdk/tuner/ITuner;

    move-result-object v0

    if-eqz v0, :cond_0

    .line 174
    invoke-direct {p0}, Lcom/qf/clientsdk/QFTunerManager;->getServer()Lcom/qf/clientsdk/tuner/ITuner;

    move-result-object v0

    invoke-interface {v0, p1}, Lcom/qf/clientsdk/tuner/ITuner;->onLoc(B)[B

    move-result-object p1

    invoke-direct {p0, p1}, Lcom/qf/clientsdk/QFTunerManager;->sendCmds([B)V

    :cond_0
    return-void
.end method

.method public onNext()V
    .locals 1

    .line 191
    invoke-direct {p0}, Lcom/qf/clientsdk/QFTunerManager;->getServer()Lcom/qf/clientsdk/tuner/ITuner;

    move-result-object v0

    if-eqz v0, :cond_0

    .line 192
    invoke-direct {p0}, Lcom/qf/clientsdk/QFTunerManager;->getServer()Lcom/qf/clientsdk/tuner/ITuner;

    move-result-object v0

    invoke-interface {v0}, Lcom/qf/clientsdk/tuner/ITuner;->onNext()[B

    move-result-object v0

    invoke-direct {p0, v0}, Lcom/qf/clientsdk/QFTunerManager;->sendCmds([B)V

    :cond_0
    return-void
.end method

.method public onPre()V
    .locals 1

    .line 182
    invoke-direct {p0}, Lcom/qf/clientsdk/QFTunerManager;->getServer()Lcom/qf/clientsdk/tuner/ITuner;

    move-result-object v0

    if-eqz v0, :cond_0

    .line 183
    invoke-direct {p0}, Lcom/qf/clientsdk/QFTunerManager;->getServer()Lcom/qf/clientsdk/tuner/ITuner;

    move-result-object v0

    invoke-interface {v0}, Lcom/qf/clientsdk/tuner/ITuner;->onPre()[B

    move-result-object v0

    invoke-direct {p0, v0}, Lcom/qf/clientsdk/QFTunerManager;->sendCmds([B)V

    :cond_0
    return-void
.end method

.method public onPresetSave(B)V
    .locals 1

    .line 120
    invoke-direct {p0}, Lcom/qf/clientsdk/QFTunerManager;->getServer()Lcom/qf/clientsdk/tuner/ITuner;

    move-result-object v0

    if-eqz v0, :cond_0

    .line 121
    invoke-direct {p0}, Lcom/qf/clientsdk/QFTunerManager;->getServer()Lcom/qf/clientsdk/tuner/ITuner;

    move-result-object v0

    invoke-interface {v0, p1}, Lcom/qf/clientsdk/tuner/ITuner;->onPresetSave(B)[B

    move-result-object p1

    invoke-direct {p0, p1}, Lcom/qf/clientsdk/QFTunerManager;->sendCmds([B)V

    :cond_0
    return-void
.end method

.method public onPresetSelect(B)V
    .locals 1

    .line 109
    invoke-direct {p0}, Lcom/qf/clientsdk/QFTunerManager;->getServer()Lcom/qf/clientsdk/tuner/ITuner;

    move-result-object v0

    if-eqz v0, :cond_0

    .line 110
    invoke-direct {p0}, Lcom/qf/clientsdk/QFTunerManager;->getServer()Lcom/qf/clientsdk/tuner/ITuner;

    move-result-object v0

    invoke-interface {v0, p1}, Lcom/qf/clientsdk/tuner/ITuner;->onPresetSelect(B)[B

    move-result-object p1

    invoke-direct {p0, p1}, Lcom/qf/clientsdk/QFTunerManager;->sendCmds([B)V

    :cond_0
    return-void
.end method

.method public onRadioArea(B)V
    .locals 1

    .line 62
    invoke-direct {p0}, Lcom/qf/clientsdk/QFTunerManager;->getServer()Lcom/qf/clientsdk/tuner/ITuner;

    move-result-object v0

    if-eqz v0, :cond_0

    .line 63
    invoke-direct {p0}, Lcom/qf/clientsdk/QFTunerManager;->getServer()Lcom/qf/clientsdk/tuner/ITuner;

    move-result-object v0

    invoke-interface {v0, p1}, Lcom/qf/clientsdk/tuner/ITuner;->onRadioArea(B)[B

    move-result-object p1

    invoke-direct {p0, p1}, Lcom/qf/clientsdk/QFTunerManager;->sendCmds([B)V

    :cond_0
    return-void
.end method

.method public onSeek(Z)V
    .locals 1

    .line 150
    invoke-direct {p0}, Lcom/qf/clientsdk/QFTunerManager;->getServer()Lcom/qf/clientsdk/tuner/ITuner;

    move-result-object v0

    if-eqz v0, :cond_0

    .line 151
    invoke-direct {p0}, Lcom/qf/clientsdk/QFTunerManager;->getServer()Lcom/qf/clientsdk/tuner/ITuner;

    move-result-object v0

    invoke-interface {v0, p1}, Lcom/qf/clientsdk/tuner/ITuner;->onSeek(Z)[B

    move-result-object p1

    invoke-direct {p0, p1}, Lcom/qf/clientsdk/QFTunerManager;->sendCmds([B)V

    :cond_0
    return-void
.end method

.method public onTune(I)V
    .locals 1

    .line 73
    invoke-direct {p0}, Lcom/qf/clientsdk/QFTunerManager;->getServer()Lcom/qf/clientsdk/tuner/ITuner;

    move-result-object v0

    if-eqz v0, :cond_0

    .line 74
    invoke-direct {p0}, Lcom/qf/clientsdk/QFTunerManager;->getServer()Lcom/qf/clientsdk/tuner/ITuner;

    move-result-object v0

    invoke-interface {v0, p1}, Lcom/qf/clientsdk/tuner/ITuner;->onTune(I)[B

    move-result-object p1

    invoke-direct {p0, p1}, Lcom/qf/clientsdk/QFTunerManager;->sendCmds([B)V

    :cond_0
    return-void
.end method

.method public openAFSwitcher(Z)V
    .locals 0

    .line 319
    invoke-direct {p0}, Lcom/qf/clientsdk/QFTunerManager;->getServer()Lcom/qf/clientsdk/tuner/ITuner;

    return-void
.end method

.method public openPTYSwitcher(Z)V
    .locals 0

    .line 308
    invoke-direct {p0}, Lcom/qf/clientsdk/QFTunerManager;->getServer()Lcom/qf/clientsdk/tuner/ITuner;

    return-void
.end method

.method public openRDSSwitcher(Z)V
    .locals 0

    .line 286
    invoke-direct {p0}, Lcom/qf/clientsdk/QFTunerManager;->getServer()Lcom/qf/clientsdk/tuner/ITuner;

    return-void
.end method

.method public openREGSwitcher(Z)V
    .locals 0

    .line 330
    invoke-direct {p0}, Lcom/qf/clientsdk/QFTunerManager;->getServer()Lcom/qf/clientsdk/tuner/ITuner;

    return-void
.end method

.method public openTASwitcher(Z)V
    .locals 0

    .line 297
    invoke-direct {p0}, Lcom/qf/clientsdk/QFTunerManager;->getServer()Lcom/qf/clientsdk/tuner/ITuner;

    return-void
.end method

.method public removeTunerTool(Lcom/qf/clientsdk/listeners/ITunerTool;)V
    .locals 1

    .line 45
    iget-object v0, p0, Lcom/qf/clientsdk/QFTunerManager;->mITunerToolList:Ljava/util/concurrent/CopyOnWriteArrayList;

    invoke-virtual {v0, p1}, Ljava/util/concurrent/CopyOnWriteArrayList;->contains(Ljava/lang/Object;)Z

    move-result v0

    if-eqz v0, :cond_0

    .line 46
    iget-object v0, p0, Lcom/qf/clientsdk/QFTunerManager;->mITunerToolList:Ljava/util/concurrent/CopyOnWriteArrayList;

    invoke-virtual {v0, p1}, Ljava/util/concurrent/CopyOnWriteArrayList;->remove(Ljava/lang/Object;)Z

    :cond_0
    return-void
.end method

.method public reportRds_TA_PlayState(Z)V
    .locals 0

    .line 352
    invoke-direct {p0}, Lcom/qf/clientsdk/QFTunerManager;->getServer()Lcom/qf/clientsdk/tuner/ITuner;

    return-void
.end method

.method public requestSendCurrentFrequencyRdsInfo(BII[B)V
    .locals 0

    .line 377
    invoke-direct {p0}, Lcom/qf/clientsdk/QFTunerManager;->getServer()Lcom/qf/clientsdk/tuner/ITuner;

    move-result-object p1

    if-eqz p1, :cond_0

    if-eqz p4, :cond_0

    array-length p1, p4

    :cond_0
    return-void
.end method

.method public requestSendTunerData([B)V
    .locals 1

    .line 363
    invoke-direct {p0}, Lcom/qf/clientsdk/QFTunerManager;->getServer()Lcom/qf/clientsdk/tuner/ITuner;

    move-result-object v0

    if-eqz v0, :cond_0

    if-eqz p1, :cond_0

    array-length v0, p1

    if-lez v0, :cond_0

    .line 364
    invoke-direct {p0, p1}, Lcom/qf/clientsdk/QFTunerManager;->sendCmds([B)V

    :cond_0
    return-void
.end method

.method public requestSetRadioAntennaSupply(I)V
    .locals 0

    .line 341
    invoke-direct {p0}, Lcom/qf/clientsdk/QFTunerManager;->getServer()Lcom/qf/clientsdk/tuner/ITuner;

    return-void
.end method

.method public setPresetList([B)V
    .locals 1

    .line 237
    invoke-direct {p0}, Lcom/qf/clientsdk/QFTunerManager;->getServer()Lcom/qf/clientsdk/tuner/ITuner;

    move-result-object v0

    if-eqz v0, :cond_0

    .line 238
    invoke-direct {p0}, Lcom/qf/clientsdk/QFTunerManager;->getServer()Lcom/qf/clientsdk/tuner/ITuner;

    move-result-object v0

    invoke-interface {v0, p1}, Lcom/qf/clientsdk/tuner/ITuner;->setPresetList([B)[B

    move-result-object p1

    invoke-direct {p0, p1}, Lcom/qf/clientsdk/QFTunerManager;->sendCmds([B)V

    :cond_0
    return-void
.end method

.method public setRdsAFSwitch()V
    .locals 1

    .line 231
    invoke-direct {p0}, Lcom/qf/clientsdk/QFTunerManager;->getServer()Lcom/qf/clientsdk/tuner/ITuner;

    move-result-object v0

    if-eqz v0, :cond_0

    .line 232
    invoke-direct {p0}, Lcom/qf/clientsdk/QFTunerManager;->getServer()Lcom/qf/clientsdk/tuner/ITuner;

    move-result-object v0

    invoke-interface {v0}, Lcom/qf/clientsdk/tuner/ITuner;->setRdsAFSwitch()[B

    move-result-object v0

    invoke-direct {p0, v0}, Lcom/qf/clientsdk/QFTunerManager;->sendCmds([B)V

    :cond_0
    return-void
.end method

.method public setRdsPtyType(B)V
    .locals 1

    .line 213
    invoke-direct {p0}, Lcom/qf/clientsdk/QFTunerManager;->getServer()Lcom/qf/clientsdk/tuner/ITuner;

    move-result-object v0

    if-eqz v0, :cond_0

    .line 214
    invoke-direct {p0}, Lcom/qf/clientsdk/QFTunerManager;->getServer()Lcom/qf/clientsdk/tuner/ITuner;

    move-result-object v0

    invoke-interface {v0, p1}, Lcom/qf/clientsdk/tuner/ITuner;->setRdsPtyType(B)[B

    move-result-object p1

    invoke-direct {p0, p1}, Lcom/qf/clientsdk/QFTunerManager;->sendCmds([B)V

    :cond_0
    return-void
.end method

.method public setRdsSwitch(B)V
    .locals 1

    .line 202
    invoke-direct {p0}, Lcom/qf/clientsdk/QFTunerManager;->getServer()Lcom/qf/clientsdk/tuner/ITuner;

    move-result-object v0

    if-eqz v0, :cond_0

    .line 203
    invoke-direct {p0}, Lcom/qf/clientsdk/QFTunerManager;->getServer()Lcom/qf/clientsdk/tuner/ITuner;

    move-result-object v0

    invoke-interface {v0, p1}, Lcom/qf/clientsdk/tuner/ITuner;->setRdsSwitch(B)[B

    move-result-object p1

    invoke-direct {p0, p1}, Lcom/qf/clientsdk/QFTunerManager;->sendCmds([B)V

    :cond_0
    return-void
.end method

.method public setRdsTASwitch()V
    .locals 1

    .line 222
    invoke-direct {p0}, Lcom/qf/clientsdk/QFTunerManager;->getServer()Lcom/qf/clientsdk/tuner/ITuner;

    move-result-object v0

    if-eqz v0, :cond_0

    .line 223
    invoke-direct {p0}, Lcom/qf/clientsdk/QFTunerManager;->getServer()Lcom/qf/clientsdk/tuner/ITuner;

    move-result-object v0

    invoke-interface {v0}, Lcom/qf/clientsdk/tuner/ITuner;->setRdsTASwitch()[B

    move-result-object v0

    invoke-direct {p0, v0}, Lcom/qf/clientsdk/QFTunerManager;->sendCmds([B)V

    :cond_0
    return-void
.end method

.method public setTunerScanning(Z)V
    .locals 0

    .line 405
    iput-boolean p1, p0, Lcom/qf/clientsdk/QFTunerManager;->tunerScanning:Z

    return-void
.end method

.method public setTunerTool(Lcom/qf/clientsdk/listeners/ITunerTool;)V
    .locals 1

    .line 39
    iget-object v0, p0, Lcom/qf/clientsdk/QFTunerManager;->mITunerToolList:Ljava/util/concurrent/CopyOnWriteArrayList;

    invoke-virtual {v0, p1}, Ljava/util/concurrent/CopyOnWriteArrayList;->contains(Ljava/lang/Object;)Z

    move-result v0

    if-nez v0, :cond_0

    .line 40
    iget-object v0, p0, Lcom/qf/clientsdk/QFTunerManager;->mITunerToolList:Ljava/util/concurrent/CopyOnWriteArrayList;

    invoke-virtual {v0, p1}, Ljava/util/concurrent/CopyOnWriteArrayList;->add(Ljava/lang/Object;)Z

    :cond_0
    return-void
.end method

.method public stopScan()V
    .locals 1

    .line 138
    invoke-direct {p0}, Lcom/qf/clientsdk/QFTunerManager;->getServer()Lcom/qf/clientsdk/tuner/ITuner;

    move-result-object v0

    if-eqz v0, :cond_0

    .line 139
    invoke-direct {p0}, Lcom/qf/clientsdk/QFTunerManager;->getServer()Lcom/qf/clientsdk/tuner/ITuner;

    move-result-object v0

    invoke-interface {v0}, Lcom/qf/clientsdk/tuner/ITuner;->stopScan()[B

    move-result-object v0

    invoke-direct {p0, v0}, Lcom/qf/clientsdk/QFTunerManager;->sendCmds([B)V

    :cond_0
    return-void
.end method

.method public tuneExt(BBBI)V
    .locals 1

    .line 87
    invoke-direct {p0}, Lcom/qf/clientsdk/QFTunerManager;->getServer()Lcom/qf/clientsdk/tuner/ITuner;

    move-result-object v0

    if-eqz v0, :cond_0

    .line 88
    invoke-direct {p0}, Lcom/qf/clientsdk/QFTunerManager;->getServer()Lcom/qf/clientsdk/tuner/ITuner;

    move-result-object v0

    invoke-interface {v0, p1, p2, p3, p4}, Lcom/qf/clientsdk/tuner/ITuner;->tuneExt(BBBI)[B

    move-result-object p1

    invoke-direct {p0, p1}, Lcom/qf/clientsdk/QFTunerManager;->sendCmds([B)V

    :cond_0
    return-void
.end method

.method public unMute()Z
    .locals 1

    .line 260
    invoke-direct {p0}, Lcom/qf/clientsdk/QFTunerManager;->getServer()Lcom/qf/clientsdk/tuner/ITuner;

    const/4 v0, 0x0

    return v0
.end method
