.class public Lcom/qf/clientsdk/tuner/TunerCmdFactory;
.super Ljava/lang/Object;
.source "TunerCmdFactory.java"

# interfaces
.implements Lcom/qf/clientsdk/tuner/ITuner;


# static fields
.field private static final AUTO_SCAN_BEGIN:B = 0x5t

.field private static final AUTO_SCAN_STOP:B = 0xct

.field private static final FINE_DOWN:B = 0x4t

.field private static final FINE_UP:B = 0x3t

.field private static final RDS_AF:B = 0x11t

.field private static final RDS_EON:B = 0x14t

.field private static final RDS_PTY:B = 0x15t

.field private static final RDS_REG:B = 0x13t

.field private static final RDS_TA:B = 0x12t

.field private static final SEEK_DOWN:B = 0x2t

.field private static final SEEK_UP:B = 0x1t

.field private static final SWITCH_BAND:B = 0x6t

.field private static final SWITCH_LOC:B = 0x7t

.field private static final TAG:Ljava/lang/String;

.field private static final TUNE_AREA:B = 0xat

.field private static final TUNE_AS:B = 0x8t

.field private static final TUNE_FREQUENCY:B = 0x0t

.field private static final TUNE_NEXT_KEY:B = 0xet

.field private static final TUNE_PRESET_SAVE:B = 0xbt

.field private static final TUNE_PRESET_SELECT:B = 0xdt

.field private static final TUNE_PRE_KEY:B = 0xft

.field private static final TUNE_PS:B = 0x9t

.field private static final TUNE_ST:B = 0x10t

.field private static instance:Lcom/qf/clientsdk/tuner/TunerCmdFactory;

.field private static final mLock:Ljava/lang/Object;


# direct methods
.method static constructor <clinit>()V
    .locals 1

    .line 11
    const-class v0, Lcom/qf/clientsdk/tuner/TunerCmdFactory;

    invoke-virtual {v0}, Ljava/lang/Class;->getSimpleName()Ljava/lang/String;

    move-result-object v0

    sput-object v0, Lcom/qf/clientsdk/tuner/TunerCmdFactory;->TAG:Ljava/lang/String;

    .line 59
    new-instance v0, Ljava/lang/Object;

    invoke-direct {v0}, Ljava/lang/Object;-><init>()V

    sput-object v0, Lcom/qf/clientsdk/tuner/TunerCmdFactory;->mLock:Ljava/lang/Object;

    return-void
.end method

.method private constructor <init>()V
    .locals 0

    .line 61
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

.method public static getInstance()Lcom/qf/clientsdk/tuner/TunerCmdFactory;
    .locals 2

    .line 66
    sget-object v0, Lcom/qf/clientsdk/tuner/TunerCmdFactory;->instance:Lcom/qf/clientsdk/tuner/TunerCmdFactory;

    if-nez v0, :cond_1

    .line 67
    sget-object v0, Lcom/qf/clientsdk/tuner/TunerCmdFactory;->mLock:Ljava/lang/Object;

    monitor-enter v0

    .line 68
    :try_start_0
    sget-object v1, Lcom/qf/clientsdk/tuner/TunerCmdFactory;->instance:Lcom/qf/clientsdk/tuner/TunerCmdFactory;

    if-nez v1, :cond_0

    .line 69
    new-instance v1, Lcom/qf/clientsdk/tuner/TunerCmdFactory;

    invoke-direct {v1}, Lcom/qf/clientsdk/tuner/TunerCmdFactory;-><init>()V

    sput-object v1, Lcom/qf/clientsdk/tuner/TunerCmdFactory;->instance:Lcom/qf/clientsdk/tuner/TunerCmdFactory;

    .line 70
    :cond_0
    monitor-exit v0

    goto :goto_0

    :catchall_0
    move-exception v1

    monitor-exit v0
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    throw v1

    .line 71
    :cond_1
    :goto_0
    sget-object v0, Lcom/qf/clientsdk/tuner/TunerCmdFactory;->instance:Lcom/qf/clientsdk/tuner/TunerCmdFactory;

    return-object v0
.end method


