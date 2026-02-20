.class public abstract Lcom/broadcom/fm/fmreceiver/IFmReceiverService$Stub;
.super Landroid/os/Binder;
.source "IFmReceiverService.java"

# interfaces
.implements Lcom/broadcom/fm/fmreceiver/IFmReceiverService;


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Lcom/broadcom/fm/fmreceiver/IFmReceiverService;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x409
    name = "Stub"
.end annotation

.annotation system Ldalvik/annotation/MemberClasses;
    value = {
        Lcom/broadcom/fm/fmreceiver/IFmReceiverService$Stub$Proxy;
    }
.end annotation


# static fields
.field private static final DESCRIPTOR:Ljava/lang/String; = "com.broadcom.fm.fmreceiver.IFmReceiverService"

.field static final TRANSACTION_cleanupFmService:I = 0x1a

.field static final TRANSACTION_estimateNoiseFloorLevel:I = 0x16

.field static final TRANSACTION_getIsMute:I = 0x7

.field static final TRANSACTION_getMonoStereoMode:I = 0x5

.field static final TRANSACTION_getRadioIsOn:I = 0x4

.field static final TRANSACTION_getStatus:I = 0xb

.field static final TRANSACTION_getTunedFrequency:I = 0x6

.field static final TRANSACTION_init:I = 0x1

.field static final TRANSACTION_muteAudio:I = 0xc

.field static final TRANSACTION_registerCallback:I = 0x2

.field static final TRANSACTION_seekRdsStation:I = 0xf

.field static final TRANSACTION_seekStation:I = 0xd

.field static final TRANSACTION_seekStationAbort:I = 0x10

.field static final TRANSACTION_seekStationCombo:I = 0xe

.field static final TRANSACTION_setAudioMode:I = 0x12

.field static final TRANSACTION_setAudioPath:I = 0x13

.field static final TRANSACTION_setFMVolume:I = 0x18

.field static final TRANSACTION_setLiveAudioPolling:I = 0x17

.field static final TRANSACTION_setRdsMode:I = 0x11

.field static final TRANSACTION_setSnrThreshold:I = 0x19

.field static final TRANSACTION_setStepSize:I = 0x14

.field static final TRANSACTION_setWorldRegion:I = 0x15

.field static final TRANSACTION_tuneRadio:I = 0xa

.field static final TRANSACTION_turnOffRadio:I = 0x8

.field static final TRANSACTION_turnOnRadio:I = 0x9

.field static final TRANSACTION_unregisterCallback:I = 0x3


# direct methods
.method public constructor <init>()V
    .locals 1

    .line 17
    invoke-direct {p0}, Landroid/os/Binder;-><init>()V

    const-string v0, "com.broadcom.fm.fmreceiver.IFmReceiverService"

    .line 18
    invoke-virtual {p0, p0, v0}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService$Stub;->attachInterface(Landroid/os/IInterface;Ljava/lang/String;)V

    return-void
.end method

.method public static asInterface(Landroid/os/IBinder;)Lcom/broadcom/fm/fmreceiver/IFmReceiverService;
    .locals 2

    if-nez p0, :cond_0

    const/4 p0, 0x0

    return-object p0

    :cond_0
    const-string v0, "com.broadcom.fm.fmreceiver.IFmReceiverService"

    .line 29
    invoke-interface {p0, v0}, Landroid/os/IBinder;->queryLocalInterface(Ljava/lang/String;)Landroid/os/IInterface;

    move-result-object v0

    if-eqz v0, :cond_1

    .line 30
    instance-of v1, v0, Lcom/broadcom/fm/fmreceiver/IFmReceiverService;

    if-eqz v1, :cond_1

    .line 31
    check-cast v0, Lcom/broadcom/fm/fmreceiver/IFmReceiverService;

    return-object v0

    .line 33
    :cond_1
    new-instance v0, Lcom/broadcom/fm/fmreceiver/IFmReceiverService$Stub$Proxy;

    invoke-direct {v0, p0}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService$Stub$Proxy;-><init>(Landroid/os/IBinder;)V

    return-object v0
.end method


# virtual methods
.method public asBinder()Landroid/os/IBinder;
    .locals 0

    return-object p0
