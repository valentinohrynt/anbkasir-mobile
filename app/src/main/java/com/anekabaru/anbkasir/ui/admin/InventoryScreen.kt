package com.anekabaru.anbkasir.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anekabaru.anbkasir.data.ProductEntity
import com.anekabaru.anbkasir.ui.PosViewModel
import com.anekabaru.anbkasir.ui.components.PullToRefreshLayout
import com.anekabaru.anbkasir.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: PosViewModel,
    onBack: () -> Unit,
    onNavigateToDetail: () -> Unit,
    onNavigateToForm: () -> Unit
) {
    val products by viewModel.products.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    val filteredProducts = remember(products, searchQuery) {
        if (searchQuery.isBlank()) {
            products
        } else {
            products.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.category.contains(searchQuery, ignoreCase = true) ||
                        (it.barcode ?: "").contains(searchQuery)
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.selectProduct(null)
                    onNavigateToForm()
                },
                containerColor = BrandBlue, // Updated to Theme
                contentColor = White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, "Add Product")
            }
        },
        containerColor = BackgroundApp // Updated to Theme
    ) { padding ->
        PullToRefreshLayout(
            isRefreshing = isSyncing,
            onRefresh = { viewModel.sync() },
            modifier = Modifier.padding(padding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header Section
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            IconButton(
                                onClick = onBack,
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    "Back",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column {
                                Text(
                                    "Inventory",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = TextPrimary
                                )
                                Text(
                                    "${products.size} Products",
                                    style = MaterialTheme.typography.bodySmall, // Using bodySmall as defined in Type.kt or default
                                    color = TextSecondary
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SurfaceBlue), // Updated to Theme
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.Inventory2,
                                contentDescription = null,
                                tint = BrandBlue, // Updated to Theme
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
                        placeholder = {
                            Text(
                                "Search products...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextTertiary
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                null,
                                tint = TextTertiary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
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

                if (filteredProducts.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Inventory2,
                                contentDescription = null,
                                tint = TextTertiary,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                if (products.isEmpty()) "No products yet" else "No results found",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                            if (products.isEmpty()) {
                                Text(
                                    "Tap + to add your first product",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextTertiary
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(filteredProducts) { product ->
                            CompactProductCard(
                                product = product,
                                onClick = {
                                    viewModel.selectProduct(product)
                                    onNavigateToDetail()
                                }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun CompactProductCard(product: ProductEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    product.name,
                    style = MaterialTheme.typography.titleSmall, // 15.sp defined in Type.kt
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = SurfaceBlue, // Updated to Theme
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            product.category,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall, // 11.sp defined in Type.kt
                            color = BrandBlue
                        )
                    }
                    Text(
                        "Rp${product.sellPrice}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = BrandGreen
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    "Stock",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
                Text(
                    "${product.stock}",
                    style = MaterialTheme.typography.titleMedium,
                    color = if(product.stock < 10) SystemRed else TextPrimary, // Updated to Theme
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}