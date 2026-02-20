.class Landroidx/cardview/widget/CardViewJellybeanMr1;
.super Landroidx/cardview/widget/CardViewEclairMr1;
.source "CardViewJellybeanMr1.java"


# direct methods
.method constructor <init>()V
    .locals 0

    .line 22
    invoke-direct {p0}, Landroidx/cardview/widget/CardViewEclairMr1;-><init>()V

    return-void
.end method


# virtual methods
.method public initStatic()V
    .locals 1

    .line 26
    new-instance v0, Landroidx/cardview/widget/CardViewJellybeanMr1$1;

    invoke-direct {v0, p0}, Landroidx/cardview/widget/CardViewJellybeanMr1$1;-><init>(Landroidx/cardview/widget/CardViewJellybeanMr1;)V

    sput-object v0, Landroidx/cardview/widget/RoundRectDrawableWithShadow;->sRoundRectHelper:Landroidx/cardview/widget/RoundRectDrawableWithShadow$RoundRectHelper;

    return-void
.end method
