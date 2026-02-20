.class public Lcom/qf/clientsdk/DefaultMcuProessor;
.super Lcom/qf/clientsdk/AbsMcuProessor;
.source "DefaultMcuProessor.java"

# interfaces
.implements Lcom/qf/clientsdk/IMcuSender;


# direct methods
.method public constructor <init>(Landroid/content/Context;)V
    .locals 0

    .line 18
    invoke-direct {p0, p1}, Lcom/qf/clientsdk/AbsMcuProessor;-><init>(Landroid/content/Context;)V

    return-void
.end method


# virtual methods
.method getHandBrakeState()Z
    .locals 3

    .line 60
    invoke-static {}, Lcom/qf/clientsdk/utils/SettingsGlobalDBProxy;->getInstance()Lcom/qf/clientsdk/utils/SettingsGlobalDBProxy;

    move-result-object v0

    const/4 v1, 0x1

    const-string v2, "qf.vehilce.handbrake"

    invoke-virtual {v0, v2, v1}, Lcom/qf/clientsdk/utils/SettingsGlobalDBProxy;->getInt(Ljava/lang/String;I)I

    move-result v0

    if-ne v0, v1, :cond_0

    goto :goto_0

    :cond_0
    const/4 v1, 0x0

    :goto_0
    return v1
.end method

.method getSeerKeyType()I
    .locals 3

    .line 65
    invoke-static {}, Lcom/qf/clientsdk/utils/SettingsGlobalDBProxy;->getInstance()Lcom/qf/clientsdk/utils/SettingsGlobalDBProxy;

    move-result-object v0

    const-string v1, "qf.vehilce.steerType"

    const/4 v2, 0x1

    invoke-virtual {v0, v1, v2}, Lcom/qf/clientsdk/utils/SettingsGlobalDBProxy;->getInt(Ljava/lang/String;I)I

    move-result v0

    return v0
.end method

.method getpowerAmpVol()I
    .locals 3

    .line 96
    invoke-static {}, Lcom/qf/clientsdk/utils/SettingsGlobalDBProxy;->getInstance()Lcom/qf/clientsdk/utils/SettingsGlobalDBProxy;

    move-result-object v0

    const-string v1, "qf.vehilce.ampVol"

    const/4 v2, 0x0

    invoke-virtual {v0, v1, v2}, Lcom/qf/clientsdk/utils/SettingsGlobalDBProxy;->getInt(Ljava/lang/String;I)I

    move-result v0

    return v0
.end method

