.class public final enum Lcom/android/fmradio/FmConstants$AudioPath;
.super Ljava/lang/Enum;
.source "FmConstants.java"


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Lcom/android/fmradio/FmConstants;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x4019
    name = "AudioPath"
.end annotation

.annotation system Ldalvik/annotation/Signature;
    value = {
        "Ljava/lang/Enum<",
        "Lcom/android/fmradio/FmConstants$AudioPath;",
        ">;"
    }
.end annotation


# static fields
.field private static final synthetic $VALUES:[Lcom/android/fmradio/FmConstants$AudioPath;

.field public static final enum FM_AUDIO_PATH_HEADSET:Lcom/android/fmradio/FmConstants$AudioPath;

.field public static final enum FM_AUDIO_PATH_NONE:Lcom/android/fmradio/FmConstants$AudioPath;

.field public static final enum FM_AUDIO_PATH_SPEAKER:Lcom/android/fmradio/FmConstants$AudioPath;

.field public static final enum FM_AUDIO_PATH_UNKNOWN:Lcom/android/fmradio/FmConstants$AudioPath;


# direct methods
.method static constructor <clinit>()V
    .locals 6

    .line 6
    new-instance v0, Lcom/android/fmradio/FmConstants$AudioPath;

    const/4 v1, 0x0

    const-string v2, "FM_AUDIO_PATH_NONE"

    invoke-direct {v0, v2, v1}, Lcom/android/fmradio/FmConstants$AudioPath;-><init>(Ljava/lang/String;I)V

    sput-object v0, Lcom/android/fmradio/FmConstants$AudioPath;->FM_AUDIO_PATH_NONE:Lcom/android/fmradio/FmConstants$AudioPath;

    .line 7
    new-instance v0, Lcom/android/fmradio/FmConstants$AudioPath;

    const/4 v2, 0x1

    const-string v3, "FM_AUDIO_PATH_SPEAKER"

    invoke-direct {v0, v3, v2}, Lcom/android/fmradio/FmConstants$AudioPath;-><init>(Ljava/lang/String;I)V

    sput-object v0, Lcom/android/fmradio/FmConstants$AudioPath;->FM_AUDIO_PATH_SPEAKER:Lcom/android/fmradio/FmConstants$AudioPath;

    .line 8
    new-instance v0, Lcom/android/fmradio/FmConstants$AudioPath;

    const/4 v3, 0x2

    const-string v4, "FM_AUDIO_PATH_HEADSET"

    invoke-direct {v0, v4, v3}, Lcom/android/fmradio/FmConstants$AudioPath;-><init>(Ljava/lang/String;I)V

    sput-object v0, Lcom/android/fmradio/FmConstants$AudioPath;->FM_AUDIO_PATH_HEADSET:Lcom/android/fmradio/FmConstants$AudioPath;

    .line 9
    new-instance v0, Lcom/android/fmradio/FmConstants$AudioPath;

    const/4 v4, 0x3

    const-string v5, "FM_AUDIO_PATH_UNKNOWN"

    invoke-direct {v0, v5, v4}, Lcom/android/fmradio/FmConstants$AudioPath;-><init>(Ljava/lang/String;I)V

    sput-object v0, Lcom/android/fmradio/FmConstants$AudioPath;->FM_AUDIO_PATH_UNKNOWN:Lcom/android/fmradio/FmConstants$AudioPath;

    const/4 v0, 0x4

    new-array v0, v0, [Lcom/android/fmradio/FmConstants$AudioPath;

    .line 5
    sget-object v5, Lcom/android/fmradio/FmConstants$AudioPath;->FM_AUDIO_PATH_NONE:Lcom/android/fmradio/FmConstants$AudioPath;

    aput-object v5, v0, v1

    sget-object v1, Lcom/android/fmradio/FmConstants$AudioPath;->FM_AUDIO_PATH_SPEAKER:Lcom/android/fmradio/FmConstants$AudioPath;

    aput-object v1, v0, v2

    sget-object v1, Lcom/android/fmradio/FmConstants$AudioPath;->FM_AUDIO_PATH_HEADSET:Lcom/android/fmradio/FmConstants$AudioPath;

    aput-object v1, v0, v3

    sget-object v1, Lcom/android/fmradio/FmConstants$AudioPath;->FM_AUDIO_PATH_UNKNOWN:Lcom/android/fmradio/FmConstants$AudioPath;

    aput-object v1, v0, v4

    sput-object v0, Lcom/android/fmradio/FmConstants$AudioPath;->$VALUES:[Lcom/android/fmradio/FmConstants$AudioPath;

    return-void
.end method

.method private constructor <init>(Ljava/lang/String;I)V
    .locals 0
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "()V"
        }
    .end annotation

    .line 5
    invoke-direct {p0, p1, p2}, Ljava/lang/Enum;-><init>(Ljava/lang/String;I)V

    return-void
.end method

.method public static valueOf(Ljava/lang/String;)Lcom/android/fmradio/FmConstants$AudioPath;
    .locals 1

    .line 5
    const-class v0, Lcom/android/fmradio/FmConstants$AudioPath;

    invoke-static {v0, p0}, Ljava/lang/Enum;->valueOf(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;

    move-result-object p0

    check-cast p0, Lcom/android/fmradio/FmConstants$AudioPath;

    return-object p0
.end method

.method public static values()[Lcom/android/fmradio/FmConstants$AudioPath;
    .locals 1

    .line 5
    sget-object v0, Lcom/android/fmradio/FmConstants$AudioPath;->$VALUES:[Lcom/android/fmradio/FmConstants$AudioPath;

    invoke-virtual {v0}, [Lcom/android/fmradio/FmConstants$AudioPath;->clone()Ljava/lang/Object;

    move-result-object v0

    check-cast v0, [Lcom/android/fmradio/FmConstants$AudioPath;

    return-object v0
.end method
