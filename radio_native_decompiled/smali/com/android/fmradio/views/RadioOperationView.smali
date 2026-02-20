.class public Lcom/android/fmradio/views/RadioOperationView;
.super Landroid/widget/LinearLayout;
.source "RadioOperationView.java"

# interfaces
.implements Landroid/view/View$OnClickListener;


# static fields
.field private static final TAG:Ljava/lang/String;


# instance fields
.field private mActivity:Lcom/android/fmradio/FmMainActivity;

.field private mBtnAuto:Landroid/widget/ImageButton;

.field private mBtnBand:Landroid/widget/ImageButton;

.field private mBtnEQ:Landroid/widget/ImageButton;

.field private mBtnLoc:Landroid/widget/ImageButton;

.field private mBtnSeekDown:Landroid/widget/ImageButton;

.field private mBtnSeekUp:Landroid/widget/ImageButton;

.field private mContext:Landroid/content/Context;

.field private mHeight:I

.field private mScale:F


# direct methods
.method static constructor <clinit>()V
    .locals 1

    .line 23
    const-class v0, Lcom/android/fmradio/views/RadioOperationView;

    invoke-virtual {v0}, Ljava/lang/Class;->getSimpleName()Ljava/lang/String;

    move-result-object v0

    sput-object v0, Lcom/android/fmradio/views/RadioOperationView;->TAG:Ljava/lang/String;

    return-void
.end method

.method public constructor <init>(Landroid/content/Context;Landroid/util/AttributeSet;)V
    .locals 4

    .line 45
    invoke-direct {p0, p1, p2}, Landroid/widget/LinearLayout;-><init>(Landroid/content/Context;Landroid/util/AttributeSet;)V

    const/4 p2, 0x0

    .line 29
    iput-object p2, p0, Lcom/android/fmradio/views/RadioOperationView;->mBtnSeekDown:Landroid/widget/ImageButton;

    .line 30
    iput-object p2, p0, Lcom/android/fmradio/views/RadioOperationView;->mBtnAuto:Landroid/widget/ImageButton;

    .line 31
    iput-object p2, p0, Lcom/android/fmradio/views/RadioOperationView;->mBtnBand:Landroid/widget/ImageButton;

    .line 32
    iput-object p2, p0, Lcom/android/fmradio/views/RadioOperationView;->mBtnSeekUp:Landroid/widget/ImageButton;

    .line 33
    iput-object p2, p0, Lcom/android/fmradio/views/RadioOperationView;->mBtnLoc:Landroid/widget/ImageButton;

    .line 34
    iput-object p2, p0, Lcom/android/fmradio/views/RadioOperationView;->mBtnEQ:Landroid/widget/ImageButton;

    const/high16 v0, 0x3f800000    # 1.0f

    .line 36
    iput v0, p0, Lcom/android/fmradio/views/RadioOperationView;->mScale:F

    const/4 v0, 0x0

    .line 38
    iput v0, p0, Lcom/android/fmradio/views/RadioOperationView;->mHeight:I

    .line 47
    iput-object p1, p0, Lcom/android/fmradio/views/RadioOperationView;->mContext:Landroid/content/Context;

    .line 49
    instance-of v1, p1, Landroid/app/Activity;

    if-eqz v1, :cond_0

    .line 50
    move-object v1, p1

    check-cast v1, Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {v1}, Lcom/android/fmradio/FmMainActivity;->getScale()F

    move-result v1

    iput v1, p0, Lcom/android/fmradio/views/RadioOperationView;->mScale:F

    .line 51
    sget-object v1, Lcom/android/fmradio/views/RadioOperationView;->TAG:Ljava/lang/String;

    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2}, Ljava/lang/StringBuilder;-><init>()V

    const-string v3, "mScale: "

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget v3, p0, Lcom/android/fmradio/views/RadioOperationView;->mScale:F

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(F)Ljava/lang/StringBuilder;

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v2

    invoke-static {v1, v2}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 54
    :cond_0
    invoke-virtual {p1}, Landroid/content/Context;->getResources()Landroid/content/res/Resources;

    move-result-object v1

    const v2, 0x7f060081

    invoke-virtual {v1, v2}, Landroid/content/res/Resources;->getDimensionPixelSize(I)I

    move-result v1

    int-to-float v1, v1

    iget v2, p0, Lcom/android/fmradio/views/RadioOperationView;->mScale:F

    mul-float/2addr v1, v2

    float-to-int v1, v1

    .line 55
    iput v1, p0, Lcom/android/fmradio/views/RadioOperationView;->mHeight:I

    .line 57
    invoke-static {p1}, Landroid/view/LayoutInflater;->from(Landroid/content/Context;)Landroid/view/LayoutInflater;

    move-result-object p1

    const v2, 0x7f0b002d

    invoke-virtual {p1, v2, p2, v0}, Landroid/view/LayoutInflater;->inflate(ILandroid/view/ViewGroup;Z)Landroid/view/View;

    move-result-object p1

    .line 58
    new-instance p2, Landroid/widget/LinearLayout$LayoutParams;

    const/4 v0, -0x1

    invoke-direct {p2, v0, v1}, Landroid/widget/LinearLayout$LayoutParams;-><init>(II)V

    invoke-virtual {p1, p2}, Landroid/view/View;->setLayoutParams(Landroid/view/ViewGroup$LayoutParams;)V

    .line 60
    invoke-direct {p0, p1}, Lcom/android/fmradio/views/RadioOperationView;->initView(Landroid/view/View;)V

    .line 62
    invoke-virtual {p0, p1}, Lcom/android/fmradio/views/RadioOperationView;->addView(Landroid/view/View;)V

    return-void
