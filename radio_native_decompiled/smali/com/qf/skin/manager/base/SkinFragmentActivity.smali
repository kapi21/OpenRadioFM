.class public Lcom/qf/skin/manager/base/SkinFragmentActivity;
.super Landroidx/fragment/app/FragmentActivity;
.source "SkinFragmentActivity.java"

# interfaces
.implements Lcom/qf/skin/manager/interfaces/ISkinUpdate;


# static fields
.field private static final TAG:Ljava/lang/String;


# instance fields
.field private lastUpdateUiMode:I

.field private skinInflaterFactory:Lcom/qf/skin/manager/loader/SkinInflaterFactory;


# direct methods
.method static constructor <clinit>()V
    .locals 1

    .line 22
    const-class v0, Lcom/qf/skin/manager/base/SkinFragmentActivity;

    invoke-virtual {v0}, Ljava/lang/Class;->getSimpleName()Ljava/lang/String;

    move-result-object v0

    sput-object v0, Lcom/qf/skin/manager/base/SkinFragmentActivity;->TAG:Ljava/lang/String;

    return-void
.end method

.method public constructor <init>()V
    .locals 1

    .line 20
    invoke-direct {p0}, Landroidx/fragment/app/FragmentActivity;-><init>()V

    const/4 v0, -0x1

    .line 27
    iput v0, p0, Lcom/qf/skin/manager/base/SkinFragmentActivity;->lastUpdateUiMode:I

    return-void
.end method

.method private isOnlyUImodeChange(Landroid/content/res/Configuration;Landroid/content/res/Configuration;)Z
    .locals 5

    .line 117
    iget v0, p1, Landroid/content/res/Configuration;->smallestScreenWidthDp:I

    iget v1, p2, Landroid/content/res/Configuration;->smallestScreenWidthDp:I

    const/4 v2, 0x0

    const-string v3, "TTTT"

    if-eq v0, v1, :cond_0

    const-string v0, "step1 SkinActivity smallestScreenWidthDp not same"

    .line 119
    invoke-static {v3, v0}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    move v0, v2

    goto :goto_0

    :cond_0
    const/4 v0, 0x1

    .line 121
    :goto_0
    iget v1, p1, Landroid/content/res/Configuration;->screenWidthDp:I

    iget v4, p2, Landroid/content/res/Configuration;->screenWidthDp:I

    if-eq v1, v4, :cond_1

    .line 123
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

    .line 125
    :cond_1
    iget p1, p1, Landroid/content/res/Configuration;->screenHeightDp:I

    iget p2, p2, Landroid/content/res/Configuration;->screenHeightDp:I

    if-eq p1, p2, :cond_2

    const-string p1, "step3 SkinActivity screenHeightDp not same"

    .line 127
    invoke-static {v3, p1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    move v0, v2

    .line 129
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
    invoke-virtual {p0}, Lcom/qf/skin/manager/base/SkinFragmentActivity;->getResources()Landroid/content/res/Resources;

    move-result-object v0

    invoke-virtual {v0}, Landroid/content/res/Resources;->getConfiguration()Landroid/content/res/Configuration;

    move-result-object v0

    .line 103
    invoke-super {p0, p1}, Landroidx/fragment/app/FragmentActivity;->onConfigurationChanged(Landroid/content/res/Configuration;)V

    .line 104
    sget-boolean v1, Lcom/qf/skin/manager/config/SkinConfig;->useSkinLib:Z

    if-eqz v1, :cond_0

    .line 105
    invoke-direct {p0, p1, v0}, Lcom/qf/skin/manager/base/SkinFragmentActivity;->isOnlyUImodeChange(Landroid/content/res/Configuration;Landroid/content/res/Configuration;)Z

    move-result v0

    if-eqz v0, :cond_0

    .line 106
    invoke-static {p0, p1}, Lcom/qf/skin/manager/hooks/ResourcesHooks;->updateResourcesConfig(Landroid/content/Context;Landroid/content/res/Configuration;)V

    .line 107
    invoke-static {p0}, Lcom/qf/skin/manager/hooks/ResourcesHooks;->releaseResourceCache(Landroid/content/Context;)V

    .line 108
    invoke-static {}, Lcom/qf/skin/manager/loader/SkinManager;->getInstance()Lcom/qf/skin/manager/loader/SkinManager;

    move-result-object p1

    invoke-virtual {p1}, Lcom/qf/skin/manager/loader/SkinManager;->notifySkinUpdate()V

    .line 110
    sget-object p1, Lcom/qf/skin/manager/base/SkinFragmentActivity;->TAG:Ljava/lang/String;

    new-instance v0, Ljava/lang/StringBuilder;

    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    const-string v1, "SkinActivity onConfigurationChanged:"

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v0, p0}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    const-string v1, " newConfig="

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {p0}, Lcom/qf/skin/manager/base/SkinFragmentActivity;->getResources()Landroid/content/res/Resources;

    move-result-object v1

    invoke-virtual {v1}, Landroid/content/res/Resources;->getConfiguration()Landroid/content/res/Configuration;

    move-result-object v1

    invoke-virtual {v1}, Landroid/content/res/Configuration;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    invoke-static {p1, v0}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    :cond_0
    return-void
