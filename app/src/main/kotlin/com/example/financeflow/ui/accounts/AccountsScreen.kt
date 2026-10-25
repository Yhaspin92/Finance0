package com.example.financeflow.ui.accounts

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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.financeflow.domain.model.*
import com.example.financeflow.ui.components.Formatters
import com.example.financeflow.ui.components.GlassCard
import com.example.financeflow.ui.theme.*

@Composable
fun AccountsScreen(
    accounts: List<Account>,
    creditCards: List<CreditCard>,
    transactions: List<Transaction>,
    onAddAccount: (name: String, bankName: String, type: AccountType, balance: Double, last4: String, colorHex: String, investment: Double, yield: Double) -> Unit,
    onAddPocket: (accountId: String, name: String, amount: Double, target: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddAccountDialog by remember { mutableStateOf(false) }
    var selectedAccountForDetail by remember { mutableStateOf<Account?>(null) }

    val totalAssets = accounts.filter { it.type != AccountType.CREDIT }.sumOf { it.balance + it.investmentAmount }
    val totalInPockets = accounts.sumOf { acc -> acc.pockets.sumOf { it.amount } }
    val totalInvested = accounts.sumOf { it.investmentAmount }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(FintechBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Encabezado
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cuentas Bancarias",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    IconButton(
                        onClick = { showAddAccountDialog = true },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(AccentNeon.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Añadir Cuenta",
                            tint = AccentNeon,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Activos totales, ahorros, apartados e inversiones",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        }

        // Tarjeta Resumen de Activos Totales
        item {
            GlassCard(
                accentGlow = AccentNeon,
                borderColor = AccentNeon.copy(alpha = 0.35f),
                shapeRadius = 20.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Total de Activos Ahorrados", fontSize = 13.sp, color = TextSecondary)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AccentNeon.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${accounts.size} CUENTAS ACTIVAS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentNeon
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = Formatters.formatCurrency(totalAssets),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AccentNeon
                )

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
                            Text("En Apartados", fontSize = 10.sp, color = TextSecondary)
                            Text(
                                text = Formatters.formatCurrency(totalInPockets),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
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
                            Text("En Inversiones", fontSize = 10.sp, color = TextSecondary)
                            Text(
                                text = Formatters.formatCurrency(totalInvested),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentSecondary
                            )
                        }
                    }
                }
            }
        }

        // Título de la lista
        item {
            Text(
                text = "Tus Cuentas (Toca una cuenta para ver apartados y tarjetas)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )
        }

        // Lista de Cuentas Bancarias
        if (accounts.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("No hay cuentas bancarias registradas.", color = TextMuted, fontSize = 13.sp)
                }
            }
        } else {
            items(accounts) { acc ->
                val linkedCards = creditCards.filter { it.accountId == acc.id }
                val bankColor = try {
                    Color(android.graphics.Color.parseColor(acc.colorHex))
                } catch (e: Exception) {
                    AccentNeon
                }

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedAccountForDetail = acc },
                    accentGlow = bankColor,
                    borderColor = bankColor.copy(alpha = 0.4f),
                    shapeRadius = 18.dp
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
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(bankColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (acc.type) {
                                        AccountType.DEBIT -> Icons.Default.AccountBalance
                                        AccountType.CASH -> Icons.Default.Payments
                                        AccountType.INVESTMENT -> Icons.Default.TrendingUp
                                        AccountType.CREDIT -> Icons.Default.CreditCard
                                    },
                                    contentDescription = null,
                                    tint = bankColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = acc.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = acc.bankName,
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                    if (acc.last4Digits.isNotBlank()) {
                                        Text(
                                            text = " •••• ${acc.last4Digits}",
                                            fontSize = 12.sp,
                                            color = TextMuted
                                        )
                                    }
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = Formatters.formatCurrency(acc.balance),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (acc.type == AccountType.CREDIT) FintechExpense else AccentNeon
                            )
                            Text(
                                text = "Total activos",
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
                    }

                    // Resumen de Sub-elementos (Apartados, Tarjetas e Inversiones)
                    val chips = mutableListOf<String>()
                    if (acc.pockets.isNotEmpty()) chips.add("📂 ${acc.pockets.size} apartados (${Formatters.formatCurrency(acc.pockets.sumOf { it.amount })})")
                    if (linkedCards.isNotEmpty()) chips.add("💳 ${linkedCards.size} tarjeta(s) vinculada(s)")
                    if (acc.investmentAmount > 0) chips.add("📈 Inversión ${Formatters.formatCurrency(acc.investmentAmount)}")

                    if (chips.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            chips.forEach { chipText ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(FintechSurface)
                                        .border(1.dp, FintechBorder, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(chipText, fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal de Detalle de Cuenta Bancaria
    if (selectedAccountForDetail != null) {
        val acc = accounts.find { it.id == selectedAccountForDetail!!.id } ?: selectedAccountForDetail!!
        AccountDetailModal(
            account = acc,
            linkedCards = creditCards.filter { it.accountId == acc.id },
            transactions = transactions.filter { it.accountId == acc.id },
            onDismiss = { selectedAccountForDetail = null },
            onAddPocket = { name, amount, target ->
                onAddPocket(acc.id, name, amount, target)
            }
        )
    }

    // Modal para Añadir Cuenta Bancaria
    if (showAddAccountDialog) {
        AddAccountModal(
            onDismiss = { showAddAccountDialog = false },
            onSave = { name, bank, type, balance, last4, colorHex, investment, yield ->
                onAddAccount(name, bank, type, balance, last4, colorHex, investment, yield)
                showAddAccountDialog = false
            }
        )
    }
}

@Composable
fun AccountDetailModal(
    account: Account,
    linkedCards: List<CreditCard>,
    transactions: List<Transaction>,
    onDismiss: () -> Unit,
    onAddPocket: (name: String, amount: Double, target: Double) -> Unit
) {
    var showCreatePocketDialog by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .fillMaxHeight(0.85f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(FintechSurface)
                    .border(1.dp, FintechBorder, RoundedCornerShape(24.dp))
                    .clickable(enabled = false) {}
                    .padding(20.dp)
            ) {
                // Barra superior
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(account.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("${account.bankName} • Saldo: ${Formatters.formatCurrency(account.balance)}", fontSize = 13.sp, color = AccentNeon, fontWeight = FontWeight.SemiBold)
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Conceptos y Apartados
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("📂 Conceptos y Apartados de Ahorro", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            TextButton(onClick = { showCreatePocketDialog = true }) {
                                Text("+ Apartado", color = AccentNeon, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (account.pockets.isEmpty()) {
                        item {
                            GlassCard(modifier = Modifier.fillMaxWidth(), shapeRadius = 12.dp) {
                                Text("No hay apartados creados en esta cuenta. Crea apartados como 'Renta', 'Vacaciones' o 'Fondo de Emergencia'.", fontSize = 11.sp, color = TextMuted)
                            }
                        }
                    } else {
                        items(account.pockets) { pocket ->
                            GlassCard(modifier = Modifier.fillMaxWidth(), shapeRadius = 12.dp) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(pocket.name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                                        if (pocket.targetAmount > 0) {
                                            Text("Meta: ${Formatters.formatCurrency(pocket.targetAmount)}", fontSize = 10.sp, color = TextSecondary)
                                        }
                                    }
                                    Text(Formatters.formatCurrency(pocket.amount), fontWeight = FontWeight.Bold, color = NeonCyan, fontSize = 14.sp)
                                }
                            }
                        }
                    }

                    // 2. Tarjetas Vinculadas a esta cuenta
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("💳 Tarjetas Vinculadas a esta Cuenta", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }

                    if (linkedCards.isEmpty()) {
                        item {
                            GlassCard(modifier = Modifier.fillMaxWidth(), shapeRadius = 12.dp) {
                                Text("No hay tarjetas asociadas a esta cuenta bancaria.", fontSize = 11.sp, color = TextMuted)
                            }
                        }
                    } else {
                        items(linkedCards) { card ->
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                accentGlow = NeonViolet,
                                shapeRadius = 12.dp
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CreditCard, contentDescription = null, tint = NeonViolet, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(card.cardName, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                                            Text("Cuenta # •••• ${card.last4Digits} • ${if (card.isDebit) "Débito" else "Crédito"}", fontSize = 11.sp, color = TextSecondary)
                                        }
                                    }
                                    Text(
                                        text = if (card.isDebit) "Vinculada" else "Deuda: ${Formatters.formatCurrency(card.currentDebt)}",
                                        fontWeight = FontWeight.Bold,
                                        color = if (card.isDebit) NeonEmerald else FintechExpense,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    // 3. Inversiones y Rendimientos
                    if (account.investmentAmount > 0 || account.annualYieldRate > 0) {
                        item {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("📈 Inversiones y Rendimientos", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }

                        item {
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                accentGlow = AccentSecondary,
                                shapeRadius = 12.dp
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Fondo / Cajita de Inversión", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                                        Text("Tasa anual estimada: ${account.annualYieldRate}%", fontSize = 11.sp, color = AccentSecondary, fontWeight = FontWeight.SemiBold)
                                    }
                                    Text(Formatters.formatCurrency(account.investmentAmount), fontWeight = FontWeight.Bold, color = AccentSecondary, fontSize = 15.sp)
                                }
                            }
                        }
                    }

                    // 4. Últimos Movimientos
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("📜 Movimientos Recientes", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }

                    val recent = transactions.take(4)
                    if (recent.isEmpty()) {
                        item {
                            GlassCard(modifier = Modifier.fillMaxWidth(), shapeRadius = 12.dp) {
                                Text("Sin movimientos registrados en esta cuenta.", fontSize = 11.sp, color = TextMuted)
                            }
                        }
                    } else {
                        items(recent) { tx ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(tx.title, color = TextPrimary, fontSize = 12.sp)
                                Text(
                                    text = Formatters.formatCurrency(tx.amount),
                                    color = if (tx.type == TransactionType.INCOME) FintechIncome else TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Crear Apartado
    if (showCreatePocketDialog) {
        var pocketName by remember { mutableStateOf("") }
        var pocketAmountText by remember { mutableStateOf("") }
        var pocketTargetText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreatePocketDialog = false },
            title = { Text("Nuevo Apartado", fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = pocketName,
                        onValueChange = { pocketName = it },
                        label = { Text("Nombre del apartado (ej. Fondo de emergencia)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = pocketAmountText,
                        onValueChange = { pocketAmountText = it },
                        label = { Text("Monto asignado ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = pocketTargetText,
                        onValueChange = { pocketTargetText = it },
                        label = { Text("Meta opcional ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = pocketAmountText.toDoubleOrNull() ?: 0.0
                        val target = pocketTargetText.toDoubleOrNull() ?: 0.0
                        if (pocketName.isNotBlank() && amount > 0) {
                            onAddPocket(pocketName.trim(), amount, target)
                            showCreatePocketDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentNeon)
                ) {
                    Text("Crear Apartado", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePocketDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun AddAccountModal(
    onDismiss: () -> Unit,
    onSave: (name: String, bankName: String, type: AccountType, balance: Double, last4: String, colorHex: String, investment: Double, yield: Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedBank by remember { mutableStateOf("Revolut México") }
    var customBankText by remember { mutableStateOf("") }
    var accountType by remember { mutableStateOf(AccountType.DEBIT) }
    var balanceText by remember { mutableStateOf("") }
    var last4Digits by remember { mutableStateOf("") }
    var investmentText by remember { mutableStateOf("") }
    var yieldText by remember { mutableStateOf("") }

    val bankOptions = listOf(
        "Revolut México", "BBVA México", "Nu México", "Mercado Pago",
        "Santander", "Banorte", "Citibanamex", "Hey Banco",
        "Stori", "Klar", "Ualá", "Plata Card", "UNO", "Clip",
        "Novacard", "Lenda", "ARQ", "Banco Azteca", "HSBC México",
        "Scotiabank", "Spin by OXXO", "Inbursa", "BanCoppel", "RappiCard",
        "DolarApp", "Efectivo", "Otro"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir Cuenta Bancaria", fontWeight = FontWeight.Bold, color = TextPrimary) },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre de la cuenta (ej. Ahorros Revolut)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text("Banco o Institución:", fontSize = 12.sp, color = TextSecondary)
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
                            label = { Text("Especifica el nombre del banco o fintech") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = balanceText,
                        onValueChange = { balanceText = it },
                        label = { Text("Saldo actual ahorrado ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = last4Digits,
                        onValueChange = { if (it.length <= 4) last4Digits = it },
                        label = { Text("Últimos 4 dígitos de tarjeta/cuenta (opcional)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = investmentText,
                        onValueChange = { investmentText = it },
                        label = { Text("Monto en inversión / Cajitas ($ opcional)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = yieldText,
                        onValueChange = { yieldText = it },
                        label = { Text("Tasa de rendimiento % anual (ej. 13.5)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val bal = balanceText.toDoubleOrNull() ?: 0.0
                    val inv = investmentText.toDoubleOrNull() ?: 0.0
                    val y = yieldText.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank()) {
                        val finalBank = if (selectedBank == "Otro" && customBankText.isNotBlank()) customBankText.trim() else selectedBank
                        val color = when (finalBank) {
                            "Revolut México" -> "#0075EB"
                            "BBVA México" -> "#004481"
                            "Nu México" -> "#820AD1"
                            "Mercado Pago" -> "#009EE3"
                            "Santander" -> "#EC0000"
                            "Banorte" -> "#EB0029"
                            "Citibanamex" -> "#002D72"
                            "Stori" -> "#00B140"
                            "UNO" -> "#FF5000"
                            "Klar" -> "#00E676"
                            "Ualá" -> "#FF3366"
                            "Plata Card" -> "#212121"
                            "Clip" -> "#FF5900"
                            "Efectivo" -> "#00E676"
                            else -> "#7C4DFF"
                        }
                        onSave(name.trim(), finalBank, accountType, bal, last4Digits.trim(), color, inv, y)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentNeon)
            ) {
                Text("Guardar Cuenta", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextSecondary)
            }
        }
    )
}
