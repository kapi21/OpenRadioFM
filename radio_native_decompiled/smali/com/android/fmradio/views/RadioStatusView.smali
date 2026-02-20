.class public Lcom/android/fmradio/views/RadioStatusView;
.super Landroid/widget/LinearLayout;
.source "RadioStatusView.java"

# interfaces
.implements Landroid/view/View$OnClickListener;
.implements Landroid/view/View$OnLongClickListener;


# static fields
.field private static final TAG:Ljava/lang/String;


# instance fields
.field private mActivity:Lcom/android/fmradio/FmMainActivity;

.field private mBtnAf:Landroid/widget/TextView;

.field private mBtnPty:Landroid/widget/TextView;

.field private mBtnTa:Landroid/widget/TextView;

.field private mContext:Landroid/content/Context;

.field private mTVLoc:Landroid/widget/TextView;

.field private mTVRdsRt:Landroid/widget/TextView;

.field private mTVST:Landroid/widget/TextView;


# direct methods
.method static constructor <clinit>()V
    .locals 1

    .line 21
    const-class v0, Lcom/android/fmradio/views/RadioStatusView;

    invoke-virtual {v0}, Ljava/lang/Class;->getSimpleName()Ljava/lang/String;

    move-result-object v0

    sput-object v0, Lcom/android/fmradio/views/RadioStatusView;->TAG:Ljava/lang/String;

    return-void
.end method

.method public constructor <init>(Landroid/content/Context;Landroid/util/AttributeSet;)V
    .locals 2

    .line 41
    invoke-direct {p0, p1, p2}, Landroid/widget/LinearLayout;-><init>(Landroid/content/Context;Landroid/util/AttributeSet;)V

    .line 43
    iput-object p1, p0, Lcom/android/fmradio/views/RadioStatusView;->mContext:Landroid/content/Context;

    .line 45
    invoke-direct {p0}, Lcom/android/fmradio/views/RadioStatusView;->initData()V

    .line 48
    iget-object p2, p0, Lcom/android/fmradio/views/RadioStatusView;->mContext:Landroid/content/Context;

    check-cast p2, Landroid/app/Activity;

    invoke-virtual {p2}, Landroid/app/Activity;->isInMultiWindowMode()Z

    move-result p2

    const/4 v0, 0x0

    const/4 v1, 0x0

    if-eqz p2, :cond_0

    .line 49
    invoke-static {p1}, Landroid/view/LayoutInflater;->from(Landroid/content/Context;)Landroid/view/LayoutInflater;

    move-result-object p1

    const p2, 0x7f0b003d

    invoke-virtual {p1, p2, v1, v0}, Landroid/view/LayoutInflater;->inflate(ILandroid/view/ViewGroup;Z)Landroid/view/View;

    move-result-object p1

    goto :goto_0

    .line 51
    :cond_0
    invoke-static {p1}, Landroid/view/LayoutInflater;->from(Landroid/content/Context;)Landroid/view/LayoutInflater;

    move-result-object p1

    const p2, 0x7f0b0030

    invoke-virtual {p1, p2, v1, v0}, Landroid/view/LayoutInflater;->inflate(ILandroid/view/ViewGroup;Z)Landroid/view/View;

    move-result-object p1

    .line 53
    :goto_0
    new-instance p2, Landroid/widget/LinearLayout$LayoutParams;

    const/4 v0, -0x1

    const/4 v1, -0x2

    invoke-direct {p2, v0, v1}, Landroid/widget/LinearLayout$LayoutParams;-><init>(II)V

    invoke-virtual {p1, p2}, Landroid/view/View;->setLayoutParams(Landroid/view/ViewGroup$LayoutParams;)V

    .line 55
    invoke-direct {p0, p1}, Lcom/android/fmradio/views/RadioStatusView;->initView(Landroid/view/View;)V

    .line 57
    invoke-virtual {p0, p1}, Lcom/android/fmradio/views/RadioStatusView;->addView(Landroid/view/View;)V

    return-void
.end method

.method private initData()V
    .locals 0

    return-void
.end method

