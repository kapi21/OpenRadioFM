.class Lcom/android/fmradio/views/RadioOperationView$1$1;
.super Ljava/lang/Object;
.source "RadioOperationView.java"

# interfaces
.implements Lcom/android/fmradio/favorite/FmFavoriteListFragment$OnFmStationInfoListener;


# annotations
.annotation system Ldalvik/annotation/EnclosingMethod;
    value = Lcom/android/fmradio/views/RadioOperationView$1;->onLongClick(Landroid/view/View;)Z
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x0
    name = null
.end annotation


# instance fields
.field final synthetic this$1:Lcom/android/fmradio/views/RadioOperationView$1;


# direct methods
.method constructor <init>(Lcom/android/fmradio/views/RadioOperationView$1;)V
    .locals 0

    .line 85
    iput-object p1, p0, Lcom/android/fmradio/views/RadioOperationView$1$1;->this$1:Lcom/android/fmradio/views/RadioOperationView$1;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public onItemClickListener(I)V
    .locals 1

    .line 88
    invoke-static {}, Landroid/qf/os/QFApi;->isBTPhoneStartup()Z

    move-result v0

    if-eqz v0, :cond_0

    return-void

    .line 92
    :cond_0
    iget-object v0, p0, Lcom/android/fmradio/views/RadioOperationView$1$1;->this$1:Lcom/android/fmradio/views/RadioOperationView$1;

    iget-object v0, v0, Lcom/android/fmradio/views/RadioOperationView$1;->this$0:Lcom/android/fmradio/views/RadioOperationView;

    invoke-static {v0}, Lcom/android/fmradio/views/RadioOperationView;->access$000(Lcom/android/fmradio/views/RadioOperationView;)Lcom/android/fmradio/FmMainActivity;

    move-result-object v0

    iput p1, v0, Lcom/android/fmradio/FmMainActivity;->mCurrentStation:I

    .line 93
    iget-object v0, p0, Lcom/android/fmradio/views/RadioOperationView$1$1;->this$1:Lcom/android/fmradio/views/RadioOperationView$1;

    iget-object v0, v0, Lcom/android/fmradio/views/RadioOperationView$1;->this$0:Lcom/android/fmradio/views/RadioOperationView;

    invoke-static {v0}, Lcom/android/fmradio/views/RadioOperationView;->access$000(Lcom/android/fmradio/views/RadioOperationView;)Lcom/android/fmradio/FmMainActivity;

    move-result-object v0

    invoke-virtual {v0}, Lcom/android/fmradio/FmMainActivity;->requestAudioFocus()V

    .line 94
    iget-object v0, p0, Lcom/android/fmradio/views/RadioOperationView$1$1;->this$1:Lcom/android/fmradio/views/RadioOperationView$1;

    iget-object v0, v0, Lcom/android/fmradio/views/RadioOperationView$1;->this$0:Lcom/android/fmradio/views/RadioOperationView;

    invoke-static {v0}, Lcom/android/fmradio/views/RadioOperationView;->access$000(Lcom/android/fmradio/views/RadioOperationView;)Lcom/android/fmradio/FmMainActivity;

    move-result-object v0

    invoke-virtual {v0, p1}, Lcom/android/fmradio/FmMainActivity;->tuneStation(I)V

    return-void
.end method
