.class public Lcom/android/fmradio/FmService;
.super Landroid/app/Service;
.source "FmService.java"


# annotations
.annotation system Ldalvik/annotation/MemberClasses;
    value = {
        Lcom/android/fmradio/FmService$OnExitListener;,
        Lcom/android/fmradio/FmService$FmRadioServiceHandler;,
        Lcom/android/fmradio/FmService$Record;,
        Lcom/android/fmradio/FmService$FmOnAudioPortUpdateListener;,
        Lcom/android/fmradio/FmService$FmServiceBroadcastReceiver;,
        Lcom/android/fmradio/FmService$ServiceBinder;
    }
.end annotation


# static fields
.field private static final CMDPAUSE:Ljava/lang/String; = "pause"

.field public static DURING_POWER_UP:I = 0x0

.field public static final FM_DECREASE:Ljava/lang/String; = "fmradio.decrease"

.field public static final FM_EXIT:Ljava/lang/String; = "fmradio.exit"

.field private static final FM_FREQUENCY:Ljava/lang/String; = "frequency"

.field public static final FM_IN:Ljava/lang/String; = "fmradio.enter"

.field public static final FM_INCREASE:Ljava/lang/String; = "fmradio.increase"

.field private static final FM_SEEK_NEXT:Ljava/lang/String; = "fmradio.seek.next"

.field private static final FM_SEEK_PREVIOUS:Ljava/lang/String; = "fmradio.seek.previous"

.field private static final FM_TURN_OFF:Ljava/lang/String; = "fmradio.turnoff"

.field private static final HEADSET_PLUG_IN:I = 0x1

.field private static final MSGID_STARTAUDIO_TRACK:I = -0x2

.field private static final OPTION:Ljava/lang/String; = "option"

.field public static POWER_DOWN:I = 0x0

.field public static POWER_UP:I = 0x0

.field private static final SOUND_POWER_DOWN_MSG:Ljava/lang/String; = "com.android.music.musicservicecommand"

.field private static final START_AUDIOTRACK_TIMES:I = 0xf

.field private static final TAG:Ljava/lang/String;

.field private static sExitListener:Lcom/android/fmradio/FmService$OnExitListener;


# instance fields
.field private mAudioFocusChangeListener:Landroid/media/AudioManager$OnAudioFocusChangeListener;

.field private mAudioManager:Landroid/media/AudioManager;

.field private mAudioPatch:Ljava/lang/Object;

.field private mAudioPortUpdateListener:Lcom/android/fmradio/FmService$FmOnAudioPortUpdateListener;

.field mAudioSink:Landroid/media/AudioDevicePort;

.field mAudioSource:Landroid/media/AudioDevicePort;

.field private final mBinder:Landroid/os/IBinder;

.field private mBroadcastReceiver:Lcom/android/fmradio/FmService$FmServiceBroadcastReceiver;

.field private mContext:Landroid/content/Context;

.field private mCurrentStation:I

.field private mFmManager:Lcom/android/fmradio/FmManagerSelect;

.field private mFmServiceHandler:Lcom/android/fmradio/FmService$FmRadioServiceHandler;

.field private mForcedUseForMedia:I

.field private mIMediaButtonListener:Lcom/android/fmradio/iface/IMediaButtonListener;

.field public mIsAudioFocusHeld:Z

.field private mIsDeviceOpen:Z

.field private mIsDistanceExceed:Z

.field private mIsFmMainForeground:Z

.field private mIsMuted:Z

.field private mIsNativeScanning:Z

.field private mIsNativeSeeking:Z

.field private mIsScanning:Z

.field private mIsSeeking:Z

.field private mIsServiceInited:Z

.field private mIsSpeakerUsed:Z

.field private mIsStopScanCalled:Z

.field private mMediaSession:Landroid/media/session/MediaSession;

.field private mPausedByTransientLossOfFocus:Z

.field private mPowerStatus:I

.field private mRecords:Ljava/util/ArrayList;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Ljava/util/ArrayList<",
            "Lcom/android/fmradio/FmService$Record;",
            ">;"
        }
    .end annotation
.end field

.field private mRenderLock:Ljava/lang/Object;

.field private mValueHeadSetPlug:I

.field private startAudioTrackTimes:I


# direct methods
.method static constructor <clinit>()V
    .locals 1

    .line 52
    const-class v0, Lcom/android/fmradio/FmService;

    invoke-virtual {v0}, Ljava/lang/Class;->getSimpleName()Ljava/lang/String;

    move-result-object v0

    sput-object v0, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    const/4 v0, 0x0

    .line 105
    sput v0, Lcom/android/fmradio/FmService;->POWER_UP:I

    const/4 v0, 0x1

    .line 106
    sput v0, Lcom/android/fmradio/FmService;->DURING_POWER_UP:I

    const/4 v0, 0x2

    .line 107
    sput v0, Lcom/android/fmradio/FmService;->POWER_DOWN:I

    const/4 v0, 0x0

    .line 135
    sput-object v0, Lcom/android/fmradio/FmService;->sExitListener:Lcom/android/fmradio/FmService$OnExitListener;

    return-void
.end method

.method public constructor <init>()V
    .locals 3

    .line 50
    invoke-direct {p0}, Landroid/app/Service;-><init>()V

    const/4 v0, 0x0

    .line 82
    iput v0, p0, Lcom/android/fmradio/FmService;->startAudioTrackTimes:I

    .line 85
    new-instance v1, Ljava/util/ArrayList;

    invoke-direct {v1}, Ljava/util/ArrayList;-><init>()V

    iput-object v1, p0, Lcom/android/fmradio/FmService;->mRecords:Ljava/util/ArrayList;

    .line 89
    iput-boolean v0, p0, Lcom/android/fmradio/FmService;->mIsNativeScanning:Z

    .line 91
    iput-boolean v0, p0, Lcom/android/fmradio/FmService;->mIsScanning:Z

    .line 93
    iput-boolean v0, p0, Lcom/android/fmradio/FmService;->mIsNativeSeeking:Z

    .line 95
    iput-boolean v0, p0, Lcom/android/fmradio/FmService;->mIsSeeking:Z

    .line 97
    iput-boolean v0, p0, Lcom/android/fmradio/FmService;->mIsStopScanCalled:Z

    .line 99
    iput-boolean v0, p0, Lcom/android/fmradio/FmService;->mIsSpeakerUsed:Z

    .line 101
    iput-boolean v0, p0, Lcom/android/fmradio/FmService;->mIsDeviceOpen:Z

    .line 103
    sget v1, Lcom/android/fmradio/FmService;->POWER_DOWN:I

    iput v1, p0, Lcom/android/fmradio/FmService;->mPowerStatus:I

    .line 109
    iput-boolean v0, p0, Lcom/android/fmradio/FmService;->mIsServiceInited:Z

    .line 112
    iput-boolean v0, p0, Lcom/android/fmradio/FmService;->mIsDistanceExceed:Z

    const/4 v1, 0x1

    .line 114
    iput-boolean v1, p0, Lcom/android/fmradio/FmService;->mIsFmMainForeground:Z

    const/4 v2, 0x0

    .line 117
    iput-object v2, p0, Lcom/android/fmradio/FmService;->mContext:Landroid/content/Context;

    .line 118
    iput-object v2, p0, Lcom/android/fmradio/FmService;->mAudioManager:Landroid/media/AudioManager;

    .line 121
    iput-boolean v0, p0, Lcom/android/fmradio/FmService;->mIsAudioFocusHeld:Z

    .line 123
    iput-boolean v0, p0, Lcom/android/fmradio/FmService;->mPausedByTransientLossOfFocus:Z

    .line 124
    iput v0, p0, Lcom/android/fmradio/FmService;->mCurrentStation:I

    .line 126
    iput v1, p0, Lcom/android/fmradio/FmService;->mValueHeadSetPlug:I

    .line 128
    new-instance v1, Lcom/android/fmradio/FmService$ServiceBinder;

    invoke-direct {v1, p0}, Lcom/android/fmradio/FmService$ServiceBinder;-><init>(Lcom/android/fmradio/FmService;)V

    iput-object v1, p0, Lcom/android/fmradio/FmService;->mBinder:Landroid/os/IBinder;

    .line 130
    iput-object v2, p0, Lcom/android/fmradio/FmService;->mBroadcastReceiver:Lcom/android/fmradio/FmService$FmServiceBroadcastReceiver;

    .line 137
    iput-boolean v0, p0, Lcom/android/fmradio/FmService;->mIsMuted:Z

    .line 141
    iput-object v2, p0, Lcom/android/fmradio/FmService;->mAudioPatch:Ljava/lang/Object;

    .line 143
    new-instance v0, Ljava/lang/Object;

    invoke-direct {v0}, Ljava/lang/Object;-><init>()V

    iput-object v0, p0, Lcom/android/fmradio/FmService;->mRenderLock:Ljava/lang/Object;

    .line 145
    iput-object v2, p0, Lcom/android/fmradio/FmService;->mFmManager:Lcom/android/fmradio/FmManagerSelect;

    .line 248
    iput-object v2, p0, Lcom/android/fmradio/FmService;->mAudioSource:Landroid/media/AudioDevicePort;

    .line 249
    iput-object v2, p0, Lcom/android/fmradio/FmService;->mAudioSink:Landroid/media/AudioDevicePort;

    .line 712
    iput-object v2, p0, Lcom/android/fmradio/FmService;->mAudioPortUpdateListener:Lcom/android/fmradio/FmService$FmOnAudioPortUpdateListener;

    .line 1090
    new-instance v0, Lcom/android/fmradio/FmService$2;

    invoke-direct {v0, p0}, Lcom/android/fmradio/FmService$2;-><init>(Lcom/android/fmradio/FmService;)V

    iput-object v0, p0, Lcom/android/fmradio/FmService;->mAudioFocusChangeListener:Landroid/media/AudioManager$OnAudioFocusChangeListener;

    return-void
.end method

.method static synthetic access$000()Ljava/lang/String;
    .locals 1

    .line 50
    sget-object v0, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    return-object v0
.end method

.method static synthetic access$100(Lcom/android/fmradio/FmService;)Lcom/android/fmradio/FmService$FmRadioServiceHandler;
    .locals 0

    .line 50
    iget-object p0, p0, Lcom/android/fmradio/FmService;->mFmServiceHandler:Lcom/android/fmradio/FmService$FmRadioServiceHandler;

    return-object p0
.end method

.method static synthetic access$1100(Lcom/android/fmradio/FmService;)I
    .locals 0

    .line 50
    iget p0, p0, Lcom/android/fmradio/FmService;->startAudioTrackTimes:I

    return p0
.end method

.method static synthetic access$1108(Lcom/android/fmradio/FmService;)I
    .locals 2

    .line 50
    iget v0, p0, Lcom/android/fmradio/FmService;->startAudioTrackTimes:I

    add-int/lit8 v1, v0, 0x1

    iput v1, p0, Lcom/android/fmradio/FmService;->startAudioTrackTimes:I

    return v0
.end method

.method static synthetic access$1200(Lcom/android/fmradio/FmService;)Z
    .locals 0

    .line 50
    iget-boolean p0, p0, Lcom/android/fmradio/FmService;->mIsNativeScanning:Z

    return p0
.end method

.method static synthetic access$1300(Lcom/android/fmradio/FmService;)Z
    .locals 0

    .line 50
    iget-boolean p0, p0, Lcom/android/fmradio/FmService;->mIsNativeSeeking:Z

    return p0
.end method

.method static synthetic access$1400(Lcom/android/fmradio/FmService;Landroid/os/Bundle;)V
    .locals 0

    .line 50
    invoke-direct {p0, p1}, Lcom/android/fmradio/FmService;->handlePowerUp(Landroid/os/Bundle;)V

    return-void
.end method

.method static synthetic access$1500(Lcom/android/fmradio/FmService;)V
    .locals 0

    .line 50
    invoke-direct {p0}, Lcom/android/fmradio/FmService;->handlePowerDown()V

    return-void
.end method

.method static synthetic access$1600(Lcom/android/fmradio/FmService;)Z
    .locals 0

    .line 50
    iget-boolean p0, p0, Lcom/android/fmradio/FmService;->mIsSpeakerUsed:Z

    return p0
.end method

.method static synthetic access$1700(Lcom/android/fmradio/FmService;Z)V
    .locals 0

    .line 50
    invoke-direct {p0, p1}, Lcom/android/fmradio/FmService;->setForceUse(Z)V

    return-void
.end method

.method static synthetic access$1800(Lcom/android/fmradio/FmService;)Z
    .locals 0

    .line 50
    invoke-direct {p0}, Lcom/android/fmradio/FmService;->powerDown()Z

    move-result p0

    return p0
.end method

.method static synthetic access$1900(Lcom/android/fmradio/FmService;)Z
    .locals 0

    .line 50
    invoke-direct {p0}, Lcom/android/fmradio/FmService;->closeDevice()Z

    move-result p0

    return p0
.end method

.method static synthetic access$200(Lcom/android/fmradio/FmService;)V
    .locals 0

    .line 50
    invoke-direct {p0}, Lcom/android/fmradio/FmService;->exitFm()V

    return-void
.end method

.method static synthetic access$2000(Lcom/android/fmradio/FmService;Landroid/os/Bundle;)V
    .locals 0

    .line 50
    invoke-direct {p0, p1}, Lcom/android/fmradio/FmService;->notifyActivityStateChanged(Landroid/os/Bundle;)V

    return-void
.end method

.method static synthetic access$2100()Lcom/android/fmradio/FmService$OnExitListener;
    .locals 1

    .line 50
    sget-object v0, Lcom/android/fmradio/FmService;->sExitListener:Lcom/android/fmradio/FmService$OnExitListener;

    return-object v0
.end method

.method static synthetic access$2200(Lcom/android/fmradio/FmService;F)Z
    .locals 0

    .line 50
    invoke-direct {p0, p1}, Lcom/android/fmradio/FmService;->tuneStation(F)Z

    move-result p0

    return p0
.end method

.method static synthetic access$2300(Lcom/android/fmradio/FmService;)Landroid/content/Context;
    .locals 0

    .line 50
    iget-object p0, p0, Lcom/android/fmradio/FmService;->mContext:Landroid/content/Context;

    return-object p0
.end method

.method static synthetic access$2400(Lcom/android/fmradio/FmService;)I
    .locals 0

    .line 50
    iget p0, p0, Lcom/android/fmradio/FmService;->mCurrentStation:I

    return p0
.end method

.method static synthetic access$2502(Lcom/android/fmradio/FmService;Z)Z
    .locals 0

    .line 50
    iput-boolean p1, p0, Lcom/android/fmradio/FmService;->mIsSeeking:Z

    return p1
.end method

