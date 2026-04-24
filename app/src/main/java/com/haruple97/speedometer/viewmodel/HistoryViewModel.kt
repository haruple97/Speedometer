package com.haruple97.speedometer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.ads.nativead.NativeAd
import com.haruple97.speedometer.data.ads.AdUnitIds
import com.haruple97.speedometer.data.ads.MobileAdsInitializer
import com.haruple97.speedometer.data.ads.NativeAdLoader
import com.haruple97.speedometer.data.database.DatabaseProvider
import com.haruple97.speedometer.data.premium.PremiumFeature
import com.haruple97.speedometer.data.premium.PremiumUnlockRepository
import com.haruple97.speedometer.data.premium.UnlockState
import com.haruple97.speedometer.data.trip.SummaryPeriod
import com.haruple97.speedometer.data.trip.TripAggregate
import com.haruple97.speedometer.data.trip.TripEntity
import com.haruple97.speedometer.util.AppScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = DatabaseProvider.get(application).tripDao()
    private val premiumRepo = PremiumUnlockRepository(application)

    val trips: StateFlow<List<TripEntity>> = dao.observeAllTrips()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = emptyList(),
        )

    /** swipe 직후 화면에서는 즉시 숨기되 5초 Undo 윈도우 동안 commit 을 보류하는 트립 id 집합. */
    private val pendingDeletions = MutableStateFlow<Set<Long>>(emptySet())
    private val commitJobs = mutableMapOf<Long, Job>()

    /** UI 가 실제로 표시할 트립. pending 인 id 는 즉시 숨김. */
    val visibleTrips: StateFlow<List<TripEntity>> =
        combine(trips, pendingDeletions) { all, pending -> all.filterNot { it.id in pending } }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = emptyList(),
            )

    /** Undo 가 가능한 가장 최근 pending 트립. UI 의 Snackbar 트리거. */
    private val _lastPendingTripId = MutableStateFlow<Long?>(null)
    val lastPendingTripId: StateFlow<Long?> = _lastPendingTripId.asStateFlow()

    private val _selectedPeriod = MutableStateFlow(SummaryPeriod.THIS_WEEK)
    val selectedPeriod: StateFlow<SummaryPeriod> = _selectedPeriod.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val summary: StateFlow<TripAggregate> = combine(_selectedPeriod, trips) { period, _ -> period }
        .flatMapLatest { period ->
            flow {
                val fromMs = period.fromEpochMs(System.currentTimeMillis())
                emit(dao.aggregate(fromMs))
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = TripAggregate.EMPTY,
        )

    private val nativeAdLoader = NativeAdLoader(
        context = application,
        adUnitId = AdUnitIds.nativeRecordList,
        count = 5,
    )
    val nativeAds: StateFlow<List<NativeAd>> = nativeAdLoader.ads

    val analyticsUnlockState: StateFlow<UnlockState> =
        premiumRepo.observeUnlockState(PremiumFeature.AdvancedAnalytics)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = UnlockState.Locked,
            )

    init {
        viewModelScope.launch {
            MobileAdsInitializer.initialized.filter { it }.first()
            nativeAdLoader.load()
        }
    }

    fun selectPeriod(period: SummaryPeriod) {
        _selectedPeriod.value = period
    }

    /** 보상형 전면 광고 시청 완료 콜백에서 호출 — 24시간 언락. */
    fun grantAnalyticsUnlock() {
        viewModelScope.launch {
            premiumRepo.unlockForDuration(
                feature = PremiumFeature.AdvancedAnalytics,
                durationMs = 24L * 60 * 60 * 1000,
            )
        }
    }

    /**
     * 사용자가 트립을 swipe 로 삭제 요청. UI 에서 즉시 숨기고 [UNDO_WINDOW_MS] 후 실제 DELETE.
     * 새 삭제가 들어오면 직전 pending 은 즉시 commit (Snackbar 가 하나만 노출되도록 단순화).
     */
    fun requestDeleteTrip(tripId: Long) {
        flushPendingExcept(tripId)
        pendingDeletions.update { it + tripId }
        _lastPendingTripId.value = tripId
        commitJobs[tripId] = viewModelScope.launch {
            delay(UNDO_WINDOW_MS)
            commitDelete(tripId)
        }
    }

    /** Snackbar "실행 취소" 누름 — pending 취소 후 항목 복귀. */
    fun undoDelete(tripId: Long) {
        commitJobs.remove(tripId)?.cancel()
        pendingDeletions.update { it - tripId }
        if (_lastPendingTripId.value == tripId) _lastPendingTripId.value = null
    }

    /** Snackbar 가 Undo 없이 사라졌을 때 — UI 정리 시그널. commit 자체는 timer 가 처리. */
    fun clearLastPending() {
        _lastPendingTripId.value = null
    }

    private fun flushPendingExcept(keep: Long) {
        val toFlush = pendingDeletions.value - keep
        toFlush.forEach { id ->
            commitJobs.remove(id)?.cancel()
            viewModelScope.launch { commitDelete(id) }
        }
    }

    private suspend fun commitDelete(tripId: Long) {
        if (tripId !in pendingDeletions.value) return
        dao.deleteTrip(tripId)
        pendingDeletions.update { it - tripId }
        commitJobs.remove(tripId)
        if (_lastPendingTripId.value == tripId) _lastPendingTripId.value = null
    }

    override fun onCleared() {
        // ViewModel 해제 시 잔여 pending 은 AppScope 로 옮겨 안전 commit.
        val remaining = pendingDeletions.value
        if (remaining.isNotEmpty()) {
            AppScope.scope.launch { remaining.forEach { dao.deleteTrip(it) } }
        }
        nativeAdLoader.destroy()
        super.onCleared()
    }

    private companion object {
        const val UNDO_WINDOW_MS = 5_000L
    }
}
