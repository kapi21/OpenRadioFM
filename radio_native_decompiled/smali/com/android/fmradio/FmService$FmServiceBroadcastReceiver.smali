.class Lcom/android/fmradio/FmService$FmServiceBroadcastReceiver;
.super Landroid/content/BroadcastReceiver;
.source "FmService.java"


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Lcom/android/fmradio/FmService;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x2
    name = "FmServiceBroadcastReceiver"
.end annotation


# instance fields
.field final synthetic this$0:Lcom/android/fmradio/FmService;


# direct methods
.method private constructor <init>(Lcom/android/fmradio/FmService;)V
    .locals 0

    .line 170
    iput-object p1, p0, Lcom/android/fmradio/FmService$FmServiceBroadcastReceiver;->this$0:Lcom/android/fmradio/FmService;

    invoke-direct {p0}, Landroid/content/BroadcastReceiver;-><init>()V

    return-void
.end method

.method synthetic constructor <init>(Lcom/android/fmradio/FmService;Lcom/android/fmradio/FmService$1;)V
    .locals 0

    .line 170
    invoke-direct {p0, p1}, Lcom/android/fmradio/FmService$FmServiceBroadcastReceiver;-><init>(Lcom/android/fmradio/FmService;)V

    return-void
.end method


# virtual methods
.method public onReceive(Landroid/content/Context;Landroid/content/Intent;)V
    .locals 4

    .line 174
    invoke-virtual {p2}, Landroid/content/Intent;->getAction()Ljava/lang/String;

    move-result-object p1

    const-string v0, "command"

    .line 175
    invoke-virtual {p2, v0}, Landroid/content/Intent;->getStringExtra(Ljava/lang/String;)Ljava/lang/String;

    move-result-object v0

    .line 176
    invoke-static {}, Lcom/android/fmradio/FmService;->access$000()Ljava/lang/String;

    move-result-object v1

    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2}, Ljava/lang/StringBuilder;-><init>()V

    const-string v3, "onReceive, action = "

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    const-string v3, " / command = "

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v2

    invoke-static {v1, v2}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    const-string v1, "com.android.music.musicservicecommand"

    .line 178
    invoke-virtual {v1, p1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v1

    const/4 v2, 0x0

    if-eqz v1, :cond_0

    const-string v1, "pause"

    invoke-virtual {v1, v0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v0

    if-eqz v0, :cond_0

    .line 180
    iget-object p1, p0, Lcom/android/fmradio/FmService$FmServiceBroadcastReceiver;->this$0:Lcom/android/fmradio/FmService;

    invoke-static {p1}, Lcom/android/fmradio/FmService;->access$100(Lcom/android/fmradio/FmService;)Lcom/android/fmradio/FmService$FmRadioServiceHandler;

    move-result-object p1

    invoke-virtual {p1, v2}, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->removeCallbacksAndMessages(Ljava/lang/Object;)V

    .line 181
    iget-object p1, p0, Lcom/android/fmradio/FmService$FmServiceBroadcastReceiver;->this$0:Lcom/android/fmradio/FmService;

    invoke-static {p1}, Lcom/android/fmradio/FmService;->access$200(Lcom/android/fmradio/FmService;)V

    .line 182
    iget-object p1, p0, Lcom/android/fmradio/FmService$FmServiceBroadcastReceiver;->this$0:Lcom/android/fmradio/FmService;

    invoke-virtual {p1}, Lcom/android/fmradio/FmService;->stopSelf()V

    goto/16 :goto_0

    :cond_0
    const-string v0, "android.intent.action.ACTION_SHUTDOWN"

    .line 184
    invoke-virtual {v0, p1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v0

    if-eqz v0, :cond_1

    .line 189
    iget-object p1, p0, Lcom/android/fmradio/FmService$FmServiceBroadcastReceiver;->this$0:Lcom/android/fmradio/FmService;

    invoke-static {p1}, Lcom/android/fmradio/FmService;->access$100(Lcom/android/fmradio/FmService;)Lcom/android/fmradio/FmService$FmRadioServiceHandler;

    move-result-object p1

    invoke-virtual {p1, v2}, Lcom/android/fmradio/FmService$FmRadioServiceHandler;->removeCallbacksAndMessages(Ljava/lang/Object;)V

    .line 190
    iget-object p1, p0, Lcom/android/fmradio/FmService$FmServiceBroadcastReceiver;->this$0:Lcom/android/fmradio/FmService;

    invoke-static {p1}, Lcom/android/fmradio/FmService;->access$200(Lcom/android/fmradio/FmService;)V

    goto/16 :goto_0

    :cond_1
    const-string v0, "android.intent.action.SCREEN_ON"

    .line 192
    invoke-virtual {v0, p1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v0

    if-eqz v0, :cond_2

    .line 193
    iget-object p1, p0, Lcom/android/fmradio/FmService$FmServiceBroadcastReceiver;->this$0:Lcom/android/fmradio/FmService;

    const/4 p2, 0x1

    invoke-virtual {p1, p2}, Lcom/android/fmradio/FmService;->setRdsAsync(Z)V

    goto :goto_0

    :cond_2
    const-string v0, "android.intent.action.SCREEN_OFF"

    .line 195
    invoke-virtual {v0, p1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v0

    if-eqz v0, :cond_3

    .line 196
    iget-object p1, p0, Lcom/android/fmradio/FmService$FmServiceBroadcastReceiver;->this$0:Lcom/android/fmradio/FmService;

    const/4 p2, 0x0

    invoke-virtual {p1, p2}, Lcom/android/fmradio/FmService;->setRdsAsync(Z)V

    goto :goto_0

    :cond_3
    const-string v0, "android.media.VOLUME_CHANGED_ACTION"

    .line 201
    invoke-virtual {v0, p1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result p1

    if-eqz p1, :cond_5

    .line 202
    iget-object p1, p0, Lcom/android/fmradio/FmService$FmServiceBroadcastReceiver;->this$0:Lcom/android/fmradio/FmService;

    invoke-virtual {p1}, Lcom/android/fmradio/FmService;->isScanning()Z

    move-result p1

    if-eqz p1, :cond_4

    return-void

    :cond_4
    const-string p1, "android.media.EXTRA_VOLUME_STREAM_TYPE"

    .line 206
    invoke-virtual {p2, p1}, Landroid/content/Intent;->hasExtra(Ljava/lang/String;)Z

    move-result v0

    if-eqz v0, :cond_5

    const-string v0, "android.media.EXTRA_VOLUME_STREAM_VALUE"

    .line 207
    invoke-virtual {p2, v0}, Landroid/content/Intent;->hasExtra(Ljava/lang/String;)Z

    move-result v1

    if-eqz v1, :cond_5

    const/4 v1, -0x1

    .line 208
    invoke-virtual {p2, p1, v1}, Landroid/content/Intent;->getIntExtra(Ljava/lang/String;I)I

    move-result p1

    const/4 v2, 0x3

    if-ne p1, v2, :cond_5

    .line 210
    invoke-virtual {p2, v0, v1}, Landroid/content/Intent;->getIntExtra(Ljava/lang/String;I)I

    move-result p2

    if-eq p2, v1, :cond_5

    .line 212
    invoke-static {}, Lcom/android/fmradio/FmService;->access$000()Ljava/lang/String;

    move-result-object v0

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "stream type "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string p1, "value "

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p1

    invoke-static {v0, p1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 213
    iget-object p1, p0, Lcom/android/fmradio/FmService$FmServiceBroadcastReceiver;->this$0:Lcom/android/fmradio/FmService;

    invoke-virtual {p1, p2}, Lcom/android/fmradio/FmService;->setVolume(I)Z

    :cond_5
    :goto_0
    return-void
.end method
