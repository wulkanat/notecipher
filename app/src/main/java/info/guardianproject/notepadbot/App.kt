package info.guardianproject.notepadbot

import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import info.guardianproject.notepadbot.cacheword.CacheWordService
import info.guardianproject.notepadbot.cacheword.CacheWordSettings

class App : Application() {
    var cWSettings: CacheWordSettings? = null
        private set

    override fun onCreate() {
        super.onCreate()
        // Apply the Google PRNG fixes to properly seed SecureRandom
        PRNGFixes.apply()

        cWSettings = CacheWordSettings(applicationContext)
        cWSettings!!.notificationIntent = PendingIntent.getActivity(
            applicationContext,
            0, Intent(this, NoteCipher::class.java),
            Intent.FLAG_ACTIVITY_NEW_TASK
        )
    }
}