.class public final enum Lcom/qf/clientsdk/ClientSdkParam$SDKAPI;
.super Ljava/lang/Enum;
.source "ClientSdkParam.java"


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Lcom/qf/clientsdk/ClientSdkParam;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x4019
    name = "SDKAPI"
.end annotation

.annotation system Ldalvik/annotation/Signature;
    value = {
        "Ljava/lang/Enum<",
        "Lcom/qf/clientsdk/ClientSdkParam$SDKAPI;",
        ">;"
    }
.end annotation


# static fields
.field private static final synthetic $VALUES:[Lcom/qf/clientsdk/ClientSdkParam$SDKAPI;

.field public static final enum DEFALUT:Lcom/qf/clientsdk/ClientSdkParam$SDKAPI;

.field public static final enum SDK_XXXXX:Lcom/qf/clientsdk/ClientSdkParam$SDKAPI;


# direct methods
.method static constructor <clinit>()V
    .locals 4

    .line 25
    new-instance v0, Lcom/qf/clientsdk/ClientSdkParam$SDKAPI;

    const/4 v1, 0x0

    const-string v2, "DEFALUT"

    invoke-direct {v0, v2, v1}, Lcom/qf/clientsdk/ClientSdkParam$SDKAPI;-><init>(Ljava/lang/String;I)V

    sput-object v0, Lcom/qf/clientsdk/ClientSdkParam$SDKAPI;->DEFALUT:Lcom/qf/clientsdk/ClientSdkParam$SDKAPI;

    .line 26
    new-instance v0, Lcom/qf/clientsdk/ClientSdkParam$SDKAPI;

    const/4 v2, 0x1

    const-string v3, "SDK_XXXXX"

    invoke-direct {v0, v3, v2}, Lcom/qf/clientsdk/ClientSdkParam$SDKAPI;-><init>(Ljava/lang/String;I)V

    sput-object v0, Lcom/qf/clientsdk/ClientSdkParam$SDKAPI;->SDK_XXXXX:Lcom/qf/clientsdk/ClientSdkParam$SDKAPI;

    const/4 v0, 0x2

    new-array v0, v0, [Lcom/qf/clientsdk/ClientSdkParam$SDKAPI;

    .line 24
    sget-object v3, Lcom/qf/clientsdk/ClientSdkParam$SDKAPI;->DEFALUT:Lcom/qf/clientsdk/ClientSdkParam$SDKAPI;

    aput-object v3, v0, v1

    sget-object v1, Lcom/qf/clientsdk/ClientSdkParam$SDKAPI;->SDK_XXXXX:Lcom/qf/clientsdk/ClientSdkParam$SDKAPI;

    aput-object v1, v0, v2

    sput-object v0, Lcom/qf/clientsdk/ClientSdkParam$SDKAPI;->$VALUES:[Lcom/qf/clientsdk/ClientSdkParam$SDKAPI;

    return-void
.end method

.method private constructor <init>(Ljava/lang/String;I)V
    .locals 0
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "()V"
        }
    .end annotation

    .line 24
    invoke-direct {p0, p1, p2}, Ljava/lang/Enum;-><init>(Ljava/lang/String;I)V

    return-void
.end method

.method public static valueOf(Ljava/lang/String;)Lcom/qf/clientsdk/ClientSdkParam$SDKAPI;
    .locals 1

    .line 24
    const-class v0, Lcom/qf/clientsdk/ClientSdkParam$SDKAPI;

    invoke-static {v0, p0}, Ljava/lang/Enum;->valueOf(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;

    move-result-object p0

    check-cast p0, Lcom/qf/clientsdk/ClientSdkParam$SDKAPI;

    return-object p0
.end method

.method public static values()[Lcom/qf/clientsdk/ClientSdkParam$SDKAPI;
    .locals 1

    .line 24
    sget-object v0, Lcom/qf/clientsdk/ClientSdkParam$SDKAPI;->$VALUES:[Lcom/qf/clientsdk/ClientSdkParam$SDKAPI;

    invoke-virtual {v0}, [Lcom/qf/clientsdk/ClientSdkParam$SDKAPI;->clone()Ljava/lang/Object;

    move-result-object v0

    check-cast v0, [Lcom/qf/clientsdk/ClientSdkParam$SDKAPI;

    return-object v0
.end method
