package com.jaywaa.receipts

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.jaywaa.receipts.navigation.AddReceipt
import com.jaywaa.receipts.navigation.Home
import com.jaywaa.receipts.navigation.ReceiptDetail
import com.jaywaa.receipts.navigation.SendPreview
import com.jaywaa.receipts.navigation.Settings
import com.jaywaa.receipts.ui.addreceipt.AddReceiptScreen
import com.jaywaa.receipts.ui.detail.DetailScreen
import com.jaywaa.receipts.ui.home.HomeScreen
import com.jaywaa.receipts.ui.send.SendPreviewScreen
import com.jaywaa.receipts.ui.settings.SettingsScreen

@Composable
fun ReceiptsApp(context: Context) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Home) {
        composable<Home> {
            HomeScreen(
                onAddReceipt = { navController.navigate(AddReceipt) },
                onReceiptClick = { id -> navController.navigate(ReceiptDetail(id)) },
                onSendReport = { navController.navigate(SendPreview) },
                onSettings = { navController.navigate(Settings) }
            )
        }
        composable<AddReceipt> {
            AddReceiptScreen(
                context = context,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable<ReceiptDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<ReceiptDetail>()
            DetailScreen(
                receiptId = route.receiptId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable<SendPreview> {
            SendPreviewScreen(
                context = context,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable<Settings> {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
