package com.haruple97.speedometer.ui.component.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.haruple97.speedometer.data.settings.DistanceUnit
import com.haruple97.speedometer.data.trip.SpeedZoneStat
import com.haruple97.speedometer.ui.theme.DigitalWhite
import com.haruple97.speedometer.ui.theme.GaugeCaution
import com.haruple97.speedometer.ui.theme.GaugeCritical
import com.haruple97.speedometer.ui.theme.GaugeDanger
import com.haruple97.speedometer.ui.theme.GaugeSafe
import com.haruple97.speedometer.ui.theme.GaugeTrack
import com.haruple97.speedometer.ui.theme.SpeedometerTextStyle
import com.haruple97.speedometer.ui.theme.UnitGray
import kotlin.math.roundToInt

/**
 * 속도 구간별 누적 거리 도넛. 중앙에 총 거리, 하단에 2열 그리드 범례(값 + %).
 */
@Composable
fun SpeedZonePie(
    stat: SpeedZoneStat,
    distanceUnit: DistanceUnit,
    modifier: Modifier = Modifier,
) {
    val slices = listOf(
        SliceData("안전", "<120 km/h", stat.safeMeters, GaugeSafe),
        SliceData("주의", "120–200", stat.cautionMeters, GaugeCaution),
        SliceData("위험", "200–280", stat.dangerMeters, GaugeDanger),
        SliceData("극한", "≥280", stat.criticalMeters, GaugeCritical),
    )
    val total = stat.totalMeters.coerceAtLeast(1f)
    val totalDisplay = distanceUnit.fromKm(stat.totalMeters / 1000f)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(160.dp),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.size(160.dp)) {
                val strokeWidth = 22.dp.toPx()
                val diameter = size.minDimension - strokeWidth
                val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
                val arcSize = Size(diameter, diameter)

                drawArc(
                    color = GaugeTrack,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth),
                )

                var current = -90f
                slices.forEach { slice ->
                    if (slice.meters <= 0f) return@forEach
                    val sweep = 360f * (slice.meters / total)
                    drawArc(
                        color = slice.color,
                        startAngle = current,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth),
                    )
                    current += sweep
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = formatDistance(totalDisplay),
                    style = SpeedometerTextStyle.Data2Style(),
                    color = DigitalWhite,
                )
                Text(
                    text = "총 ${distanceUnit.label}",
                    style = SpeedometerTextStyle.CaptionRegularStyle(),
                    color = UnitGray,
                )
            }
        }

        // 2열 그리드 범례
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            slices.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    row.forEach { slice ->
                        LegendItem(
                            slice = slice,
                            totalMeters = total,
                            distanceUnit = distanceUnit,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (row.size == 1) {
                        androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

private data class SliceData(
    val label: String,
    val rangeText: String,
    val meters: Float,
    val color: Color,
)

@Composable
private fun LegendItem(
    slice: SliceData,
    totalMeters: Float,
    distanceUnit: DistanceUnit,
    modifier: Modifier = Modifier,
) {
    val percent = (slice.meters / totalMeters * 100f).roundToInt()
    val km = slice.meters / 1000f
    val distance = distanceUnit.fromKm(km)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(slice.color),
        )
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = slice.label,
                    style = SpeedometerTextStyle.Body2Style(),
                    color = DigitalWhite,
                )
                Text(
                    text = "${percent}%",
                    style = SpeedometerTextStyle.CaptionStyle(),
                    color = UnitGray,
                )
            }
            Text(
                text = "${formatDistance(distance)} ${distanceUnit.label} · ${slice.rangeText}",
                style = SpeedometerTextStyle.CaptionRegularStyle(),
                color = UnitGray,
            )
        }
    }
}

private fun formatDistance(value: Float): String =
    if (value >= 100f) value.roundToInt().toString() else "%.1f".format(value)
