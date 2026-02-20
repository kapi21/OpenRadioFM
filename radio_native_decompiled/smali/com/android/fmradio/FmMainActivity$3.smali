.class Lcom/android/fmradio/FmMainActivity$3;
.super Landroid/os/Handler;
.source "FmMainActivity.java"


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Lcom/android/fmradio/FmMainActivity;
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

    .line 262
    iput-object p1, p0, Lcom/android/fmradio/FmMainActivity$3;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-direct {p0}, Landroid/os/Handler;-><init>()V

    return-void
.end method

.method private onUpdateControlInfo()V
    .locals 2

    .line 269
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity$3;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget-object v0, v0, Lcom/android/fmradio/FmMainActivity;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    invoke-virtual {v0}, Lcom/android/fmradio/TunerManagerForExt;->getStFlag()I

    move-result v0

    iget-object v1, p0, Lcom/android/fmradio/FmMainActivity$3;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget-object v1, v1, Lcom/android/fmradio/FmMainActivity;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    invoke-virtual {v1}, Lcom/android/fmradio/TunerManagerForExt;->getLocFlag()I

    move-result v1

    invoke-direct {p0, v0, v1}, Lcom/android/fmradio/FmMainActivity$3;->updateSTAndLocStatus(II)V

    return-void
.end method

.method private onUpdatePresetList()V
    .locals 4

    .line 279
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity$3;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget-object v1, v0, Lcom/android/fmradio/FmMainActivity;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    invoke-virtual {v1}, Lcom/android/fmradio/TunerManagerForExt;->getFrequency()I

    move-result v1

    iput v1, v0, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    .line 280
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity$3;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget-object v1, v0, Lcom/android/fmradio/FmMainActivity;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    invoke-virtual {v1}, Lcom/android/fmradio/TunerManagerForExt;->getBand()I

    move-result v1

    iput v1, v0, Lcom/android/fmradio/FmMainActivity;->mBand:I

    .line 281
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity$3;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget-object v1, v0, Lcom/android/fmradio/FmMainActivity;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    invoke-virtual {v1}, Lcom/android/fmradio/TunerManagerForExt;->getPresetIndex()I

    move-result v1

    iput v1, v0, Lcom/android/fmradio/FmMainActivity;->mPresetIndex:I

    .line 283
    new-instance v0, Ljava/lang/StringBuilder;

    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    const-string v1, "mCurrentStation: "

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-object v1, p0, Lcom/android/fmradio/FmMainActivity$3;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget v1, v1, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v1, " - mBand: "

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-object v1, p0, Lcom/android/fmradio/FmMainActivity$3;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget v1, v1, Lcom/android/fmradio/FmMainActivity;->mBand:I

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v1, " - mPresetIndex: "

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-object v1, p0, Lcom/android/fmradio/FmMainActivity$3;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget v1, v1, Lcom/android/fmradio/FmMainActivity;->mPresetIndex:I

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    .line 286
    invoke-static {}, Lcom/android/fmradio/FmMainActivity;->access$100()Ljava/lang/String;

    move-result-object v1

    invoke-static {v1, v0}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 288
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity$3;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget v1, v0, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    invoke-static {v0, v1}, Lcom/android/fmradio/FmMainActivity;->access$800(Lcom/android/fmradio/FmMainActivity;I)V

    .line 290
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity$3;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget v1, v0, Lcom/android/fmradio/FmMainActivity;->mBand:I

    iget-object v2, p0, Lcom/android/fmradio/FmMainActivity$3;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget v2, v2, Lcom/android/fmradio/FmMainActivity;->mPresetIndex:I

    iget-object v3, p0, Lcom/android/fmradio/FmMainActivity$3;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget v3, v3, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    invoke-static {v0, v1, v2, v3}, Lcom/android/fmradio/FmMainActivity;->access$900(Lcom/android/fmradio/FmMainActivity;III)V

    .line 292
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity$3;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {v0}, Lcom/android/fmradio/FmMainActivity;->updatePresetList()V

    .line 294
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity$3;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-static {v0}, Lcom/android/fmradio/FmMainActivity;->access$1000(Lcom/android/fmradio/FmMainActivity;)V

    return-void
