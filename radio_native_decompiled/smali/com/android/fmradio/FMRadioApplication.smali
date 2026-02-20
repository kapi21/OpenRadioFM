.class public Lcom/android/fmradio/FMRadioApplication;
.super Lcom/qf/skin/manager/base/SkinBaseApplication;
.source "FMRadioApplication.java"


# direct methods
.method public constructor <init>()V
    .locals 0

    .line 10
    invoke-direct {p0}, Lcom/qf/skin/manager/base/SkinBaseApplication;-><init>()V

    return-void
.end method


# virtual methods
.method public addSupportAttrName()Ljava/util/HashMap;
    .locals 1
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "()",
            "Ljava/util/HashMap<",
            "Ljava/lang/String;",
            "Lcom/qf/skin/manager/entity/SkinAttr;",
            ">;"
        }
    .end annotation

    const/4 v0, 0x0

    return-object v0
.end method

.method protected attachBaseContext(Landroid/content/Context;)V
    .locals 1

    .line 18
    sget-object v0, Lcom/android/fmradio/BuildConfig;->UseSkinLib:Ljava/lang/Boolean;

    invoke-virtual {v0}, Ljava/lang/Boolean;->booleanValue()Z

    move-result v0

    invoke-virtual {p0, v0}, Lcom/android/fmradio/FMRadioApplication;->setUseSkinLib(Z)V

    .line 19
    invoke-super {p0, p1}, Lcom/qf/skin/manager/base/SkinBaseApplication;->attachBaseContext(Landroid/content/Context;)V

    return-void
.end method
