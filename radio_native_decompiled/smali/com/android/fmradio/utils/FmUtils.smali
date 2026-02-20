.class public Lcom/android/fmradio/utils/FmUtils;
.super Ljava/lang/Object;
.source "FmUtils.java"


# static fields
.field public static final CONVERT_RATE:I = 0x64

.field private static final FM_IS_SPEAKER_MODE:Ljava/lang/String; = "fm_is_speaker_mode"

.field private static final TAG:Ljava/lang/String;

.field private static mPTYEnable:Z


# direct methods
.method static constructor <clinit>()V
    .locals 1

    .line 27
    const-class v0, Lcom/android/fmradio/utils/FmUtils;

    invoke-virtual {v0}, Ljava/lang/Class;->getSimpleName()Ljava/lang/String;

    move-result-object v0

    sput-object v0, Lcom/android/fmradio/utils/FmUtils;->TAG:Ljava/lang/String;

    const/4 v0, 0x0

    .line 34
    sput-boolean v0, Lcom/android/fmradio/utils/FmUtils;->mPTYEnable:Z

    return-void
.end method

.method public constructor <init>()V
    .locals 0

    .line 26
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

.method public static ByteToString([B)Ljava/lang/String;
    .locals 5

    if-eqz p0, :cond_1

    const/4 v0, 0x0

    const-string v1, ""

    .line 390
    :goto_0
    array-length v2, p0

    if-ge v0, v2, :cond_2

    .line 391
    aget-byte v2, p0, v0

    and-int/lit16 v2, v2, 0xff

    invoke-static {v2}, Ljava/lang/Integer;->toHexString(I)Ljava/lang/String;

    move-result-object v2

    .line 393
    invoke-virtual {v2}, Ljava/lang/String;->length()I

    move-result v3

    const/4 v4, 0x1

    if-ne v4, v3, :cond_0

    .line 394
    new-instance v3, Ljava/lang/StringBuilder;

    invoke-direct {v3}, Ljava/lang/StringBuilder;-><init>()V

    const-string v4, "0"

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v3, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v3}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v2

    .line 397
    :cond_0
    new-instance v3, Ljava/lang/StringBuilder;

    invoke-direct {v3}, Ljava/lang/StringBuilder;-><init>()V

    invoke-virtual {v3, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v3, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    const-string v1, " "

    invoke-virtual {v3, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v3}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    add-int/lit8 v0, v0, 0x1

    goto :goto_0

    :cond_1
    const-string v1, "null"

    :cond_2
    return-object v1
.end method

.method public static byteToHexString(B)Ljava/lang/String;
    .locals 0

    and-int/lit16 p0, p0, 0xff

    .line 383
    invoke-static {p0}, Ljava/lang/Integer;->toHexString(I)Ljava/lang/String;

    move-result-object p0

    return-object p0
.end method

.method public static bytes2Int([B)I
    .locals 3

    .line 368
    array-length v0, p0

    add-int/lit8 v0, v0, -0x1

    const/4 v1, 0x0

    :goto_0
    if-ltz v0, :cond_0

    shl-int/lit8 v1, v1, 0x8

    .line 370
    aget-byte v2, p0, v0

    and-int/lit16 v2, v2, 0xff

    or-int/2addr v1, v2

    add-int/lit8 v0, v0, -0x1

    goto :goto_0

    :cond_0
    return v1
.end method

.method public static computeDecreaseStation(Landroid/content/Context;I)I
    .locals 4

    .line 153
    invoke-static {}, Lcom/android/fmradio/utils/FmUtils;->getRadioArea()I

    move-result v0

    .line 154
    invoke-static {p0}, Lcom/android/fmradio/database/FmStation;->getCurrentBand(Landroid/content/Context;)I

    move-result p0

    .line 155
    sget-object v1, Lcom/android/fmradio/utils/FmUtils;->TAG:Ljava/lang/String;

    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2}, Ljava/lang/StringBuilder;-><init>()V

    const-string v3, "radioArea: "

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2, v0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v3, " - band: "

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2, p0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v2

    invoke-static {v1, v2}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 160
    invoke-static {p0}, Lcom/android/fmradio/utils/FmUtils;->isFMBand(I)Z

    move-result v1

    if-eqz v1, :cond_0

    .line 161
    invoke-static {v0, p0}, Lcom/android/fmradio/utils/FmUtils;->getRadioAreaFMMinFreq(II)I

    move-result v1

    .line 162
    invoke-static {v0, p0}, Lcom/android/fmradio/utils/FmUtils;->getRadioAreaFMMaxFreq(II)I

    move-result v2

    .line 163
    invoke-static {v0, p0}, Lcom/android/fmradio/utils/FmUtils;->getRadioAreaFMStep(II)I

    move-result p0

    goto :goto_0

    .line 165
    :cond_0
    invoke-static {v0}, Lcom/android/fmradio/utils/FmUtils;->getRadioAreaAMMinFreq(I)I

    move-result v1

    .line 166
    invoke-static {v0}, Lcom/android/fmradio/utils/FmUtils;->getRadioAreaAMMaxFreq(I)I

    move-result v2

    .line 167
    invoke-static {v0}, Lcom/android/fmradio/utils/FmUtils;->getRadioAreaAMStep(I)I

    move-result p0

    :goto_0
    sub-int p0, p1, p0

    if-ge p0, v1, :cond_1

    move p0, v2

    :cond_1
    return p0
.end method

.method public static computeFrequency(Landroid/content/Context;I)F
    .locals 3

    .line 201
    invoke-static {p0}, Lcom/android/fmradio/database/FmStation;->getCurrentBand(Landroid/content/Context;)I

    move-result p0

    .line 202
    sget-object v0, Lcom/android/fmradio/utils/FmUtils;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "station: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v2, " - band: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 203
    invoke-static {p0}, Lcom/android/fmradio/utils/FmUtils;->isFMBand(I)Z

    move-result p0

    if-eqz p0, :cond_0

    int-to-float p0, p1

    const/high16 p1, 0x42c80000    # 100.0f

    div-float/2addr p0, p1

    return p0

    :cond_0
    int-to-float p0, p1

    return p0
.end method

.method public static computeIncreaseStation(Landroid/content/Context;I)I
    .locals 4

    .line 122
    invoke-static {}, Lcom/android/fmradio/utils/FmUtils;->getRadioArea()I

    move-result v0

    .line 123
    invoke-static {p0}, Lcom/android/fmradio/database/FmStation;->getCurrentBand(Landroid/content/Context;)I

    move-result p0

    .line 124
    sget-object v1, Lcom/android/fmradio/utils/FmUtils;->TAG:Ljava/lang/String;

    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2}, Ljava/lang/StringBuilder;-><init>()V

    const-string v3, "radioArea: "

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2, v0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v3, " - band: "

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2, p0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v2

    invoke-static {v1, v2}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 129
    invoke-static {p0}, Lcom/android/fmradio/utils/FmUtils;->isFMBand(I)Z

    move-result v1

    if-eqz v1, :cond_0

    .line 130
    invoke-static {v0, p0}, Lcom/android/fmradio/utils/FmUtils;->getRadioAreaFMMinFreq(II)I

    move-result v1

    .line 131
    invoke-static {v0, p0}, Lcom/android/fmradio/utils/FmUtils;->getRadioAreaFMMaxFreq(II)I

    move-result v2

    .line 132
    invoke-static {v0, p0}, Lcom/android/fmradio/utils/FmUtils;->getRadioAreaFMStep(II)I

    move-result p0

    goto :goto_0

    .line 134
    :cond_0
    invoke-static {v0}, Lcom/android/fmradio/utils/FmUtils;->getRadioAreaAMMinFreq(I)I

    move-result v1

    .line 135
    invoke-static {v0}, Lcom/android/fmradio/utils/FmUtils;->getRadioAreaAMMaxFreq(I)I

    move-result v2

    .line 136
    invoke-static {v0}, Lcom/android/fmradio/utils/FmUtils;->getRadioAreaAMStep(I)I

    move-result p0

    :goto_0
    add-int/2addr p0, p1

    if-le p0, v2, :cond_1

    move p0, v1

    :cond_1
    return p0
