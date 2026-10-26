package com.example.financeflow.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class AccountType {
    DEBIT,
    CREDIT,
    CASH,
    INVESTMENT
}

@Serializable
enum class TransactionType {
    EXPENSE,               // Gasto / Cargo
    INCOME,                // Ingreso / Abono
    TRANSFER,              // Transferencia entre cuentas
    CREDIT_CARD_PAYMENT    // Pago a tarjeta de crédito
}

@Serializable
enum class TransactionCategory {
    ALIMENTOS_SUPER,
    RESTAURANTES_DELIVERY,
    TRANSPORTE_GASOLINA,
    SERVICIOS_HOGAR,
    SALUD_FARMACIA,
    ENTRETENIMIENTO_SUSCRIPCIONES,
    EDUCACION,
    COMPRAS_SHOPPING,
    NOMINA_SUELDO,
    TRANSFERENCIA,
    PAGO_TARJETA,
    MSI_COMPRA,
    CUENTAS_POR_COBRAR,
    OTROS
}

val TransactionCategory.displayName: String
    get() = when (this) {
        TransactionCategory.ALIMENTOS_SUPER -> "Alimentos y Super"
        TransactionCategory.RESTAURANTES_DELIVERY -> "Restaurantes y Delivery"
        TransactionCategory.TRANSPORTE_GASOLINA -> "Transporte y Gasolina"
        TransactionCategory.SERVICIOS_HOGAR -> "Servicios del Hogar"
        TransactionCategory.SALUD_FARMACIA -> "Salud y Farmacia"
        TransactionCategory.ENTRETENIMIENTO_SUSCRIPCIONES -> "Entretenimiento"
        TransactionCategory.EDUCACION -> "Educación"
        TransactionCategory.COMPRAS_SHOPPING -> "Compras y Tiendas"
        TransactionCategory.NOMINA_SUELDO -> "Nómina y Sueldo"
        TransactionCategory.TRANSFERENCIA -> "Transferencia"
        TransactionCategory.PAGO_TARJETA -> "Pago de Tarjeta"
        TransactionCategory.MSI_COMPRA -> "Compra a MSI"
        TransactionCategory.CUENTAS_POR_COBRAR -> "Cuentas por Cobrar"
        TransactionCategory.OTROS -> "Otros"
    }

@Serializable
data class AccountPocket(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val amount: Double,
    val targetAmount: Double = 0.0,
    val note: String = ""
)

@Serializable
data class Account(
    val id: String,
    val name: String,
    val bankName: String,
    val type: AccountType,
    val balance: Double,
    val creditLimit: Double = 0.0,
    val colorHex: String = "#7C4DFF",
    val last4Digits: String = "",
    val pockets: List<AccountPocket> = emptyList(),
    val investmentAmount: Double = 0.0,
    val annualYieldRate: Double = 0.0
)

@Serializable
data class Transaction(
    val id: String,
    val accountId: String,
    val targetAccountId: String? = null,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val category: TransactionCategory,
    val timestamp: Long,
    val note: String = "",
    val rawNotificationText: String? = null,
    val isAutoDetected: Boolean = false,
    val bankName: String? = null,
    val cardId: String? = null
)

@Serializable
data class CreditCard(
    val id: String,
    val accountId: String? = null,
    val cardName: String,
    val bank: String,
    val creditLimit: Double,
    val currentDebt: Double,
    val cutOffDay: Int,        // Día del mes en que corta (ej. 10)
    val paymentDueDay: Int,    // Día límite de pago (ej. 30)
    val colorHex: String = "#FF1744",
    val last4Digits: String = "",
    val isDebit: Boolean = false
)

// Modelo para Compras a Meses Sin Intereses (MSI)
@Serializable
data class InstallmentPurchase(
    val id: String,
    val cardId: String,
    val title: String,
    val totalAmount: Double,
    val totalMonths: Int,          // 3, 6, 9, 12, 18, 24 MSI
    val currentMonthPaid: Int = 1,  // e.g. Mes 1 pagado
    val monthlyAmount: Double,     // totalAmount / totalMonths
    val purchaseDate: Long = System.currentTimeMillis(),
    val note: String = ""
)

@Serializable
data class BillReminder(
    val id: String,
    val title: String,
    val amount: Double,
    val dueDay: Int,           // Día del mes (1 a 31)
    val category: TransactionCategory,
    val isPaid: Boolean = false,
    val bankOrProvider: String = ""
)

@Serializable
enum class PayrollFrequency {
    QUINCENAL,    // Días 15 y último día del mes
    CATORCENAL,   // Cada dos semanas (viernes)
    SEMANAL,      // Cada semana
    MENSUAL,      // Fin de mes o día elegido
    PERSONALIZADO // Días específicos del mes (ej. día 5 y 20)
}

@Serializable
data class PayrollConfig(
    val frequency: PayrollFrequency = PayrollFrequency.QUINCENAL,
    val day1: Int = 15,
    val day2: Int = 0,         // 0 indica último día del mes (28/29/30/31)
    val customDays: List<Int> = listOf(15, 30),
    val expectedAmount: Double = 15000.0,
    val defaultAccountId: String? = null
)

@Serializable
data class NotificationLog(
    val id: String,
    val packageName: String,
    val title: String,
    val content: String,
    val timestamp: Long,
    val parsedSuccess: Boolean,
    val detectedBank: String? = null,
    val detectedType: TransactionType? = null,
    val detectedAmount: Double? = null,
    val detectedAccountLast4: String? = null,
    val failureReason: String? = null
)

// Desglose de conceptos para Cuentas por Cobrar
@Serializable
data class ReceivableBreakdownItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val concept: String,
    val amount: Double,
    val isPaid: Boolean = false,
    val paidDate: Long? = null
)

// Modelo para Cuentas por Cobrar (Dinero por recibir en el mes)
@Serializable
data class AccountReceivable(
    val id: String,
    val debtorName: String,
    val concept: String,
    val amount: Double,
    val initialAmount: Double = amount,
    val dueDayOfMonth: Int = 1,
    val dueDate: Long = System.currentTimeMillis(),
    val isCollected: Boolean = false,
    val collectedDate: Long? = null,
    val depositAccountId: String? = null,
    val note: String = "",
    val breakdown: List<ReceivableBreakdownItem> = emptyList()
)

// Modo de visualización de tarjetas en Patrimonio Neto
@Serializable
enum class CardNetWorthMode {
    DEBT,              // Resta la deuda actual (-$6,000)
    AVAILABLE_CREDIT   // Suma el crédito disponible (+$4,000)
}

// Modelo unificado de Copia de Seguridad Local
@Serializable
data class BackupData(
    val appVersion: String = "1.9",
    val backupDate: Long = System.currentTimeMillis(),
    val accounts: List<Account> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val creditCards: List<CreditCard> = emptyList(),
    val installmentPurchases: List<InstallmentPurchase> = emptyList(),
    val billReminders: List<BillReminder> = emptyList(),
    val payrollConfig: PayrollConfig = PayrollConfig(),
    val receivables: List<AccountReceivable> = emptyList(),
    val notificationLogs: List<NotificationLog> = emptyList()
)

