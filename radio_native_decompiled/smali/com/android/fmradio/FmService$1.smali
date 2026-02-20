.class Lcom/android/fmradio/FmService$1;
.super Landroid/media/session/MediaSession$Callback;
.source "FmService.java"


# annotations
.annotation system Ldalvik/annotation/EnclosingMethod;
    value = Lcom/android/fmradio/FmService;->headSetImplApi23()V
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

    .line 992
    iput-object p1, p0, Lcom/android/fmradio/FmService$1;->this$0:Lcom/android/fmradio/FmService;

    invoke-direct {p0}, Landroid/media/session/MediaSession$Callback;-><init>()V

    return-void
.end method


# virtual methods
.method public onMediaButtonEvent(Landroid/content/Intent;)Z
    .locals 3

    .line 995
    invoke-static {}, Lcom/android/fmradio/FmService;->access$000()Ljava/lang/String;

    move-result-object v0

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "---->>onMediaButtonEvent()  intent: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 996
    invoke-virtual {p1}, Landroid/content/Intent;->getAction()Ljava/lang/String;

    move-result-object v0

    const-string v1, "android.intent.action.MEDIA_BUTTON"

    invoke-virtual {v1, v0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v0

    const/4 v1, 0x1

    if-eqz v0, :cond_1

    const-string v0, "android.intent.extra.KEY_EVENT"

    .line 998
    invoke-virtual {p1, v0}, Landroid/content/Intent;->getParcelableExtra(Ljava/lang/String;)Landroid/os/Parcelable;

    move-result-object v0

    check-cast v0, Landroid/view/KeyEvent;

    .line 1000
    invoke-virtual {v0}, Landroid/view/KeyEvent;->isLongPress()Z

    move-result v2

    if-nez v2, :cond_0

    invoke-virtual {v0}, Landroid/view/KeyEvent;->getAction()I

    move-result v2

    if-ne v2, v1, :cond_0

    return v1

    .line 1003
    :cond_0
    iget-object v2, p0, Lcom/android/fmradio/FmService$1;->this$0:Lcom/android/fmradio/FmService;

    invoke-static {v2}, Lcom/android/fmradio/FmService;->access$500(Lcom/android/fmradio/FmService;)Lcom/android/fmradio/iface/IMediaButtonListener;

    move-result-object v2

    if-eqz v2, :cond_1

    .line 1004
    iget-object v2, p0, Lcom/android/fmradio/FmService$1;->this$0:Lcom/android/fmradio/FmService;

    invoke-static {v2}, Lcom/android/fmradio/FmService;->access$500(Lcom/android/fmradio/FmService;)Lcom/android/fmradio/iface/IMediaButtonListener;

    move-result-object v2

    invoke-virtual {v0}, Landroid/view/KeyEvent;->getKeyCode()I

    move-result v0

    invoke-interface {v2, p1, v0}, Lcom/android/fmradio/iface/IMediaButtonListener;->onMediaButtonEvent(Landroid/content/Intent;I)V

    :cond_1
    return v1
.end method
