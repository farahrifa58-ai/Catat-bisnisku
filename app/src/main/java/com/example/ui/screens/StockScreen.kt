package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import com.example.data.model.Item
import com.example.data.model.ItemType
import com.example.ui.components.EmptyStateView
import com.example.ui.components.formatRupiah
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.PrimaryGreenLight
import com.example.ui.theme.SecondaryTeal
import com.example.ui.viewmodel.MainViewModel

@Composable
fun StockScreen(
  viewModel: MainViewModel,
  selectedTab: Int = 0,
  onTabSelected: (Int) -> Unit = {},
  onOpenAddDialog: (ItemType) -> Unit,
  onOpenProduction: (Item?) -> Unit,
  modifier: Modifier = Modifier
) {
  val allProducts by viewModel.products.collectAsState()
  val allRawMaterials by viewModel.rawMaterials.collectAsState()

  var searchQuery by remember { mutableStateOf("") }

  // Dialog States
  var bahanBakuToAddStock by remember { mutableStateOf<Item?>(null) }
  var productToAddStock by remember { mutableStateOf<Item?>(null) }
  var itemToReduceStock by remember { mutableStateOf<Item?>(null) }
  var editItemTarget by remember { mutableStateOf<Item?>(null) }
  var deleteConfirmItem by remember { mutableStateOf<Item?>(null) }

  val displayedItems = if (selectedTab == 0) {
    allRawMaterials
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
    // Search & Clean Header (No cluttered top button)
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "Stok Bahan & Produk",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1
          )
          Text(
            text = "${displayedItems.size} item terdaftar (${if (selectedTab == 0) "Bahan Baku" else "Produk Jadi"})",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
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

    // Always-accessible Tabs for Bahan Baku & Produk Jadi
    TabRow(
      selectedTabIndex = selectedTab,
      containerColor = MaterialTheme.colorScheme.surface,
      contentColor = PrimaryGreenLight
    ) {
      Tab(
        selected = selectedTab == 0,
        onClick = { onTabSelected(0) },
        text = {
          Text(
            "🌾 Bahan Baku (${allRawMaterials.size})",
            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
            fontSize = 13.sp
          )
        }
      )
      Tab(
        selected = selectedTab == 1,
        onClick = { onTabSelected(1) },
        text = {
          Text(
            "🧁 Produk Jadi (${allProducts.size})",
            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
            fontSize = 13.sp
          )
        }
      )
    }

    // Items List or Empty State
    if (displayedItems.isEmpty()) {
      val isBahan = selectedTab == 0
      EmptyStateView(
        title = if (isBahan) "Belum Ada Bahan Baku" else "Belum Ada Produk Jadi",
        description = if (searchQuery.isNotEmpty()) {
          "Tidak ada hasil pencarian untuk \"$searchQuery\""
        } else if (isBahan) {
          "Tambahkan bahan baku (misal: tepung, gula, telur, minyak) untuk mencatat stok dan belanja bahan usaha."
        } else {
          "Tambahkan produk jadi yang siap dijual ke pelanggan untuk memantau stok dan keuntungan transaksi."
        },
        icon = if (isBahan) Icons.Default.ShoppingCart else Icons.Default.Inventory,
        actionButtonText = if (isBahan) "+ Tambah Bahan Baku" else "+ Tambah Produk Jadi",
        onActionClick = {
          onOpenAddDialog(if (isBahan) ItemType.RAW_MATERIAL else ItemType.PRODUCT)
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
            isRawMaterial = item.itemType == ItemType.RAW_MATERIAL.name,
            onAddStock = {
              if (item.itemType == ItemType.RAW_MATERIAL.name) {
                bahanBakuToAddStock = item
              } else {
                productToAddStock = item
              }
            },
            onReduceStock = {
              itemToReduceStock = item
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

  // 1. Dialog Beli / Tambah Stok Bahan Baku (Khusus Bahan Baku)
  bahanBakuToAddStock?.let { item ->
    AddBahanBakuStockModal(
      item = item,
      onDismiss = { bahanBakuToAddStock = null },
      onConfirm = { qty, cost, note, recordExpense ->
        viewModel.addStock(item.id, qty, cost, note, recordExpense)
        bahanBakuToAddStock = null
      }
    )
  }

  // 2. Dialog Tambah Stok Produk Jadi (Khusus Produk Jadi)
  productToAddStock?.let { item ->
    AddProductStockModal(
      item = item,
      onDismiss = { productToAddStock = null },
      onOpenProduction = {
        productToAddStock = null
        onOpenProduction(item)
      },
      onConfirm = { qty, cost, note ->
        viewModel.addStock(item.id, qty, cost, note)
        productToAddStock = null
      }
    )
  }

  // 3. Dialog Kurangi Stok (Tersendiri, rapi & ada opsi alasan pengurangan)
  itemToReduceStock?.let { item ->
    ReduceStockModal(
      item = item,
      onDismiss = { itemToReduceStock = null },
      onConfirm = { qty, note ->
        viewModel.reduceStock(item.id, qty, note)
        itemToReduceStock = null
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
      text = { Text("Item ini akan dihapus dari daftar stok usaha Anda. Tindakan ini tidak dapat dibatalkan.") },
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
  isRawMaterial: Boolean,
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
            text = if (isRawMaterial) "Harga Beli" else "Harga Modal",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = formatRupiah(item.costPrice),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
          )
        }

        if (!isRawMaterial) {
          Column {
            Text(text = "Harga Jual", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
              text = formatRupiah(item.sellingPrice),
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold,
              color = PrimaryGreenLight
            )
          }
        } else {
          Column {
            Text(text = "Nilai Stok", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
              text = formatRupiah(item.stock * item.costPrice),
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold,
              color = SecondaryTeal
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Action Buttons (Clean, strictly single - and single +, no wrapping)
      if (isRawMaterial) {
        // Tab Bahan Baku: Hanya 2 tombol (Kurang & Beli Stok)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedButton(
            onClick = onReduceStock,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
          ) {
            Icon(imageVector = Icons.Default.Remove, contentDescription = "Kurang", modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Kurang", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)
          }

          Button(
            onClick = onAddStock,
            modifier = Modifier.weight(1.3f),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreenLight),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
          ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Beli Bahan", modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Beli Stok", fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
          }
        }
      } else {
        // Tab Produk Jadi: 3 tombol rapi (Kurang, Tambah Stok, Produksi)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          OutlinedButton(
            onClick = onReduceStock,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
          ) {
            Icon(imageVector = Icons.Default.Remove, contentDescription = "Kurang", modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text("Kurang", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)
          }

          OutlinedButton(
            onClick = onAddStock,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
          ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah", modifier = Modifier.size(14.dp), tint = PrimaryGreenLight)
            Spacer(modifier = Modifier.width(3.dp))
            Text("Tambah", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = PrimaryGreenLight, maxLines = 1, softWrap = false)
          }

          Button(
            onClick = onProduce,
            modifier = Modifier.weight(1.2f),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
          ) {
            Icon(imageVector = Icons.Default.Build, contentDescription = "Produksi", modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text("Produksi", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
          }
        }
      }
    }
  }
}

// -------------------------------------------------------------------------------------------------
// MODAL: BELI / TAMBAH STOK BAHAN BAKU (Khusus Bahan Baku)
// -------------------------------------------------------------------------------------------------
@Composable
fun AddBahanBakuStockModal(
  item: Item,
  onDismiss: () -> Unit,
  onConfirm: (qty: Double, newCost: Double, note: String, recordExpense: Boolean) -> Unit
) {
  var qtyStr by remember { mutableStateOf("") }
  var costStr by remember { mutableStateOf(if (item.costPrice > 0) item.costPrice.toInt().toString() else "") }
  var noteStr by remember { mutableStateOf("") }
  var recordAsExpense by remember { mutableStateOf(true) }

  val qty = qtyStr.toDoubleOrNull() ?: 0.0
  val unitCost = costStr.toDoubleOrNull() ?: item.costPrice
  val totalCost = qty * unitCost

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null, tint = PrimaryGreenLight)
        Spacer(modifier = Modifier.width(8.dp))
        Column {
          Text("Beli Bahan Baku", fontWeight = FontWeight.Bold, fontSize = 18.sp)
          Text(item.name, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Stock summary card
        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("Stok Saat Ini", style = MaterialTheme.typography.labelSmall)
              Text("${item.stock} ${item.unit}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            if (qty > 0) {
              Column(horizontalAlignment = Alignment.End) {
                Text("Stok Setelah Beli", style = MaterialTheme.typography.labelSmall)
                Text(
                  "${item.stock + qty} ${item.unit}",
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp,
                  color = PrimaryGreenLight
                )
              }
            }
          }
        }

        // Quick quantity chips
        Column {
          Text("Tambah Cepat:", style = MaterialTheme.typography.labelSmall)
          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(vertical = 4.dp)
          ) {
            val chips = listOf(1.0, 2.0, 5.0, 10.0, 25.0, 50.0)
            items(chips) { amount ->
              FilterChip(
                selected = false,
                onClick = {
                  val current = qtyStr.toDoubleOrNull() ?: 0.0
                  qtyStr = (current + amount).toString().removeSuffix(".0")
                },
                label = { Text("+$amount", fontSize = 12.sp) }
              )
            }
          }
        }

        OutlinedTextField(
          value = qtyStr,
          onValueChange = { qtyStr = it.filter { char -> char.isDigit() || char == '.' } },
          label = { Text("Jumlah Beli (${item.unit})") },
          placeholder = { Text("Contoh: 10") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = costStr,
          onValueChange = { costStr = it.filter { char -> char.isDigit() } },
          label = { Text("Harga Beli per ${item.unit} (Rp)") },
          placeholder = { Text("Contoh: 12000") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = noteStr,
          onValueChange = { noteStr = it },
          label = { Text("Nama Toko / Supplier / Catatan") },
          placeholder = { Text("Contoh: Toko Makmur / Pasar") },
          modifier = Modifier.fillMaxWidth()
        )

        // Live total preview
        if (qty > 0 && unitCost > 0) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFE8F5E9),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Text("Total Biaya Belanja:", style = MaterialTheme.typography.labelSmall)
              Text(
                formatRupiah(totalCost),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryGreen
              )
            }
          }

          // Checkbox automatically record as expense
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .fillMaxWidth()
              .clickable { recordAsExpense = !recordAsExpense }
          ) {
            Checkbox(
              checked = recordAsExpense,
              onCheckedChange = { recordAsExpense = it },
              colors = CheckboxDefaults.colors(checkedColor = PrimaryGreenLight)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              "Catat otomatis ke Pengeluaran Usaha",
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.Medium
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (qty > 0) {
            onConfirm(qty, unitCost, noteStr, recordAsExpense)
          }
        },
        enabled = qty > 0,
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreenLight)
      ) {
        Text("Simpan Pembelian")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text("Batal")
      }
    }
  )
}

