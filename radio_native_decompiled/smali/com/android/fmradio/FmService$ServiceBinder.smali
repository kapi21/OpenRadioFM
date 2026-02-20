.class public Lcom/android/fmradio/FmService$ServiceBinder;
.super Landroid/os/Binder;
.source "FmService.java"


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Lcom/android/fmradio/FmService;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x1
    name = "ServiceBinder"
.end annotation


# instance fields
.field final synthetic this$0:Lcom/android/fmradio/FmService;


# direct methods
.method public constructor <init>(Lcom/android/fmradio/FmService;)V
    .locals 0

    .line 155
    iput-object p1, p0, Lcom/android/fmradio/FmService$ServiceBinder;->this$0:Lcom/android/fmradio/FmService;

    invoke-direct {p0}, Landroid/os/Binder;-><init>()V

    return-void
.end method


# virtual methods
.method getService()Lcom/android/fmradio/FmService;
    .locals 1

    .line 162
    iget-object v0, p0, Lcom/android/fmradio/FmService$ServiceBinder;->this$0:Lcom/android/fmradio/FmService;

    return-object v0
.end method
