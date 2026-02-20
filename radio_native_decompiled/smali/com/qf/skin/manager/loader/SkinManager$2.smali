.class Lcom/qf/skin/manager/loader/SkinManager$2;
.super Ljava/lang/Object;
.source "SkinManager.java"

# interfaces
.implements Ljava/lang/Runnable;


# annotations
.annotation system Ldalvik/annotation/EnclosingMethod;
    value = Lcom/qf/skin/manager/loader/SkinManager;->notifyLoadCallbackStart()V
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x0
    name = null
.end annotation


# instance fields
.field final synthetic this$0:Lcom/qf/skin/manager/loader/SkinManager;


# direct methods
.method constructor <init>(Lcom/qf/skin/manager/loader/SkinManager;)V
    .locals 0

    .line 104
    iput-object p1, p0, Lcom/qf/skin/manager/loader/SkinManager$2;->this$0:Lcom/qf/skin/manager/loader/SkinManager;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public run()V
    .locals 1

    .line 108
    iget-object v0, p0, Lcom/qf/skin/manager/loader/SkinManager$2;->this$0:Lcom/qf/skin/manager/loader/SkinManager;

    invoke-static {v0}, Lcom/qf/skin/manager/loader/SkinManager;->access$200(Lcom/qf/skin/manager/loader/SkinManager;)Lcom/qf/skin/manager/interfaces/ISkinLoaderListener;

    move-result-object v0

    if-eqz v0, :cond_0

    .line 109
    iget-object v0, p0, Lcom/qf/skin/manager/loader/SkinManager$2;->this$0:Lcom/qf/skin/manager/loader/SkinManager;

    invoke-static {v0}, Lcom/qf/skin/manager/loader/SkinManager;->access$200(Lcom/qf/skin/manager/loader/SkinManager;)Lcom/qf/skin/manager/interfaces/ISkinLoaderListener;

    move-result-object v0

    invoke-interface {v0}, Lcom/qf/skin/manager/interfaces/ISkinLoaderListener;->onStart()V

    const-string v0, "notifyLoadCallbackStart "

    .line 110
    invoke-static {v0}, Lcom/qf/skin/manager/util/L;->d(Ljava/lang/String;)V

    :cond_0
    return-void
.end method
