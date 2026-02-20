.class Lcom/android/fmradio/FmService$2;
.super Ljava/lang/Object;
.source "FmService.java"

# interfaces
.implements Landroid/media/AudioManager$OnAudioFocusChangeListener;


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Lcom/android/fmradio/FmService;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x0
    name = null
.end annotation


# instance fields
.field final synthetic this$0:Lcom/android/fmradio/FmService;


# direct methods
.method constructor <init>(Lcom/android/fmradio/FmService;)V
    .locals 0

    .line 1090
    iput-object p1, p0, Lcom/android/fmradio/FmService$2;->this$0:Lcom/android/fmradio/FmService;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public onAudioFocusChange(I)V
    .locals 3

    const/4 v0, -0x3

    const/4 v1, 0x0

    if-eq p1, v0, :cond_4

    const/4 v0, -0x2

    if-eq p1, v0, :cond_3

    const/4 v0, -0x1

    const/4 v1, 0x1

    if-eq p1, v0, :cond_2

    if-eq p1, v1, :cond_1

    const/4 v0, 0x3

    if-eq p1, v0, :cond_0

    goto :goto_0

    .line 1144
    :cond_0
    invoke-static {}, Lcom/android/fmradio/FmService;->access$000()Ljava/lang/String;

    move-result-object p1

    const-string v0, "--->>onAudioFocusChange()  ----AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK----"

    invoke-static {p1, v0}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 1145
    iget-object p1, p0, Lcom/android/fmradio/FmService$2;->this$0:Lcom/android/fmradio/FmService;

    invoke-virtual {p1}, Lcom/android/fmradio/FmService;->requestAudioFocus()Z

    goto :goto_0

    .line 1121
    :cond_1
    invoke-static {}, Lcom/android/fmradio/FmService;->access$000()Ljava/lang/String;

    move-result-object p1

    const-string v0, "--->>onAudioFocusChange()  ----AUDIOFOCUS_GAIN----"

    invoke-static {p1, v0}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 1122
    iget-object p1, p0, Lcom/android/fmradio/FmService$2;->this$0:Lcom/android/fmradio/FmService;

    invoke-static {p1}, Lcom/android/fmradio/FmService;->access$800(Lcom/android/fmradio/FmService;)V

    .line 1123
    monitor-enter p0

    .line 1127
    :try_start_0
    iget-object p1, p0, Lcom/android/fmradio/FmService$2;->this$0:Lcom/android/fmradio/FmService;

    iput-boolean v1, p1, Lcom/android/fmradio/FmService;->mIsAudioFocusHeld:Z

    .line 1131
    iget-object p1, p0, Lcom/android/fmradio/FmService$2;->this$0:Lcom/android/fmradio/FmService;

    invoke-static {p1, v1}, Lcom/android/fmradio/FmService;->access$900(Lcom/android/fmradio/FmService;I)V

    .line 1132
    monitor-exit p0

    goto :goto_0

    :catchall_0
    move-exception p1

    monitor-exit p0
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    throw p1

    .line 1101
    :cond_2
    invoke-static {}, Lcom/android/fmradio/FmService;->access$000()Ljava/lang/String;

    move-result-object p1

    const-string v2, "--\t->>onAudioFocusChange()  ----AUDIOFOCUS_LOSS----"

    invoke-static {p1, v2}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 1102
    iget-object p1, p0, Lcom/android/fmradio/FmService$2;->this$0:Lcom/android/fmradio/FmService;

    invoke-static {p1}, Lcom/android/fmradio/FmService;->access$600(Lcom/android/fmradio/FmService;)V

    .line 1103
    monitor-enter p0

    .line 1105
    :try_start_1
    iget-object p1, p0, Lcom/android/fmradio/FmService$2;->this$0:Lcom/android/fmradio/FmService;

    invoke-virtual {p1, v1}, Lcom/android/fmradio/FmService;->setMute(Z)I

    .line 1106
    iget-object p1, p0, Lcom/android/fmradio/FmService$2;->this$0:Lcom/android/fmradio/FmService;

    invoke-static {p1, v0}, Lcom/android/fmradio/FmService;->access$700(Lcom/android/fmradio/FmService;I)V

    .line 1107
    monitor-exit p0

    goto :goto_0

    :catchall_1
    move-exception p1

    monitor-exit p0
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_1

    throw p1

    .line 1111
    :cond_3
    invoke-static {}, Lcom/android/fmradio/FmService;->access$000()Ljava/lang/String;

    move-result-object p1

    const-string v0, "--->>onAudioFocusChange()  ----AUDIOFOCUS_LOSS_TRANSIENT----"

    invoke-static {p1, v0}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 1112
    monitor-enter p0

    .line 1114
    :try_start_2
    iget-object p1, p0, Lcom/android/fmradio/FmService$2;->this$0:Lcom/android/fmradio/FmService;

    iput-boolean v1, p1, Lcom/android/fmradio/FmService;->mIsAudioFocusHeld:Z

    .line 1117
    monitor-exit p0

    goto :goto_0

    :catchall_2
    move-exception p1

    monitor-exit p0
    :try_end_2
    .catchall {:try_start_2 .. :try_end_2} :catchall_2

    throw p1

    .line 1136
    :cond_4
    invoke-static {}, Lcom/android/fmradio/FmService;->access$000()Ljava/lang/String;

    move-result-object p1

    const-string v0, "--->>onAudioFocusChange()  ----AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK----"

    invoke-static {p1, v0}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 1137
    iget-object p1, p0, Lcom/android/fmradio/FmService$2;->this$0:Lcom/android/fmradio/FmService;

    iput-boolean v1, p1, Lcom/android/fmradio/FmService;->mIsAudioFocusHeld:Z

    :goto_0
    return-void
.end method