.method public onMcuInfoChanged([B)V
    .locals 4

    const/4 v0, 0x0

    .line 23
    aget-byte v1, p1, v0

    and-int/lit16 v1, v1, 0xff

    .line 24
    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2}, Ljava/lang/StringBuilder;-><init>()V

    const-string v3, "receive mcu data: "

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-static {p1}, Lcom/qf/clientsdk/utils/ByteTool;->ByteToString([B)Ljava/lang/String;

    move-result-object v3

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v2

    const-string v3, "DefaultMcuProessor"

    invoke-static {v3, v2}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    const/16 v2, 0x19

    if-eq v1, v2, :cond_4

    const/16 v2, 0x22

    const/4 v3, 0x1

    if-eq v1, v2, :cond_2

    const/16 v2, 0x23

    if-eq v1, v2, :cond_0

    packed-switch v1, :pswitch_data_0

    goto :goto_0

    .line 46
    :pswitch_0
    invoke-static {}, Lcom/qf/clientsdk/QFTunerManager;->getInstance()Lcom/qf/clientsdk/QFTunerManager;

    move-result-object v0

    invoke-virtual {v0}, Lcom/qf/clientsdk/QFTunerManager;->getTunerPresenter()Lcom/qf/clientsdk/presenter/TunerPresenter;

    move-result-object v0

    if-eqz v0, :cond_4

    .line 48
    invoke-virtual {v0, p1}, Lcom/qf/clientsdk/presenter/TunerPresenter;->handleTunerRawData([B)V

    goto :goto_0

    .line 30
    :cond_0
    invoke-static {}, Lcom/qf/clientsdk/QFVehicleManager;->getInstance()Lcom/qf/clientsdk/QFVehicleManager;

    move-result-object v1

    aget-byte p1, p1, v3

    if-ne p1, v3, :cond_1

    move v0, v3

    :cond_1
    invoke-virtual {v1, v0}, Lcom/qf/clientsdk/QFVehicleManager;->notifyHandBrake(Z)V

    goto :goto_0

    .line 34
    :cond_2
    invoke-static {}, Lcom/qf/clientsdk/QFVehicleManager;->getInstance()Lcom/qf/clientsdk/QFVehicleManager;

    move-result-object v1

    aget-byte p1, p1, v3

    if-nez p1, :cond_3

    move v0, v3

    :cond_3
    invoke-virtual {v1, v0}, Lcom/qf/clientsdk/QFVehicleManager;->notifyHandLight(Z)V

    :cond_4
    :goto_0
    return-void

    :pswitch_data_0
    .packed-switch 0xb0
        :pswitch_0
        :pswitch_0
        :pswitch_0
        :pswitch_0
        :pswitch_0
        :pswitch_0
        :pswitch_0
        :pswitch_0
        :pswitch_0
    .end packed-switch
.end method

.method public resetAudioSettings()V
    .locals 4

    const/4 v0, 0x1

    new-array v0, v0, [B

    const/4 v1, 0x0

    const/4 v2, 0x6

    aput-byte v2, v0, v1

    .line 161
    iget-object v1, p0, Lcom/qf/clientsdk/DefaultMcuProessor;->mContext:Landroid/content/Context;

    const-string v2, "mcu_service"

    invoke-virtual {v1, v2}, Landroid/content/Context;->getSystemService(Ljava/lang/String;)Ljava/lang/Object;

    move-result-object v1

    check-cast v1, Landroid/qf/mcu/McuManager;

    const/16 v2, 0x18

    .line 163
    :try_start_0
    array-length v3, v0

    invoke-virtual {v1, v2, v0, v3}, Landroid/qf/mcu/McuManager;->RPC_SendMcuMsgData(B[BI)V
    :try_end_0
    .catch Landroid/os/RemoteException; {:try_start_0 .. :try_end_0} :catch_0

    goto :goto_0

    :catch_0
    move-exception v0

    .line 166
    invoke-virtual {v0}, Landroid/os/RemoteException;->printStackTrace()V

    :goto_0
    return-void
.end method

.method public setArmReboot2Mcu()V
    .locals 4

    const/4 v0, 0x1

    new-array v0, v0, [B

    const/4 v1, 0x0

    const/4 v2, 0x3

    aput-byte v2, v0, v1

    .line 130
    iget-object v1, p0, Lcom/qf/clientsdk/DefaultMcuProessor;->mContext:Landroid/content/Context;

    const-string v2, "mcu_service"

    invoke-virtual {v1, v2}, Landroid/content/Context;->getSystemService(Ljava/lang/String;)Ljava/lang/Object;

    move-result-object v1

    check-cast v1, Landroid/qf/mcu/McuManager;

    const/16 v2, 0x18

    .line 132
    :try_start_0
    array-length v3, v0

    invoke-virtual {v1, v2, v0, v3}, Landroid/qf/mcu/McuManager;->RPC_SendMcuMsgData(B[BI)V
    :try_end_0
    .catch Landroid/os/RemoteException; {:try_start_0 .. :try_end_0} :catch_0

    goto :goto_0

    :catch_0
    move-exception v0

    .line 135
    invoke-virtual {v0}, Landroid/os/RemoteException;->printStackTrace()V

    :goto_0
    return-void
.end method

.method public setCanUartBps(I)V
    .locals 3

    const/4 v0, 0x2

    new-array v0, v0, [B

    const/4 v1, 0x0

    const/4 v2, 0x5

    aput-byte v2, v0, v1

    int-to-byte p1, p1

    const/4 v1, 0x1

    aput-byte p1, v0, v1

    .line 146
    iget-object p1, p0, Lcom/qf/clientsdk/DefaultMcuProessor;->mContext:Landroid/content/Context;

    const-string v1, "mcu_service"

    invoke-virtual {p1, v1}, Landroid/content/Context;->getSystemService(Ljava/lang/String;)Ljava/lang/Object;

    move-result-object p1

    check-cast p1, Landroid/qf/mcu/McuManager;

    const/16 v1, 0x18

    .line 148
    :try_start_0
    array-length v2, v0

    invoke-virtual {p1, v1, v0, v2}, Landroid/qf/mcu/McuManager;->RPC_SendMcuMsgData(B[BI)V
    :try_end_0
    .catch Landroid/os/RemoteException; {:try_start_0 .. :try_end_0} :catch_0

    goto :goto_0

    :catch_0
    move-exception p1

    .line 151
    invoke-virtual {p1}, Landroid/os/RemoteException;->printStackTrace()V

    :goto_0
    return-void
.end method

.method public setPowerAmpVol(I)V
    .locals 3

    const/4 v0, 0x2

    new-array v1, v0, [B

    const/4 v2, 0x0

    aput-byte v0, v1, v2

    int-to-byte p1, p1

    const/4 v0, 0x1

    aput-byte p1, v1, v0

    .line 104
    iget-object p1, p0, Lcom/qf/clientsdk/DefaultMcuProessor;->mContext:Landroid/content/Context;

    const-string v0, "mcu_service"

    invoke-virtual {p1, v0}, Landroid/content/Context;->getSystemService(Ljava/lang/String;)Ljava/lang/Object;

    move-result-object p1

    check-cast p1, Landroid/qf/mcu/McuManager;

    const/16 v0, 0x18

    .line 106
    :try_start_0
    array-length v2, v1

    invoke-virtual {p1, v0, v1, v2}, Landroid/qf/mcu/McuManager;->RPC_SendMcuMsgData(B[BI)V
    :try_end_0
    .catch Landroid/os/RemoteException; {:try_start_0 .. :try_end_0} :catch_0

    goto :goto_0

    :catch_0
    move-exception p1

    .line 109
    invoke-virtual {p1}, Landroid/os/RemoteException;->printStackTrace()V

    :goto_0
    return-void
.end method

.method public setSteerKeyType(I)V
    .locals 5

    const/4 v0, 0x2

    new-array v0, v0, [B

    const/4 v1, 0x0

    const/4 v2, 0x1

    aput-byte v2, v0, v1

    int-to-byte p1, p1

    aput-byte p1, v0, v2

    .line 73
    iget-object p1, p0, Lcom/qf/clientsdk/DefaultMcuProessor;->mContext:Landroid/content/Context;

    const-string v3, "mcu_service"

    invoke-virtual {p1, v3}, Landroid/content/Context;->getSystemService(Ljava/lang/String;)Ljava/lang/Object;

    move-result-object p1

    check-cast p1, Landroid/qf/mcu/McuManager;

    const/16 v3, 0x18

    .line 75
    :try_start_0
    array-length v4, v0

    invoke-virtual {p1, v3, v0, v4}, Landroid/qf/mcu/McuManager;->RPC_SendMcuMsgData(B[BI)V
    :try_end_0
    .catch Landroid/os/RemoteException; {:try_start_0 .. :try_end_0} :catch_0

    goto :goto_0

    :catch_0
    move-exception v0

    .line 78
    invoke-virtual {v0}, Landroid/os/RemoteException;->printStackTrace()V

    :goto_0
    new-array v0, v2, [B

    aput-byte v2, v0, v1

    const/16 v1, 0x19

    .line 87
    :try_start_1
    array-length v2, v0

    invoke-virtual {p1, v1, v0, v2}, Landroid/qf/mcu/McuManager;->RPC_SendMcuMsgData(B[BI)V
    :try_end_1
    .catch Landroid/os/RemoteException; {:try_start_1 .. :try_end_1} :catch_1

    goto :goto_1

    :catch_1
    move-exception p1

    .line 90
    invoke-virtual {p1}, Landroid/os/RemoteException;->printStackTrace()V

    :goto_1
    return-void
.end method
