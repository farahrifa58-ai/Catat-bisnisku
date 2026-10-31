package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BusinessType
import com.example.data.model.Item
import com.example.data.model.ItemType
import com.example.ui.components.EmptyStateView
import com.example.ui.components.formatRupiah
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.PrimaryGreenLight
import com.example.ui.viewmodel.MainViewModel

@Composable
fun StockScreen(
  viewModel: MainViewModel,
  onOpenAddDialog: (ItemType) -> Unit,
  onOpenProduction: (Item?) -> Unit,
  modifier: Modifier = Modifier
) {
  val profile by viewModel.businessProfile.collectAsState()
  val allProducts by viewModel.products.collectAsState()
  val allRawMaterials by viewModel.rawMaterials.collectAsState()

  val isProduksi = profile?.type == BusinessType.PRODUKSI.name
  var selectedTab by remember { mutableIntStateOf(0) } // 0: Bahan Baku, 1: Produk Jadi
  var searchQuery by remember { mutableStateOf("") }

  // Dialog States
  var stockAdjustmentItem by remember { mutableStateOf<Item?>(null) }
  var isAddStockMode by remember { mutableStateOf(true) } // true: + Stok, false: - Stok
  var editItemTarget by remember { mutableStateOf<Item?>(null) }
  var deleteConfirmItem by remember { mutableStateOf<Item?>(null) }

  val displayedItems = if (isProduksi) {
    if (selectedTab == 0) allRawMaterials else allProducts
  } else {
    allProducts
  }.filter {
    it.name.contains(searchQuery, ignoreCase = true)
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .testTag("stock_screen")
  ) {
    // Search & Action Header
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = if (isProduksi) "Stok Bahan & Produk" else "Stok Barang Dagangan",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1
          )
          Text(
            text = "${displayedItems.size} item terdaftar",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
          )
        }
        Spacer(modifier = Modifier.width(8.dp))

        Button(
          onClick = {
            val type = if (isProduksi && selectedTab == 0) ItemType.RAW_MATERIAL else ItemType.PRODUCT
            onOpenAddDialog(type)
          },
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreenLight),
          modifier = Modifier.testTag("btn_tambah_item").height(40.dp)
        ) {
          Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (isProduksi && selectedTab == 0) "+ Bahan" else "+ Barang",
            fontSize = 13.sp,
            maxLines = 1,
            softWrap = false
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text("Cari nama barang atau bahan...") },
        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
      )
    }

    // Tabs for Usaha Produksi
    if (isProduksi) {
      TabRow(
        selectedTabIndex = selectedTab,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = PrimaryGreenLight
      ) {
        Tab(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          text = {
            Text(
              "🌾 Bahan Baku (${allRawMaterials.size})",
              fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
            )
          }
        )
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          text = {
            Text(
              "🧁 Produk Jadi (${allProducts.size})",
              fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
            )
          }
        )
      }
    }

    // Items List
    if (displayedItems.isEmpty()) {
      val itemLabel = if (isProduksi && selectedTab == 0) "bahan baku" else "barang"
      EmptyStateView(
        title = "Belum Ada $itemLabel",
        description = if (searchQuery.isNotEmpty()) "Tidak ada hasil pencarian untuk \"$searchQuery\"" else "Tambahkan $itemLabel pertama Anda untuk mulai mengontrol stok dan mencatat penjualan.",
        icon = Icons.Default.Inventory,
        actionButtonText = "+ Tambah $itemLabel",
        onActionClick = {
          val type = if (isProduksi && selectedTab == 0) ItemType.RAW_MATERIAL else ItemType.PRODUCT
          onOpenAddDialog(type)
        },
        modifier = Modifier.weight(1f)
      )
    } else {
      LazyColumn(
        modifier = Modifier
          .weight(1f)
          .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        item { Spacer(modifier = Modifier.height(6.dp)) }

        items(displayedItems, key = { it.id }) { item ->
          StockItemCard(
            item = item,
            isProduksi = isProduksi,
            onAddStock = {
              stockAdjustmentItem = item
              isAddStockMode = true
            },
            onReduceStock = {
              stockAdjustmentItem = item
              isAddStockMode = false
            },
            onProduce = {
              onOpenProduction(item)
            },
            onEdit = {
              editItemTarget = item
            },
            onDelete = {
              deleteConfirmItem = item
            }
          )
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
      }
    }
  }

  // Stock Adjustment Dialog (+ / - Stok)
  stockAdjustmentItem?.let { item ->
    StockAdjustModal(
      item = item,
      isAddMode = isAddStockMode,
      onDismiss = { stockAdjustmentItem = null },
      onConfirm = { qty, newCost, note ->
        if (isAddStockMode) {
          viewModel.addStock(item.id, qty, newCost, note)
        } else {
          viewModel.reduceStock(item.id, qty, note)
        }
        stockAdjustmentItem = null
      }
    )
  }

  // Edit Item Dialog
  editItemTarget?.let { item ->
    EditItemModal(
      item = item,
      onDismiss = { editItemTarget = null },
      onSave = { updated ->
        viewModel.updateItem(updated)
        editItemTarget = null
      }
    )
  }

  // Delete Confirm Dialog
  deleteConfirmItem?.let { item ->
    AlertDialog(
      onDismissRequest = { deleteConfirmItem = null },
      title = { Text("Hapus ${item.name}?") },
      text = { Text("Barang ini akan dihapus dari daftar stok usaha Anda. Tindakan ini tidak dapat dibatalkan.") },
      confirmButton = {
        Button(
          onClick = {
            viewModel.deleteItem(item)
            deleteConfirmItem = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Hapus")
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { deleteConfirmItem = null }) {
          Text("Batal")
        }
      }
    )
  }
}

