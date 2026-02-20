.class public Lcom/android/fmradio/favorite/FmFavoriteManager;
.super Ljava/lang/Object;
.source "FmFavoriteManager.java"


# static fields
.field private static final TAG:Ljava/lang/String;

.field private static instance:Lcom/android/fmradio/favorite/FmFavoriteManager;


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


# direct methods
.method static constructor <clinit>()V
    .locals 1

    .line 16
    const-class v0, Lcom/android/fmradio/favorite/FmFavoriteManager;

    invoke-virtual {v0}, Ljava/lang/Class;->getSimpleName()Ljava/lang/String;

    move-result-object v0

    sput-object v0, Lcom/android/fmradio/favorite/FmFavoriteManager;->TAG:Ljava/lang/String;

    return-void
.end method

.method public constructor <init>(Landroid/content/Context;)V
    .locals 1

    .line 24
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 22
    new-instance v0, Ljava/util/ArrayList;

    invoke-direct {v0}, Ljava/util/ArrayList;-><init>()V

    iput-object v0, p0, Lcom/android/fmradio/favorite/FmFavoriteManager;->mFavoriteList:Ljava/util/List;

    .line 25
    iput-object p1, p0, Lcom/android/fmradio/favorite/FmFavoriteManager;->mContext:Landroid/content/Context;

    .line 27
    invoke-virtual {p0}, Lcom/android/fmradio/favorite/FmFavoriteManager;->onLoad()V

    return-void
.end method

.method public static getInstance(Landroid/content/Context;)Lcom/android/fmradio/favorite/FmFavoriteManager;
    .locals 1

    .line 31
    sget-object v0, Lcom/android/fmradio/favorite/FmFavoriteManager;->instance:Lcom/android/fmradio/favorite/FmFavoriteManager;

    if-nez v0, :cond_0

    .line 32
    new-instance v0, Lcom/android/fmradio/favorite/FmFavoriteManager;

    invoke-direct {v0, p0}, Lcom/android/fmradio/favorite/FmFavoriteManager;-><init>(Landroid/content/Context;)V

    sput-object v0, Lcom/android/fmradio/favorite/FmFavoriteManager;->instance:Lcom/android/fmradio/favorite/FmFavoriteManager;

    .line 35
    :cond_0
    sget-object p0, Lcom/android/fmradio/favorite/FmFavoriteManager;->instance:Lcom/android/fmradio/favorite/FmFavoriteManager;

    return-object p0
.end method

.method private getSharedPreferences()Landroid/content/SharedPreferences;
    .locals 3

    .line 135
    iget-object v0, p0, Lcom/android/fmradio/favorite/FmFavoriteManager;->mContext:Landroid/content/Context;

    const-string v1, "fm_favorite"

    const/4 v2, 0x0

    invoke-virtual {v0, v1, v2}, Landroid/content/Context;->getSharedPreferences(Ljava/lang/String;I)Landroid/content/SharedPreferences;

    move-result-object v0

    return-object v0
.end method

