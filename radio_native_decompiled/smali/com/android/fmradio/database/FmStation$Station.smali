.class public final Lcom/android/fmradio/database/FmStation$Station;
.super Ljava/lang/Object;
.source "FmStation.java"

# interfaces
.implements Landroid/provider/BaseColumns;


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Lcom/android/fmradio/database/FmStation;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x19
    name = "Station"
.end annotation


# static fields
.field public static final CONTENT_URI:Landroid/net/Uri;

.field public static final FREQUENCY:Ljava/lang/String; = "frequency"

.field public static final INDEX:Ljava/lang/String; = "preset"

.field public static final IS_FAVORITE:Ljava/lang/String; = "is_favorite"

.field public static final PROGRAM_SERVICE:Ljava/lang/String; = "program_service"

.field public static final RADIO_BAND:Ljava/lang/String; = "radio_band"

.field public static final RADIO_TEXT:Ljava/lang/String; = "radio_text"

.field public static final STATION_NAME:Ljava/lang/String; = "station_name"


# direct methods
.method static constructor <clinit>()V
    .locals 1

    const-string v0, "content://com.android.fmradio.ext/station"

    .line 47
    invoke-static {v0}, Landroid/net/Uri;->parse(Ljava/lang/String;)Landroid/net/Uri;

    move-result-object v0

    sput-object v0, Lcom/android/fmradio/database/FmStation$Station;->CONTENT_URI:Landroid/net/Uri;

    return-void
.end method

.method public constructor <init>()V
    .locals 0

    .line 46
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method
