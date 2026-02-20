.class Lcom/qf/skin/manager/loader/SkinInflaterFactory2$1;
.super Ljava/lang/Object;
.source "SkinInflaterFactory2.java"

# interfaces
.implements Lcom/qf/skin/manager/loader/SkinInflaterFactory2$RunMethod;


# annotations
.annotation system Ldalvik/annotation/EnclosingMethod;
    value = Lcom/qf/skin/manager/loader/SkinInflaterFactory2;->toString()Ljava/lang/String;
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

    .line 225
    iput-object p1, p0, Lcom/qf/skin/manager/loader/SkinInflaterFactory2$1;->this$0:Lcom/qf/skin/manager/loader/SkinInflaterFactory2;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public run(Ljava/lang/Object;Ljava/lang/Object;)V
    .locals 2

    .line 228
    new-instance v0, Ljava/lang/StringBuilder;

    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    const-string v1, "key ="

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v0, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p1

    invoke-static {p1}, Lcom/qf/skin/manager/util/L;->d(Ljava/lang/String;)V

    .line 229
    new-instance p1, Ljava/lang/StringBuilder;

    invoke-direct {p1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v0, "value size ="

    invoke-virtual {p1, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    check-cast p2, Ljava/util/concurrent/CopyOnWriteArrayList;

    invoke-virtual {p2}, Ljava/util/concurrent/CopyOnWriteArrayList;->size()I

    move-result p2

    invoke-virtual {p1, p2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {p1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p1

    invoke-static {p1}, Lcom/qf/skin/manager/util/L;->d(Ljava/lang/String;)V

    return-void
.end method
