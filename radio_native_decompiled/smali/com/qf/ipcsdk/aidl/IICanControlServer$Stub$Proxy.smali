.class Lcom/qf/ipcsdk/aidl/IICanControlServer$Stub$Proxy;
.super Ljava/lang/Object;
.source "IICanControlServer.java"

# interfaces
.implements Lcom/qf/ipcsdk/aidl/IICanControlServer;


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Lcom/qf/ipcsdk/aidl/IICanControlServer$Stub;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0xa
    name = "Proxy"
.end annotation


# static fields
.field public static sDefaultImpl:Lcom/qf/ipcsdk/aidl/IICanControlServer;


# instance fields
.field private mRemote:Landroid/os/IBinder;


# direct methods
.method constructor <init>(Landroid/os/IBinder;)V
    .locals 0

    .line 78
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 79
    iput-object p1, p0, Lcom/qf/ipcsdk/aidl/IICanControlServer$Stub$Proxy;->mRemote:Landroid/os/IBinder;

    return-void
.end method


# virtual methods
.method public asBinder()Landroid/os/IBinder;
    .locals 1

    .line 83
    iget-object v0, p0, Lcom/qf/ipcsdk/aidl/IICanControlServer$Stub$Proxy;->mRemote:Landroid/os/IBinder;

    return-object v0
.end method

.method public getInterfaceDescriptor()Ljava/lang/String;
    .locals 1

    const-string v0, "com.qf.ipcsdk.aidl.IICanControlServer"

    return-object v0
.end method

.method public onCanTellMeHandBrake(Z)V
    .locals 5
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Landroid/os/RemoteException;
        }
    .end annotation

    .line 94
    invoke-static {}, Landroid/os/Parcel;->obtain()Landroid/os/Parcel;

    move-result-object v0

    .line 95
    invoke-static {}, Landroid/os/Parcel;->obtain()Landroid/os/Parcel;

    move-result-object v1

    :try_start_0
    const-string v2, "com.qf.ipcsdk.aidl.IICanControlServer"

    .line 97
    invoke-virtual {v0, v2}, Landroid/os/Parcel;->writeInterfaceToken(Ljava/lang/String;)V

    const/4 v2, 0x1

    const/4 v3, 0x0

    if-eqz p1, :cond_0

    move v4, v2

    goto :goto_0

    :cond_0
    move v4, v3

    .line 98
    :goto_0
    invoke-virtual {v0, v4}, Landroid/os/Parcel;->writeInt(I)V

    .line 99
    iget-object v4, p0, Lcom/qf/ipcsdk/aidl/IICanControlServer$Stub$Proxy;->mRemote:Landroid/os/IBinder;

    invoke-interface {v4, v2, v0, v1, v3}, Landroid/os/IBinder;->transact(ILandroid/os/Parcel;Landroid/os/Parcel;I)Z

    move-result v2

    if-nez v2, :cond_1

    .line 100
    invoke-static {}, Lcom/qf/ipcsdk/aidl/IICanControlServer$Stub;->getDefaultImpl()Lcom/qf/ipcsdk/aidl/IICanControlServer;

    move-result-object v2

    if-eqz v2, :cond_1

    .line 101
    invoke-static {}, Lcom/qf/ipcsdk/aidl/IICanControlServer$Stub;->getDefaultImpl()Lcom/qf/ipcsdk/aidl/IICanControlServer;

    move-result-object v2

    invoke-interface {v2, p1}, Lcom/qf/ipcsdk/aidl/IICanControlServer;->onCanTellMeHandBrake(Z)V
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 107
    invoke-virtual {v1}, Landroid/os/Parcel;->recycle()V

    .line 108
    invoke-virtual {v0}, Landroid/os/Parcel;->recycle()V

    return-void

    .line 104
    :cond_1
    :try_start_1
    invoke-virtual {v1}, Landroid/os/Parcel;->readException()V
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    .line 107
    invoke-virtual {v1}, Landroid/os/Parcel;->recycle()V

    .line 108
    invoke-virtual {v0}, Landroid/os/Parcel;->recycle()V

    return-void

    :catchall_0
    move-exception p1

    .line 107
    invoke-virtual {v1}, Landroid/os/Parcel;->recycle()V

    .line 108
    invoke-virtual {v0}, Landroid/os/Parcel;->recycle()V

    .line 109
    throw p1
.end method
