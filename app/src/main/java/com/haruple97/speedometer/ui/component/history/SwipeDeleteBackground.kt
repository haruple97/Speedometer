package com.haruple97.speedometer.ui.component.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.haruple97.speedometer.ui.theme.DashboardDarkGray
import com.haruple97.speedometer.ui.theme.DigitalWhite
import com.haruple97.speedometer.ui.theme.GaugeDanger
import com.haruple97.speedometer.ui.theme.SpeedometerTextStyle

/**
 * 트립 항목을 오른쪽 → 왼쪽으로 swipe 할 때 뒤에서 노출되는 배경.
 * 빨간색 + 휴지통 아이콘 + "삭제" 라벨로 사용자에게 동작을 시각적으로 알려준다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeDeleteBackground(state: SwipeToDismissBoxState) {
    val active = state.dismissDirection == SwipeToDismissBoxValue.EndToStart
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(if (active) GaugeDanger else DashboardDarkGray)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
    ) {
        Icon(
            imageVector = Icons.Filled.Delete,
            contentDescription = null,
            tint = DigitalWhite,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "삭제",
            style = SpeedometerTextStyle.H4RegularStyle(),
            color = DigitalWhite,
        )
    }
}
