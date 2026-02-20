.class public Lcom/qf/clientsdk/McuConstant$CID_OX18_Data;
.super Ljava/lang/Object;
.source "McuConstant.java"


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Lcom/qf/clientsdk/McuConstant;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x9
    name = "CID_OX18_Data"
.end annotation


# static fields
.field public static final SET_ARM_REBOOT:B = 0x3t

.field public static final SET_AWM_POWER_VOL:B = 0x2t

.field public static final SET_CAN_UART_BPS:B = 0x5t

.field public static final SET_REQUEST_PLUGIN_TUNER:B = 0x7t

.field public static final SET_RESET_AUDIO:B = 0x6t

.field public static final SET_STEER_KEY:B = 0x1t


# direct methods
.method public constructor <init>()V
    .locals 0

    .line 74
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method
