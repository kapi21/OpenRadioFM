.class public Lcom/qf/skin/manager/util/L;
.super Ljava/lang/Object;
.source "L.java"


# static fields
.field public static DEBUG:Z = true

.field public static INFO_LEVEL_DEBUG:Z = false

.field private static final LINE:Ljava/lang/String; = "________________________________________________________"

.field private static final TAG:Ljava/lang/String; = "SkinLoader"


# direct methods
.method static constructor <clinit>()V
    .locals 0

    return-void
.end method

.method private constructor <init>()V
    .locals 1

    .line 13
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 14
    new-instance v0, Ljava/lang/AssertionError;

    invoke-direct {v0}, Ljava/lang/AssertionError;-><init>()V

    throw v0
.end method

.method public static d(Ljava/lang/String;)V
    .locals 2

    .line 33
    sget-boolean v0, Lcom/qf/skin/manager/util/L;->DEBUG:Z

    if-eqz v0, :cond_0

    const-string v0, "SkinLoader"

    const-string v1, "________________________________________________________"

    .line 34
    invoke-static {v0, v1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    .line 35
    invoke-static {v0, p0}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    :cond_0
    return-void
.end method

.method public static d(Ljava/lang/String;Ljava/lang/String;)V
    .locals 1

    .line 61
    sget-boolean v0, Lcom/qf/skin/manager/util/L;->DEBUG:Z

    if-eqz v0, :cond_0

    const-string v0, "________________________________________________________"

    .line 62
    invoke-static {p0, v0}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    .line 63
    invoke-static {p0, p1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    :cond_0
    return-void
.end method

.method public static e(Ljava/lang/String;)V
    .locals 2

    .line 47
    sget-boolean v0, Lcom/qf/skin/manager/util/L;->DEBUG:Z

    if-eqz v0, :cond_0

    const-string v0, "SkinLoader"

    const-string v1, "________________________________________________________"

    .line 48
    invoke-static {v0, v1}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;)I

    .line 49
    invoke-static {v0, p0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;)I

    :cond_0
    return-void
.end method

.method public static e(Ljava/lang/String;Ljava/lang/String;)V
    .locals 1

    .line 75
    sget-boolean v0, Lcom/qf/skin/manager/util/L;->DEBUG:Z

    if-eqz v0, :cond_0

    const-string v0, "________________________________________________________"

    .line 76
    invoke-static {p0, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;)I

    .line 77
    invoke-static {p0, p1}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;)I

    :cond_0
    return-void
.end method

.method public static i(Ljava/lang/String;)V
    .locals 2

    .line 26
    sget-boolean v0, Lcom/qf/skin/manager/util/L;->DEBUG:Z

    if-eqz v0, :cond_0

    sget-boolean v0, Lcom/qf/skin/manager/util/L;->INFO_LEVEL_DEBUG:Z

    if-eqz v0, :cond_0

    const-string v0, "SkinLoader"

    const-string v1, "________________________________________________________"

    .line 27
    invoke-static {v0, v1}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    .line 28
    invoke-static {v0, p0}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    :cond_0
    return-void
.end method

.method public static i(Ljava/lang/String;Ljava/lang/String;)V
    .locals 1

    .line 54
    sget-boolean v0, Lcom/qf/skin/manager/util/L;->DEBUG:Z

    if-eqz v0, :cond_0

    const-string v0, "________________________________________________________"

    .line 55
    invoke-static {p0, v0}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    .line 56
    invoke-static {p0, p1}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    :cond_0
    return-void
.end method

.method public static setDEBUG(Z)V
    .locals 0

    .line 18
    sput-boolean p0, Lcom/qf/skin/manager/util/L;->DEBUG:Z

    return-void
.end method

.method public static setInfoDEBUG(Z)V
    .locals 0

    .line 22
    sput-boolean p0, Lcom/qf/skin/manager/util/L;->INFO_LEVEL_DEBUG:Z

    return-void
.end method

.method public static w(Ljava/lang/String;)V
    .locals 2

    .line 40
    sget-boolean v0, Lcom/qf/skin/manager/util/L;->DEBUG:Z

    if-eqz v0, :cond_0

    const-string v0, "SkinLoader"

    const-string v1, "________________________________________________________"

    .line 41
    invoke-static {v0, v1}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;)I

    .line 42
    invoke-static {v0, p0}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;)I

    :cond_0
    return-void
.end method

.method public static w(Ljava/lang/String;Ljava/lang/String;)V
    .locals 1

    .line 68
    sget-boolean v0, Lcom/qf/skin/manager/util/L;->DEBUG:Z

    if-eqz v0, :cond_0

    const-string v0, "________________________________________________________"

    .line 69
    invoke-static {p0, v0}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;)I

    .line 70
    invoke-static {p0, p1}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;)I

    :cond_0
    return-void
.end method