# virtual methods
.method public autoScan()[B
    .locals 1

    const/4 v0, 0x4

    new-array v0, v0, [B

    .line 145
    fill-array-data v0, :array_0

    return-object v0

    nop

    :array_0
    .array-data 1
        -0x60t
        0x8t
        0x0t
        0x0t
    .end array-data
.end method

.method public onBand(B)[B
    .locals 4

    const/4 v0, 0x4

    new-array v0, v0, [B

    const/4 v1, 0x0

    const/16 v2, -0x60

    aput-byte v2, v0, v1

    const/4 v2, 0x1

    const/4 v3, 0x6

    aput-byte v3, v0, v2

    const/4 v2, 0x2

    aput-byte p1, v0, v2

    const/4 p1, 0x3

    aput-byte v1, v0, p1

    return-object v0
.end method

.method public onFine(Z)[B
    .locals 5

    const/4 v0, 0x4

    new-array v1, v0, [B

    const/4 v2, 0x0

    const/16 v3, -0x60

    aput-byte v3, v1, v2

    const/4 v3, 0x3

    const/4 v4, 0x1

    if-eqz p1, :cond_0

    aput-byte v0, v1, v4

    goto :goto_0

    :cond_0
    aput-byte v3, v1, v4

    :goto_0
    const/4 p1, 0x2

    aput-byte v2, v1, p1

    aput-byte v2, v1, v3

    return-object v1
.end method

.method public onLoc(B)[B
    .locals 4

    const/4 v0, 0x4

    new-array v0, v0, [B

    const/4 v1, 0x0

    const/16 v2, -0x60

    aput-byte v2, v0, v1

    const/4 v2, 0x1

    const/4 v3, 0x7

    aput-byte v3, v0, v2

    const/4 v2, 0x2

    aput-byte p1, v0, v2

    const/4 p1, 0x3

    aput-byte v1, v0, p1

    return-object v0
.end method

.method public onNext()[B
    .locals 1

    const/4 v0, 0x4

    new-array v0, v0, [B

    .line 213
    fill-array-data v0, :array_0

    return-object v0

    nop

    :array_0
    .array-data 1
        -0x60t
        0xet
        0x0t
        0x0t
    .end array-data
.end method

.method public onPre()[B
    .locals 1

    const/4 v0, 0x4

    new-array v0, v0, [B

    .line 203
    fill-array-data v0, :array_0

    return-object v0

    nop

    :array_0
    .array-data 1
        -0x60t
        0xft
        0x0t
        0x0t
    .end array-data
.end method

.method public onPresetSave(B)[B
    .locals 4

    const/4 v0, 0x4

    new-array v0, v0, [B

    const/4 v1, 0x0

    const/16 v2, -0x60

    aput-byte v2, v0, v1

    const/4 v2, 0x1

    const/16 v3, 0xb

    aput-byte v3, v0, v2

    const/4 v2, 0x2

    aput-byte p1, v0, v2

    const/4 p1, 0x3

    aput-byte v1, v0, p1

    return-object v0
.end method

.method public onPresetSelect(B)[B
    .locals 4

    const/4 v0, 0x4

    new-array v0, v0, [B

    const/4 v1, 0x0

    const/16 v2, -0x60

    aput-byte v2, v0, v1

    const/4 v2, 0x1

    const/16 v3, 0xd

    aput-byte v3, v0, v2

    const/4 v2, 0x2

    aput-byte p1, v0, v2

    const/4 p1, 0x3

    aput-byte v1, v0, p1

    return-object v0
.end method

.method public onRadioArea(B)[B
    .locals 4

    const/4 v0, 0x4

    new-array v0, v0, [B

    const/4 v1, 0x0

    const/16 v2, -0x60

    aput-byte v2, v0, v1

    const/4 v2, 0x1

    const/16 v3, 0xa

    aput-byte v3, v0, v2

    const/4 v2, 0x2

    aput-byte p1, v0, v2

    const/4 p1, 0x3

    aput-byte v1, v0, p1

    return-object v0
.end method

.method public onSeek(Z)[B
    .locals 4

    const/4 v0, 0x4

    new-array v0, v0, [B

    const/4 v1, 0x0

    const/16 v2, -0x60

    aput-byte v2, v0, v1

    const/4 v2, 0x2

    const/4 v3, 0x1

    if-eqz p1, :cond_0

    aput-byte v2, v0, v3

    goto :goto_0

    :cond_0
    aput-byte v3, v0, v3

    :goto_0
    aput-byte v1, v0, v2

    const/4 p1, 0x3

    aput-byte v1, v0, p1

    return-object v0
.end method

.method public onTune(I)[B
    .locals 4

    const/4 v0, 0x4

    new-array v0, v0, [B

    const/4 v1, 0x0

    const/16 v2, -0x60

    aput-byte v2, v0, v1

    const/4 v2, 0x1

    aput-byte v1, v0, v2

    const/4 v3, 0x2

    .line 89
    invoke-static {p1, v3}, Lcom/qf/clientsdk/utils/ByteTool;->int2Bytes(II)[B

    move-result-object p1

    .line 90
    aget-byte v2, p1, v2

    aput-byte v2, v0, v3

    .line 91
    aget-byte p1, p1, v1

    const/4 v1, 0x3

    aput-byte p1, v0, v1

    return-object v0
.end method

.method public setPresetList([B)[B
    .locals 4

    .line 264
    array-length v0, p1

    const/4 v1, 0x1

    add-int/2addr v0, v1

    new-array v0, v0, [B

    const/4 v2, 0x0

    const/16 v3, -0x5f

    .line 265
    aput-byte v3, v0, v2

    .line 266
    array-length v3, p1

    invoke-static {p1, v2, v0, v1, v3}, Ljava/lang/System;->arraycopy(Ljava/lang/Object;ILjava/lang/Object;II)V

    return-object v0
.end method

.method public setRdsAFSwitch()[B
    .locals 1

    const/4 v0, 0x4

    new-array v0, v0, [B

    .line 254
    fill-array-data v0, :array_0

    return-object v0

    nop

    :array_0
    .array-data 1
        -0x60t
        0x11t
        0x0t
        0x0t
    .end array-data
.end method

.method public setRdsPtyType(B)[B
    .locals 4

    const/4 v0, 0x4

    new-array v0, v0, [B

    const/4 v1, 0x0

    const/16 v2, -0x60

    aput-byte v2, v0, v1

    const/4 v2, 0x1

    const/16 v3, 0x15

    aput-byte v3, v0, v2

    const/4 v2, 0x2

    aput-byte p1, v0, v2

    const/4 p1, 0x3

    aput-byte v1, v0, p1

    return-object v0
.end method

.method public setRdsSwitch(B)[B
    .locals 3

    .line 223
    sget-object v0, Lcom/qf/clientsdk/tuner/TunerCmdFactory;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "setRdsSwitch - - rds: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    const/4 v0, 0x2

    new-array v0, v0, [B

    const/4 v1, 0x0

    const/16 v2, -0x5e

    aput-byte v2, v0, v1

    const/4 v1, 0x1

    aput-byte p1, v0, v1

    .line 228
    sget-object p1, Lcom/qf/clientsdk/tuner/TunerCmdFactory;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "setRdsSwitch - cmds: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-static {v0}, Lcom/qf/clientsdk/utils/ByteTool;->ByteToString([B)Ljava/lang/String;

    move-result-object v2

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {p1, v1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    return-object v0
.end method

.method public setRdsTASwitch()[B
    .locals 1

    const/4 v0, 0x4

    new-array v0, v0, [B

    .line 244
    fill-array-data v0, :array_0

    return-object v0

    nop

    :array_0
    .array-data 1
        -0x60t
        0x12t
        0x0t
        0x0t
    .end array-data
.end method

.method public stopScan()[B
    .locals 1

    const/4 v0, 0x4

    new-array v0, v0, [B

    .line 155
    fill-array-data v0, :array_0

    return-object v0

    nop

    :array_0
    .array-data 1
        -0x60t
        0xct
        0x0t
        0x0t
    .end array-data
.end method

.method public tuneExt(BBBI)[B
    .locals 3

    .line 97
    sget-object p3, Lcom/qf/clientsdk/tuner/TunerCmdFactory;->TAG:Ljava/lang/String;

    new-instance v0, Ljava/lang/StringBuilder;

    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    const-string v1, "tuneExt - band: "

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v0, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v1, " - area: "

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v0, p2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v1, " - freq: "

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v0, p4}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    invoke-static {p3, v0}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    const/4 p3, 0x5

    new-array p3, p3, [B

    const/4 v0, 0x0

    const/16 v1, -0x60

    aput-byte v1, p3, v0

    const/4 v1, 0x1

    aput-byte v0, p3, v1

    const/4 v2, 0x4

    shl-int/2addr p1, v2

    or-int/2addr p1, p2

    int-to-byte p1, p1

    const/4 p2, 0x2

    aput-byte p1, p3, p2

    .line 106
    invoke-static {p4, p2}, Lcom/qf/clientsdk/utils/ByteTool;->int2Bytes(II)[B

    move-result-object p1

    .line 107
    aget-byte p2, p1, v1

    const/4 p4, 0x3

    aput-byte p2, p3, p4

    .line 108
    aget-byte p1, p1, v0

    aput-byte p1, p3, v2

    .line 109
    sget-object p1, Lcom/qf/clientsdk/tuner/TunerCmdFactory;->TAG:Ljava/lang/String;

    new-instance p2, Ljava/lang/StringBuilder;

    invoke-direct {p2}, Ljava/lang/StringBuilder;-><init>()V

    const-string p4, "tuneExt - cmds: "

    invoke-virtual {p2, p4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-static {p3}, Lcom/qf/clientsdk/utils/ByteTool;->ByteToString([B)Ljava/lang/String;

    move-result-object p4

    invoke-virtual {p2, p4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {p2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p2

    invoke-static {p1, p2}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    return-object p3
.end method
