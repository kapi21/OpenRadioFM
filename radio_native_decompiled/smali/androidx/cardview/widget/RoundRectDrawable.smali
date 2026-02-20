.class Landroidx/cardview/widget/RoundRectDrawable;
.super Landroid/graphics/drawable/Drawable;
.source "RoundRectDrawable.java"


# instance fields
.field private final mBoundsF:Landroid/graphics/RectF;

.field private final mBoundsI:Landroid/graphics/Rect;

.field private mInsetForPadding:Z

.field private mInsetForRadius:Z

.field private mPadding:F

.field private final mPaint:Landroid/graphics/Paint;

.field private mRadius:F


# direct methods
.method public constructor <init>(IF)V
    .locals 1

    .line 45
    invoke-direct {p0}, Landroid/graphics/drawable/Drawable;-><init>()V

    const/4 v0, 0x0

    .line 42
    iput-boolean v0, p0, Landroidx/cardview/widget/RoundRectDrawable;->mInsetForPadding:Z

    const/4 v0, 0x1

    .line 43
    iput-boolean v0, p0, Landroidx/cardview/widget/RoundRectDrawable;->mInsetForRadius:Z

    .line 46
    iput p2, p0, Landroidx/cardview/widget/RoundRectDrawable;->mRadius:F

    .line 47
    new-instance p2, Landroid/graphics/Paint;

    const/4 v0, 0x5

    invoke-direct {p2, v0}, Landroid/graphics/Paint;-><init>(I)V

    iput-object p2, p0, Landroidx/cardview/widget/RoundRectDrawable;->mPaint:Landroid/graphics/Paint;

    .line 48
    iget-object p2, p0, Landroidx/cardview/widget/RoundRectDrawable;->mPaint:Landroid/graphics/Paint;

    invoke-virtual {p2, p1}, Landroid/graphics/Paint;->setColor(I)V

    .line 49
    new-instance p1, Landroid/graphics/RectF;

    invoke-direct {p1}, Landroid/graphics/RectF;-><init>()V

    iput-object p1, p0, Landroidx/cardview/widget/RoundRectDrawable;->mBoundsF:Landroid/graphics/RectF;

    .line 50
    new-instance p1, Landroid/graphics/Rect;

    invoke-direct {p1}, Landroid/graphics/Rect;-><init>()V

    iput-object p1, p0, Landroidx/cardview/widget/RoundRectDrawable;->mBoundsI:Landroid/graphics/Rect;

    return-void
.end method

.method private updateBounds(Landroid/graphics/Rect;)V
    .locals 5

    if-nez p1, :cond_0

    .line 76
    invoke-virtual {p0}, Landroidx/cardview/widget/RoundRectDrawable;->getBounds()Landroid/graphics/Rect;

    move-result-object p1

    .line 78
    :cond_0
    iget-object v0, p0, Landroidx/cardview/widget/RoundRectDrawable;->mBoundsF:Landroid/graphics/RectF;

    iget v1, p1, Landroid/graphics/Rect;->left:I

    int-to-float v1, v1

    iget v2, p1, Landroid/graphics/Rect;->top:I

    int-to-float v2, v2

    iget v3, p1, Landroid/graphics/Rect;->right:I

    int-to-float v3, v3

    iget v4, p1, Landroid/graphics/Rect;->bottom:I

    int-to-float v4, v4

    invoke-virtual {v0, v1, v2, v3, v4}, Landroid/graphics/RectF;->set(FFFF)V

    .line 79
    iget-object v0, p0, Landroidx/cardview/widget/RoundRectDrawable;->mBoundsI:Landroid/graphics/Rect;

    invoke-virtual {v0, p1}, Landroid/graphics/Rect;->set(Landroid/graphics/Rect;)V

    .line 80
    iget-boolean p1, p0, Landroidx/cardview/widget/RoundRectDrawable;->mInsetForPadding:Z

    if-eqz p1, :cond_1

    .line 81
    iget p1, p0, Landroidx/cardview/widget/RoundRectDrawable;->mPadding:F

    iget v0, p0, Landroidx/cardview/widget/RoundRectDrawable;->mRadius:F

    iget-boolean v1, p0, Landroidx/cardview/widget/RoundRectDrawable;->mInsetForRadius:Z

    invoke-static {p1, v0, v1}, Landroidx/cardview/widget/RoundRectDrawableWithShadow;->calculateVerticalPadding(FFZ)F

    move-result p1

    .line 82
    iget v0, p0, Landroidx/cardview/widget/RoundRectDrawable;->mPadding:F

    iget v1, p0, Landroidx/cardview/widget/RoundRectDrawable;->mRadius:F

    iget-boolean v2, p0, Landroidx/cardview/widget/RoundRectDrawable;->mInsetForRadius:Z

    invoke-static {v0, v1, v2}, Landroidx/cardview/widget/RoundRectDrawableWithShadow;->calculateHorizontalPadding(FFZ)F

    move-result v0

    .line 83
    iget-object v1, p0, Landroidx/cardview/widget/RoundRectDrawable;->mBoundsI:Landroid/graphics/Rect;

    float-to-double v2, v0

    invoke-static {v2, v3}, Ljava/lang/Math;->ceil(D)D

    move-result-wide v2

    double-to-int v0, v2

    float-to-double v2, p1

    invoke-static {v2, v3}, Ljava/lang/Math;->ceil(D)D

    move-result-wide v2

    double-to-int p1, v2

    invoke-virtual {v1, v0, p1}, Landroid/graphics/Rect;->inset(II)V

    .line 85
    iget-object p1, p0, Landroidx/cardview/widget/RoundRectDrawable;->mBoundsF:Landroid/graphics/RectF;

    iget-object v0, p0, Landroidx/cardview/widget/RoundRectDrawable;->mBoundsI:Landroid/graphics/Rect;

    invoke-virtual {p1, v0}, Landroid/graphics/RectF;->set(Landroid/graphics/Rect;)V

    :cond_1
    return-void
