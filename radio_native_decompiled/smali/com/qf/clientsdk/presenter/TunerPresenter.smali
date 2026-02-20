.class public Lcom/qf/clientsdk/presenter/TunerPresenter;
.super Ljava/lang/Object;
.source "TunerPresenter.java"


# annotations
.annotation system Ldalvik/annotation/Signature;
    value = {
        "<T:",
        "Ljava/lang/Object;",
        ">",
        "Ljava/lang/Object;"
    }
.end annotation


# static fields
.field private static final DEBUG:Z = true

.field private static final TAG:Ljava/lang/String;


# instance fields
.field toolList:Ljava/util/concurrent/CopyOnWriteArrayList;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Ljava/util/concurrent/CopyOnWriteArrayList<",
            "TT;>;"
        }
    .end annotation
.end field

.field private volatile tunerScanning:Z


# direct methods
.method static constructor <clinit>()V
    .locals 1

    .line 15
    const-class v0, Lcom/qf/clientsdk/presenter/TunerPresenter;

    invoke-virtual {v0}, Ljava/lang/Class;->getSimpleName()Ljava/lang/String;

    move-result-object v0

    sput-object v0, Lcom/qf/clientsdk/presenter/TunerPresenter;->TAG:Ljava/lang/String;

    return-void
.end method

.method public constructor <init>(Ljava/util/concurrent/CopyOnWriteArrayList;)V
    .locals 1
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Ljava/util/concurrent/CopyOnWriteArrayList<",
            "TT;>;)V"
        }
    .end annotation

    .line 23
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    const/4 v0, 0x0

    .line 24
    iput-boolean v0, p0, Lcom/qf/clientsdk/presenter/TunerPresenter;->tunerScanning:Z

    .line 25
    iput-object p1, p0, Lcom/qf/clientsdk/presenter/TunerPresenter;->toolList:Ljava/util/concurrent/CopyOnWriteArrayList;

    return-void
.end method

