package info.guardianproject.notepadbot

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText

/**
 * A custom EditText that draws lines between each line of text that is displayed.
 */
class LinedEditText(context: Context?, attrs: AttributeSet?) :
    AppCompatEditText(context!!, attrs) {

    private val mRect: Rect = Rect()
    @Suppress("DEPRECATION")
    private val mPaint: Paint = Paint().apply {
        style = Paint.Style.STROKE
        color = resources.getColor(R.color.gray)
    }
    private val showLines = Settings.getNoteLinesOption(context!!)

    override fun onDraw(canvas: Canvas) {
        val height = height
        var curHeight: Int
        val baseline = getLineBounds(0, mRect)
        if (showLines) {
            curHeight = baseline + 3
            while (curHeight < height) {
                canvas.drawLine(
                    mRect.left.toFloat(),
                    curHeight.toFloat(),
                    mRect.right.toFloat(),
                    curHeight.toFloat(),
                    mPaint
                )
                curHeight += lineHeight
            }
        }
        super.onDraw(canvas)
    }
}