package com.kiosk.donation.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kiosk.donation.R
import com.kiosk.donation.data.CartItem
import com.kiosk.donation.data.Product
import com.kiosk.donation.ui.theme.*
import java.io.File
import java.math.BigDecimal

/**
 * Maps drawable resource name strings to their R.drawable integer IDs.
 * This replaces getIdentifier() which is discouraged in production builds.
 * Add any new built-in product drawables here.
 */
private val drawableMap = mapOf(
    "greybadrhoodie"        to R.drawable.greybadrhoodie,
    "greybadrtshirt"        to R.drawable.greybadrtshirt,
    "maroonbadrhoodie"      to R.drawable.maroonbadrhoodie,
    "maroonbadrhoodiezipped" to R.drawable.maroonbadrhoodiezipped,
    "greenbadrhoodie"       to R.drawable.greenbadrhoodie,
    "scoutshoodie"          to R.drawable.scoutshoodie,
)

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ProductScreen(
    products: List<Product>,
    cart: List<CartItem>,
    cartTotal: BigDecimal,
    onBack: () -> Unit,
    onAddToCart: (Product) -> Unit,
    onRemoveFromCart: (Product) -> Unit,
    onClearCart: () -> Unit,
    onCheckout: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredProducts = remember(products, searchQuery) {
        if (searchQuery.isBlank()) {
            products
        } else {
            products.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.description.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // Outer BoxWithConstraints — use scope.maxHeight / scope.maxWidth
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
    ) {
        val screenHeight    = maxHeight   // accessed via BoxWithConstraintsScope
        val screenWidth     = maxWidth
        val cartHasItems    = cart.isNotEmpty()
        
        // Proportional sizing matching DonationScreen
        val donateHeight: Dp = screenHeight * 0.11f
        val hPad:         Dp = screenWidth  * 0.04f
        val sectionGap:   Dp = screenHeight * 0.025f

        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top bar ───────────────────────────────────────────────────────
            Surface(color = KarimaDark, shadowElevation = 4.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text       = "Shop",
                        fontSize   = (screenWidth.value * 0.038f).sp,
                        color      = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier   = Modifier.weight(1f)
                    )

                    // Search Field
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .weight(1.5f)
                            .padding(horizontal = 8.dp)
                            .height(56.dp), // Increased height slightly more
                        placeholder = { 
                            Text(
                                "Search products...", 
                                fontSize = (screenWidth.value * 0.026f).sp,
                                color = Color.White.copy(alpha = 0.8f)
                            ) 
                        },
                        leadingIcon = { 
                            Icon(
                                Icons.Default.Search, 
                                contentDescription = null, 
                                modifier = Modifier.size(22.dp),
                                tint = Color.White
                            ) 
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White.copy(alpha = 0.25f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.2f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White
                        ),
                        shape = MaterialTheme.shapes.small,
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = (screenWidth.value * 0.026f).sp,
                            color = Color.White,
                            lineHeight = (screenWidth.value * 0.032f).sp // Added line height to allow for descenders
                        )
                    )

                    Spacer(Modifier.width(8.dp))

                    if (cartHasItems) {
                        IconButton(onClick = onClearCart) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Clear Basket",
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                    }

                    BadgedBox(badge = {
                        val count = cart.sumOf { it.quantity }
                        if (count > 0) Badge(containerColor = Amber, contentColor = TextDark) {
                            Text("$count", fontWeight = FontWeight.Bold)
                        }
                    }) {
                        Icon(
                            Icons.Filled.ShoppingCart,
                            contentDescription = "Cart",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            // ── Product grid — inner BoxWithConstraints for card height ────────
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                val cardHeight: Dp = maxHeight * 0.47f   // scope accessed correctly here

                if (filteredProducts.isEmpty() && products.isNotEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "No products match your search",
                            color = TextMedium,
                            fontSize = (screenWidth.value * 0.03f).sp
                        )
                    }
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement   = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredProducts) { product ->
                        val inCart = cart.find { it.product.id == product.id }?.quantity ?: 0
                        ProductCard(
                            product        = product,
                            quantityInCart = inCart,
                            screenWidth    = screenWidth.value,
                            cardHeight     = cardHeight,
                            onAdd          = { onAddToCart(product) },
                            onRemove       = { onRemoveFromCart(product) }
                        )
                    }
                }
            }

            // ── Cart panel ────────────────────────────────────────────────────
            Surface(
                modifier        = Modifier.fillMaxWidth().weight(0.35f),
                color           = SurfaceWhite,
                shadowElevation = 8.dp,
                tonalElevation  = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = hPad)
                ) {
                    // Cart details area
                    Box(modifier = Modifier.weight(1f)) {
                        CartDetails(
                            cart        = cart,
                            total       = cartTotal,
                            screenWidth = screenWidth.value
                        )
                    }

                    // Pay Button — positioned at the bottom like DonationScreen
                    Button(
                        onClick        = onCheckout,
                        enabled        = cartHasItems,
                        colors         = ButtonDefaults.buttonColors(containerColor = Amber, contentColor = TextDark),
                        shape          = MaterialTheme.shapes.large,
                        contentPadding = PaddingValues(0.dp),
                        modifier       = Modifier
                            .fillMaxWidth()
                            .height(donateHeight)
                            .padding(bottom = sectionGap)
                    ) {
                        Icon(Icons.Filled.ShoppingCart, null, modifier = Modifier.size(donateHeight * 0.4f))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (cartHasItems) "Pay £${"%.2f".format(cartTotal)}" else "Your cart is empty",
                            fontSize = (screenWidth.value * 0.030f).sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductCard(
    product: Product,
    quantityInCart: Int,
    screenWidth: Float,
    cardHeight: Dp,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth().height(cardHeight),
        shape     = MaterialTheme.shapes.medium,
        colors    = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier            = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier            = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                // Image priority: imagePath (Firebase) → imageRes (drawable map) → emoji
                when {
                    product.imagePath != null -> {
                        // Image downloaded from Firebase — loaded from file via Coil
                        AsyncImage(
                            model              = File(product.imagePath),
                            contentDescription = product.name,
                            contentScale       = ContentScale.Fit,
                            modifier           = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(4.dp)
                        )
                    }
                    product.imageRes != null -> {
                        // Built-in drawable — looked up via the drawableMap (no reflection)
                        val resId = drawableMap[product.imageRes]
                        if (resId != null) {
                            Image(
                                painter            = painterResource(id = resId),
                                contentDescription = product.name,
                                contentScale       = ContentScale.Fit,
                                modifier           = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(4.dp)
                            )
                        } else {
                            Text(product.emoji, fontSize = (screenWidth * 0.07f).sp)
                            Spacer(Modifier.height(4.dp))
                        }
                    }
                    else -> {
                        Text(product.emoji, fontSize = (screenWidth * 0.07f).sp)
                        Spacer(Modifier.height(4.dp))
                    }
                }

                Text(
                    text       = product.name,
                    fontSize   = (screenWidth * 0.030f).sp,
                    fontWeight = FontWeight.Bold,
                    textAlign  = TextAlign.Center,
                    color      = TextDark,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis
                )
                Text(
                    text      = product.description,
                    fontSize  = (screenWidth * 0.022f).sp,
                    textAlign = TextAlign.Center,
                    color     = TextMedium,
                    maxLines  = 1,
                    overflow  = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text       = "£${"%.2f".format(product.priceGBP)}",
                    fontSize   = (screenWidth * 0.034f).sp,
                    fontWeight = FontWeight.Bold,
                    color      = TextDark
                )
            }

            // Add / remove controls — always pinned to bottom of card
            if (quantityInCart == 0) {
                Button(
                    onClick  = onAdd,
                    colors   = ButtonDefaults.buttonColors(containerColor = KarimaGreen),
                    shape    = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add", fontSize = (screenWidth * 0.025f).sp)
                }
            } else {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier              = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick  = onRemove,
                        modifier = Modifier.background(DividerColor, MaterialTheme.shapes.small)
                    ) {
                        Icon(Icons.Filled.Remove, contentDescription = "Remove", tint = ErrorRed)
                    }
                    Text(
                        text       = "$quantityInCart",
                        fontSize   = (screenWidth * 0.038f).sp,
                        fontWeight = FontWeight.Bold,
                        color      = TextDark
                    )
                    IconButton(
                        onClick  = onAdd,
                        modifier = Modifier.background(KarimaGreen, MaterialTheme.shapes.small)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Add", tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun CartDetails(
    cart: List<CartItem>,
    total: BigDecimal,
    screenWidth: Float
) {
    if (cart.isEmpty()) {
        Row(
            modifier              = Modifier.fillMaxSize(),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Filled.ShoppingCart, null, tint = BorderColor, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                "Your cart is empty — tap a product to add it",
                fontSize = (screenWidth * 0.024f).sp,
                color    = TextMedium
            )
        }
    } else {
        Column(
            modifier          = Modifier.fillMaxSize().padding(vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("Your Cart", fontSize = (screenWidth * 0.028f).sp, fontWeight = FontWeight.Bold, color = TextDark)
            
            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                cart.forEach { item ->
                    Row(
                        modifier              = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            "${item.product.emoji} ${item.product.name}  ×${item.quantity}",
                            fontSize = (screenWidth * 0.024f).sp,
                            color    = TextDark,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "£${"%.2f".format(item.product.priceGBP * BigDecimal(item.quantity))}",
                            fontSize   = (screenWidth * 0.024f).sp,
                            fontWeight = FontWeight.Bold,
                            color      = TextDark
                        )
                    }
                }
            }
            
            HorizontalDivider(color = DividerColor)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total", fontSize = (screenWidth * 0.028f).sp, fontWeight = FontWeight.Bold, color = TextDark)
                Text("£${"%.2f".format(total)}", fontSize = (screenWidth * 0.028f).sp, fontWeight = FontWeight.Bold, color = TextDark)
            }
        }
    }
}