.method private onSave()V
    .locals 5

    .line 120
    new-instance v0, Ljava/lang/StringBuilder;

    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    .line 121
    iget-object v1, p0, Lcom/android/fmradio/favorite/FmFavoriteManager;->mFavoriteList:Ljava/util/List;

    invoke-interface {v1}, Ljava/util/List;->iterator()Ljava/util/Iterator;

    move-result-object v1

    :goto_0
    invoke-interface {v1}, Ljava/util/Iterator;->hasNext()Z

    move-result v2

    if-eqz v2, :cond_0

    invoke-interface {v1}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v2

    check-cast v2, Lcom/android/fmradio/info/FmFreqInfo;

    .line 122
    invoke-virtual {v2}, Lcom/android/fmradio/info/FmFreqInfo;->getBand()I

    move-result v3

    invoke-virtual {v0, v3}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v3, ":"

    invoke-virtual {v0, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 123
    invoke-virtual {v2}, Lcom/android/fmradio/info/FmFreqInfo;->getFreq()I

    move-result v4

    invoke-virtual {v0, v4}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v0, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 124
    invoke-virtual {v2}, Lcom/android/fmradio/info/FmFreqInfo;->isFavorite()Z

    move-result v2

    invoke-virtual {v0, v2}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    const-string v2, "-"

    invoke-virtual {v0, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    goto :goto_0

    .line 126
    :cond_0
    sget-object v1, Lcom/android/fmradio/favorite/FmFavoriteManager;->TAG:Ljava/lang/String;

    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2}, Ljava/lang/StringBuilder;-><init>()V

    const-string v3, "str: "

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v3

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v2

    invoke-static {v1, v2}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 128
    invoke-direct {p0}, Lcom/android/fmradio/favorite/FmFavoriteManager;->getSharedPreferences()Landroid/content/SharedPreferences;

    move-result-object v1

    .line 129
    invoke-interface {v1}, Landroid/content/SharedPreferences;->edit()Landroid/content/SharedPreferences$Editor;

    move-result-object v1

    .line 130
    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    const-string v2, "fm_favorite_key"

    invoke-interface {v1, v2, v0}, Landroid/content/SharedPreferences$Editor;->putString(Ljava/lang/String;Ljava/lang/String;)Landroid/content/SharedPreferences$Editor;

    .line 131
    invoke-interface {v1}, Landroid/content/SharedPreferences$Editor;->apply()V

    return-void
.end method


# virtual methods
.method public getFavoriteList(Z)Ljava/util/List;
    .locals 5
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(Z)",
            "Ljava/util/List<",
            "Lcom/android/fmradio/info/FmFreqInfo;",
            ">;"
        }
    .end annotation

    .line 39
    new-instance v0, Ljava/util/ArrayList;

    invoke-direct {v0}, Ljava/util/ArrayList;-><init>()V

    .line 40
    iget-object v1, p0, Lcom/android/fmradio/favorite/FmFavoriteManager;->mFavoriteList:Ljava/util/List;

    invoke-interface {v1}, Ljava/util/List;->iterator()Ljava/util/Iterator;

    move-result-object v1

    :cond_0
    :goto_0
    invoke-interface {v1}, Ljava/util/Iterator;->hasNext()Z

    move-result v2

    if-eqz v2, :cond_2

    invoke-interface {v1}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v2

    check-cast v2, Lcom/android/fmradio/info/FmFreqInfo;

    .line 41
    invoke-virtual {v2}, Lcom/android/fmradio/info/FmFreqInfo;->getBand()I

    move-result v3

    if-eqz p1, :cond_1

    .line 42
    invoke-static {v3}, Lcom/android/fmradio/utils/FmUtils;->isFMBand(I)Z

    move-result v4

    if-eqz v4, :cond_1

    .line 43
    invoke-interface {v0, v2}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    goto :goto_0

    :cond_1
    if-nez p1, :cond_0

    .line 44
    invoke-static {v3}, Lcom/android/fmradio/utils/FmUtils;->isFMBand(I)Z

    move-result v3

    if-nez v3, :cond_0

    .line 45
    invoke-interface {v0, v2}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    goto :goto_0

    :cond_2
    return-object v0
.end method

