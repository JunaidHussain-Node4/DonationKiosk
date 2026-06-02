package com.kiosk.donation.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val priceGBP: String,       // String to avoid floating point precision issues
    val emoji: String,
    val barcode: String?,       // Ready for future barcode scanner integration
    val imagePath: String?      // Absolute path to image in app-private storage
)

fun ProductEntity.toProduct() = Product(
    id          = id,
    name        = name,
    description = description,
    priceGBP    = priceGBP.toBigDecimal(),
    emoji       = emoji,
    imageRes    = null,
    imagePath   = imagePath,
    barcode     = barcode
)
