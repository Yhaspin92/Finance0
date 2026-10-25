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

        // Estado del Servicio de Escucha Android: SOLO se muestra si el permiso NO ha sido otorgado
        if (!isPermissionGranted) {
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
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(FintechWarning.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = "Estado",
                                    tint = FintechWarning
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "Requiere Acceso a Notificaciones",
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Otorga permiso en Ajustes de Android para detectar cargos en vivo.",
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
                            colors = ButtonDefaults.buttonColors(containerColor = FintechWarning)
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Ajustes", modifier = Modifier.size(16.dp), tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Activar Permiso",
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
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