.end method

.method public static computeStation(Landroid/content/Context;F)I
    .locals 3

    .line 184
    invoke-static {p0}, Lcom/android/fmradio/database/FmStation;->getCurrentBand(Landroid/content/Context;)I

    move-result p0

    .line 185
    sget-object v0, Lcom/android/fmradio/utils/FmUtils;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "frequency: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(F)Ljava/lang/StringBuilder;

    const-string v2, " - band: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 186
    invoke-static {p0}, Lcom/android/fmradio/utils/FmUtils;->isFMBand(I)Z

    move-result p0

    if-eqz p0, :cond_0

    const/high16 p0, 0x42c80000    # 100.0f

    mul-float/2addr p1, p0

    float-to-int p0, p1

    return p0

    :cond_0
    float-to-int p0, p1

    return p0
.end method

.method public static formatStation(Landroid/content/Context;I)Ljava/lang/String;
    .locals 3

    .line 217
    invoke-static {p0}, Lcom/android/fmradio/database/FmStation;->getCurrentBand(Landroid/content/Context;)I

    move-result p0

    .line 218
    sget-object v0, Lcom/android/fmradio/utils/FmUtils;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "station: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v2, " - band: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 219
    invoke-static {p0}, Lcom/android/fmradio/utils/FmUtils;->isFMBand(I)Z

    move-result p0

    if-eqz p0, :cond_0

    int-to-float p0, p1

    const/high16 p1, 0x42c80000    # 100.0f

    div-float/2addr p0, p1

    .line 223
    invoke-static {}, Lcom/android/fmradio/utils/FmUtils;->getRadioArea()I

    .line 224
    new-instance p1, Ljava/text/DecimalFormat;

    const-string v0, "0.00"

    invoke-direct {p1, v0}, Ljava/text/DecimalFormat;-><init>(Ljava/lang/String;)V

    .line 225
    new-instance v0, Ljava/text/DecimalFormatSymbols;

    sget-object v1, Ljava/util/Locale;->US:Ljava/util/Locale;

    invoke-direct {v0, v1}, Ljava/text/DecimalFormatSymbols;-><init>(Ljava/util/Locale;)V

    invoke-virtual {p1, v0}, Ljava/text/DecimalFormat;->setDecimalFormatSymbols(Ljava/text/DecimalFormatSymbols;)V

    float-to-double v0, p0

    .line 226
    invoke-virtual {p1, v0, v1}, Ljava/text/DecimalFormat;->format(D)Ljava/lang/String;

    move-result-object p0

    return-object p0

    .line 228
    :cond_0
    invoke-static {p1}, Ljava/lang/String;->valueOf(I)Ljava/lang/String;

    move-result-object p0

    return-object p0
