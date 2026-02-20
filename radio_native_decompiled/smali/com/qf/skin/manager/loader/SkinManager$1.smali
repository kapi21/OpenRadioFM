.class Lcom/qf/skin/manager/loader/SkinManager$1;
.super Landroid/os/Handler;
.source "SkinManager.java"


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Lcom/qf/skin/manager/loader/SkinManager;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x0
    name = null
.end annotation


# instance fields
.field final synthetic this$0:Lcom/qf/skin/manager/loader/SkinManager;


# direct methods
.method constructor <init>(Lcom/qf/skin/manager/loader/SkinManager;Landroid/os/Looper;)V
    .locals 0

    .line 82
    iput-object p1, p0, Lcom/qf/skin/manager/loader/SkinManager$1;->this$0:Lcom/qf/skin/manager/loader/SkinManager;

    invoke-direct {p0, p2}, Landroid/os/Handler;-><init>(Landroid/os/Looper;)V

    return-void
.end method


# virtual methods
.method public handleMessage(Landroid/os/Message;)V
    .locals 1

    .line 85
    iget p1, p1, Landroid/os/Message;->what:I

    const/4 v0, 0x2

    if-eq p1, v0, :cond_2

    const/4 v0, 0x3

    if-eq p1, v0, :cond_0

    goto :goto_0

    .line 92
    :cond_0
    iget-object p1, p0, Lcom/qf/skin/manager/loader/SkinManager$1;->this$0:Lcom/qf/skin/manager/loader/SkinManager;

    invoke-static {p1}, Lcom/qf/skin/manager/loader/SkinManager;->access$000(Lcom/qf/skin/manager/loader/SkinManager;)Z

    move-result p1

    if-nez p1, :cond_1

    goto :goto_0

    .line 95
    :cond_1
    iget-object p1, p0, Lcom/qf/skin/manager/loader/SkinManager$1;->this$0:Lcom/qf/skin/manager/loader/SkinManager;

    invoke-static {p1}, Lcom/qf/skin/manager/loader/SkinManager;->access$100(Lcom/qf/skin/manager/loader/SkinManager;)V

    :cond_2
    :goto_0
    return-void
.end method
