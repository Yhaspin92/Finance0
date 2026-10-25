package com.example.financeflow.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
    CUENTAS("Cuentas"),
    TARJETAS("Tarjetas & MSI"),
    MOVIMIENTOS("Movimientos"),
    CALENDARIO("Calendario")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: FinanceViewModel = viewModel()
) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(AppTab.RESUMEN) }
    var showQuickActionDialog by remember { mutableStateOf(false) }
    var showSettingsScreen by remember { mutableStateOf(false) }
    var showNotificationScreen by remember { mutableStateOf(false) }

    var showCalculatorDialog by remember { mutableStateOf(false) }

    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val creditCards by viewModel.creditCards.collectAsStateWithLifecycle()
    val installmentPurchases by viewModel.installmentPurchases.collectAsStateWithLifecycle()
    val billReminders by viewModel.billReminders.collectAsStateWithLifecycle()
    val payrollConfig by viewModel.payrollConfig.collectAsStateWithLifecycle()
    val notificationLogs by viewModel.notificationLogs.collectAsStateWithLifecycle()
    val receivables by viewModel.receivables.collectAsStateWithLifecycle()
    val netWorth by viewModel.netWorth.collectAsStateWithLifecycle()
    val monthlyIncome by viewModel.monthlyIncome.collectAsStateWithLifecycle()
    val monthlyExpense by viewModel.monthlyExpense.collectAsStateWithLifecycle()
    val liquidPhysicalBalance by viewModel.liquidPhysicalBalance.collectAsStateWithLifecycle()
    val pendingReceivablesTotal by viewModel.pendingReceivablesTotal.collectAsStateWithLifecycle()
    val projectedMonthlyTotal by viewModel.projectedMonthlyTotal.collectAsStateWithLifecycle()
    val isNotificationPermissionGranted by viewModel.isNotificationPermissionGranted.collectAsStateWithLifecycle()
    val isBannerDismissed by viewModel.isBannerDismissed.collectAsStateWithLifecycle()
    val updateState by viewModel.updateState.collectAsStateWithLifecycle()
    val cardNetWorthMode by viewModel.cardNetWorthMode.collectAsStateWithLifecycle()

    if (showSettingsScreen) {
        com.example.financeflow.ui.settings.SettingsScreen(
            viewModel = viewModel,
            onBack = { showSettingsScreen = false }
        )
    } else if (showNotificationScreen) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("Monitor de Notificaciones", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Lector automático y auditoría de bancos", fontSize = 11.sp, color = TextSecondary)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { showNotificationScreen = false }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = TextPrimary)
                        }
                    }
                )
            }
        ) { paddingValues ->
            NotificationMonitorScreen(
                isPermissionGranted = isNotificationPermissionGranted,
                notificationLogs = notificationLogs,
                onOpenSettings = {
                    BankNotificationListenerService.openNotificationAccessSettings(context)
                },
                onRefreshPermission = {
                    viewModel.checkPermission()
                },
                modifier = Modifier.padding(paddingValues)
            )
        }
    } else {
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
                        // Botón de Monitor de Notificaciones (Lector bancario)
                        IconButton(onClick = { showNotificationScreen = true }) {
                            BadgedBox(
                                badge = {
                                    if (!isNotificationPermissionGranted) {
                                        Badge(containerColor = FintechWarning) {
                                            Text("!", color = FintechBackground, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = "Notificaciones Bancarias",
                                    tint = if (!isNotificationPermissionGranted) FintechWarning else NeonCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        // Botón de Calculadora Funcional
                        IconButton(onClick = { showCalculatorDialog = true }) {
                            Icon(
                                Icons.Default.Calculate,
                                contentDescription = "Calculadora Financiera",
                                tint = AccentNeon,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Botón de Configuración (Temas, OTA, Nómina, Respaldo, Borrado Maestro)
                        IconButton(onClick = { showSettingsScreen = true }) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "Configuración",
                                tint = TextPrimary,
                                modifier = Modifier.size(22.dp)
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
                    selected = currentTab == AppTab.CUENTAS,
                    onClick = { currentTab = AppTab.CUENTAS },
                    icon = { Icon(Icons.Default.AccountBalance, contentDescription = "Cuentas") },
                    label = { Text("Cuentas", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AccentNeon,
                        selectedTextColor = AccentNeon,
                        indicatorColor = AccentNeon.copy(alpha = 0.15f),
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.TARJETAS,
                    onClick = { currentTab = AppTab.TARJETAS },
                    icon = { Icon(Icons.Default.CreditCard, contentDescription = "Tarjetas & MSI") },
                    label = { Text("Tarjetas", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AccentSecondary,
                        selectedTextColor = AccentSecondary,
                        indicatorColor = AccentSecondary.copy(alpha = 0.15f),
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.RESUMEN,
                    onClick = { currentTab = AppTab.RESUMEN },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Resumen") },
                    label = { Text("Resumen", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AccentNeon,
                        selectedTextColor = AccentNeon,
                        indicatorColor = AccentNeon.copy(alpha = 0.15f),
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
                        selectedIconColor = AccentNeon,
                        selectedTextColor = AccentNeon,
                        indicatorColor = AccentNeon.copy(alpha = 0.15f),
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
                        selectedIconColor = AccentNeon,
                        selectedTextColor = AccentNeon,
                        indicatorColor = AccentNeon.copy(alpha = 0.15f),
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
                    liquidPhysicalBalance = liquidPhysicalBalance,
                    pendingReceivablesTotal = pendingReceivablesTotal,
                    projectedMonthlyTotal = projectedMonthlyTotal,
                    accounts = accounts,
                    transactions = transactions,
                    creditCards = creditCards,
                    receivables = receivables,
                    cardNetWorthMode = cardNetWorthMode,
                    isNotificationPermissionGranted = isNotificationPermissionGranted,
                    isBannerDismissed = isBannerDismissed,
                    onDismissNotificationBanner = { viewModel.dismissNotificationBanner() },
                    onOpenNotificationSettings = {
                        BankNotificationListenerService.openNotificationAccessSettings(context)
                    },
                    onAddReceivable = { debtor, concept, amount, note ->
                        viewModel.addReceivable(debtor, concept, amount, note = note)
                    },
                    onUpdateReceivable = { recId, debtor, concept, amount, note ->
                        viewModel.updateReceivable(recId, debtor, concept, amount, note)
                    },
                    onCollectReceivable = { recId, targetAccId, amount, isPartial, breakdownItemId ->
                        viewModel.collectReceivable(recId, targetAccId, amount, isPartial, breakdownItemId)
                    },
                    onAddBreakdownItem = { recId, concept, amount ->
                        viewModel.addBreakdownItem(recId, concept, amount)
                    },
                    onUpdateBreakdownItem = { recId, itemId, concept, amount ->
                        viewModel.updateBreakdownItem(recId, itemId, concept, amount)
                    },
                    onRemoveBreakdownItem = { recId, itemId ->
                        viewModel.removeBreakdownItem(recId, itemId)
                    },
                    onDeleteReceivable = { recId ->
                        viewModel.deleteReceivable(recId)
                    },
                    onQuickActionClick = { showQuickActionDialog = true },
                    onNavigateToTransactions = { currentTab = AppTab.MOVIMIENTOS },
                    onNavigateToCards = { currentTab = AppTab.TARJETAS }
                )

                AppTab.CUENTAS -> com.example.financeflow.ui.accounts.AccountsScreen(
                    accounts = accounts,
                    creditCards = creditCards,
                    transactions = transactions,
                    onAddAccount = { name, bank, type, bal, last4, color, inv, yieldRate ->
                        viewModel.addAccount(name, bank, type, bal, last4, color, inv, yieldRate)
                    },
                    onUpdateAccount = { accId, name, bank, type, bal, last4, color, inv, yieldRate ->
                        viewModel.updateAccount(accId, name, bank, type, bal, last4, color, inv, yieldRate)
                    },
                    onDeleteAccount = { accId ->
                        viewModel.deleteAccount(accId)
                    },
                    onAddPocket = { accId, name, amt, target ->
                        viewModel.addPocketToAccount(accId, name, amt, target)
                    }
                )

                AppTab.TARJETAS -> CardsScreen(
                    creditCards = creditCards,
                    accounts = accounts,
                    installmentPurchases = installmentPurchases,
                    onAddCard = { name, bank, last4, limit, debt, cutOff, due, linkedAccId, isDebit ->
                        viewModel.addCreditCard(name, bank, last4, limit, debt, cutOff, due, linkedAccId, isDebit)
                    },
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

                AppTab.MOVIMIENTOS -> TransactionsScreen(
                    transactions = transactions,
                    onDeleteTransaction = { txId -> viewModel.deleteTransaction(txId) }
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

    if (showCalculatorDialog) {
        com.example.financeflow.ui.components.CalculatorDialog(
            onDismissRequest = { showCalculatorDialog = false }
        )
    }
    }
}
