package info.guardianproject.notepadbot

object NConstants {
    const val SHARED_PREFS_NOTE_LINES = "use_lines_in_notes"
    const val TAG = "NoteCipher"
    const val MAX_STREAM_SIZE = 1000000
    private const val MIN_PASS_LENGTH = 8

    /**
     * Checks if the password is valid based on it's length
     *
     * @return True if the password is a valid one, false otherwise
     */
    @JvmStatic
    fun validatePassword(pass: CharArray): Boolean {
        return pass.size >= MIN_PASS_LENGTH
    }
}