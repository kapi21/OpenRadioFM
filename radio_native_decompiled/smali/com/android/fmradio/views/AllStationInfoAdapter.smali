.class public Lcom/android/fmradio/views/AllStationInfoAdapter;
.super Landroid/widget/BaseAdapter;
.source "AllStationInfoAdapter.java"

# interfaces
.implements Landroid/view/View$OnClickListener;


# annotations
.annotation system Ldalvik/annotation/MemberClasses;
    value = {
        Lcom/android/fmradio/views/AllStationInfoAdapter$OnAllStationInfoAdapterClickListener;,
        Lcom/android/fmradio/views/AllStationInfoAdapter$ViewHolder;
    }
.end annotation


# static fields
.field private static final TAG:Ljava/lang/String;


# instance fields
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

.field private final mInflater:Landroid/view/LayoutInflater;

.field private mOnAllStationInfoAdapterClickListener:Lcom/android/fmradio/views/AllStationInfoAdapter$OnAllStationInfoAdapterClickListener;


# direct methods
.method static constructor <clinit>()V
    .locals 1

    .line 23
    const-class v0, Lcom/android/fmradio/views/AllStationInfoAdapter;

    invoke-virtual {v0}, Ljava/lang/Class;->getSimpleName()Ljava/lang/String;

    move-result-object v0

    sput-object v0, Lcom/android/fmradio/views/AllStationInfoAdapter;->TAG:Ljava/lang/String;

    return-void
.end method

.method public constructor <init>(Landroid/content/Context;)V
    .locals 1

    .line 31
    invoke-direct {p0}, Landroid/widget/BaseAdapter;-><init>()V

    .line 29
    new-instance v0, Ljava/util/ArrayList;

    invoke-direct {v0}, Ljava/util/ArrayList;-><init>()V

    iput-object v0, p0, Lcom/android/fmradio/views/AllStationInfoAdapter;->mAllStationInfoList:Ljava/util/ArrayList;

    .line 32
    iput-object p1, p0, Lcom/android/fmradio/views/AllStationInfoAdapter;->mContext:Landroid/content/Context;

    .line 34
    invoke-static {p1}, Landroid/view/LayoutInflater;->from(Landroid/content/Context;)Landroid/view/LayoutInflater;

    move-result-object p1

    iput-object p1, p0, Lcom/android/fmradio/views/AllStationInfoAdapter;->mInflater:Landroid/view/LayoutInflater;

    return-void
.end method


# virtual methods
.method public getCount()I
    .locals 1

    .line 43
    iget-object v0, p0, Lcom/android/fmradio/views/AllStationInfoAdapter;->mAllStationInfoList:Ljava/util/ArrayList;

    invoke-virtual {v0}, Ljava/util/ArrayList;->size()I

    move-result v0

    return v0
.end method

.method public getItem(I)Ljava/lang/Object;
    .locals 1

    .line 48
    iget-object v0, p0, Lcom/android/fmradio/views/AllStationInfoAdapter;->mAllStationInfoList:Ljava/util/ArrayList;

    invoke-virtual {v0, p1}, Ljava/util/ArrayList;->get(I)Ljava/lang/Object;

    move-result-object p1

    return-object p1
.end method

.method public getItemId(I)J
    .locals 2

    int-to-long v0, p1

    return-wide v0
.end method

