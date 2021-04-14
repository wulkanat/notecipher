package info.guardianproject.notepadbot

import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import androidx.constraintlayout.solver.Cache
import info.guardianproject.notepadbot.cacheword.CacheWordService
import info.guardianproject.notepadbot.cacheword.CacheWordSettings

class App : Application() {
    val cWSettings: CacheWordSettings by lazy { CacheWordSettings(applicationContext) }

    override fun onCreate() {
        super.onCreate()


        /*cWSettings!!.notificationIntent = PendingIntent.getActivity(
            applicationContext,
            0, Intent(this, NoteCipher::class.java),
            Intent.FLAG_ACTIVITY_NEW_TASK
        )*/
    }
}