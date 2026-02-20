.class public final enum Lcom/qf/clientsdk/pojo/STEERTYPE;
.super Ljava/lang/Enum;
.source "STEERTYPE.java"


# annotations
.annotation system Ldalvik/annotation/Signature;
    value = {
        "Ljava/lang/Enum<",
        "Lcom/qf/clientsdk/pojo/STEERTYPE;",
        ">;"
    }
.end annotation


# static fields
.field private static final synthetic $VALUES:[Lcom/qf/clientsdk/pojo/STEERTYPE;

.field public static final enum STEER_LOW:Lcom/qf/clientsdk/pojo/STEERTYPE;

.field public static final enum STEER_MID:Lcom/qf/clientsdk/pojo/STEERTYPE;

.field public static final enum STEER_MINLOW:Lcom/qf/clientsdk/pojo/STEERTYPE;

.field public static final enum STEER_TOP:Lcom/qf/clientsdk/pojo/STEERTYPE;


# instance fields
.field private sType:I


# direct methods
.method static constructor <clinit>()V
    .locals 6

    .line 5
    new-instance v0, Lcom/qf/clientsdk/pojo/STEERTYPE;

    const/4 v1, 0x0

    const-string v2, "STEER_MINLOW"

    invoke-direct {v0, v2, v1, v1}, Lcom/qf/clientsdk/pojo/STEERTYPE;-><init>(Ljava/lang/String;II)V

    sput-object v0, Lcom/qf/clientsdk/pojo/STEERTYPE;->STEER_MINLOW:Lcom/qf/clientsdk/pojo/STEERTYPE;

    new-instance v0, Lcom/qf/clientsdk/pojo/STEERTYPE;

    const/4 v2, 0x1

    const-string v3, "STEER_LOW"

    invoke-direct {v0, v3, v2, v2}, Lcom/qf/clientsdk/pojo/STEERTYPE;-><init>(Ljava/lang/String;II)V

    sput-object v0, Lcom/qf/clientsdk/pojo/STEERTYPE;->STEER_LOW:Lcom/qf/clientsdk/pojo/STEERTYPE;

    new-instance v0, Lcom/qf/clientsdk/pojo/STEERTYPE;

    const/4 v3, 0x2

    const-string v4, "STEER_MID"

    invoke-direct {v0, v4, v3, v3}, Lcom/qf/clientsdk/pojo/STEERTYPE;-><init>(Ljava/lang/String;II)V

    sput-object v0, Lcom/qf/clientsdk/pojo/STEERTYPE;->STEER_MID:Lcom/qf/clientsdk/pojo/STEERTYPE;

    new-instance v0, Lcom/qf/clientsdk/pojo/STEERTYPE;

    const/4 v4, 0x3

    const-string v5, "STEER_TOP"

    invoke-direct {v0, v5, v4, v4}, Lcom/qf/clientsdk/pojo/STEERTYPE;-><init>(Ljava/lang/String;II)V

    sput-object v0, Lcom/qf/clientsdk/pojo/STEERTYPE;->STEER_TOP:Lcom/qf/clientsdk/pojo/STEERTYPE;

    const/4 v0, 0x4

    new-array v0, v0, [Lcom/qf/clientsdk/pojo/STEERTYPE;

    .line 3
    sget-object v5, Lcom/qf/clientsdk/pojo/STEERTYPE;->STEER_MINLOW:Lcom/qf/clientsdk/pojo/STEERTYPE;

    aput-object v5, v0, v1

    sget-object v1, Lcom/qf/clientsdk/pojo/STEERTYPE;->STEER_LOW:Lcom/qf/clientsdk/pojo/STEERTYPE;

    aput-object v1, v0, v2

    sget-object v1, Lcom/qf/clientsdk/pojo/STEERTYPE;->STEER_MID:Lcom/qf/clientsdk/pojo/STEERTYPE;

    aput-object v1, v0, v3

    sget-object v1, Lcom/qf/clientsdk/pojo/STEERTYPE;->STEER_TOP:Lcom/qf/clientsdk/pojo/STEERTYPE;

    aput-object v1, v0, v4

    sput-object v0, Lcom/qf/clientsdk/pojo/STEERTYPE;->$VALUES:[Lcom/qf/clientsdk/pojo/STEERTYPE;

    return-void
.end method

.method private constructor <init>(Ljava/lang/String;II)V
    .locals 0
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(I)V"
        }
    .end annotation

    .line 9
    invoke-direct {p0, p1, p2}, Ljava/lang/Enum;-><init>(Ljava/lang/String;I)V

    const/4 p1, 0x0

    .line 7
    iput p1, p0, Lcom/qf/clientsdk/pojo/STEERTYPE;->sType:I

    .line 10
    iput p3, p0, Lcom/qf/clientsdk/pojo/STEERTYPE;->sType:I

    return-void
.end method

.method public static toSteerType(I)Lcom/qf/clientsdk/pojo/STEERTYPE;
    .locals 1

    if-eqz p0, :cond_3

    const/4 v0, 0x1

    if-eq p0, v0, :cond_2

    const/4 v0, 0x2

    if-eq p0, v0, :cond_1

    const/4 v0, 0x3

    if-eq p0, v0, :cond_0

    .line 28
    sget-object p0, Lcom/qf/clientsdk/pojo/STEERTYPE;->STEER_LOW:Lcom/qf/clientsdk/pojo/STEERTYPE;

    return-object p0

    .line 26
    :cond_0
    sget-object p0, Lcom/qf/clientsdk/pojo/STEERTYPE;->STEER_TOP:Lcom/qf/clientsdk/pojo/STEERTYPE;

    return-object p0

    .line 24
    :cond_1
    sget-object p0, Lcom/qf/clientsdk/pojo/STEERTYPE;->STEER_MID:Lcom/qf/clientsdk/pojo/STEERTYPE;

    return-object p0

    .line 22
    :cond_2
    sget-object p0, Lcom/qf/clientsdk/pojo/STEERTYPE;->STEER_LOW:Lcom/qf/clientsdk/pojo/STEERTYPE;

    return-object p0

    .line 20
    :cond_3
    sget-object p0, Lcom/qf/clientsdk/pojo/STEERTYPE;->STEER_MINLOW:Lcom/qf/clientsdk/pojo/STEERTYPE;

    return-object p0
.end method

.method public static valueOf(Ljava/lang/String;)Lcom/qf/clientsdk/pojo/STEERTYPE;
    .locals 1

    .line 3
    const-class v0, Lcom/qf/clientsdk/pojo/STEERTYPE;

    invoke-static {v0, p0}, Ljava/lang/Enum;->valueOf(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;

    move-result-object p0

    check-cast p0, Lcom/qf/clientsdk/pojo/STEERTYPE;

    return-object p0
.end method

.method public static values()[Lcom/qf/clientsdk/pojo/STEERTYPE;
    .locals 1

    .line 3
    sget-object v0, Lcom/qf/clientsdk/pojo/STEERTYPE;->$VALUES:[Lcom/qf/clientsdk/pojo/STEERTYPE;

    invoke-virtual {v0}, [Lcom/qf/clientsdk/pojo/STEERTYPE;->clone()Ljava/lang/Object;

    move-result-object v0

    check-cast v0, [Lcom/qf/clientsdk/pojo/STEERTYPE;

    return-object v0
.end method


# virtual methods
.method public toIntType()I
    .locals 1

    .line 14
    iget v0, p0, Lcom/qf/clientsdk/pojo/STEERTYPE;->sType:I

    return v0
.end method
