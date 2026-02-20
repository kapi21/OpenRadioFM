.class Landroidx/cardview/widget/CardViewApi21;
.super Ljava/lang/Object;
.source "CardViewApi21.java"

# interfaces
.implements Landroidx/cardview/widget/CardViewImpl;


# direct methods
.method constructor <init>()V
    .locals 0

    .line 21
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public getElevation(Landroidx/cardview/widget/CardViewDelegate;)F
    .locals 0

    .line 77
    check-cast p1, Landroid/view/View;

    invoke-virtual {p1}, Landroid/view/View;->getElevation()F

    move-result p1

    return p1
.end method

.method public getMaxElevation(Landroidx/cardview/widget/CardViewDelegate;)F
    .locals 0

    .line 52
    invoke-interface {p1}, Landroidx/cardview/widget/CardViewDelegate;->getBackground()Landroid/graphics/drawable/Drawable;

    move-result-object p1

    check-cast p1, Landroidx/cardview/widget/RoundRectDrawable;

    check-cast p1, Landroidx/cardview/widget/RoundRectDrawable;

    invoke-virtual {p1}, Landroidx/cardview/widget/RoundRectDrawable;->getPadding()F

    move-result p1

    return p1
.end method

.method public getMinHeight(Landroidx/cardview/widget/CardViewDelegate;)F
    .locals 1

    .line 62
    invoke-virtual {p0, p1}, Landroidx/cardview/widget/CardViewApi21;->getRadius(Landroidx/cardview/widget/CardViewDelegate;)F

    move-result p1

    const/high16 v0, 0x40000000    # 2.0f

    mul-float/2addr p1, v0

    return p1
.end method

.method public getMinWidth(Landroidx/cardview/widget/CardViewDelegate;)F
    .locals 1

    .line 57
    invoke-virtual {p0, p1}, Landroidx/cardview/widget/CardViewApi21;->getRadius(Landroidx/cardview/widget/CardViewDelegate;)F

    move-result p1

    const/high16 v0, 0x40000000    # 2.0f

    mul-float/2addr p1, v0

    return p1
.end method

.method public getRadius(Landroidx/cardview/widget/CardViewDelegate;)F
    .locals 0

    .line 67
    invoke-interface {p1}, Landroidx/cardview/widget/CardViewDelegate;->getBackground()Landroid/graphics/drawable/Drawable;

    move-result-object p1

    check-cast p1, Landroidx/cardview/widget/RoundRectDrawable;

    check-cast p1, Landroidx/cardview/widget/RoundRectDrawable;

    invoke-virtual {p1}, Landroidx/cardview/widget/RoundRectDrawable;->getRadius()F

    move-result p1

    return p1
.end method

.method public initStatic()V
    .locals 0

    return-void
.end method

.method public initialize(Landroidx/cardview/widget/CardViewDelegate;Landroid/content/Context;IFFF)V
    .locals 0

    .line 26
    new-instance p2, Landroidx/cardview/widget/RoundRectDrawable;

    invoke-direct {p2, p3, p4}, Landroidx/cardview/widget/RoundRectDrawable;-><init>(IF)V

    .line 27
    invoke-interface {p1, p2}, Landroidx/cardview/widget/CardViewDelegate;->setBackgroundDrawable(Landroid/graphics/drawable/Drawable;)V

    .line 28
    move-object p2, p1

    check-cast p2, Landroid/view/View;

    const/4 p3, 0x1

    .line 29
    invoke-virtual {p2, p3}, Landroid/view/View;->setClipToOutline(Z)V

    .line 30
    invoke-virtual {p2, p5}, Landroid/view/View;->setElevation(F)V

    .line 31
    invoke-virtual {p0, p1, p6}, Landroidx/cardview/widget/CardViewApi21;->setMaxElevation(Landroidx/cardview/widget/CardViewDelegate;F)V

    return-void
.end method

.method public onCompatPaddingChanged(Landroidx/cardview/widget/CardViewDelegate;)V
    .locals 1

    .line 97
    invoke-virtual {p0, p1}, Landroidx/cardview/widget/CardViewApi21;->getMaxElevation(Landroidx/cardview/widget/CardViewDelegate;)F

    move-result v0

    invoke-virtual {p0, p1, v0}, Landroidx/cardview/widget/CardViewApi21;->setMaxElevation(Landroidx/cardview/widget/CardViewDelegate;F)V

    return-void
.end method

.method public onPreventCornerOverlapChanged(Landroidx/cardview/widget/CardViewDelegate;)V
    .locals 1

    .line 102
    invoke-virtual {p0, p1}, Landroidx/cardview/widget/CardViewApi21;->getMaxElevation(Landroidx/cardview/widget/CardViewDelegate;)F

    move-result v0

    invoke-virtual {p0, p1, v0}, Landroidx/cardview/widget/CardViewApi21;->setMaxElevation(Landroidx/cardview/widget/CardViewDelegate;F)V

    return-void
