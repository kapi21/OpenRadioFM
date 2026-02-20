.class public Lcom/qf/clientsdk/QFVehicleManager;
.super Lcom/qf/clientsdk/IBaseManager;
.source "QFVehicleManager.java"


# annotations
.annotation system Ldalvik/annotation/MemberClasses;
    value = {
        Lcom/qf/clientsdk/QFVehicleManager$DoCallback;
    }
.end annotation


# static fields
.field private static final TAG:Ljava/lang/String;

.field private static mManager:Lcom/qf/clientsdk/QFVehicleManager;


# instance fields
.field private mIVehicleToolList:Ljava/util/concurrent/CopyOnWriteArrayList;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Ljava/util/concurrent/CopyOnWriteArrayList<",
            "Lcom/qf/clientsdk/listeners/IVehicleTool;",
            ">;"
        }
    .end annotation
.end field


# direct methods
.method static constructor <clinit>()V
    .locals 1

    .line 14
    const-class v0, Lcom/qf/clientsdk/QFVehicleManager;

    invoke-virtual {v0}, Ljava/lang/Class;->getSimpleName()Ljava/lang/String;

    move-result-object v0

    sput-object v0, Lcom/qf/clientsdk/QFVehicleManager;->TAG:Ljava/lang/String;

    return-void
.end method

.method private constructor <init>()V
    .locals 1

    .line 16
    invoke-direct {p0}, Lcom/qf/clientsdk/IBaseManager;-><init>()V

    .line 21
    new-instance v0, Ljava/util/concurrent/CopyOnWriteArrayList;

    invoke-direct {v0}, Ljava/util/concurrent/CopyOnWriteArrayList;-><init>()V

    iput-object v0, p0, Lcom/qf/clientsdk/QFVehicleManager;->mIVehicleToolList:Ljava/util/concurrent/CopyOnWriteArrayList;

    return-void
.end method

.method private doCallback(Lcom/qf/clientsdk/QFVehicleManager$DoCallback;)V
    .locals 5

    .line 49
    iget-object v0, p0, Lcom/qf/clientsdk/QFVehicleManager;->mIVehicleToolList:Ljava/util/concurrent/CopyOnWriteArrayList;

    if-eqz v0, :cond_0

    .line 50
    sget-object v0, Lcom/qf/clientsdk/QFVehicleManager;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "doCallback - callback: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    const-string v2, " - mIVehicleToolList: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-object v2, p0, Lcom/qf/clientsdk/QFVehicleManager;->mIVehicleToolList:Ljava/util/concurrent/CopyOnWriteArrayList;

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    .line 51
    iget-object v0, p0, Lcom/qf/clientsdk/QFVehicleManager;->mIVehicleToolList:Ljava/util/concurrent/CopyOnWriteArrayList;

    invoke-virtual {v0}, Ljava/util/concurrent/CopyOnWriteArrayList;->iterator()Ljava/util/Iterator;

    move-result-object v0

    :goto_0
    invoke-interface {v0}, Ljava/util/Iterator;->hasNext()Z

    move-result v1

    if-eqz v1, :cond_0

    invoke-interface {v0}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v1

    check-cast v1, Lcom/qf/clientsdk/listeners/IVehicleTool;

    .line 52
    sget-object v2, Lcom/qf/clientsdk/QFVehicleManager;->TAG:Ljava/lang/String;

    new-instance v3, Ljava/lang/StringBuilder;

    invoke-direct {v3}, Ljava/lang/StringBuilder;-><init>()V

    const-string v4, "doCallback - listener: "

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v3, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    invoke-virtual {v3}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v3

    invoke-static {v2, v3}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    .line 53
    invoke-interface {p1, v1}, Lcom/qf/clientsdk/QFVehicleManager$DoCallback;->doLocalCallback(Lcom/qf/clientsdk/listeners/IVehicleTool;)V

    goto :goto_0

    :cond_0
    return-void
.end method

.method public static getInstance()Lcom/qf/clientsdk/QFVehicleManager;
    .locals 2

    .line 24
    sget-object v0, Lcom/qf/clientsdk/QFVehicleManager;->mManager:Lcom/qf/clientsdk/QFVehicleManager;

    if-nez v0, :cond_0

    .line 25
    const-class v0, Lcom/qf/clientsdk/QFVehicleManager;

    monitor-enter v0

    .line 26
    :try_start_0
    new-instance v1, Lcom/qf/clientsdk/QFVehicleManager;

    invoke-direct {v1}, Lcom/qf/clientsdk/QFVehicleManager;-><init>()V

    sput-object v1, Lcom/qf/clientsdk/QFVehicleManager;->mManager:Lcom/qf/clientsdk/QFVehicleManager;

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
    sget-object v0, Lcom/qf/clientsdk/QFVehicleManager;->mManager:Lcom/qf/clientsdk/QFVehicleManager;

    return-object v0
.end method


# virtual methods
.method public getHandBrakeState()Z
    .locals 1

    .line 90
    iget-object v0, p0, Lcom/qf/clientsdk/QFVehicleManager;->mAbsMcuProessor:Lcom/qf/clientsdk/AbsMcuProessor;

    invoke-virtual {v0}, Lcom/qf/clientsdk/AbsMcuProessor;->getHandBrakeState()Z

    move-result v0

    return v0
.end method

