package com.darekbx.sambaclient.ui.maintenance

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.darekbx.sambaclient.ui.statistics.TypeStatistic

abstract class TypePieChart(context: Context, attributeSet: AttributeSet?) :
    View(context, attributeSet) {

    private var data = listOf<TypeStatistic>()

    abstract fun getPercentValueFor(typeStatistic: TypeStatistic): Float

    abstract fun getColorFor(typeStatistic: TypeStatistic): Int

    fun invalidateWithData(data: List<TypeStatistic>) {
        this.data = data
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas != null) {
            canvas.drawOval(chartArea, borderPaint)
            keepOvalDrawings(canvas)
            drawPieParts(canvas)
        }
    }

    private fun keepOvalDrawings(canvas: Canvas) {
        val clipPath = Path().apply {
            moveTo(chartArea.centerX(), chartArea.centerY())
            addOval(chartArea, Path.Direction.CW)
        }
        canvas.clipPath(clipPath)
    }

    private fun drawPieParts(canvas: Canvas) {
        var angleOffset = 0F
        val angles = mutableListOf<Float>()
        for (typeStatistic in data) {
            chartPaint.color = getColorFor(typeStatistic)
            val arcTo = getPercentValueFor(typeStatistic) * 3.6F

            canvas.drawArc(chartArea, angleOffset, arcTo, true, chartPaint)
            canvas.drawArc(chartArea, angleOffset, arcTo, true, borderPaint)

            angleOffset += arcTo
            angles.add(arcTo)
        }
    }

    private fun calculateChartRectange(): RectF {
        var xOffset = 0F
        var yOffset = 0F

        val size = when (width > height) {
            true -> {
                xOffset = (width - height) / 2F
                height
            }
            else -> {
                yOffset = (height - width) / 2F
                width
            }
        }.toFloat()

        val left = xOffset + paddingLeft
        val top = yOffset + paddingTop
        val right = size + xOffset - paddingRight
        val bottom = size + yOffset - paddingBottom

        return RectF(left, top, right, bottom)
    }

    private val chartArea by lazy { calculateChartRectange() }

    protected val sumCount by lazy { data.sumBy { it.count }.toFloat() }
    protected val sumSize by lazy { data.sumOf { it.overallSize }.toFloat() }

    private val borderPaint = Paint().apply {
        isAntiAlias = true
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 4F
    }

    private val chartPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }
}