.method private initRdsView(Landroid/view/View;)V
    .locals 1

    const v0, 0x7f080056

    .line 74
    invoke-virtual {p1, v0}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object v0

    check-cast v0, Landroid/widget/TextView;

    iput-object v0, p0, Lcom/android/fmradio/views/RadioStatusView;->mBtnPty:Landroid/widget/TextView;

    const v0, 0x7f080042

    .line 75
    invoke-virtual {p1, v0}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object v0

    check-cast v0, Landroid/widget/TextView;

    iput-object v0, p0, Lcom/android/fmradio/views/RadioStatusView;->mBtnAf:Landroid/widget/TextView;

    const v0, 0x7f080059

    .line 76
    invoke-virtual {p1, v0}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object v0

    check-cast v0, Landroid/widget/TextView;

    iput-object v0, p0, Lcom/android/fmradio/views/RadioStatusView;->mBtnTa:Landroid/widget/TextView;

    .line 78
    iget-object v0, p0, Lcom/android/fmradio/views/RadioStatusView;->mBtnPty:Landroid/widget/TextView;

    invoke-virtual {v0, p0}, Landroid/widget/TextView;->setOnClickListener(Landroid/view/View$OnClickListener;)V

    .line 79
    iget-object v0, p0, Lcom/android/fmradio/views/RadioStatusView;->mBtnAf:Landroid/widget/TextView;

    invoke-virtual {v0, p0}, Landroid/widget/TextView;->setOnClickListener(Landroid/view/View$OnClickListener;)V

    .line 80
    iget-object v0, p0, Lcom/android/fmradio/views/RadioStatusView;->mBtnTa:Landroid/widget/TextView;

    invoke-virtual {v0, p0}, Landroid/widget/TextView;->setOnClickListener(Landroid/view/View$OnClickListener;)V

    const v0, 0x7f0800d9

    .line 82
    invoke-virtual {p1, v0}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object p1

    check-cast p1, Landroid/widget/TextView;

    iput-object p1, p0, Lcom/android/fmradio/views/RadioStatusView;->mTVRdsRt:Landroid/widget/TextView;

    .line 84
    invoke-virtual {p0}, Lcom/android/fmradio/views/RadioStatusView;->updateRdsUIStatus()V

    return-void
.end method

.method private initView(Landroid/view/View;)V
    .locals 1

    .line 64
    invoke-direct {p0, p1}, Lcom/android/fmradio/views/RadioStatusView;->initRdsView(Landroid/view/View;)V

    const v0, 0x7f0800d4

    .line 66
    invoke-virtual {p1, v0}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object v0

    check-cast v0, Landroid/widget/TextView;

    iput-object v0, p0, Lcom/android/fmradio/views/RadioStatusView;->mTVLoc:Landroid/widget/TextView;

    const v0, 0x7f0800da

    .line 67
    invoke-virtual {p1, v0}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object p1

    check-cast p1, Landroid/widget/TextView;

    iput-object p1, p0, Lcom/android/fmradio/views/RadioStatusView;->mTVST:Landroid/widget/TextView;

    const/4 p1, 0x0

    .line 70
    invoke-virtual {p0, p1, p1}, Lcom/android/fmradio/views/RadioStatusView;->updateStAndLocStatus(II)V

    return-void
.end method

.method private onRdsAFSwitch()V
    .locals 4

    .line 153
    iget-object v0, p0, Lcom/android/fmradio/views/RadioStatusView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {v0}, Lcom/android/fmradio/FmMainActivity;->getRdsAFSwitch()I

    move-result v0

    .line 154
    sget-object v1, Lcom/android/fmradio/views/RadioStatusView;->TAG:Ljava/lang/String;

    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2}, Ljava/lang/StringBuilder;-><init>()V

    const-string v3, "onRdsAFSwitch - rdsAFSwitch: "

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2, v0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    invoke-static {v1, v0}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 155
    iget-object v0, p0, Lcom/android/fmradio/views/RadioStatusView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {v0}, Lcom/android/fmradio/FmMainActivity;->setRdsAFSwitch()V

    .line 157
    iget-object v0, p0, Lcom/android/fmradio/views/RadioStatusView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {v0}, Lcom/android/fmradio/FmMainActivity;->stopScan()Z

    return-void
.end method

