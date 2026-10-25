package com.example.financeflow.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.financeflow.data.updater.UpdateState
import com.example.financeflow.ui.FinanceViewModel
import com.example.financeflow.ui.components.GlassCard
import com.example.financeflow.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SettingsScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentTheme by viewModel.themeMode.collectAsState()
    val updateState by viewModel.updateState.collectAsState()

    var showMasterResetDialog by remember { mutableStateOf(false) }
    var resetSuccessMessage by remember { mutableStateOf(false) }

    var githubRepo by remember { mutableStateOf(viewModel.updateManager.configuredRepo) }
    var githubToken by remember { mutableStateOf(viewModel.updateManager.personalAccessToken) }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = TextPrimary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Configuración",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Temas, actualizaciones OTA y administración de datos",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // ==========================================
        // 1. SECCIÓN DE TEMAS VISUALES
        // ==========================================
        item {
            Text(
                text = "🎨 Temas y Apariencia",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Selecciona la paleta Dark Fintech que prefieras para la app",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        items(AppThemeMode.values().size) { index ->
            val mode = AppThemeMode.values()[index]
            val isSelected = currentTheme == mode

            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.setThemeMode(mode) },
                shapeRadius = 16.dp,
                borderColor = if (isSelected) mode.primaryPreview else FintechCardBorder,
                borderWidth = if (isSelected) 2.dp else 1.dp,
                accentGlow = if (isSelected) mode.primaryPreview else null
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
                        // Muestras de color
                        Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(mode.primaryPreview)
                                    .border(BorderStroke(2.dp, FintechBackground), CircleShape)
                            )
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(mode.secondaryPreview)
                                    .border(BorderStroke(2.dp, FintechBackground), CircleShape)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = mode.title,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 15.sp
                            )
                            Text(
                                text = mode.subtitle,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    RadioButton(
                        selected = isSelected,
                        onClick = { viewModel.setThemeMode(mode) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = mode.primaryPreview,
                            unselectedColor = TextMuted
                        )
                    )
                }
            }
        }

        // ==========================================
        // 2. SECCIÓN DE ACTUALIZACIONES GITHUB OTA
        // ==========================================
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "🚀 Actualizaciones del Motor (GitHub OTA)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Actualiza Finance0 directamente por internet sin intermediarios",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        item {
            GlassCard(shapeRadius = 18.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Versión Actual Instalada", fontSize = 12.sp, color = TextSecondary)
                        Text(
                            text = "Finance0 v${viewModel.updateManager.currentVersion}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonEmerald
                        )
                    }

                    Button(
                        onClick = { viewModel.checkForUpdates(githubRepo, githubToken) },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                    ) {
                        Icon(Icons.Default.CloudSync, contentDescription = "Buscar", tint = FintechBackground, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Buscar", color = FintechBackground, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(text = "Repositorio GitHub de Releases:", fontSize = 11.sp, color = TextSecondary)
                OutlinedTextField(
                    value = githubRepo,
                    onValueChange = {
                        githubRepo = it
                        viewModel.updateManager.configuredRepo = it
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = FintechCardBorder
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "Token de Acceso Personal (Opcional, si el repo es privado):", fontSize = 11.sp, color = TextMuted)
                OutlinedTextField(
                    value = githubToken,
                    onValueChange = {
                        githubToken = it
                        viewModel.updateManager.personalAccessToken = it
                    },
                    singleLine = true,
                    placeholder = { Text("ghp_xxxx...", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = FintechCardBorder
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Estado de la actualización
                when (val state = updateState) {
                    is UpdateState.Idle -> {
                        Text(
                            text = "Toca 'Buscar' para consultar si hay una nueva versión publicada en GitHub.",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                    is UpdateState.Checking -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = NeonCyan, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Consultando GitHub Releases...", fontSize = 12.sp, color = NeonCyan)
                        }
                    }
                    is UpdateState.UpToDate -> {
                        Text(
                            text = "✅ Finance0 está en la versión más reciente (v${state.currentVersion}).",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonEmerald
                        )
                    }
                    is UpdateState.UpdateAvailable -> {
                        Column {
                            Text(
                                text = "🚀 ¡Nueva versión disponible: v${state.release.tagName}!",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                            Text(
                                text = state.release.body,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            if (state.release.apkDownloadUrl != null) {
                                Button(
                                    onClick = { viewModel.downloadAndInstallApk(state.release.apkDownloadUrl, githubToken) },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = "Descargar", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Descargar e Instalar APK", color = FintechBackground, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    is UpdateState.Downloading -> {
                        Column {
                            Text("Descargando actualización: ${state.progress}%", fontSize = 12.sp, color = NeonCyan)
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(progress = { state.progress / 100f }, modifier = Modifier.fillMaxWidth(), color = NeonCyan)
                        }
                    }
                    is UpdateState.ReadyToInstall -> {
                        Column {
                            Text("Descarga completa. Abriendo instalador de Android...", fontSize = 12.sp, color = NeonEmerald)
                            Spacer(modifier = Modifier.height(6.dp))
                            Button(
                                onClick = { viewModel.promptInstallApk(state.apkFile) },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald)
                            ) {
                                Text("Instalar APK", color = FintechBackground, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    is UpdateState.NeedsInstallPermission -> {
                        Column {
                            Text(
                                text = "⚠️ Se requiere autorizar la instalación de aplicaciones desconocidas en Android.",
                                fontSize = 12.sp,
                                color = FintechWarning,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Button(
                                onClick = { viewModel.promptInstallApk(state.apkFile) },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald)
                            ) {
                                Text("Autorizar e Instalar", color = FintechBackground, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    is UpdateState.Error -> {
                        Text(text = state.message, fontSize = 12.sp, color = FintechExpense)
                    }
                }
            }
        }

        // ==========================================
        // 3. SECCIÓN DE BORRADO MAESTRO DE DATOS
        // ==========================================
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "⚠️ Zona de Peligro",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = FintechExpense
            )
            Text(
                text = "Acciones destructivas e irreversibles sobre la base de datos local",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        item {
            GlassCard(
                borderColor = FintechExpense.copy(alpha = 0.5f),
                accentGlow = FintechExpense,
                shapeRadius = 18.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(FintechExpense.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = "Borrado Maestro", tint = FintechExpense)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Borrado Maestro de Datos",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Elimina permanentemente todas las cuentas, transacciones, compras a MSI, registros y nóminas.",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showMasterResetDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FintechExpense)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = "Borrar", tint = TextPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Ejecutar Borrado Maestro",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                if (resetSuccessMessage) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "✅ Todos los datos fueron eliminados correctamente.",
                        color = NeonEmerald,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // ==========================================
    // MODAL DE CONFIRMACIÓN CON ESPERA DE 10 SEGUNDOS
    // ==========================================
    if (showMasterResetDialog) {
        var secondsRemaining by remember { mutableStateOf(10) }

        // Cronómetro de cuenta regresiva de 10 segundos
        LaunchedEffect(Unit) {
            secondsRemaining = 10
            while (secondsRemaining > 0) {
                delay(1000L)
                secondsRemaining--
            }
        }

        AlertDialog(
            onDismissRequest = { showMasterResetDialog = false },
            icon = {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(FintechExpense.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Warning, contentDescription = "Alerta", tint = FintechExpense, modifier = Modifier.size(30.dp))
                }
            },
            title = {
                Text(
                    text = "¡ADVERTENCIA DE BORRADO MAESTRO!",
                    fontWeight = FontWeight.ExtraBold,
                    color = FintechExpense,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Esta acción es completamente irreversible. Se borrarán permanentemente:",
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "• Todas las Cuentas de Débito, Crédito y Efectivo", fontSize = 12.sp, color = TextSecondary)
                    Text(text = "• Todo el Historial de Transacciones y Gastos", fontSize = 12.sp, color = TextSecondary)
                    Text(text = "• Todas las Compras a Meses Sin Intereses (MSI)", fontSize = 12.sp, color = TextSecondary)
                    Text(text = "• Todos los Recordatorios de Pagos y Nómina", fontSize = 12.sp, color = TextSecondary)
                    Text(text = "• Todo el Registro de Notificaciones de Bancos", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(14.dp))

                    if (secondsRemaining > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(FintechWarning.copy(alpha = 0.15f))
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "⏳ Espera $secondsRemaining segundos para habilitar la confirmación...",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = FintechWarning
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(FintechExpense.copy(alpha = 0.15f))
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "⚠️ El botón de confirmación ya está habilitado.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = FintechExpense
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (secondsRemaining == 0) {
                            viewModel.executeMasterReset()
                            showMasterResetDialog = false
                            resetSuccessMessage = true
                        }
                    },
                    enabled = secondsRemaining == 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FintechExpense,
                        disabledContainerColor = FintechCardBorder
                    )
                ) {
                    Text(
                        text = if (secondsRemaining > 0) "Espera ($secondsRemaining s)" else "Confirmar Borrado Definitivo",
                        fontWeight = FontWeight.Bold,
                        color = if (secondsRemaining == 0) TextPrimary else TextMuted
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showMasterResetDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
            containerColor = FintechSurface
        )
    }
}
