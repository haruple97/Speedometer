package com.haruple97.speedometer.ui.component.premium

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.haruple97.speedometer.ui.theme.DashboardDarkGray
import com.haruple97.speedometer.ui.theme.DigitalWhite
import com.haruple97.speedometer.ui.theme.GaugeSafe
import com.haruple97.speedometer.ui.theme.SpeedometerTextStyle
import com.haruple97.speedometer.ui.theme.UnitGray

/**
 * 프리미엄 언락을 제안하는 공용 다이얼로그.
 *
 * 광고 시청 이전의 앱-측 opt-in 단계로, 보상의 맥락·혜택을 사용자에게 명확히 전달.
 * 확인 시 [onConfirm] 호출(실제 광고 노출은 호출자가 담당).
 */
@Composable
fun UnlockPromptDialog(
    title: String,
    message: String,
    confirmText: String = "광고 시청",
    dismissText: String = "다음에",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DashboardDarkGray,
        titleContentColor = DigitalWhite,
        textContentColor = DigitalWhite,
        title = {
            Text(
                text = title,
                style = SpeedometerTextStyle.H3Style(),
                color = DigitalWhite,
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = message,
                    style = SpeedometerTextStyle.Body1RegularStyle(),
                    color = UnitGray,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = confirmText, color = GaugeSafe)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = dismissText, color = UnitGray)
            }
        },
    )
}
