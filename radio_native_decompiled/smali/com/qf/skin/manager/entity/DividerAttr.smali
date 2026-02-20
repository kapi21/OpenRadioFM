.class public Lcom/qf/skin/manager/entity/DividerAttr;
.super Lcom/qf/skin/manager/entity/SkinAttr;
.source "DividerAttr.java"


# instance fields
.field public dividerHeight:I


# direct methods
.method public constructor <init>()V
    .locals 1

    .line 10
    invoke-direct {p0}, Lcom/qf/skin/manager/entity/SkinAttr;-><init>()V

    const/4 v0, 0x1

    .line 12
    iput v0, p0, Lcom/qf/skin/manager/entity/DividerAttr;->dividerHeight:I

    return-void
.end method


# virtual methods
.method public apply(Landroid/view/View;)V
    .locals 3

    .line 16
    instance-of v0, p1, Landroid/widget/ListView;

    if-eqz v0, :cond_2

    .line 17
    move-object v0, p1

    check-cast v0, Landroid/widget/ListView;

    .line 18
    iget-object v1, p0, Lcom/qf/skin/manager/entity/DividerAttr;->attrValueTypeName:Ljava/lang/String;

    const-string v2, "color"

    invoke-virtual {v2, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v1

    if-eqz v1, :cond_0

    .line 19
    invoke-virtual {p1}, Landroid/view/View;->getResources()Landroid/content/res/Resources;

    move-result-object p1

    iget v1, p0, Lcom/qf/skin/manager/entity/DividerAttr;->attrValueRefId:I

    invoke-virtual {p1, v1}, Landroid/content/res/Resources;->getColor(I)I

    move-result p1

    .line 20
    new-instance v1, Landroid/graphics/drawable/ColorDrawable;

    invoke-direct {v1, p1}, Landroid/graphics/drawable/ColorDrawable;-><init>(I)V

    .line 21
    invoke-virtual {v0, v1}, Landroid/widget/ListView;->setDivider(Landroid/graphics/drawable/Drawable;)V

    .line 22
    iget p1, p0, Lcom/qf/skin/manager/entity/DividerAttr;->dividerHeight:I

    invoke-virtual {v0, p1}, Landroid/widget/ListView;->setDividerHeight(I)V

    goto :goto_0

    .line 23
    :cond_0
    iget-object v1, p0, Lcom/qf/skin/manager/entity/DividerAttr;->attrValueTypeName:Ljava/lang/String;

    const-string v2, "drawable"

    invoke-virtual {v2, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v1

    if-nez v1, :cond_1

    iget-object v1, p0, Lcom/qf/skin/manager/entity/DividerAttr;->attrValueTypeName:Ljava/lang/String;

    const-string v2, "mipmap"

    .line 24
    invoke-virtual {v2, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v1

    if-eqz v1, :cond_2

    .line 25
    :cond_1
    invoke-virtual {p1}, Landroid/view/View;->getResources()Landroid/content/res/Resources;

    move-result-object p1

    iget v1, p0, Lcom/qf/skin/manager/entity/DividerAttr;->attrValueRefId:I

    invoke-virtual {p1, v1}, Landroid/content/res/Resources;->getDrawable(I)Landroid/graphics/drawable/Drawable;

    move-result-object p1

    invoke-virtual {v0, p1}, Landroid/widget/ListView;->setDivider(Landroid/graphics/drawable/Drawable;)V

    :cond_2
    :goto_0
    return-void
.end method
