.class Lcom/broadcom/fm/fmreceiver/FmProxy$FmReceiverCallback;
.super Lcom/broadcom/fm/fmreceiver/IFmReceiverCallback$Stub;
.source "FmProxy.java"


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Lcom/broadcom/fm/fmreceiver/FmProxy;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x2
    name = "FmReceiverCallback"
.end annotation


# instance fields
.field final synthetic this$0:Lcom/broadcom/fm/fmreceiver/FmProxy;


# direct methods
.method private constructor <init>(Lcom/broadcom/fm/fmreceiver/FmProxy;)V
    .locals 0

    .line 1383
    iput-object p1, p0, Lcom/broadcom/fm/fmreceiver/FmProxy$FmReceiverCallback;->this$0:Lcom/broadcom/fm/fmreceiver/FmProxy;

    invoke-direct {p0}, Lcom/broadcom/fm/fmreceiver/IFmReceiverCallback$Stub;-><init>()V

    return-void
.end method

.method synthetic constructor <init>(Lcom/broadcom/fm/fmreceiver/FmProxy;Lcom/broadcom/fm/fmreceiver/FmProxy$1;)V
    .locals 0

    .line 1383
    invoke-direct {p0, p1}, Lcom/broadcom/fm/fmreceiver/FmProxy$FmReceiverCallback;-><init>(Lcom/broadcom/fm/fmreceiver/FmProxy;)V

    return-void
.end method


# virtual methods
.method public declared-synchronized onAudioModeEvent(I)V
    .locals 1
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Landroid/os/RemoteException;
        }
    .end annotation

    monitor-enter p0

    .line 1423
    :try_start_0
    iget-object v0, p0, Lcom/broadcom/fm/fmreceiver/FmProxy$FmReceiverCallback;->this$0:Lcom/broadcom/fm/fmreceiver/FmProxy;

    invoke-static {v0}, Lcom/broadcom/fm/fmreceiver/FmProxy;->access$100(Lcom/broadcom/fm/fmreceiver/FmProxy;)Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;

    move-result-object v0

    if-eqz v0, :cond_0

    .line 1424
    iget-object v0, p0, Lcom/broadcom/fm/fmreceiver/FmProxy$FmReceiverCallback;->this$0:Lcom/broadcom/fm/fmreceiver/FmProxy;

    invoke-static {v0}, Lcom/broadcom/fm/fmreceiver/FmProxy;->access$100(Lcom/broadcom/fm/fmreceiver/FmProxy;)Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;

    move-result-object v0

    invoke-interface {v0, p1}, Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;->onAudioModeEvent(I)V
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 1425
    :cond_0
    monitor-exit p0

    return-void

    :catchall_0
    move-exception p1

    monitor-exit p0

    throw p1
.end method

.method public declared-synchronized onAudioPathEvent(I)V
    .locals 1
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Landroid/os/RemoteException;
        }
    .end annotation

    monitor-enter p0

    .line 1430
    :try_start_0
    iget-object v0, p0, Lcom/broadcom/fm/fmreceiver/FmProxy$FmReceiverCallback;->this$0:Lcom/broadcom/fm/fmreceiver/FmProxy;

    invoke-static {v0}, Lcom/broadcom/fm/fmreceiver/FmProxy;->access$100(Lcom/broadcom/fm/fmreceiver/FmProxy;)Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;

    move-result-object v0

    if-eqz v0, :cond_0

    .line 1431
    iget-object v0, p0, Lcom/broadcom/fm/fmreceiver/FmProxy$FmReceiverCallback;->this$0:Lcom/broadcom/fm/fmreceiver/FmProxy;

    invoke-static {v0}, Lcom/broadcom/fm/fmreceiver/FmProxy;->access$100(Lcom/broadcom/fm/fmreceiver/FmProxy;)Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;

    move-result-object v0

    invoke-interface {v0, p1}, Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;->onAudioPathEvent(I)V
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 1432
    :cond_0
    monitor-exit p0

    return-void

    :catchall_0
    move-exception p1

    monitor-exit p0

    throw p1
.end method

.method public declared-synchronized onEstimateNflEvent(I)V
    .locals 1
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Landroid/os/RemoteException;
        }
    .end annotation

    monitor-enter p0

    .line 1437
    :try_start_0
    iget-object v0, p0, Lcom/broadcom/fm/fmreceiver/FmProxy$FmReceiverCallback;->this$0:Lcom/broadcom/fm/fmreceiver/FmProxy;

    invoke-static {v0}, Lcom/broadcom/fm/fmreceiver/FmProxy;->access$100(Lcom/broadcom/fm/fmreceiver/FmProxy;)Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;

    move-result-object v0

    if-eqz v0, :cond_0

    .line 1438
    iget-object v0, p0, Lcom/broadcom/fm/fmreceiver/FmProxy$FmReceiverCallback;->this$0:Lcom/broadcom/fm/fmreceiver/FmProxy;

    invoke-static {v0}, Lcom/broadcom/fm/fmreceiver/FmProxy;->access$100(Lcom/broadcom/fm/fmreceiver/FmProxy;)Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;

    move-result-object v0

    invoke-interface {v0, p1}, Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;->onEstimateNoiseFloorLevelEvent(I)V
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 1439
    :cond_0
    monitor-exit p0

    return-void

    :catchall_0
    move-exception p1

    monitor-exit p0

    throw p1
