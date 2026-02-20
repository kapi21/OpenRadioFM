.class public Lcom/qf/skin/manager/entity/AttrFactory;
.super Ljava/lang/Object;
.source "AttrFactory.java"


# static fields
.field public static final ATTR_BACKGROUND:Ljava/lang/String; = "background"

.field public static final ATTR_DIVIDER:Ljava/lang/String; = "divider"

.field public static final ATTR_LISTSELECTOR:Ljava/lang/String; = "listSelector"

.field public static final ATTR_SRC:Ljava/lang/String; = "src"

.field private static final ATTR_STYLE:Ljava/lang/String; = "style"

.field public static final ATTR_TEXT:Ljava/lang/String; = "text"

.field public static final ATTR_TEXTCOLOR:Ljava/lang/String; = "textColor"

.field private static supportAttrMap:Ljava/util/HashMap;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Ljava/util/HashMap<",
            "Ljava/lang/String;",
            "Ljava/lang/Object;",
            ">;"
        }
    .end annotation
.end field


# direct methods
.method static constructor <clinit>()V
    .locals 3

    .line 19
    new-instance v0, Ljava/util/HashMap;

    invoke-direct {v0}, Ljava/util/HashMap;-><init>()V

    sput-object v0, Lcom/qf/skin/manager/entity/AttrFactory;->supportAttrMap:Ljava/util/HashMap;

    .line 52
    sget-object v0, Lcom/qf/skin/manager/entity/AttrFactory;->supportAttrMap:Ljava/util/HashMap;

    new-instance v1, Lcom/qf/skin/manager/entity/BackgroundAttr;

    invoke-direct {v1}, Lcom/qf/skin/manager/entity/BackgroundAttr;-><init>()V

    const-string v2, "background"

    invoke-virtual {v0, v2, v1}, Ljava/util/HashMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

    .line 53
    sget-object v0, Lcom/qf/skin/manager/entity/AttrFactory;->supportAttrMap:Ljava/util/HashMap;

    new-instance v1, Lcom/qf/skin/manager/entity/TextColorAttr;

    invoke-direct {v1}, Lcom/qf/skin/manager/entity/TextColorAttr;-><init>()V

    const-string v2, "textColor"

    invoke-virtual {v0, v2, v1}, Ljava/util/HashMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

    .line 54
    sget-object v0, Lcom/qf/skin/manager/entity/AttrFactory;->supportAttrMap:Ljava/util/HashMap;

    new-instance v1, Lcom/qf/skin/manager/entity/ImageViewSrcAttr;

    invoke-direct {v1}, Lcom/qf/skin/manager/entity/ImageViewSrcAttr;-><init>()V

    const-string v2, "src"

    invoke-virtual {v0, v2, v1}, Ljava/util/HashMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

    .line 55
    sget-object v0, Lcom/qf/skin/manager/entity/AttrFactory;->supportAttrMap:Ljava/util/HashMap;

    new-instance v1, Lcom/qf/skin/manager/entity/ListSelectorAttr;

    invoke-direct {v1}, Lcom/qf/skin/manager/entity/ListSelectorAttr;-><init>()V

    const-string v2, "listSelector"

    invoke-virtual {v0, v2, v1}, Ljava/util/HashMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

    .line 56
    sget-object v0, Lcom/qf/skin/manager/entity/AttrFactory;->supportAttrMap:Ljava/util/HashMap;

    new-instance v1, Lcom/qf/skin/manager/entity/DividerAttr;

    invoke-direct {v1}, Lcom/qf/skin/manager/entity/DividerAttr;-><init>()V

    const-string v2, "divider"

    invoke-virtual {v0, v2, v1}, Ljava/util/HashMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

    .line 57
    sget-object v0, Lcom/qf/skin/manager/entity/AttrFactory;->supportAttrMap:Ljava/util/HashMap;

    new-instance v1, Lcom/qf/skin/manager/entity/StyleAttr;

    invoke-direct {v1}, Lcom/qf/skin/manager/entity/StyleAttr;-><init>()V

    const-string v2, "style"

    invoke-virtual {v0, v2, v1}, Ljava/util/HashMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

    return-void
.end method

.method public constructor <init>()V
    .locals 0

    .line 14
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

.method public static get(Landroid/content/Context;Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;
    .locals 3

    .line 86
    new-instance v0, Ljava/lang/StringBuilder;

    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    const-string v1, "GET attrName:"

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v0, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    const-string v1, " attrValueRefId:"

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v0, p2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v1, " attrValueRefName:"

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v0, p3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    const-string v1, " typeName:"

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v0, p4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    const-string v1, " attrValue:"

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v0, p5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    invoke-static {v0}, Lcom/qf/skin/manager/util/L;->i(Ljava/lang/String;)V

    .line 88
    sget-object v0, Lcom/qf/skin/manager/entity/AttrFactory;->supportAttrMap:Ljava/util/HashMap;

    invoke-virtual {v0, p1}, Ljava/util/HashMap;->get(Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v0

    const/4 v1, 0x0

    if-nez v0, :cond_0

    return-object v1

    .line 92
    :cond_0
    instance-of v2, v0, Lcom/qf/skin/manager/entity/SkinAttr;

    if-eqz v2, :cond_1

    .line 93
    check-cast v0, Lcom/qf/skin/manager/entity/SkinAttr;

    invoke-virtual {v0}, Lcom/qf/skin/manager/entity/SkinAttr;->clone()Lcom/qf/skin/manager/entity/SkinAttr;

    move-result-object p0

    .line 94
    iput-object p1, p0, Lcom/qf/skin/manager/entity/SkinAttr;->attrName:Ljava/lang/String;

    .line 95
    iput p2, p0, Lcom/qf/skin/manager/entity/SkinAttr;->attrValueRefId:I

    .line 96
    iput-object p3, p0, Lcom/qf/skin/manager/entity/SkinAttr;->attrValueRefName:Ljava/lang/String;

    .line 97
    iput-object p4, p0, Lcom/qf/skin/manager/entity/SkinAttr;->attrValueTypeName:Ljava/lang/String;

    return-object p0

    .line 99
    :cond_1
    instance-of p1, v0, Lcom/qf/skin/manager/entity/StyleAttr;

    if-eqz p1, :cond_2

    .line 100
    check-cast v0, Lcom/qf/skin/manager/entity/StyleAttr;

    .line 101
    invoke-virtual {v0, p0, p5}, Lcom/qf/skin/manager/entity/StyleAttr;->build(Landroid/content/Context;Ljava/lang/String;)Lcom/qf/skin/manager/entity/StyleAttr;

    move-result-object p0

    return-object p0

    :cond_2
    return-object v1
.end method

.method public static getSupportAttr()Ljava/util/HashMap;
    .locals 1
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "()",
            "Ljava/util/HashMap<",
            "Ljava/lang/String;",
            "Ljava/lang/Object;",
            ">;"
        }
    .end annotation

    .line 123
    sget-object v0, Lcom/qf/skin/manager/entity/AttrFactory;->supportAttrMap:Ljava/util/HashMap;

    return-object v0
.end method

.method public static isSupportedAttr(Ljava/lang/String;)Z
    .locals 1

    .line 114
    sget-object v0, Lcom/qf/skin/manager/entity/AttrFactory;->supportAttrMap:Ljava/util/HashMap;

    invoke-virtual {v0, p0}, Ljava/util/HashMap;->containsKey(Ljava/lang/Object;)Z

    move-result p0

    return p0
.end method
