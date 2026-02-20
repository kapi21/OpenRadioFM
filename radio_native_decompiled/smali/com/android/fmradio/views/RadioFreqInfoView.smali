.class public Lcom/android/fmradio/views/RadioFreqInfoView;
.super Landroid/widget/LinearLayout;
.source "RadioFreqInfoView.java"

# interfaces
.implements Landroid/view/View$OnClickListener;
.implements Landroid/view/View$OnLongClickListener;
.implements Lcom/android/fmradio/FmAllStationInfoFragment$OnFmStationInfoListener;


# static fields
.field private static final TAG:Ljava/lang/String;


# instance fields
.field private mActivity:Lcom/android/fmradio/FmMainActivity;

.field private mContext:Landroid/content/Context;

.field private mIBFavorite:Landroid/widget/ImageButton;

.field private mIVStation:Lcom/android/fmradio/views/RadioFreqImageView;

.field private mTVFreqBand:Landroid/widget/TextView;

.field private mTVFreqUnit:Landroid/widget/TextView;

.field private mTVStation:Landroid/widget/TextView;


# direct methods
.method static constructor <clinit>()V
    .locals 1

    .line 31
    const-class v0, Lcom/android/fmradio/views/RadioFreqInfoView;

    invoke-virtual {v0}, Ljava/lang/Class;->getSimpleName()Ljava/lang/String;

    move-result-object v0

    sput-object v0, Lcom/android/fmradio/views/RadioFreqInfoView;->TAG:Ljava/lang/String;

    return-void
.end method

.method public constructor <init>(Landroid/content/Context;Landroid/util/AttributeSet;)V
    .locals 2

    .line 50
    invoke-direct {p0, p1, p2}, Landroid/widget/LinearLayout;-><init>(Landroid/content/Context;Landroid/util/AttributeSet;)V

    .line 52
    iput-object p1, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mContext:Landroid/content/Context;

    .line 55
    iget-object p2, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mContext:Landroid/content/Context;

    invoke-static {p2}, Lcom/android/fmradio/favorite/FmFavoriteManager;->getInstance(Landroid/content/Context;)Lcom/android/fmradio/favorite/FmFavoriteManager;

    move-result-object p2

    invoke-virtual {p2}, Lcom/android/fmradio/favorite/FmFavoriteManager;->onLoad()V

    .line 59
    iget-object p2, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mContext:Landroid/content/Context;

    check-cast p2, Landroid/app/Activity;

    invoke-virtual {p2}, Landroid/app/Activity;->isInMultiWindowMode()Z

    move-result p2

    const/4 v0, 0x0

    const/4 v1, 0x0

    if-eqz p2, :cond_0

    .line 60
    invoke-static {p1}, Landroid/view/LayoutInflater;->from(Landroid/content/Context;)Landroid/view/LayoutInflater;

    move-result-object p1

    const p2, 0x7f0b0036

    invoke-virtual {p1, p2, v1, v0}, Landroid/view/LayoutInflater;->inflate(ILandroid/view/ViewGroup;Z)Landroid/view/View;

    move-result-object p1

    goto :goto_0

    .line 62
    :cond_0
    invoke-static {p1}, Landroid/view/LayoutInflater;->from(Landroid/content/Context;)Landroid/view/LayoutInflater;

    move-result-object p1

    const p2, 0x7f0b002b

    invoke-virtual {p1, p2, v1, v0}, Landroid/view/LayoutInflater;->inflate(ILandroid/view/ViewGroup;Z)Landroid/view/View;

    move-result-object p1

    .line 64
    :goto_0
    new-instance p2, Landroid/widget/LinearLayout$LayoutParams;

    const/4 v0, -0x1

    invoke-direct {p2, v0, v0}, Landroid/widget/LinearLayout$LayoutParams;-><init>(II)V

    invoke-virtual {p1, p2}, Landroid/view/View;->setLayoutParams(Landroid/view/ViewGroup$LayoutParams;)V

    .line 66
    invoke-direct {p0, p1}, Lcom/android/fmradio/views/RadioFreqInfoView;->initView(Landroid/view/View;)V

    .line 68
    invoke-virtual {p0, p1}, Lcom/android/fmradio/views/RadioFreqInfoView;->addView(Landroid/view/View;)V

    return-void
.end method

