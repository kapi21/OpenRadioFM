.class public Lcom/android/fmradio/FmStationNameEditFragment;
.super Landroid/app/DialogFragment;
.source "FmStationNameEditFragment.java"

# interfaces
.implements Landroid/view/View$OnClickListener;


# annotations
.annotation system Ldalvik/annotation/MemberClasses;
    value = {
        Lcom/android/fmradio/FmStationNameEditFragment$OnFmStationNameEditListener;
    }
.end annotation


# static fields
.field private static final TAG:Ljava/lang/String;


# instance fields
.field private mContext:Landroid/content/Context;

.field private mETStationName:Landroid/widget/EditText;

.field private mOnFmStationNameEditListener:Lcom/android/fmradio/FmStationNameEditFragment$OnFmStationNameEditListener;

.field private mScale:F

.field private mStationFreq:I

.field private mTVStation:Landroid/widget/TextView;


# direct methods
.method static constructor <clinit>()V
    .locals 1

    .line 29
    const-class v0, Lcom/android/fmradio/FmStationNameEditFragment;

    invoke-virtual {v0}, Ljava/lang/Class;->getSimpleName()Ljava/lang/String;

    move-result-object v0

    sput-object v0, Lcom/android/fmradio/FmStationNameEditFragment;->TAG:Ljava/lang/String;

    return-void
.end method

.method public constructor <init>()V
    .locals 1

    .line 27
    invoke-direct {p0}, Landroid/app/DialogFragment;-><init>()V

    const/high16 v0, 0x3f800000    # 1.0f

    .line 40
    iput v0, p0, Lcom/android/fmradio/FmStationNameEditFragment;->mScale:F

    return-void
.end method

