package com.example.financeflow.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.financeflow.data.notification.BankNotificationListenerService
import com.example.financeflow.data.repository.FinanceRepository
import com.example.financeflow.data.updater.GitHubUpdateManager
import com.example.financeflow.data.updater.UpdateState
import com.example.financeflow.domain.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FinanceRepository.getInstance(application)
    val updateManager = GitHubUpdateManager(application)

    val accounts: StateFlow<List<Account>> = repository.accounts
    val transactions: StateFlow<List<Transaction>> = repository.transactions
    val creditCards: StateFlow<List<CreditCard>> = repository.creditCards
    val installmentPurchases: StateFlow<List<InstallmentPurchase>> = repository.installmentPurchases
    val billReminders: StateFlow<List<BillReminder>> = repository.billReminders
    val payrollConfig: StateFlow<PayrollConfig> = repository.payrollConfig
    val notificationLogs: StateFlow<List<NotificationLog>> = repository.notificationLogs

    private val themePrefs = application.getSharedPreferences("finance0_theme_prefs", android.content.Context.MODE_PRIVATE)
    private val initialTheme = try {
        val saved = themePrefs.getString("theme_mode", com.example.financeflow.ui.theme.AppThemeMode.EMERALD_VIOLET.name)
        com.example.financeflow.ui.theme.AppThemeMode.valueOf(saved ?: com.example.financeflow.ui.theme.AppThemeMode.EMERALD_VIOLET.name)
    } catch (e: Exception) {
        com.example.financeflow.ui.theme.AppThemeMode.EMERALD_VIOLET
    }

    private val _themeMode = MutableStateFlow(initialTheme)
    val themeMode: StateFlow<com.example.financeflow.ui.theme.AppThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: com.example.financeflow.ui.theme.AppThemeMode) {
        _themeMode.value = mode
        themePrefs.edit().putString("theme_mode", mode.name).apply()
    }

    fun executeMasterReset() {
        repository.executeMasterReset()
    }

    private val _isNotificationPermissionGranted = MutableStateFlow(false)
    val isNotificationPermissionGranted: StateFlow<Boolean> = _isNotificationPermissionGranted.asStateFlow()

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    // Resumen financiero calculado dinámicamente
    val netWorth: StateFlow<Double> = accounts.combine(creditCards) { accList, cardsList ->
        val assets = accList.filter { it.type != AccountType.CREDIT }.sumOf { it.balance }
        val liabilities = cardsList.sumOf { it.currentDebt }
        assets - liabilities
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val monthlyIncome: StateFlow<Double> = transactions.combine(accounts) { txList, _ ->
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val cal = Calendar.getInstance()

        txList.filter { tx ->
            cal.timeInMillis = tx.timestamp
            cal.get(Calendar.MONTH) == currentMonth &&
            cal.get(Calendar.YEAR) == currentYear &&
            tx.type == TransactionType.INCOME
        }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val monthlyExpense: StateFlow<Double> = transactions.combine(accounts) { txList, _ ->
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val cal = Calendar.getInstance()

        txList.filter { tx ->
            cal.timeInMillis = tx.timestamp
            cal.get(Calendar.MONTH) == currentMonth &&
            cal.get(Calendar.YEAR) == currentYear &&
            (tx.type == TransactionType.EXPENSE || tx.type == TransactionType.CREDIT_CARD_PAYMENT)
        }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    init {
        checkPermission()
    }

    fun checkPermission() {
        _isNotificationPermissionGranted.value =
            BankNotificationListenerService.isNotificationAccessGranted(getApplication())
    }

    fun addManualTransaction(
        accountId: String,
        targetAccountId: String? = null,
        title: String,
        amount: Double,
        type: TransactionType,
        category: TransactionCategory,
        note: String = ""
    ) {
        val account = accounts.value.find { it.id == accountId }
        repository.addTransaction(
            accountId = accountId,
            targetAccountId = targetAccountId,
            title = title,
            amount = amount,
            type = type,
            category = category,
            note = note,
            isAutoDetected = false,
            bankName = account?.bankName
        )
    }

    fun payCreditCard(
        sourceAccountId: String,
        creditCardId: String,
        amount: Double,
        note: String = "Pago a tarjeta"
    ) {
        val card = creditCards.value.find { it.id == creditCardId } ?: return
        repository.addTransaction(
            accountId = sourceAccountId,
            targetAccountId = card.accountId,
            title = "Pago ${card.cardName}",
            amount = amount,
            type = TransactionType.CREDIT_CARD_PAYMENT,
            category = TransactionCategory.PAGO_TARJETA,
            note = note,
            isAutoDetected = false,
            bankName = card.bank
        )
    }

    fun addInstallmentPurchase(
        cardId: String,
        title: String,
        totalAmount: Double,
        totalMonths: Int,
        note: String = ""
    ) {
        repository.addInstallmentPurchase(
            cardId = cardId,
            title = title,
            totalAmount = totalAmount,
            totalMonths = totalMonths,
            note = note
        )
    }

    fun advanceInstallmentMonth(installmentId: String) {
        repository.advanceInstallmentMonth(installmentId)
    }

    fun deleteInstallmentPurchase(installmentId: String) {
        repository.deleteInstallmentPurchase(installmentId)
    }

    fun simulateNotification(packageName: String, title: String, text: String): Boolean {
        return repository.processNotification(packageName, title, text)
    }

    fun toggleBillPaid(billId: String) {
        repository.toggleBillPaid(billId)
    }

    fun addBillReminder(
        title: String,
        amount: Double,
        dueDay: Int,
        category: TransactionCategory,
        provider: String
    ) {
        repository.addBillReminder(
            BillReminder(
                id = java.util.UUID.randomUUID().toString(),
                title = title,
                amount = amount,
                dueDay = dueDay,
                category = category,
                isPaid = false,
                bankOrProvider = provider
            )
        )
    }

    fun updatePayroll(frequency: PayrollFrequency, day1: Int, day2: Int, amount: Double) {
        repository.updatePayrollConfig(
            PayrollConfig(
                frequency = frequency,
                day1 = day1,
                day2 = day2,
                expectedAmount = amount
            )
        )
    }

    fun deleteTransaction(txId: String) {
        repository.deleteTransaction(txId)
    }

    // OTA Updater vía GitHub
    fun checkForUpdates(
        repo: String = updateManager.configuredRepo,
        token: String = updateManager.personalAccessToken
    ) {
        viewModelScope.launch {
            _updateState.value = UpdateState.Checking
            val result = updateManager.checkForUpdates(repo, token)
            _updateState.value = result
        }
    }

    fun downloadAndInstallApk(
        downloadUrl: String,
        token: String = updateManager.personalAccessToken
    ) {
        viewModelScope.launch {
            _updateState.value = UpdateState.Downloading(0)
            val file = updateManager.downloadAndInstallApk(downloadUrl, token) { progress ->
                _updateState.value = UpdateState.Downloading(progress)
            }
            if (file != null) {
                _updateState.value = UpdateState.ReadyToInstall(file)
                val opened = updateManager.promptInstall(file)
                if (!opened) {
                    _updateState.value = UpdateState.NeedsInstallPermission(file)
                }
            } else {
                _updateState.value = UpdateState.Error("Error al descargar el archivo APK desde GitHub.")
            }
        }
    }

    fun promptInstallApk(file: File) {
        val opened = updateManager.promptInstall(file)
        if (!opened) {
            _updateState.value = UpdateState.NeedsInstallPermission(file)
        }
    }
}
