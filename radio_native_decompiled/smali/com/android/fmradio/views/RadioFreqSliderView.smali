.class public Lcom/android/fmradio/views/RadioFreqSliderView;
.super Landroid/widget/LinearLayout;
.source "RadioFreqSliderView.java"

# interfaces
.implements Landroid/view/View$OnTouchListener;


# static fields
.field private static STEP_GAP_WIDTH:I

.field private static final TAG:Ljava/lang/String;


# instance fields
.field private mActivity:Lcom/android/fmradio/FmMainActivity;

.field private mContext:Landroid/content/Context;

.field private mLayoutFreqTitle:Lcom/android/fmradio/views/NoCancelActionLinearLayout;

.field private mScaleImageWidth:I

.field private mScaleToLeft:I

.field private mTVFreqTitle1:Landroid/widget/TextView;

.field private mTVFreqTitle2:Landroid/widget/TextView;

.field private mTVFreqTitle3:Landroid/widget/TextView;

.field private mTVFreqTitle4:Landroid/widget/TextView;

.field private mTVFreqTitle5:Landroid/widget/TextView;

.field private mTouchDownPreX:I

.field private mTouchDownX:I

.field private mTouchMovePreGap:I


# direct methods
.method static constructor <clinit>()V
    .locals 1

    .line 21
    const-class v0, Lcom/android/fmradio/views/RadioFreqSliderView;

    invoke-virtual {v0}, Ljava/lang/Class;->getSimpleName()Ljava/lang/String;

    move-result-object v0

    sput-object v0, Lcom/android/fmradio/views/RadioFreqSliderView;->TAG:Ljava/lang/String;

    const/4 v0, 0x0

    .line 24
    sput v0, Lcom/android/fmradio/views/RadioFreqSliderView;->STEP_GAP_WIDTH:I

    return-void
.end method

.method public constructor <init>(Landroid/content/Context;Landroid/util/AttributeSet;)V
    .locals 2

    .line 53
    invoke-direct {p0, p1, p2}, Landroid/widget/LinearLayout;-><init>(Landroid/content/Context;Landroid/util/AttributeSet;)V

    const/4 p2, 0x0

    .line 38
    iput p2, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mScaleToLeft:I

    .line 40
    iput p2, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mScaleImageWidth:I

    .line 43
    iput p2, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mTouchDownX:I

    .line 44
    iput p2, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mTouchDownPreX:I

    .line 46
    iput p2, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mTouchMovePreGap:I

    .line 55
    iput-object p1, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mContext:Landroid/content/Context;

    .line 57
    invoke-direct {p0}, Lcom/android/fmradio/views/RadioFreqSliderView;->initData()V

    .line 59
    invoke-static {p1}, Landroid/view/LayoutInflater;->from(Landroid/content/Context;)Landroid/view/LayoutInflater;

    move-result-object p1

    const v0, 0x7f0b002c

    const/4 v1, 0x0

    invoke-virtual {p1, v0, v1, p2}, Landroid/view/LayoutInflater;->inflate(ILandroid/view/ViewGroup;Z)Landroid/view/View;

    move-result-object p1

    .line 60
    new-instance p2, Landroid/widget/LinearLayout$LayoutParams;

    const/4 v0, -0x1

    const/4 v1, -0x2

    invoke-direct {p2, v0, v1}, Landroid/widget/LinearLayout$LayoutParams;-><init>(II)V

    invoke-virtual {p1, p2}, Landroid/view/View;->setLayoutParams(Landroid/view/ViewGroup$LayoutParams;)V

    .line 62
    invoke-direct {p0, p1}, Lcom/android/fmradio/views/RadioFreqSliderView;->initView(Landroid/view/View;)V

    .line 64
    invoke-virtual {p0, p1}, Lcom/android/fmradio/views/RadioFreqSliderView;->addView(Landroid/view/View;)V

    return-void
.end method

