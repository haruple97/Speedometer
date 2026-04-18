package com.haruple97.speedometer.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.haruple97.speedometer.ui.theme.DashboardBlack
import com.haruple97.speedometer.ui.theme.DashboardDarkGray
import com.haruple97.speedometer.ui.theme.DigitalWhite
import com.haruple97.speedometer.ui.theme.SpeedometerTheme
import com.haruple97.speedometer.ui.theme.UnitGray

@Composable
fun SettingsRoute(
    onNavigateBack: () -> Unit
) {
    SettingsScreen(onNavigateBack = onNavigateBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        modifier = Modifier.background(DashboardBlack),
        containerColor = DashboardBlack,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "설정", color = DigitalWhite)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로",
                            tint = DigitalWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DashboardDarkGray,
                    titleContentColor = DigitalWhite,
                    navigationIconContentColor = DigitalWhite
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DashboardBlack)
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // TODO: 최대 속도 조절 슬라이더 (GaugeGeometry.MAX_SPEED 대체)
            Text(
                text = "최대 속도",
                style = MaterialTheme.typography.titleMedium,
                color = DigitalWhite,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Text(
                text = "추후 추가 예정",
                style = MaterialTheme.typography.bodySmall,
                color = UnitGray
            )

            // TODO: 속도 변화 그래프 표시 토글 / 기간 선택
            Text(
                text = "그래프",
                style = MaterialTheme.typography.titleMedium,
                color = DigitalWhite,
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
            )
            Text(
                text = "추후 추가 예정",
                style = MaterialTheme.typography.bodySmall,
                color = UnitGray
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun SettingsScreenPreview() {
    SpeedometerTheme {
        SettingsScreen(onNavigateBack = {})
    }
}
