package com.haruple97.speedometer.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.haruple97.speedometer.ui.theme.DashboardBlack
import com.haruple97.speedometer.ui.theme.DigitalWhite
import com.haruple97.speedometer.ui.theme.GaugeDanger
import com.haruple97.speedometer.ui.theme.GaugeSafe
import com.haruple97.speedometer.ui.theme.SpeedometerTextStyle
import com.haruple97.speedometer.ui.theme.SpeedometerTheme

/**
 * 수동 기록 모드에서 사용자가 트립을 시작/정지하는 단일 토글 버튼.
 * 자동 기록 토글이 OFF 일 때만 메인 화면에 노출된다.
 */
@Composable
fun RecordingControlButton(
    isRecording: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val container = if (isRecording) GaugeDanger else GaugeSafe
    val content = if (isRecording) DigitalWhite else DashboardBlack
    val label = if (isRecording) "기록 중지" else "기록 시작"

    Button(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minWidth = 160.dp, minHeight = 48.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = container,
            contentColor = content,
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = 20.dp,
            vertical = 8.dp,
        ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // 좌측 인디케이터: 시작 시 ●(원), 중지 시 ■(라운드 사각).
            if (isRecording) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(content)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(content)
                )
            }
            Text(
                text = label,
                style = SpeedometerTextStyle.H4RegularStyle(),
                color = content,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun RecordingControlButtonIdlePreview() {
    SpeedometerTheme {
        RecordingControlButton(
            isRecording = false,
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
private fun RecordingControlButtonActivePreview() {
    SpeedometerTheme {
        RecordingControlButton(
            isRecording = true,
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
