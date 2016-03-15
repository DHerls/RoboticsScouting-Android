package org.fullmetalfalcons.androidscouting;

import android.app.backup.BackupAgentHelper;
import android.app.backup.SharedPreferencesBackupHelper;

/**
 * Helps backup shared preferences
 *
 * Created by Dan on 3/15/2016.
 */
public class PreferenceBackupHelper extends BackupAgentHelper {
    // The name of the SharedPreferences file
    private static final String PREFS = "org.fullmetalfalcons.androidscouting_preferences";

    // A key to uniquely identify the set of backup data
    private static final String PREFS_BACKUP_KEY = "remote_database_prefs";

    // Allocate a helper and add it to the backup agent
    @Override
    public void onCreate() {
        SharedPreferencesBackupHelper helper = new SharedPreferencesBackupHelper(this, PREFS);
        addHelper(PREFS_BACKUP_KEY, helper);
    }
}
