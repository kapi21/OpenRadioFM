.class public final Lcom/android/fmradio/views/FmVisualizerView;
.super Landroid/view/View;
.source "FmVisualizerView.java"


# static fields
.field private static final COLUME_COUNTS:I = 0x3

.field private static final COLUME_PADDING_COUNTS:I = 0x2

.field private static final DEFALT_VISUALIZER_LEVEL:[F


# instance fields
.field private mAnimate:Z

.field private mColumnPadding:F

.field private mFrequency:I

.field private final mHandler:Landroid/os/Handler;

.field private mPaint:Landroid/graphics/Paint;

.field private mPrevLevels:[F

.field private final mRefreashRunnable:Ljava/lang/Runnable;


# direct methods
.method static constructor <clinit>()V
    .locals 1

    const/4 v0, 0x3

    new-array v0, v0, [F

    .line 47
    fill-array-data v0, :array_0

    sput-object v0, Lcom/android/fmradio/views/FmVisualizerView;->DEFALT_VISUALIZER_LEVEL:[F

    return-void

    nop

    :array_0
    .array-data 4
        0x3ecccccd    # 0.4f
        0x3f800000    # 1.0f
        -0x41b33333    # -0.2f
    .end array-data
.end method

.method public constructor <init>(Landroid/content/Context;)V
    .locals 0

    .line 82
    invoke-direct {p0, p1}, Landroid/view/View;-><init>(Landroid/content/Context;)V

    .line 33
    new-instance p1, Landroid/os/Handler;

    invoke-direct {p1}, Landroid/os/Handler;-><init>()V

    iput-object p1, p0, Lcom/android/fmradio/views/FmVisualizerView;->mHandler:Landroid/os/Handler;

    .line 35
    new-instance p1, Landroid/graphics/Paint;

    invoke-direct {p1}, Landroid/graphics/Paint;-><init>()V

    iput-object p1, p0, Lcom/android/fmradio/views/FmVisualizerView;->mPaint:Landroid/graphics/Paint;

    const/high16 p1, 0x40400000    # 3.0f

    .line 37
    iput p1, p0, Lcom/android/fmradio/views/FmVisualizerView;->mColumnPadding:F

    const/4 p1, 0x0

    .line 39
    iput-boolean p1, p0, Lcom/android/fmradio/views/FmVisualizerView;->mAnimate:Z

    const/16 p1, 0x64

    .line 41
    iput p1, p0, Lcom/android/fmradio/views/FmVisualizerView;->mFrequency:I

    .line 51
    sget-object p1, Lcom/android/fmradio/views/FmVisualizerView;->DEFALT_VISUALIZER_LEVEL:[F

    iput-object p1, p0, Lcom/android/fmradio/views/FmVisualizerView;->mPrevLevels:[F

    .line 139
    new-instance p1, Lcom/android/fmradio/views/FmVisualizerView$1;

    invoke-direct {p1, p0}, Lcom/android/fmradio/views/FmVisualizerView$1;-><init>(Lcom/android/fmradio/views/FmVisualizerView;)V

    iput-object p1, p0, Lcom/android/fmradio/views/FmVisualizerView;->mRefreashRunnable:Ljava/lang/Runnable;

    .line 83
    invoke-direct {p0}, Lcom/android/fmradio/views/FmVisualizerView;->init()V

    return-void
.end method

.method public constructor <init>(Landroid/content/Context;Landroid/util/AttributeSet;)V
    .locals 0

    .line 72
    invoke-direct {p0, p1, p2}, Landroid/view/View;-><init>(Landroid/content/Context;Landroid/util/AttributeSet;)V

    .line 33
    new-instance p1, Landroid/os/Handler;

    invoke-direct {p1}, Landroid/os/Handler;-><init>()V

    iput-object p1, p0, Lcom/android/fmradio/views/FmVisualizerView;->mHandler:Landroid/os/Handler;

    .line 35
    new-instance p1, Landroid/graphics/Paint;

    invoke-direct {p1}, Landroid/graphics/Paint;-><init>()V

    iput-object p1, p0, Lcom/android/fmradio/views/FmVisualizerView;->mPaint:Landroid/graphics/Paint;

    const/high16 p1, 0x40400000    # 3.0f

    .line 37
    iput p1, p0, Lcom/android/fmradio/views/FmVisualizerView;->mColumnPadding:F

    const/4 p1, 0x0

    .line 39
    iput-boolean p1, p0, Lcom/android/fmradio/views/FmVisualizerView;->mAnimate:Z

    const/16 p1, 0x64

    .line 41
    iput p1, p0, Lcom/android/fmradio/views/FmVisualizerView;->mFrequency:I

    .line 51
    sget-object p1, Lcom/android/fmradio/views/FmVisualizerView;->DEFALT_VISUALIZER_LEVEL:[F

    iput-object p1, p0, Lcom/android/fmradio/views/FmVisualizerView;->mPrevLevels:[F

    .line 139
    new-instance p1, Lcom/android/fmradio/views/FmVisualizerView$1;

    invoke-direct {p1, p0}, Lcom/android/fmradio/views/FmVisualizerView$1;-><init>(Lcom/android/fmradio/views/FmVisualizerView;)V

    iput-object p1, p0, Lcom/android/fmradio/views/FmVisualizerView;->mRefreashRunnable:Ljava/lang/Runnable;

    .line 73
    invoke-direct {p0}, Lcom/android/fmradio/views/FmVisualizerView;->init()V

    return-void
.end method

.method public constructor <init>(Landroid/content/Context;Landroid/util/AttributeSet;I)V
    .locals 0

    .line 61
    invoke-direct {p0, p1, p2, p3}, Landroid/view/View;-><init>(Landroid/content/Context;Landroid/util/AttributeSet;I)V

    .line 33
    new-instance p1, Landroid/os/Handler;

    invoke-direct {p1}, Landroid/os/Handler;-><init>()V

    iput-object p1, p0, Lcom/android/fmradio/views/FmVisualizerView;->mHandler:Landroid/os/Handler;

    .line 35
    new-instance p1, Landroid/graphics/Paint;

    invoke-direct {p1}, Landroid/graphics/Paint;-><init>()V

    iput-object p1, p0, Lcom/android/fmradio/views/FmVisualizerView;->mPaint:Landroid/graphics/Paint;

    const/high16 p1, 0x40400000    # 3.0f

    .line 37
    iput p1, p0, Lcom/android/fmradio/views/FmVisualizerView;->mColumnPadding:F

    const/4 p1, 0x0

    .line 39
    iput-boolean p1, p0, Lcom/android/fmradio/views/FmVisualizerView;->mAnimate:Z

    const/16 p1, 0x64

    .line 41
    iput p1, p0, Lcom/android/fmradio/views/FmVisualizerView;->mFrequency:I

    .line 51
    sget-object p1, Lcom/android/fmradio/views/FmVisualizerView;->DEFALT_VISUALIZER_LEVEL:[F

    iput-object p1, p0, Lcom/android/fmradio/views/FmVisualizerView;->mPrevLevels:[F

    .line 139
    new-instance p1, Lcom/android/fmradio/views/FmVisualizerView$1;

    invoke-direct {p1, p0}, Lcom/android/fmradio/views/FmVisualizerView$1;-><init>(Lcom/android/fmradio/views/FmVisualizerView;)V

    iput-object p1, p0, Lcom/android/fmradio/views/FmVisualizerView;->mRefreashRunnable:Ljava/lang/Runnable;

    .line 62
    invoke-direct {p0}, Lcom/android/fmradio/views/FmVisualizerView;->init()V

    return-void
.end method

.method private generate(I)[F
    .locals 9

    if-gtz p1, :cond_0

    const/4 p1, 0x0

    return-object p1

    :cond_0
    const/4 v0, 0x2

    new-array v0, v0, [I

    .line 193
    fill-array-data v0, :array_0

    .line 196
    new-array v1, p1, [F

    const/4 v2, 0x0

    move v3, v2

    :goto_0
    if-ge v3, p1, :cond_4

    .line 199
    :cond_1
    invoke-static {}, Ljava/lang/Math;->random()D

    move-result-wide v4

    double-to-float v4, v4

    const/high16 v5, 0x3f800000    # 1.0f

    mul-float/2addr v4, v5

    .line 200
    invoke-static {}, Ljava/lang/Math;->random()D

    move-result-wide v5

    const-wide/high16 v7, 0x4000000000000000L    # 2.0

    mul-double/2addr v5, v7

    double-to-int v5, v5

    aget v5, v0, v5

    int-to-float v5, v5

    mul-float/2addr v4, v5

    aput v4, v1, v3

    .line 201
    iget-object v4, p0, Lcom/android/fmradio/views/FmVisualizerView;->mPrevLevels:[F

    aget v4, v4, v3

    aget v5, v1, v3

    sub-float/2addr v4, v5

    invoke-static {v4}, Ljava/lang/Math;->abs(F)F

    move-result v4

    const v5, 0x3e99999a    # 0.3f

    cmpg-float v4, v4, v5

    const/4 v5, 0x1

    if-gez v4, :cond_2

    move v4, v5

    goto :goto_1

    :cond_2
    move v4, v2

    :goto_1
    aget v6, v1, v3

    const v7, -0x41666666    # -0.3f

    cmpl-float v6, v6, v7

    if-lez v6, :cond_3

    goto :goto_2

    :cond_3
    move v5, v2

    :goto_2
    and-int/2addr v4, v5

    if-eqz v4, :cond_1

    add-int/lit8 v3, v3, 0x1

    goto :goto_0

    .line 206
    :cond_4
    iput-object v1, p0, Lcom/android/fmradio/views/FmVisualizerView;->mPrevLevels:[F

    return-object v1

    :array_0
    .array-data 4
        -0x1
        0x1
    .end array-data
.end method

.method private init()V
    .locals 2

    .line 87
    iget-object v0, p0, Lcom/android/fmradio/views/FmVisualizerView;->mPaint:Landroid/graphics/Paint;

    const v1, -0x9f8275

    invoke-virtual {v0, v1}, Landroid/graphics/Paint;->setColor(I)V

    .line 88
    iget-object v0, p0, Lcom/android/fmradio/views/FmVisualizerView;->mPaint:Landroid/graphics/Paint;

    const/4 v1, 0x1

    invoke-virtual {v0, v1}, Landroid/graphics/Paint;->setAntiAlias(Z)V

    .line 89
    iget-object v0, p0, Lcom/android/fmradio/views/FmVisualizerView;->mPaint:Landroid/graphics/Paint;

    const v1, 0x3e99999a    # 0.3f

    invoke-virtual {v0, v1}, Landroid/graphics/Paint;->setStrokeWidth(F)V

    .line 90
    iget-object v0, p0, Lcom/android/fmradio/views/FmVisualizerView;->mPaint:Landroid/graphics/Paint;

    sget-object v1, Landroid/graphics/Paint$Cap;->ROUND:Landroid/graphics/Paint$Cap;

    invoke-virtual {v0, v1}, Landroid/graphics/Paint;->setStrokeCap(Landroid/graphics/Paint$Cap;)V

    .line 91
    iget-object v0, p0, Lcom/android/fmradio/views/FmVisualizerView;->mPaint:Landroid/graphics/Paint;

    sget-object v1, Landroid/graphics/Paint$Style;->FILL_AND_STROKE:Landroid/graphics/Paint$Style;

    invoke-virtual {v0, v1}, Landroid/graphics/Paint;->setStyle(Landroid/graphics/Paint$Style;)V

    const/4 v0, 0x0

    .line 92
    iput-boolean v0, p0, Lcom/android/fmradio/views/FmVisualizerView;->mAnimate:Z

    return-void
.end method


# virtual methods
.method public isAnimated()Z
    .locals 1

    .line 124
    iget-boolean v0, p0, Lcom/android/fmradio/views/FmVisualizerView;->mAnimate:Z

    return v0
.end method

.method protected onDraw(Landroid/graphics/Canvas;)V
    .locals 17

    move-object/from16 v0, p0

    move-object/from16 v1, p1

    .line 147
    invoke-super/range {p0 .. p1}, Landroid/view/View;->onDraw(Landroid/graphics/Canvas;)V

    .line 148
    invoke-virtual/range {p1 .. p1}, Landroid/graphics/Canvas;->save()I

    const/4 v2, 0x0

    .line 149
    invoke-virtual {v1, v2}, Landroid/graphics/Canvas;->drawColor(I)V

    .line 150
    invoke-virtual/range {p0 .. p0}, Lcom/android/fmradio/views/FmVisualizerView;->getHeight()I

    move-result v3

    .line 151
    invoke-virtual/range {p0 .. p0}, Lcom/android/fmradio/views/FmVisualizerView;->getWidth()I

    move-result v4

    .line 152
    invoke-virtual/range {p0 .. p0}, Lcom/android/fmradio/views/FmVisualizerView;->getPaddingLeft()I

    move-result v5

    .line 153
    invoke-virtual/range {p0 .. p0}, Lcom/android/fmradio/views/FmVisualizerView;->getPaddingRight()I

    move-result v6

    .line 154
    invoke-virtual/range {p0 .. p0}, Lcom/android/fmradio/views/FmVisualizerView;->getPaddingTop()I

    move-result v7

    .line 155
    invoke-virtual/range {p0 .. p0}, Lcom/android/fmradio/views/FmVisualizerView;->getPaddingBottom()I

    move-result v8

    sub-int/2addr v4, v5

    sub-int/2addr v4, v6

    int-to-float v4, v4

    .line 156
    iget v6, v0, Lcom/android/fmradio/views/FmVisualizerView;->mColumnPadding:F

    const/high16 v9, 0x40000000    # 2.0f

    mul-float/2addr v6, v9

    sub-float/2addr v4, v6

    const/high16 v6, 0x40400000    # 3.0f

    div-float/2addr v4, v6

    sub-int/2addr v3, v8

    sub-int v6, v3, v7

    int-to-float v6, v6

    .line 162
    iget-boolean v8, v0, Lcom/android/fmradio/views/FmVisualizerView;->mAnimate:Z

    const/4 v10, 0x3

    if-nez v8, :cond_0

    .line 163
    sget-object v8, Lcom/android/fmradio/views/FmVisualizerView;->DEFALT_VISUALIZER_LEVEL:[F

    goto :goto_0

    .line 165
    :cond_0
    invoke-direct {v0, v10}, Lcom/android/fmradio/views/FmVisualizerView;->generate(I)[F

    move-result-object v8

    :goto_0
    if-ge v2, v10, :cond_2

    int-to-float v11, v5

    int-to-float v12, v2

    .line 168
    iget v13, v0, Lcom/android/fmradio/views/FmVisualizerView;->mColumnPadding:F

    add-float/2addr v13, v4

    mul-float/2addr v12, v13

    add-float/2addr v11, v12

    add-float v12, v11, v4

    int-to-float v13, v7

    div-float v14, v6, v9

    add-float v15, v13, v14

    .line 171
    aget v16, v8, v2

    mul-float v14, v14, v16

    sub-float v14, v15, v14

    cmpg-float v15, v14, v13

    if-gez v15, :cond_1

    move v14, v13

    :cond_1
    int-to-float v13, v3

    .line 176
    new-instance v15, Landroid/graphics/RectF;

    invoke-direct {v15, v11, v14, v12, v13}, Landroid/graphics/RectF;-><init>(FFFF)V

    .line 177
    iget-object v11, v0, Lcom/android/fmradio/views/FmVisualizerView;->mPaint:Landroid/graphics/Paint;

    invoke-virtual {v1, v15, v11}, Landroid/graphics/Canvas;->drawRect(Landroid/graphics/RectF;Landroid/graphics/Paint;)V

    add-int/lit8 v2, v2, 0x1

    goto :goto_0

    .line 179
    :cond_2
    iget-object v1, v0, Lcom/android/fmradio/views/FmVisualizerView;->mHandler:Landroid/os/Handler;

    iget-object v2, v0, Lcom/android/fmradio/views/FmVisualizerView;->mRefreashRunnable:Ljava/lang/Runnable;

    invoke-virtual {v1, v2}, Landroid/os/Handler;->removeCallbacks(Ljava/lang/Runnable;)V

    .line 180
    iget-object v1, v0, Lcom/android/fmradio/views/FmVisualizerView;->mHandler:Landroid/os/Handler;

    iget-object v2, v0, Lcom/android/fmradio/views/FmVisualizerView;->mRefreashRunnable:Ljava/lang/Runnable;

    iget v3, v0, Lcom/android/fmradio/views/FmVisualizerView;->mFrequency:I

    int-to-long v3, v3

    invoke-virtual {v1, v2, v3, v4}, Landroid/os/Handler;->postDelayed(Ljava/lang/Runnable;J)Z

    return-void
.end method

.method public setAnimateFrequency(I)V
    .locals 0

    .line 133
    iput p1, p0, Lcom/android/fmradio/views/FmVisualizerView;->mFrequency:I

    return-void
.end method

.method public setColumnPadding(I)V
    .locals 0

    int-to-float p1, p1

    .line 101
    iput p1, p0, Lcom/android/fmradio/views/FmVisualizerView;->mColumnPadding:F

    return-void
.end method

.method public startAnimation()V
    .locals 1

    const/4 v0, 0x1

    .line 108
    iput-boolean v0, p0, Lcom/android/fmradio/views/FmVisualizerView;->mAnimate:Z

    return-void
.end method

.method public stopAnimation()V
    .locals 1

    const/4 v0, 0x0

    .line 115
    iput-boolean v0, p0, Lcom/android/fmradio/views/FmVisualizerView;->mAnimate:Z

    return-void
.end method
