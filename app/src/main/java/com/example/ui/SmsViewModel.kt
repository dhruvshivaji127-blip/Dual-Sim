package com.example.ui

import android.app.Activity
import android.app.Application
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Telephony
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ConversationSummary
import com.example.data.FilterRuleEntity
import com.example.data.MessageEntity
import com.example.data.SimProfileEntity
import com.example.data.SmsRepository
import com.example.filtering.SmsFilterEngine
import com.example.telephony.DualSimManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    CONVERSATIONS,
    CHAT,
    FILTER_RULES,
    NOTIFICATIONS,
    SIM_SETTINGS
}

class SmsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SmsRepository
    private val context: Context get() = getApplication<Application>().applicationContext

    init {
        val db = AppDatabase.getDatabase(application)
        repository = SmsRepository(db.smsDao())
    }

    private val _currentScreen = MutableStateFlow(AppScreen.CONVERSATIONS)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // SIM Filter: -1 = All SIMs, 0 = SIM 1, 1 = SIM 2
    private val _simTab = MutableStateFlow(-1)
    val simTab: StateFlow<Int> = _simTab.asStateFlow()

    // Category Filter
    private val _categoryFilter = MutableStateFlow(MessageEntity.CATEGORY_ALL)
    val categoryFilter: StateFlow<String> = _categoryFilter.asStateFlow()

    // Search Query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Active Chat Thread
    private val _activeConversationSender = MutableStateFlow<String?>(null)
    val activeConversationSender: StateFlow<String?> = _activeConversationSender.asStateFlow()

    // Selected Sending SIM (0 or 1)
    private val _selectedSendingSimSlot = MutableStateFlow(0)
    val selectedSendingSimSlot: StateFlow<Int> = _selectedSendingSimSlot.asStateFlow()

    // Permissions & Default App status
    private val _isDefaultSmsApp = MutableStateFlow(false)
    val isDefaultSmsApp: StateFlow<Boolean> = _isDefaultSmsApp.asStateFlow()

    private val _hasSmsPermissions = MutableStateFlow(false)
    val hasSmsPermissions: StateFlow<Boolean> = _hasSmsPermissions.asStateFlow()

    val simProfiles: StateFlow<List<SimProfileEntity>> = repository.simProfilesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filterRules: StateFlow<List<FilterRuleEntity>> = repository.filterRulesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Conversations List
    @OptIn(ExperimentalCoroutinesApi::class)
    val conversations: StateFlow<List<ConversationSummary>> = combine(
        _simTab,
        _categoryFilter,
        _searchQuery
    ) { sim, category, search ->
        Triple(sim, category, search)
    }.flatMapLatest { (sim, category, search) ->
        repository.getConversationSummaries(
            simSlotFilter = sim,
            categoryFilter = category,
            searchQuery = search
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Conversation Messages
    @OptIn(ExperimentalCoroutinesApi::class)
    val activeConversationMessages: StateFlow<List<MessageEntity>> = _activeConversationSender
        .flatMapLatest { sender ->
            if (sender != null) {
                repository.getMessagesForConversation(sender)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun refreshStatus() {
        // Check default SMS app status
        val packageName = context.packageName
        val defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(context)
        _isDefaultSmsApp.value = (packageName == defaultSmsPackage)

        // Check permissions
        val hasReceive = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECEIVE_SMS
        ) == PackageManager.PERMISSION_GRANTED
        val hasSend = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED

        _hasSmsPermissions.value = (hasReceive && hasSend)

        // Sync hardware SIM cards
        syncHardwareSims()
    }

    fun syncHardwareSims() {
        viewModelScope.launch(Dispatchers.IO) {
            val subs = DualSimManager.getActiveSubscriptions(context)
            if (subs.isNotEmpty()) {
                val currentProfiles = repository.getSimProfilesSync()
                val updatedProfiles = currentProfiles.map { profile ->
                    val matchedSub = subs.firstOrNull { it.slotIndex == profile.slotIndex }
                    if (matchedSub != null) {
                        profile.copy(
                            subId = matchedSub.subscriptionId,
                            carrierName = matchedSub.carrierName
                        )
                    } else {
                        profile
                    }
                }
                repository.saveSimProfiles(updatedProfiles)
            }
        }
    }

    fun requestDefaultSmsApp(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = activity.getSystemService(RoleManager::class.java)
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_SMS)) {
                if (!roleManager.isRoleHeld(RoleManager.ROLE_SMS)) {
                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
                    activity.startActivity(intent)
                }
            }
        } else {
            val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT).apply {
                putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, activity.packageName)
            }
            activity.startActivity(intent)
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun setSimTab(slot: Int) {
        _simTab.value = slot
    }

    fun setCategoryFilter(category: String) {
        _categoryFilter.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun openConversation(sender: String) {
        _activeConversationSender.value = sender
        _currentScreen.value = AppScreen.CHAT
        viewModelScope.launch(Dispatchers.IO) {
            repository.markConversationAsRead(sender)
        }
    }

    fun closeConversation() {
        _activeConversationSender.value = null
        _currentScreen.value = AppScreen.CONVERSATIONS
    }

    fun setSelectedSendingSimSlot(slot: Int) {
        _selectedSendingSimSlot.value = slot
    }

    fun sendMessage(recipient: String, text: String, simSlot: Int) {
        if (recipient.isBlank() || text.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            // Find subscription ID for chosen SIM slot
            val profiles = repository.getSimProfilesSync()
            val chosenProfile = profiles.firstOrNull { it.slotIndex == simSlot }
            val subId = chosenProfile?.subId ?: -1

            // Attempt to send via Android SmsManager for subscription
            DualSimManager.sendSms(
                context = context,
                subscriptionId = subId,
                recipient = recipient,
                message = text
            )

            // Insert sent message into DB
            val messageEntity = MessageEntity(
                senderOrRecipient = recipient,
                contactName = null,
                body = text,
                timestamp = System.currentTimeMillis(),
                isIncoming = false,
                simSlotIndex = simSlot,
                subId = subId,
                category = MessageEntity.CATEGORY_PERSONAL,
                isRead = true,
                isSpam = false,
                isMuted = false,
                deliveryStatus = MessageEntity.STATUS_SENT
            )
            repository.insertMessage(messageEntity)
        }
    }

    fun deleteConversation(sender: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteConversation(sender)
            if (_activeConversationSender.value == sender) {
                closeConversation()
            }
        }
    }

    fun markConversationRead(sender: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.markConversationAsRead(sender)
        }
    }

    fun toggleFilterRule(rule: FilterRuleEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateFilterRule(rule.copy(isEnabled = !rule.isEnabled))
        }
    }

    fun addCustomFilterRule(
        name: String,
        pattern: String,
        matchField: String,
        isRegex: Boolean,
        targetCategory: String,
        autoMute: Boolean,
        moveToSpam: Boolean,
        assignSimSlot: Int
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val rule = FilterRuleEntity(
                ruleName = name,
                pattern = pattern,
                matchField = matchField,
                isRegex = isRegex,
                targetCategory = targetCategory,
                autoMute = autoMute,
                moveToSpam = moveToSpam,
                assignSimSlot = assignSimSlot,
                isEnabled = true,
                isSystemDefault = false
            )
            repository.insertFilterRule(rule)
        }
    }

    fun deleteFilterRule(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteFilterRule(id)
        }
    }

    fun updateSimProfile(profile: SimProfileEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateSimProfile(profile)
        }
    }

    fun testMessageClassification(sender: String, body: String, slot: Int): com.example.filtering.FilterResult {
        val rules = filterRules.value.filter { it.isEnabled }
        return SmsFilterEngine.evaluate(sender, body, slot, rules)
    }

    fun clearAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAll()
        }
    }

    fun populateDemoDualSimData() {
        viewModelScope.launch(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val oneMinute = 60 * 1000L
            val oneHour = 60 * oneMinute
            val oneDay = 24 * oneHour

            val demoMessages = listOf(
                // SIM 1 (Personal) - Friend conversation
                MessageEntity(
                    senderOrRecipient = "+1 (555) 234-5678",
                    contactName = "Sarah Jenkins",
                    body = "Hey! Are we still meeting for brunch tomorrow morning at 10 AM?",
                    timestamp = now - (15 * oneMinute),
                    isIncoming = true,
                    simSlotIndex = 0,
                    category = MessageEntity.CATEGORY_PERSONAL,
                    isRead = false,
                    deliveryStatus = MessageEntity.STATUS_RECEIVED
                ),
                MessageEntity(
                    senderOrRecipient = "+1 (555) 234-5678",
                    contactName = "Sarah Jenkins",
                    body = "Yes absolutely! Let's meet at Sunny Cafe.",
                    timestamp = now - (20 * oneMinute),
                    isIncoming = false,
                    simSlotIndex = 0,
                    category = MessageEntity.CATEGORY_PERSONAL,
                    isRead = true,
                    deliveryStatus = MessageEntity.STATUS_DELIVERED
                ),

                // SIM 2 (Work) - Team Lead message
                MessageEntity(
                    senderOrRecipient = "+1 (555) 876-5432",
                    contactName = "David Chen (Work Lead)",
                    body = "The client presentation was approved! Please review the Q3 slide deck before our 4 PM standup.",
                    timestamp = now - (35 * oneMinute),
                    isIncoming = true,
                    simSlotIndex = 1,
                    category = MessageEntity.CATEGORY_PERSONAL,
                    isRead = false,
                    deliveryStatus = MessageEntity.STATUS_RECEIVED
                ),

                // SIM 1 (Personal) - Banking Transaction Alert
                MessageEntity(
                    senderOrRecipient = "CHASE-ALERT",
                    contactName = "Chase Bank",
                    body = "Chase Alert: Your card ending in 4128 was debited for $42.50 at Whole Foods Market. Avail balance: $3,214.80. If not you, call 1-800-432-3117.",
                    timestamp = now - (2 * oneHour),
                    isIncoming = true,
                    simSlotIndex = 0,
                    category = MessageEntity.CATEGORY_TRANSACTIONS,
                    isRead = true,
                    deliveryStatus = MessageEntity.STATUS_RECEIVED
                ),

                // SIM 2 (Work) - IT VPN Authentication OTP
                MessageEntity(
                    senderOrRecipient = "CORP-SECURE",
                    contactName = "Enterprise VPN Portal",
                    body = "Your corporate VPN authentication code is 739218. Valid for 5 minutes. Do not share this OTP with anyone.",
                    timestamp = now - (10 * oneMinute),
                    isIncoming = true,
                    simSlotIndex = 1,
                    category = MessageEntity.CATEGORY_OTP,
                    otpCode = "739218",
                    isRead = false,
                    deliveryStatus = MessageEntity.STATUS_RECEIVED
                ),

                // SIM 1 (Personal) - Delivery Tracking Update
                MessageEntity(
                    senderOrRecipient = "AMAZON-PKG",
                    contactName = "Amazon Deliveries",
                    body = "Your package with Order #402-9812-4412 has been shipped and is scheduled for delivery today by 8 PM. Track at amzn.to/track",
                    timestamp = now - (4 * oneHour),
                    isIncoming = true,
                    simSlotIndex = 0,
                    category = MessageEntity.CATEGORY_UPDATES,
                    isRead = true,
                    deliveryStatus = MessageEntity.STATUS_RECEIVED
                ),

                // SIM 1 (Personal) - Promotional Discount (Auto-Muted)
                MessageEntity(
                    senderOrRecipient = "DOMINOS-DEAL",
                    contactName = "Domino's Deals",
                    body = "Mega Weekend Offer! Get flat 40% OFF on all large gourmet pizzas with coupon code FEAST40. Order now on the app!",
                    timestamp = now - (1 * oneDay),
                    isIncoming = true,
                    simSlotIndex = 0,
                    category = MessageEntity.CATEGORY_PROMOTIONS,
                    isRead = true,
                    isMuted = true,
                    deliveryStatus = MessageEntity.STATUS_RECEIVED
                ),

                // SIM 2 (Work) - Spam / Phishing Guard (Auto-Muted & Flagged)
                MessageEntity(
                    senderOrRecipient = "+1 (800) 999-0192",
                    contactName = "Unknown Suspicious",
                    body = "Urgent: Your payroll tax clearance is pending. Click http://bit.ly/tax-urgent to claim prize and update SSN.",
                    timestamp = now - (2 * oneDay),
                    isIncoming = true,
                    simSlotIndex = 1,
                    category = MessageEntity.CATEGORY_SPAM,
                    isRead = true,
                    isMuted = true,
                    isSpam = true,
                    deliveryStatus = MessageEntity.STATUS_RECEIVED
                )
            )

            repository.insertMessages(demoMessages)
        }
    }
}
