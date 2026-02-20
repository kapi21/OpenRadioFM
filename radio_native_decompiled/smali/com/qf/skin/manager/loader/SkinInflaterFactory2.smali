.class public Lcom/qf/skin/manager/loader/SkinInflaterFactory2;
.super Ljava/lang/Object;
.source "SkinInflaterFactory2.java"

# interfaces
.implements Landroid/view/LayoutInflater$Factory2;


# annotations
.annotation system Ldalvik/annotation/MemberClasses;
    value = {
        Lcom/qf/skin/manager/loader/SkinInflaterFactory2$RunMethod;
    }
.end annotation


# static fields
.field private static final CLASS_LIST:[Ljava/lang/String;


# instance fields
.field private mConstructorArgs:[Ljava/lang/Object;

.field private mSkinItemMap:Ljava/util/Map;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Ljava/util/Map<",
            "Landroid/view/View;",
            "Ljava/util/concurrent/CopyOnWriteArrayList<",
            "Lcom/qf/skin/manager/entity/SkinItem;",
            ">;>;"
        }
    .end annotation
.end field

.field private mSkinItems:Ljava/util/concurrent/CopyOnWriteArrayList;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Ljava/util/concurrent/CopyOnWriteArrayList<",
            "Lcom/qf/skin/manager/entity/SkinItem;",
            ">;"
        }
    .end annotation
.end field

.field private sConstructorMap:Ljava/util/Map;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Ljava/util/Map<",
            "Ljava/lang/String;",
            "Ljava/lang/reflect/Constructor<",
            "+",
            "Landroid/view/View;",
            ">;>;"
        }
    .end annotation
.end field

.field private sConstructorSignature:[Ljava/lang/Class;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "[",
            "Ljava/lang/Class<",
            "*>;"
        }
    .end annotation
.end field


