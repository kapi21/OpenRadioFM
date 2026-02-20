.class public Lcom/android/fmradio/favorite/FmFavoriteListFragment;
.super Landroid/app/DialogFragment;
.source "FmFavoriteListFragment.java"

# interfaces
.implements Landroid/view/View$OnClickListener;
.implements Landroid/widget/AdapterView$OnItemClickListener;


# annotations
.annotation system Ldalvik/annotation/MemberClasses;
    value = {
        Lcom/android/fmradio/favorite/FmFavoriteListFragment$OnFmStationInfoListener;
    }
.end annotation


# static fields
.field private static final TAG:Ljava/lang/String;


# instance fields
.field private mAdapter:Lcom/android/fmradio/favorite/FmFavoriteListAdapter;

.field private mContext:Landroid/content/Context;

.field private mFmStationInfoListener:Lcom/android/fmradio/favorite/FmFavoriteListFragment$OnFmStationInfoListener;

.field private mGravity:I

.field private mLVFavoriteList:Landroid/widget/ListView;

.field private mScale:F

.field private mYOffset:I


# direct methods
.method static constructor <clinit>()V
    .locals 1

    .line 31
    const-class v0, Lcom/android/fmradio/favorite/FmFavoriteListFragment;

    invoke-virtual {v0}, Ljava/lang/Class;->getSimpleName()Ljava/lang/String;

    move-result-object v0

    sput-object v0, Lcom/android/fmradio/favorite/FmFavoriteListFragment;->TAG:Ljava/lang/String;

    return-void
.end method

.method public constructor <init>()V
    .locals 1

    .line 29
    invoke-direct {p0}, Landroid/app/DialogFragment;-><init>()V

    const/high16 v0, 0x3f800000    # 1.0f

    .line 39
    iput v0, p0, Lcom/android/fmradio/favorite/FmFavoriteListFragment;->mScale:F

    const/4 v0, 0x0

    .line 41
    iput v0, p0, Lcom/android/fmradio/favorite/FmFavoriteListFragment;->mYOffset:I

    const v0, 0x800055

    .line 43
    iput v0, p0, Lcom/android/fmradio/favorite/FmFavoriteListFragment;->mGravity:I

    return-void
.end method

.method private initData()V
    .locals 1

    .line 92
    iget-object v0, p0, Lcom/android/fmradio/favorite/FmFavoriteListFragment;->mContext:Landroid/content/Context;

    invoke-static {v0}, Lcom/android/fmradio/favorite/FmFavoriteManager;->getInstance(Landroid/content/Context;)Lcom/android/fmradio/favorite/FmFavoriteManager;

    move-result-object v0

    invoke-virtual {v0}, Lcom/android/fmradio/favorite/FmFavoriteManager;->onLoad()V

    return-void
.end method

