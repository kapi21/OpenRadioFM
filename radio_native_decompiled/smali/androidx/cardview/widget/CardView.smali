.class public Landroidx/cardview/widget/CardView;
.super Landroid/widget/FrameLayout;
.source "CardView.java"

# interfaces
.implements Landroidx/cardview/widget/CardViewDelegate;


# static fields
.field private static final IMPL:Landroidx/cardview/widget/CardViewImpl;


# instance fields
.field private mCompatPadding:Z

.field private final mContentPadding:Landroid/graphics/Rect;

.field private mPreventCornerOverlap:Z

.field private final mShadowBounds:Landroid/graphics/Rect;


# direct methods
.method static constructor <clinit>()V
    .locals 2

    .line 75
    sget v0, Landroid/os/Build$VERSION;->SDK_INT:I

    const/16 v1, 0x15

    if-lt v0, v1, :cond_0

    .line 76
    new-instance v0, Landroidx/cardview/widget/CardViewApi21;

    invoke-direct {v0}, Landroidx/cardview/widget/CardViewApi21;-><init>()V

    sput-object v0, Landroidx/cardview/widget/CardView;->IMPL:Landroidx/cardview/widget/CardViewImpl;

    goto :goto_0

    .line 77
    :cond_0
    sget v0, Landroid/os/Build$VERSION;->SDK_INT:I

    const/16 v1, 0x11

    if-lt v0, v1, :cond_1

    .line 78
    new-instance v0, Landroidx/cardview/widget/CardViewJellybeanMr1;

    invoke-direct {v0}, Landroidx/cardview/widget/CardViewJellybeanMr1;-><init>()V

    sput-object v0, Landroidx/cardview/widget/CardView;->IMPL:Landroidx/cardview/widget/CardViewImpl;

    goto :goto_0

    .line 80
    :cond_1
    new-instance v0, Landroidx/cardview/widget/CardViewEclairMr1;

    invoke-direct {v0}, Landroidx/cardview/widget/CardViewEclairMr1;-><init>()V

    sput-object v0, Landroidx/cardview/widget/CardView;->IMPL:Landroidx/cardview/widget/CardViewImpl;

    .line 82
    :goto_0
    sget-object v0, Landroidx/cardview/widget/CardView;->IMPL:Landroidx/cardview/widget/CardViewImpl;

    invoke-interface {v0}, Landroidx/cardview/widget/CardViewImpl;->initStatic()V

    return-void
.end method

.method public constructor <init>(Landroid/content/Context;)V
    .locals 2

    .line 95
    invoke-direct {p0, p1}, Landroid/widget/FrameLayout;-><init>(Landroid/content/Context;)V

    .line 89
    new-instance v0, Landroid/graphics/Rect;

    invoke-direct {v0}, Landroid/graphics/Rect;-><init>()V

    iput-object v0, p0, Landroidx/cardview/widget/CardView;->mContentPadding:Landroid/graphics/Rect;

    .line 91
    new-instance v0, Landroid/graphics/Rect;

    invoke-direct {v0}, Landroid/graphics/Rect;-><init>()V

    iput-object v0, p0, Landroidx/cardview/widget/CardView;->mShadowBounds:Landroid/graphics/Rect;

    const/4 v0, 0x0

    const/4 v1, 0x0

    .line 96
    invoke-direct {p0, p1, v0, v1}, Landroidx/cardview/widget/CardView;->initialize(Landroid/content/Context;Landroid/util/AttributeSet;I)V

    return-void
.end method

.method public constructor <init>(Landroid/content/Context;Landroid/util/AttributeSet;)V
    .locals 1

    .line 100
    invoke-direct {p0, p1, p2}, Landroid/widget/FrameLayout;-><init>(Landroid/content/Context;Landroid/util/AttributeSet;)V

    .line 89
    new-instance v0, Landroid/graphics/Rect;

    invoke-direct {v0}, Landroid/graphics/Rect;-><init>()V

    iput-object v0, p0, Landroidx/cardview/widget/CardView;->mContentPadding:Landroid/graphics/Rect;

    .line 91
    new-instance v0, Landroid/graphics/Rect;

    invoke-direct {v0}, Landroid/graphics/Rect;-><init>()V

    iput-object v0, p0, Landroidx/cardview/widget/CardView;->mShadowBounds:Landroid/graphics/Rect;

    const/4 v0, 0x0

    .line 101
    invoke-direct {p0, p1, p2, v0}, Landroidx/cardview/widget/CardView;->initialize(Landroid/content/Context;Landroid/util/AttributeSet;I)V

    return-void