.method private initData()V
    .locals 2

    .line 68
    invoke-virtual {p0}, Lcom/android/fmradio/views/RadioFreqSliderView;->getResources()Landroid/content/res/Resources;

    move-result-object v0

    const v1, 0x7f060f03

    invoke-virtual {v0, v1}, Landroid/content/res/Resources;->getDimension(I)F

    move-result v0

    float-to-int v0, v0

    iput v0, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mScaleToLeft:I

    .line 71
    invoke-virtual {p0}, Lcom/android/fmradio/views/RadioFreqSliderView;->getResources()Landroid/content/res/Resources;

    move-result-object v0

    const v1, 0x7f060f02

    invoke-virtual {v0, v1}, Landroid/content/res/Resources;->getDimension(I)F

    move-result v0

    float-to-int v0, v0

    iput v0, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mScaleImageWidth:I

    .line 73
    invoke-virtual {p0}, Lcom/android/fmradio/views/RadioFreqSliderView;->getResources()Landroid/content/res/Resources;

    move-result-object v0

    const v1, 0x7f060704

    invoke-virtual {v0, v1}, Landroid/content/res/Resources;->getDimension(I)F

    move-result v0

    float-to-int v0, v0

    sput v0, Lcom/android/fmradio/views/RadioFreqSliderView;->STEP_GAP_WIDTH:I

    return-void
.end method

.method private initFreqTitle()V
    .locals 1

    const v0, 0x7f0800cf

    .line 99
    invoke-virtual {p0, v0}, Lcom/android/fmradio/views/RadioFreqSliderView;->findViewById(I)Landroid/view/View;

    move-result-object v0

    check-cast v0, Landroid/widget/TextView;

    iput-object v0, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mTVFreqTitle1:Landroid/widget/TextView;

    const v0, 0x7f0800d0

    .line 100
    invoke-virtual {p0, v0}, Lcom/android/fmradio/views/RadioFreqSliderView;->findViewById(I)Landroid/view/View;

    move-result-object v0

    check-cast v0, Landroid/widget/TextView;

    iput-object v0, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mTVFreqTitle2:Landroid/widget/TextView;

    const v0, 0x7f0800d1

    .line 101
    invoke-virtual {p0, v0}, Lcom/android/fmradio/views/RadioFreqSliderView;->findViewById(I)Landroid/view/View;

    move-result-object v0

    check-cast v0, Landroid/widget/TextView;

    iput-object v0, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mTVFreqTitle3:Landroid/widget/TextView;

    const v0, 0x7f0800d2

    .line 102
    invoke-virtual {p0, v0}, Lcom/android/fmradio/views/RadioFreqSliderView;->findViewById(I)Landroid/view/View;

    move-result-object v0

    check-cast v0, Landroid/widget/TextView;

    iput-object v0, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mTVFreqTitle4:Landroid/widget/TextView;

    const v0, 0x7f0800d3

    .line 103
    invoke-virtual {p0, v0}, Lcom/android/fmradio/views/RadioFreqSliderView;->findViewById(I)Landroid/view/View;

    move-result-object v0

    check-cast v0, Landroid/widget/TextView;

    iput-object v0, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mTVFreqTitle5:Landroid/widget/TextView;

    .line 105
    invoke-virtual {p0}, Lcom/android/fmradio/views/RadioFreqSliderView;->setFreqTitle()V

    return-void
.end method

.method private initView(Landroid/view/View;)V
    .locals 1

    const v0, 0x7f080082

    .line 77
    invoke-virtual {p1, v0}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object p1

    check-cast p1, Lcom/android/fmradio/views/NoCancelActionLinearLayout;

    iput-object p1, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mLayoutFreqTitle:Lcom/android/fmradio/views/NoCancelActionLinearLayout;

    .line 78
    iget-object p1, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mLayoutFreqTitle:Lcom/android/fmradio/views/NoCancelActionLinearLayout;

    const/4 v0, 0x1

    invoke-virtual {p1, v0}, Lcom/android/fmradio/views/NoCancelActionLinearLayout;->requestDisallowInterceptTouchEvent(Z)V

    .line 79
    iget-object p1, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mLayoutFreqTitle:Lcom/android/fmradio/views/NoCancelActionLinearLayout;

    invoke-virtual {p1, p0}, Lcom/android/fmradio/views/NoCancelActionLinearLayout;->setOnTouchListener(Landroid/view/View$OnTouchListener;)V

    return-void
