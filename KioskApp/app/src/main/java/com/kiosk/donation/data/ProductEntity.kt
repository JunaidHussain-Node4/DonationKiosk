package com.kiosk.donation.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val priceGBP: String,       // String to avoid floating point precision issues
    val emoji: String,
    val barcode: String?,       // Ready for future barcode scanner integration
    val imagePath: String?,     // Absolute path to image in app-private storage
    val sizesJson: String? = null
)

fun ProductEntity.toProduct(): Product {
    val sizes = if (!sizesJson.isNullOrBlank()) {
        try {
            val arr = JSONArray(sizesJson)
            List(arr.length()) { i ->
                val obj = arr.getJSONObject(i)
                ProductSize(
                    id       = obj.getString("id"),
                    name     = obj.getString("name"),
                    priceGBP = obj.get("price").toString().toBigDecimal(),
                    barcode  = if (obj.isNull("barcode")) null else obj.getString("barcode")
                )
            }
        } catch (e: Exception) {
            null
        }
    } else null

    return Product(
        id          = id,
        name        = name,
        description = description,
        priceGBP    = priceGBP.toBigDecimal(),
        emoji       = emoji,
        imageRes    = null,
        imagePath   = imagePath,
        barcode     = barcode,
        sizes       = sizes
    )
}
