.class public abstract Lcom/qf/ipcsdk/aidl/IICanControlServer$Stub;
.super Landroid/os/Binder;
.source "IICanControlServer.java"

# interfaces
.implements Lcom/qf/ipcsdk/aidl/IICanControlServer;


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Lcom/qf/ipcsdk/aidl/IICanControlServer;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x409
    name = "Stub"
.end annotation

.annotation system Ldalvik/annotation/MemberClasses;
    value = {
        Lcom/qf/ipcsdk/aidl/IICanControlServer$Stub$Proxy;
    }
.end annotation


# static fields
.field private static final DESCRIPTOR:Ljava/lang/String; = "com.qf.ipcsdk.aidl.IICanControlServer"

.field static final TRANSACTION_onCanTellMeHandBrake:I = 0x1


# direct methods
.method public constructor <init>()V
    .locals 1

    .line 27
    invoke-direct {p0}, Landroid/os/Binder;-><init>()V

    const-string v0, "com.qf.ipcsdk.aidl.IICanControlServer"

    .line 28
    invoke-virtual {p0, p0, v0}, Lcom/qf/ipcsdk/aidl/IICanControlServer$Stub;->attachInterface(Landroid/os/IInterface;Ljava/lang/String;)V

    return-void
.end method

.method public static asInterface(Landroid/os/IBinder;)Lcom/qf/ipcsdk/aidl/IICanControlServer;
    .locals 2

    if-nez p0, :cond_0

    const/4 p0, 0x0

    return-object p0

    :cond_0
    const-string v0, "com.qf.ipcsdk.aidl.IICanControlServer"

    .line 39
    invoke-interface {p0, v0}, Landroid/os/IBinder;->queryLocalInterface(Ljava/lang/String;)Landroid/os/IInterface;

    move-result-object v0

    if-eqz v0, :cond_1

    .line 40
    instance-of v1, v0, Lcom/qf/ipcsdk/aidl/IICanControlServer;

    if-eqz v1, :cond_1

    .line 41
    check-cast v0, Lcom/qf/ipcsdk/aidl/IICanControlServer;

    return-object v0

    .line 43
    :cond_1
    new-instance v0, Lcom/qf/ipcsdk/aidl/IICanControlServer$Stub$Proxy;

    invoke-direct {v0, p0}, Lcom/qf/ipcsdk/aidl/IICanControlServer$Stub$Proxy;-><init>(Landroid/os/IBinder;)V

    return-object v0
.end method

.method public static getDefaultImpl()Lcom/qf/ipcsdk/aidl/IICanControlServer;
    .locals 1

    .line 122
    sget-object v0, Lcom/qf/ipcsdk/aidl/IICanControlServer$Stub$Proxy;->sDefaultImpl:Lcom/qf/ipcsdk/aidl/IICanControlServer;

    return-object v0
.end method

.method public static setDefaultImpl(Lcom/qf/ipcsdk/aidl/IICanControlServer;)Z
    .locals 1

    .line 115
    sget-object v0, Lcom/qf/ipcsdk/aidl/IICanControlServer$Stub$Proxy;->sDefaultImpl:Lcom/qf/ipcsdk/aidl/IICanControlServer;

    if-nez v0, :cond_0

    if-eqz p0, :cond_0

    .line 116
    sput-object p0, Lcom/qf/ipcsdk/aidl/IICanControlServer$Stub$Proxy;->sDefaultImpl:Lcom/qf/ipcsdk/aidl/IICanControlServer;

    const/4 p0, 0x1

    return p0

    :cond_0
    const/4 p0, 0x0

    return p0
.end method


# virtual methods
.method public asBinder()Landroid/os/IBinder;
    .locals 0

    return-object p0
.end method

.method public onTransact(ILandroid/os/Parcel;Landroid/os/Parcel;I)Z
    .locals 3
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Landroid/os/RemoteException;
        }
    .end annotation

    const/4 v0, 0x1

    const-string v1, "com.qf.ipcsdk.aidl.IICanControlServer"

    if-eq p1, v0, :cond_1

    const v2, 0x5f4e5446

    if-eq p1, v2, :cond_0

    .line 70
    invoke-super {p0, p1, p2, p3, p4}, Landroid/os/Binder;->onTransact(ILandroid/os/Parcel;Landroid/os/Parcel;I)Z

    move-result p1

    return p1

    .line 56
    :cond_0
    invoke-virtual {p3, v1}, Landroid/os/Parcel;->writeString(Ljava/lang/String;)V

    return v0

    .line 61
    :cond_1
    invoke-virtual {p2, v1}, Landroid/os/Parcel;->enforceInterface(Ljava/lang/String;)V

    .line 63
    invoke-virtual {p2}, Landroid/os/Parcel;->readInt()I

    move-result p1

    if-eqz p1, :cond_2

    move p1, v0

    goto :goto_0

    :cond_2
    const/4 p1, 0x0

    .line 64
    :goto_0
    invoke-virtual {p0, p1}, Lcom/qf/ipcsdk/aidl/IICanControlServer$Stub;->onCanTellMeHandBrake(Z)V

    .line 65
    invoke-virtual {p3}, Landroid/os/Parcel;->writeNoException()V

    return v0
.end method
