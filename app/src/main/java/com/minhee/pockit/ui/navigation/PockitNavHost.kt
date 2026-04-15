package com.minhee.pockit.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.minhee.pockit.ui.analytics.AnalyticsScreen
import com.minhee.pockit.ui.calendar.CalendarScreen
import com.minhee.pockit.ui.entry.EntryAddScreen
import com.minhee.pockit.ui.entry.EntryDetailScreen
import com.minhee.pockit.ui.entry.EntryEditScreen
import com.minhee.pockit.ui.guide.GuideScreen
import com.minhee.pockit.ui.settings.SettingsScreen

@Composable
fun PockitNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = PockitRoute.CALENDAR,
        modifier = modifier,
    ) {
        composable(PockitRoute.CALENDAR) {
            CalendarScreen(
                onDateClick = { date ->
                    navController.navigate(PockitRoute.entryAdd(date.toString()))
                },
                onEntryClick = { entryId ->
                    navController.navigate(PockitRoute.entryDetail(entryId))
                },
            )
        }

        composable(PockitRoute.ANALYTICS) {
            AnalyticsScreen()
        }

        composable(PockitRoute.GUIDE) {
            GuideScreen()
        }

        composable(PockitRoute.SETTINGS) {
            SettingsScreen()
        }

        composable(
            route = PockitRoute.ENTRY_ADD,
            arguments = listOf(
                navArgument("date") {
                    type = NavType.StringType
                    defaultValue = ""
                },
            ),
        ) {
            EntryAddScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable(
            route = PockitRoute.ENTRY_DETAIL,
            arguments = listOf(
                navArgument("entryId") { type = NavType.LongType },
            ),
        ) {
            EntryDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onEditClick = { entryId ->
                    navController.navigate(PockitRoute.entryEdit(entryId))
                },
            )
        }

        composable(
            route = PockitRoute.ENTRY_EDIT,
            arguments = listOf(
                navArgument("entryId") { type = NavType.LongType },
            ),
        ) {
            EntryEditScreen(
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
