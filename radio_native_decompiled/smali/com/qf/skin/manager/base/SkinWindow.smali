.class public Lcom/qf/skin/manager/base/SkinWindow;
.super Landroid/content/ContextWrapper;
.source "SkinWindow.java"

# interfaces
.implements Lcom/qf/skin/manager/interfaces/ISkinUpdate;


# instance fields
.field appContext:Landroid/content/Context;

.field baseContext:Landroid/content/Context;

.field private iSkinUpdate:Lcom/qf/skin/manager/interfaces/ISkinUpdate;

.field private mSkinLayoutInflater:Lcom/qf/skin/manager/loader/SkinLayoutInflater;

.field private skinInflaterFactory:Lcom/qf/skin/manager/loader/SkinInflaterFactory2;

.field private viewCount:I

.field private viewOnlyInflateOneTime:Z


# direct methods
.method public constructor <init>(Landroid/content/Context;Z)V
    .locals 2

    const/4 v0, 0x0

    .line 37
    invoke-direct {p0, v0}, Landroid/content/ContextWrapper;-><init>(Landroid/content/Context;)V

    const/4 v1, 0x0

    .line 30
    iput v1, p0, Lcom/qf/skin/manager/base/SkinWindow;->viewCount:I

    .line 31
    iput-object v0, p0, Lcom/qf/skin/manager/base/SkinWindow;->appContext:Landroid/content/Context;

    .line 32
    iput-object v0, p0, Lcom/qf/skin/manager/base/SkinWindow;->baseContext:Landroid/content/Context;

    .line 34
    iput-boolean v1, p0, Lcom/qf/skin/manager/base/SkinWindow;->viewOnlyInflateOneTime:Z

    .line 38
    iput-boolean p2, p0, Lcom/qf/skin/manager/base/SkinWindow;->viewOnlyInflateOneTime:Z

    .line 39
    invoke-virtual {p1}, Landroid/content/Context;->getApplicationContext()Landroid/content/Context;

    move-result-object p2

    iput-object p2, p0, Lcom/qf/skin/manager/base/SkinWindow;->appContext:Landroid/content/Context;

    .line 42
    :try_start_0
    invoke-virtual {p1}, Landroid/content/Context;->getPackageName()Ljava/lang/String;

    move-result-object p2

    const/4 v0, 0x1

    invoke-virtual {p1, p2, v0}, Landroid/content/Context;->createPackageContext(Ljava/lang/String;I)Landroid/content/Context;

    move-result-object p1

    iput-object p1, p0, Lcom/qf/skin/manager/base/SkinWindow;->baseContext:Landroid/content/Context;
    :try_end_0
    .catch Landroid/content/pm/PackageManager$NameNotFoundException; {:try_start_0 .. :try_end_0} :catch_0

    .line 46
    iget-object p1, p0, Lcom/qf/skin/manager/base/SkinWindow;->baseContext:Landroid/content/Context;

    invoke-virtual {p0, p1}, Lcom/qf/skin/manager/base/SkinWindow;->attachBaseContext(Landroid/content/Context;)V

    .line 48
    new-instance p1, Lcom/qf/skin/manager/loader/SkinInflaterFactory2;

    invoke-direct {p1}, Lcom/qf/skin/manager/loader/SkinInflaterFactory2;-><init>()V

    iput-object p1, p0, Lcom/qf/skin/manager/base/SkinWindow;->skinInflaterFactory:Lcom/qf/skin/manager/loader/SkinInflaterFactory2;

    .line 49
    new-instance p1, Lcom/qf/skin/manager/loader/SkinLayoutInflater;

    iget-object p2, p0, Lcom/qf/skin/manager/base/SkinWindow;->appContext:Landroid/content/Context;

    invoke-direct {p1, p2}, Lcom/qf/skin/manager/loader/SkinLayoutInflater;-><init>(Landroid/content/Context;)V

    iput-object p1, p0, Lcom/qf/skin/manager/base/SkinWindow;->mSkinLayoutInflater:Lcom/qf/skin/manager/loader/SkinLayoutInflater;

    .line 50
    iget-object p1, p0, Lcom/qf/skin/manager/base/SkinWindow;->mSkinLayoutInflater:Lcom/qf/skin/manager/loader/SkinLayoutInflater;

    iget-object p2, p0, Lcom/qf/skin/manager/base/SkinWindow;->skinInflaterFactory:Lcom/qf/skin/manager/loader/SkinInflaterFactory2;

    invoke-virtual {p1, p2}, Lcom/qf/skin/manager/loader/SkinLayoutInflater;->setFactory2(Landroid/view/LayoutInflater$Factory2;)V

    .line 52
    invoke-static {}, Lcom/qf/skin/manager/loader/SkinManager;->getInstance()Lcom/qf/skin/manager/loader/SkinManager;

    move-result-object p1

    invoke-virtual {p1, p0}, Lcom/qf/skin/manager/loader/SkinManager;->windowAttach(Lcom/qf/skin/manager/interfaces/ISkinUpdate;)V

    return-void

    :catch_0
    move-exception p1

    .line 44
    new-instance p2, Ljava/lang/RuntimeException;

    new-instance v0, Ljava/lang/StringBuilder;

    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    const-string v1, "skinWindow create fail:"

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v0, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p1

    invoke-direct {p2, p1}, Ljava/lang/RuntimeException;-><init>(Ljava/lang/String;)V

    throw p2
