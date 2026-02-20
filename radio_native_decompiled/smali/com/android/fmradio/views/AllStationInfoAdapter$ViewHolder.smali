.class Lcom/android/fmradio/views/AllStationInfoAdapter$ViewHolder;
.super Ljava/lang/Object;
.source "AllStationInfoAdapter.java"


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Lcom/android/fmradio/views/AllStationInfoAdapter;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0xa
    name = "ViewHolder"
.end annotation


# instance fields
.field mBtnDelete:Landroid/widget/ImageView;

.field mBtnEdit:Landroid/widget/ImageView;

.field mStationFreq:Landroid/widget/TextView;

.field mStationIndex:Landroid/widget/TextView;

.field mStationName:Landroid/widget/TextView;


# direct methods
.method private constructor <init>()V
    .locals 0

    .line 115
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

.method synthetic constructor <init>(Lcom/android/fmradio/views/AllStationInfoAdapter$1;)V
    .locals 0

    .line 115
    invoke-direct {p0}, Lcom/android/fmradio/views/AllStationInfoAdapter$ViewHolder;-><init>()V

    return-void
.end method