.method private initView(Landroid/view/View;)V
    .locals 2

    const v0, 0x7f0800ce

    .line 72
    invoke-virtual {p1, v0}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object v0

    check-cast v0, Landroid/widget/TextView;

    iput-object v0, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mTVFreqBand:Landroid/widget/TextView;

    const v0, 0x7f0800db

    .line 73
    invoke-virtual {p1, v0}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object v0

    check-cast v0, Landroid/widget/TextView;

    iput-object v0, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mTVStation:Landroid/widget/TextView;

    const v0, 0x7f08006e

    .line 74
    invoke-virtual {p1, v0}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object v0

    check-cast v0, Landroid/widget/TextView;

    iput-object v0, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mTVFreqUnit:Landroid/widget/TextView;

    const v0, 0x7f080081

    .line 76
    invoke-virtual {p1, v0}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object v0

    check-cast v0, Lcom/android/fmradio/views/RadioFreqImageView;

    iput-object v0, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mIVStation:Lcom/android/fmradio/views/RadioFreqImageView;

    .line 78
    iget-object v0, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mTVStation:Landroid/widget/TextView;

    invoke-virtual {v0, p0}, Landroid/widget/TextView;->setOnLongClickListener(Landroid/view/View$OnLongClickListener;)V

    .line 79
    iget-object v0, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mIVStation:Lcom/android/fmradio/views/RadioFreqImageView;

    invoke-virtual {v0, p0}, Lcom/android/fmradio/views/RadioFreqImageView;->setOnLongClickListener(Landroid/view/View$OnLongClickListener;)V

    const v0, 0x7f080047

    .line 81
    invoke-virtual {p1, v0}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object v0

    check-cast v0, Landroid/widget/ImageButton;

    const v1, 0x7f08004d

    .line 82
    invoke-virtual {p1, v1}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object v1

    check-cast v1, Landroid/widget/ImageButton;

    .line 84
    invoke-virtual {v0, p0}, Landroid/widget/ImageButton;->setOnClickListener(Landroid/view/View$OnClickListener;)V

    .line 85
    invoke-virtual {v1, p0}, Landroid/widget/ImageButton;->setOnClickListener(Landroid/view/View$OnClickListener;)V

    .line 87
    invoke-virtual {v0, p0}, Landroid/widget/ImageButton;->setOnLongClickListener(Landroid/view/View$OnLongClickListener;)V

    .line 88
    invoke-virtual {v1, p0}, Landroid/widget/ImageButton;->setOnLongClickListener(Landroid/view/View$OnLongClickListener;)V

    const v0, 0x7f08004c

    .line 90
    invoke-virtual {p1, v0}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object p1

    check-cast p1, Landroid/widget/ImageButton;

    iput-object p1, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mIBFavorite:Landroid/widget/ImageButton;

    .line 92
    iget-object p1, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mContext:Landroid/content/Context;

    check-cast p1, Landroid/app/Activity;

    invoke-virtual {p1}, Landroid/app/Activity;->isInMultiWindowMode()Z

    move-result p1

    if-eqz p1, :cond_0

    .line 93
    iget-object p1, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mIBFavorite:Landroid/widget/ImageButton;

    const/16 v0, 0x8

    invoke-virtual {p1, v0}, Landroid/widget/ImageButton;->setVisibility(I)V

    goto :goto_0

    .line 95
    :cond_0
    iget-object p1, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mIBFavorite:Landroid/widget/ImageButton;

    invoke-virtual {p1, p0}, Landroid/widget/ImageButton;->setOnClickListener(Landroid/view/View$OnClickListener;)V

    .line 97
    iget-object p1, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mIBFavorite:Landroid/widget/ImageButton;

    const/4 v0, 0x0

    invoke-virtual {p1, v0}, Landroid/widget/ImageButton;->setVisibility(I)V

    .line 103
    :goto_0
    iget-object p1, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mContext:Landroid/content/Context;

    invoke-static {p1}, Lcom/android/fmradio/database/FmStation;->getCurrentStation(Landroid/content/Context;)I

    move-result p1

    invoke-virtual {p0, p1}, Lcom/android/fmradio/views/RadioFreqInfoView;->updateFreqValue(I)V

    .line 105
    invoke-virtual {p0}, Lcom/android/fmradio/views/RadioFreqInfoView;->updateFreqBand()V

    .line 107
    invoke-virtual {p0}, Lcom/android/fmradio/views/RadioFreqInfoView;->updateFreqUnit()V

    return-void
