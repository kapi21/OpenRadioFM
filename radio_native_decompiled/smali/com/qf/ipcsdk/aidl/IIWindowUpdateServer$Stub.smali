.class public abstract Lcom/qf/ipcsdk/aidl/IIWindowUpdateServer$Stub;
.super Landroid/os/Binder;
.source "IIWindowUpdateServer.java"

# interfaces
.implements Lcom/qf/ipcsdk/aidl/IIWindowUpdateServer;


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Lcom/qf/ipcsdk/aidl/IIWindowUpdateServer;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x409
    name = "Stub"
.end annotation

.annotation system Ldalvik/annotation/MemberClasses;
    value = {
        Lcom/qf/ipcsdk/aidl/IIWindowUpdateServer$Stub$Proxy;
    }
.end annotation


# static fields
.field private static final DESCRIPTOR:Ljava/lang/String; = "com.qf.ipcsdk.aidl.IIWindowUpdateServer"

.field static final TRANSACTION_onAddView:I = 0x1

.field static final TRANSACTION_onRemoveView:I = 0x3

.field static final TRANSACTION_onUpdateView:I = 0x2


# direct methods
.method public constructor <init>()V
    .locals 1

    .line 33
    invoke-direct {p0}, Landroid/os/Binder;-><init>()V

    const-string v0, "com.qf.ipcsdk.aidl.IIWindowUpdateServer"

    .line 34
    invoke-virtual {p0, p0, v0}, Lcom/qf/ipcsdk/aidl/IIWindowUpdateServer$Stub;->attachInterface(Landroid/os/IInterface;Ljava/lang/String;)V

    return-void
.end method

.method public static asInterface(Landroid/os/IBinder;)Lcom/qf/ipcsdk/aidl/IIWindowUpdateServer;
    .locals 2

    if-nez p0, :cond_0

    const/4 p0, 0x0

    return-object p0

    :cond_0
    const-string v0, "com.qf.ipcsdk.aidl.IIWindowUpdateServer"

    .line 45
    invoke-interface {p0, v0}, Landroid/os/IBinder;->queryLocalInterface(Ljava/lang/String;)Landroid/os/IInterface;

    move-result-object v0

    if-eqz v0, :cond_1

    .line 46
    instance-of v1, v0, Lcom/qf/ipcsdk/aidl/IIWindowUpdateServer;

    if-eqz v1, :cond_1

    .line 47
    check-cast v0, Lcom/qf/ipcsdk/aidl/IIWindowUpdateServer;

    return-object v0

    .line 49
    :cond_1
    new-instance v0, Lcom/qf/ipcsdk/aidl/IIWindowUpdateServer$Stub$Proxy;

    invoke-direct {v0, p0}, Lcom/qf/ipcsdk/aidl/IIWindowUpdateServer$Stub$Proxy;-><init>(Landroid/os/IBinder;)V

    return-object v0
.end method

.method public static getDefaultImpl()Lcom/qf/ipcsdk/aidl/IIWindowUpdateServer;
    .locals 1

    .line 213
    sget-object v0, Lcom/qf/ipcsdk/aidl/IIWindowUpdateServer$Stub$Proxy;->sDefaultImpl:Lcom/qf/ipcsdk/aidl/IIWindowUpdateServer;

    return-object v0
.end method

