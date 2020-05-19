package moe.shizuku.manager.ktx

import android.graphics.Canvas
import android.widget.EdgeEffect
import androidx.recyclerview.widget.RecyclerView

class FixedAlwaysClipToPaddingEdgeEffectFactory(
        private val paddingLeft: Int,
        private val paddingTop: Int,
        private val paddingRight: Int,
        private val paddingBottom: Int
) : RecyclerView.EdgeEffectFactory() {


    override fun createEdgeEffect(view: RecyclerView, direction: Int): EdgeEffect {

        return object : EdgeEffect(view.context) {
            private var ensureSize = false

            private fun ensureSize() {
                if (ensureSize) return
                ensureSize = true

                when (direction) {
                    DIRECTION_LEFT -> {
                        setSize(view.measuredHeight - paddingTop - paddingBottom,
                                view.measuredWidth - paddingLeft - paddingRight)
                    }
                    DIRECTION_TOP -> {
                        setSize(view.measuredWidth - paddingLeft - paddingRight,
                                view.measuredHeight - paddingTop - paddingBottom)
                    }
                    DIRECTION_RIGHT -> {
                        setSize(view.measuredHeight - paddingTop - paddingBottom,
                                view.measuredWidth - paddingLeft - paddingRight)
                    }
                    DIRECTION_BOTTOM -> {
                        setSize(view.measuredWidth - paddingLeft - paddingRight,
                                view.measuredHeight - paddingTop - paddingBottom)
                    }
                }
            }

            override fun draw(c: Canvas): Boolean {
                ensureSize()

                val restore = c.save()
                when (direction) {
                    DIRECTION_LEFT -> {
                        c.translate(paddingBottom.toFloat(), 0f)
                    }
                    DIRECTION_TOP -> {
                        c.translate(paddingLeft.toFloat(), paddingTop.toFloat())
                    }
                    DIRECTION_RIGHT -> {
                        c.translate(-paddingTop.toFloat(), 0f)
                    }
                    DIRECTION_BOTTOM -> {
                        c.translate(paddingRight.toFloat(), paddingBottom.toFloat())
                    }
                }
                val res = super.draw(c)
                c.restoreToCount(restore)
                return res
            }
        }
    }
}

