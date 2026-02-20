.class public Lcom/qf/skin/manager/loader/SkinManager;
.super Ljava/lang/Object;
.source "SkinManager.java"


# static fields
.field private static final LOCK:Ljava/lang/Object;

.field private static final MSG_CHANGE_RES:I = 0x1

.field private static final MSG_NOTIF_UI:I = 0x2

.field private static final MSG_PRELOADING:I = 0x3

.field private static instance:Lcom/qf/skin/manager/loader/SkinManager;

.field private static worker:Landroid/os/HandlerThread;


# instance fields
.field private context:Landroid/content/Context;

.field private delayUpdateSkin:Ljava/lang/Runnable;

.field private isLoading:Z

.field private isPreloading:Z

.field private mILoaderListener:Lcom/qf/skin/manager/interfaces/ISkinLoaderListener;

.field private mResources:Landroid/content/res/Resources;

.field private mainHandler:Landroid/os/Handler;

.field private preInflateLayouts:Ljava/util/concurrent/CopyOnWriteArrayList;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Ljava/util/concurrent/CopyOnWriteArrayList<",
            "Ljava/lang/Integer;",
            ">;"
        }
    .end annotation
.end field

.field private skinObservers:Ljava/util/List;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Ljava/util/List<",
            "Lcom/qf/skin/manager/interfaces/ISkinUpdate;",
            ">;"
        }
    .end annotation
.end field

.field private skinPackageName:Ljava/lang/String;

.field private windowSkinObservers:Ljava/util/List;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Ljava/util/List<",
            "Lcom/qf/skin/manager/interfaces/ISkinUpdate;",
            ">;"
        }
    .end annotation
.end field

.field private workHandler:Landroid/os/Handler;


# direct methods
.method static constructor <clinit>()V
    .locals 2

    .line 40
    new-instance v0, Ljava/lang/Object;

    invoke-direct {v0}, Ljava/lang/Object;-><init>()V

    sput-object v0, Lcom/qf/skin/manager/loader/SkinManager;->LOCK:Ljava/lang/Object;

    .line 69
    new-instance v0, Landroid/os/HandlerThread;

    const-string v1, "skin-worker"

    invoke-direct {v0, v1}, Landroid/os/HandlerThread;-><init>(Ljava/lang/String;)V

    sput-object v0, Lcom/qf/skin/manager/loader/SkinManager;->worker:Landroid/os/HandlerThread;

    .line 72
    sget-object v0, Lcom/qf/skin/manager/loader/SkinManager;->worker:Landroid/os/HandlerThread;

    const/4 v1, 0x5

    invoke-virtual {v0, v1}, Landroid/os/HandlerThread;->setPriority(I)V

    .line 73
    sget-object v0, Lcom/qf/skin/manager/loader/SkinManager;->worker:Landroid/os/HandlerThread;

    invoke-virtual {v0}, Landroid/os/HandlerThread;->start()V

    return-void
.end method

