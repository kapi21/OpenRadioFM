.class public Lcom/android/fmradio/FmManagerSelect;
.super Ljava/lang/Object;
.source "FmManagerSelect.java"


# static fields
.field private static final TAG:Ljava/lang/String;


# instance fields
.field private final mContext:Landroid/content/Context;

.field private mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;


# direct methods
.method static constructor <clinit>()V
    .locals 1

    .line 12
    const-class v0, Lcom/android/fmradio/FmManagerSelect;

    invoke-virtual {v0}, Ljava/lang/Class;->getSimpleName()Ljava/lang/String;

    move-result-object v0

    sput-object v0, Lcom/android/fmradio/FmManagerSelect;->TAG:Ljava/lang/String;

    return-void
.end method

.method public constructor <init>(Landroid/content/Context;)V
    .locals 1

    .line 18
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 19
    iput-object p1, p0, Lcom/android/fmradio/FmManagerSelect;->mContext:Landroid/content/Context;

    .line 20
    sget-object p1, Lcom/android/fmradio/FmManagerSelect;->TAG:Ljava/lang/String;

    const-string v0, "FmManagerSelect start"

    invoke-static {p1, v0}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 22
    invoke-static {}, Lcom/android/fmradio/TunerManagerForExt;->getInstance()Lcom/android/fmradio/TunerManagerForExt;

    move-result-object p1

    iput-object p1, p0, Lcom/android/fmradio/FmManagerSelect;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    .line 23
    iget-object p1, p0, Lcom/android/fmradio/FmManagerSelect;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    iget-object v0, p0, Lcom/android/fmradio/FmManagerSelect;->mContext:Landroid/content/Context;

    invoke-virtual {p1, v0}, Lcom/android/fmradio/TunerManagerForExt;->setContext(Landroid/content/Context;)V

    return-void
.end method


# virtual methods
.method public autoScan(I)[I
    .locals 0

    .line 47
    iget-object p1, p0, Lcom/android/fmradio/FmManagerSelect;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    invoke-virtual {p1}, Lcom/android/fmradio/TunerManagerForExt;->autoScan()[I

    move-result-object p1

    return-object p1
.end method

.method public closeDev()Z
    .locals 1

    const/4 v0, 0x1

    return v0
.end method

.method public getFreq()I
    .locals 1

    .line 60
    iget-object v0, p0, Lcom/android/fmradio/FmManagerSelect;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    invoke-virtual {v0}, Lcom/android/fmradio/TunerManagerForExt;->getFrequency()I

    move-result v0

    return v0
.end method

.method public isRdsSupported()I
    .locals 1

    .line 89
    iget-object v0, p0, Lcom/android/fmradio/FmManagerSelect;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    invoke-virtual {v0}, Lcom/android/fmradio/TunerManagerForExt;->isRdsSupport()I

    move-result v0

    return v0
.end method

.method public openDev()Z
    .locals 1

    const/4 v0, 0x1

    return v0
.end method

.method public powerDown()Z
    .locals 1

    .line 31
    iget-object v0, p0, Lcom/android/fmradio/FmManagerSelect;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    invoke-virtual {v0}, Lcom/android/fmradio/TunerManagerForExt;->powerDown()Z

    move-result v0

    return v0
.end method

.method public powerUp(F)Z
    .locals 0

    .line 27
    iget-object p1, p0, Lcom/android/fmradio/FmManagerSelect;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    invoke-virtual {p1}, Lcom/android/fmradio/TunerManagerForExt;->powerUp()Z

    move-result p1

    return p1
.end method

.method public seekStation(FZ)F
    .locals 0

    .line 51
    iget-object p1, p0, Lcom/android/fmradio/FmManagerSelect;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    invoke-virtual {p1, p2}, Lcom/android/fmradio/TunerManagerForExt;->onSeek(Z)F

    move-result p1

    return p1
.end method

.method public setAudioPathEnable(Lcom/android/fmradio/FmConstants$AudioPath;Z)Z
    .locals 0

    const/4 p1, 0x1

    return p1
.end method

.method public setMute(Z)I
    .locals 1

    .line 68
    iget-object v0, p0, Lcom/android/fmradio/FmManagerSelect;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    invoke-virtual {v0, p1}, Lcom/android/fmradio/TunerManagerForExt;->setMute(Z)I

    move-result p1

    return p1
.end method

.method public setRdsMode(ZZ)I
    .locals 0

    .line 85
    iget-object p2, p0, Lcom/android/fmradio/FmManagerSelect;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    invoke-virtual {p2, p1}, Lcom/android/fmradio/TunerManagerForExt;->setRds(Z)I

    move-result p1

    return p1
.end method

.method public setSpeakerEnable(Lcom/android/fmradio/FmConstants$AudioPath;Z)Z
    .locals 0

    const/4 p1, 0x1

    return p1
.end method

.method public setVolume(I)Z
    .locals 1

    .line 64
    iget-object v0, p0, Lcom/android/fmradio/FmManagerSelect;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    invoke-virtual {v0, p1}, Lcom/android/fmradio/TunerManagerForExt;->setVolume(I)Z

    move-result p1

    return p1
.end method

.method public stopScan()Z
    .locals 1

    .line 43
    iget-object v0, p0, Lcom/android/fmradio/FmManagerSelect;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    invoke-virtual {v0}, Lcom/android/fmradio/TunerManagerForExt;->stopScan()Z

    move-result v0

    return v0
.end method

.method public tuneRadio(F)Z
    .locals 3

    .line 55
    sget-object v0, Lcom/android/fmradio/FmManagerSelect;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "frequency: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(F)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 56
    iget-object v0, p0, Lcom/android/fmradio/FmManagerSelect;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    invoke-virtual {v0, p1}, Lcom/android/fmradio/TunerManagerForExt;->onTune(F)Z

    move-result p1

    return p1
.end method
