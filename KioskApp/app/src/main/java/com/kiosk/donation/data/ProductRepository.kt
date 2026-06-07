package com.kiosk.donation.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * Repository that manages product data.
 *
 * Products are stored in two places:
 *  - Firestore: source of truth, managed via the web admin UI
 *  - Room (local): cached copy used by the app, survives offline
 *
 * Images are stored as Base64 strings in Firestore and saved as files
 * in app-private storage after sync, so they display quickly without
 * re-decoding Base64 on every render.
 *
 * Firestore collection structure:
 *   products/{productId}
 *     id:          string
 *     name:        string
 *     description: string
 *     price:       string  (e.g. "25.00")
 *     emoji:       string  (fallback if no image)
 *     barcode:     string  (optional, for future scanner)
 *     imageBase64: string  (optional, base64-encoded image)
 */
class ProductRepository(private val context: Context) {

    private val db      = KioskDatabase.getInstance(context)
    private val dao     = db.productDao()
    private val imageDir get() = File(context.filesDir, "product_images").also { it.mkdirs() }

    /** Live stream of products from local Room database */
    val products: Flow<List<Product>> = dao.getAllProducts().map { entities ->
        entities.map { it.toProduct() }
    }

    /** Returns true if there are any products in the local database */
    suspend fun hasProducts(): Boolean = dao.count() > 0

    /**
     * Sync products from Firestore to local Room database.
     * Downloads all product data including images.
     * Returns a [SyncResult] describing what happened.
     */
    suspend fun syncFromFirestore(): SyncResult = withContext(Dispatchers.IO) {
        try {
            val snapshot = Firebase.firestore
                .collection("products")
                .get()
                .await()

            if (snapshot.isEmpty) {
                return@withContext SyncResult.Error(
                    "No products found in Firebase. " +
                    "Add products using the web admin tool first."
                )
            }

            val entities = mutableListOf<ProductEntity>()
            val errors   = mutableListOf<String>()

            snapshot.documents.forEach { doc ->
                try {
                    val id          = doc.getString("id")   ?: doc.id
                    val name        = doc.getString("name") ?: return@forEach
                    val description = doc.getString("description") ?: ""
                    val price       = doc.getString("price") ?: "0.00"
                    val emoji       = doc.getString("emoji") ?: "🛍️"
                    val barcode     = doc.getString("barcode")
                    val imageBase64 = doc.getString("imageBase64")

                    // Extract sizes list and convert to JSON for local storage
                    val sizesJson = (doc.get("sizes") as? List<*>)?.let { list ->
                        val arr = JSONArray()
                        list.forEach { item ->
                            (item as? Map<*, *>)?.let { map ->
                                val sId    = map["id"]?.toString()
                                val sName  = map["name"]?.toString()
                                val sPrice = map["price"]?.toString()
                                if (sId != null && sName != null && sPrice != null) {
                                    val obj = JSONObject()
                                    obj.put("id",      sId)
                                    obj.put("name",    sName)
                                    obj.put("price",   sPrice)
                                    obj.put("barcode", map["barcode"]?.toString())
                                    arr.put(obj)
                                }
                            }
                        }
                        if (arr.length() > 0) arr.toString() else null
                    }

                    // Save image to app-private storage if present
                    val imagePath = imageBase64?.let {
                        saveBase64Image(it, id)
                    }

                    entities.add(
                        ProductEntity(
                            id          = id,
                            name        = name,
                            description = description,
                            priceGBP    = price,
                            emoji       = emoji,
                            barcode     = barcode,
                            imagePath   = imagePath,
                            sizesJson   = sizesJson
                        )
                    )
                } catch (e: Exception) {
                    errors.add("Skipped '${doc.id}': ${e.message}")
                }
            }

            // Replace all local products with synced data
            dao.deleteAll()
            dao.insertAll(entities)

            SyncResult.Success(
                count  = entities.size,
                errors = errors
            )

        } catch (e: Exception) {
            SyncResult.Error("Sync failed: ${e.message}")
        }
    }

    /**
     * Decode a Base64 image string and save it as a JPEG file in app-private storage.
     * Returns the absolute file path, or null if decoding fails.
     * Compresses to max 800px wide to keep storage usage reasonable.
     */
    private fun saveBase64Image(base64: String, productId: String): String? {
        return try {
            // Strip data URI prefix if present (e.g. "data:image/jpeg;base64,")
            val clean = if (base64.contains(",")) base64.substringAfter(",") else base64
            val bytes = Base64.decode(clean, Base64.DEFAULT)

            // Decode and compress
            val original = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                ?: return null

            val scaled = if (original.width > 800) {
                val ratio  = 800f / original.width
                Bitmap.createScaledBitmap(
                    original,
                    800,
                    (original.height * ratio).toInt(),
                    true
                )
            } else original

            val file = File(imageDir, "$productId.jpg")
            file.outputStream().use { out ->
                scaled.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }

            if (scaled != original) scaled.recycle()

            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }
}

sealed class SyncResult {
    data class Success(val count: Int, val errors: List<String> = emptyList()) : SyncResult()
    data class Error(val message: String) : SyncResult()
}
