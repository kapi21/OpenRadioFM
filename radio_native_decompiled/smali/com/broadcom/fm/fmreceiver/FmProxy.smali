.class public final Lcom/broadcom/fm/fmreceiver/FmProxy;
.super Ljava/lang/Object;
.source "FmProxy.java"


# annotations
.annotation system Ldalvik/annotation/MemberClasses;
    value = {
        Lcom/broadcom/fm/fmreceiver/FmProxy$EventCallbackHandler;,
        Lcom/broadcom/fm/fmreceiver/FmProxy$FmBroadcastReceiver;,
        Lcom/broadcom/fm/fmreceiver/FmProxy$FmReceiverCallback;
    }
.end annotation


# static fields
.field public static final ACTION_ON_AUDIO_MODE:Ljava/lang/String; = "com.broadcom.bt.app.fm.action.ON_AUDIO_MODE"

.field public static final ACTION_ON_AUDIO_PATH:Ljava/lang/String; = "com.broadcom.bt.app.fm.action.ON_AUDIO_PATH"

.field public static final ACTION_ON_AUDIO_QUAL:Ljava/lang/String; = "com.broadcom.bt.app.fm.action.ON_AUDIO_QUAL"

.field public static final ACTION_ON_EST_NFL:Ljava/lang/String; = "com.broadcom.bt.app.fm.action.ON_EST_NFL"

.field public static final ACTION_ON_RDS_DATA:Ljava/lang/String; = "com.broadcom.bt.app.fm.action.ON_RDS_DATA"

.field public static final ACTION_ON_RDS_MODE:Ljava/lang/String; = "com.broadcom.bt.app.fm.action.ON_RDS_MODE"

.field public static final ACTION_ON_SEEK_CMPL:Ljava/lang/String; = "com.broadcom.bt.app.fm.action.ON_SEEK_CMPL"

.field public static final ACTION_ON_STATUS:Ljava/lang/String; = "com.broadcom.bt.app.fm.action.ON_STATUS"

.field public static final ACTION_ON_VOL:Ljava/lang/String; = "ON_VOL"

.field public static final ACTION_ON_WRLD_RGN:Ljava/lang/String; = "com.broadcom.bt.app.fm.action.ON_WRLD_RGN"

.field private static final ACTION_PREFIX:Ljava/lang/String; = "com.broadcom.bt.app.fm.action."

.field private static final ACTION_PREFIX_LENGTH:I = 0x1e

.field public static final AF_MODE_DEFAULT:I = 0x0

.field public static final AF_MODE_OFF:I = 0x0

.field public static final AF_MODE_ON:I = 0x1

.field public static final AUDIO_MODE_AUTO:I = 0x0

.field public static final AUDIO_MODE_BLEND:I = 0x3

.field public static final AUDIO_MODE_MONO:I = 0x2

.field public static final AUDIO_MODE_STEREO:I = 0x1

.field public static final AUDIO_MODE_SWITCH:I = 0x3

.field public static final AUDIO_PATH_DIGITAL:I = 0x3

.field public static final AUDIO_PATH_NONE:I = 0x0

.field public static final AUDIO_PATH_SPEAKER:I = 0x1

.field public static final AUDIO_PATH_WIRE_HEADSET:I = 0x2

.field public static final AUDIO_QUALITY_BLEND:I = 0x4

.field public static final AUDIO_QUALITY_MONO:I = 0x2

.field public static final AUDIO_QUALITY_STEREO:I = 0x1

.field public static final BLUETOOTH_PERM:Ljava/lang/String; = "android.permission.BLUETOOTH"

.field private static final D:Z = true

.field public static final DEEMPHASIS_50U:I = 0x0

.field public static final DEEMPHASIS_75U:I = 0x40

.field public static final DEEMPHASIS_TIME_DEFAULT:I = 0x40

.field public static final DEFAULT_BROADCAST_RECEIVER_PRIORITY:I = 0xc8

.field public static final EXTRA_ALT_FREQ_MODE:Ljava/lang/String; = "ALT_FREQ_MODE"

.field public static final EXTRA_AUDIO_MODE:Ljava/lang/String; = "AUDIO_MODE"

.field public static final EXTRA_AUDIO_PATH:Ljava/lang/String; = "AUDIO_PATH"

.field public static final EXTRA_FREQ:Ljava/lang/String; = "FREQ"

.field public static final EXTRA_MUTED:Ljava/lang/String; = "MUTED"

.field public static final EXTRA_NFL:Ljava/lang/String; = "NFL"

.field public static final EXTRA_RADIO_ON:Ljava/lang/String; = "RADIO_ON"

.field public static final EXTRA_RDS_DATA_TYPE:Ljava/lang/String; = "RDS_DATA_TYPE"

.field public static final EXTRA_RDS_IDX:Ljava/lang/String; = "RDS_IDX"

.field public static final EXTRA_RDS_MODE:Ljava/lang/String; = "RDS_MODE"

.field public static final EXTRA_RDS_PRGM_SVC:Ljava/lang/String; = "RDS_PRGM_SVC"

.field public static final EXTRA_RDS_PRGM_TYPE:Ljava/lang/String; = "RDS_PRGM_TYPE"

.field public static final EXTRA_RDS_PRGM_TYPE_NAME:Ljava/lang/String; = "RDS_PRGM_TYPE_NAME"

.field public static final EXTRA_RDS_TXT:Ljava/lang/String; = "RDS_TXT"

.field public static final EXTRA_RSSI:Ljava/lang/String; = "RSSI"

.field public static final EXTRA_SNR:Ljava/lang/String; = "SNR"

.field public static final EXTRA_STATUS:Ljava/lang/String; = "STATUS"

.field public static final EXTRA_SUCCESS:Ljava/lang/String; = "RDS_SUCCESS"

.field public static final EXTRA_VOL:Ljava/lang/String; = "VOL"

.field public static final EXTRA_WRLD_RGN:Ljava/lang/String; = "WRLD_RGN"

.field public static final FM_MAX_SNR_THRESHOLD:I = 0x1f

.field public static final FM_MIN_SNR_THRESHOLD:I = 0x0

.field public static final FM_RECEIVER_PERM:Ljava/lang/String; = "android.permission.ACCESS_FM_RECEIVER"

.field public static final FM_VOLUME_MAX:I = 0xff

.field public static final FREQ_STEP_100KHZ:I = 0x0

.field public static final FREQ_STEP_50KHZ:I = 0x10

.field public static final FREQ_STEP_DEFAULT:I = 0x0

.field public static final FUNCTIONALITY_DEFAULT:I = 0x0

.field public static final FUNC_AF:I = 0x40

