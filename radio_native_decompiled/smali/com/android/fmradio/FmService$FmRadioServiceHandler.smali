.class Lcom/android/fmradio/FmService$FmRadioServiceHandler;
.super Landroid/os/Handler;
.source "FmService.java"


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Lcom/android/fmradio/FmService;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x0
    name = "FmRadioServiceHandler"
.end annotation


# instance fields
.field final synthetic this$0:Lcom/android/fmradio/FmService;


# direct methods
.method public constructor <init>(Lcom/android/fmradio/FmService;Landroid/os/Looper;)V
    .locals 0

    .line 1472
    iput-object p1, p0, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->this$0:Lcom/android/fmradio/FmService;

    .line 1473
    invoke-direct {p0, p2}, Landroid/os/Handler;-><init>(Landroid/os/Looper;)V

    return-void
.end method


# virtual methods
.method public handleMessage(Landroid/os/Message;)V
    .locals 9

    .line 1482
    iget v0, p1, Landroid/os/Message;->what:I

    const/4 v1, -0x2

    const/16 v2, 0xf

    if-eq v0, v1, :cond_10

    const/4 v1, 0x5

    if-eq v0, v1, :cond_f

    const/4 v1, 0x7

    if-eq v0, v1, :cond_e

    const/16 v1, 0xb

    const/16 v3, 0xd

    const/4 v4, 0x2

    const/4 v5, 0x1

    const/4 v6, 0x0

    if-eq v0, v3, :cond_6

    const/16 v3, 0x1e

    if-eq v0, v3, :cond_5

    if-eq v0, v2, :cond_3

    const/16 v3, 0x10

    if-eq v0, v3, :cond_1

    packed-switch v0, :pswitch_data_0

    goto/16 :goto_4

    .line 1503
    :pswitch_0
    invoke-static {}, Lcom/android/fmradio/FmService;->access$000()Ljava/lang/String;

    move-result-object p1

    const-string v0, "---->>handleMessage() MSGID_FM_EXIT"

    invoke-static {p1, v0}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 1504
    iget-object p1, p0, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->this$0:Lcom/android/fmradio/FmService;

    invoke-static {p1}, Lcom/android/fmradio/FmService;->access$1600(Lcom/android/fmradio/FmService;)Z

    move-result p1

    if-eqz p1, :cond_0

    .line 1505
    iget-object p1, p0, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->this$0:Lcom/android/fmradio/FmService;

    invoke-static {p1, v6}, Lcom/android/fmradio/FmService;->access$1700(Lcom/android/fmradio/FmService;Z)V

    .line 1507
    :cond_0
    iget-object p1, p0, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->this$0:Lcom/android/fmradio/FmService;

    invoke-static {p1}, Lcom/android/fmradio/FmService;->access$1800(Lcom/android/fmradio/FmService;)Z

    .line 1508
    iget-object p1, p0, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->this$0:Lcom/android/fmradio/FmService;

    invoke-static {p1}, Lcom/android/fmradio/FmService;->access$1900(Lcom/android/fmradio/FmService;)Z

    .line 1510
    new-instance p1, Landroid/os/Bundle;

    invoke-direct {p1, v5}, Landroid/os/Bundle;-><init>(I)V

    const-string v0, "callback_flag"

    .line 1511
    invoke-virtual {p1, v0, v1}, Landroid/os/Bundle;->putInt(Ljava/lang/String;I)V

    .line 1512
    iget-object v0, p0, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->this$0:Lcom/android/fmradio/FmService;

    invoke-static {v0, p1}, Lcom/android/fmradio/FmService;->access$2000(Lcom/android/fmradio/FmService;Landroid/os/Bundle;)V

    .line 1514
    invoke-static {}, Lcom/android/fmradio/FmService;->access$2100()Lcom/android/fmradio/FmService$OnExitListener;

    move-result-object p1

    if-eqz p1, :cond_12

    .line 1515
    invoke-static {}, Lcom/android/fmradio/FmService;->access$2100()Lcom/android/fmradio/FmService$OnExitListener;

    move-result-object p1

    invoke-interface {p1}, Lcom/android/fmradio/FmService$OnExitListener;->onExit()V

    goto/16 :goto_4

    .line 1498
    :pswitch_1
    iget-object p1, p0, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->this$0:Lcom/android/fmradio/FmService;

    invoke-static {p1}, Lcom/android/fmradio/FmService;->access$1500(Lcom/android/fmradio/FmService;)V

    goto/16 :goto_4

    .line 1492
    :pswitch_2
    invoke-virtual {p1}, Landroid/os/Message;->getData()Landroid/os/Bundle;

    move-result-object p1

    .line 1493
    iget-object v0, p0, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->this$0:Lcom/android/fmradio/FmService;

    invoke-static {v0, p1}, Lcom/android/fmradio/FmService;->access$1400(Lcom/android/fmradio/FmService;Landroid/os/Bundle;)V

    goto/16 :goto_4

    .line 1541
    :cond_1
    invoke-static {}, Lcom/android/fmradio/FmService;->access$000()Ljava/lang/String;

    move-result-object v0

    const-string v1, "---->>handleMessage() MSGID_SEEK_FINISHED"

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 1542
    invoke-virtual {p1}, Landroid/os/Message;->getData()Landroid/os/Bundle;

    move-result-object p1

    .line 1543
    iget-object v0, p0, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->this$0:Lcom/android/fmradio/FmService;

    invoke-static {v0, v5}, Lcom/android/fmradio/FmService;->access$2502(Lcom/android/fmradio/FmService;Z)Z

    .line 1547
    iget-object v0, p0, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->this$0:Lcom/android/fmradio/FmService;

    invoke-virtual {v0, v5}, Lcom/android/fmradio/FmService;->setMute(Z)I

    .line 1551
    iget-object v0, p0, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->this$0:Lcom/android/fmradio/FmService;

    const-string v1, "frequency"

    invoke-virtual {p1, v1}, Landroid/os/Bundle;->getFloat(Ljava/lang/String;)F

    move-result v1

    const-string v3, "option"

    invoke-virtual {p1, v3}, Landroid/os/Bundle;->getBoolean(Ljava/lang/String;)Z

    move-result p1

    invoke-static {v0, v1, p1}, Lcom/android/fmradio/FmService;->access$2600(Lcom/android/fmradio/FmService;FZ)F

    move-result p1

    .line 1553
    iget-object v0, p0, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->this$0:Lcom/android/fmradio/FmService;

    invoke-static {v0}, Lcom/android/fmradio/FmService;->access$2300(Lcom/android/fmradio/FmService;)Landroid/content/Context;

    move-result-object v0

    invoke-static {v0, p1}, Lcom/android/fmradio/utils/FmUtils;->computeStation(Landroid/content/Context;F)I

    move-result v0

    .line 1554
    invoke-static {}, Lcom/android/fmradio/FmService;->access$000()Ljava/lang/String;

    move-result-object v1

    new-instance v3, Ljava/lang/StringBuilder;

    invoke-direct {v3}, Ljava/lang/StringBuilder;-><init>()V

    const-string v5, "---->>handleMessage() MSGID_SEEK_FINISHED station: "

    invoke-virtual {v3, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v3, v0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v5, " seekStation: "

    invoke-virtual {v3, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v3, p1}, Ljava/lang/StringBuilder;->append(F)Ljava/lang/StringBuilder;

    invoke-virtual {v3}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p1

    invoke-static {v1, p1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 1556
    iget-object p1, p0, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->this$0:Lcom/android/fmradio/FmService;

    invoke-static {p1}, Lcom/android/fmradio/FmService;->access$2300(Lcom/android/fmradio/FmService;)Landroid/content/Context;

    move-result-object p1

    invoke-static {p1, v0}, Lcom/android/fmradio/utils/FmUtils;->isValidStation(Landroid/content/Context;I)Z

    move-result p1

    if-eqz p1, :cond_2

    .line 1557
    invoke-static {}, Lcom/android/fmradio/FmService;->access$000()Ljava/lang/String;

    move-result-object p1

    const-string v0, "---->>handleMessage() MSGID_SEEK_FINISHED tuneStation"

    invoke-static {p1, v0}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    goto :goto_0

    .line 1563
    :cond_2
    iget-object p1, p0, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->this$0:Lcom/android/fmradio/FmService;

    invoke-virtual {p1, v6}, Lcom/android/fmradio/FmService;->setMute(Z)I

    .line 1567
    invoke-static {}, Lcom/android/fmradio/FmService;->access$000()Ljava/lang/String;

    move-result-object p1

    new-instance v0, Ljava/lang/StringBuilder;

    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    const-string v1, "---->>handleMessage() MSGID_SEEK_FINISHED can not find tuneStation "

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-object v1, p0, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->this$0:Lcom/android/fmradio/FmService;

    invoke-static {v1}, Lcom/android/fmradio/FmService;->access$2400(Lcom/android/fmradio/FmService;)I

    move-result v1

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    invoke-static {p1, v0}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 1573
    :goto_0
    iget-object p1, p0, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->this$0:Lcom/android/fmradio/FmService;

    invoke-static {p1}, Lcom/android/fmradio/FmService;->access$2300(Lcom/android/fmradio/FmService;)Landroid/content/Context;

    move-result-object p1

    iget-object v0, p0, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->this$0:Lcom/android/fmradio/FmService;

    invoke-static {v0}, Lcom/android/fmradio/FmService;->access$2400(Lcom/android/fmradio/FmService;)I

    move-result v0

    invoke-static {p1, v0}, Lcom/android/fmradio/utils/FmUtils;->computeFrequency(Landroid/content/Context;I)F

    move-result p1

    .line 1575
    new-instance v0, Landroid/os/Bundle;

    invoke-direct {v0, v4}, Landroid/os/Bundle;-><init>(I)V

    const-string v1, "callback_flag"

    .line 1576
    invoke-virtual {v0, v1, v2}, Landroid/os/Bundle;->putInt(Ljava/lang/String;I)V

    const-string v1, "key_is_tune"

    .line 1577
    invoke-virtual {v0, v1, v6}, Landroid/os/Bundle;->putBoolean(Ljava/lang/String;Z)V

    const-string v1, "key_tune_to_station"

    .line 1578
    invoke-virtual {v0, v1, p1}, Landroid/os/Bundle;->putFloat(Ljava/lang/String;F)V

    .line 1579
    iget-object p1, p0, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->this$0:Lcom/android/fmradio/FmService;

    invoke-static {p1, v0}, Lcom/android/fmradio/FmService;->access$2000(Lcom/android/fmradio/FmService;Landroid/os/Bundle;)V

    .line 1580
    iget-object p1, p0, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->this$0:Lcom/android/fmradio/FmService;

    invoke-static {p1, v6}, Lcom/android/fmradio/FmService;->access$2502(Lcom/android/fmradio/FmService;Z)Z

    goto/16 :goto_4

    .line 1521
    :cond_3
    invoke-virtual {p1}, Landroid/os/Message;->getData()Landroid/os/Bundle;

    move-result-object p1

    const-string v0, "frequency"

    .line 1522
    invoke-virtual {p1, v0}, Landroid/os/Bundle;->getFloat(Ljava/lang/String;)F

    move-result v0

    const-string v1, "key_need_notify_info"

    .line 1523
    invoke-virtual {p1, v1}, Landroid/os/Bundle;->getBoolean(Ljava/lang/String;)Z

    move-result p1

    .line 1524
    invoke-static {}, Lcom/android/fmradio/FmService;->access$000()Ljava/lang/String;

    move-result-object v1

    const-string v3, "---->>handleMessage() MSGID_TUNE_FINISHED  tuneStation"

    invoke-static {v1, v3}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 1525
    iget-object v1, p0, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->this$0:Lcom/android/fmradio/FmService;

    invoke-static {v1, v0}, Lcom/android/fmradio/FmService;->access$2200(Lcom/android/fmradio/FmService;F)Z

    move-result v1

    if-nez v1, :cond_4

    .line 1528
    iget-object v0, p0, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->this$0:Lcom/android/fmradio/FmService;

    invoke-static {v0}, Lcom/android/fmradio/FmService;->access$2300(Lcom/android/fmradio/FmService;)Landroid/content/Context;

    move-result-object v0

    iget-object v3, p0, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->this$0:Lcom/android/fmradio/FmService;

    invoke-static {v3}, Lcom/android/fmradio/FmService;->access$2400(Lcom/android/fmradio/FmService;)I

    move-result v3

    invoke-static {v0, v3}, Lcom/android/fmradio/utils/FmUtils;->computeFrequency(Landroid/content/Context;I)F

    move-result v0

    .line 1531
    :cond_4
    new-instance v3, Landroid/os/Bundle;

    const/4 v4, 0x3

    invoke-direct {v3, v4}, Landroid/os/Bundle;-><init>(I)V

    const-string v4, "callback_flag"

    .line 1532
    invoke-virtual {v3, v4, v2}, Landroid/os/Bundle;->putInt(Ljava/lang/String;I)V

    const-string v2, "key_is_tune"

    .line 1533
    invoke-virtual {v3, v2, v1}, Landroid/os/Bundle;->putBoolean(Ljava/lang/String;Z)V

    const-string v1, "key_need_notify_info"

    .line 1534
    invoke-virtual {v3, v1, p1}, Landroid/os/Bundle;->putBoolean(Ljava/lang/String;Z)V

    const-string p1, "key_tune_to_station"

    .line 1535
    invoke-virtual {v3, p1, v0}, Landroid/os/Bundle;->putFloat(Ljava/lang/String;F)V

    .line 1536
    iget-object p1, p0, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->this$0:Lcom/android/fmradio/FmService;

    invoke-static {p1, v3}, Lcom/android/fmradio/FmService;->access$2000(Lcom/android/fmradio/FmService;Landroid/os/Bundle;)V

    goto/16 :goto_4

    .line 1666
    :cond_5
    invoke-virtual {p1}, Landroid/os/Message;->getData()Landroid/os/Bundle;

    move-result-object p1

    const-string v0, "key_audiofocus_changed"

    .line 1667
    invoke-virtual {p1, v0}, Landroid/os/Bundle;->getInt(Ljava/lang/String;)I

    move-result p1

    .line 1668
    iget-object v0, p0, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->this$0:Lcom/android/fmradio/FmService;

    invoke-static {v0, p1}, Lcom/android/fmradio/FmService;->access$3200(Lcom/android/fmradio/FmService;I)V

    .line 1670
    new-instance v0, Landroid/os/Bundle;

    invoke-direct {v0, v4}, Landroid/os/Bundle;-><init>(I)V

    const-string v1, "callback_flag"

    .line 1671
    invoke-virtual {v0, v1, v3}, Landroid/os/Bundle;->putInt(Ljava/lang/String;I)V

    const-string v1, "key_audiofocus_changed"

    .line 1672
    invoke-virtual {v0, v1, p1}, Landroid/os/Bundle;->putInt(Ljava/lang/String;I)V

    .line 1673
    iget-object p1, p0, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->this$0:Lcom/android/fmradio/FmService;

    invoke-static {p1, v0}, Lcom/android/fmradio/FmService;->access$2000(Lcom/android/fmradio/FmService;Landroid/os/Bundle;)V

    goto/16 :goto_4

    .line 1589
    :cond_6
    iget-object v0, p0, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->this$0:Lcom/android/fmradio/FmService;

    invoke-static {v0, v5}, Lcom/android/fmradio/FmService;->access$2702(Lcom/android/fmradio/FmService;Z)Z

    .line 1591
    invoke-static {}, Lcom/android/fmradio/FmService;->access$000()Ljava/lang/String;

    move-result-object v0

    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2}, Ljava/lang/StringBuilder;-><init>()V

    const-string v7, "---->>handleMessage() ----MSGID_SCAN_FINISHED---  isPowerUp: "

    invoke-virtual {v2, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2, v5}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v2

    invoke-static {v0, v2}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 1597
    iget p1, p1, Landroid/os/Message;->arg1:I

    .line 1598
    iget-object v0, p0, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->this$0:Lcom/android/fmradio/FmService;

    invoke-static {v0, p1}, Lcom/android/fmradio/FmService;->access$2800(Lcom/android/fmradio/FmService;I)[I

    move-result-object p1

    .line 1599
    invoke-static {}, Lcom/android/fmradio/FmService;->access$000()Ljava/lang/String;

    move-result-object v0

    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2}, Ljava/lang/StringBuilder;-><init>()V

    const-string v7, "---->>handleMessage() ---000-MSGID_SCAN_FINISHED---  isPowerUp: "

    invoke-virtual {v2, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2, v5}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v2

    invoke-static {v0, v2}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 1601
    invoke-static {}, Lcom/android/fmradio/FmService;->access$000()Ljava/lang/String;

    move-result-object v0

    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2}, Ljava/lang/StringBuilder;-><init>()V

    const-string v7, "---->>handleMessage() ---111-MSGID_SCAN_FINISHED---  isPowerUp: "

    invoke-virtual {v2, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2, v5}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v2

    invoke-static {v0, v2}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 1603
    iget-object v0, p0, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->this$0:Lcom/android/fmradio/FmService;

    invoke-static {v0}, Lcom/android/fmradio/FmService;->access$100(Lcom/android/fmradio/FmService;)Lcom/android/fmradio/FmService$FmRadioServiceHandler;

    move-result-object v0

    invoke-virtual {v0, v1}, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->removeMessages(I)V

    .line 1605
    invoke-static {}, Lcom/android/fmradio/FmService;->access$000()Ljava/lang/String;

    move-result-object v0

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "---->>handleMessage() --222--MSGID_SCAN_FINISHED---  isPowerUp: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, v5}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    const/16 v0, -0x64

    if-eqz p1, :cond_7

    .line 1608
    aget v1, p1, v6

    if-ne v1, v0, :cond_7

    new-array v1, v6, [I

    goto :goto_1

    :cond_7
    move-object v1, p1

    .line 1612
    :goto_1
    iget-object v2, p0, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->this$0:Lcom/android/fmradio/FmService;

    invoke-static {v2}, Lcom/android/fmradio/FmService;->access$2900(Lcom/android/fmradio/FmService;)Ljava/util/ArrayList;

    move-result-object v2

    invoke-virtual {v2}, Ljava/util/ArrayList;->isEmpty()Z

    move-result v2

    if-nez v2, :cond_a

    .line 1613
    invoke-static {}, Lcom/android/fmradio/FmService;->access$000()Ljava/lang/String;

    move-result-object v2

    new-instance v7, Ljava/lang/StringBuilder;

    invoke-direct {v7}, Ljava/lang/StringBuilder;-><init>()V

    const-string v8, "notifyCurrentActivityStateChanged = "

    invoke-virtual {v7, v8}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-object v8, p0, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->this$0:Lcom/android/fmradio/FmService;

    invoke-static {v8}, Lcom/android/fmradio/FmService;->access$2900(Lcom/android/fmradio/FmService;)Ljava/util/ArrayList;

    move-result-object v8

    invoke-virtual {v8}, Ljava/util/ArrayList;->size()I

    move-result v8

    invoke-virtual {v7, v8}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v7}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v7

    invoke-static {v2, v7}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 1614
    iget-object v2, p0, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->this$0:Lcom/android/fmradio/FmService;

    invoke-static {v2}, Lcom/android/fmradio/FmService;->access$2900(Lcom/android/fmradio/FmService;)Ljava/util/ArrayList;

    move-result-object v2

    monitor-enter v2

    .line 1615
    :try_start_0
    iget-object v7, p0, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->this$0:Lcom/android/fmradio/FmService;

    invoke-static {v7}, Lcom/android/fmradio/FmService;->access$2900(Lcom/android/fmradio/FmService;)Ljava/util/ArrayList;

    move-result-object v7

    invoke-virtual {v7}, Ljava/util/ArrayList;->size()I

    move-result v7

    if-lez v7, :cond_9

    .line 1616
    iget-object v7, p0, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->this$0:Lcom/android/fmradio/FmService;

    invoke-static {v7}, Lcom/android/fmradio/FmService;->access$2900(Lcom/android/fmradio/FmService;)Ljava/util/ArrayList;

    move-result-object v7

    iget-object v8, p0, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->this$0:Lcom/android/fmradio/FmService;

    invoke-static {v8}, Lcom/android/fmradio/FmService;->access$2900(Lcom/android/fmradio/FmService;)Ljava/util/ArrayList;

    move-result-object v8

    invoke-virtual {v8}, Ljava/util/ArrayList;->size()I

    move-result v8

    sub-int/2addr v8, v5

    invoke-virtual {v7, v8}, Ljava/util/ArrayList;->get(I)Ljava/lang/Object;

    move-result-object v7

    check-cast v7, Lcom/android/fmradio/FmService$Record;

    .line 1617
    iget-object v8, v7, Lcom/android/fmradio/FmService$Record;->mCallback:Lcom/android/fmradio/iface/FmListener;

    if-nez v8, :cond_8

    .line 1619
    iget-object p1, p0, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->this$0:Lcom/android/fmradio/FmService;

    invoke-static {p1}, Lcom/android/fmradio/FmService;->access$2900(Lcom/android/fmradio/FmService;)Ljava/util/ArrayList;

    move-result-object p1

    invoke-virtual {p1, v7}, Ljava/util/ArrayList;->remove(Ljava/lang/Object;)Z

    .line 1620
    monitor-exit v2

    return-void

    .line 1622
    :cond_8
    invoke-interface {v8, v1}, Lcom/android/fmradio/iface/FmListener;->getChannels([I)V

    .line 1624
    :cond_9
    monitor-exit v2

    goto :goto_2

    :catchall_0
    move-exception p1

    monitor-exit v2
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    throw p1

    :cond_a
    :goto_2
    if-eqz p1, :cond_b

    .line 1630
    aget v1, p1, v6

    if-ne v1, v0, :cond_b

    new-array p1, v4, [I

    .line 1632
    fill-array-data p1, :array_0

    move-object v0, p1

    move p1, v6

    goto :goto_3

    :cond_b
    if-eqz p1, :cond_c

    .line 1637
    invoke-static {}, Lcom/android/fmradio/FmService;->access$000()Ljava/lang/String;

    move-result-object v0

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "---->>handleMessage() "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    aget v2, p1, v6

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 1640
    :cond_c
    iget-object v0, p0, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->this$0:Lcom/android/fmradio/FmService;

    invoke-static {v0, p1}, Lcom/android/fmradio/FmService;->access$3000(Lcom/android/fmradio/FmService;[I)[I

    move-result-object p1

    .line 1641
    aget v0, p1, v6

    .line 1642
    invoke-static {}, Lcom/android/fmradio/FmService;->access$000()Ljava/lang/String;

    move-result-object v0

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "---->>handleMessage() ----MSGID_SCAN_FINISHED---  tuneStation "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-object v2, p0, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->this$0:Lcom/android/fmradio/FmService;

    .line 1643
    invoke-static {v2}, Lcom/android/fmradio/FmService;->access$2400(Lcom/android/fmradio/FmService;)I

    move-result v2

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    .line 1642
    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    move-object v0, p1

    move p1, v5

    .line 1651
    :goto_3
    iget-object v1, p0, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->this$0:Lcom/android/fmradio/FmService;

    iget-boolean v1, v1, Lcom/android/fmradio/FmService;->mIsAudioFocusHeld:Z

    if-eqz v1, :cond_d

    .line 1652
    iget-object v1, p0, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->this$0:Lcom/android/fmradio/FmService;

    invoke-virtual {v1, v6}, Lcom/android/fmradio/FmService;->setMute(Z)I

    .line 1654
    :cond_d
    new-instance v1, Landroid/os/Bundle;

    const/4 v2, 0x4

    invoke-direct {v1, v2}, Landroid/os/Bundle;-><init>(I)V

    const-string v2, "callback_flag"

    .line 1655
    invoke-virtual {v1, v2, v3}, Landroid/os/Bundle;->putInt(Ljava/lang/String;I)V

    .line 1656
    aget v0, v0, v5

    const-string v2, "key_station_num"

    invoke-virtual {v1, v2, v0}, Landroid/os/Bundle;->putInt(Ljava/lang/String;I)V

    const-string v0, "key_is_scan"

    .line 1657
    invoke-virtual {v1, v0, p1}, Landroid/os/Bundle;->putBoolean(Ljava/lang/String;Z)V

    .line 1659
    iget-object p1, p0, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->this$0:Lcom/android/fmradio/FmService;

    invoke-static {p1, v6}, Lcom/android/fmradio/FmService;->access$2702(Lcom/android/fmradio/FmService;Z)Z

    .line 1661
    iget-object p1, p0, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->this$0:Lcom/android/fmradio/FmService;

    invoke-static {p1, v1}, Lcom/android/fmradio/FmService;->access$3100(Lcom/android/fmradio/FmService;Landroid/os/Bundle;)V

    goto :goto_4

    .line 1682
    :cond_e
    invoke-virtual {p1}, Landroid/os/Message;->getData()Landroid/os/Bundle;

    move-result-object p1

    .line 1683
    iget-object v0, p0, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->this$0:Lcom/android/fmradio/FmService;

    const-string v1, "option"

    invoke-virtual {p1, v1}, Landroid/os/Bundle;->getBoolean(Ljava/lang/String;)Z

    move-result p1

    invoke-virtual {v0, p1}, Lcom/android/fmradio/FmService;->setMute(Z)I

    goto :goto_4

    .line 1677
    :cond_f
    invoke-virtual {p1}, Landroid/os/Message;->getData()Landroid/os/Bundle;

    move-result-object p1

    .line 1678
    iget-object v0, p0, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->this$0:Lcom/android/fmradio/FmService;

    const-string v1, "option"

    invoke-virtual {p1, v1}, Landroid/os/Bundle;->getBoolean(Ljava/lang/String;)Z

    move-result p1

    invoke-static {v0, p1}, Lcom/android/fmradio/FmService;->access$3300(Lcom/android/fmradio/FmService;Z)I

    goto :goto_4

    .line 1484
    :cond_10
    iget-object p1, p0, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->this$0:Lcom/android/fmradio/FmService;

    invoke-static {p1}, Lcom/android/fmradio/FmService;->access$1100(Lcom/android/fmradio/FmService;)I

    move-result p1

    if-ge p1, v2, :cond_11

    iget-object p1, p0, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->this$0:Lcom/android/fmradio/FmService;

    invoke-static {p1}, Lcom/android/fmradio/FmService;->access$1200(Lcom/android/fmradio/FmService;)Z

    move-result p1

    if-nez p1, :cond_11

    iget-object p1, p0, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->this$0:Lcom/android/fmradio/FmService;

    invoke-static {p1}, Lcom/android/fmradio/FmService;->access$1300(Lcom/android/fmradio/FmService;)Z

    move-result p1

    if-nez p1, :cond_11

    .line 1485
    iget-object p1, p0, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->this$0:Lcom/android/fmradio/FmService;

    invoke-static {p1}, Lcom/android/fmradio/FmService;->access$1108(Lcom/android/fmradio/FmService;)I

    :cond_11
    const-wide/16 v2, 0x5dc

    .line 1487
    invoke-virtual {p0, v1, v2, v3}, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->sendEmptyMessageDelayed(IJ)Z

    :cond_12
    :goto_4
    return-void

    nop

    :pswitch_data_0
    .packed-switch 0x9
        :pswitch_2
        :pswitch_1
        :pswitch_0
    .end packed-switch

    :array_0
    .array-data 4
        -0x1
        0x0
    .end array-data
.end method
