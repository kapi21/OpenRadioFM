.class Lcom/broadcom/fm/fmreceiver/FmProxy$FmBroadcastReceiver;
.super Landroid/content/BroadcastReceiver;
.source "FmProxy.java"


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Lcom/broadcom/fm/fmreceiver/FmProxy;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x2
    name = "FmBroadcastReceiver"
.end annotation


# instance fields
.field final synthetic this$0:Lcom/broadcom/fm/fmreceiver/FmProxy;


# direct methods
.method private constructor <init>(Lcom/broadcom/fm/fmreceiver/FmProxy;)V
    .locals 0

    .line 1462
    iput-object p1, p0, Lcom/broadcom/fm/fmreceiver/FmProxy$FmBroadcastReceiver;->this$0:Lcom/broadcom/fm/fmreceiver/FmProxy;

    invoke-direct {p0}, Landroid/content/BroadcastReceiver;-><init>()V

    return-void
.end method


# virtual methods
.method public onReceive(Landroid/content/Context;Landroid/content/Intent;)V
    .locals 10

    .line 1464
    iget-object p1, p0, Lcom/broadcom/fm/fmreceiver/FmProxy$FmBroadcastReceiver;->this$0:Lcom/broadcom/fm/fmreceiver/FmProxy;

    invoke-static {p1}, Lcom/broadcom/fm/fmreceiver/FmProxy;->access$100(Lcom/broadcom/fm/fmreceiver/FmProxy;)Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;

    move-result-object v0

    if-nez v0, :cond_0

    return-void

    .line 1469
    :cond_0
    invoke-virtual {p0}, Lcom/broadcom/fm/fmreceiver/FmProxy$FmBroadcastReceiver;->abortBroadcast()V

    .line 1471
    invoke-virtual {p2}, Landroid/content/Intent;->getAction()Ljava/lang/String;

    move-result-object p1

    .line 1472
    invoke-static {}, Lcom/broadcom/fm/fmreceiver/FmProxy;->access$200()I

    move-result v1

    const-string v2, "com.broadcom.bt.app.fm.action.ON_STATUS"

    invoke-static {v2, p1, v1}, Lcom/broadcom/fm/fmreceiver/FmProxy;->actionsEqual(Ljava/lang/String;Ljava/lang/String;I)Z

    move-result v1

    const-string v2, "RDS_TXT"

    const-string v3, "FREQ"

    const/16 v4, -0x7e

    const-string v5, "SNR"

    const-string v6, "RSSI"

    const/4 v7, 0x0

    const/4 v8, -0x1

    if-eqz v1, :cond_1

    .line 1473
    invoke-virtual {p2, v3, v7}, Landroid/content/Intent;->getIntExtra(Ljava/lang/String;I)I

    move-result v1

    invoke-virtual {p2, v6, v7}, Landroid/content/Intent;->getIntExtra(Ljava/lang/String;I)I

    move-result p1

    invoke-virtual {p2, v5, v4}, Landroid/content/Intent;->getIntExtra(Ljava/lang/String;I)I

    move-result v3

    const-string v4, "RADIO_ON"

    invoke-virtual {p2, v4, v7}, Landroid/content/Intent;->getBooleanExtra(Ljava/lang/String;Z)Z

    move-result v4

    const-string v5, "RDS_PRGM_TYPE"

    invoke-virtual {p2, v5, v8}, Landroid/content/Intent;->getIntExtra(Ljava/lang/String;I)I

    move-result v5

    const-string v6, "RDS_PRGM_SVC"

    invoke-virtual {p2, v6}, Landroid/content/Intent;->getStringExtra(Ljava/lang/String;)Ljava/lang/String;

    move-result-object v6

    invoke-virtual {p2, v2}, Landroid/content/Intent;->getStringExtra(Ljava/lang/String;)Ljava/lang/String;

    move-result-object v8

    const-string v2, "RDS_PRGM_TYPE_NAME"

    invoke-virtual {p2, v2}, Landroid/content/Intent;->getStringExtra(Ljava/lang/String;)Ljava/lang/String;

    move-result-object v9

    const-string v2, "MUTED"

    invoke-virtual {p2, v2, v7}, Landroid/content/Intent;->getBooleanExtra(Ljava/lang/String;Z)Z

    move-result p2

    move v2, p1

    move-object v7, v8

    move-object v8, v9

    move v9, p2

    invoke-interface/range {v0 .. v9}, Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;->onStatusEvent(IIIZILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Z)V

    goto/16 :goto_0

    .line 1482
    :cond_1
    invoke-static {}, Lcom/broadcom/fm/fmreceiver/FmProxy;->access$200()I

    move-result v1

    const-string v9, "com.broadcom.bt.app.fm.action.ON_AUDIO_MODE"

    invoke-static {v9, p1, v1}, Lcom/broadcom/fm/fmreceiver/FmProxy;->actionsEqual(Ljava/lang/String;Ljava/lang/String;I)Z

    move-result v1

    if-eqz v1, :cond_2

    const-string p1, "AUDIO_MODE"

    .line 1483
    invoke-virtual {p2, p1, v8}, Landroid/content/Intent;->getIntExtra(Ljava/lang/String;I)I

    move-result p1

    invoke-interface {v0, p1}, Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;->onAudioModeEvent(I)V

    goto/16 :goto_0

    .line 1484
    :cond_2
    invoke-static {}, Lcom/broadcom/fm/fmreceiver/FmProxy;->access$200()I

    move-result v1

    const-string v9, "com.broadcom.bt.app.fm.action.ON_AUDIO_PATH"

    invoke-static {v9, p1, v1}, Lcom/broadcom/fm/fmreceiver/FmProxy;->actionsEqual(Ljava/lang/String;Ljava/lang/String;I)Z

    move-result v1

    if-eqz v1, :cond_3

    const-string p1, "AUDIO_PATH"

    .line 1485
    invoke-virtual {p2, p1, v8}, Landroid/content/Intent;->getIntExtra(Ljava/lang/String;I)I

    move-result p1

    invoke-interface {v0, p1}, Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;->onAudioPathEvent(I)V

    goto/16 :goto_0

    .line 1486
    :cond_3
    invoke-static {}, Lcom/broadcom/fm/fmreceiver/FmProxy;->access$200()I

    move-result v1

    const-string v9, "com.broadcom.bt.app.fm.action.ON_AUDIO_QUAL"

    invoke-static {v9, p1, v1}, Lcom/broadcom/fm/fmreceiver/FmProxy;->actionsEqual(Ljava/lang/String;Ljava/lang/String;I)Z

    move-result v1

    if-eqz v1, :cond_4

    .line 1487
    invoke-virtual {p2, v6, v8}, Landroid/content/Intent;->getIntExtra(Ljava/lang/String;I)I

    move-result p1

    invoke-virtual {p2, v5, v4}, Landroid/content/Intent;->getIntExtra(Ljava/lang/String;I)I

    move-result p2

    invoke-interface {v0, p1, p2}, Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;->onLiveAudioQualityEvent(II)V

    goto/16 :goto_0

    .line 1489
    :cond_4
    invoke-static {}, Lcom/broadcom/fm/fmreceiver/FmProxy;->access$200()I

    move-result v1

    const-string v9, "com.broadcom.bt.app.fm.action.ON_EST_NFL"

    invoke-static {v9, p1, v1}, Lcom/broadcom/fm/fmreceiver/FmProxy;->actionsEqual(Ljava/lang/String;Ljava/lang/String;I)Z

    move-result v1

    if-eqz v1, :cond_5

    const-string p1, "NFL"

    .line 1490
    invoke-virtual {p2, p1, v8}, Landroid/content/Intent;->getIntExtra(Ljava/lang/String;I)I

    move-result p1

    invoke-interface {v0, p1}, Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;->onEstimateNoiseFloorLevelEvent(I)V

    goto/16 :goto_0

    .line 1491
    :cond_5
    invoke-static {}, Lcom/broadcom/fm/fmreceiver/FmProxy;->access$200()I

    move-result v1

    const-string v9, "com.broadcom.bt.app.fm.action.ON_RDS_DATA"

    invoke-static {v9, p1, v1}, Lcom/broadcom/fm/fmreceiver/FmProxy;->actionsEqual(Ljava/lang/String;Ljava/lang/String;I)Z

    move-result v1

    if-eqz v1, :cond_6

    const-string p1, "RDS_DATA_TYPE"

    .line 1492
    invoke-virtual {p2, p1, v8}, Landroid/content/Intent;->getIntExtra(Ljava/lang/String;I)I

    move-result p1

    const-string v1, "RDS_IDX"

    invoke-virtual {p2, v1, v8}, Landroid/content/Intent;->getIntExtra(Ljava/lang/String;I)I

    move-result v1

    invoke-virtual {p2, v2}, Landroid/content/Intent;->getStringExtra(Ljava/lang/String;)Ljava/lang/String;

    move-result-object p2

    invoke-interface {v0, p1, v1, p2}, Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;->onRdsDataEvent(IILjava/lang/String;)V

    goto :goto_0

    .line 1495
    :cond_6
    invoke-static {}, Lcom/broadcom/fm/fmreceiver/FmProxy;->access$200()I

    move-result v1

    const-string v2, "com.broadcom.bt.app.fm.action.ON_RDS_MODE"

    invoke-static {v2, p1, v1}, Lcom/broadcom/fm/fmreceiver/FmProxy;->actionsEqual(Ljava/lang/String;Ljava/lang/String;I)Z

    move-result v1

    if-eqz v1, :cond_7

    const-string p1, "RDS_MODE"

    .line 1496
    invoke-virtual {p2, p1, v8}, Landroid/content/Intent;->getIntExtra(Ljava/lang/String;I)I

    move-result p1

    const-string v1, "ALT_FREQ_MODE"

    invoke-virtual {p2, v1, v8}, Landroid/content/Intent;->getIntExtra(Ljava/lang/String;I)I

    move-result p2

    invoke-interface {v0, p1, p2}, Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;->onRdsModeEvent(II)V

    goto :goto_0

    .line 1498
    :cond_7
    invoke-static {}, Lcom/broadcom/fm/fmreceiver/FmProxy;->access$200()I

    move-result v1

    const-string v2, "com.broadcom.bt.app.fm.action.ON_SEEK_CMPL"

    invoke-static {v2, p1, v1}, Lcom/broadcom/fm/fmreceiver/FmProxy;->actionsEqual(Ljava/lang/String;Ljava/lang/String;I)Z

    move-result v1

    if-eqz v1, :cond_8

    .line 1499
    invoke-virtual {p2, v3, v8}, Landroid/content/Intent;->getIntExtra(Ljava/lang/String;I)I

    move-result p1

    invoke-virtual {p2, v6, v8}, Landroid/content/Intent;->getIntExtra(Ljava/lang/String;I)I

    move-result v1

    invoke-virtual {p2, v5, v4}, Landroid/content/Intent;->getIntExtra(Ljava/lang/String;I)I

    move-result v2

    const-string v3, "RDS_SUCCESS"

    invoke-virtual {p2, v3, v7}, Landroid/content/Intent;->getBooleanExtra(Ljava/lang/String;Z)Z

    move-result p2

    invoke-interface {v0, p1, v1, v2, p2}, Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;->onSeekCompleteEvent(IIIZ)V

    goto :goto_0

    .line 1503
    :cond_8
    invoke-static {}, Lcom/broadcom/fm/fmreceiver/FmProxy;->access$200()I

    move-result v1

    const-string v2, "ON_VOL"

    invoke-static {v2, p1, v1}, Lcom/broadcom/fm/fmreceiver/FmProxy;->actionsEqual(Ljava/lang/String;Ljava/lang/String;I)Z

    move-result v1

    if-eqz v1, :cond_9

    const-string p1, "STATUS"

    .line 1504
    invoke-virtual {p2, p1, v8}, Landroid/content/Intent;->getIntExtra(Ljava/lang/String;I)I

    move-result p1

    const-string v1, "VOL"

    invoke-virtual {p2, v1, v8}, Landroid/content/Intent;->getIntExtra(Ljava/lang/String;I)I

    move-result p2

    invoke-interface {v0, p1, p2}, Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;->onVolumeEvent(II)V

    goto :goto_0

    .line 1506
    :cond_9
    invoke-static {}, Lcom/broadcom/fm/fmreceiver/FmProxy;->access$200()I

    move-result v1

    const-string v2, "com.broadcom.bt.app.fm.action.ON_WRLD_RGN"

    invoke-static {v2, p1, v1}, Lcom/broadcom/fm/fmreceiver/FmProxy;->actionsEqual(Ljava/lang/String;Ljava/lang/String;I)Z

    move-result p1

    if-eqz p1, :cond_a

    const-string p1, "WRLD_RGN"

    .line 1507
    invoke-virtual {p2, p1, v8}, Landroid/content/Intent;->getIntExtra(Ljava/lang/String;I)I

    move-result p1

    invoke-interface {v0, p1}, Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;->onWorldRegionEvent(I)V

    :cond_a
    :goto_0
    return-void
.end method
