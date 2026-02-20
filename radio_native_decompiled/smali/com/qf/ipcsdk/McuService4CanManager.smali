.class public Lcom/qf/ipcsdk/McuService4CanManager;
.super Ljava/lang/Object;
.source "McuService4CanManager.java"


# static fields
.field static TAG:Ljava/lang/String; = "McuService4CanManager"


# direct methods
.method static constructor <clinit>()V
    .locals 0

    return-void
.end method

.method public constructor <init>()V
    .locals 0

    .line 11
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

.method protected static getMcu4CanService(Landroid/content/Context;)Lcom/qf/ipcsdk/aidl/IICanControlServer;
    .locals 2

    const-string v0, "mcu_service"

    .line 16
    invoke-virtual {p0, v0}, Landroid/content/Context;->getSystemService(Ljava/lang/String;)Ljava/lang/Object;

    move-result-object p0

    check-cast p0, Landroid/qf/mcu/McuManager;

    const/4 v0, 0x0

    if-eqz p0, :cond_0

    :try_start_0
    const-string v1, "server4Can"

    .line 20
    invoke-virtual {p0, v1}, Landroid/qf/mcu/McuManager;->getServiceByName(Ljava/lang/String;)Landroid/os/IBinder;

    move-result-object p0
    :try_end_0
    .catch Landroid/os/RemoteException; {:try_start_0 .. :try_end_0} :catch_0

    goto :goto_0

    :catch_0
    move-exception p0

    .line 23
    invoke-virtual {p0}, Landroid/os/RemoteException;->printStackTrace()V

    move-object p0, v0

    :goto_0
    if-eqz p0, :cond_0

    .line 26
    invoke-static {p0}, Lcom/qf/ipcsdk/aidl/IICanControlServer$Stub;->asInterface(Landroid/os/IBinder;)Lcom/qf/ipcsdk/aidl/IICanControlServer;

    move-result-object p0

    return-object p0

    :cond_0
    return-object v0
.end method

.method public static notifyMcuServiceHandBrake(Landroid/content/Context;Z)V
    .locals 0

    .line 38
    invoke-static {p0}, Lcom/qf/ipcsdk/McuService4CanManager;->getMcu4CanService(Landroid/content/Context;)Lcom/qf/ipcsdk/aidl/IICanControlServer;

    move-result-object p0

    if-eqz p0, :cond_0

    .line 42
    :try_start_0
    invoke-interface {p0, p1}, Lcom/qf/ipcsdk/aidl/IICanControlServer;->onCanTellMeHandBrake(Z)V
    :try_end_0
    .catch Landroid/os/RemoteException; {:try_start_0 .. :try_end_0} :catch_0

    goto :goto_0

    :catch_0
    move-exception p0

    .line 45
    invoke-virtual {p0}, Landroid/os/RemoteException;->printStackTrace()V

    goto :goto_0

    .line 48
    :cond_0
    sget-object p0, Lcom/qf/ipcsdk/McuService4CanManager;->TAG:Ljava/lang/String;

    const-string p1, "notifyMcuServiceHandBrake IICanControlServer NULL!"

    invoke-static {p0, p1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    :goto_0
    return-void
.end method