.method private constructor <init>()V
    .locals 2

    .line 147
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    const/4 v0, 0x0

    .line 56
    iput-boolean v0, p0, Lcom/qf/skin/manager/loader/SkinManager;->isLoading:Z

    .line 58
    iput-boolean v0, p0, Lcom/qf/skin/manager/loader/SkinManager;->isPreloading:Z

    const/4 v0, 0x0

    .line 60
    iput-object v0, p0, Lcom/qf/skin/manager/loader/SkinManager;->mILoaderListener:Lcom/qf/skin/manager/interfaces/ISkinLoaderListener;

    .line 65
    new-instance v0, Ljava/util/concurrent/CopyOnWriteArrayList;

    invoke-direct {v0}, Ljava/util/concurrent/CopyOnWriteArrayList;-><init>()V

    iput-object v0, p0, Lcom/qf/skin/manager/loader/SkinManager;->preInflateLayouts:Ljava/util/concurrent/CopyOnWriteArrayList;

    .line 81
    new-instance v0, Landroid/os/Handler;

    invoke-direct {v0}, Landroid/os/Handler;-><init>()V

    iput-object v0, p0, Lcom/qf/skin/manager/loader/SkinManager;->mainHandler:Landroid/os/Handler;

    .line 82
    new-instance v0, Lcom/qf/skin/manager/loader/SkinManager$1;

    sget-object v1, Lcom/qf/skin/manager/loader/SkinManager;->worker:Landroid/os/HandlerThread;

    invoke-virtual {v1}, Landroid/os/HandlerThread;->getLooper()Landroid/os/Looper;

    move-result-object v1

    invoke-direct {v0, p0, v1}, Lcom/qf/skin/manager/loader/SkinManager$1;-><init>(Lcom/qf/skin/manager/loader/SkinManager;Landroid/os/Looper;)V

    iput-object v0, p0, Lcom/qf/skin/manager/loader/SkinManager;->workHandler:Landroid/os/Handler;

    .line 322
    new-instance v0, Lcom/qf/skin/manager/loader/SkinManager$4;

    invoke-direct {v0, p0}, Lcom/qf/skin/manager/loader/SkinManager$4;-><init>(Lcom/qf/skin/manager/loader/SkinManager;)V

    iput-object v0, p0, Lcom/qf/skin/manager/loader/SkinManager;->delayUpdateSkin:Ljava/lang/Runnable;

    return-void
.end method

.method static synthetic access$000(Lcom/qf/skin/manager/loader/SkinManager;)Z
    .locals 0

    .line 38
    iget-boolean p0, p0, Lcom/qf/skin/manager/loader/SkinManager;->isPreloading:Z

    return p0
.end method

.method static synthetic access$100(Lcom/qf/skin/manager/loader/SkinManager;)V
    .locals 0

    .line 38
    invoke-direct {p0}, Lcom/qf/skin/manager/loader/SkinManager;->preInfalteLayout()V

    return-void
.end method

.method static synthetic access$200(Lcom/qf/skin/manager/loader/SkinManager;)Lcom/qf/skin/manager/interfaces/ISkinLoaderListener;
    .locals 0

    .line 38
    iget-object p0, p0, Lcom/qf/skin/manager/loader/SkinManager;->mILoaderListener:Lcom/qf/skin/manager/interfaces/ISkinLoaderListener;

    return-object p0
.end method

.method static synthetic access$300(Lcom/qf/skin/manager/loader/SkinManager;Z)V
    .locals 0

    .line 38
    invoke-direct {p0, p1}, Lcom/qf/skin/manager/loader/SkinManager;->notifySkinUpdate(Z)V

    return-void
.end method

.method public static getInstance()Lcom/qf/skin/manager/loader/SkinManager;
    .locals 2

    .line 137
    sget-object v0, Lcom/qf/skin/manager/loader/SkinManager;->instance:Lcom/qf/skin/manager/loader/SkinManager;

    if-nez v0, :cond_1

    .line 138
    sget-object v0, Lcom/qf/skin/manager/loader/SkinManager;->LOCK:Ljava/lang/Object;

    monitor-enter v0

    .line 139
    :try_start_0
    sget-object v1, Lcom/qf/skin/manager/loader/SkinManager;->instance:Lcom/qf/skin/manager/loader/SkinManager;

    if-nez v1, :cond_0

    .line 140
    new-instance v1, Lcom/qf/skin/manager/loader/SkinManager;

    invoke-direct {v1}, Lcom/qf/skin/manager/loader/SkinManager;-><init>()V

    sput-object v1, Lcom/qf/skin/manager/loader/SkinManager;->instance:Lcom/qf/skin/manager/loader/SkinManager;

    .line 142
    :cond_0
    monitor-exit v0

    goto :goto_0

    :catchall_0
    move-exception v1

    monitor-exit v0
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    throw v1

    .line 144
    :cond_1
    :goto_0
    sget-object v0, Lcom/qf/skin/manager/loader/SkinManager;->instance:Lcom/qf/skin/manager/loader/SkinManager;

    return-object v0
