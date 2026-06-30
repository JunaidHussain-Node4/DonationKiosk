package com.kiosk.donation.data

import java.math.BigDecimal

data class ProductSize(
    val id: String,
    val name: String,
    val priceGBP: BigDecimal,
    val barcode: String? = null
)

data class Category(
    val id: String,
    val name: String,
    val emoji: String,
    val imagePath: String? = null
)

data class Product(
    val id: String,
    val name: String,
    val description: String,
    val priceGBP: BigDecimal,
    val emoji: String,
    val categoryId: String? = null,
    val imageRes: String?  = null,  // Drawable resource name (built-in images)
    val imagePath: String? = null,  // Absolute path to image synced from Firebase
    val barcode: String?   = null,  // EAN/UPC barcode for future scanner integration
    val sizes: List<ProductSize>? = null
)

/** Preset donation amounts shown on the donation screen. */
val DONATION_PRESETS = listOf(
    BigDecimal("5.00"),
    BigDecimal("10.00"),
    BigDecimal("20.00"),
    BigDecimal("30.00"),
    BigDecimal("50.00"),
    BigDecimal("100.00"),
)

/**
 * Fallback products shown only when no products have been synced from Firebase yet.
 * Once Firebase sync runs successfully, these are replaced by the synced products.
 */
val defaultProducts = listOf(
    Product("p1", "Badr Hoodie Grey",          "Karima Grey Badr Hoodie",          BigDecimal("25.00"), "📛", "greybadrhoodie"),
    Product("p2", "Badr T-Shirt Grey",         "Karima Grey Badr T-Shirt",         BigDecimal("15.00"), "👜", "greybadrtshirt"),
    Product("p3", "Badr Hoodie Maroon",        "Karima Maroon Badr Hoodie",        BigDecimal("25.00"), "💌", "maroonbadrhoodie"),
    Product("p4", "Badr Hoodie Maroon Zipped", "Karima Maroon Badr Hoodie Zipped", BigDecimal("30.00"), "📌", "maroonbadrhoodiezipped"),
    Product("p5", "Badr Hoodie Green",         "Karima Green Badr Hoodie",         BigDecimal("25.00"), "🔵", "greenbadrhoodie"),
    Product("p6", "Scouts Hoodie",             "Karima Scouts Hoodie",             BigDecimal("25.00"), "🎁", "scoutshoodie"),
)

data class CartItem(
    val product: Product,
    val quantity: Int,
    val selectedSize: ProductSize? = null
)

sealed class PaymentMode {
    data class Donation(val amountGBP: BigDecimal) : PaymentMode()
    data class ProductPurchase(val items: List<CartItem>) : PaymentMode()
}
