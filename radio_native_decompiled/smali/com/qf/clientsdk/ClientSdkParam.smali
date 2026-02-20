.class public Lcom/qf/clientsdk/ClientSdkParam;
.super Ljava/lang/Object;
.source "ClientSdkParam.java"


# annotations
.annotation system Ldalvik/annotation/MemberClasses;
    value = {
        Lcom/qf/clientsdk/ClientSdkParam$SDKAPI;
    }
.end annotation


# instance fields
.field protected isSetIMcuListener:Z

.field private sContext:Landroid/content/Context;


# direct methods
.method public constructor <init>(Landroid/content/Context;Ljava/lang/String;Lcom/qf/clientsdk/ClientSdkParam$SDKAPI;)V
    .locals 0

    .line 11
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    const/4 p2, 0x1

    .line 9
    iput-boolean p2, p0, Lcom/qf/clientsdk/ClientSdkParam;->isSetIMcuListener:Z

    .line 12
    iput-object p1, p0, Lcom/qf/clientsdk/ClientSdkParam;->sContext:Landroid/content/Context;

    return-void
.end method


# virtual methods
.method public getContext()Landroid/content/Context;
    .locals 1

    .line 21
    iget-object v0, p0, Lcom/qf/clientsdk/ClientSdkParam;->sContext:Landroid/content/Context;

    return-object v0
.end method

.method public setNotMcuListener()Lcom/qf/clientsdk/ClientSdkParam;
    .locals 1

    const/4 v0, 0x0

    .line 16
    iput-boolean v0, p0, Lcom/qf/clientsdk/ClientSdkParam;->isSetIMcuListener:Z

    return-object p0
.end method
