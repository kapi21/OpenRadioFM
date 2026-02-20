.class Lcom/broadcom/fm/fmreceiver/FmProxy$EventCallbackHandler;
.super Ljava/lang/Thread;
.source "FmProxy.java"


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Lcom/broadcom/fm/fmreceiver/FmProxy;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x2
    name = "EventCallbackHandler"
.end annotation


# instance fields
.field public mHandler:Landroid/os/Handler;

.field final synthetic this$0:Lcom/broadcom/fm/fmreceiver/FmProxy;


# direct methods
.method public constructor <init>(Lcom/broadcom/fm/fmreceiver/FmProxy;)V
    .locals 0

    .line 1608
    iput-object p1, p0, Lcom/broadcom/fm/fmreceiver/FmProxy$EventCallbackHandler;->this$0:Lcom/broadcom/fm/fmreceiver/FmProxy;

    .line 1609
    invoke-direct {p0}, Ljava/lang/Thread;-><init>()V

    const/16 p1, 0xa

    .line 1610
    invoke-virtual {p0, p1}, Lcom/broadcom/fm/fmreceiver/FmProxy$EventCallbackHandler;->setPriority(I)V

    return-void
.end method


# virtual methods
.method public finish()V
    .locals 1

    .line 1619
    iget-object v0, p0, Lcom/broadcom/fm/fmreceiver/FmProxy$EventCallbackHandler;->mHandler:Landroid/os/Handler;

    if-eqz v0, :cond_0

    .line 1620
    invoke-virtual {v0}, Landroid/os/Handler;->getLooper()Landroid/os/Looper;

    move-result-object v0

    if-eqz v0, :cond_0

    .line 1622
    invoke-virtual {v0}, Landroid/os/Looper;->quit()V

    :cond_0
    return-void
.end method

.method public run()V
    .locals 1

    .line 1613
    invoke-static {}, Landroid/os/Looper;->prepare()V

    .line 1614
    new-instance v0, Landroid/os/Handler;

    invoke-direct {v0}, Landroid/os/Handler;-><init>()V

    iput-object v0, p0, Lcom/broadcom/fm/fmreceiver/FmProxy$EventCallbackHandler;->mHandler:Landroid/os/Handler;

    .line 1615
    invoke-static {}, Landroid/os/Looper;->loop()V

    return-void
.end method