.end method

.method static synthetic access$000(Lcom/android/fmradio/views/RadioOperationView;)Lcom/android/fmradio/FmMainActivity;
    .locals 0

    .line 21
    iget-object p0, p0, Lcom/android/fmradio/views/RadioOperationView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    return-object p0
.end method

.method static synthetic access$100(Lcom/android/fmradio/views/RadioOperationView;)F
    .locals 0

    .line 21
    iget p0, p0, Lcom/android/fmradio/views/RadioOperationView;->mScale:F

    return p0
.end method

.method static synthetic access$200(Lcom/android/fmradio/views/RadioOperationView;)I
    .locals 0

    .line 21
    iget p0, p0, Lcom/android/fmradio/views/RadioOperationView;->mHeight:I

    return p0
.end method

.method private initView(Landroid/view/View;)V
    .locals 1

    const v0, 0x7f080057

    .line 66
    invoke-virtual {p1, v0}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object v0

    check-cast v0, Landroid/widget/ImageButton;

    iput-object v0, p0, Lcom/android/fmradio/views/RadioOperationView;->mBtnSeekDown:Landroid/widget/ImageButton;

    const v0, 0x7f080058

    .line 67
    invoke-virtual {p1, v0}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object v0

    check-cast v0, Landroid/widget/ImageButton;

    iput-object v0, p0, Lcom/android/fmradio/views/RadioOperationView;->mBtnSeekUp:Landroid/widget/ImageButton;

    const v0, 0x7f080043

    .line 68
    invoke-virtual {p1, v0}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object v0

    check-cast v0, Landroid/widget/ImageButton;

    iput-object v0, p0, Lcom/android/fmradio/views/RadioOperationView;->mBtnAuto:Landroid/widget/ImageButton;

    const v0, 0x7f080044

    .line 69
    invoke-virtual {p1, v0}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object v0

    check-cast v0, Landroid/widget/ImageButton;

    iput-object v0, p0, Lcom/android/fmradio/views/RadioOperationView;->mBtnBand:Landroid/widget/ImageButton;

    const v0, 0x7f08004e

    .line 70
    invoke-virtual {p1, v0}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object v0

    check-cast v0, Landroid/widget/ImageButton;

    iput-object v0, p0, Lcom/android/fmradio/views/RadioOperationView;->mBtnLoc:Landroid/widget/ImageButton;

    const v0, 0x7f08004b

    .line 71
    invoke-virtual {p1, v0}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object p1

    check-cast p1, Landroid/widget/ImageButton;

    iput-object p1, p0, Lcom/android/fmradio/views/RadioOperationView;->mBtnEQ:Landroid/widget/ImageButton;

    .line 73
    iget-object p1, p0, Lcom/android/fmradio/views/RadioOperationView;->mBtnSeekDown:Landroid/widget/ImageButton;

    invoke-virtual {p1, p0}, Landroid/widget/ImageButton;->setOnClickListener(Landroid/view/View$OnClickListener;)V

    .line 74
    iget-object p1, p0, Lcom/android/fmradio/views/RadioOperationView;->mBtnAuto:Landroid/widget/ImageButton;

    invoke-virtual {p1, p0}, Landroid/widget/ImageButton;->setOnClickListener(Landroid/view/View$OnClickListener;)V

    .line 75
    iget-object p1, p0, Lcom/android/fmradio/views/RadioOperationView;->mBtnBand:Landroid/widget/ImageButton;

    invoke-virtual {p1, p0}, Landroid/widget/ImageButton;->setOnClickListener(Landroid/view/View$OnClickListener;)V

    .line 76
    iget-object p1, p0, Lcom/android/fmradio/views/RadioOperationView;->mBtnSeekUp:Landroid/widget/ImageButton;

    invoke-virtual {p1, p0}, Landroid/widget/ImageButton;->setOnClickListener(Landroid/view/View$OnClickListener;)V

    .line 77
    iget-object p1, p0, Lcom/android/fmradio/views/RadioOperationView;->mBtnLoc:Landroid/widget/ImageButton;

    invoke-virtual {p1, p0}, Landroid/widget/ImageButton;->setOnClickListener(Landroid/view/View$OnClickListener;)V

    .line 78
    iget-object p1, p0, Lcom/android/fmradio/views/RadioOperationView;->mBtnEQ:Landroid/widget/ImageButton;

    invoke-virtual {p1, p0}, Landroid/widget/ImageButton;->setOnClickListener(Landroid/view/View$OnClickListener;)V

    .line 80
    iget-object p1, p0, Lcom/android/fmradio/views/RadioOperationView;->mContext:Landroid/content/Context;

    check-cast p1, Landroid/app/Activity;

    invoke-virtual {p1}, Landroid/app/Activity;->isInMultiWindowMode()Z

    move-result p1

    if-nez p1, :cond_0

    .line 81
    iget-object p1, p0, Lcom/android/fmradio/views/RadioOperationView;->mBtnEQ:Landroid/widget/ImageButton;

    new-instance v0, Lcom/android/fmradio/views/RadioOperationView$1;

    invoke-direct {v0, p0}, Lcom/android/fmradio/views/RadioOperationView$1;-><init>(Lcom/android/fmradio/views/RadioOperationView;)V

    invoke-virtual {p1, v0}, Landroid/widget/ImageButton;->setOnLongClickListener(Landroid/view/View$OnLongClickListener;)V

    .line 105
    :cond_0
    invoke-virtual {p0}, Lcom/android/fmradio/views/RadioOperationView;->updateLocView()V

    return-void
