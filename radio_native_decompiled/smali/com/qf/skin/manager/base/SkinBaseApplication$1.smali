.class Lcom/qf/skin/manager/base/SkinBaseApplication$1;
.super Ljava/lang/Object;
.source "SkinBaseApplication.java"

# interfaces
.implements Ljava/util/function/BiConsumer;


# annotations
.annotation system Ldalvik/annotation/EnclosingMethod;
    value = Lcom/qf/skin/manager/base/SkinBaseApplication;->initSkinLoader(Landroid/content/Context;)V
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x0
    name = null
.end annotation

.annotation system Ldalvik/annotation/Signature;
    value = {
        "Ljava/lang/Object;",
        "Ljava/util/function/BiConsumer<",
        "Ljava/lang/String;",
        "Lcom/qf/skin/manager/entity/SkinAttr;",
        ">;"
    }
.end annotation


# instance fields
.field final synthetic this$0:Lcom/qf/skin/manager/base/SkinBaseApplication;


# direct methods
.method constructor <init>(Lcom/qf/skin/manager/base/SkinBaseApplication;)V
    .locals 0

    .line 59
    iput-object p1, p0, Lcom/qf/skin/manager/base/SkinBaseApplication$1;->this$0:Lcom/qf/skin/manager/base/SkinBaseApplication;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public bridge synthetic accept(Ljava/lang/Object;Ljava/lang/Object;)V
    .locals 0

    .line 59
    check-cast p1, Ljava/lang/String;

    check-cast p2, Lcom/qf/skin/manager/entity/SkinAttr;

    invoke-virtual {p0, p1, p2}, Lcom/qf/skin/manager/base/SkinBaseApplication$1;->accept(Ljava/lang/String;Lcom/qf/skin/manager/entity/SkinAttr;)V

    return-void
.end method

.method public accept(Ljava/lang/String;Lcom/qf/skin/manager/entity/SkinAttr;)V
    .locals 1

    .line 62
    invoke-static {}, Lcom/qf/skin/manager/loader/SkinManager;->getInstance()Lcom/qf/skin/manager/loader/SkinManager;

    move-result-object v0

    invoke-virtual {v0, p1, p2}, Lcom/qf/skin/manager/loader/SkinManager;->addSupportAttrName(Ljava/lang/String;Lcom/qf/skin/manager/entity/SkinAttr;)V

    return-void
.end method
