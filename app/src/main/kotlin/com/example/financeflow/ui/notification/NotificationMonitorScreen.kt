package com.example.financeflow.ui.notification

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
import com.example.financeflow.domain.model.NotificationLog
import com.example.financeflow.domain.model.TransactionType
import com.example.financeflow.ui.components.Formatters
import com.example.financeflow.ui.components.GlassCard
import com.example.financeflow.ui.theme.*

@Composable
fun NotificationMonitorScreen(
    isPermissionGranted: Boolean,
    notificationLogs: List<NotificationLog>,
    onOpenSettings: () -> Unit,
    onRefreshPermission: () -> Unit,
    onSimulateNotification: (packageName: String, title: String, text: String) -> Boolean,
    modifier: Modifier = Modifier
) {
    var customBankTitle by remember { mutableStateOf("BBVA México") }
    var customNotificationText by remember { mutableStateOf("Cargo por $450.00 en OXXO con tarjeta débito *4821") }
    var simulationMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(FintechBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Monitor de Notificaciones",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Lector en segundo plano para cargos y depósitos bancarios",
                fontSize = 13.sp,
                color = TextSecondary
            )
        }

        // Estado del Servicio de Escucha Android
        item {
            GlassCard(
                accentGlow = if (isPermissionGranted) NeonEmerald else FintechWarning,
                borderColor = if (isPermissionGranted) NeonEmerald.copy(alpha = 0.4f) else FintechWarning.copy(alpha = 0.4f)
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
                                .background(if (isPermissionGranted) NeonEmerald.copy(alpha = 0.2f) else FintechWarning.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (isPermissionGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = "Estado",
                                tint = if (isPermissionGranted) NeonEmerald else FintechWarning
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = if (isPermissionGranted) "Servicio Activo y Escuchando" else "Requiere Acceso a Notificaciones",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 15.sp
                            )
                            Text(
                                text = if (isPermissionGranted)
                                    "Los cargos y abonos de tus bancos se registrarán automáticamente."
                                else
                                    "Otorga permiso en Ajustes de Android para detectar cargos en vivo.",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onOpenSettings,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPermissionGranted) FintechSurface else NeonEmerald
                        )
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Ajustes", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPermissionGranted) "Revisar Ajustes" else "Activar Permiso",
                            fontWeight = FontWeight.Bold,
                            color = if (isPermissionGranted) TextPrimary else FintechBackground,
                            fontSize = 12.sp
                        )
                    }

                    OutlinedButton(
                        onClick = onRefreshPermission,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Simulador de Notificaciones Bancarias
        item {
            GlassCard(
                accentGlow = NeonCyan,
                shapeRadius = 20.dp
            ) {
                Text(
                    text = "🧪 Simulador de Notificaciones Bancarias",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Prueba en vivo cómo nuestro motor detecta y clasifica notificaciones de bancos mexicanos.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Plantillas rápidas
                Text(text = "Cargar plantilla rápida:", fontSize = 11.sp, color = TextMuted)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val presets = listOf(
                        Triple("BBVA Compra", "BBVA México", "BBVA: Cargo por $820.00 en Super Chedraui con tarjeta débito *4821"),
                        Triple("Nu Tarjeta", "Nu México", "Nu: Compra autorizada por $1,450.00 en Amazon con tarjeta de crédito *9042"),
                        Triple("Mercado Pago", "Mercado Pago", "Mercado Pago: Pagaste $250.00 en Farmacias Guadalajara"),
                        Triple("Santander Nómina", "Santander", "Santander: Abono a tu cuenta *1234 por $15,000.00 por concepto Nómina"),
                        Triple("Pago Tarjeta Nu", "Nu México", "Nu: Recibimos tu pago de tarjeta por $4,850.00. Tu límite fue liberado")
                    )

                    presets.forEach { (label, bank, text) ->
                        FilterChip(
                            selected = customNotificationText == text,
                            onClick = {
                                customBankTitle = bank
                                customNotificationText = text
                                simulationMessage = null
                            },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                                selectedLabelColor = NeonCyan,
                                containerColor = FintechSurface,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "Título de la Notificación (Banco):", fontSize = 11.sp, color = TextSecondary)
                OutlinedTextField(
                    value = customBankTitle,
                    onValueChange = { customBankTitle = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = FintechCardBorder,
                        focusedContainerColor = FintechSurface,
                        unfocusedContainerColor = FintechSurface,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "Cuerpo de la Notificación:", fontSize = 11.sp, color = TextSecondary)
                OutlinedTextField(
                    value = customNotificationText,
                    onValueChange = { customNotificationText = it },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = FintechCardBorder,
                        focusedContainerColor = FintechSurface,
                        unfocusedContainerColor = FintechSurface,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        val success = onSimulateNotification(
                            "com.simulated.bank",
                            customBankTitle,
                            customNotificationText
                        )
                        simulationMessage = if (success) {
                            "✅ ¡Notificación procesada y movimiento registrado exitosamente!"
                        } else {
                            "❌ No se pudo extraer el monto o formato no reconocido."
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Simular", tint = FintechBackground)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Simular Notificación Ahora",
                        color = FintechBackground,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (simulationMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = simulationMessage!!,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (simulationMessage!!.startsWith("✅")) NeonEmerald else FintechExpense
                    )
                }
            }
        }

        // Registro de notificaciones leídas
        item {
            Text(
                text = "Historial de Notificaciones Leídas",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        if (notificationLogs.isEmpty()) {
            item {
                Text(
                    text = "Aún no se han capturado notificaciones.",
                    color = TextMuted,
                    modifier = Modifier.padding(vertical = 20.dp)
                )
            }
        } else {
            items(notificationLogs) { log ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shapeRadius = 14.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (log.parsedSuccess) NeonEmerald.copy(alpha = 0.2f) else FintechExpense.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (log.parsedSuccess) Icons.Default.Check else Icons.Default.Close,
                                    contentDescription = "Estado",
                                    tint = if (log.parsedSuccess) NeonEmerald else FintechExpense,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = log.detectedBank ?: log.title.ifBlank { "Notificación" },
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = Formatters.formatDate(log.timestamp),
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        if (log.detectedAmount != null) {
                            Text(
                                text = Formatters.formatCurrency(log.detectedAmount),
                                fontWeight = FontWeight.Bold,
                                color = if (log.detectedType == TransactionType.INCOME) FintechIncome else TextPrimary,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = log.content,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(FintechSurface)
                            .padding(6.dp)
                    )
                }
            }
        }
    }
}
