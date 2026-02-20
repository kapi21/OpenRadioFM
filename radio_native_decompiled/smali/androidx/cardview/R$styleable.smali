.class public final Landroidx/cardview/R$styleable;
.super Ljava/lang/Object;
.source "R.java"


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Landroidx/cardview/R;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x19
    name = "styleable"
.end annotation


# static fields
.field public static final CardView:[I

.field public static CardView_cardBackgroundColor:I

.field public static CardView_cardCornerRadius:I

.field public static CardView_cardElevation:I

.field public static CardView_cardMaxElevation:I

.field public static CardView_cardPreventCornerOverlap:I

.field public static CardView_cardUseCompatPadding:I

.field public static CardView_contentPadding:I

.field public static CardView_contentPaddingBottom:I

.field public static CardView_contentPaddingLeft:I

.field public static CardView_contentPaddingRight:I

.field public static CardView_contentPaddingTop:I


# direct methods
.method static constructor <clinit>()V
    .locals 1

    const/16 v0, 0xb

    new-array v0, v0, [I

    .line 191
    fill-array-data v0, :array_0

    sput-object v0, Landroidx/cardview/R$styleable;->CardView:[I

    const/4 v0, 0x0

    .line 212
    sput v0, Landroidx/cardview/R$styleable;->CardView_cardBackgroundColor:I

    const/4 v0, 0x1

    .line 230
    sput v0, Landroidx/cardview/R$styleable;->CardView_cardCornerRadius:I

    const/4 v0, 0x2

    .line 248
    sput v0, Landroidx/cardview/R$styleable;->CardView_cardElevation:I

    const/4 v0, 0x3

    .line 266
    sput v0, Landroidx/cardview/R$styleable;->CardView_cardMaxElevation:I

    const/4 v0, 0x5

    .line 282
    sput v0, Landroidx/cardview/R$styleable;->CardView_cardPreventCornerOverlap:I

    const/4 v0, 0x4

    .line 298
    sput v0, Landroidx/cardview/R$styleable;->CardView_cardUseCompatPadding:I

    const/4 v0, 0x6

    .line 316
    sput v0, Landroidx/cardview/R$styleable;->CardView_contentPadding:I

    const/16 v0, 0xa

    .line 334
    sput v0, Landroidx/cardview/R$styleable;->CardView_contentPaddingBottom:I

    const/4 v0, 0x7

    .line 352
    sput v0, Landroidx/cardview/R$styleable;->CardView_contentPaddingLeft:I

    const/16 v0, 0x8

    .line 370
    sput v0, Landroidx/cardview/R$styleable;->CardView_contentPaddingRight:I

    const/16 v0, 0x9

    .line 388
    sput v0, Landroidx/cardview/R$styleable;->CardView_contentPaddingTop:I

    return-void

    :array_0
    .array-data 4
        0x7f010000
        0x7f010001
        0x7f010002
        0x7f010003
        0x7f010004
        0x7f010005
        0x7f010006
        0x7f010007
        0x7f010008
        0x7f010009
        0x7f01000a
    .end array-data
.end method

.method public constructor <init>()V
    .locals 0

    .line 160
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method
