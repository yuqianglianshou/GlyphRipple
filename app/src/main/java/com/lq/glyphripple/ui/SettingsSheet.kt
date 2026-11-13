package com.lq.glyphripple.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    settings: Settings,
    onChange: (Settings) -> Unit,
    onPlay: (text: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var draft by rememberSaveable { mutableStateOf(settings.text) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("文字") },
                    maxLines = 3,
                )
                Button(
                    onClick = { onPlay(draft) },
                    modifier = Modifier.padding(start = 12.dp),
                    enabled = draft.isNotBlank(),
                ) {
                    Text("播放")
                }
            }

            TwoChoice(
                label = "排布",
                first = "逐字随机",
                second = "整句排版",
                secondSelected = settings.sentence,
                onSelect = { onChange(settings.copy(sentence = it)) },
            )
            TwoChoice(
                label = "字形",
                first = "系统字体",
                second = "经典点阵",
                secondSelected = settings.classic,
                onSelect = { onChange(settings.copy(classic = it)) },
            )

            Column {
                Label("配色")
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Palettes.forEachIndexed { index, palette ->
                        FilterChip(
                            selected = settings.palette == index,
                            onClick = { onChange(settings.copy(palette = index)) },
                            label = { Text(palette.name) },
                        )
                    }
                }
            }

            Column {
                Label("速度  ${"%.1f".format(settings.speed)}×")
                Slider(
                    value = settings.speed,
                    onValueChange = { onChange(settings.copy(speed = it)) },
                    valueRange = Settings.MIN_SPEED..Settings.MAX_SPEED,
                )
            }

            Column {
                Label(
                    if (settings.classic) "密度  24（经典点阵固定）" else "密度  ${settings.grid}",
                )
                Slider(
                    value = settings.grid.toFloat(),
                    onValueChange = { onChange(settings.copy(grid = it.roundToInt())) },
                    valueRange = Settings.MIN_GRID.toFloat()..Settings.MAX_GRID.toFloat(),
                    enabled = !settings.classic,
                )
            }

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("循环播放", Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                Switch(checked = settings.loop, onCheckedChange = { onChange(settings.copy(loop = it)) })
            }
        }
    }
}

@Composable
private fun Label(text: String) {
    Text(
        text,
        Modifier.padding(bottom = 4.dp),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun TwoChoice(
    label: String,
    first: String,
    second: String,
    secondSelected: Boolean,
    onSelect: (second: Boolean) -> Unit,
) {
    Column {
        Label(label)
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = !secondSelected,
                onClick = { onSelect(false) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            ) { Text(first) }
            SegmentedButton(
                selected = secondSelected,
                onClick = { onSelect(true) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            ) { Text(second) }
        }
    }
}
