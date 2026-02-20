.class Lcom/android/fmradio/views/RadioOperationView$1;
.super Ljava/lang/Object;
.source "RadioOperationView.java"

# interfaces
.implements Landroid/view/View$OnLongClickListener;


# annotations
.annotation system Ldalvik/annotation/EnclosingMethod;
    value = Lcom/android/fmradio/views/RadioOperationView;->initView(Landroid/view/View;)V
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x0
    name = null
.end annotation


# instance fields
.field final synthetic this$0:Lcom/android/fmradio/views/RadioOperationView;


# direct methods
.method constructor <init>(Lcom/android/fmradio/views/RadioOperationView;)V
    .locals 0

    .line 81
    iput-object p1, p0, Lcom/android/fmradio/views/RadioOperationView$1;->this$0:Lcom/android/fmradio/views/RadioOperationView;

    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method


# virtual methods
.method public onLongClick(Landroid/view/View;)Z
    .locals 2

    .line 84
    new-instance p1, Lcom/android/fmradio/favorite/FmFavoriteListFragment;

    invoke-direct {p1}, Lcom/android/fmradio/favorite/FmFavoriteListFragment;-><init>()V

    .line 85
    new-instance v0, Lcom/android/fmradio/views/RadioOperationView$1$1;

    invoke-direct {v0, p0}, Lcom/android/fmradio/views/RadioOperationView$1$1;-><init>(Lcom/android/fmradio/views/RadioOperationView$1;)V

    invoke-virtual {p1, v0}, Lcom/android/fmradio/favorite/FmFavoriteListFragment;->setFmStationInfoListener(Lcom/android/fmradio/favorite/FmFavoriteListFragment$OnFmStationInfoListener;)V

    .line 97
    iget-object v0, p0, Lcom/android/fmradio/views/RadioOperationView$1;->this$0:Lcom/android/fmradio/views/RadioOperationView;

    invoke-static {v0}, Lcom/android/fmradio/views/RadioOperationView;->access$100(Lcom/android/fmradio/views/RadioOperationView;)F

    move-result v0

    invoke-virtual {p1, v0}, Lcom/android/fmradio/favorite/FmFavoriteListFragment;->setScale(F)V

    .line 98
    iget-object v0, p0, Lcom/android/fmradio/views/RadioOperationView$1;->this$0:Lcom/android/fmradio/views/RadioOperationView;

    invoke-static {v0}, Lcom/android/fmradio/views/RadioOperationView;->access$200(Lcom/android/fmradio/views/RadioOperationView;)I

    move-result v0

    invoke-virtual {p1, v0}, Lcom/android/fmradio/favorite/FmFavoriteListFragment;->setYOffset(I)V

    .line 99
    iget-object v0, p0, Lcom/android/fmradio/views/RadioOperationView$1;->this$0:Lcom/android/fmradio/views/RadioOperationView;

    invoke-static {v0}, Lcom/android/fmradio/views/RadioOperationView;->access$000(Lcom/android/fmradio/views/RadioOperationView;)Lcom/android/fmradio/FmMainActivity;

    move-result-object v0

    invoke-virtual {v0}, Lcom/android/fmradio/FmMainActivity;->getFragmentManager()Landroid/app/FragmentManager;

    move-result-object v0

    const-string v1, ""

    invoke-virtual {p1, v0, v1}, Lcom/android/fmradio/favorite/FmFavoriteListFragment;->show(Landroid/app/FragmentManager;Ljava/lang/String;)V

    const/4 p1, 0x1

    return p1
.end method