@Composable
fun StockItemCard(
  item: Item,
  isProduksi: Boolean,
  onAddStock: () -> Unit,
  onReduceStock: () -> Unit,
  onProduce: () -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("item_card_${item.id}"),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = item.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )

          Spacer(modifier = Modifier.height(2.dp))

          // Badge status stok
          val (badgeText, badgeBg, badgeTextColor) = when {
            item.isOutOfStock -> Triple("Habis", Color(0xFFFFEBEE), ExpenseRed)
            item.isLowStock -> Triple("Stok Menipis", Color(0xFFFFF8E1), AccentAmber)
            else -> Triple("Tersedia", Color(0xFFE8F5E9), PrimaryGreen)
          }

          Surface(
            shape = RoundedCornerShape(6.dp),
            color = badgeBg,
            modifier = Modifier.padding(vertical = 2.dp)
          ) {
            Text(
              text = badgeText,
              style = MaterialTheme.typography.labelSmall,
              color = badgeTextColor,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }

        // Action menu
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = Color.Gray, modifier = Modifier.size(16.dp))
          }
          IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus", tint = ExpenseRed, modifier = Modifier.size(16.dp))
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Numbers Row: Stock, Cost, Sell
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Text(text = "Jumlah Stok", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text(
            text = "${item.stock} ${item.unit}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = if (item.isOutOfStock) ExpenseRed else MaterialTheme.colorScheme.onSurface
          )
        }

        Column {
          Text(
            text = if (item.itemType == ItemType.RAW_MATERIAL.name) "Harga Beli" else "Harga Modal",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = formatRupiah(item.costPrice),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
          )
        }

        if (item.itemType != ItemType.RAW_MATERIAL.name) {
          Column {
            Text(text = "Harga Jual", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
              text = formatRupiah(item.sellingPrice),
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold,
              color = PrimaryGreenLight
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Quick Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        OutlinedButton(
          onClick = onReduceStock,
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(10.dp)
        ) {
          Icon(imageVector = Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("- Stok", fontSize = 12.sp)
        }

        Button(
          onClick = onAddStock,
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(10.dp),
          colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreenLight)
        ) {
          Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("+ Stok", fontSize = 12.sp)
        }

        if (isProduksi && item.itemType == ItemType.PRODUCT.name) {
          Button(
            onClick = onProduce,
            modifier = Modifier.weight(1.2f),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
          ) {
            Icon(imageVector = Icons.Default.Build, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Produksi", fontSize = 12.sp)
          }
        }
      }
    }
  }
}