.end method

.method public setBackgroundColor(Landroidx/cardview/widget/CardViewDelegate;I)V
    .locals 0

    .line 107
    invoke-interface {p1}, Landroidx/cardview/widget/CardViewDelegate;->getBackground()Landroid/graphics/drawable/Drawable;

    move-result-object p1

    check-cast p1, Landroidx/cardview/widget/RoundRectDrawable;

    check-cast p1, Landroidx/cardview/widget/RoundRectDrawable;

    invoke-virtual {p1, p2}, Landroidx/cardview/widget/RoundRectDrawable;->setColor(I)V

    return-void
.end method

.method public setElevation(Landroidx/cardview/widget/CardViewDelegate;F)V
    .locals 0

    .line 72
    check-cast p1, Landroid/view/View;

    invoke-virtual {p1, p2}, Landroid/view/View;->setElevation(F)V

    return-void
.end method

.method public setMaxElevation(Landroidx/cardview/widget/CardViewDelegate;F)V
    .locals 3

    .line 45
    invoke-interface {p1}, Landroidx/cardview/widget/CardViewDelegate;->getBackground()Landroid/graphics/drawable/Drawable;

    move-result-object v0

    check-cast v0, Landroidx/cardview/widget/RoundRectDrawable;

    check-cast v0, Landroidx/cardview/widget/RoundRectDrawable;

    invoke-interface {p1}, Landroidx/cardview/widget/CardViewDelegate;->getUseCompatPadding()Z

    move-result v1

    invoke-interface {p1}, Landroidx/cardview/widget/CardViewDelegate;->getPreventCornerOverlap()Z

    move-result v2

    invoke-virtual {v0, p2, v1, v2}, Landroidx/cardview/widget/RoundRectDrawable;->setPadding(FZZ)V

    .line 47
    invoke-virtual {p0, p1}, Landroidx/cardview/widget/CardViewApi21;->updatePadding(Landroidx/cardview/widget/CardViewDelegate;)V

    return-void
.end method

.method public setRadius(Landroidx/cardview/widget/CardViewDelegate;F)V
    .locals 0

    .line 36
    invoke-interface {p1}, Landroidx/cardview/widget/CardViewDelegate;->getBackground()Landroid/graphics/drawable/Drawable;

    move-result-object p1

    check-cast p1, Landroidx/cardview/widget/RoundRectDrawable;

    check-cast p1, Landroidx/cardview/widget/RoundRectDrawable;

    invoke-virtual {p1, p2}, Landroidx/cardview/widget/RoundRectDrawable;->setRadius(F)V

    return-void
.end method

.method public updatePadding(Landroidx/cardview/widget/CardViewDelegate;)V
    .locals 4

    .line 82
    invoke-interface {p1}, Landroidx/cardview/widget/CardViewDelegate;->getUseCompatPadding()Z

    move-result v0

    if-nez v0, :cond_0

    const/4 v0, 0x0

    .line 83
    invoke-interface {p1, v0, v0, v0, v0}, Landroidx/cardview/widget/CardViewDelegate;->setShadowPadding(IIII)V

    return-void

    .line 86
    :cond_0
    invoke-virtual {p0, p1}, Landroidx/cardview/widget/CardViewApi21;->getMaxElevation(Landroidx/cardview/widget/CardViewDelegate;)F

    move-result v0

    .line 87
    invoke-virtual {p0, p1}, Landroidx/cardview/widget/CardViewApi21;->getRadius(Landroidx/cardview/widget/CardViewDelegate;)F

    move-result v1

    .line 88
    invoke-interface {p1}, Landroidx/cardview/widget/CardViewDelegate;->getPreventCornerOverlap()Z

    move-result v2

    invoke-static {v0, v1, v2}, Landroidx/cardview/widget/RoundRectDrawableWithShadow;->calculateHorizontalPadding(FFZ)F

    move-result v2

    float-to-double v2, v2

    invoke-static {v2, v3}, Ljava/lang/Math;->ceil(D)D

    move-result-wide v2

    double-to-int v2, v2

    .line 90
    invoke-interface {p1}, Landroidx/cardview/widget/CardViewDelegate;->getPreventCornerOverlap()Z

    move-result v3

    invoke-static {v0, v1, v3}, Landroidx/cardview/widget/RoundRectDrawableWithShadow;->calculateVerticalPadding(FFZ)F

    move-result v0

    float-to-double v0, v0

    invoke-static {v0, v1}, Ljava/lang/Math;->ceil(D)D

    move-result-wide v0

    double-to-int v0, v0

    .line 92
    invoke-interface {p1, v2, v0, v2, v0}, Landroidx/cardview/widget/CardViewDelegate;->setShadowPadding(IIII)V

    return-void
.end method
