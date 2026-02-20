.class public Lcom/android/fmradio/views/RadioPresetListView;
.super Landroid/widget/LinearLayout;
.source "RadioPresetListView.java"

# interfaces
.implements Landroid/view/View$OnClickListener;
.implements Landroid/view/View$OnLongClickListener;


# annotations
.annotation system Ldalvik/annotation/MemberClasses;
    value = {
        Lcom/android/fmradio/views/RadioPresetListView$PresetListCallback;
    }
.end annotation


# static fields
.field private static final EMPTY_STATION_TEXT:Ljava/lang/String; = "-"

.field private static final PRESET_FREQ_ID_ARRAY:[I

.field private static final TAG:Ljava/lang/String;


# instance fields
.field private mContext:Landroid/content/Context;

.field public mPresetListBtn:[Lcom/android/fmradio/views/RadioPresetItemView;

.field private mPresetListCallback:Lcom/android/fmradio/views/RadioPresetListView$PresetListCallback;


# direct methods
.method static constructor <clinit>()V
    .locals 1

    .line 22
    const-class v0, Lcom/android/fmradio/views/RadioPresetListView;

    invoke-virtual {v0}, Ljava/lang/Class;->getSimpleName()Ljava/lang/String;

    move-result-object v0

    sput-object v0, Lcom/android/fmradio/views/RadioPresetListView;->TAG:Ljava/lang/String;

    const/4 v0, 0x6

    new-array v0, v0, [I

    .line 24
    fill-array-data v0, :array_0

    sput-object v0, Lcom/android/fmradio/views/RadioPresetListView;->PRESET_FREQ_ID_ARRAY:[I

    return-void

    nop

    :array_0
    .array-data 4
        0x7f080050
        0x7f080051
        0x7f080052
        0x7f080053
        0x7f080054
        0x7f080055
    .end array-data
.end method

.method public constructor <init>(Landroid/content/Context;Landroid/util/AttributeSet;)V
    .locals 3

    .line 42
    invoke-direct {p0, p1, p2}, Landroid/widget/LinearLayout;-><init>(Landroid/content/Context;Landroid/util/AttributeSet;)V

    const/4 p2, 0x6

    new-array p2, p2, [Lcom/android/fmradio/views/RadioPresetItemView;

    .line 37
    iput-object p2, p0, Lcom/android/fmradio/views/RadioPresetListView;->mPresetListBtn:[Lcom/android/fmradio/views/RadioPresetItemView;

    .line 44
    iput-object p1, p0, Lcom/android/fmradio/views/RadioPresetListView;->mContext:Landroid/content/Context;

    .line 47
    iget-object p2, p0, Lcom/android/fmradio/views/RadioPresetListView;->mContext:Landroid/content/Context;

    check-cast p2, Landroid/app/Activity;

    invoke-virtual {p2}, Landroid/app/Activity;->isInMultiWindowMode()Z

    move-result p2

    const/4 v0, 0x0

    const/4 v1, 0x0

    if-eqz p2, :cond_3

    .line 48
    invoke-static {}, Landroid/qf/os/QFApi;->isShuPing()Z

    move-result p2

    if-nez p2, :cond_2

    invoke-static {}, Landroid/qf/os/QFApi;->getProjectType()Ljava/lang/String;

    move-result-object p2

    const-string v2, "navi4"

    invoke-virtual {p2, v2}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result p2

    if-eqz p2, :cond_0

    goto :goto_0

    .line 50
    :cond_0
    invoke-static {}, Lcom/android/fmradio/utils/FmUtils;->isLongProduct()Z

    move-result p2

    if-eqz p2, :cond_1

    .line 51
    invoke-static {p1}, Landroid/view/LayoutInflater;->from(Landroid/content/Context;)Landroid/view/LayoutInflater;

    move-result-object p1

    const p2, 0x7f0b003b

    invoke-virtual {p1, p2, v1, v0}, Landroid/view/LayoutInflater;->inflate(ILandroid/view/ViewGroup;Z)Landroid/view/View;

    move-result-object p1

    goto :goto_1

    .line 53
    :cond_1
    invoke-static {p1}, Landroid/view/LayoutInflater;->from(Landroid/content/Context;)Landroid/view/LayoutInflater;

    move-result-object p1

    const p2, 0x7f0b003a

    invoke-virtual {p1, p2, v1, v0}, Landroid/view/LayoutInflater;->inflate(ILandroid/view/ViewGroup;Z)Landroid/view/View;

    move-result-object p1

    goto :goto_1

    .line 49
    :cond_2
    :goto_0
    invoke-static {p1}, Landroid/view/LayoutInflater;->from(Landroid/content/Context;)Landroid/view/LayoutInflater;

    move-result-object p1

    const p2, 0x7f0b003c

    invoke-virtual {p1, p2, v1, v0}, Landroid/view/LayoutInflater;->inflate(ILandroid/view/ViewGroup;Z)Landroid/view/View;

    move-result-object p1

    goto :goto_1

    .line 56
    :cond_3
    invoke-static {p1}, Landroid/view/LayoutInflater;->from(Landroid/content/Context;)Landroid/view/LayoutInflater;

    move-result-object p1

    const p2, 0x7f0b002f

    invoke-virtual {p1, p2, v1, v0}, Landroid/view/LayoutInflater;->inflate(ILandroid/view/ViewGroup;Z)Landroid/view/View;

    move-result-object p1

    .line 58
    :goto_1
    new-instance p2, Landroid/widget/LinearLayout$LayoutParams;

    const/4 v0, -0x1

    const/4 v1, -0x2

    invoke-direct {p2, v0, v1}, Landroid/widget/LinearLayout$LayoutParams;-><init>(II)V

    invoke-virtual {p1, p2}, Landroid/view/View;->setLayoutParams(Landroid/view/ViewGroup$LayoutParams;)V

    .line 60
    invoke-direct {p0, p1}, Lcom/android/fmradio/views/RadioPresetListView;->initView(Landroid/view/View;)V

    .line 62
    invoke-virtual {p0, p1}, Lcom/android/fmradio/views/RadioPresetListView;->addView(Landroid/view/View;)V

    return-void
.end method

.method private initView(Landroid/view/View;)V
    .locals 3

    .line 66
    sget-object v0, Lcom/android/fmradio/views/RadioPresetListView;->TAG:Ljava/lang/String;

    const-string v1, "start"

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 67
    invoke-virtual {p0}, Lcom/android/fmradio/views/RadioPresetListView;->getResources()Landroid/content/res/Resources;

    move-result-object v0

    invoke-virtual {v0}, Landroid/content/res/Resources;->getConfiguration()Landroid/content/res/Configuration;

    move-result-object v0

    iget v0, v0, Landroid/content/res/Configuration;->uiMode:I

    const/4 v0, 0x0

    .line 68
    :goto_0
    iget-object v1, p0, Lcom/android/fmradio/views/RadioPresetListView;->mPresetListBtn:[Lcom/android/fmradio/views/RadioPresetItemView;

    array-length v2, v1

    if-ge v0, v2, :cond_0

    .line 69
    sget-object v2, Lcom/android/fmradio/views/RadioPresetListView;->PRESET_FREQ_ID_ARRAY:[I

    aget v2, v2, v0

    invoke-virtual {p1, v2}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object v2

    check-cast v2, Lcom/android/fmradio/views/RadioPresetItemView;

    aput-object v2, v1, v0

    .line 70
    iget-object v1, p0, Lcom/android/fmradio/views/RadioPresetListView;->mPresetListBtn:[Lcom/android/fmradio/views/RadioPresetItemView;

    aget-object v1, v1, v0

    invoke-virtual {v1, p0}, Lcom/android/fmradio/views/RadioPresetItemView;->setOnClickListener(Landroid/view/View$OnClickListener;)V

    .line 71
    iget-object v1, p0, Lcom/android/fmradio/views/RadioPresetListView;->mPresetListBtn:[Lcom/android/fmradio/views/RadioPresetItemView;

    aget-object v1, v1, v0

    invoke-virtual {v1, p0}, Lcom/android/fmradio/views/RadioPresetItemView;->setOnLongClickListener(Landroid/view/View$OnLongClickListener;)V

    .line 73
    iget-object v1, p0, Lcom/android/fmradio/views/RadioPresetListView;->mPresetListBtn:[Lcom/android/fmradio/views/RadioPresetItemView;

    aget-object v1, v1, v0

    invoke-virtual {v1, v0}, Lcom/android/fmradio/views/RadioPresetItemView;->updatePresetIndex(I)V

    .line 74
    iget-object v1, p0, Lcom/android/fmradio/views/RadioPresetListView;->mPresetListBtn:[Lcom/android/fmradio/views/RadioPresetItemView;

    aget-object v1, v1, v0

    const-string v2, ""

    invoke-virtual {v1, v2}, Lcom/android/fmradio/views/RadioPresetItemView;->updatePresetName(Ljava/lang/String;)V

    .line 75
    iget-object v1, p0, Lcom/android/fmradio/views/RadioPresetListView;->mPresetListBtn:[Lcom/android/fmradio/views/RadioPresetItemView;

    aget-object v1, v1, v0

    const-string v2, "-"

    invoke-virtual {v1, v2}, Lcom/android/fmradio/views/RadioPresetItemView;->updatePresetStation(Ljava/lang/String;)V

    add-int/lit8 v0, v0, 0x1

    goto :goto_0

    :cond_0
    return-void
.end method


# virtual methods
.method public clearButtonBackground()V
    .locals 4

    const/4 v0, 0x0

    move v1, v0

    .line 136
    :goto_0
    iget-object v2, p0, Lcom/android/fmradio/views/RadioPresetListView;->mPresetListBtn:[Lcom/android/fmradio/views/RadioPresetItemView;

    array-length v3, v2

    if-ge v1, v3, :cond_0

    .line 137
    aget-object v2, v2, v1

    invoke-virtual {v2, v0}, Lcom/android/fmradio/views/RadioPresetItemView;->setSelected(Z)V

    add-int/lit8 v1, v1, 0x1

    goto :goto_0

    :cond_0
    return-void
.end method

.method public onClick(Landroid/view/View;)V
    .locals 2

    .line 162
    invoke-virtual {p1}, Landroid/view/View;->getId()I

    move-result p1

    const/4 v0, 0x0

    .line 163
    :goto_0
    iget-object v1, p0, Lcom/android/fmradio/views/RadioPresetListView;->mPresetListBtn:[Lcom/android/fmradio/views/RadioPresetItemView;

    array-length v1, v1

    if-ge v0, v1, :cond_1

    .line 164
    sget-object v1, Lcom/android/fmradio/views/RadioPresetListView;->PRESET_FREQ_ID_ARRAY:[I

    aget v1, v1, v0

    if-ne p1, v1, :cond_0

    .line 165
    invoke-virtual {p0, v0}, Lcom/android/fmradio/views/RadioPresetListView;->onClickItem(I)V

    goto :goto_1

    :cond_0
    add-int/lit8 v0, v0, 0x1

    goto :goto_0

    :cond_1
    :goto_1
    return-void
.end method

.method public onClickItem(I)V
    .locals 1

    .line 154
    iget-object v0, p0, Lcom/android/fmradio/views/RadioPresetListView;->mPresetListCallback:Lcom/android/fmradio/views/RadioPresetListView$PresetListCallback;

    if-eqz v0, :cond_0

    .line 155
    invoke-interface {v0, p1}, Lcom/android/fmradio/views/RadioPresetListView$PresetListCallback;->onClickItem(I)V

    .line 156
    invoke-virtual {p0, p1}, Lcom/android/fmradio/views/RadioPresetListView;->setButtonBackground(I)V

    :cond_0
    return-void
.end method

.method public onLongClick(Landroid/view/View;)Z
    .locals 2

    .line 173
    invoke-virtual {p1}, Landroid/view/View;->getId()I

    move-result p1

    const/4 v0, 0x0

    .line 174
    :goto_0
    iget-object v1, p0, Lcom/android/fmradio/views/RadioPresetListView;->mPresetListBtn:[Lcom/android/fmradio/views/RadioPresetItemView;

    array-length v1, v1

    if-ge v0, v1, :cond_1

    .line 175
    sget-object v1, Lcom/android/fmradio/views/RadioPresetListView;->PRESET_FREQ_ID_ARRAY:[I

    aget v1, v1, v0

    if-ne p1, v1, :cond_0

    .line 176
    iget-object p1, p0, Lcom/android/fmradio/views/RadioPresetListView;->mPresetListCallback:Lcom/android/fmradio/views/RadioPresetListView$PresetListCallback;

    if-eqz p1, :cond_1

    .line 177
    invoke-interface {p1, v0}, Lcom/android/fmradio/views/RadioPresetListView$PresetListCallback;->onLongClickItem(I)V

    goto :goto_1

    :cond_0
    add-int/lit8 v0, v0, 0x1

    goto :goto_0

    :cond_1
    :goto_1
    const/4 p1, 0x1

    return p1
.end method

.method public setButtonBackground(I)V
    .locals 4

    .line 142
    invoke-virtual {p0}, Lcom/android/fmradio/views/RadioPresetListView;->clearButtonBackground()V

    if-ltz p1, :cond_0

    .line 144
    iget-object v0, p0, Lcom/android/fmradio/views/RadioPresetListView;->mPresetListBtn:[Lcom/android/fmradio/views/RadioPresetItemView;

    array-length v1, v0

    if-ge p1, v1, :cond_0

    .line 145
    aget-object v0, v0, p1

    invoke-virtual {v0}, Lcom/android/fmradio/views/RadioPresetItemView;->getTVPresetStation()Landroid/widget/TextView;

    move-result-object v0

    invoke-virtual {v0}, Landroid/widget/TextView;->getText()Ljava/lang/CharSequence;

    move-result-object v0

    invoke-interface {v0}, Ljava/lang/CharSequence;->toString()Ljava/lang/String;

    move-result-object v0

    .line 146
    sget-object v1, Lcom/android/fmradio/views/RadioPresetListView;->TAG:Ljava/lang/String;

    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2}, Ljava/lang/StringBuilder;-><init>()V

    const-string v3, "presetStation: "

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v2

    invoke-static {v1, v2}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 147
    invoke-virtual {v0}, Ljava/lang/String;->length()I

    move-result v1

    if-eqz v1, :cond_0

    const-string v1, "-"

    invoke-virtual {v0, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v0

    if-nez v0, :cond_0

    .line 148
    iget-object v0, p0, Lcom/android/fmradio/views/RadioPresetListView;->mPresetListBtn:[Lcom/android/fmradio/views/RadioPresetItemView;

    aget-object p1, v0, p1

    const/4 v0, 0x1

    invoke-virtual {p1, v0}, Lcom/android/fmradio/views/RadioPresetItemView;->setSelected(Z)V

    :cond_0
    return-void
.end method

.method public setFreq(IILjava/lang/String;)V
    .locals 4

    .line 85
    sget-object v0, Lcom/android/fmradio/views/RadioPresetListView;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "setFreq - position: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v2, " - freq: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v2, " - unit: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p3

    invoke-static {v0, p3}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 86
    iget-object p3, p0, Lcom/android/fmradio/views/RadioPresetListView;->mPresetListBtn:[Lcom/android/fmradio/views/RadioPresetItemView;

    array-length v0, p3

    if-ge p1, v0, :cond_2

    if-ltz p1, :cond_2

    const-string v0, ""

    if-nez p2, :cond_0

    .line 88
    aget-object p2, p3, p1

    invoke-virtual {p2, p1}, Lcom/android/fmradio/views/RadioPresetItemView;->updatePresetIndex(I)V

    .line 89
    iget-object p2, p0, Lcom/android/fmradio/views/RadioPresetListView;->mPresetListBtn:[Lcom/android/fmradio/views/RadioPresetItemView;

    aget-object p2, p2, p1

    invoke-virtual {p2, v0}, Lcom/android/fmradio/views/RadioPresetItemView;->updatePresetName(Ljava/lang/String;)V

    .line 90
    iget-object p2, p0, Lcom/android/fmradio/views/RadioPresetListView;->mPresetListBtn:[Lcom/android/fmradio/views/RadioPresetItemView;

    aget-object p1, p2, p1

    const-string p2, "-"

    invoke-virtual {p1, p2}, Lcom/android/fmradio/views/RadioPresetItemView;->updatePresetStation(Ljava/lang/String;)V

    goto :goto_1

    .line 92
    :cond_0
    aget-object p3, p3, p1

    invoke-virtual {p3, p1}, Lcom/android/fmradio/views/RadioPresetItemView;->updatePresetIndex(I)V

    .line 93
    iget-object p3, p0, Lcom/android/fmradio/views/RadioPresetListView;->mPresetListBtn:[Lcom/android/fmradio/views/RadioPresetItemView;

    aget-object p3, p3, p1

    iget-object v1, p0, Lcom/android/fmradio/views/RadioPresetListView;->mContext:Landroid/content/Context;

    invoke-static {v1, p2}, Lcom/android/fmradio/utils/FmUtils;->formatStation(Landroid/content/Context;I)Ljava/lang/String;

    move-result-object v1

    invoke-virtual {p3, v1}, Lcom/android/fmradio/views/RadioPresetItemView;->updatePresetStation(Ljava/lang/String;)V

    .line 96
    iget-object p3, p0, Lcom/android/fmradio/views/RadioPresetListView;->mPresetListBtn:[Lcom/android/fmradio/views/RadioPresetItemView;

    aget-object p3, p3, p1

    invoke-virtual {p3, v0}, Lcom/android/fmradio/views/RadioPresetItemView;->updatePresetName(Ljava/lang/String;)V

    .line 97
    iget-object p3, p0, Lcom/android/fmradio/views/RadioPresetListView;->mContext:Landroid/content/Context;

    const-string v0, "all_station_info_key"

    invoke-static {p3, v0}, Lcom/android/fmradio/utils/AllStationInfoUtil;->getAllStationInfoData(Landroid/content/Context;Ljava/lang/String;)Ljava/lang/String;

    move-result-object p3

    .line 98
    invoke-static {p3}, Landroid/text/TextUtils;->isEmpty(Ljava/lang/CharSequence;)Z

    move-result v0

    if-nez v0, :cond_2

    const-string v0, "#"

    .line 99
    invoke-virtual {p3, v0}, Ljava/lang/String;->split(Ljava/lang/String;)[Ljava/lang/String;

    move-result-object p3

    .line 100
    array-length v0, p3

    const/4 v1, 0x0

    :goto_0
    if-ge v1, v0, :cond_2

    aget-object v2, p3, v1

    const-string v3, ","

    .line 101
    invoke-virtual {v2, v3}, Ljava/lang/String;->split(Ljava/lang/String;)[Ljava/lang/String;

    move-result-object v2

    const/4 v3, 0x1

    .line 102
    aget-object v3, v2, v3

    invoke-static {v3}, Ljava/lang/Integer;->valueOf(Ljava/lang/String;)Ljava/lang/Integer;

    move-result-object v3

    invoke-virtual {v3}, Ljava/lang/Integer;->intValue()I

    move-result v3

    if-ne v3, p2, :cond_1

    .line 104
    iget-object p2, p0, Lcom/android/fmradio/views/RadioPresetListView;->mPresetListBtn:[Lcom/android/fmradio/views/RadioPresetItemView;

    aget-object p1, p2, p1

    const/4 p2, 0x2

    aget-object p2, v2, p2

    invoke-virtual {p1, p2}, Lcom/android/fmradio/views/RadioPresetItemView;->updatePresetName(Ljava/lang/String;)V

    goto :goto_1

    :cond_1
    add-int/lit8 v1, v1, 0x1

    goto :goto_0

    :cond_2
    :goto_1
    return-void
.end method

.method public setFreq(ILjava/lang/String;)V
    .locals 3

    .line 114
    sget-object v0, Lcom/android/fmradio/views/RadioPresetListView;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "setFreq - position: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v2, " - rdsPs: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 115
    iget-object v0, p0, Lcom/android/fmradio/views/RadioPresetListView;->mPresetListBtn:[Lcom/android/fmradio/views/RadioPresetItemView;

    array-length v0, v0

    if-ge p1, v0, :cond_1

    if-ltz p1, :cond_1

    .line 116
    invoke-static {p2}, Landroid/text/TextUtils;->isEmpty(Ljava/lang/CharSequence;)Z

    move-result v0

    const-string v1, ""

    if-eqz v0, :cond_0

    .line 117
    iget-object p2, p0, Lcom/android/fmradio/views/RadioPresetListView;->mPresetListBtn:[Lcom/android/fmradio/views/RadioPresetItemView;

    aget-object p2, p2, p1

    invoke-virtual {p2, p1}, Lcom/android/fmradio/views/RadioPresetItemView;->updatePresetIndex(I)V

    .line 118
    iget-object p2, p0, Lcom/android/fmradio/views/RadioPresetListView;->mPresetListBtn:[Lcom/android/fmradio/views/RadioPresetItemView;

    aget-object p2, p2, p1

    invoke-virtual {p2, v1}, Lcom/android/fmradio/views/RadioPresetItemView;->updatePresetName(Ljava/lang/String;)V

    .line 119
    iget-object p2, p0, Lcom/android/fmradio/views/RadioPresetListView;->mPresetListBtn:[Lcom/android/fmradio/views/RadioPresetItemView;

    aget-object p1, p2, p1

    const-string p2, "-"

    invoke-virtual {p1, p2}, Lcom/android/fmradio/views/RadioPresetItemView;->updatePresetStation(Ljava/lang/String;)V

    goto :goto_0

    .line 121
    :cond_0
    iget-object v0, p0, Lcom/android/fmradio/views/RadioPresetListView;->mPresetListBtn:[Lcom/android/fmradio/views/RadioPresetItemView;

    aget-object v0, v0, p1

    invoke-virtual {v0, p1}, Lcom/android/fmradio/views/RadioPresetItemView;->updatePresetIndex(I)V

    .line 122
    iget-object v0, p0, Lcom/android/fmradio/views/RadioPresetListView;->mPresetListBtn:[Lcom/android/fmradio/views/RadioPresetItemView;

    aget-object v0, v0, p1

    invoke-virtual {v0, v1}, Lcom/android/fmradio/views/RadioPresetItemView;->updatePresetName(Ljava/lang/String;)V

    .line 123
    iget-object v0, p0, Lcom/android/fmradio/views/RadioPresetListView;->mPresetListBtn:[Lcom/android/fmradio/views/RadioPresetItemView;

    aget-object p1, v0, p1

    invoke-virtual {p1, p2}, Lcom/android/fmradio/views/RadioPresetItemView;->updatePresetStation(Ljava/lang/String;)V

    :cond_1
    :goto_0
    return-void
.end method

.method public setPresetListCallback(Lcom/android/fmradio/views/RadioPresetListView$PresetListCallback;)V
    .locals 0

    .line 81
    iput-object p1, p0, Lcom/android/fmradio/views/RadioPresetListView;->mPresetListCallback:Lcom/android/fmradio/views/RadioPresetListView$PresetListCallback;

    return-void
.end method

.method public setStationName(ILjava/lang/String;)V
    .locals 3

    .line 129
    sget-object v0, Lcom/android/fmradio/views/RadioPresetListView;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "setStationName - position: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v2, " - stationName: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 130
    iget-object v0, p0, Lcom/android/fmradio/views/RadioPresetListView;->mPresetListBtn:[Lcom/android/fmradio/views/RadioPresetItemView;

    array-length v1, v0

    if-ge p1, v1, :cond_0

    if-ltz p1, :cond_0

    .line 131
    aget-object p1, v0, p1

    invoke-virtual {p1, p2}, Lcom/android/fmradio/views/RadioPresetItemView;->updatePresetName(Ljava/lang/String;)V

    :cond_0
    return-void
.end method
