.class Lcom/android/fmradio/FmMainActivity$7;
.super Ljava/lang/Object;
.source "FmMainActivity.java"

# interfaces
.implements Landroid/widget/AdapterView$OnItemClickListener;


# annotations
.annotation system Ldalvik/annotation/EnclosingMethod;
    value = Lcom/android/fmradio/FmMainActivity;->initRdsPtyView()V
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

    .line 1296
    iput-object p1, p0, Lcom/android/fmradio/FmMainActivity$7;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public onItemClick(Landroid/widget/AdapterView;Landroid/view/View;IJ)V
    .locals 0
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Landroid/widget/AdapterView<",
            "*>;",
            "Landroid/view/View;",
            "IJ)V"
        }
    .end annotation

    .line 1299
    invoke-static {}, Landroid/qf/os/QFApi;->isBTPhoneStartup()Z

    move-result p1

    if-eqz p1, :cond_0

    return-void

    .line 1303
    :cond_0
    invoke-static {}, Lcom/android/fmradio/FmMainActivity;->access$100()Ljava/lang/String;

    move-result-object p1

    new-instance p2, Ljava/lang/StringBuilder;

    invoke-direct {p2}, Ljava/lang/StringBuilder;-><init>()V

    const-string p4, "initRdsPty - position: "

    invoke-virtual {p2, p4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {p2, p3}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {p2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p2

    invoke-static {p1, p2}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 1304
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$7;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {p1}, Lcom/android/fmradio/FmMainActivity;->requestAudioFocus()V

    .line 1306
    iget-object p1, p0, Lcom/android/fmradio/FmMainActivity$7;->this$0:Lcom/android/fmradio/FmMainActivity;

    invoke-static {p1, p3}, Lcom/android/fmradio/FmMainActivity;->access$2200(Lcom/android/fmradio/FmMainActivity;I)V

    return-void
.end method
