package com.kiosk.donation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kiosk.donation.data.Category
import com.kiosk.donation.ui.theme.*
import java.io.File

@Composable
fun CategoryScreen(
    categories: List<Category>,
    cartCount: Int,
    onCategoryClick: (Category) -> Unit,
    onSearchClick: () -> Unit,
    onBack: () -> Unit
) {
    val allCategory = Category("all", "All Products", "🛍️")
    val displayCategories = categories + allCategory

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
    ) {
        // Top Bar
        Surface(color = KarimaDark, shadowElevation = 4.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    "Categories",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = onSearchClick) {
                    Icon(Icons.Default.Search, "Search", tint = Color.White, modifier = Modifier.size(32.dp))
                }

                Spacer(Modifier.width(8.dp))

                BadgedBox(badge = {
                    if (cartCount > 0) Badge(containerColor = Amber, contentColor = TextDark) {
                        Text("$cartCount", fontWeight = FontWeight.Bold)
                    }
                }) {
                    Icon(Icons.Filled.ShoppingCart, "Cart", tint = Color.White, modifier = Modifier.size(32.dp))
                }
            }
        }

        // Category Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(displayCategories) { category ->
                CategoryCard(category = category, onClick = { onCategoryClick(category) })
            }
        }
    }
}

@Composable
private fun CategoryCard(category: Category, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (category.imagePath != null) {
                AsyncImage(
                    model = File(category.imagePath),
                    contentDescription = category.name,
                    modifier = Modifier.size(80.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                Text(category.emoji, fontSize = 64.sp)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
