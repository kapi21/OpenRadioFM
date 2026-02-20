.class Lcom/android/fmradio/views/FmVisualizerView$1;
.super Ljava/lang/Object;
.source "FmVisualizerView.java"

# interfaces
.implements Ljava/lang/Runnable;


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Lcom/android/fmradio/views/FmVisualizerView;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x0
    name = null
.end annotation


# instance fields
.field final synthetic this$0:Lcom/android/fmradio/views/FmVisualizerView;


# direct methods
.method constructor <init>(Lcom/android/fmradio/views/FmVisualizerView;)V
    .locals 0

    .line 139
    iput-object p1, p0, Lcom/android/fmradio/views/FmVisualizerView$1;->this$0:Lcom/android/fmradio/views/FmVisualizerView;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public run()V
    .locals 1

    .line 141
    iget-object v0, p0, Lcom/android/fmradio/views/FmVisualizerView$1;->this$0:Lcom/android/fmradio/views/FmVisualizerView;

    invoke-virtual {v0}, Lcom/android/fmradio/views/FmVisualizerView;->invalidate()V

    return-void
.end method
