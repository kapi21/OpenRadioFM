.class public Lcom/android/fmradio/views/RadioFreqImageView;
.super Landroid/widget/LinearLayout;
.source "RadioFreqImageView.java"


# static fields
.field private static final FREQ_IMAGE_ID_ARRAY:[I

.field private static final FREQ_IMAGE_NUM_ARRAY:[I

.field private static final TAG:Ljava/lang/String;


# instance fields
.field private mActivity:Lcom/android/fmradio/FmMainActivity;

.field private mContext:Landroid/content/Context;

.field private mFreqImageNumArray:[Landroid/widget/ImageView;

.field private mIVFreqImagePoint:Landroid/widget/ImageView;

.field private mSkinUpdate:Lcom/qf/skin/manager/interfaces/ISkinUpdate;


# direct methods
.method static constructor <clinit>()V
    .locals 1

    .line 23
    const-class v0, Lcom/android/fmradio/views/RadioFreqImageView;

    invoke-virtual {v0}, Ljava/lang/Class;->getSimpleName()Ljava/lang/String;

    move-result-object v0

    sput-object v0, Lcom/android/fmradio/views/RadioFreqImageView;->TAG:Ljava/lang/String;

    const/16 v0, 0xa

    new-array v0, v0, [I

    .line 25
    fill-array-data v0, :array_0

    sput-object v0, Lcom/android/fmradio/views/RadioFreqImageView;->FREQ_IMAGE_NUM_ARRAY:[I

    const/4 v0, 0x5

    new-array v0, v0, [I

    .line 32
    fill-array-data v0, :array_1

    sput-object v0, Lcom/android/fmradio/views/RadioFreqImageView;->FREQ_IMAGE_ID_ARRAY:[I

    return-void

    :array_0
    .array-data 4
        0x7f070092
        0x7f070093
        0x7f070094
        0x7f070095
        0x7f070096
        0x7f070097
        0x7f070098
        0x7f070099
        0x7f07009a
        0x7f07009b
    .end array-data

    :array_1
    .array-data 4
        0x7f080079
        0x7f08007a
        0x7f08007b
        0x7f08007c
        0x7f08007d
    .end array-data
.end method

.method public constructor <init>(Landroid/content/Context;Landroid/util/AttributeSet;)V
    .locals 3

    .line 80
    invoke-direct {p0, p1, p2}, Landroid/widget/LinearLayout;-><init>(Landroid/content/Context;Landroid/util/AttributeSet;)V

    .line 41
    sget-object p2, Lcom/android/fmradio/views/RadioFreqImageView;->FREQ_IMAGE_ID_ARRAY:[I

    array-length p2, p2

    new-array p2, p2, [Landroid/widget/ImageView;

    iput-object p2, p0, Lcom/android/fmradio/views/RadioFreqImageView;->mFreqImageNumArray:[Landroid/widget/ImageView;

    .line 44
    new-instance p2, Lcom/android/fmradio/views/RadioFreqImageView$1;

    invoke-direct {p2, p0}, Lcom/android/fmradio/views/RadioFreqImageView$1;-><init>(Lcom/android/fmradio/views/RadioFreqImageView;)V

    iput-object p2, p0, Lcom/android/fmradio/views/RadioFreqImageView;->mSkinUpdate:Lcom/qf/skin/manager/interfaces/ISkinUpdate;

    .line 82
    iput-object p1, p0, Lcom/android/fmradio/views/RadioFreqImageView;->mContext:Landroid/content/Context;

    .line 85
    iget-object p2, p0, Lcom/android/fmradio/views/RadioFreqImageView;->mContext:Landroid/content/Context;

    check-cast p2, Landroid/app/Activity;

    invoke-virtual {p2}, Landroid/app/Activity;->isInMultiWindowMode()Z

    move-result p2

    const/4 v0, -0x2

    const/4 v1, 0x0

    const/4 v2, 0x0

    if-eqz p2, :cond_0

    .line 86
    invoke-static {p1}, Landroid/view/LayoutInflater;->from(Landroid/content/Context;)Landroid/view/LayoutInflater;

    move-result-object p1

    const p2, 0x7f0b0035

    invoke-virtual {p1, p2, v2, v1}, Landroid/view/LayoutInflater;->inflate(ILandroid/view/ViewGroup;Z)Landroid/view/View;

    move-result-object p1

    .line 87
    new-instance p2, Landroid/widget/LinearLayout$LayoutParams;

    iget-object v1, p0, Lcom/android/fmradio/views/RadioFreqImageView;->mContext:Landroid/content/Context;

    invoke-virtual {v1}, Landroid/content/Context;->getResources()Landroid/content/res/Resources;

    move-result-object v1

    const v2, 0x7f0600b4

    invoke-virtual {v1, v2}, Landroid/content/res/Resources;->getDimensionPixelSize(I)I

    move-result v1

    invoke-direct {p2, v0, v1}, Landroid/widget/LinearLayout$LayoutParams;-><init>(II)V

    invoke-virtual {p1, p2}, Landroid/view/View;->setLayoutParams(Landroid/view/ViewGroup$LayoutParams;)V

    goto :goto_0

    .line 89
    :cond_0
    invoke-static {p1}, Landroid/view/LayoutInflater;->from(Landroid/content/Context;)Landroid/view/LayoutInflater;

    move-result-object p1

    const p2, 0x7f0b002a

    invoke-virtual {p1, p2, v2, v1}, Landroid/view/LayoutInflater;->inflate(ILandroid/view/ViewGroup;Z)Landroid/view/View;

    move-result-object p1

    .line 90
    new-instance p2, Landroid/widget/LinearLayout$LayoutParams;

    iget-object v1, p0, Lcom/android/fmradio/views/RadioFreqImageView;->mContext:Landroid/content/Context;

    invoke-virtual {v1}, Landroid/content/Context;->getResources()Landroid/content/res/Resources;

    move-result-object v1

    const v2, 0x7f0600c6

    invoke-virtual {v1, v2}, Landroid/content/res/Resources;->getDimensionPixelSize(I)I

    move-result v1

    invoke-direct {p2, v0, v1}, Landroid/widget/LinearLayout$LayoutParams;-><init>(II)V

    invoke-virtual {p1, p2}, Landroid/view/View;->setLayoutParams(Landroid/view/ViewGroup$LayoutParams;)V

    .line 93
    :goto_0
    invoke-direct {p0, p1}, Lcom/android/fmradio/views/RadioFreqImageView;->initView(Landroid/view/View;)V

    .line 95
    invoke-virtual {p0, p1}, Lcom/android/fmradio/views/RadioFreqImageView;->addView(Landroid/view/View;)V

    return-void
.end method

.method static synthetic access$000()Ljava/lang/String;
    .locals 1

    .line 21
    sget-object v0, Lcom/android/fmradio/views/RadioFreqImageView;->TAG:Ljava/lang/String;

    return-object v0
.end method

.method static synthetic access$100(Lcom/android/fmradio/views/RadioFreqImageView;)Landroid/content/Context;
    .locals 0

    .line 21
    iget-object p0, p0, Lcom/android/fmradio/views/RadioFreqImageView;->mContext:Landroid/content/Context;

    return-object p0
.end method

.method private initView(Landroid/view/View;)V
    .locals 3

    const/4 v0, 0x0

    .line 99
    :goto_0
    sget-object v1, Lcom/android/fmradio/views/RadioFreqImageView;->FREQ_IMAGE_ID_ARRAY:[I

    array-length v2, v1

    if-ge v0, v2, :cond_0

    .line 100
    iget-object v2, p0, Lcom/android/fmradio/views/RadioFreqImageView;->mFreqImageNumArray:[Landroid/widget/ImageView;

    aget v1, v1, v0

    invoke-virtual {p1, v1}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object v1

    check-cast v1, Landroid/widget/ImageView;

    aput-object v1, v2, v0

    add-int/lit8 v0, v0, 0x1

    goto :goto_0

    :cond_0
    const v0, 0x7f08007e

    .line 103
    invoke-virtual {p1, v0}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object p1

    check-cast p1, Landroid/widget/ImageView;

    iput-object p1, p0, Lcom/android/fmradio/views/RadioFreqImageView;->mIVFreqImagePoint:Landroid/widget/ImageView;

    .line 105
    iget-object p1, p0, Lcom/android/fmradio/views/RadioFreqImageView;->mContext:Landroid/content/Context;

    invoke-static {p1}, Lcom/android/fmradio/database/FmStation;->getCurrentStation(Landroid/content/Context;)I

    move-result p1

    invoke-virtual {p0, p1}, Lcom/android/fmradio/views/RadioFreqImageView;->updateFreqValue(I)V

    return-void
.end method


# virtual methods
.method protected onAttachedToWindow()V
    .locals 2

    .line 55
    invoke-super {p0}, Landroid/widget/LinearLayout;->onAttachedToWindow()V

    .line 57
    sget-object v0, Lcom/android/fmradio/views/RadioFreqImageView;->TAG:Ljava/lang/String;

    const-string v1, "start"

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 59
    sget-object v0, Lcom/android/fmradio/BuildConfig;->UseSkinLib:Ljava/lang/Boolean;

    invoke-virtual {v0}, Ljava/lang/Boolean;->booleanValue()Z

    move-result v0

    if-eqz v0, :cond_0

    .line 60
    invoke-static {}, Lcom/qf/skin/manager/loader/SkinManager;->getInstance()Lcom/qf/skin/manager/loader/SkinManager;

    move-result-object v0

    iget-object v1, p0, Lcom/android/fmradio/views/RadioFreqImageView;->mSkinUpdate:Lcom/qf/skin/manager/interfaces/ISkinUpdate;

    invoke-virtual {v0, v1}, Lcom/qf/skin/manager/loader/SkinManager;->attach(Lcom/qf/skin/manager/interfaces/ISkinUpdate;)V

    :cond_0
    return-void
.end method

.method protected onDetachedFromWindow()V
    .locals 2

    .line 66
    invoke-super {p0}, Landroid/widget/LinearLayout;->onDetachedFromWindow()V

    .line 68
    sget-object v0, Lcom/android/fmradio/views/RadioFreqImageView;->TAG:Ljava/lang/String;

    const-string v1, "start"

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 70
    sget-object v0, Lcom/android/fmradio/BuildConfig;->UseSkinLib:Ljava/lang/Boolean;

    invoke-virtual {v0}, Ljava/lang/Boolean;->booleanValue()Z

    move-result v0

    if-eqz v0, :cond_0

    .line 71
    invoke-static {}, Lcom/qf/skin/manager/loader/SkinManager;->getInstance()Lcom/qf/skin/manager/loader/SkinManager;

    move-result-object v0

    iget-object v1, p0, Lcom/android/fmradio/views/RadioFreqImageView;->mSkinUpdate:Lcom/qf/skin/manager/interfaces/ISkinUpdate;

    invoke-virtual {v0, v1}, Lcom/qf/skin/manager/loader/SkinManager;->detach(Lcom/qf/skin/manager/interfaces/ISkinUpdate;)V

    :cond_0
    return-void
.end method

.method public setActivity(Lcom/android/fmradio/FmMainActivity;)V
    .locals 0

    .line 76
    iput-object p1, p0, Lcom/android/fmradio/views/RadioFreqImageView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    return-void
.end method

.method public updateFreqValue(I)V
    .locals 6

    const/4 v0, 0x0

    move v1, v0

    .line 109
    :goto_0
    sget-object v2, Lcom/android/fmradio/views/RadioFreqImageView;->FREQ_IMAGE_ID_ARRAY:[I

    array-length v2, v2

    const/16 v3, 0x8

    if-ge v1, v2, :cond_0

    .line 110
    iget-object v2, p0, Lcom/android/fmradio/views/RadioFreqImageView;->mFreqImageNumArray:[Landroid/widget/ImageView;

    aget-object v2, v2, v1

    invoke-virtual {v2, v3}, Landroid/widget/ImageView;->setVisibility(I)V

    add-int/lit8 v1, v1, 0x1

    goto :goto_0

    .line 113
    :cond_0
    iget-object v1, p0, Lcom/android/fmradio/views/RadioFreqImageView;->mContext:Landroid/content/Context;

    invoke-static {v1}, Lcom/android/fmradio/database/FmStation;->getCurrentBand(Landroid/content/Context;)I

    move-result v1

    .line 114
    sget-object v2, Lcom/android/fmradio/views/RadioFreqImageView;->TAG:Ljava/lang/String;

    new-instance v4, Ljava/lang/StringBuilder;

    invoke-direct {v4}, Ljava/lang/StringBuilder;-><init>()V

    const-string v5, "station: "

    invoke-virtual {v4, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v4, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v5, " - band: "

    invoke-virtual {v4, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v4, v1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v4}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v4

    invoke-static {v2, v4}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 115
    invoke-static {v1}, Lcom/android/fmradio/utils/FmUtils;->isFMBand(I)Z

    move-result v1

    if-eqz v1, :cond_1

    .line 116
    iget-object v1, p0, Lcom/android/fmradio/views/RadioFreqImageView;->mIVFreqImagePoint:Landroid/widget/ImageView;

    invoke-virtual {v1, v0}, Landroid/widget/ImageView;->setVisibility(I)V

    goto :goto_1

    .line 118
    :cond_1
    iget-object v1, p0, Lcom/android/fmradio/views/RadioFreqImageView;->mIVFreqImagePoint:Landroid/widget/ImageView;

    invoke-virtual {v1, v3}, Landroid/widget/ImageView;->setVisibility(I)V

    :goto_1
    move v1, v0

    :goto_2
    if-lez p1, :cond_2

    .line 125
    rem-int/lit8 v2, p1, 0xa

    .line 126
    div-int/lit8 p1, p1, 0xa

    .line 128
    iget-object v3, p0, Lcom/android/fmradio/views/RadioFreqImageView;->mFreqImageNumArray:[Landroid/widget/ImageView;

    aget-object v3, v3, v1

    invoke-virtual {v3, v0}, Landroid/widget/ImageView;->setVisibility(I)V

    .line 129
    iget-object v3, p0, Lcom/android/fmradio/views/RadioFreqImageView;->mFreqImageNumArray:[Landroid/widget/ImageView;

    aget-object v3, v3, v1

    sget-object v4, Lcom/android/fmradio/views/RadioFreqImageView;->FREQ_IMAGE_NUM_ARRAY:[I

    aget v2, v4, v2

    invoke-virtual {v3, v2}, Landroid/widget/ImageView;->setImageResource(I)V

    add-int/lit8 v1, v1, 0x1

    goto :goto_2

    :cond_2
    return-void
.end method

.method public updateFreqValue(Ljava/lang/String;)V
    .locals 0

    return-void
.end method
