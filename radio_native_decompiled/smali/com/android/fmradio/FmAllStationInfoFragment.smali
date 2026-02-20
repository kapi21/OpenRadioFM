.class public Lcom/android/fmradio/FmAllStationInfoFragment;
.super Landroid/app/DialogFragment;
.source "FmAllStationInfoFragment.java"

# interfaces
.implements Landroid/view/View$OnClickListener;
.implements Landroid/widget/AdapterView$OnItemClickListener;
.implements Lcom/android/fmradio/FmStationNameEditFragment$OnFmStationNameEditListener;
.implements Lcom/android/fmradio/views/AllStationInfoAdapter$OnAllStationInfoAdapterClickListener;


# annotations
.annotation system Ldalvik/annotation/MemberClasses;
    value = {
        Lcom/android/fmradio/FmAllStationInfoFragment$OnFmStationInfoListener;
    }
.end annotation


# static fields
.field private static final TAG:Ljava/lang/String;


# instance fields
.field private mAMStationInfoList:Ljava/util/ArrayList;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Ljava/util/ArrayList<",
            "Lcom/android/fmradio/info/FmStationInfo;",
            ">;"
        }
    .end annotation
.end field

.field private mAdapter:Lcom/android/fmradio/views/AllStationInfoAdapter;

.field private mAllStationInfoList:Ljava/util/ArrayList;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Ljava/util/ArrayList<",
            "Lcom/android/fmradio/info/FmStationInfo;",
            ">;"
        }
    .end annotation
.end field

.field private mContext:Landroid/content/Context;

.field private mFMStationInfoList:Ljava/util/ArrayList;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Ljava/util/ArrayList<",
            "Lcom/android/fmradio/info/FmStationInfo;",
            ">;"
        }
    .end annotation
.end field

.field private mFmStationInfoListener:Lcom/android/fmradio/FmAllStationInfoFragment$OnFmStationInfoListener;

.field private mLVAllStationInfoList:Landroid/widget/ListView;

.field private mScale:F


# direct methods
.method static constructor <clinit>()V
    .locals 1

    .line 31
    const-class v0, Lcom/android/fmradio/FmAllStationInfoFragment;

    invoke-virtual {v0}, Ljava/lang/Class;->getSimpleName()Ljava/lang/String;

    move-result-object v0

    sput-object v0, Lcom/android/fmradio/FmAllStationInfoFragment;->TAG:Ljava/lang/String;

    return-void
.end method

.method public constructor <init>()V
    .locals 1

    .line 29
    invoke-direct {p0}, Landroid/app/DialogFragment;-><init>()V

    .line 40
    new-instance v0, Ljava/util/ArrayList;

    invoke-direct {v0}, Ljava/util/ArrayList;-><init>()V

    iput-object v0, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mAllStationInfoList:Ljava/util/ArrayList;

    .line 41
    new-instance v0, Ljava/util/ArrayList;

    invoke-direct {v0}, Ljava/util/ArrayList;-><init>()V

    iput-object v0, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mFMStationInfoList:Ljava/util/ArrayList;

    .line 42
    new-instance v0, Ljava/util/ArrayList;

    invoke-direct {v0}, Ljava/util/ArrayList;-><init>()V

    iput-object v0, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mAMStationInfoList:Ljava/util/ArrayList;

    const/high16 v0, 0x3f800000    # 1.0f

    .line 44
    iput v0, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mScale:F

    return-void
.end method

