.class Lcom/android/fmradio/FmMainActivity$4;
.super Ljava/lang/Object;
.source "FmMainActivity.java"

# interfaces
.implements Landroid/content/ServiceConnection;


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

    .line 470
    iput-object p1, p0, Lcom/android/fmradio/FmMainActivity$4;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public synthetic onBindingDied(Landroid/content/ComponentName;)V
    .locals 0

    invoke-static {p0, p1}, Landroid/content/ServiceConnection$-CC;->$default$onBindingDied(Landroid/content/ServiceConnection;Landroid/content/ComponentName;)V

    return-void
.end method

.method public synthetic onNullBinding(Landroid/content/ComponentName;)V
    .locals 0

    invoke-static {p0, p1}, Landroid/content/ServiceConnection$-CC;->$default$onNullBinding(Landroid/content/ServiceConnection;Landroid/content/ComponentName;)V

    return-void
.end method

.method public onServiceConnected(Landroid/content/ComponentName;Landroid/os/IBinder;)V
    .locals 1

    .line 482
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$4;->this$0:Lcom/android/fmradio/FmMainActivity;

    check-cast p2, Lcom/android/fmradio/FmService$ServiceBinder;

    invoke-virtual {p2}, Lcom/android/fmradio/FmService$ServiceBinder;->getService()Lcom/android/fmradio/FmService;

    move-result-object p2

    invoke-static {p1, p2}, Lcom/android/fmradio/FmMainActivity;->access$1202(Lcom/android/fmradio/FmMainActivity;Lcom/android/fmradio/FmService;)Lcom/android/fmradio/FmService;

    .line 483
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$4;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-static {p1}, Lcom/android/fmradio/FmMainActivity;->access$1200(Lcom/android/fmradio/FmMainActivity;)Lcom/android/fmradio/FmService;

    move-result-object p1

    if-nez p1, :cond_0

    .line 484
    invoke-static {}, Lcom/android/fmradio/FmMainActivity;->access$100()Ljava/lang/String;

    move-result-object p1

    const-string p2, "onServiceConnected, mService is null"

    invoke-static {p1, p2}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 485
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$4;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {p1}, Lcom/android/fmradio/FmMainActivity;->finish()V

    return-void

    .line 488
    :cond_0
    invoke-static {}, Lcom/android/fmradio/FmMainActivity;->access$100()Ljava/lang/String;

    move-result-object p1

    new-instance p2, Ljava/lang/StringBuilder;

    invoke-direct {p2}, Ljava/lang/StringBuilder;-><init>()V

    const-string v0, "onServiceConnected, mService is not null mCurrentStation: "

    invoke-virtual {p2, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity$4;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget v0, v0, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    invoke-virtual {p2, v0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {p2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p2

    invoke-static {p1, p2}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 489
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$4;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-static {p1}, Lcom/android/fmradio/FmMainActivity;->access$1300(Lcom/android/fmradio/FmMainActivity;)V

    .line 491
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$4;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-static {p1}, Lcom/android/fmradio/FmMainActivity;->access$1200(Lcom/android/fmradio/FmMainActivity;)Lcom/android/fmradio/FmService;

    move-result-object p1

    iget-object p2, p0, Lcom/android/fmradio/FmMainActivity$4;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-static {p2}, Lcom/android/fmradio/FmMainActivity;->access$1400(Lcom/android/fmradio/FmMainActivity;)Lcom/android/fmradio/iface/FmListener;

    move-result-object p2

    invoke-virtual {p1, p2}, Lcom/android/fmradio/FmService;->registerFmRadioListener(Lcom/android/fmradio/iface/FmListener;)V

    .line 492
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$4;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-static {p1}, Lcom/android/fmradio/FmMainActivity;->access$1200(Lcom/android/fmradio/FmMainActivity;)Lcom/android/fmradio/FmService;

    move-result-object p1

    iget-object p2, p0, Lcom/android/fmradio/FmMainActivity$4;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-static {p2}, Lcom/android/fmradio/FmMainActivity;->access$1500(Lcom/android/fmradio/FmMainActivity;)Lcom/android/fmradio/iface/IMediaButtonListener;

    move-result-object p2

    invoke-virtual {p1, p2}, Lcom/android/fmradio/FmService;->setMediaButtonListener(Lcom/android/fmradio/iface/IMediaButtonListener;)V

    .line 493
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$4;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-static {p1}, Lcom/android/fmradio/FmMainActivity;->access$1200(Lcom/android/fmradio/FmMainActivity;)Lcom/android/fmradio/FmService;

    move-result-object p1

    iget-object p2, p0, Lcom/android/fmradio/FmMainActivity$4;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-static {p2}, Lcom/android/fmradio/FmMainActivity;->access$1600(Lcom/android/fmradio/FmMainActivity;)Z

    move-result p2

    invoke-virtual {p1, p2}, Lcom/android/fmradio/FmService;->setFmMainActivityForeground(Z)V

    .line 494
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$4;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-static {p1}, Lcom/android/fmradio/FmMainActivity;->access$1200(Lcom/android/fmradio/FmMainActivity;)Lcom/android/fmradio/FmService;

    move-result-object p1

    invoke-virtual {p1}, Lcom/android/fmradio/FmService;->isServiceInited()Z

    move-result p1

    if-nez p1, :cond_1

    .line 495
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$4;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-static {p1}, Lcom/android/fmradio/FmMainActivity;->access$1200(Lcom/android/fmradio/FmMainActivity;)Lcom/android/fmradio/FmService;

    move-result-object p1

    iget-object p2, p0, Lcom/android/fmradio/FmMainActivity$4;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget p2, p2, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    invoke-virtual {p1, p2}, Lcom/android/fmradio/FmService;->initService(I)V

    .line 496
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$4;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-static {p1}, Lcom/android/fmradio/FmMainActivity;->access$1300(Lcom/android/fmradio/FmMainActivity;)V

    goto :goto_0

    .line 498
    :cond_1
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$4;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-static {p1}, Lcom/android/fmradio/FmMainActivity;->access$1200(Lcom/android/fmradio/FmMainActivity;)Lcom/android/fmradio/FmService;

    move-result-object p1

    invoke-virtual {p1}, Lcom/android/fmradio/FmService;->isDeviceOpen()Z

    move-result p1

    if-eqz p1, :cond_3

    .line 501
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$4;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-static {p1}, Lcom/android/fmradio/FmMainActivity;->access$1700(Lcom/android/fmradio/FmMainActivity;)Z

    move-result p1

    if-eqz p1, :cond_2

    .line 502
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$4;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget p2, p1, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    invoke-virtual {p1, p2}, Lcom/android/fmradio/FmMainActivity;->tuneStation(I)V

    .line 503
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$4;->this$0:Lcom/android/fmradio/FmMainActivity;

    const/4 p2, 0x0

    invoke-static {p1, p2}, Lcom/android/fmradio/FmMainActivity;->access$1702(Lcom/android/fmradio/FmMainActivity;Z)Z

    .line 505
    :cond_2
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$4;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-static {p1}, Lcom/android/fmradio/FmMainActivity;->access$1800(Lcom/android/fmradio/FmMainActivity;)V

    goto :goto_0

    .line 509
    :cond_3
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$4;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-static {p1}, Lcom/android/fmradio/FmMainActivity;->access$1900(Lcom/android/fmradio/FmMainActivity;)V

    .line 510
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$4;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {p1}, Lcom/android/fmradio/FmMainActivity;->finish()V

    :goto_0
    return-void
.end method

.method public onServiceDisconnected(Landroid/content/ComponentName;)V
    .locals 3

    .line 523
    invoke-static {}, Lcom/android/fmradio/FmMainActivity;->access$100()Ljava/lang/String;

    move-result-object v0

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "---->>onServiceDisconnected() className: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p1

    invoke-static {v0, p1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    return-void
.end method
