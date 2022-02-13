package com.masterplus.notex.roomdb.repos.abstraction

import com.masterplus.notex.roomdb.models.backups.UnitedBackup

interface IBackupRepo {
    suspend fun formBackupUnitedBackup(): UnitedBackup
    suspend fun loadUnitedBackup(unitedBackup: UnitedBackup)
    suspend fun deleteContentTables()
}