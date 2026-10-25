package com.example.financeflow.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import com.example.financeflow.domain.model.*
import com.example.financeflow.ui.components.Formatters
import com.example.financeflow.ui.components.GlassCard
import com.example.financeflow.ui.theme.*
import kotlinx.coroutines.launch

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
    isNotificationPermissionGranted: Boolean,
    isBannerDismissed: Boolean,
    onDismissNotificationBanner: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onAddReceivable: (debtorName: String, concept: String, amount: Double, dueDayOfMonth: Int, note: String) -> Unit,
    onMarkReceivableCollected: (receivableId: String, targetAccountId: String) -> Unit,
    onDeleteReceivable: (receivableId: String) -> Unit,
    onQuickActionClick: () -> Unit,
    onNavigateToTransactions: () -> Unit,
    onNavigateToCards: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddReceivableDialog by remember { mutableStateOf(false) }
    var receivableToCollect by remember { mutableStateOf<AccountReceivable?>(null) }
    var showReceivablesSubScreen by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 3 })

    // =========================================================================
    // PANTALLA SECUNDARIA DEDICADA: CUENTAS POR COBRAR
    // Se activa al tocar en el Total Proyectado / Por Cobrar
    // =========================================================================
    if (showReceivablesSubScreen) {
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
                        text = "Dinero por recibir en el mes (no físico aún)",
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
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "💡 Este dinero suma a tu total proyectado del mes. Al marcarlo como cobrado, se sumará directamente a tu cuenta bancaria seleccionada.",
                                fontSize = 11.sp,
                                color = TextMuted
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
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
                                            text = "${rec.concept} • Previsto: día ${rec.dueDayOfMonth}",
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                        if (!isDone) {
                                            Text(
                                                text = "⚠️ Pendiente (No en físico aún)",
                                                color = FintechWarning,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        } else {
                                            Text(
                                                text = "✅ Cobrado e ingresado a saldo",
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
            // CARRUSEL INTERACTIVO HERO (DESLIZAMIENTO IZQUIERDA / DERECHA)
            // 0: Patrimonio Neto | 1: Total Proyectado del Mes | 2: Últimos Movimientos
            // =====================================================================
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth()
                    ) { page ->
                        when (page) {
                            // PÁGINA 0: PATRIMONIO NETO
                            0 -> {
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

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = Formatters.formatCurrency(netWorth),
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TextPrimary
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Flujo mensual (Ingresos vs Egresos)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // Ingresos
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(FintechSurface)
                                                .padding(10.dp)
                                        ) {
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        Icons.Default.ArrowDownward,
                                                        contentDescription = "Ingreso",
                                                        tint = FintechIncome,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(text = "Ingresos Mes", fontSize = 11.sp, color = TextSecondary)
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = Formatters.formatCurrency(monthlyIncome),
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = FintechIncome
                                                )
                                            }
                                        }

                                        // Gastos
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(FintechSurface)
                                                .padding(10.dp)
                                        ) {
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        Icons.Default.ArrowUpward,
                                                        contentDescription = "Gasto",
                                                        tint = FintechExpense,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(text = "Gastos Mes", fontSize = 11.sp, color = TextSecondary)
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = Formatters.formatCurrency(monthlyExpense),
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = FintechExpense
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Desliza para ver Proyección →",
                                            fontSize = 10.sp,
                                            color = TextMuted
                                        )
                                    }
                                }
                            }

                            // PÁGINA 1: TOTAL PROYECTADO DEL MES (INTERACTIVO -> ABRE CUENTAS POR COBRAR)
                            1 -> {
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
                                                fontSize = 13.sp,
                                                color = TextSecondary,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = Formatters.formatCurrency(projectedMonthlyTotal),
                                                fontSize = 26.sp,
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

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Dinero en físico
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(FintechSurface)
                                                .padding(10.dp)
                                        ) {
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(8.dp)
                                                            .clip(CircleShape)
                                                            .background(NeonEmerald)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("En Físico", fontSize = 11.sp, color = TextSecondary)
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = Formatters.formatCurrency(liquidPhysicalBalance),
                                                    fontSize = 13.sp,
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
                                                .padding(10.dp)
                                        ) {
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(8.dp)
                                                            .clip(CircleShape)
                                                            .background(FintechWarning)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Por Cobrar", fontSize = 11.sp, color = TextSecondary)
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = Formatters.formatCurrency(pendingReceivablesTotal),
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = FintechWarning
                                                )
                                                Text("No físico aún", fontSize = 9.sp, color = FintechWarning.copy(alpha = 0.8f))
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Botón de acceso a Cuentas por Cobrar
                                    Surface(
                                        onClick = { showReceivablesSubScreen = true },
                                        shape = RoundedCornerShape(10.dp),
                                        color = AccentSecondary.copy(alpha = 0.15f),
                                        border = BorderStroke(1.dp, AccentSecondary.copy(alpha = 0.35f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "📥 Toca aquí para ver Cuentas por Cobrar (${receivables.count { !it.isCollected }} pendientes)",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AccentSecondary
                                                )
                                            }
                                            Icon(
                                                Icons.Default.ArrowForward,
                                                contentDescription = null,
                                                tint = AccentSecondary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // PÁGINA 2: ÚLTIMOS MOVIMIENTOS DESLIZABLES
                            2 -> {
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
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "⚡ Últimos Movimientos",
                                                fontSize = 14.sp,
                                                color = TextPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Text(
                                            text = "Ver todos (${transactions.size})",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AccentSecondary,
                                            modifier = Modifier.clickable { onNavigateToTransactions() }
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    val heroTxList = transactions.take(3)
                                    if (heroTxList.isEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 16.dp),
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
                                                        .padding(horizontal = 10.dp, vertical = 6.dp),
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

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Start,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "← Desliza para volver a Patrimonio",
                                            fontSize = 10.sp,
                                            color = TextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Indicadores tipo píldora del Pager interactivo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("💰 Patrimonio", "🔮 Proyección", "⚡ Movimientos").forEachIndexed { index, title ->
                            val isSelected = pagerState.currentPage == index
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) AccentNeon else FintechCardBorder)
                                    .clickable {
                                        coroutineScope.launch { pagerState.animateScrollToPage(index) }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
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

            // Sección de Movimientos Recientes Completa
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Movimientos Recientes",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Ver todos (${transactions.size})",
                        fontSize = 12.sp,
                        color = AccentSecondary,
                        modifier = Modifier.clickable { onNavigateToTransactions() }
                    )
                }
            }

            val recentTransactions = transactions.take(6)
            if (recentTransactions.isEmpty()) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "No hay movimientos registrados.",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                items(recentTransactions) { tx ->
                    val isIncome = tx.type == TransactionType.INCOME
                    val amountColor = if (isIncome) FintechIncome else TextPrimary
                    val prefix = if (isIncome) "+" else "-"

                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shapeRadius = 14.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (tx.type) {
                                                TransactionType.INCOME -> FintechIncome.copy(alpha = 0.15f)
                                                TransactionType.EXPENSE -> FintechExpense.copy(alpha = 0.15f)
                                                TransactionType.TRANSFER -> NeonViolet.copy(alpha = 0.15f)
                                                TransactionType.CREDIT_CARD_PAYMENT -> NeonCyan.copy(alpha = 0.15f)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        when (tx.type) {
                                            TransactionType.INCOME -> Icons.Default.ArrowDownward
                                            TransactionType.EXPENSE -> Icons.Default.ShoppingBag
                                            TransactionType.TRANSFER -> Icons.Default.SwapHoriz
                                            TransactionType.CREDIT_CARD_PAYMENT -> Icons.Default.CreditCard
                                        },
                                        contentDescription = tx.type.name,
                                        tint = when (tx.type) {
                                            TransactionType.INCOME -> FintechIncome
                                            TransactionType.EXPENSE -> FintechExpense
                                            TransactionType.TRANSFER -> NeonViolet
                                            TransactionType.CREDIT_CARD_PAYMENT -> NeonCyan
                                        },
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = tx.title,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary,
                                        fontSize = 14.sp,
                                        maxLines = 1
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = Formatters.formatDate(tx.timestamp),
                                            color = TextMuted,
                                            fontSize = 11.sp
                                        )
                                        if (tx.isAutoDetected) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "• 🤖 Notificación",
                                                color = AccentSecondary,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }

                            Text(
                                text = "$prefix${Formatters.formatCurrency(tx.amount)}",
                                fontWeight = FontWeight.Bold,
                                color = amountColor,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal: Agregar Cuenta por Cobrar
    if (showAddReceivableDialog) {
        var debtorName by remember { mutableStateOf("") }
        var concept by remember { mutableStateOf("") }
        var amountText by remember { mutableStateOf("") }
        var dueDayText by remember { mutableStateOf("15") }
        var note by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddReceivableDialog = false },
            title = {
                Text("Nueva Cuenta por Cobrar", fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Registra un préstamo o cobro previsto para este mes. Se sumará a tu proyección mensual, pero no como dinero en físico aún.",
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
                        value = dueDayText,
                        onValueChange = { dueDayText = it },
                        label = { Text("Día previsto del mes para cobrar (1-31)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                        val day = dueDayText.toIntOrNull() ?: 1
                        if (debtorName.isNotBlank() && amount > 0) {
                            onAddReceivable(debtorName.trim(), concept.trim(), amount, day, note.trim())
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

    // Modal: Marcar como Cobrado en Físico
    if (receivableToCollect != null) {
        val rec = receivableToCollect!!
        val liquidAccounts = accounts.filter { it.type == AccountType.DEBIT || it.type == AccountType.CASH }
        var selectedAccountId by remember { mutableStateOf(liquidAccounts.firstOrNull()?.id ?: "") }

        AlertDialog(
            onDismissRequest = { receivableToCollect = null },
            title = {
                Text("Confirmar Cobro en Físico", fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "¿A qué cuenta física ingresó el pago de ${Formatters.formatCurrency(rec.amount)} de ${rec.debtorName}?",
                        color = TextPrimary,
                        fontSize = 13.sp
                    )

                    liquidAccounts.forEach { acc ->
                        val isSelected = selectedAccountId == acc.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) AccentNeon.copy(alpha = 0.15f) else FintechSurface)
                                .clickable { selectedAccountId = acc.id }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedAccountId = acc.id },
                                colors = RadioButtonDefaults.colors(selectedColor = AccentNeon)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(acc.name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                                Text("${acc.bankName} • Saldo: ${Formatters.formatCurrency(acc.balance)}", color = TextSecondary, fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedAccountId.isNotBlank()) {
                            onMarkReceivableCollected(rec.id, selectedAccountId)
                            receivableToCollect = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald)
                ) {
                    Text("Confirmar Depósito", color = FintechBackground, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { receivableToCollect = null }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }
}
