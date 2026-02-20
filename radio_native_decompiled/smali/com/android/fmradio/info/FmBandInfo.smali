.class public Lcom/android/fmradio/info/FmBandInfo;
.super Ljava/lang/Object;
.source "FmBandInfo.java"


# instance fields
.field private mBand:I

.field private mBandFreq:I

.field private mBandPreset:I


# direct methods
.method public constructor <init>()V
    .locals 1

    .line 13
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    const/4 v0, 0x0

    .line 14
    iput v0, p0, Lcom/android/fmradio/info/FmBandInfo;->mBand:I

    .line 15
    iput v0, p0, Lcom/android/fmradio/info/FmBandInfo;->mBandPreset:I

    .line 16
    iput v0, p0, Lcom/android/fmradio/info/FmBandInfo;->mBandFreq:I

    return-void
.end method


# virtual methods
.method public getBand()I
    .locals 1

    .line 20
    iget v0, p0, Lcom/android/fmradio/info/FmBandInfo;->mBand:I

    return v0
.end method

.method public getBandFreq()I
    .locals 1

    .line 36
    iget v0, p0, Lcom/android/fmradio/info/FmBandInfo;->mBandFreq:I

    return v0
.end method

.method public getBandPreset()I
    .locals 1

    .line 28
    iget v0, p0, Lcom/android/fmradio/info/FmBandInfo;->mBandPreset:I

    return v0
.end method

.method public setBand(I)V
    .locals 0

    .line 24
    iput p1, p0, Lcom/android/fmradio/info/FmBandInfo;->mBand:I

    return-void
.end method

.method public setBandFreq(I)V
    .locals 0

    .line 40
    iput p1, p0, Lcom/android/fmradio/info/FmBandInfo;->mBandFreq:I

    return-void
.end method

.method public setBandPreset(I)V
    .locals 0

    .line 32
    iput p1, p0, Lcom/android/fmradio/info/FmBandInfo;->mBandPreset:I

    return-void
.end method
