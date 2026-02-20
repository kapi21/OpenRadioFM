.class public Lcom/android/fmradio/favorite/FmFavoriteListAdapter;
.super Landroid/widget/BaseAdapter;
.source "FmFavoriteListAdapter.java"

# interfaces
.implements Landroid/view/View$OnClickListener;


# annotations
.annotation system Ldalvik/annotation/MemberClasses;
    value = {
        Lcom/android/fmradio/favorite/FmFavoriteListAdapter$ViewHolder;
    }
.end annotation


# static fields
.field private static final TAG:Ljava/lang/String;


# instance fields
.field private mContext:Landroid/content/Context;

.field private mFavoriteList:Ljava/util/List;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Ljava/util/List<",
            "Lcom/android/fmradio/info/FmFreqInfo;",
            ">;"
        }
    .end annotation
.end field

.field private final mInflater:Landroid/view/LayoutInflater;


# direct methods
.method static constructor <clinit>()V
    .locals 1

    .line 30
    const-class v0, Lcom/android/fmradio/favorite/FmFavoriteListAdapter;

    invoke-virtual {v0}, Ljava/lang/Class;->getSimpleName()Ljava/lang/String;

    move-result-object v0

    sput-object v0, Lcom/android/fmradio/favorite/FmFavoriteListAdapter;->TAG:Ljava/lang/String;

    return-void
.end method

.method public constructor <init>(Landroid/content/Context;)V
    .locals 1

    .line 38
    invoke-direct {p0}, Landroid/widget/BaseAdapter;-><init>()V

    .line 36
    new-instance v0, Ljava/util/ArrayList;

    invoke-direct {v0}, Ljava/util/ArrayList;-><init>()V

    iput-object v0, p0, Lcom/android/fmradio/favorite/FmFavoriteListAdapter;->mFavoriteList:Ljava/util/List;

    .line 39
    iput-object p1, p0, Lcom/android/fmradio/favorite/FmFavoriteListAdapter;->mContext:Landroid/content/Context;

    .line 41
    invoke-static {p1}, Landroid/view/LayoutInflater;->from(Landroid/content/Context;)Landroid/view/LayoutInflater;

    move-result-object p1

    iput-object p1, p0, Lcom/android/fmradio/favorite/FmFavoriteListAdapter;->mInflater:Landroid/view/LayoutInflater;

    return-void
.end method


# virtual methods
.method public getCount()I
    .locals 1

    .line 50
    iget-object v0, p0, Lcom/android/fmradio/favorite/FmFavoriteListAdapter;->mFavoriteList:Ljava/util/List;

    if-eqz v0, :cond_0

    .line 51
    invoke-interface {v0}, Ljava/util/List;->size()I

    move-result v0

    return v0

    :cond_0
    const/4 v0, 0x0

    return v0
.end method

.method public getItem(I)Ljava/lang/Object;
    .locals 1

    .line 58
    iget-object v0, p0, Lcom/android/fmradio/favorite/FmFavoriteListAdapter;->mFavoriteList:Ljava/util/List;

    if-eqz v0, :cond_0

    .line 59
    invoke-interface {v0, p1}, Ljava/util/List;->get(I)Ljava/lang/Object;

    move-result-object p1

    return-object p1

    :cond_0
    const/4 p1, 0x0

    return-object p1
.end method

.method public getItemId(I)J
    .locals 2

    int-to-long v0, p1

    return-wide v0
.end method

