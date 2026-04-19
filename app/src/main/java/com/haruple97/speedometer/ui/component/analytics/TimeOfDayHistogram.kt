package com.haruple97.speedometer.ui.component.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.haruple97.speedometer.data.trip.HourStat
import com.haruple97.speedometer.ui.theme.GaugeSafe
import com.haruple97.speedometer.ui.theme.SpeedometerTextStyle
import com.haruple97.speedometer.ui.theme.TickMinor
import com.haruple97.speedometer.ui.theme.UnitGray
import com.haruple97.speedometer.ui.util.GaugeGeometry

/**
 * 시간대별 주행 빈도 히스토그램. 막대 색은 해당 시간 평균 속도에 따라 grade.
 * 가장 활동적인 시간대는 하이라이트 + 하단 요약.
 */
@Composable
fun TimeOfDayHistogram(
    data: List<HourStat>,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = SpeedometerTextStyle.CaptionRegularStyle().copy(color = UnitGray)

    val byHour = (0..23).map { h -> data.firstOrNull { it.hour == h } }
    val maxCount = (byHour.mapNotNull { it?.tripCount }.maxOrNull() ?: 0).coerceAtLeast(1)
    val peak = data.maxByOrNull { it.tripCount }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Canvas(
            modifier = modifier
                .fillMaxWidth()
                .height(130.dp),
        ) {
            val leftPad = 12.dp.toPx()
            val rightPad = 12.dp.toPx()
            val topPad = 10.dp.toPx()
            val bottomPad = 24.dp.toPx()

            val plotLeft = leftPad
            val plotRight = size.width - rightPad
            val plotTop = topPad
            val plotBottom = size.height - bottomPad
            val plotW = plotRight - plotLeft
            val plotH = plotBottom - plotTop

            val slot = plotW / 24f
            val barWidth = slot * 0.55f

            // baseline
            drawLine(
                color = TickMinor,
                start = Offset(plotLeft, plotBottom),
                end = Offset(plotRight, plotBottom),
                strokeWidth = 1.dp.toPx(),
            )

            byHour.forEachIndexed { hour, stat ->
                val cx = plotLeft + slot * hour + slot / 2f
                val count = stat?.tripCount ?: 0
                val h = plotH * (count.toFloat() / maxCount)
                if (h > 0f) {
                    val color = GaugeGeometry.speedToColor(stat?.avgSpeedKmh ?: 0f)
                    drawRect(
                        color = color,
                        topLeft = Offset(cx - barWidth / 2f, plotBottom - h),
                        size = Size(barWidth, h),
                    )
                }

                // 3시간 간격 라벨
                if (hour % 3 == 0) {
                    val layout = textMeasurer.measure(hour.toString(), labelStyle)
                    drawText(
                        textLayoutResult = layout,
                        topLeft = Offset(
                            cx - layout.size.width / 2f,
                            size.height - 4.dp.toPx() - layout.size.height,
                        ),
                    )
                }
            }
        }

        if (peak != null && peak.tripCount >= 1) {
            Text(
                text = "가장 활동적인 시간대: ${peak.hour}시 (${peak.tripCount}회)",
                style = SpeedometerTextStyle.CaptionRegularStyle(),
                color = GaugeSafe,
            )
        }
    }
}