.field public static final FUNC_RBDS:I = 0x20

.field public static final FUNC_RDS:I = 0x10

.field public static final FUNC_REGION_DEFAULT:I = 0x0

.field public static final FUNC_REGION_EUR:I = 0x1

.field public static final FUNC_REGION_JP:I = 0x2

.field public static final FUNC_REGION_JP_II:I = 0x3

.field public static final FUNC_REGION_NA:I = 0x0

.field public static final FUNC_SOFTMUTE:I = 0x100

.field public static final LIVE_AUDIO_QUALITY_DEFAULT:Z = false

.field public static final MIN_SIGNAL_STRENGTH_DEFAULT:I = 0x69

.field public static final NFL_DEFAULT:I = 0x1

.field public static final NFL_FINE:I = 0x2

.field public static final NFL_LOW:I = 0x0

.field public static final NFL_MED:I = 0x1

.field public static final RDS_COND_NONE:I = 0x0

.field public static final RDS_COND_PTY:I = 0x1

.field public static final RDS_COND_PTY_VAL:I = 0x0

.field public static final RDS_COND_TP:I = 0x2

.field public static final RDS_FEATURE_PS:I = 0x4

.field public static final RDS_FEATURE_PTY:I = 0x8

.field public static final RDS_FEATURE_PTYN:I = 0x20

.field public static final RDS_FEATURE_RT:I = 0x40

.field public static final RDS_FEATURE_TP:I = 0x10

.field public static final RDS_MODE_DEFAULT_ON:I = 0x1

.field public static final RDS_MODE_OFF:I = 0x0

.field public static final RDS_MODE_RBDS_ON:I = 0x3

.field public static final RDS_MODE_RDS_ON:I = 0x2

.field public static final SCAN_MODE_DOWN:I = 0x0

.field public static final SCAN_MODE_FAST:I = 0x1

.field public static final SCAN_MODE_FULL:I = 0x82

.field public static final SCAN_MODE_NORMAL:I = 0x0

.field public static final SCAN_MODE_UP:I = 0x80

.field public static final SIGNAL_POLL_INTERVAL_DEFAULT:I = 0x64

.field public static final STATUS_FAIL:I = 0x1

.field public static final STATUS_ILLEGAL_COMMAND:I = 0x3

.field public static final STATUS_ILLEGAL_PARAMETERS:I = 0x4

.field public static final STATUS_OK:I = 0x0

.field public static final STATUS_SERVER_FAIL:I = 0x2

.field private static final TAG:Ljava/lang/String; = "FmProxy"


# instance fields
.field protected mBroadcastReceiver:Landroid/content/BroadcastReceiver;

.field private mCallback:Lcom/broadcom/fm/fmreceiver/IFmReceiverCallback;

.field private mConnection:Landroid/content/ServiceConnection;

.field protected mContext:Landroid/content/Context;

.field protected mEventCallbackHandler:Lcom/broadcom/fm/fmreceiver/FmProxy$EventCallbackHandler;

.field private mEventHandler:Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;

.field protected mIsAvailable:Z

.field protected mProxyCback:Lcom/broadcom/fm/fmreceiver/IFmProxyCallback;

.field protected mReceiverPriority:I

.field private mService:Lcom/broadcom/fm/fmreceiver/IFmReceiverService;


# direct methods
.method static constructor <clinit>()V
    .locals 0

    return-void
.end method

.method public constructor <init>(Landroid/content/Context;Lcom/broadcom/fm/fmreceiver/IFmProxyCallback;)V
    .locals 3

    .line 615
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    const/4 v0, 0x0

    .line 585
    iput-object v0, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mEventHandler:Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;

    const/16 v0, 0xc8

    .line 1549
    iput v0, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mReceiverPriority:I

    .line 1636
    new-instance v0, Lcom/broadcom/fm/fmreceiver/FmProxy$1;

    invoke-direct {v0, p0}, Lcom/broadcom/fm/fmreceiver/FmProxy$1;-><init>(Lcom/broadcom/fm/fmreceiver/FmProxy;)V

    iput-object v0, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mConnection:Landroid/content/ServiceConnection;

    .line 616
    new-instance v0, Ljava/lang/StringBuilder;

    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    const-string v1, "FmProxy object created obj ="

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v0, p0}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    const-string v1, "FmProxy"

    invoke-static {v1, v0}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    .line 617
    iput-object p1, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mContext:Landroid/content/Context;

    .line 618
    iput-object p2, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mProxyCback:Lcom/broadcom/fm/fmreceiver/IFmProxyCallback;

    .line 620
    invoke-static {}, Landroid/bluetooth/BluetoothAdapter;->getDefaultAdapter()Landroid/bluetooth/BluetoothAdapter;

    move-result-object p1

    if-nez p1, :cond_0

    const-string p1, "BluetoothAdapter is null."

    .line 622
    invoke-static {v1, p1}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;)I

    return-void

    .line 626
    :cond_0
    new-instance p1, Landroid/content/Intent;

    const-class p2, Lcom/broadcom/fm/fmreceiver/IFmReceiverService;

    invoke-virtual {p2}, Ljava/lang/Class;->getName()Ljava/lang/String;

    move-result-object p2

    invoke-direct {p1, p2}, Landroid/content/Intent;-><init>(Ljava/lang/String;)V

    .line 627
    iget-object p2, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mContext:Landroid/content/Context;

    invoke-virtual {p2}, Landroid/content/Context;->getPackageManager()Landroid/content/pm/PackageManager;

    move-result-object p2

    const/4 v0, 0x0

    invoke-virtual {p1, p2, v0}, Landroid/content/Intent;->resolveSystemService(Landroid/content/pm/PackageManager;I)Landroid/content/ComponentName;

    move-result-object p2

    invoke-virtual {p1, p2}, Landroid/content/Intent;->setComponent(Landroid/content/ComponentName;)Landroid/content/Intent;

    .line 628
    iget-object p2, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mContext:Landroid/content/Context;

    iget-object v0, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mConnection:Landroid/content/ServiceConnection;

    const/4 v2, 0x1

    invoke-virtual {p2, p1, v0, v2}, Landroid/content/Context;->bindService(Landroid/content/Intent;Landroid/content/ServiceConnection;I)Z

    move-result p1

    if-nez p1, :cond_1

    const-string p1, "Could not bind to IFmReceiverService Service"

    .line 629
    invoke-static {v1, p1}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;)I

    :cond_1
    return-void
.end method

.method static synthetic access$100(Lcom/broadcom/fm/fmreceiver/FmProxy;)Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;
    .locals 0

    .line 111
    iget-object p0, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mEventHandler:Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;

    return-object p0
