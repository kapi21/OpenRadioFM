.class public Lcom/qf/skin/manager/entity/TextColorAttr;
.super Lcom/qf/skin/manager/entity/SkinAttr;
.source "TextColorAttr.java"


# direct methods
.method public constructor <init>()V
    .locals 0

    .line 8
    invoke-direct {p0}, Lcom/qf/skin/manager/entity/SkinAttr;-><init>()V

    return-void
.end method


# virtual methods
.method public apply(Landroid/view/View;)V
    .locals 3

    .line 12
    instance-of v0, p1, Landroid/widget/TextView;

    if-eqz v0, :cond_0

    .line 13
    move-object v0, p1

    check-cast v0, Landroid/widget/TextView;

    .line 14
    iget-object v1, p0, Lcom/qf/skin/manager/entity/TextColorAttr;->attrValueTypeName:Ljava/lang/String;

    const-string v2, "color"

    invoke-virtual {v2, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v1

    if-eqz v1, :cond_0

    .line 15
    invoke-virtual {p1}, Landroid/view/View;->getResources()Landroid/content/res/Resources;

    move-result-object p1

    iget v1, p0, Lcom/qf/skin/manager/entity/TextColorAttr;->attrValueRefId:I

    invoke-virtual {p1, v1}, Landroid/content/res/Resources;->getColorStateList(I)Landroid/content/res/ColorStateList;

    move-result-object p1

    invoke-virtual {v0, p1}, Landroid/widget/TextView;->setTextColor(Landroid/content/res/ColorStateList;)V

    :cond_0
    return-void
.end method
