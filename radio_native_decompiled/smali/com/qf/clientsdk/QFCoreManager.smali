.class public Lcom/qf/clientsdk/QFCoreManager;
.super Ljava/lang/Object;
.source "QFCoreManager.java"


# static fields
.field public static final TRANSACT_ONLY_REQUESPERMISS:I = 0x3e9

.field public static mCoreManager:Lcom/qf/clientsdk/QFCoreManager;


# instance fields
.field private mAbsMcuProessor:Lcom/qf/clientsdk/AbsMcuProessor;

.field private mContext:Landroid/content/Context;

.field private mParam:Lcom/qf/clientsdk/ClientSdkParam;


# direct methods
.method public constructor <init>()V
    .locals 1

    .line 13
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    const/4 v0, 0x0

    .line 17
    iput-object v0, p0, Lcom/qf/clientsdk/QFCoreManager;->mParam:Lcom/qf/clientsdk/ClientSdkParam;

    return-void
.end method

.method public static getInstance()Lcom/qf/clientsdk/QFCoreManager;
    .locals 2

    .line 24
    sget-object v0, Lcom/qf/clientsdk/QFCoreManager;->mCoreManager:Lcom/qf/clientsdk/QFCoreManager;

    if-nez v0, :cond_0

    .line 25
    const-class v0, Lcom/qf/clientsdk/QFCoreManager;

    monitor-enter v0

    .line 26
    :try_start_0
    new-instance v1, Lcom/qf/clientsdk/QFCoreManager;

    invoke-direct {v1}, Lcom/qf/clientsdk/QFCoreManager;-><init>()V

    sput-object v1, Lcom/qf/clientsdk/QFCoreManager;->mCoreManager:Lcom/qf/clientsdk/QFCoreManager;

    .line 27
    monitor-exit v0

    goto :goto_0

    :catchall_0
    move-exception v1

    monitor-exit v0
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    throw v1

    .line 29
    :cond_0
    :goto_0
    sget-object v0, Lcom/qf/clientsdk/QFCoreManager;->mCoreManager:Lcom/qf/clientsdk/QFCoreManager;

    return-object v0
.end method

.method public static getMcuManagerService()Landroid/qf/mcu/IMcuManager;
    .locals 1

    const-string v0, "mcu_service"

    .line 90
    invoke-static {v0}, Landroid/os/ServiceManager;->getService(Ljava/lang/String;)Landroid/os/IBinder;

    move-result-object v0

    invoke-static {v0}, Landroid/qf/mcu/IMcuManager$Stub;->asInterface(Landroid/os/IBinder;)Landroid/qf/mcu/IMcuManager;

    move-result-object v0

    return-object v0
.end method