.end method

.method static synthetic access$200()I
    .locals 1

    .line 111
    sget v0, Lcom/broadcom/fm/fmreceiver/FmProxy;->ACTION_PREFIX_LENGTH:I

    return v0
.end method

.method static synthetic access$302(Lcom/broadcom/fm/fmreceiver/FmProxy;Lcom/broadcom/fm/fmreceiver/IFmReceiverService;)Lcom/broadcom/fm/fmreceiver/IFmReceiverService;
    .locals 0

    .line 111
    iput-object p1, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mService:Lcom/broadcom/fm/fmreceiver/IFmReceiverService;

    return-object p1
.end method

.method protected static actionsEqual(Ljava/lang/String;Ljava/lang/String;I)Z
    .locals 2

    if-ne p0, p1, :cond_0

    const/4 p0, 0x1

    return p0

    .line 1539
    :cond_0
    invoke-virtual {p0}, Ljava/lang/String;->length()I

    move-result v0

    .line 1540
    invoke-virtual {p1}, Ljava/lang/String;->length()I

    move-result v1

    if-eq v0, v1, :cond_1

    const/4 p0, 0x0

    return p0

    :cond_1
    sub-int/2addr v0, p2

    .line 1543
    invoke-virtual {p0, p2, p1, p2, v0}, Ljava/lang/String;->regionMatches(ILjava/lang/String;II)Z

    move-result p0

    return p0
.end method

.method public static createFilter(Landroid/content/IntentFilter;)Landroid/content/IntentFilter;
    .locals 1

    if-nez p0, :cond_0

    .line 711
    new-instance p0, Landroid/content/IntentFilter;

    invoke-direct {p0}, Landroid/content/IntentFilter;-><init>()V

    :cond_0
    const-string v0, "com.broadcom.bt.app.fm.action.ON_AUDIO_MODE"

    .line 713
    invoke-virtual {p0, v0}, Landroid/content/IntentFilter;->addAction(Ljava/lang/String;)V

    const-string v0, "com.broadcom.bt.app.fm.action.ON_AUDIO_PATH"

    .line 714
    invoke-virtual {p0, v0}, Landroid/content/IntentFilter;->addAction(Ljava/lang/String;)V

    const-string v0, "com.broadcom.bt.app.fm.action.ON_AUDIO_QUAL"

    .line 715
    invoke-virtual {p0, v0}, Landroid/content/IntentFilter;->addAction(Ljava/lang/String;)V

    const-string v0, "com.broadcom.bt.app.fm.action.ON_EST_NFL"

    .line 716
    invoke-virtual {p0, v0}, Landroid/content/IntentFilter;->addAction(Ljava/lang/String;)V

    const-string v0, "com.broadcom.bt.app.fm.action.ON_RDS_DATA"

    .line 717
    invoke-virtual {p0, v0}, Landroid/content/IntentFilter;->addAction(Ljava/lang/String;)V

    const-string v0, "com.broadcom.bt.app.fm.action.ON_RDS_MODE"

    .line 718
    invoke-virtual {p0, v0}, Landroid/content/IntentFilter;->addAction(Ljava/lang/String;)V

    const-string v0, "com.broadcom.bt.app.fm.action.ON_SEEK_CMPL"

    .line 719
    invoke-virtual {p0, v0}, Landroid/content/IntentFilter;->addAction(Ljava/lang/String;)V

    const-string v0, "com.broadcom.bt.app.fm.action.ON_STATUS"

    .line 720
    invoke-virtual {p0, v0}, Landroid/content/IntentFilter;->addAction(Ljava/lang/String;)V

    const-string v0, "ON_VOL"

    .line 721
    invoke-virtual {p0, v0}, Landroid/content/IntentFilter;->addAction(Ljava/lang/String;)V

    const-string v0, "com.broadcom.bt.app.fm.action.ON_WRLD_RGN"

    .line 722
    invoke-virtual {p0, v0}, Landroid/content/IntentFilter;->addAction(Ljava/lang/String;)V

    return-object p0
.end method

