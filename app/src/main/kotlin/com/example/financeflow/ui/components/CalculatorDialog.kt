package com.example.financeflow.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.financeflow.ui.theme.*
import java.text.DecimalFormat

@Composable
fun CalculatorDialog(
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    var displayValue by remember { mutableStateOf("0") }
    var expressionHistory by remember { mutableStateOf("") }
    var operand1 by remember { mutableStateOf<Double?>(null) }
    var pendingOperation by remember { mutableStateOf<String?>(null) }
    var isNewNumber by remember { mutableStateOf(true) }

    val formatter = remember { DecimalFormat("#,##0.######") }

    fun formatNumber(value: Double): String {
        return if (value.isNaN() || value.isInfinite()) "Error" else formatter.format(value)
    }

    fun onNumberClick(numStr: String) {
        if (isNewNumber) {
            displayValue = if (numStr == ".") "0." else numStr
            isNewNumber = false
        } else {
            if (numStr == ".") {
                if (!displayValue.contains(".")) {
                    displayValue += "."
                }
            } else {
                if (displayValue == "0") {
                    displayValue = numStr
                } else if (displayValue.length < 14) {
                    displayValue += numStr
                }
            }
        }
    }

    fun onOperatorClick(op: String) {
        val currentVal = displayValue.toDoubleOrNull() ?: 0.0

        if (operand1 == null) {
            operand1 = currentVal
            pendingOperation = op
            expressionHistory = "${formatNumber(currentVal)} $op"
            isNewNumber = true
        } else if (pendingOperation != null) {
            if (!isNewNumber) {
                val op1 = operand1!!
                val result = when (pendingOperation) {
                    "+" -> op1 + currentVal
                    "−" -> op1 - currentVal
                    "×" -> op1 * currentVal
                    "÷" -> if (currentVal != 0.0) op1 / currentVal else Double.NaN
                    else -> currentVal
                }
                operand1 = result
                displayValue = formatNumber(result)
                expressionHistory = "${formatNumber(result)} $op"
            } else {
                expressionHistory = "${formatNumber(operand1!!)} $op"
            }
            pendingOperation = op
            isNewNumber = true
        }
    }

    fun onEqualsClick() {
        if (operand1 != null && pendingOperation != null) {
            val currentVal = displayValue.toDoubleOrNull() ?: 0.0
            val op1 = operand1!!
            val op = pendingOperation!!
            val result = when (op) {
                "+" -> op1 + currentVal
                "−" -> op1 - currentVal
                "×" -> op1 * currentVal
                "÷" -> if (currentVal != 0.0) op1 / currentVal else Double.NaN
                else -> currentVal
            }
            expressionHistory = "${formatNumber(op1)} $op ${formatNumber(currentVal)} ="
            displayValue = formatNumber(result)
            operand1 = null
            pendingOperation = null
            isNewNumber = true
        }
    }

    fun onClearClick() {
        displayValue = "0"
        expressionHistory = ""
        operand1 = null
        pendingOperation = null
        isNewNumber = true
    }

    fun onDeleteClick() {
        if (!isNewNumber && displayValue.isNotEmpty()) {
            displayValue = if (displayValue.length == 1 || (displayValue.length == 2 && displayValue.startsWith("-"))) {
                "0"
            } else {
                displayValue.dropLast(1)
            }
            if (displayValue.isEmpty()) displayValue = "0"
        }
    }

    fun onPercentClick() {
        val currentVal = displayValue.toDoubleOrNull() ?: return
        val result = currentVal / 100.0
        displayValue = formatNumber(result)
        isNewNumber = true
    }

    fun onToggleSign() {
        val currentVal = displayValue.toDoubleOrNull() ?: return
        val result = -currentVal
        displayValue = formatNumber(result)
    }

    fun copyToClipboard() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val cleanNumber = displayValue.replace(",", "")
        val clip = ClipData.newPlainText("Resultado", cleanNumber)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copiado al portapapeles: $cleanNumber", Toast.LENGTH_SHORT).show()
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable { onDismissRequest() },
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(FintechSurface)
                    .border(1.dp, FintechBorder, RoundedCornerShape(24.dp))
                    .clickable(enabled = false) {}
                    .padding(20.dp)
            ) {
                // Cabecera
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
                                .background(AccentNeon.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🧮", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Calculadora",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "Finanzas & Presupuestos",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(FintechCard)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pantalla de la calculadora
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(FintechBackground)
                        .border(1.dp, FintechBorder, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = if (expressionHistory.isNotEmpty()) expressionHistory else " ",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = displayValue,
                        color = TextPrimary,
                        fontSize = if (displayValue.length > 9) 26.sp else 34.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        textAlign = TextAlign.End
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Teclado
                val buttonSpacing = 10.dp

                // Fila 1: C, ⌫, %, ÷
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
                ) {
                    CalcKey(
                        text = "C",
                        modifier = Modifier.weight(1f),
                        backgroundColor = FintechWarning.copy(alpha = 0.15f),
                        textColor = FintechWarning,
                        onClick = { onClearClick() }
                    )
                    CalcIconKey(
                        icon = Icons.Default.Backspace,
                        modifier = Modifier.weight(1f),
                        backgroundColor = FintechCard,
                        iconTint = TextPrimary,
                        onClick = { onDeleteClick() }
                    )
                    CalcKey(
                        text = "%",
                        modifier = Modifier.weight(1f),
                        backgroundColor = FintechCard,
                        textColor = AccentSecondary,
                        onClick = { onPercentClick() }
                    )
                    CalcKey(
                        text = "÷",
                        modifier = Modifier.weight(1f),
                        backgroundColor = AccentNeon.copy(alpha = 0.2f),
                        textColor = AccentNeon,
                        isBold = true,
                        onClick = { onOperatorClick("÷") }
                    )
                }

                Spacer(modifier = Modifier.height(buttonSpacing))

                // Fila 2: 7, 8, 9, ×
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
                ) {
                    CalcKey(text = "7", modifier = Modifier.weight(1f), onClick = { onNumberClick("7") })
                    CalcKey(text = "8", modifier = Modifier.weight(1f), onClick = { onNumberClick("8") })
                    CalcKey(text = "9", modifier = Modifier.weight(1f), onClick = { onNumberClick("9") })
                    CalcKey(
                        text = "×",
                        modifier = Modifier.weight(1f),
                        backgroundColor = AccentNeon.copy(alpha = 0.2f),
                        textColor = AccentNeon,
                        isBold = true,
                        onClick = { onOperatorClick("×") }
                    )
                }

                Spacer(modifier = Modifier.height(buttonSpacing))

                // Fila 3: 4, 5, 6, −
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
                ) {
                    CalcKey(text = "4", modifier = Modifier.weight(1f), onClick = { onNumberClick("4") })
                    CalcKey(text = "5", modifier = Modifier.weight(1f), onClick = { onNumberClick("5") })
                    CalcKey(text = "6", modifier = Modifier.weight(1f), onClick = { onNumberClick("6") })
                    CalcKey(
                        text = "−",
                        modifier = Modifier.weight(1f),
                        backgroundColor = AccentNeon.copy(alpha = 0.2f),
                        textColor = AccentNeon,
                        isBold = true,
                        onClick = { onOperatorClick("−") }
                    )
                }

                Spacer(modifier = Modifier.height(buttonSpacing))

                // Fila 4: 1, 2, 3, +
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
                ) {
                    CalcKey(text = "1", modifier = Modifier.weight(1f), onClick = { onNumberClick("1") })
                    CalcKey(text = "2", modifier = Modifier.weight(1f), onClick = { onNumberClick("2") })
                    CalcKey(text = "3", modifier = Modifier.weight(1f), onClick = { onNumberClick("3") })
                    CalcKey(
                        text = "+",
                        modifier = Modifier.weight(1f),
                        backgroundColor = AccentNeon.copy(alpha = 0.2f),
                        textColor = AccentNeon,
                        isBold = true,
                        onClick = { onOperatorClick("+") }
                    )
                }

                Spacer(modifier = Modifier.height(buttonSpacing))

                // Fila 5: ±, 0, ., =
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
                ) {
                    CalcKey(
                        text = "±",
                        modifier = Modifier.weight(1f),
                        textColor = TextSecondary,
                        onClick = { onToggleSign() }
                    )
                    CalcKey(text = "0", modifier = Modifier.weight(1f), onClick = { onNumberClick("0") })
                    CalcKey(text = ".", modifier = Modifier.weight(1f), onClick = { onNumberClick(".") })
                    CalcKey(
                        text = "=",
                        modifier = Modifier.weight(1f),
                        backgroundColor = AccentNeon,
                        textColor = Color.Black,
                        isBold = true,
                        onClick = { onEqualsClick() }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botón para copiar resultado
                Button(
                    onClick = { copyToClipboard() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FintechCard,
                        contentColor = TextPrimary
                    )
                ) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Copiar",
                        tint = AccentSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Copiar resultado",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun CalcKey(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = FintechCard,
    textColor: Color = TextPrimary,
    isBold: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .aspectRatio(1.25f)
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 20.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold
        )
    }
}

@Composable
private fun CalcIconKey(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    backgroundColor: Color = FintechCard,
    iconTint: Color = TextPrimary,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .aspectRatio(1.25f)
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
    }
}
