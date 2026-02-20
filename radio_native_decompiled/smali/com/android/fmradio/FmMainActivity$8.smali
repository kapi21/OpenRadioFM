.class Lcom/android/fmradio/FmMainActivity$8;
.super Ljava/lang/Object;
.source "FmMainActivity.java"

# interfaces
.implements Lcom/android/fmradio/views/RadioPresetListView$PresetListCallback;


# annotations
.annotation system Ldalvik/annotation/EnclosingMethod;
    value = Lcom/android/fmradio/FmMainActivity;->initPresetListView()V
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x0
    name = null
.end annotation


# instance fields
.field final synthetic this$0:Lcom/android/fmradio/FmMainActivity;


# direct methods
.method constructor <init>(Lcom/android/fmradio/FmMainActivity;)V
    .locals 0

    .line 1332
    iput-object p1, p0, Lcom/android/fmradio/FmMainActivity$8;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public onClickItem(I)V
    .locals 6

    .line 1335
    invoke-static {}, Landroid/qf/os/QFApi;->isBTPhoneStartup()Z

    move-result v0

    if-eqz v0, :cond_0

    return-void

    .line 1339
    :cond_0
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity$8;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {v0}, Lcom/android/fmradio/FmMainActivity;->restorePowerUp()Z

    move-result v0

    if-eqz v0, :cond_1

    return-void

    .line 1343
    :cond_1
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity$8;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {v0}, Lcom/android/fmradio/FmMainActivity;->stopScan()Z

    move-result v0

    if-eqz v0, :cond_2

    return-void

    .line 1347
    :cond_2
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity$8;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {v0}, Lcom/android/fmradio/FmMainActivity;->requestAudioFocus()V

    .line 1349
    invoke-static {}, Lcom/android/fmradio/FmMainActivity;->access$100()Ljava/lang/String;

    move-result-object v0

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "mCurrentStation: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-object v2, p0, Lcom/android/fmradio/FmMainActivity$8;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget v2, v2, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v2, " - mBand: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-object v3, p0, Lcom/android/fmradio/FmMainActivity$8;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget v3, v3, Lcom/android/fmradio/FmMainActivity;->mBand:I

    invoke-virtual {v1, v3}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v3, " - id: "

    invoke-virtual {v1, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 1353
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity$8;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget v0, v0, Lcom/android/fmradio/FmMainActivity;->mBand:I

    invoke-static {v0}, Lcom/android/fmradio/utils/FmUtils;->isFMBand(I)Z

    move-result v0

    const/4 v1, 0x0

    if-eqz v0, :cond_4

    .line 1354
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity$8;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget v0, v0, Lcom/android/fmradio/FmMainActivity;->mBand:I

    mul-int/lit8 v0, v0, 0x6

    add-int/2addr v0, p1

    .line 1356
    iget-object v3, p0, Lcom/android/fmradio/FmMainActivity$8;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-static {v3}, Lcom/android/fmradio/FmMainActivity;->access$2300(Lcom/android/fmradio/FmMainActivity;)[Lcom/android/fmradio/info/FmFreqInfo;

    move-result-object v3

    aget-object v3, v3, v0

    invoke-virtual {v3}, Lcom/android/fmradio/info/FmFreqInfo;->getFreq()I

    move-result v3

    if-eqz v3, :cond_3

    .line 1358
    iget-object v1, p0, Lcom/android/fmradio/FmMainActivity$8;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget v4, v1, Lcom/android/fmradio/FmMainActivity;->mBand:I

    add-int/lit8 v5, p1, 0x1

    invoke-static {v1, v4, v5, v3}, Lcom/android/fmradio/FmMainActivity;->access$900(Lcom/android/fmradio/FmMainActivity;III)V

    goto :goto_0

    .line 1360
    :cond_3
    iget-object v4, p0, Lcom/android/fmradio/FmMainActivity$8;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget v5, v4, Lcom/android/fmradio/FmMainActivity;->mBand:I

    invoke-static {v4, v5, v1, v3}, Lcom/android/fmradio/FmMainActivity;->access$900(Lcom/android/fmradio/FmMainActivity;III)V

    goto :goto_0

    .line 1363
    :cond_4
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity$8;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget v0, v0, Lcom/android/fmradio/FmMainActivity;->mBand:I

    rem-int/lit8 v0, v0, 0x3

    mul-int/lit8 v0, v0, 0x6

    add-int/2addr v0, p1

    .line 1365
    iget-object v3, p0, Lcom/android/fmradio/FmMainActivity$8;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-static {v3}, Lcom/android/fmradio/FmMainActivity;->access$2400(Lcom/android/fmradio/FmMainActivity;)[Lcom/android/fmradio/info/FmFreqInfo;

    move-result-object v3

    aget-object v3, v3, v0

    invoke-virtual {v3}, Lcom/android/fmradio/info/FmFreqInfo;->getFreq()I

    move-result v3

    if-eqz v3, :cond_5

    .line 1367
    iget-object v1, p0, Lcom/android/fmradio/FmMainActivity$8;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget v4, v1, Lcom/android/fmradio/FmMainActivity;->mBand:I

    add-int/lit8 v5, p1, 0x1

    invoke-static {v1, v4, v5, v3}, Lcom/android/fmradio/FmMainActivity;->access$900(Lcom/android/fmradio/FmMainActivity;III)V

    goto :goto_0

    .line 1369
    :cond_5
    iget-object v4, p0, Lcom/android/fmradio/FmMainActivity$8;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget v5, v4, Lcom/android/fmradio/FmMainActivity;->mBand:I

    invoke-static {v4, v5, v1, v3}, Lcom/android/fmradio/FmMainActivity;->access$900(Lcom/android/fmradio/FmMainActivity;III)V

    .line 1373
    :goto_0
    invoke-static {}, Lcom/android/fmradio/FmMainActivity;->access$100()Ljava/lang/String;

    move-result-object v1

    new-instance v4, Ljava/lang/StringBuilder;

    invoke-direct {v4}, Ljava/lang/StringBuilder;-><init>()V

    const-string v5, "freq: "

    invoke-virtual {v4, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v4, v3}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v4, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-object v2, p0, Lcom/android/fmradio/FmMainActivity$8;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget v2, v2, Lcom/android/fmradio/FmMainActivity;->mBand:I

    invoke-virtual {v4, v2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v2, " - index: "

    invoke-virtual {v4, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v4, v0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v4}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    invoke-static {v1, v0}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 1375
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity$8;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget-object v0, v0, Lcom/android/fmradio/FmMainActivity;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    add-int/lit8 p1, p1, 0x1

    int-to-byte p1, p1

    invoke-virtual {v0, p1}, Lcom/android/fmradio/TunerManagerForExt;->onPresetSelect(B)V

    return-void
.end method

.method public onLongClickItem(I)V
    .locals 4

    .line 1380
    invoke-static {}, Landroid/qf/os/QFApi;->isBTPhoneStartup()Z

    move-result v0

    if-eqz v0, :cond_0

    return-void

    .line 1384
    :cond_0
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity$8;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {v0}, Lcom/android/fmradio/FmMainActivity;->restorePowerUp()Z

    move-result v0

    if-eqz v0, :cond_1

    return-void

    .line 1388
    :cond_1
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity$8;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {v0}, Lcom/android/fmradio/FmMainActivity;->stopScan()Z

    move-result v0

    if-eqz v0, :cond_2

    return-void

    .line 1392
    :cond_2
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity$8;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {v0}, Lcom/android/fmradio/FmMainActivity;->requestAudioFocus()V

    .line 1394
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity$8;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget-object v0, v0, Lcom/android/fmradio/FmMainActivity;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    invoke-virtual {v0}, Lcom/android/fmradio/TunerManagerForExt;->getRdsPsInfo()Ljava/lang/String;

    move-result-object v0

    .line 1395
    invoke-static {}, Lcom/android/fmradio/FmMainActivity;->access$100()Ljava/lang/String;

    move-result-object v1

    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2}, Ljava/lang/StringBuilder;-><init>()V

    const-string v3, "mCurrentStation: "

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-object v3, p0, Lcom/android/fmradio/FmMainActivity$8;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget v3, v3, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v3, " - id: "

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v3, " - psName: "

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v2

    invoke-static {v1, v2}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 1400
    invoke-static {v0}, Landroid/text/TextUtils;->isEmpty(Ljava/lang/CharSequence;)Z

    move-result v1

    if-nez v1, :cond_4

    .line 1402
    iget-object v1, p0, Lcom/android/fmradio/FmMainActivity$8;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-static {v1}, Lcom/android/fmradio/FmMainActivity;->access$2500(Lcom/android/fmradio/FmMainActivity;)Landroid/content/Context;

    move-result-object v1

    iget-object v2, p0, Lcom/android/fmradio/FmMainActivity$8;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget v2, v2, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    invoke-static {v1, v2}, Lcom/android/fmradio/utils/RdsPsInfoUtil;->rdsPsExist(Landroid/content/Context;I)Z

    move-result v1

    if-eqz v1, :cond_3

    .line 1403
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity$8;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-static {v0}, Lcom/android/fmradio/FmMainActivity;->access$2500(Lcom/android/fmradio/FmMainActivity;)Landroid/content/Context;

    move-result-object v0

    iget-object v1, p0, Lcom/android/fmradio/FmMainActivity$8;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget v1, v1, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/RdsPsInfoUtil;->getRdsPsName(Landroid/content/Context;I)Ljava/lang/String;

    move-result-object v0

    .line 1404
    invoke-static {}, Lcom/android/fmradio/FmMainActivity;->access$100()Ljava/lang/String;

    move-result-object v1

    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2}, Ljava/lang/StringBuilder;-><init>()V

    const-string v3, "rds ps is exist - psName: "

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v2

    invoke-static {v1, v2}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 1406
    :cond_3
    iget-object v1, p0, Lcom/android/fmradio/FmMainActivity$8;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget v2, v1, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    invoke-virtual {v1, v2, v0}, Lcom/android/fmradio/FmMainActivity;->onUpdateStationName(ILjava/lang/String;)V

    .line 1408
    iget-object v1, p0, Lcom/android/fmradio/FmMainActivity$8;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-static {v1}, Lcom/android/fmradio/FmMainActivity;->access$2500(Lcom/android/fmradio/FmMainActivity;)Landroid/content/Context;

    move-result-object v1

    iget-object v2, p0, Lcom/android/fmradio/FmMainActivity$8;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget v2, v2, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    const-string v3, "all_station_info_key"

    invoke-static {v1, v3, v2, v0}, Lcom/android/fmradio/utils/AllStationInfoUtil;->updateAllStationInfoData(Landroid/content/Context;Ljava/lang/String;ILjava/lang/String;)V

    .line 1410
    iget-object v1, p0, Lcom/android/fmradio/FmMainActivity$8;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-static {v1}, Lcom/android/fmradio/FmMainActivity;->access$2500(Lcom/android/fmradio/FmMainActivity;)Landroid/content/Context;

    move-result-object v1

    iget-object v2, p0, Lcom/android/fmradio/FmMainActivity$8;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget v2, v2, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    const-string v3, "rds_ps_info_key"

    invoke-static {v1, v3, v2, v0}, Lcom/android/fmradio/utils/RdsPsInfoUtil;->updateRdsPsInfoData(Landroid/content/Context;Ljava/lang/String;ILjava/lang/String;)V

    .line 1413
    :cond_4
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity$8;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget v1, v0, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    const/4 v2, 0x1

    invoke-static {v0, p1, v1, v2}, Lcom/android/fmradio/FmMainActivity;->access$2600(Lcom/android/fmradio/FmMainActivity;IIZ)V

    .line 1415
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity$8;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget v1, v0, Lcom/android/fmradio/FmMainActivity;->mBand:I

    add-int/2addr p1, v2

    iget-object v2, p0, Lcom/android/fmradio/FmMainActivity$8;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget v2, v2, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    invoke-static {v0, v1, p1, v2}, Lcom/android/fmradio/FmMainActivity;->access$900(Lcom/android/fmradio/FmMainActivity;III)V

    .line 1417
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity$8;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-static {v0}, Lcom/android/fmradio/FmMainActivity;->access$1100(Lcom/android/fmradio/FmMainActivity;)V

    .line 1419
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity$8;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget-object v0, v0, Lcom/android/fmradio/FmMainActivity;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    int-to-byte p1, p1

    invoke-virtual {v0, p1}, Lcom/android/fmradio/TunerManagerForExt;->onPresetSave(B)V

    return-void
.end method
