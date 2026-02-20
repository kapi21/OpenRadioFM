.class public Lcom/qf/skin/manager/base/SkinAppCompatActivity;
.super Landroidx/appcompat/app/AppCompatActivity;
.source "SkinAppCompatActivity.java"

# interfaces
.implements Lcom/qf/skin/manager/interfaces/ISkinUpdate;


# instance fields
.field private lastUpdateUiMode:I

.field private skinInflaterFactory:Lcom/qf/skin/manager/loader/SkinInflaterFactory;


# direct methods
.method public constructor <init>()V
    .locals 1

    .line 24
    invoke-direct {p0}, Landroidx/appcompat/app/AppCompatActivity;-><init>()V

    const/4 v0, -0x1

    .line 38
    iput v0, p0, Lcom/qf/skin/manager/base/SkinAppCompatActivity;->lastUpdateUiMode:I

    return-void
.end method

.method private isOnlyUImodeChange(Landroid/content/res/Configuration;Landroid/content/res/Configuration;)Z
    .locals 5

    .line 116
    iget v0, p1, Landroid/content/res/Configuration;->smallestScreenWidthDp:I

    iget v1, p2, Landroid/content/res/Configuration;->smallestScreenWidthDp:I

    const/4 v2, 0x0

    const-string v3, "TTTT"

    if-eq v0, v1, :cond_0

    const-string v0, "step1 SkinActivity smallestScreenWidthDp not same"

    .line 118
    invoke-static {v3, v0}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    move v0, v2

    goto :goto_0

    :cond_0
    const/4 v0, 0x1

    .line 120
    :goto_0
    iget v1, p1, Landroid/content/res/Configuration;->screenWidthDp:I

    iget v4, p2, Landroid/content/res/Configuration;->screenWidthDp:I

    if-eq v1, v4, :cond_1

    .line 122
    new-instance v0, Ljava/lang/StringBuilder;

    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    const-string v1, "step2 SkinActivity screenWidthDp not same "

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget v1, p1, Landroid/content/res/Configuration;->screenWidthDp:I

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v1, ","

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget v1, p2, Landroid/content/res/Configuration;->screenWidthDp:I

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    invoke-static {v3, v0}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    move v0, v2

    .line 124
    :cond_1
    iget p1, p1, Landroid/content/res/Configuration;->screenHeightDp:I

    iget p2, p2, Landroid/content/res/Configuration;->screenHeightDp:I

    if-eq p1, p2, :cond_2

    const-string p1, "step3 SkinActivity screenHeightDp not same"

    .line 126
    invoke-static {v3, p1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    move v0, v2

    .line 128
    :cond_2
    new-instance p1, Ljava/lang/StringBuilder;

    invoke-direct {p1}, Ljava/lang/StringBuilder;-><init>()V

    const-string p2, "SkinActivity isOnlyUImodeChange ="

    invoke-virtual {p1, p2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {p1, v0}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    invoke-virtual {p1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p1

    invoke-static {v3, p1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    return v0
.end method


# virtual methods
.method public onConfigurationChanged(Landroid/content/res/Configuration;)V
    .locals 2

    .line 102
    invoke-virtual {p0}, Lcom/qf/skin/manager/base/SkinAppCompatActivity;->getResources()Landroid/content/res/Resources;

    move-result-object v0

    invoke-virtual {v0}, Landroid/content/res/Resources;->getConfiguration()Landroid/content/res/Configuration;

    move-result-object v0

    .line 103
    invoke-super {p0, p1}, Landroidx/appcompat/app/AppCompatActivity;->onConfigurationChanged(Landroid/content/res/Configuration;)V

    .line 104
    sget-boolean v1, Lcom/qf/skin/manager/config/SkinConfig;->useSkinLib:Z

    if-eqz v1, :cond_0

    .line 105
    invoke-direct {p0, p1, v0}, Lcom/qf/skin/manager/base/SkinAppCompatActivity;->isOnlyUImodeChange(Landroid/content/res/Configuration;Landroid/content/res/Configuration;)Z

    move-result v0

    if-eqz v0, :cond_0

    .line 106
    invoke-static {p0, p1}, Lcom/qf/skin/manager/hooks/ResourcesHooks;->updateResourcesConfig(Landroid/content/Context;Landroid/content/res/Configuration;)V

    .line 107
    invoke-static {p0}, Lcom/qf/skin/manager/hooks/ResourcesHooks;->releaseResourceCache(Landroid/content/Context;)V

    .line 109
    invoke-static {}, Lcom/qf/skin/manager/loader/SkinManager;->getInstance()Lcom/qf/skin/manager/loader/SkinManager;

    move-result-object p1

    invoke-virtual {p1}, Lcom/qf/skin/manager/loader/SkinManager;->notifySkinUpdate()V

    :cond_0
    return-void
.end method

.method protected onCreate(Landroid/os/Bundle;)V
    .locals 2

    .line 29
    sget-boolean v0, Lcom/qf/skin/manager/config/SkinConfig;->useSkinLib:Z

    if-eqz v0, :cond_0

    .line 30
    invoke-static {p0}, Lcom/qf/skin/manager/hooks/ResourcesHooks;->hookAppCompatActivityResources(Landroidx/appcompat/app/AppCompatActivity;)V

    .line 31
    new-instance v0, Lcom/qf/skin/manager/loader/SkinInflaterFactory;

    invoke-direct {v0, p0}, Lcom/qf/skin/manager/loader/SkinInflaterFactory;-><init>(Landroid/content/Context;)V

    iput-object v0, p0, Lcom/qf/skin/manager/base/SkinAppCompatActivity;->skinInflaterFactory:Lcom/qf/skin/manager/loader/SkinInflaterFactory;

    .line 32
    invoke-virtual {p0}, Lcom/qf/skin/manager/base/SkinAppCompatActivity;->getLayoutInflater()Landroid/view/LayoutInflater;

    move-result-object v0

    iget-object v1, p0, Lcom/qf/skin/manager/base/SkinAppCompatActivity;->skinInflaterFactory:Lcom/qf/skin/manager/loader/SkinInflaterFactory;

    invoke-static {v0, v1}, Lcom/qf/skin/manager/hooks/LayoutInflaterHelper;->hookLayoutInflaterFactory(Landroid/view/LayoutInflater;Landroid/view/LayoutInflater$Factory;)V

    .line 34
    :cond_0
    invoke-super {p0, p1}, Landroidx/appcompat/app/AppCompatActivity;->onCreate(Landroid/os/Bundle;)V

    return-void
.end method

.method protected onDestroy()V
    .locals 2

    .line 76
    invoke-super {p0}, Landroidx/appcompat/app/AppCompatActivity;->onDestroy()V

    .line 77
    sget-boolean v0, Lcom/qf/skin/manager/config/SkinConfig;->useSkinLib:Z

    if-eqz v0, :cond_0

    .line 78
    invoke-static {}, Lcom/qf/skin/manager/loader/SkinManager;->getInstance()Lcom/qf/skin/manager/loader/SkinManager;

    move-result-object v0

    invoke-virtual {v0, p0}, Lcom/qf/skin/manager/loader/SkinManager;->detach(Lcom/qf/skin/manager/interfaces/ISkinUpdate;)V

    .line 79
    iget-object v0, p0, Lcom/qf/skin/manager/base/SkinAppCompatActivity;->skinInflaterFactory:Lcom/qf/skin/manager/loader/SkinInflaterFactory;

    invoke-virtual {v0}, Lcom/qf/skin/manager/loader/SkinInflaterFactory;->clean()V

    const/4 v0, 0x0

    .line 80
    iput-object v0, p0, Lcom/qf/skin/manager/base/SkinAppCompatActivity;->skinInflaterFactory:Lcom/qf/skin/manager/loader/SkinInflaterFactory;

    .line 81
    new-instance v0, Ljava/lang/StringBuilder;

    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    const-string v1, "SkinAppCompatActivity onDestroy:"

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v0, p0}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    invoke-static {v0}, Lcom/qf/skin/manager/util/L;->d(Ljava/lang/String;)V

    :cond_0
    return-void
.end method

.method protected onResume()V
    .locals 6

    .line 41
    invoke-super {p0}, Landroidx/appcompat/app/AppCompatActivity;->onResume()V

    .line 42
    sget-boolean v0, Lcom/qf/skin/manager/config/SkinConfig;->useSkinLib:Z

    if-eqz v0, :cond_4

    .line 43
    invoke-static {}, Lcom/qf/skin/manager/loader/SkinManager;->getInstance()Lcom/qf/skin/manager/loader/SkinManager;

    move-result-object v0

    invoke-virtual {v0, p0}, Lcom/qf/skin/manager/loader/SkinManager;->attach(Lcom/qf/skin/manager/interfaces/ISkinUpdate;)V

    .line 44
    new-instance v0, Ljava/lang/StringBuilder;

    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    const-string v1, "SkinAppCompatActivity onResume:"

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v0, p0}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    invoke-static {v0}, Lcom/qf/skin/manager/util/L;->d(Ljava/lang/String;)V

    .line 45
    invoke-virtual {p0}, Lcom/qf/skin/manager/base/SkinAppCompatActivity;->getResources()Landroid/content/res/Resources;

    move-result-object v0

    invoke-virtual {v0}, Landroid/content/res/Resources;->getConfiguration()Landroid/content/res/Configuration;

    move-result-object v0

    .line 46
    const-class v1, Landroid/app/UiModeManager;

    invoke-virtual {p0, v1}, Lcom/qf/skin/manager/base/SkinAppCompatActivity;->getSystemService(Ljava/lang/Class;)Ljava/lang/Object;

    move-result-object v1

    check-cast v1, Landroid/app/UiModeManager;

    if-eqz v1, :cond_4

    .line 48
    invoke-virtual {v1}, Landroid/app/UiModeManager;->getNightMode()I

    move-result v1

    const/4 v2, 0x0

    const/4 v3, 0x1

    if-ne v1, v3, :cond_0

    move v1, v3

    goto :goto_0

    :cond_0
    move v1, v2

    .line 53
    :goto_0
    iget v4, v0, Landroid/content/res/Configuration;->uiMode:I

    const/16 v5, 0x20

    and-int/2addr v4, v5

    if-eq v4, v5, :cond_1

    move v2, v3

    :cond_1
    const-string v3, "TTTT"

    if-eq v1, v2, :cond_3

    const-string v2, "SkinActivity onResume daymode has changed!!"

    .line 55
    invoke-static {v3, v2}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    if-eqz v1, :cond_2

    .line 57
    iget v1, v0, Landroid/content/res/Configuration;->uiMode:I

    and-int/lit8 v1, v1, -0x21

    iput v1, v0, Landroid/content/res/Configuration;->uiMode:I

    .line 58
    iget v1, v0, Landroid/content/res/Configuration;->uiMode:I

    or-int/lit8 v1, v1, 0x10

    iput v1, v0, Landroid/content/res/Configuration;->uiMode:I

    goto :goto_1

    .line 60
    :cond_2
    iget v1, v0, Landroid/content/res/Configuration;->uiMode:I

    or-int/2addr v1, v5

    iput v1, v0, Landroid/content/res/Configuration;->uiMode:I

    .line 61
    iget v1, v0, Landroid/content/res/Configuration;->uiMode:I

    and-int/lit8 v1, v1, -0x11

    iput v1, v0, Landroid/content/res/Configuration;->uiMode:I

    .line 63
    :goto_1
    invoke-static {p0, v0}, Lcom/qf/skin/manager/hooks/ResourcesHooks;->updateResourcesConfig(Landroid/content/Context;Landroid/content/res/Configuration;)V

    .line 65
    :cond_3
    iget v1, p0, Lcom/qf/skin/manager/base/SkinAppCompatActivity;->lastUpdateUiMode:I

    const/4 v2, -0x1

    if-eq v1, v2, :cond_4

    iget v2, v0, Landroid/content/res/Configuration;->uiMode:I

    if-eq v1, v2, :cond_4

    .line 66
    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "SkinActivity lastUpdateUiMode="

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget v2, p0, Lcom/qf/skin/manager/base/SkinAppCompatActivity;->lastUpdateUiMode:I

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v2, " newConfig.uiMode="

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget v0, v0, Landroid/content/res/Configuration;->uiMode:I

    invoke-virtual {v1, v0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    invoke-static {v3, v0}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    .line 67
    invoke-static {}, Lcom/qf/skin/manager/loader/SkinManager;->getInstance()Lcom/qf/skin/manager/loader/SkinManager;

    move-result-object v0

    invoke-virtual {v0}, Lcom/qf/skin/manager/loader/SkinManager;->notifySkinUpdate()V

    :cond_4
    return-void
.end method

.method public onThemeUpdate(Z)V
    .locals 0

    if-nez p1, :cond_0

    .line 93
    sget-boolean p1, Lcom/qf/skin/manager/config/SkinConfig;->useSkinLib:Z

    if-eqz p1, :cond_0

    .line 94
    invoke-virtual {p0}, Lcom/qf/skin/manager/base/SkinAppCompatActivity;->getResources()Landroid/content/res/Resources;

    move-result-object p1

    invoke-virtual {p1}, Landroid/content/res/Resources;->getConfiguration()Landroid/content/res/Configuration;

    move-result-object p1

    .line 95
    iget p1, p1, Landroid/content/res/Configuration;->uiMode:I

    iput p1, p0, Lcom/qf/skin/manager/base/SkinAppCompatActivity;->lastUpdateUiMode:I

    .line 96
    iget-object p1, p0, Lcom/qf/skin/manager/base/SkinAppCompatActivity;->skinInflaterFactory:Lcom/qf/skin/manager/loader/SkinInflaterFactory;

    invoke-virtual {p1}, Lcom/qf/skin/manager/loader/SkinInflaterFactory;->applySkin()V

    :cond_0
    return-void
.end method

.method public final removeSkinView(Landroid/view/View;)V
    .locals 1

    .line 86
    sget-boolean v0, Lcom/qf/skin/manager/config/SkinConfig;->useSkinLib:Z

    if-eqz v0, :cond_0

    .line 87
    iget-object v0, p0, Lcom/qf/skin/manager/base/SkinAppCompatActivity;->skinInflaterFactory:Lcom/qf/skin/manager/loader/SkinInflaterFactory;

    invoke-virtual {v0, p1}, Lcom/qf/skin/manager/loader/SkinInflaterFactory;->clean(Landroid/view/View;)V

    :cond_0
    return-void
.end method
