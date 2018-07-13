package com.crepetete.steamachievements.ui.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.crepetete.steamachievements.R


class CircularProgressBar(context: Context, attrs: AttributeSet) : View(context, attrs) {
    /**
     * ProgressBar's line thickness
     */
    private var strokeWidth = 4f
    private var progress = 0f
    private var min = 0
    private var max = 100
    /**
     * Start the progress at 12 o'clock
     */
    private val startAngle = -90f
    private var color = ContextCompat.getColor(context, R.color.colorAccent)
    private var rectF: RectF = RectF()
    private var backgroundPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var foregroundPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val animationListeners = mutableListOf<ValueAnimator.AnimatorUpdateListener>()

    init {
        val typedArray = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.CircleProgressBar,
                0, 0)

        //Reading values from the XML layout
        try {
            strokeWidth = typedArray.getDimension(
                    R.styleable.CircleProgressBar_progressBarThickness,
                    strokeWidth)
            progress = typedArray.getFloat(R.styleable.CircleProgressBar_progress, progress)
            color = typedArray.getInt(R.styleable.CircleProgressBar_progressbarColor, color)
            min = typedArray.getInt(R.styleable.CircleProgressBar_min, min)
            max = typedArray.getInt(R.styleable.CircleProgressBar_max, max)
        } finally {
            typedArray.recycle()
        }

        backgroundPaint.color = adjustAlpha(color, 0.3f)
        backgroundPaint.style = Paint.Style.STROKE
        backgroundPaint.strokeWidth = strokeWidth

        foregroundPaint.color = color
        foregroundPaint.style = Paint.Style.STROKE
        foregroundPaint.strokeWidth = strokeWidth
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawOval(rectF, backgroundPaint)
        val angle = 360 * progress / max
        canvas.drawArc(rectF, startAngle, angle, false, foregroundPaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val height = View.getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)
        val width = View.getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        val min = Math.min(width, height)
        setMeasuredDimension(min, min)
        rectF.set(0 + strokeWidth / 2,
                0 + strokeWidth / 2,
                min - strokeWidth / 2,
                min - strokeWidth / 2)
    }

    fun addListener(listener: ValueAnimator.AnimatorUpdateListener) {
        animationListeners.add(listener)
    }

    /**
     * Set the progress with an animation.
     *
     * @param progress The progress it should animate to it.
     */
    fun setProgressWithAnimation(progress: Float) {
        val valueAnimator = ValueAnimator.ofFloat(this.progress, progress)
        valueAnimator.interpolator = DecelerateInterpolator() // increase the speed first and then decrease
        valueAnimator.duration = 300
        valueAnimator.addUpdateListener { animation ->
            val p = animation.animatedValue as Float
            this.setProgress(p)
            animationListeners.forEach {
                it.onAnimationUpdate(animation)
            }
        }
        valueAnimator.start()
    }

    /**
     * Transparent the given color by the factor
     * The more the factor closer to zero the more the color gets transparent
     *
     * @param color  The color to transparent
     * @param factor 1.0f to 0.0f
     * @return int - A transplanted color
     */
    private fun adjustAlpha(color: Int, factor: Float): Int {
        val alpha = Math.round(Color.alpha(color) * factor)
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }

    private fun setProgress(progress: Float) {
        this.progress = progress
        invalidate()
    }
}