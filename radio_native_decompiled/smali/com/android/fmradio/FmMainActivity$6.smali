.class Lcom/android/fmradio/FmMainActivity$6;
.super Ljava/lang/Object;
.source "FmMainActivity.java"

# interfaces
.implements Lcom/android/fmradio/iface/IMediaButtonListener;


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

    .line 617
    iput-object p1, p0, Lcom/android/fmradio/FmMainActivity$6;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public onMediaButtonEvent(Landroid/content/Intent;I)V
    .locals 4

    const-string v0, "persist.sys.qf.last_audio_src"

    const-string v1, ""

    .line 621
    invoke-static {v0, v1}, Landroid/os/SystemProperties;->get(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;

    move-result-object v0

    .line 622
    invoke-static {}, Lcom/android/fmradio/FmMainActivity;->access$100()Ljava/lang/String;

    move-result-object v1

    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2}, Ljava/lang/StringBuilder;-><init>()V

    const-string v3, "keyCode: "

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2, p2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v3, " - lastAudioSource: "

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    invoke-static {v1, v0}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    const-string v0, "android.intent.extra.KEY_EVENT"

    .line 626
    invoke-virtual {p1, v0}, Landroid/content/Intent;->getParcelableExtra(Ljava/lang/String;)Landroid/os/Parcelable;

    move-result-object p1

    check-cast p1, Landroid/view/KeyEvent;

    packed-switch p2, :pswitch_data_0

    goto :goto_0

    .line 633
    :pswitch_0
    iget-object p2, p0, Lcom/android/fmradio/FmMainActivity$6;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-static {p2}, Lcom/android/fmradio/FmMainActivity;->access$500(Lcom/android/fmradio/FmMainActivity;)Landroid/os/Handler;

    move-result-object p2

    const/16 v0, 0x2b

    invoke-virtual {p2, v0}, Landroid/os/Handler;->removeMessages(I)V

    .line 634
    iget-object p2, p0, Lcom/android/fmradio/FmMainActivity$6;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-static {p2}, Lcom/android/fmradio/FmMainActivity;->access$500(Lcom/android/fmradio/FmMainActivity;)Landroid/os/Handler;

    move-result-object p2

    invoke-virtual {p2, v0, p1}, Landroid/os/Handler;->obtainMessage(ILjava/lang/Object;)Landroid/os/Message;

    move-result-object p1

    invoke-virtual {p1}, Landroid/os/Message;->sendToTarget()V

    goto :goto_0

    .line 629
    :pswitch_1
    iget-object p2, p0, Lcom/android/fmradio/FmMainActivity$6;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-static {p2}, Lcom/android/fmradio/FmMainActivity;->access$500(Lcom/android/fmradio/FmMainActivity;)Landroid/os/Handler;

    move-result-object p2

    const/16 v0, 0x2a

    invoke-virtual {p2, v0}, Landroid/os/Handler;->removeMessages(I)V

    .line 630
    iget-object p2, p0, Lcom/android/fmradio/FmMainActivity$6;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-static {p2}, Lcom/android/fmradio/FmMainActivity;->access$500(Lcom/android/fmradio/FmMainActivity;)Landroid/os/Handler;

    move-result-object p2

    invoke-virtual {p2, v0, p1}, Landroid/os/Handler;->obtainMessage(ILjava/lang/Object;)Landroid/os/Message;

    move-result-object p1

    invoke-virtual {p1}, Landroid/os/Message;->sendToTarget()V

    goto :goto_0

    .line 637
    :pswitch_2
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$6;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {p1}, Lcom/android/fmradio/FmMainActivity;->finish()V

    :goto_0
    return-void

    :pswitch_data_0
    .packed-switch 0x56
        :pswitch_2
        :pswitch_1
        :pswitch_0
    .end packed-switch
.end method