.end method


# virtual methods
.method public draw(Landroid/graphics/Canvas;)V
    .locals 3

    .line 71
    iget-object v0, p0, Landroidx/cardview/widget/RoundRectDrawable;->mBoundsF:Landroid/graphics/RectF;

    iget v1, p0, Landroidx/cardview/widget/RoundRectDrawable;->mRadius:F

    iget-object v2, p0, Landroidx/cardview/widget/RoundRectDrawable;->mPaint:Landroid/graphics/Paint;

    invoke-virtual {p1, v0, v1, v1, v2}, Landroid/graphics/Canvas;->drawRoundRect(Landroid/graphics/RectF;FFLandroid/graphics/Paint;)V

    return-void
.end method

.method public getOpacity()I
    .locals 1

    const/4 v0, -0x3

    return v0
.end method

.method public getOutline(Landroid/graphics/Outline;)V
    .locals 2

    .line 97
    iget-object v0, p0, Landroidx/cardview/widget/RoundRectDrawable;->mBoundsI:Landroid/graphics/Rect;

    iget v1, p0, Landroidx/cardview/widget/RoundRectDrawable;->mRadius:F

    invoke-virtual {p1, v0, v1}, Landroid/graphics/Outline;->setRoundRect(Landroid/graphics/Rect;F)V

    return-void
.end method

.method getPadding()F
    .locals 1

    .line 66
    iget v0, p0, Landroidx/cardview/widget/RoundRectDrawable;->mPadding:F

    return v0
.end method

.method public getRadius()F
    .locals 1

    .line 125
    iget v0, p0, Landroidx/cardview/widget/RoundRectDrawable;->mRadius:F

    return v0
.end method

.method protected onBoundsChange(Landroid/graphics/Rect;)V
    .locals 0

    .line 91
    invoke-super {p0, p1}, Landroid/graphics/drawable/Drawable;->onBoundsChange(Landroid/graphics/Rect;)V

    .line 92
    invoke-direct {p0, p1}, Landroidx/cardview/widget/RoundRectDrawable;->updateBounds(Landroid/graphics/Rect;)V

    return-void
.end method

.method public setAlpha(I)V
    .locals 0

    return-void
.end method

.method public setColor(I)V
    .locals 1

    .line 129
    iget-object v0, p0, Landroidx/cardview/widget/RoundRectDrawable;->mPaint:Landroid/graphics/Paint;

    invoke-virtual {v0, p1}, Landroid/graphics/Paint;->setColor(I)V

    .line 130
    invoke-virtual {p0}, Landroidx/cardview/widget/RoundRectDrawable;->invalidateSelf()V

    return-void
.end method

.method public setColorFilter(Landroid/graphics/ColorFilter;)V
    .locals 0

    return-void
.end method

.method setPadding(FZZ)V
    .locals 1

    .line 54
    iget v0, p0, Landroidx/cardview/widget/RoundRectDrawable;->mPadding:F

    cmpl-float v0, p1, v0

    if-nez v0, :cond_0

    iget-boolean v0, p0, Landroidx/cardview/widget/RoundRectDrawable;->mInsetForPadding:Z

    if-ne v0, p2, :cond_0

    iget-boolean v0, p0, Landroidx/cardview/widget/RoundRectDrawable;->mInsetForRadius:Z

    if-ne v0, p3, :cond_0

    return-void

    .line 58
    :cond_0
    iput p1, p0, Landroidx/cardview/widget/RoundRectDrawable;->mPadding:F

    .line 59
    iput-boolean p2, p0, Landroidx/cardview/widget/RoundRectDrawable;->mInsetForPadding:Z

    .line 60
    iput-boolean p3, p0, Landroidx/cardview/widget/RoundRectDrawable;->mInsetForRadius:Z

    const/4 p1, 0x0

    .line 61
    invoke-direct {p0, p1}, Landroidx/cardview/widget/RoundRectDrawable;->updateBounds(Landroid/graphics/Rect;)V

    .line 62
    invoke-virtual {p0}, Landroidx/cardview/widget/RoundRectDrawable;->invalidateSelf()V

    return-void
.end method

.method setRadius(F)V
    .locals 1

    .line 101
    iget v0, p0, Landroidx/cardview/widget/RoundRectDrawable;->mRadius:F

    cmpl-float v0, p1, v0

    if-nez v0, :cond_0

    return-void

    .line 104
    :cond_0
    iput p1, p0, Landroidx/cardview/widget/RoundRectDrawable;->mRadius:F

    const/4 p1, 0x0

    .line 105
    invoke-direct {p0, p1}, Landroidx/cardview/widget/RoundRectDrawable;->updateBounds(Landroid/graphics/Rect;)V

    .line 106
    invoke-virtual {p0}, Landroidx/cardview/widget/RoundRectDrawable;->invalidateSelf()V

    return-void
.end method