.method public static setDefaultImpl(Lcom/qf/ipcsdk/aidl/IIWindowUpdateServer;)Z
    .locals 1

    .line 206
    sget-object v0, Lcom/qf/ipcsdk/aidl/IIWindowUpdateServer$Stub$Proxy;->sDefaultImpl:Lcom/qf/ipcsdk/aidl/IIWindowUpdateServer;

    if-nez v0, :cond_0

    if-eqz p0, :cond_0

    .line 207
    sput-object p0, Lcom/qf/ipcsdk/aidl/IIWindowUpdateServer$Stub$Proxy;->sDefaultImpl:Lcom/qf/ipcsdk/aidl/IIWindowUpdateServer;

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

    const-string v1, "com.qf.ipcsdk.aidl.IIWindowUpdateServer"

    if-eq p1, v0, :cond_3

    const/4 v2, 0x2

    if-eq p1, v2, :cond_2

    const/4 v2, 0x3

    if-eq p1, v2, :cond_1

    const v2, 0x5f4e5446

    if-eq p1, v2, :cond_0

    .line 111
    invoke-super {p0, p1, p2, p3, p4}, Landroid/os/Binder;->onTransact(ILandroid/os/Parcel;Landroid/os/Parcel;I)Z

    move-result p1

    return p1

    .line 62
    :cond_0
    invoke-virtual {p3, v1}, Landroid/os/Parcel;->writeString(Ljava/lang/String;)V

    return v0

    .line 99
    :cond_1
    invoke-virtual {p2, v1}, Landroid/os/Parcel;->enforceInterface(Ljava/lang/String;)V

    .line 101
    invoke-virtual {p2}, Landroid/os/Parcel;->readString()Ljava/lang/String;

    move-result-object p1

    .line 103
    invoke-virtual {p2}, Landroid/os/Parcel;->readString()Ljava/lang/String;

    move-result-object p2

    .line 104
    invoke-virtual {p0, p1, p2}, Lcom/qf/ipcsdk/aidl/IIWindowUpdateServer$Stub;->onRemoveView(Ljava/lang/String;Ljava/lang/String;)Z

    move-result p1

    .line 105
    invoke-virtual {p3}, Landroid/os/Parcel;->writeNoException()V

    .line 106
    invoke-virtual {p3, p1}, Landroid/os/Parcel;->writeInt(I)V

    return v0

    .line 83
    :cond_2
    invoke-virtual {p2, v1}, Landroid/os/Parcel;->enforceInterface(Ljava/lang/String;)V

    .line 85
    invoke-virtual {p2}, Landroid/os/Parcel;->readString()Ljava/lang/String;

    move-result-object p1

    .line 87
    invoke-virtual {p2}, Landroid/os/Parcel;->readString()Ljava/lang/String;

    move-result-object p4

    .line 89
    invoke-virtual {p2}, Landroid/os/Parcel;->readInt()I

    move-result v1

    .line 91
    invoke-virtual {p2}, Landroid/os/Parcel;->readInt()I

    move-result p2

    .line 92
    invoke-virtual {p0, p1, p4, v1, p2}, Lcom/qf/ipcsdk/aidl/IIWindowUpdateServer$Stub;->onUpdateView(Ljava/lang/String;Ljava/lang/String;II)Z

    move-result p1

    .line 93
    invoke-virtual {p3}, Landroid/os/Parcel;->writeNoException()V

    .line 94
    invoke-virtual {p3, p1}, Landroid/os/Parcel;->writeInt(I)V

    return v0

    .line 67
    :cond_3
    invoke-virtual {p2, v1}, Landroid/os/Parcel;->enforceInterface(Ljava/lang/String;)V

    .line 69
    invoke-virtual {p2}, Landroid/os/Parcel;->readString()Ljava/lang/String;

    move-result-object p1

    .line 71
    invoke-virtual {p2}, Landroid/os/Parcel;->readString()Ljava/lang/String;

    move-result-object p4

    .line 73
    invoke-virtual {p2}, Landroid/os/Parcel;->readInt()I

    move-result v1

    .line 75
    invoke-virtual {p2}, Landroid/os/Parcel;->readInt()I

    move-result p2

    .line 76
    invoke-virtual {p0, p1, p4, v1, p2}, Lcom/qf/ipcsdk/aidl/IIWindowUpdateServer$Stub;->onAddView(Ljava/lang/String;Ljava/lang/String;II)Z

    move-result p1

    .line 77
    invoke-virtual {p3}, Landroid/os/Parcel;->writeNoException()V

    .line 78
    invoke-virtual {p3, p1}, Landroid/os/Parcel;->writeInt(I)V

    return v0
.end method
