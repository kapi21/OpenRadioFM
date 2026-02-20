.class public interface abstract Lcom/qf/ipcsdk/aidl/IIWindowUpdateServer;
.super Ljava/lang/Object;
.source "IIWindowUpdateServer.java"

# interfaces
.implements Landroid/os/IInterface;


# annotations
.annotation system Ldalvik/annotation/MemberClasses;
    value = {
        Lcom/qf/ipcsdk/aidl/IIWindowUpdateServer$Stub;,
        Lcom/qf/ipcsdk/aidl/IIWindowUpdateServer$Default;
    }
.end annotation


# virtual methods
.method public abstract onAddView(Ljava/lang/String;Ljava/lang/String;II)Z
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Landroid/os/RemoteException;
        }
    .end annotation
.end method

.method public abstract onRemoveView(Ljava/lang/String;Ljava/lang/String;)Z
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Landroid/os/RemoteException;
        }
    .end annotation
.end method

.method public abstract onUpdateView(Ljava/lang/String;Ljava/lang/String;II)Z
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Landroid/os/RemoteException;
        }
    .end annotation
.end method
