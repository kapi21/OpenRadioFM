.class Lcom/android/fmradio/FmMainActivity$2;
.super Ljava/lang/Object;
.source "FmMainActivity.java"

# interfaces
.implements Lcom/android/fmradio/iface/FmListener;


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

    .line 231
    iput-object p1, p0, Lcom/android/fmradio/FmMainActivity$2;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public getChannels([I)V
    .locals 3

    .line 250
    invoke-static {}, Lcom/android/fmradio/FmMainActivity;->access$100()Ljava/lang/String;

    move-result-object v0

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "---->>getChannels() Channels: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-static {p1}, Ljava/util/Arrays;->toString([I)Ljava/lang/String;

    move-result-object p1

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p1

    invoke-static {v0, p1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    return-void
.end method

.method public onCallBack(Landroid/os/Bundle;)V
    .locals 4

    const-string v0, "callback_flag"

    .line 234
    invoke-virtual {p1, v0}, Landroid/os/Bundle;->getInt(Ljava/lang/String;)I

    move-result v0

    .line 235
    invoke-static {}, Lcom/android/fmradio/FmMainActivity;->access$100()Ljava/lang/String;

    move-result-object v1

    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2}, Ljava/lang/StringBuilder;-><init>()V

    const-string v3, "---->>onCallBack() flag: "

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2, v0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v2

    invoke-static {v1, v2}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    const/16 v1, 0xb

    if-ne v0, v1, :cond_0

    .line 237
    invoke-static {}, Lcom/android/fmradio/FmMainActivity;->access$100()Ljava/lang/String;

    move-result-object v1

    const-string v2, "---->>onCallBack() MSGID_FM_EXIT"

    invoke-static {v1, v2}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 238
    iget-object v1, p0, Lcom/android/fmradio/FmMainActivity$2;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-static {v1}, Lcom/android/fmradio/FmMainActivity;->access$500(Lcom/android/fmradio/FmMainActivity;)Landroid/os/Handler;

    move-result-object v1

    const/4 v2, 0x0

    invoke-virtual {v1, v2}, Landroid/os/Handler;->removeCallbacksAndMessages(Ljava/lang/Object;)V

    .line 242
    :cond_0
    iget-object v1, p0, Lcom/android/fmradio/FmMainActivity$2;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-static {v1}, Lcom/android/fmradio/FmMainActivity;->access$500(Lcom/android/fmradio/FmMainActivity;)Landroid/os/Handler;

    move-result-object v1

    invoke-virtual {v1, v0}, Landroid/os/Handler;->obtainMessage(I)Landroid/os/Message;

    move-result-object v1

    .line 243
    invoke-virtual {v1, p1}, Landroid/os/Message;->setData(Landroid/os/Bundle;)V

    .line 244
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$2;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-static {p1}, Lcom/android/fmradio/FmMainActivity;->access$500(Lcom/android/fmradio/FmMainActivity;)Landroid/os/Handler;

    move-result-object p1

    invoke-virtual {p1, v0}, Landroid/os/Handler;->removeMessages(I)V

    .line 245
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$2;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-static {p1}, Lcom/android/fmradio/FmMainActivity;->access$500(Lcom/android/fmradio/FmMainActivity;)Landroid/os/Handler;

    move-result-object p1

    invoke-virtual {p1, v1}, Landroid/os/Handler;->sendMessage(Landroid/os/Message;)Z

    return-void
.end method
