package com.example.financeflow.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(FintechBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Banner de Alerta de Permiso de Notificaciones si no está activo y no ha sido descartado
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

        // Tarjeta Principal: Patrimonio Neto
        item {
            GlassCard(
                accentGlow = AccentNeon,
                borderColor = AccentNeon.copy(alpha = 0.35f)
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
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

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
                            .padding(12.dp)
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
                                fontSize = 15.sp,
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
                            .padding(12.dp)
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
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = FintechExpense
                            )
                        }
                    }
                }
            }
        }

        // Tarjeta de Proyección del Mes: Dinero en Físico vs Cuentas por Cobrar
        item {
            GlassCard(
                accentGlow = AccentSecondary,
                borderColor = AccentSecondary.copy(alpha = 0.3f),
                shapeRadius = 18.dp
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
                            text = "PROYECCIÓN MENSUAL",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Dinero disponible en físico
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
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
                                Text("Dinero en Físico", fontSize = 11.sp, color = TextSecondary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = Formatters.formatCurrency(liquidPhysicalBalance),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonEmerald
                            )
                            Text("En débito y efectivo", fontSize = 9.sp, color = TextMuted)
                        }
                    }

                    // Por cobrar (No en físico aún)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
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
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = FintechWarning
                            )
                            Text("No físico todavía", fontSize = 9.sp, color = FintechWarning.copy(alpha = 0.8f))
                        }
                    }
                }
            }
        }

        // ==========================================
        // SECCIÓN: CUENTAS POR COBRAR (DINERO POR RECIBIR)
        // ==========================================
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "📥 Cuentas por Cobrar",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Dinero que te deben (suma al mes, no físico aún)",
                        fontSize = 12.sp,
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
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Agregar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(TextMuted.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🤝", fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Sin cuentas por cobrar pendientes", fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 13.sp)
                            Text("Toca en 'Agregar' para registrar préstamos o cobros previstos", color = TextSecondary, fontSize = 11.sp)
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
                                    text = "${rec.concept} • Día previsto: ${rec.dueDayOfMonth}",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                                if (!isDone) {
                                    Text(
                                        text = "⚠️ No en físico todavía",
                                        color = FintechWarning,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                } else {
                                    Text(
                                        text = "✅ Cobrado en físico",
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
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                                        modifier = Modifier.height(26.dp)
                                    ) {
                                        Text("Cobrar", fontSize = 11.sp, color = FintechBackground, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                IconButton(
                                    onClick = { onDeleteReceivable(rec.id) },
                                    modifier = Modifier.size(26.dp)
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

        // Botón de Acción Rápida Principal
        item {
            Button(
                onClick = onQuickActionClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
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
                    fontSize = 15.sp
                )
            }
        }

        // Carrusel de Cuentas y Tarjetas
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mis Cuentas y Tarjetas",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Ver tarjetas",
                    fontSize = 13.sp,
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

        // Últimos Movimientos
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Últimos Movimientos",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Ver todos (${transactions.size})",
                    fontSize = 13.sp,
                    color = AccentSecondary,
                    modifier = Modifier.clickable { onNavigateToTransactions() }
                )
            }
        }

        val recentTransactions = transactions.take(5)
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