.end method


# virtual methods
.method public onClick(Landroid/view/View;)V
    .locals 1

    .line 110
    invoke-static {}, Landroid/qf/os/QFApi;->isBTPhoneStartup()Z

    move-result v0

    if-eqz v0, :cond_0

    return-void

    .line 114
    :cond_0
    invoke-virtual {p1}, Landroid/view/View;->getId()I

    move-result p1

    const v0, 0x7f08004e

    if-eq p1, v0, :cond_1

    .line 115
    iget-object v0, p0, Lcom/android/fmradio/views/RadioOperationView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {v0}, Lcom/android/fmradio/FmMainActivity;->stopScan()Z

    move-result v0

    if-eqz v0, :cond_1

    return-void

    :cond_1
    sparse-switch p1, :sswitch_data_0

    .line 153
    sget-object p1, Lcom/android/fmradio/views/RadioOperationView;->TAG:Ljava/lang/String;

    const-string v0, "mButtonClickListener.onClick, invalid view id"

    invoke-static {p1, v0}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    goto :goto_0

    .line 127
    :sswitch_0
    sget-object p1, Lcom/android/fmradio/views/RadioOperationView;->TAG:Ljava/lang/String;

    const-string v0, "onClick - btn_seek_up"

    invoke-static {p1, v0}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 128
    iget-object p1, p0, Lcom/android/fmradio/views/RadioOperationView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    const/4 v0, 0x1

    invoke-virtual {p1, v0}, Lcom/android/fmradio/FmMainActivity;->onNextOrPreStation(Z)V

    goto :goto_0

    .line 121
    :sswitch_1
    sget-object p1, Lcom/android/fmradio/views/RadioOperationView;->TAG:Ljava/lang/String;

    const-string v0, "onClick - btn_seek_down"

    invoke-static {p1, v0}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 122
    iget-object p1, p0, Lcom/android/fmradio/views/RadioOperationView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    const/4 v0, 0x0

    invoke-virtual {p1, v0}, Lcom/android/fmradio/FmMainActivity;->onNextOrPreStation(Z)V

    goto :goto_0

    .line 143
    :sswitch_2
    sget-object p1, Lcom/android/fmradio/views/RadioOperationView;->TAG:Ljava/lang/String;

    const-string v0, "onClick - btn_loc"

    invoke-static {p1, v0}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 144
    iget-object p1, p0, Lcom/android/fmradio/views/RadioOperationView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {p1}, Lcom/android/fmradio/FmMainActivity;->onLoc()V

    goto :goto_0

    .line 148
    :sswitch_3
    sget-object p1, Lcom/android/fmradio/views/RadioOperationView;->TAG:Ljava/lang/String;

    const-string v0, "onClick - btn_eq"

    invoke-static {p1, v0}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 149
    iget-object p1, p0, Lcom/android/fmradio/views/RadioOperationView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {p1}, Lcom/android/fmradio/FmMainActivity;->onEQ()V

    goto :goto_0

    .line 133
    :sswitch_4
    sget-object p1, Lcom/android/fmradio/views/RadioOperationView;->TAG:Ljava/lang/String;

    const-string v0, "onClick - btn_band"

    invoke-static {p1, v0}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 134
    iget-object p1, p0, Lcom/android/fmradio/views/RadioOperationView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {p1}, Lcom/android/fmradio/FmMainActivity;->onBand()V

    goto :goto_0

    .line 138
    :sswitch_5
    sget-object p1, Lcom/android/fmradio/views/RadioOperationView;->TAG:Ljava/lang/String;

    const-string v0, "onClick - btn_auto"

    invoke-static {p1, v0}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 139
    iget-object p1, p0, Lcom/android/fmradio/views/RadioOperationView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    invoke-virtual {p1}, Lcom/android/fmradio/FmMainActivity;->onAuto()V

    :goto_0
    return-void

    :sswitch_data_0
    .sparse-switch
        0x7f080043 -> :sswitch_5
        0x7f080044 -> :sswitch_4
        0x7f08004b -> :sswitch_3
        0x7f08004e -> :sswitch_2
        0x7f080057 -> :sswitch_1
        0x7f080058 -> :sswitch_0
    .end sparse-switch