.method private onRdsTASwitch()V
    .locals 4

    .line 139
    iget-object v0, p0, Lcom/android/fmradio/views/RadioStatusView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {v0}, Lcom/android/fmradio/FmMainActivity;->getRdsTASwitch()I

    move-result v0

    .line 140
    sget-object v1, Lcom/android/fmradio/views/RadioStatusView;->TAG:Ljava/lang/String;

    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2}, Ljava/lang/StringBuilder;-><init>()V

    const-string v3, "onRdsTASwitch - rdsTASwitch: "

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2, v0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v2

    invoke-static {v1, v2}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 141
    iget-object v1, p0, Lcom/android/fmradio/views/RadioStatusView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {v1}, Lcom/android/fmradio/FmMainActivity;->setRdsTASwitch()V

    .line 143
    invoke-static {}, Lcom/android/fmradio/utils/FmUtils;->isPTYEnable()Z

    move-result v1

    if-eqz v1, :cond_0

    const/4 v1, 0x1

    if-ne v0, v1, :cond_1

    .line 145
    iget-object v0, p0, Lcom/android/fmradio/views/RadioStatusView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {v0}, Lcom/android/fmradio/FmMainActivity;->stopScan()Z

    goto :goto_0

    .line 148
    :cond_0
    iget-object v0, p0, Lcom/android/fmradio/views/RadioStatusView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {v0}, Lcom/android/fmradio/FmMainActivity;->stopScan()Z

    :cond_1
    :goto_0
    return-void
.end method


# virtual methods
.method public onClick(Landroid/view/View;)V
    .locals 1

    .line 176
    invoke-static {}, Landroid/qf/os/QFApi;->isBTPhoneStartup()Z

    move-result v0

    if-eqz v0, :cond_0

    return-void

    .line 180
    :cond_0
    invoke-virtual {p1}, Landroid/view/View;->getId()I

    move-result p1

    const v0, 0x7f080056

    if-ne p1, v0, :cond_1

    .line 182
    iget-object p1, p0, Lcom/android/fmradio/views/RadioStatusView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {p1}, Lcom/android/fmradio/FmMainActivity;->requestAudioFocus()V

    .line 184
    iget-object p1, p0, Lcom/android/fmradio/views/RadioStatusView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    iget-object p1, p1, Lcom/android/fmradio/FmMainActivity;->mGVRdsPtyAdapter:Lcom/android/fmradio/views/PTYItemAdapter;

    iget-object v0, p0, Lcom/android/fmradio/views/RadioStatusView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    iget-object v0, v0, Lcom/android/fmradio/FmMainActivity;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    invoke-virtual {v0}, Lcom/android/fmradio/TunerManagerForExt;->getRdsPtyType()I

    move-result v0

    invoke-virtual {p1, v0}, Lcom/android/fmradio/views/PTYItemAdapter;->setPtyType(I)V

    .line 185
    iget-object p1, p0, Lcom/android/fmradio/views/RadioStatusView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    iget-object p1, p1, Lcom/android/fmradio/FmMainActivity;->mGVRdsPtyAdapter:Lcom/android/fmradio/views/PTYItemAdapter;

    invoke-virtual {p1}, Lcom/android/fmradio/views/PTYItemAdapter;->notifyDataSetInvalidated()V

    .line 186
    iget-object p1, p0, Lcom/android/fmradio/views/RadioStatusView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    iget-object p1, p1, Lcom/android/fmradio/FmMainActivity;->mGVRdsPty:Landroid/widget/GridView;

    const/4 v0, 0x0

    invoke-virtual {p1, v0}, Landroid/widget/GridView;->setVisibility(I)V

    goto :goto_0

    :cond_1
    const v0, 0x7f080042

    if-ne p1, v0, :cond_2

    .line 188
    iget-object p1, p0, Lcom/android/fmradio/views/RadioStatusView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {p1}, Lcom/android/fmradio/FmMainActivity;->requestAudioFocus()V

    .line 190
    invoke-direct {p0}, Lcom/android/fmradio/views/RadioStatusView;->onRdsAFSwitch()V

    goto :goto_0

    :cond_2
    const v0, 0x7f080059

    if-ne p1, v0, :cond_3

    .line 192
    iget-object p1, p0, Lcom/android/fmradio/views/RadioStatusView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {p1}, Lcom/android/fmradio/FmMainActivity;->requestAudioFocus()V

    .line 194
    invoke-direct {p0}, Lcom/android/fmradio/views/RadioStatusView;->onRdsTASwitch()V

    :cond_3
    :goto_0
    return-void
.end method

.method public onLongClick(Landroid/view/View;)Z
    .locals 1

    .line 200
    iget-object p1, p0, Lcom/android/fmradio/views/RadioStatusView;->mContext:Landroid/content/Context;

    const/4 v0, 0x0

    invoke-static {p1, v0}, Lcom/android/fmradio/utils/FmUtils;->onTest(Landroid/content/Context;Z)V

    .line 203
    iget-object p1, p0, Lcom/android/fmradio/views/RadioStatusView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    if-eqz p1, :cond_0

    .line 204
    invoke-virtual {p1}, Lcom/android/fmradio/FmMainActivity;->finish()V

    :cond_0
    const/4 p1, 0x1

    return p1
