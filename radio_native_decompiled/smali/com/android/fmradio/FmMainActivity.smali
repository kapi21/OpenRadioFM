.class public Lcom/android/fmradio/FmMainActivity;
.super Lcom/qf/skin/manager/base/SkinFragmentActivity;
.source "FmMainActivity.java"


# annotations
.annotation system Ldalvik/annotation/MemberClasses;
    value = {
        Lcom/android/fmradio/FmMainActivity$FmBroadCastReceiver;
    }
.end annotation


# static fields
.field private static AM_UNIT:Ljava/lang/String;

.field private static final BAND_FREQ_STR:[Ljava/lang/String;

.field private static final BAND_PRESET_STR:[Ljava/lang/String;

.field private static final BAND_STR:[Ljava/lang/String;

.field private static FM_UNIT:Ljava/lang/String;

.field private static final TAG:Ljava/lang/String;

.field private static mLastButtonEventTime:J


# instance fields
.field private mAMStationList:[Lcom/android/fmradio/info/FmFreqInfo;

.field public mBand:I

.field private mBandInfoMap:Ljava/util/HashMap;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Ljava/util/HashMap<",
            "Ljava/lang/Integer;",
            "Lcom/android/fmradio/info/FmBandInfo;",
            ">;"
        }
    .end annotation
.end field

.field public mBandMaxFreq:I

.field public mBandMinFreq:I

.field private mContext:Landroid/content/Context;

.field public mCurrentStation:I

.field public mDiffFreq:I

.field private mFMStationList:[Lcom/android/fmradio/info/FmFreqInfo;

.field private mFmBroadCastReceiver:Lcom/android/fmradio/FmMainActivity$FmBroadCastReceiver;

.field private mFmRadioListener:Lcom/android/fmradio/iface/FmListener;

.field public mGVRdsPty:Landroid/widget/GridView;

.field public mGVRdsPtyAdapter:Lcom/android/fmradio/views/PTYItemAdapter;

.field private mHandler:Landroid/os/Handler;

.field private mIsActivityForeground:Z

.field private mIsServiceBinder:Z

.field private mIsServiceStarted:Z

.field private mIsTune:Z

.field private mKeyEventService:Landroid/qf/util/UtilEventManager;

.field private mKeyListener:Landroid/qf/util/UtilEventListener;

.field private mLocalReceiver:Landroid/content/BroadcastReceiver;

.field private mMediaButtonListener:Lcom/android/fmradio/iface/IMediaButtonListener;

.field private mPreIsInMultiWindowMode:Z

.field public mPresetIndex:I

.field private mPresetListCallback:Lcom/android/fmradio/views/RadioPresetListView$PresetListCallback;

.field public mRadioArea:I

.field private mRadioFreqInfoView:Lcom/android/fmradio/views/RadioFreqInfoView;

.field private mRadioFreqSliderView:Lcom/android/fmradio/views/RadioFreqSliderView;

.field private mRadioOperationView:Lcom/android/fmradio/views/RadioOperationView;

.field private mRadioPresetListView:Lcom/android/fmradio/views/RadioPresetListView;

.field private mRadioStatusView:Lcom/android/fmradio/views/RadioStatusView;

.field private mScale:F

.field private mService:Lcom/android/fmradio/FmService;

.field private final mServiceConnection:Landroid/content/ServiceConnection;

.field private mToast:Landroid/widget/Toast;

.field public mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;


# direct methods
.method static constructor <clinit>()V
    .locals 7

    .line 67
    const-class v0, Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {v0}, Ljava/lang/Class;->getSimpleName()Ljava/lang/String;

    move-result-object v0

    sput-object v0, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    const-string v1, "radio_band_fm1"

    const-string v2, "radio_band_fm2"

    const-string v3, "radio_band_fm3"

    const-string v4, "radio_band_am1"

    const-string v5, "radio_band_am2"

    const-string v6, "radio_band_am3"

    .line 69
    filled-new-array/range {v1 .. v6}, [Ljava/lang/String;

    move-result-object v0

    sput-object v0, Lcom/android/fmradio/FmMainActivity;->BAND_STR:[Ljava/lang/String;

    const-string v1, "radio_band_fm1_preset"

    const-string v2, "radio_band_fm2_preset"

    const-string v3, "radio_band_fm3_preset"

    const-string v4, "radio_band_am1_preset"

    const-string v5, "radio_band_am2_preset"

    const-string v6, "radio_band_am3_preset"

    .line 73
    filled-new-array/range {v1 .. v6}, [Ljava/lang/String;

    move-result-object v0

    sput-object v0, Lcom/android/fmradio/FmMainActivity;->BAND_PRESET_STR:[Ljava/lang/String;

    const-string v1, "radio_band_fm1_freq"

    const-string v2, "radio_band_fm2_freq"

    const-string v3, "radio_band_fm3_freq"

    const-string v4, "radio_band_am1_freq"

    const-string v5, "radio_band_am2_freq"

    const-string v6, "radio_band_am3_freq"

    .line 77
    filled-new-array/range {v1 .. v6}, [Ljava/lang/String;

    move-result-object v0

    sput-object v0, Lcom/android/fmradio/FmMainActivity;->BAND_FREQ_STR:[Ljava/lang/String;

    return-void
.end method

.method public constructor <init>()V
    .locals 2

    .line 65
    invoke-direct {p0}, Lcom/qf/skin/manager/base/SkinFragmentActivity;-><init>()V

    const/16 v0, 0x12

    new-array v0, v0, [Lcom/android/fmradio/info/FmFreqInfo;

    .line 105
    iput-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mFMStationList:[Lcom/android/fmradio/info/FmFreqInfo;

    const/16 v0, 0xc

    new-array v0, v0, [Lcom/android/fmradio/info/FmFreqInfo;

    .line 106
    iput-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mAMStationList:[Lcom/android/fmradio/info/FmFreqInfo;

    .line 108
    new-instance v0, Ljava/util/HashMap;

    invoke-direct {v0}, Ljava/util/HashMap;-><init>()V

    iput-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mBandInfoMap:Ljava/util/HashMap;

    const/4 v0, 0x0

    .line 111
    iput-boolean v0, p0, Lcom/android/fmradio/FmMainActivity;->mIsServiceStarted:Z

    .line 113
    iput-boolean v0, p0, Lcom/android/fmradio/FmMainActivity;->mIsServiceBinder:Z

    .line 115
    iput-boolean v0, p0, Lcom/android/fmradio/FmMainActivity;->mIsTune:Z

    const/4 v1, 0x1

    .line 117
    iput-boolean v1, p0, Lcom/android/fmradio/FmMainActivity;->mIsActivityForeground:Z

    const/4 v1, 0x0

    .line 120
    iput-object v1, p0, Lcom/android/fmradio/FmMainActivity;->mService:Lcom/android/fmradio/FmService;

    .line 122
    iput-object v1, p0, Lcom/android/fmradio/FmMainActivity;->mContext:Landroid/content/Context;

    .line 124
    iput-object v1, p0, Lcom/android/fmradio/FmMainActivity;->mToast:Landroid/widget/Toast;

    .line 130
    iput v0, p0, Lcom/android/fmradio/FmMainActivity;->mBand:I

    .line 132
    iput v0, p0, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    .line 134
    iput v0, p0, Lcom/android/fmradio/FmMainActivity;->mPresetIndex:I

    .line 136
    iput v0, p0, Lcom/android/fmradio/FmMainActivity;->mBandMinFreq:I

    .line 137
    iput v0, p0, Lcom/android/fmradio/FmMainActivity;->mBandMaxFreq:I

    .line 138
    iput v0, p0, Lcom/android/fmradio/FmMainActivity;->mDiffFreq:I

    const/high16 v0, 0x3f800000    # 1.0f

    .line 142
    iput v0, p0, Lcom/android/fmradio/FmMainActivity;->mScale:F

    .line 144
    new-instance v0, Lcom/android/fmradio/FmMainActivity$1;

    invoke-direct {v0, p0}, Lcom/android/fmradio/FmMainActivity$1;-><init>(Lcom/android/fmradio/FmMainActivity;)V

    iput-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mLocalReceiver:Landroid/content/BroadcastReceiver;

    .line 231
    new-instance v0, Lcom/android/fmradio/FmMainActivity$2;

    invoke-direct {v0, p0}, Lcom/android/fmradio/FmMainActivity$2;-><init>(Lcom/android/fmradio/FmMainActivity;)V

    iput-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mFmRadioListener:Lcom/android/fmradio/iface/FmListener;

    .line 261
    new-instance v0, Lcom/android/fmradio/FmMainActivity$3;

    invoke-direct {v0, p0}, Lcom/android/fmradio/FmMainActivity$3;-><init>(Lcom/android/fmradio/FmMainActivity;)V

    iput-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mHandler:Landroid/os/Handler;

    .line 470
    new-instance v0, Lcom/android/fmradio/FmMainActivity$4;

    invoke-direct {v0, p0}, Lcom/android/fmradio/FmMainActivity$4;-><init>(Lcom/android/fmradio/FmMainActivity;)V

    iput-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mServiceConnection:Landroid/content/ServiceConnection;

    .line 527
    new-instance v0, Lcom/android/fmradio/FmMainActivity$5;

    invoke-direct {v0, p0}, Lcom/android/fmradio/FmMainActivity$5;-><init>(Lcom/android/fmradio/FmMainActivity;)V

    iput-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mKeyListener:Landroid/qf/util/UtilEventListener;

    .line 617
    new-instance v0, Lcom/android/fmradio/FmMainActivity$6;

    invoke-direct {v0, p0}, Lcom/android/fmradio/FmMainActivity$6;-><init>(Lcom/android/fmradio/FmMainActivity;)V

    iput-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mMediaButtonListener:Lcom/android/fmradio/iface/IMediaButtonListener;

    return-void
.end method

.method static synthetic access$000(Lcom/android/fmradio/FmMainActivity;)Lcom/android/fmradio/views/RadioFreqInfoView;
    .locals 0

    .line 65
    iget-object p0, p0, Lcom/android/fmradio/FmMainActivity;->mRadioFreqInfoView:Lcom/android/fmradio/views/RadioFreqInfoView;

    return-object p0
.end method

.method static synthetic access$100()Ljava/lang/String;
    .locals 1

    .line 65
    sget-object v0, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    return-object v0
.end method

.method static synthetic access$1000(Lcom/android/fmradio/FmMainActivity;)V
    .locals 0

    .line 65
    invoke-direct {p0}, Lcom/android/fmradio/FmMainActivity;->sendFmInfo()V

    return-void
.end method

.method static synthetic access$1100(Lcom/android/fmradio/FmMainActivity;)V
    .locals 0

    .line 65
    invoke-direct {p0}, Lcom/android/fmradio/FmMainActivity;->onAutoFinished()V

    return-void
.end method

.method static synthetic access$1200(Lcom/android/fmradio/FmMainActivity;)Lcom/android/fmradio/FmService;
    .locals 0

    .line 65
    iget-object p0, p0, Lcom/android/fmradio/FmMainActivity;->mService:Lcom/android/fmradio/FmService;

    return-object p0
.end method

.method static synthetic access$1202(Lcom/android/fmradio/FmMainActivity;Lcom/android/fmradio/FmService;)Lcom/android/fmradio/FmService;
    .locals 0

    .line 65
    iput-object p1, p0, Lcom/android/fmradio/FmMainActivity;->mService:Lcom/android/fmradio/FmService;

    return-object p1
.end method

.method static synthetic access$1300(Lcom/android/fmradio/FmMainActivity;)V
    .locals 0

    .line 65
    invoke-direct {p0}, Lcom/android/fmradio/FmMainActivity;->powerUpFm()V

    return-void
.end method

.method static synthetic access$1400(Lcom/android/fmradio/FmMainActivity;)Lcom/android/fmradio/iface/FmListener;
    .locals 0

    .line 65
    iget-object p0, p0, Lcom/android/fmradio/FmMainActivity;->mFmRadioListener:Lcom/android/fmradio/iface/FmListener;

    return-object p0
.end method

.method static synthetic access$1500(Lcom/android/fmradio/FmMainActivity;)Lcom/android/fmradio/iface/IMediaButtonListener;
    .locals 0

    .line 65
    iget-object p0, p0, Lcom/android/fmradio/FmMainActivity;->mMediaButtonListener:Lcom/android/fmradio/iface/IMediaButtonListener;

    return-object p0
.end method

.method static synthetic access$1600(Lcom/android/fmradio/FmMainActivity;)Z
    .locals 0

    .line 65
    iget-boolean p0, p0, Lcom/android/fmradio/FmMainActivity;->mIsActivityForeground:Z

    return p0
.end method

.method static synthetic access$1700(Lcom/android/fmradio/FmMainActivity;)Z
    .locals 0

    .line 65
    iget-boolean p0, p0, Lcom/android/fmradio/FmMainActivity;->mIsTune:Z

    return p0
.end method

.method static synthetic access$1702(Lcom/android/fmradio/FmMainActivity;Z)Z
    .locals 0

    .line 65
    iput-boolean p1, p0, Lcom/android/fmradio/FmMainActivity;->mIsTune:Z

    return p1
.end method

.method static synthetic access$1800(Lcom/android/fmradio/FmMainActivity;)V
    .locals 0

    .line 65
    invoke-direct {p0}, Lcom/android/fmradio/FmMainActivity;->updateCurrentStation()V

    return-void
.end method

.method static synthetic access$1900(Lcom/android/fmradio/FmMainActivity;)V
    .locals 0

    .line 65
    invoke-direct {p0}, Lcom/android/fmradio/FmMainActivity;->exitService()V

    return-void
.end method

.method static synthetic access$200(Lcom/android/fmradio/FmMainActivity;IZ)V
    .locals 0

    .line 65
    invoke-direct {p0, p1, p2}, Lcom/android/fmradio/FmMainActivity;->tuneStation(IZ)V

    return-void
.end method

.method static synthetic access$2000(Lcom/android/fmradio/FmMainActivity;I)V
    .locals 0

    .line 65
    invoke-direct {p0, p1}, Lcom/android/fmradio/FmMainActivity;->onBandExt(I)V

    return-void
.end method

.method static synthetic access$2100(Lcom/android/fmradio/FmMainActivity;)Lcom/android/fmradio/views/RadioPresetListView;
    .locals 0

    .line 65
    iget-object p0, p0, Lcom/android/fmradio/FmMainActivity;->mRadioPresetListView:Lcom/android/fmradio/views/RadioPresetListView;

    return-object p0
.end method

.method static synthetic access$2200(Lcom/android/fmradio/FmMainActivity;I)V
    .locals 0

    .line 65
    invoke-direct {p0, p1}, Lcom/android/fmradio/FmMainActivity;->onRdsPtyItemClick(I)V

    return-void
.end method

.method static synthetic access$2300(Lcom/android/fmradio/FmMainActivity;)[Lcom/android/fmradio/info/FmFreqInfo;
    .locals 0

    .line 65
    iget-object p0, p0, Lcom/android/fmradio/FmMainActivity;->mFMStationList:[Lcom/android/fmradio/info/FmFreqInfo;

    return-object p0
.end method

.method static synthetic access$2400(Lcom/android/fmradio/FmMainActivity;)[Lcom/android/fmradio/info/FmFreqInfo;
    .locals 0

    .line 65
    iget-object p0, p0, Lcom/android/fmradio/FmMainActivity;->mAMStationList:[Lcom/android/fmradio/info/FmFreqInfo;

    return-object p0
.end method

.method static synthetic access$2500(Lcom/android/fmradio/FmMainActivity;)Landroid/content/Context;
    .locals 0

    .line 65
    iget-object p0, p0, Lcom/android/fmradio/FmMainActivity;->mContext:Landroid/content/Context;

    return-object p0
.end method

.method static synthetic access$2600(Lcom/android/fmradio/FmMainActivity;IIZ)V
    .locals 0

    .line 65
    invoke-direct {p0, p1, p2, p3}, Lcom/android/fmradio/FmMainActivity;->updateStationList(IIZ)V

    return-void
.end method

.method static synthetic access$300(Lcom/android/fmradio/FmMainActivity;Ljava/lang/CharSequence;)V
    .locals 0

    .line 65
    invoke-direct {p0, p1}, Lcom/android/fmradio/FmMainActivity;->showToast(Ljava/lang/CharSequence;)V

    return-void
.end method

.method static synthetic access$400(Lcom/android/fmradio/FmMainActivity;)V
    .locals 0

    .line 65
    invoke-direct {p0}, Lcom/android/fmradio/FmMainActivity;->updateRadioArea()V

    return-void
.end method

.method static synthetic access$500(Lcom/android/fmradio/FmMainActivity;)Landroid/os/Handler;
    .locals 0

    .line 65
    iget-object p0, p0, Lcom/android/fmradio/FmMainActivity;->mHandler:Landroid/os/Handler;

    return-object p0
.end method

.method static synthetic access$600(Lcom/android/fmradio/FmMainActivity;)Lcom/android/fmradio/views/RadioStatusView;
    .locals 0

    .line 65
    iget-object p0, p0, Lcom/android/fmradio/FmMainActivity;->mRadioStatusView:Lcom/android/fmradio/views/RadioStatusView;

    return-object p0
.end method

.method static synthetic access$700(Lcom/android/fmradio/FmMainActivity;)Lcom/android/fmradio/views/RadioOperationView;
    .locals 0

    .line 65
    iget-object p0, p0, Lcom/android/fmradio/FmMainActivity;->mRadioOperationView:Lcom/android/fmradio/views/RadioOperationView;

    return-object p0
.end method

.method static synthetic access$800(Lcom/android/fmradio/FmMainActivity;I)V
    .locals 0

    .line 65
    invoke-direct {p0, p1}, Lcom/android/fmradio/FmMainActivity;->refreshStationUI(I)V

    return-void
.end method

.method static synthetic access$900(Lcom/android/fmradio/FmMainActivity;III)V
    .locals 0

    .line 65
    invoke-direct {p0, p1, p2, p3}, Lcom/android/fmradio/FmMainActivity;->setBandInfo(III)V

    return-void
.end method

.method private cleanAllPageButtonHighlight()V
    .locals 5

    .line 2153
    invoke-static {p0}, Lcom/android/fmradio/database/FmStation;->getCurrentBand(Landroid/content/Context;)I

    move-result v0

    invoke-static {v0}, Lcom/android/fmradio/utils/FmUtils;->isFMBand(I)Z

    move-result v0

    const/4 v1, 0x1

    const/4 v2, 0x0

    if-eqz v0, :cond_2

    .line 2158
    sget-object v0, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    new-instance v3, Ljava/lang/StringBuilder;

    invoke-direct {v3}, Ljava/lang/StringBuilder;-><init>()V

    const-string v4, "cleanAllPageButtonHighlight - mBand: "

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget v4, p0, Lcom/android/fmradio/FmMainActivity;->mBand:I

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v3}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v3

    invoke-static {v0, v3}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 2159
    iget v0, p0, Lcom/android/fmradio/FmMainActivity;->mBand:I

    const/4 v3, 0x6

    if-nez v0, :cond_0

    .line 2160
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mBandInfoMap:Ljava/util/HashMap;

    invoke-static {v2}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v1

    invoke-virtual {v0, v1}, Ljava/util/HashMap;->remove(Ljava/lang/Object;)Ljava/lang/Object;

    move v0, v2

    :goto_0
    if-ge v0, v3, :cond_1

    .line 2163
    new-instance v1, Lcom/android/fmradio/info/FmFreqInfo;

    invoke-direct {v1}, Lcom/android/fmradio/info/FmFreqInfo;-><init>()V

    .line 2164
    invoke-virtual {v1, v2}, Lcom/android/fmradio/info/FmFreqInfo;->setBand(I)V

    .line 2165
    iget-object v4, p0, Lcom/android/fmradio/FmMainActivity;->mFMStationList:[Lcom/android/fmradio/info/FmFreqInfo;

    aput-object v1, v4, v0

    add-int/lit8 v0, v0, 0x1

    goto :goto_0

    .line 2168
    :cond_0
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mBandInfoMap:Ljava/util/HashMap;

    invoke-static {v1}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v1

    invoke-virtual {v0, v1}, Ljava/util/HashMap;->remove(Ljava/lang/Object;)Ljava/lang/Object;

    .line 2169
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mBandInfoMap:Ljava/util/HashMap;

    const/4 v1, 0x2

    invoke-static {v1}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v1

    invoke-virtual {v0, v1}, Ljava/util/HashMap;->remove(Ljava/lang/Object;)Ljava/lang/Object;

    :goto_1
    const/16 v0, 0x12

    if-ge v3, v0, :cond_1

    .line 2172
    new-instance v0, Lcom/android/fmradio/info/FmFreqInfo;

    invoke-direct {v0}, Lcom/android/fmradio/info/FmFreqInfo;-><init>()V

    .line 2173
    invoke-virtual {v0, v2}, Lcom/android/fmradio/info/FmFreqInfo;->setBand(I)V

    .line 2174
    iget-object v1, p0, Lcom/android/fmradio/FmMainActivity;->mFMStationList:[Lcom/android/fmradio/info/FmFreqInfo;

    aput-object v0, v1, v3

    add-int/lit8 v3, v3, 0x1

    goto :goto_1

    .line 2178
    :cond_1
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mContext:Landroid/content/Context;

    invoke-static {v0, v2}, Lcom/android/fmradio/database/FmStation;->cleanSearchedStations(Landroid/content/Context;I)V

    goto :goto_3

    :cond_2
    const/4 v0, 0x3

    :goto_2
    const/4 v3, 0x4

    if-gt v0, v3, :cond_3

    .line 2190
    iget-object v3, p0, Lcom/android/fmradio/FmMainActivity;->mBandInfoMap:Ljava/util/HashMap;

    invoke-static {v0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v4

    invoke-virtual {v3, v4}, Ljava/util/HashMap;->remove(Ljava/lang/Object;)Ljava/lang/Object;

    add-int/lit8 v0, v0, 0x1

    goto :goto_2

    .line 2193
    :cond_3
    invoke-direct {p0, v2}, Lcom/android/fmradio/FmMainActivity;->resetStationList(Z)V

    .line 2195
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mContext:Landroid/content/Context;

    invoke-static {v0, v1}, Lcom/android/fmradio/database/FmStation;->cleanSearchedStations(Landroid/content/Context;I)V

    .line 2198
    :goto_3
    invoke-direct {p0}, Lcom/android/fmradio/FmMainActivity;->clearButtonText()V

    return-void
.end method

.method private clearButtonBackground()V
    .locals 1

    .line 2205
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mRadioPresetListView:Lcom/android/fmradio/views/RadioPresetListView;

    if-eqz v0, :cond_0

    .line 2206
    invoke-virtual {v0}, Lcom/android/fmradio/views/RadioPresetListView;->clearButtonBackground()V

    :cond_0
    return-void
.end method

.method private clearButtonText()V
    .locals 4

    .line 2140
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mRadioPresetListView:Lcom/android/fmradio/views/RadioPresetListView;

    if-eqz v0, :cond_0

    .line 2141
    invoke-virtual {v0}, Lcom/android/fmradio/views/RadioPresetListView;->clearButtonBackground()V

    const/4 v0, 0x0

    move v1, v0

    :goto_0
    const/4 v2, 0x6

    if-ge v1, v2, :cond_0

    .line 2144
    iget-object v2, p0, Lcom/android/fmradio/FmMainActivity;->mRadioPresetListView:Lcom/android/fmradio/views/RadioPresetListView;

    const-string v3, ""

    invoke-virtual {v2, v1, v0, v3}, Lcom/android/fmradio/views/RadioPresetListView;->setFreq(IILjava/lang/String;)V

    add-int/lit8 v1, v1, 0x1

    goto :goto_0

    :cond_0
    return-void
.end method

.method private exitService()V
    .locals 3

    .line 1956
    sget-object v0, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "--->>exitService() mIsServiceStarted: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-boolean v2, p0, Lcom/android/fmradio/FmMainActivity;->mIsServiceStarted:Z

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 1957
    iget-boolean v0, p0, Lcom/android/fmradio/FmMainActivity;->mIsServiceBinder:Z

    const/4 v1, 0x0

    if-eqz v0, :cond_0

    .line 1958
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mServiceConnection:Landroid/content/ServiceConnection;

    invoke-virtual {p0, v0}, Lcom/android/fmradio/FmMainActivity;->unbindService(Landroid/content/ServiceConnection;)V

    .line 1959
    iput-boolean v1, p0, Lcom/android/fmradio/FmMainActivity;->mIsServiceBinder:Z

    .line 1962
    :cond_0
    iget-boolean v0, p0, Lcom/android/fmradio/FmMainActivity;->mIsServiceStarted:Z

    if-eqz v0, :cond_1

    .line 1964
    new-instance v0, Landroid/content/Intent;

    const-class v2, Lcom/android/fmradio/FmService;

    invoke-direct {v0, p0, v2}, Landroid/content/Intent;-><init>(Landroid/content/Context;Ljava/lang/Class;)V

    const-string v2, "fmradio.exit"

    .line 1965
    invoke-virtual {v0, v2}, Landroid/content/Intent;->setAction(Ljava/lang/String;)Landroid/content/Intent;

    .line 1966
    invoke-virtual {p0, v0}, Lcom/android/fmradio/FmMainActivity;->startService(Landroid/content/Intent;)Landroid/content/ComponentName;

    .line 1967
    iput-boolean v1, p0, Lcom/android/fmradio/FmMainActivity;->mIsServiceStarted:Z

    :cond_1
    return-void
.end method

.method private initBandInfo()V
    .locals 10

    const/4 v0, 0x0

    :goto_0
    const/4 v1, 0x5

    if-ge v0, v1, :cond_3

    .line 1130
    new-instance v1, Lcom/android/fmradio/info/FmBandInfo;

    invoke-direct {v1}, Lcom/android/fmradio/info/FmBandInfo;-><init>()V

    .line 1131
    sget-object v2, Lcom/android/fmradio/FmMainActivity;->BAND_STR:[Ljava/lang/String;

    aget-object v2, v2, v0

    invoke-static {p0, v2}, Lcom/android/fmradio/utils/RadioConfigData;->getFMBand(Landroid/content/Context;Ljava/lang/String;)I

    move-result v2

    const/4 v3, -0x1

    if-ne v2, v3, :cond_0

    move v2, v0

    .line 1135
    :cond_0
    sget-object v3, Lcom/android/fmradio/FmMainActivity;->BAND_PRESET_STR:[Ljava/lang/String;

    aget-object v3, v3, v0

    invoke-static {p0, v3}, Lcom/android/fmradio/utils/RadioConfigData;->getFMBandPreset(Landroid/content/Context;Ljava/lang/String;)I

    move-result v3

    .line 1137
    sget-object v4, Lcom/android/fmradio/FmMainActivity;->BAND_FREQ_STR:[Ljava/lang/String;

    aget-object v4, v4, v0

    invoke-static {p0, v4}, Lcom/android/fmradio/utils/RadioConfigData;->getFMBandFreq(Landroid/content/Context;Ljava/lang/String;)I

    move-result v4

    .line 1138
    sget-object v5, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    new-instance v6, Ljava/lang/StringBuilder;

    invoke-direct {v6}, Ljava/lang/StringBuilder;-><init>()V

    const-string v7, "1 initBandInfo - band: "

    invoke-virtual {v6, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v6, v2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v7, " - bandPreset: "

    invoke-virtual {v6, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v6, v3}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v8, " - bandFreq: "

    invoke-virtual {v6, v8}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v6, v4}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v6}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v6

    invoke-static {v5, v6}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    if-nez v4, :cond_2

    if-ltz v2, :cond_1

    const/4 v4, 0x2

    if-gt v2, v4, :cond_1

    .line 1143
    iget v4, p0, Lcom/android/fmradio/FmMainActivity;->mRadioArea:I

    invoke-static {v4, v2}, Lcom/android/fmradio/utils/FmUtils;->getRadioAreaFMMinFreq(II)I

    move-result v4

    goto :goto_1

    .line 1145
    :cond_1
    iget v4, p0, Lcom/android/fmradio/FmMainActivity;->mRadioArea:I

    invoke-static {v4}, Lcom/android/fmradio/utils/FmUtils;->getRadioAreaAMMinFreq(I)I

    move-result v4

    .line 1149
    :cond_2
    :goto_1
    sget-object v5, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    new-instance v6, Ljava/lang/StringBuilder;

    invoke-direct {v6}, Ljava/lang/StringBuilder;-><init>()V

    const-string v9, "2 initBandInfo - band: "

    invoke-virtual {v6, v9}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v6, v2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v6, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v6, v3}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v6, v8}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v6, v4}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v6}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v6

    invoke-static {v5, v6}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 1151
    invoke-virtual {v1, v2}, Lcom/android/fmradio/info/FmBandInfo;->setBand(I)V

    .line 1152
    invoke-virtual {v1, v3}, Lcom/android/fmradio/info/FmBandInfo;->setBandPreset(I)V

    .line 1153
    invoke-virtual {v1, v4}, Lcom/android/fmradio/info/FmBandInfo;->setBandFreq(I)V

    .line 1155
    iget-object v2, p0, Lcom/android/fmradio/FmMainActivity;->mBandInfoMap:Ljava/util/HashMap;

    invoke-static {v0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v3

    invoke-virtual {v2, v3, v1}, Ljava/util/HashMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

    add-int/lit8 v0, v0, 0x1

    goto/16 :goto_0

    :cond_3
    return-void
.end method

.method private initData()V
    .locals 2

    .line 813
    invoke-virtual {p0}, Lcom/android/fmradio/FmMainActivity;->getResources()Landroid/content/res/Resources;

    move-result-object v0

    const v1, 0x7f0c0027

    invoke-virtual {v0, v1}, Landroid/content/res/Resources;->getString(I)Ljava/lang/String;

    move-result-object v0

    sput-object v0, Lcom/android/fmradio/FmMainActivity;->FM_UNIT:Ljava/lang/String;

    .line 814
    invoke-virtual {p0}, Lcom/android/fmradio/FmMainActivity;->getResources()Landroid/content/res/Resources;

    move-result-object v0

    const v1, 0x7f0c0026

    invoke-virtual {v0, v1}, Landroid/content/res/Resources;->getString(I)Ljava/lang/String;

    move-result-object v0

    sput-object v0, Lcom/android/fmradio/FmMainActivity;->AM_UNIT:Ljava/lang/String;

    .line 816
    invoke-static {}, Lcom/android/fmradio/TunerManagerForExt;->getInstance()Lcom/android/fmradio/TunerManagerForExt;

    move-result-object v0

    iput-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    .line 817
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    invoke-virtual {v0, p0}, Lcom/android/fmradio/TunerManagerForExt;->init(Lcom/android/fmradio/FmMainActivity;)V

    .line 819
    invoke-direct {p0}, Lcom/android/fmradio/FmMainActivity;->updateRadioArea()V

    .line 832
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mContext:Landroid/content/Context;

    invoke-static {v0}, Lcom/android/fmradio/database/FmStation;->getCurrentStation(Landroid/content/Context;)I

    move-result v0

    iput v0, p0, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    .line 835
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mContext:Landroid/content/Context;

    invoke-static {v0}, Lcom/android/fmradio/database/FmStation;->getCurrentBand(Landroid/content/Context;)I

    move-result v0

    iput v0, p0, Lcom/android/fmradio/FmMainActivity;->mBand:I

    .line 838
    new-instance v0, Ljava/lang/StringBuilder;

    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    const-string v1, "initData - mRadioArea: "

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget v1, p0, Lcom/android/fmradio/FmMainActivity;->mRadioArea:I

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v1, " - mBand: "

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget v1, p0, Lcom/android/fmradio/FmMainActivity;->mBand:I

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v1, " - mCurrentStation: "

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget v1, p0, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    .line 841
    sget-object v1, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    invoke-static {v1, v0}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 843
    invoke-direct {p0}, Lcom/android/fmradio/FmMainActivity;->initFreq()V

    .line 845
    invoke-direct {p0}, Lcom/android/fmradio/FmMainActivity;->initBandInfo()V

    .line 847
    invoke-direct {p0}, Lcom/android/fmradio/FmMainActivity;->initStationList()V

    return-void
.end method

.method private initFreq()V
    .locals 3

    .line 915
    invoke-static {p0}, Lcom/android/fmradio/database/FmStation;->getCurrentBand(Landroid/content/Context;)I

    move-result v0

    invoke-static {v0}, Lcom/android/fmradio/utils/FmUtils;->isFMBand(I)Z

    move-result v0

    if-eqz v0, :cond_0

    .line 916
    iget v0, p0, Lcom/android/fmradio/FmMainActivity;->mRadioArea:I

    iget v1, p0, Lcom/android/fmradio/FmMainActivity;->mBand:I

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/FmUtils;->getRadioAreaFMMinFreq(II)I

    move-result v0

    iput v0, p0, Lcom/android/fmradio/FmMainActivity;->mBandMinFreq:I

    .line 917
    iget v0, p0, Lcom/android/fmradio/FmMainActivity;->mRadioArea:I

    iget v1, p0, Lcom/android/fmradio/FmMainActivity;->mBand:I

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/FmUtils;->getRadioAreaFMMaxFreq(II)I

    move-result v0

    iput v0, p0, Lcom/android/fmradio/FmMainActivity;->mBandMaxFreq:I

    goto :goto_0

    .line 919
    :cond_0
    iget v0, p0, Lcom/android/fmradio/FmMainActivity;->mRadioArea:I

    invoke-static {v0}, Lcom/android/fmradio/utils/FmUtils;->getRadioAreaAMMinFreq(I)I

    move-result v0

    iput v0, p0, Lcom/android/fmradio/FmMainActivity;->mBandMinFreq:I

    .line 920
    iget v0, p0, Lcom/android/fmradio/FmMainActivity;->mRadioArea:I

    invoke-static {v0}, Lcom/android/fmradio/utils/FmUtils;->getRadioAreaAMMaxFreq(I)I

    move-result v0

    iput v0, p0, Lcom/android/fmradio/FmMainActivity;->mBandMaxFreq:I

    .line 923
    :goto_0
    sget-object v0, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "initFreq - mBandMinFreq: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget v2, p0, Lcom/android/fmradio/FmMainActivity;->mBandMinFreq:I

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v2, " - mBandMaxFreq: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget v2, p0, Lcom/android/fmradio/FmMainActivity;->mBandMaxFreq:I

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 925
    iget v0, p0, Lcom/android/fmradio/FmMainActivity;->mBandMaxFreq:I

    iget v1, p0, Lcom/android/fmradio/FmMainActivity;->mBandMinFreq:I

    sub-int/2addr v0, v1

    iput v0, p0, Lcom/android/fmradio/FmMainActivity;->mDiffFreq:I

    return-void
.end method

.method private initPresetListView()V
    .locals 2

    .line 1332
    new-instance v0, Lcom/android/fmradio/FmMainActivity$8;

    invoke-direct {v0, p0}, Lcom/android/fmradio/FmMainActivity$8;-><init>(Lcom/android/fmradio/FmMainActivity;)V

    iput-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mPresetListCallback:Lcom/android/fmradio/views/RadioPresetListView$PresetListCallback;

    const v0, 0x7f08009e

    .line 1423
    invoke-virtual {p0, v0}, Lcom/android/fmradio/FmMainActivity;->findViewById(I)Landroid/view/View;

    move-result-object v0

    check-cast v0, Lcom/android/fmradio/views/RadioPresetListView;

    iput-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mRadioPresetListView:Lcom/android/fmradio/views/RadioPresetListView;

    .line 1424
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mRadioPresetListView:Lcom/android/fmradio/views/RadioPresetListView;

    iget-object v1, p0, Lcom/android/fmradio/FmMainActivity;->mPresetListCallback:Lcom/android/fmradio/views/RadioPresetListView$PresetListCallback;

    invoke-virtual {v0, v1}, Lcom/android/fmradio/views/RadioPresetListView;->setPresetListCallback(Lcom/android/fmradio/views/RadioPresetListView$PresetListCallback;)V

    return-void
.end method

.method private initRadioFreqInfoView()V
    .locals 1

    const v0, 0x7f080084

    .line 1322
    invoke-virtual {p0, v0}, Lcom/android/fmradio/FmMainActivity;->findViewById(I)Landroid/view/View;

    move-result-object v0

    check-cast v0, Lcom/android/fmradio/views/RadioFreqInfoView;

    iput-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mRadioFreqInfoView:Lcom/android/fmradio/views/RadioFreqInfoView;

    .line 1323
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mRadioFreqInfoView:Lcom/android/fmradio/views/RadioFreqInfoView;

    invoke-virtual {v0, p0}, Lcom/android/fmradio/views/RadioFreqInfoView;->setActivity(Lcom/android/fmradio/FmMainActivity;)V

    return-void
.end method

.method private initRadioFreqSliderView()V
    .locals 1

    const v0, 0x7f080085

    .line 1316
    invoke-virtual {p0, v0}, Lcom/android/fmradio/FmMainActivity;->findViewById(I)Landroid/view/View;

    move-result-object v0

    check-cast v0, Lcom/android/fmradio/views/RadioFreqSliderView;

    iput-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mRadioFreqSliderView:Lcom/android/fmradio/views/RadioFreqSliderView;

    .line 1317
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mRadioFreqSliderView:Lcom/android/fmradio/views/RadioFreqSliderView;

    invoke-virtual {v0, p0}, Lcom/android/fmradio/views/RadioFreqSliderView;->setActivity(Lcom/android/fmradio/FmMainActivity;)V

    .line 1318
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mRadioFreqSliderView:Lcom/android/fmradio/views/RadioFreqSliderView;

    invoke-virtual {v0}, Lcom/android/fmradio/views/RadioFreqSliderView;->initSlider()V

    return-void
.end method

.method private initRadioOperationView()V
    .locals 1

    const v0, 0x7f08009d

    .line 1290
    invoke-virtual {p0, v0}, Lcom/android/fmradio/FmMainActivity;->findViewById(I)Landroid/view/View;

    move-result-object v0

    check-cast v0, Lcom/android/fmradio/views/RadioOperationView;

    iput-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mRadioOperationView:Lcom/android/fmradio/views/RadioOperationView;

    .line 1291
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mRadioOperationView:Lcom/android/fmradio/views/RadioOperationView;

    invoke-virtual {v0, p0}, Lcom/android/fmradio/views/RadioOperationView;->setActivity(Lcom/android/fmradio/FmMainActivity;)V

    return-void
.end method

.method private initRadioStatusView()V
    .locals 1

    const v0, 0x7f080086

    .line 1327
    invoke-virtual {p0, v0}, Lcom/android/fmradio/FmMainActivity;->findViewById(I)Landroid/view/View;

    move-result-object v0

    check-cast v0, Lcom/android/fmradio/views/RadioStatusView;

    iput-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mRadioStatusView:Lcom/android/fmradio/views/RadioStatusView;

    .line 1328
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mRadioStatusView:Lcom/android/fmradio/views/RadioStatusView;

    invoke-virtual {v0, p0}, Lcom/android/fmradio/views/RadioStatusView;->setActivity(Lcom/android/fmradio/FmMainActivity;)V

    return-void
.end method

.method private initRdsPtyView()V
    .locals 2

    const v0, 0x7f080070

    .line 1295
    invoke-virtual {p0, v0}, Lcom/android/fmradio/FmMainActivity;->findViewById(I)Landroid/view/View;

    move-result-object v0

    check-cast v0, Landroid/widget/GridView;

    iput-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mGVRdsPty:Landroid/widget/GridView;

    .line 1296
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mGVRdsPty:Landroid/widget/GridView;

    new-instance v1, Lcom/android/fmradio/FmMainActivity$7;

    invoke-direct {v1, p0}, Lcom/android/fmradio/FmMainActivity$7;-><init>(Lcom/android/fmradio/FmMainActivity;)V

    invoke-virtual {v0, v1}, Landroid/widget/GridView;->setOnItemClickListener(Landroid/widget/AdapterView$OnItemClickListener;)V

    .line 1310
    new-instance v0, Lcom/android/fmradio/views/PTYItemAdapter;

    sget-object v1, Lcom/android/fmradio/FmConstants;->RDS_PTY_DATAS:[Ljava/lang/String;

    invoke-direct {v0, p0, v1}, Lcom/android/fmradio/views/PTYItemAdapter;-><init>(Landroid/content/Context;[Ljava/lang/String;)V

    iput-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mGVRdsPtyAdapter:Lcom/android/fmradio/views/PTYItemAdapter;

    .line 1311
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mGVRdsPtyAdapter:Lcom/android/fmradio/views/PTYItemAdapter;

    iget-object v1, p0, Lcom/android/fmradio/FmMainActivity;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    invoke-virtual {v1}, Lcom/android/fmradio/TunerManagerForExt;->getRdsPtyType()I

    move-result v1

    invoke-virtual {v0, v1}, Lcom/android/fmradio/views/PTYItemAdapter;->setPtyType(I)V

    .line 1312
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mGVRdsPty:Landroid/widget/GridView;

    iget-object v1, p0, Lcom/android/fmradio/FmMainActivity;->mGVRdsPtyAdapter:Lcom/android/fmradio/views/PTYItemAdapter;

    invoke-virtual {v0, v1}, Landroid/widget/GridView;->setAdapter(Landroid/widget/ListAdapter;)V

    return-void
.end method

.method private initStationList()V
    .locals 2

    .line 932
    sget-object v0, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    const-string v1, "initStationList - start"

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    const/4 v0, 0x1

    .line 934
    invoke-direct {p0, v0}, Lcom/android/fmradio/FmMainActivity;->updateStationList(Z)V

    const/4 v0, 0x0

    .line 936
    invoke-direct {p0, v0}, Lcom/android/fmradio/FmMainActivity;->updateStationList(Z)V

    .line 938
    sget-object v0, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    const-string v1, "initStationList - end"

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    return-void
.end method

.method private initUiComponent()V
    .locals 0

    .line 1276
    invoke-direct {p0}, Lcom/android/fmradio/FmMainActivity;->initRadioFreqSliderView()V

    .line 1278
    invoke-direct {p0}, Lcom/android/fmradio/FmMainActivity;->initRdsPtyView()V

    .line 1280
    invoke-direct {p0}, Lcom/android/fmradio/FmMainActivity;->initRadioStatusView()V

    .line 1282
    invoke-direct {p0}, Lcom/android/fmradio/FmMainActivity;->initRadioFreqInfoView()V

    .line 1284
    invoke-direct {p0}, Lcom/android/fmradio/FmMainActivity;->initPresetListView()V

    .line 1286
    invoke-direct {p0}, Lcom/android/fmradio/FmMainActivity;->initRadioOperationView()V

    return-void
.end method

.method private initUtilEventManager()V
    .locals 2

    const-string v0, "util_service"

    .line 1428
    invoke-virtual {p0, v0}, Lcom/android/fmradio/FmMainActivity;->getSystemService(Ljava/lang/String;)Ljava/lang/Object;

    move-result-object v0

    check-cast v0, Landroid/qf/util/UtilEventManager;

    iput-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mKeyEventService:Landroid/qf/util/UtilEventManager;

    .line 1430
    :try_start_0
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mKeyEventService:Landroid/qf/util/UtilEventManager;

    iget-object v1, p0, Lcom/android/fmradio/FmMainActivity;->mKeyListener:Landroid/qf/util/UtilEventListener;

    invoke-virtual {v0, v1}, Landroid/qf/util/UtilEventManager;->RPC_KeyEventChangedListener(Landroid/qf/util/UtilEventListener;)V
    :try_end_0
    .catch Landroid/os/RemoteException; {:try_start_0 .. :try_end_0} :catch_0

    goto :goto_0

    :catch_0
    move-exception v0

    .line 1432
    invoke-virtual {v0}, Landroid/os/RemoteException;->printStackTrace()V

    :goto_0
    return-void
.end method

.method private onAutoFinished()V
    .locals 12

    .line 2049
    sget-object v0, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    const-string v1, "start"

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 2052
    invoke-static {p0}, Lcom/android/fmradio/database/FmStation;->getCurrentBand(Landroid/content/Context;)I

    move-result v0

    invoke-static {v0}, Lcom/android/fmradio/utils/FmUtils;->isFMBand(I)Z

    move-result v0

    const/4 v1, 0x1

    const/4 v2, 0x0

    if-eqz v0, :cond_0

    .line 2053
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mContext:Landroid/content/Context;

    invoke-static {v0, v2}, Lcom/android/fmradio/database/FmStation;->cleanSearchedStations(Landroid/content/Context;I)V

    goto :goto_0

    .line 2055
    :cond_0
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mContext:Landroid/content/Context;

    invoke-static {v0, v1}, Lcom/android/fmradio/database/FmStation;->cleanSearchedStations(Landroid/content/Context;I)V

    .line 2060
    :goto_0
    iget v0, p0, Lcom/android/fmradio/FmMainActivity;->mBand:I

    invoke-static {v0}, Lcom/android/fmradio/utils/FmUtils;->isFMBand(I)Z

    move-result v0

    if-eqz v0, :cond_1

    .line 2061
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mFMStationList:[Lcom/android/fmradio/info/FmFreqInfo;

    const/16 v3, 0x12

    invoke-static {v0, v3}, Ljava/util/Arrays;->copyOf([Ljava/lang/Object;I)[Ljava/lang/Object;

    move-result-object v0

    check-cast v0, [Lcom/android/fmradio/info/FmFreqInfo;

    goto :goto_1

    .line 2063
    :cond_1
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mAMStationList:[Lcom/android/fmradio/info/FmFreqInfo;

    const/16 v3, 0xc

    invoke-static {v0, v3}, Ljava/util/Arrays;->copyOf([Ljava/lang/Object;I)[Ljava/lang/Object;

    move-result-object v0

    check-cast v0, [Lcom/android/fmradio/info/FmFreqInfo;

    .line 2066
    :goto_1
    new-instance v3, Ljava/lang/StringBuilder;

    invoke-direct {v3}, Ljava/lang/StringBuilder;-><init>()V

    .line 2067
    array-length v4, v0

    move v5, v2

    :goto_2
    if-ge v5, v4, :cond_2

    aget-object v6, v0, v5

    .line 2068
    invoke-virtual {v6}, Lcom/android/fmradio/info/FmFreqInfo;->toString()Ljava/lang/String;

    move-result-object v6

    invoke-virtual {v3, v6}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    const-string v6, " -- "

    invoke-virtual {v3, v6}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    add-int/lit8 v5, v5, 0x1

    goto :goto_2

    .line 2070
    :cond_2
    sget-object v4, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    invoke-virtual {v3}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v3

    invoke-static {v4, v3}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 2073
    array-length v3, v0

    new-array v3, v3, [Landroid/content/ContentValues;

    .line 2075
    array-length v4, v0

    move v5, v2

    move v6, v5

    move v7, v6

    :goto_3
    if-ge v5, v4, :cond_4

    aget-object v8, v0, v5

    .line 2076
    new-instance v9, Landroid/content/ContentValues;

    const/4 v10, 0x4

    invoke-direct {v9, v10}, Landroid/content/ContentValues;-><init>(I)V

    .line 2077
    invoke-virtual {v8}, Lcom/android/fmradio/info/FmFreqInfo;->getFreqIndex()I

    move-result v10

    invoke-static {v10}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v10

    const-string v11, "preset"

    invoke-virtual {v9, v11, v10}, Landroid/content/ContentValues;->put(Ljava/lang/String;Ljava/lang/Integer;)V

    .line 2078
    invoke-virtual {v8}, Lcom/android/fmradio/info/FmFreqInfo;->getFreq()I

    move-result v10

    invoke-static {v10}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v10

    const-string v11, "frequency"

    invoke-virtual {v9, v11, v10}, Landroid/content/ContentValues;->put(Ljava/lang/String;Ljava/lang/Integer;)V

    const-string v10, "station_name"

    const-string v11, ""

    .line 2079
    invoke-virtual {v9, v10, v11}, Landroid/content/ContentValues;->put(Ljava/lang/String;Ljava/lang/String;)V

    .line 2080
    invoke-virtual {v8}, Lcom/android/fmradio/info/FmFreqInfo;->getBand()I

    move-result v10

    invoke-static {v10}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v10

    const-string v11, "radio_band"

    invoke-virtual {v9, v11, v10}, Landroid/content/ContentValues;->put(Ljava/lang/String;Ljava/lang/Integer;)V

    add-int/lit8 v10, v6, 0x1

    .line 2082
    aput-object v9, v3, v6

    if-nez v7, :cond_3

    .line 2084
    invoke-virtual {v8}, Lcom/android/fmradio/info/FmFreqInfo;->getFreq()I

    move-result v6

    if-eqz v6, :cond_3

    move v7, v1

    :cond_3
    add-int/lit8 v5, v5, 0x1

    move v6, v10

    goto :goto_3

    .line 2090
    :cond_4
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mContext:Landroid/content/Context;

    invoke-virtual {v0}, Landroid/content/Context;->getContentResolver()Landroid/content/ContentResolver;

    move-result-object v0

    sget-object v1, Lcom/android/fmradio/database/FmStation$Station;->CONTENT_URI:Landroid/net/Uri;

    invoke-virtual {v0, v1, v3}, Landroid/content/ContentResolver;->bulkInsert(Landroid/net/Uri;[Landroid/content/ContentValues;)I

    .line 2094
    sget-object v0, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v3, "hasValidFreq: "

    invoke-virtual {v1, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, v7}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    if-eqz v7, :cond_5

    .line 2095
    invoke-static {}, Landroid/qf/os/QFApi;->isCurrentMute()Z

    move-result v0

    if-eqz v0, :cond_5

    .line 2096
    invoke-static {v2}, Landroid/qf/os/QFApi;->setVolumeStateMute(Z)V

    :cond_5
    return-void
.end method

.method private onBandExt(I)V
    .locals 6

    .line 1804
    invoke-static {}, Landroid/os/SystemClock;->elapsedRealtime()J

    move-result-wide v0

    .line 1805
    sget-wide v2, Lcom/android/fmradio/FmMainActivity;->mLastButtonEventTime:J

    sub-long v2, v0, v2

    const-wide/16 v4, 0x1f4

    cmp-long v2, v2, v4

    if-gez v2, :cond_0

    return-void

    .line 1809
    :cond_0
    sput-wide v0, Lcom/android/fmradio/FmMainActivity;->mLastButtonEventTime:J

    .line 1811
    sget-object v0, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "onBandExt - band: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 1813
    invoke-direct {p0, p1}, Lcom/android/fmradio/FmMainActivity;->setBand(I)V

    return-void
.end method

.method private onOperationDelay(I)Z
    .locals 6

    .line 1869
    invoke-static {}, Landroid/os/SystemClock;->elapsedRealtime()J

    move-result-wide v0

    .line 1870
    sget-wide v2, Lcom/android/fmradio/FmMainActivity;->mLastButtonEventTime:J

    sub-long v2, v0, v2

    int-to-long v4, p1

    cmp-long p1, v2, v4

    if-gez p1, :cond_0

    const/4 p1, 0x1

    return p1

    .line 1874
    :cond_0
    sput-wide v0, Lcom/android/fmradio/FmMainActivity;->mLastButtonEventTime:J

    const/4 p1, 0x0

    return p1
.end method

.method private onRdsPtyItemClick(I)V
    .locals 2

    .line 1617
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mGVRdsPty:Landroid/widget/GridView;

    const/16 v1, 0x8

    invoke-virtual {v0, v1}, Landroid/widget/GridView;->setVisibility(I)V

    if-nez p1, :cond_0

    const/4 v0, 0x0

    .line 1619
    invoke-static {v0}, Lcom/android/fmradio/utils/FmUtils;->setPTYEnable(Z)V

    goto :goto_0

    :cond_0
    const/4 v0, 0x1

    .line 1623
    invoke-static {v0}, Lcom/android/fmradio/utils/FmUtils;->setPTYEnable(Z)V

    .line 1628
    :goto_0
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    invoke-virtual {v0, p1}, Lcom/android/fmradio/TunerManagerForExt;->setRdsPtyType(I)V

    return-void
.end method

.method private powerDownFm()V
    .locals 2

    .line 1921
    sget-object v0, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    const-string v1, "---->>powerDownFm() "

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 1922
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mService:Lcom/android/fmradio/FmService;

    if-eqz v0, :cond_0

    .line 1923
    invoke-virtual {v0}, Lcom/android/fmradio/FmService;->powerDownAsync()V

    :cond_0
    return-void
.end method

.method private powerUpFm()V
    .locals 3

    .line 1913
    sget-object v0, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "---->>powerUpFm() - powerStatus: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-object v2, p0, Lcom/android/fmradio/FmMainActivity;->mService:Lcom/android/fmradio/FmService;

    invoke-virtual {v2}, Lcom/android/fmradio/FmService;->getPowerStatus()I

    move-result v2

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 1915
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mService:Lcom/android/fmradio/FmService;

    invoke-virtual {v0}, Lcom/android/fmradio/FmService;->getPowerStatus()I

    move-result v0

    sget v1, Lcom/android/fmradio/FmService;->POWER_UP:I

    if-eq v0, v1, :cond_0

    .line 1916
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mService:Lcom/android/fmradio/FmService;

    iget-object v1, p0, Lcom/android/fmradio/FmMainActivity;->mContext:Landroid/content/Context;

    iget v2, p0, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    invoke-static {v1, v2}, Lcom/android/fmradio/utils/FmUtils;->computeFrequency(Landroid/content/Context;I)F

    move-result v1

    invoke-virtual {v0, v1}, Lcom/android/fmradio/FmService;->powerUpAsync(F)V

    :cond_0
    return-void
.end method

.method private refreshStationUI(I)V
    .locals 1

    const/4 v0, 0x0

    .line 1886
    invoke-virtual {p0, v0, p1}, Lcom/android/fmradio/FmMainActivity;->updateStationValue(ZI)V

    return-void
.end method

.method private registerReceiver()V
    .locals 3

    .line 1445
    new-instance v0, Landroid/content/IntentFilter;

    invoke-direct {v0}, Landroid/content/IntentFilter;-><init>()V

    const-string v1, "ailit.set.radio.frequency"

    .line 1446
    invoke-virtual {v0, v1}, Landroid/content/IntentFilter;->addAction(Ljava/lang/String;)V

    const-string v1, "/customize/radio/close"

    .line 1447
    invoke-virtual {v0, v1}, Landroid/content/IntentFilter;->addAction(Ljava/lang/String;)V

    const-string v1, "/customize/radio/pre"

    .line 1448
    invoke-virtual {v0, v1}, Landroid/content/IntentFilter;->addAction(Ljava/lang/String;)V

    const-string v1, "/customize/radio/next"

    .line 1449
    invoke-virtual {v0, v1}, Landroid/content/IntentFilter;->addAction(Ljava/lang/String;)V

    const-string v1, "/customize/radio/station"

    .line 1450
    invoke-virtual {v0, v1}, Landroid/content/IntentFilter;->addAction(Ljava/lang/String;)V

    const-string v1, "/customize/radio/seek_up"

    .line 1451
    invoke-virtual {v0, v1}, Landroid/content/IntentFilter;->addAction(Ljava/lang/String;)V

    const-string v1, "/customize/radio/seek_down"

    .line 1452
    invoke-virtual {v0, v1}, Landroid/content/IntentFilter;->addAction(Ljava/lang/String;)V

    const-string v1, "/customize/radio/band"

    .line 1453
    invoke-virtual {v0, v1}, Landroid/content/IntentFilter;->addAction(Ljava/lang/String;)V

    const-string v1, "com.qf.action.ACC_OFF"

    .line 1454
    invoke-virtual {v0, v1}, Landroid/content/IntentFilter;->addAction(Ljava/lang/String;)V

    const-string v1, "com.qf.action.update_radio_area"

    .line 1455
    invoke-virtual {v0, v1}, Landroid/content/IntentFilter;->addAction(Ljava/lang/String;)V

    .line 1456
    new-instance v1, Lcom/android/fmradio/FmMainActivity$FmBroadCastReceiver;

    invoke-direct {v1, p0}, Lcom/android/fmradio/FmMainActivity$FmBroadCastReceiver;-><init>(Lcom/android/fmradio/FmMainActivity;)V

    iput-object v1, p0, Lcom/android/fmradio/FmMainActivity;->mFmBroadCastReceiver:Lcom/android/fmradio/FmMainActivity$FmBroadCastReceiver;

    .line 1457
    iget-object v1, p0, Lcom/android/fmradio/FmMainActivity;->mFmBroadCastReceiver:Lcom/android/fmradio/FmMainActivity$FmBroadCastReceiver;

    invoke-virtual {p0, v1, v0}, Lcom/android/fmradio/FmMainActivity;->registerReceiver(Landroid/content/BroadcastReceiver;Landroid/content/IntentFilter;)Landroid/content/Intent;

    .line 1459
    new-instance v0, Landroid/content/IntentFilter;

    invoke-direct {v0}, Landroid/content/IntentFilter;-><init>()V

    const-string v1, "com.android.fmradio.favorite_changed"

    .line 1460
    invoke-virtual {v0, v1}, Landroid/content/IntentFilter;->addAction(Ljava/lang/String;)V

    .line 1461
    iget-object v1, p0, Lcom/android/fmradio/FmMainActivity;->mContext:Landroid/content/Context;

    invoke-static {v1}, Landroidx/localbroadcastmanager/content/LocalBroadcastManager;->getInstance(Landroid/content/Context;)Landroidx/localbroadcastmanager/content/LocalBroadcastManager;

    move-result-object v1

    iget-object v2, p0, Lcom/android/fmradio/FmMainActivity;->mLocalReceiver:Landroid/content/BroadcastReceiver;

    invoke-virtual {v1, v2, v0}, Landroidx/localbroadcastmanager/content/LocalBroadcastManager;->registerReceiver(Landroid/content/BroadcastReceiver;Landroid/content/IntentFilter;)V

    return-void
.end method

.method private resetBand()V
    .locals 2

    .line 1260
    invoke-static {p0}, Lcom/android/fmradio/database/FmStation;->getCurrentBand(Landroid/content/Context;)I

    move-result v0

    invoke-static {v0}, Lcom/android/fmradio/utils/FmUtils;->isFMBand(I)Z

    move-result v0

    const/4 v1, 0x3

    if-eqz v0, :cond_1

    .line 1262
    iget v0, p0, Lcom/android/fmradio/FmMainActivity;->mRadioArea:I

    if-ne v0, v1, :cond_0

    iget v0, p0, Lcom/android/fmradio/FmMainActivity;->mBand:I

    if-eqz v0, :cond_0

    const/4 v0, 0x1

    .line 1263
    iput v0, p0, Lcom/android/fmradio/FmMainActivity;->mBand:I

    goto :goto_0

    :cond_0
    const/4 v0, 0x0

    .line 1265
    iput v0, p0, Lcom/android/fmradio/FmMainActivity;->mBand:I

    goto :goto_0

    .line 1271
    :cond_1
    iput v1, p0, Lcom/android/fmradio/FmMainActivity;->mBand:I

    :goto_0
    return-void
.end method

.method private resetRadioData()V
    .locals 5

    .line 855
    sget-object v0, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    const-string v1, "resetRadioData - start"

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    const/4 v0, 0x0

    .line 856
    invoke-direct {p0, v0}, Lcom/android/fmradio/FmMainActivity;->setBand(I)V

    .line 857
    iget-object v1, p0, Lcom/android/fmradio/FmMainActivity;->mContext:Landroid/content/Context;

    invoke-static {v1, v0}, Lcom/android/fmradio/database/FmStation;->setCurrentBand(Landroid/content/Context;I)V

    .line 859
    invoke-static {}, Lcom/android/fmradio/utils/FmUtils;->getRadioArea()I

    move-result v1

    iput v1, p0, Lcom/android/fmradio/FmMainActivity;->mRadioArea:I

    .line 860
    iget-object v1, p0, Lcom/android/fmradio/FmMainActivity;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    if-eqz v1, :cond_0

    .line 861
    iget v2, p0, Lcom/android/fmradio/FmMainActivity;->mRadioArea:I

    invoke-virtual {v1, v2}, Lcom/android/fmradio/TunerManagerForExt;->onRadioArea(I)V

    .line 863
    :cond_0
    iget v1, p0, Lcom/android/fmradio/FmMainActivity;->mRadioArea:I

    const-string v2, "com.qf.radio.action.area_change"

    invoke-static {p0, v2, v1}, Lcom/android/fmradio/utils/RadioConfigData;->setRadioArea(Landroid/content/Context;Ljava/lang/String;I)V

    .line 865
    iget v1, p0, Lcom/android/fmradio/FmMainActivity;->mRadioArea:I

    iget v2, p0, Lcom/android/fmradio/FmMainActivity;->mBand:I

    invoke-static {v1, v2}, Lcom/android/fmradio/utils/FmUtils;->getRadioAreaFMMinFreq(II)I

    move-result v1

    iput v1, p0, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    .line 866
    iget-object v1, p0, Lcom/android/fmradio/FmMainActivity;->mContext:Landroid/content/Context;

    iget v2, p0, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    invoke-static {v1, v2}, Lcom/android/fmradio/database/FmStation;->setCurrentStation(Landroid/content/Context;I)V

    .line 868
    invoke-direct {p0}, Lcom/android/fmradio/FmMainActivity;->initFreq()V

    .line 870
    iget-object v1, p0, Lcom/android/fmradio/FmMainActivity;->mRadioFreqSliderView:Lcom/android/fmradio/views/RadioFreqSliderView;

    if-eqz v1, :cond_1

    .line 871
    invoke-virtual {v1}, Lcom/android/fmradio/views/RadioFreqSliderView;->setSlider()V

    .line 874
    :cond_1
    iget-object v1, p0, Lcom/android/fmradio/FmMainActivity;->mBandInfoMap:Ljava/util/HashMap;

    invoke-virtual {v1}, Ljava/util/HashMap;->clear()V

    move v1, v0

    :goto_0
    const/4 v2, 0x5

    if-ge v1, v2, :cond_3

    if-ltz v1, :cond_2

    const/4 v2, 0x2

    if-gt v1, v2, :cond_2

    .line 878
    iget v2, p0, Lcom/android/fmradio/FmMainActivity;->mRadioArea:I

    invoke-static {v2, v1}, Lcom/android/fmradio/utils/FmUtils;->getRadioAreaFMMinFreq(II)I

    move-result v2

    goto :goto_1

    .line 880
    :cond_2
    iget v2, p0, Lcom/android/fmradio/FmMainActivity;->mRadioArea:I

    invoke-static {v2}, Lcom/android/fmradio/utils/FmUtils;->getRadioAreaAMMinFreq(I)I

    move-result v2

    .line 882
    :goto_1
    sget-object v3, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    const-string v4, "resetRadioData - setBandInfo"

    invoke-static {v3, v4}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 883
    invoke-direct {p0, v1, v0, v2}, Lcom/android/fmradio/FmMainActivity;->setBandInfo(III)V

    add-int/lit8 v1, v1, 0x1

    goto :goto_0

    :cond_3
    const/4 v1, 0x1

    .line 886
    invoke-direct {p0, v1}, Lcom/android/fmradio/FmMainActivity;->resetStationList(Z)V

    .line 887
    invoke-direct {p0, v0}, Lcom/android/fmradio/FmMainActivity;->resetStationList(Z)V

    .line 889
    invoke-direct {p0}, Lcom/android/fmradio/FmMainActivity;->clearButtonText()V

    .line 891
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mContext:Landroid/content/Context;

    invoke-static {v0}, Lcom/android/fmradio/database/FmStation;->cleanAllStations(Landroid/content/Context;)V

    return-void
.end method

.method private resetStationList(Z)V
    .locals 3

    const/4 v0, 0x0

    if-eqz p1, :cond_0

    move p1, v0

    :goto_0
    const/16 v1, 0x12

    if-ge p1, v1, :cond_1

    .line 1227
    new-instance v1, Lcom/android/fmradio/info/FmFreqInfo;

    invoke-direct {v1}, Lcom/android/fmradio/info/FmFreqInfo;-><init>()V

    .line 1228
    invoke-virtual {v1, v0}, Lcom/android/fmradio/info/FmFreqInfo;->setBand(I)V

    .line 1229
    iget-object v2, p0, Lcom/android/fmradio/FmMainActivity;->mFMStationList:[Lcom/android/fmradio/info/FmFreqInfo;

    aput-object v1, v2, p1

    add-int/lit8 p1, p1, 0x1

    goto :goto_0

    :cond_0
    :goto_1
    const/16 p1, 0xc

    if-ge v0, p1, :cond_1

    .line 1233
    new-instance p1, Lcom/android/fmradio/info/FmFreqInfo;

    invoke-direct {p1}, Lcom/android/fmradio/info/FmFreqInfo;-><init>()V

    const/4 v1, 0x1

    .line 1234
    invoke-virtual {p1, v1}, Lcom/android/fmradio/info/FmFreqInfo;->setBand(I)V

    .line 1235
    iget-object v1, p0, Lcom/android/fmradio/FmMainActivity;->mAMStationList:[Lcom/android/fmradio/info/FmFreqInfo;

    aput-object p1, v1, v0

    add-int/lit8 v0, v0, 0x1

    goto :goto_1

    :cond_1
    return-void
.end method

.method private sendFmInfo()V
    .locals 4

    .line 1893
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mContext:Landroid/content/Context;

    iget v1, p0, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/AllStationInfoUtil;->getStationName(Landroid/content/Context;I)Ljava/lang/String;

    move-result-object v0

    .line 1894
    sget-object v1, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2}, Ljava/lang/StringBuilder;-><init>()V

    const-string v3, "sendFmInfo - mCurrentStation "

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget v3, p0, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v3, " - mBand: "

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget v3, p0, Lcom/android/fmradio/FmMainActivity;->mBand:I

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v3, " - stationName: "

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v2

    invoke-static {v1, v2}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 1896
    new-instance v1, Landroid/content/Intent;

    const-string v2, "com.qf.radio.update_action"

    invoke-direct {v1, v2}, Landroid/content/Intent;-><init>(Ljava/lang/String;)V

    .line 1897
    iget-object v2, p0, Lcom/android/fmradio/FmMainActivity;->mContext:Landroid/content/Context;

    iget v3, p0, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    invoke-static {v2, v3}, Lcom/android/fmradio/utils/FmUtils;->formatStation(Landroid/content/Context;I)Ljava/lang/String;

    move-result-object v2

    const-string v3, "com.qf.radio.update_action_key"

    invoke-virtual {v1, v3, v2}, Landroid/content/Intent;->putExtra(Ljava/lang/String;Ljava/lang/String;)Landroid/content/Intent;

    .line 1898
    iget v2, p0, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    const-string v3, "com.qf.radio.update_action_freq_key"

    invoke-virtual {v1, v3, v2}, Landroid/content/Intent;->putExtra(Ljava/lang/String;I)Landroid/content/Intent;

    .line 1899
    iget v2, p0, Lcom/android/fmradio/FmMainActivity;->mBand:I

    const-string v3, "com.qf.radio.update_action_band_key"

    invoke-virtual {v1, v3, v2}, Landroid/content/Intent;->putExtra(Ljava/lang/String;I)Landroid/content/Intent;

    .line 1900
    iget v2, p0, Lcom/android/fmradio/FmMainActivity;->mPresetIndex:I

    const-string v3, "com.qf.radio.update_action_preset_key"

    invoke-virtual {v1, v3, v2}, Landroid/content/Intent;->putExtra(Ljava/lang/String;I)Landroid/content/Intent;

    .line 1901
    invoke-virtual {p0}, Lcom/android/fmradio/FmMainActivity;->isSearching()Z

    move-result v2

    const-string v3, "com.qf.radio.update_action_searching_key"

    invoke-virtual {v1, v3, v2}, Landroid/content/Intent;->putExtra(Ljava/lang/String;Z)Landroid/content/Intent;

    const-string v2, "com.qf.radio.update_action_name_key"

    .line 1902
    invoke-virtual {v1, v2, v0}, Landroid/content/Intent;->putExtra(Ljava/lang/String;Ljava/lang/String;)Landroid/content/Intent;

    .line 1903
    sget-object v0, Landroid/os/UserHandle;->ALL:Landroid/os/UserHandle;

    invoke-virtual {p0, v1, v0}, Lcom/android/fmradio/FmMainActivity;->sendBroadcastAsUser(Landroid/content/Intent;Landroid/os/UserHandle;)V

    return-void
.end method

.method private setBackground()V
    .locals 9

    .line 752
    invoke-virtual {p0}, Lcom/android/fmradio/FmMainActivity;->getResources()Landroid/content/res/Resources;

    move-result-object v0

    invoke-virtual {v0}, Landroid/content/res/Resources;->getConfiguration()Landroid/content/res/Configuration;

    move-result-object v0

    iget v0, v0, Landroid/content/res/Configuration;->uiMode:I

    .line 753
    invoke-static {}, Lcom/android/fmradio/utils/FmUtils;->getAppBgStyle()I

    move-result v1

    .line 754
    invoke-virtual {p0}, Lcom/android/fmradio/FmMainActivity;->getResources()Landroid/content/res/Resources;

    move-result-object v2

    invoke-virtual {v2}, Landroid/content/res/Resources;->getConfiguration()Landroid/content/res/Configuration;

    move-result-object v2

    iget v2, v2, Landroid/content/res/Configuration;->orientation:I

    .line 755
    sget-object v3, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    new-instance v4, Ljava/lang/StringBuilder;

    invoke-direct {v4}, Ljava/lang/StringBuilder;-><init>()V

    const-string v5, "mPreIsInMultiWindowMode: "

    invoke-virtual {v4, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-boolean v5, p0, Lcom/android/fmradio/FmMainActivity;->mPreIsInMultiWindowMode:Z

    invoke-virtual {v4, v5}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    const-string v5, " - uiMode: "

    invoke-virtual {v4, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v4, v0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v5, " - appBgStyle: "

    invoke-virtual {v4, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v4, v1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v5, " - orientation: "

    invoke-virtual {v4, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v4, v2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v4}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v4

    invoke-static {v3, v4}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 760
    iget-boolean v3, p0, Lcom/android/fmradio/FmMainActivity;->mPreIsInMultiWindowMode:Z

    const-string v4, "jitu2"

    const v5, 0x7f080083

    const v6, 0x7f0700c4

    const/16 v7, 0x20

    const v8, 0x7f05001a

    if-eqz v3, :cond_2

    .line 761
    invoke-virtual {p0, v5}, Lcom/android/fmradio/FmMainActivity;->findViewById(I)Landroid/view/View;

    move-result-object v1

    and-int/2addr v0, v7

    if-ne v0, v7, :cond_1

    .line 763
    invoke-static {v4}, Landroid/qf/os/QFApi;->isClientProduct(Ljava/lang/String;)Z

    move-result v0

    if-eqz v0, :cond_0

    .line 764
    invoke-virtual {v1, v6}, Landroid/view/View;->setBackgroundResource(I)V

    goto :goto_0

    .line 766
    :cond_0
    invoke-virtual {p0, v8}, Lcom/android/fmradio/FmMainActivity;->getColor(I)I

    move-result v0

    invoke-virtual {v1, v0}, Landroid/view/View;->setBackgroundColor(I)V

    goto :goto_0

    .line 769
    :cond_1
    invoke-virtual {p0, v8}, Lcom/android/fmradio/FmMainActivity;->getColor(I)I

    move-result v0

    invoke-virtual {v1, v0}, Landroid/view/View;->setBackgroundColor(I)V

    goto :goto_0

    .line 772
    :cond_2
    invoke-virtual {p0, v5}, Lcom/android/fmradio/FmMainActivity;->findViewById(I)Landroid/view/View;

    move-result-object v3

    and-int/2addr v0, v7

    if-ne v0, v7, :cond_5

    const/4 v0, 0x1

    if-ne v1, v0, :cond_4

    if-ne v2, v0, :cond_3

    .line 776
    invoke-virtual {p0, v8}, Lcom/android/fmradio/FmMainActivity;->getColor(I)I

    move-result v0

    invoke-virtual {v3, v0}, Landroid/view/View;->setBackgroundColor(I)V

    goto :goto_0

    .line 778
    :cond_3
    invoke-virtual {v3, v6}, Landroid/view/View;->setBackgroundResource(I)V

    goto :goto_0

    :cond_4
    const/4 v0, 0x2

    if-ne v1, v0, :cond_7

    .line 781
    invoke-virtual {p0, v8}, Lcom/android/fmradio/FmMainActivity;->getColor(I)I

    move-result v0

    invoke-virtual {v3, v0}, Landroid/view/View;->setBackgroundColor(I)V

    goto :goto_0

    .line 784
    :cond_5
    invoke-static {v4}, Landroid/qf/os/QFApi;->isClientProduct(Ljava/lang/String;)Z

    move-result v0

    if-eqz v0, :cond_6

    .line 785
    invoke-virtual {v3, v6}, Landroid/view/View;->setBackgroundResource(I)V

    goto :goto_0

    .line 787
    :cond_6
    invoke-virtual {p0, v8}, Lcom/android/fmradio/FmMainActivity;->getColor(I)I

    move-result v0

    invoke-virtual {v3, v0}, Landroid/view/View;->setBackgroundColor(I)V

    :cond_7
    :goto_0
    return-void
.end method

.method private setBand(I)V
    .locals 1

    .line 1253
    iput p1, p0, Lcom/android/fmradio/FmMainActivity;->mBand:I

    .line 1254
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    if-eqz p1, :cond_0

    .line 1255
    iget v0, p0, Lcom/android/fmradio/FmMainActivity;->mBand:I

    int-to-byte v0, v0

    invoke-virtual {p1, v0}, Lcom/android/fmradio/TunerManagerForExt;->onBand(B)V

    :cond_0
    return-void
.end method

.method private setBandInfo(III)V
    .locals 4

    .line 1167
    new-instance v0, Lcom/android/fmradio/info/FmBandInfo;

    invoke-direct {v0}, Lcom/android/fmradio/info/FmBandInfo;-><init>()V

    .line 1168
    invoke-virtual {v0, p1}, Lcom/android/fmradio/info/FmBandInfo;->setBand(I)V

    .line 1169
    invoke-virtual {v0, p2}, Lcom/android/fmradio/info/FmBandInfo;->setBandPreset(I)V

    .line 1170
    invoke-virtual {v0, p3}, Lcom/android/fmradio/info/FmBandInfo;->setBandFreq(I)V

    .line 1172
    sget-object v1, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2}, Ljava/lang/StringBuilder;-><init>()V

    const-string v3, "setBandInfo - band: "

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v3, " - bandPreset: "

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2, p2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v3, " - bandFreq: "

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2, p3}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v2

    invoke-static {v1, v2}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 1175
    iget-object v1, p0, Lcom/android/fmradio/FmMainActivity;->mBandInfoMap:Ljava/util/HashMap;

    invoke-static {p1}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v2

    invoke-virtual {v1, v2, v0}, Ljava/util/HashMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

    .line 1177
    sget-object v0, Lcom/android/fmradio/FmMainActivity;->BAND_STR:[Ljava/lang/String;

    aget-object v0, v0, p1

    invoke-static {p0, v0, p1}, Lcom/android/fmradio/utils/RadioConfigData;->setFMBand(Landroid/content/Context;Ljava/lang/String;I)V

    .line 1178
    sget-object v0, Lcom/android/fmradio/FmMainActivity;->BAND_PRESET_STR:[Ljava/lang/String;

    aget-object v0, v0, p1

    invoke-static {p0, v0, p2}, Lcom/android/fmradio/utils/RadioConfigData;->setFMBandPreset(Landroid/content/Context;Ljava/lang/String;I)V

    .line 1179
    sget-object p2, Lcom/android/fmradio/FmMainActivity;->BAND_FREQ_STR:[Ljava/lang/String;

    aget-object p1, p2, p1

    invoke-static {p0, p1, p3}, Lcom/android/fmradio/utils/RadioConfigData;->setFMBandFreq(Landroid/content/Context;Ljava/lang/String;I)V

    return-void
.end method

.method private setStatusBarFullTransparent()V
    .locals 5

    .line 645
    invoke-virtual {p0}, Lcom/android/fmradio/FmMainActivity;->getWindow()Landroid/view/Window;

    move-result-object v0

    const/high16 v1, 0x4000000

    .line 646
    invoke-virtual {v0, v1}, Landroid/view/Window;->clearFlags(I)V

    .line 647
    invoke-virtual {p0}, Lcom/android/fmradio/FmMainActivity;->getResources()Landroid/content/res/Resources;

    move-result-object v1

    invoke-virtual {v1}, Landroid/content/res/Resources;->getConfiguration()Landroid/content/res/Configuration;

    move-result-object v1

    iget v1, v1, Landroid/content/res/Configuration;->uiMode:I

    .line 648
    sget-object v2, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    new-instance v3, Ljava/lang/StringBuilder;

    invoke-direct {v3}, Ljava/lang/StringBuilder;-><init>()V

    const-string v4, "uiMode: "

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v3, v1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v3}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v3

    invoke-static {v2, v3}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    const/16 v2, 0x20

    and-int/2addr v1, v2

    if-ne v1, v2, :cond_0

    .line 650
    invoke-virtual {v0}, Landroid/view/Window;->getDecorView()Landroid/view/View;

    move-result-object v1

    const/16 v2, 0x500

    invoke-virtual {v1, v2}, Landroid/view/View;->setSystemUiVisibility(I)V

    goto :goto_0

    .line 653
    :cond_0
    invoke-virtual {v0}, Landroid/view/Window;->getDecorView()Landroid/view/View;

    move-result-object v1

    const/16 v2, 0x2500

    invoke-virtual {v1, v2}, Landroid/view/View;->setSystemUiVisibility(I)V

    :goto_0
    const/high16 v1, -0x80000000

    .line 657
    invoke-virtual {v0, v1}, Landroid/view/Window;->addFlags(I)V

    const/4 v1, 0x0

    .line 658
    invoke-virtual {v0, v1}, Landroid/view/Window;->setStatusBarColor(I)V

    return-void
.end method

.method private showToast(Ljava/lang/CharSequence;)V
    .locals 2

    .line 1945
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mToast:Landroid/widget/Toast;

    if-nez v0, :cond_0

    .line 1946
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mContext:Landroid/content/Context;

    const/4 v1, 0x0

    invoke-static {v0, p1, v1}, Landroid/widget/Toast;->makeText(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;

    move-result-object v0

    iput-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mToast:Landroid/widget/Toast;

    .line 1948
    :cond_0
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mToast:Landroid/widget/Toast;

    invoke-virtual {v0, p1}, Landroid/widget/Toast;->setText(Ljava/lang/CharSequence;)V

    .line 1949
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity;->mToast:Landroid/widget/Toast;

    invoke-virtual {p1}, Landroid/widget/Toast;->show()V

    return-void
.end method

.method private tuneStation(IZ)V
    .locals 3

    .line 1936
    sget-object v0, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "tuneStation, station: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v2, " needNotifyInfo: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p2}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 1937
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mService:Lcom/android/fmradio/FmService;

    if-nez v0, :cond_0

    .line 1938
    sget-object p1, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    const-string p2, "tuneStation, mService is null"

    invoke-static {p1, p2}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    return-void

    .line 1941
    :cond_0
    iget-object v1, p0, Lcom/android/fmradio/FmMainActivity;->mContext:Landroid/content/Context;

    invoke-static {v1, p1}, Lcom/android/fmradio/utils/FmUtils;->computeFrequency(Landroid/content/Context;I)F

    move-result p1

    invoke-virtual {v0, p1, p2}, Lcom/android/fmradio/FmService;->tuneStationAsync(FZ)V

    return-void
.end method

.method private uninitUtilEventManager()V
    .locals 2

    .line 1438
    :try_start_0
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mKeyEventService:Landroid/qf/util/UtilEventManager;

    iget-object v1, p0, Lcom/android/fmradio/FmMainActivity;->mKeyListener:Landroid/qf/util/UtilEventListener;

    invoke-virtual {v0, v1}, Landroid/qf/util/UtilEventManager;->RPC_RemoveListener(Landroid/qf/util/UtilEventListener;)V
    :try_end_0
    .catch Landroid/os/RemoteException; {:try_start_0 .. :try_end_0} :catch_0

    goto :goto_0

    :catch_0
    move-exception v0

    .line 1440
    invoke-virtual {v0}, Landroid/os/RemoteException;->printStackTrace()V

    :goto_0
    return-void
.end method

.method private unregisterReceiver()V
    .locals 2

    .line 1465
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mFmBroadCastReceiver:Lcom/android/fmradio/FmMainActivity$FmBroadCastReceiver;

    if-eqz v0, :cond_0

    .line 1466
    invoke-virtual {p0, v0}, Lcom/android/fmradio/FmMainActivity;->unregisterReceiver(Landroid/content/BroadcastReceiver;)V

    .line 1469
    :cond_0
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mLocalReceiver:Landroid/content/BroadcastReceiver;

    if-eqz v0, :cond_1

    .line 1470
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mContext:Landroid/content/Context;

    invoke-static {v0}, Landroidx/localbroadcastmanager/content/LocalBroadcastManager;->getInstance(Landroid/content/Context;)Landroidx/localbroadcastmanager/content/LocalBroadcastManager;

    move-result-object v0

    iget-object v1, p0, Lcom/android/fmradio/FmMainActivity;->mLocalReceiver:Landroid/content/BroadcastReceiver;

    invoke-virtual {v0, v1}, Landroidx/localbroadcastmanager/content/LocalBroadcastManager;->unregisterReceiver(Landroid/content/BroadcastReceiver;)V

    :cond_1
    return-void
.end method

.method private updateCurrentStation()V
    .locals 4

    .line 1978
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    invoke-virtual {v0}, Lcom/android/fmradio/TunerManagerForExt;->getFrequency()I

    move-result v0

    .line 1979
    sget-object v1, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2}, Ljava/lang/StringBuilder;-><init>()V

    const-string v3, "mCurrentStation: "

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget v3, p0, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v3, " - freq: "

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2, v0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v2

    invoke-static {v1, v2}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 1980
    iget-object v1, p0, Lcom/android/fmradio/FmMainActivity;->mContext:Landroid/content/Context;

    invoke-static {v1, v0}, Lcom/android/fmradio/utils/FmUtils;->isValidStation(Landroid/content/Context;I)Z

    move-result v1

    if-eqz v1, :cond_0

    .line 1981
    iget v1, p0, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    if-eq v1, v0, :cond_0

    .line 1982
    iput v0, p0, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    .line 1983
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mContext:Landroid/content/Context;

    iget v1, p0, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    invoke-static {v0, v1}, Lcom/android/fmradio/database/FmStation;->setCurrentStation(Landroid/content/Context;I)V

    .line 1984
    iget v0, p0, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    invoke-direct {p0, v0}, Lcom/android/fmradio/FmMainActivity;->refreshStationUI(I)V

    :cond_0
    return-void
.end method

.method private updateRadioArea()V
    .locals 4

    .line 898
    invoke-static {}, Lcom/android/fmradio/utils/FmUtils;->getRadioArea()I

    move-result v0

    iput v0, p0, Lcom/android/fmradio/FmMainActivity;->mRadioArea:I

    .line 899
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    if-eqz v0, :cond_0

    .line 900
    iget v1, p0, Lcom/android/fmradio/FmMainActivity;->mRadioArea:I

    invoke-virtual {v0, v1}, Lcom/android/fmradio/TunerManagerForExt;->onRadioArea(I)V

    :cond_0
    const-string v0, "com.qf.radio.action.area_change"

    .line 903
    invoke-static {p0, v0}, Lcom/android/fmradio/utils/RadioConfigData;->getRadioArea(Landroid/content/Context;Ljava/lang/String;)I

    move-result v0

    .line 904
    sget-object v1, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2}, Ljava/lang/StringBuilder;-><init>()V

    const-string v3, "updateRadioArea - new_radioArea: "

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget v3, p0, Lcom/android/fmradio/FmMainActivity;->mRadioArea:I

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v3, " - old_radioArea: "

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2, v0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v2

    invoke-static {v1, v2}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 906
    iget v1, p0, Lcom/android/fmradio/FmMainActivity;->mRadioArea:I

    if-eq v1, v0, :cond_1

    .line 907
    invoke-direct {p0}, Lcom/android/fmradio/FmMainActivity;->resetRadioData()V

    :cond_1
    return-void
.end method

.method private updateStationList(IIZ)V
    .locals 1

    .line 1190
    iget v0, p0, Lcom/android/fmradio/FmMainActivity;->mBand:I

    invoke-static {v0}, Lcom/android/fmradio/utils/FmUtils;->isFMBand(I)Z

    move-result v0

    if-eqz v0, :cond_1

    const/4 v0, 0x0

    if-eqz p3, :cond_0

    .line 1192
    iget-object p3, p0, Lcom/android/fmradio/FmMainActivity;->mContext:Landroid/content/Context;

    invoke-static {p3, v0}, Lcom/android/fmradio/database/FmStation;->cleanSearchedStations(Landroid/content/Context;I)V

    .line 1195
    :cond_0
    iget p3, p0, Lcom/android/fmradio/FmMainActivity;->mBand:I

    mul-int/lit8 p3, p3, 0x6

    add-int/2addr p3, p1

    .line 1197
    new-instance p1, Lcom/android/fmradio/info/FmFreqInfo;

    invoke-direct {p1}, Lcom/android/fmradio/info/FmFreqInfo;-><init>()V

    .line 1198
    invoke-virtual {p1, v0}, Lcom/android/fmradio/info/FmFreqInfo;->setBand(I)V

    .line 1199
    invoke-virtual {p1, p3}, Lcom/android/fmradio/info/FmFreqInfo;->setFreqIndex(I)V

    .line 1200
    invoke-virtual {p1, p2}, Lcom/android/fmradio/info/FmFreqInfo;->setFreq(I)V

    .line 1202
    iget-object p2, p0, Lcom/android/fmradio/FmMainActivity;->mFMStationList:[Lcom/android/fmradio/info/FmFreqInfo;

    aput-object p1, p2, p3

    goto :goto_0

    :cond_1
    const/4 v0, 0x1

    if-eqz p3, :cond_2

    .line 1205
    iget-object p3, p0, Lcom/android/fmradio/FmMainActivity;->mContext:Landroid/content/Context;

    invoke-static {p3, v0}, Lcom/android/fmradio/database/FmStation;->cleanSearchedStations(Landroid/content/Context;I)V

    .line 1208
    :cond_2
    iget p3, p0, Lcom/android/fmradio/FmMainActivity;->mBand:I

    add-int/lit8 p3, p3, -0x3

    mul-int/lit8 p3, p3, 0x6

    add-int/2addr p3, p1

    .line 1210
    new-instance p1, Lcom/android/fmradio/info/FmFreqInfo;

    invoke-direct {p1}, Lcom/android/fmradio/info/FmFreqInfo;-><init>()V

    .line 1211
    invoke-virtual {p1, v0}, Lcom/android/fmradio/info/FmFreqInfo;->setBand(I)V

    .line 1212
    invoke-virtual {p1, p3}, Lcom/android/fmradio/info/FmFreqInfo;->setFreqIndex(I)V

    .line 1213
    invoke-virtual {p1, p2}, Lcom/android/fmradio/info/FmFreqInfo;->setFreq(I)V

    .line 1215
    iget-object p2, p0, Lcom/android/fmradio/FmMainActivity;->mAMStationList:[Lcom/android/fmradio/info/FmFreqInfo;

    aput-object p1, p2, p3

    :goto_0
    return-void
.end method

.method private updateStationList(Z)V
    .locals 21

    move-object/from16 v0, p0

    move/from16 v1, p1

    .line 947
    sget-object v2, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    new-instance v3, Ljava/lang/StringBuilder;

    invoke-direct {v3}, Ljava/lang/StringBuilder;-><init>()V

    const-string v4, "start - isFMBand: "

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v3, v1}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    const-string v4, " - mBand: "

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget v4, v0, Lcom/android/fmradio/FmMainActivity;->mBand:I

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v4, " - mRadioArea: "

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget v4, v0, Lcom/android/fmradio/FmMainActivity;->mRadioArea:I

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v3}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v3

    invoke-static {v2, v3}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    xor-int/lit8 v2, v1, 0x1

    .line 950
    new-instance v3, Ljava/util/ArrayList;

    invoke-direct {v3}, Ljava/util/ArrayList;-><init>()V

    const/16 v4, 0x2e

    new-array v5, v4, [B

    and-int/lit8 v6, v2, 0x1

    const/4 v7, 0x3

    shl-int/2addr v6, v7

    .line 961
    iget v8, v0, Lcom/android/fmradio/FmMainActivity;->mBand:I

    const/4 v9, 0x7

    and-int/2addr v8, v9

    or-int/2addr v6, v8

    int-to-byte v6, v6

    const/4 v8, 0x0

    aput-byte v6, v5, v8

    move v6, v8

    :goto_0
    const/4 v10, 0x5

    const/4 v11, 0x2

    const/4 v12, 0x6

    const/4 v13, 0x1

    if-ge v6, v10, :cond_3

    const-string v10, " - bandPreset: "

    const-string v14, "updateStationList - band: "

    if-eqz v1, :cond_1

    if-lt v6, v7, :cond_0

    goto/16 :goto_1

    .line 970
    :cond_0
    iget-object v15, v0, Lcom/android/fmradio/FmMainActivity;->mBandInfoMap:Ljava/util/HashMap;

    invoke-static {v6}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v9

    invoke-virtual {v15, v9}, Ljava/util/HashMap;->get(Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v9

    check-cast v9, Lcom/android/fmradio/info/FmBandInfo;

    .line 971
    invoke-virtual {v9}, Lcom/android/fmradio/info/FmBandInfo;->getBandFreq()I

    move-result v15

    invoke-static {v15, v11}, Lcom/android/fmradio/utils/FmUtils;->int2Bytes(II)[B

    move-result-object v15

    mul-int/lit8 v17, v6, 0x2

    add-int/lit8 v18, v17, 0x1

    .line 972
    aget-byte v13, v15, v13

    aput-byte v13, v5, v18

    add-int/lit8 v17, v17, 0x2

    .line 973
    aget-byte v11, v15, v8

    aput-byte v11, v5, v17

    .line 975
    sget-object v11, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    new-instance v13, Ljava/lang/StringBuilder;

    invoke-direct {v13}, Ljava/lang/StringBuilder;-><init>()V

    invoke-virtual {v13, v14}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v13, v6}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v13, v10}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v9}, Lcom/android/fmradio/info/FmBandInfo;->getBandPreset()I

    move-result v10

    invoke-virtual {v13, v10}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v13}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v10

    invoke-static {v11, v10}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    add-int/lit8 v10, v6, 0x1

    add-int/2addr v10, v12

    .line 976
    invoke-virtual {v9}, Lcom/android/fmradio/info/FmBandInfo;->getBandPreset()I

    move-result v9

    and-int/lit16 v9, v9, 0xff

    int-to-byte v9, v9

    aput-byte v9, v5, v10

    goto :goto_1

    :cond_1
    if-ge v6, v7, :cond_2

    goto :goto_1

    .line 982
    :cond_2
    iget-object v9, v0, Lcom/android/fmradio/FmMainActivity;->mBandInfoMap:Ljava/util/HashMap;

    invoke-static {v6}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v15

    invoke-virtual {v9, v15}, Ljava/util/HashMap;->get(Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v9

    check-cast v9, Lcom/android/fmradio/info/FmBandInfo;

    .line 983
    invoke-virtual {v9}, Lcom/android/fmradio/info/FmBandInfo;->getBandFreq()I

    move-result v15

    invoke-static {v15, v11}, Lcom/android/fmradio/utils/FmUtils;->int2Bytes(II)[B

    move-result-object v15

    add-int/lit8 v17, v6, -0x3

    mul-int/lit8 v18, v17, 0x2

    add-int/lit8 v19, v18, 0x1

    .line 985
    aget-byte v20, v15, v13

    aput-byte v20, v5, v19

    add-int/lit8 v18, v18, 0x2

    .line 986
    aget-byte v11, v15, v8

    aput-byte v11, v5, v18

    .line 988
    sget-object v11, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    new-instance v15, Ljava/lang/StringBuilder;

    invoke-direct {v15}, Ljava/lang/StringBuilder;-><init>()V

    invoke-virtual {v15, v14}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v15, v6}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v15, v10}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v9}, Lcom/android/fmradio/info/FmBandInfo;->getBandPreset()I

    move-result v10

    invoke-virtual {v15, v10}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v15}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v10

    invoke-static {v11, v10}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    add-int/lit8 v17, v17, 0x1

    add-int/lit8 v17, v17, 0x6

    .line 989
    invoke-virtual {v9}, Lcom/android/fmradio/info/FmBandInfo;->getBandPreset()I

    move-result v9

    and-int/lit16 v9, v9, 0xff

    int-to-byte v9, v9

    aput-byte v9, v5, v17

    :goto_1
    add-int/lit8 v6, v6, 0x1

    const/4 v9, 0x7

    goto/16 :goto_0

    .line 996
    :cond_3
    iget-object v6, v0, Lcom/android/fmradio/FmMainActivity;->mContext:Landroid/content/Context;

    invoke-static {v6, v2}, Lcom/android/fmradio/database/FmStation;->queryStations(Landroid/content/Context;I)Landroid/database/Cursor;

    move-result-object v6

    if-eqz v6, :cond_6

    .line 998
    sget-object v14, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    new-instance v15, Ljava/lang/StringBuilder;

    invoke-direct {v15}, Ljava/lang/StringBuilder;-><init>()V

    const-string v9, "updateStationList - cursor.getCount: "

    invoke-virtual {v15, v9}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-interface {v6}, Landroid/database/Cursor;->getCount()I

    move-result v9

    invoke-virtual {v15, v9}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v15}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v9

    invoke-static {v14, v9}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    const/16 v9, 0xa

    .line 1000
    :goto_2
    invoke-interface {v6}, Landroid/database/Cursor;->moveToNext()Z

    move-result v14

    if-eqz v14, :cond_5

    const-string v14, "preset"

    .line 1001
    invoke-interface {v6, v14}, Landroid/database/Cursor;->getColumnIndex(Ljava/lang/String;)I

    move-result v14

    invoke-interface {v6, v14}, Landroid/database/Cursor;->getInt(I)I

    move-result v14

    const-string v15, "frequency"

    .line 1002
    invoke-interface {v6, v15}, Landroid/database/Cursor;->getColumnIndex(Ljava/lang/String;)I

    move-result v15

    invoke-interface {v6, v15}, Landroid/database/Cursor;->getInt(I)I

    move-result v15

    .line 1003
    sget-object v10, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    new-instance v12, Ljava/lang/StringBuilder;

    invoke-direct {v12}, Ljava/lang/StringBuilder;-><init>()V

    const-string v7, "freq: "

    invoke-virtual {v12, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v12, v15}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v7, " - index: "

    invoke-virtual {v12, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v12, v14}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v7, " - presetIndex: "

    invoke-virtual {v12, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v12, v9}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v12}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v7

    invoke-static {v10, v7}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 1004
    new-instance v7, Lcom/android/fmradio/info/FmFreqInfo;

    invoke-direct {v7}, Lcom/android/fmradio/info/FmFreqInfo;-><init>()V

    .line 1005
    invoke-virtual {v7, v2}, Lcom/android/fmradio/info/FmFreqInfo;->setBand(I)V

    .line 1006
    invoke-virtual {v7, v14}, Lcom/android/fmradio/info/FmFreqInfo;->setFreqIndex(I)V

    .line 1007
    invoke-virtual {v7, v15}, Lcom/android/fmradio/info/FmFreqInfo;->setFreq(I)V

    .line 1008
    invoke-virtual {v3, v7}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 1010
    invoke-static {v15, v11}, Lcom/android/fmradio/utils/FmUtils;->int2Bytes(II)[B

    move-result-object v7

    add-int/lit8 v10, v9, 0x1

    .line 1011
    aget-byte v12, v7, v13

    aput-byte v12, v5, v9

    add-int/lit8 v9, v10, 0x1

    .line 1012
    aget-byte v7, v7, v8

    aput-byte v7, v5, v10

    if-ne v9, v4, :cond_4

    goto :goto_3

    :cond_4
    const/4 v7, 0x3

    const/4 v10, 0x5

    const/4 v12, 0x6

    goto :goto_2

    .line 1018
    :cond_5
    :goto_3
    invoke-interface {v6}, Landroid/database/Cursor;->close()V

    .line 1022
    :cond_6
    iget-object v4, v0, Lcom/android/fmradio/FmMainActivity;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    if-eqz v4, :cond_19

    if-eqz v6, :cond_19

    invoke-interface {v6}, Landroid/database/Cursor;->getCount()I

    move-result v4

    if-nez v4, :cond_19

    const/4 v4, 0x4

    if-eqz v1, :cond_10

    move v6, v8

    const/16 v17, 0xa

    :goto_4
    const/16 v7, 0x12

    if-ge v6, v7, :cond_19

    const/16 v7, 0x222e

    .line 1028
    iget v9, v0, Lcom/android/fmradio/FmMainActivity;->mRadioArea:I

    if-nez v9, :cond_7

    .line 1029
    sget-object v7, Lcom/android/fmradio/FmConstants;->RADIO_AREA_USA_FM_DEFAULT_PRESET_LIST:[I

    rem-int/lit8 v9, v6, 0x6

    aget v7, v7, v9

    goto :goto_5

    :cond_7
    if-ne v9, v13, :cond_8

    .line 1031
    sget-object v7, Lcom/android/fmradio/FmConstants;->RADIO_AREA_WESTERN_EUROPE_FM_DEFAULT_PRESET_LIST:[I

    rem-int/lit8 v9, v6, 0x6

    aget v7, v7, v9

    goto :goto_5

    :cond_8
    if-ne v9, v11, :cond_9

    .line 1033
    sget-object v7, Lcom/android/fmradio/FmConstants;->RADIO_AREA_SOUTH_AMERICA_FM_DEFAULT_PRESET_LIST:[I

    rem-int/lit8 v9, v6, 0x6

    aget v7, v7, v9

    goto :goto_5

    :cond_9
    const/4 v10, 0x3

    if-ne v9, v10, :cond_b

    const/4 v10, 0x6

    if-ge v6, v10, :cond_a

    .line 1036
    sget-object v7, Lcom/android/fmradio/FmConstants;->RADIO_AREA_EASTERN_EUROPE_FM1_DEFAULT_PRESET_LIST:[I

    aget v7, v7, v6

    goto :goto_5

    .line 1038
    :cond_a
    sget-object v7, Lcom/android/fmradio/FmConstants;->RADIO_AREA_EASTERN_EUROPE_FM2_DEFAULT_PRESET_LIST:[I

    rem-int/lit8 v9, v6, 0x6

    aget v7, v7, v9

    goto :goto_5

    :cond_b
    if-ne v9, v4, :cond_c

    .line 1041
    sget-object v7, Lcom/android/fmradio/FmConstants;->RADIO_AREA_CHINA_FM_DEFAULT_PRESET_LIST:[I

    rem-int/lit8 v9, v6, 0x6

    aget v7, v7, v9

    goto :goto_5

    :cond_c
    const/4 v10, 0x5

    if-ne v9, v10, :cond_d

    .line 1043
    sget-object v7, Lcom/android/fmradio/FmConstants;->RADIO_AREA_JAPAN_FM_DEFAULT_PRESET_LIST:[I

    rem-int/lit8 v9, v6, 0x6

    aget v7, v7, v9

    goto :goto_5

    :cond_d
    const/4 v10, 0x6

    if-ne v9, v10, :cond_e

    .line 1045
    sget-object v7, Lcom/android/fmradio/FmConstants;->RADIO_AREA_SOUTHEAST_ASIA_FM_DEFAULT_PRESET_LIST:[I

    rem-int/lit8 v9, v6, 0x6

    aget v7, v7, v9

    goto :goto_5

    :cond_e
    const/4 v10, 0x7

    if-ne v9, v10, :cond_f

    .line 1047
    sget-object v7, Lcom/android/fmradio/FmConstants;->RADIO_AREA_SOUTH_AMERICA2_FM_DEFAULT_PRESET_LIST:[I

    rem-int/lit8 v9, v6, 0x6

    aget v7, v7, v9

    .line 1050
    :cond_f
    :goto_5
    new-instance v9, Lcom/android/fmradio/info/FmFreqInfo;

    invoke-direct {v9}, Lcom/android/fmradio/info/FmFreqInfo;-><init>()V

    .line 1051
    invoke-virtual {v9, v2}, Lcom/android/fmradio/info/FmFreqInfo;->setBand(I)V

    .line 1052
    invoke-virtual {v9, v6}, Lcom/android/fmradio/info/FmFreqInfo;->setFreqIndex(I)V

    .line 1053
    invoke-virtual {v9, v7}, Lcom/android/fmradio/info/FmFreqInfo;->setFreq(I)V

    .line 1054
    invoke-virtual {v3, v9}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 1056
    invoke-static {v7, v11}, Lcom/android/fmradio/utils/FmUtils;->int2Bytes(II)[B

    move-result-object v7

    add-int/lit8 v9, v17, 0x1

    .line 1057
    aget-byte v10, v7, v13

    aput-byte v10, v5, v17

    add-int/lit8 v17, v9, 0x1

    .line 1058
    aget-byte v7, v7, v8

    aput-byte v7, v5, v9

    add-int/lit8 v6, v6, 0x1

    goto :goto_4

    :cond_10
    move v6, v8

    const/16 v17, 0xa

    :goto_6
    const/16 v7, 0xc

    if-ge v6, v7, :cond_19

    const/16 v7, 0x213

    .line 1063
    iget v9, v0, Lcom/android/fmradio/FmMainActivity;->mRadioArea:I

    if-nez v9, :cond_11

    .line 1064
    sget-object v7, Lcom/android/fmradio/FmConstants;->RADIO_AREA_USA_AM_DEFAULT_PRESET_LIST:[I

    rem-int/lit8 v9, v6, 0x6

    aget v7, v7, v9

    :goto_7
    const/4 v10, 0x3

    :goto_8
    const/4 v12, 0x5

    :goto_9
    const/4 v14, 0x6

    :goto_a
    const/4 v15, 0x7

    goto :goto_b

    :cond_11
    if-ne v9, v13, :cond_12

    .line 1066
    sget-object v7, Lcom/android/fmradio/FmConstants;->RADIO_AREA_WESTERN_EUROPE_AM_DEFAULT_PRESET_LIST:[I

    rem-int/lit8 v9, v6, 0x6

    aget v7, v7, v9

    goto :goto_7

    :cond_12
    if-ne v9, v11, :cond_13

    .line 1068
    sget-object v7, Lcom/android/fmradio/FmConstants;->RADIO_AREA_SOUTH_AMERICA_AM_DEFAULT_PRESET_LIST:[I

    rem-int/lit8 v9, v6, 0x6

    aget v7, v7, v9

    goto :goto_7

    :cond_13
    const/4 v10, 0x3

    if-ne v9, v10, :cond_14

    .line 1070
    sget-object v7, Lcom/android/fmradio/FmConstants;->RADIO_AREA_EASTERN_EUROPE_AM_DEFAULT_PRESET_LIST:[I

    rem-int/lit8 v9, v6, 0x6

    aget v7, v7, v9

    goto :goto_8

    :cond_14
    if-ne v9, v4, :cond_15

    .line 1072
    sget-object v7, Lcom/android/fmradio/FmConstants;->RADIO_AREA_CHINA_AM_DEFAULT_PRESET_LIST:[I

    rem-int/lit8 v9, v6, 0x6

    aget v7, v7, v9

    goto :goto_8

    :cond_15
    const/4 v12, 0x5

    if-ne v9, v12, :cond_16

    .line 1074
    sget-object v7, Lcom/android/fmradio/FmConstants;->RADIO_AREA_JAPAN_AM_DEFAULT_PRESET_LIST:[I

    rem-int/lit8 v9, v6, 0x6

    aget v7, v7, v9

    goto :goto_9

    :cond_16
    const/4 v14, 0x6

    if-ne v9, v14, :cond_17

    .line 1076
    sget-object v7, Lcom/android/fmradio/FmConstants;->RADIO_AREA_SOUTHEAST_ASIA_AM_DEFAULT_PRESET_LIST:[I

    rem-int/lit8 v9, v6, 0x6

    aget v7, v7, v9

    goto :goto_a

    :cond_17
    const/4 v15, 0x7

    if-ne v9, v15, :cond_18

    .line 1078
    sget-object v7, Lcom/android/fmradio/FmConstants;->RADIO_AREA_SOUTH_AMERICA2_AM_DEFAULT_PRESET_LIST:[I

    rem-int/lit8 v9, v6, 0x6

    aget v7, v7, v9

    .line 1081
    :cond_18
    :goto_b
    new-instance v9, Lcom/android/fmradio/info/FmFreqInfo;

    invoke-direct {v9}, Lcom/android/fmradio/info/FmFreqInfo;-><init>()V

    .line 1082
    invoke-virtual {v9, v2}, Lcom/android/fmradio/info/FmFreqInfo;->setBand(I)V

    .line 1083
    invoke-virtual {v9, v6}, Lcom/android/fmradio/info/FmFreqInfo;->setFreqIndex(I)V

    .line 1084
    invoke-virtual {v9, v7}, Lcom/android/fmradio/info/FmFreqInfo;->setFreq(I)V

    .line 1085
    invoke-virtual {v3, v9}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 1087
    invoke-static {v7, v11}, Lcom/android/fmradio/utils/FmUtils;->int2Bytes(II)[B

    move-result-object v7

    add-int/lit8 v9, v17, 0x1

    .line 1088
    aget-byte v16, v7, v13

    aput-byte v16, v5, v17

    add-int/lit8 v17, v9, 0x1

    .line 1089
    aget-byte v7, v7, v8

    aput-byte v7, v5, v9

    add-int/lit8 v6, v6, 0x1

    goto :goto_6

    .line 1094
    :cond_19
    sget-object v2, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    new-instance v4, Ljava/lang/StringBuilder;

    invoke-direct {v4}, Ljava/lang/StringBuilder;-><init>()V

    const-string v6, "data: "

    invoke-virtual {v4, v6}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-static {v5}, Lcom/android/fmradio/utils/FmUtils;->ByteToString([B)Ljava/lang/String;

    move-result-object v6

    invoke-virtual {v4, v6}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v4}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v4

    invoke-static {v2, v4}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 1096
    iget-object v2, v0, Lcom/android/fmradio/FmMainActivity;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    if-eqz v2, :cond_1a

    .line 1097
    invoke-virtual {v2, v5}, Lcom/android/fmradio/TunerManagerForExt;->setPresetList([B)V

    .line 1101
    :cond_1a
    iget v2, v0, Lcom/android/fmradio/FmMainActivity;->mBand:I

    invoke-static {v2}, Lcom/android/fmradio/utils/FmUtils;->isFMBand(I)Z

    move-result v2

    if-eqz v2, :cond_1b

    .line 1102
    invoke-direct {v0, v13}, Lcom/android/fmradio/FmMainActivity;->resetStationList(Z)V

    goto :goto_c

    .line 1104
    :cond_1b
    invoke-direct {v0, v8}, Lcom/android/fmradio/FmMainActivity;->resetStationList(Z)V

    .line 1107
    :goto_c
    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2}, Ljava/lang/StringBuilder;-><init>()V

    const-string v4, "stationList - "

    .line 1108
    invoke-virtual {v2, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 1109
    invoke-virtual {v3}, Ljava/util/ArrayList;->size()I

    move-result v4

    :goto_d
    if-ge v8, v4, :cond_1d

    .line 1111
    invoke-virtual {v3, v8}, Ljava/util/ArrayList;->get(I)Ljava/lang/Object;

    move-result-object v5

    check-cast v5, Lcom/android/fmradio/info/FmFreqInfo;

    if-eqz v1, :cond_1c

    .line 1113
    iget-object v6, v0, Lcom/android/fmradio/FmMainActivity;->mFMStationList:[Lcom/android/fmradio/info/FmFreqInfo;

    aput-object v5, v6, v8

    goto :goto_e

    .line 1115
    :cond_1c
    iget-object v6, v0, Lcom/android/fmradio/FmMainActivity;->mAMStationList:[Lcom/android/fmradio/info/FmFreqInfo;

    aput-object v5, v6, v8

    .line 1118
    :goto_e
    invoke-virtual {v5}, Lcom/android/fmradio/info/FmFreqInfo;->getFreqIndex()I

    move-result v6

    invoke-virtual {v2, v6}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v6, " : "

    invoke-virtual {v2, v6}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 1119
    invoke-virtual {v5}, Lcom/android/fmradio/info/FmFreqInfo;->getBand()I

    move-result v7

    invoke-virtual {v2, v7}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v2, v6}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 1120
    invoke-virtual {v5}, Lcom/android/fmradio/info/FmFreqInfo;->getFreq()I

    move-result v5

    invoke-virtual {v2, v5}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v5, " -- "

    invoke-virtual {v2, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    add-int/lit8 v8, v8, 0x1

    goto :goto_d

    .line 1122
    :cond_1d
    sget-object v1, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v2

    invoke-static {v1, v2}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    return-void
.end method


# virtual methods
.method public getHandler()Landroid/os/Handler;
    .locals 1

    .line 255
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mHandler:Landroid/os/Handler;

    return-object v0
.end method

.method public getRdsAFSwitch()I
    .locals 1

    .line 1652
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    if-eqz v0, :cond_0

    .line 1653
    invoke-virtual {v0}, Lcom/android/fmradio/TunerManagerForExt;->getRdsAFSwitch()I

    move-result v0

    return v0

    :cond_0
    const/4 v0, 0x0

    return v0
.end method

.method public getRdsPtyType()I
    .locals 1

    .line 1632
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    if-eqz v0, :cond_0

    .line 1633
    invoke-virtual {v0}, Lcom/android/fmradio/TunerManagerForExt;->getRdsPtyType()I

    move-result v0

    return v0

    :cond_0
    const/4 v0, 0x0

    return v0
.end method

.method public getRdsTASwitch()I
    .locals 1

    .line 1639
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    if-eqz v0, :cond_0

    .line 1640
    invoke-virtual {v0}, Lcom/android/fmradio/TunerManagerForExt;->getRdsTASwitch()I

    move-result v0

    return v0

    :cond_0
    const/4 v0, 0x0

    return v0
.end method

.method public getScale()F
    .locals 1

    .line 806
    iget v0, p0, Lcom/android/fmradio/FmMainActivity;->mScale:F

    return v0
.end method

.method public isSearching()Z
    .locals 1

    .line 1665
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    if-eqz v0, :cond_0

    invoke-virtual {v0}, Lcom/android/fmradio/TunerManagerForExt;->isScanning()Z

    move-result v0

    if-eqz v0, :cond_0

    const/4 v0, 0x1

    goto :goto_0

    :cond_0
    const/4 v0, 0x0

    :goto_0
    return v0
.end method

.method public onAuto()V
    .locals 3

    .line 1820
    sget-object v0, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "onAuto - mService: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-object v2, p0, Lcom/android/fmradio/FmMainActivity;->mService:Lcom/android/fmradio/FmService;

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    const-string v2, " - mBand: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget v2, p0, Lcom/android/fmradio/FmMainActivity;->mBand:I

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v2, " - mRadioArea: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget v2, p0, Lcom/android/fmradio/FmMainActivity;->mRadioArea:I

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 1822
    invoke-virtual {p0}, Lcom/android/fmradio/FmMainActivity;->restorePowerUp()Z

    move-result v0

    if-eqz v0, :cond_0

    return-void

    .line 1826
    :cond_0
    invoke-virtual {p0}, Lcom/android/fmradio/FmMainActivity;->requestAudioFocus()V

    .line 1829
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mService:Lcom/android/fmradio/FmService;

    if-eqz v0, :cond_1

    .line 1830
    invoke-direct {p0}, Lcom/android/fmradio/FmMainActivity;->cleanAllPageButtonHighlight()V

    .line 1832
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mService:Lcom/android/fmradio/FmService;

    invoke-virtual {v0}, Lcom/android/fmradio/FmService;->startScanAsync()V

    :cond_1
    return-void
.end method

.method public onBackPressed()V
    .locals 2

    .line 1604
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mGVRdsPty:Landroid/widget/GridView;

    invoke-virtual {v0}, Landroid/widget/GridView;->getVisibility()I

    move-result v0

    if-nez v0, :cond_0

    .line 1605
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mGVRdsPty:Landroid/widget/GridView;

    const/16 v1, 0x8

    invoke-virtual {v0, v1}, Landroid/widget/GridView;->setVisibility(I)V

    goto :goto_0

    .line 1608
    :cond_0
    sget v0, Landroid/os/Build$VERSION;->SDK_INT:I

    const/16 v1, 0x1f

    if-lt v0, v1, :cond_1

    .line 1609
    invoke-virtual {p0}, Lcom/android/fmradio/FmMainActivity;->finish()V

    goto :goto_0

    .line 1611
    :cond_1
    invoke-super {p0}, Lcom/qf/skin/manager/base/SkinFragmentActivity;->onBackPressed()V

    :goto_0
    return-void
.end method

.method public onBand()V
    .locals 3

    .line 1772
    invoke-virtual {p0}, Lcom/android/fmradio/FmMainActivity;->stopScan()Z

    move-result v0

    if-eqz v0, :cond_0

    return-void

    .line 1776
    :cond_0
    invoke-virtual {p0}, Lcom/android/fmradio/FmMainActivity;->restorePowerUp()Z

    move-result v0

    if-eqz v0, :cond_1

    return-void

    :cond_1
    const/16 v0, 0x1f4

    .line 1780
    invoke-direct {p0, v0}, Lcom/android/fmradio/FmMainActivity;->onOperationDelay(I)Z

    move-result v0

    if-eqz v0, :cond_2

    return-void

    .line 1784
    :cond_2
    invoke-virtual {p0}, Lcom/android/fmradio/FmMainActivity;->requestAudioFocus()V

    .line 1786
    sget-object v0, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "onBand - old band: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget v2, p0, Lcom/android/fmradio/FmMainActivity;->mBand:I

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 1788
    iget v0, p0, Lcom/android/fmradio/FmMainActivity;->mBand:I

    if-ltz v0, :cond_3

    const/4 v1, 0x4

    if-ge v0, v1, :cond_3

    add-int/lit8 v0, v0, 0x1

    .line 1789
    iput v0, p0, Lcom/android/fmradio/FmMainActivity;->mBand:I

    goto :goto_0

    :cond_3
    const/4 v0, 0x0

    .line 1791
    iput v0, p0, Lcom/android/fmradio/FmMainActivity;->mBand:I

    .line 1793
    :goto_0
    sget-object v0, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "onBand - new band: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget v2, p0, Lcom/android/fmradio/FmMainActivity;->mBand:I

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 1795
    iget v0, p0, Lcom/android/fmradio/FmMainActivity;->mBand:I

    invoke-direct {p0, v0}, Lcom/android/fmradio/FmMainActivity;->setBand(I)V

    return-void
.end method

.method public onConfigurationChanged(Landroid/content/res/Configuration;)V
    .locals 3

    .line 795
    invoke-super {p0, p1}, Lcom/qf/skin/manager/base/SkinFragmentActivity;->onConfigurationChanged(Landroid/content/res/Configuration;)V

    .line 796
    sget-object v0, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "newConfig: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p1

    invoke-static {v0, p1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 798
    sget-object p1, Lcom/android/fmradio/BuildConfig;->UseSkinLib:Ljava/lang/Boolean;

    invoke-virtual {p1}, Ljava/lang/Boolean;->booleanValue()Z

    move-result p1

    if-eqz p1, :cond_0

    .line 799
    invoke-direct {p0}, Lcom/android/fmradio/FmMainActivity;->setStatusBarFullTransparent()V

    .line 801
    invoke-direct {p0}, Lcom/android/fmradio/FmMainActivity;->setBackground()V

    :cond_0
    return-void
.end method

.method public onCreate(Landroid/os/Bundle;)V
    .locals 7

    .line 711
    invoke-super {p0, p1}, Lcom/qf/skin/manager/base/SkinFragmentActivity;->onCreate(Landroid/os/Bundle;)V

    .line 713
    invoke-direct {p0}, Lcom/android/fmradio/FmMainActivity;->setStatusBarFullTransparent()V

    const-string p1, "sys.qf.radio.status"

    const-string v0, "true"

    .line 715
    invoke-static {p1, v0}, Landroid/os/SystemProperties;->set(Ljava/lang/String;Ljava/lang/String;)V

    const/4 p1, 0x3

    .line 717
    invoke-virtual {p0, p1}, Lcom/android/fmradio/FmMainActivity;->setVolumeControlStream(I)V

    .line 719
    invoke-virtual {p0}, Lcom/android/fmradio/FmMainActivity;->isInMultiWindowMode()Z

    move-result p1

    iput-boolean p1, p0, Lcom/android/fmradio/FmMainActivity;->mPreIsInMultiWindowMode:Z

    .line 721
    iget-boolean p1, p0, Lcom/android/fmradio/FmMainActivity;->mPreIsInMultiWindowMode:Z

    if-eqz p1, :cond_0

    const p1, 0x7f0b0034

    .line 722
    invoke-virtual {p0, p1}, Lcom/android/fmradio/FmMainActivity;->setContentView(I)V

    goto :goto_0

    :cond_0
    const p1, 0x7f0b0022

    .line 724
    invoke-virtual {p0, p1}, Lcom/android/fmradio/FmMainActivity;->setContentView(I)V

    .line 727
    :goto_0
    invoke-direct {p0}, Lcom/android/fmradio/FmMainActivity;->setBackground()V

    .line 729
    invoke-virtual {p0}, Lcom/android/fmradio/FmMainActivity;->getResources()Landroid/content/res/Resources;

    move-result-object p1

    invoke-virtual {p1}, Landroid/content/res/Resources;->getConfiguration()Landroid/content/res/Configuration;

    move-result-object p1

    .line 730
    iget v0, p1, Landroid/content/res/Configuration;->smallestScreenWidthDp:I

    .line 731
    iget v1, p1, Landroid/content/res/Configuration;->screenWidthDp:I

    .line 732
    iget v2, p1, Landroid/content/res/Configuration;->screenHeightDp:I

    .line 733
    iget v3, p1, Landroid/content/res/Configuration;->densityDpi:I

    .line 734
    sget-object v4, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    new-instance v5, Ljava/lang/StringBuilder;

    invoke-direct {v5}, Ljava/lang/StringBuilder;-><init>()V

    const-string v6, "smallestScreenWidth: "

    invoke-virtual {v5, v6}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v5, v0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v0, " - screenWidthDp: "

    invoke-virtual {v5, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v5, v1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v0, " - screenHeightDp: "

    invoke-virtual {v5, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v5, v2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v0, " - densityDpi: "

    invoke-virtual {v5, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v5, v3}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v0, " - config: "

    invoke-virtual {v5, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 738
    invoke-virtual {p1}, Landroid/content/res/Configuration;->toString()Ljava/lang/String;

    move-result-object p1

    invoke-virtual {v5, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v5}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p1

    .line 734
    invoke-static {v4, p1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 740
    invoke-virtual {p0}, Lcom/android/fmradio/FmMainActivity;->getApplicationContext()Landroid/content/Context;

    move-result-object p1

    iput-object p1, p0, Lcom/android/fmradio/FmMainActivity;->mContext:Landroid/content/Context;

    .line 742
    invoke-direct {p0}, Lcom/android/fmradio/FmMainActivity;->initData()V

    .line 744
    invoke-direct {p0}, Lcom/android/fmradio/FmMainActivity;->initUiComponent()V

    .line 746
    invoke-direct {p0}, Lcom/android/fmradio/FmMainActivity;->initUtilEventManager()V

    .line 748
    invoke-direct {p0}, Lcom/android/fmradio/FmMainActivity;->registerReceiver()V

    return-void
.end method

.method public onDestroy()V
    .locals 6

    .line 1570
    sget-object v0, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    const-string v1, "start"

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    const-string v0, "sys.qf.radio.status"

    const-string v1, "false"

    .line 1572
    invoke-static {v0, v1}, Landroid/os/SystemProperties;->set(Ljava/lang/String;Ljava/lang/String;)V

    const-string v0, "nothing"

    const-string v1, "persist.sys.qf.last_audio_src"

    .line 1574
    invoke-static {v1, v0}, Landroid/os/SystemProperties;->get(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;

    move-result-object v2

    .line 1575
    sget-object v3, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    new-instance v4, Ljava/lang/StringBuilder;

    invoke-direct {v4}, Ljava/lang/StringBuilder;-><init>()V

    const-string v5, "currentAudioSource: "

    invoke-virtual {v4, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v4, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v4}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v4

    invoke-static {v3, v4}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 1576
    invoke-virtual {p0}, Lcom/android/fmradio/FmMainActivity;->getPackageName()Ljava/lang/String;

    move-result-object v3

    invoke-virtual {v2, v3}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v2

    if-eqz v2, :cond_0

    const/4 v2, 0x1

    const-string v3, "sys.qf.is.acc.on"

    .line 1577
    invoke-static {v3, v2}, Landroid/os/SystemProperties;->getBoolean(Ljava/lang/String;Z)Z

    move-result v2

    if-eqz v2, :cond_0

    .line 1578
    invoke-static {v1, v0}, Landroid/os/SystemProperties;->set(Ljava/lang/String;Ljava/lang/String;)V

    .line 1581
    :cond_0
    invoke-virtual {p0}, Lcom/android/fmradio/FmMainActivity;->stopScan()Z

    .line 1583
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mHandler:Landroid/os/Handler;

    const/4 v1, 0x0

    invoke-virtual {v0, v1}, Landroid/os/Handler;->removeCallbacksAndMessages(Ljava/lang/Object;)V

    .line 1585
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mService:Lcom/android/fmradio/FmService;

    if-eqz v0, :cond_1

    .line 1586
    iget-object v2, p0, Lcom/android/fmradio/FmMainActivity;->mFmRadioListener:Lcom/android/fmradio/iface/FmListener;

    invoke-virtual {v0, v2}, Lcom/android/fmradio/FmService;->unregisterFmRadioListener(Lcom/android/fmradio/iface/FmListener;)V

    .line 1589
    :cond_1
    iput-object v1, p0, Lcom/android/fmradio/FmMainActivity;->mFmRadioListener:Lcom/android/fmradio/iface/FmListener;

    .line 1591
    invoke-direct {p0}, Lcom/android/fmradio/FmMainActivity;->uninitUtilEventManager()V

    .line 1593
    invoke-direct {p0}, Lcom/android/fmradio/FmMainActivity;->powerDownFm()V

    .line 1595
    invoke-direct {p0}, Lcom/android/fmradio/FmMainActivity;->exitService()V

    .line 1597
    invoke-direct {p0}, Lcom/android/fmradio/FmMainActivity;->unregisterReceiver()V

    .line 1599
    invoke-super {p0}, Lcom/qf/skin/manager/base/SkinFragmentActivity;->onDestroy()V

    return-void
.end method

.method public onEQ()V
    .locals 4

    .line 1847
    invoke-virtual {p0}, Lcom/android/fmradio/FmMainActivity;->restorePowerUp()Z

    move-result v0

    if-eqz v0, :cond_0

    return-void

    .line 1851
    :cond_0
    invoke-virtual {p0}, Lcom/android/fmradio/FmMainActivity;->requestAudioFocus()V

    .line 1854
    :try_start_0
    new-instance v0, Landroid/content/Intent;

    const-string v1, "android.intent.action.MAIN"

    invoke-direct {v0, v1}, Landroid/content/Intent;-><init>(Ljava/lang/String;)V

    const-string v1, "android.intent.category.LAUNCHER"

    .line 1855
    invoke-virtual {v0, v1}, Landroid/content/Intent;->addCategory(Ljava/lang/String;)Landroid/content/Intent;

    .line 1856
    new-instance v1, Landroid/content/ComponentName;

    const-string v2, "com.qf.soundeffect"

    const-string v3, "com.qf.soundeffect.SoundActivity"

    invoke-direct {v1, v2, v3}, Landroid/content/ComponentName;-><init>(Ljava/lang/String;Ljava/lang/String;)V

    invoke-virtual {v0, v1}, Landroid/content/Intent;->setComponent(Landroid/content/ComponentName;)Landroid/content/Intent;

    const/high16 v1, 0x10000000

    .line 1857
    invoke-virtual {v0, v1}, Landroid/content/Intent;->addFlags(I)Landroid/content/Intent;

    .line 1858
    invoke-virtual {p0, v0}, Lcom/android/fmradio/FmMainActivity;->startActivity(Landroid/content/Intent;)V
    :try_end_0
    .catch Ljava/lang/Exception; {:try_start_0 .. :try_end_0} :catch_0

    :catch_0
    return-void
.end method

.method public onFine(Z)V
    .locals 1

    .line 1755
    invoke-virtual {p0}, Lcom/android/fmradio/FmMainActivity;->restorePowerUp()Z

    move-result v0

    if-eqz v0, :cond_0

    return-void

    :cond_0
    const/16 v0, 0xa

    .line 1759
    invoke-direct {p0, v0}, Lcom/android/fmradio/FmMainActivity;->onOperationDelay(I)Z

    move-result v0

    if-eqz v0, :cond_1

    return-void

    .line 1763
    :cond_1
    invoke-virtual {p0}, Lcom/android/fmradio/FmMainActivity;->requestAudioFocus()V

    .line 1765
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    invoke-virtual {v0, p1}, Lcom/android/fmradio/TunerManagerForExt;->onFine(Z)V

    return-void
.end method

.method public onLoc()V
    .locals 2

    .line 1837
    invoke-virtual {p0}, Lcom/android/fmradio/FmMainActivity;->restorePowerUp()Z

    move-result v0

    if-eqz v0, :cond_0

    return-void

    .line 1841
    :cond_0
    invoke-virtual {p0}, Lcom/android/fmradio/FmMainActivity;->requestAudioFocus()V

    .line 1843
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    invoke-virtual {v0}, Lcom/android/fmradio/TunerManagerForExt;->getLocFlag()I

    move-result v1

    invoke-virtual {v0, v1}, Lcom/android/fmradio/TunerManagerForExt;->onLoc(I)V

    return-void
.end method

.method public onMultiWindowModeChanged(Z)V
    .locals 8

    .line 663
    invoke-super {p0, p1}, Lcom/qf/skin/manager/base/SkinFragmentActivity;->onMultiWindowModeChanged(Z)V

    .line 664
    invoke-virtual {p0}, Lcom/android/fmradio/FmMainActivity;->getResources()Landroid/content/res/Resources;

    move-result-object v0

    invoke-virtual {v0}, Landroid/content/res/Resources;->getConfiguration()Landroid/content/res/Configuration;

    move-result-object v0

    iget v0, v0, Landroid/content/res/Configuration;->uiMode:I

    .line 665
    invoke-static {}, Lcom/android/fmradio/utils/FmUtils;->getAppBgStyle()I

    move-result v1

    .line 666
    sget-object v2, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    new-instance v3, Ljava/lang/StringBuilder;

    invoke-direct {v3}, Ljava/lang/StringBuilder;-><init>()V

    const-string v4, "start - mPreIsInMultiWindowMode: "

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-boolean v4, p0, Lcom/android/fmradio/FmMainActivity;->mPreIsInMultiWindowMode:Z

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    const-string v4, " - isInMultiWindowMode: "

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 667
    invoke-virtual {p0}, Lcom/android/fmradio/FmMainActivity;->isInMultiWindowMode()Z

    move-result v4

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    const-string v4, " - appBgStyle: "

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v3, v1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v3}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v3

    .line 666
    invoke-static {v2, v3}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 670
    iget-boolean v2, p0, Lcom/android/fmradio/FmMainActivity;->mPreIsInMultiWindowMode:Z

    const-string v3, "jitu2"

    const v4, 0x7f080083

    const v5, 0x7f0700c4

    const v6, 0x7f05001a

    const/16 v7, 0x20

    if-eqz v2, :cond_3

    if-nez p1, :cond_3

    const p1, 0x7f0b0022

    .line 671
    invoke-virtual {p0, p1}, Lcom/android/fmradio/FmMainActivity;->setContentView(I)V

    .line 673
    invoke-virtual {p0, v4}, Lcom/android/fmradio/FmMainActivity;->findViewById(I)Landroid/view/View;

    move-result-object p1

    and-int/2addr v0, v7

    if-ne v0, v7, :cond_1

    const/4 v0, 0x1

    if-ne v1, v0, :cond_0

    .line 676
    invoke-virtual {p1, v5}, Landroid/view/View;->setBackgroundResource(I)V

    goto :goto_0

    :cond_0
    const/4 v0, 0x2

    if-ne v1, v0, :cond_6

    .line 678
    invoke-virtual {p0, v6}, Lcom/android/fmradio/FmMainActivity;->getColor(I)I

    move-result v0

    invoke-virtual {p1, v0}, Landroid/view/View;->setBackgroundColor(I)V

    goto :goto_0

    .line 681
    :cond_1
    invoke-static {v3}, Landroid/qf/os/QFApi;->isClientProduct(Ljava/lang/String;)Z

    move-result v0

    if-eqz v0, :cond_2

    .line 682
    invoke-virtual {p1, v5}, Landroid/view/View;->setBackgroundResource(I)V

    goto :goto_0

    .line 684
    :cond_2
    invoke-virtual {p0, v6}, Lcom/android/fmradio/FmMainActivity;->getColor(I)I

    move-result v0

    invoke-virtual {p1, v0}, Landroid/view/View;->setBackgroundColor(I)V

    goto :goto_0

    .line 687
    :cond_3
    iget-boolean v1, p0, Lcom/android/fmradio/FmMainActivity;->mPreIsInMultiWindowMode:Z

    if-eqz v1, :cond_6

    if-eqz p1, :cond_6

    const p1, 0x7f0b0034

    .line 688
    invoke-virtual {p0, p1}, Lcom/android/fmradio/FmMainActivity;->setContentView(I)V

    .line 690
    invoke-virtual {p0, v4}, Lcom/android/fmradio/FmMainActivity;->findViewById(I)Landroid/view/View;

    move-result-object p1

    and-int/2addr v0, v7

    if-ne v0, v7, :cond_5

    .line 692
    invoke-static {v3}, Landroid/qf/os/QFApi;->isClientProduct(Ljava/lang/String;)Z

    move-result v0

    if-eqz v0, :cond_4

    .line 693
    invoke-virtual {p1, v5}, Landroid/view/View;->setBackgroundResource(I)V

    goto :goto_0

    .line 695
    :cond_4
    invoke-virtual {p0, v6}, Lcom/android/fmradio/FmMainActivity;->getColor(I)I

    move-result v0

    invoke-virtual {p1, v0}, Landroid/view/View;->setBackgroundColor(I)V

    goto :goto_0

    .line 698
    :cond_5
    invoke-virtual {p0, v6}, Lcom/android/fmradio/FmMainActivity;->getColor(I)I

    move-result v0

    invoke-virtual {p1, v0}, Landroid/view/View;->setBackgroundColor(I)V

    .line 702
    :cond_6
    :goto_0
    invoke-direct {p0}, Lcom/android/fmradio/FmMainActivity;->initData()V

    .line 704
    invoke-direct {p0}, Lcom/android/fmradio/FmMainActivity;->initUiComponent()V

    .line 706
    invoke-virtual {p0}, Lcom/android/fmradio/FmMainActivity;->updatePresetList()V

    return-void
.end method

.method public onNextOrPreStation(Z)V
    .locals 3

    .line 1713
    invoke-virtual {p0}, Lcom/android/fmradio/FmMainActivity;->stopScan()Z

    move-result v0

    if-eqz v0, :cond_0

    return-void

    .line 1717
    :cond_0
    invoke-virtual {p0}, Lcom/android/fmradio/FmMainActivity;->requestAudioFocus()V

    .line 1719
    sget-object v0, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "isNextStation: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    if-eqz p1, :cond_1

    .line 1721
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    invoke-virtual {p1}, Lcom/android/fmradio/TunerManagerForExt;->onNext()V

    goto :goto_0

    .line 1723
    :cond_1
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    invoke-virtual {p1}, Lcom/android/fmradio/TunerManagerForExt;->onPre()V

    :goto_0
    return-void
.end method

.method public onPause()V
    .locals 2

    .line 1536
    sget-object v0, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    const-string v1, " ---->>onPause() "

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    const/4 v0, 0x0

    .line 1537
    iput-boolean v0, p0, Lcom/android/fmradio/FmMainActivity;->mIsActivityForeground:Z

    .line 1538
    invoke-direct {p0}, Lcom/android/fmradio/FmMainActivity;->sendFmInfo()V

    .line 1539
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mService:Lcom/android/fmradio/FmService;

    if-eqz v0, :cond_0

    .line 1540
    iget-boolean v1, p0, Lcom/android/fmradio/FmMainActivity;->mIsActivityForeground:Z

    invoke-virtual {v0, v1}, Lcom/android/fmradio/FmService;->setFmMainActivityForeground(Z)V

    .line 1542
    :cond_0
    invoke-super {p0}, Lcom/qf/skin/manager/base/SkinFragmentActivity;->onPause()V

    return-void
.end method

.method public onResume()V
    .locals 2

    .line 1510
    invoke-super {p0}, Lcom/qf/skin/manager/base/SkinFragmentActivity;->onResume()V

    .line 1511
    sget-object v0, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    const-string v1, " ---->>onResume() "

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    const/4 v0, 0x1

    .line 1512
    iput-boolean v0, p0, Lcom/android/fmradio/FmMainActivity;->mIsActivityForeground:Z

    .line 1514
    invoke-direct {p0}, Lcom/android/fmradio/FmMainActivity;->updateRadioArea()V

    .line 1516
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mRadioStatusView:Lcom/android/fmradio/views/RadioStatusView;

    invoke-virtual {v0}, Lcom/android/fmradio/views/RadioStatusView;->updateRdsUIStatus()V

    .line 1518
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mService:Lcom/android/fmradio/FmService;

    if-nez v0, :cond_0

    .line 1519
    sget-object v0, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    const-string v1, "onResume, mService is null"

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    return-void

    .line 1523
    :cond_0
    invoke-virtual {p0}, Lcom/android/fmradio/FmMainActivity;->requestAudioFocus()V

    .line 1525
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mService:Lcom/android/fmradio/FmService;

    iget-boolean v1, p0, Lcom/android/fmradio/FmMainActivity;->mIsActivityForeground:Z

    invoke-virtual {v0, v1}, Lcom/android/fmradio/FmService;->setFmMainActivityForeground(Z)V

    .line 1527
    invoke-virtual {p0}, Lcom/android/fmradio/FmMainActivity;->getPackageName()Ljava/lang/String;

    move-result-object v0

    const-string v1, "persist.sys.qf.last_audio_src"

    invoke-static {v1, v0}, Landroid/os/SystemProperties;->set(Ljava/lang/String;Ljava/lang/String;)V

    return-void
.end method

.method public onSeekStation(IZ)V
    .locals 2

    .line 1734
    invoke-virtual {p0}, Lcom/android/fmradio/FmMainActivity;->restorePowerUp()Z

    move-result v0

    if-eqz v0, :cond_0

    return-void

    .line 1740
    :cond_0
    invoke-virtual {p0}, Lcom/android/fmradio/FmMainActivity;->stopScan()Z

    move-result v0

    if-eqz v0, :cond_1

    return-void

    .line 1744
    :cond_1
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mService:Lcom/android/fmradio/FmService;

    if-nez v0, :cond_2

    .line 1745
    sget-object p1, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    const-string p2, "onSeekStation, mService is null"

    invoke-static {p1, p2}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    return-void

    .line 1749
    :cond_2
    sget-object v0, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    const-string v1, "onSeekStation - start"

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 1751
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mService:Lcom/android/fmradio/FmService;

    iget-object v1, p0, Lcom/android/fmradio/FmMainActivity;->mContext:Landroid/content/Context;

    invoke-static {v1, p1}, Lcom/android/fmradio/utils/FmUtils;->computeFrequency(Landroid/content/Context;I)F

    move-result p1

    invoke-virtual {v0, p1, p2}, Lcom/android/fmradio/FmService;->seekStationAsync(FZ)V

    return-void
.end method

.method public onStart()V
    .locals 3

    .line 1479
    invoke-super {p0}, Lcom/qf/skin/manager/base/SkinFragmentActivity;->onStart()V

    .line 1480
    sget-object v0, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    const-string v1, " ---->>onStart() "

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 1483
    new-instance v0, Landroid/content/Intent;

    const-class v1, Lcom/android/fmradio/FmService;

    invoke-direct {v0, p0, v1}, Landroid/content/Intent;-><init>(Landroid/content/Context;Ljava/lang/Class;)V

    const-string v1, "fmradio.enter"

    .line 1484
    invoke-virtual {v0, v1}, Landroid/content/Intent;->setAction(Ljava/lang/String;)Landroid/content/Intent;

    .line 1485
    invoke-virtual {p0, v0}, Lcom/android/fmradio/FmMainActivity;->startService(Landroid/content/Intent;)Landroid/content/ComponentName;

    move-result-object v0

    if-nez v0, :cond_0

    .line 1486
    sget-object v0, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    const-string v1, "onStart, cannot start FM service"

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    return-void

    :cond_0
    const/4 v0, 0x1

    .line 1490
    iput-boolean v0, p0, Lcom/android/fmradio/FmMainActivity;->mIsServiceStarted:Z

    .line 1495
    new-instance v1, Landroid/content/Intent;

    const-class v2, Lcom/android/fmradio/FmService;

    invoke-direct {v1, p0, v2}, Landroid/content/Intent;-><init>(Landroid/content/Context;Ljava/lang/Class;)V

    iget-object v2, p0, Lcom/android/fmradio/FmMainActivity;->mServiceConnection:Landroid/content/ServiceConnection;

    invoke-virtual {p0, v1, v2, v0}, Lcom/android/fmradio/FmMainActivity;->bindService(Landroid/content/Intent;Landroid/content/ServiceConnection;I)Z

    move-result v0

    iput-boolean v0, p0, Lcom/android/fmradio/FmMainActivity;->mIsServiceBinder:Z

    .line 1498
    iget-boolean v0, p0, Lcom/android/fmradio/FmMainActivity;->mIsServiceBinder:Z

    if-nez v0, :cond_1

    .line 1499
    sget-object v0, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    const-string v1, "onStart, cannot bind FM service"

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 1500
    invoke-virtual {p0}, Lcom/android/fmradio/FmMainActivity;->finish()V

    :cond_1
    return-void
.end method

.method public onStation(Ljava/lang/String;)V
    .locals 3

    .line 1691
    sget-object v0, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "station: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 1692
    invoke-static {p1}, Landroid/text/TextUtils;->isEmpty(Ljava/lang/CharSequence;)Z

    move-result v0

    if-nez v0, :cond_1

    .line 1693
    invoke-virtual {p0}, Lcom/android/fmradio/FmMainActivity;->stopScan()Z

    move-result v0

    if-eqz v0, :cond_0

    return-void

    .line 1697
    :cond_0
    invoke-virtual {p0}, Lcom/android/fmradio/FmMainActivity;->requestAudioFocus()V

    .line 1699
    invoke-static {p1}, Ljava/lang/Float;->parseFloat(Ljava/lang/String;)F

    move-result p1

    .line 1700
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    if-eqz v0, :cond_1

    .line 1701
    invoke-virtual {v0, p1}, Lcom/android/fmradio/TunerManagerForExt;->onTune(F)Z

    :cond_1
    return-void
.end method

.method public onStop()V
    .locals 2

    .line 1551
    sget-object v0, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    const-string v1, " ---->>onStop() "

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 1553
    iget-boolean v0, p0, Lcom/android/fmradio/FmMainActivity;->mIsServiceBinder:Z

    if-eqz v0, :cond_0

    .line 1554
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mServiceConnection:Landroid/content/ServiceConnection;

    invoke-virtual {p0, v0}, Lcom/android/fmradio/FmMainActivity;->unbindService(Landroid/content/ServiceConnection;)V

    const/4 v0, 0x0

    .line 1555
    iput-boolean v0, p0, Lcom/android/fmradio/FmMainActivity;->mIsServiceBinder:Z

    .line 1558
    :cond_0
    invoke-super {p0}, Lcom/qf/skin/manager/base/SkinFragmentActivity;->onStop()V

    return-void
.end method

.method public onUpdateStationName(ILjava/lang/String;)V
    .locals 4

    .line 2031
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    invoke-virtual {v0}, Lcom/android/fmradio/TunerManagerForExt;->getPresetList()[I

    move-result-object v0

    if-eqz v0, :cond_1

    .line 2032
    array-length v1, v0

    const/4 v2, 0x6

    if-ne v1, v2, :cond_1

    const/4 v1, 0x0

    :goto_0
    if-ge v1, v2, :cond_1

    .line 2034
    aget v3, v0, v1

    if-ne p1, v3, :cond_0

    .line 2037
    iget-object v3, p0, Lcom/android/fmradio/FmMainActivity;->mRadioPresetListView:Lcom/android/fmradio/views/RadioPresetListView;

    invoke-virtual {v3, v1, p2}, Lcom/android/fmradio/views/RadioPresetListView;->setStationName(ILjava/lang/String;)V

    .line 2039
    invoke-direct {p0}, Lcom/android/fmradio/FmMainActivity;->sendFmInfo()V

    :cond_0
    add-int/lit8 v1, v1, 0x1

    goto :goto_0

    :cond_1
    return-void
.end method

.method public requestAudioFocus()V
    .locals 1

    .line 1907
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mService:Lcom/android/fmradio/FmService;

    if-eqz v0, :cond_0

    invoke-static {}, Landroid/qf/os/QFApi;->isBTPhoneStartup()Z

    move-result v0

    if-nez v0, :cond_0

    .line 1908
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mService:Lcom/android/fmradio/FmService;

    invoke-virtual {v0}, Lcom/android/fmradio/FmService;->requestAudioFocus()Z

    :cond_0
    return-void
.end method

.method public restorePowerUp()Z
    .locals 2

    .line 1681
    invoke-virtual {p0}, Lcom/android/fmradio/FmMainActivity;->isInMultiWindowMode()Z

    move-result v0

    if-eqz v0, :cond_0

    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mService:Lcom/android/fmradio/FmService;

    invoke-virtual {v0}, Lcom/android/fmradio/FmService;->getPowerStatus()I

    move-result v0

    sget v1, Lcom/android/fmradio/FmService;->POWER_DOWN:I

    if-ne v0, v1, :cond_0

    .line 1682
    sget-object v0, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    const-string v1, "restorePowerUp - start"

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 1683
    invoke-direct {p0}, Lcom/android/fmradio/FmMainActivity;->powerUpFm()V

    const/4 v0, 0x1

    return v0

    :cond_0
    const/4 v0, 0x0

    return v0
.end method

.method public setButtonHighLight()V
    .locals 6

    .line 2121
    invoke-direct {p0}, Lcom/android/fmradio/FmMainActivity;->clearButtonBackground()V

    .line 2124
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    invoke-virtual {v0}, Lcom/android/fmradio/TunerManagerForExt;->getPresetList()[I

    move-result-object v0

    const/4 v1, 0x0

    if-eqz v0, :cond_1

    .line 2125
    array-length v2, v0

    const/4 v3, 0x6

    if-ne v2, v3, :cond_1

    move v2, v1

    :goto_0
    if-ge v2, v3, :cond_1

    .line 2127
    iget v4, p0, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    aget v5, v0, v2

    if-ne v4, v5, :cond_0

    .line 2128
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mRadioPresetListView:Lcom/android/fmradio/views/RadioPresetListView;

    rem-int/2addr v2, v3

    invoke-virtual {v0, v2}, Lcom/android/fmradio/views/RadioPresetListView;->setButtonBackground(I)V

    const/4 v1, 0x1

    goto :goto_1

    :cond_0
    add-int/lit8 v2, v2, 0x1

    goto :goto_0

    .line 2135
    :cond_1
    :goto_1
    sget-object v0, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2}, Ljava/lang/StringBuilder;-><init>()V

    const-string v3, "setButtonHighLight - freqExist: "

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2, v1}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    const-string v1, " - mBand: "

    invoke-virtual {v2, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget v1, p0, Lcom/android/fmradio/FmMainActivity;->mBand:I

    invoke-virtual {v2, v1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v1, " - mCurrentStation: "

    invoke-virtual {v2, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget v1, p0, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    invoke-virtual {v2, v1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    return-void
.end method

.method public setFreq(I)V
    .locals 3

    .line 1241
    sget-object v0, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "setFreq - freq: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 1242
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    if-eqz v0, :cond_0

    .line 1243
    invoke-virtual {v0, p1}, Lcom/android/fmradio/TunerManagerForExt;->onTuneExt(I)Z

    :cond_0
    return-void
.end method

.method public setRdsAFSwitch()V
    .locals 1

    .line 1659
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    if-eqz v0, :cond_0

    .line 1660
    invoke-virtual {v0}, Lcom/android/fmradio/TunerManagerForExt;->setRdsAFSwitch()V

    :cond_0
    return-void
.end method

.method public setRdsTASwitch()V
    .locals 1

    .line 1646
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    if-eqz v0, :cond_0

    .line 1647
    invoke-virtual {v0}, Lcom/android/fmradio/TunerManagerForExt;->setRdsTASwitch()V

    :cond_0
    return-void
.end method

.method public stopScan()Z
    .locals 1

    .line 1669
    invoke-virtual {p0}, Lcom/android/fmradio/FmMainActivity;->isSearching()Z

    move-result v0

    if-eqz v0, :cond_0

    .line 1670
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mService:Lcom/android/fmradio/FmService;

    invoke-virtual {v0}, Lcom/android/fmradio/FmService;->stopScan()Z

    const/4 v0, 0x1

    return v0

    :cond_0
    const/4 v0, 0x0

    return v0
.end method

.method public tuneStation(I)V
    .locals 1

    const/4 v0, 0x0

    .line 1928
    invoke-direct {p0, p1, v0}, Lcom/android/fmradio/FmMainActivity;->tuneStation(IZ)V

    return-void
.end method

.method public updatePresetList()V
    .locals 7

    .line 2006
    sget-object v0, Lcom/android/fmradio/FmMainActivity;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "start - isSearching: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {p0}, Lcom/android/fmradio/FmMainActivity;->isSearching()Z

    move-result v2

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    const-string v2, " - mBand: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget v2, p0, Lcom/android/fmradio/FmMainActivity;->mBand:I

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 2007
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    invoke-virtual {v0}, Lcom/android/fmradio/TunerManagerForExt;->getPresetList()[I

    move-result-object v0

    if-eqz v0, :cond_4

    .line 2008
    array-length v1, v0

    const/4 v2, 0x6

    if-ne v1, v2, :cond_4

    const/4 v1, 0x0

    move v3, v1

    :goto_0
    if-ge v3, v2, :cond_2

    .line 2010
    aget v4, v0, v3

    .line 2012
    iget-object v5, p0, Lcom/android/fmradio/FmMainActivity;->mRadioPresetListView:Lcom/android/fmradio/views/RadioPresetListView;

    iget v6, p0, Lcom/android/fmradio/FmMainActivity;->mBand:I

    invoke-static {v6}, Lcom/android/fmradio/utils/FmUtils;->isFMBand(I)Z

    move-result v6

    if-eqz v6, :cond_0

    sget-object v6, Lcom/android/fmradio/FmMainActivity;->FM_UNIT:Ljava/lang/String;

    goto :goto_1

    :cond_0
    sget-object v6, Lcom/android/fmradio/FmMainActivity;->AM_UNIT:Ljava/lang/String;

    :goto_1
    invoke-virtual {v5, v3, v4, v6}, Lcom/android/fmradio/views/RadioPresetListView;->setFreq(IILjava/lang/String;)V

    if-nez v4, :cond_1

    goto :goto_2

    .line 2019
    :cond_1
    invoke-direct {p0, v3, v4, v1}, Lcom/android/fmradio/FmMainActivity;->updateStationList(IIZ)V

    :goto_2
    add-int/lit8 v3, v3, 0x1

    goto :goto_0

    .line 2022
    :cond_2
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    invoke-virtual {v0}, Lcom/android/fmradio/TunerManagerForExt;->getPresetIndex()I

    move-result v0

    if-nez v0, :cond_3

    .line 2023
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mRadioPresetListView:Lcom/android/fmradio/views/RadioPresetListView;

    invoke-virtual {v0}, Lcom/android/fmradio/views/RadioPresetListView;->clearButtonBackground()V

    goto :goto_3

    .line 2025
    :cond_3
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mRadioPresetListView:Lcom/android/fmradio/views/RadioPresetListView;

    iget-object v1, p0, Lcom/android/fmradio/FmMainActivity;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    invoke-virtual {v1}, Lcom/android/fmradio/TunerManagerForExt;->getPresetIndex()I

    move-result v1

    add-int/lit8 v1, v1, -0x1

    invoke-virtual {v0, v1}, Lcom/android/fmradio/views/RadioPresetListView;->setButtonBackground(I)V

    :cond_4
    :goto_3
    return-void
.end method

.method public updateRdsPsPresetList()V
    .locals 5

    .line 2101
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    invoke-virtual {v0}, Lcom/android/fmradio/TunerManagerForExt;->getRdsPsPresetList()[Ljava/lang/String;

    move-result-object v0

    if-eqz v0, :cond_2

    .line 2102
    array-length v1, v0

    const/4 v2, 0x6

    if-ne v1, v2, :cond_2

    const/4 v1, 0x0

    :goto_0
    if-ge v1, v2, :cond_0

    .line 2104
    iget-object v3, p0, Lcom/android/fmradio/FmMainActivity;->mRadioPresetListView:Lcom/android/fmradio/views/RadioPresetListView;

    aget-object v4, v0, v1

    invoke-virtual {v3, v1, v4}, Lcom/android/fmradio/views/RadioPresetListView;->setFreq(ILjava/lang/String;)V

    add-int/lit8 v1, v1, 0x1

    goto :goto_0

    .line 2107
    :cond_0
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    invoke-virtual {v0}, Lcom/android/fmradio/TunerManagerForExt;->getPresetIndex()I

    move-result v0

    if-nez v0, :cond_1

    .line 2108
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mRadioPresetListView:Lcom/android/fmradio/views/RadioPresetListView;

    invoke-virtual {v0}, Lcom/android/fmradio/views/RadioPresetListView;->clearButtonBackground()V

    goto :goto_1

    .line 2110
    :cond_1
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity;->mRadioPresetListView:Lcom/android/fmradio/views/RadioPresetListView;

    iget-object v1, p0, Lcom/android/fmradio/FmMainActivity;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    invoke-virtual {v1}, Lcom/android/fmradio/TunerManagerForExt;->getPresetIndex()I

    move-result v1

    add-int/lit8 v1, v1, -0x1

    invoke-virtual {v0, v1}, Lcom/android/fmradio/views/RadioPresetListView;->setButtonBackground(I)V

    :cond_2
    :goto_1
    return-void
.end method

.method public updateStationValue(ZI)V
    .locals 0

    .line 1990
    invoke-direct {p0}, Lcom/android/fmradio/FmMainActivity;->initFreq()V

    .line 1992
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity;->mRadioStatusView:Lcom/android/fmradio/views/RadioStatusView;

    invoke-virtual {p1}, Lcom/android/fmradio/views/RadioStatusView;->updateRdsUIStatus()V

    .line 1994
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity;->mRadioFreqInfoView:Lcom/android/fmradio/views/RadioFreqInfoView;

    invoke-virtual {p1, p2}, Lcom/android/fmradio/views/RadioFreqInfoView;->updateFreqValue(I)V

    .line 1996
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity;->mRadioFreqInfoView:Lcom/android/fmradio/views/RadioFreqInfoView;

    invoke-virtual {p1}, Lcom/android/fmradio/views/RadioFreqInfoView;->updateFreqBand()V

    .line 1998
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity;->mRadioFreqInfoView:Lcom/android/fmradio/views/RadioFreqInfoView;

    invoke-virtual {p1}, Lcom/android/fmradio/views/RadioFreqInfoView;->updateFreqUnit()V

    .line 2000
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity;->mRadioFreqSliderView:Lcom/android/fmradio/views/RadioFreqSliderView;

    invoke-virtual {p1}, Lcom/android/fmradio/views/RadioFreqSliderView;->setSlider()V

    .line 2002
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity;->mRadioFreqSliderView:Lcom/android/fmradio/views/RadioFreqSliderView;

    invoke-virtual {p1}, Lcom/android/fmradio/views/RadioFreqSliderView;->setFreqTitle()V

    return-void
.end method
