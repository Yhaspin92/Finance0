package com.example.financeflow.data.notification

import com.example.financeflow.domain.model.TransactionCategory
import com.example.financeflow.domain.model.TransactionType
import java.util.regex.Pattern

data class ParsedBankNotification(
    val bankName: String,
    val amount: Double,
    val type: TransactionType,
    val category: TransactionCategory,
    val accountLast4: String?,
    val merchantOrConcept: String,
    val rawText: String,
    val confidence: Float
)

object BankParserEngine {

    private val BANK_PACKAGE_MAP = mapOf(
        "com.bbva.bbvacontigo" to "BBVA México",
        "com.bbva.mexico" to "BBVA México",
        "com.nu.production" to "Nu México",
        "com.mercadopago.wallet" to "Mercado Pago",
        "com.mercadolibre" to "Mercado Pago",
        "com.santander.supermovilmx" to "Santander",
        "com.santander.superwalletmx" to "Santander",
        "com.citibanamex.banamexmovil" to "Citibanamex",
        "com.banorte.mimovil" to "Banorte",
        "com.heybanco" to "Hey Banco",
        "com.banregio" to "Banregio",
        "com.bancoazteca.appmovil" to "Banco Azteca",
        "mx.hsbc.hsbcmexico" to "HSBC México",
        "com.scotiabank.scotiamovilmx" to "Scotiabank",
        "com.spin.oxxo" to "Spin by OXXO",
        "com.klar" to "Klar",
        "com.rappicard" to "RappiCard",
        "com.stori" to "Stori",
        "com.storicard.stori" to "Stori",
        "com.uala.mexico" to "Ualá",
        "com.revolut.revolut" to "Revolut México",
        "mx.plata.card" to "Plata Card",
        "com.payclip.clip" to "Clip",
        "com.bancouno.app" to "UNO",
        "com.novacard.app" to "Novacard"
    )

    private val AMOUNT_REGEX = Pattern.compile(
        """(?:\$|MXN\s*|\bMN\s*|\bpesos\s*)?\s*([0-9]{1,3}(?:,[0-9]{3})*(?:\.[0-9]{1,2})?|[0-9]+(?:\.[0-9]{1,2})?)\s*(?:MXN|MN|pesos)?""",
        Pattern.CASE_INSENSITIVE
    )

    private val CARD_LAST4_REGEX = Pattern.compile(
        """(?:\*|terminada?\s+en\s+|cuenta\s+|tarjeta\s+|terminación\s+|#)?\s*([0-9]{4})\b""",
        Pattern.CASE_INSENSITIVE
    )

    fun parse(packageName: String, title: String, text: String): ParsedBankNotification? {
        val fullContent = "$title $text".trim()
        if (fullContent.isBlank()) return null

        val detectedBank = detectBank(packageName, fullContent)
        val extractedAmount = extractAmount(fullContent) ?: return null
        val detectedType = detectType(fullContent)
        val last4 = extractLast4Digits(fullContent)
        val category = detectCategory(fullContent, detectedType)
        val merchant = extractMerchant(fullContent, detectedType)

        return ParsedBankNotification(
            bankName = detectedBank,
            amount = extractedAmount,
            type = detectedType,
            category = category,
            accountLast4 = last4,
            merchantOrConcept = merchant,
            rawText = fullContent,
            confidence = if (detectedBank != "Banco / Fintech") 0.95f else 0.75f
        )
    }

