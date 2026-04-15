package com.minhee.pockit.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class TopLevelDestination(
    val route: String,
    val icon: ImageVector,
    val label: String,
) {
    Calendar("calendar", Icons.Default.CalendarMonth, "달력"),
    Analytics("analytics", Icons.Default.BarChart, "분석"),
    Guide("guide", Icons.Default.MenuBook, "가이드"),
    Settings("settings", Icons.Default.Settings, "설정"),
}

object PockitRoute {
    const val CALENDAR = "calendar"
    const val ANALYTICS = "analytics"
    const val GUIDE = "guide"
    const val SETTINGS = "settings"
    const val ENTRY_ADD = "entry/add?date={date}"
    const val ENTRY_DETAIL = "entry/{entryId}"
    const val ENTRY_EDIT = "entry/{entryId}/edit"

    fun entryAdd(date: String) = "entry/add?date=$date"
    fun entryDetail(entryId: Long) = "entry/$entryId"
    fun entryEdit(entryId: Long) = "entry/$entryId/edit"
}
