package com.example.financeflow.ui.cards

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.financeflow.domain.model.Account
import com.example.financeflow.domain.model.AccountType
import com.example.financeflow.domain.model.CreditCard
import com.example.financeflow.domain.model.InstallmentPurchase
import com.example.financeflow.domain.model.Transaction
import com.example.financeflow.domain.model.TransactionCategory
import com.example.financeflow.domain.model.TransactionType
import com.example.financeflow.domain.model.displayName
import com.example.financeflow.ui.components.Formatters
import com.example.financeflow.ui.components.GlassCard
import com.example.financeflow.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CardsScreen(
    creditCards: List<CreditCard>,
    accounts: List<Account>,
    installmentPurchases: List<InstallmentPurchase>,
    transactions: List<Transaction> = emptyList(),
    onAddCard: (cardName: String, bank: String, last4: String, creditLimit: Double, debt: Double, cutOff: Int, dueDay: Int, linkedAccId: String?, isDebit: Boolean) -> Unit,
    onPayCardClick: (debitAccountId: String?, creditCardId: String, amount: Double) -> Unit,
    onAddInstallmentPurchase: (cardId: String, title: String, totalAmount: Double, totalMonths: Int, note: String) -> Unit,
    onAdvanceInstallmentMonth: (String) -> Unit,
    onDeleteInstallmentPurchase: (String) -> Unit,
    onAddCardTransaction: (cardId: String, title: String, amount: Double, category: TransactionCategory, note: String) -> Unit = { _, _, _, _, _ -> },
    onDeleteCard: (String) -> Unit = {},
    onDeleteTransaction: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedCardForDetailId by remember { mutableStateOf<String?>(null) }
    var payingCard by remember { mutableStateOf<CreditCard?>(null) }
    var showAddMsiDialog by remember { mutableStateOf(false) }
    var preselectedMsiCardId by remember { mutableStateOf<String?>(null) }
    var showAddCardDialog by remember { mutableStateOf(false) }
    var cardForNewCharge by remember { mutableStateOf<CreditCard?>(null) }

    val currentSelectedCard = creditCards.find { it.id == selectedCardForDetailId }

    // Si hay una tarjeta seleccionada para ver su detalle, mostrar la subpágina dedicada
    if (currentSelectedCard != null) {
        CreditCardDetailPage(
            card = currentSelectedCard,
            accounts = accounts,
            installmentPurchases = installmentPurchases.filter { it.cardId == currentSelectedCard.id },
            transactions = transactions.filter {
                it.cardId == currentSelectedCard.id ||
                (!currentSelectedCard.accountId.isNullOrBlank() && it.accountId == currentSelectedCard.accountId)
            },
            onBack = { selectedCardForDetailId = null },
            onPayCardClick = { debitAccId, amount ->
                onPayCardClick(debitAccId, currentSelectedCard.id, amount)
            },
            onOpenAddMsi = {
                preselectedMsiCardId = currentSelectedCard.id
                showAddMsiDialog = true
            },
            onAdvanceInstallmentMonth = onAdvanceInstallmentMonth,
            onDeleteInstallmentPurchase = onDeleteInstallmentPurchase,
            onOpenAddCharge = {
                cardForNewCharge = currentSelectedCard
            },
            onDeleteCard = {
                onDeleteCard(currentSelectedCard.id)
                selectedCardForDetailId = null
            },
            onDeleteTransaction = onDeleteTransaction,
            modifier = modifier
        )
    } else {
        // Vista principal del Módulo de Tarjetas
        val todayDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        val maxDayInMonth = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)

        val totalDebt = creditCards.sumOf { it.currentDebt }
        val totalLimit = creditCards.sumOf { it.creditLimit }
        val totalAvailable = (totalLimit - totalDebt).coerceAtLeast(0.0)
        val overallRatio = if (totalLimit > 0) (totalDebt / totalLimit).toFloat() else 0f

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(FintechBackground)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header del módulo
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tarjetas de Crédito y Débito",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        IconButton(
                            onClick = { showAddCardDialog = true },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(AccentNeon.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Añadir Tarjeta",
                                tint = AccentNeon,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Gestión de líneas, cortes, pagos, compras a MSI y movimientos",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }

            // 1. DEUDA TOTAL EN TARJETAS (Destacado superior)
            item {
                GlassCard(
                    accentGlow = NeonViolet,
                    borderColor = NeonViolet.copy(alpha = 0.4f),
                    shapeRadius = 22.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Deuda Total en Tarjetas", fontSize = 13.sp, color = TextSecondary)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonViolet.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${creditCards.size} TARJETAS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonViolet
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = Formatters.formatCurrency(totalDebt),
                        fontSize = 32.sp,
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

            // Título de la lista de tarjetas
            item {
                Text(
                    text = "Tus Tarjetas (Toca una tarjeta para ver detalle, movimientos y MSI)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            }

            // 2. LISTADO DE TARJETAS (Interactivas sin el botón Registrar Pago en el item)
            if (creditCards.isEmpty()) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "No tienes tarjetas de crédito agregadas. Toca el botón '+' superior para añadir una.",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }
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

                    val cardColor = try {
                        Color(android.graphics.Color.parseColor(card.colorHex))
                    } catch (e: Exception) {
                        NeonViolet
                    }

                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedCardForDetailId = card.id },
                        accentGlow = cardColor,
                        borderColor = cardColor.copy(alpha = 0.35f),
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
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(cardColor.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.CreditCard,
                                        contentDescription = "Tarjeta",
                                        tint = cardColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = card.cardName,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${card.bank} •••• ${if (card.last4Digits.isNotBlank()) card.last4Digits else "Card"}",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = Formatters.formatCurrency(card.currentDebt),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (card.currentDebt > 0) FintechExpense else NeonEmerald
                                )
                                Text(
                                    text = "Deuda actual",
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

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
                                    Spacer(modifier = Modifier.height(2.dp))
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
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (daysToPayment == 0) "¡VENCE HOY!" else "Faltan $daysToPayment días",
                                        fontSize = 11.sp,
                                        color = if (daysToPayment <= 3) FintechAlert else NeonEmerald,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Línea: ${Formatters.formatCurrency(card.creditLimit)}",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = "Disponible: ${Formatters.formatCurrency(available)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
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

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Toca para ver movimientos, MSI y pagar",
                                fontSize = 11.sp,
                                color = NeonCyan,
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = "Ver",
                                tint = NeonCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // SECCIÓN GLOBAL MESES SIN INTERESES (MSI)
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🛍️ Compras a Meses Sin Intereses (MSI)",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                preselectedMsiCardId = null
                                showAddMsiDialog = true
                            },
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(AccentSecondary.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Añadir MSI",
                                tint = AccentSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Monitorea tus mensualidades activas en todas tus tarjetas",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
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

                        Spacer(modifier = Modifier.height(10.dp))

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

                        Spacer(modifier = Modifier.height(8.dp))

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
    }

    // Modal para Registrar Nueva Compra a Meses Sin Intereses (MSI)
    if (showAddMsiDialog) {
        AddMsiDialog(
            creditCards = creditCards,
            preselectedCardId = preselectedMsiCardId,
            onDismiss = { showAddMsiDialog = false },
            onConfirm = { cardId, title, total, months, note ->
                onAddInstallmentPurchase(cardId, title, total, months, note)
                showAddMsiDialog = false
            }
        )
    }

    // Modal para Añadir Tarjeta (Crédito o Débito con vinculación OPCIONAL)
    if (showAddCardDialog) {
        AddCreditCardDialog(
            accounts = accounts,
            onDismiss = { showAddCardDialog = false },
            onSave = { name, bank, last4, limit, debt, cutOff, dueDay, linkedAccId, isDebit ->
                onAddCard(name, bank, last4, limit, debt, cutOff, dueDay, linkedAccId, isDebit)
                showAddCardDialog = false
            }
        )
    }

    // Modal para Añadir Cargo / Movimiento a la tarjeta
    if (cardForNewCharge != null) {
        AddCardTransactionDialog(
            card = cardForNewCharge!!,
            onDismiss = { cardForNewCharge = null },
            onConfirm = { title, amount, category, note ->
                onAddCardTransaction(cardForNewCharge!!.id, title, amount, category, note)
                cardForNewCharge = null
            }
        )
    }
}

/**
 * Subpágina dedicada para la Tarjeta de Crédito interactiva.
 * Muestra información completa, botones de acción (Registrar Pago, Compras a MSI, + Movimiento),
 * historial de compras a MSI de la tarjeta y su historial completo de transacciones.
 */
@Composable
fun CreditCardDetailPage(
    card: CreditCard,
    accounts: List<Account>,
    installmentPurchases: List<InstallmentPurchase>,
    transactions: List<Transaction>,
    onBack: () -> Unit,
    onPayCardClick: (debitAccountId: String?, amount: Double) -> Unit,
    onOpenAddMsi: () -> Unit,
    onAdvanceInstallmentMonth: (String) -> Unit,
    onDeleteInstallmentPurchase: (String) -> Unit,
    onOpenAddCharge: () -> Unit,
    onDeleteCard: () -> Unit,
    onDeleteTransaction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    var showPayModal by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val todayDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    val maxDayInMonth = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH)

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

    val cardColor = try {
        Color(android.graphics.Color.parseColor(card.colorHex))
    } catch (e: Exception) {
        NeonViolet
    }

    val linkedAccount = accounts.find { it.id == card.accountId }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(FintechBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Barra superior con botón volver, título y eliminar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(FintechSurface)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = TextPrimary)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = card.cardName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${card.bank} •••• ${if (card.last4Digits.isNotBlank()) card.last4Digits else "Card"}",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                IconButton(
                    onClick = { showDeleteConfirmDialog = true },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(FintechExpense.copy(alpha = 0.15f))
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar Tarjeta", tint = FintechExpense, modifier = Modifier.size(18.dp))
                }
            }
        }

        // Hero Card con Toda la Información de la Tarjeta
        item {
            GlassCard(
                accentGlow = cardColor,
                borderColor = cardColor.copy(alpha = 0.4f),
                shapeRadius = 24.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Deuda Actual de la Tarjeta", fontSize = 13.sp, color = TextSecondary)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(cardColor.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (card.isDebit) "DÉBITO" else "CRÉDITO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = cardColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = Formatters.formatCurrency(card.currentDebt),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (card.currentDebt > 0) FintechExpense else NeonEmerald
                )

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
                        fontWeight = FontWeight.Bold,
                        color = NeonEmerald
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { ratio.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = when {
                        ratio < 0.3f -> NeonEmerald
                        ratio < 0.6f -> FintechWarning
                        else -> FintechExpense
                    },
                    trackColor = FintechSurface
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Fechas de Corte y de Pago
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
                            Text(text = "Día de Corte", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                text = "Día ${card.cutOffDay}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (daysToCutoff == 0) "¡Corta Hoy!" else "En $daysToCutoff días",
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
                                text = "Día ${card.paymentDueDay}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (daysToPayment == 0) "¡Vence Hoy!" else "En $daysToPayment días",
                                fontSize = 11.sp,
                                color = if (daysToPayment <= 3) FintechAlert else NeonEmerald,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Cuenta de débito vinculada o aviso opcional
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (linkedAccount != null) Icons.Default.Link else Icons.Default.LinkOff,
                        contentDescription = "Vinculación",
                        tint = if (linkedAccount != null) NeonCyan else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (linkedAccount != null) "Vinculada a: ${linkedAccount.name} (${linkedAccount.bankName})"
                               else "Sin cuenta vinculada (Opcional)",
                        fontSize = 11.sp,
                        color = if (linkedAccount != null) TextPrimary else TextMuted
                    )
                }
            }
        }

        // BOTONES DE ACCIÓN PRINCIPALES
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Botón REGISTRAR PAGO
                    Button(
                        onClick = { showPayModal = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald)
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = "Pagar", tint = FintechBackground, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Registrar Pago",
                            fontWeight = FontWeight.Bold,
                            color = FintechBackground,
                            fontSize = 13.sp
                        )
                    }

                    // Botón COMPRAS A MSI
                    Button(
                        onClick = onOpenAddMsi,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                    ) {
                        Icon(Icons.Default.ShoppingBag, contentDescription = "MSI", tint = FintechBackground, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Compras a MSI",
                            fontWeight = FontWeight.Bold,
                            color = FintechBackground,
                            fontSize = 13.sp
                        )
                    }
                }

                // Botón + AÑADIR MOVIMIENTO / CARGO
                Button(
                    onClick = onOpenAddCharge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonViolet)
                ) {
                    Icon(Icons.Default.AddCard, contentDescription = "Añadir Cargo", tint = TextPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "+ Añadir Movimiento / Cargo a Tarjeta",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // SECCIÓN: COMPRAS A MESES SIN INTERESES EN ESTA TARJETA
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🛍️ Compras a MSI en esta Tarjeta",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${installmentPurchases.size} ACTIVAS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
            }
        }

        if (installmentPurchases.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "No hay compras a MSI en esta tarjeta. Puedes registrar una con el botón 'Compras a MSI'.",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }
        } else {
            items(installmentPurchases) { msi ->
                val progress = (msi.currentMonthPaid.toFloat() / msi.totalMonths.toFloat()).coerceIn(0f, 1f)
                val remainingMonths = (msi.totalMonths - msi.currentMonthPaid).coerceAtLeast(0)
                val remainingAmount = remainingMonths * msi.monthlyAmount

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    accentGlow = NeonCyan,
                    shapeRadius = 14.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(msi.title, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                            Text("${msi.totalMonths} Meses Sin Intereses", fontSize = 11.sp, color = NeonCyan)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${Formatters.formatCurrency(msi.monthlyAmount)} / mes",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Total: ${Formatters.formatCurrency(msi.totalAmount)}",
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Mes ${msi.currentMonthPaid} de ${msi.totalMonths} pagados",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = if (remainingMonths == 0) "¡Liquidado!" else "Faltan $remainingMonths meses (${Formatters.formatCurrency(remainingAmount)})",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (remainingMonths == 0) NeonEmerald else FintechWarning
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (progress >= 1f) NeonEmerald else NeonCyan,
                        trackColor = FintechSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (msi.currentMonthPaid < msi.totalMonths) {
                            TextButton(onClick = { onAdvanceInstallmentMonth(msi.id) }) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Avanzar Mes (+1)", fontSize = 11.sp, color = NeonEmerald)
                            }
                        }
                        TextButton(onClick = { onDeleteInstallmentPurchase(msi.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = FintechExpense, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Eliminar", fontSize = 11.sp, color = FintechExpense)
                        }
                    }
                }
            }
        }

        // SECCIÓN: HISTORIAL DE TRANSACCIONES / MOVIMIENTOS DE LA TARJETA
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📋 Historial de Transacciones",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${transactions.size} MOVIMIENTOS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentNeon
                )
            }
        }

        if (transactions.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "No hay movimientos registrados en esta tarjeta. Toca '+ Añadir Movimiento' para registrar un cargo.",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }
        } else {
            items(transactions) { tx ->
                val isExpense = tx.type == TransactionType.EXPENSE
                val isPayment = tx.type == TransactionType.CREDIT_CARD_PAYMENT
                val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(tx.timestamp))

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shapeRadius = 14.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isPayment) NeonEmerald.copy(alpha = 0.2f) else FintechExpense.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (isPayment) Icons.Default.ArrowDownward else Icons.Default.ShoppingCart,
                                    contentDescription = null,
                                    tint = if (isPayment) NeonEmerald else FintechExpense,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = tx.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "$dateStr • ${tx.category.displayName}",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                                if (tx.note.isNotBlank()) {
                                    Text(
                                        text = tx.note,
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${if (isPayment) "-" else "+"}${Formatters.formatCurrency(tx.amount)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPayment) NeonEmerald else FintechExpense
                            )
                            IconButton(onClick = { onDeleteTransaction(tx.id) }, modifier = Modifier.size(30.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = TextMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal para Registrar Pago
    if (showPayModal) {
        var payAmountText by remember { mutableStateOf(card.currentDebt.toString()) }
        var selectedDebitAccountId by remember {
            mutableStateOf(card.accountId ?: accounts.firstOrNull { it.type != AccountType.CREDIT }?.id)
        }

        AlertDialog(
            onDismissRequest = { showPayModal = false },
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

                    Text(text = "Pagar desde cuenta de débito (opcional):", fontSize = 12.sp, color = TextSecondary)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedDebitAccountId == null,
                            onClick = { selectedDebitAccountId = null },
                            label = { Text("Sin deducir cuenta") }
                        )
                        accounts.filter { it.type != AccountType.CREDIT }.forEach { acc ->
                            val isSelected = acc.id == selectedDebitAccountId
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedDebitAccountId = acc.id },
                                label = { Text("${acc.bankName} (${Formatters.formatCurrency(acc.balance)})") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Monto a abonar o liquidar ($ MXN):", fontSize = 12.sp, color = TextSecondary)
                    OutlinedTextField(
                        value = payAmountText,
                        onValueChange = { payAmountText = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = payAmountText.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            onPayCardClick(selectedDebitAccountId, amount)
                            showPayModal = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald)
                ) {
                    Text("Confirmar Pago", color = FintechBackground, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPayModal = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = FintechSurface
        )
    }

    // Diálogo de Confirmación para Borrar Tarjeta
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("¿Eliminar tarjeta?", fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = {
                Text(
                    "Se eliminará la tarjeta '${card.cardName}' y todas sus compras a MSI asociadas. Las cuentas bancarias no se verán afectadas.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteCard()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FintechExpense)
                ) {
                    Text("Eliminar", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = FintechSurface
        )
    }
}

/**
 * Diálogo para Añadir Tarjeta de Crédito o Débito con vinculación Opcional
 */
@Composable
fun AddCreditCardDialog(
    accounts: List<Account>,
    onDismiss: () -> Unit,
    onSave: (name: String, bank: String, last4: String, limit: Double, debt: Double, cutOff: Int, dueDay: Int, linkedAccId: String?, isDebit: Boolean) -> Unit
) {
    var cardName by remember { mutableStateOf("") }
    var isDebit by remember { mutableStateOf(false) }
    var selectedBank by remember { mutableStateOf("Revolut México") }
    var customBankText by remember { mutableStateOf("") }
    var last4Digits by remember { mutableStateOf("") }
    var creditLimitText by remember { mutableStateOf("") }
    var currentDebtText by remember { mutableStateOf("0") }
    var cutOffDayText by remember { mutableStateOf("10") }
    var paymentDueDayText by remember { mutableStateOf("30") }
    var selectedAccountId by remember { mutableStateOf<String?>(null) } // Opcional por defecto

    val debitAccounts = accounts.filter { it.type != AccountType.CREDIT }

    val bankOptions = listOf(
        "Revolut México", "BBVA México", "Nu México", "Mercado Pago",
        "Santander", "Banorte", "Citibanamex", "Hey Banco",
        "Stori", "Klar", "Ualá", "Plata Card", "UNO", "Clip",
        "Novacard", "Lenda", "ARQ", "Banco Azteca", "HSBC México",
        "Scotiabank", "Spin by OXXO", "Inbursa", "BanCoppel", "RappiCard",
        "DolarApp", "Otro"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Añadir Tarjeta", fontWeight = FontWeight.Bold, color = TextPrimary)
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    // Selector: ¿Crédito o Débito?
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilterChip(
                            selected = !isDebit,
                            onClick = { isDebit = false },
                            label = { Text("Tarjeta de Crédito") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = isDebit,
                            onClick = { isDebit = true },
                            label = { Text("Tarjeta de Débito") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = cardName,
                        onValueChange = { cardName = it },
                        label = { Text("Nombre de la tarjeta (ej. Nu Platinum, BBVA Azul)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text("Banco Emisor:", fontSize = 12.sp, color = TextSecondary)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        bankOptions.forEach { b ->
                            FilterChip(
                                selected = selectedBank == b,
                                onClick = { selectedBank = b },
                                label = { Text(b, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                if (selectedBank == "Otro") {
                    item {
                        OutlinedTextField(
                            value = customBankText,
                            onValueChange = { customBankText = it },
                            label = { Text("Especifica el nombre del banco emisor") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = last4Digits,
                        onValueChange = { if (it.length <= 4) last4Digits = it },
                        label = { Text("Últimos 4 dígitos (ej. 9042)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text("¿Vinculada a alguna cuenta de débito? (Opcional):", fontSize = 12.sp, color = TextSecondary)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        // Opción 1: Sin cuenta vinculada (Opcional)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedAccountId == null) AccentNeon.copy(alpha = 0.15f) else FintechSurface)
                                .clickable { selectedAccountId = null }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedAccountId == null,
                                onClick = { selectedAccountId = null }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("Sin cuenta vinculada (Opcional)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("No vincular a cuenta de débito", fontSize = 10.sp, color = TextMuted)
                            }
                        }

                        // Lista de cuentas existentes
                        debitAccounts.forEach { acc ->
                            val isSelected = selectedAccountId == acc.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) AccentNeon.copy(alpha = 0.15f) else FintechSurface)
                                    .clickable { selectedAccountId = acc.id }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedAccountId = acc.id }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("${acc.name} (${acc.bankName})", fontSize = 12.sp, color = TextPrimary)
                            }
                        }
                    }
                }

                if (!isDebit) {
                    item {
                        OutlinedTextField(
                            value = creditLimitText,
                            onValueChange = { creditLimitText = it },
                            label = { Text("Límite de Crédito ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = currentDebtText,
                            onValueChange = { currentDebtText = it },
                            label = { Text("Deuda actual en la tarjeta ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = cutOffDayText,
                                onValueChange = { cutOffDayText = it },
                                label = { Text("Día de corte") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = paymentDueDayText,
                                onValueChange = { paymentDueDayText = it },
                                label = { Text("Día límite pago") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val limit = creditLimitText.toDoubleOrNull() ?: 0.0
                    val debt = currentDebtText.toDoubleOrNull() ?: 0.0
                    val cutOff = cutOffDayText.toIntOrNull() ?: 10
                    val dueDay = paymentDueDayText.toIntOrNull() ?: 30
                    if (cardName.isNotBlank()) {
                        val finalBank = if (selectedBank == "Otro" && customBankText.isNotBlank()) customBankText.trim() else selectedBank
                        onSave(
                            cardName.trim(),
                            finalBank,
                            last4Digits.trim(),
                            limit,
                            debt,
                            cutOff,
                            dueDay,
                            selectedAccountId,
                            isDebit
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentNeon)
            ) {
                Text("Guardar Tarjeta", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextSecondary)
            }
        },
        containerColor = FintechSurface
    )
}

/**
 * Diálogo para registrar una compra a Meses Sin Intereses (MSI)
 */
@Composable
fun AddMsiDialog(
    creditCards: List<CreditCard>,
    preselectedCardId: String?,
    onDismiss: () -> Unit,
    onConfirm: (cardId: String, title: String, totalAmount: Double, months: Int, note: String) -> Unit
) {
    var msiTitle by remember { mutableStateOf("") }
    var msiAmountText by remember { mutableStateOf("") }
    var msiMonths by remember { mutableStateOf(12) }
    var selectedMsiCardId by remember {
        mutableStateOf(preselectedCardId ?: creditCards.firstOrNull()?.id ?: "")
    }
    var msiNote by remember { mutableStateOf("") }

    val amountVal = msiAmountText.toDoubleOrNull() ?: 0.0
    val monthlyCalc = if (msiMonths > 0 && amountVal > 0) amountVal / msiMonths else 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Nueva Compra a Meses Sin Intereses", fontWeight = FontWeight.Bold, color = TextPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (preselectedCardId == null) {
                    Text(text = "Tarjeta de Crédito:", fontSize = 12.sp, color = TextSecondary)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        creditCards.forEach { card ->
                            FilterChip(
                                selected = card.id == selectedMsiCardId,
                                onClick = { selectedMsiCardId = card.id },
                                label = { Text(card.cardName) }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = msiTitle,
                    onValueChange = { msiTitle = it },
                    label = { Text("Concepto (ej. iPhone 16 Pro, Vuelos)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = msiAmountText,
                    onValueChange = { msiAmountText = it },
                    label = { Text("Monto total de la compra ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(text = "Plazo de Mensualidades (MSI):", fontSize = 12.sp, color = TextSecondary)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(3, 6, 9, 12, 18, 24).forEach { m ->
                        FilterChip(
                            selected = msiMonths == m,
                            onClick = { msiMonths = m },
                            label = { Text("$m MSI") }
                        )
                    }
                }

                if (monthlyCalc > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(NeonCyan.copy(alpha = 0.15f))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "Pago mensual: ${Formatters.formatCurrency(monthlyCalc)} / mes",
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                OutlinedTextField(
                    value = msiNote,
                    onValueChange = { msiNote = it },
                    label = { Text("Nota (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (msiTitle.isNotBlank() && amountVal > 0 && selectedMsiCardId.isNotBlank()) {
                        onConfirm(selectedMsiCardId, msiTitle.trim(), amountVal, msiMonths, msiNote.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonViolet)
            ) {
                Text("Registrar MSI", color = TextPrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextSecondary)
            }
        },
        containerColor = FintechSurface
    )
}

/**
 * Diálogo para Añadir Movimiento / Cargo a una Tarjeta de Crédito
 */
@Composable
fun AddCardTransactionDialog(
    card: CreditCard,
    onDismiss: () -> Unit,
    onConfirm: (title: String, amount: Double, category: TransactionCategory, note: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(TransactionCategory.ALIMENTOS_SUPER) }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Añadir Cargo a ${card.cardName}", fontWeight = FontWeight.Bold, color = TextPrimary)
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Concepto de la compra (ej. Súper, Gasolina)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Monto del cargo ($ MXN)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text("Categoría:", fontSize = 12.sp, color = TextSecondary)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TransactionCategory.values().forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat.displayName, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Nota adicional (opcional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && amount > 0) {
                        onConfirm(title.trim(), amount, selectedCategory, note.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonViolet)
            ) {
                Text("Registrar Cargo", color = TextPrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextSecondary)
            }
        },
        containerColor = FintechSurface
    )
}
