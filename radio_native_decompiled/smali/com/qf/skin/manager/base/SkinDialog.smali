.class public abstract Lcom/qf/skin/manager/base/SkinDialog;
.super Landroid/app/Dialog;
.source "SkinDialog.java"

# interfaces
.implements Lcom/qf/skin/manager/interfaces/ISkinUpdate;


# direct methods
.method public constructor <init>(Landroid/content/Context;)V
    .locals 0

    .line 14
    invoke-direct {p0, p1}, Landroid/app/Dialog;-><init>(Landroid/content/Context;)V

    return-void
.end method


# virtual methods
.method public dismiss()V
    .locals 1

    .line 25
    invoke-super {p0}, Landroid/app/Dialog;->dismiss()V

    .line 26
    invoke-static {}, Lcom/qf/skin/manager/loader/SkinManager;->getInstance()Lcom/qf/skin/manager/loader/SkinManager;

    move-result-object v0

    invoke-virtual {v0, p0}, Lcom/qf/skin/manager/loader/SkinManager;->detach(Lcom/qf/skin/manager/interfaces/ISkinUpdate;)V

    return-void
.end method

.method public show()V
    .locals 1

    .line 19
    invoke-super {p0}, Landroid/app/Dialog;->show()V

    .line 20
    invoke-static {}, Lcom/qf/skin/manager/loader/SkinManager;->getInstance()Lcom/qf/skin/manager/loader/SkinManager;

    move-result-object v0

    invoke-virtual {v0, p0}, Lcom/qf/skin/manager/loader/SkinManager;->attach(Lcom/qf/skin/manager/interfaces/ISkinUpdate;)V

    return-void
.end method
