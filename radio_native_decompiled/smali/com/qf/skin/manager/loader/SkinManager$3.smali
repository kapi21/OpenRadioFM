.class Lcom/qf/skin/manager/loader/SkinManager$3;
.super Ljava/lang/Object;
.source "SkinManager.java"

# interfaces
.implements Ljava/lang/Runnable;


# annotations
.annotation system Ldalvik/annotation/EnclosingMethod;
    value = Lcom/qf/skin/manager/loader/SkinManager;->notifyLoadCallbackFailed(Lcom/qf/skin/manager/exception/LoadException;)V
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x0
    name = null
.end annotation


# instance fields
.field final synthetic this$0:Lcom/qf/skin/manager/loader/SkinManager;

.field final synthetic val$e:Lcom/qf/skin/manager/exception/LoadException;


# direct methods
.method constructor <init>(Lcom/qf/skin/manager/loader/SkinManager;Lcom/qf/skin/manager/exception/LoadException;)V
    .locals 0

    .line 118
    iput-object p1, p0, Lcom/qf/skin/manager/loader/SkinManager$3;->this$0:Lcom/qf/skin/manager/loader/SkinManager;

    iput-object p2, p0, Lcom/qf/skin/manager/loader/SkinManager$3;->val$e:Lcom/qf/skin/manager/exception/LoadException;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public run()V
    .locals 2

    .line 121
    iget-object v0, p0, Lcom/qf/skin/manager/loader/SkinManager$3;->this$0:Lcom/qf/skin/manager/loader/SkinManager;

    invoke-static {v0}, Lcom/qf/skin/manager/loader/SkinManager;->access$200(Lcom/qf/skin/manager/loader/SkinManager;)Lcom/qf/skin/manager/interfaces/ISkinLoaderListener;

    move-result-object v0

    if-eqz v0, :cond_0

    .line 122
    iget-object v0, p0, Lcom/qf/skin/manager/loader/SkinManager$3;->this$0:Lcom/qf/skin/manager/loader/SkinManager;

    invoke-static {v0}, Lcom/qf/skin/manager/loader/SkinManager;->access$200(Lcom/qf/skin/manager/loader/SkinManager;)Lcom/qf/skin/manager/interfaces/ISkinLoaderListener;

    move-result-object v0

    iget-object v1, p0, Lcom/qf/skin/manager/loader/SkinManager$3;->val$e:Lcom/qf/skin/manager/exception/LoadException;

    invoke-interface {v0, v1}, Lcom/qf/skin/manager/interfaces/ISkinLoaderListener;->onFailed(Lcom/qf/skin/manager/exception/LoadException;)V

    .line 123
    new-instance v0, Ljava/lang/StringBuilder;

    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    const-string v1, "notifyLoadCallbackFailed "

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-object v1, p0, Lcom/qf/skin/manager/loader/SkinManager$3;->val$e:Lcom/qf/skin/manager/exception/LoadException;

    invoke-virtual {v1}, Lcom/qf/skin/manager/exception/LoadException;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    invoke-static {v0}, Lcom/qf/skin/manager/util/L;->d(Ljava/lang/String;)V

    :cond_0
    return-void
.end method