.end method


# virtual methods
.method public initSlider()V
    .locals 0

    .line 83
    invoke-virtual {p0}, Lcom/android/fmradio/views/RadioFreqSliderView;->setSlider()V

    .line 85
    invoke-direct {p0}, Lcom/android/fmradio/views/RadioFreqSliderView;->initFreqTitle()V

    return-void
.end method

.method public onTouch(Landroid/view/View;Landroid/view/MotionEvent;)Z
    .locals 5

    .line 176
    invoke-static {}, Landroid/qf/os/QFApi;->isBTPhoneStartup()Z

    move-result p1

    const/4 v0, 0x1

    if-eqz p1, :cond_0

    return v0

    .line 180
    :cond_0
    iget-object p1, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {p1}, Lcom/android/fmradio/FmMainActivity;->stopScan()Z

    move-result p1

    if-eqz p1, :cond_1

    return v0

    .line 184
    :cond_1
    invoke-virtual {p2}, Landroid/view/MotionEvent;->getAction()I

    move-result p1

    .line 186
    invoke-static {}, Lcom/android/fmradio/utils/FmUtils;->getRadioArea()I

    move-result v1

    .line 188
    iget-object v2, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mContext:Landroid/content/Context;

    invoke-static {v2}, Lcom/android/fmradio/database/FmStation;->getCurrentBand(Landroid/content/Context;)I

    move-result v2

    invoke-static {v2}, Lcom/android/fmradio/utils/FmUtils;->isFMBand(I)Z

    move-result v2

    if-eqz v2, :cond_2

    .line 189
    iget-object v2, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    iget v2, v2, Lcom/android/fmradio/FmMainActivity;->mBand:I

    invoke-static {v1, v2}, Lcom/android/fmradio/utils/FmUtils;->getRadioAreaFMStep(II)I

    move-result v1

    goto :goto_0

    .line 191
    :cond_2
    invoke-static {v1}, Lcom/android/fmradio/utils/FmUtils;->getRadioAreaAMStep(I)I

    move-result v1

    .line 193
    :goto_0
    sget-object v2, Lcom/android/fmradio/views/RadioFreqSliderView;->TAG:Ljava/lang/String;

    new-instance v3, Ljava/lang/StringBuilder;

    invoke-direct {v3}, Ljava/lang/StringBuilder;-><init>()V

    const-string v4, "start - action: "

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v3, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v4, " - radioStep: "

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v3, v1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v4, " - mCurrentStation "

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-object v4, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    iget v4, v4, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v3}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v3

    invoke-static {v2, v3}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    const/4 v2, 0x0

    if-nez p1, :cond_3

    .line 196
    invoke-virtual {p2}, Landroid/view/MotionEvent;->getX()F

    move-result p1

    float-to-int p1, p1

    .line 197
    iget p2, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mScaleToLeft:I

    sub-int v0, p1, p2

    if-lez v0, :cond_10

    iget v0, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mScaleImageWidth:I

    add-int/2addr p2, v0

    if-ge p1, p2, :cond_10

    .line 199
    iput p1, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mTouchDownX:I

    .line 200
    iput p1, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mTouchDownPreX:I

    .line 202
    iput v2, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mTouchMovePreGap:I

    goto/16 :goto_6

    :cond_3
    const/4 v3, 0x2

    if-ne p1, v3, :cond_f

    .line 216
    invoke-virtual {p2}, Landroid/view/MotionEvent;->getX()F

    move-result p1

    float-to-int p1, p1

    .line 218
    sget-object p2, Lcom/android/fmradio/views/RadioFreqSliderView;->TAG:Ljava/lang/String;

    new-instance v0, Ljava/lang/StringBuilder;

    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    const-string v3, "clickPointX: "

    invoke-virtual {v0, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v0, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v3, " - mTouchDownX: "

    invoke-virtual {v0, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget v3, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mTouchDownX:I

    invoke-virtual {v0, v3}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v3, " - mTouchDownPreX: "

    invoke-virtual {v0, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget v3, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mTouchDownPreX:I

    invoke-virtual {v0, v3}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    invoke-static {p2, v0}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 222
    iget p2, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mTouchDownPreX:I

    if-lt p1, p2, :cond_5

    if-ge p2, p1, :cond_7

    .line 226
    iget v0, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mTouchDownX:I

    if-ge p2, v0, :cond_4

    .line 227
    iput p2, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mTouchDownX:I

    .line 230
    iput v2, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mTouchMovePreGap:I

    .line 233
    :cond_4
    iput p1, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mTouchDownPreX:I

    goto :goto_1

    :cond_5
    if-le p2, p1, :cond_7

    .line 239
    iget v0, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mTouchDownX:I

    if-le p2, v0, :cond_6

    .line 240
    iput p2, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mTouchDownX:I

    .line 243
    iput v2, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mTouchMovePreGap:I

    .line 246
    :cond_6
    iput p1, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mTouchDownPreX:I

    .line 250
    :cond_7
    :goto_1
    iget p2, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mScaleToLeft:I

    sub-int v0, p1, p2

    if-lez v0, :cond_8

    iget v0, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mScaleImageWidth:I

    add-int/2addr v0, p2

    if-ge p1, v0, :cond_8

    sub-int p2, p1, p2

    goto :goto_3

    .line 252
    :cond_8
    iget p2, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mScaleToLeft:I

    sub-int v0, p1, p2

    if-gtz v0, :cond_9

    goto :goto_2

    .line 254
    :cond_9
    iget v0, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mScaleImageWidth:I

    add-int/2addr p2, v0

    if-lt p1, p2, :cond_a

    move p2, v0

    goto :goto_3

    :cond_a
    :goto_2
    move p2, v2

    .line 258
    :goto_3
    iget v0, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mTouchDownX:I

    sub-int/2addr p2, v0

    invoke-static {p2}, Ljava/lang/Math;->abs(I)I

    move-result p2

    .line 261
    sget v0, Lcom/android/fmradio/views/RadioFreqSliderView;->STEP_GAP_WIDTH:I

    div-int v0, p2, v0

    int-to-float v0, v0

    invoke-static {v0}, Ljava/lang/Math;->round(F)I

    move-result v0

    .line 263
    iget v3, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mTouchMovePreGap:I

    if-eq v3, v0, :cond_e

    .line 264
    iget-object v3, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    iget v3, v3, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    .line 267
    iget v4, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mTouchDownX:I

    if-lt p1, v4, :cond_b

    .line 268
    iget p1, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mTouchMovePreGap:I

    sub-int/2addr p1, v0

    invoke-static {p1}, Ljava/lang/Math;->abs(I)I

    move-result p1

    mul-int/2addr p1, v1

    add-int/2addr v3, p1

    goto :goto_4

    .line 270
    :cond_b
    iget p1, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mTouchMovePreGap:I

    sub-int/2addr p1, v0

    invoke-static {p1}, Ljava/lang/Math;->abs(I)I

    move-result p1

    mul-int/2addr p1, v1

    sub-int/2addr v3, p1

    .line 274
    :goto_4
    iput v0, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mTouchMovePreGap:I

    .line 276
    iget-object p1, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    iget p1, p1, Lcom/android/fmradio/FmMainActivity;->mBandMinFreq:I

    if-ge v3, p1, :cond_c

    .line 277
    iget-object p1, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    iget v3, p1, Lcom/android/fmradio/FmMainActivity;->mBandMinFreq:I

    goto :goto_5

    .line 278
    :cond_c
    iget-object p1, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    iget p1, p1, Lcom/android/fmradio/FmMainActivity;->mBandMaxFreq:I

    if-le v3, p1, :cond_d

    .line 279
    iget-object p1, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    iget v3, p1, Lcom/android/fmradio/FmMainActivity;->mBandMaxFreq:I

    .line 282
    :cond_d
    :goto_5
    iget-object p1, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    iput v3, p1, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    .line 284
    iget v1, p1, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    invoke-virtual {p1, v2, v1}, Lcom/android/fmradio/FmMainActivity;->updateStationValue(ZI)V

    .line 287
    :cond_e
    sget-object p1, Lcom/android/fmradio/views/RadioFreqSliderView;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v3, "position: "

    invoke-virtual {v1, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string p2, " - tempStep: "

    invoke-virtual {v1, p2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, v0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string p2, " - mTouchMovePreGap: "

    invoke-virtual {v1, p2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget p2, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mTouchMovePreGap:I

    invoke-virtual {v1, p2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p2

    invoke-static {p1, p2}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    goto :goto_6

    :cond_f
    if-ne p1, v0, :cond_10

    .line 291
    iput v2, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mTouchMovePreGap:I

    .line 293
    iget-object p1, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {p1}, Lcom/android/fmradio/FmMainActivity;->requestAudioFocus()V

    .line 295
    iget-object p1, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    iget p2, p1, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    invoke-virtual {p1, p2}, Lcom/android/fmradio/FmMainActivity;->tuneStation(I)V

    .line 298
    :cond_10
    :goto_6
    iget-object p1, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {p1}, Lcom/android/fmradio/FmMainActivity;->setButtonHighLight()V

    .line 300
    sget-object p1, Lcom/android/fmradio/views/RadioFreqSliderView;->TAG:Ljava/lang/String;

    new-instance p2, Ljava/lang/StringBuilder;

    invoke-direct {p2}, Ljava/lang/StringBuilder;-><init>()V

    const-string v0, "end - mCurrentStation "

    invoke-virtual {p2, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-object v0, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    iget v0, v0, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    invoke-virtual {p2, v0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {p2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p2

    invoke-static {p1, p2}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    return v2
.end method

.method public setActivity(Lcom/android/fmradio/FmMainActivity;)V
    .locals 0

    .line 49
    iput-object p1, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    return-void
.end method

.method public setFreqTitle()V
    .locals 8

    .line 114
    iget-object v0, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mContext:Landroid/content/Context;

    invoke-static {v0}, Lcom/android/fmradio/database/FmStation;->getCurrentBand(Landroid/content/Context;)I

    move-result v0

    invoke-static {v0}, Lcom/android/fmradio/utils/FmUtils;->isFMBand(I)Z

    move-result v0

    if-eqz v0, :cond_0

    .line 115
    iget-object v0, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    iget v0, v0, Lcom/android/fmradio/FmMainActivity;->mRadioArea:I

    iget-object v1, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    iget v1, v1, Lcom/android/fmradio/FmMainActivity;->mBand:I

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/FmUtils;->getRadioAreaFMStep(II)I

    move-result v0

    goto :goto_0

    .line 117
    :cond_0
    iget-object v0, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    iget v0, v0, Lcom/android/fmradio/FmMainActivity;->mRadioArea:I

    invoke-static {v0}, Lcom/android/fmradio/utils/FmUtils;->getRadioAreaAMStep(I)I

    move-result v0

    .line 120
    :goto_0
    iget-object v1, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    iget v1, v1, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    mul-int/lit8 v2, v0, 0x14

    sub-int/2addr v1, v2

    .line 121
    iget-object v3, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    iget v3, v3, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    mul-int/lit8 v0, v0, 0xa

    sub-int/2addr v3, v0

    .line 122
    iget-object v4, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    iget v4, v4, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    .line 123
    iget-object v5, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    iget v5, v5, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    add-int/2addr v5, v0

    .line 124
    iget-object v0, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    iget v0, v0, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    add-int/2addr v0, v2

    .line 126
    iget-object v2, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    iget v2, v2, Lcom/android/fmradio/FmMainActivity;->mBandMinFreq:I

    const/4 v6, 0x0

    if-ge v1, v2, :cond_1

    move v1, v6

    .line 130
    :cond_1
    iget-object v2, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    iget v2, v2, Lcom/android/fmradio/FmMainActivity;->mBandMinFreq:I

    if-ge v3, v2, :cond_2

    move v3, v6

    .line 134
    :cond_2
    iget-object v2, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    iget v2, v2, Lcom/android/fmradio/FmMainActivity;->mBandMaxFreq:I

    if-le v5, v2, :cond_3

    move v5, v6

    .line 138
    :cond_3
    iget-object v2, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    iget v2, v2, Lcom/android/fmradio/FmMainActivity;->mBandMaxFreq:I

    if-le v0, v2, :cond_4

    move v0, v6

    :cond_4
    const-string v2, ""

    if-nez v1, :cond_5

    .line 152
    iget-object v1, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mTVFreqTitle1:Landroid/widget/TextView;

    invoke-virtual {v1, v2}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    goto :goto_1

    .line 154
    :cond_5
    iget-object v6, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mTVFreqTitle1:Landroid/widget/TextView;

    iget-object v7, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mContext:Landroid/content/Context;

    invoke-static {v7, v1}, Lcom/android/fmradio/utils/FmUtils;->formatStation(Landroid/content/Context;I)Ljava/lang/String;

    move-result-object v1

    invoke-virtual {v6, v1}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    :goto_1
    if-nez v3, :cond_6

    .line 157
    iget-object v1, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mTVFreqTitle2:Landroid/widget/TextView;

    invoke-virtual {v1, v2}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    goto :goto_2

    .line 159
    :cond_6
    iget-object v1, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mTVFreqTitle2:Landroid/widget/TextView;

    iget-object v6, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mContext:Landroid/content/Context;

    invoke-static {v6, v3}, Lcom/android/fmradio/utils/FmUtils;->formatStation(Landroid/content/Context;I)Ljava/lang/String;

    move-result-object v3

    invoke-virtual {v1, v3}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    .line 161
    :goto_2
    iget-object v1, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mTVFreqTitle3:Landroid/widget/TextView;

    iget-object v3, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mContext:Landroid/content/Context;

    invoke-static {v3, v4}, Lcom/android/fmradio/utils/FmUtils;->formatStation(Landroid/content/Context;I)Ljava/lang/String;

    move-result-object v3

    invoke-virtual {v1, v3}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    if-nez v5, :cond_7

    .line 163
    iget-object v1, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mTVFreqTitle4:Landroid/widget/TextView;

    invoke-virtual {v1, v2}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    goto :goto_3

    .line 165
    :cond_7
    iget-object v1, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mTVFreqTitle4:Landroid/widget/TextView;

    iget-object v3, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mContext:Landroid/content/Context;

    invoke-static {v3, v5}, Lcom/android/fmradio/utils/FmUtils;->formatStation(Landroid/content/Context;I)Ljava/lang/String;

    move-result-object v3

    invoke-virtual {v1, v3}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    :goto_3
    if-nez v0, :cond_8

    .line 168
    iget-object v0, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mTVFreqTitle5:Landroid/widget/TextView;

    invoke-virtual {v0, v2}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    goto :goto_4

    .line 170
    :cond_8
    iget-object v1, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mTVFreqTitle5:Landroid/widget/TextView;

    iget-object v2, p0, Lcom/android/fmradio/views/RadioFreqSliderView;->mContext:Landroid/content/Context;

    invoke-static {v2, v0}, Lcom/android/fmradio/utils/FmUtils;->formatStation(Landroid/content/Context;I)Ljava/lang/String;

    move-result-object v0

    invoke-virtual {v1, v0}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    :goto_4
    return-void
.end method

.method public setSlider()V
    .locals 0

    return-void
.end method
