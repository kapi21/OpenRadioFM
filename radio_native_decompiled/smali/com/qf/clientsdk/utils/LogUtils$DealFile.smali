.class Lcom/qf/clientsdk/utils/LogUtils$DealFile;
.super Ljava/lang/Object;
.source "LogUtils.java"

# interfaces
.implements Ljava/lang/Runnable;


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Lcom/qf/clientsdk/utils/LogUtils;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x0
    name = "DealFile"
.end annotation


# instance fields
.field private out:Ljava/io/FileOutputStream;

.field private queue:Ljava/util/concurrent/ConcurrentLinkedQueue;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Ljava/util/concurrent/ConcurrentLinkedQueue<",
            "Ljava/lang/String;",
            ">;"
        }
    .end annotation
.end field

.field final synthetic this$0:Lcom/qf/clientsdk/utils/LogUtils;


# direct methods
.method public constructor <init>(Lcom/qf/clientsdk/utils/LogUtils;)V
    .locals 0

    .line 222
    iput-object p1, p0, Lcom/qf/clientsdk/utils/LogUtils$DealFile;->this$0:Lcom/qf/clientsdk/utils/LogUtils;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

.method public constructor <init>(Lcom/qf/clientsdk/utils/LogUtils;Ljava/io/FileOutputStream;Ljava/util/concurrent/ConcurrentLinkedQueue;)V
    .locals 0
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Ljava/io/FileOutputStream;",
            "Ljava/util/concurrent/ConcurrentLinkedQueue<",
            "Ljava/lang/String;",
            ">;)V"
        }
    .end annotation

    .line 225
    iput-object p1, p0, Lcom/qf/clientsdk/utils/LogUtils$DealFile;->this$0:Lcom/qf/clientsdk/utils/LogUtils;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 226
    iput-object p2, p0, Lcom/qf/clientsdk/utils/LogUtils$DealFile;->out:Ljava/io/FileOutputStream;

    .line 227
    iput-object p3, p0, Lcom/qf/clientsdk/utils/LogUtils$DealFile;->queue:Ljava/util/concurrent/ConcurrentLinkedQueue;

    return-void
.end method


# virtual methods
.method public run()V
    .locals 2

    .line 232
    :goto_0
    iget-object v0, p0, Lcom/qf/clientsdk/utils/LogUtils$DealFile;->queue:Ljava/util/concurrent/ConcurrentLinkedQueue;

    invoke-virtual {v0}, Ljava/util/concurrent/ConcurrentLinkedQueue;->isEmpty()Z

    move-result v0

    if-nez v0, :cond_0

    .line 234
    :try_start_0
    iget-object v0, p0, Lcom/qf/clientsdk/utils/LogUtils$DealFile;->out:Ljava/io/FileOutputStream;

    iget-object v1, p0, Lcom/qf/clientsdk/utils/LogUtils$DealFile;->queue:Ljava/util/concurrent/ConcurrentLinkedQueue;

    invoke-virtual {v1}, Ljava/util/concurrent/ConcurrentLinkedQueue;->poll()Ljava/lang/Object;

    move-result-object v1

    check-cast v1, Ljava/lang/String;

    invoke-virtual {v1}, Ljava/lang/String;->getBytes()[B

    move-result-object v1

    invoke-virtual {v0, v1}, Ljava/io/FileOutputStream;->write([B)V
    :try_end_0
    .catch Ljava/io/IOException; {:try_start_0 .. :try_end_0} :catch_0

    goto :goto_1

    :catch_0
    move-exception v0

    .line 236
    invoke-virtual {v0}, Ljava/io/IOException;->printStackTrace()V

    :cond_0
    :goto_1
    const-wide/16 v0, 0x64

    .line 240
    :try_start_1
    invoke-static {v0, v1}, Ljava/lang/Thread;->sleep(J)V
    :try_end_1
    .catch Ljava/lang/InterruptedException; {:try_start_1 .. :try_end_1} :catch_1

    goto :goto_0

    :catch_1
    move-exception v0

    .line 242
    invoke-virtual {v0}, Ljava/lang/InterruptedException;->printStackTrace()V

    goto :goto_0
.end method