.method public getView(ILandroid/view/View;Landroid/view/ViewGroup;)Landroid/view/View;
    .locals 7

    const/4 v0, 0x0

    if-nez p2, :cond_0

    .line 73
    new-instance p2, Lcom/android/fmradio/favorite/FmFavoriteListAdapter$ViewHolder;

    const/4 v1, 0x0

    invoke-direct {p2, v1}, Lcom/android/fmradio/favorite/FmFavoriteListAdapter$ViewHolder;-><init>(Lcom/android/fmradio/favorite/FmFavoriteListAdapter$1;)V

    .line 75
    iget-object v1, p0, Lcom/android/fmradio/favorite/FmFavoriteListAdapter;->mInflater:Landroid/view/LayoutInflater;

    const v2, 0x7f0b001e

    invoke-virtual {v1, v2, p3, v0}, Landroid/view/LayoutInflater;->inflate(ILandroid/view/ViewGroup;Z)Landroid/view/View;

    move-result-object p3

    .line 76
    new-instance v1, Landroid/widget/LinearLayout$LayoutParams;

    const/4 v2, -0x1

    iget-object v3, p0, Lcom/android/fmradio/favorite/FmFavoriteListAdapter;->mContext:Landroid/content/Context;

    .line 77
    invoke-virtual {v3}, Landroid/content/Context;->getResources()Landroid/content/res/Resources;

    move-result-object v3

    const v4, 0x7f060bf0

    invoke-virtual {v3, v4}, Landroid/content/res/Resources;->getDimensionPixelSize(I)I

    move-result v3

    invoke-direct {v1, v2, v3}, Landroid/widget/LinearLayout$LayoutParams;-><init>(II)V

    .line 76
    invoke-virtual {p3, v1}, Landroid/view/View;->setLayoutParams(Landroid/view/ViewGroup$LayoutParams;)V

    const v1, 0x7f0800dc

    .line 79
    invoke-virtual {p3, v1}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object v1

    check-cast v1, Landroid/widget/TextView;

    iput-object v1, p2, Lcom/android/fmradio/favorite/FmFavoriteListAdapter$ViewHolder;->mStationBand:Landroid/widget/TextView;

    const v1, 0x7f0800dd

    .line 80
    invoke-virtual {p3, v1}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object v1

    check-cast v1, Landroid/widget/TextView;

    iput-object v1, p2, Lcom/android/fmradio/favorite/FmFavoriteListAdapter$ViewHolder;->mStationFreq:Landroid/widget/TextView;

    const v1, 0x7f08004c

    .line 81
    invoke-virtual {p3, v1}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object v1

    check-cast v1, Landroid/widget/ImageButton;

    iput-object v1, p2, Lcom/android/fmradio/favorite/FmFavoriteListAdapter$ViewHolder;->mBtnFavorite:Landroid/widget/ImageButton;

    .line 83
    iget-object v1, p2, Lcom/android/fmradio/favorite/FmFavoriteListAdapter$ViewHolder;->mBtnFavorite:Landroid/widget/ImageButton;

    invoke-virtual {v1, p0}, Landroid/widget/ImageButton;->setOnClickListener(Landroid/view/View$OnClickListener;)V

    .line 85
    invoke-virtual {p3, p2}, Landroid/view/View;->setTag(Ljava/lang/Object;)V

    goto :goto_0

    .line 87
    :cond_0
    invoke-virtual {p2}, Landroid/view/View;->getTag()Ljava/lang/Object;

    move-result-object p3

    check-cast p3, Lcom/android/fmradio/favorite/FmFavoriteListAdapter$ViewHolder;

    move-object v6, p3

    move-object p3, p2

    move-object p2, v6

    .line 90
    :goto_0
    iget-object v1, p0, Lcom/android/fmradio/favorite/FmFavoriteListAdapter;->mFavoriteList:Ljava/util/List;

    invoke-interface {v1, p1}, Ljava/util/List;->get(I)Ljava/lang/Object;

    move-result-object p1

    check-cast p1, Lcom/android/fmradio/info/FmFreqInfo;

    .line 91
    invoke-virtual {p1}, Lcom/android/fmradio/info/FmFreqInfo;->getBand()I

    move-result v1

    .line 92
    invoke-static {v1}, Lcom/android/fmradio/utils/FmUtils;->isFMBand(I)Z

    move-result v2

    const/4 v3, 0x1

    if-eqz v2, :cond_1

    .line 93
    iget-object v2, p2, Lcom/android/fmradio/favorite/FmFavoriteListAdapter$ViewHolder;->mStationBand:Landroid/widget/TextView;

    sget-object v4, Ljava/util/Locale;->ENGLISH:Ljava/util/Locale;

    new-array v5, v3, [Ljava/lang/Object;

    add-int/2addr v1, v3

    invoke-static {v1}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v1

    aput-object v1, v5, v0

    const-string v0, "FM %d"

    invoke-static {v4, v0, v5}, Ljava/lang/String;->format(Ljava/util/Locale;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v0

    invoke-virtual {v2, v0}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    goto :goto_1

    .line 95
    :cond_1
    iget-object v2, p2, Lcom/android/fmradio/favorite/FmFavoriteListAdapter$ViewHolder;->mStationBand:Landroid/widget/TextView;

    sget-object v4, Ljava/util/Locale;->ENGLISH:Ljava/util/Locale;

    new-array v5, v3, [Ljava/lang/Object;

    rem-int/lit8 v1, v1, 0x3

    add-int/2addr v1, v3

    invoke-static {v1}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v1

    aput-object v1, v5, v0

    const-string v0, "AM %d"

    invoke-static {v4, v0, v5}, Ljava/lang/String;->format(Ljava/util/Locale;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v0

    invoke-virtual {v2, v0}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    .line 98
    :goto_1
    iget-object v0, p2, Lcom/android/fmradio/favorite/FmFavoriteListAdapter$ViewHolder;->mStationFreq:Landroid/widget/TextView;

    iget-object v1, p0, Lcom/android/fmradio/favorite/FmFavoriteListAdapter;->mContext:Landroid/content/Context;

    invoke-virtual {p1}, Lcom/android/fmradio/info/FmFreqInfo;->getFreq()I

    move-result v2

    invoke-static {v1, v2}, Lcom/android/fmradio/utils/FmUtils;->formatStation(Landroid/content/Context;I)Ljava/lang/String;

    move-result-object v1

    invoke-static {v1}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v1

    invoke-virtual {v0, v1}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    .line 100
    iget-object v0, p2, Lcom/android/fmradio/favorite/FmFavoriteListAdapter$ViewHolder;->mBtnFavorite:Landroid/widget/ImageButton;

    invoke-virtual {v0, v3}, Landroid/widget/ImageButton;->setSelected(Z)V

    .line 101
    iget-object p2, p2, Lcom/android/fmradio/favorite/FmFavoriteListAdapter$ViewHolder;->mBtnFavorite:Landroid/widget/ImageButton;

    invoke-virtual {p1}, Lcom/android/fmradio/info/FmFreqInfo;->getFreq()I

    move-result p1

    invoke-static {p1}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object p1

    invoke-virtual {p2, p1}, Landroid/widget/ImageButton;->setTag(Ljava/lang/Object;)V

    return-object p3
.end method

.method public onClick(Landroid/view/View;)V
    .locals 3

    .line 108
    invoke-virtual {p1}, Landroid/view/View;->getId()I

    move-result v0

    const v1, 0x7f08004c

    if-ne v0, v1, :cond_0

    .line 110
    invoke-virtual {p1}, Landroid/view/View;->getTag()Ljava/lang/Object;

    move-result-object p1

    check-cast p1, Ljava/lang/Integer;

    invoke-virtual {p1}, Ljava/lang/Integer;->intValue()I

    move-result p1

    .line 111
    sget-object v0, Lcom/android/fmradio/favorite/FmFavoriteListAdapter;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "freq: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 113
    iget-object v0, p0, Lcom/android/fmradio/favorite/FmFavoriteListAdapter;->mContext:Landroid/content/Context;

    invoke-static {v0}, Lcom/android/fmradio/favorite/FmFavoriteManager;->getInstance(Landroid/content/Context;)Lcom/android/fmradio/favorite/FmFavoriteManager;

    move-result-object v0

    invoke-virtual {v0, p1}, Lcom/android/fmradio/favorite/FmFavoriteManager;->onRemove(I)V

    .line 115
    iget-object p1, p0, Lcom/android/fmradio/favorite/FmFavoriteListAdapter;->mContext:Landroid/content/Context;

    invoke-static {p1}, Lcom/android/fmradio/favorite/FmFavoriteManager;->getInstance(Landroid/content/Context;)Lcom/android/fmradio/favorite/FmFavoriteManager;

    move-result-object p1

    iget-object v0, p0, Lcom/android/fmradio/favorite/FmFavoriteListAdapter;->mContext:Landroid/content/Context;

    invoke-static {v0}, Lcom/android/fmradio/database/FmStation;->getCurrentBand(Landroid/content/Context;)I

    move-result v0

    invoke-static {v0}, Lcom/android/fmradio/utils/FmUtils;->isFMBand(I)Z

    move-result v0

    invoke-virtual {p1, v0}, Lcom/android/fmradio/favorite/FmFavoriteManager;->getFavoriteList(Z)Ljava/util/List;

    move-result-object p1

    invoke-virtual {p0, p1}, Lcom/android/fmradio/favorite/FmFavoriteListAdapter;->setFavoriteList(Ljava/util/List;)V

    .line 117
    invoke-virtual {p0}, Lcom/android/fmradio/favorite/FmFavoriteListAdapter;->notifyDataSetChanged()V

    .line 119
    new-instance p1, Landroid/content/Intent;

    const-string v0, "com.android.fmradio.favorite_changed"

    invoke-direct {p1, v0}, Landroid/content/Intent;-><init>(Ljava/lang/String;)V

    const/4 v0, 0x0

    const-string v1, "favorite_changed_key"

    .line 120
    invoke-virtual {p1, v1, v0}, Landroid/content/Intent;->putExtra(Ljava/lang/String;Z)Landroid/content/Intent;

    .line 121
    iget-object v0, p0, Lcom/android/fmradio/favorite/FmFavoriteListAdapter;->mContext:Landroid/content/Context;

    invoke-static {v0}, Landroidx/localbroadcastmanager/content/LocalBroadcastManager;->getInstance(Landroid/content/Context;)Landroidx/localbroadcastmanager/content/LocalBroadcastManager;

    move-result-object v0

    invoke-virtual {v0, p1}, Landroidx/localbroadcastmanager/content/LocalBroadcastManager;->sendBroadcast(Landroid/content/Intent;)Z

    :cond_0
    return-void
.end method

.method public setFavoriteList(Ljava/util/List;)V
    .locals 0
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Ljava/util/List<",
            "Lcom/android/fmradio/info/FmFreqInfo;",
            ">;)V"
        }
    .end annotation

    .line 45
    iput-object p1, p0, Lcom/android/fmradio/favorite/FmFavoriteListAdapter;->mFavoriteList:Ljava/util/List;

    return-void
.end method
