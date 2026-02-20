.class Lcom/qf/skin/manager/base/SkinBaseApplication$SkinChangeObserver;
.super Landroid/database/ContentObserver;
.source "SkinBaseApplication.java"


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Lcom/qf/skin/manager/base/SkinBaseApplication;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x2
    name = "SkinChangeObserver"
.end annotation


# instance fields
.field final synthetic this$0:Lcom/qf/skin/manager/base/SkinBaseApplication;


# direct methods
.method private constructor <init>(Lcom/qf/skin/manager/base/SkinBaseApplication;)V
    .locals 0

    .line 72
    iput-object p1, p0, Lcom/qf/skin/manager/base/SkinBaseApplication$SkinChangeObserver;->this$0:Lcom/qf/skin/manager/base/SkinBaseApplication;

    .line 73
    new-instance p1, Landroid/os/Handler;

    invoke-direct {p1}, Landroid/os/Handler;-><init>()V

    invoke-direct {p0, p1}, Landroid/database/ContentObserver;-><init>(Landroid/os/Handler;)V

    return-void
.end method

.method synthetic constructor <init>(Lcom/qf/skin/manager/base/SkinBaseApplication;Lcom/qf/skin/manager/base/SkinBaseApplication$1;)V
    .locals 0

    .line 71
    invoke-direct {p0, p1}, Lcom/qf/skin/manager/base/SkinBaseApplication$SkinChangeObserver;-><init>(Lcom/qf/skin/manager/base/SkinBaseApplication;)V

    return-void
.end method


# virtual methods
.method public onChange(ZLandroid/net/Uri;)V
    .locals 0

    .line 78
    invoke-super {p0, p1, p2}, Landroid/database/ContentObserver;->onChange(ZLandroid/net/Uri;)V

    .line 79
    invoke-virtual {p2}, Landroid/net/Uri;->getLastPathSegment()Ljava/lang/String;

    .line 80
    invoke-static {}, Lcom/qf/skin/manager/loader/SkinManager;->getInstance()Lcom/qf/skin/manager/loader/SkinManager;

    move-result-object p1

    invoke-virtual {p1}, Lcom/qf/skin/manager/loader/SkinManager;->handlerNotifySkinUpdate()V

    return-void
.end method
