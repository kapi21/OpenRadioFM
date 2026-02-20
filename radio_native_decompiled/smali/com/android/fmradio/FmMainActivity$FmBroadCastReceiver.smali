.class public Lcom/android/fmradio/FmMainActivity$FmBroadCastReceiver;
.super Landroid/content/BroadcastReceiver;
.source "FmMainActivity.java"


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Lcom/android/fmradio/FmMainActivity;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x1
    name = "FmBroadCastReceiver"
.end annotation


# instance fields
.field final synthetic this$0:Lcom/android/fmradio/FmMainActivity;


# direct methods
.method public constructor <init>(Lcom/android/fmradio/FmMainActivity;)V
    .locals 0

    .line 155
    iput-object p1, p0, Lcom/android/fmradio/FmMainActivity$FmBroadCastReceiver;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-direct {p0}, Landroid/content/BroadcastReceiver;-><init>()V

    return-void
.end method

.method private voiceTuneStation(Landroid/content/Intent;)V
    .locals 6

    .line 157
    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity$FmBroadCastReceiver;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {v0}, Lcom/android/fmradio/FmMainActivity;->stopScan()Z

    move-result v0

    if-eqz v0, :cond_0

    return-void

    :cond_0
    if-eqz p1, :cond_5

    const-string v0, "band"

    .line 161
    invoke-virtual {p1, v0}, Landroid/content/Intent;->getStringExtra(Ljava/lang/String;)Ljava/lang/String;

    move-result-object v0

    .line 162
    invoke-static {}, Lcom/android/fmradio/FmMainActivity;->access$100()Ljava/lang/String;

    move-result-object v1

    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2}, Ljava/lang/StringBuilder;-><init>()V

    const-string v3, "voiceTuneStation - bandStr: "

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v2

    invoke-static {v1, v2}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    const v1, 0x7f0c001c

    if-eqz v0, :cond_4

    .line 164
    invoke-virtual {v0}, Ljava/lang/String;->isEmpty()Z

    move-result v2

    if-nez v2, :cond_4

    .line 165
    invoke-static {}, Lcom/android/fmradio/utils/FmUtils;->getRadioArea()I

    move-result v2

    const-string v3, "fm"

    .line 169
    invoke-virtual {v0, v3}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v3

    if-eqz v3, :cond_1

    .line 170
    iget-object v3, p0, Lcom/android/fmradio/FmMainActivity$FmBroadCastReceiver;->this$0:Lcom/android/fmradio/FmMainActivity;

    const/4 v4, 0x0

    iput v4, v3, Lcom/android/fmradio/FmMainActivity;->mBand:I

    .line 172
    iget v3, v3, Lcom/android/fmradio/FmMainActivity;->mBand:I

    invoke-static {v2, v3}, Lcom/android/fmradio/utils/FmUtils;->getRadioAreaFMMinFreq(II)I

    move-result v2

    goto :goto_0

    .line 174
    :cond_1
    iget-object v3, p0, Lcom/android/fmradio/FmMainActivity$FmBroadCastReceiver;->this$0:Lcom/android/fmradio/FmMainActivity;

    const/4 v4, 0x3

    iput v4, v3, Lcom/android/fmradio/FmMainActivity;->mBand:I

    .line 176
    invoke-static {v2}, Lcom/android/fmradio/utils/FmUtils;->getRadioAreaAMMinFreq(I)I

    move-result v2

    :goto_0
    int-to-float v2, v2

    const-string v3, "freq"

    .line 179
    invoke-virtual {p1, v3, v2}, Landroid/content/Intent;->getFloatExtra(Ljava/lang/String;F)F

    move-result p1

    float-to-int v2, p1

    .line 182
    iget-object v3, p0, Lcom/android/fmradio/FmMainActivity$FmBroadCastReceiver;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget v3, v3, Lcom/android/fmradio/FmMainActivity;->mBand:I

    if-nez v3, :cond_2

    const/high16 v2, 0x42c80000    # 100.0f

    mul-float/2addr v2, p1

    float-to-int v2, v2

    .line 186
    :cond_2
    invoke-static {}, Lcom/android/fmradio/FmMainActivity;->access$100()Ljava/lang/String;

    move-result-object v3

    new-instance v4, Ljava/lang/StringBuilder;

    invoke-direct {v4}, Ljava/lang/StringBuilder;-><init>()V

    const-string v5, "RADIO_FM_CMD - bandStr: "

    invoke-virtual {v4, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v4, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    const-string v0, " - freq: "

    invoke-virtual {v4, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v4, p1}, Ljava/lang/StringBuilder;->append(F)Ljava/lang/StringBuilder;

    const-string p1, " - validFreq: "

    invoke-virtual {v4, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v4, v2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v4}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p1

    invoke-static {v3, p1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 188
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$FmBroadCastReceiver;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget p1, p1, Lcom/android/fmradio/FmMainActivity;->mBand:I

    invoke-static {v2, p1}, Lcom/android/fmradio/utils/FmUtils;->isValidStation(II)Z

    move-result p1

    if-eqz p1, :cond_3

    .line 189
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$FmBroadCastReceiver;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget-object p1, p1, Lcom/android/fmradio/FmMainActivity;->mTunerManagerForExt:Lcom/android/fmradio/TunerManagerForExt;

    iget-object v0, p0, Lcom/android/fmradio/FmMainActivity$FmBroadCastReceiver;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget v0, v0, Lcom/android/fmradio/FmMainActivity;->mBand:I

    int-to-byte v0, v0

    invoke-virtual {p1, v0}, Lcom/android/fmradio/TunerManagerForExt;->onBand(B)V

    .line 191
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$FmBroadCastReceiver;->this$0:Lcom/android/fmradio/FmMainActivity;

    const/4 v0, 0x1

    invoke-static {p1, v2, v0}, Lcom/android/fmradio/FmMainActivity;->access$200(Lcom/android/fmradio/FmMainActivity;IZ)V

    goto :goto_1

    .line 193
    :cond_3
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$FmBroadCastReceiver;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {p1, v1}, Lcom/android/fmradio/FmMainActivity;->getString(I)Ljava/lang/String;

    move-result-object v0

    invoke-static {p1, v0}, Lcom/android/fmradio/FmMainActivity;->access$300(Lcom/android/fmradio/FmMainActivity;Ljava/lang/CharSequence;)V

    goto :goto_1

    .line 196
    :cond_4
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$FmBroadCastReceiver;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {p1, v1}, Lcom/android/fmradio/FmMainActivity;->getString(I)Ljava/lang/String;

    move-result-object v0

    invoke-static {p1, v0}, Lcom/android/fmradio/FmMainActivity;->access$300(Lcom/android/fmradio/FmMainActivity;Ljava/lang/CharSequence;)V

    :cond_5
    :goto_1
    return-void
.end method


# virtual methods
.method public onReceive(Landroid/content/Context;Landroid/content/Intent;)V
    .locals 3

    .line 203
    invoke-static {}, Lcom/android/fmradio/FmMainActivity;->access$100()Ljava/lang/String;

    move-result-object p1

    new-instance v0, Ljava/lang/StringBuilder;

    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    const-string v1, "action: "

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {p2}, Landroid/content/Intent;->getAction()Ljava/lang/String;

    move-result-object v1

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    invoke-static {p1, v0}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 204
    invoke-virtual {p2}, Landroid/content/Intent;->getAction()Ljava/lang/String;

    move-result-object p1

    const-string v0, "ailit.set.radio.frequency"

    invoke-virtual {p1, v0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result p1

    if-eqz p1, :cond_0

    .line 205
    invoke-direct {p0, p2}, Lcom/android/fmradio/FmMainActivity$FmBroadCastReceiver;->voiceTuneStation(Landroid/content/Intent;)V

    goto/16 :goto_0

    .line 206
    :cond_0
    invoke-virtual {p2}, Landroid/content/Intent;->getAction()Ljava/lang/String;

    move-result-object p1

    const-string v0, "/customize/radio/pre"

    invoke-virtual {p1, v0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result p1

    const/4 v0, 0x0

    if-eqz p1, :cond_1

    .line 207
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$FmBroadCastReceiver;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {p1, v0}, Lcom/android/fmradio/FmMainActivity;->onNextOrPreStation(Z)V

    goto/16 :goto_0

    .line 208
    :cond_1
    invoke-virtual {p2}, Landroid/content/Intent;->getAction()Ljava/lang/String;

    move-result-object p1

    const-string v1, "/customize/radio/next"

    invoke-virtual {p1, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result p1

    const/4 v1, 0x1

    if-eqz p1, :cond_2

    .line 209
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$FmBroadCastReceiver;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {p1, v1}, Lcom/android/fmradio/FmMainActivity;->onNextOrPreStation(Z)V

    goto/16 :goto_0

    .line 210
    :cond_2
    invoke-virtual {p2}, Landroid/content/Intent;->getAction()Ljava/lang/String;

    move-result-object p1

    const-string v2, "/customize/radio/station"

    invoke-virtual {p1, v2}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result p1

    if-eqz p1, :cond_3

    const-string p1, "com.qf.radio.update_action_key"

    .line 211
    invoke-virtual {p2, p1}, Landroid/content/Intent;->getStringExtra(Ljava/lang/String;)Ljava/lang/String;

    move-result-object p1

    .line 212
    iget-object p2, p0, Lcom/android/fmradio/FmMainActivity$FmBroadCastReceiver;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {p2, p1}, Lcom/android/fmradio/FmMainActivity;->onStation(Ljava/lang/String;)V

    goto/16 :goto_0

    .line 213
    :cond_3
    invoke-virtual {p2}, Landroid/content/Intent;->getAction()Ljava/lang/String;

    move-result-object p1

    const-string v2, "/customize/radio/seek_up"

    invoke-virtual {p1, v2}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result p1

    if-eqz p1, :cond_4

    .line 214
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$FmBroadCastReceiver;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget p2, p1, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    invoke-virtual {p1, p2, v0}, Lcom/android/fmradio/FmMainActivity;->onSeekStation(IZ)V

    goto :goto_0

    .line 215
    :cond_4
    invoke-virtual {p2}, Landroid/content/Intent;->getAction()Ljava/lang/String;

    move-result-object p1

    const-string v0, "/customize/radio/seek_down"

    invoke-virtual {p1, v0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result p1

    if-eqz p1, :cond_5

    .line 216
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$FmBroadCastReceiver;->this$0:Lcom/android/fmradio/FmMainActivity;

    iget p2, p1, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    invoke-virtual {p1, p2, v1}, Lcom/android/fmradio/FmMainActivity;->onSeekStation(IZ)V

    goto :goto_0

    .line 217
    :cond_5
    invoke-virtual {p2}, Landroid/content/Intent;->getAction()Ljava/lang/String;

    move-result-object p1

    const-string v0, "/customize/radio/close"

    invoke-virtual {p1, v0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result p1

    if-eqz p1, :cond_6

    .line 218
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$FmBroadCastReceiver;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {p1}, Lcom/android/fmradio/FmMainActivity;->finish()V

    goto :goto_0

    .line 219
    :cond_6
    invoke-virtual {p2}, Landroid/content/Intent;->getAction()Ljava/lang/String;

    move-result-object p1

    const-string v0, "/customize/radio/band"

    invoke-virtual {p1, v0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result p1

    if-eqz p1, :cond_7

    .line 220
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$FmBroadCastReceiver;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {p1}, Lcom/android/fmradio/FmMainActivity;->onBand()V

    goto :goto_0

    .line 221
    :cond_7
    invoke-virtual {p2}, Landroid/content/Intent;->getAction()Ljava/lang/String;

    move-result-object p1

    const-string v0, "com.qf.action.update_radio_area"

    invoke-virtual {p1, v0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result p1

    if-eqz p1, :cond_8

    .line 222
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$FmBroadCastReceiver;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-static {p1}, Lcom/android/fmradio/FmMainActivity;->access$400(Lcom/android/fmradio/FmMainActivity;)V

    goto :goto_0

    .line 223
    :cond_8
    invoke-virtual {p2}, Landroid/content/Intent;->getAction()Ljava/lang/String;

    move-result-object p1

    const-string p2, "com.qf.action.ACC_OFF"

    invoke-virtual {p1, p2}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result p1

    if-eqz p1, :cond_9

    .line 224
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$FmBroadCastReceiver;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {p1}, Lcom/android/fmradio/FmMainActivity;->stopScan()Z

    :cond_9
    :goto_0
    return-void
.end method
