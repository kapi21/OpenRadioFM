.class public Lcom/android/fmradio/info/FmStationInfo;
.super Ljava/lang/Object;
.source "FmStationInfo.java"


# instance fields
.field private mBand:I

.field private mFreq:I

.field private mStationName:Ljava/lang/String;


# direct methods
.method public constructor <init>()V
    .locals 1

    .line 14
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    const/4 v0, 0x0

    .line 15
    iput v0, p0, Lcom/android/fmradio/info/FmStationInfo;->mBand:I

    .line 16
    iput v0, p0, Lcom/android/fmradio/info/FmStationInfo;->mFreq:I

    const-string v0, ""

    .line 17
    iput-object v0, p0, Lcom/android/fmradio/info/FmStationInfo;->mStationName:Ljava/lang/String;

    return-void
.end method


# virtual methods
.method public getBand()I
    .locals 1

    .line 21
    iget v0, p0, Lcom/android/fmradio/info/FmStationInfo;->mBand:I

    return v0
.end method

.method public getFreq()I
    .locals 1

    .line 29
    iget v0, p0, Lcom/android/fmradio/info/FmStationInfo;->mFreq:I

    return v0
.end method

.method public getStationName()Ljava/lang/String;
    .locals 1

    .line 37
    iget-object v0, p0, Lcom/android/fmradio/info/FmStationInfo;->mStationName:Ljava/lang/String;

    return-object v0
.end method

.method public setBand(I)V
    .locals 0

    .line 25
    iput p1, p0, Lcom/android/fmradio/info/FmStationInfo;->mBand:I

    return-void
.end method

.method public setFreq(I)V
    .locals 0

    .line 33
    iput p1, p0, Lcom/android/fmradio/info/FmStationInfo;->mFreq:I

    return-void
.end method

.method public setStationName(Ljava/lang/String;)V
    .locals 0

    .line 41
    iput-object p1, p0, Lcom/android/fmradio/info/FmStationInfo;->mStationName:Ljava/lang/String;

    return-void
.end method

.method public toString()Ljava/lang/String;
    .locals 3

    .line 45
    new-instance v0, Ljava/lang/StringBuilder;

    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    const-string v1, "band: "

    .line 46
    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget v1, p0, Lcom/android/fmradio/info/FmStationInfo;->mBand:I

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v1, " - "

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    const-string v2, "freq: "

    .line 47
    invoke-virtual {v0, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget v2, p0, Lcom/android/fmradio/info/FmStationInfo;->mFreq:I

    invoke-virtual {v0, v2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    const-string v1, "stationName: "

    .line 48
    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-object v1, p0, Lcom/android/fmradio/info/FmStationInfo;->mStationName:Ljava/lang/String;

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 49
    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    return-object v0
.end method
