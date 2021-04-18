package info.guardianproject.notepadbot.cacheword

import android.content.Context
import info.guardianproject.notepadbot.cacheword.SecretsManager
import android.content.SharedPreferences
import android.util.Base64
import info.guardianproject.notepadbot.R
import info.guardianproject.notepadbot.cacheword.PRNGFixes

object SecretsManager {
    private var prngFixesApplied = false
    @JvmStatic
    fun isInitialized(ctx: Context): Boolean {
        possiblyApplyPRNGFixes(ctx)
        return getPrefs(ctx).getBoolean(Constants.SHARED_PREFS_INITIALIZED, false)
    }

    @JvmStatic
    fun saveBytes(ctx: Context, key: String?, value: ByteArray?): Boolean {
        val encoded = Base64.encodeToString(value, Base64.DEFAULT)
        val e = getPrefs(ctx).edit()
        e.putString(key, encoded)
        return e.commit()
    }

    @JvmStatic
    fun getBytes(ctx: Context, key: String?): ByteArray? {
        val encoded = getPrefs(ctx).getString(key, null) ?: return null
        return Base64.decode(encoded, Base64.DEFAULT)
    }

    @JvmStatic
    fun setInitialized(ctx: Context, initialized: Boolean): Boolean {
        val e = getPrefs(ctx).edit()
        e.putBoolean(Constants.SHARED_PREFS_INITIALIZED, initialized)
        return e.commit()
    }

    private fun possiblyApplyPRNGFixes(ctx: Context) {
        if (!prngFixesApplied && ctx.resources.getBoolean(R.bool.cacheword_apply_android_securerandom_fixes)) {
            PRNGFixes.apply()
            prngFixesApplied = true
        }
    }

    private fun getPrefs(ctx: Context): SharedPreferences {
        return ctx
            .getSharedPreferences(Constants.SHARED_PREFS, Constants.SHARED_PREFS_PRIVATE_MODE)
    }
}