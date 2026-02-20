.class public Lcom/qf/skin/manager/config/SkinConfig;
.super Ljava/lang/Object;
.source "SkinConfig.java"


# static fields
.field public static final CUSTOM_STYLE1:I = 0x0

.field public static final CUSTOM_STYLE2:I = 0x1

.field public static final CUSTOM_STYLE3:I = 0x2

.field public static final CUSTOM_STYLE4:I = 0x3

.field public static final CUSTOM_THEME_STYLE_SYS:Ljava/lang/String; = "persist.sys.custom.theme"

.field public static final DISPLAY_THEME:Ljava/lang/String; = "display_theme"

.field public static final UI_NIGHT_MODE:Ljava/lang/String; = "ui_night_mode"

.field public static useSkinLib:Z = true


# direct methods
.method static constructor <clinit>()V
    .locals 0

    return-void
.end method

.method public constructor <init>()V
    .locals 0

    .line 10
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

.method public static setDebug(Z)V
    .locals 0

    .line 30
    invoke-static {p0}, Lcom/qf/skin/manager/util/L;->setDEBUG(Z)V

    return-void
.end method

.method public static setInfoDebug(Z)V
    .locals 0

    .line 34
    invoke-static {p0}, Lcom/qf/skin/manager/util/L;->setInfoDEBUG(Z)V

    return-void
.end method
