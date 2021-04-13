package info.guardianproject.notepadbot

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.os.ResultReceiver
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.TextView.OnEditorActionListener
import androidx.appcompat.app.AppCompatActivity
import info.guardianproject.notepadbot.NConstants.validatePassword
import info.guardianproject.notepadbot.cacheword.CacheWordActivityHandler
import info.guardianproject.notepadbot.cacheword.ICacheWordSubscriber
import java.security.GeneralSecurityException

class LockScreenActivity : AppCompatActivity(), ICacheWordSubscriber {
    private lateinit var mEnterPassphrase: EditText
    private lateinit var mNewPassphrase: EditText
    private lateinit var mConfirmNewPassphrase: EditText
    private lateinit var mViewCreatePassphrase: View
    private lateinit var mViewEnterPassphrase: View
    private lateinit var mBtnOpen: Button
    private lateinit var mCacheWord: CacheWordActivityHandler
    private var mPasswordError: String? = null
    private var mSlider: TwoViewSlider? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock_screen)
        mCacheWord = CacheWordActivityHandler(this, (application as App).cWSettings)
        mViewCreatePassphrase = findViewById(R.id.llCreatePassphrase)
        mViewEnterPassphrase = findViewById(R.id.llEnterPassphrase)
        mEnterPassphrase = findViewById<View>(R.id.editEnterPassphrase) as EditText
        mNewPassphrase = findViewById<View>(R.id.editNewPassphrase) as EditText
        mConfirmNewPassphrase = findViewById<View>(R.id.editConfirmNewPassphrase) as EditText
        val vf = findViewById<View>(R.id.viewFlipper1) as ViewFlipper
        val flipView1 = findViewById<View>(R.id.flipView1) as LinearLayout
        val flipView2 = findViewById<View>(R.id.flipView2) as LinearLayout
        mSlider = TwoViewSlider(vf, flipView1, flipView2, mNewPassphrase, mConfirmNewPassphrase)
    }

    override fun onPause() {
        super.onPause()
        mCacheWord.onPause()
    }

    override fun onResume() {
        super.onResume()
        mCacheWord.onResume()
    }

    override fun onCacheWordUninitialized() {
        initializePassphrase()
    }

    override fun onCacheWordLocked() {
        promptPassphrase()
    }

    override fun onCacheWordOpened() {
        val intent = intent.getParcelableExtra<Parcelable>("originalIntent") as Intent?
        intent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
        finish()
        overridePendingTransition(0, 0)
    }

    private fun newEqualsConfirmation(): Boolean {
        return mNewPassphrase.text.toString() != mConfirmNewPassphrase.text.toString()
    }

    private fun showValidationError() {
        Toast.makeText(this@LockScreenActivity, mPasswordError, Toast.LENGTH_LONG).show()
        mNewPassphrase.requestFocus()
    }

    private fun showInequalityError() {
        Toast.makeText(
            this@LockScreenActivity,
            R.string.lock_screen_passphrases_not_matching,
            Toast.LENGTH_SHORT
        ).show()
        clearNewFields()
    }

    private fun clearNewFields() {
        mNewPassphrase.editableText.clear()
        mConfirmNewPassphrase.editableText.clear()
    }

    private val isPasswordNotValid: Boolean
        get() {
            val valid = validatePassword(mNewPassphrase.text.toString().toCharArray())
            if (!valid) mPasswordError = getString(R.string.pass_err_length)
            return !valid
        }
    private val isConfirmationFieldEmpty: Boolean
        get() = mConfirmNewPassphrase.text.toString().isEmpty()

    private fun initializePassphrase() {
        // Passphrase is not set, so allow the user to create one
        mViewCreatePassphrase.visibility = View.VISIBLE
        mViewEnterPassphrase.visibility = View.GONE
        mNewPassphrase.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_NULL || actionId == EditorInfo.IME_ACTION_DONE) {
                if (isPasswordNotValid) showValidationError() else mSlider!!.showConfirmationField()
            }
            false
        }
        mConfirmNewPassphrase.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_NULL || actionId == EditorInfo.IME_ACTION_DONE) {
                if (newEqualsConfirmation()) {
                    showInequalityError()
                    mSlider!!.showNewPasswordField()
                }
            }
            false
        }
        val btnCreate = findViewById<View>(R.id.btnCreate) as Button
        btnCreate.setOnClickListener {
            // validate
            when {
                isPasswordNotValid -> {
                    showValidationError()
                    mSlider!!.showNewPasswordField()
                }
                isConfirmationFieldEmpty -> {
                    mSlider!!.showConfirmationField()
                }
                newEqualsConfirmation() -> {
                    showInequalityError()
                    mSlider!!.showNewPasswordField()
                }
                else -> {
                    try {
                        mCacheWord.setPassphrase(mNewPassphrase.text.toString().toCharArray())
                    } catch (e: GeneralSecurityException) {
                        // TODO initialization failed
                        Log.e(TAG, "Cache word pass initialization failed: " + e.message)
                    }
                }
            }
        }
    }

    private fun promptPassphrase() {
        mViewCreatePassphrase.visibility = View.GONE
        mViewEnterPassphrase.visibility = View.VISIBLE
        mBtnOpen = findViewById<View>(R.id.btnOpen) as Button
        mBtnOpen.setOnClickListener {
            if (mEnterPassphrase.text.toString().isEmpty()) return@setOnClickListener
            // Check passphrase
            try {
                mCacheWord.setPassphrase(mEnterPassphrase.text.toString().toCharArray())
            } catch (e: GeneralSecurityException) {
                mEnterPassphrase.setText("")
                // TODO implement try again and wipe if fail
                Log.e(TAG, "Cache word pass verification failed: " + e.message)
            }
        }
        mEnterPassphrase.setOnEditorActionListener(OnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_NULL || actionId == EditorInfo.IME_ACTION_GO) {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                val threadHandler = Handler()
                imm.hideSoftInputFromWindow(v.windowToken, 0, object : ResultReceiver(
                    threadHandler
                ) {
                    override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
                        super.onReceiveResult(resultCode, resultData)
                        mBtnOpen.performClick()
                    }
                })
                return@OnEditorActionListener true
            }
            false
        })
    }

    inner class TwoViewSlider(
        private val flipper: ViewFlipper,
        private val container1: LinearLayout,
        private val container2: LinearLayout,
        private val firstView: View?,
        private val secondView: View?
    ) {
        private var firstIsShown = true
        private val pushRightIn: Animation =
            AnimationUtils.loadAnimation(this@LockScreenActivity, R.anim.push_right_in)
        private val pushRightOut: Animation =
            AnimationUtils.loadAnimation(this@LockScreenActivity, R.anim.push_right_out)
        private val pushLeftIn: Animation =
            AnimationUtils.loadAnimation(this@LockScreenActivity, R.anim.push_left_in)
        private val pushLeftOut: Animation =
            AnimationUtils.loadAnimation(this@LockScreenActivity, R.anim.push_left_out)
        fun showNewPasswordField() {
            if (firstIsShown) return
            flipper.inAnimation = pushRightIn
            flipper.outAnimation = pushRightOut
            flip()
        }

        fun showConfirmationField() {
            if (!firstIsShown) return
            flipper.inAnimation = pushLeftIn
            flipper.outAnimation = pushLeftOut
            flip()
        }

        private fun flip() {
            if (firstIsShown) {
                firstIsShown = false
                container2.removeAllViews()
                container2.addView(secondView)
            } else {
                firstIsShown = true
                container1.removeAllViews()
                container1.addView(firstView)
            }
            flipper.showNext()
        }

    }

    companion object {
        private const val TAG = "LockScreenActivity"
    }
}