.end method

.method public static getAppBgStyle()I
    .locals 1

    const-string v0, "lixinhang2"

    .line 436
    invoke-static {v0}, Landroid/qf/os/QFApi;->isClientProduct(Ljava/lang/String;)Z

    move-result v0

    if-eqz v0, :cond_0

    const/4 v0, 0x2

    return v0

    :cond_0
    const/4 v0, 0x1

    return v0
.end method

.method public static getIsSpeakerModeOnFocusLost(Landroid/content/Context;)Z
    .locals 2

    .line 239
    invoke-static {p0}, Landroid/preference/PreferenceManager;->getDefaultSharedPreferences(Landroid/content/Context;)Landroid/content/SharedPreferences;

    move-result-object p0

    const-string v0, "fm_is_speaker_mode"

    const/4 v1, 0x0

    .line 241
    invoke-interface {p0, v0, v1}, Landroid/content/SharedPreferences;->getBoolean(Ljava/lang/String;Z)Z

    move-result p0

    return p0
.end method

.method public static getNavigationBarHeight(Landroid/content/Context;)I
    .locals 4

    .line 479
    invoke-static {}, Landroid/qf/os/QFApi;->getNavigationBarHeight()I

    move-result p0

    .line 490
    invoke-static {}, Landroid/qf/os/QFApi;->getProjectType()Ljava/lang/String;

    move-result-object v0

    const-string v1, "navi1"

    invoke-virtual {v0, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v0

    if-eqz v0, :cond_0

    invoke-static {}, Landroid/qf/os/QFApi;->getCustomPlatform()Ljava/lang/String;

    move-result-object v0

    const-string v1, "6125"

    invoke-virtual {v0, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v0

    if-nez v0, :cond_1

    .line 491
    :cond_0
    invoke-static {}, Landroid/qf/os/QFApi;->getProjectType()Ljava/lang/String;

    move-result-object v0

    const-string v1, "navi2"

    invoke-virtual {v0, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v0

    if-eqz v0, :cond_2

    :cond_1
    const/4 v0, 0x0

    const-string v1, "persist.sys.navigation_bar_height"

    .line 492
    invoke-static {v1, v0}, Landroid/os/SystemProperties;->getInt(Ljava/lang/String;I)I

    move-result v0

    .line 493
    sget-object v1, Lcom/android/fmradio/utils/FmUtils;->TAG:Ljava/lang/String;

    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2}, Ljava/lang/StringBuilder;-><init>()V

    const-string v3, "getNavigationBarHeight - customNaviBarHeight: "

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2, v0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v2

    invoke-static {v1, v2}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    if-eqz v0, :cond_2

    move p0, v0

    .line 498
    :cond_2
    sget-object v0, Lcom/android/fmradio/utils/FmUtils;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "getNavigationBarHeight - navigationBarHeight: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    return p0
.end method

.method public static getRadioArea()I
    .locals 2

    const-string v0, "persist.sys.radio_area"

    const/4 v1, 0x4

    .line 267
    invoke-static {v0, v1}, Landroid/os/SystemProperties;->getInt(Ljava/lang/String;I)I

    move-result v0

    return v0
.end method

.method public static getRadioAreaAMMaxFreq(I)I
    .locals 1

    .line 347
    sget-object v0, Lcom/android/fmradio/FmConstants;->RADIO_AREA_AM_MAX_FREQ:[I

    aget p0, v0, p0

    return p0
.end method

.method public static getRadioAreaAMMinFreq(I)I
    .locals 1

    .line 337
    sget-object v0, Lcom/android/fmradio/FmConstants;->RADIO_AREA_AM_MIN_FREQ:[I

    aget p0, v0, p0

    return p0
.end method

.method public static getRadioAreaAMStep(I)I
    .locals 1

    .line 357
    sget-object v0, Lcom/android/fmradio/FmConstants;->RADIO_AREA_AM_STEP:[I

    aget p0, v0, p0

    return p0
.end method

.method public static getRadioAreaFMMaxFreq(II)I
    .locals 1

    const/4 v0, 0x3

    if-ne p0, v0, :cond_1

    const/4 v0, 0x1

    if-eq p1, v0, :cond_0

    const/4 v0, 0x2

    if-ne p1, v0, :cond_1

    :cond_0
    const/16 p0, 0x2a30

    return p0

    .line 303
    :cond_1
    sget-object p1, Lcom/android/fmradio/FmConstants;->RADIO_AREA_FM_MAX_FREQ:[I

    aget p0, p1, p0

    return p0
.end method

.method public static getRadioAreaFMMinFreq(II)I
    .locals 1

    const/4 v0, 0x3

    if-ne p0, v0, :cond_1

    const/4 v0, 0x1

    if-eq p1, v0, :cond_0

    const/4 v0, 0x2

    if-ne p1, v0, :cond_1

    :cond_0
    const/16 p0, 0x222e

    return p0

    .line 283
    :cond_1
    sget-object p1, Lcom/android/fmradio/FmConstants;->RADIO_AREA_FM_MIN_FREQ:[I

    aget p0, p1, p0

    return p0
.end method

.method public static getRadioAreaFMStep(II)I
    .locals 1

    const/4 v0, 0x3

    if-ne p0, v0, :cond_1

    const/4 v0, 0x1

    if-eq p1, v0, :cond_0

    const/4 v0, 0x2

    if-ne p1, v0, :cond_1

    :cond_0
    const/4 p0, 0x5

    return p0

    .line 323
    :cond_1
    sget-object p1, Lcom/android/fmradio/FmConstants;->RADIO_AREA_FM_STEP:[I

    aget p0, p1, p0

    return p0
.end method

.method public static getRadioType()I
    .locals 5

    const-string v0, "persist.sys.qf.mcu.version"

    .line 522
    invoke-static {v0}, Landroid/os/SystemProperties;->get(Ljava/lang/String;)Ljava/lang/String;

    move-result-object v0

    .line 523
    sget-object v1, Lcom/android/fmradio/utils/FmUtils;->TAG:Ljava/lang/String;

    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2}, Ljava/lang/StringBuilder;-><init>()V

    const-string v3, "getRadioType - mcuVersion: "

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v2

    invoke-static {v1, v2}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    const-string v1, "\\."

    .line 525
    invoke-virtual {v0, v1}, Ljava/lang/String;->split(Ljava/lang/String;)[Ljava/lang/String;

    move-result-object v0

    const/4 v1, 0x1

    if-eqz v0, :cond_1

    .line 527
    array-length v2, v0

    sub-int/2addr v2, v1

    aget-object v0, v0, v2

    .line 528
    sget-object v2, Lcom/android/fmradio/utils/FmUtils;->TAG:Ljava/lang/String;

    new-instance v3, Ljava/lang/StringBuilder;

    invoke-direct {v3}, Ljava/lang/StringBuilder;-><init>()V

    const-string v4, "getRadioType - typeStr: "

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v3, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v3}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v3

    invoke-static {v2, v3}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    if-eqz v0, :cond_1

    .line 530
    invoke-virtual {v0}, Ljava/lang/String;->length()I

    move-result v2

    const/4 v3, 0x6

    if-lt v2, v3, :cond_1

    const/4 v2, 0x2

    .line 532
    invoke-virtual {v0, v2}, Ljava/lang/String;->charAt(I)C

    move-result v0

    invoke-static {v0}, Ljava/lang/String;->valueOf(C)Ljava/lang/String;

    move-result-object v0

    .line 533
    invoke-static {v0}, Landroid/text/TextUtils;->isEmpty(Ljava/lang/CharSequence;)Z

    move-result v2

    if-nez v2, :cond_1

    invoke-virtual {v0}, Ljava/lang/String;->length()I

    move-result v2

    if-ne v2, v1, :cond_1

    const/4 v1, 0x0

    .line 534
    invoke-virtual {v0, v1}, Ljava/lang/String;->charAt(I)C

    move-result v2

    invoke-static {v2}, Ljava/lang/Character;->isDigit(C)Z

    move-result v2

    if-eqz v2, :cond_0

    .line 535
    invoke-static {v0}, Ljava/lang/Integer;->parseInt(Ljava/lang/String;)I

    move-result v1

    goto :goto_0

    .line 537
    :cond_0
    invoke-virtual {v0, v1}, Ljava/lang/String;->charAt(I)C

    move-result v0

    add-int/lit8 v0, v0, 0xa

    add-int/lit8 v1, v0, -0x61

    .line 542
    :cond_1
    :goto_0
    sget-object v0, Lcom/android/fmradio/utils/FmUtils;->TAG:Ljava/lang/String;

    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2}, Ljava/lang/StringBuilder;-><init>()V

    const-string v3, "getRadioType - radioType: "

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2, v1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v2

    invoke-static {v0, v2}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    return v1
.end method

.method public static getStatusBarHeight(Landroid/content/Context;)I
    .locals 3

    .line 462
    invoke-static {}, Landroid/qf/os/QFApi;->getStatusBarHeight()I

    move-result p0

    .line 472
    sget-object v0, Lcom/android/fmradio/utils/FmUtils;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "getStatusBarHeight - statusHeight: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    return p0
.end method

.method public static int2Bytes(II)[B
    .locals 3

    .line 413
    new-array v0, p1, [B

    const/4 v1, 0x0

    :goto_0
    if-ge v1, p1, :cond_0

    mul-int/lit8 v2, v1, 0x8

    shr-int v2, p0, v2

    and-int/lit16 v2, v2, 0xff

    int-to-byte v2, v2

    .line 415
    aput-byte v2, v0, v1

    add-int/lit8 v1, v1, 0x1

    goto :goto_0

    :cond_0
    return-object v0
.end method

.method public static isClientProduct(Ljava/lang/String;)Z
    .locals 0

    .line 427
    invoke-static {p0}, Landroid/qf/os/QFApi;->isClientProduct(Ljava/lang/String;)Z

    move-result p0

    return p0
.end method

.method public static isFMBand(I)Z
    .locals 1

    if-ltz p0, :cond_0

    const/4 v0, 0x2

    if-gt p0, v0, :cond_0

    const/4 p0, 0x1

    goto :goto_0

    :cond_0
    const/4 p0, 0x0

    :goto_0
    return p0
.end method

.method public static isLongProduct()Z
    .locals 2

    .line 450
    invoke-static {}, Landroid/qf/os/QFApi;->getProductName()Ljava/lang/String;

    move-result-object v0

    .line 451
    invoke-static {v0}, Landroid/text/TextUtils;->isEmpty(Ljava/lang/CharSequence;)Z

    move-result v1

    if-nez v1, :cond_1

    const-string v1, "jitu2_1600x720"

    invoke-virtual {v0, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v1

    if-nez v1, :cond_1

    const-string v1, "1600x720"

    invoke-virtual {v0, v1}, Ljava/lang/String;->contains(Ljava/lang/CharSequence;)Z

    move-result v1

    if-nez v1, :cond_0

    const-string v1, "1920x720"

    .line 452
    invoke-virtual {v0, v1}, Ljava/lang/String;->contains(Ljava/lang/CharSequence;)Z

    move-result v1

    if-nez v1, :cond_0

    const-string v1, "alphard_1920x1080"

    invoke-virtual {v0, v1}, Ljava/lang/String;->contains(Ljava/lang/CharSequence;)Z

    move-result v0

    if-eqz v0, :cond_1

    :cond_0
    const/4 v0, 0x1

    return v0

    :cond_1
    const/4 v0, 0x0

    return v0
.end method

.method public static isPTYEnable()Z
    .locals 1

    .line 41
    sget-boolean v0, Lcom/android/fmradio/utils/FmUtils;->mPTYEnable:Z

    return v0
.end method

.method public static isRDSEnable()Z
    .locals 2

    const-string v0, "persist.sys.qf.rds_switch"

    const/4 v1, 0x0

    .line 37
    invoke-static {v0, v1}, Landroid/os/SystemProperties;->getBoolean(Ljava/lang/String;Z)Z

    move-result v0

    return v0
.end method

.method public static isValidStation(II)Z
    .locals 5

    .line 86
    invoke-static {}, Lcom/android/fmradio/utils/FmUtils;->getRadioArea()I

    move-result v0

    .line 88
    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "isValidStation - station: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v2, " - radioArea: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, v0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v2, " - band: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    const-string v2, "FmUtils"

    invoke-static {v2, v1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    .line 93
    invoke-static {p1}, Lcom/android/fmradio/utils/FmUtils;->isFMBand(I)Z

    move-result v1

    if-eqz v1, :cond_0

    .line 94
    invoke-static {v0, p1}, Lcom/android/fmradio/utils/FmUtils;->getRadioAreaFMMinFreq(II)I

    move-result v1

    .line 95
    invoke-static {v0, p1}, Lcom/android/fmradio/utils/FmUtils;->getRadioAreaFMMaxFreq(II)I

    move-result v3

    .line 96
    invoke-static {v0, p1}, Lcom/android/fmradio/utils/FmUtils;->getRadioAreaFMStep(II)I

    move-result p1

    goto :goto_0

    .line 98
    :cond_0
    invoke-static {v0}, Lcom/android/fmradio/utils/FmUtils;->getRadioAreaAMMinFreq(I)I

    move-result v1

    .line 99
    invoke-static {v0}, Lcom/android/fmradio/utils/FmUtils;->getRadioAreaAMMaxFreq(I)I

    move-result v3

    .line 100
    invoke-static {v0}, Lcom/android/fmradio/utils/FmUtils;->getRadioAreaAMStep(I)I

    move-result p1

    .line 103
    :goto_0
    new-instance v0, Ljava/lang/StringBuilder;

    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    const-string v4, "isValidStation - minFreq: "

    invoke-virtual {v0, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v4, "- maxFreq: "

    invoke-virtual {v0, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v0, v3}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v4, " - step: "

    invoke-virtual {v0, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v0, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    invoke-static {v2, v0}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    const/4 v0, 0x0

    if-lt p0, v1, :cond_1

    if-gt p0, v3, :cond_1

    const/4 v3, 0x1

    goto :goto_1

    :cond_1
    move v3, v0

    :goto_1
    if-eqz v3, :cond_2

    sub-int/2addr p0, v1

    .line 106
    rem-int/2addr p0, p1

    if-eqz p0, :cond_2

    goto :goto_2

    :cond_2
    move v0, v3

    .line 110
    :goto_2
    new-instance p0, Ljava/lang/StringBuilder;

    invoke-direct {p0}, Ljava/lang/StringBuilder;-><init>()V

    const-string p1, "isValidStation - isValid: "

    invoke-virtual {p0, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {p0, v0}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    invoke-virtual {p0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p0

    invoke-static {v2, p0}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    return v0
.end method

.method public static isValidStation(Landroid/content/Context;I)Z
    .locals 4

    .line 55
    invoke-static {}, Lcom/android/fmradio/utils/FmUtils;->getRadioArea()I

    move-result v0

    .line 57
    invoke-static {p0}, Lcom/android/fmradio/database/FmStation;->getCurrentBand(Landroid/content/Context;)I

    move-result p0

    .line 58
    sget-object v1, Lcom/android/fmradio/utils/FmUtils;->TAG:Ljava/lang/String;

    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2}, Ljava/lang/StringBuilder;-><init>()V

    const-string v3, "isValidStation - station: "

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v3, " - radioArea: "

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2, v0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v3, " - band: "

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2, p0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v2

    invoke-static {v1, v2}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    .line 63
    invoke-static {p0}, Lcom/android/fmradio/utils/FmUtils;->isFMBand(I)Z

    move-result v1

    if-eqz v1, :cond_0

    .line 64
    invoke-static {v0, p0}, Lcom/android/fmradio/utils/FmUtils;->getRadioAreaFMMinFreq(II)I

    move-result v1

    .line 65
    invoke-static {v0, p0}, Lcom/android/fmradio/utils/FmUtils;->getRadioAreaFMMaxFreq(II)I

    move-result v2

    .line 66
    invoke-static {v0, p0}, Lcom/android/fmradio/utils/FmUtils;->getRadioAreaFMStep(II)I

    move-result p0

    goto :goto_0

    .line 68
    :cond_0
    invoke-static {v0}, Lcom/android/fmradio/utils/FmUtils;->getRadioAreaAMMinFreq(I)I

    move-result v1

    .line 69
    invoke-static {v0}, Lcom/android/fmradio/utils/FmUtils;->getRadioAreaAMMaxFreq(I)I

    move-result v2

    .line 70
    invoke-static {v0}, Lcom/android/fmradio/utils/FmUtils;->getRadioAreaAMStep(I)I

    move-result p0

    .line 73
    :goto_0
    new-instance v0, Ljava/lang/StringBuilder;

    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    const-string v3, "isValidStation - minFreq: "

    invoke-virtual {v0, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v3, "- maxFreq: "

    invoke-virtual {v0, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v0, v2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v3, " - step: "

    invoke-virtual {v0, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v0, p0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    const-string v3, "FmUtils"

    invoke-static {v3, v0}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    const/4 v0, 0x0

    if-lt p1, v1, :cond_1

    if-gt p1, v2, :cond_1

    const/4 v2, 0x1

    goto :goto_1

    :cond_1
    move v2, v0

    :goto_1
    if-eqz v2, :cond_2

    sub-int/2addr p1, v1

    .line 76
    rem-int/2addr p1, p0

    if-eqz p1, :cond_2

    goto :goto_2

    :cond_2
    move v0, v2

    .line 80
    :goto_2
    new-instance p0, Ljava/lang/StringBuilder;

    invoke-direct {p0}, Ljava/lang/StringBuilder;-><init>()V

    const-string p1, "isValidStation - isValid: "

    invoke-virtual {p0, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {p0, v0}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    invoke-virtual {p0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p0

    invoke-static {v3, p0}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    return v0
.end method

.method public static onTest(Landroid/content/Context;Z)V
    .locals 3

    .line 510
    new-instance v0, Landroid/content/Intent;

    invoke-direct {v0}, Landroid/content/Intent;-><init>()V

    const-string v1, "com.qf.test"

    const-string v2, "com.qf.test.FactoryTestMainActivity"

    .line 511
    invoke-virtual {v0, v1, v2}, Landroid/content/Intent;->setClassName(Ljava/lang/String;Ljava/lang/String;)Landroid/content/Intent;

    const-string v1, "hide_ddr"

    .line 512
    invoke-virtual {v0, v1, p1}, Landroid/content/Intent;->putExtra(Ljava/lang/String;Z)Landroid/content/Intent;

    const/high16 p1, 0x10000000

    .line 513
    invoke-virtual {v0, p1}, Landroid/content/Intent;->setFlags(I)Landroid/content/Intent;

    .line 514
    invoke-virtual {p0, v0}, Landroid/content/Context;->startActivity(Landroid/content/Intent;)V

    return-void
.end method

.method public static setIsSpeakerModeOnFocusLost(Landroid/content/Context;Z)V
    .locals 1

    .line 251
    invoke-static {p0}, Landroid/preference/PreferenceManager;->getDefaultSharedPreferences(Landroid/content/Context;)Landroid/content/SharedPreferences;

    move-result-object p0

    .line 252
    invoke-interface {p0}, Landroid/content/SharedPreferences;->edit()Landroid/content/SharedPreferences$Editor;

    move-result-object p0

    const-string v0, "fm_is_speaker_mode"

    .line 253
    invoke-interface {p0, v0, p1}, Landroid/content/SharedPreferences$Editor;->putBoolean(Ljava/lang/String;Z)Landroid/content/SharedPreferences$Editor;

    .line 254
    invoke-interface {p0}, Landroid/content/SharedPreferences$Editor;->commit()Z

    return-void
.end method

.method public static setPTYEnable(Z)V
    .locals 0

    .line 45
    sput-boolean p0, Lcom/android/fmradio/utils/FmUtils;->mPTYEnable:Z

    return-void
.end method
