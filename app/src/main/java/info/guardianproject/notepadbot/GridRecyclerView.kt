package info.guardianproject.notepadbot

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.GridLayoutAnimationController
import androidx.recyclerview.R
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

/**
 * https://proandroiddev.com/enter-animation-using-recyclerview-and-layoutanimation-part-2-grids-688829b1d29b
 */
class GridRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = R.attr.recyclerViewStyle
) : RecyclerView(context, attrs, defStyle) {
    override fun attachLayoutAnimationParameters(
        child: View?,
        params: ViewGroup.LayoutParams?,
        index: Int,
        count: Int
    ) {
        adapter ?: return

        when (val layoutManager = layoutManager) {
            is GridLayoutManager -> {
                val animationParameters = params?.layoutAnimationParameters
                        as GridLayoutAnimationController.AnimationParameters?
                    ?: GridLayoutAnimationController.AnimationParameters().also {
                        params!!.layoutAnimationParameters = it
                    }

                animationParameters.count = count
                animationParameters.index = index

                val columns = layoutManager.spanCount
                animationParameters.columnsCount = columns
                animationParameters.rowsCount = count / columns

                val invertedIndex = count - 1 - index
                animationParameters.column = columns - 1 - (invertedIndex % columns)
                animationParameters.rowsCount = count / columns
            }
            is StaggeredGridLayoutManager -> {
                val animationParameters = params?.layoutAnimationParameters
                        as StaggeredGridLayoutAnimationController.AnimationParameters?
                    ?: StaggeredGridLayoutAnimationController.AnimationParameters().also {
                        params!!.layoutAnimationParameters = it
                    }

                animationParameters.parentHeight = height
                animationParameters.parentWidth = width
            }
            else -> super.attachLayoutAnimationParameters(child, params, index, count)
        }
    }
}