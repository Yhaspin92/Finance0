package com.example.financeflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.example.financeflow.domain.model.*
import com.example.financeflow.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickActionDialog(
    accounts: List<Account>,
    creditCards: List<CreditCard>,
    onDismiss: () -> Unit,
    onSaveTransaction: (
        accountId: String,
        targetAccountId: String?,
        title: String,
        amount: Double,
        type: TransactionType,
        category: TransactionCategory,
        note: String
    ) -> Unit
) {
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var amountText by remember { mutableStateOf("") }
    var titleText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: "") }
    var selectedTargetAccountId by remember { mutableStateOf(accounts.getOrNull(1)?.id ?: "") }
    var selectedCategory by remember { mutableStateOf(TransactionCategory.ALIMENTOS_SUPER) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            color = FintechSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, FintechCardBorder)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nuevo Movimiento",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Selector de Tipo de Movimiento (Tabs)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(FintechCard)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val types = listOf(
                        TransactionType.EXPENSE to "Gasto",
                        TransactionType.INCOME to "Ingreso",
                        TransactionType.TRANSFER to "Traspaso",
                        TransactionType.CREDIT_CARD_PAYMENT to "Pagar TdC"
                    )

                    types.forEach { (type, label) ->
                        val isSelected = selectedType == type
                        val activeColor = when (type) {
                            TransactionType.EXPENSE -> FintechExpense
                            TransactionType.INCOME -> FintechIncome
                            TransactionType.TRANSFER -> NeonViolet
                            TransactionType.CREDIT_CARD_PAYMENT -> NeonCyan
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) activeColor.copy(alpha = 0.25f) else Color.Transparent)
                                .clickable { selectedType = type }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) activeColor else TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Campo de Monto
                Text(text = "Monto ($ MXN)", fontSize = 12.sp, color = TextSecondary)
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    placeholder = { Text("0.00", color = TextMuted, fontSize = 24.sp) },
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonEmerald,
                        unfocusedBorderColor = FintechCardBorder,
                        focusedContainerColor = FintechCard,
                        unfocusedContainerColor = FintechCard
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Campo de Concepto
                Text(text = "Concepto o Comercio", fontSize = 12.sp, color = TextSecondary)
                OutlinedTextField(
                    value = titleText,
                    onValueChange = { titleText = it },
                    placeholder = { Text("Ej. Oxxo, Chedraui, Sueldo, Gasolina...", color = TextMuted) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonEmerald,
                        unfocusedBorderColor = FintechCardBorder,
                        focusedContainerColor = FintechCard,
                        unfocusedContainerColor = FintechCard,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Selector de Cuenta Origen
                Text(
                    text = if (selectedType == TransactionType.CREDIT_CARD_PAYMENT) "Cuenta de Débito (De donde pagas)" else "Cuenta",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    accounts.forEach { acc ->
                        val isSelected = acc.id == selectedAccountId
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedAccountId = acc.id },
                            label = { Text("${acc.name} (${acc.bankName})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonEmerald.copy(alpha = 0.2f),
                                selectedLabelColor = NeonEmerald,
                                containerColor = FintechCard,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                // Selector de Destino (Si es transferencia o pago de tarjeta)
                if (selectedType == TransactionType.TRANSFER || selectedType == TransactionType.CREDIT_CARD_PAYMENT) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (selectedType == TransactionType.CREDIT_CARD_PAYMENT) "Tarjeta de Crédito Destino" else "Cuenta Destino",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        accounts.filter { it.id != selectedAccountId }.forEach { acc ->
                            val isSelected = acc.id == selectedTargetAccountId
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedTargetAccountId = acc.id },
                                label = { Text("${acc.name} (${acc.bankName})") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonViolet.copy(alpha = 0.2f),
                                    selectedLabelColor = NeonViolet,
                                    containerColor = FintechCard,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Selector de Categoría
                Text(text = "Categoría", fontSize = 12.sp, color = TextSecondary)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val categories = listOf(
                        TransactionCategory.ALIMENTOS_SUPER to "Supermercado",
                        TransactionCategory.RESTAURANTES_DELIVERY to "Restaurantes",
                        TransactionCategory.TRANSPORTE_GASOLINA to "Transporte",
                        TransactionCategory.SERVICIOS_HOGAR to "Servicios",
                        TransactionCategory.ENTRETENIMIENTO_SUSCRIPCIONES to "Suscripciones",
                        TransactionCategory.SALUD_FARMACIA to "Salud",
                        TransactionCategory.COMPRAS_SHOPPING to "Compras",
                        TransactionCategory.NOMINA_SUELDO to "Nómina",
                        TransactionCategory.OTROS to "Otros"
                    )

                    categories.forEach { (cat, label) ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                                selectedLabelColor = NeonCyan,
                                containerColor = FintechCard,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Nota adicional
                Text(text = "Nota (Opcional)", fontSize = 12.sp, color = TextSecondary)
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    placeholder = { Text("Comentarios o detalles...", color = TextMuted) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonEmerald,
                        unfocusedBorderColor = FintechCardBorder,
                        focusedContainerColor = FintechCard,
                        unfocusedContainerColor = FintechCard,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Botón Guardar
                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        if (amount > 0.0 && selectedAccountId.isNotBlank()) {
                            val defaultTitle = if (titleText.isNotBlank()) titleText else when (selectedType) {
                                TransactionType.EXPENSE -> "Gasto"
                                TransactionType.INCOME -> "Ingreso"
                                TransactionType.TRANSFER -> "Transferencia"
                                TransactionType.CREDIT_CARD_PAYMENT -> "Pago Tarjeta"
                            }
                            onSaveTransaction(
                                selectedAccountId,
                                if (selectedType == TransactionType.TRANSFER || selectedType == TransactionType.CREDIT_CARD_PAYMENT) selectedTargetAccountId else null,
                                defaultTitle,
                                amount,
                                selectedType,
                                selectedCategory,
                                noteText
                            )
                            onDismiss()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald)
                ) {
                    Text(
                        text = "Registrar Movimiento",
                        fontWeight = FontWeight.Bold,
                        color = FintechBackground,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