// -------------------------------------------------------------------------------------------------
// MODAL: TAMBAH STOK PRODUK JADI (Khusus Produk Jadi)
// -------------------------------------------------------------------------------------------------
@Composable
fun AddProductStockModal(
  item: Item,
  onDismiss: () -> Unit,
  onOpenProduction: () -> Unit,
  onConfirm: (qty: Double, newCost: Double, note: String) -> Unit
) {
  var qtyStr by remember { mutableStateOf("") }
  var costStr by remember { mutableStateOf(if (item.costPrice > 0) item.costPrice.toInt().toString() else "") }
  var noteStr by remember { mutableStateOf("") }

  val qty = qtyStr.toDoubleOrNull() ?: 0.0

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = Icons.Default.Inventory, contentDescription = null, tint = PrimaryGreenLight)
        Spacer(modifier = Modifier.width(8.dp))
        Column {
          Text("Tambah Stok Produk", fontWeight = FontWeight.Bold, fontSize = 18.sp)
          Text(item.name, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Banner recommend using Produksi
        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Build, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Buat dari Bahan Baku?", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PrimaryGreen)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              "Gunakan fitur Produksi agar stok bahan baku otomatis berkurang dan modal produk dihitung tepat.",
              fontSize = 11.sp,
              color = Color(0xFF2E7D32)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Button(
              onClick = onOpenProduction,
              shape = RoundedCornerShape(8.dp),
              colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
              modifier = Modifier.fillMaxWidth().height(34.dp),
              contentPadding = PaddingValues(vertical = 0.dp)
            ) {
              Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Buka Form Produksi", fontSize = 12.sp)
            }
          }
        }

        Text(
          text = "Atau Tambah Stok Langsung:",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Stock summary
        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("Stok Saat Ini", style = MaterialTheme.typography.labelSmall)
              Text("${item.stock} ${item.unit}", fontWeight = FontWeight.Bold)
            }
            if (qty > 0) {
              Column(horizontalAlignment = Alignment.End) {
                Text("Stok Baru", style = MaterialTheme.typography.labelSmall)
                Text("${item.stock + qty} ${item.unit}", fontWeight = FontWeight.Bold, color = PrimaryGreenLight)
              }
            }
          }
        }

        // Quick add chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          val chips = listOf(1.0, 5.0, 10.0, 20.0, 50.0)
          items(chips) { amount ->
            FilterChip(
              selected = false,
              onClick = {
                val current = qtyStr.toDoubleOrNull() ?: 0.0
                qtyStr = (current + amount).toString().removeSuffix(".0")
              },
              label = { Text("+$amount", fontSize = 12.sp) }
            )
          }
        }

        OutlinedTextField(
          value = qtyStr,
          onValueChange = { qtyStr = it.filter { char -> char.isDigit() || char == '.' } },
          label = { Text("Jumlah Stok Masuk (${item.unit})") },
          placeholder = { Text("Contoh: 10") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = costStr,
          onValueChange = { costStr = it.filter { char -> char.isDigit() } },
          label = { Text("Harga Modal per ${item.unit} (Rp)") },
          placeholder = { Text("Opsional (default: ${formatRupiah(item.costPrice)})") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = noteStr,
          onValueChange = { noteStr = it },
          label = { Text("Catatan / Keterangan") },
          placeholder = { Text("Contoh: Stok awal / Retur konsumen") },
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val cost = costStr.toDoubleOrNull() ?: item.costPrice
          if (qty > 0) {
            onConfirm(qty, cost, noteStr)
          }
        },
        enabled = qty > 0,
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreenLight)
      ) {
        Text("Tambah Stok")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text("Batal")
      }
    }
  )
}

