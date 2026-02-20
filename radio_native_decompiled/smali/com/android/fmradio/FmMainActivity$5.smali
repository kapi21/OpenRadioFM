.class Lcom/android/fmradio/FmMainActivity$5;
.super Ljava/lang/Object;
.source "FmMainActivity.java"

# interfaces
.implements Landroid/qf/util/UtilEventListener;


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

    .line 527
    iput-object p1, p0, Lcom/android/fmradio/FmMainActivity$5;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public onReceived(ILandroid/qf/util/QFKeyEventInfo;)V
    .locals 5

    .line 530
    invoke-virtual {p2}, Landroid/qf/util/QFKeyEventInfo;->getKeyEventInfo()Landroid/view/KeyEvent;

    move-result-object p1

    invoke-virtual {p1}, Landroid/view/KeyEvent;->getAction()I

    move-result p1

    .line 531
    invoke-virtual {p2}, Landroid/qf/util/QFKeyEventInfo;->getKeyEventInfo()Landroid/view/KeyEvent;

    move-result-object p2

    invoke-virtual {p2}, Landroid/view/KeyEvent;->getKeyCode()I

    move-result p2

    const-string v0, "persist.sys.qf.last_audio_src"

    const-string v1, ""

    .line 532
    invoke-static {v0, v1}, Landroid/os/SystemProperties;->get(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;

    move-result-object v0

    .line 533
    invoke-static {}, Lcom/android/fmradio/FmMainActivity;->access$100()Ljava/lang/String;

    move-result-object v1

    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2}, Ljava/lang/StringBuilder;-><init>()V

    const-string v3, "lastAudioSource: "

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    const-string v3, " action: "

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v3, " keyCode: "

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2, p2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v2

    invoke-static {v1, v2}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    const-string v1, "com.android.fmradio"

    .line 537
    invoke-virtual {v0, v1}, Ljava/lang/String;->startsWith(Ljava/lang/String;)Z

    move-result v0

    if-nez v0, :cond_0

    return-void

    :cond_0
    const-string v0, "KEYCODE_KNOB_TUNER"

    .line 541
    invoke-static {v0}, Landroid/qf/os/QFApi;->getKeyCode(Ljava/lang/String;)I

    move-result v0

    const/4 v1, 0x4

    const/4 v2, 0x3

    const/4 v3, 0x0

    const/4 v4, 0x1

    if-ne p2, v0, :cond_2

    if-ne p1, v2, :cond_1

    .line 543
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$5;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget-object p1, p1, Lcom/android/fmradio/FmMainActivity;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    invoke-virtual {p1, v3}, Lcom/android/fmradio/TunerManagerForExt;->onFine(Z)V

    goto/16 :goto_3

    :cond_1
    if-ne p1, v1, :cond_13

    .line 545
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$5;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget-object p1, p1, Lcom/android/fmradio/FmMainActivity;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    invoke-virtual {p1, v4}, Lcom/android/fmradio/TunerManagerForExt;->onFine(Z)V

    goto/16 :goto_3

    :cond_2
    const-string v0, "KEYCODE_STUDY_RADIO"

    .line 547
    invoke-static {v0}, Landroid/qf/os/QFApi;->getKeyCode(Ljava/lang/String;)I

    move-result v0

    if-ne p2, v0, :cond_4

    if-ne p1, v4, :cond_13

    .line 548
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$5;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-static {p1}, Lcom/android/fmradio/FmMainActivity;->access$1600(Lcom/android/fmradio/FmMainActivity;)Z

    move-result p1

    if-eqz p1, :cond_13

    .line 549
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$5;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget p1, p1, Lcom/android/fmradio/FmMainActivity;->mBand:I

    if-ltz p1, :cond_3

    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$5;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget p1, p1, Lcom/android/fmradio/FmMainActivity;->mBand:I

    if-ge p1, v1, :cond_3

    .line 550
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$5;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {p1}, Lcom/android/fmradio/FmMainActivity;->onBand()V

    goto/16 :goto_3

    .line 552
    :cond_3
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$5;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-static {p1, v3}, Lcom/android/fmradio/FmMainActivity;->access$2000(Lcom/android/fmradio/FmMainActivity;I)V

    goto/16 :goto_3

    :cond_4
    const-string v0, "KEYCODE_STUDY_RADIO_FM"

    .line 555
    invoke-static {v0}, Landroid/qf/os/QFApi;->getKeyCode(Ljava/lang/String;)I

    move-result v0

    if-ne p2, v0, :cond_6

    if-ne p1, v4, :cond_13

    .line 556
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$5;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-static {p1}, Lcom/android/fmradio/FmMainActivity;->access$1600(Lcom/android/fmradio/FmMainActivity;)Z

    move-result p1

    if-eqz p1, :cond_13

    .line 557
    invoke-static {}, Lcom/android/fmradio/FmMainActivity;->access$100()Ljava/lang/String;

    move-result-object p1

    new-instance p2, Ljava/lang/StringBuilder;

    invoke-direct {p2}, Ljava/lang/StringBuilder;-><init>()V

    const-string v0, "KEYCODE_STUDY_RADIO_FM - mBand: "

    invoke-virtual {p2, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity$5;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget v0, v0, Lcom/android/fmradio/FmMainActivity;->mBand:I

    invoke-virtual {p2, v0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {p2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p2

    invoke-static {p1, p2}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 558
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$5;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget p1, p1, Lcom/android/fmradio/FmMainActivity;->mBand:I

    if-ltz p1, :cond_5

    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$5;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget p1, p1, Lcom/android/fmradio/FmMainActivity;->mBand:I

    const/4 p2, 0x2

    if-ge p1, p2, :cond_5

    .line 559
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$5;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {p1}, Lcom/android/fmradio/FmMainActivity;->onBand()V

    goto/16 :goto_3

    .line 561
    :cond_5
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$5;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-static {p1, v3}, Lcom/android/fmradio/FmMainActivity;->access$2000(Lcom/android/fmradio/FmMainActivity;I)V

    goto/16 :goto_3

    :cond_6
    const-string v0, "KEYCODE_STUDY_RADIO_AM"

    .line 564
    invoke-static {v0}, Landroid/qf/os/QFApi;->getKeyCode(Ljava/lang/String;)I

    move-result v0

    if-ne p2, v0, :cond_8

    if-ne p1, v4, :cond_13

    .line 565
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$5;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-static {p1}, Lcom/android/fmradio/FmMainActivity;->access$1600(Lcom/android/fmradio/FmMainActivity;)Z

    move-result p1

    if-eqz p1, :cond_13

    .line 566
    invoke-static {}, Lcom/android/fmradio/FmMainActivity;->access$100()Ljava/lang/String;

    move-result-object p1

    new-instance p2, Ljava/lang/StringBuilder;

    invoke-direct {p2}, Ljava/lang/StringBuilder;-><init>()V

    const-string v0, "KEYCODE_STUDY_RADIO_AM - mBand: "

    invoke-virtual {p2, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity$5;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget v0, v0, Lcom/android/fmradio/FmMainActivity;->mBand:I

    invoke-virtual {p2, v0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {p2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p2

    invoke-static {p1, p2}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 567
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$5;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget p1, p1, Lcom/android/fmradio/FmMainActivity;->mBand:I

    if-ne p1, v2, :cond_7

    .line 568
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$5;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {p1}, Lcom/android/fmradio/FmMainActivity;->onBand()V

    goto/16 :goto_3

    .line 570
    :cond_7
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$5;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-static {p1, v2}, Lcom/android/fmradio/FmMainActivity;->access$2000(Lcom/android/fmradio/FmMainActivity;I)V

    goto/16 :goto_3

    :cond_8
    const-string v0, "KEYCODE_STUDY_SAVE"

    .line 573
    invoke-static {v0}, Landroid/qf/os/QFApi;->getKeyCode(Ljava/lang/String;)I

    move-result v0

    if-ne p2, v0, :cond_9

    if-ne p1, v4, :cond_13

    .line 575
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$5;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {p1}, Lcom/android/fmradio/FmMainActivity;->onAuto()V

    goto/16 :goto_3

    :cond_9
    const/16 v0, 0x112

    if-eq p2, v0, :cond_12

    const-string v0, "KEYCODE_STUDY_STEP_FORWARD"

    .line 578
    invoke-static {v0}, Landroid/qf/os/QFApi;->getKeyCode(Ljava/lang/String;)I

    move-result v0

    if-ne p2, v0, :cond_a

    goto/16 :goto_2

    :cond_a
    const/16 v0, 0x113

    if-eq p2, v0, :cond_11

    const-string v0, "KEYCODE_STUDY_STEP_BACKWARD"

    .line 583
    invoke-static {v0}, Landroid/qf/os/QFApi;->getKeyCode(Ljava/lang/String;)I

    move-result v0

    if-ne p2, v0, :cond_b

    goto/16 :goto_1

    :cond_b
    const/16 v0, 0x110

    if-ne p2, v0, :cond_c

    if-ne p1, v4, :cond_13

    .line 589
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$5;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget p2, p1, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    invoke-virtual {p1, p2, v4}, Lcom/android/fmradio/FmMainActivity;->onSeekStation(IZ)V

    goto/16 :goto_3

    :cond_c
    const/16 v0, 0x111

    if-ne p2, v0, :cond_d

    if-ne p1, v4, :cond_13

    .line 593
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$5;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget p2, p1, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    invoke-virtual {p1, p2, v3}, Lcom/android/fmradio/FmMainActivity;->onSeekStation(IZ)V

    goto/16 :goto_3

    :cond_d
    const-string v0, "KEYCODE_STUDY_M1"

    .line 595
    invoke-static {v0}, Landroid/qf/os/QFApi;->getKeyCode(Ljava/lang/String;)I

    move-result v1

    if-eq p2, v1, :cond_10

    const-string v1, "KEYCODE_STUDY_M2"

    .line 596
    invoke-static {v1}, Landroid/qf/os/QFApi;->getKeyCode(Ljava/lang/String;)I

    move-result v1

    if-eq p2, v1, :cond_10

    const-string v1, "KEYCODE_STUDY_M3"

    .line 597
    invoke-static {v1}, Landroid/qf/os/QFApi;->getKeyCode(Ljava/lang/String;)I

    move-result v1

    if-eq p2, v1, :cond_10

    const-string v1, "KEYCODE_STUDY_M4"

    .line 598
    invoke-static {v1}, Landroid/qf/os/QFApi;->getKeyCode(Ljava/lang/String;)I

    move-result v1

    if-eq p2, v1, :cond_10

    const-string v1, "KEYCODE_STUDY_M5"

    .line 599
    invoke-static {v1}, Landroid/qf/os/QFApi;->getKeyCode(Ljava/lang/String;)I

    move-result v1

    if-eq p2, v1, :cond_10

    const-string v1, "KEYCODE_STUDY_M6"

    .line 600
    invoke-static {v1}, Landroid/qf/os/QFApi;->getKeyCode(Ljava/lang/String;)I

    move-result v1

    if-ne p2, v1, :cond_e

    goto :goto_0

    :cond_e
    const/16 v0, 0x8

    if-eq p2, v0, :cond_f

    const/16 v1, 0x9

    if-eq p2, v1, :cond_f

    const/16 v1, 0xa

    if-eq p2, v1, :cond_f

    const/16 v1, 0xb

    if-eq p2, v1, :cond_f

    const/16 v1, 0xc

    if-eq p2, v1, :cond_f

    const/16 v1, 0xd

    if-ne p2, v1, :cond_13

    :cond_f
    if-ne p1, v4, :cond_13

    .line 610
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$5;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-static {p1}, Lcom/android/fmradio/FmMainActivity;->access$2100(Lcom/android/fmradio/FmMainActivity;)Lcom/android/fmradio/views/RadioPresetListView;

    move-result-object p1

    if-eqz p1, :cond_13

    .line 611
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$5;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-static {p1}, Lcom/android/fmradio/FmMainActivity;->access$2100(Lcom/android/fmradio/FmMainActivity;)Lcom/android/fmradio/views/RadioPresetListView;

    move-result-object p1

    sub-int/2addr p2, v0

    invoke-virtual {p1, p2}, Lcom/android/fmradio/views/RadioPresetListView;->onClickItem(I)V

    goto :goto_3

    :cond_10
    :goto_0
    if-ne p1, v4, :cond_13

    .line 601
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$5;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-static {p1}, Lcom/android/fmradio/FmMainActivity;->access$2100(Lcom/android/fmradio/FmMainActivity;)Lcom/android/fmradio/views/RadioPresetListView;

    move-result-object p1

    if-eqz p1, :cond_13

    .line 602
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$5;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-static {p1}, Lcom/android/fmradio/FmMainActivity;->access$2100(Lcom/android/fmradio/FmMainActivity;)Lcom/android/fmradio/views/RadioPresetListView;

    move-result-object p1

    invoke-static {v0}, Landroid/qf/os/QFApi;->getKeyCode(Ljava/lang/String;)I

    move-result v0

    sub-int/2addr p2, v0

    invoke-virtual {p1, p2}, Lcom/android/fmradio/views/RadioPresetListView;->onClickItem(I)V

    goto :goto_3

    :cond_11
    :goto_1
    if-ne p1, v4, :cond_13

    .line 585
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$5;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {p1, v4}, Lcom/android/fmradio/FmMainActivity;->onFine(Z)V

    goto :goto_3

    :cond_12
    :goto_2
    if-ne p1, v4, :cond_13

    .line 580
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$5;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {p1, v3}, Lcom/android/fmradio/FmMainActivity;->onFine(Z)V

    :cond_13
    :goto_3
    return-void
.end method
