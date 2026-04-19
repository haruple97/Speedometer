package com.haruple97.speedometer.ui.component.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.haruple97.speedometer.data.settings.DistanceUnit
import com.haruple97.speedometer.data.trip.DayStat
import com.haruple97.speedometer.ui.theme.DashboardBlack
import com.haruple97.speedometer.ui.theme.DashboardDarkGray
import com.haruple97.speedometer.ui.theme.DigitalWhite
import com.haruple97.speedometer.ui.theme.GaugeSafe
import com.haruple97.speedometer.ui.theme.SpeedometerTextStyle
import com.haruple97.speedometer.ui.theme.UnitGray
import java.util.Calendar
import kotlin.math.roundToInt

private val DAY_LABELS = arrayOf("일", "월", "화", "수", "목", "금", "토")

/**
 * 이번 달 일별 주행 거리 히트맵.
 * - 셀 배경 강도 = 당일 주행 거리 / 최대 거리
 * - 오늘 셀에 GaugeSafe 테두리
 * - 요일 라벨 7개 전부, 범례 바, 월 요약
 */
@Composable
fun MonthHeatmap(
    dayStats: List<DayStat>,
    distanceUnit: DistanceUnit,
    modifier: Modifier = Modifier,
) {
    val labelStyle = SpeedometerTextStyle.CaptionRegularStyle().copy(color = UnitGray)
    val cellLabelStyle = SpeedometerTextStyle.CaptionRegularStyle()

    val now = Calendar.getInstance()
    val today = now.get(Calendar.DAY_OF_MONTH)
    val currentMonth = now.get(Calendar.MONTH)
    val daysInMonth = now.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayWeekIndex = run {
        val cal = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1) }
        cal.get(Calendar.DAY_OF_WEEK) - 1
    }
    val totalMonthMeters = dayStats.sumOf { it.totalDistanceMeters.toDouble() }.toFloat()
    val totalDisplay = distanceUnit.fromKm(totalMonthMeters / 1000f)
    val maxDistance = (dayStats.maxOfOrNull { it.totalDistanceMeters } ?: 0f).coerceAtLeast(1f)
    val byDay: Map<Int, DayStat> = dayStats.associateBy { stat ->
        val cal = Calendar.getInstance().apply { timeInMillis = stat.dayEpochMs }
        cal.get(Calendar.DAY_OF_MONTH)
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // 요일 라벨
        Row(modifier = Modifier.fillMaxWidth()) {
            DAY_LABELS.forEach { d ->
                Text(
                    text = d,
                    style = labelStyle,
                    color = UnitGray,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }

        // 6행 × 7열 그리드 — 요일 헤더와 동일한 weight(1f) 를 써야 열이 정렬된다.
        val cellShape = RoundedCornerShape(4.dp)
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            repeat(6) { rowIdx ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    repeat(7) { colIdx ->
                        val cellIndex = rowIdx * 7 + colIdx
                        val day = cellIndex - firstDayWeekIndex + 1
                        val inRange = day in 1..daysInMonth
                        val stat = if (inRange) byDay[day] else null
                        val intensity = stat?.let { it.totalDistanceMeters / maxDistance } ?: 0f

                        val cellModifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .let { m ->
                                if (!inRange) {
                                    m
                                } else {
                                    val bg = if (intensity > 0f) {
                                        lerpColor(DashboardDarkGray, GaugeSafe, intensity.coerceIn(0.15f, 1f))
                                    } else {
                                        DashboardDarkGray
                                    }
                                    val withBg = m.background(bg, cellShape)
                                    if (day == today && now.get(Calendar.MONTH) == currentMonth) {
                                        withBg.border(2.dp, GaugeSafe, cellShape)
                                    } else {
                                        withBg
                                    }
                                }
                            }

                        Box(
                            modifier = cellModifier,
                            contentAlignment = Alignment.Center,
                        ) {
                            if (inRange) {
                                Text(
                                    text = day.toString(),
                                    style = cellLabelStyle,
                                    color = if (intensity > 0.55f) DashboardBlack else UnitGray,
                                )
                            }
                        }
                    }
                }
            }
        }

        // 범례 바 + 요약
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(text = "적음", style = labelStyle, color = UnitGray)
                listOf(0.15f, 0.4f, 0.65f, 0.9f).forEach { t ->
                    Box(
                        modifier = Modifier
                            .size(width = 14.dp, height = 10.dp),
                    ) {
                        Canvas(Modifier.size(14.dp, 10.dp)) {
                            drawRoundRect(
                                color = lerpColor(DashboardDarkGray, GaugeSafe, t),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                                    3.dp.toPx(), 3.dp.toPx(),
                                ),
                            )
                        }
                    }
                }
                Text(text = "많음", style = labelStyle, color = UnitGray)
            }
            Text(
                text = "이번 달 총 ${formatDistance(totalDisplay)} ${distanceUnit.label}",
                style = SpeedometerTextStyle.CaptionStyle(),
                color = DigitalWhite,
            )
        }
    }
}

private fun lerpColor(from: Color, to: Color, t: Float): Color {
    val c = t.coerceIn(0f, 1f)
    return Color(
        red = from.red + (to.red - from.red) * c,
        green = from.green + (to.green - from.green) * c,
        blue = from.blue + (to.blue - from.blue) * c,
        alpha = from.alpha + (to.alpha - from.alpha) * c,
    )
}

private fun formatDistance(value: Float): String =
    if (value >= 100f) value.roundToInt().toString() else "%.1f".format(value)
