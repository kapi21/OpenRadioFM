.class public Lcom/qf/skin/manager/base/SkinFragment;
.super Landroid/app/Fragment;
.source "SkinFragment.java"


# direct methods
.method public constructor <init>()V
    .locals 0

    .line 14
    invoke-direct {p0}, Landroid/app/Fragment;-><init>()V

    return-void
.end method

.method private removeAllView(Landroid/view/View;)V
    .locals 3

    .line 33
    instance-of v0, p1, Landroid/view/ViewGroup;

    if-eqz v0, :cond_1

    .line 34
    move-object v0, p1

    check-cast v0, Landroid/view/ViewGroup;

    const/4 v1, 0x0

    .line 35
    :goto_0
    invoke-virtual {v0}, Landroid/view/ViewGroup;->getChildCount()I

    move-result v2

    if-ge v1, v2, :cond_0

    .line 36
    invoke-virtual {v0, v1}, Landroid/view/ViewGroup;->getChildAt(I)Landroid/view/View;

    move-result-object v2

    invoke-direct {p0, v2}, Lcom/qf/skin/manager/base/SkinFragment;->removeAllView(Landroid/view/View;)V

    add-int/lit8 v1, v1, 0x1

    goto :goto_0

    .line 38
    :cond_0
    invoke-direct {p0, p1}, Lcom/qf/skin/manager/base/SkinFragment;->removeViewInSkinInflaterFactory(Landroid/view/View;)V

    goto :goto_1

    .line 40
    :cond_1
    invoke-direct {p0, p1}, Lcom/qf/skin/manager/base/SkinFragment;->removeViewInSkinInflaterFactory(Landroid/view/View;)V

    :goto_1
    return-void
.end method

.method private removeViewInSkinInflaterFactory(Landroid/view/View;)V
    .locals 1

    .line 45
    invoke-virtual {p0}, Lcom/qf/skin/manager/base/SkinFragment;->getActivity()Landroid/app/Activity;

    move-result-object v0

    instance-of v0, v0, Lcom/qf/skin/manager/base/SkinActivity;

    if-eqz v0, :cond_0

    .line 46
    invoke-virtual {p0}, Lcom/qf/skin/manager/base/SkinFragment;->getActivity()Landroid/app/Activity;

    move-result-object v0

    check-cast v0, Lcom/qf/skin/manager/base/SkinActivity;

    .line 47
    invoke-virtual {v0, p1}, Lcom/qf/skin/manager/base/SkinActivity;->removeSkinView(Landroid/view/View;)V

    :cond_0
    return-void
.end method


# virtual methods
.method public onAttach(Landroid/content/Context;)V
    .locals 0

    .line 20
    invoke-super {p0, p1}, Landroid/app/Fragment;->onAttach(Landroid/content/Context;)V

    return-void
.end method

.method public onDestroyView()V
    .locals 1

    .line 26
    sget-boolean v0, Lcom/qf/skin/manager/config/SkinConfig;->useSkinLib:Z

    if-eqz v0, :cond_0

    .line 27
    invoke-virtual {p0}, Lcom/qf/skin/manager/base/SkinFragment;->getView()Landroid/view/View;

    move-result-object v0

    invoke-direct {p0, v0}, Lcom/qf/skin/manager/base/SkinFragment;->removeAllView(Landroid/view/View;)V

    .line 29
    :cond_0
    invoke-super {p0}, Landroid/app/Fragment;->onDestroyView()V

    return-void
.end method
