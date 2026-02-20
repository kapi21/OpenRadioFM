.class Lcom/qf/ipcsdk/aidl/IIWindowUpdateServer$Stub$Proxy;
.super Ljava/lang/Object;
.source "IIWindowUpdateServer.java"

# interfaces
.implements Lcom/qf/ipcsdk/aidl/IIWindowUpdateServer;


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Lcom/qf/ipcsdk/aidl/IIWindowUpdateServer$Stub;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0xa
    name = "Proxy"
.end annotation


# static fields
.field public static sDefaultImpl:Lcom/qf/ipcsdk/aidl/IIWindowUpdateServer;


# instance fields
.field private mRemote:Landroid/os/IBinder;


# direct methods
.method constructor <init>(Landroid/os/IBinder;)V
    .locals 0

    .line 119
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 120
    iput-object p1, p0, Lcom/qf/ipcsdk/aidl/IIWindowUpdateServer$Stub$Proxy;->mRemote:Landroid/os/IBinder;

    return-void
.end method


# virtual methods
.method public asBinder()Landroid/os/IBinder;
    .locals 1

    .line 124
    iget-object v0, p0, Lcom/qf/ipcsdk/aidl/IIWindowUpdateServer$Stub$Proxy;->mRemote:Landroid/os/IBinder;

    return-object v0
.end method

.method public getInterfaceDescriptor()Ljava/lang/String;
    .locals 1

    const-string v0, "com.qf.ipcsdk.aidl.IIWindowUpdateServer"

    return-object v0
.end method

.method public onAddView(Ljava/lang/String;Ljava/lang/String;II)Z
    .locals 5
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Landroid/os/RemoteException;
        }
    .end annotation

    .line 132
    invoke-static {}, Landroid/os/Parcel;->obtain()Landroid/os/Parcel;

    move-result-object v0

    .line 133
    invoke-static {}, Landroid/os/Parcel;->obtain()Landroid/os/Parcel;

    move-result-object v1

    :try_start_0
    const-string v2, "com.qf.ipcsdk.aidl.IIWindowUpdateServer"

    .line 136
    invoke-virtual {v0, v2}, Landroid/os/Parcel;->writeInterfaceToken(Ljava/lang/String;)V

    .line 137
    invoke-virtual {v0, p1}, Landroid/os/Parcel;->writeString(Ljava/lang/String;)V

    .line 138
    invoke-virtual {v0, p2}, Landroid/os/Parcel;->writeString(Ljava/lang/String;)V

    .line 139
    invoke-virtual {v0, p3}, Landroid/os/Parcel;->writeInt(I)V

    .line 140
    invoke-virtual {v0, p4}, Landroid/os/Parcel;->writeInt(I)V

    .line 141
    iget-object v2, p0, Lcom/qf/ipcsdk/aidl/IIWindowUpdateServer$Stub$Proxy;->mRemote:Landroid/os/IBinder;

    const/4 v3, 0x0

    const/4 v4, 0x1

    invoke-interface {v2, v4, v0, v1, v3}, Landroid/os/IBinder;->transact(ILandroid/os/Parcel;Landroid/os/Parcel;I)Z

    move-result v2

    if-nez v2, :cond_0

    .line 142
    invoke-static {}, Lcom/qf/ipcsdk/aidl/IIWindowUpdateServer$Stub;->getDefaultImpl()Lcom/qf/ipcsdk/aidl/IIWindowUpdateServer;

    move-result-object v2

    if-eqz v2, :cond_0

    .line 143
    invoke-static {}, Lcom/qf/ipcsdk/aidl/IIWindowUpdateServer$Stub;->getDefaultImpl()Lcom/qf/ipcsdk/aidl/IIWindowUpdateServer;

    move-result-object v2

    invoke-interface {v2, p1, p2, p3, p4}, Lcom/qf/ipcsdk/aidl/IIWindowUpdateServer;->onAddView(Ljava/lang/String;Ljava/lang/String;II)Z

    move-result p1
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 149
    invoke-virtual {v1}, Landroid/os/Parcel;->recycle()V

    .line 150
    invoke-virtual {v0}, Landroid/os/Parcel;->recycle()V

    return p1

    .line 145
    :cond_0
    :try_start_1
    invoke-virtual {v1}, Landroid/os/Parcel;->readException()V

    .line 146
    invoke-virtual {v1}, Landroid/os/Parcel;->readInt()I

    move-result p1
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    if-eqz p1, :cond_1

    move v3, v4

    .line 149
    :cond_1
    invoke-virtual {v1}, Landroid/os/Parcel;->recycle()V

    .line 150
    invoke-virtual {v0}, Landroid/os/Parcel;->recycle()V

    return v3

    :catchall_0
    move-exception p1

    .line 149
    invoke-virtual {v1}, Landroid/os/Parcel;->recycle()V

    .line 150
    invoke-virtual {v0}, Landroid/os/Parcel;->recycle()V

    .line 151
    throw p1
