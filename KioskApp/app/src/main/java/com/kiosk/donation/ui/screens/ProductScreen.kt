package com.kiosk.donation.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.kiosk.donation.R
import com.kiosk.donation.data.CartItem
import com.kiosk.donation.data.Product
import com.kiosk.donation.data.ProductSize
import com.kiosk.donation.ui.theme.*
import java.io.File
import java.math.BigDecimal
import kotlinx.coroutines.launch

/**
 * Maps drawable resource name strings to their R.drawable integer IDs.
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
    categoryName: String,
    cart: List<CartItem>,
    cartTotal: BigDecimal,
    isOnline: Boolean,
    onBack: () -> Unit,
    onAddToCart: (Product, ProductSize?) -> Unit,
    onRemoveFromCart: (Product, ProductSize?) -> Unit,
    onClearCart: () -> Unit,
    onCheckout: () -> Unit,
    onUserActivity: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var productForSizeSelection by remember { mutableStateOf<Product?>(null) }
    var showNoConnectionError by remember { mutableStateOf(false) }

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

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
    ) {
        val screenHeight    = maxHeight
        val screenWidth     = maxWidth
        val cartHasItems    = cart.isNotEmpty()
        
        val donateHeight: Dp = screenHeight * 0.10f
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
                        text       = categoryName,
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
                            .height(56.dp),
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
                            lineHeight = (screenWidth.value * 0.032f).sp
                        )
                    )

                    Spacer(Modifier.width(8.dp))

                    if (cartHasItems) {
                        IconButton(onClick = onClearCart) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Clear Basket",
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(36.dp)
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
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            // ── Product grid ──────────────────────────────────────────────────
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.9f)
            ) {
                val gridState = rememberLazyGridState()
                val cardHeight: Dp = maxHeight * 0.47f

                if (filteredProducts.isEmpty() && products.isNotEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "No products match your search",
                            color = TextMedium,
                            fontSize = (screenWidth.value * 0.03f).sp
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxSize()) {
                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredProducts) { product ->
                            val itemsInCart = cart.filter { it.product.id == product.id }
                            val totalInCart = itemsInCart.sumOf { it.quantity }

                            ProductCard(
                                product = product,
                                quantityInCart = totalInCart,
                                screenWidth = screenWidth.value,
                                cardHeight = cardHeight,
                                onAdd = {
                                    onUserActivity()
                                    if (product.sizes.isNullOrEmpty()) {
                                        onAddToCart(product, null)
                                    } else {
                                        productForSizeSelection = product
                                    }
                                },
                                onRemove = {
                                    onUserActivity()
                                    if (product.sizes.isNullOrEmpty()) {
                                        onRemoveFromCart(product, null)
                                    }
                                }
                            )
                        }
                    }

                    // ── Custom Scrollbar ──────────────────────────────────────────
                    if (filteredProducts.size > 4) {
                        VerticalScrollbar(
                            gridState = gridState,
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(12.dp)
                                .padding(vertical = 8.dp, horizontal = 2.dp)
                        )
                    }
                }
            }

            // ── Cart panel ────────────────────────────────────────────────────
            Surface(
                modifier        = Modifier.fillMaxWidth().weight(0.45f),
                color           = SurfaceWhite,
                shadowElevation = 8.dp,
                tonalElevation  = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = hPad)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        CartDetails(
                            cart           = cart,
                            total          = cartTotal,
                            screenWidth    = screenWidth.value,
                            onAdd          = onAddToCart,
                            onRemove       = onRemoveFromCart,
                            onUserActivity = onUserActivity
                        )
                    }

                    Button(
                        onClick        = {
                            onUserActivity()
                            if (isOnline) onCheckout() else showNoConnectionError = true
                        },
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

        productForSizeSelection?.let { product ->
            SizeSelectionDialog(
                product = product,
                onDismiss = { productForSizeSelection = null },
                onSizeSelected = { size ->
                    onAddToCart(product, size)
                    productForSizeSelection = null
                }
            )
        }

        if (showNoConnectionError) {
            AlertDialog(
                onDismissRequest = { showNoConnectionError = false },
                title = { Text("Connection Lost") },
                text  = { Text("A connection is required to process payments. Please check your Wi-Fi and try again.") },
                confirmButton = {
                    Button(onClick = { showNoConnectionError = false }) { Text("OK") }
                }
            )
        }
    }
}

@Composable
fun VerticalScrollbar(
    gridState: LazyGridState,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    BoxWithConstraints(modifier = modifier) {
        val maxHeightPx = constraints.maxHeight.toFloat()
        val layoutInfo = gridState.layoutInfo
        val totalItems = layoutInfo.totalItemsCount
        val visibleItems = layoutInfo.visibleItemsInfo
        
        if (visibleItems.isEmpty() || totalItems == 0) return@BoxWithConstraints

        // Total rows in our 2-column grid
        val totalRows = (totalItems + 1) / 2
        
        // Get the height of a single item and the spacing (10dp)
        val firstItem = visibleItems.first()
        val itemHeight = firstItem.size.height.toFloat()
        val spacingPx = with(androidx.compose.ui.platform.LocalDensity.current) { 10.dp.toPx() }
        val rowHeight = itemHeight + spacingPx
        
        val viewportHeight = layoutInfo.viewportSize.height.toFloat()
        val totalContentHeight = (totalRows * rowHeight) - spacingPx
        
        // Only show scrollbar if content is actually scrollable
        if (totalContentHeight <= viewportHeight) return@BoxWithConstraints

        // Current scroll offset: (Number of rows scrolled away * rowHeight) + pixels of current row scrolled
        val firstVisibleRow = firstItem.index / 2
        val currentScrollOffset = (firstVisibleRow * rowHeight) - firstItem.offset.y
        
        val maxScrollOffset = (totalContentHeight - viewportHeight).coerceAtLeast(1f)
        val scrollFraction = (currentScrollOffset / maxScrollOffset).coerceIn(0f, 1f)

        // Calculate thumb height and offset
        val thumbHeightFraction = (viewportHeight / totalContentHeight).coerceIn(0.1f, 1f)
        val thumbHeight = maxHeightPx * thumbHeightFraction
        val thumbOffset = (maxHeightPx - thumbHeight) * scrollFraction

        // Track Background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
        )

        // Thumb
        Box(
            modifier = Modifier
                .offset(y = (thumbOffset / (maxHeightPx / constraints.maxHeight)).dp)
                .height((thumbHeight / (maxHeightPx / constraints.maxHeight)).dp)
                .fillMaxWidth()
                .background(KarimaGreen.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
                .pointerInput(maxScrollOffset) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        // Convert drag pixels to scroll pixels
                        val scrollDelta = (dragAmount.y / (maxHeightPx - thumbHeight)) * maxScrollOffset
                        coroutineScope.launch {
                            gridState.scrollBy(scrollDelta)
                        }
                    }
                }
        )
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
                when {
                    product.imagePath != null -> {
                        AsyncImage(
                            model              = File(product.imagePath),
                            contentDescription = product.name,
                            contentScale       = ContentScale.Fit,
                            modifier           = Modifier.fillMaxWidth().weight(1f).padding(4.dp)
                        )
                    }
                    product.imageRes != null -> {
                        val resId = drawableMap[product.imageRes]
                        if (resId != null) {
                            Image(
                                painter            = painterResource(id = resId),
                                contentDescription = product.name,
                                contentScale       = ContentScale.Fit,
                                modifier           = Modifier.fillMaxWidth().weight(1f).padding(4.dp)
                            )
                        } else {
                            Text(product.emoji, fontSize = (screenWidth * 0.07f).sp)
                        }
                    }
                    else -> {
                        Text(product.emoji, fontSize = (screenWidth * 0.07f).sp)
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
                    fontSize  = (screenWidth * 0.026f).sp,
                    textAlign = TextAlign.Center,
                    color     = TextMedium,
                    maxLines  = 1,
                    overflow  = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                
                // Show "From £..." if sizes have different prices, or just the main price
                val minPrice = product.sizes?.minOfOrNull { it.priceGBP }
                val maxPrice = product.sizes?.maxOfOrNull { it.priceGBP }
                val priceText = if (minPrice != null && maxPrice != null && minPrice != maxPrice) {
                    "From £${"%.2f".format(minPrice)}"
                } else {
                    "£${"%.2f".format(product.priceGBP)}"
                }
                
                Text(
                    text       = priceText,
                    fontSize   = (screenWidth * 0.034f).sp,
                    fontWeight = FontWeight.Bold,
                    color      = TextDark
                )
            }

            if (quantityInCart == 0) {
                Button(
                    onClick  = onAdd,
                    colors   = ButtonDefaults.buttonColors(containerColor = KarimaGreen),
                    shape    = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(if (product.sizes.isNullOrEmpty()) "Add" else "Select Size", fontSize = (screenWidth * 0.025f).sp)
                }
            } else {
                // If in cart, show a button to add more (which might trigger size selection)
                Button(
                    onClick  = onAdd,
                    colors   = ButtonDefaults.buttonColors(containerColor = KarimaGreen),
                    shape    = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("$quantityInCart in cart (Add more)", fontSize = (screenWidth * 0.024f).sp)
                }
            }
        }
    }
}

@Composable
private fun CartDetails(
    cart: List<CartItem>,
    total: BigDecimal,
    screenWidth: Float,
    onAdd: (Product, ProductSize?) -> Unit,
    onRemove: (Product, ProductSize?) -> Unit,
    onUserActivity: () -> Unit
) {
    if (cart.isEmpty()) {
        Row(
            modifier              = Modifier.fillMaxSize(),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Filled.ShoppingCart, null, tint = BorderColor, modifier = Modifier.size(36.dp))
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
                        modifier              = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "${item.product.emoji} ${item.product.name}${if (item.selectedSize != null) " (${item.selectedSize.name})" else ""}",
                                fontSize = (screenWidth * 0.024f).sp,
                                color    = TextDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val priceEach = item.selectedSize?.priceGBP ?: item.product.priceGBP
                            Text(
                                "£${"%.2f".format(priceEach)} each",
                                fontSize = (screenWidth * 0.022f).sp,
                                color = TextMedium
                            )
                        }

                        // Quantity Control Box - Centered/Aligned (Adjusted width/position)
                        Box(
                            modifier = Modifier.width(screenWidth.dp * 0.20f),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 2.dp, vertical = 2.dp)
                            ) {
                                IconButton(
                                    onClick = { 
                                        onUserActivity()
                                        onRemove(item.product, item.selectedSize) 
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Remove, 
                                        contentDescription = "Remove",
                                        tint = if (item.quantity > 1) TextDark else ErrorRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Text(
                                    text = "${item.quantity}",
                                    fontSize = (screenWidth * 0.024f).sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )

                                IconButton(
                                    onClick = { 
                                        onUserActivity()
                                        onAdd(item.product, item.selectedSize) 
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Add, 
                                        contentDescription = "Add",
                                        tint = KarimaGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                        
                        // Line Total - Aligned to the right
                        val linePrice = (item.selectedSize?.priceGBP ?: item.product.priceGBP) * BigDecimal(item.quantity)
                        Text(
                            "£${"%.2f".format(linePrice)}",
                            fontSize   = (screenWidth * 0.024f).sp,
                            fontWeight = FontWeight.Bold,
                            color      = TextDark,
                            modifier = Modifier.width(screenWidth.dp * 0.12f),
                            textAlign = TextAlign.End
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

@Composable
fun SizeSelectionDialog(
    product: Product,
    onDismiss: () -> Unit,
    onSizeSelected: (ProductSize) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = SurfaceWhite,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Select Size for ${product.name}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                
                product.sizes?.forEach { size ->
                    Button(
                        onClick = { onSizeSelected(size) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OffWhite, contentColor = TextDark),
                        shape = MaterialTheme.shapes.medium,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(size.name, fontWeight = FontWeight.Medium)
                            Text("£${"%.2f".format(size.priceGBP)}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = ErrorRed)
                }
            }
        }
    }
}