.end method

.method private onUpdateRangInfo()V
    .locals 2

    .line 265
    invoke-static {}, Lcom/android/fmradio/FmMainActivity;->access$100()Ljava/lang/String;

    move-result-object v0

    const-string v1, "start"

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    return-void
.end method

.method private onUpdateRdsInfo()V
    .locals 3

    .line 298
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity$3;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-static {v0}, Lcom/android/fmradio/FmMainActivity;->access$600(Lcom/android/fmradio/FmMainActivity;)Lcom/android/fmradio/views/RadioStatusView;

    move-result-object v0

    iget-object v1, p0, Lcom/android/fmradio/FmMainActivity$3;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget-object v1, v1, Lcom/android/fmradio/FmMainActivity;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    invoke-virtual {v1}, Lcom/android/fmradio/TunerManagerForExt;->getRdsAFSwitch()I

    move-result v1

    iget-object v2, p0, Lcom/android/fmradio/FmMainActivity$3;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget-object v2, v2, Lcom/android/fmradio/FmMainActivity;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    invoke-virtual {v2}, Lcom/android/fmradio/TunerManagerForExt;->getRdsTASwitch()I

    move-result v2

    invoke-virtual {v0, v1, v2}, Lcom/android/fmradio/views/RadioStatusView;->updateRdsInfo(II)V

    return-void
.end method

.method private onUpdateRdsPsInfo()V
    .locals 2

    .line 307
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity$3;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-static {v0}, Lcom/android/fmradio/FmMainActivity;->access$000(Lcom/android/fmradio/FmMainActivity;)Lcom/android/fmradio/views/RadioFreqInfoView;

    move-result-object v0

    iget-object v1, p0, Lcom/android/fmradio/FmMainActivity$3;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget-object v1, v1, Lcom/android/fmradio/FmMainActivity;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    invoke-virtual {v1}, Lcom/android/fmradio/TunerManagerForExt;->getRdsPsInfo()Ljava/lang/String;

    move-result-object v1

    invoke-virtual {v0, v1}, Lcom/android/fmradio/views/RadioFreqInfoView;->updateFreqValue(Ljava/lang/String;)V

    return-void
.end method

.method private onUpdateRdsPtyTypeInfo()V
    .locals 2

    .line 302
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity$3;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-static {v0}, Lcom/android/fmradio/FmMainActivity;->access$600(Lcom/android/fmradio/FmMainActivity;)Lcom/android/fmradio/views/RadioStatusView;

    move-result-object v0

    iget-object v1, p0, Lcom/android/fmradio/FmMainActivity$3;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget-object v1, v1, Lcom/android/fmradio/FmMainActivity;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    invoke-virtual {v1}, Lcom/android/fmradio/TunerManagerForExt;->getRdsPtyType()I

    move-result v1

    invoke-virtual {v0, v1}, Lcom/android/fmradio/views/RadioStatusView;->updateRdsPtyTypeInfo(I)V

    return-void
.end method

.method private onUpdateRdsRtInfo()V
    .locals 2

    .line 314
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity$3;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-static {v0}, Lcom/android/fmradio/FmMainActivity;->access$600(Lcom/android/fmradio/FmMainActivity;)Lcom/android/fmradio/views/RadioStatusView;

    move-result-object v0

    iget-object v1, p0, Lcom/android/fmradio/FmMainActivity$3;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget-object v1, v1, Lcom/android/fmradio/FmMainActivity;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    invoke-virtual {v1}, Lcom/android/fmradio/TunerManagerForExt;->getRdsRTInfo()Ljava/lang/String;

    move-result-object v1

    invoke-virtual {v0, v1}, Lcom/android/fmradio/views/RadioStatusView;->updateRdsRTInfo(Ljava/lang/String;)V

    return-void
.end method