.method private initView(Landroid/view/View;)V
    .locals 3

    const v0, 0x7f08008c

    .line 96
    invoke-virtual {p1, v0}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object v0

    check-cast v0, Landroid/widget/ListView;

    iput-object v0, p0, Lcom/android/fmradio/favorite/FmFavoriteListFragment;->mLVFavoriteList:Landroid/widget/ListView;

    .line 97
    iget-object v0, p0, Lcom/android/fmradio/favorite/FmFavoriteListFragment;->mLVFavoriteList:Landroid/widget/ListView;

    invoke-virtual {v0, p0}, Landroid/widget/ListView;->setOnItemClickListener(Landroid/widget/AdapterView$OnItemClickListener;)V

    .line 99
    new-instance v0, Lcom/android/fmradio/favorite/FmFavoriteListAdapter;

    iget-object v1, p0, Lcom/android/fmradio/favorite/FmFavoriteListFragment;->mContext:Landroid/content/Context;

    invoke-direct {v0, v1}, Lcom/android/fmradio/favorite/FmFavoriteListAdapter;-><init>(Landroid/content/Context;)V

    iput-object v0, p0, Lcom/android/fmradio/favorite/FmFavoriteListFragment;->mAdapter:Lcom/android/fmradio/favorite/FmFavoriteListAdapter;

    .line 100
    iget-object v0, p0, Lcom/android/fmradio/favorite/FmFavoriteListFragment;->mAdapter:Lcom/android/fmradio/favorite/FmFavoriteListAdapter;

    iget-object v1, p0, Lcom/android/fmradio/favorite/FmFavoriteListFragment;->mContext:Landroid/content/Context;

    invoke-static {v1}, Lcom/android/fmradio/favorite/FmFavoriteManager;->getInstance(Landroid/content/Context;)Lcom/android/fmradio/favorite/FmFavoriteManager;

    move-result-object v1

    iget-object v2, p0, Lcom/android/fmradio/favorite/FmFavoriteListFragment;->mContext:Landroid/content/Context;

    invoke-static {v2}, Lcom/android/fmradio/database/FmStation;->getCurrentBand(Landroid/content/Context;)I

    move-result v2

    invoke-static {v2}, Lcom/android/fmradio/utils/FmUtils;->isFMBand(I)Z

    move-result v2

    invoke-virtual {v1, v2}, Lcom/android/fmradio/favorite/FmFavoriteManager;->getFavoriteList(Z)Ljava/util/List;

    move-result-object v1

    invoke-virtual {v0, v1}, Lcom/android/fmradio/favorite/FmFavoriteListAdapter;->setFavoriteList(Ljava/util/List;)V

    .line 101
    iget-object v0, p0, Lcom/android/fmradio/favorite/FmFavoriteListFragment;->mLVFavoriteList:Landroid/widget/ListView;

    iget-object v1, p0, Lcom/android/fmradio/favorite/FmFavoriteListFragment;->mAdapter:Lcom/android/fmradio/favorite/FmFavoriteListAdapter;

    invoke-virtual {v0, v1}, Landroid/widget/ListView;->setAdapter(Landroid/widget/ListAdapter;)V

    const v0, 0x7f080046

    .line 103
    invoke-virtual {p1, v0}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object p1

    check-cast p1, Landroid/widget/ImageButton;

    .line 104
    invoke-virtual {p1, p0}, Landroid/widget/ImageButton;->setOnClickListener(Landroid/view/View$OnClickListener;)V

    return-void
.end method


# virtual methods
.method public onClick(Landroid/view/View;)V
    .locals 2

    .line 122
    invoke-virtual {p1}, Landroid/view/View;->getId()I

    move-result p1

    const v0, 0x7f080046

    if-ne p1, v0, :cond_0

    .line 124
    iget-object p1, p0, Lcom/android/fmradio/favorite/FmFavoriteListFragment;->mContext:Landroid/content/Context;

    invoke-static {p1}, Lcom/android/fmradio/favorite/FmFavoriteManager;->getInstance(Landroid/content/Context;)Lcom/android/fmradio/favorite/FmFavoriteManager;

    move-result-object p1

    invoke-virtual {p1}, Lcom/android/fmradio/favorite/FmFavoriteManager;->onClear()V

    .line 126
    iget-object p1, p0, Lcom/android/fmradio/favorite/FmFavoriteListFragment;->mAdapter:Lcom/android/fmradio/favorite/FmFavoriteListAdapter;

    iget-object v0, p0, Lcom/android/fmradio/favorite/FmFavoriteListFragment;->mContext:Landroid/content/Context;

    invoke-static {v0}, Lcom/android/fmradio/favorite/FmFavoriteManager;->getInstance(Landroid/content/Context;)Lcom/android/fmradio/favorite/FmFavoriteManager;

    move-result-object v0

    iget-object v1, p0, Lcom/android/fmradio/favorite/FmFavoriteListFragment;->mContext:Landroid/content/Context;

    invoke-static {v1}, Lcom/android/fmradio/database/FmStation;->getCurrentBand(Landroid/content/Context;)I

    move-result v1

    invoke-static {v1}, Lcom/android/fmradio/utils/FmUtils;->isFMBand(I)Z

    move-result v1

    invoke-virtual {v0, v1}, Lcom/android/fmradio/favorite/FmFavoriteManager;->getFavoriteList(Z)Ljava/util/List;

    move-result-object v0

    invoke-virtual {p1, v0}, Lcom/android/fmradio/favorite/FmFavoriteListAdapter;->setFavoriteList(Ljava/util/List;)V

    .line 127
    iget-object p1, p0, Lcom/android/fmradio/favorite/FmFavoriteListFragment;->mAdapter:Lcom/android/fmradio/favorite/FmFavoriteListAdapter;

    invoke-virtual {p1}, Lcom/android/fmradio/favorite/FmFavoriteListAdapter;->notifyDataSetChanged()V

    .line 129
    new-instance p1, Landroid/content/Intent;

    const-string v0, "com.android.fmradio.favorite_changed"

    invoke-direct {p1, v0}, Landroid/content/Intent;-><init>(Ljava/lang/String;)V

    const/4 v0, 0x0

    const-string v1, "favorite_changed_key"

    .line 130
    invoke-virtual {p1, v1, v0}, Landroid/content/Intent;->putExtra(Ljava/lang/String;Z)Landroid/content/Intent;

    .line 131
    iget-object v0, p0, Lcom/android/fmradio/favorite/FmFavoriteListFragment;->mContext:Landroid/content/Context;

    invoke-static {v0}, Landroidx/localbroadcastmanager/content/LocalBroadcastManager;->getInstance(Landroid/content/Context;)Landroidx/localbroadcastmanager/content/LocalBroadcastManager;

    move-result-object v0

    invoke-virtual {v0, p1}, Landroidx/localbroadcastmanager/content/LocalBroadcastManager;->sendBroadcast(Landroid/content/Intent;)Z

    :cond_0
    return-void
