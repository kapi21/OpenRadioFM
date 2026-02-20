.class public Lcom/qf/skin/manager/entity/ImageViewSrcAttr;
.super Lcom/qf/skin/manager/entity/SkinAttr;
.source "ImageViewSrcAttr.java"


# direct methods
.method public constructor <init>()V
    .locals 0

    .line 13
    invoke-direct {p0}, Lcom/qf/skin/manager/entity/SkinAttr;-><init>()V

    return-void
.end method


# virtual methods
.method public apply(Landroid/view/View;)V
    .locals 3

    .line 18
    instance-of v0, p1, Landroid/widget/ImageView;

    if-eqz v0, :cond_2

    .line 19
    move-object v0, p1

    check-cast v0, Landroid/widget/ImageView;

    .line 20
    iget-object v1, p0, Lcom/qf/skin/manager/entity/ImageViewSrcAttr;->attrValueTypeName:Ljava/lang/String;

    const-string v2, "color"

    invoke-virtual {v2, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v1

    if-eqz v1, :cond_0

    .line 21
    new-instance v1, Landroid/graphics/drawable/ColorDrawable;

    invoke-virtual {p1}, Landroid/view/View;->getResources()Landroid/content/res/Resources;

    move-result-object p1

    iget v2, p0, Lcom/qf/skin/manager/entity/ImageViewSrcAttr;->attrValueRefId:I

    invoke-virtual {p1, v2}, Landroid/content/res/Resources;->getColor(I)I

    move-result p1

    invoke-direct {v1, p1}, Landroid/graphics/drawable/ColorDrawable;-><init>(I)V

    invoke-virtual {v0, v1}, Landroid/widget/ImageView;->setImageDrawable(Landroid/graphics/drawable/Drawable;)V

    goto :goto_0

    .line 22
    :cond_0
    iget-object v1, p0, Lcom/qf/skin/manager/entity/ImageViewSrcAttr;->attrValueTypeName:Ljava/lang/String;

    const-string v2, "drawable"

    invoke-virtual {v2, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v1

    if-nez v1, :cond_1

    iget-object v1, p0, Lcom/qf/skin/manager/entity/ImageViewSrcAttr;->attrValueTypeName:Ljava/lang/String;

    const-string v2, "mipmap"

    .line 23
    invoke-virtual {v2, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v1

    if-eqz v1, :cond_2

    .line 24
    :cond_1
    invoke-virtual {p1}, Landroid/view/View;->getResources()Landroid/content/res/Resources;

    move-result-object p1

    iget v1, p0, Lcom/qf/skin/manager/entity/ImageViewSrcAttr;->attrValueRefId:I

    invoke-virtual {p1, v1}, Landroid/content/res/Resources;->getDrawable(I)Landroid/graphics/drawable/Drawable;

    move-result-object p1

    invoke-virtual {v0, p1}, Landroid/widget/ImageView;->setImageDrawable(Landroid/graphics/drawable/Drawable;)V

    :cond_2
    :goto_0
    return-void
.end method
