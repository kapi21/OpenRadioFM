.class Lcom/qf/skin/manager/loader/SkinInflaterFactory2$2;
.super Ljava/lang/Object;
.source "SkinInflaterFactory2.java"

# interfaces
.implements Lcom/qf/skin/manager/loader/SkinInflaterFactory2$RunMethod;


# annotations
.annotation system Ldalvik/annotation/EnclosingMethod;
    value = Lcom/qf/skin/manager/loader/SkinInflaterFactory2;->applySkin()V
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x0
    name = null
.end annotation


# instance fields
.field final synthetic this$0:Lcom/qf/skin/manager/loader/SkinInflaterFactory2;


# direct methods
.method constructor <init>(Lcom/qf/skin/manager/loader/SkinInflaterFactory2;)V
    .locals 0

    .line 245
    iput-object p1, p0, Lcom/qf/skin/manager/loader/SkinInflaterFactory2$2;->this$0:Lcom/qf/skin/manager/loader/SkinInflaterFactory2;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public run(Ljava/lang/Object;Ljava/lang/Object;)V
    .locals 1

    .line 248
    check-cast p2, Ljava/util/concurrent/CopyOnWriteArrayList;

    .line 249
    new-instance p1, Ljava/lang/StringBuilder;

    invoke-direct {p1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v0, " SkinInflaterFactory2 applySkin size:"

    invoke-virtual {p1, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {p2}, Ljava/util/concurrent/CopyOnWriteArrayList;->size()I

    move-result v0

    invoke-virtual {p1, v0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {p1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p1

    invoke-static {p1}, Lcom/qf/skin/manager/util/L;->d(Ljava/lang/String;)V

    .line 250
    invoke-virtual {p2}, Ljava/util/concurrent/CopyOnWriteArrayList;->iterator()Ljava/util/Iterator;

    move-result-object p1

    :goto_0
    invoke-interface {p1}, Ljava/util/Iterator;->hasNext()Z

    move-result p2

    if-eqz p2, :cond_1

    invoke-interface {p1}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object p2

    check-cast p2, Lcom/qf/skin/manager/entity/SkinItem;

    .line 251
    iget-object v0, p2, Lcom/qf/skin/manager/entity/SkinItem;->view:Landroid/view/View;

    if-nez v0, :cond_0

    goto :goto_0

    .line 254
    :cond_0
    invoke-virtual {p2}, Lcom/qf/skin/manager/entity/SkinItem;->apply()V

    goto :goto_0

    :cond_1
    return-void
.end method