.end method

.method public onCreate(Landroid/os/Bundle;)V
    .locals 2

    .line 47
    sget-object v0, Lcom/android/fmradio/favorite/FmFavoriteListFragment;->TAG:Ljava/lang/String;

    const-string v1, "start"

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    const/4 v0, 0x1

    const/4 v1, 0x0

    .line 48
    invoke-virtual {p0, v0, v1}, Lcom/android/fmradio/favorite/FmFavoriteListFragment;->setStyle(II)V

    .line 49
    invoke-super {p0, p1}, Landroid/app/DialogFragment;->onCreate(Landroid/os/Bundle;)V

    .line 51
    invoke-virtual {p0}, Lcom/android/fmradio/favorite/FmFavoriteListFragment;->getActivity()Landroid/app/Activity;

    move-result-object p1

    iput-object p1, p0, Lcom/android/fmradio/favorite/FmFavoriteListFragment;->mContext:Landroid/content/Context;

    .line 53
    invoke-direct {p0}, Lcom/android/fmradio/favorite/FmFavoriteListFragment;->initData()V

    return-void
.end method

.method public onCreateView(Landroid/view/LayoutInflater;Landroid/view/ViewGroup;Landroid/os/Bundle;)Landroid/view/View;
    .locals 1

    .line 84
    sget-object p3, Lcom/android/fmradio/favorite/FmFavoriteListFragment;->TAG:Ljava/lang/String;

    const-string v0, "start"

    invoke-static {p3, v0}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    const p3, 0x7f0b0021

    .line 86
    invoke-virtual {p1, p3, p2}, Landroid/view/LayoutInflater;->inflate(ILandroid/view/ViewGroup;)Landroid/view/View;

    move-result-object p1

    .line 87
    invoke-direct {p0, p1}, Lcom/android/fmradio/favorite/FmFavoriteListFragment;->initView(Landroid/view/View;)V

    return-object p1
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

    .line 137
    iget-object p1, p0, Lcom/android/fmradio/favorite/FmFavoriteListFragment;->mAdapter:Lcom/android/fmradio/favorite/FmFavoriteListAdapter;

    invoke-virtual {p1, p3}, Lcom/android/fmradio/favorite/FmFavoriteListAdapter;->getItem(I)Ljava/lang/Object;

    move-result-object p1

    check-cast p1, Lcom/android/fmradio/info/FmFreqInfo;

    if-eqz p1, :cond_0

    .line 139
    sget-object p2, Lcom/android/fmradio/favorite/FmFavoriteListFragment;->TAG:Ljava/lang/String;

    new-instance p3, Ljava/lang/StringBuilder;

    invoke-direct {p3}, Ljava/lang/StringBuilder;-><init>()V

    const-string p4, "freq: "

    invoke-virtual {p3, p4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {p1}, Lcom/android/fmradio/info/FmFreqInfo;->getFreq()I

    move-result p4

    invoke-virtual {p3, p4}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {p3}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p3

    invoke-static {p2, p3}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 140
    iget-object p2, p0, Lcom/android/fmradio/favorite/FmFavoriteListFragment;->mFmStationInfoListener:Lcom/android/fmradio/favorite/FmFavoriteListFragment$OnFmStationInfoListener;

    if-eqz p2, :cond_0

    .line 141
    invoke-virtual {p1}, Lcom/android/fmradio/info/FmFreqInfo;->getFreq()I

    move-result p1

    invoke-interface {p2, p1}, Lcom/android/fmradio/favorite/FmFavoriteListFragment$OnFmStationInfoListener;->onItemClickListener(I)V

    :cond_0
    return-void
.end method

.method public onStart()V
    .locals 4

    .line 58
    sget-object v0, Lcom/android/fmradio/favorite/FmFavoriteListFragment;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "start - mScale: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget v2, p0, Lcom/android/fmradio/favorite/FmFavoriteListFragment;->mScale:F

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(F)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 59
    invoke-virtual {p0}, Lcom/android/fmradio/favorite/FmFavoriteListFragment;->getDialog()Landroid/app/Dialog;

    move-result-object v0

    invoke-virtual {v0}, Landroid/app/Dialog;->getWindow()Landroid/view/Window;

    move-result-object v0

    .line 60
    iget-object v1, p0, Lcom/android/fmradio/favorite/FmFavoriteListFragment;->mContext:Landroid/content/Context;

    invoke-virtual {v1}, Landroid/content/Context;->getResources()Landroid/content/res/Resources;

    move-result-object v1

    const v2, 0x7f060bf1

    invoke-virtual {v1, v2}, Landroid/content/res/Resources;->getDimensionPixelSize(I)I

    move-result v1

    .line 61
    iget-object v2, p0, Lcom/android/fmradio/favorite/FmFavoriteListFragment;->mContext:Landroid/content/Context;

    invoke-virtual {v2}, Landroid/content/Context;->getResources()Landroid/content/res/Resources;

    move-result-object v2

    const v3, 0x7f060bef

    invoke-virtual {v2, v3}, Landroid/content/res/Resources;->getDimensionPixelSize(I)I

    move-result v2

    int-to-float v2, v2

    iget v3, p0, Lcom/android/fmradio/favorite/FmFavoriteListFragment;->mScale:F

    mul-float/2addr v2, v3

    float-to-int v2, v2

    .line 62
    invoke-virtual {v0, v1, v2}, Landroid/view/Window;->setLayout(II)V

    .line 64
    invoke-super {p0}, Landroid/app/DialogFragment;->onStart()V

    .line 66
    invoke-virtual {p0}, Lcom/android/fmradio/favorite/FmFavoriteListFragment;->getDialog()Landroid/app/Dialog;

    move-result-object v0

    if-eqz v0, :cond_0

    .line 68
    invoke-virtual {v0}, Landroid/app/Dialog;->getWindow()Landroid/view/Window;

    move-result-object v1

    invoke-virtual {v1}, Landroid/view/Window;->getAttributes()Landroid/view/WindowManager$LayoutParams;

    move-result-object v1

    .line 69
    iget v2, p0, Lcom/android/fmradio/favorite/FmFavoriteListFragment;->mGravity:I

    iput v2, v1, Landroid/view/WindowManager$LayoutParams;->gravity:I

    .line 70
    iget v2, p0, Lcom/android/fmradio/favorite/FmFavoriteListFragment;->mYOffset:I

    iput v2, v1, Landroid/view/WindowManager$LayoutParams;->y:I

    .line 78
    invoke-virtual {v0}, Landroid/app/Dialog;->getWindow()Landroid/view/Window;

    move-result-object v0

    invoke-virtual {v0, v1}, Landroid/view/Window;->setAttributes(Landroid/view/WindowManager$LayoutParams;)V

    :cond_0
    return-void
.end method

.method public setFmStationInfoListener(Lcom/android/fmradio/favorite/FmFavoriteListFragment$OnFmStationInfoListener;)V
    .locals 0

    .line 149
    iput-object p1, p0, Lcom/android/fmradio/favorite/FmFavoriteListFragment;->mFmStationInfoListener:Lcom/android/fmradio/favorite/FmFavoriteListFragment$OnFmStationInfoListener;

    return-void
.end method

.method public setGravity(I)V
    .locals 0

    .line 117
    iput p1, p0, Lcom/android/fmradio/favorite/FmFavoriteListFragment;->mGravity:I

    return-void
.end method

.method public setScale(F)V
    .locals 3

    .line 108
    sget-object v0, Lcom/android/fmradio/favorite/FmFavoriteListFragment;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "mScale: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget v2, p0, Lcom/android/fmradio/favorite/FmFavoriteListFragment;->mScale:F

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(F)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 109
    iput p1, p0, Lcom/android/fmradio/favorite/FmFavoriteListFragment;->mScale:F

    return-void
.end method

.method public setYOffset(I)V
    .locals 0

    .line 113
    iput p1, p0, Lcom/android/fmradio/favorite/FmFavoriteListFragment;->mYOffset:I

    return-void
.end method
