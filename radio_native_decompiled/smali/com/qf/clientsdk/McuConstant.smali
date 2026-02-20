.class public Lcom/qf/clientsdk/McuConstant;
.super Ljava/lang/Object;
.source "McuConstant.java"


# annotations
.annotation system Ldalvik/annotation/MemberClasses;
    value = {
        Lcom/qf/clientsdk/McuConstant$CID_OX1A_Data;,
        Lcom/qf/clientsdk/McuConstant$CID_OX17_Data;,
        Lcom/qf/clientsdk/McuConstant$CID_OX19_Data;,
        Lcom/qf/clientsdk/McuConstant$CID_OX18_Data;
    }
.end annotation


# static fields
.field public static final CMD_ARM2MCU_ANDROID_STATUS:I = 0x10

.field public static final CMD_ARM2MCU_AUDIO:I = 0x15

.field public static final CMD_ARM2MCU_AUDIO_STATE_GET:I = 0x17

.field public static final CMD_ARM2MCU_CANBUS:I = 0x40

.field public static final CMD_ARM2MCU_HEADLIGHT:I = 0x16

.field public static final CMD_ARM2MCU_MISC_C18_TYPE_SET:I = 0x18

.field public static final CMD_ARM2MCU_MISC_C19_TYPE_GET:I = 0x19

.field public static final CMD_ARM2MCU_POWEROFF:I = 0x11

.field public static final CMD_ARM2MCU_RECOVERY:I = 0x12

.field public static final CMD_ARM2MCU_UPGRADE_END:I = 0x33

.field public static final CMD_ARM2MCU_UPGRADE_START:I = 0x30

.field public static final CMD_CONTROL_INDEX:I = 0x5

.field public static final CMD_DATA_INDEX:I = 0x6

.field public static final CMD_DATA_LENGTH_INDEX:I = 0x3

.field public static final CMD_MCU2ARM_ACC_STATUS:I = 0x24

.field public static final CMD_MCU2ARM_AUDIO_TYPE:I = 0x17

.field public static final CMD_MCU2ARM_CANBUS:I = 0x41

.field public static final CMD_MCU2ARM_HANDBRAKE:I = 0x23

.field public static final CMD_MCU2ARM_HEADLIGHT:I = 0x22

.field public static final CMD_MCU2ARM_KEYCODE:I = 0x20

.field public static final CMD_MCU2ARM_KEYCODE_EXT:I = 0x27

.field public static final CMD_MCU2ARM_MISC_C19_TYPE:I = 0x19

.field public static final CMD_MCU2ARM_UPGRADE_FAIL:I = 0x32

.field public static final CMD_MCU2ARM_UPGRADE_INFO:I = 0x31

.field public static final CMD_MCU2ARM_VERSION:I = 0x25

.field public static final CMD_TYPE_ACK:I = 0x2

.field public static final CMD_TYPE_CONTROL:I = 0x0

.field public static final CMD_TYPE_INDEX:I = 0x4

.field public static final CMD_TYPE_REQUEST:I = 0x1

.field public static final MCU_CHECKSUM_START_INDEX:I = 0x1

.field public static final MCU_CHECKSUM_START_OFFSET:I = 0x3

.field public static final MCU_CMD_MIN_LENGTH:I = 0x8

.field public static final MCU_CMD_STARTCODE:I = 0xff

.field public static final MCU_HEADER_ARM2MCU:I = 0xfd

.field public static final MCU_HEADER_MCU2ARM:I = 0xfe

.field public static final VOLUME_DATA_LENGTH:I = 0x8


# direct methods
.method public constructor <init>()V
    .locals 0

    .line 3
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method
