.class public Lcom/qf/skin/manager/base/SkinAppCompatFragment;
.super Landroidx/fragment/app/Fragment;
.source "SkinAppCompatFragment.java"


# direct methods
.method public constructor <init>()V
    .locals 0

    .line 12
    invoke-direct {p0}, Landroidx/fragment/app/Fragment;-><init>()V

    return-void
.end method

.method private removeAllView(Landroid/view/View;)V
    .locals 3

    .line 26
    instance-of v0, p1, Landroid/view/ViewGroup;

    if-eqz v0, :cond_1

    .line 27
    move-object v0, p1

    check-cast v0, Landroid/view/ViewGroup;

    const/4 v1, 0x0

    .line 28
    :goto_0
    invoke-virtual {v0}, Landroid/view/ViewGroup;->getChildCount()I

    move-result v2

    if-ge v1, v2, :cond_0

    .line 29
    invoke-virtual {v0, v1}, Landroid/view/ViewGroup;->getChildAt(I)Landroid/view/View;

    move-result-object v2

    invoke-direct {p0, v2}, Lcom/qf/skin/manager/base/SkinAppCompatFragment;->removeAllView(Landroid/view/View;)V

    add-int/lit8 v1, v1, 0x1

    goto :goto_0

    .line 31
    :cond_0
    invoke-direct {p0, p1}, Lcom/qf/skin/manager/base/SkinAppCompatFragment;->removeViewInSkinInflaterFactory(Landroid/view/View;)V

    goto :goto_1

    .line 33
    :cond_1
    invoke-direct {p0, p1}, Lcom/qf/skin/manager/base/SkinAppCompatFragment;->removeViewInSkinInflaterFactory(Landroid/view/View;)V

    :goto_1
    return-void
.end method

.method private removeViewInSkinInflaterFactory(Landroid/view/View;)V
    .locals 1

    .line 38
    invoke-virtual {p0}, Lcom/qf/skin/manager/base/SkinAppCompatFragment;->getActivity()Landroidx/fragment/app/FragmentActivity;

    move-result-object v0

    instance-of v0, v0, Lcom/qf/skin/manager/base/SkinAppCompatActivity;

    if-eqz v0, :cond_0

    .line 39
    invoke-virtual {p0}, Lcom/qf/skin/manager/base/SkinAppCompatFragment;->getActivity()Landroidx/fragment/app/FragmentActivity;

    move-result-object v0

    check-cast v0, Lcom/qf/skin/manager/base/SkinAppCompatActivity;

    .line 40
    invoke-virtual {v0, p1}, Lcom/qf/skin/manager/base/SkinAppCompatActivity;->removeSkinView(Landroid/view/View;)V

    :cond_0
    return-void
.end method


# virtual methods
.method public onAttach(Landroid/content/Context;)V
    .locals 0

    .line 15
    invoke-super {p0, p1}, Landroidx/fragment/app/Fragment;->onAttach(Landroid/content/Context;)V

    return-void
.end method

.method public onDestroyView()V
    .locals 1

    .line 21
    invoke-virtual {p0}, Lcom/qf/skin/manager/base/SkinAppCompatFragment;->getView()Landroid/view/View;

    move-result-object v0

    invoke-direct {p0, v0}, Lcom/qf/skin/manager/base/SkinAppCompatFragment;->removeAllView(Landroid/view/View;)V

    .line 22
    invoke-super {p0}, Landroidx/fragment/app/Fragment;->onDestroyView()V

    return-void
.end method
