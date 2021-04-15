package info.guardianproject.notepadbot

import android.view.View
import android.view.animation.Animation
import android.view.animation.GridLayoutAnimationController
import android.view.animation.LinearInterpolator

class StaggeredGridLayoutAnimationController(
    animation: Animation,
    private val horizontalPercentage: Float,
    private val verticalPercentage: Float,
) : GridLayoutAnimationController(animation) {

    override fun getDelayForView(view: View): Long {
        val params = view.layoutParams.layoutAnimationParameters as AnimationParameters
        val duration = mAnimation.duration
        mInterpolator = mInterpolator ?: LinearInterpolator()

        val horizontalPercentage = horizontalPercentage * duration
        val verticalPercentage = verticalPercentage * duration
        val delay = ((view.x / params.parentWidth) * horizontalPercentage) * 0.5F +
                ((view.y / params.parentHeight) * verticalPercentage) * 0.5F

        return mInterpolator.getInterpolation(delay).toLong()
    }

    class AnimationParameters : GridLayoutAnimationController.AnimationParameters() {
        var parentWidth: Int = -1
        var parentHeight: Int = -1
    }
}