.end method

.method protected onCreate(Landroid/os/Bundle;)V
    .locals 2

    .line 31
    sget-boolean v0, Lcom/qf/skin/manager/config/SkinConfig;->useSkinLib:Z

    if-eqz v0, :cond_0

    .line 32
    invoke-static {p0}, Lcom/qf/skin/manager/hooks/ResourcesHooks;->hookActivityResources(Landroid/app/Activity;)V

    .line 33
    new-instance v0, Lcom/qf/skin/manager/loader/SkinInflaterFactory;

    invoke-direct {v0, p0}, Lcom/qf/skin/manager/loader/SkinInflaterFactory;-><init>(Landroid/content/Context;)V

    iput-object v0, p0, Lcom/qf/skin/manager/base/SkinFragmentActivity;->skinInflaterFactory:Lcom/qf/skin/manager/loader/SkinInflaterFactory;

    .line 34
    invoke-virtual {p0}, Lcom/qf/skin/manager/base/SkinFragmentActivity;->getLayoutInflater()Landroid/view/LayoutInflater;

    move-result-object v0

    iget-object v1, p0, Lcom/qf/skin/manager/base/SkinFragmentActivity;->skinInflaterFactory:Lcom/qf/skin/manager/loader/SkinInflaterFactory;

    invoke-static {v0, v1}, Lcom/qf/skin/manager/hooks/LayoutInflaterHelper;->hookLayoutInflaterFactory(Landroid/view/LayoutInflater;Landroid/view/LayoutInflater$Factory;)V

    .line 36
    :cond_0
    invoke-super {p0, p1}, Landroidx/fragment/app/FragmentActivity;->onCreate(Landroid/os/Bundle;)V

    return-void
.end method

.method protected onDestroy()V
    .locals 3

    .line 81
    invoke-super {p0}, Landroidx/fragment/app/FragmentActivity;->onDestroy()V

    .line 83
    sget-boolean v0, Lcom/qf/skin/manager/config/SkinConfig;->useSkinLib:Z

    if-eqz v0, :cond_0

    .line 84
    invoke-static {}, Lcom/qf/skin/manager/loader/SkinManager;->getInstance()Lcom/qf/skin/manager/loader/SkinManager;

    move-result-object v0

    invoke-virtual {v0, p0}, Lcom/qf/skin/manager/loader/SkinManager;->detach(Lcom/qf/skin/manager/interfaces/ISkinUpdate;)V

    .line 85
    iget-object v0, p0, Lcom/qf/skin/manager/base/SkinFragmentActivity;->skinInflaterFactory:Lcom/qf/skin/manager/loader/SkinInflaterFactory;

    invoke-virtual {v0}, Lcom/qf/skin/manager/loader/SkinInflaterFactory;->clean()V

    const/4 v0, 0x0

    .line 86
    iput-object v0, p0, Lcom/qf/skin/manager/base/SkinFragmentActivity;->skinInflaterFactory:Lcom/qf/skin/manager/loader/SkinInflaterFactory;

    .line 87
    sget-object v0, Lcom/qf/skin/manager/base/SkinFragmentActivity;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "SkinActivity onDestroy:"

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p0}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    :cond_0
    return-void
.end method

