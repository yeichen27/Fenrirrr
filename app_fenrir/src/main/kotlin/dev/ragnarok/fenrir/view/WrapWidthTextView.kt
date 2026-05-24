package dev.ragnarok.fenrir.view

import android.content.Context
import android.graphics.text.LineBreaker
import android.text.Layout
import android.util.AttributeSet
import com.google.android.material.textview.MaterialTextView
import dev.ragnarok.fenrir.R
import kotlin.math.ceil

open class WrapWidthTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : MaterialTextView(context, attrs) {
    private var mFixWrapText = false
    private fun init(context: Context, attributeSet: AttributeSet?) {
        val a =
            context.theme.obtainStyledAttributes(attributeSet, R.styleable.WrapWidthTextView, 0, 0)
        mFixWrapText = try {
            a.getBoolean(R.styleable.WrapWidthTextView_fixWrapText, false)
        } finally {
            a.recycle()
        }
        if (mFixWrapText) {
            breakStrategy = LineBreaker.BREAK_STRATEGY_SIMPLE
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (!mFixWrapText) {
            return
        }
        val textLayout = layout ?: return
        if (textLayout.lineCount < 2) return
        val width =
            ceil(getMaxLineWidth(textLayout)).toInt() + compoundPaddingLeft + compoundPaddingRight
        val height = measuredHeight
        setMeasuredDimension(width, height)
    }

    private fun getMaxLineWidth(textLayout: Layout): Float {
        var maxWidth = 0.0f
        val lines = textLayout.lineCount
        for (i in 0 until lines) {
            val wi = textLayout.getLineWidth(i)
            if (wi > maxWidth) {
                maxWidth = wi
            }
        }
        return maxWidth
    }

    init {
        init(context, attrs)
    }
}