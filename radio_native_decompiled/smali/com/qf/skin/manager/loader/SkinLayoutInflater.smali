.class public Lcom/qf/skin/manager/loader/SkinLayoutInflater;
.super Landroid/view/LayoutInflater;
.source "SkinLayoutInflater.java"


# static fields
.field private static final sClassPrefixList:[Ljava/lang/String;


# direct methods
.method static constructor <clinit>()V
    .locals 3

    const-string v0, "android.widget."

    const-string v1, "android.webkit."

    const-string v2, "android.app."

    .line 18
    filled-new-array {v0, v1, v2}, [Ljava/lang/String;

    move-result-object v0

    sput-object v0, Lcom/qf/skin/manager/loader/SkinLayoutInflater;->sClassPrefixList:[Ljava/lang/String;

    return-void
.end method

.method public constructor <init>(Landroid/content/Context;)V
    .locals 0

    .line 25
    invoke-direct {p0, p1}, Landroid/view/LayoutInflater;-><init>(Landroid/content/Context;)V

    return-void
.end method

.method public constructor <init>(Landroid/view/LayoutInflater;Landroid/content/Context;)V
    .locals 0

    .line 29
    invoke-direct {p0, p1, p2}, Landroid/view/LayoutInflater;-><init>(Landroid/view/LayoutInflater;Landroid/content/Context;)V

    return-void
.end method


# virtual methods
.method public cloneInContext(Landroid/content/Context;)Landroid/view/LayoutInflater;
    .locals 1

    .line 50
    new-instance v0, Lcom/qf/skin/manager/loader/SkinLayoutInflater;

    invoke-direct {v0, p0, p1}, Lcom/qf/skin/manager/loader/SkinLayoutInflater;-><init>(Landroid/view/LayoutInflater;Landroid/content/Context;)V

    return-object v0
.end method

.method public inflate(Lorg/xmlpull/v1/XmlPullParser;Landroid/view/ViewGroup;Z)Landroid/view/View;
    .locals 1

    .line 56
    invoke-virtual {p0}, Lcom/qf/skin/manager/loader/SkinLayoutInflater;->getFactory2()Landroid/view/LayoutInflater$Factory2;

    move-result-object v0

    check-cast v0, Lcom/qf/skin/manager/loader/SkinInflaterFactory2;

    invoke-virtual {v0}, Lcom/qf/skin/manager/loader/SkinInflaterFactory2;->preInflate()V

    .line 57
    invoke-super {p0, p1, p2, p3}, Landroid/view/LayoutInflater;->inflate(Lorg/xmlpull/v1/XmlPullParser;Landroid/view/ViewGroup;Z)Landroid/view/View;

    move-result-object p1

    .line 58
    invoke-virtual {p0}, Lcom/qf/skin/manager/loader/SkinLayoutInflater;->getFactory2()Landroid/view/LayoutInflater$Factory2;

    move-result-object p2

    check-cast p2, Lcom/qf/skin/manager/loader/SkinInflaterFactory2;

    invoke-virtual {p2, p1}, Lcom/qf/skin/manager/loader/SkinInflaterFactory2;->completeInflate(Landroid/view/View;)V

    return-object p1
.end method

.method protected onCreateView(Ljava/lang/String;Landroid/util/AttributeSet;)Landroid/view/View;
    .locals 4
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Ljava/lang/ClassNotFoundException;
        }
    .end annotation

    .line 34
    sget-object v0, Lcom/qf/skin/manager/loader/SkinLayoutInflater;->sClassPrefixList:[Ljava/lang/String;

    array-length v1, v0

    const/4 v2, 0x0

    :goto_0
    if-ge v2, v1, :cond_1

    aget-object v3, v0, v2

    .line 36
    :try_start_0
    invoke-virtual {p0, p1, v3, p2}, Lcom/qf/skin/manager/loader/SkinLayoutInflater;->createView(Ljava/lang/String;Ljava/lang/String;Landroid/util/AttributeSet;)Landroid/view/View;

    move-result-object v3
    :try_end_0
    .catch Ljava/lang/ClassNotFoundException; {:try_start_0 .. :try_end_0} :catch_0

    if-eqz v3, :cond_0

    return-object v3

    :catch_0
    :cond_0
    add-int/lit8 v2, v2, 0x1

    goto :goto_0

    .line 45
    :cond_1
    invoke-super {p0, p1, p2}, Landroid/view/LayoutInflater;->onCreateView(Ljava/lang/String;Landroid/util/AttributeSet;)Landroid/view/View;

    move-result-object p1

    return-object p1
.end method
