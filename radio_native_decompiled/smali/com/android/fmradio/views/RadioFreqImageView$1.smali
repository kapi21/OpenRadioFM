.class Lcom/android/fmradio/views/RadioFreqImageView$1;
.super Ljava/lang/Object;
.source "RadioFreqImageView.java"

# interfaces
.implements Lcom/qf/skin/manager/interfaces/ISkinUpdate;


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Lcom/android/fmradio/views/RadioFreqImageView;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x0
    name = null
.end annotation


# instance fields
.field final synthetic this$0:Lcom/android/fmradio/views/RadioFreqImageView;


# direct methods
.method constructor <init>(Lcom/android/fmradio/views/RadioFreqImageView;)V
    .locals 0

    .line 44
    iput-object p1, p0, Lcom/android/fmradio/views/RadioFreqImageView$1;->this$0:Lcom/android/fmradio/views/RadioFreqImageView;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public onThemeUpdate(Z)V
    .locals 1

    .line 47
    invoke-static {}, Lcom/android/fmradio/views/RadioFreqImageView;->access$000()Ljava/lang/String;

    move-result-object p1

    const-string v0, "start"

    invoke-static {p1, v0}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 49
    iget-object p1, p0, Lcom/android/fmradio/views/RadioFreqImageView$1;->this$0:Lcom/android/fmradio/views/RadioFreqImageView;

    invoke-static {p1}, Lcom/android/fmradio/views/RadioFreqImageView;->access$100(Lcom/android/fmradio/views/RadioFreqImageView;)Landroid/content/Context;

    move-result-object v0

    invoke-static {v0}, Lcom/android/fmradio/database/FmStation;->getCurrentStation(Landroid/content/Context;)I

    move-result v0

    invoke-virtual {p1, v0}, Lcom/android/fmradio/views/RadioFreqImageView;->updateFreqValue(I)V

    return-void
.end method