.method private requesPermission(Ljava/lang/String;)V
    .locals 6

    const-string v0, "QFCoreManager writeInterfaceToken="

    .line 67
    invoke-static {}, Lcom/qf/clientsdk/QFCoreManager;->getMcuManagerService()Landroid/qf/mcu/IMcuManager;

    move-result-object v1

    if-eqz v1, :cond_0

    .line 69
    invoke-static {}, Landroid/os/Parcel;->obtain()Landroid/os/Parcel;

    move-result-object v2

    .line 70
    invoke-static {}, Landroid/os/Parcel;->obtain()Landroid/os/Parcel;

    move-result-object v3

    .line 73
    :try_start_0
    new-instance v4, Ljava/lang/StringBuilder;

    invoke-direct {v4}, Ljava/lang/StringBuilder;-><init>()V

    invoke-virtual {v4, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-interface {v1}, Landroid/qf/mcu/IMcuManager;->asBinder()Landroid/os/IBinder;

    move-result-object v5

    invoke-interface {v5}, Landroid/os/IBinder;->getInterfaceDescriptor()Ljava/lang/String;

    move-result-object v5

    invoke-virtual {v4, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v4}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v4

    invoke-static {v4}, Lcom/qf/clientsdk/utils/LogUtils;->clzLog(Ljava/lang/String;)V

    .line 74
    invoke-interface {v1}, Landroid/qf/mcu/IMcuManager;->asBinder()Landroid/os/IBinder;

    move-result-object v4

    invoke-interface {v4}, Landroid/os/IBinder;->getInterfaceDescriptor()Ljava/lang/String;

    move-result-object v4

    invoke-virtual {v2, v4}, Landroid/os/Parcel;->writeInterfaceToken(Ljava/lang/String;)V

    .line 75
    invoke-virtual {v2, p1}, Landroid/os/Parcel;->writeString(Ljava/lang/String;)V

    .line 76
    invoke-interface {v1}, Landroid/qf/mcu/IMcuManager;->asBinder()Landroid/os/IBinder;

    move-result-object p1

    const/16 v1, 0x3e9

    const/4 v4, 0x0

    invoke-interface {p1, v1, v2, v3, v4}, Landroid/os/IBinder;->transact(ILandroid/os/Parcel;Landroid/os/Parcel;I)Z
    :try_end_0
    .catch Landroid/os/RemoteException; {:try_start_0 .. :try_end_0} :catch_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    goto :goto_0

    :catchall_0
    move-exception p1

    goto :goto_1

    :catch_0
    move-exception p1

    .line 78
    :try_start_1
    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    invoke-virtual {v1, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    invoke-static {v0}, Lcom/qf/clientsdk/utils/LogUtils;->clzLog(Ljava/lang/String;)V

    .line 79
    invoke-virtual {p1}, Landroid/os/RemoteException;->printStackTrace()V
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    .line 83
    :goto_0
    invoke-virtual {v3}, Landroid/os/Parcel;->recycle()V

    .line 84
    invoke-virtual {v2}, Landroid/os/Parcel;->recycle()V

    goto :goto_2

    .line 83
    :goto_1
    invoke-virtual {v3}, Landroid/os/Parcel;->recycle()V

    .line 84
    invoke-virtual {v2}, Landroid/os/Parcel;->recycle()V

    .line 85
    throw p1

    :cond_0
    :goto_2
    return-void
.end method


# virtual methods
.method public getCoreParam()Lcom/qf/clientsdk/ClientSdkParam;
    .locals 1

    .line 63
    iget-object v0, p0, Lcom/qf/clientsdk/QFCoreManager;->mParam:Lcom/qf/clientsdk/ClientSdkParam;

    return-object v0
.end method

.method public initCoreManager(Lcom/qf/clientsdk/ClientSdkParam;)V
    .locals 4

    const-string v0, "mcuInfo"

    .line 33
    iget-object v1, p0, Lcom/qf/clientsdk/QFCoreManager;->mParam:Lcom/qf/clientsdk/ClientSdkParam;

    if-nez v1, :cond_1

    .line 34
    iput-object p1, p0, Lcom/qf/clientsdk/QFCoreManager;->mParam:Lcom/qf/clientsdk/ClientSdkParam;

    .line 35
    iget-object p1, p0, Lcom/qf/clientsdk/QFCoreManager;->mParam:Lcom/qf/clientsdk/ClientSdkParam;

    invoke-virtual {p1}, Lcom/qf/clientsdk/ClientSdkParam;->getContext()Landroid/content/Context;

    move-result-object p1

    iput-object p1, p0, Lcom/qf/clientsdk/QFCoreManager;->mContext:Landroid/content/Context;

    .line 37
    new-instance p1, Lcom/qf/clientsdk/DefaultMcuProessor;

    iget-object v1, p0, Lcom/qf/clientsdk/QFCoreManager;->mContext:Landroid/content/Context;

    invoke-direct {p1, v1}, Lcom/qf/clientsdk/DefaultMcuProessor;-><init>(Landroid/content/Context;)V

    .line 38
    iput-object p1, p0, Lcom/qf/clientsdk/QFCoreManager;->mAbsMcuProessor:Lcom/qf/clientsdk/AbsMcuProessor;

    .line 39
    iget-object v1, p0, Lcom/qf/clientsdk/QFCoreManager;->mParam:Lcom/qf/clientsdk/ClientSdkParam;

    iget-boolean v1, v1, Lcom/qf/clientsdk/ClientSdkParam;->isSetIMcuListener:Z

    if-eqz v1, :cond_0

    .line 41
    :try_start_0
    iget-object v1, p0, Lcom/qf/clientsdk/QFCoreManager;->mContext:Landroid/content/Context;

    const-string v2, "mcu_service"

    invoke-virtual {v1, v2}, Landroid/content/Context;->getSystemService(Ljava/lang/String;)Ljava/lang/Object;

    move-result-object v1

    check-cast v1, Landroid/qf/mcu/McuManager;

    .line 42
    iget-object v2, p0, Lcom/qf/clientsdk/QFCoreManager;->mAbsMcuProessor:Lcom/qf/clientsdk/AbsMcuProessor;

    invoke-virtual {v1, v2}, Landroid/qf/mcu/McuManager;->RPC_RequestMcuInfoChangedListener(Landroid/qf/mcu/McuListener;)V

    const-string v1, "\u8bbe\u7f6eMCU\u76d1\u542c"

    .line 43
    invoke-static {v0, v1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I
    :try_end_0
    .catch Landroid/os/RemoteException; {:try_start_0 .. :try_end_0} :catch_0

    goto :goto_0

    :catch_0
    move-exception v1

    .line 45
    invoke-virtual {v1}, Landroid/os/RemoteException;->printStackTrace()V

    .line 46
    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2}, Ljava/lang/StringBuilder;-><init>()V

    const-string v3, "\u8bbe\u7f6eMCU\u76d1\u542c\u5f02\u5e38: "

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Landroid/os/RemoteException;->getMessage()Ljava/lang/String;

    move-result-object v1

    invoke-virtual {v2, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    .line 50
    :cond_0
    :goto_0
    invoke-static {}, Lcom/qf/clientsdk/utils/SettingsGlobalDBProxy;->getInstance()Lcom/qf/clientsdk/utils/SettingsGlobalDBProxy;

    move-result-object v0

    iget-object v1, p0, Lcom/qf/clientsdk/QFCoreManager;->mContext:Landroid/content/Context;

    invoke-virtual {v0, v1}, Lcom/qf/clientsdk/utils/SettingsGlobalDBProxy;->initContext(Landroid/content/Context;)V

    .line 54
    iget-object v0, p0, Lcom/qf/clientsdk/QFCoreManager;->mContext:Landroid/content/Context;

    invoke-virtual {v0}, Landroid/content/Context;->getPackageName()Ljava/lang/String;

    move-result-object v0

    invoke-direct {p0, v0}, Lcom/qf/clientsdk/QFCoreManager;->requesPermission(Ljava/lang/String;)V

    .line 55
    invoke-static {}, Lcom/qf/clientsdk/QFVehicleManager;->getInstance()Lcom/qf/clientsdk/QFVehicleManager;

    move-result-object v0

    iget-object v1, p0, Lcom/qf/clientsdk/QFCoreManager;->mAbsMcuProessor:Lcom/qf/clientsdk/AbsMcuProessor;

    invoke-virtual {v0, v1, p1}, Lcom/qf/clientsdk/QFVehicleManager;->setUpMcuProessor(Lcom/qf/clientsdk/AbsMcuProessor;Lcom/qf/clientsdk/IMcuSender;)V

    .line 56
    invoke-static {}, Lcom/qf/clientsdk/QFSystemManager;->getInstance()Lcom/qf/clientsdk/QFSystemManager;

    move-result-object v0

    iget-object v1, p0, Lcom/qf/clientsdk/QFCoreManager;->mAbsMcuProessor:Lcom/qf/clientsdk/AbsMcuProessor;

    invoke-virtual {v0, v1, p1}, Lcom/qf/clientsdk/QFSystemManager;->setUpMcuProessor(Lcom/qf/clientsdk/AbsMcuProessor;Lcom/qf/clientsdk/IMcuSender;)V

    goto :goto_1

    :cond_1
    const-string p1, "QFCoreManager initCoreManager aready!"

    .line 58
    invoke-static {p1}, Lcom/qf/clientsdk/utils/LogUtils;->clzLog(Ljava/lang/String;)V

    :goto_1
    return-void
.end method
