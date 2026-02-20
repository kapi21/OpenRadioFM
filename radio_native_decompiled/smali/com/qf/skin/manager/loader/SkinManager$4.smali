.class Lcom/qf/skin/manager/loader/SkinManager$4;
.super Ljava/lang/Object;
.source "SkinManager.java"

# interfaces
.implements Ljava/lang/Runnable;


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
.method constructor <init>(Lcom/qf/skin/manager/loader/SkinManager;)V
    .locals 0

    .line 322
    iput-object p1, p0, Lcom/qf/skin/manager/loader/SkinManager$4;->this$0:Lcom/qf/skin/manager/loader/SkinManager;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public run()V
    .locals 2

    .line 325
    iget-object v0, p0, Lcom/qf/skin/manager/loader/SkinManager$4;->this$0:Lcom/qf/skin/manager/loader/SkinManager;

    const/4 v1, 0x1

    invoke-static {v0, v1}, Lcom/qf/skin/manager/loader/SkinManager;->access$300(Lcom/qf/skin/manager/loader/SkinManager;Z)V

    return-void
.end method