.method private handleFreqRangInfo([B)V
    .locals 3

    .line 92
    iget-object v0, p0, Lcom/qf/clientsdk/presenter/TunerPresenter;->toolList:Ljava/util/concurrent/CopyOnWriteArrayList;

    if-eqz v0, :cond_1

    invoke-virtual {v0}, Ljava/util/concurrent/CopyOnWriteArrayList;->size()I

    move-result v0

    if-lez v0, :cond_1

    .line 93
    iget-object v0, p0, Lcom/qf/clientsdk/presenter/TunerPresenter;->toolList:Ljava/util/concurrent/CopyOnWriteArrayList;

    invoke-virtual {v0}, Ljava/util/concurrent/CopyOnWriteArrayList;->iterator()Ljava/util/Iterator;

    move-result-object v0

    :cond_0
    :goto_0
    invoke-interface {v0}, Ljava/util/Iterator;->hasNext()Z

    move-result v1

    if-eqz v1, :cond_1

    invoke-interface {v0}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v1

    .line 94
    instance-of v2, v1, Lcom/qf/clientsdk/listeners/ITunerTool;

    if-eqz v2, :cond_0

    .line 95
    check-cast v1, Lcom/qf/clientsdk/listeners/ITunerTool;

    .line 96
    invoke-interface {v1, p1}, Lcom/qf/clientsdk/listeners/ITunerTool;->onTunerRangInfoChanged([B)V

    goto :goto_0

    :cond_1
    return-void
.end method

.method private handlePresetListInfo([B)V
    .locals 3

    .line 81
    iget-object v0, p0, Lcom/qf/clientsdk/presenter/TunerPresenter;->toolList:Ljava/util/concurrent/CopyOnWriteArrayList;

    if-eqz v0, :cond_1

    invoke-virtual {v0}, Ljava/util/concurrent/CopyOnWriteArrayList;->size()I

    move-result v0

    if-lez v0, :cond_1

    .line 82
    iget-object v0, p0, Lcom/qf/clientsdk/presenter/TunerPresenter;->toolList:Ljava/util/concurrent/CopyOnWriteArrayList;

    invoke-virtual {v0}, Ljava/util/concurrent/CopyOnWriteArrayList;->iterator()Ljava/util/Iterator;

    move-result-object v0

    :cond_0
    :goto_0
    invoke-interface {v0}, Ljava/util/Iterator;->hasNext()Z

    move-result v1

    if-eqz v1, :cond_1

    invoke-interface {v0}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v1

    .line 83
    instance-of v2, v1, Lcom/qf/clientsdk/listeners/ITunerTool;

    if-eqz v2, :cond_0

    .line 84
    check-cast v1, Lcom/qf/clientsdk/listeners/ITunerTool;

    .line 85
    invoke-interface {v1, p1}, Lcom/qf/clientsdk/listeners/ITunerTool;->onTunerPresetListChanged([B)V

    goto :goto_0

    :cond_1
    return-void
.end method

.method private handleRdsIndicateInfo([B)V
    .locals 3

    .line 114
    iget-object v0, p0, Lcom/qf/clientsdk/presenter/TunerPresenter;->toolList:Ljava/util/concurrent/CopyOnWriteArrayList;

    if-eqz v0, :cond_1

    invoke-virtual {v0}, Ljava/util/concurrent/CopyOnWriteArrayList;->size()I

    move-result v0

    if-lez v0, :cond_1

    .line 115
    iget-object v0, p0, Lcom/qf/clientsdk/presenter/TunerPresenter;->toolList:Ljava/util/concurrent/CopyOnWriteArrayList;

    invoke-virtual {v0}, Ljava/util/concurrent/CopyOnWriteArrayList;->iterator()Ljava/util/Iterator;

    move-result-object v0

    :cond_0
    :goto_0
    invoke-interface {v0}, Ljava/util/Iterator;->hasNext()Z

    move-result v1

    if-eqz v1, :cond_1

    invoke-interface {v0}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v1

    .line 116
    instance-of v2, v1, Lcom/qf/clientsdk/listeners/ITunerTool;

    if-eqz v2, :cond_0

    .line 117
    check-cast v1, Lcom/qf/clientsdk/listeners/ITunerTool;

    .line 118
    invoke-interface {v1, p1}, Lcom/qf/clientsdk/listeners/ITunerTool;->onTuneRdsIndicateInfo([B)V

    goto :goto_0

    :cond_1
    return-void
.end method

.method private handleRdsInfo([B)V
    .locals 3

    .line 103
    iget-object v0, p0, Lcom/qf/clientsdk/presenter/TunerPresenter;->toolList:Ljava/util/concurrent/CopyOnWriteArrayList;

    if-eqz v0, :cond_1

    invoke-virtual {v0}, Ljava/util/concurrent/CopyOnWriteArrayList;->size()I

    move-result v0

    if-lez v0, :cond_1

    .line 104
    iget-object v0, p0, Lcom/qf/clientsdk/presenter/TunerPresenter;->toolList:Ljava/util/concurrent/CopyOnWriteArrayList;

    invoke-virtual {v0}, Ljava/util/concurrent/CopyOnWriteArrayList;->iterator()Ljava/util/Iterator;

    move-result-object v0

    :cond_0
    :goto_0
    invoke-interface {v0}, Ljava/util/Iterator;->hasNext()Z

    move-result v1

    if-eqz v1, :cond_1

    invoke-interface {v0}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v1

    .line 105
    instance-of v2, v1, Lcom/qf/clientsdk/listeners/ITunerTool;

    if-eqz v2, :cond_0

    .line 106
    check-cast v1, Lcom/qf/clientsdk/listeners/ITunerTool;

    .line 107
    invoke-interface {v1, p1}, Lcom/qf/clientsdk/listeners/ITunerTool;->onTuneRdsInfo([B)V

    goto :goto_0

    :cond_1
    return-void
.end method

.method private handleRdsPSInfo([B)V
    .locals 3

    .line 136
    iget-object v0, p0, Lcom/qf/clientsdk/presenter/TunerPresenter;->toolList:Ljava/util/concurrent/CopyOnWriteArrayList;

    if-eqz v0, :cond_1

    invoke-virtual {v0}, Ljava/util/concurrent/CopyOnWriteArrayList;->size()I

    move-result v0

    if-lez v0, :cond_1

    .line 137
    iget-object v0, p0, Lcom/qf/clientsdk/presenter/TunerPresenter;->toolList:Ljava/util/concurrent/CopyOnWriteArrayList;

    invoke-virtual {v0}, Ljava/util/concurrent/CopyOnWriteArrayList;->iterator()Ljava/util/Iterator;

    move-result-object v0

    :cond_0
    :goto_0
    invoke-interface {v0}, Ljava/util/Iterator;->hasNext()Z

    move-result v1

    if-eqz v1, :cond_1

    invoke-interface {v0}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v1

    .line 138
    instance-of v2, v1, Lcom/qf/clientsdk/listeners/ITunerTool;

    if-eqz v2, :cond_0

    .line 139
    check-cast v1, Lcom/qf/clientsdk/listeners/ITunerTool;

    .line 140
    invoke-interface {v1, p1}, Lcom/qf/clientsdk/listeners/ITunerTool;->onTuneRdsPSInfo([B)V

    goto :goto_0

    :cond_1
    return-void
.end method

.method private handleRdsPSPresetListInfo([B)V
    .locals 3

    .line 158
    iget-object v0, p0, Lcom/qf/clientsdk/presenter/TunerPresenter;->toolList:Ljava/util/concurrent/CopyOnWriteArrayList;

    if-eqz v0, :cond_1

    invoke-virtual {v0}, Ljava/util/concurrent/CopyOnWriteArrayList;->size()I

    move-result v0

    if-lez v0, :cond_1

    .line 159
    iget-object v0, p0, Lcom/qf/clientsdk/presenter/TunerPresenter;->toolList:Ljava/util/concurrent/CopyOnWriteArrayList;

    invoke-virtual {v0}, Ljava/util/concurrent/CopyOnWriteArrayList;->iterator()Ljava/util/Iterator;

    move-result-object v0

    :cond_0
    :goto_0
    invoke-interface {v0}, Ljava/util/Iterator;->hasNext()Z

    move-result v1

    if-eqz v1, :cond_1

    invoke-interface {v0}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v1

    .line 160
    instance-of v2, v1, Lcom/qf/clientsdk/listeners/ITunerTool;

    if-eqz v2, :cond_0

    .line 161
    check-cast v1, Lcom/qf/clientsdk/listeners/ITunerTool;

    .line 162
    invoke-interface {v1, p1}, Lcom/qf/clientsdk/listeners/ITunerTool;->onTunerRdsPSPresetListInfo([B)V

    goto :goto_0

    :cond_1
    return-void
.end method

.method private handleRdsPtyTypeInfo([B)V
    .locals 3

    .line 125
    iget-object v0, p0, Lcom/qf/clientsdk/presenter/TunerPresenter;->toolList:Ljava/util/concurrent/CopyOnWriteArrayList;

    if-eqz v0, :cond_1

    invoke-virtual {v0}, Ljava/util/concurrent/CopyOnWriteArrayList;->size()I

    move-result v0

    if-lez v0, :cond_1

    .line 126
    iget-object v0, p0, Lcom/qf/clientsdk/presenter/TunerPresenter;->toolList:Ljava/util/concurrent/CopyOnWriteArrayList;

    invoke-virtual {v0}, Ljava/util/concurrent/CopyOnWriteArrayList;->iterator()Ljava/util/Iterator;

    move-result-object v0

    :cond_0
    :goto_0
    invoke-interface {v0}, Ljava/util/Iterator;->hasNext()Z

    move-result v1

    if-eqz v1, :cond_1

    invoke-interface {v0}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v1

    .line 127
    instance-of v2, v1, Lcom/qf/clientsdk/listeners/ITunerTool;

    if-eqz v2, :cond_0

    .line 128
    check-cast v1, Lcom/qf/clientsdk/listeners/ITunerTool;

    .line 129
    invoke-interface {v1, p1}, Lcom/qf/clientsdk/listeners/ITunerTool;->onTuneRdsPtyTypeInfo([B)V

    goto :goto_0

    :cond_1
    return-void
.end method

.method private handleRdsRTInfo([B)V
    .locals 3

    .line 147
    iget-object v0, p0, Lcom/qf/clientsdk/presenter/TunerPresenter;->toolList:Ljava/util/concurrent/CopyOnWriteArrayList;

    if-eqz v0, :cond_1

    invoke-virtual {v0}, Ljava/util/concurrent/CopyOnWriteArrayList;->size()I

    move-result v0

    if-lez v0, :cond_1

    .line 148
    iget-object v0, p0, Lcom/qf/clientsdk/presenter/TunerPresenter;->toolList:Ljava/util/concurrent/CopyOnWriteArrayList;

    invoke-virtual {v0}, Ljava/util/concurrent/CopyOnWriteArrayList;->iterator()Ljava/util/Iterator;

    move-result-object v0

    :cond_0
    :goto_0
    invoke-interface {v0}, Ljava/util/Iterator;->hasNext()Z

    move-result v1

    if-eqz v1, :cond_1

    invoke-interface {v0}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v1

    .line 149
    instance-of v2, v1, Lcom/qf/clientsdk/listeners/ITunerTool;

    if-eqz v2, :cond_0

    .line 150
    check-cast v1, Lcom/qf/clientsdk/listeners/ITunerTool;

    .line 151
    invoke-interface {v1, p1}, Lcom/qf/clientsdk/listeners/ITunerTool;->onTuneRdsRTInfo([B)V

    goto :goto_0

    :cond_1
    return-void
.end method

.method private handleTunerInfo([B)V
    .locals 3

    .line 70
    iget-object v0, p0, Lcom/qf/clientsdk/presenter/TunerPresenter;->toolList:Ljava/util/concurrent/CopyOnWriteArrayList;

    if-eqz v0, :cond_1

    invoke-virtual {v0}, Ljava/util/concurrent/CopyOnWriteArrayList;->size()I

    move-result v0

    if-lez v0, :cond_1

    .line 71
    iget-object v0, p0, Lcom/qf/clientsdk/presenter/TunerPresenter;->toolList:Ljava/util/concurrent/CopyOnWriteArrayList;

    invoke-virtual {v0}, Ljava/util/concurrent/CopyOnWriteArrayList;->iterator()Ljava/util/Iterator;

    move-result-object v0

    :cond_0
    :goto_0
    invoke-interface {v0}, Ljava/util/Iterator;->hasNext()Z

    move-result v1

    if-eqz v1, :cond_1

    invoke-interface {v0}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v1

    .line 72
    instance-of v2, v1, Lcom/qf/clientsdk/listeners/ITunerTool;

    if-eqz v2, :cond_0

    .line 73
    check-cast v1, Lcom/qf/clientsdk/listeners/ITunerTool;

    .line 74
    invoke-interface {v1, p1}, Lcom/qf/clientsdk/listeners/ITunerTool;->onTunerInfoChanged([B)V

    goto :goto_0

    :cond_1
    return-void
.end method


# virtual methods
.method public handleTunerRawData([B)V
    .locals 12

    .line 29
    sget-object v0, Lcom/qf/clientsdk/presenter/TunerPresenter;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "handleTunerRawData: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-static {p1}, Lcom/qf/clientsdk/utils/ByteTool;->ByteToString([B)Ljava/lang/String;

    move-result-object v2

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    const/4 v0, 0x0

    .line 32
    :try_start_0
    aget-byte v0, p1, v0

    and-int/lit16 v0, v0, 0xff

    .line 33
    array-length v1, p1

    const/16 v2, 0xb8

    const/16 v3, 0xb7

    const/16 v4, 0xb6

    const/16 v5, 0xb5

    const/16 v6, 0xb4

    const/16 v7, 0xb3

    const/16 v8, 0xb2

    const/16 v9, 0xb1

    const/16 v10, 0xb0

    if-eq v10, v0, :cond_0

    if-eq v9, v0, :cond_0

    if-eq v8, v0, :cond_0

    if-eq v7, v0, :cond_0

    if-eq v6, v0, :cond_0

    if-eq v5, v0, :cond_0

    if-eq v4, v0, :cond_0

    if-eq v3, v0, :cond_0

    if-ne v2, v0, :cond_9

    :cond_0
    const/4 v11, 0x1

    if-le v1, v11, :cond_9

    if-ne v10, v0, :cond_1

    .line 45
    invoke-direct {p0, p1}, Lcom/qf/clientsdk/presenter/TunerPresenter;->handleTunerInfo([B)V

    goto :goto_0

    :cond_1
    if-ne v9, v0, :cond_2

    .line 47
    invoke-direct {p0, p1}, Lcom/qf/clientsdk/presenter/TunerPresenter;->handlePresetListInfo([B)V

    goto :goto_0

    :cond_2
    if-ne v8, v0, :cond_3

    .line 49
    invoke-direct {p0, p1}, Lcom/qf/clientsdk/presenter/TunerPresenter;->handleFreqRangInfo([B)V

    goto :goto_0

    :cond_3
    if-ne v7, v0, :cond_4

    .line 51
    invoke-direct {p0, p1}, Lcom/qf/clientsdk/presenter/TunerPresenter;->handleRdsInfo([B)V

    goto :goto_0

    :cond_4
    if-ne v6, v0, :cond_5

    .line 53
    invoke-direct {p0, p1}, Lcom/qf/clientsdk/presenter/TunerPresenter;->handleRdsIndicateInfo([B)V

    goto :goto_0

    :cond_5
    if-ne v5, v0, :cond_6

    .line 55
    invoke-direct {p0, p1}, Lcom/qf/clientsdk/presenter/TunerPresenter;->handleRdsPtyTypeInfo([B)V

    goto :goto_0

    :cond_6
    if-ne v4, v0, :cond_7

    .line 57
    invoke-direct {p0, p1}, Lcom/qf/clientsdk/presenter/TunerPresenter;->handleRdsPSInfo([B)V

    goto :goto_0

    :cond_7
    if-ne v3, v0, :cond_8

    .line 59
    invoke-direct {p0, p1}, Lcom/qf/clientsdk/presenter/TunerPresenter;->handleRdsRTInfo([B)V

    goto :goto_0

    :cond_8
    if-ne v2, v0, :cond_9

    .line 61
    invoke-direct {p0, p1}, Lcom/qf/clientsdk/presenter/TunerPresenter;->handleRdsPSPresetListInfo([B)V
    :try_end_0
    .catch Ljava/lang/Exception; {:try_start_0 .. :try_end_0} :catch_0

    :catch_0
    :cond_9
    :goto_0
    return-void
.end method

.method public isTunerScanning()Z
    .locals 1

    .line 169
    iget-boolean v0, p0, Lcom/qf/clientsdk/presenter/TunerPresenter;->tunerScanning:Z

    return v0
.end method

.method public setTunerScanning(Z)V
    .locals 1

    .line 173
    iput-boolean p1, p0, Lcom/qf/clientsdk/presenter/TunerPresenter;->tunerScanning:Z

    .line 174
    invoke-static {}, Lcom/qf/clientsdk/QFTunerManager;->getInstance()Lcom/qf/clientsdk/QFTunerManager;

    move-result-object v0

    invoke-virtual {v0, p1}, Lcom/qf/clientsdk/QFTunerManager;->setTunerScanning(Z)V

    return-void
.end method