.method public isFavorite(I)Z
    .locals 4

    .line 52
    iget-object v0, p0, Lcom/android/fmradio/favorite/FmFavoriteManager;->mFavoriteList:Ljava/util/List;

    invoke-interface {v0}, Ljava/util/List;->iterator()Ljava/util/Iterator;

    move-result-object v0

    :cond_0
    invoke-interface {v0}, Ljava/util/Iterator;->hasNext()Z

    move-result v1

    const-string v2, "freq: "

    if-eqz v1, :cond_1

    invoke-interface {v0}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v1

    check-cast v1, Lcom/android/fmradio/info/FmFreqInfo;

    .line 53
    invoke-virtual {v1}, Lcom/android/fmradio/info/FmFreqInfo;->getFreq()I

    move-result v3

    if-ne p1, v3, :cond_0

    .line 54
    sget-object v0, Lcom/android/fmradio/favorite/FmFavoriteManager;->TAG:Ljava/lang/String;

    new-instance v3, Ljava/lang/StringBuilder;

    invoke-direct {v3}, Ljava/lang/StringBuilder;-><init>()V

    invoke-virtual {v3, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v3, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string p1, " is favorite station"

    invoke-virtual {v3, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v3}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p1

    invoke-static {v0, p1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 55
    invoke-virtual {v1}, Lcom/android/fmradio/info/FmFreqInfo;->isFavorite()Z

    move-result p1

    return p1

    .line 58
    :cond_1
    sget-object v0, Lcom/android/fmradio/favorite/FmFavoriteManager;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string p1, " is not favorite station"

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p1

    invoke-static {v0, p1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    const/4 p1, 0x0

    return p1
.end method

.method public onAdd(Lcom/android/fmradio/info/FmFreqInfo;)V
    .locals 4

    .line 63
    invoke-virtual {p1}, Lcom/android/fmradio/info/FmFreqInfo;->getFreq()I

    move-result v0

    .line 64
    invoke-virtual {p1}, Lcom/android/fmradio/info/FmFreqInfo;->getBand()I

    move-result p1

    .line 65
    sget-object v1, Lcom/android/fmradio/favorite/FmFavoriteManager;->TAG:Ljava/lang/String;

    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2}, Ljava/lang/StringBuilder;-><init>()V

    const-string v3, "freq: "

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2, v0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v3, " - band: "

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v2

    invoke-static {v1, v2}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 67
    new-instance v1, Lcom/android/fmradio/info/FmFreqInfo;

    invoke-direct {v1}, Lcom/android/fmradio/info/FmFreqInfo;-><init>()V

    .line 68
    invoke-virtual {v1, v0}, Lcom/android/fmradio/info/FmFreqInfo;->setFreq(I)V

    .line 69
    invoke-virtual {v1, p1}, Lcom/android/fmradio/info/FmFreqInfo;->setBand(I)V

    const/4 p1, 0x1

    .line 70
    invoke-virtual {v1, p1}, Lcom/android/fmradio/info/FmFreqInfo;->setFavorite(Z)V

    .line 71
    iget-object p1, p0, Lcom/android/fmradio/favorite/FmFavoriteManager;->mFavoriteList:Ljava/util/List;

    invoke-interface {p1, v1}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    .line 73
    invoke-direct {p0}, Lcom/android/fmradio/favorite/FmFavoriteManager;->onSave()V

    return-void
.end method

.method public onClear()V
    .locals 1

    .line 90
    iget-object v0, p0, Lcom/android/fmradio/favorite/FmFavoriteManager;->mFavoriteList:Ljava/util/List;

    invoke-interface {v0}, Ljava/util/List;->clear()V

    .line 92
    invoke-direct {p0}, Lcom/android/fmradio/favorite/FmFavoriteManager;->onSave()V

    return-void
.end method

.method public onLoad()V
    .locals 8

    .line 96
    iget-object v0, p0, Lcom/android/fmradio/favorite/FmFavoriteManager;->mFavoriteList:Ljava/util/List;

    invoke-interface {v0}, Ljava/util/List;->clear()V

    .line 98
    invoke-direct {p0}, Lcom/android/fmradio/favorite/FmFavoriteManager;->getSharedPreferences()Landroid/content/SharedPreferences;

    move-result-object v0

    const-string v1, "fm_favorite_key"

    const/4 v2, 0x0

    .line 99
    invoke-interface {v0, v1, v2}, Landroid/content/SharedPreferences;->getString(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;

    move-result-object v0

    if-eqz v0, :cond_1

    .line 101
    sget-object v1, Lcom/android/fmradio/favorite/FmFavoriteManager;->TAG:Ljava/lang/String;

    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2}, Ljava/lang/StringBuilder;-><init>()V

    const-string v3, "str: "

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v2

    invoke-static {v1, v2}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    const-string v1, "-"

    .line 102
    invoke-virtual {v0, v1}, Ljava/lang/String;->split(Ljava/lang/String;)[Ljava/lang/String;

    move-result-object v0

    .line 103
    array-length v1, v0

    const/4 v2, 0x0

    move v3, v2

    :goto_0
    if-ge v3, v1, :cond_1

    aget-object v4, v0, v3

    .line 104
    sget-object v5, Lcom/android/fmradio/favorite/FmFavoriteManager;->TAG:Ljava/lang/String;

    new-instance v6, Ljava/lang/StringBuilder;

    invoke-direct {v6}, Ljava/lang/StringBuilder;-><init>()V

    const-string v7, "value: "

    invoke-virtual {v6, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v6, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v6}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v6

    invoke-static {v5, v6}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 105
    invoke-static {v4}, Landroid/text/TextUtils;->isEmpty(Ljava/lang/CharSequence;)Z

    move-result v5

    if-nez v5, :cond_0

    const-string v5, ":"

    .line 106
    invoke-virtual {v4, v5}, Ljava/lang/String;->split(Ljava/lang/String;)[Ljava/lang/String;

    move-result-object v4

    .line 107
    new-instance v5, Lcom/android/fmradio/info/FmFreqInfo;

    invoke-direct {v5}, Lcom/android/fmradio/info/FmFreqInfo;-><init>()V

    .line 108
    aget-object v6, v4, v2

    invoke-static {v6}, Ljava/lang/Integer;->parseInt(Ljava/lang/String;)I

    move-result v6

    invoke-virtual {v5, v6}, Lcom/android/fmradio/info/FmFreqInfo;->setBand(I)V

    const/4 v6, 0x1

    .line 109
    aget-object v4, v4, v6

    invoke-static {v4}, Ljava/lang/Integer;->parseInt(Ljava/lang/String;)I

    move-result v4

    invoke-virtual {v5, v4}, Lcom/android/fmradio/info/FmFreqInfo;->setFreq(I)V

    .line 110
    invoke-virtual {v5, v6}, Lcom/android/fmradio/info/FmFreqInfo;->setFavorite(Z)V

    .line 111
    iget-object v4, p0, Lcom/android/fmradio/favorite/FmFavoriteManager;->mFavoriteList:Ljava/util/List;

    invoke-interface {v4, v5}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    :cond_0
    add-int/lit8 v3, v3, 0x1

    goto :goto_0

    .line 116
    :cond_1
    sget-object v0, Lcom/android/fmradio/favorite/FmFavoriteManager;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "mFavoriteList size: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-object v2, p0, Lcom/android/fmradio/favorite/FmFavoriteManager;->mFavoriteList:Ljava/util/List;

    invoke-interface {v2}, Ljava/util/List;->size()I

    move-result v2

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    return-void
.end method

.method public onRemove(I)V
    .locals 2

    const/4 v0, 0x0

    .line 77
    :goto_0
    iget-object v1, p0, Lcom/android/fmradio/favorite/FmFavoriteManager;->mFavoriteList:Ljava/util/List;

    invoke-interface {v1}, Ljava/util/List;->size()I

    move-result v1

    if-ge v0, v1, :cond_1

    .line 78
    iget-object v1, p0, Lcom/android/fmradio/favorite/FmFavoriteManager;->mFavoriteList:Ljava/util/List;

    invoke-interface {v1, v0}, Ljava/util/List;->get(I)Ljava/lang/Object;

    move-result-object v1

    check-cast v1, Lcom/android/fmradio/info/FmFreqInfo;

    .line 79
    invoke-virtual {v1}, Lcom/android/fmradio/info/FmFreqInfo;->getFreq()I

    move-result v1

    if-ne v1, p1, :cond_0

    .line 80
    iget-object p1, p0, Lcom/android/fmradio/favorite/FmFavoriteManager;->mFavoriteList:Ljava/util/List;

    invoke-interface {p1, v0}, Ljava/util/List;->remove(I)Ljava/lang/Object;

    .line 82
    invoke-direct {p0}, Lcom/android/fmradio/favorite/FmFavoriteManager;->onSave()V

    goto :goto_1

    :cond_0
    add-int/lit8 v0, v0, 0x1

    goto :goto_0

    :cond_1
    :goto_1
    return-void
.end method
