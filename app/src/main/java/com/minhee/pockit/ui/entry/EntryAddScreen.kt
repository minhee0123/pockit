package com.minhee.pockit.ui.entry

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minhee.pockit.domain.model.Tag
import com.minhee.pockit.ui.theme.LossLavender
import com.minhee.pockit.ui.theme.ProfitPink
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EntryAddScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EntryAddViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val themeTags by viewModel.themeTags.collectAsStateWithLifecycle()
    val emotionTags by viewModel.emotionTags.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                EntryAddEvent.SaveSuccess -> onNavigateBack()
            }
        }
    }

    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.date
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        viewModel.onDateChange(date)
                    }
                    showDatePicker = false
                }) { Text("확인") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("취소") }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("기록 추가") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            // Date selector
            SectionLabel("날짜")
            Surface(
                onClick = { showDatePicker = true },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = uiState.date.format(
                            DateTimeFormatter.ofPattern("yyyy년 M월 d일 (E)", Locale.KOREAN),
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Cumulative P&L input
            SectionLabel("오늘의 누적 평가손익 (원) *")
            OutlinedTextField(
                value = uiState.cumulativePnlText,
                onValueChange = { viewModel.onCumulativePnlChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("증권사 앱에 표시된 누적 손익 입력") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next,
                ),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                supportingText = {
                    uiState.cumulativePnl?.let { pnl ->
                        val prefix = if (pnl > 0) "+" else ""
                        Text(
                            text = "누적: ${prefix}${String.format("%,d", pnl)}원",
                            color = if (pnl >= 0) ProfitPink else LossLavender,
                        )
                    }
                },
            )

            // Daily P&L calculation result
            uiState.dailyPnl?.let { daily ->
                Spacer(modifier = Modifier.height(8.dp))
                DailyPnlCard(
                    dailyPnl = daily,
                    previousCumulativePnl = uiState.previousCumulativePnl,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stock name
            SectionLabel("종목명")
            OutlinedTextField(
                value = uiState.stockName,
                onValueChange = { viewModel.onStockNameChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("예: 삼성전자") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Theme tags
            SectionLabel("테마 태그")
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                themeTags.forEach { tag ->
                    TagChip(
                        tag = tag,
                        selected = tag.id in uiState.selectedThemeTagIds,
                        onClick = { viewModel.onThemeTagToggle(tag.id) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Emotion tags
            SectionLabel("감정 태그")
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                emotionTags.forEach { tag ->
                    TagChip(
                        tag = tag,
                        selected = tag.id in uiState.selectedEmotionTagIds,
                        onClick = { viewModel.onEmotionTagToggle(tag.id) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Memo
            SectionLabel("메모 (${uiState.memo.length}/200)")
            OutlinedTextField(
                value = uiState.memo,
                onValueChange = { viewModel.onMemoChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                placeholder = { Text("오늘의 매매에 대한 메모를 남겨보세요") },
                shape = RoundedCornerShape(12.dp),
                maxLines = 5,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Save button
            Button(
                onClick = { viewModel.save() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = uiState.cumulativePnl != null && !uiState.isSaving,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Text(
                    text = if (uiState.isSaving) "저장 중..." else "저장하기",
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun DailyPnlCard(
    dailyPnl: Long,
    previousCumulativePnl: Long?,
) {
    val prefix = if (dailyPnl > 0) "+" else ""
    val color = if (dailyPnl >= 0) ProfitPink else LossLavender

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f),
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "오늘의 일간 손익",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${prefix}${String.format("%,d", dailyPnl)}원",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = color,
            )
            if (previousCumulativePnl != null) {
                Spacer(modifier = Modifier.height(4.dp))
                val prevPrefix = if (previousCumulativePnl > 0) "+" else ""
                Text(
                    text = "이전 누적 손익: ${prevPrefix}${String.format("%,d", previousCumulativePnl)}원",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "첫 기록이므로 입력한 누적 손익이 일간 손익이 됩니다",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TagChip(
    tag: Tag,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(tag.name) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    )
}
