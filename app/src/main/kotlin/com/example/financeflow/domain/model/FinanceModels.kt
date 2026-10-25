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
    OTROS
}

@Serializable
data class Account(
    val id: String,
    val name: String,
    val bankName: String,
    val type: AccountType,
    val balance: Double,
    val creditLimit: Double = 0.0,
    val colorHex: String = "#7C4DFF",
    val last4Digits: String = ""
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
    val bankName: String? = null
)

@Serializable
data class CreditCard(
    val id: String,
    val accountId: String,
    val cardName: String,
    val bank: String,
    val creditLimit: Double,
    val currentDebt: Double,
    val cutOffDay: Int,        // Día del mes en que corta (ej. 10)
    val paymentDueDay: Int,    // Día límite de pago (ej. 30)
    val colorHex: String = "#FF1744",
    val last4Digits: String = ""
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
    QUINCENAL,  // Días 15 y último día del mes
    CATORCENAL, // Cada dos semanas (viernes)
    SEMANAL,    // Cada semana
    MENSUAL     // Fin de mes
}

@Serializable
data class PayrollConfig(
    val frequency: PayrollFrequency = PayrollFrequency.QUINCENAL,
    val day1: Int = 15,
    val day2: Int = 0,         // 0 indica último día del mes (28/29/30/31)
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