.end method

.method public declared-synchronized onLiveAudioQualityEvent(II)V
    .locals 1
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Landroid/os/RemoteException;
        }
    .end annotation

    monitor-enter p0

    .line 1444
    :try_start_0
    iget-object v0, p0, Lcom/broadcom/fm/fmreceiver/FmProxy$FmReceiverCallback;->this$0:Lcom/broadcom/fm/fmreceiver/FmProxy;

    invoke-static {v0}, Lcom/broadcom/fm/fmreceiver/FmProxy;->access$100(Lcom/broadcom/fm/fmreceiver/FmProxy;)Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;

    move-result-object v0

    if-eqz v0, :cond_0

    .line 1445
    iget-object v0, p0, Lcom/broadcom/fm/fmreceiver/FmProxy$FmReceiverCallback;->this$0:Lcom/broadcom/fm/fmreceiver/FmProxy;

    invoke-static {v0}, Lcom/broadcom/fm/fmreceiver/FmProxy;->access$100(Lcom/broadcom/fm/fmreceiver/FmProxy;)Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;

    move-result-object v0

    invoke-interface {v0, p1, p2}, Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;->onLiveAudioQualityEvent(II)V
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 1446
    :cond_0
    monitor-exit p0

    return-void

    :catchall_0
    move-exception p1

    monitor-exit p0

    throw p1
.end method

.method public declared-synchronized onRdsDataEvent(IILjava/lang/String;)V
    .locals 1
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Landroid/os/RemoteException;
        }
    .end annotation

    monitor-enter p0

    .line 1416
    :try_start_0
    iget-object v0, p0, Lcom/broadcom/fm/fmreceiver/FmProxy$FmReceiverCallback;->this$0:Lcom/broadcom/fm/fmreceiver/FmProxy;

    invoke-static {v0}, Lcom/broadcom/fm/fmreceiver/FmProxy;->access$100(Lcom/broadcom/fm/fmreceiver/FmProxy;)Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;

    move-result-object v0

    if-eqz v0, :cond_0

    .line 1417
    iget-object v0, p0, Lcom/broadcom/fm/fmreceiver/FmProxy$FmReceiverCallback;->this$0:Lcom/broadcom/fm/fmreceiver/FmProxy;

    invoke-static {v0}, Lcom/broadcom/fm/fmreceiver/FmProxy;->access$100(Lcom/broadcom/fm/fmreceiver/FmProxy;)Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;

    move-result-object v0

    invoke-interface {v0, p1, p2, p3}, Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;->onRdsDataEvent(IILjava/lang/String;)V
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 1418
    :cond_0
    monitor-exit p0

    return-void

    :catchall_0
    move-exception p1

    monitor-exit p0

    throw p1
.end method

.method public declared-synchronized onRdsModeEvent(II)V
    .locals 1
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Landroid/os/RemoteException;
        }
    .end annotation

    monitor-enter p0

    .line 1408
    :try_start_0
    iget-object v0, p0, Lcom/broadcom/fm/fmreceiver/FmProxy$FmReceiverCallback;->this$0:Lcom/broadcom/fm/fmreceiver/FmProxy;

    invoke-static {v0}, Lcom/broadcom/fm/fmreceiver/FmProxy;->access$100(Lcom/broadcom/fm/fmreceiver/FmProxy;)Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;

    move-result-object v0

    if-eqz v0, :cond_0

    .line 1409
    iget-object v0, p0, Lcom/broadcom/fm/fmreceiver/FmProxy$FmReceiverCallback;->this$0:Lcom/broadcom/fm/fmreceiver/FmProxy;

    invoke-static {v0}, Lcom/broadcom/fm/fmreceiver/FmProxy;->access$100(Lcom/broadcom/fm/fmreceiver/FmProxy;)Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;

    move-result-object v0

    invoke-interface {v0, p1, p2}, Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;->onRdsModeEvent(II)V
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 1410
    :cond_0
    monitor-exit p0

    return-void

    :catchall_0
    move-exception p1

    monitor-exit p0

    throw p1
.end method

