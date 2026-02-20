.class public Lcom/qf/skin/manager/exception/LoadException;
.super Ljava/lang/Object;
.source "LoadException.java"


# static fields
.field public static final E_LOAD_OTHER_SKIN:B = 0x1t

.field public static final E_LOAD_PATH_ERROR:B = 0x3t

.field public static final E_THE_SAME_SKIN:B = 0x2t


# instance fields
.field private exceptionTag:B


# direct methods
.method public constructor <init>(B)V
    .locals 1

    .line 16
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    const/4 v0, 0x0

    .line 14
    iput-byte v0, p0, Lcom/qf/skin/manager/exception/LoadException;->exceptionTag:B

    .line 17
    iput-byte p1, p0, Lcom/qf/skin/manager/exception/LoadException;->exceptionTag:B

    return-void
.end method


# virtual methods
.method public getExceptionTag()B
    .locals 1

    .line 21
    iget-byte v0, p0, Lcom/qf/skin/manager/exception/LoadException;->exceptionTag:B

    return v0
.end method

.method public toString()Ljava/lang/String;
    .locals 2

    .line 27
    iget-byte v0, p0, Lcom/qf/skin/manager/exception/LoadException;->exceptionTag:B

    const/4 v1, 0x1

    if-eq v0, v1, :cond_2

    const/4 v1, 0x2

    if-eq v0, v1, :cond_1

    const/4 v1, 0x3

    if-eq v0, v1, :cond_0

    const/4 v0, 0x0

    return-object v0

    .line 33
    :cond_0
    new-instance v0, Ljava/lang/String;

    const-string v1, "Can not find this skin path!!"

    invoke-direct {v0, v1}, Ljava/lang/String;-><init>(Ljava/lang/String;)V

    return-object v0

    .line 31
    :cond_1
    new-instance v0, Ljava/lang/String;

    const-string v1, "this skin is using now!!"

    invoke-direct {v0, v1}, Ljava/lang/String;-><init>(Ljava/lang/String;)V

    return-object v0

    .line 29
    :cond_2
    new-instance v0, Ljava/lang/String;

    const-string v1, "It is Loading onth skin now!!"

    invoke-direct {v0, v1}, Ljava/lang/String;-><init>(Ljava/lang/String;)V

    return-object v0
.end method
