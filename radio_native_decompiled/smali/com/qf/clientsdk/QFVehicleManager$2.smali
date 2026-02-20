.class Lcom/qf/clientsdk/QFVehicleManager$2;
.super Ljava/lang/Object;
.source "QFVehicleManager.java"

# interfaces
.implements Lcom/qf/clientsdk/QFVehicleManager$DoCallback;


# annotations
.annotation system Ldalvik/annotation/EnclosingMethod;
    value = Lcom/qf/clientsdk/QFVehicleManager;->notifyHandLight(Z)V
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x0
    name = null
.end annotation


# instance fields
.field final synthetic this$0:Lcom/qf/clientsdk/QFVehicleManager;

.field final synthetic val$isOn:Z


# direct methods
.method constructor <init>(Lcom/qf/clientsdk/QFVehicleManager;Z)V
    .locals 0

    .line 81
    iput-object p1, p0, Lcom/qf/clientsdk/QFVehicleManager$2;->this$0:Lcom/qf/clientsdk/QFVehicleManager;

    iput-boolean p2, p0, Lcom/qf/clientsdk/QFVehicleManager$2;->val$isOn:Z

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public doLocalCallback(Lcom/qf/clientsdk/listeners/IVehicleTool;)V
    .locals 1

    .line 84
    iget-boolean v0, p0, Lcom/qf/clientsdk/QFVehicleManager$2;->val$isOn:Z

    invoke-interface {p1, v0}, Lcom/qf/clientsdk/listeners/IVehicleTool;->onHeadLightChange(Z)V

    return-void
.end method