.method public static getProxy(Landroid/content/Context;Lcom/broadcom/fm/fmreceiver/IFmProxyCallback;)Z
    .locals 1

    .line 604
    :try_start_0
    new-instance v0, Lcom/broadcom/fm/fmreceiver/FmProxy;

    invoke-direct {v0, p0, p1}, Lcom/broadcom/fm/fmreceiver/FmProxy;-><init>(Landroid/content/Context;Lcom/broadcom/fm/fmreceiver/IFmProxyCallback;)V
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    const/4 p0, 0x1

    return p0

    :catchall_0
    move-exception p0

    const-string p1, "FmProxy"

    const-string v0, "Unable to get FM Proxy"

    .line 606
    invoke-static {p1, v0, p0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    const/4 p0, 0x0

    return p0
.end method


# virtual methods
.method protected baseFinalize()V
    .locals 0

    .line 1601
    invoke-virtual {p0}, Lcom/broadcom/fm/fmreceiver/FmProxy;->finish()V

    return-void
.end method

.method public declared-synchronized baseFinish()V
    .locals 3

    monitor-enter p0

    :try_start_0
    const-string v0, "FmProxy"

    .line 1580
    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "finish() mContext = "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-object v2, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mContext:Landroid/content/Context;

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    .line 1582
    iget-object v0, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mEventCallbackHandler:Lcom/broadcom/fm/fmreceiver/FmProxy$EventCallbackHandler;

    const/4 v1, 0x0

    if-eqz v0, :cond_0

    .line 1583
    iget-object v0, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mEventCallbackHandler:Lcom/broadcom/fm/fmreceiver/FmProxy$EventCallbackHandler;

    invoke-virtual {v0}, Lcom/broadcom/fm/fmreceiver/FmProxy$EventCallbackHandler;->finish()V

    .line 1584
    iput-object v1, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mEventCallbackHandler:Lcom/broadcom/fm/fmreceiver/FmProxy$EventCallbackHandler;

    .line 1587
    :cond_0
    iget-object v0, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mContext:Landroid/content/Context;

    if-eqz v0, :cond_1

    .line 1588
    iget-object v0, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mContext:Landroid/content/Context;

    iget-object v2, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mConnection:Landroid/content/ServiceConnection;

    invoke-virtual {v0, v2}, Landroid/content/Context;->unbindService(Landroid/content/ServiceConnection;)V

    .line 1589
    iput-object v1, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mContext:Landroid/content/Context;
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 1591
    :cond_1
    monitor-exit p0

    return-void

    :catchall_0
    move-exception v0

    monitor-exit p0

    throw v0
.end method

.method public declared-synchronized cleanupFmService()I
    .locals 4

    monitor-enter p0

    const/4 v0, 0x2

    .line 857
    :try_start_0
    iget-object v1, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mService:Lcom/broadcom/fm/fmreceiver/IFmReceiverService;

    invoke-interface {v1}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService;->cleanupFmService()I
    :try_end_0
    .catch Landroid/os/RemoteException; {:try_start_0 .. :try_end_0} :catch_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    goto :goto_0

    :catchall_0
    move-exception v0

    goto :goto_1

    :catch_0
    move-exception v1

    :try_start_1
    const-string v2, "FmProxy"

    const-string v3, "cleanupFmService() failed"

    .line 859
    invoke-static {v2, v3, v1}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    :goto_0
    const-string v1, "FmProxy"

    const-string v2, "cleanup triggered"

    .line 861
    invoke-static {v1, v2}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    .line 862
    monitor-exit p0

    return v0

    :goto_1
    monitor-exit p0

    throw v0
.end method

.method public declared-synchronized estimateNoiseFloorLevel(I)I
    .locals 3

    monitor-enter p0

    const/4 v0, 0x2

    .line 1319
    :try_start_0
    iget-object v1, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mService:Lcom/broadcom/fm/fmreceiver/IFmReceiverService;

    invoke-interface {v1, p1}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService;->estimateNoiseFloorLevel(I)I

    move-result v0
    :try_end_0
    .catch Landroid/os/RemoteException; {:try_start_0 .. :try_end_0} :catch_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    goto :goto_0

    :catchall_0
    move-exception p1

    goto :goto_1

    :catch_0
    move-exception p1

    :try_start_1
    const-string v1, "FmProxy"

    const-string v2, "estimateNoiseFloorLevel() failed"

    .line 1321
    invoke-static {v1, v2, p1}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    .line 1324
    :goto_0
    monitor-exit p0

    return v0

    :goto_1
    monitor-exit p0

    throw p1
.end method

.method protected finalize()V
    .locals 0

    .line 1374
    invoke-virtual {p0}, Lcom/broadcom/fm/fmreceiver/FmProxy;->baseFinalize()V

    return-void
.end method

.method public declared-synchronized finish()V
    .locals 4

    monitor-enter p0

    .line 746
    :try_start_0
    iget-object v0, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mEventHandler:Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;

    const/4 v1, 0x0

    if-eqz v0, :cond_0

    .line 747
    iput-object v1, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mEventHandler:Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;

    .line 756
    :cond_0
    iget-object v0, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mCallback:Lcom/broadcom/fm/fmreceiver/IFmReceiverCallback;

    if-eqz v0, :cond_1

    iget-object v0, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mService:Lcom/broadcom/fm/fmreceiver/IFmReceiverService;
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_1

    if-eqz v0, :cond_1

    .line 758
    :try_start_1
    iget-object v0, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mService:Lcom/broadcom/fm/fmreceiver/IFmReceiverService;

    iget-object v2, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mCallback:Lcom/broadcom/fm/fmreceiver/IFmReceiverCallback;

    invoke-interface {v0, v2}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService;->unregisterCallback(Lcom/broadcom/fm/fmreceiver/IFmReceiverCallback;)V
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    goto :goto_0

    :catchall_0
    move-exception v0

    :try_start_2
    const-string v2, "FmProxy"

    const-string v3, "Unable to unregister callback"

    .line 760
    invoke-static {v2, v3, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    .line 762
    :goto_0
    iput-object v1, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mCallback:Lcom/broadcom/fm/fmreceiver/IFmReceiverCallback;

    .line 765
    :cond_1
    invoke-virtual {p0}, Lcom/broadcom/fm/fmreceiver/FmProxy;->baseFinish()V

    .line 766
    iput-object v1, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mContext:Landroid/content/Context;

    .line 767
    iput-object v1, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mService:Lcom/broadcom/fm/fmreceiver/IFmReceiverService;
    :try_end_2
    .catchall {:try_start_2 .. :try_end_2} :catchall_1

    .line 768
    monitor-exit p0

    return-void

    :catchall_1
    move-exception v0

    monitor-exit p0

    throw v0
.end method

.method protected declared-synchronized finishEventCallbackHandler()V
    .locals 1

    monitor-enter p0

    .line 1567
    :try_start_0
    iget-object v0, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mEventCallbackHandler:Lcom/broadcom/fm/fmreceiver/FmProxy$EventCallbackHandler;

    if-eqz v0, :cond_0

    .line 1568
    iget-object v0, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mEventCallbackHandler:Lcom/broadcom/fm/fmreceiver/FmProxy$EventCallbackHandler;

    invoke-virtual {v0}, Lcom/broadcom/fm/fmreceiver/FmProxy$EventCallbackHandler;->finish()V

    const/4 v0, 0x0

    .line 1569
    iput-object v0, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mEventCallbackHandler:Lcom/broadcom/fm/fmreceiver/FmProxy$EventCallbackHandler;
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 1571
    :cond_0
    monitor-exit p0

    return-void

    :catchall_0
    move-exception v0

    monitor-exit p0

    throw v0
.end method

.method public getIsMute()Z
    .locals 3

    .line 963
    :try_start_0
    iget-object v0, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mService:Lcom/broadcom/fm/fmreceiver/IFmReceiverService;

    invoke-interface {v0}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService;->getIsMute()Z

    move-result v0
    :try_end_0
    .catch Landroid/os/RemoteException; {:try_start_0 .. :try_end_0} :catch_0

    goto :goto_0

    :catch_0
    move-exception v0

    const-string v1, "FmProxy"

    const-string v2, "getIsMute() failed"

    .line 965
    invoke-static {v1, v2, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    const/4 v0, 0x0

    :goto_0
    return v0
.end method

.method public getMonoStereoMode()I
    .locals 3

    .line 933
    :try_start_0
    iget-object v0, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mService:Lcom/broadcom/fm/fmreceiver/IFmReceiverService;

    invoke-interface {v0}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService;->getMonoStereoMode()I

    move-result v0
    :try_end_0
    .catch Landroid/os/RemoteException; {:try_start_0 .. :try_end_0} :catch_0

    goto :goto_0

    :catch_0
    move-exception v0

    const-string v1, "FmProxy"

    const-string v2, "getMonoStereoMode() failed"

    .line 935
    invoke-static {v1, v2, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    const/4 v0, 0x0

    :goto_0
    return v0
.end method

.method public getRadioIsOn()Z
    .locals 4

    .line 910
    iget-object v0, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mService:Lcom/broadcom/fm/fmreceiver/IFmReceiverService;

    const/4 v1, 0x0

    if-nez v0, :cond_0

    return v1

    .line 914
    :cond_0
    :try_start_0
    invoke-interface {v0}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService;->getRadioIsOn()Z

    move-result v1
    :try_end_0
    .catch Landroid/os/RemoteException; {:try_start_0 .. :try_end_0} :catch_0

    goto :goto_0

    :catch_0
    move-exception v0

    const-string v2, "FmProxy"

    const-string v3, "getRadioIsOn() failed"

    .line 916
    invoke-static {v2, v3, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    :goto_0
    return v1
.end method

.method public declared-synchronized getStatus()I
    .locals 4

    monitor-enter p0

    const/4 v0, 0x2

    .line 896
    :try_start_0
    iget-object v1, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mService:Lcom/broadcom/fm/fmreceiver/IFmReceiverService;

    invoke-interface {v1}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService;->getStatus()I

    move-result v0
    :try_end_0
    .catch Landroid/os/RemoteException; {:try_start_0 .. :try_end_0} :catch_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    goto :goto_0

    :catchall_0
    move-exception v0

    goto :goto_1

    :catch_0
    move-exception v1

    :try_start_1
    const-string v2, "FmProxy"

    const-string v3, "getStatus() failed"

    .line 898
    invoke-static {v2, v3, v1}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    .line 901
    :goto_0
    monitor-exit p0

    return v0

    :goto_1
    monitor-exit p0

    throw v0
.end method

.method public getTunedFrequency()I
    .locals 3

    .line 948
    :try_start_0
    iget-object v0, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mService:Lcom/broadcom/fm/fmreceiver/IFmReceiverService;

    invoke-interface {v0}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService;->getTunedFrequency()I

    move-result v0
    :try_end_0
    .catch Landroid/os/RemoteException; {:try_start_0 .. :try_end_0} :catch_0

    goto :goto_0

    :catch_0
    move-exception v0

    const-string v1, "FmProxy"

    const-string v2, "getTunedFrequency() failed"

    .line 950
    invoke-static {v1, v2, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    const/4 v0, 0x0

    :goto_0
    return v0
.end method

.method protected init(Landroid/os/IBinder;)Z
    .locals 2

    .line 639
    :try_start_0
    invoke-static {p1}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService$Stub;->asInterface(Landroid/os/IBinder;)Lcom/broadcom/fm/fmreceiver/IFmReceiverService;

    move-result-object p1

    iput-object p1, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mService:Lcom/broadcom/fm/fmreceiver/IFmReceiverService;
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    const/4 p1, 0x1

    return p1

    :catchall_0
    move-exception p1

    const-string v0, "FmProxy"

    const-string v1, "Unable to initialize BluetoothFM proxy with service"

    .line 642
    invoke-static {v0, v1, p1}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    const/4 p1, 0x0

    return p1
.end method

.method protected declared-synchronized initEventCallbackHandler()Landroid/os/Handler;
    .locals 2

    monitor-enter p0

    .line 1554
    :try_start_0
    iget-object v0, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mEventCallbackHandler:Lcom/broadcom/fm/fmreceiver/FmProxy$EventCallbackHandler;

    if-nez v0, :cond_0

    .line 1555
    new-instance v0, Lcom/broadcom/fm/fmreceiver/FmProxy$EventCallbackHandler;

    invoke-direct {v0, p0}, Lcom/broadcom/fm/fmreceiver/FmProxy$EventCallbackHandler;-><init>(Lcom/broadcom/fm/fmreceiver/FmProxy;)V

    iput-object v0, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mEventCallbackHandler:Lcom/broadcom/fm/fmreceiver/FmProxy$EventCallbackHandler;

    .line 1556
    iget-object v0, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mEventCallbackHandler:Lcom/broadcom/fm/fmreceiver/FmProxy$EventCallbackHandler;

    invoke-virtual {v0}, Lcom/broadcom/fm/fmreceiver/FmProxy$EventCallbackHandler;->start()V

    .line 1557
    :catch_0
    :goto_0
    iget-object v0, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mEventCallbackHandler:Lcom/broadcom/fm/fmreceiver/FmProxy$EventCallbackHandler;

    iget-object v0, v0, Lcom/broadcom/fm/fmreceiver/FmProxy$EventCallbackHandler;->mHandler:Landroid/os/Handler;
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    if-nez v0, :cond_0

    const-wide/16 v0, 0x64

    .line 1559
    :try_start_1
    invoke-static {v0, v1}, Ljava/lang/Thread;->sleep(J)V
    :try_end_1
    .catch Ljava/lang/Exception; {:try_start_1 .. :try_end_1} :catch_0
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    goto :goto_0

    .line 1563
    :cond_0
    :try_start_2
    iget-object v0, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mEventCallbackHandler:Lcom/broadcom/fm/fmreceiver/FmProxy$EventCallbackHandler;

    iget-object v0, v0, Lcom/broadcom/fm/fmreceiver/FmProxy$EventCallbackHandler;->mHandler:Landroid/os/Handler;
    :try_end_2
    .catchall {:try_start_2 .. :try_end_2} :catchall_0

    monitor-exit p0

    return-object v0

    :catchall_0
    move-exception v0

    monitor-exit p0

    throw v0
.end method

.method public declared-synchronized muteAudio(Z)I
    .locals 3

    monitor-enter p0

    const/4 v0, 0x2

    .line 983
    :try_start_0
    iget-object v1, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mService:Lcom/broadcom/fm/fmreceiver/IFmReceiverService;

    invoke-interface {v1, p1}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService;->muteAudio(Z)I

    move-result v0
    :try_end_0
    .catch Landroid/os/RemoteException; {:try_start_0 .. :try_end_0} :catch_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    goto :goto_0

    :catchall_0
    move-exception p1

    goto :goto_1

    :catch_0
    move-exception p1

    :try_start_1
    const-string v1, "FmProxy"

    const-string v2, "muteAudio() failed"

    .line 985
    invoke-static {v1, v2, p1}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    .line 988
    :goto_0
    monitor-exit p0

    return v0

    :goto_1
    monitor-exit p0

    throw p1
.end method

.method public declared-synchronized registerEventHandler(Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;)V
    .locals 3

    monitor-enter p0

    :try_start_0
    const-string v0, "FmProxy"

    const-string v1, "registerEventHandler()"

    .line 654
    invoke-static {v0, v1}, Landroid/util/Log;->v(Ljava/lang/String;Ljava/lang/String;)I

    const/4 v0, 0x0

    const/4 v1, 0x1

    const/16 v2, 0xc8

    .line 655
    invoke-virtual {p0, p1, v0, v1, v2}, Lcom/broadcom/fm/fmreceiver/FmProxy;->registerEventHandler(Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;Landroid/content/IntentFilter;ZI)V
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 656
    monitor-exit p0

    return-void

    :catchall_0
    move-exception p1

    monitor-exit p0

    throw p1
.end method

.method public declared-synchronized registerEventHandler(Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;Landroid/content/IntentFilter;Landroid/os/Handler;I)V
    .locals 0

    monitor-enter p0

    .line 674
    :try_start_0
    iput-object p1, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mEventHandler:Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;

    .line 693
    iget-object p1, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mCallback:Lcom/broadcom/fm/fmreceiver/IFmReceiverCallback;

    if-nez p1, :cond_0

    .line 694
    new-instance p1, Lcom/broadcom/fm/fmreceiver/FmProxy$FmReceiverCallback;

    const/4 p2, 0x0

    invoke-direct {p1, p0, p2}, Lcom/broadcom/fm/fmreceiver/FmProxy$FmReceiverCallback;-><init>(Lcom/broadcom/fm/fmreceiver/FmProxy;Lcom/broadcom/fm/fmreceiver/FmProxy$1;)V

    iput-object p1, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mCallback:Lcom/broadcom/fm/fmreceiver/IFmReceiverCallback;
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 696
    :try_start_1
    iget-object p1, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mService:Lcom/broadcom/fm/fmreceiver/IFmReceiverService;

    iget-object p2, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mCallback:Lcom/broadcom/fm/fmreceiver/IFmReceiverCallback;

    invoke-interface {p1, p2}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService;->registerCallback(Lcom/broadcom/fm/fmreceiver/IFmReceiverCallback;)V
    :try_end_1
    .catch Landroid/os/RemoteException; {:try_start_1 .. :try_end_1} :catch_0
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    goto :goto_0

    :catch_0
    move-exception p1

    :try_start_2
    const-string p2, "FmProxy"

    const-string p3, "Error registering callback handler"

    .line 698
    invoke-static {p2, p3, p1}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    :try_end_2
    .catchall {:try_start_2 .. :try_end_2} :catchall_0

    .line 702
    :cond_0
    :goto_0
    monitor-exit p0

    return-void

    :catchall_0
    move-exception p1

    monitor-exit p0

    throw p1
.end method

.method public declared-synchronized registerEventHandler(Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;Landroid/content/IntentFilter;ZI)V
    .locals 0

    monitor-enter p0

    const/4 p2, 0x0

    .line 666
    :try_start_0
    invoke-virtual {p0, p1, p2, p2, p4}, Lcom/broadcom/fm/fmreceiver/FmProxy;->registerEventHandler(Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;Landroid/content/IntentFilter;Landroid/os/Handler;I)V
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 668
    monitor-exit p0

    return-void

    :catchall_0
    move-exception p1

    monitor-exit p0

    throw p1
.end method

.method public requiresAccessProcessing()Z
    .locals 1

    const/4 v0, 0x0

    return v0
.end method

.method public seekRdsStation(III)I
    .locals 1

    const/16 v0, 0x69

    .line 1132
    invoke-virtual {p0, p1, v0, p2, p3}, Lcom/broadcom/fm/fmreceiver/FmProxy;->seekRdsStation(IIII)I

    move-result p1

    return p1
.end method

.method public declared-synchronized seekRdsStation(IIII)I
    .locals 2

    monitor-enter p0

    const/4 v0, 0x2

    .line 1104
    :try_start_0
    iget-object v1, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mService:Lcom/broadcom/fm/fmreceiver/IFmReceiverService;

    invoke-interface {v1, p1, p2, p3, p4}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService;->seekRdsStation(IIII)I

    move-result v0
    :try_end_0
    .catch Landroid/os/RemoteException; {:try_start_0 .. :try_end_0} :catch_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    goto :goto_0

    :catchall_0
    move-exception p1

    goto :goto_1

    :catch_0
    move-exception p1

    :try_start_1
    const-string p2, "FmProxy"

    const-string p3, "seekRdsStation() failed"

    .line 1107
    invoke-static {p2, p3, p1}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    .line 1110
    :goto_0
    monitor-exit p0

    return v0

    :goto_1
    monitor-exit p0

    throw p1
.end method

.method public seekStation(I)I
    .locals 1

    const/16 v0, 0x69

    .line 1033
    invoke-virtual {p0, p1, v0}, Lcom/broadcom/fm/fmreceiver/FmProxy;->seekStation(II)I

    move-result p1

    return p1
.end method

.method public declared-synchronized seekStation(II)I
    .locals 2

    monitor-enter p0

    const/4 v0, 0x2

    .line 1011
    :try_start_0
    iget-object v1, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mService:Lcom/broadcom/fm/fmreceiver/IFmReceiverService;

    invoke-interface {v1, p1, p2}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService;->seekStation(II)I

    move-result v0
    :try_end_0
    .catch Landroid/os/RemoteException; {:try_start_0 .. :try_end_0} :catch_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    goto :goto_0

    :catchall_0
    move-exception p1

    goto :goto_1

    :catch_0
    move-exception p1

    :try_start_1
    const-string p2, "FmProxy"

    const-string v1, "seekStation() failed"

    .line 1013
    invoke-static {p2, v1, p1}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    .line 1016
    :goto_0
    monitor-exit p0

    return v0

    :goto_1
    monitor-exit p0

    throw p1
.end method

.method public declared-synchronized seekStationAbort()I
    .locals 4

    monitor-enter p0

    const/4 v0, 0x2

    .line 1146
    :try_start_0
    iget-object v1, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mService:Lcom/broadcom/fm/fmreceiver/IFmReceiverService;

    invoke-interface {v1}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService;->seekStationAbort()I

    move-result v0
    :try_end_0
    .catch Landroid/os/RemoteException; {:try_start_0 .. :try_end_0} :catch_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    goto :goto_0

    :catchall_0
    move-exception v0

    goto :goto_1

    :catch_0
    move-exception v1

    :try_start_1
    const-string v2, "FmProxy"

    const-string v3, "seekStationAbort() failed"

    .line 1148
    invoke-static {v2, v3, v1}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    .line 1151
    :goto_0
    monitor-exit p0

    return v0

    :goto_1
    monitor-exit p0

    throw v0
.end method

.method public declared-synchronized seekStationCombo(IIIIIZII)I
    .locals 12

    move-object v1, p0

    monitor-enter p0

    const/4 v2, 0x2

    .line 1072
    :try_start_0
    iget-object v3, v1, Lcom/broadcom/fm/fmreceiver/FmProxy;->mService:Lcom/broadcom/fm/fmreceiver/IFmReceiverService;

    move v4, p1

    move v5, p2

    move v6, p3

    move/from16 v7, p4

    move/from16 v8, p5

    move/from16 v9, p6

    move/from16 v10, p7

    move/from16 v11, p8

    invoke-interface/range {v3 .. v11}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService;->seekStationCombo(IIIIIZII)I

    move-result v2
    :try_end_0
    .catch Landroid/os/RemoteException; {:try_start_0 .. :try_end_0} :catch_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    goto :goto_0

    :catchall_0
    move-exception v0

    goto :goto_1

    :catch_0
    move-exception v0

    :try_start_1
    const-string v3, "FmProxy"

    const-string v4, "seekStation() failed"

    .line 1074
    invoke-static {v3, v4, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    .line 1077
    :goto_0
    monitor-exit p0

    return v2

    :goto_1
    monitor-exit p0

    throw v0
.end method

.method public declared-synchronized setAudioMode(I)I
    .locals 3

    monitor-enter p0

    const/4 v0, 0x2

    .line 1201
    :try_start_0
    iget-object v1, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mService:Lcom/broadcom/fm/fmreceiver/IFmReceiverService;

    invoke-interface {v1, p1}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService;->setAudioMode(I)I

    move-result v0
    :try_end_0
    .catch Landroid/os/RemoteException; {:try_start_0 .. :try_end_0} :catch_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    goto :goto_0

    :catchall_0
    move-exception p1

    goto :goto_1

    :catch_0
    move-exception p1

    :try_start_1
    const-string v1, "FmProxy"

    const-string v2, "setAudioMode() failed"

    .line 1203
    invoke-static {v1, v2, p1}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    .line 1206
    :goto_0
    monitor-exit p0

    return v0

    :goto_1
    monitor-exit p0

    throw p1
.end method

.method public declared-synchronized setAudioPath(I)I
    .locals 3

    monitor-enter p0

    const/4 v0, 0x2

    .line 1228
    :try_start_0
    iget-object v1, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mService:Lcom/broadcom/fm/fmreceiver/IFmReceiverService;

    invoke-interface {v1, p1}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService;->setAudioPath(I)I

    move-result v0
    :try_end_0
    .catch Landroid/os/RemoteException; {:try_start_0 .. :try_end_0} :catch_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    goto :goto_0

    :catchall_0
    move-exception p1

    goto :goto_1

    :catch_0
    move-exception p1

    :try_start_1
    const-string v1, "FmProxy"

    const-string v2, "setAudioPath() failed"

    .line 1230
    invoke-static {v1, v2, p1}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    .line 1233
    :goto_0
    monitor-exit p0

    return v0

    :goto_1
    monitor-exit p0

    throw p1
.end method

.method public declared-synchronized setFMVolume(I)I
    .locals 3

    monitor-enter p0

    const/4 v0, 0x2

    .line 1270
    :try_start_0
    iget-object v1, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mService:Lcom/broadcom/fm/fmreceiver/IFmReceiverService;

    invoke-interface {v1, p1}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService;->setFMVolume(I)I

    move-result v0
    :try_end_0
    .catch Landroid/os/RemoteException; {:try_start_0 .. :try_end_0} :catch_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    goto :goto_0

    :catchall_0
    move-exception p1

    goto :goto_1

    :catch_0
    move-exception p1

    :try_start_1
    const-string v1, "FmProxy"

    const-string v2, "setFMVolume() failed"

    .line 1272
    invoke-static {v1, v2, p1}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    .line 1275
    :goto_0
    monitor-exit p0

    return v0

    :goto_1
    monitor-exit p0

    throw p1
.end method

.method public declared-synchronized setLiveAudioPolling(ZI)I
    .locals 2

    monitor-enter p0

    const/4 v0, 0x2

    .line 1344
    :try_start_0
    iget-object v1, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mService:Lcom/broadcom/fm/fmreceiver/IFmReceiverService;

    invoke-interface {v1, p1, p2}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService;->setLiveAudioPolling(ZI)I

    move-result v0
    :try_end_0
    .catch Landroid/os/RemoteException; {:try_start_0 .. :try_end_0} :catch_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    goto :goto_0

    :catchall_0
    move-exception p1

    goto :goto_1

    :catch_0
    move-exception p1

    :try_start_1
    const-string p2, "FmProxy"

    const-string v1, "setLiveAudioPolling() failed"

    .line 1346
    invoke-static {p2, v1, p1}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    .line 1349
    :goto_0
    monitor-exit p0

    return v0

    :goto_1
    monitor-exit p0

    throw p1
.end method

.method public declared-synchronized setRdsMode(IIII)I
    .locals 2

    monitor-enter p0

    const/4 v0, 0x2

    .line 1177
    :try_start_0
    iget-object v1, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mService:Lcom/broadcom/fm/fmreceiver/IFmReceiverService;

    invoke-interface {v1, p1, p2, p3, p4}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService;->setRdsMode(IIII)I

    move-result v0
    :try_end_0
    .catch Landroid/os/RemoteException; {:try_start_0 .. :try_end_0} :catch_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    goto :goto_0

    :catchall_0
    move-exception p1

    goto :goto_1

    :catch_0
    move-exception p1

    :try_start_1
    const-string p2, "FmProxy"

    const-string p3, "setRdsMode() failed"

    .line 1179
    invoke-static {p2, p3, p1}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    .line 1182
    :goto_0
    monitor-exit p0

    return v0

    :goto_1
    monitor-exit p0

    throw p1
.end method

.method public declared-synchronized setSnrThreshold(I)I
    .locals 3

    monitor-enter p0

    const/4 v0, 0x2

    .line 1366
    :try_start_0
    iget-object v1, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mService:Lcom/broadcom/fm/fmreceiver/IFmReceiverService;

    invoke-interface {v1, p1}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService;->setSnrThreshold(I)I

    move-result v0
    :try_end_0
    .catch Landroid/os/RemoteException; {:try_start_0 .. :try_end_0} :catch_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    goto :goto_0

    :catchall_0
    move-exception p1

    goto :goto_1

    :catch_0
    move-exception p1

    :try_start_1
    const-string v1, "FmProxy"

    const-string v2, "setSnrThreshold() failed"

    .line 1368
    invoke-static {v1, v2, p1}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    .line 1370
    :goto_0
    monitor-exit p0

    return v0

    :goto_1
    monitor-exit p0

    throw p1
.end method

.method public declared-synchronized setStepSize(I)I
    .locals 3

    monitor-enter p0

    const/4 v0, 0x2

    .line 1250
    :try_start_0
    iget-object v1, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mService:Lcom/broadcom/fm/fmreceiver/IFmReceiverService;

    invoke-interface {v1, p1}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService;->setStepSize(I)I

    move-result v0
    :try_end_0
    .catch Landroid/os/RemoteException; {:try_start_0 .. :try_end_0} :catch_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    goto :goto_0

    :catchall_0
    move-exception p1

    goto :goto_1

    :catch_0
    move-exception p1

    :try_start_1
    const-string v1, "FmProxy"

    const-string v2, "setStepSize() failed"

    .line 1252
    invoke-static {v1, v2, p1}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    .line 1255
    :goto_0
    monitor-exit p0

    return v0

    :goto_1
    monitor-exit p0

    throw p1
.end method

.method public declared-synchronized setWorldRegion(II)I
    .locals 2

    monitor-enter p0

    const/4 v0, 0x2

    .line 1296
    :try_start_0
    iget-object v1, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mService:Lcom/broadcom/fm/fmreceiver/IFmReceiverService;

    invoke-interface {v1, p1, p2}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService;->setWorldRegion(II)I

    move-result v0
    :try_end_0
    .catch Landroid/os/RemoteException; {:try_start_0 .. :try_end_0} :catch_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    goto :goto_0

    :catchall_0
    move-exception p1

    goto :goto_1

    :catch_0
    move-exception p1

    :try_start_1
    const-string p2, "FmProxy"

    const-string v1, "setWorldRegion() failed"

    .line 1298
    invoke-static {p2, v1, p1}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    .line 1301
    :goto_0
    monitor-exit p0

    return v0

    :goto_1
    monitor-exit p0

    throw p1
.end method

.method public declared-synchronized tuneRadio(I)I
    .locals 3

    monitor-enter p0

    const/4 v0, 0x2

    .line 878
    :try_start_0
    iget-object v1, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mService:Lcom/broadcom/fm/fmreceiver/IFmReceiverService;

    invoke-interface {v1, p1}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService;->tuneRadio(I)I

    move-result v0
    :try_end_0
    .catch Landroid/os/RemoteException; {:try_start_0 .. :try_end_0} :catch_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    goto :goto_0

    :catchall_0
    move-exception p1

    goto :goto_1

    :catch_0
    move-exception p1

    :try_start_1
    const-string v1, "FmProxy"

    const-string v2, "tuneRadio() failed"

    .line 880
    invoke-static {v1, v2, p1}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    .line 883
    :goto_0
    monitor-exit p0

    return v0

    :goto_1
    monitor-exit p0

    throw p1
.end method

.method public declared-synchronized turnOffRadio()I
    .locals 4

    monitor-enter p0

    const/4 v0, 0x2

    .line 839
    :try_start_0
    iget-object v1, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mService:Lcom/broadcom/fm/fmreceiver/IFmReceiverService;

    invoke-interface {v1}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService;->turnOffRadio()I

    move-result v0
    :try_end_0
    .catch Landroid/os/RemoteException; {:try_start_0 .. :try_end_0} :catch_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 845
    monitor-exit p0

    return v0

    :catchall_0
    move-exception v0

    goto :goto_0

    :catch_0
    move-exception v1

    :try_start_1
    const-string v2, "FmProxy"

    const-string v3, "turnOffRadio() failed"

    .line 841
    invoke-static {v2, v3, v1}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    .line 842
    monitor-exit p0

    return v0

    :goto_0
    monitor-exit p0

    throw v0
.end method

.method public declared-synchronized turnOnRadio(ILjava/lang/String;)I
    .locals 4

    monitor-enter p0

    const/4 v0, 0x2

    :try_start_0
    const-string v1, "FmProxy"

    .line 795
    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2}, Ljava/lang/StringBuilder;-><init>()V

    const-string v3, "Fmproxy"

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2, p0}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    const-string v3, "mService"

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-object v3, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mService:Lcom/broadcom/fm/fmreceiver/IFmReceiverService;

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v2

    invoke-static {v1, v2}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 798
    :try_start_1
    iget-object v1, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mService:Lcom/broadcom/fm/fmreceiver/IFmReceiverService;

    invoke-virtual {p2}, Ljava/lang/String;->toCharArray()[C

    move-result-object p2

    invoke-interface {v1, p1, p2}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService;->turnOnRadio(I[C)I

    move-result v0
    :try_end_1
    .catch Landroid/os/RemoteException; {:try_start_1 .. :try_end_1} :catch_0
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    goto :goto_0

    :catch_0
    move-exception p1

    :try_start_2
    const-string p2, "FmProxy"

    const-string v1, "turnOnRadio() failed"

    .line 800
    invoke-static {p2, v1, p1}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    :try_end_2
    .catchall {:try_start_2 .. :try_end_2} :catchall_0

    .line 803
    :goto_0
    monitor-exit p0

    return v0

    :catchall_0
    move-exception p1

    monitor-exit p0

    throw p1
.end method

.method public turnOnRadio(Ljava/lang/String;)I
    .locals 1

    const/4 v0, 0x0

    .line 823
    invoke-virtual {p0, v0, p1}, Lcom/broadcom/fm/fmreceiver/FmProxy;->turnOnRadio(ILjava/lang/String;)I

    move-result p1

    return p1
.end method

.method public declared-synchronized unregisterEventHandler()V
    .locals 3

    monitor-enter p0

    :try_start_0
    const-string v0, "FmProxy"

    const-string v1, "unregisterEventHandler()"

    .line 727
    invoke-static {v0, v1}, Landroid/util/Log;->v(Ljava/lang/String;Ljava/lang/String;)I

    const/4 v0, 0x0

    .line 729
    iput-object v0, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mEventHandler:Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_1

    .line 738
    :try_start_1
    iget-object v0, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mService:Lcom/broadcom/fm/fmreceiver/IFmReceiverService;

    iget-object v1, p0, Lcom/broadcom/fm/fmreceiver/FmProxy;->mCallback:Lcom/broadcom/fm/fmreceiver/IFmReceiverCallback;

    invoke-interface {v0, v1}, Lcom/broadcom/fm/fmreceiver/IFmReceiverService;->unregisterCallback(Lcom/broadcom/fm/fmreceiver/IFmReceiverCallback;)V
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    goto :goto_0

    :catchall_0
    move-exception v0

    :try_start_2
    const-string v1, "FmProxy"

    const-string v2, "Unable to unregister callback"

    .line 740
    invoke-static {v1, v2, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I
    :try_end_2
    .catchall {:try_start_2 .. :try_end_2} :catchall_1

    .line 743
    :goto_0
    monitor-exit p0

    return-void

    :catchall_1
    move-exception v0

    monitor-exit p0

    throw v0
.end method
