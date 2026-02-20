.class public abstract Lcom/qf/skin/manager/base/SkinBaseApplication;
.super Landroid/app/Application;
.source "SkinBaseApplication.java"


# annotations
.annotation system Ldalvik/annotation/MemberClasses;
    value = {
        Lcom/qf/skin/manager/base/SkinBaseApplication$SkinChangeObserver;
    }
.end annotation


# instance fields
.field private skinChangeObserver:Lcom/qf/skin/manager/base/SkinBaseApplication$SkinChangeObserver;

.field private useSkinLib:Z


# direct methods
.method public constructor <init>()V
    .locals 1

    .line 24
    invoke-direct {p0}, Landroid/app/Application;-><init>()V

    const/4 v0, 0x0

    .line 32
    iput-boolean v0, p0, Lcom/qf/skin/manager/base/SkinBaseApplication;->useSkinLib:Z

    return-void
.end method

.method private initSkinLoader(Landroid/content/Context;)V
    .locals 2

    .line 57
    invoke-virtual {p0}, Lcom/qf/skin/manager/base/SkinBaseApplication;->addSupportAttrName()Ljava/util/HashMap;

    move-result-object v0

    if-eqz v0, :cond_0

    .line 58
    invoke-virtual {v0}, Ljava/util/HashMap;->size()I

    move-result v1

    if-lez v1, :cond_0

    .line 59
    new-instance v1, Lcom/qf/skin/manager/base/SkinBaseApplication$1;

    invoke-direct {v1, p0}, Lcom/qf/skin/manager/base/SkinBaseApplication$1;-><init>(Lcom/qf/skin/manager/base/SkinBaseApplication;)V

    invoke-virtual {v0, v1}, Ljava/util/HashMap;->forEach(Ljava/util/function/BiConsumer;)V

    :cond_0
    const/4 v0, 0x0

    .line 66
    invoke-static {v0}, Lcom/qf/skin/manager/config/SkinConfig;->setInfoDebug(Z)V

    .line 67
    invoke-static {v0}, Lcom/qf/skin/manager/config/SkinConfig;->setDebug(Z)V

    .line 68
    invoke-static {}, Lcom/qf/skin/manager/loader/SkinManager;->getInstance()Lcom/qf/skin/manager/loader/SkinManager;

    move-result-object v0

    const/4 v1, 0x1

    invoke-virtual {v0, p1, v1}, Lcom/qf/skin/manager/loader/SkinManager;->init(Landroid/content/Context;Z)V

    return-void
.end method


# virtual methods
.method public abstract addSupportAttrName()Ljava/util/HashMap;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "()",
            "Ljava/util/HashMap<",
            "Ljava/lang/String;",
            "Lcom/qf/skin/manager/entity/SkinAttr;",
            ">;"
        }
    .end annotation
.end method

.method protected attachBaseContext(Landroid/content/Context;)V
    .locals 1

    .line 45
    iget-boolean v0, p0, Lcom/qf/skin/manager/base/SkinBaseApplication;->useSkinLib:Z

    if-eqz v0, :cond_0

    .line 46
    invoke-direct {p0, p1}, Lcom/qf/skin/manager/base/SkinBaseApplication;->initSkinLoader(Landroid/content/Context;)V

    .line 48
    :cond_0
    invoke-super {p0, p1}, Landroid/app/Application;->attachBaseContext(Landroid/content/Context;)V

    return-void
.end method

.method public onCreate()V
    .locals 4

    .line 36
    invoke-super {p0}, Landroid/app/Application;->onCreate()V

    .line 37
    iget-boolean v0, p0, Lcom/qf/skin/manager/base/SkinBaseApplication;->useSkinLib:Z

    if-eqz v0, :cond_0

    .line 38
    new-instance v0, Lcom/qf/skin/manager/base/SkinBaseApplication$SkinChangeObserver;

    const/4 v1, 0x0

    invoke-direct {v0, p0, v1}, Lcom/qf/skin/manager/base/SkinBaseApplication$SkinChangeObserver;-><init>(Lcom/qf/skin/manager/base/SkinBaseApplication;Lcom/qf/skin/manager/base/SkinBaseApplication$1;)V

    iput-object v0, p0, Lcom/qf/skin/manager/base/SkinBaseApplication;->skinChangeObserver:Lcom/qf/skin/manager/base/SkinBaseApplication$SkinChangeObserver;

    .line 39
    invoke-virtual {p0}, Lcom/qf/skin/manager/base/SkinBaseApplication;->getContentResolver()Landroid/content/ContentResolver;

    move-result-object v0

    const-string v1, "ui_night_mode"

    invoke-static {v1}, Landroid/provider/Settings$Secure;->getUriFor(Ljava/lang/String;)Landroid/net/Uri;

    move-result-object v1

    const/4 v2, 0x0

    iget-object v3, p0, Lcom/qf/skin/manager/base/SkinBaseApplication;->skinChangeObserver:Lcom/qf/skin/manager/base/SkinBaseApplication$SkinChangeObserver;

    invoke-virtual {v0, v1, v2, v3}, Landroid/content/ContentResolver;->registerContentObserver(Landroid/net/Uri;ZLandroid/database/ContentObserver;)V

    :cond_0
    return-void
.end method

.method public onTerminate()V
    .locals 2

    .line 86
    invoke-super {p0}, Landroid/app/Application;->onTerminate()V

    .line 87
    iget-object v0, p0, Lcom/qf/skin/manager/base/SkinBaseApplication;->skinChangeObserver:Lcom/qf/skin/manager/base/SkinBaseApplication$SkinChangeObserver;

    if-eqz v0, :cond_0

    .line 88
    invoke-virtual {p0}, Lcom/qf/skin/manager/base/SkinBaseApplication;->getContentResolver()Landroid/content/ContentResolver;

    move-result-object v0

    iget-object v1, p0, Lcom/qf/skin/manager/base/SkinBaseApplication;->skinChangeObserver:Lcom/qf/skin/manager/base/SkinBaseApplication$SkinChangeObserver;

    invoke-virtual {v0, v1}, Landroid/content/ContentResolver;->unregisterContentObserver(Landroid/database/ContentObserver;)V

    :cond_0
    return-void
.end method

.method public setUseSkinLib(Z)V
    .locals 0

    .line 52
    iput-boolean p1, p0, Lcom/qf/skin/manager/base/SkinBaseApplication;->useSkinLib:Z

    .line 53
    iget-boolean p1, p0, Lcom/qf/skin/manager/base/SkinBaseApplication;->useSkinLib:Z

    invoke-static {p1}, Lcom/qf/skin/manager/config/SkinConfig;->setDebug(Z)V

    return-void
.end method
