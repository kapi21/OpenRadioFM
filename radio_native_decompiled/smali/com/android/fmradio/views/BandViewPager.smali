.class public Lcom/android/fmradio/views/BandViewPager;
.super Landroidx/viewpager/widget/ViewPager;
.source "BandViewPager.java"


# instance fields
.field private noScroll:Z


# direct methods
.method public constructor <init>(Landroid/content/Context;)V
    .locals 0

    .line 18
    invoke-direct {p0, p1}, Landroidx/viewpager/widget/ViewPager;-><init>(Landroid/content/Context;)V

    const/4 p1, 0x1

    .line 10
    iput-boolean p1, p0, Lcom/android/fmradio/views/BandViewPager;->noScroll:Z

    return-void
.end method

.method public constructor <init>(Landroid/content/Context;Landroid/util/AttributeSet;)V
    .locals 0

    .line 13
    invoke-direct {p0, p1, p2}, Landroidx/viewpager/widget/ViewPager;-><init>(Landroid/content/Context;Landroid/util/AttributeSet;)V

    const/4 p1, 0x1

    .line 10
    iput-boolean p1, p0, Lcom/android/fmradio/views/BandViewPager;->noScroll:Z

    return-void
.end method


# virtual methods
.method public onInterceptTouchEvent(Landroid/view/MotionEvent;)Z
    .locals 1

    .line 40
    iget-boolean v0, p0, Lcom/android/fmradio/views/BandViewPager;->noScroll:Z

    if-eqz v0, :cond_0

    const/4 p1, 0x0

    return p1

    .line 43
    :cond_0
    invoke-super {p0, p1}, Landroidx/viewpager/widget/ViewPager;->onInterceptTouchEvent(Landroid/view/MotionEvent;)Z

    move-result p1

    return p1
.end method

.method public onTouchEvent(Landroid/view/MotionEvent;)Z
    .locals 1

    .line 32
    iget-boolean v0, p0, Lcom/android/fmradio/views/BandViewPager;->noScroll:Z

    if-eqz v0, :cond_0

    const/4 p1, 0x0

    return p1

    .line 35
    :cond_0
    invoke-super {p0, p1}, Landroidx/viewpager/widget/ViewPager;->onTouchEvent(Landroid/view/MotionEvent;)Z

    move-result p1

    return p1
.end method

.method public scrollTo(II)V
    .locals 0

    .line 27
    invoke-super {p0, p1, p2}, Landroidx/viewpager/widget/ViewPager;->scrollTo(II)V

    return-void
.end method

.method public setNoScroll(Z)V
    .locals 0

    .line 22
    iput-boolean p1, p0, Lcom/android/fmradio/views/BandViewPager;->noScroll:Z

    return-void
.end method
