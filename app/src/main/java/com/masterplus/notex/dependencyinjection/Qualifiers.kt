package com.masterplus.notex.dependencyinjection

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MasterKey

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class EncryptedPreferences

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SettingPreferences