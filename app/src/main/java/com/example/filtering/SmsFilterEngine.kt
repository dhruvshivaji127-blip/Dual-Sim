package com.example.filtering

import com.example.data.FilterRuleEntity
import com.example.data.MessageEntity
import java.util.regex.Pattern

data class FilterResult(
    val category: String,
    val isSpam: Boolean,
    val isMuted: Boolean,
    val assignedSimSlot: Int,
    val extractedOtp: String? = null,
    val matchedRuleName: String? = null
)

object SmsFilterEngine {

    // Regex for OTP extraction
    private val OTP_PATTERN = Pattern.compile(
        "(?i)(?:code|otp|passcode|pin|secret|verification code)\\s*(?:is|:|-)?\\s*([0-9]{4,8})|([0-9]{4,8})\\s*(?:is your (?:verification|otp|login|security) code)",
        Pattern.CASE_INSENSITIVE
    )

    private val SIMPLE_CODE_PATTERN = Pattern.compile("\\b([0-9]{4,8})\\b")

    // Default heuristic regexes
    private val OTP_KEYWORDS = Pattern.compile("(?i)\\b(otp|one time password|verification code|security code|login code|authorization code)\\b")
    private val TRANSACTION_KEYWORDS = Pattern.compile("(?i)\\b(debited|credited|ac |a/c|balance|atm|txn|transaction|vpa|upi|bank|inr|usd|eur|stmt|withdrawn|deposited)\\b")
    private val PROMO_KEYWORDS = Pattern.compile("(?i)\\b(offer|sale|discount|off%|coupon|cashback|promo|voucher|deal|limited time|hurry|shop now|flat \\d+%)\\b")
    private val UPDATE_KEYWORDS = Pattern.compile("(?i)\\b(shipped|out for delivery|arriving|tracking|order id|flight|booking|scheduled|appointment|rescheduled|delivered)\\b")
    private val SPAM_KEYWORDS = Pattern.compile("(?i)\\b(claim prize|won lottery|click http|verify your card now|urgent account suspended|congratulations you won|claim cash)\\b")

    fun evaluate(
        sender: String,
        body: String,
        currentSimSlot: Int,
        activeRules: List<FilterRuleEntity>
    ): FilterResult {
        val extractedOtp = extractOtp(body)

        // 1. Check custom user rules
        for (rule in activeRules) {
            val contentToMatch = if (rule.matchField == FilterRuleEntity.FIELD_SENDER) sender else body
            val isMatch = if (rule.isRegex) {
                try {
                    Pattern.compile(rule.pattern, Pattern.CASE_INSENSITIVE).matcher(contentToMatch).find()
                } catch (e: Exception) {
                    contentToMatch.contains(rule.pattern, ignoreCase = true)
                }
            } else {
                contentToMatch.contains(rule.pattern, ignoreCase = true)
            }

            if (isMatch) {
                val assignedSlot = if (rule.assignSimSlot in 0..1) rule.assignSimSlot else currentSimSlot
                return FilterResult(
                    category = rule.targetCategory,
                    isSpam = rule.moveToSpam || rule.targetCategory == MessageEntity.CATEGORY_SPAM,
                    isMuted = rule.autoMute,
                    assignedSimSlot = assignedSlot,
                    extractedOtp = extractedOtp,
                    matchedRuleName = rule.ruleName
                )
            }
        }

        // 2. Default heuristics classification
        if (SPAM_KEYWORDS.matcher(body).find()) {
            return FilterResult(
                category = MessageEntity.CATEGORY_SPAM,
                isSpam = true,
                isMuted = true,
                assignedSimSlot = currentSimSlot,
                extractedOtp = extractedOtp,
                matchedRuleName = "Spam Heuristic Guard"
            )
        }

        if (extractedOtp != null || OTP_KEYWORDS.matcher(body).find()) {
            return FilterResult(
                category = MessageEntity.CATEGORY_OTP,
                isSpam = false,
                isMuted = false,
                assignedSimSlot = currentSimSlot,
                extractedOtp = extractedOtp,
                matchedRuleName = "Smart OTP Detector"
            )
        }

        if (TRANSACTION_KEYWORDS.matcher(body).find()) {
            return FilterResult(
                category = MessageEntity.CATEGORY_TRANSACTIONS,
                isSpam = false,
                isMuted = false,
                assignedSimSlot = currentSimSlot,
                extractedOtp = null,
                matchedRuleName = "Transaction Classifier"
            )
        }

        if (UPDATE_KEYWORDS.matcher(body).find()) {
            return FilterResult(
                category = MessageEntity.CATEGORY_UPDATES,
                isSpam = false,
                isMuted = false,
                assignedSimSlot = currentSimSlot,
                extractedOtp = null,
                matchedRuleName = "Service & Updates"
            )
        }

        if (PROMO_KEYWORDS.matcher(body).find()) {
            return FilterResult(
                category = MessageEntity.CATEGORY_PROMOTIONS,
                isSpam = false,
                isMuted = true,
                assignedSimSlot = currentSimSlot,
                extractedOtp = null,
                matchedRuleName = "Promotions & Offers"
            )
        }

        // Default to personal message
        return FilterResult(
            category = MessageEntity.CATEGORY_PERSONAL,
            isSpam = false,
            isMuted = false,
            assignedSimSlot = currentSimSlot,
            extractedOtp = null,
            matchedRuleName = null
        )
    }

    fun extractOtp(body: String): String? {
        if (!OTP_KEYWORDS.matcher(body).find() && !body.contains("code", ignoreCase = true) && !body.contains("pin", ignoreCase = true)) {
            return null
        }

        val matcher = OTP_PATTERN.matcher(body)
        if (matcher.find()) {
            val group1 = matcher.group(1)
            val group2 = matcher.group(2)
            return group1 ?: group2
        }

        // Fallback: search for numbers with 4 to 8 digits
        val simpleMatcher = SIMPLE_CODE_PATTERN.matcher(body)
        if (simpleMatcher.find()) {
            return simpleMatcher.group(1)
        }

        return null
    }
}
