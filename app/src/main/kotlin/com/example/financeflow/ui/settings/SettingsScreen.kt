package com.example.financeflow.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financeflow.data.updater.UpdateState
import com.example.financeflow.ui.FinanceViewModel
import com.example.financeflow.ui.components.GlassCard
import com.example.financeflow.domain.model.PayrollFrequency
import com.example.financeflow.domain.model.AccountType
import com.example.financeflow.domain.model.CardNetWorthMode
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import com.example.financeflow.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentTheme by viewModel.themeMode.collectAsState()
    val cardNetWorthMode by viewModel.cardNetWorthMode.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    val payrollConfig by viewModel.payrollConfig.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val isAutoBackupEnabled by viewModel.isAutoBackupEnabled.collectAsState()
    val autoBackupFrequency by viewModel.autoBackupFrequency.collectAsState()
    val autoBackupTime by viewModel.autoBackupTime.collectAsState()
    val lastAutoBackupDate by viewModel.lastAutoBackupDate.collectAsState()
    val context = LocalContext.current

    var showMasterResetDialog by remember { mutableStateOf(false) }
    var resetSuccessMessage by remember { mutableStateOf(false) }
    var backupStatusMessage by remember { mutableStateOf<String?>(null) }
    var autoBackupStatusMessage by remember { mutableStateOf<String?>(null) }

    var payrollFrequency by remember(payrollConfig) { mutableStateOf(payrollConfig.frequency) }
    var payrollAmountText by remember(payrollConfig) { mutableStateOf(payrollConfig.expectedAmount.toString()) }
    var payrollCustomDaysText by remember(payrollConfig) { mutableStateOf(payrollConfig.customDays.joinToString(", ")) }
    var payrollAccountId by remember(payrollConfig) { mutableStateOf(payrollConfig.defaultAccountId ?: accounts.firstOrNull()?.id ?: "") }
    var payrollSavedMessage by remember { mutableStateOf(false) }

    var githubRepo by remember { mutableStateOf(viewModel.updateManager.configuredRepo) }
    var githubToken by remember { mutableStateOf(viewModel.updateManager.personalAccessToken) }

    val createDocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    os.write(viewModel.createBackupJson().toByteArray(Charsets.UTF_8))
                }
                backupStatusMessage = "✅ Copia de seguridad guardada con éxito en el archivo."
            } catch (e: Exception) {
                backupStatusMessage = "❌ Error al guardar archivo de respaldo: ${e.message}"
            }
        }
    }

    val openDocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                val jsonString = context.contentResolver.openInputStream(uri)?.use {
                    it.bufferedReader().readText()
                } ?: ""
                val res = viewModel.restoreBackup(jsonString)
                if (res.isSuccess) {
                    backupStatusMessage = "✅ ${res.getOrNull()}"
                } else {
                    backupStatusMessage = "❌ Archivo de respaldo incompatible o dañado."
                }
            } catch (e: Exception) {
                backupStatusMessage = "❌ Error al leer archivo: ${e.message}"
            }
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
                        text = "Apariencia, Horarios, Datos y Sistema",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // ==========================================
        // 1. CONCEPTO: APARIENCIA - TEMAS
        // ==========================================
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "🎨 Apariencia",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Temas visuales Dark Fintech para la interfaz",
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

        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "💳 Cómputo de Tarjetas en Patrimonio Neto",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Elige si las tarjetas de crédito restan la deuda o suman el saldo disponible",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shapeRadius = 16.dp,
                borderColor = FintechCardBorder
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Opción 1: Deuda actual (Resta)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.setCardNetWorthMode(CardNetWorthMode.DEBT) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Restar deuda acumulada (-$)",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Ej. Si debes $6,000 de $10,000, se restan -$6,000 en el patrimonio neto.",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        RadioButton(
                            selected = cardNetWorthMode == CardNetWorthMode.DEBT,
                            onClick = { viewModel.setCardNetWorthMode(CardNetWorthMode.DEBT) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = AccentNeon,
                                unselectedColor = TextMuted
                            )
                        )
                    }

                    HorizontalDivider(color = FintechBorder.copy(alpha = 0.5f))

                    // Opción 2: Crédito disponible (Suma)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.setCardNetWorthMode(CardNetWorthMode.AVAILABLE_CREDIT) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Sumar crédito disponible (+$)",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Ej. Si tienes ocupados $6,000 de $10,000, muestra un saldo positivo de +$4,000 disponibles.",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        RadioButton(
                            selected = cardNetWorthMode == CardNetWorthMode.AVAILABLE_CREDIT,
                            onClick = { viewModel.setCardNetWorthMode(CardNetWorthMode.AVAILABLE_CREDIT) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = AccentNeon,
                                unselectedColor = TextMuted
                            )
                        )
                    }
                }
            }
        }

        // ==========================================
        // 2. CONCEPTO: HORARIOS Y FECHAS - CALENDARIO DE COBRO DE NÓMINA
        // ==========================================
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "📅 Horarios y Fechas",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Calendario de cobro de nómina y periodicidad",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        item {
            GlassCard(
                borderColor = AccentNeon.copy(alpha = 0.4f),
                accentGlow = AccentNeon,
                shapeRadius = 18.dp
            ) {
                Text("Frecuencia de Cobro de Nómina:", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PayrollFrequency.values().forEach { freq ->
                        val isSelected = payrollFrequency == freq
                        FilterChip(
                            selected = isSelected,
                            onClick = { payrollFrequency = freq },
                            label = {
                                Text(
                                    when (freq) {
                                        PayrollFrequency.QUINCENAL -> "Por Quincena (15 y fin)"
                                        PayrollFrequency.SEMANAL -> "Por Semana"
                                        PayrollFrequency.MENSUAL -> "Por Mes"
                                        PayrollFrequency.CATORCENAL -> "Catorcenal (14 días)"
                                        PayrollFrequency.PERSONALIZADO -> "Personalizado"
                                    }
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (payrollFrequency == PayrollFrequency.PERSONALIZADO) {
                    OutlinedTextField(
                        value = payrollCustomDaysText,
                        onValueChange = { payrollCustomDaysText = it },
                        label = { Text("Días del mes en que cobras (ej. 5, 20 o 1, 15)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                OutlinedTextField(
                    value = payrollAmountText,
                    onValueChange = { payrollAmountText = it },
                    label = { Text("Monto esperado por pago de nómina ($ MXN)") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Cuenta bancaria donde recibes tu nómina:", fontSize = 12.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    accounts.filter { it.type != AccountType.CREDIT }.forEach { acc ->
                        val isSelected = payrollAccountId == acc.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) AccentNeon.copy(alpha = 0.15f) else FintechSurface)
                                .clickable { payrollAccountId = acc.id }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { payrollAccountId = acc.id }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("${acc.name} (${acc.bankName})", fontSize = 12.sp, color = TextPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        val amount = payrollAmountText.toDoubleOrNull() ?: 15000.0
                        val customDays = payrollCustomDaysText
                            .split(",")
                            .mapNotNull { it.trim().toIntOrNull() }
                            .filter { it in 1..31 }

                        val newConfig = payrollConfig.copy(
                            frequency = payrollFrequency,
                            expectedAmount = amount,
                            customDays = if (customDays.isNotEmpty()) customDays else listOf(15, 30),
                            defaultAccountId = payrollAccountId.ifBlank { null }
                        )
                        viewModel.updatePayrollConfig(newConfig)
                        payrollSavedMessage = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentNeon)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Guardar Configuración de Nómina", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                if (payrollSavedMessage) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "✅ Configuración de nómina y calendario actualizada.",
                        color = NeonEmerald,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // ==========================================
        // 3. CONCEPTO: DATOS - RESPALDO, RESPALDO AUTOMÁTICO Y ZONA DE PELIGRO
        // ==========================================
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "💾 Datos",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Respaldo local, respaldo automático y zona de peligro",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        // 3.1 Respaldo Manual Local
        item {
            GlassCard(
                borderColor = AccentNeon.copy(alpha = 0.4f),
                accentGlow = AccentNeon,
                shapeRadius = 18.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(AccentNeon.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Respaldo", tint = AccentNeon)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Respaldo Manual (JSON)",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Exporta o restaura tus cuentas, movimientos y configuraciones en archivo JSON",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                            createDocLauncher.launch("Finance0_Backup_$timeStamp.json")
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentNeon)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Exportar", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            try {
                                val jsonContent = viewModel.createBackupJson()
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Copia de Seguridad Finance0")
                                    putExtra(Intent.EXTRA_TEXT, jsonContent)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Compartir Copia de Seguridad"))
                            } catch (e: Exception) {
                                backupStatusMessage = "❌ Error al compartir: ${e.message}"
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, AccentSecondary)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = AccentSecondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Compartir", color = AccentSecondary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = {
                        openDocLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, FintechBorder)
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Restaurar desde archivo JSON", color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                }

                if (backupStatusMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = backupStatusMessage!!,
                        color = if (backupStatusMessage!!.startsWith("✅")) NeonEmerald else FintechExpense,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // 3.2 Respaldo Automático Programable
        item {
            GlassCard(
                borderColor = NeonCyan.copy(alpha = 0.4f),
                accentGlow = NeonCyan,
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
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(NeonCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = "Auto Respaldo", tint = NeonCyan)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Respaldo Automático",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Elige periodicidad y hora de guardado automático",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Switch(
                        checked = isAutoBackupEnabled,
                        onCheckedChange = { viewModel.setAutoBackupEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = FintechBackground,
                            checkedTrackColor = NeonCyan
                        )
                    )
                }

                if (isAutoBackupEnabled) {
                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Frecuencia del Respaldo Automático:", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "DAILY" to "Diario",
                            "WEEKLY" to "Semanal",
                            "MONTHLY" to "Mensual"
                        ).forEach { (key, label) ->
                            val isSel = autoBackupFrequency == key
                            FilterChip(
                                selected = isSel,
                                onClick = { viewModel.setAutoBackupFrequency(key) },
                                label = { Text(label, fontSize = 12.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Hora de Ejecución:", fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("00:00", "02:00", "06:00", "12:00", "20:00", "23:00").forEach { timeOption ->
                            val isSel = autoBackupTime == timeOption
                            FilterChip(
                                selected = isSel,
                                onClick = { viewModel.setAutoBackupTime(timeOption) },
                                label = { Text(timeOption, fontSize = 12.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(FintechSurface)
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Último respaldo: $lastAutoBackupDate",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            val msg = viewModel.triggerManualAutoBackupNow()
                            autoBackupStatusMessage = "✅ $msg"
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = FintechBackground, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ejecutar Respaldo Automático Ahora", color = FintechBackground, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    if (autoBackupStatusMessage != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = autoBackupStatusMessage!!,
                            color = NeonEmerald,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 3.3 Zona de Peligro: Borrado Maestro
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
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(FintechExpense.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = "Borrado Maestro", tint = FintechExpense)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Zona de Peligro: Borrado Maestro",
                            fontWeight = FontWeight.Bold,
                            color = FintechExpense,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Elimina permanentemente todas las cuentas, movimientos, MSI y nóminas.",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

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

        // ==========================================
        // 4. CONCEPTO: SISTEMA - NOTIFICACIONES Y ACTUALIZACIONES
        // ==========================================
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "⚙️ Sistema",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Actualizaciones del motor y repositorio",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        // 4.1 Actualizaciones del Motor (GitHub OTA)
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

        // 4.2 Avisos de Pagos de Tarjetas y Servicios
        item {
            var remindersCheckedMsg by remember { mutableStateOf<String?>(null) }
            val context = LocalContext.current

            GlassCard(shapeRadius = 18.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "🔔 Avisos de Pagos y Servicios", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(
                            text = "Avisos automáticos de vencimientos de tarjetas de crédito y pagos de servicios (3 días previos y el día límite).",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            viewModel.checkPaymentReminders(context)
                            remindersCheckedMsg = "¡Avisos de pago comprobados exitosamente!"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald)
                    ) {
                        Text("Verificar", color = FintechBackground, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                if (remindersCheckedMsg != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = remindersCheckedMsg!!, fontSize = 11.sp, color = NeonEmerald, fontWeight = FontWeight.SemiBold)
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