.method static synthetic access$2600(Lcom/android/fmradio/FmService;FZ)F
    .locals 0

    .line 50
    invoke-direct {p0, p1, p2}, Lcom/android/fmradio/FmService;->seekStation(FZ)F

    move-result p0

    return p0
.end method

.method static synthetic access$2702(Lcom/android/fmradio/FmService;Z)Z
    .locals 0

    .line 50
    iput-boolean p1, p0, Lcom/android/fmradio/FmService;->mIsScanning:Z

    return p1
.end method

.method static synthetic access$2800(Lcom/android/fmradio/FmService;I)[I
    .locals 0

    .line 50
    invoke-direct {p0, p1}, Lcom/android/fmradio/FmService;->startScan(I)[I

    move-result-object p0

    return-object p0
.end method

.method static synthetic access$2900(Lcom/android/fmradio/FmService;)Ljava/util/ArrayList;
    .locals 0

    .line 50
    iget-object p0, p0, Lcom/android/fmradio/FmService;->mRecords:Ljava/util/ArrayList;

    return-object p0
.end method

.method static synthetic access$300(Lcom/android/fmradio/FmService;Z)V
    .locals 0

    .line 50
    invoke-direct {p0, p1}, Lcom/android/fmradio/FmService;->enableFmAudio(Z)V

    return-void
.end method

.method static synthetic access$3000(Lcom/android/fmradio/FmService;[I)[I
    .locals 0

    .line 50
    invoke-direct {p0, p1}, Lcom/android/fmradio/FmService;->updateStations([I)[I

    move-result-object p0

    return-object p0
.end method

.method static synthetic access$3100(Lcom/android/fmradio/FmService;Landroid/os/Bundle;)V
    .locals 0

    .line 50
    invoke-direct {p0, p1}, Lcom/android/fmradio/FmService;->notifyCurrentActivityStateChanged(Landroid/os/Bundle;)V

    return-void
.end method

.method static synthetic access$3200(Lcom/android/fmradio/FmService;I)V
    .locals 0

    .line 50
    invoke-direct {p0, p1}, Lcom/android/fmradio/FmService;->updateAudioFocus(I)V

    return-void
.end method

.method static synthetic access$3300(Lcom/android/fmradio/FmService;Z)I
    .locals 0

    .line 50
    invoke-direct {p0, p1}, Lcom/android/fmradio/FmService;->setRds(Z)I

    move-result p0

    return p0
.end method

.method static synthetic access$500(Lcom/android/fmradio/FmService;)Lcom/android/fmradio/iface/IMediaButtonListener;
    .locals 0

    .line 50
    iget-object p0, p0, Lcom/android/fmradio/FmService;->mIMediaButtonListener:Lcom/android/fmradio/iface/IMediaButtonListener;

    return-object p0
.end method

.method static synthetic access$600(Lcom/android/fmradio/FmService;)V
    .locals 0

    .line 50
    invoke-direct {p0}, Lcom/android/fmradio/FmService;->headSetNotActive()V

    return-void
.end method

.method static synthetic access$700(Lcom/android/fmradio/FmService;I)V
    .locals 0

    .line 50
    invoke-direct {p0, p1}, Lcom/android/fmradio/FmService;->focusChanged(I)V

    return-void
.end method

.method static synthetic access$800(Lcom/android/fmradio/FmService;)V
    .locals 0

    .line 50
    invoke-direct {p0}, Lcom/android/fmradio/FmService;->headSetActive()V

    return-void
.end method

.method static synthetic access$900(Lcom/android/fmradio/FmService;I)V
    .locals 0

    .line 50
    invoke-direct {p0, p1}, Lcom/android/fmradio/FmService;->updateAudioFocusAync(I)V

    return-void
.end method

.method private closeDevice()Z
    .locals 5

    .line 275
    sget-object v0, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "--11111-->>closeDevice() mIsDeviceOpen: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-boolean v2, p0, Lcom/android/fmradio/FmService;->mIsDeviceOpen:Z

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 276
    iget-boolean v0, p0, Lcom/android/fmradio/FmService;->mIsDeviceOpen:Z

    const/4 v1, 0x0

    if-eqz v0, :cond_0

    .line 277
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mFmManager:Lcom/android/fmradio/FmManagerSelect;

    invoke-virtual {v0}, Lcom/android/fmradio/FmManagerSelect;->closeDev()Z

    move-result v0

    .line 278
    sget-object v2, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    new-instance v3, Ljava/lang/StringBuilder;

    invoke-direct {v3}, Ljava/lang/StringBuilder;-><init>()V

    const-string v4, "--2222-->>closeDevice() isDeviceClose: "

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v3, v0}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    invoke-virtual {v3}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v3

    invoke-static {v2, v3}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 281
    iput-boolean v1, p0, Lcom/android/fmradio/FmService;->mIsDeviceOpen:Z

    goto :goto_0

    :cond_0
    move v0, v1

    :goto_0
    return v0
.end method

.method private declared-synchronized createAudioPatch()I
    .locals 3

    monitor-enter p0

    .line 685
    :try_start_0
    sget-object v0, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    const-string v1, "--->>createAudioPatch()"

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    const/4 v0, -0x1

    .line 687
    iget-object v1, p0, Lcom/android/fmradio/FmService;->mAudioPatch:Ljava/lang/Object;

    if-eqz v1, :cond_0

    .line 688
    sget-object v1, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    const-string v2, "createAudioPatch, mAudioPatch is not null, return"

    invoke-static {v1, v2}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 689
    monitor-exit p0

    return v0

    .line 691
    :cond_0
    :try_start_1
    new-instance v1, Ljava/lang/Object;

    invoke-direct {v1}, Ljava/lang/Object;-><init>()V

    iput-object v1, p0, Lcom/android/fmradio/FmService;->mAudioPatch:Ljava/lang/Object;
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    .line 709
    monitor-exit p0

    return v0

    :catchall_0
    move-exception v0

    monitor-exit p0

    throw v0
.end method

.method private enableFmAudio(Z)V
    .locals 3

    .line 901
    sget-object v0, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "--->>enableFmAudio() enable: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    const-string v2, " mAudioPatch: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-object v2, p0, Lcom/android/fmradio/FmService;->mAudioPatch:Ljava/lang/Object;

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    if-eqz p1, :cond_3

    .line 903
    iget p1, p0, Lcom/android/fmradio/FmService;->mPowerStatus:I

    sget v0, Lcom/android/fmradio/FmService;->POWER_UP:I

    if-ne p1, v0, :cond_2

    iget-boolean p1, p0, Lcom/android/fmradio/FmService;->mIsAudioFocusHeld:Z

    if-nez p1, :cond_0

    goto :goto_0

    .line 909
    :cond_0
    new-instance p1, Ljava/util/ArrayList;

    invoke-direct {p1}, Ljava/util/ArrayList;-><init>()V

    .line 910
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mAudioManager:Landroid/media/AudioManager;

    invoke-static {p1}, Landroid/media/AudioManager;->listAudioPatches(Ljava/util/ArrayList;)I

    .line 911
    sget-object v0, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    const-string v1, "mAudioPatch == null"

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 912
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mAudioPatch:Ljava/lang/Object;

    if-nez v0, :cond_4

    .line 913
    invoke-direct {p0, p1}, Lcom/android/fmradio/FmService;->isPatchMixerToEarphone(Ljava/util/ArrayList;)Z

    move-result p1

    if-eqz p1, :cond_1

    .line 915
    invoke-direct {p0}, Lcom/android/fmradio/FmService;->stopRender()V

    .line 916
    invoke-direct {p0}, Lcom/android/fmradio/FmService;->createAudioPatch()I

    move-result p1

    if-eqz p1, :cond_4

    .line 918
    sget-object p1, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    const-string v0, "enableFmAudio: fallback as createAudioPatch failed"

    invoke-static {p1, v0}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 919
    invoke-direct {p0}, Lcom/android/fmradio/FmService;->startRender()V

    goto :goto_1

    .line 922
    :cond_1
    invoke-direct {p0}, Lcom/android/fmradio/FmService;->startRender()V

    goto :goto_1

    .line 904
    :cond_2
    :goto_0
    sget-object p1, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    new-instance v0, Ljava/lang/StringBuilder;

    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    const-string v1, "enableFmAudio, current not available return.mIsAudioFocusHeld:"

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-boolean v1, p0, Lcom/android/fmradio/FmService;->mIsAudioFocusHeld:Z

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    invoke-static {p1, v0}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    return-void

    .line 926
    :cond_3
    invoke-direct {p0}, Lcom/android/fmradio/FmService;->releaseAudioPatch()V

    .line 927
    invoke-direct {p0}, Lcom/android/fmradio/FmService;->stopRender()V

    :cond_4
    :goto_1
    return-void
.end method

.method private exitFm()V
    .locals 3

    .line 812
    sget-object v0, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "---->>exitFm() mIsNativeScanning\uff1a"

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-boolean v2, p0, Lcom/android/fmradio/FmService;->mIsNativeScanning:Z

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    const-string v2, " mIsNativeSeeking: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-boolean v2, p0, Lcom/android/fmradio/FmService;->mIsNativeSeeking:Z

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    const/4 v0, 0x0

    .line 814
    iput-boolean v0, p0, Lcom/android/fmradio/FmService;->mIsAudioFocusHeld:Z

    .line 817
    iget-boolean v0, p0, Lcom/android/fmradio/FmService;->mIsNativeScanning:Z

    if-nez v0, :cond_0

    iget-boolean v0, p0, Lcom/android/fmradio/FmService;->mIsNativeSeeking:Z

    if-eqz v0, :cond_1

    .line 818
    :cond_0
    invoke-virtual {p0}, Lcom/android/fmradio/FmService;->stopScan()Z

    .line 821
    :cond_1
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mFmServiceHandler:Lcom/android/fmradio/FmService$FmRadioServiceHandler;

    const/4 v1, 0x0

    invoke-virtual {v0, v1}, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->removeCallbacksAndMessages(Ljava/lang/Object;)V

    .line 822
    sget-object v0, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    const-string v1, "---->>exitFm() MSGID_FM_EXIT"

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 823
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mFmServiceHandler:Lcom/android/fmradio/FmService$FmRadioServiceHandler;

    const/16 v1, 0xb

    invoke-virtual {v0, v1}, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->removeMessages(I)V

    .line 824
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mFmServiceHandler:Lcom/android/fmradio/FmService$FmRadioServiceHandler;

    invoke-virtual {v0, v1}, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->sendEmptyMessage(I)Z

    return-void
.end method

.method private firstPlaying(F)Z
    .locals 5

    .line 1758
    iget v0, p0, Lcom/android/fmradio/FmService;->mPowerStatus:I

    sget v1, Lcom/android/fmradio/FmService;->POWER_UP:I

    const/4 v2, 0x0

    if-eq v0, v1, :cond_0

    .line 1759
    sget-object p1, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    const-string v0, "firstPlaying, FM is not powered up"

    invoke-static {p1, v0}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    return v2

    .line 1765
    :cond_0
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mContext:Landroid/content/Context;

    invoke-static {v0, p1}, Lcom/android/fmradio/utils/FmUtils;->computeStation(Landroid/content/Context;F)I

    move-result v0

    .line 1766
    sget-object v1, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    new-instance v3, Ljava/lang/StringBuilder;

    invoke-direct {v3}, Ljava/lang/StringBuilder;-><init>()V

    const-string v4, "---->>firstPlaying()  firstPlaying: "

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v3, p1}, Ljava/lang/StringBuilder;->append(F)Ljava/lang/StringBuilder;

    invoke-virtual {v3}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v3

    invoke-static {v1, v3}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 1767
    iget-object v1, p0, Lcom/android/fmradio/FmService;->mContext:Landroid/content/Context;

    invoke-static {v1, v0}, Lcom/android/fmradio/utils/FmUtils;->isValidStation(Landroid/content/Context;I)Z

    move-result v0

    if-eqz v0, :cond_1

    .line 1768
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mFmManager:Lcom/android/fmradio/FmManagerSelect;

    invoke-virtual {v0, p1}, Lcom/android/fmradio/FmManagerSelect;->tuneRadio(F)Z

    move-result v2

    if-eqz v2, :cond_1

    .line 1770
    invoke-direct {p0, p1}, Lcom/android/fmradio/FmService;->playFrequency(F)Z

    :cond_1
    if-nez v2, :cond_2

    .line 1779
    iget-object p1, p0, Lcom/android/fmradio/FmService;->mContext:Landroid/content/Context;

    iget v0, p0, Lcom/android/fmradio/FmService;->mCurrentStation:I

    invoke-static {p1, v0}, Lcom/android/fmradio/utils/FmUtils;->computeFrequency(Landroid/content/Context;I)F

    :cond_2
    return v2
.end method

.method private fm_in()V
    .locals 4

    .line 871
    invoke-direct {p0}, Lcom/android/fmradio/FmService;->registerFmBroadcastReceiver()V

    .line 873
    invoke-direct {p0}, Lcom/android/fmradio/FmService;->openDevice()Z

    .line 875
    iget-boolean v0, p0, Lcom/android/fmradio/FmService;->mIsSpeakerUsed:Z

    invoke-direct {p0, v0}, Lcom/android/fmradio/FmService;->setForceUse(Z)V

    const/4 v0, 0x0

    .line 877
    iput v0, p0, Lcom/android/fmradio/FmService;->startAudioTrackTimes:I

    .line 878
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mFmServiceHandler:Lcom/android/fmradio/FmService$FmRadioServiceHandler;

    const/4 v1, -0x2

    const-wide/16 v2, 0x3e8

    invoke-virtual {v0, v1, v2, v3}, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->sendEmptyMessageDelayed(IJ)Z

    return-void
.end method

.method private fm_out()V
    .locals 2

    .line 882
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mFmServiceHandler:Lcom/android/fmradio/FmService$FmRadioServiceHandler;

    const/4 v1, -0x2

    invoke-virtual {v0, v1}, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->removeMessages(I)V

    const/4 v0, 0x1

    .line 884
    invoke-virtual {p0, v0}, Lcom/android/fmradio/FmService;->setMute(Z)I

    .line 886
    invoke-direct {p0}, Lcom/android/fmradio/FmService;->unregisterFmBroadcastReceiver()V

    .line 888
    invoke-direct {p0}, Lcom/android/fmradio/FmService;->headSetNotActive()V

    .line 889
    invoke-virtual {p0}, Lcom/android/fmradio/FmService;->abandonAudioFocus()V

    .line 890
    invoke-direct {p0}, Lcom/android/fmradio/FmService;->exitFm()V

    .line 892
    invoke-direct {p0}, Lcom/android/fmradio/FmService;->releaseAudioPatch()V

    return-void
.end method

.method private focusChanged(I)V
    .locals 1

    const/4 v0, 0x0

    .line 1032
    iput-boolean v0, p0, Lcom/android/fmradio/FmService;->mIsAudioFocusHeld:Z

    .line 1033
    iget-boolean v0, p0, Lcom/android/fmradio/FmService;->mIsNativeScanning:Z

    if-nez v0, :cond_0

    iget-boolean v0, p0, Lcom/android/fmradio/FmService;->mIsNativeSeeking:Z

    .line 1040
    :cond_0
    invoke-direct {p0, p1}, Lcom/android/fmradio/FmService;->updateAudioFocusAync(I)V

    return-void
.end method

.method private forceToHeadsetMode()V
    .locals 2

    .line 1219
    iget-boolean v0, p0, Lcom/android/fmradio/FmService;->mIsSpeakerUsed:Z

    if-eqz v0, :cond_0

    invoke-direct {p0}, Lcom/android/fmradio/FmService;->isHeadSetIn()Z

    move-result v0

    if-eqz v0, :cond_0

    .line 1220
    sget-object v0, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    const-string v1, "forceToHeadsetMode"

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 1224
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mContext:Landroid/content/Context;

    const/4 v1, 0x1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/FmUtils;->setIsSpeakerModeOnFocusLost(Landroid/content/Context;Z)V

    :cond_0
    return-void
.end method

.method private handlePowerDown()V
    .locals 3

    .line 1697
    sget-object v0, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    const-string v1, "--->>handlePowerDown() "

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 1699
    invoke-direct {p0}, Lcom/android/fmradio/FmService;->powerDown()Z

    .line 1700
    new-instance v0, Landroid/os/Bundle;

    const/4 v1, 0x1

    invoke-direct {v0, v1}, Landroid/os/Bundle;-><init>(I)V

    const-string v1, "callback_flag"

    const/16 v2, 0xa

    .line 1701
    invoke-virtual {v0, v1, v2}, Landroid/os/Bundle;->putInt(Ljava/lang/String;I)V

    .line 1702
    invoke-direct {p0, v0}, Lcom/android/fmradio/FmService;->notifyActivityStateChanged(Landroid/os/Bundle;)V

    return-void
.end method

.method private handlePowerUp(Landroid/os/Bundle;)V
    .locals 2

    .line 1711
    sget-object v0, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    const-string v1, "----->>handlePowerUp() "

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    const-string v0, "frequency"

    .line 1713
    invoke-virtual {p1, v0}, Landroid/os/Bundle;->getFloat(Ljava/lang/String;)F

    move-result p1

    .line 1715
    invoke-direct {p0, p1}, Lcom/android/fmradio/FmService;->powerUp(F)Z

    move-result v0

    if-eqz v0, :cond_0

    .line 1716
    invoke-direct {p0, p1}, Lcom/android/fmradio/FmService;->firstPlaying(F)Z

    const/4 p1, 0x0

    .line 1718
    iput-boolean p1, p0, Lcom/android/fmradio/FmService;->mPausedByTransientLossOfFocus:Z

    .line 1720
    :cond_0
    new-instance p1, Landroid/os/Bundle;

    const/4 v0, 0x2

    invoke-direct {p1, v0}, Landroid/os/Bundle;-><init>(I)V

    const/16 v0, 0x9

    const-string v1, "callback_flag"

    .line 1721
    invoke-virtual {p1, v1, v0}, Landroid/os/Bundle;->putInt(Ljava/lang/String;I)V

    .line 1722
    iget v0, p0, Lcom/android/fmradio/FmService;->mCurrentStation:I

    const-string v1, "key_tune_to_station"

    invoke-virtual {p1, v1, v0}, Landroid/os/Bundle;->putInt(Ljava/lang/String;I)V

    .line 1723
    invoke-direct {p0, p1}, Lcom/android/fmradio/FmService;->notifyActivityStateChanged(Landroid/os/Bundle;)V

    return-void
.end method

.method private headSetActive()V
    .locals 2

    .line 1015
    sget-object v0, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    const-string v1, "---->>onMediaButtonEvent()"

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 1016
    invoke-direct {p0}, Lcom/android/fmradio/FmService;->headSetImplApi23()V

    .line 1018
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mMediaSession:Landroid/media/session/MediaSession;

    invoke-virtual {v0}, Landroid/media/session/MediaSession;->isActive()Z

    move-result v0

    if-nez v0, :cond_0

    .line 1019
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mMediaSession:Landroid/media/session/MediaSession;

    const/4 v1, 0x1

    invoke-virtual {v0, v1}, Landroid/media/session/MediaSession;->setActive(Z)V

    :cond_0
    return-void
.end method

.method private headSetImplApi23()V
    .locals 5

    .line 976
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mMediaSession:Landroid/media/session/MediaSession;

    if-nez v0, :cond_0

    .line 977
    new-instance v0, Landroid/media/session/MediaSession;

    iget-object v1, p0, Lcom/android/fmradio/FmService;->mContext:Landroid/content/Context;

    const-string v2, "FmRadioApp"

    invoke-direct {v0, v1, v2}, Landroid/media/session/MediaSession;-><init>(Landroid/content/Context;Ljava/lang/String;)V

    iput-object v0, p0, Lcom/android/fmradio/FmService;->mMediaSession:Landroid/media/session/MediaSession;

    .line 978
    sget-object v0, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "---->>onMediaButtonEvent()  headSetImplApi23 - mMediaSession: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-object v2, p0, Lcom/android/fmradio/FmService;->mMediaSession:Landroid/media/session/MediaSession;

    invoke-virtual {v2}, Ljava/lang/Object;->toString()Ljava/lang/String;

    move-result-object v2

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 984
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mMediaSession:Landroid/media/session/MediaSession;

    const v1, 0x10001

    invoke-virtual {v0, v1}, Landroid/media/session/MediaSession;->setFlags(I)V

    .line 985
    new-instance v0, Landroid/media/session/PlaybackState$Builder;

    invoke-direct {v0}, Landroid/media/session/PlaybackState$Builder;-><init>()V

    const-wide/16 v1, 0x277

    .line 986
    invoke-virtual {v0, v1, v2}, Landroid/media/session/PlaybackState$Builder;->setActions(J)Landroid/media/session/PlaybackState$Builder;

    const/4 v1, 0x3

    const-wide/16 v2, 0x1

    const/high16 v4, 0x3f800000    # 1.0f

    .line 990
    invoke-virtual {v0, v1, v2, v3, v4}, Landroid/media/session/PlaybackState$Builder;->setState(IJF)Landroid/media/session/PlaybackState$Builder;

    .line 991
    iget-object v1, p0, Lcom/android/fmradio/FmService;->mMediaSession:Landroid/media/session/MediaSession;

    invoke-virtual {v0}, Landroid/media/session/PlaybackState$Builder;->build()Landroid/media/session/PlaybackState;

    move-result-object v0

    invoke-virtual {v1, v0}, Landroid/media/session/MediaSession;->setPlaybackState(Landroid/media/session/PlaybackState;)V

    .line 992
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mMediaSession:Landroid/media/session/MediaSession;

    new-instance v1, Lcom/android/fmradio/FmService$1;

    invoke-direct {v1, p0}, Lcom/android/fmradio/FmService$1;-><init>(Lcom/android/fmradio/FmService;)V

    invoke-virtual {v0, v1}, Landroid/media/session/MediaSession;->setCallback(Landroid/media/session/MediaSession$Callback;)V

    :cond_0
    return-void
.end method

.method private headSetNotActive()V
    .locals 2

    .line 1024
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mMediaSession:Landroid/media/session/MediaSession;

    if-eqz v0, :cond_0

    const/4 v1, 0x0

    .line 1025
    invoke-virtual {v0, v1}, Landroid/media/session/MediaSession;->setActive(Z)V

    .line 1026
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mMediaSession:Landroid/media/session/MediaSession;

    invoke-virtual {v0}, Landroid/media/session/MediaSession;->release()V

    const/4 v0, 0x0

    .line 1027
    iput-object v0, p0, Lcom/android/fmradio/FmService;->mMediaSession:Landroid/media/session/MediaSession;

    :cond_0
    return-void
.end method

.method private isHeadSetIn()Z
    .locals 1

    .line 963
    iget v0, p0, Lcom/android/fmradio/FmService;->mValueHeadSetPlug:I

    if-nez v0, :cond_0

    const/4 v0, 0x1

    goto :goto_0

    :cond_0
    const/4 v0, 0x0

    :goto_0
    return v0
.end method

.method private isPatchMixerToEarphone(Ljava/util/ArrayList;)Z
    .locals 8
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Ljava/util/ArrayList<",
            "Landroid/media/AudioPatch;",
            ">;)Z"
        }
    .end annotation

    .line 935
    invoke-virtual {p1}, Ljava/util/ArrayList;->iterator()Ljava/util/Iterator;

    move-result-object p1

    const/4 v0, 0x0

    move v1, v0

    move v2, v1

    :cond_0
    :goto_0
    invoke-interface {p1}, Ljava/util/Iterator;->hasNext()Z

    move-result v3

    if-eqz v3, :cond_2

    invoke-interface {p1}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v3

    check-cast v3, Landroid/media/AudioPatch;

    .line 936
    invoke-virtual {v3}, Landroid/media/AudioPatch;->sources()[Landroid/media/AudioPortConfig;

    move-result-object v4

    .line 937
    invoke-virtual {v3}, Landroid/media/AudioPatch;->sinks()[Landroid/media/AudioPortConfig;

    move-result-object v3

    .line 938
    aget-object v4, v4, v0

    .line 939
    aget-object v3, v3, v0

    .line 940
    invoke-virtual {v4}, Landroid/media/AudioPortConfig;->port()Landroid/media/AudioPort;

    move-result-object v4

    .line 941
    invoke-virtual {v3}, Landroid/media/AudioPortConfig;->port()Landroid/media/AudioPort;

    move-result-object v3

    .line 942
    sget-object v5, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    new-instance v6, Ljava/lang/StringBuilder;

    invoke-direct {v6}, Ljava/lang/StringBuilder;-><init>()V

    const-string v7, "isPatchMixerToEarphone "

    invoke-virtual {v6, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v6, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    const-string v7, " ====> "

    invoke-virtual {v6, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v6, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    invoke-virtual {v6}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v6

    invoke-static {v5, v6}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 943
    instance-of v4, v4, Landroid/media/AudioMixPort;

    if-eqz v4, :cond_0

    instance-of v4, v3, Landroid/media/AudioDevicePort;

    if-eqz v4, :cond_0

    add-int/lit8 v2, v2, 0x1

    .line 945
    check-cast v3, Landroid/media/AudioDevicePort;

    invoke-virtual {v3}, Landroid/media/AudioDevicePort;->type()I

    move-result v3

    const/4 v4, 0x4

    if-eq v3, v4, :cond_1

    const/16 v4, 0x8

    if-ne v3, v4, :cond_0

    :cond_1
    add-int/lit8 v1, v1, 0x1

    goto :goto_0

    :cond_2
    const/4 p1, 0x1

    if-ne v1, p1, :cond_3

    if-ne v2, v1, :cond_3

    return p1

    :cond_3
    return v0
.end method

.method private isSpeakerPhoneOn()Z
    .locals 2

    .line 252
    iget v0, p0, Lcom/android/fmradio/FmService;->mForcedUseForMedia:I

    const/4 v1, 0x1

    if-ne v0, v1, :cond_0

    goto :goto_0

    :cond_0
    const/4 v1, 0x0

    :goto_0
    return v1
.end method

.method private notifyActivityStateChanged(Landroid/os/Bundle;)V
    .locals 3

    .line 1268
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mRecords:Ljava/util/ArrayList;

    invoke-virtual {v0}, Ljava/util/ArrayList;->isEmpty()Z

    move-result v0

    if-nez v0, :cond_2

    .line 1269
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mRecords:Ljava/util/ArrayList;

    monitor-enter v0

    .line 1270
    :try_start_0
    sget-object v1, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    const-string v2, "---->>notifyActivityStateChanged()"

    invoke-static {v1, v2}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 1271
    iget-object v1, p0, Lcom/android/fmradio/FmService;->mRecords:Ljava/util/ArrayList;

    invoke-virtual {v1}, Ljava/util/ArrayList;->iterator()Ljava/util/Iterator;

    move-result-object v1

    .line 1272
    :goto_0
    invoke-interface {v1}, Ljava/util/Iterator;->hasNext()Z

    move-result v2

    if-eqz v2, :cond_1

    .line 1273
    invoke-interface {v1}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v2

    check-cast v2, Lcom/android/fmradio/FmService$Record;

    .line 1275
    iget-object v2, v2, Lcom/android/fmradio/FmService$Record;->mCallback:Lcom/android/fmradio/iface/FmListener;

    if-nez v2, :cond_0

    .line 1278
    invoke-interface {v1}, Ljava/util/Iterator;->remove()V

    .line 1279
    monitor-exit v0

    return-void

    .line 1282
    :cond_0
    invoke-interface {v2, p1}, Lcom/android/fmradio/iface/FmListener;->onCallBack(Landroid/os/Bundle;)V

    goto :goto_0

    .line 1284
    :cond_1
    monitor-exit v0

    goto :goto_1

    :catchall_0
    move-exception p1

    monitor-exit v0
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    throw p1

    :cond_2
    :goto_1
    return-void
.end method

.method private notifyCurrentActivityStateChanged(Landroid/os/Bundle;)V
    .locals 3

    .line 1295
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mRecords:Ljava/util/ArrayList;

    invoke-virtual {v0}, Ljava/util/ArrayList;->isEmpty()Z

    move-result v0

    if-nez v0, :cond_2

    .line 1296
    sget-object v0, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "notifyCurrentActivityStateChanged = "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-object v2, p0, Lcom/android/fmradio/FmService;->mRecords:Ljava/util/ArrayList;

    invoke-virtual {v2}, Ljava/util/ArrayList;->size()I

    move-result v2

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 1297
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mRecords:Ljava/util/ArrayList;

    monitor-enter v0

    .line 1298
    :try_start_0
    iget-object v1, p0, Lcom/android/fmradio/FmService;->mRecords:Ljava/util/ArrayList;

    invoke-virtual {v1}, Ljava/util/ArrayList;->size()I

    move-result v1

    if-lez v1, :cond_1

    .line 1299
    iget-object v1, p0, Lcom/android/fmradio/FmService;->mRecords:Ljava/util/ArrayList;

    iget-object v2, p0, Lcom/android/fmradio/FmService;->mRecords:Ljava/util/ArrayList;

    invoke-virtual {v2}, Ljava/util/ArrayList;->size()I

    move-result v2

    add-int/lit8 v2, v2, -0x1

    invoke-virtual {v1, v2}, Ljava/util/ArrayList;->get(I)Ljava/lang/Object;

    move-result-object v1

    check-cast v1, Lcom/android/fmradio/FmService$Record;

    .line 1300
    iget-object v2, v1, Lcom/android/fmradio/FmService$Record;->mCallback:Lcom/android/fmradio/iface/FmListener;

    if-nez v2, :cond_0

    .line 1302
    iget-object p1, p0, Lcom/android/fmradio/FmService;->mRecords:Ljava/util/ArrayList;

    invoke-virtual {p1, v1}, Ljava/util/ArrayList;->remove(Ljava/lang/Object;)Z

    .line 1303
    monitor-exit v0

    return-void

    .line 1305
    :cond_0
    invoke-interface {v2, p1}, Lcom/android/fmradio/iface/FmListener;->onCallBack(Landroid/os/Bundle;)V

    .line 1307
    :cond_1
    monitor-exit v0

    goto :goto_0

    :catchall_0
    move-exception p1

    monitor-exit v0
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    throw p1

    :cond_2
    :goto_0
    return-void
.end method

.method private openDevice()Z
    .locals 2

    .line 261
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mFmServiceHandler:Lcom/android/fmradio/FmService$FmRadioServiceHandler;

    const/16 v1, 0xb

    invoke-virtual {v0, v1}, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->removeMessages(I)V

    .line 262
    iget-boolean v0, p0, Lcom/android/fmradio/FmService;->mIsDeviceOpen:Z

    if-nez v0, :cond_0

    .line 263
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mFmManager:Lcom/android/fmradio/FmManagerSelect;

    invoke-virtual {v0}, Lcom/android/fmradio/FmManagerSelect;->openDev()Z

    move-result v0

    iput-boolean v0, p0, Lcom/android/fmradio/FmService;->mIsDeviceOpen:Z

    .line 265
    :cond_0
    iget-boolean v0, p0, Lcom/android/fmradio/FmService;->mIsDeviceOpen:Z

    return v0
.end method

.method private playFrequency(F)Z
    .locals 3

    .line 344
    sget-object v0, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "---->>playFrequency() frequency: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(F)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 345
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mContext:Landroid/content/Context;

    invoke-static {v0, p1}, Lcom/android/fmradio/utils/FmUtils;->computeStation(Landroid/content/Context;F)I

    move-result p1

    iput p1, p0, Lcom/android/fmradio/FmService;->mCurrentStation:I

    .line 346
    iget-object p1, p0, Lcom/android/fmradio/FmService;->mContext:Landroid/content/Context;

    iget v0, p0, Lcom/android/fmradio/FmService;->mCurrentStation:I

    invoke-static {p1, v0}, Lcom/android/fmradio/database/FmStation;->setCurrentStation(Landroid/content/Context;I)V

    .line 348
    iget-boolean p1, p0, Lcom/android/fmradio/FmService;->mIsSpeakerUsed:Z

    invoke-direct {p0}, Lcom/android/fmradio/FmService;->isSpeakerPhoneOn()Z

    move-result v0

    if-eq p1, v0, :cond_0

    .line 349
    iget-boolean p1, p0, Lcom/android/fmradio/FmService;->mIsSpeakerUsed:Z

    invoke-direct {p0, p1}, Lcom/android/fmradio/FmService;->setForceUse(Z)V

    :cond_0
    const/4 p1, 0x1

    .line 352
    invoke-direct {p0, p1}, Lcom/android/fmradio/FmService;->enableFmAudio(Z)V

    .line 354
    invoke-direct {p0, p1}, Lcom/android/fmradio/FmService;->setRds(Z)I

    const/4 v0, 0x0

    .line 355
    invoke-virtual {p0, v0}, Lcom/android/fmradio/FmService;->setMute(Z)I

    .line 357
    iget v1, p0, Lcom/android/fmradio/FmService;->mPowerStatus:I

    sget v2, Lcom/android/fmradio/FmService;->POWER_UP:I

    if-ne v1, v2, :cond_1

    goto :goto_0

    :cond_1
    move p1, v0

    :goto_0
    return p1
.end method

.method private powerDown()Z
    .locals 3

    .line 382
    sget-object v0, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "---->>powerDown()  mPowerStatus: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget v2, p0, Lcom/android/fmradio/FmService;->mPowerStatus:I

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 383
    iget v0, p0, Lcom/android/fmradio/FmService;->mPowerStatus:I

    sget v1, Lcom/android/fmradio/FmService;->POWER_DOWN:I

    const/4 v2, 0x1

    if-ne v0, v1, :cond_0

    return v2

    .line 387
    :cond_0
    invoke-virtual {p0, v2}, Lcom/android/fmradio/FmService;->setMute(Z)I

    const/4 v0, 0x0

    .line 388
    invoke-direct {p0, v0}, Lcom/android/fmradio/FmService;->setRds(Z)I

    .line 389
    invoke-direct {p0, v0}, Lcom/android/fmradio/FmService;->enableFmAudio(Z)V

    .line 391
    iget-object v1, p0, Lcom/android/fmradio/FmService;->mFmManager:Lcom/android/fmradio/FmManagerSelect;

    invoke-virtual {v1}, Lcom/android/fmradio/FmManagerSelect;->powerDown()Z

    move-result v1

    if-nez v1, :cond_1

    return v0

    .line 395
    :cond_1
    sget v0, Lcom/android/fmradio/FmService;->POWER_DOWN:I

    iput v0, p0, Lcom/android/fmradio/FmService;->mPowerStatus:I

    return v2
.end method

.method private powerUp(F)Z
    .locals 3

    .line 317
    sget-object v0, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "---->>powerUp() frequency: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(F)Ljava/lang/StringBuilder;

    const-string v2, " mPowerStatus: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget v2, p0, Lcom/android/fmradio/FmService;->mPowerStatus:I

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 318
    iget v0, p0, Lcom/android/fmradio/FmService;->mPowerStatus:I

    sget v1, Lcom/android/fmradio/FmService;->POWER_UP:I

    const/4 v2, 0x1

    if-ne v0, v1, :cond_0

    return v2

    .line 322
    :cond_0
    sget v0, Lcom/android/fmradio/FmService;->DURING_POWER_UP:I

    iput v0, p0, Lcom/android/fmradio/FmService;->mPowerStatus:I

    .line 326
    iget-boolean v0, p0, Lcom/android/fmradio/FmService;->mIsDeviceOpen:Z

    if-nez v0, :cond_1

    .line 327
    invoke-direct {p0}, Lcom/android/fmradio/FmService;->openDevice()Z

    .line 330
    :cond_1
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mFmManager:Lcom/android/fmradio/FmManagerSelect;

    invoke-virtual {v0, p1}, Lcom/android/fmradio/FmManagerSelect;->powerUp(F)Z

    move-result p1

    const/4 v0, 0x0

    if-nez p1, :cond_2

    .line 332
    sget p1, Lcom/android/fmradio/FmService;->POWER_DOWN:I

    iput p1, p0, Lcom/android/fmradio/FmService;->mPowerStatus:I

    return v0

    .line 336
    :cond_2
    sget p1, Lcom/android/fmradio/FmService;->POWER_UP:I

    iput p1, p0, Lcom/android/fmradio/FmService;->mPowerStatus:I

    .line 338
    invoke-virtual {p0, v2}, Lcom/android/fmradio/FmService;->setMute(Z)I

    .line 340
    iget p1, p0, Lcom/android/fmradio/FmService;->mPowerStatus:I

    sget v1, Lcom/android/fmradio/FmService;->POWER_UP:I

    if-ne p1, v1, :cond_3

    move v0, v2

    :cond_3
    return v0
.end method

.method private registerFmBroadcastReceiver()V
    .locals 3

    .line 757
    new-instance v0, Landroid/content/IntentFilter;

    invoke-direct {v0}, Landroid/content/IntentFilter;-><init>()V

    const-string v1, "com.android.music.musicservicecommand"

    .line 758
    invoke-virtual {v0, v1}, Landroid/content/IntentFilter;->addAction(Ljava/lang/String;)V

    const-string v1, "android.intent.action.ACTION_SHUTDOWN"

    .line 759
    invoke-virtual {v0, v1}, Landroid/content/IntentFilter;->addAction(Ljava/lang/String;)V

    const-string v1, "android.intent.action.SCREEN_ON"

    .line 760
    invoke-virtual {v0, v1}, Landroid/content/IntentFilter;->addAction(Ljava/lang/String;)V

    const-string v1, "android.intent.action.SCREEN_OFF"

    .line 761
    invoke-virtual {v0, v1}, Landroid/content/IntentFilter;->addAction(Ljava/lang/String;)V

    const-string v1, "android.media.VOLUME_CHANGED_ACTION"

    .line 762
    invoke-virtual {v0, v1}, Landroid/content/IntentFilter;->addAction(Ljava/lang/String;)V

    .line 763
    new-instance v1, Lcom/android/fmradio/FmService$FmServiceBroadcastReceiver;

    const/4 v2, 0x0

    invoke-direct {v1, p0, v2}, Lcom/android/fmradio/FmService$FmServiceBroadcastReceiver;-><init>(Lcom/android/fmradio/FmService;Lcom/android/fmradio/FmService$1;)V

    iput-object v1, p0, Lcom/android/fmradio/FmService;->mBroadcastReceiver:Lcom/android/fmradio/FmService$FmServiceBroadcastReceiver;

    .line 764
    iget-object v1, p0, Lcom/android/fmradio/FmService;->mBroadcastReceiver:Lcom/android/fmradio/FmService$FmServiceBroadcastReceiver;

    invoke-virtual {p0, v1, v0}, Lcom/android/fmradio/FmService;->registerReceiver(Landroid/content/BroadcastReceiver;Landroid/content/IntentFilter;)Landroid/content/Intent;

    return-void
.end method

.method private declared-synchronized releaseAudioPatch()V
    .locals 3

    monitor-enter p0

    .line 745
    :try_start_0
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mAudioPatch:Ljava/lang/Object;

    const/4 v1, 0x0

    if-eqz v0, :cond_0

    .line 746
    sget-object v0, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    const-string v2, "releaseAudioPatch"

    invoke-static {v0, v2}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 750
    iput-object v1, p0, Lcom/android/fmradio/FmService;->mAudioPatch:Ljava/lang/Object;

    .line 752
    :cond_0
    iput-object v1, p0, Lcom/android/fmradio/FmService;->mAudioSource:Landroid/media/AudioDevicePort;

    .line 753
    iput-object v1, p0, Lcom/android/fmradio/FmService;->mAudioSink:Landroid/media/AudioDevicePort;
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 754
    monitor-exit p0

    return-void

    :catchall_0
    move-exception v0

    monitor-exit p0

    throw v0
.end method

.method private remove(I)V
    .locals 3

    .line 1326
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mRecords:Ljava/util/ArrayList;

    monitor-enter v0

    .line 1327
    :try_start_0
    iget-object v1, p0, Lcom/android/fmradio/FmService;->mRecords:Ljava/util/ArrayList;

    invoke-virtual {v1}, Ljava/util/ArrayList;->iterator()Ljava/util/Iterator;

    move-result-object v1

    .line 1328
    :cond_0
    :goto_0
    invoke-interface {v1}, Ljava/util/Iterator;->hasNext()Z

    move-result v2

    if-eqz v2, :cond_1

    .line 1329
    invoke-interface {v1}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v2

    check-cast v2, Lcom/android/fmradio/FmService$Record;

    .line 1330
    iget v2, v2, Lcom/android/fmradio/FmService$Record;->mHashCode:I

    if-ne v2, p1, :cond_0

    .line 1331
    invoke-interface {v1}, Ljava/util/Iterator;->remove()V

    goto :goto_0

    .line 1334
    :cond_1
    monitor-exit v0

    return-void

    :catchall_0
    move-exception p1

    monitor-exit v0
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    throw p1
.end method

.method private seekStation(FZ)F
    .locals 3

    .line 476
    sget-object v0, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "---->>seekStation()  frequency: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(F)Ljava/lang/StringBuilder;

    const-string v2, " isUp: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p2}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    const-string v2, " mPowerStatus: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget v2, p0, Lcom/android/fmradio/FmService;->mPowerStatus:I

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 478
    iget v0, p0, Lcom/android/fmradio/FmService;->mPowerStatus:I

    sget v1, Lcom/android/fmradio/FmService;->POWER_UP:I

    if-eq v0, v1, :cond_0

    const/high16 p1, -0x40800000    # -1.0f

    return p1

    :cond_0
    const/4 v0, 0x0

    .line 482
    invoke-direct {p0, v0}, Lcom/android/fmradio/FmService;->setRds(Z)I

    const/4 v1, 0x1

    .line 483
    iput-boolean v1, p0, Lcom/android/fmradio/FmService;->mIsNativeSeeking:Z

    .line 485
    iget-object v1, p0, Lcom/android/fmradio/FmService;->mFmManager:Lcom/android/fmradio/FmManagerSelect;

    invoke-virtual {v1, p1, p2}, Lcom/android/fmradio/FmManagerSelect;->seekStation(FZ)F

    move-result p1

    .line 486
    iput-boolean v0, p0, Lcom/android/fmradio/FmService;->mIsNativeSeeking:Z

    .line 489
    iput-boolean v0, p0, Lcom/android/fmradio/FmService;->mIsStopScanCalled:Z

    return p1
.end method

.method private setForceUse(Z)V
    .locals 3

    .line 225
    sget-object v0, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "--->>setForceUseSpeaker() isSpeaker: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 227
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mFmManager:Lcom/android/fmradio/FmManagerSelect;

    sget-object v1, Lcom/android/fmradio/FmConstants$AudioPath;->FM_AUDIO_PATH_SPEAKER:Lcom/android/fmradio/FmConstants$AudioPath;

    invoke-virtual {v0, v1, p1}, Lcom/android/fmradio/FmManagerSelect;->setSpeakerEnable(Lcom/android/fmradio/FmConstants$AudioPath;Z)Z

    .line 229
    iput-boolean p1, p0, Lcom/android/fmradio/FmService;->mIsSpeakerUsed:Z

    return-void
.end method

.method private setRds(Z)I
    .locals 3

    .line 584
    iget v0, p0, Lcom/android/fmradio/FmService;->mPowerStatus:I

    sget v1, Lcom/android/fmradio/FmService;->POWER_UP:I

    const/4 v2, -0x1

    if-eq v0, v1, :cond_0

    return v2

    .line 588
    :cond_0
    invoke-virtual {p0}, Lcom/android/fmradio/FmService;->isRdsSupported()Z

    move-result v0

    if-eqz v0, :cond_1

    .line 589
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mFmManager:Lcom/android/fmradio/FmManagerSelect;

    const/4 v1, 0x0

    invoke-virtual {v0, p1, v1}, Lcom/android/fmradio/FmManagerSelect;->setRdsMode(ZZ)I

    move-result v2

    :cond_1
    return v2
.end method

.method private declared-synchronized startRender()V
    .locals 3

    monitor-enter p0

    .line 235
    :try_start_0
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mFmManager:Lcom/android/fmradio/FmManagerSelect;

    sget-object v1, Lcom/android/fmradio/FmConstants$AudioPath;->FM_AUDIO_PATH_HEADSET:Lcom/android/fmradio/FmConstants$AudioPath;

    const/4 v2, 0x1

    invoke-virtual {v0, v1, v2}, Lcom/android/fmradio/FmManagerSelect;->setAudioPathEnable(Lcom/android/fmradio/FmConstants$AudioPath;Z)Z

    .line 237
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mRenderLock:Ljava/lang/Object;

    monitor-enter v0
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_1

    .line 238
    :try_start_1
    iget-object v1, p0, Lcom/android/fmradio/FmService;->mRenderLock:Ljava/lang/Object;

    invoke-virtual {v1}, Ljava/lang/Object;->notify()V

    .line 239
    monitor-exit v0
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    .line 240
    monitor-exit p0

    return-void

    :catchall_0
    move-exception v1

    .line 239
    :try_start_2
    monitor-exit v0
    :try_end_2
    .catchall {:try_start_2 .. :try_end_2} :catchall_0

    :try_start_3
    throw v1
    :try_end_3
    .catchall {:try_start_3 .. :try_end_3} :catchall_1

    :catchall_1
    move-exception v0

    monitor-exit p0

    throw v0
.end method

.method private startScan(I)[I
    .locals 4

    .line 505
    sget-object v0, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "---->>startScan()  start_freq: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v2, " mIsStopScanCalled: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-boolean v2, p0, Lcom/android/fmradio/FmService;->mIsStopScanCalled:Z

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    const/4 v0, 0x0

    .line 509
    invoke-direct {p0, v0}, Lcom/android/fmradio/FmService;->setRds(Z)I

    const/4 v1, 0x1

    .line 510
    invoke-virtual {p0, v1}, Lcom/android/fmradio/FmService;->setMute(Z)I

    .line 512
    iput-boolean v1, p0, Lcom/android/fmradio/FmService;->mIsNativeScanning:Z

    .line 514
    iget-object v2, p0, Lcom/android/fmradio/FmService;->mFmManager:Lcom/android/fmradio/FmManagerSelect;

    invoke-virtual {v2, p1}, Lcom/android/fmradio/FmManagerSelect;->autoScan(I)[I

    move-result-object p1

    .line 515
    sget-object v2, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    const-string v3, "startScan - autoScan finish"

    invoke-static {v2, v3}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 516
    iput-boolean v0, p0, Lcom/android/fmradio/FmService;->mIsNativeScanning:Z

    .line 518
    invoke-direct {p0, v1}, Lcom/android/fmradio/FmService;->setRds(Z)I

    .line 519
    iget-boolean v2, p0, Lcom/android/fmradio/FmService;->mIsStopScanCalled:Z

    if-eqz v2, :cond_0

    new-array p1, v1, [I

    const/16 v1, -0x64

    aput v1, p1, v0

    .line 525
    iput-boolean v0, p0, Lcom/android/fmradio/FmService;->mIsStopScanCalled:Z

    :cond_0
    return-object p1
.end method

.method private declared-synchronized stopRender()V
    .locals 3

    monitor-enter p0

    .line 243
    :try_start_0
    sget-object v0, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    const-string v1, "---->>stopRender()"

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 245
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mFmManager:Lcom/android/fmradio/FmManagerSelect;

    sget-object v1, Lcom/android/fmradio/FmConstants$AudioPath;->FM_AUDIO_PATH_NONE:Lcom/android/fmradio/FmConstants$AudioPath;

    const/4 v2, 0x0

    invoke-virtual {v0, v1, v2}, Lcom/android/fmradio/FmManagerSelect;->setAudioPathEnable(Lcom/android/fmradio/FmConstants$AudioPath;Z)Z
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 246
    monitor-exit p0

    return-void

    :catchall_0
    move-exception v0

    monitor-exit p0

    throw v0
.end method

.method private tuneStation(F)Z
    .locals 5

    .line 424
    sget-object v0, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "---->>tuneStation()  frequency: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(F)Ljava/lang/StringBuilder;

    const-string v2, " mPowerStatus: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget v2, p0, Lcom/android/fmradio/FmService;->mPowerStatus:I

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    const/4 v0, 0x0

    .line 425
    iput v0, p0, Lcom/android/fmradio/FmService;->startAudioTrackTimes:I

    .line 426
    iget-object v1, p0, Lcom/android/fmradio/FmService;->mFmServiceHandler:Lcom/android/fmradio/FmService$FmRadioServiceHandler;

    const/4 v2, -0x2

    invoke-virtual {v1, v2}, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->sendEmptyMessage(I)Z

    .line 427
    iget v1, p0, Lcom/android/fmradio/FmService;->mPowerStatus:I

    sget v2, Lcom/android/fmradio/FmService;->POWER_UP:I

    if-ne v1, v2, :cond_1

    .line 428
    invoke-direct {p0, v0}, Lcom/android/fmradio/FmService;->setRds(Z)I

    .line 430
    iget-object v1, p0, Lcom/android/fmradio/FmService;->mFmManager:Lcom/android/fmradio/FmManagerSelect;

    invoke-virtual {v1, p1}, Lcom/android/fmradio/FmManagerSelect;->tuneRadio(F)Z

    move-result v1

    .line 431
    sget-object v2, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    new-instance v3, Ljava/lang/StringBuilder;

    invoke-direct {v3}, Ljava/lang/StringBuilder;-><init>()V

    const-string v4, "---->>tuneStation()  bRet: "

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v3, v1}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    invoke-virtual {v3}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v3

    invoke-static {v2, v3}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    if-eqz v1, :cond_0

    const/4 v2, 0x1

    .line 434
    invoke-direct {p0, v2}, Lcom/android/fmradio/FmService;->setRds(Z)I

    .line 435
    iget-object v2, p0, Lcom/android/fmradio/FmService;->mContext:Landroid/content/Context;

    invoke-static {v2, p1}, Lcom/android/fmradio/utils/FmUtils;->computeStation(Landroid/content/Context;F)I

    move-result p1

    iput p1, p0, Lcom/android/fmradio/FmService;->mCurrentStation:I

    .line 436
    iget-object p1, p0, Lcom/android/fmradio/FmService;->mContext:Landroid/content/Context;

    iget v2, p0, Lcom/android/fmradio/FmService;->mCurrentStation:I

    invoke-static {p1, v2}, Lcom/android/fmradio/database/FmStation;->setCurrentStation(Landroid/content/Context;I)V

    .line 438
    :cond_0
    invoke-virtual {p0, v0}, Lcom/android/fmradio/FmService;->setMute(Z)I

    return v1

    .line 446
    :cond_1
    invoke-direct {p0, p1}, Lcom/android/fmradio/FmService;->powerUp(F)Z

    move-result v1

    if-eqz v1, :cond_2

    .line 447
    invoke-direct {p0, p1}, Lcom/android/fmradio/FmService;->playFrequency(F)Z

    move-result v0

    :cond_2
    return v0
.end method

.method private unregisterFmBroadcastReceiver()V
    .locals 1

    .line 768
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mBroadcastReceiver:Lcom/android/fmradio/FmService$FmServiceBroadcastReceiver;

    if-eqz v0, :cond_0

    .line 769
    invoke-virtual {p0, v0}, Lcom/android/fmradio/FmService;->unregisterReceiver(Landroid/content/BroadcastReceiver;)V

    const/4 v0, 0x0

    .line 770
    iput-object v0, p0, Lcom/android/fmradio/FmService;->mBroadcastReceiver:Lcom/android/fmradio/FmService$FmServiceBroadcastReceiver;

    :cond_0
    return-void
.end method

.method private updateAudioFocus(I)V
    .locals 3

    .line 1175
    sget-object v0, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "---->>updateAudioFocus()  focusState: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    const/4 v0, -0x3

    const/4 v1, 0x1

    if-eq p1, v0, :cond_6

    const/4 v0, -0x2

    if-eq p1, v0, :cond_4

    const/4 v0, -0x1

    const/4 v2, 0x0

    if-eq p1, v0, :cond_3

    if-eq p1, v1, :cond_0

    goto :goto_0

    .line 1194
    :cond_0
    iget-object p1, p0, Lcom/android/fmradio/FmService;->mContext:Landroid/content/Context;

    invoke-static {p1}, Lcom/android/fmradio/utils/FmUtils;->getIsSpeakerModeOnFocusLost(Landroid/content/Context;)Z

    move-result p1

    if-eqz p1, :cond_1

    .line 1195
    invoke-direct {p0, v1}, Lcom/android/fmradio/FmService;->setForceUse(Z)V

    .line 1196
    iget-object p1, p0, Lcom/android/fmradio/FmService;->mContext:Landroid/content/Context;

    invoke-static {p1, v2}, Lcom/android/fmradio/utils/FmUtils;->setIsSpeakerModeOnFocusLost(Landroid/content/Context;Z)V

    .line 1198
    :cond_1
    iget p1, p0, Lcom/android/fmradio/FmService;->mPowerStatus:I

    sget v0, Lcom/android/fmradio/FmService;->POWER_UP:I

    if-eq p1, v0, :cond_2

    iget-boolean p1, p0, Lcom/android/fmradio/FmService;->mPausedByTransientLossOfFocus:Z

    if-eqz p1, :cond_2

    .line 1200
    iget-object p1, p0, Lcom/android/fmradio/FmService;->mFmServiceHandler:Lcom/android/fmradio/FmService$FmRadioServiceHandler;

    const/16 v0, 0x9

    invoke-virtual {p1, v0}, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->removeMessages(I)V

    .line 1201
    iget-object p1, p0, Lcom/android/fmradio/FmService;->mFmServiceHandler:Lcom/android/fmradio/FmService$FmRadioServiceHandler;

    const/16 v0, 0xa

    invoke-virtual {p1, v0}, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->removeMessages(I)V

    .line 1202
    new-instance p1, Landroid/os/Bundle;

    invoke-direct {p1, v1}, Landroid/os/Bundle;-><init>(I)V

    .line 1203
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mContext:Landroid/content/Context;

    iget v1, p0, Lcom/android/fmradio/FmService;->mCurrentStation:I

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/FmUtils;->computeFrequency(Landroid/content/Context;I)F

    move-result v0

    const-string v1, "frequency"

    invoke-virtual {p1, v1, v0}, Landroid/os/Bundle;->putFloat(Ljava/lang/String;F)V

    .line 1204
    invoke-direct {p0, p1}, Lcom/android/fmradio/FmService;->handlePowerUp(Landroid/os/Bundle;)V

    .line 1206
    :cond_2
    invoke-virtual {p0, v2}, Lcom/android/fmradio/FmService;->setMute(Z)I

    goto :goto_0

    .line 1178
    :cond_3
    iput-boolean v2, p0, Lcom/android/fmradio/FmService;->mPausedByTransientLossOfFocus:Z

    .line 1180
    invoke-direct {p0}, Lcom/android/fmradio/FmService;->handlePowerDown()V

    .line 1181
    invoke-direct {p0}, Lcom/android/fmradio/FmService;->forceToHeadsetMode()V

    goto :goto_0

    .line 1185
    :cond_4
    iget p1, p0, Lcom/android/fmradio/FmService;->mPowerStatus:I

    sget v0, Lcom/android/fmradio/FmService;->POWER_UP:I

    if-ne p1, v0, :cond_5

    .line 1186
    iput-boolean v1, p0, Lcom/android/fmradio/FmService;->mPausedByTransientLossOfFocus:Z

    .line 1189
    :cond_5
    invoke-direct {p0}, Lcom/android/fmradio/FmService;->handlePowerDown()V

    .line 1190
    invoke-direct {p0}, Lcom/android/fmradio/FmService;->forceToHeadsetMode()V

    goto :goto_0

    .line 1210
    :cond_6
    invoke-virtual {p0, v1}, Lcom/android/fmradio/FmService;->setMute(Z)I

    :goto_0
    return-void
.end method

.method private declared-synchronized updateAudioFocusAync(I)V
    .locals 2

    monitor-enter p0

    .line 1162
    :try_start_0
    new-instance v0, Landroid/os/Bundle;

    const/4 v1, 0x1

    invoke-direct {v0, v1}, Landroid/os/Bundle;-><init>(I)V

    const-string v1, "key_audiofocus_changed"

    .line 1163
    invoke-virtual {v0, v1, p1}, Landroid/os/Bundle;->putInt(Ljava/lang/String;I)V

    .line 1164
    iget-object p1, p0, Lcom/android/fmradio/FmService;->mFmServiceHandler:Lcom/android/fmradio/FmService$FmRadioServiceHandler;

    const/16 v1, 0x1e

    invoke-virtual {p1, v1}, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->obtainMessage(I)Landroid/os/Message;

    move-result-object p1

    .line 1165
    invoke-virtual {p1, v0}, Landroid/os/Message;->setData(Landroid/os/Bundle;)V

    .line 1166
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mFmServiceHandler:Lcom/android/fmradio/FmService$FmRadioServiceHandler;

    invoke-virtual {v0, p1}, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->sendMessage(Landroid/os/Message;)Z
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 1167
    monitor-exit p0

    return-void

    :catchall_0
    move-exception p1

    monitor-exit p0

    throw p1
.end method

.method private updateDBInLocation([I)I
    .locals 11

    const-string v0, "frequency"

    .line 1414
    array-length v1, p1

    .line 1415
    new-instance v2, Ljava/util/ArrayList;

    invoke-direct {v2}, Ljava/util/ArrayList;-><init>()V

    const/4 v3, 0x0

    .line 1419
    :try_start_0
    iget-object v4, p0, Lcom/android/fmradio/FmService;->mContext:Landroid/content/Context;

    invoke-virtual {v4}, Landroid/content/Context;->getContentResolver()Landroid/content/ContentResolver;

    move-result-object v5

    sget-object v6, Lcom/android/fmradio/database/FmStation$Station;->CONTENT_URI:Landroid/net/Uri;

    filled-new-array {v0}, [Ljava/lang/String;

    move-result-object v7

    const-string v8, "is_favorite=0"

    const/4 v9, 0x0

    const-string v10, "frequency"

    invoke-virtual/range {v5 .. v10}, Landroid/content/ContentResolver;->query(Landroid/net/Uri;[Ljava/lang/String;Ljava/lang/String;[Ljava/lang/String;Ljava/lang/String;)Landroid/database/Cursor;

    move-result-object v3

    if-eqz v3, :cond_1

    .line 1422
    invoke-interface {v3}, Landroid/database/Cursor;->moveToFirst()Z

    move-result v4

    if-eqz v4, :cond_1

    .line 1425
    :cond_0
    invoke-interface {v3, v0}, Landroid/database/Cursor;->getColumnIndex(Ljava/lang/String;)I

    move-result v4

    invoke-interface {v3, v4}, Landroid/database/Cursor;->getInt(I)I

    move-result v4

    .line 1426
    invoke-static {v4}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v4

    invoke-virtual {v2, v4}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 1427
    invoke-interface {v3}, Landroid/database/Cursor;->moveToNext()Z

    move-result v4

    if-nez v4, :cond_0

    goto :goto_0

    .line 1430
    :cond_1
    sget-object v0, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    const-string v4, "updateDBInLocation, insertSearchedStation cursor is null"

    invoke-static {v0, v4}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    :goto_0
    if-eqz v3, :cond_2

    .line 1434
    invoke-interface {v3}, Landroid/database/Cursor;->close()V

    .line 1438
    :cond_2
    invoke-virtual {v2}, Ljava/util/ArrayList;->size()I

    move-result v0

    const/4 v3, 0x0

    move v4, v3

    :goto_1
    if-ge v4, v0, :cond_6

    .line 1441
    invoke-virtual {v2, v4}, Ljava/util/ArrayList;->get(I)Ljava/lang/Object;

    move-result-object v5

    check-cast v5, Ljava/lang/Integer;

    invoke-virtual {v5}, Ljava/lang/Integer;->intValue()I

    move-result v5

    move v6, v3

    :goto_2
    if-ge v6, v1, :cond_5

    .line 1443
    aget v7, p1, v6

    if-ne v5, v7, :cond_3

    goto :goto_3

    :cond_3
    add-int/lit8 v8, v1, -0x1

    if-ne v6, v8, :cond_4

    if-eq v5, v7, :cond_4

    .line 1449
    iget-object v7, p0, Lcom/android/fmradio/FmService;->mContext:Landroid/content/Context;

    invoke-static {v7, v5}, Lcom/android/fmradio/database/FmStation;->deleteStationInDb(Landroid/content/Context;I)V

    :cond_4
    add-int/lit8 v6, v6, 0x1

    goto :goto_2

    :cond_5
    :goto_3
    add-int/lit8 v4, v4, 0x1

    goto :goto_1

    :cond_6
    move v0, v3

    move v4, v0

    :goto_4
    if-ge v0, v1, :cond_8

    .line 1456
    aget v5, p1, v0

    .line 1457
    iget-object v6, p0, Lcom/android/fmradio/FmService;->mContext:Landroid/content/Context;

    invoke-static {v6, v5}, Lcom/android/fmradio/utils/FmUtils;->isValidStation(Landroid/content/Context;I)Z

    move-result v6

    if-eqz v6, :cond_7

    add-int/lit8 v4, v4, 0x1

    .line 1459
    invoke-static {v5}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v6

    invoke-virtual {v2, v6}, Ljava/util/ArrayList;->contains(Ljava/lang/Object;)Z

    move-result v6

    if-nez v6, :cond_7

    iget-object v6, p0, Lcom/android/fmradio/FmService;->mContext:Landroid/content/Context;

    invoke-static {v6, v5}, Lcom/android/fmradio/database/FmStation;->isFavoriteStation(Landroid/content/Context;I)Z

    move-result v6

    if-nez v6, :cond_7

    .line 1461
    iget-object v6, p0, Lcom/android/fmradio/FmService;->mContext:Landroid/content/Context;

    const-string v7, ""

    invoke-static {v6, v3, v5, v7}, Lcom/android/fmradio/database/FmStation;->insertStationToDb(Landroid/content/Context;IILjava/lang/String;)V

    :cond_7
    add-int/lit8 v0, v0, 0x1

    goto :goto_4

    :cond_8
    return v4

    :catchall_0
    move-exception p1

    if-eqz v3, :cond_9

    .line 1434
    invoke-interface {v3}, Landroid/database/Cursor;->close()V

    .line 1436
    :cond_9
    throw p1
.end method

.method private updateStations([I)[I
    .locals 14

    .line 1338
    sget-object v0, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "updateStations.firstValidstation:"

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-static {p1}, Ljava/util/Arrays;->toString([I)Ljava/lang/String;

    move-result-object v3

    invoke-virtual {v1, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 1339
    iget v0, p0, Lcom/android/fmradio/FmService;->mCurrentStation:I

    .line 1343
    invoke-virtual {p0}, Lcom/android/fmradio/FmService;->getResources()Landroid/content/res/Resources;

    move-result-object v1

    const v3, 0x7f020002

    .line 1344
    invoke-virtual {v1, v3}, Landroid/content/res/Resources;->getStringArray(I)[Ljava/lang/String;

    move-result-object v3

    const v4, 0x7f020001

    .line 1345
    invoke-virtual {v1, v4}, Landroid/content/res/Resources;->getStringArray(I)[Ljava/lang/String;

    move-result-object v1

    .line 1346
    new-instance v4, Landroid/content/ContentValues;

    const/4 v5, 0x2

    invoke-direct {v4, v5}, Landroid/content/ContentValues;-><init>(I)V

    const/4 v6, 0x0

    .line 1347
    invoke-static {v6}, Ljava/lang/Float;->valueOf(F)Ljava/lang/Float;

    const/4 v6, 0x1

    const/4 v7, 0x0

    if-eqz p1, :cond_5

    .line 1355
    array-length v8, p1

    .line 1356
    iget-boolean v9, p0, Lcom/android/fmradio/FmService;->mIsDistanceExceed:Z

    if-eqz v9, :cond_4

    move v9, v7

    :goto_0
    if-ge v9, v8, :cond_5

    .line 1359
    aget v10, p1, v9

    .line 1369
    iget-object v11, p0, Lcom/android/fmradio/FmService;->mContext:Landroid/content/Context;

    invoke-static {v11, v10}, Lcom/android/fmradio/utils/FmUtils;->isValidStation(Landroid/content/Context;I)Z

    move-result v11

    if-eqz v11, :cond_3

    iget-object v11, p0, Lcom/android/fmradio/FmService;->mContext:Landroid/content/Context;

    invoke-static {v11, v10}, Lcom/android/fmradio/database/FmStation;->isFavoriteStation(Landroid/content/Context;I)Z

    move-result v11

    if-nez v11, :cond_3

    move v11, v7

    .line 1370
    :goto_1
    array-length v12, v1

    if-ge v11, v12, :cond_1

    .line 1371
    aget-object v12, v1, v11

    invoke-static {v12}, Ljava/lang/Float;->parseFloat(Ljava/lang/String;)F

    move-result v12

    invoke-static {v12}, Ljava/lang/Float;->valueOf(F)Ljava/lang/Float;

    move-result-object v12

    .line 1372
    invoke-virtual {v12}, Ljava/lang/Float;->floatValue()F

    move-result v12

    const/high16 v13, 0x42c80000    # 100.0f

    mul-float/2addr v12, v13

    float-to-int v12, v12

    if-ne v10, v12, :cond_0

    .line 1374
    invoke-static {v12}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v12

    const-string v13, "frequency"

    invoke-virtual {v4, v13, v12}, Landroid/content/ContentValues;->put(Ljava/lang/String;Ljava/lang/Integer;)V

    .line 1375
    aget-object v11, v3, v11

    const-string v12, "station_name"

    invoke-virtual {v4, v12, v11}, Landroid/content/ContentValues;->put(Ljava/lang/String;Ljava/lang/String;)V

    .line 1376
    iget-object v11, p0, Lcom/android/fmradio/FmService;->mContext:Landroid/content/Context;

    invoke-static {v11, v4}, Lcom/android/fmradio/database/FmStation;->insertStationToDb(Landroid/content/Context;Landroid/content/ContentValues;)V

    .line 1377
    invoke-virtual {v4}, Landroid/content/ContentValues;->clear()V

    move v11, v6

    goto :goto_2

    :cond_0
    add-int/lit8 v11, v11, 0x1

    goto :goto_1

    :cond_1
    move v11, v7

    :goto_2
    if-nez v11, :cond_3

    .line 1383
    iget-object v11, p0, Lcom/android/fmradio/FmService;->mContext:Landroid/content/Context;

    invoke-static {v11}, Lcom/android/fmradio/database/FmStation;->getCurrentBand(Landroid/content/Context;)I

    move-result v11

    invoke-static {v11}, Lcom/android/fmradio/utils/FmUtils;->isFMBand(I)Z

    move-result v11

    const/4 v12, 0x0

    if-eqz v11, :cond_2

    .line 1384
    iget-object v11, p0, Lcom/android/fmradio/FmService;->mContext:Landroid/content/Context;

    invoke-static {v11, v7, v10, v12}, Lcom/android/fmradio/database/FmStation;->insertStationToDb(Landroid/content/Context;IILjava/lang/String;)V

    goto :goto_3

    .line 1386
    :cond_2
    iget-object v11, p0, Lcom/android/fmradio/FmService;->mContext:Landroid/content/Context;

    invoke-static {v11, v6, v10, v12}, Lcom/android/fmradio/database/FmStation;->insertStationToDb(Landroid/content/Context;IILjava/lang/String;)V

    :cond_3
    :goto_3
    add-int/lit8 v9, v9, 0x1

    goto :goto_0

    .line 1397
    :cond_4
    invoke-direct {p0, p1}, Lcom/android/fmradio/FmService;->updateDBInLocation([I)I

    move-result p1

    goto :goto_4

    :cond_5
    move p1, v7

    .line 1401
    :goto_4
    sget-object v1, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    new-instance v3, Ljava/lang/StringBuilder;

    invoke-direct {v3}, Ljava/lang/StringBuilder;-><init>()V

    invoke-virtual {v3, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v3, v0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v2, ",stationNum:"

    invoke-virtual {v3, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v3, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v3}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v2

    invoke-static {v1, v2}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    new-array v1, v5, [I

    aput v0, v1, v7

    aput p1, v1, v6

    return-object v1
.end method


# virtual methods
.method public abandonAudioFocus()V
    .locals 2

    .line 1082
    sget-object v0, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    const-string v1, "---->>abandonAudioFocus() "

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 1083
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mAudioManager:Landroid/media/AudioManager;

    iget-object v1, p0, Lcom/android/fmradio/FmService;->mAudioFocusChangeListener:Landroid/media/AudioManager$OnAudioFocusChangeListener;

    invoke-virtual {v0, v1}, Landroid/media/AudioManager;->abandonAudioFocus(Landroid/media/AudioManager$OnAudioFocusChangeListener;)I

    const/4 v0, 0x0

    .line 1084
    iput-boolean v0, p0, Lcom/android/fmradio/FmService;->mIsAudioFocusHeld:Z

    return-void
.end method

.method public getFrequency()I
    .locals 1

    .line 672
    iget v0, p0, Lcom/android/fmradio/FmService;->mCurrentStation:I

    return v0
.end method

.method public getPowerStatus()I
    .locals 1

    .line 401
    iget v0, p0, Lcom/android/fmradio/FmService;->mPowerStatus:I

    return v0
.end method

.method public initService(I)V
    .locals 1

    const/4 v0, 0x1

    .line 653
    iput-boolean v0, p0, Lcom/android/fmradio/FmService;->mIsServiceInited:Z

    .line 654
    iput p1, p0, Lcom/android/fmradio/FmService;->mCurrentStation:I

    return-void
.end method

.method public isDeviceOpen()Z
    .locals 1

    .line 294
    iget-boolean v0, p0, Lcom/android/fmradio/FmService;->mIsDeviceOpen:Z

    return v0
.end method

.method public isMuted()Z
    .locals 1

    .line 633
    iget-boolean v0, p0, Lcom/android/fmradio/FmService;->mIsMuted:Z

    return v0
.end method

.method public isRdsSupported()Z
    .locals 2

    .line 642
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mFmManager:Lcom/android/fmradio/FmManagerSelect;

    invoke-virtual {v0}, Lcom/android/fmradio/FmManagerSelect;->isRdsSupported()I

    move-result v0

    const/4 v1, 0x1

    if-ne v0, v1, :cond_0

    goto :goto_0

    :cond_0
    const/4 v1, 0x0

    :goto_0
    return v1
.end method

.method public isScanning()Z
    .locals 1

    .line 547
    iget-boolean v0, p0, Lcom/android/fmradio/FmService;->mIsScanning:Z

    return v0
.end method

.method public isServiceInited()Z
    .locals 1

    .line 663
    iget-boolean v0, p0, Lcom/android/fmradio/FmService;->mIsServiceInited:Z

    return v0
.end method

.method public onBind(Landroid/content/Intent;)Landroid/os/IBinder;
    .locals 0

    .line 149
    iget-object p1, p0, Lcom/android/fmradio/FmService;->mBinder:Landroid/os/IBinder;

    return-object p1
.end method

.method public onConfigurationChanged(Landroid/content/res/Configuration;)V
    .locals 1

    .line 829
    invoke-super {p0, p1}, Landroid/app/Service;->onConfigurationChanged(Landroid/content/res/Configuration;)V

    .line 831
    iget p1, p0, Lcom/android/fmradio/FmService;->mPowerStatus:I

    sget v0, Lcom/android/fmradio/FmService;->POWER_UP:I

    if-ne p1, v0, :cond_0

    .line 832
    sget-object p1, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    const-string v0, "----->>onConfigurationChanged()"

    invoke-static {p1, v0}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    :cond_0
    return-void
.end method

.method public onCreate()V
    .locals 2

    .line 776
    invoke-super {p0}, Landroid/app/Service;->onCreate()V

    .line 777
    sget-object v0, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    const-string v1, "---->>onCreate() "

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 778
    invoke-virtual {p0}, Lcom/android/fmradio/FmService;->getApplicationContext()Landroid/content/Context;

    move-result-object v0

    iput-object v0, p0, Lcom/android/fmradio/FmService;->mContext:Landroid/content/Context;

    .line 780
    sget v0, Lcom/android/fmradio/FmService;->POWER_DOWN:I

    iput v0, p0, Lcom/android/fmradio/FmService;->mPowerStatus:I

    const-string v0, "audio"

    .line 782
    invoke-virtual {p0, v0}, Lcom/android/fmradio/FmService;->getSystemService(Ljava/lang/String;)Ljava/lang/Object;

    move-result-object v0

    check-cast v0, Landroid/media/AudioManager;

    iput-object v0, p0, Lcom/android/fmradio/FmService;->mAudioManager:Landroid/media/AudioManager;

    .line 784
    new-instance v0, Lcom/android/fmradio/FmManagerSelect;

    invoke-direct {v0, p0}, Lcom/android/fmradio/FmManagerSelect;-><init>(Landroid/content/Context;)V

    iput-object v0, p0, Lcom/android/fmradio/FmService;->mFmManager:Lcom/android/fmradio/FmManagerSelect;

    .line 786
    new-instance v0, Landroid/os/HandlerThread;

    const-string v1, "FmRadioServiceThread"

    invoke-direct {v0, v1}, Landroid/os/HandlerThread;-><init>(Ljava/lang/String;)V

    .line 787
    invoke-virtual {v0}, Landroid/os/HandlerThread;->start()V

    .line 788
    new-instance v1, Lcom/android/fmradio/FmService$FmRadioServiceHandler;

    invoke-virtual {v0}, Landroid/os/HandlerThread;->getLooper()Landroid/os/Looper;

    move-result-object v0

    invoke-direct {v1, p0, v0}, Lcom/android/fmradio/FmService$FmRadioServiceHandler;-><init>(Lcom/android/fmradio/FmService;Landroid/os/Looper;)V

    iput-object v1, p0, Lcom/android/fmradio/FmService;->mFmServiceHandler:Lcom/android/fmradio/FmService$FmRadioServiceHandler;

    return-void
.end method

.method public onDestroy()V
    .locals 2

    .line 793
    sget-object v0, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    const-string v1, "---->>onDestroy()"

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    const/4 v0, 0x1

    .line 795
    invoke-virtual {p0, v0}, Lcom/android/fmradio/FmService;->setMute(Z)I

    .line 799
    invoke-direct {p0}, Lcom/android/fmradio/FmService;->headSetNotActive()V

    .line 800
    invoke-virtual {p0}, Lcom/android/fmradio/FmService;->abandonAudioFocus()V

    .line 801
    invoke-direct {p0}, Lcom/android/fmradio/FmService;->exitFm()V

    .line 803
    invoke-direct {p0}, Lcom/android/fmradio/FmService;->releaseAudioPatch()V

    .line 805
    invoke-super {p0}, Landroid/app/Service;->onDestroy()V

    return-void
.end method

.method public onStartCommand(Landroid/content/Intent;II)I
    .locals 1

    .line 838
    invoke-super {p0, p1, p2, p3}, Landroid/app/Service;->onStartCommand(Landroid/content/Intent;II)I

    if-eqz p1, :cond_8

    .line 841
    invoke-virtual {p1}, Landroid/content/Intent;->getAction()Ljava/lang/String;

    move-result-object p1

    .line 842
    sget-object p2, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    new-instance p3, Ljava/lang/StringBuilder;

    invoke-direct {p3}, Ljava/lang/StringBuilder;-><init>()V

    const-string v0, "---->>onStartCommand() action: "

    invoke-virtual {p3, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {p3, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {p3}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p3

    invoke-static {p2, p3}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    const-string p2, "fmradio.seek.previous"

    .line 843
    invoke-virtual {p2, p1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result p2

    if-eqz p2, :cond_0

    .line 844
    iget-object p1, p0, Lcom/android/fmradio/FmService;->mContext:Landroid/content/Context;

    iget p2, p0, Lcom/android/fmradio/FmService;->mCurrentStation:I

    invoke-static {p1, p2}, Lcom/android/fmradio/utils/FmUtils;->computeFrequency(Landroid/content/Context;I)F

    move-result p1

    const/4 p2, 0x0

    invoke-virtual {p0, p1, p2}, Lcom/android/fmradio/FmService;->seekStationAsync(FZ)V

    goto/16 :goto_0

    :cond_0
    const-string p2, "fmradio.seek.next"

    .line 845
    invoke-virtual {p2, p1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result p2

    const/4 p3, 0x1

    if-eqz p2, :cond_1

    .line 846
    iget-object p1, p0, Lcom/android/fmradio/FmService;->mContext:Landroid/content/Context;

    iget p2, p0, Lcom/android/fmradio/FmService;->mCurrentStation:I

    invoke-static {p1, p2}, Lcom/android/fmradio/utils/FmUtils;->computeFrequency(Landroid/content/Context;I)F

    move-result p1

    invoke-virtual {p0, p1, p3}, Lcom/android/fmradio/FmService;->seekStationAsync(FZ)V

    goto :goto_0

    :cond_1
    const-string p2, "fmradio.turnoff"

    .line 847
    invoke-virtual {p2, p1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result p2

    if-eqz p2, :cond_2

    .line 848
    invoke-virtual {p0}, Lcom/android/fmradio/FmService;->powerDownAsync()V

    goto :goto_0

    :cond_2
    const-string p2, "fmradio.enter"

    .line 849
    invoke-virtual {p2, p1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result p2

    if-eqz p2, :cond_3

    .line 850
    invoke-direct {p0}, Lcom/android/fmradio/FmService;->fm_in()V

    goto :goto_0

    :cond_3
    const-string p2, "fmradio.exit"

    .line 851
    invoke-virtual {p2, p1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result p2

    if-eqz p2, :cond_4

    .line 852
    invoke-direct {p0}, Lcom/android/fmradio/FmService;->fm_out()V

    goto :goto_0

    :cond_4
    const-string p2, "fmradio.decrease"

    .line 853
    invoke-virtual {p2, p1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result p2

    if-eqz p2, :cond_6

    .line 854
    iget-object p1, p0, Lcom/android/fmradio/FmService;->mContext:Landroid/content/Context;

    iget p2, p0, Lcom/android/fmradio/FmService;->mCurrentStation:I

    invoke-static {p1, p2}, Lcom/android/fmradio/utils/FmUtils;->computeDecreaseStation(Landroid/content/Context;I)I

    move-result p2

    invoke-static {p1, p2}, Lcom/android/fmradio/utils/FmUtils;->computeFrequency(Landroid/content/Context;I)F

    move-result p1

    .line 855
    iget-boolean p2, p0, Lcom/android/fmradio/FmService;->mIsServiceInited:Z

    if-nez p2, :cond_5

    .line 856
    invoke-virtual {p0, p1}, Lcom/android/fmradio/FmService;->powerUpAsync(F)V

    .line 858
    :cond_5
    invoke-virtual {p0, p1, p3}, Lcom/android/fmradio/FmService;->tuneStationAsync(FZ)V

    goto :goto_0

    :cond_6
    const-string p2, "fmradio.increase"

    .line 859
    invoke-virtual {p2, p1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result p1

    if-eqz p1, :cond_8

    .line 860
    iget-object p1, p0, Lcom/android/fmradio/FmService;->mContext:Landroid/content/Context;

    iget p2, p0, Lcom/android/fmradio/FmService;->mCurrentStation:I

    invoke-static {p1, p2}, Lcom/android/fmradio/utils/FmUtils;->computeIncreaseStation(Landroid/content/Context;I)I

    move-result p2

    invoke-static {p1, p2}, Lcom/android/fmradio/utils/FmUtils;->computeFrequency(Landroid/content/Context;I)F

    move-result p1

    .line 861
    iget-boolean p2, p0, Lcom/android/fmradio/FmService;->mIsServiceInited:Z

    if-nez p2, :cond_7

    .line 862
    invoke-virtual {p0, p1}, Lcom/android/fmradio/FmService;->powerUpAsync(F)V

    .line 864
    :cond_7
    invoke-virtual {p0, p1, p3}, Lcom/android/fmradio/FmService;->tuneStationAsync(FZ)V

    :cond_8
    :goto_0
    const/4 p1, 0x2

    return p1
.end method

.method public onTaskRemoved(Landroid/content/Intent;)V
    .locals 0

    .line 1751
    invoke-super {p0, p1}, Landroid/app/Service;->onTaskRemoved(Landroid/content/Intent;)V

    .line 1752
    invoke-virtual {p0}, Lcom/android/fmradio/FmService;->stopSelf()V

    return-void
.end method

.method public powerDownAsync()V
    .locals 3

    .line 364
    sget-object v0, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    const-string v1, "---->>powerDownAsync()"

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 368
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mFmServiceHandler:Lcom/android/fmradio/FmService$FmRadioServiceHandler;

    const/16 v1, 0xd

    invoke-virtual {v0, v1}, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->removeMessages(I)V

    .line 369
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mFmServiceHandler:Lcom/android/fmradio/FmService$FmRadioServiceHandler;

    const/16 v1, 0x10

    invoke-virtual {v0, v1}, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->removeMessages(I)V

    .line 370
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mFmServiceHandler:Lcom/android/fmradio/FmService$FmRadioServiceHandler;

    const/16 v1, 0xf

    invoke-virtual {v0, v1}, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->removeMessages(I)V

    .line 371
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mFmServiceHandler:Lcom/android/fmradio/FmService$FmRadioServiceHandler;

    const/16 v1, 0xa

    invoke-virtual {v0, v1}, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->removeMessages(I)V

    .line 372
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mFmServiceHandler:Lcom/android/fmradio/FmService$FmRadioServiceHandler;

    const/16 v2, 0x9

    invoke-virtual {v0, v2}, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->removeMessages(I)V

    .line 373
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mFmServiceHandler:Lcom/android/fmradio/FmService$FmRadioServiceHandler;

    invoke-virtual {v0, v1}, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->sendEmptyMessage(I)Z

    return-void
.end method

.method public powerUpAsync(F)V
    .locals 3

    .line 303
    sget-object v0, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "---->>powerUpAsync() frequency: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(F)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 305
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mFmServiceHandler:Lcom/android/fmradio/FmService$FmRadioServiceHandler;

    const/16 v1, 0xb

    invoke-virtual {v0, v1}, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->removeMessages(I)V

    .line 306
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mFmServiceHandler:Lcom/android/fmradio/FmService$FmRadioServiceHandler;

    const/16 v1, 0x9

    invoke-virtual {v0, v1}, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->removeMessages(I)V

    .line 307
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mFmServiceHandler:Lcom/android/fmradio/FmService$FmRadioServiceHandler;

    const/16 v2, 0xa

    invoke-virtual {v0, v2}, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->removeMessages(I)V

    .line 308
    new-instance v0, Landroid/os/Bundle;

    const/4 v2, 0x1

    invoke-direct {v0, v2}, Landroid/os/Bundle;-><init>(I)V

    const-string v2, "frequency"

    .line 309
    invoke-virtual {v0, v2, p1}, Landroid/os/Bundle;->putFloat(Ljava/lang/String;F)V

    .line 310
    iget-object p1, p0, Lcom/android/fmradio/FmService;->mFmServiceHandler:Lcom/android/fmradio/FmService$FmRadioServiceHandler;

    invoke-virtual {p1, v1}, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->obtainMessage(I)Landroid/os/Message;

    move-result-object p1

    .line 311
    invoke-virtual {p1, v0}, Landroid/os/Message;->setData(Landroid/os/Bundle;)V

    .line 312
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mFmServiceHandler:Lcom/android/fmradio/FmService$FmRadioServiceHandler;

    invoke-virtual {v0, p1}, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->sendMessage(Landroid/os/Message;)Z

    .line 313
    invoke-virtual {p0}, Lcom/android/fmradio/FmService;->requestAudioFocus()Z

    return-void
.end method

.method public registerFmRadioListener(Lcom/android/fmradio/iface/FmListener;)V
    .locals 5

    .line 1243
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mRecords:Ljava/util/ArrayList;

    monitor-enter v0

    .line 1247
    :try_start_0
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I

    move-result v1

    .line 1248
    iget-object v2, p0, Lcom/android/fmradio/FmService;->mRecords:Ljava/util/ArrayList;

    invoke-virtual {v2}, Ljava/util/ArrayList;->size()I

    move-result v2

    const/4 v3, 0x0

    :goto_0
    if-ge v3, v2, :cond_1

    .line 1250
    iget-object v4, p0, Lcom/android/fmradio/FmService;->mRecords:Ljava/util/ArrayList;

    invoke-virtual {v4, v3}, Ljava/util/ArrayList;->get(I)Ljava/lang/Object;

    move-result-object v4

    check-cast v4, Lcom/android/fmradio/FmService$Record;

    .line 1251
    iget v4, v4, Lcom/android/fmradio/FmService$Record;->mHashCode:I

    if-ne v1, v4, :cond_0

    .line 1252
    monitor-exit v0

    return-void

    :cond_0
    add-int/lit8 v3, v3, 0x1

    goto :goto_0

    .line 1255
    :cond_1
    new-instance v2, Lcom/android/fmradio/FmService$Record;

    const/4 v3, 0x0

    invoke-direct {v2, v3}, Lcom/android/fmradio/FmService$Record;-><init>(Lcom/android/fmradio/FmService$1;)V

    .line 1256
    iput v1, v2, Lcom/android/fmradio/FmService$Record;->mHashCode:I

    .line 1257
    iput-object p1, v2, Lcom/android/fmradio/FmService$Record;->mCallback:Lcom/android/fmradio/iface/FmListener;

    .line 1258
    iget-object p1, p0, Lcom/android/fmradio/FmService;->mRecords:Ljava/util/ArrayList;

    invoke-virtual {p1, v2}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 1259
    monitor-exit v0

    return-void

    :catchall_0
    move-exception p1

    monitor-exit v0
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    throw p1
.end method

.method public requestAudioFocus()Z
    .locals 6

    .line 1049
    sget-object v0, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "---->>requestAudioFocus() mIsAudioFocusHeld: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-boolean v2, p0, Lcom/android/fmradio/FmService;->mIsAudioFocusHeld:Z

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 1050
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mContext:Landroid/content/Context;

    invoke-static {v0}, Lcom/android/fmradio/utils/FmUtils;->getIsSpeakerModeOnFocusLost(Landroid/content/Context;)Z

    move-result v0

    const/4 v1, 0x0

    const/4 v2, 0x1

    if-eqz v0, :cond_0

    .line 1051
    invoke-direct {p0, v2}, Lcom/android/fmradio/FmService;->setForceUse(Z)V

    .line 1052
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mContext:Landroid/content/Context;

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/FmUtils;->setIsSpeakerModeOnFocusLost(Landroid/content/Context;Z)V

    .line 1054
    :cond_0
    iget-boolean v0, p0, Lcom/android/fmradio/FmService;->mIsAudioFocusHeld:Z

    if-eqz v0, :cond_1

    return v2

    .line 1063
    :cond_1
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mAudioManager:Landroid/media/AudioManager;

    iget-object v3, p0, Lcom/android/fmradio/FmService;->mAudioFocusChangeListener:Landroid/media/AudioManager$OnAudioFocusChangeListener;

    const/4 v4, 0x3

    invoke-virtual {v0, v3, v4, v2}, Landroid/media/AudioManager;->requestAudioFocus(Landroid/media/AudioManager$OnAudioFocusChangeListener;II)I

    move-result v0

    .line 1067
    sget-object v3, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    new-instance v4, Ljava/lang/StringBuilder;

    invoke-direct {v4}, Ljava/lang/StringBuilder;-><init>()V

    const-string v5, "---->>requestAudioFocus() audioFocus: "

    invoke-virtual {v4, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v4, v0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v4}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v4

    invoke-static {v3, v4}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    if-ne v2, v0, :cond_2

    move v1, v2

    .line 1071
    :cond_2
    iput-boolean v1, p0, Lcom/android/fmradio/FmService;->mIsAudioFocusHeld:Z

    .line 1072
    iget-boolean v0, p0, Lcom/android/fmradio/FmService;->mIsAudioFocusHeld:Z

    if-eqz v0, :cond_3

    .line 1073
    invoke-direct {p0}, Lcom/android/fmradio/FmService;->headSetActive()V

    .line 1075
    :cond_3
    iget-boolean v0, p0, Lcom/android/fmradio/FmService;->mIsAudioFocusHeld:Z

    return v0
.end method

.method public seekStationAsync(FZ)V
    .locals 3

    .line 461
    sget-object v0, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "---->>seekStationAsync()  frequency: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(F)Ljava/lang/StringBuilder;

    const-string v2, " isUp: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p2}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    const-string v2, " mIsScanning: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-boolean v2, p0, Lcom/android/fmradio/FmService;->mIsScanning:Z

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 463
    iget-boolean v0, p0, Lcom/android/fmradio/FmService;->mIsScanning:Z

    if-eqz v0, :cond_0

    return-void

    .line 465
    :cond_0
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mFmServiceHandler:Lcom/android/fmradio/FmService$FmRadioServiceHandler;

    const/16 v1, 0x10

    invoke-virtual {v0, v1}, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->removeMessages(I)V

    .line 467
    new-instance v0, Landroid/os/Bundle;

    const/4 v2, 0x2

    invoke-direct {v0, v2}, Landroid/os/Bundle;-><init>(I)V

    const-string v2, "frequency"

    .line 468
    invoke-virtual {v0, v2, p1}, Landroid/os/Bundle;->putFloat(Ljava/lang/String;F)V

    const-string p1, "option"

    .line 469
    invoke-virtual {v0, p1, p2}, Landroid/os/Bundle;->putBoolean(Ljava/lang/String;Z)V

    .line 470
    iget-object p1, p0, Lcom/android/fmradio/FmService;->mFmServiceHandler:Lcom/android/fmradio/FmService$FmRadioServiceHandler;

    invoke-virtual {p1, v1}, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->obtainMessage(I)Landroid/os/Message;

    move-result-object p1

    .line 471
    invoke-virtual {p1, v0}, Landroid/os/Message;->setData(Landroid/os/Bundle;)V

    .line 472
    iget-object p2, p0, Lcom/android/fmradio/FmService;->mFmServiceHandler:Lcom/android/fmradio/FmService$FmRadioServiceHandler;

    invoke-virtual {p2, p1}, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->sendMessage(Landroid/os/Message;)Z

    return-void
.end method

.method public setFmMainActivityForeground(Z)V
    .locals 0

    .line 1732
    iput-boolean p1, p0, Lcom/android/fmradio/FmService;->mIsFmMainForeground:Z

    return-void
.end method

.method public setFrequency(I)V
    .locals 0

    .line 681
    iput p1, p0, Lcom/android/fmradio/FmService;->mCurrentStation:I

    return-void
.end method

.method protected setMediaButtonListener(Lcom/android/fmradio/iface/IMediaButtonListener;)V
    .locals 3

    .line 971
    sget-object v0, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "headsetListen: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 972
    iput-object p1, p0, Lcom/android/fmradio/FmService;->mIMediaButtonListener:Lcom/android/fmradio/iface/IMediaButtonListener;

    return-void
.end method

.method public setMute(Z)I
    .locals 5

    .line 601
    iget v0, p0, Lcom/android/fmradio/FmService;->mPowerStatus:I

    sget v1, Lcom/android/fmradio/FmService;->POWER_UP:I

    if-eq v0, v1, :cond_0

    .line 602
    sget-object p1, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    const-string v0, "setMute, FM is not powered up"

    invoke-static {p1, v0}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    const/4 p1, -0x1

    return p1

    .line 607
    :cond_0
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mFmManager:Lcom/android/fmradio/FmManagerSelect;

    invoke-virtual {v0, p1}, Lcom/android/fmradio/FmManagerSelect;->setMute(Z)I

    move-result v0

    .line 612
    sget-object v1, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2}, Ljava/lang/StringBuilder;-><init>()V

    const-string v3, "--->>setMute() mute: "

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2, p1}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v2

    invoke-static {v1, v2}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    if-eqz p1, :cond_1

    const/4 v1, 0x0

    .line 614
    invoke-virtual {p0, v1}, Lcom/android/fmradio/FmService;->setVolume(I)Z

    goto :goto_0

    .line 616
    :cond_1
    iget-object v1, p0, Lcom/android/fmradio/FmService;->mAudioManager:Landroid/media/AudioManager;

    const/4 v2, 0x3

    invoke-virtual {v1, v2}, Landroid/media/AudioManager;->getStreamVolume(I)I

    move-result v1

    .line 617
    sget-object v2, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    new-instance v3, Ljava/lang/StringBuilder;

    invoke-direct {v3}, Ljava/lang/StringBuilder;-><init>()V

    const-string v4, "---->>setMute()  volume: "

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v3, v1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v3}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v3

    invoke-static {v2, v3}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 618
    invoke-virtual {p0, v1}, Lcom/android/fmradio/FmService;->setVolume(I)Z

    .line 623
    :goto_0
    iput-boolean p1, p0, Lcom/android/fmradio/FmService;->mIsMuted:Z

    return v0
.end method

.method public setRdsAsync(Z)V
    .locals 3

    .line 575
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mFmServiceHandler:Lcom/android/fmradio/FmService$FmRadioServiceHandler;

    const/4 v1, 0x5

    invoke-virtual {v0, v1}, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->removeMessages(I)V

    .line 576
    new-instance v0, Landroid/os/Bundle;

    const/4 v2, 0x1

    invoke-direct {v0, v2}, Landroid/os/Bundle;-><init>(I)V

    const-string v2, "option"

    .line 577
    invoke-virtual {v0, v2, p1}, Landroid/os/Bundle;->putBoolean(Ljava/lang/String;Z)V

    .line 578
    iget-object p1, p0, Lcom/android/fmradio/FmService;->mFmServiceHandler:Lcom/android/fmradio/FmService$FmRadioServiceHandler;

    invoke-virtual {p1, v1}, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->obtainMessage(I)Landroid/os/Message;

    move-result-object p1

    .line 579
    invoke-virtual {p1, v0}, Landroid/os/Message;->setData(Landroid/os/Bundle;)V

    .line 580
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mFmServiceHandler:Lcom/android/fmradio/FmService$FmRadioServiceHandler;

    invoke-virtual {v0, p1}, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->sendMessage(Landroid/os/Message;)Z

    return-void
.end method

.method public setVolume(I)Z
    .locals 9

    const/16 v0, 0x8

    const-string v1, "persist.sys.fm.max.volume"

    .line 1790
    invoke-static {v1, v0}, Landroid/os/SystemProperties;->getInt(Ljava/lang/String;I)I

    move-result v1

    const/4 v2, 0x0

    const/4 v3, 0x1

    const/16 v4, 0x9

    if-lt p1, v4, :cond_0

    move v0, v1

    goto :goto_0

    :cond_0
    if-ne p1, v0, :cond_1

    if-le v1, v3, :cond_8

    add-int/lit8 v0, v1, -0x1

    goto :goto_0

    :cond_1
    const/4 v0, 0x2

    const/4 v4, 0x7

    if-ne p1, v4, :cond_2

    if-le v1, v0, :cond_8

    add-int/lit8 v0, v1, -0x2

    goto :goto_0

    :cond_2
    const/4 v5, 0x3

    const/4 v6, 0x6

    if-ne p1, v6, :cond_3

    if-le v1, v5, :cond_8

    add-int/lit8 v0, v1, -0x3

    goto :goto_0

    :cond_3
    const/4 v7, 0x4

    const/4 v8, 0x5

    if-ne p1, v8, :cond_4

    if-le v1, v7, :cond_8

    add-int/lit8 v0, v1, -0x4

    goto :goto_0

    :cond_4
    if-ne p1, v7, :cond_5

    if-le v1, v8, :cond_8

    add-int/lit8 v0, v1, -0x5

    goto :goto_0

    :cond_5
    if-ne p1, v5, :cond_6

    if-le v1, v6, :cond_8

    add-int/lit8 v0, v1, -0x6

    goto :goto_0

    :cond_6
    if-ne p1, v0, :cond_7

    if-le v1, v4, :cond_8

    add-int/lit8 v0, v1, -0x7

    goto :goto_0

    :cond_7
    if-ne p1, v3, :cond_9

    :cond_8
    move v0, v3

    goto :goto_0

    :cond_9
    if-gtz p1, :cond_a

    move v0, v2

    goto :goto_0

    :cond_a
    move v0, p1

    .line 1813
    :goto_0
    sget-object v4, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    new-instance v5, Ljava/lang/StringBuilder;

    invoke-direct {v5}, Ljava/lang/StringBuilder;-><init>()V

    const-string v6, "setVolume("

    invoke-virtual {v5, v6}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v5, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string p1, "), new volume="

    invoke-virtual {v5, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v5, v0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string p1, ", max_fm_volume="

    invoke-virtual {v5, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v5, v1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string p1, ", mIsDeviceOpen="

    invoke-virtual {v5, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-boolean p1, p0, Lcom/android/fmradio/FmService;->mIsDeviceOpen:Z

    invoke-virtual {v5, p1}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    invoke-virtual {v5}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p1

    invoke-static {v4, p1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 1817
    iget-boolean p1, p0, Lcom/android/fmradio/FmService;->mIsDeviceOpen:Z

    if-eqz p1, :cond_c

    if-nez v0, :cond_b

    .line 1819
    iget-object p1, p0, Lcom/android/fmradio/FmService;->mFmManager:Lcom/android/fmradio/FmManagerSelect;

    invoke-virtual {p1, v3}, Lcom/android/fmradio/FmManagerSelect;->setMute(Z)I

    goto :goto_1

    .line 1821
    :cond_b
    iget-object p1, p0, Lcom/android/fmradio/FmService;->mFmManager:Lcom/android/fmradio/FmManagerSelect;

    invoke-virtual {p1, v2}, Lcom/android/fmradio/FmManagerSelect;->setMute(Z)I

    .line 1824
    :goto_1
    iget-object p1, p0, Lcom/android/fmradio/FmService;->mFmManager:Lcom/android/fmradio/FmManagerSelect;

    invoke-virtual {p1, v0}, Lcom/android/fmradio/FmManagerSelect;->setVolume(I)Z

    move v2, v3

    :cond_c
    return v2
.end method

.method public startScanAsync()V
    .locals 3

    .line 497
    sget-object v0, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "startScanAsync - mIsSeeking: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-boolean v2, p0, Lcom/android/fmradio/FmService;->mIsSeeking:Z

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 498
    iget-boolean v0, p0, Lcom/android/fmradio/FmService;->mIsSeeking:Z

    if-eqz v0, :cond_0

    return-void

    .line 500
    :cond_0
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mFmServiceHandler:Lcom/android/fmradio/FmService$FmRadioServiceHandler;

    const/16 v1, 0xd

    invoke-virtual {v0, v1}, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->removeMessages(I)V

    .line 501
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mFmServiceHandler:Lcom/android/fmradio/FmService$FmRadioServiceHandler;

    invoke-virtual {v0, v1}, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->sendEmptyMessage(I)Z

    return-void
.end method

.method public stopScan()Z
    .locals 3

    .line 556
    sget-object v0, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "---->>stopScan()  mPowerStatus: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget v2, p0, Lcom/android/fmradio/FmService;->mPowerStatus:I

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v2, " mIsNativeScanning: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-boolean v2, p0, Lcom/android/fmradio/FmService;->mIsNativeScanning:Z

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    const-string v2, " mIsNativeSeeking: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-boolean v2, p0, Lcom/android/fmradio/FmService;->mIsNativeSeeking:Z

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    const/4 v0, 0x0

    .line 561
    iput-boolean v0, p0, Lcom/android/fmradio/FmService;->mIsStopScanCalled:Z

    .line 562
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mFmManager:Lcom/android/fmradio/FmManagerSelect;

    invoke-virtual {v0}, Lcom/android/fmradio/FmManagerSelect;->stopScan()Z

    move-result v0

    .line 563
    sget-object v1, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    const-string v2, "stopScan finish"

    invoke-static {v1, v2}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    return v0
.end method

.method public tuneStationAsync(FZ)V
    .locals 3

    .line 412
    sget-object v0, Lcom/android/fmradio/FmService;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "---->>tuneStationAsync()  frequency: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(F)Ljava/lang/StringBuilder;

    const-string v2, " needNotifyInfo: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p2}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 413
    iget-object v0, p0, Lcom/android/fmradio/FmService;->mFmServiceHandler:Lcom/android/fmradio/FmService$FmRadioServiceHandler;

    const/16 v1, 0xf

    invoke-virtual {v0, v1}, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->removeMessages(I)V

    .line 415
    new-instance v0, Landroid/os/Bundle;

    const/4 v2, 0x1

    invoke-direct {v0, v2}, Landroid/os/Bundle;-><init>(I)V

    const-string v2, "frequency"

    .line 416
    invoke-virtual {v0, v2, p1}, Landroid/os/Bundle;->putFloat(Ljava/lang/String;F)V

    const-string p1, "key_need_notify_info"

    .line 417
    invoke-virtual {v0, p1, p2}, Landroid/os/Bundle;->putBoolean(Ljava/lang/String;Z)V

    .line 418
    iget-object p1, p0, Lcom/android/fmradio/FmService;->mFmServiceHandler:Lcom/android/fmradio/FmService$FmRadioServiceHandler;

    invoke-virtual {p1, v1}, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->obtainMessage(I)Landroid/os/Message;

    move-result-object p1

    .line 419
    invoke-virtual {p1, v0}, Landroid/os/Message;->setData(Landroid/os/Bundle;)V

    .line 420
    iget-object p2, p0, Lcom/android/fmradio/FmService;->mFmServiceHandler:Lcom/android/fmradio/FmService$FmRadioServiceHandler;

    invoke-virtual {p2, p1}, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->sendMessage(Landroid/os/Message;)Z

    return-void
.end method

.method public unregisterFmRadioListener(Lcom/android/fmradio/iface/FmListener;)V
    .locals 0

    .line 1317
    invoke-virtual {p1}, Ljava/lang/Object;->hashCode()I

    move-result p1

    invoke-direct {p0, p1}, Lcom/android/fmradio/FmService;->remove(I)V

    return-void
.end method