.method public getHeadLightState()Z
    .locals 2

    const-string v0, "sys.qf.vehicle.headlight_state"

    const/4 v1, 0x0

    .line 110
    invoke-static {v0, v1}, Landroid/os/SystemProperties;->getBoolean(Ljava/lang/String;Z)Z

    move-result v0

    return v0
.end method

.method public getPowerAmpVol()I
    .locals 1

    .line 106
    iget-object v0, p0, Lcom/qf/clientsdk/QFVehicleManager;->mAbsMcuProessor:Lcom/qf/clientsdk/AbsMcuProessor;

    invoke-virtual {v0}, Lcom/qf/clientsdk/AbsMcuProessor;->getpowerAmpVol()I

    move-result v0

    return v0
.end method

.method public getSeerKeyType()Lcom/qf/clientsdk/pojo/STEERTYPE;
    .locals 1

    .line 98
    iget-object v0, p0, Lcom/qf/clientsdk/QFVehicleManager;->mAbsMcuProessor:Lcom/qf/clientsdk/AbsMcuProessor;

    invoke-virtual {v0}, Lcom/qf/clientsdk/AbsMcuProessor;->getSeerKeyType()I

    move-result v0

    invoke-static {v0}, Lcom/qf/clientsdk/pojo/STEERTYPE;->toSteerType(I)Lcom/qf/clientsdk/pojo/STEERTYPE;

    move-result-object v0

    return-object v0
.end method

.method protected notifyHandBrake(Z)V
    .locals 1

    .line 67
    new-instance v0, Lcom/qf/clientsdk/QFVehicleManager$1;

    invoke-direct {v0, p0, p1}, Lcom/qf/clientsdk/QFVehicleManager$1;-><init>(Lcom/qf/clientsdk/QFVehicleManager;Z)V

    invoke-direct {p0, v0}, Lcom/qf/clientsdk/QFVehicleManager;->doCallback(Lcom/qf/clientsdk/QFVehicleManager$DoCallback;)V

    return-void
.end method

.method protected notifyHandLight(Z)V
    .locals 1

    .line 81
    new-instance v0, Lcom/qf/clientsdk/QFVehicleManager$2;

    invoke-direct {v0, p0, p1}, Lcom/qf/clientsdk/QFVehicleManager$2;-><init>(Lcom/qf/clientsdk/QFVehicleManager;Z)V

    invoke-direct {p0, v0}, Lcom/qf/clientsdk/QFVehicleManager;->doCallback(Lcom/qf/clientsdk/QFVehicleManager$DoCallback;)V

    return-void
.end method

.method public registerVehicleTool(Lcom/qf/clientsdk/listeners/IVehicleTool;)V
    .locals 3

    .line 33
    iget-object v0, p0, Lcom/qf/clientsdk/QFVehicleManager;->mIVehicleToolList:Ljava/util/concurrent/CopyOnWriteArrayList;

    invoke-virtual {v0, p1}, Ljava/util/concurrent/CopyOnWriteArrayList;->contains(Ljava/lang/Object;)Z

    move-result v0

    if-nez v0, :cond_0

    .line 34
    sget-object v0, Lcom/qf/clientsdk/QFVehicleManager;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "registerVehicleTool - tool: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    .line 35
    :cond_0
    iget-object v0, p0, Lcom/qf/clientsdk/QFVehicleManager;->mIVehicleToolList:Ljava/util/concurrent/CopyOnWriteArrayList;

    invoke-virtual {v0, p1}, Ljava/util/concurrent/CopyOnWriteArrayList;->add(Ljava/lang/Object;)Z

    return-void
.end method

.method public setPowerAmpVol(I)V
    .locals 1

    .line 102
    iget-object v0, p0, Lcom/qf/clientsdk/QFVehicleManager;->mMcuSender:Lcom/qf/clientsdk/IMcuSender;

    invoke-interface {v0, p1}, Lcom/qf/clientsdk/IMcuSender;->setPowerAmpVol(I)V

    return-void
.end method

.method public setSteerKeyType(Lcom/qf/clientsdk/pojo/STEERTYPE;)V
    .locals 1

    .line 94
    iget-object v0, p0, Lcom/qf/clientsdk/QFVehicleManager;->mMcuSender:Lcom/qf/clientsdk/IMcuSender;

    invoke-virtual {p1}, Lcom/qf/clientsdk/pojo/STEERTYPE;->toIntType()I

    move-result p1

    invoke-interface {v0, p1}, Lcom/qf/clientsdk/IMcuSender;->setSteerKeyType(I)V

    return-void
.end method

.method public unRegisterVehicleTool(Lcom/qf/clientsdk/listeners/IVehicleTool;)V
    .locals 3

    .line 39
    iget-object v0, p0, Lcom/qf/clientsdk/QFVehicleManager;->mIVehicleToolList:Ljava/util/concurrent/CopyOnWriteArrayList;

    invoke-virtual {v0, p1}, Ljava/util/concurrent/CopyOnWriteArrayList;->contains(Ljava/lang/Object;)Z

    move-result v0

    if-eqz v0, :cond_0

    .line 40
    sget-object v0, Lcom/qf/clientsdk/QFVehicleManager;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "unRegisterVehicleTool - tool: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    .line 41
    :cond_0
    iget-object v0, p0, Lcom/qf/clientsdk/QFVehicleManager;->mIVehicleToolList:Ljava/util/concurrent/CopyOnWriteArrayList;

    invoke-virtual {v0, p1}, Ljava/util/concurrent/CopyOnWriteArrayList;->remove(Ljava/lang/Object;)Z

    return-void
.end method