.method private initData()V
    .locals 8

    .line 91
    iget-object v0, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mContext:Landroid/content/Context;

    const-string v1, "all_station_info_key"

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/AllStationInfoUtil;->getAllStationInfoData(Landroid/content/Context;Ljava/lang/String;)Ljava/lang/String;

    move-result-object v0

    .line 92
    invoke-static {v0}, Landroid/text/TextUtils;->isEmpty(Ljava/lang/CharSequence;)Z

    move-result v1

    if-nez v1, :cond_2

    const-string v1, "#"

    .line 93
    invoke-virtual {v0, v1}, Ljava/lang/String;->split(Ljava/lang/String;)[Ljava/lang/String;

    move-result-object v0

    .line 94
    array-length v1, v0

    const/4 v2, 0x0

    move v3, v2

    :goto_0
    if-ge v3, v1, :cond_2

    aget-object v4, v0, v3

    .line 95
    sget-object v5, Lcom/android/fmradio/FmAllStationInfoFragment;->TAG:Ljava/lang/String;

    new-instance v6, Ljava/lang/StringBuilder;

    invoke-direct {v6}, Ljava/lang/StringBuilder;-><init>()V

    const-string v7, "info: "

    invoke-virtual {v6, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v6, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v6}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v6

    invoke-static {v5, v6}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    const-string v5, ","

    .line 96
    invoke-virtual {v4, v5}, Ljava/lang/String;->split(Ljava/lang/String;)[Ljava/lang/String;

    move-result-object v4

    .line 98
    array-length v5, v4

    const/4 v6, 0x3

    if-ne v5, v6, :cond_1

    .line 99
    new-instance v5, Lcom/android/fmradio/info/FmStationInfo;

    invoke-direct {v5}, Lcom/android/fmradio/info/FmStationInfo;-><init>()V

    .line 100
    aget-object v6, v4, v2

    invoke-static {v6}, Ljava/lang/Integer;->valueOf(Ljava/lang/String;)Ljava/lang/Integer;

    move-result-object v6

    invoke-virtual {v6}, Ljava/lang/Integer;->intValue()I

    move-result v6

    invoke-virtual {v5, v6}, Lcom/android/fmradio/info/FmStationInfo;->setBand(I)V

    const/4 v6, 0x1

    .line 101
    aget-object v7, v4, v6

    invoke-static {v7}, Ljava/lang/Integer;->valueOf(Ljava/lang/String;)Ljava/lang/Integer;

    move-result-object v7

    invoke-virtual {v7}, Ljava/lang/Integer;->intValue()I

    move-result v7

    invoke-virtual {v5, v7}, Lcom/android/fmradio/info/FmStationInfo;->setFreq(I)V

    const/4 v7, 0x2

    .line 102
    aget-object v7, v4, v7

    invoke-virtual {v5, v7}, Lcom/android/fmradio/info/FmStationInfo;->setStationName(Ljava/lang/String;)V

    .line 104
    iget-object v7, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mAllStationInfoList:Ljava/util/ArrayList;

    invoke-virtual {v7, v5}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 106
    aget-object v4, v4, v2

    invoke-static {v4}, Ljava/lang/Integer;->valueOf(Ljava/lang/String;)Ljava/lang/Integer;

    move-result-object v4

    invoke-virtual {v4}, Ljava/lang/Integer;->intValue()I

    move-result v4

    .line 107
    iget-object v7, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mContext:Landroid/content/Context;

    invoke-static {v7}, Lcom/android/fmradio/database/FmStation;->getCurrentBand(Landroid/content/Context;)I

    move-result v7

    invoke-static {v7}, Lcom/android/fmradio/utils/FmUtils;->isFMBand(I)Z

    move-result v7

    if-eqz v7, :cond_0

    if-nez v4, :cond_0

    .line 108
    iget-object v4, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mFMStationInfoList:Ljava/util/ArrayList;

    invoke-virtual {v4, v5}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    goto :goto_1

    .line 109
    :cond_0
    iget-object v7, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mContext:Landroid/content/Context;

    invoke-static {v7}, Lcom/android/fmradio/database/FmStation;->getCurrentBand(Landroid/content/Context;)I

    move-result v7

    invoke-static {v7}, Lcom/android/fmradio/utils/FmUtils;->isFMBand(I)Z

    move-result v7

    if-nez v7, :cond_1

    if-ne v4, v6, :cond_1

    .line 110
    iget-object v4, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mAMStationInfoList:Ljava/util/ArrayList;

    invoke-virtual {v4, v5}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    :cond_1
    :goto_1
    add-int/lit8 v3, v3, 0x1

    goto/16 :goto_0

    .line 116
    :cond_2
    new-instance v0, Lcom/android/fmradio/views/AllStationInfoAdapter;

    iget-object v1, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mContext:Landroid/content/Context;

    invoke-direct {v0, v1}, Lcom/android/fmradio/views/AllStationInfoAdapter;-><init>(Landroid/content/Context;)V

    iput-object v0, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mAdapter:Lcom/android/fmradio/views/AllStationInfoAdapter;

    .line 117
    iget-object v0, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mContext:Landroid/content/Context;

    invoke-static {v0}, Lcom/android/fmradio/database/FmStation;->getCurrentBand(Landroid/content/Context;)I

    move-result v0

    invoke-static {v0}, Lcom/android/fmradio/utils/FmUtils;->isFMBand(I)Z

    move-result v0

    if-eqz v0, :cond_3

    .line 118
    iget-object v0, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mAdapter:Lcom/android/fmradio/views/AllStationInfoAdapter;

    iget-object v1, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mFMStationInfoList:Ljava/util/ArrayList;

    invoke-virtual {v0, v1}, Lcom/android/fmradio/views/AllStationInfoAdapter;->setAllStationInfoList(Ljava/util/ArrayList;)V

    goto :goto_2

    .line 120
    :cond_3
    iget-object v0, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mAdapter:Lcom/android/fmradio/views/AllStationInfoAdapter;

    iget-object v1, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mAMStationInfoList:Ljava/util/ArrayList;

    invoke-virtual {v0, v1}, Lcom/android/fmradio/views/AllStationInfoAdapter;->setAllStationInfoList(Ljava/util/ArrayList;)V

    .line 122
    :goto_2
    iget-object v0, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mAdapter:Lcom/android/fmradio/views/AllStationInfoAdapter;

    invoke-virtual {v0, p0}, Lcom/android/fmradio/views/AllStationInfoAdapter;->setAllStationInfoAdapterClickListener(Lcom/android/fmradio/views/AllStationInfoAdapter$OnAllStationInfoAdapterClickListener;)V

    return-void
.end method

.method private initView(Landroid/view/View;)V
    .locals 9

    .line 126
    iget-object v0, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mContext:Landroid/content/Context;

    check-cast v0, Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {v0}, Lcom/android/fmradio/FmMainActivity;->getScale()F

    move-result v0

    iput v0, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mScale:F

    .line 127
    sget-object v0, Lcom/android/fmradio/FmAllStationInfoFragment;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "mScale: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget v2, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mScale:F

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(F)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    const v0, 0x7f080049

    .line 129
    invoke-virtual {p1, v0}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object v0

    check-cast v0, Landroid/widget/Button;

    const v1, 0x7f080041

    .line 130
    invoke-virtual {p1, v1}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object v1

    check-cast v1, Landroid/widget/Button;

    const v2, 0x7f080045

    .line 131
    invoke-virtual {p1, v2}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object v2

    check-cast v2, Landroid/widget/Button;

    .line 133
    invoke-virtual {v0}, Landroid/widget/Button;->getLayoutParams()Landroid/view/ViewGroup$LayoutParams;

    move-result-object v3

    .line 134
    invoke-virtual {v1}, Landroid/widget/Button;->getLayoutParams()Landroid/view/ViewGroup$LayoutParams;

    move-result-object v4

    .line 135
    invoke-virtual {v2}, Landroid/widget/Button;->getLayoutParams()Landroid/view/ViewGroup$LayoutParams;

    move-result-object v5

    .line 137
    iget-object v6, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mContext:Landroid/content/Context;

    invoke-virtual {v6}, Landroid/content/Context;->getResources()Landroid/content/res/Resources;

    move-result-object v6

    const v7, 0x7f0607e3

    invoke-virtual {v6, v7}, Landroid/content/res/Resources;->getDimensionPixelSize(I)I

    move-result v6

    int-to-float v6, v6

    iget v7, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mScale:F

    mul-float/2addr v6, v7

    float-to-int v6, v6

    .line 138
    iget-object v7, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mContext:Landroid/content/Context;

    invoke-virtual {v7}, Landroid/content/Context;->getResources()Landroid/content/res/Resources;

    move-result-object v7

    const v8, 0x7f0602ae

    invoke-virtual {v7, v8}, Landroid/content/res/Resources;->getDimensionPixelSize(I)I

    move-result v7

    int-to-float v7, v7

    iget v8, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mScale:F

    mul-float/2addr v7, v8

    float-to-int v7, v7

    .line 140
    iput v6, v3, Landroid/view/ViewGroup$LayoutParams;->width:I

    .line 141
    iput v7, v3, Landroid/view/ViewGroup$LayoutParams;->height:I

    .line 143
    iput v6, v4, Landroid/view/ViewGroup$LayoutParams;->width:I

    .line 144
    iput v7, v4, Landroid/view/ViewGroup$LayoutParams;->height:I

    .line 146
    iput v6, v5, Landroid/view/ViewGroup$LayoutParams;->width:I

    .line 147
    iput v7, v5, Landroid/view/ViewGroup$LayoutParams;->height:I

    .line 149
    invoke-virtual {v0, v3}, Landroid/widget/Button;->setLayoutParams(Landroid/view/ViewGroup$LayoutParams;)V

    .line 150
    invoke-virtual {v1, v4}, Landroid/widget/Button;->setLayoutParams(Landroid/view/ViewGroup$LayoutParams;)V

    .line 151
    invoke-virtual {v2, v5}, Landroid/widget/Button;->setLayoutParams(Landroid/view/ViewGroup$LayoutParams;)V

    .line 153
    invoke-virtual {v0, p0}, Landroid/widget/Button;->setOnClickListener(Landroid/view/View$OnClickListener;)V

    .line 154
    invoke-virtual {v1, p0}, Landroid/widget/Button;->setOnClickListener(Landroid/view/View$OnClickListener;)V

    .line 155
    invoke-virtual {v2, p0}, Landroid/widget/Button;->setOnClickListener(Landroid/view/View$OnClickListener;)V

    const v0, 0x7f08008b

    .line 157
    invoke-virtual {p1, v0}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object p1

    check-cast p1, Landroid/widget/ListView;

    iput-object p1, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mLVAllStationInfoList:Landroid/widget/ListView;

    .line 158
    iget-object p1, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mLVAllStationInfoList:Landroid/widget/ListView;

    iget-object v0, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mAdapter:Lcom/android/fmradio/views/AllStationInfoAdapter;

    invoke-virtual {p1, v0}, Landroid/widget/ListView;->setAdapter(Landroid/widget/ListAdapter;)V

    .line 159
    iget-object p1, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mLVAllStationInfoList:Landroid/widget/ListView;

    invoke-virtual {p1, p0}, Landroid/widget/ListView;->setOnItemClickListener(Landroid/widget/AdapterView$OnItemClickListener;)V

    return-void
.end method


# virtual methods
.method public onClick(Landroid/view/View;)V
    .locals 4

    .line 164
    invoke-virtual {p1}, Landroid/view/View;->getId()I

    move-result p1

    const v0, 0x7f080041

    const-string v1, ""

    if-eq p1, v0, :cond_4

    const v0, 0x7f080045

    if-eq p1, v0, :cond_3

    const v0, 0x7f080049

    if-eq p1, v0, :cond_0

    goto :goto_1

    .line 168
    :cond_0
    iget-object p1, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mAllStationInfoList:Ljava/util/ArrayList;

    invoke-virtual {p1}, Ljava/util/ArrayList;->size()I

    move-result p1

    const/4 v0, 0x0

    :goto_0
    if-ge v0, p1, :cond_2

    .line 170
    iget-object v2, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mAllStationInfoList:Ljava/util/ArrayList;

    invoke-virtual {v2, v0}, Ljava/util/ArrayList;->get(I)Ljava/lang/Object;

    move-result-object v2

    check-cast v2, Lcom/android/fmradio/info/FmStationInfo;

    .line 171
    iget-object v3, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mFmStationInfoListener:Lcom/android/fmradio/FmAllStationInfoFragment$OnFmStationInfoListener;

    if-eqz v3, :cond_1

    .line 172
    invoke-virtual {v2}, Lcom/android/fmradio/info/FmStationInfo;->getFreq()I

    move-result v2

    invoke-interface {v3, v2, v1}, Lcom/android/fmradio/FmAllStationInfoFragment$OnFmStationInfoListener;->onUpdateStationName(ILjava/lang/String;)V

    :cond_1
    add-int/lit8 v0, v0, 0x1

    goto :goto_0

    .line 176
    :cond_2
    iget-object p1, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mAllStationInfoList:Ljava/util/ArrayList;

    invoke-virtual {p1}, Ljava/util/ArrayList;->clear()V

    .line 177
    iget-object p1, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mFMStationInfoList:Ljava/util/ArrayList;

    invoke-virtual {p1}, Ljava/util/ArrayList;->clear()V

    .line 178
    iget-object p1, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mAMStationInfoList:Ljava/util/ArrayList;

    invoke-virtual {p1}, Ljava/util/ArrayList;->clear()V

    .line 180
    iget-object p1, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mContext:Landroid/content/Context;

    const-string v0, "all_station_info_key"

    invoke-static {p1, v0, v1}, Lcom/android/fmradio/utils/AllStationInfoUtil;->setAllStationInfoData(Landroid/content/Context;Ljava/lang/String;Ljava/lang/String;)V

    .line 182
    iget-object p1, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mContext:Landroid/content/Context;

    const-string v0, "rds_ps_edit_key"

    invoke-static {p1, v0, v1}, Lcom/android/fmradio/utils/RdsPsInfoUtil;->setRdsPsEditData(Landroid/content/Context;Ljava/lang/String;Ljava/lang/String;)V

    .line 184
    iget-object p1, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mAdapter:Lcom/android/fmradio/views/AllStationInfoAdapter;

    invoke-virtual {p1}, Lcom/android/fmradio/views/AllStationInfoAdapter;->notifyDataSetChanged()V

    goto :goto_1

    .line 195
    :cond_3
    invoke-virtual {p0}, Lcom/android/fmradio/FmAllStationInfoFragment;->dismiss()V

    goto :goto_1

    .line 188
    :cond_4
    new-instance p1, Lcom/android/fmradio/FmStationNameEditFragment;

    invoke-direct {p1}, Lcom/android/fmradio/FmStationNameEditFragment;-><init>()V

    .line 189
    invoke-virtual {p1, p0}, Lcom/android/fmradio/FmStationNameEditFragment;->setFmStationNameEditListener(Lcom/android/fmradio/FmStationNameEditFragment$OnFmStationNameEditListener;)V

    .line 190
    iget-object v0, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mContext:Landroid/content/Context;

    invoke-static {v0}, Lcom/android/fmradio/database/FmStation;->getCurrentStation(Landroid/content/Context;)I

    move-result v0

    invoke-virtual {p1, v0}, Lcom/android/fmradio/FmStationNameEditFragment;->setStationFreq(I)V

    .line 191
    invoke-virtual {p0}, Lcom/android/fmradio/FmAllStationInfoFragment;->getActivity()Landroid/app/Activity;

    move-result-object v0

    invoke-virtual {v0}, Landroid/app/Activity;->getFragmentManager()Landroid/app/FragmentManager;

    move-result-object v0

    invoke-virtual {p1, v0, v1}, Lcom/android/fmradio/FmStationNameEditFragment;->show(Landroid/app/FragmentManager;Ljava/lang/String;)V

    :goto_1
    return-void
.end method

.method public onCreate(Landroid/os/Bundle;)V
    .locals 2

    .line 48
    sget-object v0, Lcom/android/fmradio/FmAllStationInfoFragment;->TAG:Ljava/lang/String;

    const-string v1, "start"

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    const/4 v0, 0x1

    const/4 v1, 0x0

    .line 49
    invoke-virtual {p0, v0, v1}, Lcom/android/fmradio/FmAllStationInfoFragment;->setStyle(II)V

    .line 50
    invoke-super {p0, p1}, Landroid/app/DialogFragment;->onCreate(Landroid/os/Bundle;)V

    .line 52
    invoke-virtual {p0}, Lcom/android/fmradio/FmAllStationInfoFragment;->getActivity()Landroid/app/Activity;

    move-result-object p1

    iput-object p1, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mContext:Landroid/content/Context;

    .line 54
    invoke-direct {p0}, Lcom/android/fmradio/FmAllStationInfoFragment;->initData()V

    return-void
.end method

.method public onCreateView(Landroid/view/LayoutInflater;Landroid/view/ViewGroup;Landroid/os/Bundle;)Landroid/view/View;
    .locals 1

    .line 83
    sget-object p3, Lcom/android/fmradio/FmAllStationInfoFragment;->TAG:Ljava/lang/String;

    const-string v0, "start"

    invoke-static {p3, v0}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    const p3, 0x7f0b0020

    .line 85
    invoke-virtual {p1, p3, p2}, Landroid/view/LayoutInflater;->inflate(ILandroid/view/ViewGroup;)Landroid/view/View;

    move-result-object p1

    .line 86
    invoke-direct {p0, p1}, Lcom/android/fmradio/FmAllStationInfoFragment;->initView(Landroid/view/View;)V

    return-object p1
.end method

.method public onDelete(I)V
    .locals 7

    .line 299
    sget-object v0, Lcom/android/fmradio/FmAllStationInfoFragment;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "freq: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 300
    iget-object v0, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mAllStationInfoList:Ljava/util/ArrayList;

    invoke-virtual {v0}, Ljava/util/ArrayList;->size()I

    move-result v0

    const/4 v1, 0x0

    move v2, v1

    :goto_0
    if-ge v2, v0, :cond_1

    .line 302
    iget-object v3, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mAllStationInfoList:Ljava/util/ArrayList;

    invoke-virtual {v3, v2}, Ljava/util/ArrayList;->get(I)Ljava/lang/Object;

    move-result-object v3

    check-cast v3, Lcom/android/fmradio/info/FmStationInfo;

    .line 303
    invoke-virtual {v3}, Lcom/android/fmradio/info/FmStationInfo;->getFreq()I

    move-result v3

    if-ne v3, p1, :cond_0

    .line 304
    iget-object v0, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mAllStationInfoList:Ljava/util/ArrayList;

    invoke-virtual {v0, v2}, Ljava/util/ArrayList;->remove(I)Ljava/lang/Object;

    .line 306
    iget-object v0, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mFmStationInfoListener:Lcom/android/fmradio/FmAllStationInfoFragment$OnFmStationInfoListener;

    if-eqz v0, :cond_1

    const-string v2, ""

    .line 307
    invoke-interface {v0, p1, v2}, Lcom/android/fmradio/FmAllStationInfoFragment$OnFmStationInfoListener;->onUpdateStationName(ILjava/lang/String;)V

    goto :goto_1

    :cond_0
    add-int/lit8 v2, v2, 0x1

    goto :goto_0

    .line 313
    :cond_1
    :goto_1
    iget-object v0, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mContext:Landroid/content/Context;

    invoke-static {v0}, Lcom/android/fmradio/database/FmStation;->getCurrentBand(Landroid/content/Context;)I

    move-result v0

    invoke-static {v0}, Lcom/android/fmradio/utils/FmUtils;->isFMBand(I)Z

    move-result v0

    if-eqz v0, :cond_3

    .line 314
    iget-object v0, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mFMStationInfoList:Ljava/util/ArrayList;

    invoke-virtual {v0}, Ljava/util/ArrayList;->size()I

    move-result v0

    move v2, v1

    :goto_2
    if-ge v2, v0, :cond_5

    .line 316
    iget-object v3, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mFMStationInfoList:Ljava/util/ArrayList;

    invoke-virtual {v3, v2}, Ljava/util/ArrayList;->get(I)Ljava/lang/Object;

    move-result-object v3

    check-cast v3, Lcom/android/fmradio/info/FmStationInfo;

    .line 317
    invoke-virtual {v3}, Lcom/android/fmradio/info/FmStationInfo;->getFreq()I

    move-result v3

    if-ne v3, p1, :cond_2

    .line 318
    iget-object p1, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mFMStationInfoList:Ljava/util/ArrayList;

    invoke-virtual {p1, v2}, Ljava/util/ArrayList;->remove(I)Ljava/lang/Object;

    goto :goto_4

    :cond_2
    add-int/lit8 v2, v2, 0x1

    goto :goto_2

    .line 323
    :cond_3
    iget-object v0, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mAMStationInfoList:Ljava/util/ArrayList;

    invoke-virtual {v0}, Ljava/util/ArrayList;->size()I

    move-result v0

    move v2, v1

    :goto_3
    if-ge v2, v0, :cond_5

    .line 325
    iget-object v3, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mAMStationInfoList:Ljava/util/ArrayList;

    invoke-virtual {v3, v2}, Ljava/util/ArrayList;->get(I)Ljava/lang/Object;

    move-result-object v3

    check-cast v3, Lcom/android/fmradio/info/FmStationInfo;

    .line 326
    invoke-virtual {v3}, Lcom/android/fmradio/info/FmStationInfo;->getFreq()I

    move-result v3

    if-ne v3, p1, :cond_4

    .line 327
    iget-object p1, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mAMStationInfoList:Ljava/util/ArrayList;

    invoke-virtual {p1, v2}, Ljava/util/ArrayList;->remove(I)Ljava/lang/Object;

    goto :goto_4

    :cond_4
    add-int/lit8 v2, v2, 0x1

    goto :goto_3

    .line 333
    :cond_5
    :goto_4
    new-instance p1, Ljava/lang/StringBuilder;

    invoke-direct {p1}, Ljava/lang/StringBuilder;-><init>()V

    .line 334
    iget-object v0, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mAllStationInfoList:Ljava/util/ArrayList;

    invoke-virtual {v0}, Ljava/util/ArrayList;->size()I

    move-result v0

    move v2, v1

    :goto_5
    const-string v3, "#"

    const-string v4, ","

    if-ge v2, v0, :cond_7

    .line 336
    iget-object v5, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mAllStationInfoList:Ljava/util/ArrayList;

    invoke-virtual {v5, v2}, Ljava/util/ArrayList;->get(I)Ljava/lang/Object;

    move-result-object v5

    check-cast v5, Lcom/android/fmradio/info/FmStationInfo;

    .line 337
    invoke-virtual {v5}, Lcom/android/fmradio/info/FmStationInfo;->getBand()I

    move-result v6

    invoke-virtual {p1, v6}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {p1, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 338
    invoke-virtual {v5}, Lcom/android/fmradio/info/FmStationInfo;->getFreq()I

    move-result v6

    invoke-virtual {p1, v6}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {p1, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 339
    invoke-virtual {v5}, Lcom/android/fmradio/info/FmStationInfo;->getStationName()Ljava/lang/String;

    move-result-object v4

    invoke-virtual {p1, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    add-int/lit8 v4, v0, -0x1

    if-eq v2, v4, :cond_6

    .line 341
    invoke-virtual {p1, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    :cond_6
    add-int/lit8 v2, v2, 0x1

    goto :goto_5

    .line 344
    :cond_7
    iget-object v2, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mContext:Landroid/content/Context;

    invoke-virtual {p1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p1

    const-string v5, "all_station_info_key"

    invoke-static {v2, v5, p1}, Lcom/android/fmradio/utils/AllStationInfoUtil;->setAllStationInfoData(Landroid/content/Context;Ljava/lang/String;Ljava/lang/String;)V

    .line 346
    new-instance p1, Ljava/lang/StringBuilder;

    invoke-direct {p1}, Ljava/lang/StringBuilder;-><init>()V

    :goto_6
    if-ge v1, v0, :cond_9

    .line 348
    iget-object v2, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mAllStationInfoList:Ljava/util/ArrayList;

    invoke-virtual {v2, v1}, Ljava/util/ArrayList;->get(I)Ljava/lang/Object;

    move-result-object v2

    check-cast v2, Lcom/android/fmradio/info/FmStationInfo;

    .line 349
    invoke-virtual {v2}, Lcom/android/fmradio/info/FmStationInfo;->getFreq()I

    move-result v5

    invoke-virtual {p1, v5}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {p1, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 350
    invoke-virtual {v2}, Lcom/android/fmradio/info/FmStationInfo;->getStationName()Ljava/lang/String;

    move-result-object v2

    invoke-virtual {p1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    add-int/lit8 v2, v0, -0x1

    if-eq v1, v2, :cond_8

    .line 352
    invoke-virtual {p1, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    :cond_8
    add-int/lit8 v1, v1, 0x1

    goto :goto_6

    .line 355
    :cond_9
    sget-object v0, Lcom/android/fmradio/FmAllStationInfoFragment;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "allStationInfo: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {p1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v2

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 356
    iget-object v0, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mContext:Landroid/content/Context;

    invoke-virtual {p1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p1

    const-string v1, "rds_ps_edit_key"

    invoke-static {v0, v1, p1}, Lcom/android/fmradio/utils/RdsPsInfoUtil;->setRdsPsEditData(Landroid/content/Context;Ljava/lang/String;Ljava/lang/String;)V

    .line 358
    iget-object p1, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mAdapter:Lcom/android/fmradio/views/AllStationInfoAdapter;

    invoke-virtual {p1}, Lcom/android/fmradio/views/AllStationInfoAdapter;->notifyDataSetChanged()V

    return-void
.end method

.method public onEdit(I)V
    .locals 3

    .line 290
    sget-object v0, Lcom/android/fmradio/FmAllStationInfoFragment;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "freq: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 291
    new-instance v0, Lcom/android/fmradio/FmStationNameEditFragment;

    invoke-direct {v0}, Lcom/android/fmradio/FmStationNameEditFragment;-><init>()V

    .line 292
    invoke-virtual {v0, p0}, Lcom/android/fmradio/FmStationNameEditFragment;->setFmStationNameEditListener(Lcom/android/fmradio/FmStationNameEditFragment$OnFmStationNameEditListener;)V

    .line 293
    invoke-virtual {v0, p1}, Lcom/android/fmradio/FmStationNameEditFragment;->setStationFreq(I)V

    .line 294
    invoke-virtual {p0}, Lcom/android/fmradio/FmAllStationInfoFragment;->getActivity()Landroid/app/Activity;

    move-result-object p1

    invoke-virtual {p1}, Landroid/app/Activity;->getFragmentManager()Landroid/app/FragmentManager;

    move-result-object p1

    const-string v1, ""

    invoke-virtual {v0, p1, v1}, Lcom/android/fmradio/FmStationNameEditFragment;->show(Landroid/app/FragmentManager;Ljava/lang/String;)V

    return-void
.end method

.method public onItemClick(Landroid/widget/AdapterView;Landroid/view/View;IJ)V
    .locals 0
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Landroid/widget/AdapterView<",
            "*>;",
            "Landroid/view/View;",
            "IJ)V"
        }
    .end annotation

    .line 205
    invoke-virtual {p1}, Landroid/widget/AdapterView;->getAdapter()Landroid/widget/Adapter;

    move-result-object p1

    invoke-interface {p1, p3}, Landroid/widget/Adapter;->getItem(I)Ljava/lang/Object;

    move-result-object p1

    check-cast p1, Lcom/android/fmradio/info/FmStationInfo;

    if-eqz p1, :cond_0

    .line 207
    sget-object p2, Lcom/android/fmradio/FmAllStationInfoFragment;->TAG:Ljava/lang/String;

    new-instance p3, Ljava/lang/StringBuilder;

    invoke-direct {p3}, Ljava/lang/StringBuilder;-><init>()V

    const-string p4, "freq: "

    invoke-virtual {p3, p4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {p1}, Lcom/android/fmradio/info/FmStationInfo;->getFreq()I

    move-result p4

    invoke-virtual {p3, p4}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {p3}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p3

    invoke-static {p2, p3}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 208
    iget-object p2, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mFmStationInfoListener:Lcom/android/fmradio/FmAllStationInfoFragment$OnFmStationInfoListener;

    if-eqz p2, :cond_0

    .line 209
    invoke-virtual {p1}, Lcom/android/fmradio/info/FmStationInfo;->getFreq()I

    move-result p1

    invoke-interface {p2, p1}, Lcom/android/fmradio/FmAllStationInfoFragment$OnFmStationInfoListener;->onItemClickListener(I)V

    :cond_0
    return-void
.end method

.method public onStart()V
    .locals 4

    .line 59
    sget-object v0, Lcom/android/fmradio/FmAllStationInfoFragment;->TAG:Ljava/lang/String;

    const-string v1, "start"

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 60
    invoke-virtual {p0}, Lcom/android/fmradio/FmAllStationInfoFragment;->getDialog()Landroid/app/Dialog;

    move-result-object v0

    invoke-virtual {v0}, Landroid/app/Dialog;->getWindow()Landroid/view/Window;

    move-result-object v0

    .line 61
    iget-object v1, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mContext:Landroid/content/Context;

    invoke-virtual {v1}, Landroid/content/Context;->getResources()Landroid/content/res/Resources;

    move-result-object v1

    const v2, 0x7f060a2f

    invoke-virtual {v1, v2}, Landroid/content/res/Resources;->getDimensionPixelSize(I)I

    move-result v1

    .line 62
    iget-object v2, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mContext:Landroid/content/Context;

    invoke-virtual {v2}, Landroid/content/Context;->getResources()Landroid/content/res/Resources;

    move-result-object v2

    const v3, 0x7f060229

    invoke-virtual {v2, v3}, Landroid/content/res/Resources;->getDimensionPixelSize(I)I

    move-result v2

    int-to-float v2, v2

    .line 63
    iget v3, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mScale:F

    mul-float/2addr v2, v3

    float-to-int v2, v2

    invoke-virtual {v0, v1, v2}, Landroid/view/Window;->setLayout(II)V

    .line 65
    invoke-super {p0}, Landroid/app/DialogFragment;->onStart()V

    .line 67
    invoke-virtual {p0}, Lcom/android/fmradio/FmAllStationInfoFragment;->getDialog()Landroid/app/Dialog;

    move-result-object v0

    if-eqz v0, :cond_1

    .line 69
    invoke-virtual {v0}, Landroid/app/Dialog;->getWindow()Landroid/view/Window;

    move-result-object v1

    invoke-virtual {v1}, Landroid/view/Window;->getAttributes()Landroid/view/WindowManager$LayoutParams;

    move-result-object v1

    .line 71
    invoke-virtual {p0}, Lcom/android/fmradio/FmAllStationInfoFragment;->getResources()Landroid/content/res/Resources;

    move-result-object v2

    invoke-virtual {v2}, Landroid/content/res/Resources;->getConfiguration()Landroid/content/res/Configuration;

    move-result-object v2

    iget v2, v2, Landroid/content/res/Configuration;->uiMode:I

    const/16 v3, 0x20

    and-int/2addr v2, v3

    if-ne v2, v3, :cond_0

    const/4 v2, 0x0

    .line 73
    iput v2, v1, Landroid/view/WindowManager$LayoutParams;->dimAmount:F

    goto :goto_0

    :cond_0
    const/high16 v2, 0x3f000000    # 0.5f

    .line 75
    iput v2, v1, Landroid/view/WindowManager$LayoutParams;->dimAmount:F

    .line 77
    :goto_0
    invoke-virtual {v0}, Landroid/app/Dialog;->getWindow()Landroid/view/Window;

    move-result-object v0

    invoke-virtual {v0, v1}, Landroid/view/Window;->setAttributes(Landroid/view/WindowManager$LayoutParams;)V

    :cond_1
    return-void
.end method

.method public onUpdateStationName(ILjava/lang/String;)V
    .locals 9

    if-eqz p2, :cond_8

    .line 218
    iget-object v0, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mAllStationInfoList:Ljava/util/ArrayList;

    invoke-virtual {v0}, Ljava/util/ArrayList;->iterator()Ljava/util/Iterator;

    move-result-object v0

    :cond_0
    invoke-interface {v0}, Ljava/util/Iterator;->hasNext()Z

    move-result v1

    const/4 v2, 0x0

    const/4 v3, 0x1

    if-eqz v1, :cond_1

    invoke-interface {v0}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v1

    check-cast v1, Lcom/android/fmradio/info/FmStationInfo;

    .line 219
    invoke-virtual {v1}, Lcom/android/fmradio/info/FmStationInfo;->getFreq()I

    move-result v4

    if-ne v4, p1, :cond_0

    .line 220
    invoke-virtual {v1, p2}, Lcom/android/fmradio/info/FmStationInfo;->setStationName(Ljava/lang/String;)V

    move v0, v3

    goto :goto_0

    :cond_1
    move v0, v2

    .line 227
    :goto_0
    sget-object v1, Lcom/android/fmradio/FmAllStationInfoFragment;->TAG:Ljava/lang/String;

    new-instance v4, Ljava/lang/StringBuilder;

    invoke-direct {v4}, Ljava/lang/StringBuilder;-><init>()V

    const-string v5, "stationFreq: "

    invoke-virtual {v4, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v4, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v5, " - stationName: "

    invoke-virtual {v4, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v4, p2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    const-string v5, " - exist: "

    invoke-virtual {v4, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v4, v0}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    invoke-virtual {v4}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v4

    invoke-static {v1, v4}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    if-nez v0, :cond_3

    .line 229
    new-instance v0, Lcom/android/fmradio/info/FmStationInfo;

    invoke-direct {v0}, Lcom/android/fmradio/info/FmStationInfo;-><init>()V

    .line 230
    invoke-virtual {v0, p1}, Lcom/android/fmradio/info/FmStationInfo;->setFreq(I)V

    .line 231
    invoke-virtual {v0, p2}, Lcom/android/fmradio/info/FmStationInfo;->setStationName(Ljava/lang/String;)V

    .line 232
    iget-object v1, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mContext:Landroid/content/Context;

    invoke-static {v1}, Lcom/android/fmradio/database/FmStation;->getCurrentBand(Landroid/content/Context;)I

    move-result v1

    invoke-static {v1}, Lcom/android/fmradio/utils/FmUtils;->isFMBand(I)Z

    move-result v1

    if-eqz v1, :cond_2

    .line 233
    invoke-virtual {v0, v2}, Lcom/android/fmradio/info/FmStationInfo;->setBand(I)V

    .line 234
    iget-object v1, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mFMStationInfoList:Ljava/util/ArrayList;

    invoke-virtual {v1, v0}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    goto :goto_1

    .line 236
    :cond_2
    invoke-virtual {v0, v3}, Lcom/android/fmradio/info/FmStationInfo;->setBand(I)V

    .line 237
    iget-object v1, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mAMStationInfoList:Ljava/util/ArrayList;

    invoke-virtual {v1, v0}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 240
    :goto_1
    iget-object v1, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mAllStationInfoList:Ljava/util/ArrayList;

    invoke-virtual {v1, v0}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 243
    :cond_3
    new-instance v0, Ljava/lang/StringBuilder;

    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    .line 244
    iget-object v1, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mAllStationInfoList:Ljava/util/ArrayList;

    invoke-virtual {v1}, Ljava/util/ArrayList;->size()I

    move-result v1

    move v3, v2

    :goto_2
    const-string v4, "#"

    const-string v5, ","

    if-ge v3, v1, :cond_5

    .line 246
    iget-object v6, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mAllStationInfoList:Ljava/util/ArrayList;

    invoke-virtual {v6, v3}, Ljava/util/ArrayList;->get(I)Ljava/lang/Object;

    move-result-object v6

    check-cast v6, Lcom/android/fmradio/info/FmStationInfo;

    .line 247
    invoke-virtual {v6}, Lcom/android/fmradio/info/FmStationInfo;->getBand()I

    move-result v7

    invoke-virtual {v0, v7}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v0, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 248
    invoke-virtual {v6}, Lcom/android/fmradio/info/FmStationInfo;->getFreq()I

    move-result v7

    invoke-virtual {v0, v7}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v0, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 249
    invoke-virtual {v6}, Lcom/android/fmradio/info/FmStationInfo;->getStationName()Ljava/lang/String;

    move-result-object v5

    invoke-virtual {v0, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    add-int/lit8 v5, v1, -0x1

    if-eq v3, v5, :cond_4

    .line 251
    invoke-virtual {v0, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    :cond_4
    add-int/lit8 v3, v3, 0x1

    goto :goto_2

    .line 254
    :cond_5
    sget-object v3, Lcom/android/fmradio/FmAllStationInfoFragment;->TAG:Ljava/lang/String;

    new-instance v6, Ljava/lang/StringBuilder;

    invoke-direct {v6}, Ljava/lang/StringBuilder;-><init>()V

    const-string v7, "allStationInfo: "

    invoke-virtual {v6, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v8

    invoke-virtual {v6, v8}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v6}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v6

    invoke-static {v3, v6}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 255
    iget-object v3, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mContext:Landroid/content/Context;

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    const-string v6, "all_station_info_key"

    invoke-static {v3, v6, v0}, Lcom/android/fmradio/utils/AllStationInfoUtil;->setAllStationInfoData(Landroid/content/Context;Ljava/lang/String;Ljava/lang/String;)V

    .line 261
    new-instance v0, Ljava/lang/StringBuilder;

    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    :goto_3
    if-ge v2, v1, :cond_7

    .line 263
    iget-object v3, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mAllStationInfoList:Ljava/util/ArrayList;

    invoke-virtual {v3, v2}, Ljava/util/ArrayList;->get(I)Ljava/lang/Object;

    move-result-object v3

    check-cast v3, Lcom/android/fmradio/info/FmStationInfo;

    .line 264
    invoke-virtual {v3}, Lcom/android/fmradio/info/FmStationInfo;->getFreq()I

    move-result v6

    invoke-virtual {v0, v6}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v0, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 265
    invoke-virtual {v3}, Lcom/android/fmradio/info/FmStationInfo;->getStationName()Ljava/lang/String;

    move-result-object v3

    invoke-virtual {v0, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    add-int/lit8 v3, v1, -0x1

    if-eq v2, v3, :cond_6

    .line 267
    invoke-virtual {v0, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    :cond_6
    add-int/lit8 v2, v2, 0x1

    goto :goto_3

    .line 270
    :cond_7
    sget-object v1, Lcom/android/fmradio/FmAllStationInfoFragment;->TAG:Ljava/lang/String;

    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2}, Ljava/lang/StringBuilder;-><init>()V

    invoke-virtual {v2, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v3

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v2

    invoke-static {v1, v2}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 271
    iget-object v1, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mContext:Landroid/content/Context;

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    const-string v2, "rds_ps_edit_key"

    invoke-static {v1, v2, v0}, Lcom/android/fmradio/utils/RdsPsInfoUtil;->setRdsPsEditData(Landroid/content/Context;Ljava/lang/String;Ljava/lang/String;)V

    .line 273
    iget-object v0, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mAdapter:Lcom/android/fmradio/views/AllStationInfoAdapter;

    invoke-virtual {v0}, Lcom/android/fmradio/views/AllStationInfoAdapter;->notifyDataSetChanged()V

    .line 275
    iget-object v0, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mFmStationInfoListener:Lcom/android/fmradio/FmAllStationInfoFragment$OnFmStationInfoListener;

    if-eqz v0, :cond_8

    .line 276
    invoke-interface {v0, p1, p2}, Lcom/android/fmradio/FmAllStationInfoFragment$OnFmStationInfoListener;->onUpdateStationName(ILjava/lang/String;)V

    :cond_8
    return-void
.end method

.method public setFmStationInfoListener(Lcom/android/fmradio/FmAllStationInfoFragment$OnFmStationInfoListener;)V
    .locals 0

    .line 285
    iput-object p1, p0, Lcom/android/fmradio/FmAllStationInfoFragment;->mFmStationInfoListener:Lcom/android/fmradio/FmAllStationInfoFragment$OnFmStationInfoListener;

    return-void
.end method
