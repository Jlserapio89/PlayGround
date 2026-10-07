package com.nexusai.data.local

import android.app.backup.BackupAgentHelper
import android.app.backup.SharedPreferencesBackupHelper

/** Backup Auto (Android Backup Service) para DataStore/preferencias. Room se respalda vía fullBackupContent XML. */
class NexusBackupAgent : BackupAgentHelper() {
    override fun onCreate() {
        addHelper("prefs", SharedPreferencesBackupHelper(this, "nexus_settings.preferences_pb"))
    }
}