// -------------------------------------------------------------------------------------------------
// MODAL: KURANGI STOK (Rapi, Terstruktur, dengan Alasan Pengurangan)
// -------------------------------------------------------------------------------------------------
@Composable
fun ReduceStockModal(
  item: Item,
  onDismiss: () -> Unit,
  onConfirm: (qty: Double, note: String) -> Unit
) {
  var qtyStr by remember { mutableStateOf("") }
  var selectedReason by remember { mutableStateOf("Barang Rusak / Basi") }
  var customNote by remember { mutableStateOf("") }

  val qty = qtyStr.toDoubleOrNull() ?: 0.0
  val remainingStock = item.stock - qty
  val isOverStock = qty > item.stock

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = Icons.Default.RemoveCircleOutline, contentDescription = null, tint = ExpenseRed)
        Spacer(modifier = Modifier.width(8.dp))
        Column {
          Text("Kurangi Stok", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = ExpenseRed)
          Text(item.name, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Stock tracker card
        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = if (isOverStock) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.surfaceVariant)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text("Stok Sekarang:", style = MaterialTheme.typography.bodySmall)
              Text("${item.stock} ${item.unit}", fontWeight = FontWeight.Bold)
            }
            if (qty > 0) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text("Akan Dikurangi:", style = MaterialTheme.typography.bodySmall, color = ExpenseRed)
                Text("-$qty ${item.unit}", fontWeight = FontWeight.Bold, color = ExpenseRed)
              }
              Box(modifier = Modifier.fillMaxWidth().height(1.dp).padding(vertical = 4.dp).background(Color.LightGray))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text("Sisa Stok:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                Text(
                  "${remainingStock.coerceAtLeast(0.0)} ${item.unit}",
                  fontWeight = FontWeight.Bold,
                  color = if (isOverStock) ExpenseRed else PrimaryGreen
                )
              }
            }
          }
        }

        if (isOverStock) {
          Text(
            text = "⚠️ Peringatan: Jumlah pengurangan melebihi stok yang tersedia!",
            style = MaterialTheme.typography.labelSmall,
            color = ExpenseRed,
            fontWeight = FontWeight.Bold
          )
        }

        // Reason chips
        Column {
          Text("Alasan Pengurangan:", style = MaterialTheme.typography.labelSmall)
          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(vertical = 4.dp)
          ) {
            val reasons = listOf("Barang Rusak / Basi", "Kadaluarsa", "Dipakai Sendiri", "Koreksi Fisik", "Lainnya")
            items(reasons) { reason ->
              FilterChip(
                selected = selectedReason == reason,
                onClick = { selectedReason = reason },
                label = { Text(reason, fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = ExpenseRed.copy(alpha = 0.15f),
                  selectedLabelColor = ExpenseRed
                )
              )
            }
          }
        }

        OutlinedTextField(
          value = qtyStr,
          onValueChange = { qtyStr = it.filter { char -> char.isDigit() || char == '.' } },
          label = { Text("Jumlah Pengurangan (${item.unit})") },
          placeholder = { Text("Contoh: 1") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = customNote,
          onValueChange = { customNote = it },
          label = { Text("Catatan / Keterangan Tambahan") },
          placeholder = { Text("Contoh: Kemasan pecah saat bongkar") },
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (qty > 0) {
            val finalNote = if (customNote.isNotBlank()) "$selectedReason: $customNote" else selectedReason
            onConfirm(qty, finalNote)
          }
        },
        enabled = qty > 0,
        colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
      ) {
        Text("Kurangi Stok")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text("Batal")
      }
    }
  )
}

// -------------------------------------------------------------------------------------------------
// MODAL: EDIT ITEM (BARANG / BAHAN)
// -------------------------------------------------------------------------------------------------
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