.method private onUpdaterRdsPsPresetList()V
    .locals 2

    .line 318
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity$3;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget-object v1, v0, Lcom/android/fmradio/FmMainActivity;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    invoke-virtual {v1}, Lcom/android/fmradio/TunerManagerForExt;->getFrequency()I

    move-result v1

    iput v1, v0, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    .line 319
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity$3;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget-object v1, v0, Lcom/android/fmradio/FmMainActivity;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    invoke-virtual {v1}, Lcom/android/fmradio/TunerManagerForExt;->getBand()I

    move-result v1

    iput v1, v0, Lcom/android/fmradio/FmMainActivity;->mBand:I

    .line 320
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity$3;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget-object v1, v0, Lcom/android/fmradio/FmMainActivity;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    invoke-virtual {v1}, Lcom/android/fmradio/TunerManagerForExt;->getPresetIndex()I

    move-result v1

    iput v1, v0, Lcom/android/fmradio/FmMainActivity;->mPresetIndex:I

    .line 322
    new-instance v0, Ljava/lang/StringBuilder;

    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    const-string v1, "mCurrentStation: "

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-object v1, p0, Lcom/android/fmradio/FmMainActivity$3;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget v1, v1, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v1, " - mBand: "

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-object v1, p0, Lcom/android/fmradio/FmMainActivity$3;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget v1, v1, Lcom/android/fmradio/FmMainActivity;->mBand:I

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v1, " - mPresetIndex: "

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-object v1, p0, Lcom/android/fmradio/FmMainActivity$3;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget v1, v1, Lcom/android/fmradio/FmMainActivity;->mPresetIndex:I

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    .line 325
    invoke-static {}, Lcom/android/fmradio/FmMainActivity;->access$100()Ljava/lang/String;

    move-result-object v1

    invoke-static {v1, v0}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 327
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity$3;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {v0}, Lcom/android/fmradio/FmMainActivity;->updateRdsPsPresetList()V

    return-void
.end method

.method private updateSTAndLocStatus(II)V
    .locals 1

    .line 273
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity$3;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-static {v0}, Lcom/android/fmradio/FmMainActivity;->access$600(Lcom/android/fmradio/FmMainActivity;)Lcom/android/fmradio/views/RadioStatusView;

    move-result-object v0

    invoke-virtual {v0, p1, p2}, Lcom/android/fmradio/views/RadioStatusView;->updateStAndLocStatus(II)V

    .line 275
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$3;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-static {p1}, Lcom/android/fmradio/FmMainActivity;->access$700(Lcom/android/fmradio/FmMainActivity;)Lcom/android/fmradio/views/RadioOperationView;

    move-result-object p1

    invoke-virtual {p1}, Lcom/android/fmradio/views/RadioOperationView;->updateLocView()V

    return-void
.end method