.end method

.method public setActivity(Lcom/android/fmradio/FmMainActivity;)V
    .locals 0

    .line 37
    iput-object p1, p0, Lcom/android/fmradio/views/RadioStatusView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    return-void
.end method

.method public setPtyStatus(Z)V
    .locals 1

    .line 135
    iget-object v0, p0, Lcom/android/fmradio/views/RadioStatusView;->mBtnPty:Landroid/widget/TextView;

    invoke-virtual {v0, p1}, Landroid/widget/TextView;->setSelected(Z)V

    return-void
.end method

.method public updateRdsInfo(II)V
    .locals 3

    .line 161
    iget-object v0, p0, Lcom/android/fmradio/views/RadioStatusView;->mBtnAf:Landroid/widget/TextView;

    const/4 v1, 0x0

    const/4 v2, 0x1

    if-ne p1, v2, :cond_0

    move p1, v2

    goto :goto_0

    :cond_0
    move p1, v1

    :goto_0
    invoke-virtual {v0, p1}, Landroid/widget/TextView;->setSelected(Z)V

    .line 163
    iget-object p1, p0, Lcom/android/fmradio/views/RadioStatusView;->mBtnTa:Landroid/widget/TextView;

    if-ne p2, v2, :cond_1

    move v1, v2

    :cond_1
    invoke-virtual {p1, v1}, Landroid/widget/TextView;->setSelected(Z)V

    return-void
.end method

.method public updateRdsPtyTypeInfo(I)V
    .locals 0

    if-eqz p1, :cond_0

    const/4 p1, 0x1

    goto :goto_0

    :cond_0
    const/4 p1, 0x0

    .line 167
    :goto_0
    invoke-virtual {p0, p1}, Lcom/android/fmradio/views/RadioStatusView;->setPtyStatus(Z)V

    return-void
.end method

.method public updateRdsRTInfo(Ljava/lang/String;)V
    .locals 1

    .line 171
    iget-object v0, p0, Lcom/android/fmradio/views/RadioStatusView;->mTVRdsRt:Landroid/widget/TextView;

    invoke-virtual {v0, p1}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    return-void
.end method

.method public updateRdsUIStatus()V
    .locals 2

    .line 88
    iget-object v0, p0, Lcom/android/fmradio/views/RadioStatusView;->mContext:Landroid/content/Context;

    check-cast v0, Landroid/app/Activity;

    invoke-virtual {v0}, Landroid/app/Activity;->isInMultiWindowMode()Z

    move-result v0

    if-eqz v0, :cond_0

    return-void

    .line 91
    :cond_0
    iget-object v0, p0, Lcom/android/fmradio/views/RadioStatusView;->mBtnPty:Landroid/widget/TextView;

    if-eqz v0, :cond_2

    iget-object v0, p0, Lcom/android/fmradio/views/RadioStatusView;->mBtnAf:Landroid/widget/TextView;

    if-eqz v0, :cond_2

    iget-object v0, p0, Lcom/android/fmradio/views/RadioStatusView;->mBtnTa:Landroid/widget/TextView;

    if-eqz v0, :cond_2

    iget-object v0, p0, Lcom/android/fmradio/views/RadioStatusView;->mTVRdsRt:Landroid/widget/TextView;

    if-eqz v0, :cond_2

    .line 95
    invoke-static {}, Lcom/android/fmradio/utils/FmUtils;->isRDSEnable()Z

    move-result v0

    if-eqz v0, :cond_1

    iget-object v0, p0, Lcom/android/fmradio/views/RadioStatusView;->mContext:Landroid/content/Context;

    invoke-static {v0}, Lcom/android/fmradio/database/FmStation;->getCurrentBand(Landroid/content/Context;)I

    move-result v0

    invoke-static {v0}, Lcom/android/fmradio/utils/FmUtils;->isFMBand(I)Z

    move-result v0

    if-eqz v0, :cond_1

    .line 96
    iget-object v0, p0, Lcom/android/fmradio/views/RadioStatusView;->mBtnPty:Landroid/widget/TextView;

    const/4 v1, 0x0

    invoke-virtual {v0, v1}, Landroid/widget/TextView;->setVisibility(I)V

    .line 97
    iget-object v0, p0, Lcom/android/fmradio/views/RadioStatusView;->mBtnAf:Landroid/widget/TextView;

    invoke-virtual {v0, v1}, Landroid/widget/TextView;->setVisibility(I)V

    .line 98
    iget-object v0, p0, Lcom/android/fmradio/views/RadioStatusView;->mBtnTa:Landroid/widget/TextView;

    invoke-virtual {v0, v1}, Landroid/widget/TextView;->setVisibility(I)V

    .line 99
    iget-object v0, p0, Lcom/android/fmradio/views/RadioStatusView;->mTVRdsRt:Landroid/widget/TextView;

    invoke-virtual {v0, v1}, Landroid/widget/TextView;->setVisibility(I)V

    goto :goto_0

    .line 101
    :cond_1
    iget-object v0, p0, Lcom/android/fmradio/views/RadioStatusView;->mBtnPty:Landroid/widget/TextView;

    const/16 v1, 0x8

    invoke-virtual {v0, v1}, Landroid/widget/TextView;->setVisibility(I)V

    .line 102
    iget-object v0, p0, Lcom/android/fmradio/views/RadioStatusView;->mBtnAf:Landroid/widget/TextView;

    invoke-virtual {v0, v1}, Landroid/widget/TextView;->setVisibility(I)V

    .line 103
    iget-object v0, p0, Lcom/android/fmradio/views/RadioStatusView;->mBtnTa:Landroid/widget/TextView;

    invoke-virtual {v0, v1}, Landroid/widget/TextView;->setVisibility(I)V

    .line 104
    iget-object v0, p0, Lcom/android/fmradio/views/RadioStatusView;->mTVRdsRt:Landroid/widget/TextView;

    const/4 v1, 0x4

    invoke-virtual {v0, v1}, Landroid/widget/TextView;->setVisibility(I)V

    :cond_2
    :goto_0
    return-void
