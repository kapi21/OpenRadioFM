.class public Lcom/android/fmradio/views/RadioPresetItemView;
.super Landroid/widget/LinearLayout;
.source "RadioPresetItemView.java"


# instance fields
.field private mTVPresetIndex:Landroid/widget/TextView;

.field private mTVPresetName:Landroid/widget/TextView;

.field private mTVPresetStation:Landroid/widget/TextView;


# direct methods
.method public constructor <init>(Landroid/content/Context;Landroid/util/AttributeSet;)V
    .locals 2

    .line 23
    invoke-direct {p0, p1, p2}, Landroid/widget/LinearLayout;-><init>(Landroid/content/Context;Landroid/util/AttributeSet;)V

    .line 26
    iget-object p2, p0, Lcom/android/fmradio/views/RadioPresetItemView;->mContext:Landroid/content/Context;

    check-cast p2, Landroid/app/Activity;

    invoke-virtual {p2}, Landroid/app/Activity;->isInMultiWindowMode()Z

    move-result p2

    const/4 v0, 0x0

    const/4 v1, 0x0

    if-eqz p2, :cond_0

    .line 27
    invoke-static {p1}, Landroid/view/LayoutInflater;->from(Landroid/content/Context;)Landroid/view/LayoutInflater;

    move-result-object p1

    const p2, 0x7f0b0039

    invoke-virtual {p1, p2, v1, v0}, Landroid/view/LayoutInflater;->inflate(ILandroid/view/ViewGroup;Z)Landroid/view/View;

    move-result-object p1

    goto :goto_0

    .line 29
    :cond_0
    invoke-static {p1}, Landroid/view/LayoutInflater;->from(Landroid/content/Context;)Landroid/view/LayoutInflater;

    move-result-object p1

    const p2, 0x7f0b002e

    invoke-virtual {p1, p2, v1, v0}, Landroid/view/LayoutInflater;->inflate(ILandroid/view/ViewGroup;Z)Landroid/view/View;

    move-result-object p1

    .line 31
    :goto_0
    new-instance p2, Landroid/widget/LinearLayout$LayoutParams;

    const/4 v0, -0x1

    invoke-direct {p2, v0, v0}, Landroid/widget/LinearLayout$LayoutParams;-><init>(II)V

    invoke-virtual {p1, p2}, Landroid/view/View;->setLayoutParams(Landroid/view/ViewGroup$LayoutParams;)V

    .line 33
    invoke-direct {p0, p1}, Lcom/android/fmradio/views/RadioPresetItemView;->initView(Landroid/view/View;)V

    .line 35
    invoke-virtual {p0, p1}, Lcom/android/fmradio/views/RadioPresetItemView;->addView(Landroid/view/View;)V

    return-void
.end method

.method private initView(Landroid/view/View;)V
    .locals 1

    const v0, 0x7f0800d5

    .line 39
    invoke-virtual {p1, v0}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object v0

    check-cast v0, Landroid/widget/TextView;

    iput-object v0, p0, Lcom/android/fmradio/views/RadioPresetItemView;->mTVPresetIndex:Landroid/widget/TextView;

    const v0, 0x7f0800d6

    .line 40
    invoke-virtual {p1, v0}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object v0

    check-cast v0, Landroid/widget/TextView;

    iput-object v0, p0, Lcom/android/fmradio/views/RadioPresetItemView;->mTVPresetName:Landroid/widget/TextView;

    const v0, 0x7f0800d7

    .line 41
    invoke-virtual {p1, v0}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object p1

    check-cast p1, Landroid/widget/TextView;

    iput-object p1, p0, Lcom/android/fmradio/views/RadioPresetItemView;->mTVPresetStation:Landroid/widget/TextView;

    return-void
.end method


# virtual methods
.method public getTVPresetStation()Landroid/widget/TextView;
    .locals 1

    .line 57
    iget-object v0, p0, Lcom/android/fmradio/views/RadioPresetItemView;->mTVPresetStation:Landroid/widget/TextView;

    return-object v0
.end method

.method public updatePresetIndex(I)V
    .locals 3

    .line 45
    iget-object v0, p0, Lcom/android/fmradio/views/RadioPresetItemView;->mTVPresetIndex:Landroid/widget/TextView;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "P"

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    add-int/lit8 p1, p1, 0x1

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p1

    invoke-virtual {v0, p1}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    return-void
.end method

.method public updatePresetName(Ljava/lang/String;)V
    .locals 1

    .line 49
    iget-object v0, p0, Lcom/android/fmradio/views/RadioPresetItemView;->mTVPresetName:Landroid/widget/TextView;

    invoke-virtual {v0, p1}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    return-void
.end method

.method public updatePresetStation(Ljava/lang/String;)V
    .locals 1

    .line 53
    iget-object v0, p0, Lcom/android/fmradio/views/RadioPresetItemView;->mTVPresetStation:Landroid/widget/TextView;

    invoke-virtual {v0, p1}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    return-void
.end method
