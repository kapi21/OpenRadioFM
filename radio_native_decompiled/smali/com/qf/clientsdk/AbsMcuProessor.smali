.class public abstract Lcom/qf/clientsdk/AbsMcuProessor;
.super Ljava/lang/Object;
.source "AbsMcuProessor.java"

# interfaces
.implements Landroid/qf/mcu/McuListener;


# instance fields
.field mContext:Landroid/content/Context;


# direct methods
.method public constructor <init>(Landroid/content/Context;)V
    .locals 0

    .line 10
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 11
    iput-object p1, p0, Lcom/qf/clientsdk/AbsMcuProessor;->mContext:Landroid/content/Context;

    return-void
.end method


# virtual methods
.method abstract getHandBrakeState()Z
.end method

.method abstract getSeerKeyType()I
.end method

.method abstract getpowerAmpVol()I
.end method
