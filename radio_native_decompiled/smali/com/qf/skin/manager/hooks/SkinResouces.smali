.class public Lcom/qf/skin/manager/hooks/SkinResouces;
.super Landroid/content/res/Resources;
.source "SkinResouces.java"


# instance fields
.field private TAG:Ljava/lang/String;


# direct methods
.method public constructor <init>(Landroid/content/res/AssetManager;Landroid/util/DisplayMetrics;Landroid/content/res/Configuration;)V
    .locals 0

    .line 26
    invoke-direct {p0, p1, p2, p3}, Landroid/content/res/Resources;-><init>(Landroid/content/res/AssetManager;Landroid/util/DisplayMetrics;Landroid/content/res/Configuration;)V

    .line 22
    const-class p1, Lcom/qf/skin/manager/hooks/SkinResouces;

    invoke-virtual {p1}, Ljava/lang/Class;->getSimpleName()Ljava/lang/String;

    move-result-object p1

    iput-object p1, p0, Lcom/qf/skin/manager/hooks/SkinResouces;->TAG:Ljava/lang/String;

    return-void
.end method

.method private getNewResName(II)Ljava/lang/String;
    .locals 2

    .line 119
    invoke-virtual {p0, p1}, Lcom/qf/skin/manager/hooks/SkinResouces;->getResourceEntryName(I)Ljava/lang/String;

    move-result-object p1

    const-string v0, "_style"

    .line 120
    invoke-virtual {p1, v0}, Ljava/lang/String;->contains(Ljava/lang/CharSequence;)Z

    move-result v0

    if-eqz v0, :cond_0

    return-object p1

    .line 123
    :cond_0
    new-instance v0, Ljava/lang/StringBuilder;

    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    invoke-virtual {v0, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    const-string v1, "_style2"

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    const/4 v1, 0x2

    if-ne p2, v1, :cond_1

    .line 125
    new-instance p2, Ljava/lang/StringBuilder;

    invoke-direct {p2}, Ljava/lang/StringBuilder;-><init>()V

    invoke-virtual {p2, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    const-string p1, "_style3"

    invoke-virtual {p2, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {p2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    goto :goto_0

    :cond_1
    const/4 v1, 0x3

    if-ne p2, v1, :cond_2

    .line 127
    new-instance p2, Ljava/lang/StringBuilder;

    invoke-direct {p2}, Ljava/lang/StringBuilder;-><init>()V

    invoke-virtual {p2, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    const-string p1, "_style4"

    invoke-virtual {p2, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {p2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    :cond_2
    :goto_0
    return-object v0
.end method


# virtual methods
.method public getColor(I)I
    .locals 3
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Landroid/content/res/Resources$NotFoundException;
        }
    .end annotation

    const-string v0, "persist.sys.custom.theme"

    const/4 v1, 0x0

    .line 75
    invoke-static {v0, v1}, Landroid/os/SystemProperties;->getInt(Ljava/lang/String;I)I

    move-result v0

    if-eqz v0, :cond_0

    .line 77
    invoke-direct {p0, p1, v0}, Lcom/qf/skin/manager/hooks/SkinResouces;->getNewResName(II)Ljava/lang/String;

    move-result-object v0

    .line 78
    invoke-static {}, Lcom/qf/skin/manager/loader/SkinManager;->getInstance()Lcom/qf/skin/manager/loader/SkinManager;

    move-result-object v1

    invoke-virtual {v1}, Lcom/qf/skin/manager/loader/SkinManager;->getSkinPackageName()Ljava/lang/String;

    move-result-object v1

    const-string v2, "color"

    invoke-virtual {p0, v0, v2, v1}, Lcom/qf/skin/manager/hooks/SkinResouces;->getIdentifier(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)I

    move-result v0

    if-lez v0, :cond_0

    .line 81
    invoke-super {p0, v0}, Landroid/content/res/Resources;->getColor(I)I

    move-result p1

    return p1

    .line 84
    :cond_0
    invoke-super {p0, p1}, Landroid/content/res/Resources;->getColor(I)I

    move-result p1

    return p1
.end method

.method public getColor(ILandroid/content/res/Resources$Theme;)I
    .locals 3
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Landroid/content/res/Resources$NotFoundException;
        }
    .end annotation

    const-string v0, "persist.sys.custom.theme"

    const/4 v1, 0x0

    .line 61
    invoke-static {v0, v1}, Landroid/os/SystemProperties;->getInt(Ljava/lang/String;I)I

    move-result v0

    if-eqz v0, :cond_0

    .line 63
    invoke-direct {p0, p1, v0}, Lcom/qf/skin/manager/hooks/SkinResouces;->getNewResName(II)Ljava/lang/String;

    move-result-object v0

    .line 64
    invoke-static {}, Lcom/qf/skin/manager/loader/SkinManager;->getInstance()Lcom/qf/skin/manager/loader/SkinManager;

    move-result-object v1

    invoke-virtual {v1}, Lcom/qf/skin/manager/loader/SkinManager;->getSkinPackageName()Ljava/lang/String;

    move-result-object v1

    const-string v2, "color"

    invoke-virtual {p0, v0, v2, v1}, Lcom/qf/skin/manager/hooks/SkinResouces;->getIdentifier(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)I

    move-result v0

    if-lez v0, :cond_0

    .line 67
    invoke-super {p0, v0, p2}, Landroid/content/res/Resources;->getColor(ILandroid/content/res/Resources$Theme;)I

    move-result p1

    return p1

    .line 70
    :cond_0
    invoke-super {p0, p1, p2}, Landroid/content/res/Resources;->getColor(ILandroid/content/res/Resources$Theme;)I

    move-result p1

    return p1
.end method

.method public getColorStateList(I)Landroid/content/res/ColorStateList;
    .locals 3
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Landroid/content/res/Resources$NotFoundException;
        }
    .end annotation

    const-string v0, "persist.sys.custom.theme"

    const/4 v1, 0x0

    .line 90
    invoke-static {v0, v1}, Landroid/os/SystemProperties;->getInt(Ljava/lang/String;I)I

    move-result v0

    if-eqz v0, :cond_0

    .line 92
    invoke-direct {p0, p1, v0}, Lcom/qf/skin/manager/hooks/SkinResouces;->getNewResName(II)Ljava/lang/String;

    move-result-object v0

    .line 93
    invoke-static {}, Lcom/qf/skin/manager/loader/SkinManager;->getInstance()Lcom/qf/skin/manager/loader/SkinManager;

    move-result-object v1

    invoke-virtual {v1}, Lcom/qf/skin/manager/loader/SkinManager;->getSkinPackageName()Ljava/lang/String;

    move-result-object v1

    const-string v2, "color"

    invoke-virtual {p0, v0, v2, v1}, Lcom/qf/skin/manager/hooks/SkinResouces;->getIdentifier(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)I

    move-result v0

    if-lez v0, :cond_0

    .line 96
    invoke-super {p0, v0}, Landroid/content/res/Resources;->getColorStateList(I)Landroid/content/res/ColorStateList;

    move-result-object p1

    return-object p1

    .line 99
    :cond_0
    invoke-super {p0, p1}, Landroid/content/res/Resources;->getColorStateList(I)Landroid/content/res/ColorStateList;

    move-result-object p1

    return-object p1
.end method

.method public getColorStateList(ILandroid/content/res/Resources$Theme;)Landroid/content/res/ColorStateList;
    .locals 3
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Landroid/content/res/Resources$NotFoundException;
        }
    .end annotation

    const-string v0, "persist.sys.custom.theme"

    const/4 v1, 0x0

    .line 105
    invoke-static {v0, v1}, Landroid/os/SystemProperties;->getInt(Ljava/lang/String;I)I

    move-result v0

    if-eqz v0, :cond_0

    .line 107
    invoke-direct {p0, p1, v0}, Lcom/qf/skin/manager/hooks/SkinResouces;->getNewResName(II)Ljava/lang/String;

    move-result-object v0

    .line 108
    invoke-static {}, Lcom/qf/skin/manager/loader/SkinManager;->getInstance()Lcom/qf/skin/manager/loader/SkinManager;

    move-result-object v1

    invoke-virtual {v1}, Lcom/qf/skin/manager/loader/SkinManager;->getSkinPackageName()Ljava/lang/String;

    move-result-object v1

    const-string v2, "color"

    invoke-virtual {p0, v0, v2, v1}, Lcom/qf/skin/manager/hooks/SkinResouces;->getIdentifier(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)I

    move-result v0

    if-lez v0, :cond_0

    .line 111
    invoke-super {p0, v0, p2}, Landroid/content/res/Resources;->getColorStateList(ILandroid/content/res/Resources$Theme;)Landroid/content/res/ColorStateList;

    move-result-object p1

    return-object p1

    .line 114
    :cond_0
    invoke-super {p0, p1, p2}, Landroid/content/res/Resources;->getColorStateList(ILandroid/content/res/Resources$Theme;)Landroid/content/res/ColorStateList;

    move-result-object p1

    return-object p1
.end method

.method public getDrawable(I)Landroid/graphics/drawable/Drawable;
    .locals 3
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Landroid/content/res/Resources$NotFoundException;
        }
    .end annotation

    const-string v0, "persist.sys.custom.theme"

    const/4 v1, 0x0

    .line 32
    invoke-static {v0, v1}, Landroid/os/SystemProperties;->getInt(Ljava/lang/String;I)I

    move-result v0

    if-eqz v0, :cond_0

    .line 34
    invoke-direct {p0, p1, v0}, Lcom/qf/skin/manager/hooks/SkinResouces;->getNewResName(II)Ljava/lang/String;

    move-result-object v0

    .line 35
    invoke-static {}, Lcom/qf/skin/manager/loader/SkinManager;->getInstance()Lcom/qf/skin/manager/loader/SkinManager;

    move-result-object v1

    invoke-virtual {v1}, Lcom/qf/skin/manager/loader/SkinManager;->getSkinPackageName()Ljava/lang/String;

    move-result-object v1

    const-string v2, "drawable"

    invoke-virtual {p0, v0, v2, v1}, Lcom/qf/skin/manager/hooks/SkinResouces;->getIdentifier(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)I

    move-result v0

    if-lez v0, :cond_0

    .line 38
    invoke-super {p0, v0}, Landroid/content/res/Resources;->getDrawable(I)Landroid/graphics/drawable/Drawable;

    move-result-object p1

    return-object p1

    .line 42
    :cond_0
    invoke-super {p0, p1}, Landroid/content/res/Resources;->getDrawable(I)Landroid/graphics/drawable/Drawable;

    move-result-object p1

    return-object p1
.end method

.method public getDrawable(ILandroid/content/res/Resources$Theme;)Landroid/graphics/drawable/Drawable;
    .locals 3
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Landroid/content/res/Resources$NotFoundException;
        }
    .end annotation

    const-string v0, "persist.sys.custom.theme"

    const/4 v1, 0x0

    .line 47
    invoke-static {v0, v1}, Landroid/os/SystemProperties;->getInt(Ljava/lang/String;I)I

    move-result v0

    if-eqz v0, :cond_0

    .line 49
    invoke-direct {p0, p1, v0}, Lcom/qf/skin/manager/hooks/SkinResouces;->getNewResName(II)Ljava/lang/String;

    move-result-object v0

    .line 50
    invoke-static {}, Lcom/qf/skin/manager/loader/SkinManager;->getInstance()Lcom/qf/skin/manager/loader/SkinManager;

    move-result-object v1

    invoke-virtual {v1}, Lcom/qf/skin/manager/loader/SkinManager;->getSkinPackageName()Ljava/lang/String;

    move-result-object v1

    const-string v2, "drawable"

    invoke-virtual {p0, v0, v2, v1}, Lcom/qf/skin/manager/hooks/SkinResouces;->getIdentifier(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)I

    move-result v0

    if-lez v0, :cond_0

    .line 53
    invoke-super {p0, v0, p2}, Landroid/content/res/Resources;->getDrawable(ILandroid/content/res/Resources$Theme;)Landroid/graphics/drawable/Drawable;

    move-result-object p1

    return-object p1

    .line 56
    :cond_0
    invoke-super {p0, p1, p2}, Landroid/content/res/Resources;->getDrawable(ILandroid/content/res/Resources$Theme;)Landroid/graphics/drawable/Drawable;

    move-result-object p1

    return-object p1
.end method