.end method

.method public constructor <init>(Landroid/content/Context;Landroid/util/AttributeSet;I)V
    .locals 1

    .line 105
    invoke-direct {p0, p1, p2, p3}, Landroid/widget/FrameLayout;-><init>(Landroid/content/Context;Landroid/util/AttributeSet;I)V

    .line 89
    new-instance v0, Landroid/graphics/Rect;

    invoke-direct {v0}, Landroid/graphics/Rect;-><init>()V

    iput-object v0, p0, Landroidx/cardview/widget/CardView;->mContentPadding:Landroid/graphics/Rect;

    .line 91
    new-instance v0, Landroid/graphics/Rect;

    invoke-direct {v0}, Landroid/graphics/Rect;-><init>()V

    iput-object v0, p0, Landroidx/cardview/widget/CardView;->mShadowBounds:Landroid/graphics/Rect;

    .line 106
    invoke-direct {p0, p1, p2, p3}, Landroidx/cardview/widget/CardView;->initialize(Landroid/content/Context;Landroid/util/AttributeSet;I)V

    return-void
.end method

.method private initialize(Landroid/content/Context;Landroid/util/AttributeSet;I)V
    .locals 8

    .line 203
    sget-object v0, Landroidx/cardview/R$styleable;->CardView:[I

    sget v1, Landroidx/cardview/R$style;->CardView_Light:I

    invoke-virtual {p1, p2, v0, p3, v1}, Landroid/content/Context;->obtainStyledAttributes(Landroid/util/AttributeSet;[III)Landroid/content/res/TypedArray;

    move-result-object p2

    .line 205
    sget p3, Landroidx/cardview/R$styleable;->CardView_cardBackgroundColor:I

    const/4 v0, 0x0

    invoke-virtual {p2, p3, v0}, Landroid/content/res/TypedArray;->getColor(II)I

    move-result v4

    .line 206
    sget p3, Landroidx/cardview/R$styleable;->CardView_cardCornerRadius:I

    const/4 v1, 0x0

    invoke-virtual {p2, p3, v1}, Landroid/content/res/TypedArray;->getDimension(IF)F

    move-result v5

    .line 207
    sget p3, Landroidx/cardview/R$styleable;->CardView_cardElevation:I

    invoke-virtual {p2, p3, v1}, Landroid/content/res/TypedArray;->getDimension(IF)F

    move-result v6

    .line 208
    sget p3, Landroidx/cardview/R$styleable;->CardView_cardMaxElevation:I

    invoke-virtual {p2, p3, v1}, Landroid/content/res/TypedArray;->getDimension(IF)F

    move-result p3

    .line 209
    sget v1, Landroidx/cardview/R$styleable;->CardView_cardUseCompatPadding:I

    invoke-virtual {p2, v1, v0}, Landroid/content/res/TypedArray;->getBoolean(IZ)Z

    move-result v1

    iput-boolean v1, p0, Landroidx/cardview/widget/CardView;->mCompatPadding:Z

    .line 210
    sget v1, Landroidx/cardview/R$styleable;->CardView_cardPreventCornerOverlap:I

    const/4 v2, 0x1

    invoke-virtual {p2, v1, v2}, Landroid/content/res/TypedArray;->getBoolean(IZ)Z

    move-result v1

    iput-boolean v1, p0, Landroidx/cardview/widget/CardView;->mPreventCornerOverlap:Z

    .line 211
    sget v1, Landroidx/cardview/R$styleable;->CardView_contentPadding:I

    invoke-virtual {p2, v1, v0}, Landroid/content/res/TypedArray;->getDimensionPixelSize(II)I

    move-result v0

    .line 212
    iget-object v1, p0, Landroidx/cardview/widget/CardView;->mContentPadding:Landroid/graphics/Rect;

    sget v2, Landroidx/cardview/R$styleable;->CardView_contentPaddingLeft:I

    invoke-virtual {p2, v2, v0}, Landroid/content/res/TypedArray;->getDimensionPixelSize(II)I

    move-result v2

    iput v2, v1, Landroid/graphics/Rect;->left:I

    .line 214
    iget-object v1, p0, Landroidx/cardview/widget/CardView;->mContentPadding:Landroid/graphics/Rect;

    sget v2, Landroidx/cardview/R$styleable;->CardView_contentPaddingTop:I

    invoke-virtual {p2, v2, v0}, Landroid/content/res/TypedArray;->getDimensionPixelSize(II)I

    move-result v2

    iput v2, v1, Landroid/graphics/Rect;->top:I

    .line 216
    iget-object v1, p0, Landroidx/cardview/widget/CardView;->mContentPadding:Landroid/graphics/Rect;

    sget v2, Landroidx/cardview/R$styleable;->CardView_contentPaddingRight:I

    invoke-virtual {p2, v2, v0}, Landroid/content/res/TypedArray;->getDimensionPixelSize(II)I

    move-result v2

    iput v2, v1, Landroid/graphics/Rect;->right:I

    .line 218
    iget-object v1, p0, Landroidx/cardview/widget/CardView;->mContentPadding:Landroid/graphics/Rect;

    sget v2, Landroidx/cardview/R$styleable;->CardView_contentPaddingBottom:I

    invoke-virtual {p2, v2, v0}, Landroid/content/res/TypedArray;->getDimensionPixelSize(II)I

    move-result v0

    iput v0, v1, Landroid/graphics/Rect;->bottom:I

    cmpl-float v0, v6, p3

    if-lez v0, :cond_0

    move v7, v6

    goto :goto_0

    :cond_0
    move v7, p3

    .line 223
    :goto_0
    invoke-virtual {p2}, Landroid/content/res/TypedArray;->recycle()V

    .line 224
    sget-object v1, Landroidx/cardview/widget/CardView;->IMPL:Landroidx/cardview/widget/CardViewImpl;

    move-object v2, p0

    move-object v3, p1

    invoke-interface/range {v1 .. v7}, Landroidx/cardview/widget/CardViewImpl;->initialize(Landroidx/cardview/widget/CardViewDelegate;Landroid/content/Context;IFFF)V

    return-void
.end method


# virtual methods
.method public getCardElevation()F
    .locals 1

    .line 326
    sget-object v0, Landroidx/cardview/widget/CardView;->IMPL:Landroidx/cardview/widget/CardViewImpl;

    invoke-interface {v0, p0}, Landroidx/cardview/widget/CardViewImpl;->getElevation(Landroidx/cardview/widget/CardViewDelegate;)F

    move-result v0

    return v0
.end method

.method public getContentPaddingBottom()I
    .locals 1

    .line 270
    iget-object v0, p0, Landroidx/cardview/widget/CardView;->mContentPadding:Landroid/graphics/Rect;

    iget v0, v0, Landroid/graphics/Rect;->bottom:I

    return v0
.end method

.method public getContentPaddingLeft()I
    .locals 1

    .line 243
    iget-object v0, p0, Landroidx/cardview/widget/CardView;->mContentPadding:Landroid/graphics/Rect;

    iget v0, v0, Landroid/graphics/Rect;->left:I

    return v0
.end method

.method public getContentPaddingRight()I
    .locals 1

    .line 252
    iget-object v0, p0, Landroidx/cardview/widget/CardView;->mContentPadding:Landroid/graphics/Rect;

    iget v0, v0, Landroid/graphics/Rect;->right:I

    return v0
.end method

.method public getContentPaddingTop()I
    .locals 1

    .line 261
    iget-object v0, p0, Landroidx/cardview/widget/CardView;->mContentPadding:Landroid/graphics/Rect;

    iget v0, v0, Landroid/graphics/Rect;->top:I

    return v0
.end method

.method public getMaxCardElevation()F
    .locals 1

    .line 352
    sget-object v0, Landroidx/cardview/widget/CardView;->IMPL:Landroidx/cardview/widget/CardViewImpl;

    invoke-interface {v0, p0}, Landroidx/cardview/widget/CardViewImpl;->getMaxElevation(Landroidx/cardview/widget/CardViewDelegate;)F

    move-result v0

    return v0
.end method

.method public getPreventCornerOverlap()Z
    .locals 1

    .line 364
    iget-boolean v0, p0, Landroidx/cardview/widget/CardView;->mPreventCornerOverlap:Z

    return v0
.end method

.method public getRadius()F
    .locals 1

    .line 291
    sget-object v0, Landroidx/cardview/widget/CardView;->IMPL:Landroidx/cardview/widget/CardViewImpl;

    invoke-interface {v0, p0}, Landroidx/cardview/widget/CardViewImpl;->getRadius(Landroidx/cardview/widget/CardViewDelegate;)F

    move-result v0

    return v0
.end method

.method public getUseCompatPadding()Z
    .locals 1

    .line 126
    iget-boolean v0, p0, Landroidx/cardview/widget/CardView;->mCompatPadding:Z

    return v0
.end method

.method protected onMeasure(II)V
    .locals 5

    .line 176
    sget-object v0, Landroidx/cardview/widget/CardView;->IMPL:Landroidx/cardview/widget/CardViewImpl;

    instance-of v0, v0, Landroidx/cardview/widget/CardViewApi21;

    if-nez v0, :cond_2

    .line 177
    invoke-static {p1}, Landroid/view/View$MeasureSpec;->getMode(I)I

    move-result v0

    const/high16 v1, 0x40000000    # 2.0f

    const/high16 v2, -0x80000000

    if-eq v0, v2, :cond_0

    if-eq v0, v1, :cond_0

    goto :goto_0

    .line 181
    :cond_0
    sget-object v3, Landroidx/cardview/widget/CardView;->IMPL:Landroidx/cardview/widget/CardViewImpl;

    invoke-interface {v3, p0}, Landroidx/cardview/widget/CardViewImpl;->getMinWidth(Landroidx/cardview/widget/CardViewDelegate;)F

    move-result v3

    float-to-double v3, v3

    invoke-static {v3, v4}, Ljava/lang/Math;->ceil(D)D

    move-result-wide v3

    double-to-int v3, v3

    .line 182
    invoke-static {p1}, Landroid/view/View$MeasureSpec;->getSize(I)I

    move-result p1

    invoke-static {v3, p1}, Ljava/lang/Math;->max(II)I

    move-result p1

    invoke-static {p1, v0}, Landroid/view/View$MeasureSpec;->makeMeasureSpec(II)I

    move-result p1

    .line 187
    :goto_0
    invoke-static {p2}, Landroid/view/View$MeasureSpec;->getMode(I)I

    move-result v0

    if-eq v0, v2, :cond_1

    if-eq v0, v1, :cond_1

    goto :goto_1

    .line 191
    :cond_1
    sget-object v1, Landroidx/cardview/widget/CardView;->IMPL:Landroidx/cardview/widget/CardViewImpl;

    invoke-interface {v1, p0}, Landroidx/cardview/widget/CardViewImpl;->getMinHeight(Landroidx/cardview/widget/CardViewDelegate;)F

    move-result v1

    float-to-double v1, v1

    invoke-static {v1, v2}, Ljava/lang/Math;->ceil(D)D

    move-result-wide v1

    double-to-int v1, v1

    .line 192
    invoke-static {p2}, Landroid/view/View$MeasureSpec;->getSize(I)I

    move-result p2

    invoke-static {v1, p2}, Ljava/lang/Math;->max(II)I

    move-result p2

    invoke-static {p2, v0}, Landroid/view/View$MeasureSpec;->makeMeasureSpec(II)I

    move-result p2

    .line 196
    :goto_1
    invoke-super {p0, p1, p2}, Landroid/widget/FrameLayout;->onMeasure(II)V

    goto :goto_2

    .line 198
    :cond_2
    invoke-super {p0, p1, p2}, Landroid/widget/FrameLayout;->onMeasure(II)V

    :goto_2
    return-void
.end method

.method public setCardBackgroundColor(I)V
    .locals 1

    .line 234
    sget-object v0, Landroidx/cardview/widget/CardView;->IMPL:Landroidx/cardview/widget/CardViewImpl;

    invoke-interface {v0, p0, p1}, Landroidx/cardview/widget/CardViewImpl;->setBackgroundColor(Landroidx/cardview/widget/CardViewDelegate;I)V

    return-void
.end method

.method public setCardElevation(F)V
    .locals 1

    .line 315
    sget-object v0, Landroidx/cardview/widget/CardView;->IMPL:Landroidx/cardview/widget/CardViewImpl;

    invoke-interface {v0, p0, p1}, Landroidx/cardview/widget/CardViewImpl;->setElevation(Landroidx/cardview/widget/CardViewDelegate;F)V

    return-void
.end method

.method public setContentPadding(IIII)V
    .locals 1

    .line 170
    iget-object v0, p0, Landroidx/cardview/widget/CardView;->mContentPadding:Landroid/graphics/Rect;

    invoke-virtual {v0, p1, p2, p3, p4}, Landroid/graphics/Rect;->set(IIII)V

    .line 171
    sget-object p1, Landroidx/cardview/widget/CardView;->IMPL:Landroidx/cardview/widget/CardViewImpl;

    invoke-interface {p1, p0}, Landroidx/cardview/widget/CardViewImpl;->updatePadding(Landroidx/cardview/widget/CardViewDelegate;)V

    return-void
.end method

.method public setMaxCardElevation(F)V
    .locals 1

    .line 341
    sget-object v0, Landroidx/cardview/widget/CardView;->IMPL:Landroidx/cardview/widget/CardViewImpl;

    invoke-interface {v0, p0, p1}, Landroidx/cardview/widget/CardViewImpl;->setMaxElevation(Landroidx/cardview/widget/CardViewDelegate;F)V

    return-void
.end method

.method public setPadding(IIII)V
    .locals 0

    return-void
.end method

.method public setPaddingRelative(IIII)V
    .locals 0

    return-void
.end method

.method public setPreventCornerOverlap(Z)V
    .locals 1

    .line 381
    iget-boolean v0, p0, Landroidx/cardview/widget/CardView;->mPreventCornerOverlap:Z

    if-ne p1, v0, :cond_0

    return-void

    .line 384
    :cond_0
    iput-boolean p1, p0, Landroidx/cardview/widget/CardView;->mPreventCornerOverlap:Z

    .line 385
    sget-object p1, Landroidx/cardview/widget/CardView;->IMPL:Landroidx/cardview/widget/CardViewImpl;

    invoke-interface {p1, p0}, Landroidx/cardview/widget/CardViewImpl;->onPreventCornerOverlapChanged(Landroidx/cardview/widget/CardViewDelegate;)V

    return-void
.end method

.method public setRadius(F)V
    .locals 1

    .line 281
    sget-object v0, Landroidx/cardview/widget/CardView;->IMPL:Landroidx/cardview/widget/CardViewImpl;

    invoke-interface {v0, p0, p1}, Landroidx/cardview/widget/CardViewImpl;->setRadius(Landroidx/cardview/widget/CardViewDelegate;F)V

    return-void
.end method

.method public setShadowPadding(IIII)V
    .locals 1

    .line 301
    iget-object v0, p0, Landroidx/cardview/widget/CardView;->mShadowBounds:Landroid/graphics/Rect;

    invoke-virtual {v0, p1, p2, p3, p4}, Landroid/graphics/Rect;->set(IIII)V

    .line 302
    iget-object v0, p0, Landroidx/cardview/widget/CardView;->mContentPadding:Landroid/graphics/Rect;

    iget v0, v0, Landroid/graphics/Rect;->left:I

    add-int/2addr p1, v0

    iget-object v0, p0, Landroidx/cardview/widget/CardView;->mContentPadding:Landroid/graphics/Rect;

    iget v0, v0, Landroid/graphics/Rect;->top:I

    add-int/2addr p2, v0

    iget-object v0, p0, Landroidx/cardview/widget/CardView;->mContentPadding:Landroid/graphics/Rect;

    iget v0, v0, Landroid/graphics/Rect;->right:I

    add-int/2addr p3, v0

    iget-object v0, p0, Landroidx/cardview/widget/CardView;->mContentPadding:Landroid/graphics/Rect;

    iget v0, v0, Landroid/graphics/Rect;->bottom:I

    add-int/2addr p4, v0

    invoke-super {p0, p1, p2, p3, p4}, Landroid/widget/FrameLayout;->setPadding(IIII)V

    return-void
.end method

.method public setUseCompatPadding(Z)V
    .locals 1

    .line 146
    iget-boolean v0, p0, Landroidx/cardview/widget/CardView;->mCompatPadding:Z

    if-ne v0, p1, :cond_0

    return-void

    .line 149
    :cond_0
    iput-boolean p1, p0, Landroidx/cardview/widget/CardView;->mCompatPadding:Z

    .line 150
    sget-object p1, Landroidx/cardview/widget/CardView;->IMPL:Landroidx/cardview/widget/CardViewImpl;

    invoke-interface {p1, p0}, Landroidx/cardview/widget/CardViewImpl;->onCompatPaddingChanged(Landroidx/cardview/widget/CardViewDelegate;)V

    return-void
.end method