.end method

.method private onFavorite()V
    .locals 3

    .line 233
    iget-object v0, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mContext:Landroid/content/Context;

    invoke-static {v0}, Lcom/android/fmradio/database/FmStation;->getCurrentStation(Landroid/content/Context;)I

    move-result v0

    .line 234
    iget-object v1, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mContext:Landroid/content/Context;

    invoke-static {v1}, Lcom/android/fmradio/favorite/FmFavoriteManager;->getInstance(Landroid/content/Context;)Lcom/android/fmradio/favorite/FmFavoriteManager;

    move-result-object v1

    invoke-virtual {v1, v0}, Lcom/android/fmradio/favorite/FmFavoriteManager;->isFavorite(I)Z

    move-result v1

    if-eqz v1, :cond_0

    .line 235
    iget-object v1, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mIBFavorite:Landroid/widget/ImageButton;

    const/4 v2, 0x0

    invoke-virtual {v1, v2}, Landroid/widget/ImageButton;->setSelected(Z)V

    .line 237
    iget-object v1, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mContext:Landroid/content/Context;

    invoke-static {v1}, Lcom/android/fmradio/favorite/FmFavoriteManager;->getInstance(Landroid/content/Context;)Lcom/android/fmradio/favorite/FmFavoriteManager;

    move-result-object v1

    invoke-virtual {v1, v0}, Lcom/android/fmradio/favorite/FmFavoriteManager;->onRemove(I)V

    goto :goto_0

    .line 239
    :cond_0
    iget-object v1, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mIBFavorite:Landroid/widget/ImageButton;

    const/4 v2, 0x1

    invoke-virtual {v1, v2}, Landroid/widget/ImageButton;->setSelected(Z)V

    .line 241
    new-instance v1, Lcom/android/fmradio/info/FmFreqInfo;

    invoke-direct {v1}, Lcom/android/fmradio/info/FmFreqInfo;-><init>()V

    .line 242
    invoke-virtual {v1, v0}, Lcom/android/fmradio/info/FmFreqInfo;->setFreq(I)V

    .line 243
    iget-object v0, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mContext:Landroid/content/Context;

    invoke-static {v0}, Lcom/android/fmradio/database/FmStation;->getCurrentBand(Landroid/content/Context;)I

    move-result v0

    invoke-virtual {v1, v0}, Lcom/android/fmradio/info/FmFreqInfo;->setBand(I)V

    .line 244
    iget-object v0, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mContext:Landroid/content/Context;

    invoke-static {v0}, Lcom/android/fmradio/favorite/FmFavoriteManager;->getInstance(Landroid/content/Context;)Lcom/android/fmradio/favorite/FmFavoriteManager;

    move-result-object v0

    invoke-virtual {v0, v1}, Lcom/android/fmradio/favorite/FmFavoriteManager;->onAdd(Lcom/android/fmradio/info/FmFreqInfo;)V

    :goto_0
    return-void
.end method


# virtual methods
.method public onClick(Landroid/view/View;)V
    .locals 1

    .line 171
    invoke-static {}, Landroid/qf/os/QFApi;->isBTPhoneStartup()Z

    move-result v0

    if-eqz v0, :cond_0

    return-void

    .line 175
    :cond_0
    invoke-virtual {p1}, Landroid/view/View;->getId()I

    move-result p1

    const v0, 0x7f08004c

    if-ne p1, v0, :cond_1

    .line 177
    invoke-direct {p0}, Lcom/android/fmradio/views/RadioFreqInfoView;->onFavorite()V

    return-void

    .line 181
    :cond_1
    iget-object v0, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {v0}, Lcom/android/fmradio/FmMainActivity;->stopScan()Z

    move-result v0

    if-eqz v0, :cond_2

    return-void

    :cond_2
    const v0, 0x7f080047

    if-ne p1, v0, :cond_3

    .line 186
    iget-object p1, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    const/4 v0, 0x1

    invoke-virtual {p1, v0}, Lcom/android/fmradio/FmMainActivity;->onFine(Z)V

    goto :goto_0

    :cond_3
    const v0, 0x7f08004d

    if-ne p1, v0, :cond_4

    .line 188
    iget-object p1, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    const/4 v0, 0x0

    invoke-virtual {p1, v0}, Lcom/android/fmradio/FmMainActivity;->onFine(Z)V

    :cond_4
    :goto_0
    return-void