# direct methods
.method static constructor <clinit>()V
    .locals 3

    const-string v0, "android.widget."

    const-string v1, "android.view."

    const-string v2, "android.webkit."

    .line 29
    filled-new-array {v0, v1, v2}, [Ljava/lang/String;

    move-result-object v0

    sput-object v0, Lcom/qf/skin/manager/loader/SkinInflaterFactory2;->CLASS_LIST:[Ljava/lang/String;

    return-void
.end method

.method public constructor <init>()V
    .locals 3

    .line 43
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    const/4 v0, 0x2

    new-array v1, v0, [Ljava/lang/Object;

    .line 35
    iput-object v1, p0, Lcom/qf/skin/manager/loader/SkinInflaterFactory2;->mConstructorArgs:[Ljava/lang/Object;

    .line 36
    new-instance v1, Ljava/util/HashMap;

    invoke-direct {v1}, Ljava/util/HashMap;-><init>()V

    iput-object v1, p0, Lcom/qf/skin/manager/loader/SkinInflaterFactory2;->sConstructorMap:Ljava/util/Map;

    new-array v0, v0, [Ljava/lang/Class;

    .line 38
    const-class v1, Landroid/content/Context;

    const/4 v2, 0x0

    aput-object v1, v0, v2

    const-class v1, Landroid/util/AttributeSet;

    const/4 v2, 0x1

    aput-object v1, v0, v2

    iput-object v0, p0, Lcom/qf/skin/manager/loader/SkinInflaterFactory2;->sConstructorSignature:[Ljava/lang/Class;

    .line 40
    new-instance v0, Ljava/util/HashMap;

    invoke-direct {v0}, Ljava/util/HashMap;-><init>()V

    iput-object v0, p0, Lcom/qf/skin/manager/loader/SkinInflaterFactory2;->mSkinItemMap:Ljava/util/Map;

    .line 41
    new-instance v0, Ljava/util/concurrent/CopyOnWriteArrayList;

    invoke-direct {v0}, Ljava/util/concurrent/CopyOnWriteArrayList;-><init>()V

    iput-object v0, p0, Lcom/qf/skin/manager/loader/SkinInflaterFactory2;->mSkinItems:Ljava/util/concurrent/CopyOnWriteArrayList;

    return-void
.end method

.method private createView(Landroid/content/Context;Ljava/lang/String;Ljava/lang/String;)Landroid/view/View;
    .locals 1
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Ljava/lang/ClassNotFoundException;,
            Landroid/view/InflateException;
        }
    .end annotation

    .line 124
    iget-object v0, p0, Lcom/qf/skin/manager/loader/SkinInflaterFactory2;->sConstructorMap:Ljava/util/Map;

    invoke-interface {v0, p2}, Ljava/util/Map;->get(Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v0

    check-cast v0, Ljava/lang/reflect/Constructor;

    if-nez v0, :cond_1

    .line 128
    :try_start_0
    invoke-virtual {p1}, Landroid/content/Context;->getClassLoader()Ljava/lang/ClassLoader;

    move-result-object p1

    if-eqz p3, :cond_0

    new-instance v0, Ljava/lang/StringBuilder;

    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    invoke-virtual {v0, p3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v0, p2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p3

    goto :goto_0

    :cond_0
    move-object p3, p2

    :goto_0
    invoke-virtual {p1, p3}, Ljava/lang/ClassLoader;->loadClass(Ljava/lang/String;)Ljava/lang/Class;

    move-result-object p1

    const-class p3, Landroid/view/View;

    .line 129
    invoke-virtual {p1, p3}, Ljava/lang/Class;->asSubclass(Ljava/lang/Class;)Ljava/lang/Class;

    move-result-object p1

    .line 131
    iget-object p3, p0, Lcom/qf/skin/manager/loader/SkinInflaterFactory2;->sConstructorSignature:[Ljava/lang/Class;

    invoke-virtual {p1, p3}, Ljava/lang/Class;->getConstructor([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;

    move-result-object v0

    .line 132
    iget-object p1, p0, Lcom/qf/skin/manager/loader/SkinInflaterFactory2;->sConstructorMap:Ljava/util/Map;

    invoke-interface {p1, p2, v0}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

    :cond_1
    const/4 p1, 0x1

    .line 134
    invoke-virtual {v0, p1}, Ljava/lang/reflect/Constructor;->setAccessible(Z)V

    .line 135
    iget-object p1, p0, Lcom/qf/skin/manager/loader/SkinInflaterFactory2;->mConstructorArgs:[Ljava/lang/Object;

    invoke-virtual {v0, p1}, Ljava/lang/reflect/Constructor;->newInstance([Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object p1

    check-cast p1, Landroid/view/View;
    :try_end_0
    .catch Ljava/lang/Exception; {:try_start_0 .. :try_end_0} :catch_0

    return-object p1

    :catch_0
    const/4 p1, 0x0

    return-object p1
.end method

.method private createViewFromTag(Landroid/content/Context;Ljava/lang/String;Landroid/util/AttributeSet;)Landroid/view/View;
    .locals 4

    const-string v0, "view"

    .line 94
    invoke-virtual {p2, v0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v0

    const/4 v1, 0x0

    if-eqz v0, :cond_0

    const-string p2, "class"

    .line 95
    invoke-interface {p3, v1, p2}, Landroid/util/AttributeSet;->getAttributeValue(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;

    move-result-object p2

    :cond_0
    const/4 v0, 0x1

    const/4 v2, 0x0

    .line 98
    :try_start_0
    iget-object v3, p0, Lcom/qf/skin/manager/loader/SkinInflaterFactory2;->mConstructorArgs:[Ljava/lang/Object;

    aput-object p1, v3, v2

    .line 99
    iget-object v3, p0, Lcom/qf/skin/manager/loader/SkinInflaterFactory2;->mConstructorArgs:[Ljava/lang/Object;

    aput-object p3, v3, v0

    const/4 p3, -0x1

    const/16 v3, 0x2e

    .line 100
    invoke-virtual {p2, v3}, Ljava/lang/String;->indexOf(I)I

    move-result v3

    if-ne p3, v3, :cond_3

    move p3, v2

    .line 101
    :goto_0
    sget-object v3, Lcom/qf/skin/manager/loader/SkinInflaterFactory2;->CLASS_LIST:[Ljava/lang/String;

    array-length v3, v3

    if-ge p3, v3, :cond_2

    .line 102
    sget-object v3, Lcom/qf/skin/manager/loader/SkinInflaterFactory2;->CLASS_LIST:[Ljava/lang/String;

    aget-object v3, v3, p3

    invoke-direct {p0, p1, p2, v3}, Lcom/qf/skin/manager/loader/SkinInflaterFactory2;->createView(Landroid/content/Context;Ljava/lang/String;Ljava/lang/String;)Landroid/view/View;

    move-result-object v3
    :try_end_0
    .catch Ljava/lang/Exception; {:try_start_0 .. :try_end_0} :catch_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    if-eqz v3, :cond_1

    .line 117
    iget-object p1, p0, Lcom/qf/skin/manager/loader/SkinInflaterFactory2;->mConstructorArgs:[Ljava/lang/Object;

    aput-object v1, p1, v2

    .line 118
    aput-object v1, p1, v0

    return-object v3

    :cond_1
    add-int/lit8 p3, p3, 0x1

    goto :goto_0

    .line 117
    :cond_2
    iget-object p1, p0, Lcom/qf/skin/manager/loader/SkinInflaterFactory2;->mConstructorArgs:[Ljava/lang/Object;

    aput-object v1, p1, v2

    .line 118
    aput-object v1, p1, v0

    return-object v1

    .line 109
    :cond_3
    :try_start_1
    invoke-direct {p0, p1, p2, v1}, Lcom/qf/skin/manager/loader/SkinInflaterFactory2;->createView(Landroid/content/Context;Ljava/lang/String;Ljava/lang/String;)Landroid/view/View;

    move-result-object p1
    :try_end_1
    .catch Ljava/lang/Exception; {:try_start_1 .. :try_end_1} :catch_0
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    .line 117
    iget-object p2, p0, Lcom/qf/skin/manager/loader/SkinInflaterFactory2;->mConstructorArgs:[Ljava/lang/Object;

    aput-object v1, p2, v2

    .line 118
    aput-object v1, p2, v0

    return-object p1

    :catchall_0
    move-exception p1

    .line 117
    iget-object p2, p0, Lcom/qf/skin/manager/loader/SkinInflaterFactory2;->mConstructorArgs:[Ljava/lang/Object;

    aput-object v1, p2, v2

    .line 118
    aput-object v1, p2, v0

    .line 119
    throw p1

    .line 117
    :catch_0
    iget-object p1, p0, Lcom/qf/skin/manager/loader/SkinInflaterFactory2;->mConstructorArgs:[Ljava/lang/Object;

    aput-object v1, p1, v2

    .line 118
    aput-object v1, p1, v0

    return-object v1
.end method

.method private noHave(Landroid/view/View;Ljava/util/concurrent/CopyOnWriteArrayList;)Z
    .locals 1
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Landroid/view/View;",
            "Ljava/util/concurrent/CopyOnWriteArrayList<",
            "Lcom/qf/skin/manager/entity/SkinItem;",
            ">;)Z"
        }
    .end annotation

    .line 212
    invoke-virtual {p2}, Ljava/util/concurrent/CopyOnWriteArrayList;->iterator()Ljava/util/Iterator;

    move-result-object p2

    :cond_0
    invoke-interface {p2}, Ljava/util/Iterator;->hasNext()Z

    move-result v0

    if-eqz v0, :cond_1

    invoke-interface {p2}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v0

    check-cast v0, Lcom/qf/skin/manager/entity/SkinItem;

    .line 213
    iget-object v0, v0, Lcom/qf/skin/manager/entity/SkinItem;->view:Landroid/view/View;

    invoke-virtual {v0, p1}, Ljava/lang/Object;->equals(Ljava/lang/Object;)Z

    move-result v0

    if-eqz v0, :cond_0

    .line 214
    new-instance p2, Ljava/lang/StringBuilder;

    invoke-direct {p2}, Ljava/lang/StringBuilder;-><init>()V

    const-string v0, "parseSkinAttr noHave:"

    invoke-virtual {p2, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {p1}, Landroid/view/View;->getId()I

    move-result p1

    invoke-virtual {p2, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {p2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p1

    invoke-static {p1}, Lcom/qf/skin/manager/util/L;->i(Ljava/lang/String;)V

    const/4 p1, 0x0

    return p1

    :cond_1
    const/4 p1, 0x1

    return p1
.end method

.method private parseSkinAttr(Landroid/content/Context;Landroid/util/AttributeSet;Landroid/view/View;)V
    .locals 11

    .line 144
    new-instance v0, Ljava/util/ArrayList;

    invoke-direct {v0}, Ljava/util/ArrayList;-><init>()V

    .line 145
    invoke-virtual {p3}, Landroid/view/View;->getId()I

    move-result v1

    const/4 v2, -0x1

    if-eq v1, v2, :cond_0

    .line 148
    :try_start_0
    new-instance v3, Ljava/lang/StringBuilder;

    invoke-direct {v3}, Ljava/lang/StringBuilder;-><init>()V

    const-string v4, "parseSkinAttr viewIdName:"

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {p1}, Landroid/content/Context;->getResources()Landroid/content/res/Resources;

    move-result-object v4

    invoke-virtual {v4, v1}, Landroid/content/res/Resources;->getResourceName(I)Ljava/lang/String;

    move-result-object v4

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v3}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v3

    invoke-static {v3}, Lcom/qf/skin/manager/util/L;->i(Ljava/lang/String;)V
    :try_end_0
    .catch Landroid/content/res/Resources$NotFoundException; {:try_start_0 .. :try_end_0} :catch_0

    goto :goto_0

    .line 150
    :catch_0
    new-instance v3, Ljava/lang/StringBuilder;

    invoke-direct {v3}, Ljava/lang/StringBuilder;-><init>()V

    const-string v4, "parseSkinAttr Unable to find resource ID:"

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v3, v1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v3}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v1}, Lcom/qf/skin/manager/util/L;->i(Ljava/lang/String;)V

    :cond_0
    :goto_0
    const/4 v1, 0x0

    move v3, v1

    .line 153
    :goto_1
    invoke-interface {p2}, Landroid/util/AttributeSet;->getAttributeCount()I

    move-result v4

    if-ge v3, v4, :cond_8

    .line 154
    invoke-interface {p2, v3}, Landroid/util/AttributeSet;->getAttributeName(I)Ljava/lang/String;

    move-result-object v6

    .line 155
    invoke-interface {p2, v3}, Landroid/util/AttributeSet;->getAttributeValue(I)Ljava/lang/String;

    move-result-object v10

    .line 156
    invoke-static {v6}, Lcom/qf/skin/manager/entity/AttrFactory;->isSupportedAttr(Ljava/lang/String;)Z

    move-result v4

    if-nez v4, :cond_1

    goto/16 :goto_5

    .line 159
    :cond_1
    new-instance v4, Ljava/lang/StringBuilder;

    invoke-direct {v4}, Ljava/lang/StringBuilder;-><init>()V

    const-string v5, "attrName:"

    invoke-virtual {v4, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v4, v6}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    const-string v5, " attrValue:"

    invoke-virtual {v4, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v4, v10}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v4}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v4

    invoke-static {v4}, Lcom/qf/skin/manager/util/L;->d(Ljava/lang/String;)V

    const-string v4, "@"

    .line 160
    invoke-virtual {v10, v4}, Ljava/lang/String;->startsWith(Ljava/lang/String;)Z

    move-result v4

    if-eqz v4, :cond_7

    const-string v4, "style"

    .line 162
    invoke-virtual {v4, v6}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v4

    if-eqz v4, :cond_2

    const/4 v7, -0x1

    const/4 v8, 0x0

    const/4 v9, 0x0

    move-object v5, p1

    .line 163
    invoke-static/range {v5 .. v10}, Lcom/qf/skin/manager/entity/AttrFactory;->get(Landroid/content/Context;Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;

    move-result-object v4

    goto :goto_4

    :cond_2
    const/4 v4, 0x1

    .line 167
    :try_start_1
    invoke-virtual {v10, v4}, Ljava/lang/String;->substring(I)Ljava/lang/String;

    move-result-object v5

    invoke-static {v5}, Ljava/lang/Integer;->parseInt(Ljava/lang/String;)I

    move-result v4
    :try_end_1
    .catch Ljava/lang/Exception; {:try_start_1 .. :try_end_1} :catch_1

    :goto_2
    move v7, v4

    goto :goto_3

    .line 170
    :catch_1
    invoke-virtual {v10, v4}, Ljava/lang/String;->substring(I)Ljava/lang/String;

    move-result-object v5

    const-string v7, "/"

    .line 171
    invoke-virtual {v5, v7}, Ljava/lang/String;->split(Ljava/lang/String;)[Ljava/lang/String;

    move-result-object v5

    .line 172
    array-length v7, v5

    const/4 v8, 0x2

    if-eq v7, v8, :cond_3

    goto :goto_5

    .line 175
    :cond_3
    invoke-virtual {p1}, Landroid/content/Context;->getResources()Landroid/content/res/Resources;

    move-result-object v7

    aget-object v4, v5, v4

    aget-object v5, v5, v1

    invoke-virtual {p1}, Landroid/content/Context;->getPackageName()Ljava/lang/String;

    move-result-object v8

    invoke-virtual {v7, v4, v5, v8}, Landroid/content/res/Resources;->getIdentifier(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)I

    move-result v4

    goto :goto_2

    :goto_3
    if-eq v7, v2, :cond_7

    if-nez v7, :cond_4

    goto :goto_5

    .line 180
    :cond_4
    invoke-virtual {p1}, Landroid/content/Context;->getResources()Landroid/content/res/Resources;

    move-result-object v4

    invoke-virtual {v4, v7}, Landroid/content/res/Resources;->getResourceEntryName(I)Ljava/lang/String;

    move-result-object v8

    .line 181
    invoke-virtual {p1}, Landroid/content/Context;->getResources()Landroid/content/res/Resources;

    move-result-object v4

    invoke-virtual {v4, v7}, Landroid/content/res/Resources;->getResourceTypeName(I)Ljava/lang/String;

    move-result-object v9

    move-object v5, p1

    .line 182
    invoke-static/range {v5 .. v10}, Lcom/qf/skin/manager/entity/AttrFactory;->get(Landroid/content/Context;Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;

    move-result-object v4

    :goto_4
    if-nez v4, :cond_5

    goto :goto_5

    .line 188
    :cond_5
    instance-of v5, v4, Lcom/qf/skin/manager/entity/SkinAttr;

    if-eqz v5, :cond_6

    .line 189
    check-cast v4, Lcom/qf/skin/manager/entity/SkinAttr;

    .line 190
    invoke-interface {v0, v4}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    goto :goto_5

    .line 191
    :cond_6
    instance-of v5, v4, Lcom/qf/skin/manager/entity/StyleAttr;

    if-eqz v5, :cond_7

    .line 192
    check-cast v4, Lcom/qf/skin/manager/entity/StyleAttr;

    .line 193
    invoke-virtual {v4}, Lcom/qf/skin/manager/entity/StyleAttr;->getSkinAttr()Ljava/util/List;

    move-result-object v4

    invoke-interface {v0, v4}, Ljava/util/List;->addAll(Ljava/util/Collection;)Z

    :cond_7
    :goto_5
    add-int/lit8 v3, v3, 0x1

    goto/16 :goto_1

    .line 199
    :cond_8
    new-instance p1, Lcom/qf/skin/manager/entity/SkinItem;

    invoke-direct {p1}, Lcom/qf/skin/manager/entity/SkinItem;-><init>()V

    .line 200
    iput-object p3, p1, Lcom/qf/skin/manager/entity/SkinItem;->view:Landroid/view/View;

    .line 201
    iput-object v0, p1, Lcom/qf/skin/manager/entity/SkinItem;->attrs:Ljava/util/List;

    .line 203
    invoke-virtual {p1}, Lcom/qf/skin/manager/entity/SkinItem;->apply()V

    .line 204
    iget-object p2, p0, Lcom/qf/skin/manager/loader/SkinInflaterFactory2;->mSkinItems:Ljava/util/concurrent/CopyOnWriteArrayList;

    invoke-direct {p0, p3, p2}, Lcom/qf/skin/manager/loader/SkinInflaterFactory2;->noHave(Landroid/view/View;Ljava/util/concurrent/CopyOnWriteArrayList;)Z

    move-result p2

    if-eqz p2, :cond_9

    .line 205
    iget-object p2, p0, Lcom/qf/skin/manager/loader/SkinInflaterFactory2;->mSkinItems:Ljava/util/concurrent/CopyOnWriteArrayList;

    invoke-virtual {p2, p1}, Ljava/util/concurrent/CopyOnWriteArrayList;->add(Ljava/lang/Object;)Z

    .line 207
    :cond_9
    new-instance p2, Ljava/lang/StringBuilder;

    invoke-direct {p2}, Ljava/lang/StringBuilder;-><init>()V

    const-string p3, "parseSkinAttr add:"

    invoke-virtual {p2, p3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {p1}, Lcom/qf/skin/manager/entity/SkinItem;->toString()Ljava/lang/String;

    move-result-object p1

    invoke-virtual {p2, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    const-string p1, "  mSkinItems size:"

    invoke-virtual {p2, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-object p1, p0, Lcom/qf/skin/manager/loader/SkinInflaterFactory2;->mSkinItems:Ljava/util/concurrent/CopyOnWriteArrayList;

    invoke-virtual {p1}, Ljava/util/concurrent/CopyOnWriteArrayList;->size()I

    move-result p1

    invoke-virtual {p2, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {p2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p1

    invoke-static {p1}, Lcom/qf/skin/manager/util/L;->i(Ljava/lang/String;)V

    return-void
.end method


# virtual methods
.method public applySkin()V
    .locals 2

    .line 245
    iget-object v0, p0, Lcom/qf/skin/manager/loader/SkinInflaterFactory2;->mSkinItemMap:Ljava/util/Map;

    new-instance v1, Lcom/qf/skin/manager/loader/SkinInflaterFactory2$2;

    invoke-direct {v1, p0}, Lcom/qf/skin/manager/loader/SkinInflaterFactory2$2;-><init>(Lcom/qf/skin/manager/loader/SkinInflaterFactory2;)V

    invoke-virtual {p0, v0, v1}, Lcom/qf/skin/manager/loader/SkinInflaterFactory2;->foreachTheMap(Ljava/util/Map;Lcom/qf/skin/manager/loader/SkinInflaterFactory2$RunMethod;)V

    return-void
.end method

.method public clean()V
    .locals 3

    .line 269
    iget-object v0, p0, Lcom/qf/skin/manager/loader/SkinInflaterFactory2;->mSkinItems:Ljava/util/concurrent/CopyOnWriteArrayList;

    if-nez v0, :cond_0

    return-void

    .line 272
    :cond_0
    invoke-virtual {v0}, Ljava/util/concurrent/CopyOnWriteArrayList;->iterator()Ljava/util/Iterator;

    move-result-object v0

    :goto_0
    invoke-interface {v0}, Ljava/util/Iterator;->hasNext()Z

    move-result v1

    if-eqz v1, :cond_2

    invoke-interface {v0}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v1

    check-cast v1, Lcom/qf/skin/manager/entity/SkinItem;

    .line 273
    iget-object v2, v1, Lcom/qf/skin/manager/entity/SkinItem;->view:Landroid/view/View;

    if-nez v2, :cond_1

    goto :goto_0

    .line 276
    :cond_1
    invoke-virtual {v1}, Lcom/qf/skin/manager/entity/SkinItem;->clean()V

    goto :goto_0

    :cond_2
    return-void
.end method

.method public clean(Landroid/view/View;)V
    .locals 4

    .line 281
    iget-object v0, p0, Lcom/qf/skin/manager/loader/SkinInflaterFactory2;->mSkinItems:Ljava/util/concurrent/CopyOnWriteArrayList;

    if-nez v0, :cond_0

    return-void

    :cond_0
    const/4 v1, 0x0

    .line 285
    invoke-virtual {v0}, Ljava/util/concurrent/CopyOnWriteArrayList;->iterator()Ljava/util/Iterator;

    move-result-object v0

    :cond_1
    invoke-interface {v0}, Ljava/util/Iterator;->hasNext()Z

    move-result v2

    if-eqz v2, :cond_2

    invoke-interface {v0}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v2

    check-cast v2, Lcom/qf/skin/manager/entity/SkinItem;

    .line 286
    iget-object v3, v2, Lcom/qf/skin/manager/entity/SkinItem;->view:Landroid/view/View;

    if-eqz v3, :cond_1

    iget-object v3, v2, Lcom/qf/skin/manager/entity/SkinItem;->view:Landroid/view/View;

    if-ne v3, p1, :cond_1

    .line 287
    invoke-virtual {v2}, Lcom/qf/skin/manager/entity/SkinItem;->clean()V

    move-object v1, v2

    .line 292
    :cond_2
    iget-object p1, p0, Lcom/qf/skin/manager/loader/SkinInflaterFactory2;->mSkinItems:Ljava/util/concurrent/CopyOnWriteArrayList;

    invoke-virtual {p1, v1}, Ljava/util/concurrent/CopyOnWriteArrayList;->remove(Ljava/lang/Object;)Z

    return-void
.end method

.method public completeInflate(Landroid/view/View;)V
    .locals 2

    .line 88
    iget-object v0, p0, Lcom/qf/skin/manager/loader/SkinInflaterFactory2;->mSkinItemMap:Ljava/util/Map;

    iget-object v1, p0, Lcom/qf/skin/manager/loader/SkinInflaterFactory2;->mSkinItems:Ljava/util/concurrent/CopyOnWriteArrayList;

    invoke-interface {v0, p1, v1}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

    const/4 p1, 0x0

    .line 89
    iput-object p1, p0, Lcom/qf/skin/manager/loader/SkinInflaterFactory2;->mSkinItems:Ljava/util/concurrent/CopyOnWriteArrayList;

    .line 90
    invoke-virtual {p0}, Lcom/qf/skin/manager/loader/SkinInflaterFactory2;->toString()Ljava/lang/String;

    move-result-object p1

    const-string v0, "BPBB"

    invoke-static {v0, p1}, Lcom/qf/skin/manager/util/L;->d(Ljava/lang/String;Ljava/lang/String;)V

    return-void
.end method

.method public foreachTheMap(Ljava/util/Map;Lcom/qf/skin/manager/loader/SkinInflaterFactory2$RunMethod;)V
    .locals 2

    .line 237
    invoke-interface {p1}, Ljava/util/Map;->entrySet()Ljava/util/Set;

    move-result-object p1

    invoke-interface {p1}, Ljava/util/Set;->iterator()Ljava/util/Iterator;

    move-result-object p1

    .line 238
    :goto_0
    invoke-interface {p1}, Ljava/util/Iterator;->hasNext()Z

    move-result v0

    if-eqz v0, :cond_0

    .line 239
    invoke-interface {p1}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v0

    check-cast v0, Ljava/util/Map$Entry;

    .line 240
    invoke-interface {v0}, Ljava/util/Map$Entry;->getKey()Ljava/lang/Object;

    move-result-object v1

    invoke-interface {v0}, Ljava/util/Map$Entry;->getValue()Ljava/lang/Object;

    move-result-object v0

    invoke-interface {p2, v1, v0}, Lcom/qf/skin/manager/loader/SkinInflaterFactory2$RunMethod;->run(Ljava/lang/Object;Ljava/lang/Object;)V

    goto :goto_0

    :cond_0
    return-void
.end method

.method public onCreateView(Landroid/view/View;Ljava/lang/String;Landroid/content/Context;Landroid/util/AttributeSet;)Landroid/view/View;
    .locals 0

    .line 49
    invoke-direct {p0, p3, p2, p4}, Lcom/qf/skin/manager/loader/SkinInflaterFactory2;->createViewFromTag(Landroid/content/Context;Ljava/lang/String;Landroid/util/AttributeSet;)Landroid/view/View;

    move-result-object p1

    if-nez p1, :cond_0

    const/4 p1, 0x0

    return-object p1

    .line 54
    :cond_0
    invoke-direct {p0, p3, p4, p1}, Lcom/qf/skin/manager/loader/SkinInflaterFactory2;->parseSkinAttr(Landroid/content/Context;Landroid/util/AttributeSet;Landroid/view/View;)V

    return-object p1
.end method

.method public onCreateView(Ljava/lang/String;Landroid/content/Context;Landroid/util/AttributeSet;)Landroid/view/View;
    .locals 0

    const/4 p1, 0x0

    return-object p1
.end method

.method public preInflate()V
    .locals 1

    .line 81
    new-instance v0, Ljava/util/concurrent/CopyOnWriteArrayList;

    invoke-direct {v0}, Ljava/util/concurrent/CopyOnWriteArrayList;-><init>()V

    iput-object v0, p0, Lcom/qf/skin/manager/loader/SkinInflaterFactory2;->mSkinItems:Ljava/util/concurrent/CopyOnWriteArrayList;

    return-void
.end method

.method public removeViewString(Landroid/view/View;)V
    .locals 2

    .line 64
    iget-object v0, p0, Lcom/qf/skin/manager/loader/SkinInflaterFactory2;->mSkinItemMap:Ljava/util/Map;

    invoke-interface {v0, p1}, Ljava/util/Map;->containsKey(Ljava/lang/Object;)Z

    move-result v0

    if-eqz v0, :cond_1

    .line 65
    iget-object v0, p0, Lcom/qf/skin/manager/loader/SkinInflaterFactory2;->mSkinItemMap:Ljava/util/Map;

    invoke-interface {v0, p1}, Ljava/util/Map;->remove(Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object p1

    check-cast p1, Ljava/util/concurrent/CopyOnWriteArrayList;

    .line 66
    invoke-virtual {p1}, Ljava/util/concurrent/CopyOnWriteArrayList;->iterator()Ljava/util/Iterator;

    move-result-object p1

    :goto_0
    invoke-interface {p1}, Ljava/util/Iterator;->hasNext()Z

    move-result v0

    if-eqz v0, :cond_1

    invoke-interface {p1}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v0

    check-cast v0, Lcom/qf/skin/manager/entity/SkinItem;

    .line 67
    iget-object v1, v0, Lcom/qf/skin/manager/entity/SkinItem;->view:Landroid/view/View;

    if-nez v1, :cond_0

    goto :goto_0

    .line 70
    :cond_0
    invoke-virtual {v0}, Lcom/qf/skin/manager/entity/SkinItem;->clean()V

    goto :goto_0

    .line 73
    :cond_1
    invoke-virtual {p0}, Lcom/qf/skin/manager/loader/SkinInflaterFactory2;->toString()Ljava/lang/String;

    move-result-object p1

    const-string v0, "BPBB"

    invoke-static {v0, p1}, Lcom/qf/skin/manager/util/L;->d(Ljava/lang/String;Ljava/lang/String;)V

    return-void
.end method

.method public toString()Ljava/lang/String;
    .locals 3

    .line 223
    new-instance v0, Ljava/lang/StringBuilder;

    const/16 v1, 0x80

    invoke-direct {v0, v1}, Ljava/lang/StringBuilder;-><init>(I)V

    .line 224
    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "mSkinItemMap size:"

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-object v2, p0, Lcom/qf/skin/manager/loader/SkinInflaterFactory2;->mSkinItemMap:Ljava/util/Map;

    invoke-interface {v2}, Ljava/util/Map;->size()I

    move-result v2

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 225
    iget-object v1, p0, Lcom/qf/skin/manager/loader/SkinInflaterFactory2;->mSkinItemMap:Ljava/util/Map;

    new-instance v2, Lcom/qf/skin/manager/loader/SkinInflaterFactory2$1;

    invoke-direct {v2, p0}, Lcom/qf/skin/manager/loader/SkinInflaterFactory2$1;-><init>(Lcom/qf/skin/manager/loader/SkinInflaterFactory2;)V

    invoke-virtual {p0, v1, v2}, Lcom/qf/skin/manager/loader/SkinInflaterFactory2;->foreachTheMap(Ljava/util/Map;Lcom/qf/skin/manager/loader/SkinInflaterFactory2$RunMethod;)V

    .line 232
    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    return-object v0
.end method