    private fun detectBank(packageName: String, content: String): String {
        BANK_PACKAGE_MAP[packageName.lowercase()]?.let { return it }

        val lower = content.lowercase()
        return when {
            lower.contains("bbva") || lower.contains("bancomer") -> "BBVA México"
            lower.contains("nu ") || lower.contains("nubank") || lower.contains("cuenta nu") -> "Nu México"
            lower.contains("mercado pago") || lower.contains("mercadopago") -> "Mercado Pago"
            lower.contains("santander") -> "Santander"
            lower.contains("banamex") || lower.contains("citibanamex") || lower.contains("bancanet") -> "Citibanamex"
            lower.contains("banorte") -> "Banorte"
            lower.contains("hey banco") || lower.contains("heybanco") -> "Hey Banco"
            lower.contains("banregio") -> "Banregio"
            lower.contains("banco azteca") || lower.contains("baz") -> "Banco Azteca"
            lower.contains("hsbc") -> "HSBC México"
            lower.contains("scotiabank") -> "Scotiabank"
            lower.contains("spin by oxxo") || lower.contains("spin") -> "Spin by OXXO"
            lower.contains("klar") -> "Klar"
            lower.contains("rappicard") || lower.contains("rappi") -> "RappiCard"
            lower.contains("revolut") -> "Revolut México"
            lower.contains("stori") -> "Stori"
            lower.contains("plata card") || lower.contains("plata") -> "Plata Card"
            lower.contains("banco uno") || lower.contains("financiera uno") || lower.contains("tarjeta uno") -> "UNO"
            lower.contains("clip") -> "Clip"
            lower.contains("novacard") -> "Novacard"
            lower.contains("lenda") -> "Lenda"
            lower.contains("arq") -> "ARQ"
            lower.contains("inbursa") -> "Inbursa"
            lower.contains("uala") || lower.contains("ualá") -> "Ualá"
            else -> "Banco / Fintech"
        }
    }

    private fun extractAmount(content: String): Double? {
        // Buscamos patrones de montos precedidos de signos de pesos o palabras clave
        val specificPattern = Pattern.compile(
            """(?:\$|por\s+\$|de\s+\$|monto\s+de\s+\$|cantidad\s+de\s+\$)\s*([0-9]{1,3}(?:,[0-9]{3})*(?:\.[0-9]{1,2})?|[0-9]+(?:\.[0-9]{1,2})?)""",
            Pattern.CASE_INSENSITIVE
        )
        val matcher = specificPattern.matcher(content)
        if (matcher.find()) {
            val rawNum = matcher.group(1)?.replace(",", "")
            return rawNum?.toDoubleOrNull()
        }

        // Búsqueda genérica de montos numéricos con decimales
        val generalMatcher = AMOUNT_REGEX.matcher(content)
        while (generalMatcher.find()) {
            val rawNum = generalMatcher.group(1)?.replace(",", "")
            val parsed = rawNum?.toDoubleOrNull()
            if (parsed != null && parsed > 0.0 && parsed < 1_000_000_000.0) {
                return parsed
            }
        }
        return null
    }

    private fun detectType(content: String): TransactionType {
        val lower = content.lowercase()
        return when {
            lower.contains("pago de tarjeta") || lower.contains("pago a tu tarjeta") || lower.contains("pago recibido en tu tarjeta") -> {
                TransactionType.CREDIT_CARD_PAYMENT
            }
            lower.contains("transferiste") || lower.contains("transferencia enviada") || lower.contains("spei enviado") -> {
                TransactionType.TRANSFER
            }
            lower.contains("depósito") || lower.contains("deposito") || lower.contains("abono") ||
            lower.contains("recibiste") || lower.contains("te transfirieron") || lower.contains("nómina") ||
            lower.contains("nomina") || lower.contains("abono a cuenta") -> {
                TransactionType.INCOME
            }
            lower.contains("compra") || lower.contains("cargo") || lower.contains("retiro") ||
            lower.contains("pagaste") || lower.contains("consumo") || lower.contains("autorizada") ||
            lower.contains("disposición") || lower.contains("cargo a tu") -> {
                TransactionType.EXPENSE
            }
            else -> TransactionType.EXPENSE
        }
    }

    private fun extractLast4Digits(content: String): String? {
        val matcher = CARD_LAST4_REGEX.matcher(content)
        if (matcher.find()) {
            return matcher.group(1)
        }
        return null
    }