@Composable
fun StockAdjustModal(
  item: Item,
  isAddMode: Boolean,
  onDismiss: () -> Unit,
  onConfirm: (qty: Double, newCost: Double, note: String) -> Unit
) {
  var qtyStr by remember { mutableStateOf("") }
  var costStr by remember { mutableStateOf(if (isAddMode && item.costPrice > 0) item.costPrice.toInt().toString() else "") }
  var noteStr by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(if (isAddMode) "Tambah Stok: ${item.name}" else "Kurangi Stok: ${item.name}", fontWeight = FontWeight.Bold)
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = "Stok saat ini: ${item.stock} ${item.unit}",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
          value = qtyStr,
          onValueChange = { qtyStr = it.filter { char -> char.isDigit() || char == '.' } },
          label = { Text("Jumlah (${item.unit})") },
          placeholder = { Text("Contoh: 10") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        if (isAddMode) {
          OutlinedTextField(
            value = costStr,
            onValueChange = { costStr = it.filter { char -> char.isDigit() } },
            label = { Text("Harga Modal Beli per ${item.unit} (Rp)") },
            placeholder = { Text("Opsional (default: ${formatRupiah(item.costPrice)})") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }

        OutlinedTextField(
          value = noteStr,
          onValueChange = { noteStr = it },
          label = { Text("Catatan / Keterangan") },
          placeholder = { Text(if (isAddMode) "Contoh: Beli dari supplier Toko Makmur" else "Contoh: Barang rusak / expired") },
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val qty = qtyStr.toDoubleOrNull() ?: 0.0
          val cost = costStr.toDoubleOrNull() ?: 0.0
          if (qty > 0) {
            onConfirm(qty, cost, noteStr)
          }
        },
        enabled = (qtyStr.toDoubleOrNull() ?: 0.0) > 0,
        colors = if (isAddMode) ButtonDefaults.buttonColors(containerColor = PrimaryGreenLight) else ButtonDefaults.buttonColors(containerColor = ExpenseRed)
      ) {
        Text(if (isAddMode) "Tambah Stok" else "Kurangi Stok")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text("Batal")
      }
    }
  )
}

@Composable
fun EditItemModal(
  item: Item,
  onDismiss: () -> Unit,
  onSave: (Item) -> Unit
) {
  var name by remember { mutableStateOf(item.name) }
  var unit by remember { mutableStateOf(item.unit) }
  var costPriceStr by remember { mutableStateOf(item.costPrice.toInt().toString()) }
  var sellingPriceStr by remember { mutableStateOf(item.sellingPrice.toInt().toString()) }
  var minStockStr by remember { mutableStateOf(item.minStockAlert.toInt().toString()) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Edit: ${item.name}", fontWeight = FontWeight.Bold) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Nama") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = unit,
          onValueChange = { unit = it },
          label = { Text("Satuan (Pcs, Kg, Liter, dll)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = costPriceStr,
          onValueChange = { costPriceStr = it.filter { char -> char.isDigit() } },
          label = { Text("Harga Modal (Rp)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        if (item.itemType != ItemType.RAW_MATERIAL.name) {
          OutlinedTextField(
            value = sellingPriceStr,
            onValueChange = { sellingPriceStr = it.filter { char -> char.isDigit() } },
            label = { Text("Harga Jual (Rp)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }
        OutlinedTextField(
          value = minStockStr,
          onValueChange = { minStockStr = it.filter { char -> char.isDigit() || char == '.' } },
          label = { Text("Batas Peringatan Minimum") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onSave(
            item.copy(
              name = name.trim(),
              unit = unit.trim().ifBlank { "Pcs" },
              costPrice = costPriceStr.toDoubleOrNull() ?: item.costPrice,
              sellingPrice = sellingPriceStr.toDoubleOrNull() ?: item.sellingPrice,
              minStockAlert = minStockStr.toDoubleOrNull() ?: item.minStockAlert
            )
          )
        },
        enabled = name.isNotBlank()
      ) {
        Text("Simpan Perubahan")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text("Batal")
      }
    }
  )
}
