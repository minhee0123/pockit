package com.minhee.pockit.ui.calendar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minhee.pockit.domain.model.PockitEntry
import com.minhee.pockit.ui.theme.LossLavender
import com.minhee.pockit.ui.theme.ProfitPink
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(
    onDateClick: (LocalDate) -> Unit,
    onEntryClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val date = uiState.selectedDate ?: LocalDate.now()
                    onDateClick(date)
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(Icons.Default.Add, contentDescription = "기록 추가")
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // Fixed top section: Summary + Calendar
            MonthlySummaryCard(
                profit = uiState.monthlyProfit,
                loss = uiState.monthlyLoss,
                net = uiState.monthlyNet,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )

            MonthHeader(
                currentMonth = uiState.currentMonth,
                onPreviousMonth = viewModel::onPreviousMonth,
                onNextMonth = viewModel::onNextMonth,
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            DayOfWeekRow(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            )

            CalendarGrid(
                currentMonth = uiState.currentMonth,
                selectedDate = uiState.selectedDate,
                entries = uiState.entries,
                onDateSelected = viewModel::onDateSelected,
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            // Bottom section: Selected date entries (scrollable)
            AnimatedVisibility(
                visible = uiState.selectedDate != null,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )

                    uiState.selectedDate?.let { date ->
                        Text(
                            text = date.format(
                                DateTimeFormatter.ofPattern("M월 d일 (E)", Locale.KOREAN),
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }

                    if (uiState.selectedDateEntries.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "기록이 없습니다",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(top = 8.dp),
                        ) {
                            items(
                                items = uiState.selectedDateEntries,
                                key = { it.id },
                            ) { entry ->
                                EntryListItem(
                                    entry = entry,
                                    onClick = { onEntryClick(entry.id) },
                                    modifier = Modifier.padding(
                                        horizontal = 16.dp,
                                        vertical = 4.dp,
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthlySummaryCard(
    profit: Long,
    loss: Long,
    net: Long,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "이번 달 요약",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = formatWon(net),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = when {
                    net > 0 -> ProfitPink
                    net < 0 -> LossLavender
                    else -> MaterialTheme.colorScheme.onSurface
                },
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = "수익",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = formatWon(profit),
                        style = MaterialTheme.typography.titleMedium,
                        color = ProfitPink,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "손실",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = formatWon(loss),
                        style = MaterialTheme.typography.titleMedium,
                        color = LossLavender,
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthHeader(
    currentMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "이전 달",
            )
        }
        Text(
            text = "${currentMonth.year}년 ${currentMonth.monthValue}월",
            style = MaterialTheme.typography.titleLarge,
        )
        IconButton(onClick = onNextMonth) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "다음 달",
            )
        }
    }
}

@Composable
private fun DayOfWeekRow(modifier: Modifier = Modifier) {
    val daysOfWeek = listOf(
        DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY,
    )
    Row(modifier = modifier.fillMaxWidth()) {
        daysOfWeek.forEach { day ->
            Text(
                text = day.getDisplayName(TextStyle.SHORT, Locale.KOREAN),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                color = when (day) {
                    DayOfWeek.SUNDAY -> ProfitPink
                    DayOfWeek.SATURDAY -> LossLavender
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    currentMonth: YearMonth,
    selectedDate: LocalDate?,
    entries: Map<LocalDate, List<PockitEntry>>,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val firstDayOfMonth = currentMonth.atDay(1)
    val lastDay = currentMonth.lengthOfMonth()
    val startOffset = firstDayOfMonth.dayOfWeek.value % 7
    val totalCells = startOffset + lastDay
    val rows = (totalCells + 6) / 7

    Column(modifier = modifier.fillMaxWidth()) {
        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0..6) {
                    val cellIndex = row * 7 + col
                    val dayOfMonth = cellIndex - startOffset + 1

                    if (dayOfMonth in 1..lastDay) {
                        val date = currentMonth.atDay(dayOfMonth)
                        val dayEntries = entries[date] ?: emptyList()
                        val dailyPnl = dayEntries.sumOf { it.dailyPnl }
                        val isSelected = date == selectedDate
                        val isToday = date == LocalDate.now()

                        CalendarDayCell(
                            day = dayOfMonth,
                            dailyPnl = dailyPnl,
                            hasEntries = dayEntries.isNotEmpty(),
                            isSelected = isSelected,
                            isToday = isToday,
                            isSunday = col == 0,
                            isSaturday = col == 6,
                            onClick = { onDateSelected(date) },
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: Int,
    dailyPnl: Long,
    hasEntries: Boolean,
    isSelected: Boolean,
    isToday: Boolean,
    isSunday: Boolean,
    isSaturday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        else -> Color.Transparent
    }
    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
        isSunday -> ProfitPink
        isSaturday -> LossLavender
        else -> MaterialTheme.colorScheme.onBackground
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isToday) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            CircleShape,
                        ),
                )
            }
            Text(
                text = "$day",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                ),
                color = if (isToday) MaterialTheme.colorScheme.onPrimary else textColor,
            )
        }

        if (hasEntries) {
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = formatCompactWon(dailyPnl),
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 9.sp),
                color = if (dailyPnl >= 0) ProfitPink else LossLavender,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun EntryListItem(
    entry: PockitEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        if (entry.dailyPnl >= 0) ProfitPink else LossLavender,
                        CircleShape,
                    ),
            )
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                entry.stockName?.let { name ->
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (entry.themeTags.isNotEmpty()) {
                    Text(
                        text = entry.themeTags.joinToString(" ") { "#${it.name}" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Text(
                text = formatWon(entry.dailyPnl),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = if (entry.dailyPnl >= 0) ProfitPink else LossLavender,
            )
        }
    }
}

private fun formatWon(amount: Long): String {
    val prefix = if (amount > 0) "+" else ""
    return "${prefix}${String.format("%,d", amount)}원"
}

private fun formatCompactWon(amount: Long): String {
    val prefix = if (amount > 0) "+" else ""
    return when {
        amount == 0L -> "0"
        kotlin.math.abs(amount) >= 10_000 -> "${prefix}${amount / 10_000}만"
        kotlin.math.abs(amount) >= 1_000 -> "${prefix}${amount / 1_000}천"
        else -> "${prefix}${amount}"
    }
}
