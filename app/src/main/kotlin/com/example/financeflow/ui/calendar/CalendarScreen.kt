package com.example.financeflow.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financeflow.domain.model.BillReminder
import com.example.financeflow.domain.model.CreditCard
import com.example.financeflow.domain.model.PayrollConfig
import com.example.financeflow.domain.model.TransactionCategory
import com.example.financeflow.ui.components.Formatters
import com.example.financeflow.ui.components.GlassCard
import com.example.financeflow.ui.theme.*
import java.util.Calendar

@Composable
fun CalendarScreen(
    payrollConfig: PayrollConfig,
    creditCards: List<CreditCard>,
    billReminders: List<BillReminder>,
    onToggleBillPaid: (String) -> Unit,
    onAddBillReminder: (title: String, amount: Double, dueDay: Int, category: TransactionCategory, provider: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val todayCal = Calendar.getInstance()
    val todayDay = todayCal.get(Calendar.DAY_OF_MONTH)
    val maxDaysInMonth = todayCal.getActualMaximum(Calendar.DAY_OF_MONTH)

    var selectedDay by remember { mutableStateOf(todayDay) }
    var showAddBillDialog by remember { mutableStateOf(false) }

    // Días de nómina
    val payrollDays = remember(payrollConfig, maxDaysInMonth) {
        val list = mutableListOf<Int>()
        list.add(payrollConfig.day1)
        val day2 = if (payrollConfig.day2 == 0) maxDaysInMonth else payrollConfig.day2
        list.add(day2)
        list
    }

    // Calcular días restantes para la próxima quincena
    val daysToNextPayroll = remember(todayDay, payrollDays) {
        val upcoming = payrollDays.filter { it >= todayDay }.minOrNull()
        if (upcoming != null) {
            upcoming - todayDay
        } else {
            // Próximo mes día 15
            (maxDaysInMonth - todayDay) + payrollDays.minOrNull()!!
        }
    }

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
                        text = "Calendario Financiero",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Nómina, cortes de tarjeta y gastos recurrentes",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }

                IconButton(
                    onClick = { showAddBillDialog = true },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(NeonEmerald.copy(alpha = 0.2f))
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar Pago", tint = NeonEmerald)
                }
            }
        }

        // Tarjeta Banner: Próximo Cobro de Nómina
        item {
            GlassCard(
                accentGlow = NeonEmerald,
                borderColor = NeonEmerald.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Celebration, contentDescription = "Nómina", tint = NeonEmerald, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Próxima Quincena", fontSize = 13.sp, color = NeonEmerald, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (daysToNextPayroll == 0) "¡HOY ES DÍA DE PAGO!" else "Faltan $daysToNextPayroll días para el cobro",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Monto esperado: ${Formatters.formatCurrency(payrollConfig.expectedAmount)}",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(NeonEmerald)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (daysToNextPayroll == 0) "🎉" else "$daysToNextPayroll d",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = FintechBackground
                        )
                    }
                }
            }
        }

        // Calendario Visual Interactivo (Mes Actual)
        item {
            GlassCard(shapeRadius = 20.dp) {
                Text(
                    text = "Días Clave del Mes",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Grilla de días (1 a maxDays)
                val totalRows = (maxDaysInMonth + 6) / 7
                for (row in 0 until totalRows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (col in 0 until 7) {
                            val day = row * 7 + col + 1
                            if (day <= maxDaysInMonth) {
                                val isSelected = day == selectedDay
                                val isToday = day == todayDay
                                val isPayroll = payrollDays.contains(day)
                                val hasCutoff = creditCards.any { it.cutOffDay == day }
                                val hasPayment = creditCards.any { it.paymentDueDay == day } || billReminders.any { it.dueDay == day }

                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isSelected -> NeonEmerald
                                                isToday -> FintechCardBorder
                                                else -> Color.Transparent
                                            }
                                        )
                                        .clickable { selectedDay = day },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "$day",
                                            fontSize = 12.sp,
                                            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) FintechBackground else TextPrimary
                                        )
                                        // Puntos indicadores
                                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                            if (isPayroll) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(4.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isSelected) FintechBackground else NeonEmerald)
                                                )
                                            }
                                            if (hasCutoff) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(4.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isSelected) FintechBackground else NeonViolet)
                                                )
                                            }
                                            if (hasPayment) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(4.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isSelected) FintechBackground else FintechWarning)
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.size(38.dp))
                            }
                        }
                    }
                    if (row < totalRows - 1) Spacer(modifier = Modifier.height(6.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Leyenda
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    LegendItem(color = NeonEmerald, label = "Nómina")
                    LegendItem(color = NeonViolet, label = "Corte TdC")
                    LegendItem(color = FintechWarning, label = "Límite Pago")
                }
            }
        }

        // Compromisos Financieros y Servicios Programados
        item {
            Text(
                text = "Compromisos del Mes",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        items(billReminders) { bill ->
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
                        Checkbox(
                            checked = bill.isPaid,
                            onCheckedChange = { onToggleBillPaid(bill.id) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = NeonEmerald,
                                checkmarkColor = FintechBackground
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = bill.title,
                                fontWeight = FontWeight.Bold,
                                color = if (bill.isPaid) TextMuted else TextPrimary,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Día ${bill.dueDay} del mes • ${bill.bankOrProvider}",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = Formatters.formatCurrency(bill.amount),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (bill.isPaid) TextMuted else TextPrimary
                        )
                        Text(
                            text = if (bill.isPaid) "PAGADO" else "PENDIENTE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (bill.isPaid) NeonEmerald else FintechWarning
                        )
                    }
                }
            }
        }
    }

    // Modal para Agregar Recordatorio de Pago / Servicio
    if (showAddBillDialog) {
        var billTitle by remember { mutableStateOf("") }
        var billAmount by remember { mutableStateOf("") }
        var billDueDay by remember { mutableStateOf(selectedDay.toString()) }
        var billProvider by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddBillDialog = false },
            title = {
                Text(
                    text = "Nuevo Recordatorio de Pago",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column {
                    Text(text = "Servicio o Concepto:", fontSize = 12.sp, color = TextSecondary)
                    OutlinedTextField(
                        value = billTitle,
                        onValueChange = { billTitle = it },
                        placeholder = { Text("Ej. Luz CFE, Renta, Netflix, Gimnasio") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(text = "Monto Estimado ($ MXN):", fontSize = 12.sp, color = TextSecondary)
                    OutlinedTextField(
                        value = billAmount,
                        onValueChange = { billAmount = it },
                        placeholder = { Text("0.00") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(text = "Día del mes de vencimiento (1 a 31):", fontSize = 12.sp, color = TextSecondary)
                    OutlinedTextField(
                        value = billDueDay,
                        onValueChange = { billDueDay = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(text = "Proveedor o Banco:", fontSize = 12.sp, color = TextSecondary)
                    OutlinedTextField(
                        value = billProvider,
                        onValueChange = { billProvider = it },
                        placeholder = { Text("Ej. CFE, Telmex, Nu") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = billAmount.toDoubleOrNull() ?: 0.0
                        val day = billDueDay.toIntOrNull() ?: selectedDay
                        if (billTitle.isNotBlank() && amount > 0) {
                            onAddBillReminder(
                                billTitle,
                                amount,
                                day.coerceIn(1, 31),
                                TransactionCategory.SERVICIOS_HOGAR,
                                billProvider
                            )
                            showAddBillDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald)
                ) {
                    Text("Guardar Recordatorio", color = FintechBackground, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBillDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = FintechSurface
        )
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 11.sp, color = TextSecondary)
    }
}