.method protected onResume()V
    .locals 9

    .line 41
    invoke-super {p0}, Landroidx/fragment/app/FragmentActivity;->onResume()V

    .line 43
    sget-boolean v0, Lcom/qf/skin/manager/config/SkinConfig;->useSkinLib:Z

    if-eqz v0, :cond_5

    .line 44
    invoke-static {}, Lcom/qf/skin/manager/loader/SkinManager;->getInstance()Lcom/qf/skin/manager/loader/SkinManager;

    move-result-object v0

    invoke-virtual {v0, p0}, Lcom/qf/skin/manager/loader/SkinManager;->attach(Lcom/qf/skin/manager/interfaces/ISkinUpdate;)V

    .line 45
    sget-object v0, Lcom/qf/skin/manager/base/SkinFragmentActivity;->TAG:Ljava/lang/String;

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "SkinActivity onResume:"

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p0}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    .line 46
    invoke-virtual {p0}, Lcom/qf/skin/manager/base/SkinFragmentActivity;->getResources()Landroid/content/res/Resources;

    move-result-object v0

    invoke-virtual {v0}, Landroid/content/res/Resources;->getConfiguration()Landroid/content/res/Configuration;

    move-result-object v0

    .line 47
    const-class v1, Landroid/app/UiModeManager;

    invoke-virtual {p0, v1}, Lcom/qf/skin/manager/base/SkinFragmentActivity;->getSystemService(Ljava/lang/Class;)Ljava/lang/Object;

    move-result-object v1

    check-cast v1, Landroid/app/UiModeManager;

    if-eqz v1, :cond_5

    .line 49
    invoke-virtual {v1}, Landroid/app/UiModeManager;->getNightMode()I

    move-result v1

    const/4 v2, 0x0

    const/4 v3, 0x1

    if-ne v1, v3, :cond_0

    move v1, v3

    goto :goto_0

    :cond_0
    move v1, v2

    .line 54
    :goto_0
    iget v4, v0, Landroid/content/res/Configuration;->uiMode:I

    const/16 v5, 0x20

    and-int/2addr v4, v5

    if-eq v4, v5, :cond_1

    move v4, v3

    goto :goto_1

    :cond_1
    move v4, v2

    .line 55
    :goto_1
    iget v6, v0, Landroid/content/res/Configuration;->uiMode:I

    const/16 v7, 0x10

    and-int/2addr v6, v7

    if-ne v6, v7, :cond_2

    move v2, v3

    .line 56
    :cond_2
    sget-object v3, Lcom/qf/skin/manager/base/SkinFragmentActivity;->TAG:Ljava/lang/String;

    new-instance v6, Ljava/lang/StringBuilder;

    invoke-direct {v6}, Ljava/lang/StringBuilder;-><init>()V

    const-string v8, "SkinActivity onResume nowDayMode:"

    invoke-virtual {v6, v8}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v6, v4}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    const-string v8, " nowDayModeSure="

    invoke-virtual {v6, v8}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v6, v2}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    invoke-virtual {v6}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v6

    invoke-static {v3, v6}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    if-ne v4, v2, :cond_4

    if-eq v1, v4, :cond_4

    .line 59
    sget-object v2, Lcom/qf/skin/manager/base/SkinFragmentActivity;->TAG:Ljava/lang/String;

    const-string v3, "SkinActivity onResume daymode has changed!!"

    invoke-static {v2, v3}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    if-eqz v1, :cond_3

    .line 61
    iget v1, v0, Landroid/content/res/Configuration;->uiMode:I

    and-int/lit8 v1, v1, -0x21

    iput v1, v0, Landroid/content/res/Configuration;->uiMode:I

    .line 62
    iget v1, v0, Landroid/content/res/Configuration;->uiMode:I

    or-int/2addr v1, v7

    iput v1, v0, Landroid/content/res/Configuration;->uiMode:I

    goto :goto_2

    .line 64
    :cond_3
    iget v1, v0, Landroid/content/res/Configuration;->uiMode:I

    or-int/2addr v1, v5

    iput v1, v0, Landroid/content/res/Configuration;->uiMode:I

    .line 65
    iget v1, v0, Landroid/content/res/Configuration;->uiMode:I

    and-int/lit8 v1, v1, -0x11

    iput v1, v0, Landroid/content/res/Configuration;->uiMode:I

    .line 67
    :goto_2
    invoke-static {p0, v0}, Lcom/qf/skin/manager/hooks/ResourcesHooks;->updateResourcesConfig(Landroid/content/Context;Landroid/content/res/Configuration;)V

    .line 71
    :cond_4
    iget v1, p0, Lcom/qf/skin/manager/base/SkinFragmentActivity;->lastUpdateUiMode:I

    const/4 v2, -0x1

    if-eq v1, v2, :cond_5

    iget v2, v0, Landroid/content/res/Configuration;->uiMode:I

    if-eq v1, v2, :cond_5

    .line 72
    sget-object v1, Lcom/qf/skin/manager/base/SkinFragmentActivity;->TAG:Ljava/lang/String;

    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2}, Ljava/lang/StringBuilder;-><init>()V

    const-string v3, "SkinActivity lastUpdateUiMode="

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget v3, p0, Lcom/qf/skin/manager/base/SkinFragmentActivity;->lastUpdateUiMode:I

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v3, " newConfig.uiMode="

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget v0, v0, Landroid/content/res/Configuration;->uiMode:I

    invoke-virtual {v2, v0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    invoke-static {v1, v0}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    .line 73
    invoke-static {}, Lcom/qf/skin/manager/loader/SkinManager;->getInstance()Lcom/qf/skin/manager/loader/SkinManager;

    move-result-object v0

    invoke-virtual {v0}, Lcom/qf/skin/manager/loader/SkinManager;->notifySkinUpdate()V

    :cond_5
    return-void
.end method

.method public onThemeUpdate(Z)V
    .locals 1

    .line 93
    sget-boolean v0, Lcom/qf/skin/manager/config/SkinConfig;->useSkinLib:Z

    if-eqz v0, :cond_0

    if-nez p1, :cond_0

    .line 94
    invoke-virtual {p0}, Lcom/qf/skin/manager/base/SkinFragmentActivity;->getResources()Landroid/content/res/Resources;

    move-result-object p1

    invoke-virtual {p1}, Landroid/content/res/Resources;->getConfiguration()Landroid/content/res/Configuration;

    move-result-object p1

    .line 95
    iget p1, p1, Landroid/content/res/Configuration;->uiMode:I

    iput p1, p0, Lcom/qf/skin/manager/base/SkinFragmentActivity;->lastUpdateUiMode:I

    .line 96
    iget-object p1, p0, Lcom/qf/skin/manager/base/SkinFragmentActivity;->skinInflaterFactory:Lcom/qf/skin/manager/loader/SkinInflaterFactory;

    invoke-virtual {p1}, Lcom/qf/skin/manager/loader/SkinInflaterFactory;->applySkin()V

    :cond_0
    return-void
.end method
