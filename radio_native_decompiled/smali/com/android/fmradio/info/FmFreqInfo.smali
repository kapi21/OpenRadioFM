.class public Lcom/android/fmradio/info/FmFreqInfo;
.super Ljava/lang/Object;
.source "FmFreqInfo.java"


# instance fields
.field private mBand:I

.field private mFreq:I

.field private mFreqIndex:I

.field private mIsFavorite:Z


# direct methods
.method public constructor <init>()V
    .locals 2

    .line 9
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    const/4 v0, 0x0

    .line 10
    iput v0, p0, Lcom/android/fmradio/info/FmFreqInfo;->mBand:I

    const/4 v1, -0x1

    .line 11
    iput v1, p0, Lcom/android/fmradio/info/FmFreqInfo;->mFreqIndex:I

    .line 12
    iput v0, p0, Lcom/android/fmradio/info/FmFreqInfo;->mFreq:I

    .line 13
    iput-boolean v0, p0, Lcom/android/fmradio/info/FmFreqInfo;->mIsFavorite:Z

    return-void
.end method


# virtual methods
.method public getBand()I
    .locals 1

    .line 17
    iget v0, p0, Lcom/android/fmradio/info/FmFreqInfo;->mBand:I

    return v0
.end method

.method public getFreq()I
    .locals 1

    .line 33
    iget v0, p0, Lcom/android/fmradio/info/FmFreqInfo;->mFreq:I

    return v0
.end method

.method public getFreqIndex()I
    .locals 1

    .line 25
    iget v0, p0, Lcom/android/fmradio/info/FmFreqInfo;->mFreqIndex:I

    return v0
.end method

.method public isFavorite()Z
    .locals 1

    .line 41
    iget-boolean v0, p0, Lcom/android/fmradio/info/FmFreqInfo;->mIsFavorite:Z

    return v0
.end method

.method public setBand(I)V
    .locals 0

    .line 21
    iput p1, p0, Lcom/android/fmradio/info/FmFreqInfo;->mBand:I

    return-void
.end method

.method public setFavorite(Z)V
    .locals 0

    .line 45
    iput-boolean p1, p0, Lcom/android/fmradio/info/FmFreqInfo;->mIsFavorite:Z

    return-void
.end method

.method public setFreq(I)V
    .locals 0

    .line 37
    iput p1, p0, Lcom/android/fmradio/info/FmFreqInfo;->mFreq:I

    return-void
.end method

.method public setFreqIndex(I)V
    .locals 0

    .line 29
    iput p1, p0, Lcom/android/fmradio/info/FmFreqInfo;->mFreqIndex:I

    return-void
.end method

.method public toString()Ljava/lang/String;
    .locals 3

    .line 49
    new-instance v0, Ljava/lang/StringBuilder;

    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    const-string v1, "band: "

    .line 50
    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget v1, p0, Lcom/android/fmradio/info/FmFreqInfo;->mBand:I

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v1, " - "

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    const-string v2, "mFreqIndex: "

    .line 51
    invoke-virtual {v0, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget v2, p0, Lcom/android/fmradio/info/FmFreqInfo;->mFreqIndex:I

    invoke-virtual {v0, v2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    const-string v1, "mFreq: "

    .line 52
    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget v1, p0, Lcom/android/fmradio/info/FmFreqInfo;->mFreq:I

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v1, "mIsFavorite: "

    .line 53
    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-boolean v1, p0, Lcom/android/fmradio/info/FmFreqInfo;->mIsFavorite:Z

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    .line 54
    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    return-object v0
.end method
