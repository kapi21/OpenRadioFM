.class public Lcom/qf/clientsdk/QFSystemManager;
.super Lcom/qf/clientsdk/IBaseManager;
.source "QFSystemManager.java"


# static fields
.field public static final CAN_UART_BPS_115200:I = 0x1

.field public static final CAN_UART_BPS_19200:I = 0x2

.field public static final CAN_UART_BPS_38400:I

.field private static mManager:Lcom/qf/clientsdk/QFSystemManager;


# direct methods
.method public constructor <init>()V
    .locals 0

    .line 3
    invoke-direct {p0}, Lcom/qf/clientsdk/IBaseManager;-><init>()V

    return-void
.end method

.method public static getInstance()Lcom/qf/clientsdk/QFSystemManager;
    .locals 2

    .line 11
    sget-object v0, Lcom/qf/clientsdk/QFSystemManager;->mManager:Lcom/qf/clientsdk/QFSystemManager;

    if-nez v0, :cond_0

    .line 12
    const-class v0, Lcom/qf/clientsdk/QFSystemManager;

    monitor-enter v0

    .line 13
    :try_start_0
    new-instance v1, Lcom/qf/clientsdk/QFSystemManager;

    invoke-direct {v1}, Lcom/qf/clientsdk/QFSystemManager;-><init>()V

    sput-object v1, Lcom/qf/clientsdk/QFSystemManager;->mManager:Lcom/qf/clientsdk/QFSystemManager;

    .line 14
    monitor-exit v0

    goto :goto_0

    :catchall_0
    move-exception v1

    monitor-exit v0
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    throw v1

    .line 16
    :cond_0
    :goto_0
    sget-object v0, Lcom/qf/clientsdk/QFSystemManager;->mManager:Lcom/qf/clientsdk/QFSystemManager;

    return-object v0
.end method


# virtual methods
.method public callMcuArmRebootCmd()V
    .locals 1

    .line 20
    iget-object v0, p0, Lcom/qf/clientsdk/QFSystemManager;->mMcuSender:Lcom/qf/clientsdk/IMcuSender;

    invoke-interface {v0}, Lcom/qf/clientsdk/IMcuSender;->setArmReboot2Mcu()V

    return-void
.end method

.method public callMcuResetAudioSettings()V
    .locals 1

    .line 28
    iget-object v0, p0, Lcom/qf/clientsdk/QFSystemManager;->mMcuSender:Lcom/qf/clientsdk/IMcuSender;

    invoke-interface {v0}, Lcom/qf/clientsdk/IMcuSender;->resetAudioSettings()V

    return-void
.end method

.method public setCanUartBps(I)V
    .locals 1

    .line 24
    iget-object v0, p0, Lcom/qf/clientsdk/QFSystemManager;->mMcuSender:Lcom/qf/clientsdk/IMcuSender;

    invoke-interface {v0, p1}, Lcom/qf/clientsdk/IMcuSender;->setCanUartBps(I)V

    return-void
.end method
