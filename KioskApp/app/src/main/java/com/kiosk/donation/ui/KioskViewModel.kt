package com.kiosk.donation.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kiosk.donation.data.*
import com.kiosk.donation.util.ConnectivityObserver
import com.kiosk.donation.util.NetworkConnectivityObserver
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal

class KioskViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs      = AdminPreferences(application)
    private val repository = ProductRepository(application)
    private val connectivityObserver = NetworkConnectivityObserver(application)

    // ── Connectivity ──────────────────────────────────────────────────────────
    val connectivityStatus: StateFlow<ConnectivityObserver.Status> = connectivityObserver.observe()
        .stateIn(viewModelScope, SharingStarted.Eagerly, ConnectivityObserver.Status.Available)

    val isOnline: StateFlow<Boolean> = connectivityStatus
        .map { it == ConnectivityObserver.Status.Available }
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    // ── Settings ──────────────────────────────────────────────────────────────
    val orgName: StateFlow<String> = prefs.orgName
        .stateIn(viewModelScope, SharingStarted.Eagerly, "Our Charity")

    val sumupAffiliateKey: StateFlow<String> = prefs.sumupAffiliateKey
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    val adminPin: StateFlow<String> = prefs.adminPin
        .stateIn(viewModelScope, SharingStarted.Eagerly, AdminPreferences.DEFAULT_PIN)

    val isDonationsEnabled: StateFlow<Boolean> = prefs.isDonationsEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val isProductsEnabled: StateFlow<Boolean> = prefs.isProductsEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val basketTimeoutMinutes: StateFlow<Int> = prefs.basketTimeoutMinutes
        .stateIn(viewModelScope, SharingStarted.Eagerly, 10)

    // ── Products — from Room database (synced from Firebase) ──────────────────
    // Falls back to defaultProducts if database is empty (before first sync)
    val products: StateFlow<List<Product>> = repository.products
        .map { dbProducts -> dbProducts.ifEmpty { defaultProducts } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, defaultProducts)

    // ── Sync state ────────────────────────────────────────────────────────────
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState

    fun syncProducts() {
        viewModelScope.launch {
            _syncState.value = SyncState.Syncing
            _syncState.value = when (val result = repository.syncFromFirestore()) {
                is SyncResult.Success -> SyncState.Success(
                    "Synced ${result.count} products successfully." +
                    if (result.errors.isNotEmpty()) " (${result.errors.size} skipped)" else ""
                )
                is SyncResult.Error -> SyncState.Failed(result.message)
            }
        }
    }

    fun clearSyncState() {
        _syncState.value = SyncState.Idle
    }

    // ── Payment state ─────────────────────────────────────────────────────────
    private val _pendingPayment = MutableStateFlow<PaymentMode?>(null)
    val pendingPayment: StateFlow<PaymentMode?> = _pendingPayment

    // ── Cart ──────────────────────────────────────────────────────────────────
    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart

    val cartTotal: StateFlow<BigDecimal> = _cart.map { items ->
        items.fold(BigDecimal.ZERO) { acc, item ->
            val price = item.selectedSize?.priceGBP ?: item.product.priceGBP
            acc + price * BigDecimal(item.quantity)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, BigDecimal.ZERO)

    private var lastActivityTime = System.currentTimeMillis()

    init {
        // Inactivity timer to clear cart
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(30000) // Check every 30 seconds
                val timeoutMs = basketTimeoutMinutes.value * 60 * 1000L
                if (_cart.value.isNotEmpty() && System.currentTimeMillis() - lastActivityTime > timeoutMs) {
                    clearCart()
                }
            }
        }
    }

    fun updateActivity() {
        lastActivityTime = System.currentTimeMillis()
    }

    fun addToCart(product: Product, size: ProductSize? = null) {
        updateActivity()
        _cart.update { current ->
            val existing = current.find { it.product.id == product.id && it.selectedSize?.id == size?.id }
            if (existing != null) {
                current.map { 
                    if (it.product.id == product.id && it.selectedSize?.id == size?.id) 
                        it.copy(quantity = it.quantity + 1) 
                    else it 
                }
            } else {
                current + CartItem(product, 1, size)
            }
        }
    }

    fun removeFromCart(product: Product, size: ProductSize? = null) {
        updateActivity()
        _cart.update { current ->
            val existing = current.find { it.product.id == product.id && it.selectedSize?.id == size?.id }
            if (existing != null && existing.quantity > 1) {
                current.map { 
                    if (it.product.id == product.id && it.selectedSize?.id == size?.id) 
                        it.copy(quantity = it.quantity - 1) 
                    else it 
                }
            } else {
                current.filter { it.product.id != product.id || it.selectedSize?.id != size?.id }
            }
        }
    }

    fun clearCart() { 
        _cart.value = emptyList() 
    }

    fun initiateDonation(amount: BigDecimal) {
        _pendingPayment.value = PaymentMode.Donation(amount)
    }

    fun initiateProductPurchase() {
        _pendingPayment.value = PaymentMode.ProductPurchase(_cart.value)
    }

    fun clearPayment() { _pendingPayment.value = null }

    // ── Admin settings ────────────────────────────────────────────────────────
    fun saveAdminPin(pin: String)    = viewModelScope.launch { prefs.setAdminPin(pin) }
    fun saveSumupKey(key: String)    = viewModelScope.launch { prefs.setSumupAffiliateKey(key) }
    fun saveOrgName(name: String)    = viewModelScope.launch { prefs.setOrgName(name) }
    fun setDonationsEnabled(enabled: Boolean) = viewModelScope.launch { prefs.setDonationsEnabled(enabled) }
    fun setProductsEnabled(enabled: Boolean)  = viewModelScope.launch { prefs.setProductsEnabled(enabled) }
    fun setBasketTimeout(minutes: Int)       = viewModelScope.launch { prefs.setBasketTimeout(minutes) }

    private val _loginRequested = MutableStateFlow(false)
    val loginRequested: StateFlow<Boolean> = _loginRequested

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    init {
        // Initial check
        _isLoggedIn.value = SumUpManager.isLoggedIn()
    }

    fun sumupLogin()      { _loginRequested.value = true }
    fun onLoginHandled()  { 
        _loginRequested.value = false
        _isLoggedIn.value = SumUpManager.isLoggedIn()
    }
    fun sumupLogout() {
        SumUpManager.logout()
        _isLoggedIn.value = false
    }
}

sealed class SyncState {
    object Idle                         : SyncState()
    object Syncing                      : SyncState()
    data class Success(val message: String) : SyncState()
    data class Failed(val message: String)  : SyncState()
}