# virtual methods
.method public handleMessage(Landroid/os/Message;)V
    .locals 5

    .line 332
    invoke-static {}, Lcom/android/fmradio/FmMainActivity;->access$100()Ljava/lang/String;

    move-result-object v0

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "mHandler.handleMessage, what = "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget v2, p1, Landroid/os/Message;->what:I

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 334
    iget v0, p1, Landroid/os/Message;->what:I

    const/16 v1, 0xd

    if-eq v0, v1, :cond_a

    const/16 v1, 0xf

    const-string v2, "key_tune_to_station"

    if-eq v0, v1, :cond_8

    const/16 v1, 0x1e

    const/4 v3, 0x1

    if-eq v0, v1, :cond_7

    const/16 v1, 0x33

    if-eq v0, v1, :cond_6

    const/16 v1, 0x2a

    const/4 v4, 0x0

    if-eq v0, v1, :cond_3

    const/16 v1, 0x2b

    if-eq v0, v1, :cond_0

    packed-switch v0, :pswitch_data_0

    packed-switch v0, :pswitch_data_1

    goto/16 :goto_0

    .line 368
    :pswitch_0
    invoke-direct {p0}, Lcom/android/fmradio/FmMainActivity$3;->onUpdaterRdsPsPresetList()V

    goto/16 :goto_0

    .line 364
    :pswitch_1
    invoke-direct {p0}, Lcom/android/fmradio/FmMainActivity$3;->onUpdateRdsRtInfo()V

    goto/16 :goto_0

    .line 360
    :pswitch_2
    invoke-direct {p0}, Lcom/android/fmradio/FmMainActivity$3;->onUpdateRdsPsInfo()V

    goto/16 :goto_0

    .line 356
    :pswitch_3
    invoke-direct {p0}, Lcom/android/fmradio/FmMainActivity$3;->onUpdateRdsPtyTypeInfo()V

    goto/16 :goto_0

    .line 352
    :pswitch_4
    invoke-direct {p0}, Lcom/android/fmradio/FmMainActivity$3;->onUpdateRdsInfo()V

    goto/16 :goto_0

    .line 348
    :pswitch_5
    invoke-direct {p0}, Lcom/android/fmradio/FmMainActivity$3;->onUpdatePresetList()V

    goto/16 :goto_0

    .line 344
    :pswitch_6
    invoke-direct {p0}, Lcom/android/fmradio/FmMainActivity$3;->onUpdateControlInfo()V

    goto/16 :goto_0

    .line 340
    :pswitch_7
    invoke-direct {p0}, Lcom/android/fmradio/FmMainActivity$3;->onUpdateRangInfo()V

    goto/16 :goto_0

    .line 439
    :pswitch_8
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$3;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {p1}, Lcom/android/fmradio/FmMainActivity;->finish()V

    goto/16 :goto_0

    .line 408
    :pswitch_9
    invoke-virtual {p1}, Landroid/os/Message;->getData()Landroid/os/Bundle;

    goto/16 :goto_0

    .line 396
    :pswitch_a
    invoke-virtual {p1}, Landroid/os/Message;->getData()Landroid/os/Bundle;

    move-result-object p1

    .line 397
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity$3;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-static {v0}, Lcom/android/fmradio/FmMainActivity;->access$1200(Lcom/android/fmradio/FmMainActivity;)Lcom/android/fmradio/FmService;

    move-result-object v0

    invoke-virtual {v0}, Lcom/android/fmradio/FmService;->getPowerStatus()I

    move-result v0

    sget v1, Lcom/android/fmradio/FmService;->POWER_UP:I

    .line 398
    invoke-virtual {p1, v2}, Landroid/os/Bundle;->getInt(Ljava/lang/String;)I

    goto/16 :goto_0

    .line 384
    :cond_0
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity$3;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {v0}, Lcom/android/fmradio/FmMainActivity;->stopScan()Z

    move-result v0

    if-eqz v0, :cond_1

    goto/16 :goto_0

    .line 387
    :cond_1
    iget-object p1, p1, Landroid/os/Message;->obj:Ljava/lang/Object;

    check-cast p1, Landroid/view/KeyEvent;

    if-eqz p1, :cond_2

    .line 388
    invoke-virtual {p1}, Landroid/view/KeyEvent;->isLongPress()Z

    move-result p1

    if-eqz p1, :cond_2

    .line 389
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$3;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget v0, p1, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    invoke-virtual {p1, v0, v3}, Lcom/android/fmradio/FmMainActivity;->onSeekStation(IZ)V

    goto/16 :goto_0

    .line 391
    :cond_2
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$3;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {p1, v4}, Lcom/android/fmradio/FmMainActivity;->onNextOrPreStation(Z)V

    goto/16 :goto_0

    .line 372
    :cond_3
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity$3;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {v0}, Lcom/android/fmradio/FmMainActivity;->stopScan()Z

    move-result v0

    if-eqz v0, :cond_4

    goto/16 :goto_0

    .line 375
    :cond_4
    iget-object p1, p1, Landroid/os/Message;->obj:Ljava/lang/Object;

    check-cast p1, Landroid/view/KeyEvent;

    if-eqz p1, :cond_5

    .line 376
    invoke-virtual {p1}, Landroid/view/KeyEvent;->isLongPress()Z

    move-result p1

    if-eqz p1, :cond_5

    .line 377
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$3;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget v0, p1, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    invoke-virtual {p1, v0, v4}, Lcom/android/fmradio/FmMainActivity;->onSeekStation(IZ)V

    goto/16 :goto_0

    .line 379
    :cond_5
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$3;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {p1, v3}, Lcom/android/fmradio/FmMainActivity;->onNextOrPreStation(Z)V

    goto/16 :goto_0

    .line 336
    :cond_6
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$3;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-static {p1}, Lcom/android/fmradio/FmMainActivity;->access$1100(Lcom/android/fmradio/FmMainActivity;)V

    goto/16 :goto_0

    .line 451
    :cond_7
    invoke-virtual {p1}, Landroid/os/Message;->getData()Landroid/os/Bundle;

    move-result-object p1

    const-string v0, "key_audiofocus_changed"

    .line 452
    invoke-virtual {p1, v0}, Landroid/os/Bundle;->getInt(Ljava/lang/String;)I

    move-result p1

    .line 453
    invoke-static {}, Lcom/android/fmradio/FmMainActivity;->access$100()Ljava/lang/String;

    move-result-object v0

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "handleMessage - KEY_AUDIOFOCUS_CHANGED - focusState: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    const/4 v0, -0x1

    goto/16 :goto_0

    .line 412
    :cond_8
    invoke-virtual {p1}, Landroid/os/Message;->getData()Landroid/os/Bundle;

    move-result-object p1

    const-string v0, "key_is_tune"

    .line 413
    invoke-virtual {p1, v0}, Landroid/os/Bundle;->getBoolean(Ljava/lang/String;)Z

    move-result v0

    .line 414
    iget-object v1, p0, Lcom/android/fmradio/FmMainActivity$3;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-static {v1}, Lcom/android/fmradio/FmMainActivity;->access$1200(Lcom/android/fmradio/FmMainActivity;)Lcom/android/fmradio/FmService;

    move-result-object v1

    invoke-virtual {v1}, Lcom/android/fmradio/FmService;->getPowerStatus()I

    move-result v1

    sget v3, Lcom/android/fmradio/FmService;->POWER_UP:I

    const-string v1, "key_need_notify_info"

    .line 415
    invoke-virtual {p1, v1}, Landroid/os/Bundle;->getBoolean(Ljava/lang/String;)Z

    .line 418
    invoke-virtual {p1, v2}, Landroid/os/Bundle;->getFloat(Ljava/lang/String;)F

    if-nez v0, :cond_9

    .line 425
    invoke-static {}, Lcom/android/fmradio/FmMainActivity;->access$100()Ljava/lang/String;

    move-result-object p1

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "mHandler.tune: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, v0}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    invoke-static {p1, v0}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    return-void

    .line 434
    :cond_9
    invoke-static {}, Lcom/android/fmradio/FmMainActivity;->access$100()Ljava/lang/String;

    move-result-object p1

    new-instance v0, Ljava/lang/StringBuilder;

    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    const-string v1, "----->>handleMessage() ---MMM_MSGID_TUNE_FINISHED----mCurrentStation: "

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-object v1, p0, Lcom/android/fmradio/FmMainActivity$3;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget v1, v1, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    invoke-static {p1, v0}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    goto :goto_0

    .line 443
    :cond_a
    invoke-virtual {p1}, Landroid/os/Message;->getData()Landroid/os/Bundle;

    move-result-object p1

    const-string v0, "key_is_scan"

    .line 444
    invoke-virtual {p1, v0}, Landroid/os/Bundle;->getBoolean(Ljava/lang/String;)Z

    move-result v0

    const-string v1, "key_station_num"

    .line 445
    invoke-virtual {p1, v1}, Landroid/os/Bundle;->getInt(Ljava/lang/String;)I

    move-result p1

    .line 446
    invoke-static {}, Lcom/android/fmradio/FmMainActivity;->access$100()Ljava/lang/String;

    move-result-object v1

    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2}, Ljava/lang/StringBuilder;-><init>()V

    const-string v3, "----->>handleMessage() ---MSGID_SCAN_FINISHED----isScan: "

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2, v0}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    const-string v0, " searchedNum: "

    invoke-virtual {v2, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p1

    invoke-static {v1, p1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    :goto_0
    return-void

    :pswitch_data_0
    .packed-switch 0x9
        :pswitch_a
        :pswitch_9
        :pswitch_8
    .end packed-switch

    :pswitch_data_1
    .packed-switch 0x36
        :pswitch_7
        :pswitch_6
        :pswitch_5
        :pswitch_4
        :pswitch_3
        :pswitch_2
        :pswitch_1
        :pswitch_0
    .end packed-switch
.end method