.end method

.method public onItemClickListener(I)V
    .locals 1

    .line 218
    invoke-static {}, Landroid/qf/os/QFApi;->isBTPhoneStartup()Z

    move-result v0

    if-eqz v0, :cond_0

    return-void

    .line 222
    :cond_0
    iget-object v0, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    iput p1, v0, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    .line 223
    invoke-virtual {v0}, Lcom/android/fmradio/FmMainActivity;->requestAudioFocus()V

    .line 224
    iget-object v0, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {v0, p1}, Lcom/android/fmradio/FmMainActivity;->tuneStation(I)V

    return-void
.end method

.method public onLongClick(Landroid/view/View;)Z
    .locals 3

    .line 194
    invoke-static {}, Landroid/qf/os/QFApi;->isBTPhoneStartup()Z

    move-result v0

    const/4 v1, 0x1

    if-eqz v0, :cond_0

    return v1

    .line 198
    :cond_0
    iget-object v0, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {v0}, Lcom/android/fmradio/FmMainActivity;->stopScan()Z

    move-result v0

    if-eqz v0, :cond_1

    return v1

    .line 202
    :cond_1
    invoke-virtual {p1}, Landroid/view/View;->getId()I

    move-result p1

    const v0, 0x7f080047

    if-ne p1, v0, :cond_2

    .line 204
    iget-object p1, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    iget v0, p1, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    invoke-virtual {p1, v0, v1}, Lcom/android/fmradio/FmMainActivity;->onSeekStation(IZ)V

    goto :goto_0

    :cond_2
    const v0, 0x7f08004d

    if-ne p1, v0, :cond_3

    .line 206
    iget-object p1, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    iget v0, p1, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    const/4 v2, 0x0

    invoke-virtual {p1, v0, v2}, Lcom/android/fmradio/FmMainActivity;->onSeekStation(IZ)V

    goto :goto_0

    :cond_3
    const v0, 0x7f080081

    if-eq p1, v0, :cond_4

    const v0, 0x7f0800db

    if-ne p1, v0, :cond_5

    .line 208
    :cond_4
    new-instance p1, Lcom/android/fmradio/FmAllStationInfoFragment;

    invoke-direct {p1}, Lcom/android/fmradio/FmAllStationInfoFragment;-><init>()V

    .line 209
    invoke-virtual {p1, p0}, Lcom/android/fmradio/FmAllStationInfoFragment;->setFmStationInfoListener(Lcom/android/fmradio/FmAllStationInfoFragment$OnFmStationInfoListener;)V

    .line 210
    iget-object v0, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {v0}, Lcom/android/fmradio/FmMainActivity;->getFragmentManager()Landroid/app/FragmentManager;

    move-result-object v0

    const-string v2, ""

    invoke-virtual {p1, v0, v2}, Lcom/android/fmradio/FmAllStationInfoFragment;->show(Landroid/app/FragmentManager;Ljava/lang/String;)V

    :cond_5
    :goto_0
    return v1
.end method

.method public onUpdateStationName(ILjava/lang/String;)V
    .locals 1

    .line 229
    iget-object v0, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {v0, p1, p2}, Lcom/android/fmradio/FmMainActivity;->onUpdateStationName(ILjava/lang/String;)V

    return-void
.end method

.method public setActivity(Lcom/android/fmradio/FmMainActivity;)V
    .locals 0

    .line 46
    iput-object p1, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    return-void
.end method

.method public updateFavoriteState(Z)V
    .locals 3

    .line 249
    sget-object v0, Lcom/android/fmradio/views/RadioFreqInfoView;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "isFavorite: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    if-eqz p1, :cond_0

    .line 251
    iget-object p1, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mIBFavorite:Landroid/widget/ImageButton;

    const/4 v0, 0x1

    invoke-virtual {p1, v0}, Landroid/widget/ImageButton;->setSelected(Z)V

    goto :goto_0

    .line 253
    :cond_0
    iget-object p1, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mIBFavorite:Landroid/widget/ImageButton;

    const/4 v0, 0x0

    invoke-virtual {p1, v0}, Landroid/widget/ImageButton;->setSelected(Z)V

    :goto_0
    return-void
