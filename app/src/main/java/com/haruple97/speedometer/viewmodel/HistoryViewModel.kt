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
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    override fun onCleared() {
        nativeAdLoader.destroy()
        super.onCleared()
    }
}