.end method

.method private initResources()V
    .locals 4

    const-string v0, "initResources getCurrSkin: "

    .line 166
    invoke-static {v0}, Lcom/qf/skin/manager/util/L;->d(Ljava/lang/String;)V

    .line 167
    iget-object v0, p0, Lcom/qf/skin/manager/loader/SkinManager;->context:Landroid/content/Context;

    invoke-virtual {v0}, Landroid/content/Context;->getResources()Landroid/content/res/Resources;

    move-result-object v0

    .line 168
    new-instance v1, Lcom/qf/skin/manager/hooks/SkinResouces;

    iget-object v2, p0, Lcom/qf/skin/manager/loader/SkinManager;->context:Landroid/content/Context;

    invoke-virtual {v2}, Landroid/content/Context;->getAssets()Landroid/content/res/AssetManager;

    move-result-object v2

    invoke-virtual {v0}, Landroid/content/res/Resources;->getDisplayMetrics()Landroid/util/DisplayMetrics;

    move-result-object v3

    .line 169
    invoke-virtual {v0}, Landroid/content/res/Resources;->getConfiguration()Landroid/content/res/Configuration;

    move-result-object v0

    invoke-direct {v1, v2, v3, v0}, Lcom/qf/skin/manager/hooks/SkinResouces;-><init>(Landroid/content/res/AssetManager;Landroid/util/DisplayMetrics;Landroid/content/res/Configuration;)V

    .line 170
    iget-object v0, p0, Lcom/qf/skin/manager/loader/SkinManager;->context:Landroid/content/Context;

    invoke-virtual {v0}, Landroid/content/Context;->getPackageName()Ljava/lang/String;

    move-result-object v0

    iput-object v0, p0, Lcom/qf/skin/manager/loader/SkinManager;->skinPackageName:Ljava/lang/String;

    .line 171
    iput-object v1, p0, Lcom/qf/skin/manager/loader/SkinManager;->mResources:Landroid/content/res/Resources;

    .line 172
    iget-object v0, p0, Lcom/qf/skin/manager/loader/SkinManager;->context:Landroid/content/Context;

    invoke-static {v0, v1}, Lcom/qf/skin/manager/hooks/ResourcesHooks;->hookResources(Landroid/content/Context;Landroid/content/res/Resources;)V

    return-void
.end method

.method private notifyLoadCallbackFailed(Lcom/qf/skin/manager/exception/LoadException;)V
    .locals 2

    .line 118
    iget-object v0, p0, Lcom/qf/skin/manager/loader/SkinManager;->mainHandler:Landroid/os/Handler;

    new-instance v1, Lcom/qf/skin/manager/loader/SkinManager$3;

    invoke-direct {v1, p0, p1}, Lcom/qf/skin/manager/loader/SkinManager$3;-><init>(Lcom/qf/skin/manager/loader/SkinManager;Lcom/qf/skin/manager/exception/LoadException;)V

    invoke-virtual {v0, v1}, Landroid/os/Handler;->post(Ljava/lang/Runnable;)Z

    return-void
.end method

.method private notifyLoadCallbackStart()V
    .locals 2

    .line 104
    iget-object v0, p0, Lcom/qf/skin/manager/loader/SkinManager;->mainHandler:Landroid/os/Handler;

    new-instance v1, Lcom/qf/skin/manager/loader/SkinManager$2;

    invoke-direct {v1, p0}, Lcom/qf/skin/manager/loader/SkinManager$2;-><init>(Lcom/qf/skin/manager/loader/SkinManager;)V

    invoke-virtual {v0, v1}, Landroid/os/Handler;->post(Ljava/lang/Runnable;)Z

    return-void
.end method