.end method

.method public updateStAndLocStatus(II)V
    .locals 3

    .line 111
    iget-object v0, p0, Lcom/android/fmradio/views/RadioStatusView;->mContext:Landroid/content/Context;

    invoke-static {v0}, Lcom/android/fmradio/database/FmStation;->getCurrentBand(Landroid/content/Context;)I

    move-result v0

    invoke-static {v0}, Lcom/android/fmradio/utils/FmUtils;->isFMBand(I)Z

    move-result v0

    const/4 v1, 0x4

    if-eqz v0, :cond_2

    const/4 v0, 0x0

    const/4 v2, 0x1

    if-ne p2, v2, :cond_0

    .line 113
    iget-object p2, p0, Lcom/android/fmradio/views/RadioStatusView;->mTVLoc:Landroid/widget/TextView;

    invoke-virtual {p2, v0}, Landroid/widget/TextView;->setVisibility(I)V

    goto :goto_0

    .line 115
    :cond_0
    iget-object p2, p0, Lcom/android/fmradio/views/RadioStatusView;->mTVLoc:Landroid/widget/TextView;

    invoke-virtual {p2, v1}, Landroid/widget/TextView;->setVisibility(I)V

    .line 119
    :goto_0
    iget-object p2, p0, Lcom/android/fmradio/views/RadioStatusView;->mTVST:Landroid/widget/TextView;

    invoke-virtual {p2, v0}, Landroid/widget/TextView;->setVisibility(I)V

    if-ne p1, v2, :cond_1

    .line 122
    iget-object p1, p0, Lcom/android/fmradio/views/RadioStatusView;->mTVST:Landroid/widget/TextView;

    invoke-virtual {p1, v2}, Landroid/widget/TextView;->setSelected(Z)V

    goto :goto_1

    .line 124
    :cond_1
    iget-object p1, p0, Lcom/android/fmradio/views/RadioStatusView;->mTVST:Landroid/widget/TextView;

    invoke-virtual {p1, v0}, Landroid/widget/TextView;->setSelected(Z)V

    goto :goto_1

    .line 127
    :cond_2
    iget-object p1, p0, Lcom/android/fmradio/views/RadioStatusView;->mTVLoc:Landroid/widget/TextView;

    invoke-virtual {p1, v1}, Landroid/widget/TextView;->setVisibility(I)V

    .line 130
    iget-object p1, p0, Lcom/android/fmradio/views/RadioStatusView;->mTVST:Landroid/widget/TextView;

    invoke-virtual {p1, v1}, Landroid/widget/TextView;->setVisibility(I)V

    :goto_1
    return-void
.end method
