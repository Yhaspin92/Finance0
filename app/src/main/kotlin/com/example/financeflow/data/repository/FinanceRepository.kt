package com.example.financeflow.data.repository

import android.content.Context
import com.example.financeflow.data.notification.BankParserEngine
import com.example.financeflow.domain.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID

class FinanceRepository private constructor(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }
    private val scope = CoroutineScope(Dispatchers.IO)

    private val accountsFile get() = File(context.filesDir, "ff_accounts.json")
    private val transactionsFile get() = File(context.filesDir, "ff_transactions.json")
    private val creditCardsFile get() = File(context.filesDir, "ff_credit_cards.json")
    private val billsFile get() = File(context.filesDir, "ff_bills.json")
    private val payrollFile get() = File(context.filesDir, "ff_payroll.json")
    private val notificationLogsFile get() = File(context.filesDir, "ff_notification_logs.json")
    private val installmentPurchasesFile get() = File(context.filesDir, "ff_installments.json")
    private val receivablesFile get() = File(context.filesDir, "ff_receivables.json")

    private val _accounts = MutableStateFlow<List<Account>>(emptyList())
    val accounts: StateFlow<List<Account>> = _accounts.asStateFlow()

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _creditCards = MutableStateFlow<List<CreditCard>>(emptyList())
    val creditCards: StateFlow<List<CreditCard>> = _creditCards.asStateFlow()

    private val _installmentPurchases = MutableStateFlow<List<InstallmentPurchase>>(emptyList())
    val installmentPurchases: StateFlow<List<InstallmentPurchase>> = _installmentPurchases.asStateFlow()

    private val _billReminders = MutableStateFlow<List<BillReminder>>(emptyList())
    val billReminders: StateFlow<List<BillReminder>> = _billReminders.asStateFlow()

    private val _payrollConfig = MutableStateFlow(PayrollConfig())
    val payrollConfig: StateFlow<PayrollConfig> = _payrollConfig.asStateFlow()

    private val _receivables = MutableStateFlow<List<AccountReceivable>>(emptyList())
    val receivables: StateFlow<List<AccountReceivable>> = _receivables.asStateFlow()

    private val _notificationLogs = MutableStateFlow<List<NotificationLog>>(emptyList())
    val notificationLogs: StateFlow<List<NotificationLog>> = _notificationLogs.asStateFlow()

    private val repoPrefs = context.getSharedPreferences("finance0_repo_prefs", Context.MODE_PRIVATE)
    private val PREF_HAS_INITIALIZED = "has_initialized_seed_data"
    private val PREF_CARD_NET_WORTH_MODE = "card_net_worth_mode"

    private val _cardNetWorthMode = MutableStateFlow(
        try {
            CardNetWorthMode.valueOf(repoPrefs.getString(PREF_CARD_NET_WORTH_MODE, CardNetWorthMode.DEBT.name) ?: CardNetWorthMode.DEBT.name)
        } catch (_: Exception) {
            CardNetWorthMode.DEBT
        }
    )
    val cardNetWorthMode: StateFlow<CardNetWorthMode> = _cardNetWorthMode.asStateFlow()

    fun setCardNetWorthMode(mode: CardNetWorthMode) {
        _cardNetWorthMode.value = mode
        repoPrefs.edit().putString(PREF_CARD_NET_WORTH_MODE, mode.name).apply()
    }

    init {
        loadData()
    }

    private fun loadData() {
        if (accountsFile.exists()) {
            try {
                val content = accountsFile.readText()
                if (content.isNotBlank()) {
                    _accounts.value = json.decodeFromString(content)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (transactionsFile.exists()) {
            try {
                val content = transactionsFile.readText()
                if (content.isNotBlank()) {
                    _transactions.value = json.decodeFromString(content)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (creditCardsFile.exists()) {
            try {
                val content = creditCardsFile.readText()
                if (content.isNotBlank()) {
                    _creditCards.value = json.decodeFromString(content)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (billsFile.exists()) {
            try {
                val content = billsFile.readText()
                if (content.isNotBlank()) {
                    _billReminders.value = json.decodeFromString(content)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (payrollFile.exists()) {
            try {
                val content = payrollFile.readText()
                if (content.isNotBlank()) {
                    _payrollConfig.value = json.decodeFromString(content)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (notificationLogsFile.exists()) {
            try {
                val content = notificationLogsFile.readText()
                if (content.isNotBlank()) {
                    _notificationLogs.value = json.decodeFromString(content)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (installmentPurchasesFile.exists()) {
            try {
                val content = installmentPurchasesFile.readText()
                if (content.isNotBlank()) {
                    _installmentPurchases.value = json.decodeFromString(content)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (receivablesFile.exists()) {
            try {
                val content = receivablesFile.readText()
                if (content.isNotBlank()) {
                    _receivables.value = json.decodeFromString(content)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Sembrar datos de ejemplo ÚNICAMENTE la primera vez que se abre la app en limpio.
        // NUNCA sobreescribir datos del usuario ni sembrar al actualizar versión.
        val hasSeededBefore = repoPrefs.getBoolean(PREF_HAS_INITIALIZED, false)
        val hasExistingFiles = accountsFile.exists() || transactionsFile.exists() || creditCardsFile.exists()

        if (!hasSeededBefore && !hasExistingFiles) {
            seedInitialData()
            repoPrefs.edit().putBoolean(PREF_HAS_INITIALIZED, true).apply()
        } else if (!hasSeededBefore) {
            repoPrefs.edit().putBoolean(PREF_HAS_INITIALIZED, true).apply()
        }
    }

    private fun saveData() {
        scope.launch {
            try {
                accountsFile.writeText(json.encodeToString(_accounts.value))
                transactionsFile.writeText(json.encodeToString(_transactions.value))
                creditCardsFile.writeText(json.encodeToString(_creditCards.value))
                billsFile.writeText(json.encodeToString(_billReminders.value))
                payrollFile.writeText(json.encodeToString(_payrollConfig.value))
                notificationLogsFile.writeText(json.encodeToString(_notificationLogs.value))
                installmentPurchasesFile.writeText(json.encodeToString(_installmentPurchases.value))
                receivablesFile.writeText(json.encodeToString(_receivables.value))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun seedInitialData() {
        val accBbva = Account(
            id = "acc_bbva_1",
            name = "BBVA Nómina & Ahorros",
            bankName = "BBVA México",
            type = AccountType.DEBIT,
            balance = 18450.00,
            colorHex = "#004481", // Azul BBVA
            last4Digits = "4821",
            pockets = listOf(
                AccountPocket(name = "Fondo Emergencia", amount = 8000.0, targetAmount = 20000.0),
                AccountPocket(name = "Vacaciones Cancún", amount = 4500.0, targetAmount = 10000.0)
            )
        )
        val accNuCredit = Account(
            id = "acc_nu_1",
            name = "Cuenta Nu & Cajita",
            bankName = "Nu México",
            type = AccountType.DEBIT,
            balance = 15200.00,
            creditLimit = 32000.00,
            colorHex = "#820AD1", // Morado Nu
            last4Digits = "9042",
            investmentAmount = 12000.0,
            annualYieldRate = 13.5
        )
        val accMp = Account(
            id = "acc_mp_1",
            name = "Mercado Pago GBM",
            bankName = "Mercado Pago",
            type = AccountType.DEBIT,
            balance = 4320.50,
            colorHex = "#009EE3", // Azul Mercado Pago
            last4Digits = "1182",
            investmentAmount = 3500.0,
            annualYieldRate = 10.4
        )
        val accCash = Account(
            id = "acc_cash_1",
            name = "Efectivo Físico",
            bankName = "Efectivo",
            type = AccountType.CASH,
            balance = 1200.00,
            colorHex = "#00E676",
            last4Digits = ""
        )

        _accounts.value = listOf(accBbva, accNuCredit, accMp, accCash)

        _creditCards.value = listOf(
            CreditCard(
                id = "card_nu_1",
                accountId = accNuCredit.id,
                cardName = "Nu Platinum Mastercard",
                bank = "Nu México",
                creditLimit = 32000.00,
                currentDebt = 4850.00,
                cutOffDay = 12,
                paymentDueDay = 2,
                colorHex = "#820AD1",
                last4Digits = "9042"
            )
        )

        val now = System.currentTimeMillis()
        val oneHour = 3600_000L
        val oneDay = 86400_000L

        _transactions.value = listOf(
            Transaction(
                id = UUID.randomUUID().toString(),
                accountId = accBbva.id,
                title = "Depósito de Nómina Quincenal",
                amount = 16500.00,
                type = TransactionType.INCOME,
                category = TransactionCategory.NOMINA_SUELDO,
                timestamp = now - (oneDay * 4),
                note = "Quincena depositada",
                rawNotificationText = "BBVA: Recibiste un depósito de nómina por $16,500.00 en tu cuenta *4821",
                isAutoDetected = true,
                bankName = "BBVA México"
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                accountId = accNuCredit.id,
                title = "Supermercado Walmart",
                amount = 1240.50,
                type = TransactionType.EXPENSE,
                category = TransactionCategory.ALIMENTOS_SUPER,
                timestamp = now - (oneDay * 2) - (oneHour * 3),
                note = "Despensa quincenal",
                rawNotificationText = "Nu: Compra autorizada por $1,240.50 en Walmart con tu tarjeta *9042",
                isAutoDetected = true,
                bankName = "Nu México"
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                accountId = accBbva.id,
                title = "Gasolina BP Insurgentes",
                amount = 650.00,
                type = TransactionType.EXPENSE,
                category = TransactionCategory.TRANSPORTE_GASOLINA,
                timestamp = now - (oneDay * 1),
                note = "Tanque medio",
                rawNotificationText = "BBVA: Cargo por $650.00 en Gasolinera BP con tarjeta débito *4821",
                isAutoDetected = true,
                bankName = "BBVA México"
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                accountId = accMp.id,
                title = "Rappi Cena",
                amount = 285.00,
                type = TransactionType.EXPENSE,
                category = TransactionCategory.RESTAURANTES_DELIVERY,
                timestamp = now - (oneHour * 5),
                note = "Cena fin de semana",
                rawNotificationText = "Mercado Pago: Pagaste $285.00 en Rappi",
                isAutoDetected = true,
                bankName = "Mercado Pago"
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                accountId = accBbva.id,
                targetAccountId = accNuCredit.id,
                title = "Pago Parcial Tarjeta Nu",
                amount = 2000.00,
                type = TransactionType.CREDIT_CARD_PAYMENT,
                category = TransactionCategory.PAGO_TARJETA,
                timestamp = now - (oneHour * 2),
                note = "Pago para liberar línea",
                isAutoDetected = false,
                bankName = "BBVA México"
            )
        )

        _billReminders.value = listOf(
            BillReminder(
                id = "bill_1",
                title = "Totalplay Internet Fibra",
                amount = 699.00,
                dueDay = 10,
                category = TransactionCategory.SERVICIOS_HOGAR,
                isPaid = true,
                bankOrProvider = "Totalplay"
            ),
            BillReminder(
                id = "bill_2",
                title = "Tarjeta Nu - Límite de Pago",
                amount = 4850.00,
                dueDay = 2,
                category = TransactionCategory.PAGO_TARJETA,
                isPaid = false,
                bankOrProvider = "Nu México"
            ),
            BillReminder(
                id = "bill_3",
                title = "CFE Electricidad",
                amount = 540.00,
                dueDay = 18,
                category = TransactionCategory.SERVICIOS_HOGAR,
                isPaid = false,
                bankOrProvider = "CFE"
            ),
            BillReminder(
                id = "bill_4",
                title = "Spotify Familiar",
                amount = 199.00,
                dueDay = 25,
                category = TransactionCategory.ENTRETENIMIENTO_SUSCRIPCIONES,
                isPaid = false,
                bankOrProvider = "Spotify"
            )
        )

        _payrollConfig.value = PayrollConfig(
            frequency = PayrollFrequency.QUINCENAL,
            day1 = 15,
            day2 = 0,
            expectedAmount = 16500.00,
            defaultAccountId = accBbva.id
        )

        _notificationLogs.value = listOf(
            NotificationLog(
                id = UUID.randomUUID().toString(),
                packageName = "com.bbva.bbvacontigo",
                title = "BBVA México",
                content = "Cargo por $650.00 en Gasolinera BP con tarjeta débito *4821",
                timestamp = now - (oneDay * 1),
                parsedSuccess = true,
                detectedBank = "BBVA México",
                detectedType = TransactionType.EXPENSE,
                detectedAmount = 650.00,
                detectedAccountLast4 = "4821"
            ),
            NotificationLog(
                id = UUID.randomUUID().toString(),
                packageName = "com.nu.production",
                title = "Nu",
                content = "Compra autorizada por $1,240.50 en Walmart con tu tarjeta *9042",
                timestamp = now - (oneDay * 2),
                parsedSuccess = true,
                detectedBank = "Nu México",
                detectedType = TransactionType.EXPENSE,
                detectedAmount = 1240.50,
                detectedAccountLast4 = "9042"
            )
        )

        _installmentPurchases.value = listOf(
            InstallmentPurchase(
                id = "msi_1",
                cardId = "card_nu_1",
                title = "MacBook Air M2",
                totalAmount = 24000.0,
                totalMonths = 12,
                currentMonthPaid = 4,
                monthlyAmount = 2000.0,
                purchaseDate = now - (oneDay * 90),
                note = "12 MSI tienda oficial"
            ),
            InstallmentPurchase(
                id = "msi_2",
                cardId = "card_nu_1",
                title = "Vuelos Vacaciones Cancún",
                totalAmount = 7200.0,
                totalMonths = 6,
                currentMonthPaid = 2,
                monthlyAmount = 1200.0,
                purchaseDate = now - (oneDay * 45),
                note = "6 MSI Aeroméxico"
            )
        )

        _receivables.value = listOf(
            AccountReceivable(
                id = "rec_1",
                debtorName = "Carlos Mendoza",
                concept = "Préstamo personal quincena",
                amount = 2500.00,
                dueDayOfMonth = 15,
                dueDate = now + (oneDay * 6),
                isCollected = false,
                note = "Prometió transferir al cobrar su nómina"
            ),
            AccountReceivable(
                id = "rec_2",
                debtorName = "Freelance Diseño Web",
                concept = "Anticipo de desarrollo UI",
                amount = 4800.00,
                dueDayOfMonth = 28,
                dueDate = now + (oneDay * 12),
                isCollected = false,
                note = "Entrega final pactada fin de mes"
            )
        )

        saveData()
    }

    fun addTransaction(
        accountId: String,
        targetAccountId: String? = null,
        title: String,
        amount: Double,
        type: TransactionType,
        category: TransactionCategory,
        timestamp: Long = System.currentTimeMillis(),
        note: String = "",
        rawNotificationText: String? = null,
        isAutoDetected: Boolean = false,
        bankName: String? = null,
        updateAccountBalance: Boolean = true
    ): Transaction {
        val tx = Transaction(
            id = UUID.randomUUID().toString(),
            accountId = accountId,
            targetAccountId = targetAccountId,
            title = title,
            amount = amount,
            type = type,
            category = category,
            timestamp = timestamp,
            note = note,
            rawNotificationText = rawNotificationText,
            isAutoDetected = isAutoDetected,
            bankName = bankName
        )

        if (updateAccountBalance) {
            // Actualizar saldos de cuentas
            val currentAccounts = _accounts.value.toMutableList()
            val sourceIndex = currentAccounts.indexOfFirst { it.id == accountId }

            when (type) {
                TransactionType.EXPENSE -> {
                    if (sourceIndex != -1) {
                        val acc = currentAccounts[sourceIndex]
                        val newBalance = if (acc.type == AccountType.CREDIT) {
                            acc.balance + amount // Más deuda
                        } else {
                            acc.balance - amount
                        }
                        currentAccounts[sourceIndex] = acc.copy(balance = newBalance)
                        updateCreditCardDebt(acc.id, newBalance)
                    }
                }
                TransactionType.INCOME -> {
                    if (sourceIndex != -1) {
                        val acc = currentAccounts[sourceIndex]
                        val newBalance = acc.balance + amount
                        currentAccounts[sourceIndex] = acc.copy(balance = newBalance)
                    }
                }
                TransactionType.TRANSFER -> {
                    if (sourceIndex != -1) {
                        val src = currentAccounts[sourceIndex]
                        currentAccounts[sourceIndex] = src.copy(balance = src.balance - amount)
                    }
                    if (targetAccountId != null) {
                        val targetIndex = currentAccounts.indexOfFirst { it.id == targetAccountId }
                        if (targetIndex != -1) {
                            val tgt = currentAccounts[targetIndex]
                            currentAccounts[targetIndex] = tgt.copy(balance = tgt.balance + amount)
                        }
                    }
                }
                TransactionType.CREDIT_CARD_PAYMENT -> {
                    // Se deduce del origen (débito) y se abona a la tarjeta
                    if (sourceIndex != -1) {
                        val src = currentAccounts[sourceIndex]
                        currentAccounts[sourceIndex] = src.copy(balance = src.balance - amount)
                    }
                    if (targetAccountId != null) {
                        val targetIndex = currentAccounts.indexOfFirst { it.id == targetAccountId }
                        if (targetIndex != -1) {
                            val tgt = currentAccounts[targetIndex]
                            val newDebt = (tgt.balance - amount).coerceAtLeast(0.0)
                            currentAccounts[targetIndex] = tgt.copy(balance = newDebt)
                            updateCreditCardDebt(tgt.id, newDebt)
                        }
                    }
                }
            }
            _accounts.value = currentAccounts
        }

        _transactions.value = listOf(tx) + _transactions.value
        saveData()
        return tx
    }

    private fun updateCreditCardDebt(accountId: String, debt: Double) {
        val cards = _creditCards.value.toMutableList()
        val index = cards.indexOfFirst { it.accountId == accountId }
        if (index != -1) {
            cards[index] = cards[index].copy(currentDebt = debt)
            _creditCards.value = cards
        }
    }

    fun processNotification(packageName: String, title: String, text: String): Boolean {
        val registeredAccounts = _accounts.value
        val registeredCards = _creditCards.value
        val registeredBankNames = (registeredAccounts.map { it.bankName } + registeredCards.map { it.bank })
            .filter { it.isNotBlank() }
            .distinct()

        val parsed = BankParserEngine.parse(packageName, title, text, registeredBankNames)
        val now = System.currentTimeMillis()

        if (parsed == null) {
            val log = NotificationLog(
                id = UUID.randomUUID().toString(),
                packageName = packageName,
                title = title,
                content = text,
                timestamp = now,
                parsedSuccess = false,
                failureReason = "No es una notificación de ningún banco reconocido o no contiene monto"
            )
            _notificationLogs.value = listOf(log) + _notificationLogs.value
            saveData()
            return false
        }

        // 1. Buscar cuenta coincidente por últimos 4 dígitos
        var matchedAccount = registeredAccounts.find { acc ->
            !parsed.accountLast4.isNullOrBlank() && acc.last4Digits == parsed.accountLast4
        }
        // 2. Si no coincide por last4, buscar por nombre de banco en cuentas
        if (matchedAccount == null) {
            matchedAccount = registeredAccounts.find { acc ->
                acc.bankName.equals(parsed.bankName, ignoreCase = true) ||
                (parsed.bankName.isNotBlank() && (acc.bankName.contains(parsed.bankName, ignoreCase = true) || parsed.bankName.contains(acc.bankName, ignoreCase = true)))
            }
        }
        // 3. Buscar si coincide con alguna tarjeta de crédito registrada
        val matchedCard = registeredCards.find { card ->
            (!parsed.accountLast4.isNullOrBlank() && card.last4Digits == parsed.accountLast4) ||
            card.bank.equals(parsed.bankName, ignoreCase = true) ||
            (parsed.bankName.isNotBlank() && (card.bank.contains(parsed.bankName, ignoreCase = true) || parsed.bankName.contains(card.bank, ignoreCase = true)))
        }
        if (matchedAccount == null && matchedCard != null) {
            matchedAccount = registeredAccounts.find { it.id == matchedCard.accountId }
                ?: registeredAccounts.firstOrNull { it.type == AccountType.CREDIT }
                ?: registeredAccounts.firstOrNull()
        }

        // SI NO COINCIDE CON NINGÚN BANCO O CUENTA DADA DE ALTA POR EL USUARIO:
        // Se descarta sin crear cuentas falsas o no deseadas
        if (matchedAccount == null && matchedCard == null) {
            val log = NotificationLog(
                id = UUID.randomUUID().toString(),
                packageName = packageName,
                title = title,
                content = text,
                timestamp = now,
                parsedSuccess = false,
                detectedBank = parsed.bankName,
                detectedAmount = parsed.amount,
                detectedAccountLast4 = parsed.accountLast4,
                failureReason = "El banco detectado (${parsed.bankName}) no está dado de alta en tus Cuentas Bancarias de Finance0."
            )
            _notificationLogs.value = listOf(log) + _notificationLogs.value
            saveData()
            return false
        }

        val targetAccount = matchedAccount ?: registeredAccounts.firstOrNull()
        if (targetAccount == null) {
            return false
        }

        // Registrar la transacción en la cuenta bancaria del usuario
        addTransaction(
            accountId = targetAccount.id,
            title = parsed.merchantOrConcept,
            amount = parsed.amount,
            type = parsed.type,
            category = parsed.category,
            timestamp = now,
            note = "Detectado automáticamente de notificación",
            rawNotificationText = parsed.rawText,
            isAutoDetected = true,
            bankName = parsed.bankName
        )

        val log = NotificationLog(
            id = UUID.randomUUID().toString(),
            packageName = packageName,
            title = title,
            content = text,
            timestamp = now,
            parsedSuccess = true,
            detectedBank = parsed.bankName,
            detectedType = parsed.type,
            detectedAmount = parsed.amount,
            detectedAccountLast4 = parsed.accountLast4
        )
        _notificationLogs.value = listOf(log) + _notificationLogs.value
        saveData()
        return true
    }

    fun addAccount(account: Account) {
        _accounts.value = _accounts.value + account
        saveData()
    }

    fun addCreditCard(card: CreditCard) {
        _creditCards.value = _creditCards.value + card
        saveData()
    }

    fun toggleBillPaid(billId: String) {
        _billReminders.value = _billReminders.value.map {
            if (it.id == billId) it.copy(isPaid = !it.isPaid) else it
        }
        saveData()
    }

    fun addBillReminder(bill: BillReminder) {
        _billReminders.value = _billReminders.value + bill
        saveData()
    }

    fun updatePayrollConfig(config: PayrollConfig) {
        _payrollConfig.value = config
        saveData()
    }

    fun deleteTransaction(txId: String) {
        _transactions.value = _transactions.value.filter { it.id != txId }
        saveData()
    }

    fun addInstallmentPurchase(
        cardId: String,
        title: String,
        totalAmount: Double,
        totalMonths: Int,
        note: String = ""
    ): InstallmentPurchase {
        val monthly = if (totalMonths > 0) totalAmount / totalMonths else totalAmount
        val item = InstallmentPurchase(
            id = UUID.randomUUID().toString(),
            cardId = cardId,
            title = title,
            totalAmount = totalAmount,
            totalMonths = totalMonths,
            currentMonthPaid = 1,
            monthlyAmount = monthly,
            purchaseDate = System.currentTimeMillis(),
            note = note
        )
        _installmentPurchases.value = listOf(item) + _installmentPurchases.value

        // Registrar en Movimientos Recientes para que aparezca cualquier cambio de cualquier rubro
        val defaultAcc = _accounts.value.firstOrNull { it.id == cardId } ?: _accounts.value.firstOrNull()
        addTransaction(
            accountId = defaultAcc?.id ?: cardId,
            title = "Compra MSI: $title ($totalMonths meses)",
            amount = totalAmount,
            type = TransactionType.EXPENSE,
            category = TransactionCategory.MSI_COMPRA,
            note = "Compra a meses sin intereses registrada. ${note}".trim(),
            bankName = "MSI",
            updateAccountBalance = false
        )

        saveData()
        return item
    }

    fun advanceInstallmentMonth(installmentId: String) {
        _installmentPurchases.value = _installmentPurchases.value.map {
            if (it.id == installmentId) {
                val nextMonth = (it.currentMonthPaid + 1).coerceAtMost(it.totalMonths)
                it.copy(currentMonthPaid = nextMonth)
            } else it
        }
        saveData()
    }

    fun deleteInstallmentPurchase(installmentId: String) {
        val target = _installmentPurchases.value.find { it.id == installmentId }
        _installmentPurchases.value = _installmentPurchases.value.filter { it.id != installmentId }
        if (target != null) {
            val defaultAcc = _accounts.value.firstOrNull { it.id == target.cardId } ?: _accounts.value.firstOrNull()
            addTransaction(
                accountId = defaultAcc?.id ?: target.cardId,
                title = "Cancelación MSI: ${target.title}",
                amount = target.totalAmount,
                type = TransactionType.INCOME,
                category = TransactionCategory.MSI_COMPRA,
                note = "Plan de MSI cancelado/liquidado.",
                bankName = "MSI",
                updateAccountBalance = false
            )
        }
        saveData()
    }

    // --- Módulo Cuentas Bancarias & Tarjetas ---

    fun addAccount(
        name: String,
        bankName: String,
        type: AccountType,
        balance: Double,
        last4Digits: String = "",
        colorHex: String = "#7C4DFF",
        investmentAmount: Double = 0.0,
        annualYieldRate: Double = 0.0
    ): Account {
        val newAcc = Account(
            id = UUID.randomUUID().toString(),
            name = name,
            bankName = bankName,
            type = type,
            balance = balance,
            last4Digits = last4Digits,
            colorHex = colorHex,
            investmentAmount = investmentAmount,
            annualYieldRate = annualYieldRate
        )
        _accounts.value = _accounts.value + newAcc
        saveData()
        return newAcc
    }

    fun updateAccount(
        accountId: String,
        name: String,
        bankName: String,
        type: AccountType,
        balance: Double,
        last4Digits: String = "",
        colorHex: String = "#7C4DFF",
        investmentAmount: Double = 0.0,
        annualYieldRate: Double = 0.0
    ) {
        _accounts.value = _accounts.value.map { acc ->
            if (acc.id == accountId) {
                acc.copy(
                    name = name,
                    bankName = bankName,
                    type = type,
                    balance = balance,
                    last4Digits = last4Digits,
                    colorHex = colorHex,
                    investmentAmount = investmentAmount,
                    annualYieldRate = annualYieldRate
                )
            } else acc
        }
        saveData()
    }

    fun deleteAccount(accountId: String) {
        val target = _accounts.value.find { it.id == accountId }
        _accounts.value = _accounts.value.filter { it.id != accountId }
        // Desvincular tarjetas asociadas a esta cuenta
        _creditCards.value = _creditCards.value.map { card ->
            if (card.accountId == accountId) card.copy(accountId = "") else card
        }
        if (target != null) {
            addTransaction(
                accountId = accountId,
                title = "Cuenta Eliminada: ${target.name}",
                amount = target.balance,
                type = TransactionType.EXPENSE,
                category = TransactionCategory.OTROS,
                note = "Cuenta bancaria eliminada por el usuario (${target.bankName}).",
                bankName = target.bankName,
                updateAccountBalance = false
            )
        }
        saveData()
    }

    fun addPocketToAccount(accountId: String, name: String, amount: Double, targetAmount: Double = 0.0) {
        _accounts.value = _accounts.value.map { acc ->
            if (acc.id == accountId) {
                val newPocket = AccountPocket(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    amount = amount,
                    targetAmount = targetAmount
                )
                acc.copy(pockets = acc.pockets + newPocket)
            } else acc
        }
        saveData()
    }

    fun addCreditCard(
        cardName: String,
        bank: String,
        last4Digits: String,
        creditLimit: Double,
        currentDebt: Double,
        cutOffDay: Int,
        paymentDueDay: Int,
        linkedAccountId: String?,
        isDebit: Boolean = false,
        colorHex: String = "#FF1744"
    ): CreditCard {
        val targetAccId = if (!linkedAccountId.isNullOrBlank()) {
            linkedAccountId
        } else {
            val newAcc = addAccount(
                name = cardName,
                bankName = bank,
                type = if (isDebit) AccountType.DEBIT else AccountType.CREDIT,
                balance = if (isDebit) 0.0 else -currentDebt,
                last4Digits = last4Digits,
                colorHex = colorHex
            )
            newAcc.id
        }

        val newCard = CreditCard(
            id = UUID.randomUUID().toString(),
            accountId = targetAccId,
            cardName = cardName,
            bank = bank,
            last4Digits = last4Digits,
            creditLimit = creditLimit,
            currentDebt = currentDebt,
            cutOffDay = cutOffDay,
            paymentDueDay = paymentDueDay,
            colorHex = colorHex,
            isDebit = isDebit
        )
        _creditCards.value = _creditCards.value + newCard
        saveData()
        return newCard
    }

    // --- Módulo Cuentas por Cobrar (Dinero por recibir en el mes) ---

    fun addReceivable(
        debtorName: String,
        concept: String,
        amount: Double,
        dueDayOfMonth: Int = 1,
        note: String = ""
    ): AccountReceivable {
        val newRec = AccountReceivable(
            id = UUID.randomUUID().toString(),
            debtorName = debtorName,
            concept = concept,
            amount = amount,
            initialAmount = amount,
            dueDayOfMonth = dueDayOfMonth,
            dueDate = System.currentTimeMillis(),
            isCollected = false,
            note = note
        )
        _receivables.value = _receivables.value + newRec

        // Registrar en Movimientos Recientes para que aparezca cualquier cambio de cualquier rubro
        val defaultAcc = _accounts.value.firstOrNull()
        addTransaction(
            accountId = defaultAcc?.id ?: "receivable_acc",
            title = "Por Cobrar: $debtorName - $concept",
            amount = amount,
            type = TransactionType.INCOME,
            category = TransactionCategory.CUENTAS_POR_COBRAR,
            note = "Cuenta por cobrar registrada. ${note}".trim(),
            bankName = "Por Cobrar",
            updateAccountBalance = false
        )

        saveData()
        return newRec
    }

    fun updateReceivable(
        receivableId: String,
        debtorName: String,
        concept: String,
        amount: Double,
        note: String
    ) {
        val target = _receivables.value.find { it.id == receivableId } ?: return
        val updated = target.copy(
            debtorName = debtorName,
            concept = concept,
            amount = amount,
            note = note
        )
        _receivables.value = _receivables.value.map { if (it.id == receivableId) updated else it }

        // Registrar en Movimientos Recientes
        val defaultAcc = _accounts.value.firstOrNull()
        addTransaction(
            accountId = defaultAcc?.id ?: "receivable_acc",
            title = "Edición Por Cobrar: $debtorName - $concept",
            amount = amount,
            type = TransactionType.INCOME,
            category = TransactionCategory.CUENTAS_POR_COBRAR,
            note = "Modificación de cuenta por cobrar. Anterior: ${target.debtorName} ($${target.amount})",
            bankName = "Por Cobrar",
            updateAccountBalance = false
        )

        saveData()
    }

    fun collectReceivable(
        receivableId: String,
        targetAccountId: String,
        amountToCollect: Double,
        isPartial: Boolean,
        breakdownItemId: String? = null
    ) {
        val target = _receivables.value.find { it.id == receivableId } ?: return
        val acc = _accounts.value.find { it.id == targetAccountId }
        val now = System.currentTimeMillis()

        if (isPartial) {
            val remaining = (target.amount - amountToCollect).coerceAtLeast(0.0)
            val isFullyCollected = remaining <= 0.001

            // Actualizar el concepto desglosado si fue seleccionado uno
            val updatedBreakdown = target.breakdown.map { item ->
                if (item.id == breakdownItemId) {
                    item.copy(isPaid = true, paidDate = now)
                } else item
            }

            val updated = target.copy(
                amount = remaining,
                isCollected = isFullyCollected,
                collectedDate = if (isFullyCollected) now else target.collectedDate,
                depositAccountId = targetAccountId,
                breakdown = updatedBreakdown
            )
            _receivables.value = _receivables.value.map { if (it.id == receivableId) updated else it }

            val itemPaid = target.breakdown.find { it.id == breakdownItemId }
            val txTitle = if (itemPaid != null) {
                "Abono [${itemPaid.concept}]: ${target.debtorName}"
            } else {
                "Abono Parcial: ${target.debtorName} (${target.concept})"
            }

            // Depositar monto parcial a la cuenta bancaria física
            addTransaction(
                accountId = targetAccountId,
                title = txTitle,
                amount = amountToCollect,
                type = TransactionType.INCOME,
                category = TransactionCategory.CUENTAS_POR_COBRAR,
                note = if (itemPaid != null) "Pago parcial por concepto '${itemPaid.concept}'. Saldo restante: $${remaining}"
                       else "Abono parcial a cuenta por cobrar. Saldo restante: $${remaining}",
                bankName = acc?.bankName,
                updateAccountBalance = true
            )
        } else {
            // Pago completo: marcar todos los conceptos del desglose como pagados
            val amountCollected = target.amount
            val updatedBreakdown = target.breakdown.map { it.copy(isPaid = true, paidDate = now) }
            val updated = target.copy(
                amount = 0.0,
                isCollected = true,
                collectedDate = now,
                depositAccountId = targetAccountId,
                breakdown = updatedBreakdown
            )
            _receivables.value = _receivables.value.map { if (it.id == receivableId) updated else it }

            // Depositar monto total a la cuenta bancaria física
            addTransaction(
                accountId = targetAccountId,
                title = "Cobro Completo: ${target.debtorName} (${target.concept})",
                amount = amountCollected,
                type = TransactionType.INCOME,
                category = TransactionCategory.CUENTAS_POR_COBRAR,
                note = "Cuenta por cobrar liquidada en su totalidad. ${target.note}".trim(),
                bankName = acc?.bankName,
                updateAccountBalance = true
            )
        }
        saveData()
    }

    fun markReceivableAsCollected(receivableId: String, targetAccountId: String) {
        val target = _receivables.value.find { it.id == receivableId } ?: return
        collectReceivable(receivableId, targetAccountId, target.amount, isPartial = false)
    }

    fun addBreakdownItem(receivableId: String, concept: String, amount: Double) {
        val target = _receivables.value.find { it.id == receivableId } ?: return
        val newItem = ReceivableBreakdownItem(
            id = UUID.randomUUID().toString(),
            concept = concept,
            amount = amount
        )
        val updated = target.copy(breakdown = target.breakdown + newItem)
        _receivables.value = _receivables.value.map { if (it.id == receivableId) updated else it }
        saveData()
    }

    fun updateBreakdownItem(receivableId: String, itemId: String, concept: String, amount: Double) {
        val target = _receivables.value.find { it.id == receivableId } ?: return
        val updatedBreakdown = target.breakdown.map {
            if (it.id == itemId) it.copy(concept = concept, amount = amount) else it
        }
        val updated = target.copy(breakdown = updatedBreakdown)
        _receivables.value = _receivables.value.map { if (it.id == receivableId) updated else it }
        saveData()
    }

    fun removeBreakdownItem(receivableId: String, itemId: String) {
        val target = _receivables.value.find { it.id == receivableId } ?: return
        val updated = target.copy(breakdown = target.breakdown.filter { it.id != itemId })
        _receivables.value = _receivables.value.map { if (it.id == receivableId) updated else it }
        saveData()
    }

    fun deleteReceivable(receivableId: String) {
        val target = _receivables.value.find { it.id == receivableId }
        _receivables.value = _receivables.value.filter { it.id != receivableId }
        if (target != null) {
            val defaultAcc = _accounts.value.firstOrNull()
            addTransaction(
                accountId = defaultAcc?.id ?: "receivable_acc",
                title = "Cancelado Por Cobrar: ${target.debtorName} - ${target.concept}",
                amount = target.amount,
                type = TransactionType.EXPENSE,
                category = TransactionCategory.CUENTAS_POR_COBRAR,
                note = "Cuenta por cobrar cancelada / eliminada.",
                bankName = "Por Cobrar",
                updateAccountBalance = false
            )
        }
        saveData()
    }

    // --- Módulo Respaldo y Restauración Local ---

    fun createBackupJson(): String {
        val backup = BackupData(
            appVersion = "1.7",
            backupDate = System.currentTimeMillis(),
            accounts = _accounts.value,
            transactions = _transactions.value,
            creditCards = _creditCards.value,
            installmentPurchases = _installmentPurchases.value,
            billReminders = _billReminders.value,
            payrollConfig = _payrollConfig.value,
            receivables = _receivables.value,
            notificationLogs = _notificationLogs.value
        )
        return json.encodeToString(backup)
    }

    fun restoreFromBackupJson(jsonString: String): Result<String> {
        return try {
            val backup = json.decodeFromString<BackupData>(jsonString)
            _accounts.value = backup.accounts
            _transactions.value = backup.transactions
            _creditCards.value = backup.creditCards
            _installmentPurchases.value = backup.installmentPurchases
            _billReminders.value = backup.billReminders
            _payrollConfig.value = backup.payrollConfig
            _receivables.value = backup.receivables
            _notificationLogs.value = backup.notificationLogs
            saveData()
            Result.success("Respaldo restaurado con éxito: ${backup.accounts.size} cuentas, ${backup.transactions.size} movimientos y ${backup.receivables.size} cuentas por cobrar.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun executeMasterReset() {
        try {
            if (accountsFile.exists()) accountsFile.delete()
            if (transactionsFile.exists()) transactionsFile.delete()
            if (creditCardsFile.exists()) creditCardsFile.delete()
            if (billsFile.exists()) billsFile.delete()
            if (payrollFile.exists()) payrollFile.delete()
            if (notificationLogsFile.exists()) notificationLogsFile.delete()
            if (installmentPurchasesFile.exists()) installmentPurchasesFile.delete()
            if (receivablesFile.exists()) receivablesFile.delete()

            _accounts.value = emptyList()
            _transactions.value = emptyList()
            _creditCards.value = emptyList()
            _installmentPurchases.value = emptyList()
            _billReminders.value = emptyList()
            _notificationLogs.value = emptyList()
            _receivables.value = emptyList()
            _payrollConfig.value = PayrollConfig()

            saveData()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: FinanceRepository? = null

        fun getInstance(context: Context): FinanceRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FinanceRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
