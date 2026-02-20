.class public Lcom/android/fmradio/views/NoCancelActionRelativeLayout;
.super Landroid/widget/RelativeLayout;
.source "NoCancelActionRelativeLayout.java"


# direct methods
.method public constructor <init>(Landroid/content/Context;Landroid/util/AttributeSet;)V
    .locals 0

    .line 18
    invoke-direct {p0, p1, p2}, Landroid/widget/RelativeLayout;-><init>(Landroid/content/Context;Landroid/util/AttributeSet;)V

    return-void
.end method


# virtual methods
.method public onTouchEvent(Landroid/view/MotionEvent;)Z
    .locals 3

    const/4 v0, 0x1

    .line 23
    invoke-virtual {p0, v0}, Lcom/android/fmradio/views/NoCancelActionRelativeLayout;->requestDisallowInterceptTouchEvent(Z)V

    const-string v1, "NoCancelActionRelativeLayout"

    const-string v2, "onTouchEvent - start"

    .line 24
    invoke-static {v1, v2}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 25
    invoke-virtual {p1}, Landroid/view/MotionEvent;->getAction()I

    move-result p1

    const/4 v2, 0x3

    if-ne p1, v2, :cond_0

    const-string p1, "onTouchEvent - ACTION_CANCEL"

    .line 26
    invoke-static {v1, p1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    :cond_0
    return v0
.end method