.end method

.method public onTransact(ILandroid/os/Parcel;Landroid/os/Parcel;I)Z
    .locals 14
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Landroid/os/RemoteException;
        }
    .end annotation

    move-object v9, p0

    move v0, p1

    move-object/from16 v1, p2

    move-object/from16 v10, p3

    const v2, 0x5f4e5446

    const-string v3, "com.broadcom.fm.fmreceiver.IFmReceiverService"

    const/4 v11, 0x1

    if-eq v0, v2, :cond_3

    const/4 v2, 0x0

    packed-switch v0, :pswitch_data_0

    .line 323
    invoke-super/range {p0 .. p4}, Landroid/os/Binder;->onTransact(ILandroid/os/Parcel;Landroid/os/Parcel;I)Z

    move-result v0

    return v0

    .line 316
    :pswitch_0
    invoke-virtual {v1, v3}, Landroid/os/Parcel;->enforceInterface(Ljava/lang/String;)V

    .line 317
    invoke-virtual {p0}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService$Stub;->cleanupFmService()I

    move-result v0

    .line 318
    invoke-virtual/range {p3 .. p3}, Landroid/os/Parcel;->writeNoException()V

    .line 319
    invoke-virtual {v10, v0}, Landroid/os/Parcel;->writeInt(I)V

    return v11

    .line 306
    :pswitch_1
    invoke-virtual {v1, v3}, Landroid/os/Parcel;->enforceInterface(Ljava/lang/String;)V

    .line 308
    invoke-virtual/range {p2 .. p2}, Landroid/os/Parcel;->readInt()I

    move-result v0

    .line 309
    invoke-virtual {p0, v0}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService$Stub;->setSnrThreshold(I)I

    move-result v0

    .line 310
    invoke-virtual/range {p3 .. p3}, Landroid/os/Parcel;->writeNoException()V

    .line 311
    invoke-virtual {v10, v0}, Landroid/os/Parcel;->writeInt(I)V

    return v11

    .line 296
    :pswitch_2
    invoke-virtual {v1, v3}, Landroid/os/Parcel;->enforceInterface(Ljava/lang/String;)V

    .line 298
    invoke-virtual/range {p2 .. p2}, Landroid/os/Parcel;->readInt()I

    move-result v0

    .line 299
    invoke-virtual {p0, v0}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService$Stub;->setFMVolume(I)I

    move-result v0

    .line 300
    invoke-virtual/range {p3 .. p3}, Landroid/os/Parcel;->writeNoException()V

    .line 301
    invoke-virtual {v10, v0}, Landroid/os/Parcel;->writeInt(I)V

    return v11

    .line 284
    :pswitch_3
    invoke-virtual {v1, v3}, Landroid/os/Parcel;->enforceInterface(Ljava/lang/String;)V

    .line 286
    invoke-virtual/range {p2 .. p2}, Landroid/os/Parcel;->readInt()I

    move-result v0

    if-eqz v0, :cond_0

    move v2, v11

    .line 288
    :cond_0
    invoke-virtual/range {p2 .. p2}, Landroid/os/Parcel;->readInt()I

    move-result v0

    .line 289
    invoke-virtual {p0, v2, v0}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService$Stub;->setLiveAudioPolling(ZI)I

    move-result v0

    .line 290
    invoke-virtual/range {p3 .. p3}, Landroid/os/Parcel;->writeNoException()V

    .line 291
    invoke-virtual {v10, v0}, Landroid/os/Parcel;->writeInt(I)V

    return v11

    .line 274
    :pswitch_4
    invoke-virtual {v1, v3}, Landroid/os/Parcel;->enforceInterface(Ljava/lang/String;)V

    .line 276
    invoke-virtual/range {p2 .. p2}, Landroid/os/Parcel;->readInt()I

    move-result v0

    .line 277
    invoke-virtual {p0, v0}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService$Stub;->estimateNoiseFloorLevel(I)I

    move-result v0

    .line 278
    invoke-virtual/range {p3 .. p3}, Landroid/os/Parcel;->writeNoException()V

    .line 279
    invoke-virtual {v10, v0}, Landroid/os/Parcel;->writeInt(I)V

    return v11

    .line 262
    :pswitch_5
    invoke-virtual {v1, v3}, Landroid/os/Parcel;->enforceInterface(Ljava/lang/String;)V

    .line 264
    invoke-virtual/range {p2 .. p2}, Landroid/os/Parcel;->readInt()I

    move-result v0

    .line 266
    invoke-virtual/range {p2 .. p2}, Landroid/os/Parcel;->readInt()I

    move-result v1

    .line 267
    invoke-virtual {p0, v0, v1}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService$Stub;->setWorldRegion(II)I

    move-result v0

    .line 268
    invoke-virtual/range {p3 .. p3}, Landroid/os/Parcel;->writeNoException()V

    .line 269
    invoke-virtual {v10, v0}, Landroid/os/Parcel;->writeInt(I)V

    return v11

    .line 252
    :pswitch_6
    invoke-virtual {v1, v3}, Landroid/os/Parcel;->enforceInterface(Ljava/lang/String;)V

    .line 254
    invoke-virtual/range {p2 .. p2}, Landroid/os/Parcel;->readInt()I

    move-result v0

    .line 255
    invoke-virtual {p0, v0}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService$Stub;->setStepSize(I)I

    move-result v0

    .line 256
    invoke-virtual/range {p3 .. p3}, Landroid/os/Parcel;->writeNoException()V

    .line 257
    invoke-virtual {v10, v0}, Landroid/os/Parcel;->writeInt(I)V

    return v11

    .line 242
    :pswitch_7
    invoke-virtual {v1, v3}, Landroid/os/Parcel;->enforceInterface(Ljava/lang/String;)V

    .line 244
    invoke-virtual/range {p2 .. p2}, Landroid/os/Parcel;->readInt()I

    move-result v0

    .line 245
    invoke-virtual {p0, v0}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService$Stub;->setAudioPath(I)I

    move-result v0

    .line 246
    invoke-virtual/range {p3 .. p3}, Landroid/os/Parcel;->writeNoException()V

    .line 247
    invoke-virtual {v10, v0}, Landroid/os/Parcel;->writeInt(I)V

    return v11

    .line 232
    :pswitch_8
    invoke-virtual {v1, v3}, Landroid/os/Parcel;->enforceInterface(Ljava/lang/String;)V

    .line 234
    invoke-virtual/range {p2 .. p2}, Landroid/os/Parcel;->readInt()I

    move-result v0

    .line 235
    invoke-virtual {p0, v0}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService$Stub;->setAudioMode(I)I

    move-result v0

    .line 236
    invoke-virtual/range {p3 .. p3}, Landroid/os/Parcel;->writeNoException()V

    .line 237
    invoke-virtual {v10, v0}, Landroid/os/Parcel;->writeInt(I)V

    return v11

    .line 216
    :pswitch_9
    invoke-virtual {v1, v3}, Landroid/os/Parcel;->enforceInterface(Ljava/lang/String;)V

    .line 218
    invoke-virtual/range {p2 .. p2}, Landroid/os/Parcel;->readInt()I

    move-result v0

    .line 220
    invoke-virtual/range {p2 .. p2}, Landroid/os/Parcel;->readInt()I

    move-result v2

    .line 222
    invoke-virtual/range {p2 .. p2}, Landroid/os/Parcel;->readInt()I

    move-result v3

    .line 224
    invoke-virtual/range {p2 .. p2}, Landroid/os/Parcel;->readInt()I

    move-result v1

    .line 225
    invoke-virtual {p0, v0, v2, v3, v1}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService$Stub;->setRdsMode(IIII)I

    move-result v0

    .line 226
    invoke-virtual/range {p3 .. p3}, Landroid/os/Parcel;->writeNoException()V

    .line 227
    invoke-virtual {v10, v0}, Landroid/os/Parcel;->writeInt(I)V

    return v11

    .line 208
    :pswitch_a
    invoke-virtual {v1, v3}, Landroid/os/Parcel;->enforceInterface(Ljava/lang/String;)V

    .line 209
    invoke-virtual {p0}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService$Stub;->seekStationAbort()I

    move-result v0

    .line 210
    invoke-virtual/range {p3 .. p3}, Landroid/os/Parcel;->writeNoException()V

    .line 211
    invoke-virtual {v10, v0}, Landroid/os/Parcel;->writeInt(I)V

    return v11

    .line 192
    :pswitch_b
    invoke-virtual {v1, v3}, Landroid/os/Parcel;->enforceInterface(Ljava/lang/String;)V

    .line 194
    invoke-virtual/range {p2 .. p2}, Landroid/os/Parcel;->readInt()I

    move-result v0

    .line 196
    invoke-virtual/range {p2 .. p2}, Landroid/os/Parcel;->readInt()I

    move-result v2

    .line 198
    invoke-virtual/range {p2 .. p2}, Landroid/os/Parcel;->readInt()I

    move-result v3

    .line 200
    invoke-virtual/range {p2 .. p2}, Landroid/os/Parcel;->readInt()I

    move-result v1

    .line 201
    invoke-virtual {p0, v0, v2, v3, v1}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService$Stub;->seekRdsStation(IIII)I

    move-result v0

    .line 202
    invoke-virtual/range {p3 .. p3}, Landroid/os/Parcel;->writeNoException()V

    .line 203
    invoke-virtual {v10, v0}, Landroid/os/Parcel;->writeInt(I)V

    return v11

    .line 168
    :pswitch_c
    invoke-virtual {v1, v3}, Landroid/os/Parcel;->enforceInterface(Ljava/lang/String;)V

    .line 170
    invoke-virtual/range {p2 .. p2}, Landroid/os/Parcel;->readInt()I

    move-result v3

    .line 172
    invoke-virtual/range {p2 .. p2}, Landroid/os/Parcel;->readInt()I

    move-result v4

    .line 174
    invoke-virtual/range {p2 .. p2}, Landroid/os/Parcel;->readInt()I

    move-result v5

    .line 176
    invoke-virtual/range {p2 .. p2}, Landroid/os/Parcel;->readInt()I

    move-result v6

    .line 178
    invoke-virtual/range {p2 .. p2}, Landroid/os/Parcel;->readInt()I

    move-result v7

    .line 180
    invoke-virtual/range {p2 .. p2}, Landroid/os/Parcel;->readInt()I

    move-result v0

    if-eqz v0, :cond_1

    move v8, v11

    goto :goto_0

    :cond_1
    move v8, v2

    .line 182
    :goto_0
    invoke-virtual/range {p2 .. p2}, Landroid/os/Parcel;->readInt()I

    move-result v12

    .line 184
    invoke-virtual/range {p2 .. p2}, Landroid/os/Parcel;->readInt()I

    move-result v13

    move-object v0, p0

    move v1, v3

    move v2, v4

    move v3, v5

    move v4, v6

    move v5, v7

    move v6, v8

    move v7, v12

    move v8, v13

    .line 185
    invoke-virtual/range {v0 .. v8}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService$Stub;->seekStationCombo(IIIIIZII)I

    move-result v0

    .line 186
    invoke-virtual/range {p3 .. p3}, Landroid/os/Parcel;->writeNoException()V

    .line 187
    invoke-virtual {v10, v0}, Landroid/os/Parcel;->writeInt(I)V

    return v11

    .line 156
    :pswitch_d
    invoke-virtual {v1, v3}, Landroid/os/Parcel;->enforceInterface(Ljava/lang/String;)V

    .line 158
    invoke-virtual/range {p2 .. p2}, Landroid/os/Parcel;->readInt()I

    move-result v0

    .line 160
    invoke-virtual/range {p2 .. p2}, Landroid/os/Parcel;->readInt()I

    move-result v1

    .line 161
    invoke-virtual {p0, v0, v1}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService$Stub;->seekStation(II)I

    move-result v0

    .line 162
    invoke-virtual/range {p3 .. p3}, Landroid/os/Parcel;->writeNoException()V

    .line 163
    invoke-virtual {v10, v0}, Landroid/os/Parcel;->writeInt(I)V

    return v11

    .line 146
    :pswitch_e
    invoke-virtual {v1, v3}, Landroid/os/Parcel;->enforceInterface(Ljava/lang/String;)V

    .line 148
    invoke-virtual/range {p2 .. p2}, Landroid/os/Parcel;->readInt()I

    move-result v0

    if-eqz v0, :cond_2

    move v2, v11

    .line 149
    :cond_2
    invoke-virtual {p0, v2}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService$Stub;->muteAudio(Z)I

    move-result v0

    .line 150
    invoke-virtual/range {p3 .. p3}, Landroid/os/Parcel;->writeNoException()V

    .line 151
    invoke-virtual {v10, v0}, Landroid/os/Parcel;->writeInt(I)V

    return v11

    .line 138
    :pswitch_f
    invoke-virtual {v1, v3}, Landroid/os/Parcel;->enforceInterface(Ljava/lang/String;)V

    .line 139
    invoke-virtual {p0}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService$Stub;->getStatus()I

    move-result v0

    .line 140
    invoke-virtual/range {p3 .. p3}, Landroid/os/Parcel;->writeNoException()V

    .line 141
    invoke-virtual {v10, v0}, Landroid/os/Parcel;->writeInt(I)V

    return v11

    .line 128
    :pswitch_10
    invoke-virtual {v1, v3}, Landroid/os/Parcel;->enforceInterface(Ljava/lang/String;)V

    .line 130
    invoke-virtual/range {p2 .. p2}, Landroid/os/Parcel;->readInt()I

    move-result v0

    .line 131
    invoke-virtual {p0, v0}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService$Stub;->tuneRadio(I)I

    move-result v0

    .line 132
    invoke-virtual/range {p3 .. p3}, Landroid/os/Parcel;->writeNoException()V

    .line 133
    invoke-virtual {v10, v0}, Landroid/os/Parcel;->writeInt(I)V

    return v11

    .line 115
    :pswitch_11
    invoke-virtual {v1, v3}, Landroid/os/Parcel;->enforceInterface(Ljava/lang/String;)V

    .line 117
    invoke-virtual/range {p2 .. p2}, Landroid/os/Parcel;->readInt()I

    move-result v0

    .line 119
    invoke-virtual/range {p2 .. p2}, Landroid/os/Parcel;->createCharArray()[C

    move-result-object v1

    .line 120
    invoke-virtual {p0, v0, v1}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService$Stub;->turnOnRadio(I[C)I

    move-result v0

    .line 121
    invoke-virtual/range {p3 .. p3}, Landroid/os/Parcel;->writeNoException()V

    .line 122
    invoke-virtual {v10, v0}, Landroid/os/Parcel;->writeInt(I)V

    .line 123
    invoke-virtual {v10, v1}, Landroid/os/Parcel;->writeCharArray([C)V

    return v11

    .line 107
    :pswitch_12
    invoke-virtual {v1, v3}, Landroid/os/Parcel;->enforceInterface(Ljava/lang/String;)V

    .line 108
    invoke-virtual {p0}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService$Stub;->turnOffRadio()I

    move-result v0

    .line 109
    invoke-virtual/range {p3 .. p3}, Landroid/os/Parcel;->writeNoException()V

    .line 110
    invoke-virtual {v10, v0}, Landroid/os/Parcel;->writeInt(I)V

    return v11

    .line 99
    :pswitch_13
    invoke-virtual {v1, v3}, Landroid/os/Parcel;->enforceInterface(Ljava/lang/String;)V

    .line 100
    invoke-virtual {p0}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService$Stub;->getIsMute()Z

    move-result v0

    .line 101
    invoke-virtual/range {p3 .. p3}, Landroid/os/Parcel;->writeNoException()V

    .line 102
    invoke-virtual {v10, v0}, Landroid/os/Parcel;->writeInt(I)V

    return v11

    .line 91
    :pswitch_14
    invoke-virtual {v1, v3}, Landroid/os/Parcel;->enforceInterface(Ljava/lang/String;)V

    .line 92
    invoke-virtual {p0}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService$Stub;->getTunedFrequency()I

    move-result v0

    .line 93
    invoke-virtual/range {p3 .. p3}, Landroid/os/Parcel;->writeNoException()V

    .line 94
    invoke-virtual {v10, v0}, Landroid/os/Parcel;->writeInt(I)V

    return v11

    .line 83
    :pswitch_15
    invoke-virtual {v1, v3}, Landroid/os/Parcel;->enforceInterface(Ljava/lang/String;)V

    .line 84
    invoke-virtual {p0}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService$Stub;->getMonoStereoMode()I

    move-result v0

    .line 85
    invoke-virtual/range {p3 .. p3}, Landroid/os/Parcel;->writeNoException()V

    .line 86
    invoke-virtual {v10, v0}, Landroid/os/Parcel;->writeInt(I)V

    return v11

    .line 75
    :pswitch_16
    invoke-virtual {v1, v3}, Landroid/os/Parcel;->enforceInterface(Ljava/lang/String;)V

    .line 76
    invoke-virtual {p0}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService$Stub;->getRadioIsOn()Z

    move-result v0

    .line 77
    invoke-virtual/range {p3 .. p3}, Landroid/os/Parcel;->writeNoException()V

    .line 78
    invoke-virtual {v10, v0}, Landroid/os/Parcel;->writeInt(I)V

    return v11

    .line 66
    :pswitch_17
    invoke-virtual {v1, v3}, Landroid/os/Parcel;->enforceInterface(Ljava/lang/String;)V

    .line 68
    invoke-virtual/range {p2 .. p2}, Landroid/os/Parcel;->readStrongBinder()Landroid/os/IBinder;

    move-result-object v0

    invoke-static {v0}, Lcom/broadcom/fm/fmreceiver/IFmReceiverCallback$Stub;->asInterface(Landroid/os/IBinder;)Lcom/broadcom/fm/fmreceiver/IFmReceiverCallback;

    move-result-object v0

    .line 69
    invoke-virtual {p0, v0}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService$Stub;->unregisterCallback(Lcom/broadcom/fm/fmreceiver/IFmReceiverCallback;)V

    .line 70
    invoke-virtual/range {p3 .. p3}, Landroid/os/Parcel;->writeNoException()V

    return v11

    .line 57
    :pswitch_18
    invoke-virtual {v1, v3}, Landroid/os/Parcel;->enforceInterface(Ljava/lang/String;)V

    .line 59
    invoke-virtual/range {p2 .. p2}, Landroid/os/Parcel;->readStrongBinder()Landroid/os/IBinder;

    move-result-object v0

    invoke-static {v0}, Lcom/broadcom/fm/fmreceiver/IFmReceiverCallback$Stub;->asInterface(Landroid/os/IBinder;)Lcom/broadcom/fm/fmreceiver/IFmReceiverCallback;

    move-result-object v0

    .line 60
    invoke-virtual {p0, v0}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService$Stub;->registerCallback(Lcom/broadcom/fm/fmreceiver/IFmReceiverCallback;)V

    .line 61
    invoke-virtual/range {p3 .. p3}, Landroid/os/Parcel;->writeNoException()V

    return v11

    .line 50
    :pswitch_19
    invoke-virtual {v1, v3}, Landroid/os/Parcel;->enforceInterface(Ljava/lang/String;)V

    .line 51
    invoke-virtual {p0}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService$Stub;->init()V

    .line 52
    invoke-virtual/range {p3 .. p3}, Landroid/os/Parcel;->writeNoException()V

    return v11

    .line 45
    :cond_3
    invoke-virtual {v10, v3}, Landroid/os/Parcel;->writeString(Ljava/lang/String;)V

    return v11

    :pswitch_data_0
    .packed-switch 0x1
        :pswitch_19
        :pswitch_18
        :pswitch_17
        :pswitch_16
        :pswitch_15
        :pswitch_14
        :pswitch_13
        :pswitch_12
        :pswitch_11
        :pswitch_10
        :pswitch_f
        :pswitch_e
        :pswitch_d
        :pswitch_c
        :pswitch_b
        :pswitch_a
        :pswitch_9
        :pswitch_8
        :pswitch_7
        :pswitch_6
        :pswitch_5
        :pswitch_4
        :pswitch_3
        :pswitch_2
        :pswitch_1
        :pswitch_0
    .end packed-switch
.end method
