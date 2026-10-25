package com.example.financeflow.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financeflow.data.notification.BankNotificationListenerService
import com.example.financeflow.data.updater.UpdateState
import com.example.financeflow.ui.FinanceViewModel
import com.example.financeflow.ui.calendar.CalendarScreen
import com.example.financeflow.ui.cards.CardsScreen
import com.example.financeflow.ui.components.QuickActionDialog
import com.example.financeflow.ui.dashboard.DashboardScreen
import com.example.financeflow.ui.notification.NotificationMonitorScreen
import com.example.financeflow.ui.theme.*
import com.example.financeflow.ui.transactions.TransactionsScreen

enum class AppTab(val title: String) {
    RESUMEN("Resumen"),
    MOVIMIENTOS("Movimientos"),
    TARJETAS("Tarjetas"),
    CALENDARIO("Calendario"),
    NOTIFICACIONES("Notificaciones")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: FinanceViewModel = viewModel()
) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(AppTab.RESUMEN) }
    var showQuickActionDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }

    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val creditCards by viewModel.creditCards.collectAsStateWithLifecycle()
    val installmentPurchases by viewModel.installmentPurchases.collectAsStateWithLifecycle()
    val billReminders by viewModel.billReminders.collectAsStateWithLifecycle()
    val payrollConfig by viewModel.payrollConfig.collectAsStateWithLifecycle()
    val notificationLogs by viewModel.notificationLogs.collectAsStateWithLifecycle()
    val netWorth by viewModel.netWorth.collectAsStateWithLifecycle()
    val monthlyIncome by viewModel.monthlyIncome.collectAsStateWithLifecycle()
    val monthlyExpense by viewModel.monthlyExpense.collectAsStateWithLifecycle()
    val isNotificationPermissionGranted by viewModel.isNotificationPermissionGranted.collectAsStateWithLifecycle()
    val updateState by viewModel.updateState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(NeonEmerald),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = "Logo",
                                tint = FintechBackground,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Finance0",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Finanzas & Notificaciones",
                                fontSize = 11.sp,
                                color = NeonEmerald,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                actions = {
                    // Botón de Actualizaciones OTA vía GitHub
                    IconButton(onClick = { showUpdateDialog = true }) {
                        Icon(
                            Icons.Default.CloudSync,
                            contentDescription = "Actualizaciones GitHub",
                            tint = NeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Botón de Nuevo Movimiento
                    IconButton(onClick = { showQuickActionDialog = true }) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(NeonEmerald),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Nuevo Movimiento",
                                tint = FintechBackground,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FintechBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = FintechSurface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == AppTab.RESUMEN,
                    onClick = { currentTab = AppTab.RESUMEN },
                    icon = { Icon(Icons.Default.AccountBalance, contentDescription = "Resumen") },
                    label = { Text("Resumen", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonEmerald,
                        selectedTextColor = NeonEmerald,
                        indicatorColor = NeonEmerald.copy(alpha = 0.15f),
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.MOVIMIENTOS,
                    onClick = { currentTab = AppTab.MOVIMIENTOS },
                    icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = "Movimientos") },
                    label = { Text("Movimientos", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonEmerald,
                        selectedTextColor = NeonEmerald,
                        indicatorColor = NeonEmerald.copy(alpha = 0.15f),
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.TARJETAS,
                    onClick = { currentTab = AppTab.TARJETAS },
                    icon = { Icon(Icons.Default.CreditCard, contentDescription = "Tarjetas") },
                    label = { Text("Tarjetas & MSI", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonViolet,
                        selectedTextColor = NeonViolet,
                        indicatorColor = NeonViolet.copy(alpha = 0.15f),
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.CALENDARIO,
                    onClick = { currentTab = AppTab.CALENDARIO },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Calendario") },
                    label = { Text("Calendario", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonEmerald,
                        selectedTextColor = NeonEmerald,
                        indicatorColor = NeonEmerald.copy(alpha = 0.15f),
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.NOTIFICACIONES,
                    onClick = { currentTab = AppTab.NOTIFICACIONES },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (!isNotificationPermissionGranted) {
                                    Badge(containerColor = FintechWarning) {
                                        Text("!", color = FintechBackground, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = "Notificaciones")
                        }
                    },
                    label = { Text("Notificaciones", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonCyan,
                        selectedTextColor = NeonCyan,
                        indicatorColor = NeonCyan.copy(alpha = 0.15f),
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
            }
        },
        containerColor = FintechBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentTab) {
                AppTab.RESUMEN -> DashboardScreen(
                    netWorth = netWorth,
                    monthlyIncome = monthlyIncome,
                    monthlyExpense = monthlyExpense,
                    accounts = accounts,
                    transactions = transactions,
                    creditCards = creditCards,
                    isNotificationPermissionGranted = isNotificationPermissionGranted,
                    onOpenNotificationSettings = {
                        BankNotificationListenerService.openNotificationAccessSettings(context)
                    },
                    onQuickActionClick = { showQuickActionDialog = true },
                    onNavigateToTransactions = { currentTab = AppTab.MOVIMIENTOS },
                    onNavigateToCards = { currentTab = AppTab.TARJETAS }
                )

                AppTab.MOVIMIENTOS -> TransactionsScreen(
                    transactions = transactions,
                    onDeleteTransaction = { txId -> viewModel.deleteTransaction(txId) }
                )

                AppTab.TARJETAS -> CardsScreen(
                    creditCards = creditCards,
                    accounts = accounts,
                    installmentPurchases = installmentPurchases,
                    onPayCardClick = { debitAccId, cardId, amount ->
                        viewModel.payCreditCard(debitAccId, cardId, amount)
                    },
                    onAddInstallmentPurchase = { cardId, title, total, months, note ->
                        viewModel.addInstallmentPurchase(cardId, title, total, months, note)
                    },
                    onAdvanceInstallmentMonth = { id ->
                        viewModel.advanceInstallmentMonth(id)
                    },
                    onDeleteInstallmentPurchase = { id ->
                        viewModel.deleteInstallmentPurchase(id)
                    }
                )

                AppTab.CALENDARIO -> CalendarScreen(
                    payrollConfig = payrollConfig,
                    creditCards = creditCards,
                    billReminders = billReminders,
                    onToggleBillPaid = { billId -> viewModel.toggleBillPaid(billId) },
                    onAddBillReminder = { title, amount, dueDay, category, provider ->
                        viewModel.addBillReminder(title, amount, dueDay, category, provider)
                    }
                )

                AppTab.NOTIFICACIONES -> NotificationMonitorScreen(
                    isPermissionGranted = isNotificationPermissionGranted,
                    notificationLogs = notificationLogs,
                    onOpenSettings = {
                        BankNotificationListenerService.openNotificationAccessSettings(context)
                    },
                    onRefreshPermission = {
                        viewModel.checkPermission()
                    },
                    onSimulateNotification = { pkg, title, text ->
                        viewModel.simulateNotification(pkg, title, text)
                    }
                )
            }
        }
    }

    // Modal de Nuevo Movimiento Manual
    if (showQuickActionDialog) {
        QuickActionDialog(
            accounts = accounts,
            creditCards = creditCards,
            onDismiss = { showQuickActionDialog = false },
            onSaveTransaction = { accId, tgtId, title, amount, type, cat, note ->
                viewModel.addManualTransaction(
                    accountId = accId,
                    targetAccountId = tgtId,
                    title = title,
                    amount = amount,
                    type = type,
                    category = cat,
                    note = note
                )
            }
        )
    }

    // Modal de Actualizaciones OTA vía GitHub
    if (showUpdateDialog) {
        var githubRepo by remember { mutableStateOf(viewModel.updateManager.configuredRepo) }
        var githubToken by remember { mutableStateOf(viewModel.updateManager.personalAccessToken) }

        AlertDialog(
            onDismissRequest = { showUpdateDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudSync, contentDescription = "GitHub", tint = NeonCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Actualizaciones de Finance0",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Versión instalada: v${viewModel.updateManager.currentVersion}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NeonEmerald
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Repositorio GitHub para actualizaciones OTA:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    OutlinedTextField(
                        value = githubRepo,
                        onValueChange = {
                            githubRepo = it
                            viewModel.updateManager.configuredRepo = it
                        },
                        singleLine = true,
                        placeholder = { Text("usuario/Finance0") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "GitHub Token (Opcional, solo si el repo es privado):",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    OutlinedTextField(
                        value = githubToken,
                        onValueChange = {
                            githubToken = it
                            viewModel.updateManager.personalAccessToken = it
                        },
                        singleLine = true,
                        placeholder = { Text("ghp_xxxx...") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    when (val state = updateState) {
                        is UpdateState.Idle -> {
                            Text(
                                text = "Presiona el botón para comprobar si hay nuevas versiones publicadas en GitHub Releases.",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                        is UpdateState.Checking -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = NeonCyan,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Consultando GitHub Releases...", fontSize = 12.sp, color = NeonCyan)
                            }
                        }
                        is UpdateState.UpToDate -> {
                            Text(
                                text = "✅ Finance0 está al día (v${state.currentVersion}). No hay versiones pendientes.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonEmerald
                            )
                        }
                        is UpdateState.UpdateAvailable -> {
                            Column {
                                Text(
                                    text = "🚀 ¡Nueva versión disponible: v${state.release.tagName}!",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan
                                )
                                Text(
                                    text = state.release.body,
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                if (state.release.apkDownloadUrl != null) {
                                    Button(
                                        onClick = { viewModel.downloadAndInstallApk(state.release.apkDownloadUrl, githubToken) },
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald)
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = "Descargar", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Descargar e Instalar APK", color = FintechBackground, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Text(
                                        text = "El release no contiene un archivo .apk adjunto.",
                                        fontSize = 11.sp,
                                        color = FintechWarning
                                    )
                                }
                            }
                        }
                        is UpdateState.Downloading -> {
                            Column {
                                Text("Descargando actualización: ${state.progress}%", fontSize = 12.sp, color = NeonCyan)
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { state.progress / 100f },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = NeonCyan
                                )
                            }
                        }
                        is UpdateState.ReadyToInstall -> {
                            Text(
                                text = "Descarga completada. Abriendo instalador de Android...",
                                fontSize = 12.sp,
                                color = NeonEmerald
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Button(
                                onClick = { viewModel.promptInstallApk(state.apkFile) },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald)
                            ) {
                                Text("Instalar APK", color = FintechBackground, fontWeight = FontWeight.Bold)
                            }
                        }
                        is UpdateState.NeedsInstallPermission -> {
                            Column {
                                Text(
                                    text = "⚠️ Se requiere autorizar la instalación de fuentes desconocidas en Android.",
                                    fontSize = 12.sp,
                                    color = FintechWarning,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Button(
                                    onClick = { viewModel.promptInstallApk(state.apkFile) },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald)
                                ) {
                                    Text("Autorizar e Instalar", color = FintechBackground, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        is UpdateState.Error -> {
                            Text(
                                text = state.message,
                                fontSize = 12.sp,
                                color = FintechExpense
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.checkForUpdates(githubRepo, githubToken) },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Text("Comprobar Ahora", color = FintechBackground, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpdateDialog = false }) {
                    Text("Cerrar", color = TextSecondary)
                }
            },
            containerColor = FintechSurface
        )
    }
}