.method private initStationName()V
    .locals 6

    .line 147
    iget-object v0, p0, Lcom/android/fmradio/FmStationNameEditFragment;->mContext:Landroid/content/Context;

    const-string v1, "all_station_info_key"

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/AllStationInfoUtil;->getAllStationInfoData(Landroid/content/Context;Ljava/lang/String;)Ljava/lang/String;

    move-result-object v0

    .line 148
    invoke-static {v0}, Landroid/text/TextUtils;->isEmpty(Ljava/lang/CharSequence;)Z

    move-result v1

    if-nez v1, :cond_1

    const-string v1, "#"

    .line 149
    invoke-virtual {v0, v1}, Ljava/lang/String;->split(Ljava/lang/String;)[Ljava/lang/String;

    move-result-object v0

    .line 150
    array-length v1, v0

    const/4 v2, 0x0

    :goto_0
    if-ge v2, v1, :cond_1

    aget-object v3, v0, v2

    const-string v4, ","

    .line 151
    invoke-virtual {v3, v4}, Ljava/lang/String;->split(Ljava/lang/String;)[Ljava/lang/String;

    move-result-object v3

    const/4 v4, 0x1

    .line 152
    aget-object v4, v3, v4

    invoke-static {v4}, Ljava/lang/Integer;->valueOf(Ljava/lang/String;)Ljava/lang/Integer;

    move-result-object v4

    invoke-virtual {v4}, Ljava/lang/Integer;->intValue()I

    move-result v4

    .line 153
    iget v5, p0, Lcom/android/fmradio/FmStationNameEditFragment;->mStationFreq:I

    if-ne v4, v5, :cond_0

    .line 154
    iget-object v4, p0, Lcom/android/fmradio/FmStationNameEditFragment;->mETStationName:Landroid/widget/EditText;

    const/4 v5, 0x2

    aget-object v3, v3, v5

    invoke-virtual {v4, v3}, Landroid/widget/EditText;->setText(Ljava/lang/CharSequence;)V

    :cond_0
    add-int/lit8 v2, v2, 0x1

    goto :goto_0

    :cond_1
    return-void
.end method

.method private initView(Landroid/view/View;)V
    .locals 5

    .line 95
    iget-object v0, p0, Lcom/android/fmradio/FmStationNameEditFragment;->mContext:Landroid/content/Context;

    check-cast v0, Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {v0}, Lcom/android/fmradio/FmMainActivity;->getScale()F

    move-result v0

    iput v0, p0, Lcom/android/fmradio/FmStationNameEditFragment;->mScale:F

    .line 96
    sget-object v0, Lcom/android/fmradio/FmStationNameEditFragment;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "mScale: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget v2, p0, Lcom/android/fmradio/FmStationNameEditFragment;->mScale:F

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(F)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    const v0, 0x7f0800db

    .line 98
    invoke-virtual {p1, v0}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object v0

    check-cast v0, Landroid/widget/TextView;

    iput-object v0, p0, Lcom/android/fmradio/FmStationNameEditFragment;->mTVStation:Landroid/widget/TextView;

    .line 99
    iget-object v0, p0, Lcom/android/fmradio/FmStationNameEditFragment;->mTVStation:Landroid/widget/TextView;

    iget-object v1, p0, Lcom/android/fmradio/FmStationNameEditFragment;->mContext:Landroid/content/Context;

    iget v2, p0, Lcom/android/fmradio/FmStationNameEditFragment;->mStationFreq:I

    invoke-static {v1, v2}, Lcom/android/fmradio/utils/FmUtils;->formatStation(Landroid/content/Context;I)Ljava/lang/String;

    move-result-object v1

    invoke-virtual {v0, v1}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    const v0, 0x7f08006a

    .line 101
    invoke-virtual {p1, v0}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object v0

    check-cast v0, Landroid/widget/EditText;

    iput-object v0, p0, Lcom/android/fmradio/FmStationNameEditFragment;->mETStationName:Landroid/widget/EditText;

    .line 102
    invoke-direct {p0}, Lcom/android/fmradio/FmStationNameEditFragment;->initStationName()V

    const v0, 0x7f080047

    .line 104
    invoke-virtual {p1, v0}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object v0

    check-cast v0, Landroid/widget/ImageButton;

    const v1, 0x7f08004d

    .line 105
    invoke-virtual {p1, v1}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object v1

    check-cast v1, Landroid/widget/ImageButton;

    .line 106
    invoke-virtual {v0, p0}, Landroid/widget/ImageButton;->setOnClickListener(Landroid/view/View$OnClickListener;)V

    .line 107
    invoke-virtual {v1, p0}, Landroid/widget/ImageButton;->setOnClickListener(Landroid/view/View$OnClickListener;)V

    const v2, 0x7f08004f

    .line 109
    invoke-virtual {p1, v2}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object v2

    check-cast v2, Landroid/widget/Button;

    const v3, 0x7f080045

    .line 110
    invoke-virtual {p1, v3}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object p1

    check-cast p1, Landroid/widget/Button;

    .line 111
    invoke-virtual {v2, p0}, Landroid/widget/Button;->setOnClickListener(Landroid/view/View$OnClickListener;)V

    .line 112
    invoke-virtual {p1, p0}, Landroid/widget/Button;->setOnClickListener(Landroid/view/View$OnClickListener;)V

    .line 114
    invoke-virtual {p0}, Lcom/android/fmradio/FmStationNameEditFragment;->getResources()Landroid/content/res/Resources;

    move-result-object v3

    const v4, 0x7f06038c

    invoke-virtual {v3, v4}, Landroid/content/res/Resources;->getDimensionPixelOffset(I)I

    move-result v3

    int-to-float v3, v3

    iget v4, p0, Lcom/android/fmradio/FmStationNameEditFragment;->mScale:F

    mul-float/2addr v3, v4

    float-to-int v3, v3

    .line 115
    invoke-virtual {v0}, Landroid/widget/ImageButton;->getLayoutParams()Landroid/view/ViewGroup$LayoutParams;

    move-result-object v4

    .line 116
    iput v3, v4, Landroid/view/ViewGroup$LayoutParams;->width:I

    .line 117
    iput v3, v4, Landroid/view/ViewGroup$LayoutParams;->height:I

    .line 118
    invoke-virtual {v0, v4}, Landroid/widget/ImageButton;->setLayoutParams(Landroid/view/ViewGroup$LayoutParams;)V

    .line 120
    invoke-virtual {v1}, Landroid/widget/ImageButton;->getLayoutParams()Landroid/view/ViewGroup$LayoutParams;

    move-result-object v0

    .line 121
    iput v3, v0, Landroid/view/ViewGroup$LayoutParams;->width:I

    .line 122
    iput v3, v0, Landroid/view/ViewGroup$LayoutParams;->height:I

    .line 123
    invoke-virtual {v1, v0}, Landroid/widget/ImageButton;->setLayoutParams(Landroid/view/ViewGroup$LayoutParams;)V

    .line 125
    invoke-virtual {p0}, Lcom/android/fmradio/FmStationNameEditFragment;->getResources()Landroid/content/res/Resources;

    move-result-object v0

    const v1, 0x7f0603fb

    invoke-virtual {v0, v1}, Landroid/content/res/Resources;->getDimensionPixelOffset(I)I

    move-result v0

    .line 126
    iget-object v1, p0, Lcom/android/fmradio/FmStationNameEditFragment;->mTVStation:Landroid/widget/TextView;

    int-to-float v0, v0

    iget v3, p0, Lcom/android/fmradio/FmStationNameEditFragment;->mScale:F

    mul-float/2addr v3, v0

    float-to-int v3, v3

    invoke-virtual {v1, v3}, Landroid/widget/TextView;->setMaxHeight(I)V

    .line 127
    iget-object v1, p0, Lcom/android/fmradio/FmStationNameEditFragment;->mTVStation:Landroid/widget/TextView;

    iget v3, p0, Lcom/android/fmradio/FmStationNameEditFragment;->mScale:F

    mul-float/2addr v0, v3

    float-to-int v0, v0

    invoke-virtual {v1, v0}, Landroid/widget/TextView;->setMinHeight(I)V

    .line 129
    invoke-virtual {p0}, Lcom/android/fmradio/FmStationNameEditFragment;->getResources()Landroid/content/res/Resources;

    move-result-object v0

    const v1, 0x7f0607e3

    invoke-virtual {v0, v1}, Landroid/content/res/Resources;->getDimensionPixelOffset(I)I

    move-result v0

    int-to-float v0, v0

    iget v1, p0, Lcom/android/fmradio/FmStationNameEditFragment;->mScale:F

    mul-float/2addr v0, v1

    float-to-int v0, v0

    .line 130
    invoke-virtual {p0}, Lcom/android/fmradio/FmStationNameEditFragment;->getResources()Landroid/content/res/Resources;

    move-result-object v1

    const v3, 0x7f0602ae

    invoke-virtual {v1, v3}, Landroid/content/res/Resources;->getDimensionPixelOffset(I)I

    move-result v1

    int-to-float v1, v1

    iget v3, p0, Lcom/android/fmradio/FmStationNameEditFragment;->mScale:F

    mul-float/2addr v1, v3

    float-to-int v1, v1

    .line 132
    invoke-virtual {v2}, Landroid/widget/Button;->getLayoutParams()Landroid/view/ViewGroup$LayoutParams;

    move-result-object v3

    .line 133
    iput v0, v3, Landroid/view/ViewGroup$LayoutParams;->width:I

    .line 134
    iput v1, v3, Landroid/view/ViewGroup$LayoutParams;->height:I

    .line 135
    invoke-virtual {v2, v3}, Landroid/widget/Button;->setLayoutParams(Landroid/view/ViewGroup$LayoutParams;)V

    .line 137
    invoke-virtual {p1}, Landroid/widget/Button;->getLayoutParams()Landroid/view/ViewGroup$LayoutParams;

    move-result-object v2

    .line 138
    iput v0, v2, Landroid/view/ViewGroup$LayoutParams;->width:I

    .line 139
    iput v1, v2, Landroid/view/ViewGroup$LayoutParams;->height:I

    .line 140
    invoke-virtual {p1, v2}, Landroid/widget/Button;->setLayoutParams(Landroid/view/ViewGroup$LayoutParams;)V

    return-void
.end method

.method private updateStationFreq(Z)V
    .locals 4

    .line 221
    invoke-static {}, Lcom/android/fmradio/utils/FmUtils;->getRadioArea()I

    move-result v0

    .line 222
    iget-object v1, p0, Lcom/android/fmradio/FmStationNameEditFragment;->mContext:Landroid/content/Context;

    invoke-static {v1}, Lcom/android/fmradio/database/FmStation;->getCurrentBand(Landroid/content/Context;)I

    move-result v1

    .line 226
    iget-object v2, p0, Lcom/android/fmradio/FmStationNameEditFragment;->mContext:Landroid/content/Context;

    invoke-static {v2}, Lcom/android/fmradio/database/FmStation;->getCurrentBand(Landroid/content/Context;)I

    move-result v2

    invoke-static {v2}, Lcom/android/fmradio/utils/FmUtils;->isFMBand(I)Z

    move-result v2

    if-eqz v2, :cond_0

    .line 227
    invoke-static {v0, v1}, Lcom/android/fmradio/utils/FmUtils;->getRadioAreaFMStep(II)I

    move-result v2

    .line 228
    invoke-static {v0, v1}, Lcom/android/fmradio/utils/FmUtils;->getRadioAreaFMMaxFreq(II)I

    move-result v3

    .line 229
    invoke-static {v0, v1}, Lcom/android/fmradio/utils/FmUtils;->getRadioAreaFMMinFreq(II)I

    move-result v0

    goto :goto_0

    .line 231
    :cond_0
    invoke-static {v0}, Lcom/android/fmradio/utils/FmUtils;->getRadioAreaAMStep(I)I

    move-result v2

    .line 232
    invoke-static {v0}, Lcom/android/fmradio/utils/FmUtils;->getRadioAreaAMMaxFreq(I)I

    move-result v3

    .line 233
    invoke-static {v0}, Lcom/android/fmradio/utils/FmUtils;->getRadioAreaAMMinFreq(I)I

    move-result v0

    :goto_0
    if-eqz p1, :cond_2

    .line 237
    iget p1, p0, Lcom/android/fmradio/FmStationNameEditFragment;->mStationFreq:I

    if-ge p1, v3, :cond_1

    add-int/2addr p1, v2

    .line 238
    iput p1, p0, Lcom/android/fmradio/FmStationNameEditFragment;->mStationFreq:I

    goto :goto_1

    .line 240
    :cond_1
    iput v0, p0, Lcom/android/fmradio/FmStationNameEditFragment;->mStationFreq:I

    goto :goto_1

    .line 243
    :cond_2
    iget p1, p0, Lcom/android/fmradio/FmStationNameEditFragment;->mStationFreq:I

    if-le p1, v0, :cond_3

    sub-int/2addr p1, v2

    .line 244
    iput p1, p0, Lcom/android/fmradio/FmStationNameEditFragment;->mStationFreq:I

    goto :goto_1

    .line 246
    :cond_3
    iput v3, p0, Lcom/android/fmradio/FmStationNameEditFragment;->mStationFreq:I

    .line 251
    :goto_1
    iget-object p1, p0, Lcom/android/fmradio/FmStationNameEditFragment;->mTVStation:Landroid/widget/TextView;

    iget-object v0, p0, Lcom/android/fmradio/FmStationNameEditFragment;->mContext:Landroid/content/Context;

    iget v1, p0, Lcom/android/fmradio/FmStationNameEditFragment;->mStationFreq:I

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/FmUtils;->formatStation(Landroid/content/Context;I)Ljava/lang/String;

    move-result-object v0

    invoke-virtual {p1, v0}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    .line 254
    iget p1, p0, Lcom/android/fmradio/FmStationNameEditFragment;->mStationFreq:I

    invoke-direct {p0, p1}, Lcom/android/fmradio/FmStationNameEditFragment;->updateStationName(I)V

    return-void
.end method

.method private updateStationName(I)V
    .locals 5

    .line 166
    iget-object v0, p0, Lcom/android/fmradio/FmStationNameEditFragment;->mETStationName:Landroid/widget/EditText;

    const-string v1, ""

    invoke-virtual {v0, v1}, Landroid/widget/EditText;->setText(Ljava/lang/CharSequence;)V

    .line 168
    iget-object v0, p0, Lcom/android/fmradio/FmStationNameEditFragment;->mContext:Landroid/content/Context;

    const-string v1, "all_station_info_key"

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/AllStationInfoUtil;->getAllStationInfoData(Landroid/content/Context;Ljava/lang/String;)Ljava/lang/String;

    move-result-object v0

    .line 169
    invoke-static {v0}, Landroid/text/TextUtils;->isEmpty(Ljava/lang/CharSequence;)Z

    move-result v1

    if-nez v1, :cond_1

    const-string v1, "#"

    .line 170
    invoke-virtual {v0, v1}, Ljava/lang/String;->split(Ljava/lang/String;)[Ljava/lang/String;

    move-result-object v0

    .line 171
    array-length v1, v0

    const/4 v2, 0x0

    :goto_0
    if-ge v2, v1, :cond_1

    aget-object v3, v0, v2

    const-string v4, ","

    .line 172
    invoke-virtual {v3, v4}, Ljava/lang/String;->split(Ljava/lang/String;)[Ljava/lang/String;

    move-result-object v3

    const/4 v4, 0x1

    .line 173
    aget-object v4, v3, v4

    invoke-static {v4}, Ljava/lang/Integer;->valueOf(Ljava/lang/String;)Ljava/lang/Integer;

    move-result-object v4

    invoke-virtual {v4}, Ljava/lang/Integer;->intValue()I

    move-result v4

    if-ne v4, p1, :cond_0

    .line 175
    iget-object p1, p0, Lcom/android/fmradio/FmStationNameEditFragment;->mETStationName:Landroid/widget/EditText;

    const/4 v0, 0x2

    aget-object v0, v3, v0

    invoke-virtual {p1, v0}, Landroid/widget/EditText;->setText(Ljava/lang/CharSequence;)V

    goto :goto_1

    :cond_0
    add-int/lit8 v2, v2, 0x1

    goto :goto_0

    :cond_1
    :goto_1
    return-void
.end method


# virtual methods
.method public onClick(Landroid/view/View;)V
    .locals 5

    .line 184
    invoke-virtual {p1}, Landroid/view/View;->getId()I

    move-result p1

    const/4 v0, 0x0

    const v1, 0x7f080047

    if-ne p1, v1, :cond_0

    .line 186
    invoke-direct {p0, v0}, Lcom/android/fmradio/FmStationNameEditFragment;->updateStationFreq(Z)V

    goto/16 :goto_2

    :cond_0
    const v1, 0x7f08004d

    if-ne p1, v1, :cond_1

    const/4 p1, 0x1

    .line 188
    invoke-direct {p0, p1}, Lcom/android/fmradio/FmStationNameEditFragment;->updateStationFreq(Z)V

    goto/16 :goto_2

    :cond_1
    const v1, 0x7f08004f

    if-ne p1, v1, :cond_6

    .line 190
    iget-object p1, p0, Lcom/android/fmradio/FmStationNameEditFragment;->mOnFmStationNameEditListener:Lcom/android/fmradio/FmStationNameEditFragment$OnFmStationNameEditListener;

    if-eqz p1, :cond_5

    .line 191
    iget-object p1, p0, Lcom/android/fmradio/FmStationNameEditFragment;->mETStationName:Landroid/widget/EditText;

    invoke-virtual {p1}, Landroid/widget/EditText;->getText()Landroid/text/Editable;

    move-result-object p1

    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;

    move-result-object p1

    invoke-virtual {p1}, Ljava/lang/String;->trim()Ljava/lang/String;

    move-result-object p1

    .line 193
    invoke-virtual {p1}, Ljava/lang/String;->isEmpty()Z

    move-result v1

    if-eqz v1, :cond_2

    const-string p1, " "

    :cond_2
    const-string v1, ","

    .line 197
    invoke-virtual {p1, v1}, Ljava/lang/String;->contains(Ljava/lang/CharSequence;)Z

    move-result v1

    const/16 v2, 0x11

    if-nez v1, :cond_4

    const-string v1, "#"

    invoke-virtual {p1, v1}, Ljava/lang/String;->contains(Ljava/lang/CharSequence;)Z

    move-result v1

    if-eqz v1, :cond_3

    goto :goto_0

    .line 206
    :cond_3
    sget-object v1, Lcom/android/fmradio/FmStationNameEditFragment;->TAG:Ljava/lang/String;

    new-instance v3, Ljava/lang/StringBuilder;

    invoke-direct {v3}, Ljava/lang/StringBuilder;-><init>()V

    const-string v4, "mStationFreq: "

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget v4, p0, Lcom/android/fmradio/FmStationNameEditFragment;->mStationFreq:I

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v4, " - stationName: "

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v3, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v3}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v3

    invoke-static {v1, v3}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 207
    iget-object v1, p0, Lcom/android/fmradio/FmStationNameEditFragment;->mOnFmStationNameEditListener:Lcom/android/fmradio/FmStationNameEditFragment$OnFmStationNameEditListener;

    iget v3, p0, Lcom/android/fmradio/FmStationNameEditFragment;->mStationFreq:I

    invoke-interface {v1, v3, p1}, Lcom/android/fmradio/FmStationNameEditFragment$OnFmStationNameEditListener;->onUpdateStationName(ILjava/lang/String;)V

    .line 209
    iget-object p1, p0, Lcom/android/fmradio/FmStationNameEditFragment;->mContext:Landroid/content/Context;

    const v1, 0x7f0c0021

    invoke-static {p1, v1, v0}, Landroid/widget/Toast;->makeText(Landroid/content/Context;II)Landroid/widget/Toast;

    move-result-object p1

    .line 210
    invoke-virtual {p1, v2, v0, v0}, Landroid/widget/Toast;->setGravity(III)V

    .line 211
    invoke-virtual {p1}, Landroid/widget/Toast;->show()V

    goto :goto_1

    .line 198
    :cond_4
    :goto_0
    new-instance p1, Ljava/lang/StringBuilder;

    invoke-direct {p1}, Ljava/lang/StringBuilder;-><init>()V

    const v1, 0x7f0c0020

    invoke-virtual {p0, v1}, Lcom/android/fmradio/FmStationNameEditFragment;->getString(I)Ljava/lang/String;

    move-result-object v1

    invoke-virtual {p1, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    const-string v1, "\t\",\"\t\"#\""

    invoke-virtual {p1, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {p1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p1

    .line 199
    sget-object v1, Lcom/android/fmradio/FmStationNameEditFragment;->TAG:Ljava/lang/String;

    invoke-static {v1, p1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 201
    iget-object v1, p0, Lcom/android/fmradio/FmStationNameEditFragment;->mContext:Landroid/content/Context;

    invoke-static {v1, p1, v0}, Landroid/widget/Toast;->makeText(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;

    move-result-object p1

    .line 202
    invoke-virtual {p1, v2, v0, v0}, Landroid/widget/Toast;->setGravity(III)V

    .line 203
    invoke-virtual {p1}, Landroid/widget/Toast;->show()V

    return-void

    .line 214
    :cond_5
    :goto_1
    invoke-virtual {p0}, Lcom/android/fmradio/FmStationNameEditFragment;->dismiss()V

    goto :goto_2

    :cond_6
    const v0, 0x7f080045

    if-ne p1, v0, :cond_7

    .line 216
    invoke-virtual {p0}, Lcom/android/fmradio/FmStationNameEditFragment;->dismiss()V

    :cond_7
    :goto_2
    return-void
.end method

.method public onCreate(Landroid/os/Bundle;)V
    .locals 2

    .line 51
    sget-object v0, Lcom/android/fmradio/FmStationNameEditFragment;->TAG:Ljava/lang/String;

    const-string v1, "start"

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    const/4 v0, 0x0

    const/4 v1, 0x1

    .line 52
    invoke-virtual {p0, v1, v0}, Lcom/android/fmradio/FmStationNameEditFragment;->setStyle(II)V

    .line 53
    invoke-super {p0, p1}, Landroid/app/DialogFragment;->onCreate(Landroid/os/Bundle;)V

    .line 55
    invoke-virtual {p0}, Lcom/android/fmradio/FmStationNameEditFragment;->getActivity()Landroid/app/Activity;

    move-result-object p1

    iput-object p1, p0, Lcom/android/fmradio/FmStationNameEditFragment;->mContext:Landroid/content/Context;

    const-string p1, "sys.qf.station_freq"

    .line 57
    invoke-static {p1, v0}, Landroid/os/SystemProperties;->getInt(Ljava/lang/String;I)I

    move-result p1

    iput p1, p0, Lcom/android/fmradio/FmStationNameEditFragment;->mStationFreq:I

    .line 58
    sget-object p1, Lcom/android/fmradio/FmStationNameEditFragment;->TAG:Ljava/lang/String;

    new-instance v0, Ljava/lang/StringBuilder;

    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    const-string v1, "mStationFreq: "

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget v1, p0, Lcom/android/fmradio/FmStationNameEditFragment;->mStationFreq:I

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    invoke-static {p1, v0}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    return-void
.end method

.method public onCreateView(Landroid/view/LayoutInflater;Landroid/view/ViewGroup;Landroid/os/Bundle;)Landroid/view/View;
    .locals 1

    .line 87
    sget-object p3, Lcom/android/fmradio/FmStationNameEditFragment;->TAG:Ljava/lang/String;

    const-string v0, "start"

    invoke-static {p3, v0}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    const p3, 0x7f0b001f

    .line 89
    invoke-virtual {p1, p3, p2}, Landroid/view/LayoutInflater;->inflate(ILandroid/view/ViewGroup;)Landroid/view/View;

    move-result-object p1

    .line 90
    invoke-direct {p0, p1}, Lcom/android/fmradio/FmStationNameEditFragment;->initView(Landroid/view/View;)V

    return-object p1
.end method

.method public onStart()V
    .locals 4

    .line 63
    sget-object v0, Lcom/android/fmradio/FmStationNameEditFragment;->TAG:Ljava/lang/String;

    const-string v1, "start"

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 64
    invoke-virtual {p0}, Lcom/android/fmradio/FmStationNameEditFragment;->getDialog()Landroid/app/Dialog;

    move-result-object v0

    invoke-virtual {v0}, Landroid/app/Dialog;->getWindow()Landroid/view/Window;

    move-result-object v0

    .line 65
    iget-object v1, p0, Lcom/android/fmradio/FmStationNameEditFragment;->mContext:Landroid/content/Context;

    invoke-virtual {v1}, Landroid/content/Context;->getResources()Landroid/content/res/Resources;

    move-result-object v1

    const v2, 0x7f060a02

    invoke-virtual {v1, v2}, Landroid/content/res/Resources;->getDimensionPixelSize(I)I

    move-result v1

    .line 66
    iget-object v2, p0, Lcom/android/fmradio/FmStationNameEditFragment;->mContext:Landroid/content/Context;

    invoke-virtual {v2}, Landroid/content/Context;->getResources()Landroid/content/res/Resources;

    move-result-object v2

    const v3, 0x7f0601d1

    invoke-virtual {v2, v3}, Landroid/content/res/Resources;->getDimensionPixelSize(I)I

    move-result v2

    int-to-float v2, v2

    .line 67
    iget v3, p0, Lcom/android/fmradio/FmStationNameEditFragment;->mScale:F

    mul-float/2addr v2, v3

    float-to-int v2, v2

    invoke-virtual {v0, v1, v2}, Landroid/view/Window;->setLayout(II)V

    .line 69
    invoke-super {p0}, Landroid/app/DialogFragment;->onStart()V

    .line 71
    invoke-virtual {p0}, Lcom/android/fmradio/FmStationNameEditFragment;->getDialog()Landroid/app/Dialog;

    move-result-object v0

    if-eqz v0, :cond_1

    .line 73
    invoke-virtual {v0}, Landroid/app/Dialog;->getWindow()Landroid/view/Window;

    move-result-object v1

    invoke-virtual {v1}, Landroid/view/Window;->getAttributes()Landroid/view/WindowManager$LayoutParams;

    move-result-object v1

    .line 75
    invoke-virtual {p0}, Lcom/android/fmradio/FmStationNameEditFragment;->getResources()Landroid/content/res/Resources;

    move-result-object v2

    invoke-virtual {v2}, Landroid/content/res/Resources;->getConfiguration()Landroid/content/res/Configuration;

    move-result-object v2

    iget v2, v2, Landroid/content/res/Configuration;->uiMode:I

    const/16 v3, 0x20

    and-int/2addr v2, v3

    if-ne v2, v3, :cond_0

    const/4 v2, 0x0

    .line 77
    iput v2, v1, Landroid/view/WindowManager$LayoutParams;->dimAmount:F

    goto :goto_0

    :cond_0
    const/high16 v2, 0x3f000000    # 0.5f

    .line 79
    iput v2, v1, Landroid/view/WindowManager$LayoutParams;->dimAmount:F

    .line 81
    :goto_0
    invoke-virtual {v0}, Landroid/app/Dialog;->getWindow()Landroid/view/Window;

    move-result-object v0

    invoke-virtual {v0, v1}, Landroid/view/Window;->setAttributes(Landroid/view/WindowManager$LayoutParams;)V

    :cond_1
    return-void
.end method

.method public setFmStationNameEditListener(Lcom/android/fmradio/FmStationNameEditFragment$OnFmStationNameEditListener;)V
    .locals 0

    .line 260
    iput-object p1, p0, Lcom/android/fmradio/FmStationNameEditFragment;->mOnFmStationNameEditListener:Lcom/android/fmradio/FmStationNameEditFragment$OnFmStationNameEditListener;

    return-void
.end method

.method public setStationFreq(I)V
    .locals 1

    .line 43
    iput p1, p0, Lcom/android/fmradio/FmStationNameEditFragment;->mStationFreq:I

    .line 46
    invoke-static {p1}, Ljava/lang/String;->valueOf(I)Ljava/lang/String;

    move-result-object p1

    const-string v0, "sys.qf.station_freq"

    invoke-static {v0, p1}, Landroid/os/SystemProperties;->set(Ljava/lang/String;Ljava/lang/String;)V

    return-void
.end method
