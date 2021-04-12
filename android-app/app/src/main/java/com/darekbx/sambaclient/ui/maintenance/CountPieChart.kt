package com.darekbx.sambaclient.ui.maintenance

import android.content.Context
import android.util.AttributeSet
import com.darekbx.sambaclient.R
import com.darekbx.sambaclient.statistics.TypeStatistic

class CountPieChart(context: Context, attributeSet: AttributeSet?) :
    TypePieChart(context, attributeSet) {

    override fun getPercentValueFor(typeStatistic: TypeStatistic) =
        typeStatistic.count.toLong() * 100F / sumCount

    override fun getColorFor(typeStatistic: TypeStatistic): Int {
        return when (typeStatistic.fileType) {
            TypeStatistic.TYPE_DOCUMENTS -> context.getColor(R.color.chart_doc)
            TypeStatistic.TYPE_IMAGES -> context.getColor(R.color.chart_image)
            TypeStatistic.TYPE_MOVIES -> context.getColor(R.color.chart_movie)
            TypeStatistic.TYPE_ARCHIVES -> context.getColor(R.color.chart_archive)
            else -> context.getColor(R.color.chart_other)
        }
    }
}
