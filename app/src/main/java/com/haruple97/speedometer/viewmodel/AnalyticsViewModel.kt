package com.haruple97.speedometer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.haruple97.speedometer.data.database.DatabaseProvider
import com.haruple97.speedometer.data.trip.ComparisonPair
import com.haruple97.speedometer.data.trip.DayStat
import com.haruple97.speedometer.data.trip.HourStat
import com.haruple97.speedometer.data.trip.SpeedZoneStat
import com.haruple97.speedometer.data.trip.TripAggregate
import com.haruple97.speedometer.data.trip.TripEntity
import com.haruple97.speedometer.data.trip.WeekStat
import com.haruple97.speedometer.data.trip.WeekdayStat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

data class AnalyticsUiState(
    val loading: Boolean = true,
    val weekday: List<WeekdayStat> = emptyList(),
    val hour: List<HourStat> = emptyList(),
    val monthDays: List<DayStat> = emptyList(),
    val speedZones: SpeedZoneStat = SpeedZoneStat(0f, 0f, 0f, 0f),
    val topDistanceTrip: TripEntity? = null,
    val topMaxSpeedTrip: TripEntity? = null,
    val topDurationTrip: TripEntity? = null,
    val topOverspeedTrip: TripEntity? = null,
    // 업그레이드 섹션
    val thisMonth: TripAggregate = TripAggregate.EMPTY,
    val total: TripAggregate = TripAggregate.EMPTY,
    val weeklyTrend: List<WeekStat> = emptyList(),
    val weekComparison: ComparisonPair? = null,
) {
    val isEmpty: Boolean
        get() = total.tripCount == 0
}

class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = DatabaseProvider.get(application).tripDao()

    private val _state = MutableStateFlow(AnalyticsUiState())
    val state: StateFlow<AnalyticsUiState> = _state.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            val weekday = dao.aggregateByWeekday()
            val hour = dao.aggregateByHour()
            val monthStart = currentMonthStartMs()
            val monthDays = dao.aggregateByDay(monthStart)
            val zones = computeSpeedZones()

            val total = dao.totalAggregate()
            val thisMonth = dao.rangeAggregate(monthStart, System.currentTimeMillis() + 1)

            val eightWeeksAgo = System.currentTimeMillis() - (7L * 24 * 3600 * 1000 * 8)
            val weeklyTrend = dao.aggregateByWeek(eightWeeksAgo)

            val (thisWeekStart, thisWeekEnd) = thisWeekRange()
            val (lastWeekStart, lastWeekEnd) = lastWeekRange()
            val weekComparison = ComparisonPair(
                current = dao.rangeAggregate(thisWeekStart, thisWeekEnd),
                previous = dao.rangeAggregate(lastWeekStart, lastWeekEnd),
            )

            _state.value = AnalyticsUiState(
                loading = false,
                weekday = weekday,
                hour = hour,
                monthDays = monthDays,
                speedZones = zones,
                topDistanceTrip = dao.topDistanceTrip(),
                topMaxSpeedTrip = dao.topMaxSpeedTrip(),
                topDurationTrip = dao.topDurationTrip(),
                topOverspeedTrip = dao.topOverspeedTrip(),
                thisMonth = thisMonth,
                total = total,
                weeklyTrend = weeklyTrend,
                weekComparison = weekComparison,
            )
        }
    }

    private suspend fun computeSpeedZones(): SpeedZoneStat {
        val trips = dao.observeAllTrips().first()
        return trips.fold(SpeedZoneStat(0f, 0f, 0f, 0f)) { acc, trip ->
            val s = trip.avgSpeedKmh
            val d = trip.distanceMeters
            when {
                s < 120f -> acc.copy(safeMeters = acc.safeMeters + d)
                s < 200f -> acc.copy(cautionMeters = acc.cautionMeters + d)
                s < 280f -> acc.copy(dangerMeters = acc.dangerMeters + d)
                else -> acc.copy(criticalMeters = acc.criticalMeters + d)
            }
        }
    }

    private fun currentMonthStartMs(): Long = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun thisWeekRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            val dow = get(Calendar.DAY_OF_WEEK)
            val daysFromMonday = (dow - Calendar.MONDAY + 7) % 7
            add(Calendar.DAY_OF_MONTH, -daysFromMonday)
        }
        val start = cal.timeInMillis
        val end = start + 7L * 24 * 3600 * 1000
        return start to end
    }

    private fun lastWeekRange(): Pair<Long, Long> {
        val (thisStart, _) = thisWeekRange()
        val lastStart = thisStart - 7L * 24 * 3600 * 1000
        return lastStart to thisStart
    }
}