.method public declared-synchronized onSeekCompleteEvent(IIIZ)V
    .locals 1
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Landroid/os/RemoteException;
        }
    .end annotation

    monitor-enter p0

    .line 1401
    :try_start_0
    iget-object v0, p0, Lcom/broadcom/fm/fmreceiver/FmProxy$FmReceiverCallback;->this$0:Lcom/broadcom/fm/fmreceiver/FmProxy;

    invoke-static {v0}, Lcom/broadcom/fm/fmreceiver/FmProxy;->access$100(Lcom/broadcom/fm/fmreceiver/FmProxy;)Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;

    move-result-object v0

    if-eqz v0, :cond_0

    .line 1402
    iget-object v0, p0, Lcom/broadcom/fm/fmreceiver/FmProxy$FmReceiverCallback;->this$0:Lcom/broadcom/fm/fmreceiver/FmProxy;

    invoke-static {v0}, Lcom/broadcom/fm/fmreceiver/FmProxy;->access$100(Lcom/broadcom/fm/fmreceiver/FmProxy;)Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;

    move-result-object v0

    invoke-interface {v0, p1, p2, p3, p4}, Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;->onSeekCompleteEvent(IIIZ)V
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 1403
    :cond_0
    monitor-exit p0

    return-void

    :catchall_0
    move-exception p1

    monitor-exit p0

    throw p1
.end method

.method public declared-synchronized onStatusEvent(IIIZILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Z)V
    .locals 12
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Landroid/os/RemoteException;
        }
    .end annotation

    move-object v1, p0

    monitor-enter p0

    .line 1391
    :try_start_0
    iget-object v0, v1, Lcom/broadcom/fm/fmreceiver/FmProxy$FmReceiverCallback;->this$0:Lcom/broadcom/fm/fmreceiver/FmProxy;

    invoke-static {v0}, Lcom/broadcom/fm/fmreceiver/FmProxy;->access$100(Lcom/broadcom/fm/fmreceiver/FmProxy;)Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;

    move-result-object v0

    if-eqz v0, :cond_0

    .line 1392
    iget-object v0, v1, Lcom/broadcom/fm/fmreceiver/FmProxy$FmReceiverCallback;->this$0:Lcom/broadcom/fm/fmreceiver/FmProxy;

    invoke-static {v0}, Lcom/broadcom/fm/fmreceiver/FmProxy;->access$100(Lcom/broadcom/fm/fmreceiver/FmProxy;)Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;

    move-result-object v2

    move v3, p1

    move v4, p2

    move v5, p3

    move/from16 v6, p4

    move/from16 v7, p5

    move-object/from16 v8, p6

    move-object/from16 v9, p7

    move-object/from16 v10, p8

    move/from16 v11, p9

    invoke-interface/range {v2 .. v11}, Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;->onStatusEvent(IIIZILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Z)V
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 1396
    :cond_0
    monitor-exit p0

    return-void

    :catchall_0
    move-exception v0

    monitor-exit p0

    throw v0
.end method

.method public declared-synchronized onVolumeEvent(II)V
    .locals 1
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Landroid/os/RemoteException;
        }
    .end annotation

    monitor-enter p0

    .line 1457
    :try_start_0
    iget-object v0, p0, Lcom/broadcom/fm/fmreceiver/FmProxy$FmReceiverCallback;->this$0:Lcom/broadcom/fm/fmreceiver/FmProxy;

    invoke-static {v0}, Lcom/broadcom/fm/fmreceiver/FmProxy;->access$100(Lcom/broadcom/fm/fmreceiver/FmProxy;)Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;

    move-result-object v0

    if-eqz v0, :cond_0

    .line 1458
    iget-object v0, p0, Lcom/broadcom/fm/fmreceiver/FmProxy$FmReceiverCallback;->this$0:Lcom/broadcom/fm/fmreceiver/FmProxy;

    invoke-static {v0}, Lcom/broadcom/fm/fmreceiver/FmProxy;->access$100(Lcom/broadcom/fm/fmreceiver/FmProxy;)Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;

    move-result-object v0

    invoke-interface {v0, p1, p2}, Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;->onVolumeEvent(II)V
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 1459
    :cond_0
    monitor-exit p0

    return-void

    :catchall_0
    move-exception p1

    monitor-exit p0

    throw p1
.end method

.method public declared-synchronized onWorldRegionEvent(I)V
    .locals 1
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Landroid/os/RemoteException;
        }
    .end annotation

    monitor-enter p0

    .line 1451
    :try_start_0
    iget-object v0, p0, Lcom/broadcom/fm/fmreceiver/FmProxy$FmReceiverCallback;->this$0:Lcom/broadcom/fm/fmreceiver/FmProxy;

    invoke-static {v0}, Lcom/broadcom/fm/fmreceiver/FmProxy;->access$100(Lcom/broadcom/fm/fmreceiver/FmProxy;)Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;

    move-result-object v0

    if-eqz v0, :cond_0

    .line 1452
    iget-object v0, p0, Lcom/broadcom/fm/fmreceiver/FmProxy$FmReceiverCallback;->this$0:Lcom/broadcom/fm/fmreceiver/FmProxy;

    invoke-static {v0}, Lcom/broadcom/fm/fmreceiver/FmProxy;->access$100(Lcom/broadcom/fm/fmreceiver/FmProxy;)Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;

    move-result-object v0

    invoke-interface {v0, p1}, Lcom/broadcom/fm/fmreceiver/IFmReceiverEventHandler;->onWorldRegionEvent(I)V
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 1453
    :cond_0
    monitor-exit p0

    return-void

    :catchall_0
    move-exception p1

    monitor-exit p0

    throw p1
.end method
