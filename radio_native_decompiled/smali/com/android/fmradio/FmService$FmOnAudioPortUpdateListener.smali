.class Lcom/android/fmradio/FmService$FmOnAudioPortUpdateListener;
.super Ljava/lang/Object;
.source "FmService.java"

# interfaces
.implements Landroid/media/AudioManager$OnAudioPortUpdateListener;


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Lcom/android/fmradio/FmService;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x2
    name = "FmOnAudioPortUpdateListener"
.end annotation


# instance fields
.field final synthetic this$0:Lcom/android/fmradio/FmService;


# direct methods
.method private constructor <init>(Lcom/android/fmradio/FmService;)V
    .locals 0

    .line 714
    iput-object p1, p0, Lcom/android/fmradio/FmService$FmOnAudioPortUpdateListener;->this$0:Lcom/android/fmradio/FmService;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public onAudioPatchListUpdate([Landroid/media/AudioPatch;)V
    .locals 0

    return-void
.end method

.method public onAudioPortListUpdate([Landroid/media/AudioPort;)V
    .locals 0

    return-void
.end method

.method public onServiceDied()V
    .locals 2

    .line 740
    iget-object v0, p0, Lcom/android/fmradio/FmService$FmOnAudioPortUpdateListener;->this$0:Lcom/android/fmradio/FmService;

    const/4 v1, 0x0

    invoke-static {v0, v1}, Lcom/android/fmradio/FmService;->access$300(Lcom/android/fmradio/FmService;Z)V

    return-void
.end method
