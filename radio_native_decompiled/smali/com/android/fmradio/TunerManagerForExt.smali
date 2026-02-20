.class public Lcom/android/fmradio/TunerManagerForExt;
.super Ljava/lang/Object;
.source "TunerManagerForExt.java"


# annotations
.annotation system Ldalvik/annotation/MemberClasses;
    value = {
        Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;
    }
.end annotation


# static fields
.field private static final TAG:Ljava/lang/String;

.field private static instance:Lcom/android/fmradio/TunerManagerForExt;

.field private static final mLock:Ljava/lang/Object;


# instance fields
.field private mBand:I

.field private mContext:Landroid/content/Context;

.field private mFmMainActivity:Lcom/android/fmradio/FmMainActivity;

.field private mFrequency:I

.field private mHandler:Landroid/os/Handler;

.field private mIsSearching:Z

.field private mLocFlag:I

.field private mPresetIndex:I

.field private mPresetList:[I

.field private mRdsAFSwitch:I

.field private mRdsPsInfo:Ljava/lang/String;

.field private mRdsPsPresetList:[Ljava/lang/String;

.field private mRdsPtyType:I

.field private mRdsRTInfo:Ljava/lang/String;

.field private mRdsSwitch:I

.field private mRdsTASwitch:I

.field private mStFlag:I

.field private mTempSearching:Z

.field private tunerObserver:Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;


# direct methods
.method static constructor <clinit>()V
    .locals 1

    .line 21
    const-class v0, Lcom/android/fmradio/TunerManagerForExt;

    invoke-virtual {v0}, Ljava/lang/Class;->getSimpleName()Ljava/lang/String;

    move-result-object v0

    sput-object v0, Lcom/android/fmradio/TunerManagerForExt;->TAG:Ljava/lang/String;

    .line 65
    new-instance v0, Ljava/lang/Object;

    invoke-direct {v0}, Ljava/lang/Object;-><init>()V

    sput-object v0, Lcom/android/fmradio/TunerManagerForExt;->mLock:Ljava/lang/Object;

    return-void
.end method

.method private constructor <init>()V
    .locals 3

    .line 309
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    const/4 v0, 0x0

    .line 30
    iput v0, p0, Lcom/android/fmradio/TunerManagerForExt;->mBand:I

    .line 33
    iput v0, p0, Lcom/android/fmradio/TunerManagerForExt;->mPresetIndex:I

    .line 36
    iput v0, p0, Lcom/android/fmradio/TunerManagerForExt;->mFrequency:I

    const/4 v1, 0x6

    new-array v2, v1, [I

    .line 38
    iput-object v2, p0, Lcom/android/fmradio/TunerManagerForExt;->mPresetList:[I

    .line 45
    iput v0, p0, Lcom/android/fmradio/TunerManagerForExt;->mLocFlag:I

    .line 48
    iput v0, p0, Lcom/android/fmradio/TunerManagerForExt;->mStFlag:I

    .line 50
    iput v0, p0, Lcom/android/fmradio/TunerManagerForExt;->mRdsPtyType:I

    .line 52
    iput v0, p0, Lcom/android/fmradio/TunerManagerForExt;->mRdsTASwitch:I

    .line 54
    iput v0, p0, Lcom/android/fmradio/TunerManagerForExt;->mRdsAFSwitch:I

    .line 56
    iput v0, p0, Lcom/android/fmradio/TunerManagerForExt;->mRdsSwitch:I

    const-string v0, ""

    .line 58
    iput-object v0, p0, Lcom/android/fmradio/TunerManagerForExt;->mRdsPsInfo:Ljava/lang/String;

    .line 59
    iput-object v0, p0, Lcom/android/fmradio/TunerManagerForExt;->mRdsRTInfo:Ljava/lang/String;

    new-array v0, v1, [Ljava/lang/String;

    .line 61
    iput-object v0, p0, Lcom/android/fmradio/TunerManagerForExt;->mRdsPsPresetList:[Ljava/lang/String;

    .line 67
    new-instance v0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;

    const/4 v1, 0x0

    invoke-direct {v0, p0, v1}, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;-><init>(Lcom/android/fmradio/TunerManagerForExt;Lcom/android/fmradio/TunerManagerForExt$1;)V

    iput-object v0, p0, Lcom/android/fmradio/TunerManagerForExt;->tunerObserver:Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;

    return-void
.end method

.method static synthetic access$100(Lcom/android/fmradio/TunerManagerForExt;)Landroid/os/Handler;
    .locals 0

    .line 19
    iget-object p0, p0, Lcom/android/fmradio/TunerManagerForExt;->mHandler:Landroid/os/Handler;

    return-object p0
.end method

.method static synthetic access$1000(Lcom/android/fmradio/TunerManagerForExt;)I
    .locals 0

    .line 19
    iget p0, p0, Lcom/android/fmradio/TunerManagerForExt;->mFrequency:I

    return p0
.end method

.method static synthetic access$1002(Lcom/android/fmradio/TunerManagerForExt;I)I
    .locals 0

    .line 19
    iput p1, p0, Lcom/android/fmradio/TunerManagerForExt;->mFrequency:I

    return p1
.end method

.method static synthetic access$1100(Lcom/android/fmradio/TunerManagerForExt;)[I
    .locals 0

    .line 19
    iget-object p0, p0, Lcom/android/fmradio/TunerManagerForExt;->mPresetList:[I

    return-object p0
.end method

.method static synthetic access$1200(Lcom/android/fmradio/TunerManagerForExt;)I
    .locals 0

    .line 19
    iget p0, p0, Lcom/android/fmradio/TunerManagerForExt;->mRdsAFSwitch:I

    return p0
.end method

.method static synthetic access$1202(Lcom/android/fmradio/TunerManagerForExt;I)I
    .locals 0

    .line 19
    iput p1, p0, Lcom/android/fmradio/TunerManagerForExt;->mRdsAFSwitch:I

    return p1
.end method

.method static synthetic access$1300(Lcom/android/fmradio/TunerManagerForExt;)I
    .locals 0

    .line 19
    iget p0, p0, Lcom/android/fmradio/TunerManagerForExt;->mRdsTASwitch:I

    return p0
.end method

.method static synthetic access$1302(Lcom/android/fmradio/TunerManagerForExt;I)I
    .locals 0

    .line 19
    iput p1, p0, Lcom/android/fmradio/TunerManagerForExt;->mRdsTASwitch:I

    return p1
.end method

.method static synthetic access$1400(Lcom/android/fmradio/TunerManagerForExt;)I
    .locals 0

    .line 19
    iget p0, p0, Lcom/android/fmradio/TunerManagerForExt;->mRdsSwitch:I

    return p0
.end method

.method static synthetic access$1402(Lcom/android/fmradio/TunerManagerForExt;I)I
    .locals 0

    .line 19
    iput p1, p0, Lcom/android/fmradio/TunerManagerForExt;->mRdsSwitch:I

    return p1
.end method

.method static synthetic access$1500(Lcom/android/fmradio/TunerManagerForExt;)Ljava/lang/String;
    .locals 0

    .line 19
    iget-object p0, p0, Lcom/android/fmradio/TunerManagerForExt;->mRdsPsInfo:Ljava/lang/String;

    return-object p0
.end method

.method static synthetic access$1502(Lcom/android/fmradio/TunerManagerForExt;Ljava/lang/String;)Ljava/lang/String;
    .locals 0

    .line 19
    iput-object p1, p0, Lcom/android/fmradio/TunerManagerForExt;->mRdsPsInfo:Ljava/lang/String;

    return-object p1
.end method

.method static synthetic access$1600(Lcom/android/fmradio/TunerManagerForExt;)Ljava/lang/String;
    .locals 0

    .line 19
    iget-object p0, p0, Lcom/android/fmradio/TunerManagerForExt;->mRdsRTInfo:Ljava/lang/String;

    return-object p0
.end method

.method static synthetic access$1602(Lcom/android/fmradio/TunerManagerForExt;Ljava/lang/String;)Ljava/lang/String;
    .locals 0

    .line 19
    iput-object p1, p0, Lcom/android/fmradio/TunerManagerForExt;->mRdsRTInfo:Ljava/lang/String;

    return-object p1
.end method

.method static synthetic access$1700(Lcom/android/fmradio/TunerManagerForExt;)I
    .locals 0

    .line 19
    iget p0, p0, Lcom/android/fmradio/TunerManagerForExt;->mRdsPtyType:I

    return p0
.end method

.method static synthetic access$1702(Lcom/android/fmradio/TunerManagerForExt;I)I
    .locals 0

    .line 19
    iput p1, p0, Lcom/android/fmradio/TunerManagerForExt;->mRdsPtyType:I

    return p1
.end method

.method static synthetic access$1800(Lcom/android/fmradio/TunerManagerForExt;)[Ljava/lang/String;
    .locals 0

    .line 19
    iget-object p0, p0, Lcom/android/fmradio/TunerManagerForExt;->mRdsPsPresetList:[Ljava/lang/String;

    return-object p0
.end method

.method static synthetic access$200(Lcom/android/fmradio/TunerManagerForExt;)I
    .locals 0

    .line 19
    iget p0, p0, Lcom/android/fmradio/TunerManagerForExt;->mStFlag:I

    return p0
.end method

.method static synthetic access$202(Lcom/android/fmradio/TunerManagerForExt;I)I
    .locals 0

    .line 19
    iput p1, p0, Lcom/android/fmradio/TunerManagerForExt;->mStFlag:I

    return p1
.end method

.method static synthetic access$300(Lcom/android/fmradio/TunerManagerForExt;)I
    .locals 0

    .line 19
    iget p0, p0, Lcom/android/fmradio/TunerManagerForExt;->mLocFlag:I

    return p0
.end method

.method static synthetic access$302(Lcom/android/fmradio/TunerManagerForExt;I)I
    .locals 0

    .line 19
    iput p1, p0, Lcom/android/fmradio/TunerManagerForExt;->mLocFlag:I

    return p1
.end method

.method static synthetic access$400(Lcom/android/fmradio/TunerManagerForExt;)Z
    .locals 0

    .line 19
    iget-boolean p0, p0, Lcom/android/fmradio/TunerManagerForExt;->mIsSearching:Z

    return p0
.end method

.method static synthetic access$402(Lcom/android/fmradio/TunerManagerForExt;Z)Z
    .locals 0

    .line 19
    iput-boolean p1, p0, Lcom/android/fmradio/TunerManagerForExt;->mIsSearching:Z

    return p1
.end method

.method static synthetic access$500(Lcom/android/fmradio/TunerManagerForExt;)Z
    .locals 0

    .line 19
    iget-boolean p0, p0, Lcom/android/fmradio/TunerManagerForExt;->mTempSearching:Z

    return p0
.end method

.method static synthetic access$502(Lcom/android/fmradio/TunerManagerForExt;Z)Z
    .locals 0

    .line 19
    iput-boolean p1, p0, Lcom/android/fmradio/TunerManagerForExt;->mTempSearching:Z

    return p1
.end method

.method static synthetic access$600()Ljava/lang/String;
    .locals 1

    .line 19
    sget-object v0, Lcom/android/fmradio/TunerManagerForExt;->TAG:Ljava/lang/String;

    return-object v0
.end method

.method static synthetic access$700(Lcom/android/fmradio/TunerManagerForExt;)I
    .locals 0

    .line 19
    iget p0, p0, Lcom/android/fmradio/TunerManagerForExt;->mBand:I

    return p0
.end method

.method static synthetic access$702(Lcom/android/fmradio/TunerManagerForExt;I)I
    .locals 0

    .line 19
    iput p1, p0, Lcom/android/fmradio/TunerManagerForExt;->mBand:I

    return p1
.end method

.method static synthetic access$800(Lcom/android/fmradio/TunerManagerForExt;)Landroid/content/Context;
    .locals 0

    .line 19
    iget-object p0, p0, Lcom/android/fmradio/TunerManagerForExt;->mContext:Landroid/content/Context;

    return-object p0
.end method

.method static synthetic access$900(Lcom/android/fmradio/TunerManagerForExt;)I
    .locals 0

    .line 19
    iget p0, p0, Lcom/android/fmradio/TunerManagerForExt;->mPresetIndex:I

    return p0
.end method

.method static synthetic access$902(Lcom/android/fmradio/TunerManagerForExt;I)I
    .locals 0

    .line 19
    iput p1, p0, Lcom/android/fmradio/TunerManagerForExt;->mPresetIndex:I

    return p1
.end method

.method public static getInstance()Lcom/android/fmradio/TunerManagerForExt;
    .locals 2

    .line 314
    sget-object v0, Lcom/android/fmradio/TunerManagerForExt;->instance:Lcom/android/fmradio/TunerManagerForExt;

    if-nez v0, :cond_1

    .line 315
    sget-object v0, Lcom/android/fmradio/TunerManagerForExt;->mLock:Ljava/lang/Object;

    monitor-enter v0

    .line 316
    :try_start_0
    sget-object v1, Lcom/android/fmradio/TunerManagerForExt;->instance:Lcom/android/fmradio/TunerManagerForExt;

    if-nez v1, :cond_0

    .line 317
    new-instance v1, Lcom/android/fmradio/TunerManagerForExt;

    invoke-direct {v1}, Lcom/android/fmradio/TunerManagerForExt;-><init>()V

    sput-object v1, Lcom/android/fmradio/TunerManagerForExt;->instance:Lcom/android/fmradio/TunerManagerForExt;

    .line 319
    :cond_0
    monitor-exit v0

    goto :goto_0

    :catchall_0
    move-exception v1

    monitor-exit v0
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    throw v1

    .line 321
    :cond_1
    :goto_0
    sget-object v0, Lcom/android/fmradio/TunerManagerForExt;->instance:Lcom/android/fmradio/TunerManagerForExt;

    return-object v0
.end method


# virtual methods
.method public autoScan()[I
    .locals 3

    .line 436
    sget-object v0, Lcom/android/fmradio/TunerManagerForExt;->TAG:Ljava/lang/String;

    const-string v1, "start"

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 437
    invoke-static {}, Lcom/qf/clientsdk/QFTunerManager;->getInstance()Lcom/qf/clientsdk/QFTunerManager;

    move-result-object v0

    invoke-virtual {v0}, Lcom/qf/clientsdk/QFTunerManager;->autoScan()V

    const/4 v0, 0x1

    new-array v0, v0, [I

    const/4 v1, 0x0

    const/16 v2, -0x64

    aput v2, v0, v1

    return-object v0
.end method

.method public getBand()I
    .locals 1

    .line 563
    iget v0, p0, Lcom/android/fmradio/TunerManagerForExt;->mBand:I

    return v0
.end method

.method public getContext()Landroid/content/Context;
    .locals 1

    .line 325
    iget-object v0, p0, Lcom/android/fmradio/TunerManagerForExt;->mContext:Landroid/content/Context;

    return-object v0
.end method

.method public getFrequency()I
    .locals 1

    .line 571
    iget v0, p0, Lcom/android/fmradio/TunerManagerForExt;->mFrequency:I

    return v0
.end method

.method public getLocFlag()I
    .locals 1

    .line 583
    iget v0, p0, Lcom/android/fmradio/TunerManagerForExt;->mLocFlag:I

    return v0
.end method

.method public getPresetIndex()I
    .locals 1

    .line 567
    iget v0, p0, Lcom/android/fmradio/TunerManagerForExt;->mPresetIndex:I

    return v0
.end method

.method public getPresetList()[I
    .locals 1

    .line 575
    iget-object v0, p0, Lcom/android/fmradio/TunerManagerForExt;->mPresetList:[I

    return-object v0
.end method

.method public getRdsAFSwitch()I
    .locals 1

    .line 543
    iget v0, p0, Lcom/android/fmradio/TunerManagerForExt;->mRdsAFSwitch:I

    return v0
.end method

.method public getRdsPsInfo()Ljava/lang/String;
    .locals 1

    .line 555
    iget-object v0, p0, Lcom/android/fmradio/TunerManagerForExt;->mRdsPsInfo:Ljava/lang/String;

    return-object v0
.end method

.method public getRdsPsPresetList()[Ljava/lang/String;
    .locals 1

    .line 579
    iget-object v0, p0, Lcom/android/fmradio/TunerManagerForExt;->mRdsPsPresetList:[Ljava/lang/String;

    return-object v0
.end method

.method public getRdsPtyType()I
    .locals 1

    .line 525
    iget v0, p0, Lcom/android/fmradio/TunerManagerForExt;->mRdsPtyType:I

    return v0
.end method

.method public getRdsRTInfo()Ljava/lang/String;
    .locals 1

    .line 559
    iget-object v0, p0, Lcom/android/fmradio/TunerManagerForExt;->mRdsRTInfo:Ljava/lang/String;

    return-object v0
.end method

.method public getRdsSwitch()I
    .locals 1

    .line 551
    iget v0, p0, Lcom/android/fmradio/TunerManagerForExt;->mRdsSwitch:I

    return v0
.end method

.method public getRdsTASwitch()I
    .locals 1

    .line 535
    iget v0, p0, Lcom/android/fmradio/TunerManagerForExt;->mRdsTASwitch:I

    return v0
.end method

.method public getStFlag()I
    .locals 1

    .line 587
    iget v0, p0, Lcom/android/fmradio/TunerManagerForExt;->mStFlag:I

    return v0
.end method

.method public init(Lcom/android/fmradio/FmMainActivity;)V
    .locals 4

    .line 333
    iput-object p1, p0, Lcom/android/fmradio/TunerManagerForExt;->mFmMainActivity:Lcom/android/fmradio/FmMainActivity;

    .line 334
    iget-object v0, p0, Lcom/android/fmradio/TunerManagerForExt;->mFmMainActivity:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {v0}, Lcom/android/fmradio/FmMainActivity;->getHandler()Landroid/os/Handler;

    move-result-object v0

    iput-object v0, p0, Lcom/android/fmradio/TunerManagerForExt;->mHandler:Landroid/os/Handler;

    .line 337
    invoke-static {}, Lcom/qf/clientsdk/QFCoreManager;->getInstance()Lcom/qf/clientsdk/QFCoreManager;

    move-result-object v0

    new-instance v1, Lcom/qf/clientsdk/ClientSdkParam;

    .line 338
    invoke-virtual {p1}, Lcom/android/fmradio/FmMainActivity;->getApplicationContext()Landroid/content/Context;

    move-result-object p1

    sget-object v2, Lcom/qf/clientsdk/ClientSdkParam$SDKAPI;->DEFALUT:Lcom/qf/clientsdk/ClientSdkParam$SDKAPI;

    const-string v3, "ext_radio"

    invoke-direct {v1, p1, v3, v2}, Lcom/qf/clientsdk/ClientSdkParam;-><init>(Landroid/content/Context;Ljava/lang/String;Lcom/qf/clientsdk/ClientSdkParam$SDKAPI;)V

    .line 337
    invoke-virtual {v0, v1}, Lcom/qf/clientsdk/QFCoreManager;->initCoreManager(Lcom/qf/clientsdk/ClientSdkParam;)V

    .line 340
    invoke-static {}, Lcom/qf/clientsdk/QFTunerManager;->getInstance()Lcom/qf/clientsdk/QFTunerManager;

    move-result-object p1

    iget-object v0, p0, Lcom/android/fmradio/TunerManagerForExt;->tunerObserver:Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;

    invoke-virtual {p1, v0}, Lcom/qf/clientsdk/QFTunerManager;->setTunerTool(Lcom/qf/clientsdk/listeners/ITunerTool;)V

    return-void
.end method

.method public isRdsSupport()I
    .locals 1

    const/4 v0, 0x0

    return v0
.end method

.method public isScanning()Z
    .locals 1

    .line 358
    iget-boolean v0, p0, Lcom/android/fmradio/TunerManagerForExt;->mIsSearching:Z

    return v0
.end method

.method public onBand(B)V
    .locals 3

    .line 408
    sget-object v0, Lcom/android/fmradio/TunerManagerForExt;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "band: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 409
    invoke-static {}, Lcom/qf/clientsdk/QFTunerManager;->getInstance()Lcom/qf/clientsdk/QFTunerManager;

    move-result-object v0

    invoke-virtual {v0, p1}, Lcom/qf/clientsdk/QFTunerManager;->onBand(B)V

    return-void
.end method

.method public onFine(Z)V
    .locals 3

    .line 457
    sget-object v0, Lcom/android/fmradio/TunerManagerForExt;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "isDownSeek: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 458
    invoke-static {}, Lcom/qf/clientsdk/QFTunerManager;->getInstance()Lcom/qf/clientsdk/QFTunerManager;

    move-result-object v0

    invoke-virtual {v0, p1}, Lcom/qf/clientsdk/QFTunerManager;->onFine(Z)V

    return-void
.end method

.method public onLoc(I)V
    .locals 1

    .line 480
    invoke-static {}, Lcom/qf/clientsdk/QFTunerManager;->getInstance()Lcom/qf/clientsdk/QFTunerManager;

    move-result-object v0

    int-to-byte p1, p1

    invoke-virtual {v0, p1}, Lcom/qf/clientsdk/QFTunerManager;->onLoc(B)V

    return-void
.end method

.method public onNext()V
    .locals 2

    .line 495
    sget-object v0, Lcom/android/fmradio/TunerManagerForExt;->TAG:Ljava/lang/String;

    const-string v1, "start"

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 496
    invoke-static {}, Lcom/qf/clientsdk/QFTunerManager;->getInstance()Lcom/qf/clientsdk/QFTunerManager;

    move-result-object v0

    invoke-virtual {v0}, Lcom/qf/clientsdk/QFTunerManager;->onNext()V

    return-void
.end method

.method public onPre()V
    .locals 2

    .line 487
    sget-object v0, Lcom/android/fmradio/TunerManagerForExt;->TAG:Ljava/lang/String;

    const-string v1, "start"

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 488
    invoke-static {}, Lcom/qf/clientsdk/QFTunerManager;->getInstance()Lcom/qf/clientsdk/QFTunerManager;

    move-result-object v0

    invoke-virtual {v0}, Lcom/qf/clientsdk/QFTunerManager;->onPre()V

    return-void
.end method

.method public onPresetSave(B)V
    .locals 1

    .line 427
    invoke-static {}, Lcom/qf/clientsdk/QFTunerManager;->getInstance()Lcom/qf/clientsdk/QFTunerManager;

    move-result-object v0

    invoke-virtual {v0, p1}, Lcom/qf/clientsdk/QFTunerManager;->onPresetSave(B)V

    return-void
.end method

.method public onPresetSelect(B)V
    .locals 1

    .line 418
    invoke-static {}, Lcom/qf/clientsdk/QFTunerManager;->getInstance()Lcom/qf/clientsdk/QFTunerManager;

    move-result-object v0

    invoke-virtual {v0, p1}, Lcom/qf/clientsdk/QFTunerManager;->onPresetSelect(B)V

    return-void
.end method

.method public onRadioArea(I)V
    .locals 3

    .line 367
    sget-object v0, Lcom/android/fmradio/TunerManagerForExt;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "radioArea: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 368
    invoke-static {}, Lcom/qf/clientsdk/QFTunerManager;->getInstance()Lcom/qf/clientsdk/QFTunerManager;

    move-result-object v0

    int-to-byte p1, p1

    invoke-virtual {v0, p1}, Lcom/qf/clientsdk/QFTunerManager;->onRadioArea(B)V

    return-void
.end method

.method public onSeek(Z)F
    .locals 3

    .line 468
    sget-object v0, Lcom/android/fmradio/TunerManagerForExt;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "isDownSeek: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 469
    invoke-static {}, Lcom/qf/clientsdk/QFTunerManager;->getInstance()Lcom/qf/clientsdk/QFTunerManager;

    move-result-object v0

    invoke-virtual {v0, p1}, Lcom/qf/clientsdk/QFTunerManager;->onSeek(Z)V

    const/4 p1, 0x0

    return p1
.end method

.method public onTune(F)Z
    .locals 3

    .line 379
    iget-object v0, p0, Lcom/android/fmradio/TunerManagerForExt;->mContext:Landroid/content/Context;

    invoke-static {v0}, Lcom/android/fmradio/database/FmStation;->getCurrentBand(Landroid/content/Context;)I

    move-result v0

    invoke-static {v0}, Lcom/android/fmradio/utils/FmUtils;->isFMBand(I)Z

    move-result v0

    if-eqz v0, :cond_0

    const/high16 v0, 0x42c80000    # 100.0f

    mul-float/2addr p1, v0

    :cond_0
    float-to-int p1, p1

    .line 385
    sget-object v0, Lcom/android/fmradio/TunerManagerForExt;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "mBand: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget v2, p0, Lcom/android/fmradio/TunerManagerForExt;->mBand:I

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v2, " - freq: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 386
    invoke-static {}, Lcom/qf/clientsdk/QFTunerManager;->getInstance()Lcom/qf/clientsdk/QFTunerManager;

    move-result-object v0

    invoke-virtual {v0, p1}, Lcom/qf/clientsdk/QFTunerManager;->onTune(I)V

    const/4 p1, 0x1

    return p1
.end method

.method public onTuneExt(I)Z
    .locals 3

    .line 397
    sget-object v0, Lcom/android/fmradio/TunerManagerForExt;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "mBand: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget v2, p0, Lcom/android/fmradio/TunerManagerForExt;->mBand:I

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v2, " - freq: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 398
    invoke-static {}, Lcom/qf/clientsdk/QFTunerManager;->getInstance()Lcom/qf/clientsdk/QFTunerManager;

    move-result-object v0

    invoke-virtual {v0, p1}, Lcom/qf/clientsdk/QFTunerManager;->onTune(I)V

    const/4 p1, 0x1

    return p1
.end method

.method public powerDown()Z
    .locals 1

    const/4 v0, 0x1

    return v0
.end method

.method public powerUp()Z
    .locals 1

    const/4 v0, 0x1

    return v0
.end method

.method public setContext(Landroid/content/Context;)V
    .locals 0

    .line 329
    iput-object p1, p0, Lcom/android/fmradio/TunerManagerForExt;->mContext:Landroid/content/Context;

    return-void
.end method

.method public setFrequency(I)V
    .locals 2

    .line 516
    iput p1, p0, Lcom/android/fmradio/TunerManagerForExt;->mFrequency:I

    .line 517
    sget-object p1, Lcom/android/fmradio/TunerManagerForExt;->TAG:Ljava/lang/String;

    new-instance v0, Ljava/lang/StringBuilder;

    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    const-string v1, "setFrequency - mFrequency: "

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget v1, p0, Lcom/android/fmradio/TunerManagerForExt;->mFrequency:I

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    invoke-static {p1, v0}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    return-void
.end method

.method public setMute(Z)I
    .locals 0

    const/4 p1, 0x1

    return p1
.end method

.method public setPresetList([B)V
    .locals 1

    .line 591
    invoke-static {}, Lcom/qf/clientsdk/QFTunerManager;->getInstance()Lcom/qf/clientsdk/QFTunerManager;

    move-result-object v0

    invoke-virtual {v0, p1}, Lcom/qf/clientsdk/QFTunerManager;->setPresetList([B)V

    return-void
.end method

.method public setRds(Z)I
    .locals 0

    const/4 p1, 0x1

    return p1
.end method

.method public setRdsAFSwitch()V
    .locals 1

    .line 547
    invoke-static {}, Lcom/qf/clientsdk/QFTunerManager;->getInstance()Lcom/qf/clientsdk/QFTunerManager;

    move-result-object v0

    invoke-virtual {v0}, Lcom/qf/clientsdk/QFTunerManager;->setRdsAFSwitch()V

    return-void
.end method

.method public setRdsPtyType(I)V
    .locals 1

    .line 529
    iput p1, p0, Lcom/android/fmradio/TunerManagerForExt;->mRdsPtyType:I

    .line 531
    invoke-static {}, Lcom/qf/clientsdk/QFTunerManager;->getInstance()Lcom/qf/clientsdk/QFTunerManager;

    move-result-object p1

    iget v0, p0, Lcom/android/fmradio/TunerManagerForExt;->mRdsPtyType:I

    int-to-byte v0, v0

    invoke-virtual {p1, v0}, Lcom/qf/clientsdk/QFTunerManager;->setRdsPtyType(B)V

    return-void
.end method

.method public setRdsSwitch()V
    .locals 2

    .line 521
    invoke-static {}, Lcom/qf/clientsdk/QFTunerManager;->getInstance()Lcom/qf/clientsdk/QFTunerManager;

    move-result-object v0

    invoke-static {}, Lcom/android/fmradio/utils/FmUtils;->isRDSEnable()Z

    move-result v1

    int-to-byte v1, v1

    invoke-virtual {v0, v1}, Lcom/qf/clientsdk/QFTunerManager;->setRdsSwitch(B)V

    return-void
.end method

.method public setRdsTASwitch()V
    .locals 1

    .line 539
    invoke-static {}, Lcom/qf/clientsdk/QFTunerManager;->getInstance()Lcom/qf/clientsdk/QFTunerManager;

    move-result-object v0

    invoke-virtual {v0}, Lcom/qf/clientsdk/QFTunerManager;->setRdsTASwitch()V

    return-void
.end method

.method public setVolume(I)Z
    .locals 0

    const/4 p1, 0x1

    return p1
.end method

.method public stopScan()Z
    .locals 2

    .line 446
    sget-object v0, Lcom/android/fmradio/TunerManagerForExt;->TAG:Ljava/lang/String;

    const-string v1, "start"

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 447
    invoke-static {}, Lcom/qf/clientsdk/QFTunerManager;->getInstance()Lcom/qf/clientsdk/QFTunerManager;

    move-result-object v0

    invoke-virtual {v0}, Lcom/qf/clientsdk/QFTunerManager;->stopScan()V

    const/4 v0, 0x1

    return v0
.end method
