.class public Lcom/qf/clientsdk/IBaseManager;
.super Ljava/lang/Object;
.source "IBaseManager.java"


# instance fields
.field mAbsMcuProessor:Lcom/qf/clientsdk/AbsMcuProessor;

.field mMcuSender:Lcom/qf/clientsdk/IMcuSender;


# direct methods
.method public constructor <init>()V
    .locals 0

    .line 3
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method protected setUpMcuProessor(Lcom/qf/clientsdk/AbsMcuProessor;Lcom/qf/clientsdk/IMcuSender;)V
    .locals 0

    .line 11
    iput-object p1, p0, Lcom/qf/clientsdk/IBaseManager;->mAbsMcuProessor:Lcom/qf/clientsdk/AbsMcuProessor;

    .line 12
    iput-object p2, p0, Lcom/qf/clientsdk/IBaseManager;->mMcuSender:Lcom/qf/clientsdk/IMcuSender;

    return-void
.end method