.end method

.method public setActivity(Lcom/android/fmradio/FmMainActivity;)V
    .locals 0

    .line 41
    iput-object p1, p0, Lcom/android/fmradio/views/RadioOperationView;->mActivity:Lcom/android/fmradio/FmMainActivity;

    return-void
.end method

.method public updateLocView()V
    .locals 2

    .line 160
    iget-object v0, p0, Lcom/android/fmradio/views/RadioOperationView;->mContext:Landroid/content/Context;

    invoke-static {v0}, Lcom/android/fmradio/database/FmStation;->getCurrentBand(Landroid/content/Context;)I

    move-result v0

    invoke-static {v0}, Lcom/android/fmradio/utils/FmUtils;->isFMBand(I)Z

    move-result v0

    if-eqz v0, :cond_0

    .line 161
    iget-object v0, p0, Lcom/android/fmradio/views/RadioOperationView;->mBtnLoc:Landroid/widget/ImageButton;

    const/4 v1, 0x1

    invoke-virtual {v0, v1}, Landroid/widget/ImageButton;->setEnabled(Z)V

    goto :goto_0

    .line 163
    :cond_0
    iget-object v0, p0, Lcom/android/fmradio/views/RadioOperationView;->mBtnLoc:Landroid/widget/ImageButton;

    const/4 v1, 0x0

    invoke-virtual {v0, v1}, Landroid/widget/ImageButton;->setEnabled(Z)V

    :goto_0
    return-void
.end method