.end method

.method public updateFreqBand()V
    .locals 6

    .line 154
    iget-object v0, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mContext:Landroid/content/Context;

    invoke-static {v0}, Lcom/android/fmradio/database/FmStation;->getCurrentBand(Landroid/content/Context;)I

    move-result v0

    invoke-static {v0}, Lcom/android/fmradio/utils/FmUtils;->isFMBand(I)Z

    move-result v0

    const/4 v1, 0x0

    const/4 v2, 0x1

    if-eqz v0, :cond_0

    .line 155
    iget-object v0, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mTVFreqBand:Landroid/widget/TextView;

    sget-object v3, Ljava/util/Locale;->ENGLISH:Ljava/util/Locale;

    new-array v4, v2, [Ljava/lang/Object;

    iget-object v5, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mContext:Landroid/content/Context;

    invoke-static {v5}, Lcom/android/fmradio/database/FmStation;->getCurrentBand(Landroid/content/Context;)I

    move-result v5

    add-int/2addr v5, v2

    invoke-static {v5}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v2

    aput-object v2, v4, v1

    const-string v1, "FM %d"

    invoke-static {v3, v1, v4}, Ljava/lang/String;->format(Ljava/util/Locale;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v1

    invoke-virtual {v0, v1}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    goto :goto_0

    .line 157
    :cond_0
    iget-object v0, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mTVFreqBand:Landroid/widget/TextView;

    sget-object v3, Ljava/util/Locale;->ENGLISH:Ljava/util/Locale;

    new-array v4, v2, [Ljava/lang/Object;

    iget-object v5, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mContext:Landroid/content/Context;

    invoke-static {v5}, Lcom/android/fmradio/database/FmStation;->getCurrentBand(Landroid/content/Context;)I

    move-result v5

    rem-int/lit8 v5, v5, 0x3

    add-int/2addr v5, v2

    invoke-static {v5}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v2

    aput-object v2, v4, v1

    const-string v1, "AM %d"

    invoke-static {v3, v1, v4}, Ljava/lang/String;->format(Ljava/util/Locale;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v1

    invoke-virtual {v0, v1}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    :goto_0
    return-void
.end method

.method public updateFreqUnit()V
    .locals 2

    .line 162
    iget-object v0, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mContext:Landroid/content/Context;

    invoke-static {v0}, Lcom/android/fmradio/database/FmStation;->getCurrentBand(Landroid/content/Context;)I

    move-result v0

    invoke-static {v0}, Lcom/android/fmradio/utils/FmUtils;->isFMBand(I)Z

    move-result v0

    if-eqz v0, :cond_0

    .line 163
    iget-object v0, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mTVFreqUnit:Landroid/widget/TextView;

    const v1, 0x7f0c0027

    invoke-virtual {v0, v1}, Landroid/widget/TextView;->setText(I)V

    goto :goto_0

    .line 165
    :cond_0
    iget-object v0, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mTVFreqUnit:Landroid/widget/TextView;

    const v1, 0x7f0c0026

    invoke-virtual {v0, v1}, Landroid/widget/TextView;->setText(I)V

    :goto_0
    return-void
.end method

.method public updateFreqValue(I)V
    .locals 3

    .line 111
    sget-object v0, Lcom/android/fmradio/views/RadioFreqInfoView;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "station: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 113
    iget-object v0, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mContext:Landroid/content/Context;

    invoke-static {v0}, Lcom/android/fmradio/favorite/FmFavoriteManager;->getInstance(Landroid/content/Context;)Lcom/android/fmradio/favorite/FmFavoriteManager;

    move-result-object v0

    invoke-virtual {v0, p1}, Lcom/android/fmradio/favorite/FmFavoriteManager;->isFavorite(I)Z

    move-result v0

    const/4 v1, 0x0

    if-eqz v0, :cond_0

    .line 114
    iget-object v0, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mIBFavorite:Landroid/widget/ImageButton;

    const/4 v2, 0x1

    invoke-virtual {v0, v2}, Landroid/widget/ImageButton;->setSelected(Z)V

    goto :goto_0

    .line 116
    :cond_0
    iget-object v0, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mIBFavorite:Landroid/widget/ImageButton;

    invoke-virtual {v0, v1}, Landroid/widget/ImageButton;->setSelected(Z)V

    .line 123
    :goto_0
    iget-object v0, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mTVStation:Landroid/widget/TextView;

    const/16 v2, 0x8

    invoke-virtual {v0, v2}, Landroid/widget/TextView;->setVisibility(I)V

    .line 124
    iget-object v0, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mIVStation:Lcom/android/fmradio/views/RadioFreqImageView;

    invoke-virtual {v0, v1}, Lcom/android/fmradio/views/RadioFreqImageView;->setVisibility(I)V

    .line 126
    iget-object v0, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mIVStation:Lcom/android/fmradio/views/RadioFreqImageView;

    invoke-virtual {v0, p1}, Lcom/android/fmradio/views/RadioFreqImageView;->updateFreqValue(I)V

    return-void
.end method

.method public updateFreqValue(Ljava/lang/String;)V
    .locals 4

    .line 130
    invoke-virtual {p1}, Ljava/lang/String;->isEmpty()Z

    move-result v0

    if-eqz v0, :cond_0

    return-void

    .line 133
    :cond_0
    iget-object v0, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mTVStation:Landroid/widget/TextView;

    const/4 v1, 0x0

    invoke-virtual {v0, v1}, Landroid/widget/TextView;->setVisibility(I)V

    .line 134
    iget-object v0, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mIVStation:Lcom/android/fmradio/views/RadioFreqImageView;

    const/16 v2, 0x8

    invoke-virtual {v0, v2}, Lcom/android/fmradio/views/RadioFreqImageView;->setVisibility(I)V

    .line 136
    iget-object v0, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mTVStation:Landroid/widget/TextView;

    invoke-virtual {p0}, Lcom/android/fmradio/views/RadioFreqInfoView;->getResources()Landroid/content/res/Resources;

    move-result-object v2

    const v3, 0x7f060ef4

    invoke-virtual {v2, v3}, Landroid/content/res/Resources;->getDimensionPixelSize(I)I

    move-result v2

    int-to-float v2, v2

    invoke-virtual {v0, v1, v2}, Landroid/widget/TextView;->setTextSize(IF)V

    .line 137
    iget-object v0, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mTVStation:Landroid/widget/TextView;

    invoke-virtual {v0, p1}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    .line 143
    iget-object v0, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mContext:Landroid/content/Context;

    invoke-static {v0}, Lcom/android/fmradio/database/FmStation;->getCurrentStation(Landroid/content/Context;)I

    move-result v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/RdsPsInfoUtil;->rdsPsExist(Landroid/content/Context;I)Z

    move-result v0

    if-nez v0, :cond_1

    .line 145
    iget-object v0, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mContext:Landroid/content/Context;

    invoke-static {v0}, Lcom/android/fmradio/database/FmStation;->getCurrentStation(Landroid/content/Context;)I

    move-result v0

    invoke-virtual {p0, v0, p1}, Lcom/android/fmradio/views/RadioFreqInfoView;->onUpdateStationName(ILjava/lang/String;)V

    .line 147
    iget-object v0, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mContext:Landroid/content/Context;

    invoke-static {v0}, Lcom/android/fmradio/database/FmStation;->getCurrentStation(Landroid/content/Context;)I

    move-result v1

    const-string v2, "all_station_info_key"

    invoke-static {v0, v2, v1, p1}, Lcom/android/fmradio/utils/AllStationInfoUtil;->updateAllStationInfoData(Landroid/content/Context;Ljava/lang/String;ILjava/lang/String;)V

    .line 149
    iget-object v0, p0, Lcom/android/fmradio/views/RadioFreqInfoView;->mContext:Landroid/content/Context;

    invoke-static {v0}, Lcom/android/fmradio/database/FmStation;->getCurrentStation(Landroid/content/Context;)I

    move-result v1

    const-string v2, "rds_ps_info_key"

    invoke-static {v0, v2, v1, p1}, Lcom/android/fmradio/utils/RdsPsInfoUtil;->updateRdsPsInfoData(Landroid/content/Context;Ljava/lang/String;ILjava/lang/String;)V

    :cond_1
    return-void
.end method
