package com.anekabaru.anbkasir.ui.pos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anekabaru.anbkasir.data.ProductEntity
import com.anekabaru.anbkasir.ui.PosViewModel
import com.anekabaru.anbkasir.ui.components.PullToRefreshLayout
import com.anekabaru.anbkasir.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(
    viewModel: PosViewModel,
    onBack: () -> Unit,
    onViewCart: () -> Unit
) {
    val products by viewModel.products.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val total by viewModel.grandTotal.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val filteredProducts = remember(products, searchQuery) {
        if (searchQuery.isBlank()) products else products.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.category.contains(searchQuery, ignoreCase = true) ||
                    (it.barcode ?: "").contains(searchQuery)
        }
    }

    val itemCount = cart.sumOf { it.quantity }

    Scaffold(
        bottomBar = {
            if (itemCount > 0) {
                Surface(
                    shadowElevation = 16.dp,
                    color = White,
                    modifier = Modifier.fillMaxWidth().clickable { onViewCart() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .background(BrandGreen, RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("$itemCount Items", color = White, style = MaterialTheme.typography.labelSmall)
                            Text(
                                "Total: Rp${"%.2f".format(total)}",
                                color = White,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("View Cart", color = White, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.ShoppingCart, null, tint = White)
                        }
                    }
                }
            }
        },
        containerColor = BackgroundApp
    ) { padding ->
        PullToRefreshLayout(
            isRefreshing = isSyncing,
            onRefresh = { viewModel.sync() },
            modifier = Modifier.padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // --- CUSTOM HEADER ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(White)
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Back Button & Title
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    "Back",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column {
                                Text(
                                    "Point of Sale",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = TextPrimary
                                )
                                Text(
                                    "Product Catalog",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }

                        // Right: Decorative Store Icon (Replaces Refresh Button)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SurfaceGreen), // POS Theme Color
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Store, // Decorative Icon
                                contentDescription = null,
                                tint = BrandGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search products...", style = MaterialTheme.typography.bodyMedium, color = TextTertiary) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = TextTertiary, modifier = Modifier.size(20.dp)) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandBlue,
                            unfocusedBorderColor = BorderColor,
                            focusedContainerColor = BackgroundApp,
                            unfocusedContainerColor = BackgroundApp
                        ),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- CONTENT ---
                if (filteredProducts.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "No products found.",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 160.dp),
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredProducts) { product ->
                            val qtyInCart = cart.find { it.product.id == product.id }?.quantity ?: 0
                            ProductCardSimple(
                                product = product,
                                qty = qtyInCart,
                                onAdd = { viewModel.updateCartQuantity(product.id, 1) },
                                onRemove = { viewModel.updateCartQuantity(product.id, -1) },
                                onClickInitial = { viewModel.addToCart(product) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCardSimple(
    product: ProductEntity,
    qty: Int,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
    onClickInitial: () -> Unit
) {
    val isSelected = qty > 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clickable(enabled = !isSelected) { onClickInitial() },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, BrandGreen) else null
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    color = SurfaceBlue,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        product.category.uppercase(),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = BrandBlue,
                        fontSize = 10.sp
                    )
                }
                Text(
                    product.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = TextPrimary
                )
            }

            if (isSelected) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        modifier = Modifier.size(28.dp).clickable { onRemove() },
                        shape = RoundedCornerShape(8.dp),
                        color = BackgroundApp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Remove, null, tint = SystemRed, modifier = Modifier.size(16.dp))
                        }
                    }
                    Text("$qty", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    Surface(
                        modifier = Modifier.size(28.dp).clickable { onAdd() },
                        shape = RoundedCornerShape(8.dp),
                        color = BrandGreen
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Add, null, tint = White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            } else {
                Text(
                    "Rp${product.sellPrice}",
                    style = MaterialTheme.typography.titleMedium,
                    color = BrandGreen
                )
            }
        }
    }
}