.class Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;
.super Lcom/qf/clientsdk/listeners/TunerObserver;
.source "TunerManagerForExt.java"


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Lcom/android/fmradio/TunerManagerForExt;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x2
    name = "UITunerObserver"
.end annotation


# instance fields
.field final synthetic this$0:Lcom/android/fmradio/TunerManagerForExt;


# direct methods
.method private constructor <init>(Lcom/android/fmradio/TunerManagerForExt;)V
    .locals 0

    .line 69
    iput-object p1, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    invoke-direct {p0}, Lcom/qf/clientsdk/listeners/TunerObserver;-><init>()V

    return-void
.end method

.method synthetic constructor <init>(Lcom/android/fmradio/TunerManagerForExt;Lcom/android/fmradio/TunerManagerForExt$1;)V
    .locals 0

    .line 69
    invoke-direct {p0, p1}, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;-><init>(Lcom/android/fmradio/TunerManagerForExt;)V

    return-void
.end method


# virtual methods
.method public onTuneRdsIndicateInfo([B)V
    .locals 2

    const/4 v0, 0x1

    .line 188
    aget-byte p1, p1, v0

    .line 191
    iget-object v0, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    and-int/lit8 v1, p1, 0x4

    shr-int/lit8 v1, v1, 0x2

    invoke-static {v0, v1}, Lcom/android/fmradio/TunerManagerForExt;->access$1402(Lcom/android/fmradio/TunerManagerForExt;I)I

    .line 193
    new-instance v0, Ljava/lang/StringBuilder;

    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    const-string v1, "onTuneRdsIndicateInfo - flag: "

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v0, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string p1, " - mRdsSwitch: "

    invoke-virtual {v0, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-object p1, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    .line 194
    invoke-static {p1}, Lcom/android/fmradio/TunerManagerForExt;->access$1400(Lcom/android/fmradio/TunerManagerForExt;)I

    move-result p1

    invoke-virtual {v0, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p1

    .line 195
    invoke-static {}, Lcom/android/fmradio/TunerManagerForExt;->access$600()Ljava/lang/String;

    move-result-object v0

    invoke-static {v0, p1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 197
    iget-object p1, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    invoke-static {p1}, Lcom/android/fmradio/TunerManagerForExt;->access$1400(Lcom/android/fmradio/TunerManagerForExt;)I

    move-result p1

    if-nez p1, :cond_0

    .line 198
    iget-object p1, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    const-string v0, ""

    invoke-static {p1, v0}, Lcom/android/fmradio/TunerManagerForExt;->access$1502(Lcom/android/fmradio/TunerManagerForExt;Ljava/lang/String;)Ljava/lang/String;

    .line 199
    iget-object p1, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    invoke-static {p1}, Lcom/android/fmradio/TunerManagerForExt;->access$100(Lcom/android/fmradio/TunerManagerForExt;)Landroid/os/Handler;

    move-result-object p1

    const/16 v0, 0x3b

    invoke-virtual {p1, v0}, Landroid/os/Handler;->removeMessages(I)V

    .line 200
    iget-object p1, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    invoke-static {p1}, Lcom/android/fmradio/TunerManagerForExt;->access$100(Lcom/android/fmradio/TunerManagerForExt;)Landroid/os/Handler;

    move-result-object p1

    invoke-virtual {p1, v0}, Landroid/os/Handler;->obtainMessage(I)Landroid/os/Message;

    move-result-object p1

    .line 201
    iget-object v0, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    invoke-static {v0}, Lcom/android/fmradio/TunerManagerForExt;->access$100(Lcom/android/fmradio/TunerManagerForExt;)Landroid/os/Handler;

    move-result-object v0

    invoke-virtual {v0, p1}, Landroid/os/Handler;->sendMessage(Landroid/os/Message;)Z

    .line 203
    iget-object p1, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    const-string v0, "None"

    invoke-static {p1, v0}, Lcom/android/fmradio/TunerManagerForExt;->access$1602(Lcom/android/fmradio/TunerManagerForExt;Ljava/lang/String;)Ljava/lang/String;

    .line 204
    iget-object p1, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    invoke-static {p1}, Lcom/android/fmradio/TunerManagerForExt;->access$100(Lcom/android/fmradio/TunerManagerForExt;)Landroid/os/Handler;

    move-result-object p1

    const/16 v0, 0x3c

    invoke-virtual {p1, v0}, Landroid/os/Handler;->removeMessages(I)V

    .line 205
    iget-object p1, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    invoke-static {p1}, Lcom/android/fmradio/TunerManagerForExt;->access$100(Lcom/android/fmradio/TunerManagerForExt;)Landroid/os/Handler;

    move-result-object p1

    invoke-virtual {p1, v0}, Landroid/os/Handler;->obtainMessage(I)Landroid/os/Message;

    move-result-object p1

    .line 206
    iget-object v0, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    invoke-static {v0}, Lcom/android/fmradio/TunerManagerForExt;->access$100(Lcom/android/fmradio/TunerManagerForExt;)Landroid/os/Handler;

    move-result-object v0

    invoke-virtual {v0, p1}, Landroid/os/Handler;->sendMessage(Landroid/os/Message;)Z

    :cond_0
    return-void
.end method

.method public onTuneRdsInfo([B)V
    .locals 3

    const/4 v0, 0x1

    .line 161
    aget-byte p1, p1, v0

    .line 164
    iget-object v1, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    and-int/lit8 v2, p1, 0x1

    invoke-static {v1, v2}, Lcom/android/fmradio/TunerManagerForExt;->access$1202(Lcom/android/fmradio/TunerManagerForExt;I)I

    .line 166
    iget-object v1, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    and-int/lit8 v2, p1, 0x2

    shr-int/lit8 v0, v2, 0x1

    invoke-static {v1, v0}, Lcom/android/fmradio/TunerManagerForExt;->access$1302(Lcom/android/fmradio/TunerManagerForExt;I)I

    .line 169
    new-instance v0, Ljava/lang/StringBuilder;

    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    iget-object v1, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    invoke-static {v1}, Lcom/android/fmradio/TunerManagerForExt;->access$1200(Lcom/android/fmradio/TunerManagerForExt;)I

    move-result v1

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v1, "-"

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-object v1, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    invoke-static {v1}, Lcom/android/fmradio/TunerManagerForExt;->access$1300(Lcom/android/fmradio/TunerManagerForExt;)I

    move-result v1

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    const-string v1, "persist.sys.rds_info"

    invoke-static {v1, v0}, Landroid/os/SystemProperties;->set(Ljava/lang/String;Ljava/lang/String;)V

    .line 171
    new-instance v0, Ljava/lang/StringBuilder;

    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    const-string v1, "onTuneRdsInfo - flag: "

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v0, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string p1, " - mRdsAFSwitch: "

    invoke-virtual {v0, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-object p1, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    .line 172
    invoke-static {p1}, Lcom/android/fmradio/TunerManagerForExt;->access$1200(Lcom/android/fmradio/TunerManagerForExt;)I

    move-result p1

    invoke-virtual {v0, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string p1, " - mRdsTASwitch: "

    invoke-virtual {v0, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-object p1, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    .line 173
    invoke-static {p1}, Lcom/android/fmradio/TunerManagerForExt;->access$1300(Lcom/android/fmradio/TunerManagerForExt;)I

    move-result p1

    invoke-virtual {v0, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p1

    .line 174
    invoke-static {}, Lcom/android/fmradio/TunerManagerForExt;->access$600()Ljava/lang/String;

    move-result-object v0

    invoke-static {v0, p1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 176
    iget-object p1, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    invoke-static {p1}, Lcom/android/fmradio/TunerManagerForExt;->access$100(Lcom/android/fmradio/TunerManagerForExt;)Landroid/os/Handler;

    move-result-object p1

    const/16 v0, 0x39

    invoke-virtual {p1, v0}, Landroid/os/Handler;->removeMessages(I)V

    .line 177
    iget-object p1, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    invoke-static {p1}, Lcom/android/fmradio/TunerManagerForExt;->access$100(Lcom/android/fmradio/TunerManagerForExt;)Landroid/os/Handler;

    move-result-object p1

    invoke-virtual {p1, v0}, Landroid/os/Handler;->obtainMessage(I)Landroid/os/Message;

    move-result-object p1

    .line 178
    iget-object v0, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    invoke-static {v0}, Lcom/android/fmradio/TunerManagerForExt;->access$100(Lcom/android/fmradio/TunerManagerForExt;)Landroid/os/Handler;

    move-result-object v0

    invoke-virtual {v0, p1}, Landroid/os/Handler;->sendMessage(Landroid/os/Message;)Z

    return-void
.end method

.method public onTuneRdsPSInfo([B)V
    .locals 5

    .line 234
    invoke-static {}, Lcom/android/fmradio/TunerManagerForExt;->access$600()Ljava/lang/String;

    move-result-object v0

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "onTuneRdsPSInfo - len: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    array-length v2, p1

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v2, " - data: "

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-static {p1}, Lcom/android/fmradio/utils/FmUtils;->ByteToString([B)Ljava/lang/String;

    move-result-object v2

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    const/16 v0, 0x8

    new-array v1, v0, [B

    const/4 v2, 0x0

    const/4 v3, 0x1

    .line 237
    invoke-static {p1, v3, v1, v2, v0}, Ljava/lang/System;->arraycopy(Ljava/lang/Object;ILjava/lang/Object;II)V

    move p1, v2

    :goto_0
    if-ge v2, v0, :cond_3

    .line 242
    aget-byte v3, v1, v2

    const/16 v4, 0x2c

    if-eq v3, v4, :cond_2

    aget-byte v3, v1, v2

    const/16 v4, 0x23

    if-ne v3, v4, :cond_0

    goto :goto_1

    .line 247
    :cond_0
    aget-byte v3, v1, v2

    const/16 v4, 0x20

    if-gt v3, v4, :cond_1

    add-int/lit8 p1, p1, 0x1

    :cond_1
    add-int/lit8 v2, v2, 0x1

    goto :goto_0

    .line 243
    :cond_2
    :goto_1
    invoke-static {}, Lcom/android/fmradio/TunerManagerForExt;->access$600()Ljava/lang/String;

    move-result-object p1

    const-string v0, "onTuneRdsPSInfo - has char 0x2c(,) or 0x23(#)"

    invoke-static {p1, v0}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    return-void

    :cond_3
    if-ne p1, v0, :cond_4

    .line 253
    invoke-static {}, Lcom/android/fmradio/TunerManagerForExt;->access$600()Ljava/lang/String;

    move-result-object p1

    const-string v0, "onTuneRdsPSInfo - all char is 0x20"

    invoke-static {p1, v0}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    return-void

    .line 257
    :cond_4
    iget-object p1, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    new-instance v0, Ljava/lang/String;

    invoke-direct {v0, v1}, Ljava/lang/String;-><init>([B)V

    invoke-static {p1, v0}, Lcom/android/fmradio/TunerManagerForExt;->access$1502(Lcom/android/fmradio/TunerManagerForExt;Ljava/lang/String;)Ljava/lang/String;

    .line 258
    invoke-static {}, Lcom/android/fmradio/TunerManagerForExt;->access$600()Ljava/lang/String;

    move-result-object p1

    new-instance v0, Ljava/lang/StringBuilder;

    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    const-string v1, "onTuneRdsPSInfo - mRdsPsInfo: "

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-object v1, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    invoke-static {v1}, Lcom/android/fmradio/TunerManagerForExt;->access$1500(Lcom/android/fmradio/TunerManagerForExt;)Ljava/lang/String;

    move-result-object v1

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    invoke-static {p1, v0}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 260
    iget-object p1, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    invoke-static {p1}, Lcom/android/fmradio/TunerManagerForExt;->access$100(Lcom/android/fmradio/TunerManagerForExt;)Landroid/os/Handler;

    move-result-object p1

    const/16 v0, 0x3b

    invoke-virtual {p1, v0}, Landroid/os/Handler;->removeMessages(I)V

    .line 261
    iget-object p1, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    invoke-static {p1}, Lcom/android/fmradio/TunerManagerForExt;->access$100(Lcom/android/fmradio/TunerManagerForExt;)Landroid/os/Handler;

    move-result-object p1

    invoke-virtual {p1, v0}, Landroid/os/Handler;->obtainMessage(I)Landroid/os/Message;

    move-result-object p1

    .line 262
    iget-object v0, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    invoke-static {v0}, Lcom/android/fmradio/TunerManagerForExt;->access$100(Lcom/android/fmradio/TunerManagerForExt;)Landroid/os/Handler;

    move-result-object v0

    invoke-virtual {v0, p1}, Landroid/os/Handler;->sendMessage(Landroid/os/Message;)Z

    return-void
.end method

.method public onTuneRdsPtyTypeInfo([B)V
    .locals 2

    .line 217
    iget-object v0, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    const/4 v1, 0x1

    aget-byte p1, p1, v1

    and-int/lit16 p1, p1, 0xff

    invoke-static {v0, p1}, Lcom/android/fmradio/TunerManagerForExt;->access$1702(Lcom/android/fmradio/TunerManagerForExt;I)I

    .line 219
    new-instance p1, Ljava/lang/StringBuilder;

    invoke-direct {p1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v0, "onTuneRdsPtyTypeInfo - mRdsPtyType: "

    invoke-virtual {p1, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-object v0, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    invoke-static {v0}, Lcom/android/fmradio/TunerManagerForExt;->access$1700(Lcom/android/fmradio/TunerManagerForExt;)I

    move-result v0

    invoke-virtual {p1, v0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {p1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p1

    .line 220
    invoke-static {}, Lcom/android/fmradio/TunerManagerForExt;->access$600()Ljava/lang/String;

    move-result-object v0

    invoke-static {v0, p1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 222
    iget-object p1, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    invoke-static {p1}, Lcom/android/fmradio/TunerManagerForExt;->access$100(Lcom/android/fmradio/TunerManagerForExt;)Landroid/os/Handler;

    move-result-object p1

    const/16 v0, 0x3a

    invoke-virtual {p1, v0}, Landroid/os/Handler;->removeMessages(I)V

    .line 223
    iget-object p1, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    invoke-static {p1}, Lcom/android/fmradio/TunerManagerForExt;->access$100(Lcom/android/fmradio/TunerManagerForExt;)Landroid/os/Handler;

    move-result-object p1

    invoke-virtual {p1, v0}, Landroid/os/Handler;->obtainMessage(I)Landroid/os/Message;

    move-result-object p1

    .line 224
    iget-object v0, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    invoke-static {v0}, Lcom/android/fmradio/TunerManagerForExt;->access$100(Lcom/android/fmradio/TunerManagerForExt;)Landroid/os/Handler;

    move-result-object v0

    invoke-virtual {v0, p1}, Landroid/os/Handler;->sendMessage(Landroid/os/Message;)Z

    return-void
.end method

.method public onTuneRdsRTInfo([B)V
    .locals 4

    .line 272
    array-length v0, p1

    add-int/lit8 v0, v0, -0x3

    new-array v0, v0, [B

    .line 273
    invoke-static {}, Lcom/android/fmradio/TunerManagerForExt;->access$600()Ljava/lang/String;

    move-result-object v1

    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2}, Ljava/lang/StringBuilder;-><init>()V

    const-string v3, "onTuneRdsRTInfo - len: "

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    array-length v3, p1

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v3, " - data: "

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-static {p1}, Lcom/android/fmradio/utils/FmUtils;->ByteToString([B)Ljava/lang/String;

    move-result-object v3

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v2

    invoke-static {v1, v2}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 274
    array-length v1, p1

    add-int/lit8 v1, v1, -0x3

    const/4 v2, 0x1

    const/4 v3, 0x0

    invoke-static {p1, v2, v0, v3, v1}, Ljava/lang/System;->arraycopy(Ljava/lang/Object;ILjava/lang/Object;II)V

    .line 275
    iget-object p1, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    new-instance v1, Ljava/lang/String;

    invoke-direct {v1, v0}, Ljava/lang/String;-><init>([B)V

    invoke-static {p1, v1}, Lcom/android/fmradio/TunerManagerForExt;->access$1602(Lcom/android/fmradio/TunerManagerForExt;Ljava/lang/String;)Ljava/lang/String;

    .line 276
    invoke-static {}, Lcom/android/fmradio/TunerManagerForExt;->access$600()Ljava/lang/String;

    move-result-object p1

    new-instance v0, Ljava/lang/StringBuilder;

    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    const-string v1, "onTuneRdsRTInfo - mRdsRTInfo: "

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-object v1, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    invoke-static {v1}, Lcom/android/fmradio/TunerManagerForExt;->access$1600(Lcom/android/fmradio/TunerManagerForExt;)Ljava/lang/String;

    move-result-object v1

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    invoke-static {p1, v0}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 278
    iget-object p1, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    invoke-static {p1}, Lcom/android/fmradio/TunerManagerForExt;->access$100(Lcom/android/fmradio/TunerManagerForExt;)Landroid/os/Handler;

    move-result-object p1

    const/16 v0, 0x3c

    invoke-virtual {p1, v0}, Landroid/os/Handler;->removeMessages(I)V

    .line 279
    iget-object p1, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    invoke-static {p1}, Lcom/android/fmradio/TunerManagerForExt;->access$100(Lcom/android/fmradio/TunerManagerForExt;)Landroid/os/Handler;

    move-result-object p1

    invoke-virtual {p1, v0}, Landroid/os/Handler;->obtainMessage(I)Landroid/os/Message;

    move-result-object p1

    .line 280
    iget-object v0, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    invoke-static {v0}, Lcom/android/fmradio/TunerManagerForExt;->access$100(Lcom/android/fmradio/TunerManagerForExt;)Landroid/os/Handler;

    move-result-object v0

    invoke-virtual {v0, p1}, Landroid/os/Handler;->sendMessage(Landroid/os/Message;)Z

    return-void
.end method

.method public onTunerInfoChanged([B)V
    .locals 8

    const/4 v0, 0x1

    .line 85
    aget-byte p1, p1, v0

    and-int/lit8 v1, p1, 0x1

    and-int/lit8 v2, p1, 0x2

    shr-int/2addr v2, v0

    and-int/lit8 v3, p1, 0x4

    shr-int/lit8 v3, v3, 0x2

    and-int/lit8 v4, p1, 0x8

    shr-int/lit8 v4, v4, 0x3

    .line 95
    iget-object v5, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    and-int/lit8 v6, p1, 0x10

    shr-int/lit8 v6, v6, 0x4

    invoke-static {v5, v6}, Lcom/android/fmradio/TunerManagerForExt;->access$202(Lcom/android/fmradio/TunerManagerForExt;I)I

    .line 97
    iget-object v5, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    and-int/lit8 v6, p1, 0x20

    shr-int/lit8 v6, v6, 0x5

    invoke-static {v5, v6}, Lcom/android/fmradio/TunerManagerForExt;->access$302(Lcom/android/fmradio/TunerManagerForExt;I)I

    .line 99
    new-instance v5, Ljava/lang/StringBuilder;

    invoke-direct {v5}, Ljava/lang/StringBuilder;-><init>()V

    const-string v6, "onTunerInfoChanged - flag: "

    invoke-virtual {v5, v6}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v5, p1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string p1, " - asFlag: "

    invoke-virtual {v5, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v5, v1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v6, " - psFlag: "

    invoke-virtual {v5, v6}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v5, v2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v2, " - scanFlag: "

    invoke-virtual {v5, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v5, v3}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v2, " - seekFlag: "

    invoke-virtual {v5, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v5, v4}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v3, " - mStFlag: "

    invoke-virtual {v5, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-object v3, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    .line 104
    invoke-static {v3}, Lcom/android/fmradio/TunerManagerForExt;->access$200(Lcom/android/fmradio/TunerManagerForExt;)I

    move-result v3

    invoke-virtual {v5, v3}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v3, " - mLocFlag: "

    invoke-virtual {v5, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-object v3, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    .line 105
    invoke-static {v3}, Lcom/android/fmradio/TunerManagerForExt;->access$300(Lcom/android/fmradio/TunerManagerForExt;)I

    move-result v3

    invoke-virtual {v5, v3}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v3, " - mIsSearching: "

    invoke-virtual {v5, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-object v3, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    .line 106
    invoke-static {v3}, Lcom/android/fmradio/TunerManagerForExt;->access$400(Lcom/android/fmradio/TunerManagerForExt;)Z

    move-result v3

    invoke-virtual {v5, v3}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    const-string v3, " - mTempSearching: "

    invoke-virtual {v5, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-object v6, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    .line 107
    invoke-static {v6}, Lcom/android/fmradio/TunerManagerForExt;->access$500(Lcom/android/fmradio/TunerManagerForExt;)Z

    move-result v6

    invoke-virtual {v5, v6}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    invoke-virtual {v5}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v5

    .line 108
    invoke-static {}, Lcom/android/fmradio/TunerManagerForExt;->access$600()Ljava/lang/String;

    move-result-object v6

    invoke-static {v6, v5}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 110
    iget-object v5, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    const/4 v6, 0x0

    if-eq v1, v0, :cond_1

    if-ne v4, v0, :cond_0

    goto :goto_0

    :cond_0
    move v7, v6

    goto :goto_1

    :cond_1
    :goto_0
    move v7, v0

    :goto_1
    invoke-static {v5, v7}, Lcom/android/fmradio/TunerManagerForExt;->access$402(Lcom/android/fmradio/TunerManagerForExt;Z)Z

    if-ne v1, v0, :cond_2

    .line 111
    iget-object v5, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    invoke-static {v5}, Lcom/android/fmradio/TunerManagerForExt;->access$500(Lcom/android/fmradio/TunerManagerForExt;)Z

    move-result v5

    if-nez v5, :cond_2

    .line 112
    iget-object v5, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    invoke-static {v5, v0}, Lcom/android/fmradio/TunerManagerForExt;->access$502(Lcom/android/fmradio/TunerManagerForExt;Z)Z

    goto :goto_2

    :cond_2
    if-nez v1, :cond_3

    .line 113
    iget-object v0, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    invoke-static {v0}, Lcom/android/fmradio/TunerManagerForExt;->access$500(Lcom/android/fmradio/TunerManagerForExt;)Z

    move-result v0

    if-eqz v0, :cond_3

    .line 114
    iget-object v0, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    invoke-static {v0, v6}, Lcom/android/fmradio/TunerManagerForExt;->access$502(Lcom/android/fmradio/TunerManagerForExt;Z)Z

    .line 115
    iget-object v0, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    invoke-static {v0}, Lcom/android/fmradio/TunerManagerForExt;->access$100(Lcom/android/fmradio/TunerManagerForExt;)Landroid/os/Handler;

    move-result-object v0

    const/16 v5, 0x33

    invoke-virtual {v0, v5}, Landroid/os/Handler;->obtainMessage(I)Landroid/os/Message;

    move-result-object v0

    .line 116
    iget-object v5, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    invoke-static {v5}, Lcom/android/fmradio/TunerManagerForExt;->access$100(Lcom/android/fmradio/TunerManagerForExt;)Landroid/os/Handler;

    move-result-object v5

    invoke-virtual {v5, v0}, Landroid/os/Handler;->sendMessage(Landroid/os/Message;)Z

    .line 118
    :cond_3
    :goto_2
    invoke-static {}, Lcom/android/fmradio/TunerManagerForExt;->access$600()Ljava/lang/String;

    move-result-object v0

    new-instance v5, Ljava/lang/StringBuilder;

    invoke-direct {v5}, Ljava/lang/StringBuilder;-><init>()V

    const-string v6, "mIsSearching: "

    invoke-virtual {v5, v6}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-object v6, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    invoke-static {v6}, Lcom/android/fmradio/TunerManagerForExt;->access$400(Lcom/android/fmradio/TunerManagerForExt;)Z

    move-result v6

    invoke-virtual {v5, v6}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    invoke-virtual {v5, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-object v3, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    .line 119
    invoke-static {v3}, Lcom/android/fmradio/TunerManagerForExt;->access$500(Lcom/android/fmradio/TunerManagerForExt;)Z

    move-result v3

    invoke-virtual {v5, v3}, Ljava/lang/StringBuilder;->append(Z)Ljava/lang/StringBuilder;

    invoke-virtual {v5, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v5, v1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v5, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v5, v4}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v5}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p1

    .line 118
    invoke-static {v0, p1}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 123
    iget-object p1, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    invoke-static {p1}, Lcom/android/fmradio/TunerManagerForExt;->access$100(Lcom/android/fmradio/TunerManagerForExt;)Landroid/os/Handler;

    move-result-object p1

    const/16 v0, 0x37

    invoke-virtual {p1, v0}, Landroid/os/Handler;->removeMessages(I)V

    .line 124
    iget-object p1, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    invoke-static {p1}, Lcom/android/fmradio/TunerManagerForExt;->access$100(Lcom/android/fmradio/TunerManagerForExt;)Landroid/os/Handler;

    move-result-object p1

    invoke-virtual {p1, v0}, Landroid/os/Handler;->obtainMessage(I)Landroid/os/Message;

    move-result-object p1

    .line 125
    iget-object v0, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    invoke-static {v0}, Lcom/android/fmradio/TunerManagerForExt;->access$100(Lcom/android/fmradio/TunerManagerForExt;)Landroid/os/Handler;

    move-result-object v0

    invoke-virtual {v0, p1}, Landroid/os/Handler;->sendMessage(Landroid/os/Message;)Z

    return-void
.end method

.method public onTunerPresetListChanged([B)V
    .locals 9

    .line 130
    iget-object v0, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    const/4 v1, 0x1

    aget-byte v2, p1, v1

    and-int/lit16 v2, v2, 0xff

    invoke-static {v0, v2}, Lcom/android/fmradio/TunerManagerForExt;->access$702(Lcom/android/fmradio/TunerManagerForExt;I)I

    .line 131
    iget-object v0, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    invoke-static {v0}, Lcom/android/fmradio/TunerManagerForExt;->access$800(Lcom/android/fmradio/TunerManagerForExt;)Landroid/content/Context;

    move-result-object v0

    iget-object v2, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    invoke-static {v2}, Lcom/android/fmradio/TunerManagerForExt;->access$700(Lcom/android/fmradio/TunerManagerForExt;)I

    move-result v2

    invoke-static {v0, v2}, Lcom/android/fmradio/database/FmStation;->setCurrentBand(Landroid/content/Context;I)V

    .line 133
    iget-object v0, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    const/4 v2, 0x2

    aget-byte v3, p1, v2

    and-int/lit16 v3, v3, 0xff

    invoke-static {v0, v3}, Lcom/android/fmradio/TunerManagerForExt;->access$902(Lcom/android/fmradio/TunerManagerForExt;I)I

    .line 135
    iget-object v0, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    new-array v3, v2, [B

    const/4 v4, 0x4

    aget-byte v4, p1, v4

    const/4 v5, 0x0

    aput-byte v4, v3, v5

    const/4 v4, 0x3

    aget-byte v4, p1, v4

    aput-byte v4, v3, v1

    invoke-static {v3}, Lcom/android/fmradio/utils/FmUtils;->bytes2Int([B)I

    move-result v3

    invoke-static {v0, v3}, Lcom/android/fmradio/TunerManagerForExt;->access$1002(Lcom/android/fmradio/TunerManagerForExt;I)I

    .line 136
    iget-object v0, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    invoke-static {v0}, Lcom/android/fmradio/TunerManagerForExt;->access$800(Lcom/android/fmradio/TunerManagerForExt;)Landroid/content/Context;

    move-result-object v0

    iget-object v3, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    invoke-static {v3}, Lcom/android/fmradio/TunerManagerForExt;->access$1000(Lcom/android/fmradio/TunerManagerForExt;)I

    move-result v3

    invoke-static {v0, v3}, Lcom/android/fmradio/database/FmStation;->setCurrentStation(Landroid/content/Context;I)V

    .line 138
    new-instance v0, Ljava/lang/StringBuilder;

    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    const-string v3, " - mBand: "

    invoke-virtual {v0, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-object v3, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    .line 139
    invoke-static {v3}, Lcom/android/fmradio/TunerManagerForExt;->access$700(Lcom/android/fmradio/TunerManagerForExt;)I

    move-result v3

    invoke-virtual {v0, v3}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v3, " - mPresetIndex: "

    invoke-virtual {v0, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-object v3, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    .line 140
    invoke-static {v3}, Lcom/android/fmradio/TunerManagerForExt;->access$900(Lcom/android/fmradio/TunerManagerForExt;)I

    move-result v3

    invoke-virtual {v0, v3}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v3, " - mFrequency: "

    invoke-virtual {v0, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-object v3, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    .line 141
    invoke-static {v3}, Lcom/android/fmradio/TunerManagerForExt;->access$1000(Lcom/android/fmradio/TunerManagerForExt;)I

    move-result v3

    invoke-virtual {v0, v3}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    move-object v3, v0

    move v0, v5

    :goto_0
    const/4 v4, 0x6

    if-ge v0, v4, :cond_0

    .line 144
    iget-object v4, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    invoke-static {v4}, Lcom/android/fmradio/TunerManagerForExt;->access$1100(Lcom/android/fmradio/TunerManagerForExt;)[I

    move-result-object v4

    new-array v6, v2, [B

    mul-int/lit8 v7, v0, 0x2

    add-int/lit8 v7, v7, 0x5

    add-int/lit8 v8, v7, 0x1

    aget-byte v8, p1, v8

    aput-byte v8, v6, v5

    aget-byte v7, p1, v7

    aput-byte v7, v6, v1

    invoke-static {v6}, Lcom/android/fmradio/utils/FmUtils;->bytes2Int([B)I

    move-result v6

    aput v6, v4, v0

    .line 145
    new-instance v4, Ljava/lang/StringBuilder;

    invoke-direct {v4}, Ljava/lang/StringBuilder;-><init>()V

    invoke-virtual {v4, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    const-string v3, " - index: "

    invoke-virtual {v4, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v4, v0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v3, " - preset: "

    invoke-virtual {v4, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-object v3, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    invoke-static {v3}, Lcom/android/fmradio/TunerManagerForExt;->access$1100(Lcom/android/fmradio/TunerManagerForExt;)[I

    move-result-object v3

    aget v3, v3, v0

    invoke-virtual {v4, v3}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v4}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v3

    add-int/lit8 v0, v0, 0x1

    goto :goto_0

    .line 147
    :cond_0
    invoke-static {}, Lcom/android/fmradio/TunerManagerForExt;->access$600()Ljava/lang/String;

    move-result-object p1

    invoke-static {p1, v3}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    .line 149
    iget-object p1, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    invoke-static {p1}, Lcom/android/fmradio/TunerManagerForExt;->access$100(Lcom/android/fmradio/TunerManagerForExt;)Landroid/os/Handler;

    move-result-object p1

    const/16 v0, 0x38

    invoke-virtual {p1, v0}, Landroid/os/Handler;->removeMessages(I)V

    .line 150
    iget-object p1, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    invoke-static {p1}, Lcom/android/fmradio/TunerManagerForExt;->access$100(Lcom/android/fmradio/TunerManagerForExt;)Landroid/os/Handler;

    move-result-object p1

    invoke-virtual {p1, v0}, Landroid/os/Handler;->obtainMessage(I)Landroid/os/Message;

    move-result-object p1

    .line 151
    iget-object v0, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    invoke-static {v0}, Lcom/android/fmradio/TunerManagerForExt;->access$100(Lcom/android/fmradio/TunerManagerForExt;)Landroid/os/Handler;

    move-result-object v0

    invoke-virtual {v0, p1}, Landroid/os/Handler;->sendMessage(Landroid/os/Message;)Z

    return-void
.end method

.method public onTunerRangInfoChanged([B)V
    .locals 3

    .line 73
    iget-object v0, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    invoke-static {v0}, Lcom/android/fmradio/TunerManagerForExt;->access$100(Lcom/android/fmradio/TunerManagerForExt;)Landroid/os/Handler;

    move-result-object v0

    const/16 v1, 0x36

    invoke-virtual {v0, v1}, Landroid/os/Handler;->removeMessages(I)V

    .line 75
    new-instance v0, Landroid/os/Bundle;

    const/4 v2, 0x1

    invoke-direct {v0, v2}, Landroid/os/Bundle;-><init>(I)V

    const-string v2, "key_tune_rang_info"

    .line 76
    invoke-virtual {v0, v2, p1}, Landroid/os/Bundle;->putByteArray(Ljava/lang/String;[B)V

    .line 78
    iget-object p1, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    invoke-static {p1}, Lcom/android/fmradio/TunerManagerForExt;->access$100(Lcom/android/fmradio/TunerManagerForExt;)Landroid/os/Handler;

    move-result-object p1

    invoke-virtual {p1, v1}, Landroid/os/Handler;->obtainMessage(I)Landroid/os/Message;

    move-result-object p1

    .line 79
    invoke-virtual {p1, v0}, Landroid/os/Message;->setData(Landroid/os/Bundle;)V

    .line 80
    iget-object v0, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    invoke-static {v0}, Lcom/android/fmradio/TunerManagerForExt;->access$100(Lcom/android/fmradio/TunerManagerForExt;)Landroid/os/Handler;

    move-result-object v0

    invoke-virtual {v0, p1}, Landroid/os/Handler;->sendMessage(Landroid/os/Message;)Z

    return-void
.end method

.method public onTunerRdsPSPresetListInfo([B)V
    .locals 6

    .line 290
    iget-object v0, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    const/4 v1, 0x1

    aget-byte v1, p1, v1

    and-int/lit16 v1, v1, 0xff

    invoke-static {v0, v1}, Lcom/android/fmradio/TunerManagerForExt;->access$702(Lcom/android/fmradio/TunerManagerForExt;I)I

    .line 291
    iget-object v0, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    invoke-static {v0}, Lcom/android/fmradio/TunerManagerForExt;->access$800(Lcom/android/fmradio/TunerManagerForExt;)Landroid/content/Context;

    move-result-object v0

    iget-object v1, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    invoke-static {v1}, Lcom/android/fmradio/TunerManagerForExt;->access$700(Lcom/android/fmradio/TunerManagerForExt;)I

    move-result v1

    invoke-static {v0, v1}, Lcom/android/fmradio/database/FmStation;->setCurrentBand(Landroid/content/Context;I)V

    .line 293
    new-instance v0, Ljava/lang/StringBuilder;

    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    const-string v1, "onTunerRdsPSPresetListInfo - mBand: "

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-object v1, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    invoke-static {v1}, Lcom/android/fmradio/TunerManagerForExt;->access$700(Lcom/android/fmradio/TunerManagerForExt;)I

    move-result v1

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    const/4 v1, 0x0

    move-object v2, v0

    move v0, v1

    :goto_0
    const/4 v3, 0x6

    if-ge v0, v3, :cond_0

    const/16 v3, 0x8

    new-array v4, v3, [B

    mul-int/lit8 v5, v0, 0x8

    add-int/lit8 v5, v5, 0x2

    .line 297
    invoke-static {p1, v5, v4, v1, v3}, Ljava/lang/System;->arraycopy(Ljava/lang/Object;ILjava/lang/Object;II)V

    .line 298
    iget-object v3, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    invoke-static {v3}, Lcom/android/fmradio/TunerManagerForExt;->access$1800(Lcom/android/fmradio/TunerManagerForExt;)[Ljava/lang/String;

    move-result-object v3

    new-instance v5, Ljava/lang/String;

    invoke-direct {v5, v4}, Ljava/lang/String;-><init>([B)V

    aput-object v5, v3, v0

    .line 299
    new-instance v3, Ljava/lang/StringBuilder;

    invoke-direct {v3}, Ljava/lang/StringBuilder;-><init>()V

    invoke-virtual {v3, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    const-string v2, " - index: "

    invoke-virtual {v3, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v3, v0}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    const-string v2, " - preset_rds_ps: "

    invoke-virtual {v3, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    iget-object v2, p0, Lcom/android/fmradio/TunerManagerForExt$UITunerObserver;->this$0:Lcom/android/fmradio/TunerManagerForExt;

    invoke-static {v2}, Lcom/android/fmradio/TunerManagerForExt;->access$1800(Lcom/android/fmradio/TunerManagerForExt;)[Ljava/lang/String;

    move-result-object v2

    aget-object v2, v2, v0

    invoke-virtual {v3, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v3}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v2

    add-int/lit8 v0, v0, 0x1

    goto :goto_0

    .line 301
    :cond_0
    invoke-static {}, Lcom/android/fmradio/TunerManagerForExt;->access$600()Ljava/lang/String;

    move-result-object p1

    invoke-static {p1, v2}, Lcom/android/fmradio/utils/LogUtils;->print(Ljava/lang/String;Ljava/lang/String;)V

    return-void
.end method
