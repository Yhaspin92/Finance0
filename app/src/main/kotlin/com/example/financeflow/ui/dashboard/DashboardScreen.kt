package com.example.financeflow.ui.dashboard

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.financeflow.domain.model.*
import com.example.financeflow.ui.components.Formatters
import com.example.financeflow.ui.components.GlassCard
import com.example.financeflow.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun DashboardScreen(
    netWorth: Double,
    monthlyIncome: Double,
    monthlyExpense: Double,
    liquidPhysicalBalance: Double,
    pendingReceivablesTotal: Double,
    projectedMonthlyTotal: Double,
    accounts: List<Account>,
    transactions: List<Transaction>,
    creditCards: List<CreditCard>,
    receivables: List<AccountReceivable>,
    cardNetWorthMode: CardNetWorthMode = CardNetWorthMode.DEBT,
    isNotificationPermissionGranted: Boolean,
    isBannerDismissed: Boolean,
    onDismissNotificationBanner: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onAddReceivable: (debtorName: String, concept: String, amount: Double, note: String) -> Unit,
    onUpdateReceivable: (receivableId: String, debtorName: String, concept: String, amount: Double, note: String) -> Unit,
    onCollectReceivable: (receivableId: String, targetAccountId: String, amountToCollect: Double, isPartial: Boolean, breakdownItemId: String?) -> Unit,
    onAddBreakdownItem: (receivableId: String, concept: String, amount: Double) -> Unit,
    onUpdateBreakdownItem: (receivableId: String, itemId: String, concept: String, amount: Double) -> Unit,
    onRemoveBreakdownItem: (receivableId: String, itemId: String) -> Unit,
    onDeleteReceivable: (receivableId: String) -> Unit,
    onQuickActionClick: () -> Unit,
    onNavigateToTransactions: () -> Unit,
    onNavigateToCards: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddReceivableDialog by remember { mutableStateOf(false) }
    var receivableToCollect by remember { mutableStateOf<AccountReceivable?>(null) }
    var receivableToEdit by remember { mutableStateOf<AccountReceivable?>(null) }
    var selectedReceivableDetailId by remember { mutableStateOf<String?>(null) }
    var showIncompleteBreakdownAlert by remember { mutableStateOf(false) }
    var showReceivablesSubScreen by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 2 })

    // =========================================================================
    // PANTALLA SECUNDARIA DEDICADA: CUENTAS POR COBRAR
    // Se activa al tocar en el Total Proyectado / Por Cobrar
    // =========================================================================
    if (showReceivablesSubScreen) {
        val selectedRec = receivables.find { it.id == selectedReceivableDetailId }

        BackHandler(enabled = selectedReceivableDetailId != null) {
            val rec = receivables.find { it.id == selectedReceivableDetailId }
            if (rec != null && rec.breakdown.isNotEmpty()) {
                val sum = rec.breakdown.sumOf { it.amount }
                if (kotlin.math.abs(sum - rec.amount) > 0.01) {
                    showIncompleteBreakdownAlert = true
                    return@BackHandler
                }
            }
            selectedReceivableDetailId = null
        }

        BackHandler(enabled = selectedReceivableDetailId == null) {
            showReceivablesSubScreen = false
        }

        if (selectedRec != null) {
            ReceivableDetailPage(
                receivable = selectedRec,
                onBack = {
                    val sum = selectedRec.breakdown.sumOf { it.amount }
                    if (selectedRec.breakdown.isNotEmpty() && kotlin.math.abs(sum - selectedRec.amount) > 0.01) {
                        showIncompleteBreakdownAlert = true
                    } else {
                        selectedReceivableDetailId = null
                    }
                },
                onEdit = { receivableToEdit = selectedRec },
                onDelete = {
                    onDeleteReceivable(selectedRec.id)
                    selectedReceivableDetailId = null
                },
                onCollect = { receivableToCollect = selectedRec },
                onAddBreakdownItem = { concept: String, amt: Double -> onAddBreakdownItem(selectedRec.id, concept, amt) },
                onUpdateBreakdownItem = { itemId: String, concept: String, amt: Double -> onUpdateBreakdownItem(selectedRec.id, itemId, concept, amt) },
                onRemoveBreakdownItem = { itemId: String -> onRemoveBreakdownItem(selectedRec.id, itemId) },
                onClearAllBreakdown = {
                    selectedRec.breakdown.forEach { onRemoveBreakdownItem(selectedRec.id, it.id) }
                },
                modifier = modifier
            )
        } else {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(FintechBackground)
            ) {
                // Cabecera con botón de retorno
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showReceivablesSubScreen = false }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar",
                            tint = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "📥 Cuentas por Cobrar",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Dinero por recibir en el mes",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    FilledTonalButton(
                        onClick = { showAddReceivableDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = AccentNeon.copy(alpha = 0.15f),
                            contentColor = AccentNeon
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Agregar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Banner resumen del total pendiente por cobrar
                    item {
                        GlassCard(
                            accentGlow = FintechWarning,
                            borderColor = FintechWarning.copy(alpha = 0.35f),
                            shapeRadius = 18.dp
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Total Pendiente por Cobrar",
                                        fontSize = 13.sp,
                                        color = TextSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(FintechWarning.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "${receivables.count { !it.isCollected }} PENDIENTES",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = FintechWarning
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = Formatters.formatCurrency(pendingReceivablesTotal),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = FintechWarning
                                )
                            }
                        }
                    }

                    if (receivables.isEmpty()) {
                        item {
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                shapeRadius = 14.dp
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(TextMuted.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("🤝", fontSize = 18.sp)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Sin cuentas por cobrar registradas", fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 14.sp)
                                        Text("Toca en 'Agregar' para registrar préstamos o cobros previstos del mes.", color = TextSecondary, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    } else {
                        items(receivables) { rec ->
                            val isDone = rec.isCollected
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                borderColor = if (isDone) FintechBorder else FintechWarning.copy(alpha = 0.35f),
                                shapeRadius = 14.dp
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Parte izquierda interactiva: Abre página nueva de detalle al tocar
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { selectedReceivableDetailId = rec.id }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(if (isDone) NeonEmerald.copy(alpha = 0.15f) else FintechWarning.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                if (isDone) Icons.Default.CheckCircle else Icons.Default.PendingActions,
                                                contentDescription = null,
                                                tint = if (isDone) NeonEmerald else FintechWarning,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = rec.debtorName,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary,
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = rec.concept,
                                                color = TextSecondary,
                                                fontSize = 11.sp
                                            )
                                            if (!isDone) {
                                                Text(
                                                    text = "⚠️ Pendiente",
                                                    color = FintechWarning,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            } else {
                                                Text(
                                                    text = "✅ Cobrado",
                                                    color = NeonEmerald,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = Formatters.formatCurrency(rec.amount),
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDone) NeonEmerald else AccentNeon,
                                            fontSize = 15.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (!isDone) {
                                                Button(
                                                    onClick = { receivableToCollect = rec },
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                                                    modifier = Modifier.height(28.dp)
                                                ) {
                                                    Text("Cobrar", fontSize = 11.sp, color = FintechBackground, fontWeight = FontWeight.Bold)
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                            }
                                            IconButton(
                                                onClick = { onDeleteReceivable(rec.id) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "Eliminar",
                                                    tint = TextMuted,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        // =========================================================================
        // PANTALLA PRINCIPAL DEL DASHBOARD (RESUMEN)
        // =========================================================================
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(FintechBackground)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Banner de Permiso de Notificaciones (si aplica)
            if (!isNotificationPermissionGranted && !isBannerDismissed) {
                item {
                    GlassCard(
                        accentGlow = FintechWarning,
                        borderColor = FintechWarning.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onOpenNotificationSettings() }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(FintechWarning.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.NotificationsActive,
                                        contentDescription = "Alerta",
                                        tint = FintechWarning
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Lectura de Notificaciones",
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Toca para autorizar el lector bancario",
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            IconButton(
                                onClick = onDismissNotificationBanner,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Cerrar banner",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // =====================================================================
            // CARRUSEL INTERACTIVO HERO
            // Página 0: Patrimonio Neto y Proyección juntos
            // Página 1: Últimos Movimientos Recientes
            // =====================================================================
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth()
                    ) { page ->
                        when (page) {
                            // PÁGINA 0: PATRIMONIO NETO Y PROYECCIÓN JUNTOS
                            0 -> {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    // 1. Tarjeta Patrimonio Neto
                                    GlassCard(
                                        accentGlow = AccentNeon,
                                        borderColor = AccentNeon.copy(alpha = 0.35f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Patrimonio Neto",
                                                fontSize = 14.sp,
                                                color = TextSecondary,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(AccentNeon.copy(alpha = 0.15f))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = "ACTUALIZADO",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AccentNeon
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = Formatters.formatCurrency(netWorth),
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = TextPrimary
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))
                                        val totalCardsDebt = creditCards.sumOf { it.currentDebt }
                                        val totalAvailableCredit = creditCards.sumOf { (it.creditLimit - it.currentDebt).coerceAtLeast(0.0) }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (cardNetWorthMode == CardNetWorthMode.DEBT) FintechExpense.copy(alpha = 0.15f) else AccentNeon.copy(alpha = 0.15f))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = if (cardNetWorthMode == CardNetWorthMode.DEBT)
                                                    "💳 Deuda en tarjetas: -${Formatters.formatCurrency(totalCardsDebt)}"
                                                else
                                                    "💳 Crédito disponible: +${Formatters.formatCurrency(totalAvailableCredit)}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (cardNetWorthMode == CardNetWorthMode.DEBT) FintechExpense else AccentNeon
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Flujo mensual (Ingresos vs Egresos)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            // Ingresos
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(FintechSurface)
                                                    .padding(8.dp)
                                            ) {
                                                Column {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            Icons.Default.ArrowDownward,
                                                            contentDescription = "Ingreso",
                                                            tint = FintechIncome,
                                                            modifier = Modifier.size(13.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(text = "Ingresos Mes", fontSize = 10.sp, color = TextSecondary)
                                                    }
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = Formatters.formatCurrency(monthlyIncome),
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = FintechIncome
                                                    )
                                                }
                                            }

                                            // Gastos
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(FintechSurface)
                                                    .padding(8.dp)
                                            ) {
                                                Column {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            Icons.Default.ArrowUpward,
                                                            contentDescription = "Gasto",
                                                            tint = FintechExpense,
                                                            modifier = Modifier.size(13.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(text = "Gastos Mes", fontSize = 10.sp, color = TextSecondary)
                                                    }
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = Formatters.formatCurrency(monthlyExpense),
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = FintechExpense
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // 2. Tarjeta Proyección del Mes (Interactiva -> Cuentas por Cobrar)
                                    GlassCard(
                                        accentGlow = AccentSecondary,
                                        borderColor = AccentSecondary.copy(alpha = 0.35f),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { showReceivablesSubScreen = true }
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "Total Proyectado del Mes",
                                                    fontSize = 12.sp,
                                                    color = TextSecondary,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Text(
                                                    text = Formatters.formatCurrency(projectedMonthlyTotal),
                                                    fontSize = 24.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = AccentSecondary
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(AccentSecondary.copy(alpha = 0.15f))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = "PROYECCIÓN",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AccentSecondary
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // Dinero en físico
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(FintechSurface)
                                                    .padding(8.dp)
                                            ) {
                                                Column {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(7.dp)
                                                                .clip(CircleShape)
                                                                .background(NeonEmerald)
                                                        )
                                                        Spacer(modifier = Modifier.width(5.dp))
                                                        Text("En Físico", fontSize = 10.sp, color = TextSecondary)
                                                    }
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = Formatters.formatCurrency(liquidPhysicalBalance),
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = NeonEmerald
                                                    )
                                                    Text("Débito y efectivo", fontSize = 9.sp, color = TextMuted)
                                                }
                                            }

                                            // Por cobrar
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(FintechSurface)
                                                    .clickable { showReceivablesSubScreen = true }
                                                    .padding(8.dp)
                                            ) {
                                                Column {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(7.dp)
                                                                .clip(CircleShape)
                                                                .background(FintechWarning)
                                                        )
                                                        Spacer(modifier = Modifier.width(5.dp))
                                                        Text("Por Cobrar", fontSize = 10.sp, color = TextSecondary)
                                                    }
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = Formatters.formatCurrency(pendingReceivablesTotal),
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = FintechWarning
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            Text(
                                                text = "Desliza para ver Movimientos Recientes →",
                                                fontSize = 10.sp,
                                                color = TextMuted
                                            )
                                        }
                                    }
                                }
                            }

                            // PÁGINA 1: MOVIMIENTOS RECIENTES
                            1 -> {
                                GlassCard(
                                    accentGlow = NeonViolet,
                                    borderColor = NeonViolet.copy(alpha = 0.35f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "⚡ Movimientos Recientes",
                                            fontSize = 14.sp,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Text(
                                            text = "Ver todos (${transactions.size})",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AccentSecondary,
                                            modifier = Modifier.clickable { onNavigateToTransactions() }
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    val heroTxList = transactions.take(5)
                                    if (heroTxList.isEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 24.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Sin movimientos registrados recientemente.",
                                                fontSize = 12.sp,
                                                color = TextMuted
                                            )
                                        }
                                    } else {
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            heroTxList.forEach { tx ->
                                                val isIncome = tx.type == TransactionType.INCOME
                                                val prefix = if (isIncome) "+" else "-"
                                                val amountColor = if (isIncome) FintechIncome else TextPrimary

                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(FintechSurface)
                                                        .padding(horizontal = 10.dp, vertical = 7.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Icon(
                                                            when (tx.type) {
                                                                TransactionType.INCOME -> Icons.Default.ArrowDownward
                                                                TransactionType.EXPENSE -> Icons.Default.ShoppingBag
                                                                TransactionType.TRANSFER -> Icons.Default.SwapHoriz
                                                                TransactionType.CREDIT_CARD_PAYMENT -> Icons.Default.CreditCard
                                                            },
                                                            contentDescription = null,
                                                            tint = when (tx.type) {
                                                                TransactionType.INCOME -> FintechIncome
                                                                TransactionType.EXPENSE -> FintechExpense
                                                                TransactionType.TRANSFER -> NeonViolet
                                                                TransactionType.CREDIT_CARD_PAYMENT -> NeonCyan
                                                            },
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Column {
                                                            Text(
                                                                text = tx.title,
                                                                fontWeight = FontWeight.SemiBold,
                                                                fontSize = 12.sp,
                                                                color = TextPrimary,
                                                                maxLines = 1
                                                            )
                                                            Text(
                                                                text = Formatters.formatDate(tx.timestamp),
                                                                fontSize = 10.sp,
                                                                color = TextMuted
                                                            )
                                                        }
                                                    }

                                                    Text(
                                                        text = "$prefix${Formatters.formatCurrency(tx.amount)}",
                                                        fontWeight = FontWeight.Bold,
                                                        color = amountColor,
                                                        fontSize = 13.sp
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Start,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "← Desliza para volver a Patrimonio & Proyección",
                                            fontSize = 10.sp,
                                            color = TextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Indicadores tipo píldora del Pager
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("💰 Resumen & Proyección", "⚡ Movimientos Recientes").forEachIndexed { index, title ->
                            val isSelected = pagerState.currentPage == index
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) AccentNeon else FintechCardBorder)
                                    .clickable {
                                        coroutineScope.launch { pagerState.animateScrollToPage(index) }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) FintechBackground else TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Botón de Acción Rápida Principal
            item {
                Button(
                    onClick = onQuickActionClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentNeon)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Registrar",
                        tint = FintechBackground,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Registrar Movimiento Manual",
                        color = FintechBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            // Carrusel de Mis Cuentas y Tarjetas
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mis Cuentas y Tarjetas",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Ver tarjetas",
                        fontSize = 12.sp,
                        color = AccentSecondary,
                        modifier = Modifier.clickable { onNavigateToCards() }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    accounts.forEach { acc ->
                        val isCredit = acc.type == AccountType.CREDIT
                        val cardGlow = if (isCredit) NeonViolet else AccentNeon

                        GlassCard(
                            modifier = Modifier.width(170.dp),
                            accentGlow = cardGlow,
                            shapeRadius = 16.dp
                        ) {
                            Text(
                                text = acc.bankName,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = acc.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = if (isCredit) "Deuda Actual" else "Saldo Disponible",
                                fontSize = 10.sp,
                                color = if (isCredit) FintechExpense else NeonEmerald
                            )
                            Text(
                                text = Formatters.formatCurrency(acc.balance),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // MODALES Y VENTANAS FLOTANTES DE CUENTAS POR COBRAR
    // =========================================================================

    // 1. Modal: Agregar Cuenta por Cobrar (Sin día previsto)
    if (showAddReceivableDialog) {
        var debtorName by remember { mutableStateOf("") }
        var concept by remember { mutableStateOf("") }
        var amountText by remember { mutableStateOf("") }
        var note by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddReceivableDialog = false },
            title = {
                Text("Nueva Cuenta por Cobrar", fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Registra un cobro previsto para este mes. Se sumará a tu proyección mensual.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    OutlinedTextField(
                        value = debtorName,
                        onValueChange = { debtorName = it },
                        label = { Text("¿Quién te debe? (Persona o Cliente)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = concept,
                        onValueChange = { concept = it },
                        label = { Text("Concepto (ej. Préstamo, Trabajo freelance)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Monto por cobrar ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Nota adicional (opcional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        if (debtorName.isNotBlank() && amount > 0) {
                            onAddReceivable(debtorName.trim(), concept.trim(), amount, note.trim())
                            showAddReceivableDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentNeon)
                ) {
                    Text("Guardar Cuenta", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddReceivableDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }

    // 2. Modal: Editar Cuenta por Cobrar
    if (receivableToEdit != null) {
        val target = receivableToEdit!!
        var editDebtor by remember(target.id) { mutableStateOf(target.debtorName) }
        var editConcept by remember(target.id) { mutableStateOf(target.concept) }
        var editAmountText by remember(target.id) { mutableStateOf(target.amount.toString()) }
        var editNote by remember(target.id) { mutableStateOf(target.note) }

        AlertDialog(
            onDismissRequest = { receivableToEdit = null },
            title = {
                Text("Editar Cuenta por Cobrar", fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editDebtor,
                        onValueChange = { editDebtor = it },
                        label = { Text("¿Quién te debe?") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editConcept,
                        onValueChange = { editConcept = it },
                        label = { Text("Concepto") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editAmountText,
                        onValueChange = { editAmountText = it },
                        label = { Text("Monto por cobrar ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editNote,
                        onValueChange = { editNote = it },
                        label = { Text("Nota adicional") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = editAmountText.toDoubleOrNull() ?: 0.0
                        if (editDebtor.isNotBlank() && amt > 0) {
                            onUpdateReceivable(target.id, editDebtor.trim(), editConcept.trim(), amt, editNote.trim())
                            receivableToEdit = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentNeon)
                ) {
                    Text("Guardar Cambios", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { receivableToEdit = null }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }

    // 3. Modal: Cobro (Pago Parcial o Pago Completo)
    if (receivableToCollect != null) {
        val rec = receivableToCollect!!
        val liquidAccounts = accounts.filter { it.type == AccountType.DEBIT || it.type == AccountType.CASH }
        var selectedAccountId by remember { mutableStateOf(liquidAccounts.firstOrNull()?.id ?: "") }
        var isPartialPayment by remember { mutableStateOf(false) }
        var partialAmountText by remember { mutableStateOf("") }
        val unpaidBreakdown = remember(rec) { rec.breakdown.filter { !it.isPaid } }
        var selectedBreakdownItemId by remember(rec) { mutableStateOf<String?>(unpaidBreakdown.firstOrNull()?.id) }

        LaunchedEffect(selectedBreakdownItemId, isPartialPayment) {
            if (isPartialPayment && unpaidBreakdown.isNotEmpty() && selectedBreakdownItemId != null) {
                val item = unpaidBreakdown.find { it.id == selectedBreakdownItemId }
                if (item != null) {
                    partialAmountText = item.amount.toString()
                }
            }
        }

        AlertDialog(
            onDismissRequest = { receivableToCollect = null },
            title = {
                Text("Cobrar Cuenta", fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "${rec.debtorName} • ${rec.concept}",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Saldo total por cobrar: ${Formatters.formatCurrency(rec.amount)}",
                        color = AccentNeon,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Selecciona el tipo de pago:", fontSize = 12.sp, color = TextSecondary)

                    // Selector: Pago Completo o Pago Parcial
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = !isPartialPayment,
                            onClick = { isPartialPayment = false },
                            label = { Text("Pago Completo", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.weight(1f),
                            leadingIcon = if (!isPartialPayment) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null
                        )

                        FilterChip(
                            selected = isPartialPayment,
                            onClick = { isPartialPayment = true },
                            label = { Text("Pago Parcial", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.weight(1f),
                            leadingIcon = if (isPartialPayment) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null
                        )
                    }

                    if (isPartialPayment) {
                        if (unpaidBreakdown.isNotEmpty()) {
                            Text("Selecciona el concepto que te están pagando:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(FintechSurface)
                                    .padding(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                unpaidBreakdown.forEach { item ->
                                    val isSelected = selectedBreakdownItemId == item.id
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) AccentNeon.copy(alpha = 0.15f) else Color.Transparent)
                                            .clickable {
                                                selectedBreakdownItemId = item.id
                                                partialAmountText = item.amount.toString()
                                            }
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = {
                                                selectedBreakdownItemId = item.id
                                                partialAmountText = item.amount.toString()
                                            },
                                            colors = RadioButtonDefaults.colors(selectedColor = AccentNeon)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(item.concept, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextPrimary)
                                            Text("Monto: ${Formatters.formatCurrency(item.amount)}", fontSize = 11.sp, color = AccentNeon)
                                        }
                                    }
                                }
                            }

                            val chosenItem = unpaidBreakdown.find { it.id == selectedBreakdownItemId }
                            val chosenAmt = chosenItem?.amount ?: 0.0
                            val remaining = (rec.amount - chosenAmt).coerceAtLeast(0.0)
                            Text(
                                text = "Monto a cobrar: ${Formatters.formatCurrency(chosenAmt)} • Saldo posterior: ${Formatters.formatCurrency(remaining)}",
                                fontSize = 11.sp,
                                color = NeonEmerald,
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            OutlinedTextField(
                                value = partialAmountText,
                                onValueChange = { partialAmountText = it },
                                label = { Text("Monto abonado ($)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth()
                            )

                            val parsedPartial = partialAmountText.toDoubleOrNull() ?: 0.0
                            val remaining = (rec.amount - parsedPartial).coerceAtLeast(0.0)
                            Text(
                                text = if (parsedPartial > rec.amount) "⚠️ El abono excede el saldo restante"
                                else "Saldo restante tras este abono: ${Formatters.formatCurrency(remaining)}",
                                fontSize = 11.sp,
                                color = if (parsedPartial > rec.amount) FintechExpense else NeonEmerald,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Text("¿A qué cuenta física ingresa el dinero?", fontSize = 12.sp, color = TextSecondary)

                    liquidAccounts.forEach { acc ->
                        val isSelected = selectedAccountId == acc.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) AccentNeon.copy(alpha = 0.15f) else FintechSurface)
                                .clickable { selectedAccountId = acc.id }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedAccountId = acc.id },
                                colors = RadioButtonDefaults.colors(selectedColor = AccentNeon)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(acc.name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 12.sp)
                                Text("${acc.bankName} • Saldo: ${Formatters.formatCurrency(acc.balance)}", color = TextSecondary, fontSize = 10.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedAccountId.isNotBlank()) {
                            if (isPartialPayment) {
                                if (unpaidBreakdown.isNotEmpty()) {
                                    val chosenItem = unpaidBreakdown.find { it.id == selectedBreakdownItemId }
                                    if (chosenItem != null) {
                                        onCollectReceivable(rec.id, selectedAccountId, chosenItem.amount, true, chosenItem.id)
                                        receivableToCollect = null
                                    }
                                } else {
                                    val amt = partialAmountText.toDoubleOrNull() ?: 0.0
                                    if (amt > 0 && amt <= rec.amount) {
                                        onCollectReceivable(rec.id, selectedAccountId, amt, true, null)
                                        receivableToCollect = null
                                    }
                                }
                            } else {
                                onCollectReceivable(rec.id, selectedAccountId, rec.amount, false, null)
                                receivableToCollect = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald)
                ) {
                    Text("Confirmar Cobro", color = FintechBackground, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { receivableToCollect = null }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }

    // Alerta bloqueante: no permite salir si el desglose no está completo (suma == total)
    if (showIncompleteBreakdownAlert) {
        val currentRec = receivables.find { it.id == selectedReceivableDetailId }
        val bSum = currentRec?.breakdown?.sumOf { it.amount } ?: 0.0
        val totalAmt = currentRec?.amount ?: 0.0
        val diff = (totalAmt - bSum)

        AlertDialog(
            onDismissRequest = { /* Bloqueado hasta resolver */ },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = FintechWarning)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Desglose Incompleto", fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "No puedes salir hasta completar el desglose o vaciarlo.",
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "• Monto de la cuenta: ${Formatters.formatCurrency(totalAmt)}\n• Suma de conceptos: ${Formatters.formatCurrency(bSum)}\n• Diferencia restante: ${Formatters.formatCurrency(diff)}",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "La suma de los conceptos debe coincidir exactamente con el total de la cuenta por cobrar.",
                        color = FintechWarning,
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showIncompleteBreakdownAlert = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentNeon)
                ) {
                    Text("Completar Desglose", color = FintechBackground, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        if (currentRec != null) {
                            currentRec.breakdown.forEach { onRemoveBreakdownItem(currentRec.id, it.id) }
                        }
                        showIncompleteBreakdownAlert = false
                        selectedReceivableDetailId = null
                    }
                ) {
                    Text("Vaciar Desglose y Salir", color = FintechExpense, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

// =============================================================================
// PÁGINA COMPLETA NUEVA DE DETALLE DE CUENTA POR COBRAR
// Reemplaza el popup anterior con navegación y pantalla completa
// =============================================================================
@Composable
fun ReceivableDetailPage(
    receivable: AccountReceivable,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCollect: () -> Unit,
    onAddBreakdownItem: (concept: String, amount: Double) -> Unit,
    onUpdateBreakdownItem: (itemId: String, concept: String, amount: Double) -> Unit,
    onRemoveBreakdownItem: (itemId: String) -> Unit,
    onClearAllBreakdown: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var newConceptText by remember { mutableStateOf("") }
    var newAmountText by remember { mutableStateOf("") }
    var editingItem by remember { mutableStateOf<ReceivableBreakdownItem?>(null) }
    var editConceptText by remember { mutableStateOf("") }
    var editAmountText by remember { mutableStateOf("") }

    val breakdownSum = receivable.breakdown.sumOf { it.amount }
    val remainingToBreakdown = (receivable.amount - breakdownSum).coerceAtLeast(0.0)
    val isComplete = receivable.breakdown.isEmpty() || kotlin.math.abs(breakdownSum - receivable.amount) < 0.01

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(FintechBackground)
    ) {
        // Cabecera de la Página Nueva
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Regresar",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "Detalle de Cuenta",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = receivable.debtorName,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            // Acciones en la esquina: Editar y Borrar
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar Cuenta",
                        tint = AccentNeon
                    )
                }
                IconButton(onClick = { showDeleteConfirmDialog = true }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar Cuenta",
                        tint = FintechExpense
                    )
                }
            }
        }

        // Contenido con scroll
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Tarjeta Principal de Información
            GlassCard(
                accentGlow = if (receivable.isCollected) NeonEmerald else FintechWarning,
                borderColor = if (receivable.isCollected) NeonEmerald.copy(alpha = 0.35f) else FintechWarning.copy(alpha = 0.35f),
                shapeRadius = 18.dp
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = receivable.debtorName,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                            Text(
                                text = receivable.concept,
                                fontSize = 13.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (receivable.isCollected) NeonEmerald.copy(alpha = 0.15f)
                                    else FintechWarning.copy(alpha = 0.15f)
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (receivable.isCollected) "✅ COBRADO" else "⏳ PENDIENTE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (receivable.isCollected) NeonEmerald else FintechWarning
                            )
                        }
                    }

                    if (receivable.note.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "📝 ${receivable.note}",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = FintechBorder, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = if (receivable.isCollected) "Monto Cobrado" else "Saldo Pendiente",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = Formatters.formatCurrency(receivable.amount),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (receivable.isCollected) NeonEmerald else AccentNeon
                            )
                        }

                        if (receivable.initialAmount > receivable.amount && !receivable.isCollected) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Monto inicial", fontSize = 10.sp, color = TextMuted)
                                Text(
                                    Formatters.formatCurrency(receivable.initialAmount),
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text("Abonado: ${Formatters.formatCurrency(receivable.initialAmount - receivable.amount)}", fontSize = 10.sp, color = NeonEmerald)
                            }
                        }
                    }
                }
            }

            // Botón de Cobrar
            if (!receivable.isCollected) {
                Button(
                    onClick = onCollect,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald)
                ) {
                    Icon(Icons.Default.Payments, contentDescription = null, tint = FintechBackground, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cobrar (Pago Parcial o Completo)", color = FintechBackground, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            // SECCIÓN: DESGLOSE DE CONCEPTOS
            GlassCard(
                accentGlow = if (isComplete) NeonEmerald else FintechWarning,
                borderColor = if (isComplete) NeonEmerald.copy(alpha = 0.35f) else FintechWarning.copy(alpha = 0.35f),
                shapeRadius = 18.dp
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FormatListBulleted, contentDescription = null, tint = AccentNeon, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Desglose por Conceptos",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextPrimary
                            )
                        }

                        if (receivable.breakdown.isNotEmpty()) {
                            Text(
                                text = "${receivable.breakdown.size} conceptos",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    // Barra de progreso del desglose
                    if (receivable.amount > 0) {
                        LinearProgressIndicator(
                            progress = {
                                (breakdownSum / receivable.amount).toFloat().coerceIn(0f, 1f)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = if (isComplete) NeonEmerald else FintechWarning,
                            trackColor = FintechSurface
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Desglosado: ${Formatters.formatCurrency(breakdownSum)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isComplete) NeonEmerald else TextSecondary
                        )
                        Text(
                            text = if (receivable.breakdown.isEmpty()) "Sin desglose"
                            else if (kotlin.math.abs(remainingToBreakdown) < 0.01) "✅ 100% Cubierto"
                            else "⚠️ Faltan ${Formatters.formatCurrency(remainingToBreakdown)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isComplete) NeonEmerald else FintechWarning
                        )
                    }

                    if (receivable.breakdown.isNotEmpty() && !isComplete) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(FintechWarning.copy(alpha = 0.12f))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "⚠️ La suma de los desgloses no coincide con el total. No podrás salir de esta pantalla hasta completar los ${Formatters.formatCurrency(remainingToBreakdown)} restantes o vaciar el desglose.",
                                color = FintechWarning,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Lista de conceptos existentes
                    if (receivable.breakdown.isEmpty()) {
                        Text(
                            text = "No has desglosado esta cuenta aún. Si deseas desglosarla (ej. 5 pagos específicos), agrega aquí los conceptos hasta alcanzar los ${Formatters.formatCurrency(receivable.amount)}.",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            receivable.breakdown.forEach { item ->
                                val isPaid = item.isPaid
                                val isCurrentlyEditing = editingItem?.id == item.id

                                if (isCurrentlyEditing) {
                                    // Formulario de edición en línea
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(FintechSurface)
                                            .border(1.dp, AccentNeon, RoundedCornerShape(10.dp))
                                            .padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text("Editar concepto", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentNeon)
                                        OutlinedTextField(
                                            value = editConceptText,
                                            onValueChange = { editConceptText = it },
                                            label = { Text("Concepto") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true
                                        )
                                        OutlinedTextField(
                                            value = editAmountText,
                                            onValueChange = { editAmountText = it },
                                            label = { Text("Monto ($)") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            TextButton(onClick = { editingItem = null }) {
                                                Text("Cancelar", color = TextSecondary, fontSize = 12.sp)
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Button(
                                                onClick = {
                                                    val amt = editAmountText.toDoubleOrNull() ?: 0.0
                                                    if (editConceptText.isNotBlank() && amt > 0) {
                                                        onUpdateBreakdownItem(item.id, editConceptText.trim(), amt)
                                                        editingItem = null
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = AccentNeon)
                                            ) {
                                                Text("Guardar", color = FintechBackground, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                } else {
                                    // Visualización normal del concepto
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isPaid) NeonEmerald.copy(alpha = 0.08f) else FintechSurface)
                                            .border(0.5.dp, if (isPaid) NeonEmerald.copy(alpha = 0.3f) else FintechBorder, RoundedCornerShape(10.dp))
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    item.concept,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 13.sp,
                                                    color = TextPrimary
                                                )
                                                if (isPaid) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        "✅ Pagado",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = NeonEmerald
                                                    )
                                                }
                                            }
                                            Text(
                                                Formatters.formatCurrency(item.amount),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = if (isPaid) NeonEmerald else AccentNeon
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (!isPaid) {
                                                IconButton(
                                                    onClick = {
                                                        editingItem = item
                                                        editConceptText = item.concept
                                                        editAmountText = item.amount.toString()
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = AccentNeon, modifier = Modifier.size(16.dp))
                                                }
                                                Spacer(modifier = Modifier.width(2.dp))
                                            }
                                            IconButton(
                                                onClick = { onRemoveBreakdownItem(item.id) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Quitar", tint = TextMuted, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(color = FintechBorder, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(4.dp))

                    // Formulario para Agregar Nuevo Concepto
                    Text("Agregar concepto al desglose:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)

                    OutlinedTextField(
                        value = newConceptText,
                        onValueChange = { newConceptText = it },
                        label = { Text("Concepto (ej. Pago 1, Despensa, etc.)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = newAmountText,
                        onValueChange = { newAmountText = it },
                        label = { Text("Monto ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    if (remainingToBreakdown > 0.01) {
                        TextButton(
                            onClick = {
                                val rounded = (remainingToBreakdown * 100.0).roundToInt() / 100.0
                                newAmountText = rounded.toString()
                            },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                "⚡ Autocompletar faltante: ${Formatters.formatCurrency(remainingToBreakdown)}",
                                fontSize = 11.sp,
                                color = AccentSecondary
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val amt = newAmountText.toDoubleOrNull() ?: 0.0
                            if (newConceptText.isNotBlank() && amt > 0) {
                                onAddBreakdownItem(newConceptText.trim(), amt)
                                newConceptText = ""
                                newAmountText = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentNeon)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = FintechBackground, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Agregar Concepto", color = FintechBackground, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

        // Diálogo de Confirmación para Eliminar Cuenta
        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false },
                title = { Text("¿Eliminar cuenta por cobrar?", fontWeight = FontWeight.Bold, color = TextPrimary) },
                text = { Text("Se cancelará la cuenta de ${receivable.debtorName} por ${Formatters.formatCurrency(receivable.amount)} y se registrará en el historial.", color = TextSecondary) },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteConfirmDialog = false
                            onDelete()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FintechExpense)
                    ) {
                        Text("Eliminar", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = false }) {
                        Text("Cancelar", color = TextSecondary)
                    }
                }
            )
        }
    }
}
