.class public interface abstract Lcom/qf/ipcsdk/aidl/IICanControlServer;
.super Ljava/lang/Object;
.source "IICanControlServer.java"

# interfaces
.implements Landroid/os/IInterface;


# annotations
.annotation system Ldalvik/annotation/MemberClasses;
    value = {
        Lcom/qf/ipcsdk/aidl/IICanControlServer$Stub;,
        Lcom/qf/ipcsdk/aidl/IICanControlServer$Default;
    }
.end annotation


# virtual methods
.method public abstract onCanTellMeHandBrake(Z)V
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Landroid/os/RemoteException;
        }
    .end annotation
.end method
