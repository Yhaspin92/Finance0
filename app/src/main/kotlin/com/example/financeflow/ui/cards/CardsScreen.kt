package com.example.financeflow.ui.cards

import androidx.compose.foundation.background
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financeflow.domain.model.Account
import com.example.financeflow.domain.model.CreditCard
import com.example.financeflow.domain.model.InstallmentPurchase
import com.example.financeflow.ui.components.Formatters
import com.example.financeflow.ui.components.GlassCard
import com.example.financeflow.ui.theme.*
import java.util.Calendar

@Composable
fun CardsScreen(
    creditCards: List<CreditCard>,
    accounts: List<Account>,
    installmentPurchases: List<InstallmentPurchase>,
    onPayCardClick: (debitAccountId: String, creditCardId: String, amount: Double) -> Unit,
    onAddInstallmentPurchase: (cardId: String, title: String, totalAmount: Double, totalMonths: Int, note: String) -> Unit,
    onAdvanceInstallmentMonth: (String) -> Unit,
    onDeleteInstallmentPurchase: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var payingCard by remember { mutableStateOf<CreditCard?>(null) }
    var payAmountText by remember { mutableStateOf("") }
    var selectedDebitAccountId by remember {
        mutableStateOf(accounts.firstOrNull { it.type != com.example.financeflow.domain.model.AccountType.CREDIT }?.id ?: "")
    }

    var showAddMsiDialog by remember { mutableStateOf(false) }

    val todayDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    val maxDayInMonth = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(FintechBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Tarjetas de Crédito",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Cortes, fechas límite y Meses Sin Intereses (MSI)",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }

                Button(
                    onClick = { showAddMsiDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonViolet)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "MSI", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ MSI", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Resumen total de Deuda vs Límite de Crédito
        item {
            val totalDebt = creditCards.sumOf { it.currentDebt }
            val totalLimit = creditCards.sumOf { it.creditLimit }
            val totalAvailable = (totalLimit - totalDebt).coerceAtLeast(0.0)
            val overallRatio = if (totalLimit > 0) (totalDebt / totalLimit).toFloat() else 0f

            GlassCard(
                accentGlow = NeonViolet,
                borderColor = NeonViolet.copy(alpha = 0.4f)
            ) {
                Text(text = "Deuda Total en Tarjetas", fontSize = 13.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = Formatters.formatCurrency(totalDebt),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = FintechExpense
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Línea Total: ${Formatters.formatCurrency(totalLimit)}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "Disponible: ${Formatters.formatCurrency(totalAvailable)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NeonEmerald
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { overallRatio.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = when {
                        overallRatio < 0.3f -> NeonEmerald
                        overallRatio < 0.6f -> FintechWarning
                        else -> FintechExpense
                    },
                    trackColor = FintechSurface
                )

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Uso de crédito total: ${(overallRatio * 100).toInt()}%",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }

        // Listado de Tarjetas
        if (creditCards.isEmpty()) {
            item {
                Text(
                    text = "No tienes tarjetas de crédito agregadas.",
                    color = TextMuted,
                    modifier = Modifier.padding(vertical = 20.dp)
                )
            }
        } else {
            items(creditCards) { card ->
                val ratio = if (card.creditLimit > 0) (card.currentDebt / card.creditLimit).toFloat() else 0f
                val available = (card.creditLimit - card.currentDebt).coerceAtLeast(0.0)

                val daysToCutoff = if (card.cutOffDay >= todayDay) {
                    card.cutOffDay - todayDay
                } else {
                    (maxDayInMonth - todayDay) + card.cutOffDay
                }

                val daysToPayment = if (card.paymentDueDay >= todayDay) {
                    card.paymentDueDay - todayDay
                } else {
                    (maxDayInMonth - todayDay) + card.paymentDueDay
                }

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    accentGlow = NeonViolet,
                    shapeRadius = 20.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(NeonViolet.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.CreditCard,
                                    contentDescription = "Tarjeta",
                                    tint = NeonViolet,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = card.cardName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${card.bank} •••• ${card.last4Digits}",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Text(
                            text = Formatters.formatCurrency(card.currentDebt),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = FintechExpense
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(FintechSurface)
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(text = "Fecha de Corte", fontSize = 11.sp, color = TextSecondary)
                                Text(
                                    text = "Día ${card.cutOffDay} de cada mes",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (daysToCutoff == 0) "¡CORTA HOY!" else "Faltan $daysToCutoff días",
                                    fontSize = 11.sp,
                                    color = if (daysToCutoff <= 3) FintechWarning else NeonCyan,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(FintechSurface)
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(text = "Límite de Pago", fontSize = 11.sp, color = TextSecondary)
                                Text(
                                    text = "Día ${card.paymentDueDay} de cada mes",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (daysToPayment == 0) "¡VENCE HOY!" else "Faltan $daysToPayment días",
                                    fontSize = 11.sp,
                                    color = if (daysToPayment <= 3) FintechAlert else NeonEmerald,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Línea: ${Formatters.formatCurrency(card.creditLimit)}",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "Disponible: ${Formatters.formatCurrency(available)}",
                            fontSize = 12.sp,
                            color = NeonEmerald
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { ratio.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = when {
                            ratio < 0.3f -> NeonEmerald
                            ratio < 0.6f -> FintechWarning
                            else -> FintechExpense
                        },
                        trackColor = FintechSurface
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            payingCard = card
                            payAmountText = card.currentDebt.toString()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonViolet)
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = "Pagar", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Registrar Pago a ${card.cardName}",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // SECCIÓN MESES SIN INTERESES (MSI)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🛍️ Compras a Meses Sin Intereses (MSI)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Monitorea tus mensualidades activas y pagos restantes",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        if (installmentPurchases.isEmpty()) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shapeRadius = 14.dp
                ) {
                    Text(
                        text = "No tienes compras a Meses Sin Intereses registradas.",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showAddMsiDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonViolet)
                    ) {
                        Text("Registrar primera compra a MSI", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            items(installmentPurchases) { msi ->
                val card = creditCards.find { it.id == msi.cardId }
                val progress = (msi.currentMonthPaid.toFloat() / msi.totalMonths.toFloat()).coerceIn(0f, 1f)
                val remainingMonths = (msi.totalMonths - msi.currentMonthPaid).coerceAtLeast(0)
                val remainingAmount = remainingMonths * msi.monthlyAmount

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    accentGlow = NeonCyan,
                    shapeRadius = 16.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = msi.title,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "${card?.cardName ?: "Tarjeta"} • ${msi.totalMonths} MSI",
                                color = NeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${Formatters.formatCurrency(msi.monthlyAmount)} / mes",
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Total: ${Formatters.formatCurrency(msi.totalAmount)}",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Barra de progreso de MSI
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Mes ${msi.currentMonthPaid} de ${msi.totalMonths} pagados",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                        Text(
                            text = if (remainingMonths == 0) "¡LIQUIDADO!" else "Faltan $remainingMonths meses (${Formatters.formatCurrency(remainingAmount)})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (remainingMonths == 0) NeonEmerald else FintechWarning
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (progress >= 1f) NeonEmerald else NeonCyan,
                        trackColor = FintechSurface
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (msi.currentMonthPaid < msi.totalMonths) {
                            TextButton(
                                onClick = { onAdvanceInstallmentMonth(msi.id) },
                                colors = ButtonDefaults.textButtonColors(contentColor = NeonEmerald)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Avanzar", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Avanzar Mes (+1)", fontSize = 11.sp)
                            }
                        }

                        TextButton(
                            onClick = { onDeleteInstallmentPurchase(msi.id) },
                            colors = ButtonDefaults.textButtonColors(contentColor = FintechExpense)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Borrar", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Eliminar", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }

    // Modal para registrar pago de tarjeta
    if (payingCard != null) {
        val card = payingCard!!
        AlertDialog(
            onDismissRequest = { payingCard = null },
            title = {
                Text(
                    text = "Pagar ${card.cardName}",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Deuda actual: ${Formatters.formatCurrency(card.currentDebt)}",
                        fontSize = 14.sp,
                        color = FintechExpense,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "Pagar desde cuenta de débito:", fontSize = 12.sp, color = TextSecondary)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        accounts.filter { it.type != com.example.financeflow.domain.model.AccountType.CREDIT }.forEach { acc ->
                            val isSelected = acc.id == selectedDebitAccountId
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedDebitAccountId = acc.id },
                                label = { Text("${acc.bankName} (${Formatters.formatCurrency(acc.balance)})") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Monto a pagar ($ MXN):", fontSize = 12.sp, color = TextSecondary)
                    OutlinedTextField(
                        value = payAmountText,
                        onValueChange = { payAmountText = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = payAmountText.toDoubleOrNull() ?: 0.0
                        if (amount > 0 && selectedDebitAccountId.isNotBlank()) {
                            onPayCardClick(selectedDebitAccountId, card.id, amount)
                            payingCard = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald)
                ) {
                    Text("Confirmar Pago", color = FintechBackground, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { payingCard = null }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = FintechSurface
        )
    }

    // Modal para Registrar Nueva Compra a Meses Sin Intereses (MSI)
    if (showAddMsiDialog) {
        var msiTitle by remember { mutableStateOf("") }
        var msiAmountText by remember { mutableStateOf("") }
        var msiMonths by remember { mutableStateOf(12) }
        var selectedMsiCardId by remember { mutableStateOf(creditCards.firstOrNull()?.id ?: "") }
        var msiNote by remember { mutableStateOf("") }

        val amountVal = msiAmountText.toDoubleOrNull() ?: 0.0
        val monthlyCalc = if (msiMonths > 0 && amountVal > 0) amountVal / msiMonths else 0.0

        AlertDialog(
            onDismissRequest = { showAddMsiDialog = false },
            title = {
                Text(
                    text = "Nueva Compra a Meses Sin Intereses",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column {
                    Text(text = "Tarjeta de Crédito:", fontSize = 12.sp, color = TextSecondary)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        creditCards.forEach { card ->
                            val isSelected = card.id == selectedMsiCardId
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedMsiCardId = card.id },
                                label = { Text(card.cardName) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "Concepto o Comercio:", fontSize = 12.sp, color = TextSecondary)
                    OutlinedTextField(
                        value = msiTitle,
                        onValueChange = { msiTitle = it },
                        placeholder = { Text("Ej. MacBook, iPhone, Vuelo, Pantalla") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "Monto Total de la Compra ($ MXN):", fontSize = 12.sp, color = TextSecondary)
                    OutlinedTextField(
                        value = msiAmountText,
                        onValueChange = { msiAmountText = it },
                        placeholder = { Text("0.00") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "Plazo de Meses Sin Intereses:", fontSize = 12.sp, color = TextSecondary)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(3, 6, 9, 12, 18, 24).forEach { months ->
                            FilterChip(
                                selected = msiMonths == months,
                                onClick = { msiMonths = months },
                                label = { Text("$months MSI") }
                            )
                        }
                    }

                    if (amountVal > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(NeonViolet.copy(alpha = 0.15f))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "💡 Pagarás ${Formatters.formatCurrency(monthlyCalc)} cada mes durante $msiMonths meses",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonViolet
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (msiTitle.isNotBlank() && amountVal > 0 && selectedMsiCardId.isNotBlank()) {
                            onAddInstallmentPurchase(
                                selectedMsiCardId,
                                msiTitle,
                                amountVal,
                                msiMonths,
                                msiNote
                            )
                            showAddMsiDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonViolet)
                ) {
                    Text("Registrar MSI", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMsiDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = FintechSurface
        )
    }
}
