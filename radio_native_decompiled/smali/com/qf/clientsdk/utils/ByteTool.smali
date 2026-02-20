.class public Lcom/qf/clientsdk/utils/ByteTool;
.super Ljava/lang/Object;
.source "ByteTool.java"


# static fields
.field public static final DEFAULT_ENCODE:Ljava/lang/String; = "UTF-8"


# direct methods
.method public constructor <init>()V
    .locals 0

    .line 7
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

.method public static ByteToString([B)Ljava/lang/String;
    .locals 3

    if-eqz p0, :cond_0

    const/4 v0, 0x0

    const-string v1, ""

    .line 109
    :goto_0
    array-length v2, p0

    if-ge v0, v2, :cond_1

    .line 110
    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2}, Ljava/lang/StringBuilder;-><init>()V

    invoke-virtual {v2, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    aget-byte v1, p0, v0

    invoke-static {v1}, Lcom/qf/clientsdk/utils/ByteTool;->byteToHexString(B)Ljava/lang/String;

    move-result-object v1

    invoke-virtual {v2, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    const-string v1, " "

    invoke-virtual {v2, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    add-int/lit8 v0, v0, 0x1

    goto :goto_0

    :cond_0
    const-string v1, "null"

    :cond_1
    return-object v1
.end method

.method public static byteLen([BI)[B
    .locals 3

    .line 241
    new-array v0, p1, [B

    .line 242
    array-length v1, p0

    sub-int/2addr p1, v1

    const/4 v2, 0x0

    if-ltz p1, :cond_0

    .line 245
    invoke-static {p0, v2, v0, p1, v1}, Ljava/lang/System;->arraycopy(Ljava/lang/Object;ILjava/lang/Object;II)V

    goto :goto_0

    :cond_0
    const/16 p1, 0x14

    .line 247
    invoke-static {p0, v2, v0, v2, p1}, Ljava/lang/System;->arraycopy(Ljava/lang/Object;ILjava/lang/Object;II)V

    :goto_0
    return-object v0
.end method

.method public static byteToAddr([BII)Ljava/lang/String;
    .locals 3

    .line 293
    new-instance v0, Ljava/lang/StringBuffer;

    invoke-direct {v0}, Ljava/lang/StringBuffer;-><init>()V

    const/4 v1, 0x0

    :goto_0
    if-ge v1, p2, :cond_1

    add-int v2, v1, p1

    .line 295
    aget-byte v2, p0, v2

    invoke-static {v2}, Lcom/qf/clientsdk/utils/ByteTool;->byteToInt(B)I

    move-result v2

    .line 296
    invoke-static {v2}, Ljava/lang/String;->valueOf(I)Ljava/lang/String;

    move-result-object v2

    invoke-virtual {v0, v2}, Ljava/lang/StringBuffer;->append(Ljava/lang/String;)Ljava/lang/StringBuffer;

    add-int/lit8 v2, p2, -0x1

    if-eq v1, v2, :cond_0

    const/16 v2, 0x2e

    .line 298
    invoke-virtual {v0, v2}, Ljava/lang/StringBuffer;->append(C)Ljava/lang/StringBuffer;

    :cond_0
    add-int/lit8 v1, v1, 0x1

    goto :goto_0

    .line 301
    :cond_1
    invoke-virtual {v0}, Ljava/lang/StringBuffer;->toString()Ljava/lang/String;

    move-result-object p0

    return-object p0
.end method

.method public static byteToHexString(B)Ljava/lang/String;
    .locals 0

    and-int/lit16 p0, p0, 0xff

    .line 88
    invoke-static {p0}, Ljava/lang/Integer;->toHexString(I)Ljava/lang/String;

    move-result-object p0

    return-object p0
.end method

.method public static byteToHexString([BI)Ljava/lang/String;
    .locals 1

    .line 100
    array-length v0, p0

    add-int/lit8 v0, v0, -0x1

    if-gt p1, v0, :cond_0

    if-ltz p1, :cond_0

    .line 103
    aget-byte p0, p0, p1

    invoke-static {p0}, Lcom/qf/clientsdk/utils/ByteTool;->byteToHexString(B)Ljava/lang/String;

    move-result-object p0

    return-object p0

    .line 101
    :cond_0
    new-instance p0, Ljava/lang/ArrayIndexOutOfBoundsException;

    invoke-direct {p0}, Ljava/lang/ArrayIndexOutOfBoundsException;-><init>()V

    throw p0
.end method

.method public static byteToInt(B)I
    .locals 0

    and-int/lit16 p0, p0, 0xff

    return p0
.end method

.method public static byteToInt([BI)I
    .locals 1

    .line 137
    array-length v0, p0

    add-int/lit8 v0, v0, -0x1

    if-gt p1, v0, :cond_0

    if-ltz p1, :cond_0

    .line 140
    aget-byte p0, p0, p1

    invoke-static {p0}, Lcom/qf/clientsdk/utils/ByteTool;->byteToInt(B)I

    move-result p0

    return p0

    .line 138
    :cond_0
    new-instance p0, Ljava/lang/ArrayIndexOutOfBoundsException;

    invoke-direct {p0}, Ljava/lang/ArrayIndexOutOfBoundsException;-><init>()V

    throw p0
.end method

.method public static byteToLong([BI)J
    .locals 4

    add-int/lit8 v0, p1, 0x8

    .line 150
    array-length v1, p0

    if-gt v0, v1, :cond_0

    if-ltz p1, :cond_0

    .line 153
    invoke-static {p0, p1}, Lcom/qf/clientsdk/utils/ByteTool;->byteToInt([BI)I

    move-result v0

    shl-int/lit8 v0, v0, 0x18

    add-int/lit8 v1, p1, 0x1

    invoke-static {p0, v1}, Lcom/qf/clientsdk/utils/ByteTool;->byteToInt([BI)I

    move-result v1

    shl-int/lit8 v1, v1, 0x10

    or-int/2addr v0, v1

    add-int/lit8 v1, p1, 0x2

    invoke-static {p0, v1}, Lcom/qf/clientsdk/utils/ByteTool;->byteToInt([BI)I

    move-result v1

    shl-int/lit8 v1, v1, 0x8

    or-int/2addr v0, v1

    add-int/lit8 v1, p1, 0x3

    .line 154
    invoke-static {p0, v1}, Lcom/qf/clientsdk/utils/ByteTool;->byteToInt([BI)I

    move-result v1

    or-int/2addr v0, v1

    add-int/lit8 v1, p1, 0x4

    .line 155
    invoke-static {p0, v1}, Lcom/qf/clientsdk/utils/ByteTool;->byteToInt([BI)I

    move-result v1

    shl-int/lit8 v1, v1, 0x18

    add-int/lit8 v2, p1, 0x5

    invoke-static {p0, v2}, Lcom/qf/clientsdk/utils/ByteTool;->byteToInt([BI)I

    move-result v2

    shl-int/lit8 v2, v2, 0x10

    or-int/2addr v1, v2

    add-int/lit8 v2, p1, 0x6

    .line 156
    invoke-static {p0, v2}, Lcom/qf/clientsdk/utils/ByteTool;->byteToInt([BI)I

    move-result v2

    shl-int/lit8 v2, v2, 0x8

    or-int/2addr v1, v2

    add-int/lit8 p1, p1, 0x7

    invoke-static {p0, p1}, Lcom/qf/clientsdk/utils/ByteTool;->byteToInt([BI)I

    move-result p0

    or-int/2addr p0, v1

    int-to-long v0, v0

    const-wide v2, 0xffffffffL

    and-long/2addr v0, v2

    const/16 p1, 0x20

    shl-long/2addr v0, p1

    int-to-long p0, p0

    and-long/2addr p0, v2

    or-long/2addr p0, v0

    return-wide p0

    .line 151
    :cond_0
    new-instance p0, Ljava/lang/ArrayIndexOutOfBoundsException;

    invoke-direct {p0}, Ljava/lang/ArrayIndexOutOfBoundsException;-><init>()V

    throw p0
.end method

.method public static byteToMac([BII)Ljava/lang/String;
    .locals 3

    .line 313
    new-instance v0, Ljava/lang/StringBuffer;

    invoke-direct {v0}, Ljava/lang/StringBuffer;-><init>()V

    const/4 v1, 0x0

    :goto_0
    if-ge v1, p2, :cond_1

    add-int v2, v1, p1

    .line 315
    aget-byte v2, p0, v2

    invoke-static {v2}, Lcom/qf/clientsdk/utils/ByteTool;->byteToHexString(B)Ljava/lang/String;

    move-result-object v2

    .line 316
    invoke-static {v2}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v2

    invoke-virtual {v0, v2}, Ljava/lang/StringBuffer;->append(Ljava/lang/String;)Ljava/lang/StringBuffer;

    add-int/lit8 v2, p2, -0x1

    if-eq v1, v2, :cond_0

    const/16 v2, 0x2d

    .line 318
    invoke-virtual {v0, v2}, Ljava/lang/StringBuffer;->append(C)Ljava/lang/StringBuffer;

    :cond_0
    add-int/lit8 v1, v1, 0x1

    goto :goto_0

    .line 321
    :cond_1
    invoke-virtual {v0}, Ljava/lang/StringBuffer;->toString()Ljava/lang/String;

    move-result-object p0

    return-object p0
.end method

.method public static byteToMadVersion([B)Ljava/lang/String;
    .locals 4

    .line 332
    new-instance v0, Ljava/lang/StringBuffer;

    invoke-direct {v0}, Ljava/lang/StringBuffer;-><init>()V

    .line 333
    array-length v1, p0

    const/4 v2, 0x0

    :goto_0
    if-ge v2, v1, :cond_0

    .line 334
    aget-byte v3, p0, v2

    .line 335
    invoke-static {v3}, Ljava/lang/String;->valueOf(I)Ljava/lang/String;

    move-result-object v3

    invoke-virtual {v0, v3}, Ljava/lang/StringBuffer;->append(Ljava/lang/String;)Ljava/lang/StringBuffer;

    add-int/lit8 v2, v2, 0x1

    goto :goto_0

    .line 337
    :cond_0
    invoke-virtual {v0}, Ljava/lang/StringBuffer;->toString()Ljava/lang/String;

    move-result-object p0

    return-object p0
.end method

.method public static byteToString([B)Ljava/lang/String;
    .locals 1

    const-string v0, "UTF-8"

    .line 357
    invoke-static {p0, v0}, Lcom/qf/clientsdk/utils/ByteTool;->byteToString([BLjava/lang/String;)Ljava/lang/String;

    move-result-object p0

    return-object p0
.end method

.method public static byteToString([BLjava/lang/String;)Ljava/lang/String;
    .locals 1

    .line 261
    :try_start_0
    new-instance v0, Ljava/lang/String;

    invoke-direct {v0, p0, p1}, Ljava/lang/String;-><init>([BLjava/lang/String;)V
    :try_end_0
    .catch Ljava/io/UnsupportedEncodingException; {:try_start_0 .. :try_end_0} :catch_0

    return-object v0

    :catch_0
    move-exception p1

    .line 263
    invoke-virtual {p1}, Ljava/io/UnsupportedEncodingException;->printStackTrace()V

    .line 265
    new-instance p1, Ljava/lang/String;

    invoke-direct {p1, p0}, Ljava/lang/String;-><init>([B)V

    return-object p1
.end method

.method public static byteToVersion([B)Ljava/lang/String;
    .locals 4

    .line 276
    new-instance v0, Ljava/lang/StringBuffer;

    invoke-direct {v0}, Ljava/lang/StringBuffer;-><init>()V

    .line 277
    array-length v1, p0

    const/4 v2, 0x0

    :goto_0
    if-ge v2, v1, :cond_0

    .line 278
    aget-byte v3, p0, v2

    .line 279
    invoke-static {v3}, Ljava/lang/String;->valueOf(I)Ljava/lang/String;

    move-result-object v3

    invoke-virtual {v0, v3}, Ljava/lang/StringBuffer;->append(Ljava/lang/String;)Ljava/lang/StringBuffer;

    add-int/lit8 v2, v2, 0x1

    goto :goto_0

    .line 281
    :cond_0
    invoke-virtual {v0}, Ljava/lang/StringBuffer;->toString()Ljava/lang/String;

    move-result-object p0

    return-object p0
.end method

.method public static bytes2Int([B)I
    .locals 3

    .line 430
    array-length v0, p0

    add-int/lit8 v0, v0, -0x1

    const/4 v1, 0x0

    :goto_0
    if-ltz v0, :cond_0

    shl-int/lit8 v1, v1, 0x8

    .line 432
    aget-byte v2, p0, v0

    and-int/lit16 v2, v2, 0xff

    or-int/2addr v1, v2

    add-int/lit8 v0, v0, -0x1

    goto :goto_0

    :cond_0
    return v1
.end method

.method public static bytesToInt([B)I
    .locals 3

    .line 46
    array-length v0, p0

    add-int/lit8 v0, v0, -0x1

    const/4 v1, 0x0

    :goto_0
    if-ltz v0, :cond_0

    shl-int/lit8 v1, v1, 0x8

    .line 48
    aget-byte v2, p0, v0

    and-int/lit16 v2, v2, 0xff

    or-int/2addr v1, v2

    add-int/lit8 v0, v0, -0x1

    goto :goto_0

    :cond_0
    return v1
.end method

.method public static bytesToLong([B)J
    .locals 7

    const-wide/16 v0, 0x0

    const/4 v2, 0x0

    .line 71
    :goto_0
    array-length v3, p0

    if-ge v2, v3, :cond_0

    .line 72
    aget-byte v3, p0, v2

    and-int/lit16 v3, v3, 0xff

    int-to-long v3, v3

    .line 73
    sget-object v5, Ljava/lang/System;->out:Ljava/io/PrintStream;

    add-int/lit8 v2, v2, 0x1

    mul-int/lit8 v6, v2, 0x8

    rsub-int/lit8 v6, v6, 0x40

    shl-long/2addr v3, v6

    invoke-virtual {v5, v3, v4}, Ljava/io/PrintStream;->println(J)V

    or-long/2addr v0, v3

    goto :goto_0

    :cond_0
    return-wide v0
.end method

.method public static getByteFrom([BII)[B
    .locals 2

    sub-int/2addr p2, p1

    add-int/lit8 p2, p2, 0x1

    .line 31
    new-array p2, p2, [B

    const/4 v0, 0x0

    .line 32
    :goto_0
    array-length v1, p2

    if-ge v0, v1, :cond_0

    add-int v1, v0, p1

    .line 33
    aget-byte v1, p0, v1

    aput-byte v1, p2, v0

    add-int/lit8 v0, v0, 0x1

    goto :goto_0

    :cond_0
    return-object p2
.end method

.method public static int2Bytes(II)[B
    .locals 3

    .line 397
    new-array v0, p1, [B

    const/4 v1, 0x0

    :goto_0
    if-ge v1, p1, :cond_0

    mul-int/lit8 v2, v1, 0x8

    shr-int v2, p0, v2

    and-int/lit16 v2, v2, 0xff

    int-to-byte v2, v2

    .line 399
    aput-byte v2, v0, v1

    add-int/lit8 v1, v1, 0x1

    goto :goto_0

    :cond_0
    return-object v0
.end method

.method public static int2BytesHigh(II)[B
    .locals 4

    .line 413
    new-array v0, p1, [B

    add-int/lit8 v1, p1, -0x1

    const/4 v2, 0x0

    :goto_0
    if-ge v2, p1, :cond_0

    mul-int/lit8 v3, v2, 0x8

    shr-int v3, p0, v3

    and-int/lit16 v3, v3, 0xff

    int-to-byte v3, v3

    .line 416
    aput-byte v3, v0, v1

    add-int/lit8 v1, v1, -0x1

    add-int/lit8 v2, v2, 0x1

    goto :goto_0

    :cond_0
    return-object v0
.end method

.method public static invertArray(Ljava/lang/Object;)Ljava/lang/Object;
    .locals 5
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "<T:",
            "Ljava/lang/Object;",
            ">(TT;)TT;"
        }
    .end annotation

    .line 442
    invoke-static {p0}, Ljava/lang/reflect/Array;->getLength(Ljava/lang/Object;)I

    move-result v0

    .line 444
    invoke-virtual {p0}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    move-result-object v1

    invoke-virtual {v1}, Ljava/lang/Class;->getComponentType()Ljava/lang/Class;

    move-result-object v1

    .line 446
    invoke-static {v1, v0}, Ljava/lang/reflect/Array;->newInstance(Ljava/lang/Class;I)Ljava/lang/Object;

    move-result-object v1

    const/4 v2, 0x0

    .line 448
    invoke-static {p0, v2, v1, v2, v0}, Ljava/lang/System;->arraycopy(Ljava/lang/Object;ILjava/lang/Object;II)V

    .line 452
    :goto_0
    div-int/lit8 p0, v0, 0x2

    if-ge v2, p0, :cond_0

    .line 453
    invoke-static {v1, v2}, Ljava/lang/reflect/Array;->get(Ljava/lang/Object;I)Ljava/lang/Object;

    move-result-object p0

    sub-int v3, v0, v2

    add-int/lit8 v3, v3, -0x1

    .line 454
    invoke-static {v1, v3}, Ljava/lang/reflect/Array;->get(Ljava/lang/Object;I)Ljava/lang/Object;

    move-result-object v4

    invoke-static {v1, v2, v4}, Ljava/lang/reflect/Array;->set(Ljava/lang/Object;ILjava/lang/Object;)V

    .line 455
    invoke-static {v1, v3, p0}, Ljava/lang/reflect/Array;->set(Ljava/lang/Object;ILjava/lang/Object;)V

    add-int/lit8 v2, v2, 0x1

    goto :goto_0

    :cond_0
    return-object v1
.end method

.method public static isMatch(BI)Z
    .locals 0

    if-ne p0, p1, :cond_0

    const/4 p0, 0x1

    goto :goto_0

    :cond_0
    const/4 p0, 0x0

    :goto_0
    return p0
.end method

.method public static isMatchSingle(BI)Z
    .locals 0

    and-int/2addr p0, p1

    if-eqz p0, :cond_0

    const/4 p0, 0x1

    goto :goto_0

    :cond_0
    const/4 p0, 0x0

    :goto_0
    return p0
.end method

.method public static isMatchZero(B)Z
    .locals 0

    and-int/lit16 p0, p0, 0xff

    if-nez p0, :cond_0

    const/4 p0, 0x1

    goto :goto_0

    :cond_0
    const/4 p0, 0x0

    :goto_0
    return p0
.end method

.method public static jdsByteToString([B)Ljava/lang/String;
    .locals 3

    .line 118
    new-instance v0, Ljava/lang/StringBuffer;

    invoke-direct {v0}, Ljava/lang/StringBuffer;-><init>()V

    if-eqz p0, :cond_0

    const/4 v1, 0x0

    .line 121
    :goto_0
    array-length v2, p0

    if-ge v1, v2, :cond_0

    .line 122
    aget-byte v2, p0, v1

    invoke-static {v2}, Lcom/qf/clientsdk/utils/ByteTool;->byteToHexString(B)Ljava/lang/String;

    move-result-object v2

    invoke-virtual {v0, v2}, Ljava/lang/StringBuffer;->append(Ljava/lang/String;)Ljava/lang/StringBuffer;

    add-int/lit8 v1, v1, 0x1

    goto :goto_0

    .line 126
    :cond_0
    invoke-virtual {v0}, Ljava/lang/StringBuffer;->toString()Ljava/lang/String;

    move-result-object p0

    return-object p0
.end method

.method public static main([Ljava/lang/String;)V
    .locals 2

    const/16 p0, 0x8

    new-array p0, p0, [B

    .line 54
    fill-array-data p0, :array_0

    .line 56
    invoke-static {p0}, Lcom/qf/clientsdk/utils/ByteTool;->bytesToLong([B)J

    move-result-wide v0

    .line 57
    sget-object p0, Ljava/lang/System;->out:Ljava/io/PrintStream;

    invoke-virtual {p0, v0, v1}, Ljava/io/PrintStream;->println(J)V

    return-void

    nop

    :array_0
    .array-data 1
        -0x2t
        -0x56t
        0x13t
        0x23t
        -0x1bt
        0x76t
        0x24t
        0x66t
    .end array-data
.end method

.method public static readIntTwo(I)B
    .locals 0

    shr-int/lit8 p0, p0, 0x8

    int-to-byte p0, p0

    return p0
.end method

.method public static shortToInt([BI)I
    .locals 2

    add-int/lit8 v0, p1, 0x2

    .line 169
    array-length v1, p0

    if-gt v0, v1, :cond_0

    if-ltz p1, :cond_0

    .line 172
    aget-byte v0, p0, p1

    and-int/lit16 v0, v0, 0xff

    shl-int/lit8 v0, v0, 0x8

    add-int/lit8 p1, p1, 0x1

    aget-byte p0, p0, p1

    and-int/lit16 p0, p0, 0xff

    or-int/2addr p0, v0

    const p1, 0xffff

    and-int/2addr p0, p1

    return p0

    .line 170
    :cond_0
    new-instance p0, Ljava/lang/ArrayIndexOutOfBoundsException;

    invoke-direct {p0}, Ljava/lang/ArrayIndexOutOfBoundsException;-><init>()V

    throw p0
.end method

.method public static stringToByte(Ljava/lang/String;)[B
    .locals 1

    const-string v0, "UTF-8"

    .line 347
    invoke-static {p0, v0}, Lcom/qf/clientsdk/utils/ByteTool;->stringToByte(Ljava/lang/String;Ljava/lang/String;)[B

    move-result-object p0

    return-object p0
.end method

.method public static stringToByte(Ljava/lang/String;Ljava/lang/String;)[B
    .locals 0

    .line 226
    :try_start_0
    invoke-virtual {p0, p1}, Ljava/lang/String;->getBytes(Ljava/lang/String;)[B

    move-result-object p0
    :try_end_0
    .catch Ljava/io/UnsupportedEncodingException; {:try_start_0 .. :try_end_0} :catch_0

    return-object p0

    :catch_0
    move-exception p1

    .line 228
    invoke-virtual {p1}, Ljava/io/UnsupportedEncodingException;->printStackTrace()V

    .line 230
    invoke-virtual {p0}, Ljava/lang/String;->getBytes()[B

    move-result-object p0

    return-object p0
.end method
