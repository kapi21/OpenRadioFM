.class public Lcom/android/fmradio/views/FmTouchlessScrollView;
.super Landroid/widget/ScrollView;
.source "FmTouchlessScrollView.java"


# direct methods
.method public constructor <init>(Landroid/content/Context;)V
    .locals 1

    const/4 v0, 0x0

    .line 38
    invoke-direct {p0, p1, v0}, Lcom/android/fmradio/views/FmTouchlessScrollView;-><init>(Landroid/content/Context;Landroid/util/AttributeSet;)V

    return-void
.end method

.method public constructor <init>(Landroid/content/Context;Landroid/util/AttributeSet;)V
    .locals 1

    const/4 v0, 0x0

    .line 48
    invoke-direct {p0, p1, p2, v0}, Lcom/android/fmradio/views/FmTouchlessScrollView;-><init>(Landroid/content/Context;Landroid/util/AttributeSet;I)V

    return-void
.end method

.method public constructor <init>(Landroid/content/Context;Landroid/util/AttributeSet;I)V
    .locals 0

    .line 59
    invoke-direct {p0, p1, p2, p3}, Landroid/widget/ScrollView;-><init>(Landroid/content/Context;Landroid/util/AttributeSet;I)V

    return-void
.end method


# virtual methods
.method public onInterceptTouchEvent(Landroid/view/MotionEvent;)Z
    .locals 0

    const/4 p1, 0x0

    return p1
.end method

.method protected onSaveInstanceState()Landroid/os/Parcelable;
    .locals 2

    .line 66
    invoke-virtual {p0}, Lcom/android/fmradio/views/FmTouchlessScrollView;->getScrollY()I

    move-result v0

    const/4 v1, 0x0

    .line 67
    invoke-virtual {p0, v1}, Lcom/android/fmradio/views/FmTouchlessScrollView;->setScrollY(I)V

    .line 68
    invoke-super {p0}, Landroid/widget/ScrollView;->onSaveInstanceState()Landroid/os/Parcelable;

    move-result-object v1

    .line 69
    invoke-virtual {p0, v0}, Lcom/android/fmradio/views/FmTouchlessScrollView;->setScrollY(I)V

    return-object v1
.end method

.method public onTouchEvent(Landroid/view/MotionEvent;)Z
    .locals 0

    const/4 p1, 0x0

    return p1
.end method
