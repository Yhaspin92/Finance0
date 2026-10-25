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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    accounts: List<Account>,
    transactions: List<Transaction>,
    creditCards: List<CreditCard>,
    isNotificationPermissionGranted: Boolean,
    onOpenNotificationSettings: () -> Unit,
    onQuickActionClick: () -> Unit,
    onNavigateToTransactions: () -> Unit,
    onNavigateToCards: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(FintechBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Banner de Alerta de Permiso de Notificaciones si no está activo
        if (!isNotificationPermissionGranted) {
            item {
                GlassCard(
                    accentGlow = FintechWarning,
                    borderColor = FintechWarning.copy(alpha = 0.5f),
                    onClick = onOpenNotificationSettings
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
                                    text = "Lectura de Notificaciones Inactiva",
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Toca aquí para autorizar la lectura de bancos",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "Abrir",
                            tint = FintechWarning
                        )
                    }
                }
            }
        }

        // Tarjeta Principal: Patrimonio Neto
        item {
            GlassCard(
                accentGlow = NeonEmerald,
                borderColor = NeonEmerald.copy(alpha = 0.35f)
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
                            .background(NeonEmerald.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "ACTUALIZADO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonEmerald
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

        // Botón de Acción Rápida Principal
        item {
            Button(
                onClick = onQuickActionClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald)
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
                    color = NeonCyan,
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
                    val cardGlow = if (isCredit) NeonViolet else NeonEmerald

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
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        if (acc.last4Digits.isNotBlank()) {
                            Text(
                                text = "•••• ${acc.last4Digits}",
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
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
                    text = "Ver todos",
                    fontSize = 13.sp,
                    color = NeonEmerald,
                    modifier = Modifier.clickable { onNavigateToTransactions() }
                )
            }
        }

        val recentTx = transactions.take(6)
        if (recentTx.isEmpty()) {
            item {
                Text(
                    text = "No hay transacciones registradas aún.",
                    color = TextMuted,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        } else {
            items(recentTx) { tx ->
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
                                            color = NeonCyan,
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