.end method

.method private addView_()V
    .locals 2

    .line 61
    iget v0, p0, Lcom/qf/skin/manager/base/SkinWindow;->viewCount:I

    const/4 v1, 0x1

    add-int/2addr v0, v1

    iput v0, p0, Lcom/qf/skin/manager/base/SkinWindow;->viewCount:I

    .line 62
    iget-boolean v0, p0, Lcom/qf/skin/manager/base/SkinWindow;->viewOnlyInflateOneTime:Z

    if-nez v0, :cond_0

    .line 63
    iget v0, p0, Lcom/qf/skin/manager/base/SkinWindow;->viewCount:I

    if-ne v0, v1, :cond_0

    .line 64
    invoke-static {}, Lcom/qf/skin/manager/loader/SkinManager;->getInstance()Lcom/qf/skin/manager/loader/SkinManager;

    move-result-object v0

    invoke-virtual {v0, p0}, Lcom/qf/skin/manager/loader/SkinManager;->windowAttach(Lcom/qf/skin/manager/interfaces/ISkinUpdate;)V

    :cond_0
    return-void
.end method

.method private changeRes()V
    .locals 9

    .line 115
    iget-object v0, p0, Lcom/qf/skin/manager/base/SkinWindow;->baseContext:Landroid/content/Context;

    invoke-virtual {v0}, Landroid/content/Context;->getResources()Landroid/content/res/Resources;

    move-result-object v0

    invoke-virtual {v0}, Landroid/content/res/Resources;->getConfiguration()Landroid/content/res/Configuration;

    move-result-object v0

    .line 116
    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "-----onThemeUpdate--------"

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    const-string v2, "TTT"

    invoke-static {v2, v1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    .line 117
    iget-object v1, p0, Lcom/qf/skin/manager/base/SkinWindow;->appContext:Landroid/content/Context;

    const-class v3, Landroid/app/UiModeManager;

    invoke-virtual {v1, v3}, Landroid/content/Context;->getSystemService(Ljava/lang/Class;)Ljava/lang/Object;

    move-result-object v1

    check-cast v1, Landroid/app/UiModeManager;

    .line 118
    invoke-virtual {v1}, Landroid/app/UiModeManager;->getNightMode()I

    move-result v1

    const/4 v3, 0x0

    const/4 v4, 0x1

    if-ne v1, v4, :cond_0

    move v1, v4

    goto :goto_0

    :cond_0
    move v1, v3

    .line 123
    :goto_0
    iget v5, v0, Landroid/content/res/Configuration;->uiMode:I

    const/16 v6, 0x20

    and-int/2addr v5, v6

    if-eq v5, v6, :cond_1

    move v5, v4

    goto :goto_1

    :cond_1
    move v5, v3

    .line 124
    :goto_1
    iget v7, v0, Landroid/content/res/Configuration;->uiMode:I

    const/16 v8, 0x10

    and-int/2addr v7, v8

    if-ne v7, v8, :cond_2

    move v3, v4

    :cond_2
    if-ne v5, v3, :cond_4

    if-eqz v1, :cond_3

    .line 127
    iget v1, v0, Landroid/content/res/Configuration;->uiMode:I

    and-int/lit8 v1, v1, -0x21

    iput v1, v0, Landroid/content/res/Configuration;->uiMode:I

    .line 128
    iget v1, v0, Landroid/content/res/Configuration;->uiMode:I

    or-int/2addr v1, v8

    iput v1, v0, Landroid/content/res/Configuration;->uiMode:I

    goto :goto_2

    .line 130
    :cond_3
    iget v1, v0, Landroid/content/res/Configuration;->uiMode:I

    or-int/2addr v1, v6

    iput v1, v0, Landroid/content/res/Configuration;->uiMode:I

    .line 131
    iget v1, v0, Landroid/content/res/Configuration;->uiMode:I

    and-int/lit8 v1, v1, -0x11

    iput v1, v0, Landroid/content/res/Configuration;->uiMode:I

    .line 133
    :goto_2
    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v3, "-----force update--------"

    invoke-virtual {v1, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v2, v1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    .line 135
    :cond_4
    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "-----onThemeUpdate---baseContext-"

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    const-string v2, "SkinWindow"

    invoke-static {v2, v1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    .line 136
    iget-object v1, p0, Lcom/qf/skin/manager/base/SkinWindow;->appContext:Landroid/content/Context;

    if-eqz v1, :cond_5

    .line 137
    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v3, "-----onThemeUpdate --appContext--"

    invoke-virtual {v1, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-object v3, p0, Lcom/qf/skin/manager/base/SkinWindow;->appContext:Landroid/content/Context;

    invoke-virtual {v3}, Landroid/content/Context;->getResources()Landroid/content/res/Resources;

    move-result-object v3

    invoke-virtual {v3}, Landroid/content/res/Resources;->getConfiguration()Landroid/content/res/Configuration;

    move-result-object v3

    invoke-virtual {v1, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v2, v1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    .line 138
    iget-object v1, p0, Lcom/qf/skin/manager/base/SkinWindow;->appContext:Landroid/content/Context;

    invoke-static {v1, v0}, Lcom/qf/skin/manager/hooks/ResourcesHooks;->updateResourcesConfig(Landroid/content/Context;Landroid/content/res/Configuration;)V

    :cond_5
    return-void
.end method

.method private removeView_(Landroid/view/View;)V
    .locals 1

    .line 70
    iget v0, p0, Lcom/qf/skin/manager/base/SkinWindow;->viewCount:I

    add-int/lit8 v0, v0, -0x1

    iput v0, p0, Lcom/qf/skin/manager/base/SkinWindow;->viewCount:I

    .line 71
    iget-boolean v0, p0, Lcom/qf/skin/manager/base/SkinWindow;->viewOnlyInflateOneTime:Z

    if-nez v0, :cond_0

    .line 72
    iget-object v0, p0, Lcom/qf/skin/manager/base/SkinWindow;->skinInflaterFactory:Lcom/qf/skin/manager/loader/SkinInflaterFactory2;

    invoke-virtual {v0, p1}, Lcom/qf/skin/manager/loader/SkinInflaterFactory2;->removeViewString(Landroid/view/View;)V

    .line 73
    iget p1, p0, Lcom/qf/skin/manager/base/SkinWindow;->viewCount:I

    if-nez p1, :cond_0

    .line 74
    invoke-static {}, Lcom/qf/skin/manager/loader/SkinManager;->getInstance()Lcom/qf/skin/manager/loader/SkinManager;

    move-result-object p1

    invoke-virtual {p1, p0}, Lcom/qf/skin/manager/loader/SkinManager;->windowdetach(Lcom/qf/skin/manager/interfaces/ISkinUpdate;)V

    :cond_0
    return-void
.end method


# virtual methods
.method public addView(Landroid/view/View;Landroid/view/WindowManager$LayoutParams;)V
    .locals 1

    const-string v0, "window"

    .line 80
    invoke-virtual {p0, v0}, Lcom/qf/skin/manager/base/SkinWindow;->getSystemService(Ljava/lang/String;)Ljava/lang/Object;

    move-result-object v0

    check-cast v0, Landroid/view/WindowManager;

    .line 81
    invoke-interface {v0, p1, p2}, Landroid/view/WindowManager;->addView(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V

    .line 82
    invoke-direct {p0}, Lcom/qf/skin/manager/base/SkinWindow;->addView_()V

    return-void
.end method

.method public getLayoutInflater()Landroid/view/LayoutInflater;
    .locals 1

    .line 56
    iget-object v0, p0, Lcom/qf/skin/manager/base/SkinWindow;->mSkinLayoutInflater:Lcom/qf/skin/manager/loader/SkinLayoutInflater;

    return-object v0
.end method

.method public mayBeChangeRotation()Z
    .locals 2

    .line 143
    iget-object v0, p0, Lcom/qf/skin/manager/base/SkinWindow;->baseContext:Landroid/content/Context;

    invoke-virtual {v0}, Landroid/content/Context;->getResources()Landroid/content/res/Resources;

    move-result-object v0

    invoke-virtual {v0}, Landroid/content/res/Resources;->getConfiguration()Landroid/content/res/Configuration;

    move-result-object v0

    .line 144
    iget-object v1, p0, Lcom/qf/skin/manager/base/SkinWindow;->appContext:Landroid/content/Context;

    invoke-virtual {v1}, Landroid/content/Context;->getResources()Landroid/content/res/Resources;

    move-result-object v1

    invoke-virtual {v1}, Landroid/content/res/Resources;->getConfiguration()Landroid/content/res/Configuration;

    move-result-object v1

    .line 145
    iget v0, v0, Landroid/content/res/Configuration;->orientation:I

    iget v1, v1, Landroid/content/res/Configuration;->orientation:I

    if-eq v0, v1, :cond_0

    .line 146
    invoke-direct {p0}, Lcom/qf/skin/manager/base/SkinWindow;->changeRes()V

    const/4 v0, 0x1

    return v0

    :cond_0
    const/4 v0, 0x0

    return v0
.end method

.method public onThemeUpdate(Z)V
    .locals 1

    .line 105
    invoke-direct {p0}, Lcom/qf/skin/manager/base/SkinWindow;->changeRes()V

    .line 107
    iget-object v0, p0, Lcom/qf/skin/manager/base/SkinWindow;->skinInflaterFactory:Lcom/qf/skin/manager/loader/SkinInflaterFactory2;

    invoke-virtual {v0}, Lcom/qf/skin/manager/loader/SkinInflaterFactory2;->applySkin()V

    .line 108
    iget-object v0, p0, Lcom/qf/skin/manager/base/SkinWindow;->iSkinUpdate:Lcom/qf/skin/manager/interfaces/ISkinUpdate;

    if-eqz v0, :cond_0

    .line 109
    invoke-interface {v0, p1}, Lcom/qf/skin/manager/interfaces/ISkinUpdate;->onThemeUpdate(Z)V

    :cond_0
    return-void
.end method

.method public removeView(Landroid/view/View;)V
    .locals 1

    const-string v0, "window"

    .line 86
    invoke-virtual {p0, v0}, Lcom/qf/skin/manager/base/SkinWindow;->getSystemService(Ljava/lang/String;)Ljava/lang/Object;

    move-result-object v0

    check-cast v0, Landroid/view/WindowManager;

    .line 87
    invoke-interface {v0, p1}, Landroid/view/WindowManager;->removeView(Landroid/view/View;)V

    .line 88
    invoke-direct {p0, p1}, Lcom/qf/skin/manager/base/SkinWindow;->removeView_(Landroid/view/View;)V

    return-void
.end method

.method public removeViewImmediate(Landroid/view/View;)V
    .locals 1

    const-string v0, "window"

    .line 92
    invoke-virtual {p0, v0}, Lcom/qf/skin/manager/base/SkinWindow;->getSystemService(Ljava/lang/String;)Ljava/lang/Object;

    move-result-object v0

    check-cast v0, Landroid/view/WindowManager;

    .line 93
    invoke-interface {v0, p1}, Landroid/view/WindowManager;->removeViewImmediate(Landroid/view/View;)V

    .line 94
    invoke-direct {p0, p1}, Lcom/qf/skin/manager/base/SkinWindow;->removeView_(Landroid/view/View;)V

    return-void
.end method

.method public setOnSkinUpdate(Lcom/qf/skin/manager/interfaces/ISkinUpdate;)V
    .locals 0

    .line 152
    iput-object p1, p0, Lcom/qf/skin/manager/base/SkinWindow;->iSkinUpdate:Lcom/qf/skin/manager/interfaces/ISkinUpdate;

    return-void
.end method

.method public updateViewLayout(Landroid/view/View;Landroid/view/WindowManager$LayoutParams;)V
    .locals 1

    const-string v0, "window"

    .line 98
    invoke-virtual {p0, v0}, Lcom/qf/skin/manager/base/SkinWindow;->getSystemService(Ljava/lang/String;)Ljava/lang/Object;

    move-result-object v0

    check-cast v0, Landroid/view/WindowManager;

    .line 99
    invoke-interface {v0, p1, p2}, Landroid/view/WindowManager;->updateViewLayout(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V

    return-void
.end method