.end method

.method public onRemoveView(Ljava/lang/String;Ljava/lang/String;)Z
    .locals 5
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Landroid/os/RemoteException;
        }
    .end annotation

    .line 180
    invoke-static {}, Landroid/os/Parcel;->obtain()Landroid/os/Parcel;

    move-result-object v0

    .line 181
    invoke-static {}, Landroid/os/Parcel;->obtain()Landroid/os/Parcel;

    move-result-object v1

    :try_start_0
    const-string v2, "com.qf.ipcsdk.aidl.IIWindowUpdateServer"

    .line 184
    invoke-virtual {v0, v2}, Landroid/os/Parcel;->writeInterfaceToken(Ljava/lang/String;)V

    .line 185
    invoke-virtual {v0, p1}, Landroid/os/Parcel;->writeString(Ljava/lang/String;)V

    .line 186
    invoke-virtual {v0, p2}, Landroid/os/Parcel;->writeString(Ljava/lang/String;)V

    .line 187
    iget-object v2, p0, Lcom/qf/ipcsdk/aidl/IIWindowUpdateServer$Stub$Proxy;->mRemote:Landroid/os/IBinder;

    const/4 v3, 0x3

    const/4 v4, 0x0

    invoke-interface {v2, v3, v0, v1, v4}, Landroid/os/IBinder;->transact(ILandroid/os/Parcel;Landroid/os/Parcel;I)Z

    move-result v2

    if-nez v2, :cond_0

    .line 188
    invoke-static {}, Lcom/qf/ipcsdk/aidl/IIWindowUpdateServer$Stub;->getDefaultImpl()Lcom/qf/ipcsdk/aidl/IIWindowUpdateServer;

    move-result-object v2

    if-eqz v2, :cond_0

    .line 189
    invoke-static {}, Lcom/qf/ipcsdk/aidl/IIWindowUpdateServer$Stub;->getDefaultImpl()Lcom/qf/ipcsdk/aidl/IIWindowUpdateServer;

    move-result-object v2

    invoke-interface {v2, p1, p2}, Lcom/qf/ipcsdk/aidl/IIWindowUpdateServer;->onRemoveView(Ljava/lang/String;Ljava/lang/String;)Z

    move-result p1
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 195
    invoke-virtual {v1}, Landroid/os/Parcel;->recycle()V

    .line 196
    invoke-virtual {v0}, Landroid/os/Parcel;->recycle()V

    return p1

    .line 191
    :cond_0
    :try_start_1
    invoke-virtual {v1}, Landroid/os/Parcel;->readException()V

    .line 192
    invoke-virtual {v1}, Landroid/os/Parcel;->readInt()I

    move-result p1
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    if-eqz p1, :cond_1

    const/4 v4, 0x1

    .line 195
    :cond_1
    invoke-virtual {v1}, Landroid/os/Parcel;->recycle()V

    .line 196
    invoke-virtual {v0}, Landroid/os/Parcel;->recycle()V

    return v4

    :catchall_0
    move-exception p1

    .line 195
    invoke-virtual {v1}, Landroid/os/Parcel;->recycle()V

    .line 196
    invoke-virtual {v0}, Landroid/os/Parcel;->recycle()V

    .line 197
    throw p1