    private fun detectCategory(content: String, type: TransactionType): TransactionCategory {
        val lower = content.lowercase()
        if (type == TransactionType.CREDIT_CARD_PAYMENT) return TransactionCategory.PAGO_TARJETA
        if (type == TransactionType.TRANSFER) return TransactionCategory.TRANSFERENCIA
        if (lower.contains("nómina") || lower.contains("nomina") || lower.contains("sueldo") || lower.contains("quincena")) {
            return TransactionCategory.NOMINA_SUELDO
        }

        return when {
            lower.contains("uber") || lower.contains("didi") || lower.contains("gasolina") ||
            lower.contains("gasolinera") || lower.contains("shell") || lower.contains("oxxo gas") ||
            lower.contains("peaje") || lower.contains("estacionamiento") -> {
                TransactionCategory.TRANSPORTE_GASOLINA
            }
            lower.contains("walmart") || lower.contains("soriana") || lower.contains("costco") ||
            lower.contains("chedraui") || lower.contains("oxxo") || lower.contains("7-eleven") ||
            lower.contains("super") || lower.contains("bodega aurrera") -> {
                TransactionCategory.ALIMENTOS_SUPER
            }
            lower.contains("rappi") || lower.contains("uber eats") || lower.contains("didi food") ||
            lower.contains("starbucks") || lower.contains("restaurante") || lower.contains("mcdonalds") ||
            lower.contains("burger") || lower.contains("tacos") || lower.contains("pizza") || lower.contains("café") -> {
                TransactionCategory.RESTAURANTES_DELIVERY
            }
            lower.contains("netflix") || lower.contains("spotify") || lower.contains("prime") ||
            lower.contains("disney") || lower.contains("cine") || lower.contains("cinemex") ||
            lower.contains("cinepolis") || lower.contains("hbo") || lower.contains("youtube") -> {
                TransactionCategory.ENTRETENIMIENTO_SUSCRIPCIONES
            }
            lower.contains("farmacia") || lower.contains("guadalajara") || lower.contains("benavides") ||
            lower.contains("san pablo") || lower.contains("médico") || lower.contains("hospital") ||
            lower.contains("salud") || lower.contains("laboratorio") -> {
                TransactionCategory.SALUD_FARMACIA
            }
            lower.contains("cfe") || lower.contains("luz") || lower.contains("telmex") ||
            lower.contains("totalplay") || lower.contains("izzi") || lower.contains("agua") ||
            lower.contains("gas natural") || lower.contains("megacable") -> {
                TransactionCategory.SERVICIOS_HOGAR
            }
            lower.contains("amazon") || lower.contains("mercado libre") || lower.contains("shein") ||
            lower.contains("zara") || lower.contains("liverpool") || lower.contains("palacio de hierro") -> {
                TransactionCategory.COMPRAS_SHOPPING
            }
            else -> TransactionCategory.OTROS
        }
    }

    private fun extractMerchant(content: String, type: TransactionType): String {
        // Intentar buscar "en <Comercio>"
        val enPattern = Pattern.compile("""(?:\ben\s+|\bde\s+)([A-Z0-9a-zÁÉÍÓÚáéíóúÑñ\.\s]{3,25})(?:\s+por|\s+con|\s+el|\s+a\s+las|$)""")
        val m = enPattern.matcher(content)
        if (m.find()) {
            val merchant = m.group(1)?.trim()
            if (!merchant.isNullOrBlank() && merchant.length in 3..25) {
                return merchant
            }
        }

        return when (type) {
            TransactionType.EXPENSE -> "Cargo bancario"
            TransactionType.INCOME -> "Abono / Depósito"
            TransactionType.TRANSFER -> "Transferencia bancaria"
            TransactionType.CREDIT_CARD_PAYMENT -> "Pago de tarjeta"
        }
    }
}
