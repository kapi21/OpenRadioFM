.class public Lcom/qf/skin/manager/entity/StyleAttr;
.super Ljava/lang/Object;
.source "StyleAttr.java"


# instance fields
.field private backgroundAttr:Lcom/qf/skin/manager/entity/BackgroundAttr;

.field private textColorAttr:Lcom/qf/skin/manager/entity/TextColorAttr;


# direct methods
.method public constructor <init>()V
    .locals 1

    .line 22
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    const/4 v0, 0x0

    .line 15
    iput-object v0, p0, Lcom/qf/skin/manager/entity/StyleAttr;->backgroundAttr:Lcom/qf/skin/manager/entity/BackgroundAttr;

    .line 16
    iput-object v0, p0, Lcom/qf/skin/manager/entity/StyleAttr;->textColorAttr:Lcom/qf/skin/manager/entity/TextColorAttr;

    return-void
.end method


# virtual methods
.method public build(Landroid/content/Context;Ljava/lang/String;)Lcom/qf/skin/manager/entity/StyleAttr;
    .locals 8

    if-nez p2, :cond_0

    return-object p0

    :cond_0
    const/4 v0, 0x0

    .line 29
    iput-object v0, p0, Lcom/qf/skin/manager/entity/StyleAttr;->textColorAttr:Lcom/qf/skin/manager/entity/TextColorAttr;

    .line 30
    iput-object v0, p0, Lcom/qf/skin/manager/entity/StyleAttr;->backgroundAttr:Lcom/qf/skin/manager/entity/BackgroundAttr;

    .line 35
    sget v0, Landroid/os/Build$VERSION;->SDK_INT:I

    const/16 v1, 0x16

    const/4 v2, 0x1

    if-le v0, v1, :cond_1

    const-string v0, "/"

    .line 36
    invoke-virtual {p2, v0}, Ljava/lang/String;->indexOf(Ljava/lang/String;)I

    move-result v0

    add-int/2addr v0, v2

    invoke-virtual {p2, v0}, Ljava/lang/String;->substring(I)Ljava/lang/String;

    move-result-object v0

    .line 37
    invoke-virtual {p1}, Landroid/content/Context;->getResources()Landroid/content/res/Resources;

    move-result-object v1

    invoke-virtual {p1}, Landroid/content/Context;->getPackageName()Ljava/lang/String;

    move-result-object v3

    const-string v4, "style"

    invoke-virtual {v1, v0, v4, v3}, Landroid/content/res/Resources;->getIdentifier(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)I

    move-result v0

    goto :goto_0

    .line 39
    :cond_1
    invoke-virtual {p2, v2}, Ljava/lang/String;->substring(I)Ljava/lang/String;

    move-result-object v0

    invoke-static {v0}, Ljava/lang/Integer;->parseInt(Ljava/lang/String;)I

    move-result v0

    :goto_0
    const/4 v1, 0x2

    new-array v1, v1, [I

    .line 41
    fill-array-data v1, :array_0

    .line 42
    invoke-virtual {p1}, Landroid/content/Context;->getTheme()Landroid/content/res/Resources$Theme;

    move-result-object v3

    invoke-virtual {v3, v0, v1}, Landroid/content/res/Resources$Theme;->obtainStyledAttributes(I[I)Landroid/content/res/TypedArray;

    move-result-object v1

    const/4 v3, 0x0

    const/4 v6, -0x1

    .line 43
    invoke-virtual {v1, v3, v6}, Landroid/content/res/TypedArray;->getResourceId(II)I

    move-result v3

    .line 44
    invoke-virtual {v1, v2, v6}, Landroid/content/res/TypedArray;->getResourceId(II)I

    move-result v7

    .line 45
    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    invoke-virtual {v1, v0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v0, "  "

    invoke-virtual {v1, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    const-string v0, " "

    invoke-virtual {v1, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, v3}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v1, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, v7}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    const-string v1, "style build"

    invoke-static {v1, v0}, Lcom/qf/skin/manager/util/L;->i(Ljava/lang/String;Ljava/lang/String;)V

    if-eq v3, v6, :cond_2

    .line 47
    invoke-virtual {p1}, Landroid/content/Context;->getResources()Landroid/content/res/Resources;

    move-result-object v0

    invoke-virtual {v0, v3}, Landroid/content/res/Resources;->getResourceEntryName(I)Ljava/lang/String;

    move-result-object v4

    .line 48
    invoke-virtual {p1}, Landroid/content/Context;->getResources()Landroid/content/res/Resources;

    move-result-object v0

    invoke-virtual {v0, v3}, Landroid/content/res/Resources;->getResourceTypeName(I)Ljava/lang/String;

    move-result-object v5

    const-string v1, "textColor"

    move-object v0, p1

    move v2, v3

    move-object v3, v4

    move-object v4, v5

    move-object v5, p2

    .line 49
    invoke-static/range {v0 .. v5}, Lcom/qf/skin/manager/entity/AttrFactory;->get(Landroid/content/Context;Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;

    move-result-object v0

    if-eqz v0, :cond_2

    .line 52
    check-cast v0, Lcom/qf/skin/manager/entity/TextColorAttr;

    iput-object v0, p0, Lcom/qf/skin/manager/entity/StyleAttr;->textColorAttr:Lcom/qf/skin/manager/entity/TextColorAttr;

    :cond_2
    if-eq v7, v6, :cond_3

    .line 56
    invoke-virtual {p1}, Landroid/content/Context;->getResources()Landroid/content/res/Resources;

    move-result-object v0

    invoke-virtual {v0, v7}, Landroid/content/res/Resources;->getResourceEntryName(I)Ljava/lang/String;

    move-result-object v3

    .line 57
    invoke-virtual {p1}, Landroid/content/Context;->getResources()Landroid/content/res/Resources;

    move-result-object v0

    invoke-virtual {v0, v7}, Landroid/content/res/Resources;->getResourceTypeName(I)Ljava/lang/String;

    move-result-object v4

    const-string v1, "background"

    move-object v0, p1

    move v2, v7

    move-object v5, p2

    .line 58
    invoke-static/range {v0 .. v5}, Lcom/qf/skin/manager/entity/AttrFactory;->get(Landroid/content/Context;Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;

    move-result-object v0

    if-eqz v0, :cond_3

    .line 60
    check-cast v0, Lcom/qf/skin/manager/entity/BackgroundAttr;

    iput-object v0, p0, Lcom/qf/skin/manager/entity/StyleAttr;->backgroundAttr:Lcom/qf/skin/manager/entity/BackgroundAttr;

    :cond_3
    return-object p0

    nop

    :array_0
    .array-data 4
        0x1010098
        0x10100d4
    .end array-data
.end method

.method public getBackgroundAttr()Lcom/qf/skin/manager/entity/BackgroundAttr;
    .locals 1

    .line 19
    iget-object v0, p0, Lcom/qf/skin/manager/entity/StyleAttr;->backgroundAttr:Lcom/qf/skin/manager/entity/BackgroundAttr;

    return-object v0
.end method

.method public getSkinAttr()Ljava/util/List;
    .locals 2
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "()",
            "Ljava/util/List<",
            "Lcom/qf/skin/manager/entity/SkinAttr;",
            ">;"
        }
    .end annotation

    .line 71
    new-instance v0, Ljava/util/ArrayList;

    invoke-direct {v0}, Ljava/util/ArrayList;-><init>()V

    .line 72
    iget-object v1, p0, Lcom/qf/skin/manager/entity/StyleAttr;->backgroundAttr:Lcom/qf/skin/manager/entity/BackgroundAttr;

    if-eqz v1, :cond_0

    .line 73
    invoke-interface {v0, v1}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    .line 75
    :cond_0
    iget-object v1, p0, Lcom/qf/skin/manager/entity/StyleAttr;->textColorAttr:Lcom/qf/skin/manager/entity/TextColorAttr;

    if-eqz v1, :cond_1

    .line 76
    invoke-interface {v0, v1}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    :cond_1
    return-object v0
.end method

.method public getTextColorAttr()Lcom/qf/skin/manager/entity/TextColorAttr;
    .locals 1

    .line 67
    iget-object v0, p0, Lcom/qf/skin/manager/entity/StyleAttr;->textColorAttr:Lcom/qf/skin/manager/entity/TextColorAttr;

    return-object v0
.end method