.end method

.method public onUpdateView(Ljava/lang/String;Ljava/lang/String;II)Z
    .locals 5
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Landroid/os/RemoteException;
        }
    .end annotation

    .line 156
    invoke-static {}, Landroid/os/Parcel;->obtain()Landroid/os/Parcel;

    move-result-object v0

    .line 157
    invoke-static {}, Landroid/os/Parcel;->obtain()Landroid/os/Parcel;

    move-result-object v1

    :try_start_0
    const-string v2, "com.qf.ipcsdk.aidl.IIWindowUpdateServer"

    .line 160
    invoke-virtual {v0, v2}, Landroid/os/Parcel;->writeInterfaceToken(Ljava/lang/String;)V

    .line 161
    invoke-virtual {v0, p1}, Landroid/os/Parcel;->writeString(Ljava/lang/String;)V

    .line 162
    invoke-virtual {v0, p2}, Landroid/os/Parcel;->writeString(Ljava/lang/String;)V

    .line 163
    invoke-virtual {v0, p3}, Landroid/os/Parcel;->writeInt(I)V

    .line 164
    invoke-virtual {v0, p4}, Landroid/os/Parcel;->writeInt(I)V

    .line 165
    iget-object v2, p0, Lcom/qf/ipcsdk/aidl/IIWindowUpdateServer$Stub$Proxy;->mRemote:Landroid/os/IBinder;

    const/4 v3, 0x2

    const/4 v4, 0x0

    invoke-interface {v2, v3, v0, v1, v4}, Landroid/os/IBinder;->transact(ILandroid/os/Parcel;Landroid/os/Parcel;I)Z

    move-result v2

    if-nez v2, :cond_0

    .line 166
    invoke-static {}, Lcom/qf/ipcsdk/aidl/IIWindowUpdateServer$Stub;->getDefaultImpl()Lcom/qf/ipcsdk/aidl/IIWindowUpdateServer;

    move-result-object v2

    if-eqz v2, :cond_0

    .line 167
    invoke-static {}, Lcom/qf/ipcsdk/aidl/IIWindowUpdateServer$Stub;->getDefaultImpl()Lcom/qf/ipcsdk/aidl/IIWindowUpdateServer;

    move-result-object v2

    invoke-interface {v2, p1, p2, p3, p4}, Lcom/qf/ipcsdk/aidl/IIWindowUpdateServer;->onUpdateView(Ljava/lang/String;Ljava/lang/String;II)Z

    move-result p1
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 173
    invoke-virtual {v1}, Landroid/os/Parcel;->recycle()V

    .line 174
    invoke-virtual {v0}, Landroid/os/Parcel;->recycle()V

    return p1

    .line 169
    :cond_0
    :try_start_1
    invoke-virtual {v1}, Landroid/os/Parcel;->readException()V

    .line 170
    invoke-virtual {v1}, Landroid/os/Parcel;->readInt()I

    move-result p1
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    if-eqz p1, :cond_1

    const/4 v4, 0x1

    .line 173
    :cond_1
    invoke-virtual {v1}, Landroid/os/Parcel;->recycle()V

    .line 174
    invoke-virtual {v0}, Landroid/os/Parcel;->recycle()V

    return v4

    :catchall_0
    move-exception p1

    .line 173
    invoke-virtual {v1}, Landroid/os/Parcel;->recycle()V

    .line 174
    invoke-virtual {v0}, Landroid/os/Parcel;->recycle()V

    .line 175
    throw p1
.end method
