.class public Lcom/android/fmradio/views/PTYItemAdapter;
.super Landroid/widget/BaseAdapter;
.source "PTYItemAdapter.java"


# annotations
.annotation system Ldalvik/annotation/MemberClasses;
    value = {
        Lcom/android/fmradio/views/PTYItemAdapter$ViewHolder;
    }
.end annotation


# static fields
.field private static final TAG:Ljava/lang/String;


# instance fields
.field private mContext:Landroid/content/Context;

.field private mDatas:[Ljava/lang/String;

.field private mItemHeight:I

.field private mLayoutInflater:Landroid/view/LayoutInflater;

.field private mPtyType:I


# direct methods
.method static constructor <clinit>()V
    .locals 1

    .line 22
    const-class v0, Lcom/android/fmradio/views/PTYItemAdapter;

    invoke-virtual {v0}, Ljava/lang/Class;->getSimpleName()Ljava/lang/String;

    move-result-object v0

    sput-object v0, Lcom/android/fmradio/views/PTYItemAdapter;->TAG:Ljava/lang/String;

    return-void
.end method

.method public constructor <init>(Landroid/content/Context;[Ljava/lang/String;)V
    .locals 6

    .line 33
    invoke-direct {p0}, Landroid/widget/BaseAdapter;-><init>()V

    .line 34
    iput-object p1, p0, Lcom/android/fmradio/views/PTYItemAdapter;->mContext:Landroid/content/Context;

    .line 35
    invoke-static {p1}, Landroid/view/LayoutInflater;->from(Landroid/content/Context;)Landroid/view/LayoutInflater;

    move-result-object v0

    iput-object v0, p0, Lcom/android/fmradio/views/PTYItemAdapter;->mLayoutInflater:Landroid/view/LayoutInflater;

    .line 37
    iput-object p2, p0, Lcom/android/fmradio/views/PTYItemAdapter;->mDatas:[Ljava/lang/String;

    .line 39
    iget-object p2, p0, Lcom/android/fmradio/views/PTYItemAdapter;->mContext:Landroid/content/Context;

    check-cast p2, Landroid/app/Activity;

    invoke-virtual {p2}, Landroid/app/Activity;->getWindowManager()Landroid/view/WindowManager;

    move-result-object p2

    invoke-interface {p2}, Landroid/view/WindowManager;->getDefaultDisplay()Landroid/view/Display;

    move-result-object p2

    .line 40
    new-instance v0, Landroid/graphics/Point;

    invoke-direct {v0}, Landroid/graphics/Point;-><init>()V

    .line 41
    invoke-virtual {p2, v0}, Landroid/view/Display;->getRealSize(Landroid/graphics/Point;)V

    .line 43
    iget p2, v0, Landroid/graphics/Point;->x:I

    .line 44
    iget v0, v0, Landroid/graphics/Point;->y:I

    .line 46
    invoke-static {p1}, Lcom/android/fmradio/utils/FmUtils;->getStatusBarHeight(Landroid/content/Context;)I

    move-result v1

    .line 47
    invoke-static {p1}, Lcom/android/fmradio/utils/FmUtils;->getNavigationBarHeight(Landroid/content/Context;)I

    move-result v2

    .line 49
    invoke-virtual {p1}, Landroid/content/Context;->getResources()Landroid/content/res/Resources;

    move-result-object v3

    invoke-virtual {v3}, Landroid/content/res/Resources;->getConfiguration()Landroid/content/res/Configuration;

    move-result-object v3

    .line 50
    iget v3, v3, Landroid/content/res/Configuration;->orientation:I

    .line 52
    invoke-virtual {p1}, Landroid/content/Context;->getResources()Landroid/content/res/Resources;

    move-result-object p1

    const v4, 0x7f06006a

    invoke-virtual {p1, v4}, Landroid/content/res/Resources;->getDimensionPixelOffset(I)I

    move-result p1

    add-int/2addr p1, v1

    sub-int/2addr v0, p1

    .line 53
    invoke-static {}, Landroid/qf/os/QFApi;->isShuPing()Z

    move-result p1

    if-nez p1, :cond_1

    .line 54
    invoke-static {}, Landroid/qf/os/QFApi;->isYaoTouJi()Z

    move-result p1

    if-eqz p1, :cond_0

    invoke-static {}, Landroid/qf/os/QFApi;->showNaviBar()Z

    move-result p1

    if-nez p1, :cond_1

    .line 55
    :cond_0
    invoke-static {}, Landroid/qf/os/QFApi;->getProjectType()Ljava/lang/String;

    move-result-object p1

    const-string v4, "navi4"

    invoke-virtual {p1, v4}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result p1

    if-eqz p1, :cond_2

    :cond_1
    sub-int/2addr v0, v2

    .line 58
    :cond_2
    div-int/lit8 p1, v0, 0x8

    iput p1, p0, Lcom/android/fmradio/views/PTYItemAdapter;->mItemHeight:I

    .line 60
    sget-object p1, Lcom/android/fmradio/views/PTYItemAdapter;->TAG:Ljava/lang/String;

    new-instance v4, Ljava/lang/StringBuilder;

    invoke-direct {v4}, Ljava/lang/StringBuilder;-><init>()V

    const-string v5, "orientation: "

    invoke-virtual {v4, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v4, v3}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v3, " - screenWidth: "

    invoke-virtual {v4, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v4, p2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string p2, " - screenHeight: "

    invoke-virtual {v4, p2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v4, v0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string p2, " - statusBarHeight: "

    invoke-virtual {v4, p2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v4, v1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string p2, " - navigationBarHeight: "

    invoke-virtual {v4, p2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v4, v2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string p2, " - mItemHeight: "

    invoke-virtual {v4, p2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget p2, p0, Lcom/android/fmradio/views/PTYItemAdapter;->mItemHeight:I

    invoke-virtual {v4, p2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v4}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p2

    invoke-static {p1, p2}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    return-void
.end method


# virtual methods
.method public getCount()I
    .locals 1

    .line 74
    iget-object v0, p0, Lcom/android/fmradio/views/PTYItemAdapter;->mDatas:[Ljava/lang/String;

    array-length v0, v0

    return v0
.end method

.method public bridge synthetic getItem(I)Ljava/lang/Object;
    .locals 0

    .line 20
    invoke-virtual {p0, p1}, Lcom/android/fmradio/views/PTYItemAdapter;->getItem(I)Ljava/lang/String;

    move-result-object p1

    return-object p1
.end method

.method public getItem(I)Ljava/lang/String;
    .locals 1

    .line 79
    iget-object v0, p0, Lcom/android/fmradio/views/PTYItemAdapter;->mDatas:[Ljava/lang/String;

    aget-object p1, v0, p1

    return-object p1
.end method

.method public getItemId(I)J
    .locals 2

    int-to-long v0, p1

    return-wide v0
.end method

.method public getView(ILandroid/view/View;Landroid/view/ViewGroup;)Landroid/view/View;
    .locals 5

    const/4 v0, 0x0

    if-nez p2, :cond_0

    .line 92
    new-instance p2, Lcom/android/fmradio/views/PTYItemAdapter$ViewHolder;

    invoke-direct {p2, v0}, Lcom/android/fmradio/views/PTYItemAdapter$ViewHolder;-><init>(Lcom/android/fmradio/views/PTYItemAdapter$1;)V

    .line 93
    iget-object v1, p0, Lcom/android/fmradio/views/PTYItemAdapter;->mLayoutInflater:Landroid/view/LayoutInflater;

    const v2, 0x7f0b0029

    const/4 v3, 0x0

    invoke-virtual {v1, v2, p3, v3}, Landroid/view/LayoutInflater;->inflate(ILandroid/view/ViewGroup;Z)Landroid/view/View;

    move-result-object p3

    .line 94
    new-instance v1, Landroid/widget/LinearLayout$LayoutParams;

    const/4 v2, -0x1

    iget v3, p0, Lcom/android/fmradio/views/PTYItemAdapter;->mItemHeight:I

    invoke-direct {v1, v2, v3}, Landroid/widget/LinearLayout$LayoutParams;-><init>(II)V

    invoke-virtual {p3, v1}, Landroid/view/View;->setLayoutParams(Landroid/view/ViewGroup$LayoutParams;)V

    const v1, 0x7f0800d8

    .line 95
    invoke-virtual {p3, v1}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object v1

    check-cast v1, Landroid/widget/TextView;

    iput-object v1, p2, Lcom/android/fmradio/views/PTYItemAdapter$ViewHolder;->mTVPtyItem:Landroid/widget/TextView;

    .line 96
    invoke-virtual {p3, p2}, Landroid/view/View;->setTag(Ljava/lang/Object;)V

    goto :goto_0

    .line 98
    :cond_0
    invoke-virtual {p2}, Landroid/view/View;->getTag()Ljava/lang/Object;

    move-result-object p3

    check-cast p3, Lcom/android/fmradio/views/PTYItemAdapter$ViewHolder;

    move-object v4, p3

    move-object p3, p2

    move-object p2, v4

    .line 101
    :goto_0
    iget-object v1, p2, Lcom/android/fmradio/views/PTYItemAdapter$ViewHolder;->mTVPtyItem:Landroid/widget/TextView;

    invoke-virtual {p0, p1}, Lcom/android/fmradio/views/PTYItemAdapter;->getItem(I)Ljava/lang/String;

    move-result-object v2

    invoke-virtual {v1, v2}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    .line 103
    iget v1, p0, Lcom/android/fmradio/views/PTYItemAdapter;->mPtyType:I

    if-ne p1, v1, :cond_1

    .line 104
    iget-object p1, p2, Lcom/android/fmradio/views/PTYItemAdapter$ViewHolder;->mTVPtyItem:Landroid/widget/TextView;

    iget-object p2, p0, Lcom/android/fmradio/views/PTYItemAdapter;->mContext:Landroid/content/Context;

    invoke-virtual {p2}, Landroid/content/Context;->getResources()Landroid/content/res/Resources;

    move-result-object p2

    const v1, 0x7f0700c2

    invoke-virtual {p2, v1, v0}, Landroid/content/res/Resources;->getDrawable(ILandroid/content/res/Resources$Theme;)Landroid/graphics/drawable/Drawable;

    move-result-object p2

    invoke-virtual {p1, p2}, Landroid/widget/TextView;->setBackground(Landroid/graphics/drawable/Drawable;)V

    goto :goto_1

    .line 106
    :cond_1
    iget-object p1, p2, Lcom/android/fmradio/views/PTYItemAdapter$ViewHolder;->mTVPtyItem:Landroid/widget/TextView;

    iget-object p2, p0, Lcom/android/fmradio/views/PTYItemAdapter;->mContext:Landroid/content/Context;

    invoke-virtual {p2}, Landroid/content/Context;->getResources()Landroid/content/res/Resources;

    move-result-object p2

    const v1, 0x7f0700c0

    invoke-virtual {p2, v1, v0}, Landroid/content/res/Resources;->getDrawable(ILandroid/content/res/Resources$Theme;)Landroid/graphics/drawable/Drawable;

    move-result-object p2

    invoke-virtual {p1, p2}, Landroid/widget/TextView;->setBackground(Landroid/graphics/drawable/Drawable;)V

    :goto_1
    return-object p3
.end method

.method public setPtyType(I)V
    .locals 0

    .line 69
    iput p1, p0, Lcom/android/fmradio/views/PTYItemAdapter;->mPtyType:I

    return-void
.end method
