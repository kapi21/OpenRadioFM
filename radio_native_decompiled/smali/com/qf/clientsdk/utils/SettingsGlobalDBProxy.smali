.class public Lcom/qf/clientsdk/utils/SettingsGlobalDBProxy;
.super Ljava/lang/Object;
.source "SettingsGlobalDBProxy.java"


# static fields
.field private static instance:Lcom/qf/clientsdk/utils/SettingsGlobalDBProxy;


# instance fields
.field private context:Landroid/content/Context;


# direct methods
.method public constructor <init>()V
    .locals 0

    .line 6
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

.method public static getInstance()Lcom/qf/clientsdk/utils/SettingsGlobalDBProxy;
    .locals 1

    .line 13
    sget-object v0, Lcom/qf/clientsdk/utils/SettingsGlobalDBProxy;->instance:Lcom/qf/clientsdk/utils/SettingsGlobalDBProxy;

    if-nez v0, :cond_0

    .line 14
    new-instance v0, Lcom/qf/clientsdk/utils/SettingsGlobalDBProxy;

    invoke-direct {v0}, Lcom/qf/clientsdk/utils/SettingsGlobalDBProxy;-><init>()V

    sput-object v0, Lcom/qf/clientsdk/utils/SettingsGlobalDBProxy;->instance:Lcom/qf/clientsdk/utils/SettingsGlobalDBProxy;

    .line 16
    :cond_0
    sget-object v0, Lcom/qf/clientsdk/utils/SettingsGlobalDBProxy;->instance:Lcom/qf/clientsdk/utils/SettingsGlobalDBProxy;

    return-object v0
.end method


# virtual methods
.method public getFloat(Ljava/lang/String;F)F
    .locals 1

    .line 60
    iget-object v0, p0, Lcom/qf/clientsdk/utils/SettingsGlobalDBProxy;->context:Landroid/content/Context;

    invoke-virtual {v0}, Landroid/content/Context;->getContentResolver()Landroid/content/ContentResolver;

    move-result-object v0

    invoke-static {v0, p1, p2}, Landroid/provider/Settings$Global;->getFloat(Landroid/content/ContentResolver;Ljava/lang/String;F)F

    move-result p1

    return p1
.end method

.method public getInt(Ljava/lang/String;I)I
    .locals 1

    .line 40
    iget-object v0, p0, Lcom/qf/clientsdk/utils/SettingsGlobalDBProxy;->context:Landroid/content/Context;

    invoke-virtual {v0}, Landroid/content/Context;->getContentResolver()Landroid/content/ContentResolver;

    move-result-object v0

    invoke-static {v0, p1, p2}, Landroid/provider/Settings$Global;->getInt(Landroid/content/ContentResolver;Ljava/lang/String;I)I

    move-result p1

    return p1
.end method

.method public getLong(Ljava/lang/String;J)J
    .locals 1

    .line 65
    iget-object v0, p0, Lcom/qf/clientsdk/utils/SettingsGlobalDBProxy;->context:Landroid/content/Context;

    invoke-virtual {v0}, Landroid/content/Context;->getContentResolver()Landroid/content/ContentResolver;

    move-result-object v0

    invoke-static {v0, p1, p2, p3}, Landroid/provider/Settings$Global;->getLong(Landroid/content/ContentResolver;Ljava/lang/String;J)J

    move-result-wide p1

    return-wide p1
.end method

.method public getString(Ljava/lang/String;)Ljava/lang/String;
    .locals 1

    .line 46
    iget-object v0, p0, Lcom/qf/clientsdk/utils/SettingsGlobalDBProxy;->context:Landroid/content/Context;

    invoke-virtual {v0}, Landroid/content/Context;->getContentResolver()Landroid/content/ContentResolver;

    move-result-object v0

    invoke-static {v0, p1}, Landroid/provider/Settings$Global;->getString(Landroid/content/ContentResolver;Ljava/lang/String;)Ljava/lang/String;

    move-result-object p1

    return-object p1
.end method

.method public getString(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
    .locals 1

    .line 52
    :try_start_0
    iget-object v0, p0, Lcom/qf/clientsdk/utils/SettingsGlobalDBProxy;->context:Landroid/content/Context;

    invoke-virtual {v0}, Landroid/content/Context;->getContentResolver()Landroid/content/ContentResolver;

    move-result-object v0

    invoke-static {v0, p1}, Landroid/provider/Settings$Global;->getString(Landroid/content/ContentResolver;Ljava/lang/String;)Ljava/lang/String;

    move-result-object p1
    :try_end_0
    .catch Ljava/lang/Exception; {:try_start_0 .. :try_end_0} :catch_0

    if-nez p1, :cond_0

    move-object p1, p2

    :cond_0
    return-object p1

    :catch_0
    return-object p2
.end method

.method public initContext(Landroid/content/Context;)V
    .locals 0

    .line 20
    iput-object p1, p0, Lcom/qf/clientsdk/utils/SettingsGlobalDBProxy;->context:Landroid/content/Context;

    return-void
.end method

.method public putFloat(Ljava/lang/String;F)V
    .locals 1

    .line 32
    iget-object v0, p0, Lcom/qf/clientsdk/utils/SettingsGlobalDBProxy;->context:Landroid/content/Context;

    invoke-virtual {v0}, Landroid/content/Context;->getContentResolver()Landroid/content/ContentResolver;

    move-result-object v0

    invoke-static {v0, p1, p2}, Landroid/provider/Settings$Global;->putFloat(Landroid/content/ContentResolver;Ljava/lang/String;F)Z

    return-void
.end method

.method public putInt(Ljava/lang/String;I)V
    .locals 1

    .line 24
    iget-object v0, p0, Lcom/qf/clientsdk/utils/SettingsGlobalDBProxy;->context:Landroid/content/Context;

    invoke-virtual {v0}, Landroid/content/Context;->getContentResolver()Landroid/content/ContentResolver;

    move-result-object v0

    invoke-static {v0, p1, p2}, Landroid/provider/Settings$Global;->putInt(Landroid/content/ContentResolver;Ljava/lang/String;I)Z

    return-void
.end method

.method public putLong(Ljava/lang/String;J)V
    .locals 1

    .line 36
    iget-object v0, p0, Lcom/qf/clientsdk/utils/SettingsGlobalDBProxy;->context:Landroid/content/Context;

    invoke-virtual {v0}, Landroid/content/Context;->getContentResolver()Landroid/content/ContentResolver;

    move-result-object v0

    invoke-static {v0, p1, p2, p3}, Landroid/provider/Settings$Global;->putLong(Landroid/content/ContentResolver;Ljava/lang/String;J)Z

    return-void
.end method

.method public putString(Ljava/lang/String;Ljava/lang/String;)V
    .locals 1

    .line 28
    iget-object v0, p0, Lcom/qf/clientsdk/utils/SettingsGlobalDBProxy;->context:Landroid/content/Context;

    invoke-virtual {v0}, Landroid/content/Context;->getContentResolver()Landroid/content/ContentResolver;

    move-result-object v0

    invoke-static {v0, p1, p2}, Landroid/provider/Settings$Global;->putString(Landroid/content/ContentResolver;Ljava/lang/String;Ljava/lang/String;)Z

    return-void
.end method
