.class Lcom/broadcom/fm/fmreceiver/FmProxy$1;
.super Ljava/lang/Object;
.source "FmProxy.java"

# interfaces
.implements Landroid/content/ServiceConnection;


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Lcom/broadcom/fm/fmreceiver/FmProxy;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x0
    name = null
.end annotation


# instance fields
.field final synthetic this$0:Lcom/broadcom/fm/fmreceiver/FmProxy;


# direct methods
.method constructor <init>(Lcom/broadcom/fm/fmreceiver/FmProxy;)V
    .locals 0

    .line 1636
    iput-object p1, p0, Lcom/broadcom/fm/fmreceiver/FmProxy$1;->this$0:Lcom/broadcom/fm/fmreceiver/FmProxy;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public onServiceConnected(Landroid/content/ComponentName;Landroid/os/IBinder;)V
    .locals 2

    .line 1639
    new-instance v0, Ljava/lang/StringBuilder;

    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    const-string v1, "Fm proxy onServiceConnected() name = "

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v0, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    const-string p1, ", service = "

    invoke-virtual {v0, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v0, p2}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p1

    const-string v0, "FmProxy"

    invoke-static {v0, p1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    if-eqz p2, :cond_0

    .line 1642
    iget-object p1, p0, Lcom/broadcom/fm/fmreceiver/FmProxy$1;->this$0:Lcom/broadcom/fm/fmreceiver/FmProxy;

    invoke-virtual {p1, p2}, Lcom/broadcom/fm/fmreceiver/FmProxy;->init(Landroid/os/IBinder;)Z

    move-result p1

    if-nez p1, :cond_1

    iget-object p1, p0, Lcom/broadcom/fm/fmreceiver/FmProxy$1;->this$0:Lcom/broadcom/fm/fmreceiver/FmProxy;

    iget-object p1, p1, Lcom/broadcom/fm/fmreceiver/FmProxy;->mProxyCback:Lcom/broadcom/fm/fmreceiver/IFmProxyCallback;

    if-eqz p1, :cond_1

    :cond_0
    const-string p1, "Unable to create proxy"

    .line 1643
    invoke-static {v0, p1}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;)I

    .line 1645
    :cond_1
    iget-object p1, p0, Lcom/broadcom/fm/fmreceiver/FmProxy$1;->this$0:Lcom/broadcom/fm/fmreceiver/FmProxy;

    iget-object p1, p1, Lcom/broadcom/fm/fmreceiver/FmProxy;->mProxyCback:Lcom/broadcom/fm/fmreceiver/IFmProxyCallback;

    if-eqz p1, :cond_2

    .line 1646
    iget-object p1, p0, Lcom/broadcom/fm/fmreceiver/FmProxy$1;->this$0:Lcom/broadcom/fm/fmreceiver/FmProxy;

    iget-object p1, p1, Lcom/broadcom/fm/fmreceiver/FmProxy;->mProxyCback:Lcom/broadcom/fm/fmreceiver/IFmProxyCallback;

    iget-object p2, p0, Lcom/broadcom/fm/fmreceiver/FmProxy$1;->this$0:Lcom/broadcom/fm/fmreceiver/FmProxy;

    invoke-interface {p1, p2}, Lcom/broadcom/fm/fmreceiver/IFmProxyCallback;->onProxyAvailable(Ljava/lang/Object;)V

    :cond_2
    return-void
.end method

.method public onServiceDisconnected(Landroid/content/ComponentName;)V
    .locals 1

    const-string p1, "FmProxy"

    const-string v0, "Fm Proxy object disconnected"

    .line 1652
    invoke-static {p1, v0}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    .line 1653
    iget-object p1, p0, Lcom/broadcom/fm/fmreceiver/FmProxy$1;->this$0:Lcom/broadcom/fm/fmreceiver/FmProxy;

    const/4 v0, 0x0

    invoke-static {p1, v0}, Lcom/broadcom/fm/fmreceiver/FmProxy;->access$302(Lcom/broadcom/fm/fmreceiver/FmProxy;Lcom/broadcom/fm/fmreceiver/IFmReceiverService;)Lcom/broadcom/fm/fmreceiver/IFmReceiverService;

    .line 1654
    iget-object p1, p0, Lcom/broadcom/fm/fmreceiver/FmProxy$1;->this$0:Lcom/broadcom/fm/fmreceiver/FmProxy;

    iget-object p1, p1, Lcom/broadcom/fm/fmreceiver/FmProxy;->mProxyCback:Lcom/broadcom/fm/fmreceiver/IFmProxyCallback;

    if-eqz p1, :cond_0

    .line 1655
    iget-object p1, p0, Lcom/broadcom/fm/fmreceiver/FmProxy$1;->this$0:Lcom/broadcom/fm/fmreceiver/FmProxy;

    iget-object p1, p1, Lcom/broadcom/fm/fmreceiver/FmProxy;->mProxyCback:Lcom/broadcom/fm/fmreceiver/IFmProxyCallback;

    invoke-interface {p1}, Lcom/broadcom/fm/fmreceiver/IFmProxyCallback;->onProxyUnAvailable()V

    .line 1656
    iget-object p1, p0, Lcom/broadcom/fm/fmreceiver/FmProxy$1;->this$0:Lcom/broadcom/fm/fmreceiver/FmProxy;

    iput-object v0, p1, Lcom/broadcom/fm/fmreceiver/FmProxy;->mProxyCback:Lcom/broadcom/fm/fmreceiver/IFmProxyCallback;

    :cond_0
    return-void
.end method