.method private notifySkinUpdate(Z)V
    .locals 2

    .line 299
    iget-object v0, p0, Lcom/qf/skin/manager/loader/SkinManager;->skinObservers:Ljava/util/List;

    if-nez v0, :cond_0

    return-void

    .line 302
    :cond_0
    iget-object v0, p0, Lcom/qf/skin/manager/loader/SkinManager;->mainHandler:Landroid/os/Handler;

    iget-object v1, p0, Lcom/qf/skin/manager/loader/SkinManager;->delayUpdateSkin:Ljava/lang/Runnable;

    invoke-virtual {v0, v1}, Landroid/os/Handler;->removeCallbacks(Ljava/lang/Runnable;)V

    .line 303
    new-instance v0, Ljava/lang/StringBuilder;

    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    const-string v1, "notifySkinUpdate  fromObser="

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v0, p1}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    const-string v1, "TTT"

    invoke-static {v1, v0}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    if-nez p1, :cond_1

    .line 305
    iget-object v0, p0, Lcom/qf/skin/manager/loader/SkinManager;->skinObservers:Ljava/util/List;

    invoke-interface {v0}, Ljava/util/List;->iterator()Ljava/util/Iterator;

    move-result-object v0

    :goto_0
    invoke-interface {v0}, Ljava/util/Iterator;->hasNext()Z

    move-result v1

    if-eqz v1, :cond_1

    invoke-interface {v0}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v1

    check-cast v1, Lcom/qf/skin/manager/interfaces/ISkinUpdate;

    .line 306
    invoke-interface {v1, p1}, Lcom/qf/skin/manager/interfaces/ISkinUpdate;->onThemeUpdate(Z)V

    goto :goto_0

    .line 309
    :cond_1
    iget-object v0, p0, Lcom/qf/skin/manager/loader/SkinManager;->windowSkinObservers:Ljava/util/List;

    if-nez v0, :cond_2

    return-void

    .line 312
    :cond_2
    invoke-interface {v0}, Ljava/util/List;->iterator()Ljava/util/Iterator;

    move-result-object v0

    :goto_1
    invoke-interface {v0}, Ljava/util/Iterator;->hasNext()Z

    move-result v1

    if-eqz v1, :cond_3

    invoke-interface {v0}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v1

    check-cast v1, Lcom/qf/skin/manager/interfaces/ISkinUpdate;

    .line 313
    invoke-interface {v1, p1}, Lcom/qf/skin/manager/interfaces/ISkinUpdate;->onThemeUpdate(Z)V

    goto :goto_1

    :cond_3
    return-void
.end method

.method private preInfalteLayout()V
    .locals 4

    const-string v0, "preInfalteLayout--begin"

    .line 177
    invoke-static {v0}, Lcom/qf/skin/manager/util/L;->d(Ljava/lang/String;)V

    .line 178
    iget-object v0, p0, Lcom/qf/skin/manager/loader/SkinManager;->context:Landroid/content/Context;

    .line 179
    new-instance v1, Lcom/qf/skin/manager/loader/SkinLayoutInflater;

    invoke-direct {v1, v0}, Lcom/qf/skin/manager/loader/SkinLayoutInflater;-><init>(Landroid/content/Context;)V

    .line 180
    new-instance v0, Lcom/qf/skin/manager/loader/SkinInflaterFactory2;

    invoke-direct {v0}, Lcom/qf/skin/manager/loader/SkinInflaterFactory2;-><init>()V

    invoke-virtual {v1, v0}, Lcom/qf/skin/manager/loader/SkinLayoutInflater;->setFactory2(Landroid/view/LayoutInflater$Factory2;)V

    .line 181
    iget-object v0, p0, Lcom/qf/skin/manager/loader/SkinManager;->preInflateLayouts:Ljava/util/concurrent/CopyOnWriteArrayList;

    invoke-virtual {v0}, Ljava/util/concurrent/CopyOnWriteArrayList;->iterator()Ljava/util/Iterator;

    move-result-object v0

    :goto_0
    invoke-interface {v0}, Ljava/util/Iterator;->hasNext()Z

    move-result v2

    if-eqz v2, :cond_0

    invoke-interface {v0}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v2

    check-cast v2, Ljava/lang/Integer;

    invoke-virtual {v2}, Ljava/lang/Integer;->intValue()I

    move-result v2

    const/4 v3, 0x0

    .line 182
    invoke-virtual {v1, v2, v3}, Lcom/qf/skin/manager/loader/SkinLayoutInflater;->inflate(ILandroid/view/ViewGroup;)Landroid/view/View;

    goto :goto_0

    :cond_0
    const-string v0, "preInfalteLayout--end"

    .line 184
    invoke-static {v0}, Lcom/qf/skin/manager/util/L;->d(Ljava/lang/String;)V

    return-void