.method public getView(ILandroid/view/View;Landroid/view/ViewGroup;)Landroid/view/View;
    .locals 9

    const/4 v0, 0x0

    if-nez p2, :cond_0

    .line 60
    new-instance p2, Lcom/android/fmradio/views/AllStationInfoAdapter$ViewHolder;

    const/4 v1, 0x0

    invoke-direct {p2, v1}, Lcom/android/fmradio/views/AllStationInfoAdapter$ViewHolder;-><init>(Lcom/android/fmradio/views/AllStationInfoAdapter$1;)V

    .line 62
    iget-object v1, p0, Lcom/android/fmradio/views/AllStationInfoAdapter;->mInflater:Landroid/view/LayoutInflater;

    const v2, 0x7f0b001c

    invoke-virtual {v1, v2, p3, v0}, Landroid/view/LayoutInflater;->inflate(ILandroid/view/ViewGroup;Z)Landroid/view/View;

    move-result-object p3

    .line 63
    new-instance v1, Landroid/widget/LinearLayout$LayoutParams;

    const/4 v2, -0x1

    iget-object v3, p0, Lcom/android/fmradio/views/AllStationInfoAdapter;->mContext:Landroid/content/Context;

    .line 64
    invoke-virtual {v3}, Landroid/content/Context;->getResources()Landroid/content/res/Resources;

    move-result-object v3

    const v4, 0x7f0602f0

    invoke-virtual {v3, v4}, Landroid/content/res/Resources;->getDimensionPixelSize(I)I

    move-result v3

    invoke-direct {v1, v2, v3}, Landroid/widget/LinearLayout$LayoutParams;-><init>(II)V

    .line 63
    invoke-virtual {p3, v1}, Landroid/view/View;->setLayoutParams(Landroid/view/ViewGroup$LayoutParams;)V

    const v1, 0x7f0800de

    .line 66
    invoke-virtual {p3, v1}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object v1

    check-cast v1, Landroid/widget/TextView;

    iput-object v1, p2, Lcom/android/fmradio/views/AllStationInfoAdapter$ViewHolder;->mStationIndex:Landroid/widget/TextView;

    const v1, 0x7f0800dd

    .line 67
    invoke-virtual {p3, v1}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object v1

    check-cast v1, Landroid/widget/TextView;

    iput-object v1, p2, Lcom/android/fmradio/views/AllStationInfoAdapter$ViewHolder;->mStationFreq:Landroid/widget/TextView;

    const v1, 0x7f0800df

    .line 68
    invoke-virtual {p3, v1}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object v1

    check-cast v1, Landroid/widget/TextView;

    iput-object v1, p2, Lcom/android/fmradio/views/AllStationInfoAdapter$ViewHolder;->mStationName:Landroid/widget/TextView;

    const v1, 0x7f08004a

    .line 69
    invoke-virtual {p3, v1}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object v1

    check-cast v1, Landroid/widget/ImageView;

    iput-object v1, p2, Lcom/android/fmradio/views/AllStationInfoAdapter$ViewHolder;->mBtnEdit:Landroid/widget/ImageView;

    const v1, 0x7f080048

    .line 70
    invoke-virtual {p3, v1}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object v1

    check-cast v1, Landroid/widget/ImageView;

    iput-object v1, p2, Lcom/android/fmradio/views/AllStationInfoAdapter$ViewHolder;->mBtnDelete:Landroid/widget/ImageView;

    .line 72
    iget-object v1, p2, Lcom/android/fmradio/views/AllStationInfoAdapter$ViewHolder;->mBtnEdit:Landroid/widget/ImageView;

    invoke-virtual {v1, p0}, Landroid/widget/ImageView;->setOnClickListener(Landroid/view/View$OnClickListener;)V

    .line 73
    iget-object v1, p2, Lcom/android/fmradio/views/AllStationInfoAdapter$ViewHolder;->mBtnDelete:Landroid/widget/ImageView;

    invoke-virtual {v1, p0}, Landroid/widget/ImageView;->setOnClickListener(Landroid/view/View$OnClickListener;)V

    .line 75
    invoke-virtual {p3, p2}, Landroid/view/View;->setTag(Ljava/lang/Object;)V

    goto :goto_0

    .line 77
    :cond_0
    invoke-virtual {p2}, Landroid/view/View;->getTag()Ljava/lang/Object;

    move-result-object p3

    check-cast p3, Lcom/android/fmradio/views/AllStationInfoAdapter$ViewHolder;

    move-object v8, p3

    move-object p3, p2

    move-object p2, v8

    .line 80
    :goto_0
    iget-object v1, p0, Lcom/android/fmradio/views/AllStationInfoAdapter;->mAllStationInfoList:Ljava/util/ArrayList;

    invoke-virtual {v1, p1}, Ljava/util/ArrayList;->get(I)Ljava/lang/Object;

    move-result-object v1

    check-cast v1, Lcom/android/fmradio/info/FmStationInfo;

    if-eqz v1, :cond_2

    .line 82
    iget-object v2, p2, Lcom/android/fmradio/views/AllStationInfoAdapter$ViewHolder;->mBtnEdit:Landroid/widget/ImageView;

    invoke-virtual {v1}, Lcom/android/fmradio/info/FmStationInfo;->getFreq()I

    move-result v3

    invoke-static {v3}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v3

    invoke-virtual {v2, v3}, Landroid/widget/ImageView;->setTag(Ljava/lang/Object;)V

    .line 83
    iget-object v2, p2, Lcom/android/fmradio/views/AllStationInfoAdapter$ViewHolder;->mBtnDelete:Landroid/widget/ImageView;

    invoke-virtual {v1}, Lcom/android/fmradio/info/FmStationInfo;->getFreq()I

    move-result v3

    invoke-static {v3}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v3

    invoke-virtual {v2, v3}, Landroid/widget/ImageView;->setTag(Ljava/lang/Object;)V

    .line 85
    iget-object v2, p2, Lcom/android/fmradio/views/AllStationInfoAdapter$ViewHolder;->mStationIndex:Landroid/widget/TextView;

    sget-object v3, Ljava/util/Locale;->ENGLISH:Ljava/util/Locale;

    const/4 v4, 0x1

    new-array v5, v4, [Ljava/lang/Object;

    add-int/2addr p1, v4

    invoke-static {p1}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object p1

    aput-object p1, v5, v0

    const-string p1, "%d."

    invoke-static {v3, p1, v5}, Ljava/lang/String;->format(Ljava/util/Locale;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;

    move-result-object p1

    invoke-virtual {v2, p1}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    .line 86
    iget-object p1, p0, Lcom/android/fmradio/views/AllStationInfoAdapter;->mContext:Landroid/content/Context;

    invoke-static {p1}, Lcom/android/fmradio/database/FmStation;->getCurrentBand(Landroid/content/Context;)I

    move-result p1

    invoke-static {p1}, Lcom/android/fmradio/utils/FmUtils;->isFMBand(I)Z

    move-result p1

    const/4 v2, 0x2

    const-string v3, "%s(%s)"

    if-eqz p1, :cond_1

    .line 87
    iget-object p1, p2, Lcom/android/fmradio/views/AllStationInfoAdapter$ViewHolder;->mStationFreq:Landroid/widget/TextView;

    sget-object v5, Ljava/util/Locale;->ENGLISH:Ljava/util/Locale;

    new-array v2, v2, [Ljava/lang/Object;

    iget-object v6, p0, Lcom/android/fmradio/views/AllStationInfoAdapter;->mContext:Landroid/content/Context;

    .line 88
    invoke-virtual {v1}, Lcom/android/fmradio/info/FmStationInfo;->getFreq()I

    move-result v7

    invoke-static {v6, v7}, Lcom/android/fmradio/utils/FmUtils;->formatStation(Landroid/content/Context;I)Ljava/lang/String;

    move-result-object v6

    aput-object v6, v2, v0

    iget-object v0, p0, Lcom/android/fmradio/views/AllStationInfoAdapter;->mContext:Landroid/content/Context;

    .line 89
    invoke-virtual {v0}, Landroid/content/Context;->getResources()Landroid/content/res/Resources;

    move-result-object v0

    const v6, 0x7f0c0027

    invoke-virtual {v0, v6}, Landroid/content/res/Resources;->getText(I)Ljava/lang/CharSequence;

    move-result-object v0

    aput-object v0, v2, v4

    .line 87
    invoke-static {v5, v3, v2}, Ljava/lang/String;->format(Ljava/util/Locale;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v0

    invoke-virtual {p1, v0}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    goto :goto_1

    .line 91
    :cond_1
    iget-object p1, p2, Lcom/android/fmradio/views/AllStationInfoAdapter$ViewHolder;->mStationFreq:Landroid/widget/TextView;

    sget-object v5, Ljava/util/Locale;->ENGLISH:Ljava/util/Locale;

    new-array v2, v2, [Ljava/lang/Object;

    iget-object v6, p0, Lcom/android/fmradio/views/AllStationInfoAdapter;->mContext:Landroid/content/Context;

    .line 92
    invoke-virtual {v1}, Lcom/android/fmradio/info/FmStationInfo;->getFreq()I

    move-result v7

    invoke-static {v6, v7}, Lcom/android/fmradio/utils/FmUtils;->formatStation(Landroid/content/Context;I)Ljava/lang/String;

    move-result-object v6

    aput-object v6, v2, v0

    iget-object v0, p0, Lcom/android/fmradio/views/AllStationInfoAdapter;->mContext:Landroid/content/Context;

    .line 93
    invoke-virtual {v0}, Landroid/content/Context;->getResources()Landroid/content/res/Resources;

    move-result-object v0

    const v6, 0x7f0c0026

    invoke-virtual {v0, v6}, Landroid/content/res/Resources;->getText(I)Ljava/lang/CharSequence;

    move-result-object v0

    aput-object v0, v2, v4

    .line 91
    invoke-static {v5, v3, v2}, Ljava/lang/String;->format(Ljava/util/Locale;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v0

    invoke-virtual {p1, v0}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    .line 95
    :goto_1
    iget-object p1, p2, Lcom/android/fmradio/views/AllStationInfoAdapter$ViewHolder;->mStationName:Landroid/widget/TextView;

    invoke-virtual {v1}, Lcom/android/fmradio/info/FmStationInfo;->getStationName()Ljava/lang/String;

    move-result-object p2

    invoke-virtual {p1, p2}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    :cond_2
    return-object p3
.end method

.method public onClick(Landroid/view/View;)V
    .locals 2

    .line 103
    invoke-virtual {p1}, Landroid/view/View;->getId()I

    move-result v0

    const v1, 0x7f08004a

    if-ne v0, v1, :cond_0

    .line 105
    iget-object v0, p0, Lcom/android/fmradio/views/AllStationInfoAdapter;->mOnAllStationInfoAdapterClickListener:Lcom/android/fmradio/views/AllStationInfoAdapter$OnAllStationInfoAdapterClickListener;

    if-eqz v0, :cond_1

    .line 106
    invoke-virtual {p1}, Landroid/view/View;->getTag()Ljava/lang/Object;

    move-result-object p1

    check-cast p1, Ljava/lang/Integer;

    invoke-virtual {p1}, Ljava/lang/Integer;->intValue()I

    move-result p1

    invoke-interface {v0, p1}, Lcom/android/fmradio/views/AllStationInfoAdapter$OnAllStationInfoAdapterClickListener;->onEdit(I)V

    goto :goto_0

    :cond_0
    const v1, 0x7f080048

    if-ne v0, v1, :cond_1

    .line 109
    iget-object v0, p0, Lcom/android/fmradio/views/AllStationInfoAdapter;->mOnAllStationInfoAdapterClickListener:Lcom/android/fmradio/views/AllStationInfoAdapter$OnAllStationInfoAdapterClickListener;

    if-eqz v0, :cond_1

    .line 110
    invoke-virtual {p1}, Landroid/view/View;->getTag()Ljava/lang/Object;

    move-result-object p1

    check-cast p1, Ljava/lang/Integer;

    invoke-virtual {p1}, Ljava/lang/Integer;->intValue()I

    move-result p1

    invoke-interface {v0, p1}, Lcom/android/fmradio/views/AllStationInfoAdapter$OnAllStationInfoAdapterClickListener;->onDelete(I)V

    :cond_1
    :goto_0
    return-void
.end method

.method public setAllStationInfoAdapterClickListener(Lcom/android/fmradio/views/AllStationInfoAdapter$OnAllStationInfoAdapterClickListener;)V
    .locals 0

    .line 126
    iput-object p1, p0, Lcom/android/fmradio/views/AllStationInfoAdapter;->mOnAllStationInfoAdapterClickListener:Lcom/android/fmradio/views/AllStationInfoAdapter$OnAllStationInfoAdapterClickListener;

    return-void
.end method

.method public setAllStationInfoList(Ljava/util/ArrayList;)V
    .locals 0
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Ljava/util/ArrayList<",
            "Lcom/android/fmradio/info/FmStationInfo;",
            ">;)V"
        }
    .end annotation

    .line 38
    iput-object p1, p0, Lcom/android/fmradio/views/AllStationInfoAdapter;->mAllStationInfoList:Ljava/util/ArrayList;

    return-void
.end method
