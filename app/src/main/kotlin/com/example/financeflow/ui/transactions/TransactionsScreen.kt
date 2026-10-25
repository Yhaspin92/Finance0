package com.example.financeflow.ui.transactions

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financeflow.domain.model.Transaction
import com.example.financeflow.domain.model.TransactionType
import com.example.financeflow.ui.components.Formatters
import com.example.financeflow.ui.components.GlassCard
import com.example.financeflow.ui.theme.*

@Composable
fun TransactionsScreen(
    transactions: List<Transaction>,
    onDeleteTransaction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterType by remember { mutableStateOf<TransactionType?>(null) }
    var expandedTxId by remember { mutableStateOf<String?>(null) }

    val filteredList = transactions.filter { tx ->
        val matchesSearch = tx.title.contains(searchQuery, ignoreCase = true) ||
                (tx.bankName?.contains(searchQuery, ignoreCase = true) == true) ||
                tx.note.contains(searchQuery, ignoreCase = true)
        val matchesType = selectedFilterType == null || tx.type == selectedFilterType
        matchesSearch && matchesType
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(FintechBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Movimientos",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Historial completo de cargos, abonos y transferencias",
                fontSize = 13.sp,
                color = TextSecondary
            )
        }

        // Buscador
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar por comercio, banco o nota...", color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar", tint = TextSecondary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar", tint = TextSecondary)
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonEmerald,
                    unfocusedBorderColor = FintechCardBorder,
                    focusedContainerColor = FintechCard,
                    unfocusedContainerColor = FintechCard,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
        }

        // Filtros por Tipo de Transacción
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilterType == null,
                    onClick = { selectedFilterType = null },
                    label = { Text("Todos (${transactions.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonEmerald.copy(alpha = 0.2f),
                        selectedLabelColor = NeonEmerald,
                        containerColor = FintechCard,
                        labelColor = TextSecondary
                    )
                )

                val filterTypes = listOf(
                    TransactionType.EXPENSE to "Gastos",
                    TransactionType.INCOME to "Ingresos",
                    TransactionType.TRANSFER to "Transferencias",
                    TransactionType.CREDIT_CARD_PAYMENT to "Pagos TdC"
                )

                filterTypes.forEach { (type, label) ->
                    val count = transactions.count { it.type == type }
                    FilterChip(
                        selected = selectedFilterType == type,
                        onClick = { selectedFilterType = if (selectedFilterType == type) null else type },
                        label = { Text("$label ($count)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonEmerald.copy(alpha = 0.2f),
                            selectedLabelColor = NeonEmerald,
                            containerColor = FintechCard,
                            labelColor = TextSecondary
                        )
                    )
                }
            }
        }

        // Conteo y Suma de lista filtrada
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredList.size} movimientos encontrados",
                    fontSize = 12.sp,
                    color = TextMuted
                )
                val totalFiltered = filteredList.sumOf {
                    if (it.type == TransactionType.INCOME) it.amount else -it.amount
                }
                Text(
                    text = "Balance: ${Formatters.formatCurrency(totalFiltered)}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (totalFiltered >= 0) FintechIncome else FintechExpense
                )
            }
        }

        if (filteredList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No se encontraron movimientos con este criterio.",
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            items(filteredList, key = { it.id }) { tx ->
                val isIncome = tx.type == TransactionType.INCOME
                val isExpanded = expandedTxId == tx.id

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shapeRadius = 16.dp,
                    onClick = {
                        expandedTxId = if (isExpanded) null else tx.id
                    }
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
                                    .size(42.dp)
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
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = tx.title,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    maxLines = 1
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = Formatters.formatDate(tx.timestamp),
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                    if (tx.bankName != null) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "• ${tx.bankName}",
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${if (isIncome) "+" else "-"}${Formatters.formatCurrency(tx.amount)}",
                                fontWeight = FontWeight.Bold,
                                color = if (isIncome) FintechIncome else TextPrimary,
                                fontSize = 16.sp
                            )
                            if (tx.isAutoDetected) {
                                Text(
                                    text = "🤖 Auto",
                                    fontSize = 10.sp,
                                    color = NeonCyan,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Detalle expandible
                    AnimatedVisibility(visible = isExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        ) {
                            HorizontalDivider(color = FintechCardBorder)
                            Spacer(modifier = Modifier.height(10.dp))

                            if (tx.rawNotificationText != null) {
                                Text(
                                    text = "Texto original de la notificación bancaria:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = NeonCyan
                                )
                                Text(
                                    text = tx.rawNotificationText,
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(FintechSurface)
                                        .padding(8.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            if (tx.note.isNotBlank()) {
                                Text(
                                    text = "Nota: ${tx.note}",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = { onDeleteTransaction(tx.id) },
                                    colors = ButtonDefaults.textButtonColors(contentColor = FintechExpense)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Eliminar Movimiento", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