.end method


# virtual methods
.method public addSkinEnableView(Landroid/app/Activity;Landroid/view/View;[Ljava/lang/String;[I)V
    .locals 2

    .line 258
    invoke-virtual {p1}, Landroid/app/Activity;->getLayoutInflater()Landroid/view/LayoutInflater;

    move-result-object v0

    invoke-virtual {v0}, Landroid/view/LayoutInflater;->getFactory()Landroid/view/LayoutInflater$Factory;

    move-result-object v0

    .line 259
    instance-of v1, v0, Lcom/qf/skin/manager/loader/SkinInflaterFactory;

    if-eqz v1, :cond_0

    .line 260
    check-cast v0, Lcom/qf/skin/manager/loader/SkinInflaterFactory;

    invoke-virtual {v0, p1, p2, p3, p4}, Lcom/qf/skin/manager/loader/SkinInflaterFactory;->dynamicAddSkinEnableView(Landroid/content/Context;Landroid/view/View;[Ljava/lang/String;[I)V

    :cond_0
    return-void
.end method

.method public addSkinPreInflateLayout(I)V
    .locals 2

    .line 270
    iget-object v0, p0, Lcom/qf/skin/manager/loader/SkinManager;->preInflateLayouts:Ljava/util/concurrent/CopyOnWriteArrayList;

    invoke-static {p1}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v1

    invoke-virtual {v0, v1}, Ljava/util/concurrent/CopyOnWriteArrayList;->contains(Ljava/lang/Object;)Z

    move-result v0

    if-nez v0, :cond_0

    .line 271
    iget-object v0, p0, Lcom/qf/skin/manager/loader/SkinManager;->preInflateLayouts:Ljava/util/concurrent/CopyOnWriteArrayList;

    invoke-static {p1}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object p1

    invoke-virtual {v0, p1}, Ljava/util/concurrent/CopyOnWriteArrayList;->add(Ljava/lang/Object;)Z

    :cond_0
    return-void
.end method

.method public addSupportAttrName(Ljava/lang/String;Lcom/qf/skin/manager/entity/SkinAttr;)V
    .locals 1

    .line 230
    invoke-static {}, Lcom/qf/skin/manager/entity/AttrFactory;->getSupportAttr()Ljava/util/HashMap;

    move-result-object v0

    invoke-virtual {v0, p1, p2}, Ljava/util/HashMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

    return-void
.end method

.method public addSupportAttrName(Ljava/lang/String;Ljava/lang/String;)V
    .locals 2

    .line 241
    invoke-static {}, Lcom/qf/skin/manager/entity/AttrFactory;->getSupportAttr()Ljava/util/HashMap;

    move-result-object v0

    invoke-virtual {v0, p2}, Ljava/util/HashMap;->get(Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v0

    .line 242
    instance-of v1, v0, Lcom/qf/skin/manager/entity/SkinAttr;

    if-eqz v1, :cond_0

    .line 243
    invoke-static {}, Lcom/qf/skin/manager/entity/AttrFactory;->getSupportAttr()Ljava/util/HashMap;

    move-result-object p2

    invoke-virtual {p2, p1, v0}, Ljava/util/HashMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

    goto :goto_0

    .line 245
    :cond_0
    new-instance v0, Ljava/lang/StringBuilder;

    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    const-string v1, "attrName:"

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v0, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    const-string p1, "-->nameType:"

    invoke-virtual {v0, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v0, p2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    const-string p1, " can not find nameType:"

    invoke-virtual {v0, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v0, p2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p1

    invoke-static {p1}, Lcom/qf/skin/manager/util/L;->e(Ljava/lang/String;)V

    :goto_0
    return-void
.end method

.method public attach(Lcom/qf/skin/manager/interfaces/ISkinUpdate;)V
    .locals 1

    .line 194
    iget-object v0, p0, Lcom/qf/skin/manager/loader/SkinManager;->skinObservers:Ljava/util/List;

    if-nez v0, :cond_0

    .line 195
    new-instance v0, Ljava/util/ArrayList;

    invoke-direct {v0}, Ljava/util/ArrayList;-><init>()V

    iput-object v0, p0, Lcom/qf/skin/manager/loader/SkinManager;->skinObservers:Ljava/util/List;

    .line 197
    :cond_0
    iget-object v0, p0, Lcom/qf/skin/manager/loader/SkinManager;->skinObservers:Ljava/util/List;

    invoke-interface {v0, p1}, Ljava/util/List;->contains(Ljava/lang/Object;)Z

    move-result v0

    if-nez v0, :cond_1

    .line 198
    iget-object v0, p0, Lcom/qf/skin/manager/loader/SkinManager;->skinObservers:Ljava/util/List;

    invoke-interface {v0, p1}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    :cond_1
    return-void
.end method

.method public convertToColorStateList(I)Landroid/content/res/ColorStateList;
    .locals 5

    .line 354
    :try_start_0
    iget-object v0, p0, Lcom/qf/skin/manager/loader/SkinManager;->mResources:Landroid/content/res/Resources;

    invoke-virtual {v0, p1}, Landroid/content/res/Resources;->getColorStateList(I)Landroid/content/res/ColorStateList;

    move-result-object p1
    :try_end_0
    .catch Landroid/content/res/Resources$NotFoundException; {:try_start_0 .. :try_end_0} :catch_0

    return-object p1

    :catch_0
    move-exception v0

    .line 357
    invoke-virtual {v0}, Landroid/content/res/Resources$NotFoundException;->printStackTrace()V

    const/4 v0, 0x1

    .line 360
    filled-new-array {v0, v0}, [I

    move-result-object v1

    const-class v2, I

    invoke-static {v2, v1}, Ljava/lang/reflect/Array;->newInstance(Ljava/lang/Class;[I)Ljava/lang/Object;

    move-result-object v1

    check-cast v1, [[I

    .line 361
    new-instance v2, Landroid/content/res/ColorStateList;

    new-array v0, v0, [I

    const/4 v3, 0x0

    iget-object v4, p0, Lcom/qf/skin/manager/loader/SkinManager;->mResources:Landroid/content/res/Resources;

    invoke-virtual {v4, p1}, Landroid/content/res/Resources;->getColor(I)I

    move-result p1

    aput p1, v0, v3

    invoke-direct {v2, v1, v0}, Landroid/content/res/ColorStateList;-><init>([[I[I)V

    return-object v2
.end method

.method public detach(Lcom/qf/skin/manager/interfaces/ISkinUpdate;)V
    .locals 1

    .line 281
    iget-object v0, p0, Lcom/qf/skin/manager/loader/SkinManager;->skinObservers:Ljava/util/List;

    if-nez v0, :cond_0

    return-void

    .line 284
    :cond_0
    invoke-interface {v0, p1}, Ljava/util/List;->contains(Ljava/lang/Object;)Z

    move-result v0

    if-eqz v0, :cond_1

    .line 285
    iget-object v0, p0, Lcom/qf/skin/manager/loader/SkinManager;->skinObservers:Ljava/util/List;

    invoke-interface {v0, p1}, Ljava/util/List;->remove(Ljava/lang/Object;)Z

    :cond_1
    return-void
.end method

.method public getColor(I)I
    .locals 1

    .line 334
    iget-object v0, p0, Lcom/qf/skin/manager/loader/SkinManager;->mResources:Landroid/content/res/Resources;

    invoke-virtual {v0, p1}, Landroid/content/res/Resources;->getColor(I)I

    move-result p1

    return p1
.end method

.method public getDrawable(I)Landroid/graphics/drawable/Drawable;
    .locals 1

    .line 340
    iget-object v0, p0, Lcom/qf/skin/manager/loader/SkinManager;->mResources:Landroid/content/res/Resources;

    invoke-virtual {v0, p1}, Landroid/content/res/Resources;->getDrawable(I)Landroid/graphics/drawable/Drawable;

    move-result-object p1

    return-object p1
.end method

.method public getDrawableByResName(Ljava/lang/String;)Landroid/graphics/drawable/Drawable;
    .locals 1

    const-string v0, "drawable"

    .line 390
    invoke-virtual {p0, v0, p1}, Lcom/qf/skin/manager/loader/SkinManager;->getResourceIdByName(Ljava/lang/String;Ljava/lang/String;)I

    move-result p1

    .line 391
    iget-object v0, p0, Lcom/qf/skin/manager/loader/SkinManager;->mResources:Landroid/content/res/Resources;

    invoke-virtual {v0, p1}, Landroid/content/res/Resources;->getDrawable(I)Landroid/graphics/drawable/Drawable;

    move-result-object p1

    return-object p1
.end method

.method public getResourceIdByName(Ljava/lang/String;Ljava/lang/String;)I
    .locals 3

    if-eqz p2, :cond_0

    .line 404
    :try_start_0
    iget-object v0, p0, Lcom/qf/skin/manager/loader/SkinManager;->mResources:Landroid/content/res/Resources;

    iget-object v1, p0, Lcom/qf/skin/manager/loader/SkinManager;->skinPackageName:Ljava/lang/String;

    invoke-virtual {v0, p2, p1, v1}, Landroid/content/res/Resources;->getIdentifier(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)I

    move-result p1
    :try_end_0
    .catch Ljava/lang/Exception; {:try_start_0 .. :try_end_0} :catch_0

    return p1

    :catch_0
    move-exception v0

    .line 406
    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, " cant not find <type> "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    const-string p1, "  <resname> "

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1, p2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    const-string p1, " id"

    invoke-virtual {v1, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p1

    invoke-static {p1}, Lcom/qf/skin/manager/util/L;->d(Ljava/lang/String;)V

    .line 408
    invoke-virtual {v0}, Ljava/lang/Exception;->printStackTrace()V

    :cond_0
    const/4 p1, 0x0

    return p1
.end method

.method public getResources()Landroid/content/res/Resources;
    .locals 1

    .line 370
    iget-object v0, p0, Lcom/qf/skin/manager/loader/SkinManager;->mResources:Landroid/content/res/Resources;

    return-object v0
.end method

.method public getSkinPackageName()Ljava/lang/String;
    .locals 1

    .line 329
    iget-object v0, p0, Lcom/qf/skin/manager/loader/SkinManager;->skinPackageName:Ljava/lang/String;

    return-object v0
.end method

.method public getStringByResName(Ljava/lang/String;)Ljava/lang/String;
    .locals 1

    const-string v0, "string"

    .line 377
    invoke-virtual {p0, v0, p1}, Lcom/qf/skin/manager/loader/SkinManager;->getResourceIdByName(Ljava/lang/String;Ljava/lang/String;)I

    move-result v0

    if-gtz v0, :cond_0

    return-object p1

    .line 381
    :cond_0
    invoke-virtual {p0}, Lcom/qf/skin/manager/loader/SkinManager;->getResources()Landroid/content/res/Resources;

    move-result-object p1

    invoke-virtual {p1, v0}, Landroid/content/res/Resources;->getString(I)Ljava/lang/String;

    move-result-object p1

    return-object p1
.end method

.method public handlerNotifySkinUpdate()V
    .locals 4

    .line 318
    iget-object v0, p0, Lcom/qf/skin/manager/loader/SkinManager;->mainHandler:Landroid/os/Handler;

    iget-object v1, p0, Lcom/qf/skin/manager/loader/SkinManager;->delayUpdateSkin:Ljava/lang/Runnable;

    invoke-virtual {v0, v1}, Landroid/os/Handler;->removeCallbacks(Ljava/lang/Runnable;)V

    .line 319
    iget-object v0, p0, Lcom/qf/skin/manager/loader/SkinManager;->mainHandler:Landroid/os/Handler;

    iget-object v1, p0, Lcom/qf/skin/manager/loader/SkinManager;->delayUpdateSkin:Ljava/lang/Runnable;

    const-wide/16 v2, 0x1f4

    invoke-virtual {v0, v1, v2, v3}, Landroid/os/Handler;->postDelayed(Ljava/lang/Runnable;J)Z

    return-void
.end method

.method public init(Landroid/content/Context;Z)V
    .locals 0

    .line 157
    iput-object p1, p0, Lcom/qf/skin/manager/loader/SkinManager;->context:Landroid/content/Context;

    .line 158
    iput-boolean p2, p0, Lcom/qf/skin/manager/loader/SkinManager;->isPreloading:Z

    .line 159
    invoke-direct {p0}, Lcom/qf/skin/manager/loader/SkinManager;->initResources()V

    return-void
.end method

.method public notifySkinUpdate()V
    .locals 1

    const/4 v0, 0x0

    .line 294
    invoke-direct {p0, v0}, Lcom/qf/skin/manager/loader/SkinManager;->notifySkinUpdate(Z)V

    return-void
.end method

.method public windowAttach(Lcom/qf/skin/manager/interfaces/ISkinUpdate;)V
    .locals 1

    .line 203
    iget-object v0, p0, Lcom/qf/skin/manager/loader/SkinManager;->windowSkinObservers:Ljava/util/List;

    if-nez v0, :cond_0

    .line 204
    new-instance v0, Ljava/util/ArrayList;

    invoke-direct {v0}, Ljava/util/ArrayList;-><init>()V

    iput-object v0, p0, Lcom/qf/skin/manager/loader/SkinManager;->windowSkinObservers:Ljava/util/List;

    .line 206
    :cond_0
    iget-object v0, p0, Lcom/qf/skin/manager/loader/SkinManager;->windowSkinObservers:Ljava/util/List;

    invoke-interface {v0, p1}, Ljava/util/List;->contains(Ljava/lang/Object;)Z

    move-result v0

    if-nez v0, :cond_1

    .line 207
    iget-object v0, p0, Lcom/qf/skin/manager/loader/SkinManager;->windowSkinObservers:Ljava/util/List;

    invoke-interface {v0, p1}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    :cond_1
    return-void
.end method

.method public windowdetach(Lcom/qf/skin/manager/interfaces/ISkinUpdate;)V
    .locals 1

    .line 212
    iget-object v0, p0, Lcom/qf/skin/manager/loader/SkinManager;->windowSkinObservers:Ljava/util/List;

    if-nez v0, :cond_0

    return-void

    .line 215
    :cond_0
    invoke-interface {v0, p1}, Ljava/util/List;->contains(Ljava/lang/Object;)Z

    move-result v0

    if-eqz v0, :cond_1

    .line 216
    iget-object v0, p0, Lcom/qf/skin/manager/loader/SkinManager;->windowSkinObservers:Ljava/util/List;

    invoke-interface {v0, p1}, Ljava/util/List;->remove(Ljava/lang/Object;)Z

    :cond_1
    return-void
.end method
