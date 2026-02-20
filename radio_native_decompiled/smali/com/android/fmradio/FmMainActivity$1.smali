.class Lcom/android/fmradio/FmMainActivity$1;
.super Landroid/content/BroadcastReceiver;
.source "FmMainActivity.java"


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

    .line 144
    iput-object p1, p0, Lcom/android/fmradio/FmMainActivity$1;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-direct {p0}, Landroid/content/BroadcastReceiver;-><init>()V

    return-void
.end method


# virtual methods
.method public onReceive(Landroid/content/Context;Landroid/content/Intent;)V
    .locals 1

    .line 147
    invoke-virtual {p2}, Landroid/content/Intent;->getAction()Ljava/lang/String;

    move-result-object p1

    const-string v0, "com.android.fmradio.favorite_changed"

    .line 148
    invoke-virtual {p1, v0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result p1

    if-eqz p1, :cond_0

    const/4 p1, 0x0

    const-string v0, "favorite_changed_key"

    .line 149
    invoke-virtual {p2, v0, p1}, Landroid/content/Intent;->getBooleanExtra(Ljava/lang/String;Z)Z

    move-result p1

    .line 150
    iget-object p2, p0, Lcom/android/fmradio/FmMainActivity$1;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-static {p2}, Lcom/android/fmradio/FmMainActivity;->access$000(Lcom/android/fmradio/FmMainActivity;)Lcom/android/fmradio/views/RadioFreqInfoView;

    move-result-object p2

    invoke-virtual {p2, p1}, Lcom/android/fmradio/views/RadioFreqInfoView;->updateFavoriteState(Z)V

    :cond_0
    return-void
.end method
