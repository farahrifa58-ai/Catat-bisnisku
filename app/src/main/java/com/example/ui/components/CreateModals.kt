package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DEFAULT_EXPENSE_CATEGORIES
import com.example.data.model.Item
import com.example.data.model.ItemType
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.PrimaryGreenLight

val COMMON_UNITS = listOf("Pcs", "Kg", "Gram", "Liter", "Botol", "Dus", "Kotak", "Porsi", "Unit", "Bungkus")

@Composable
fun AddItemModal(
  initialType: ItemType = ItemType.PRODUCT,
  onDismiss: () -> Unit,
  onSave: (name: String, type: ItemType, stock: Double, unit: String, cost: Double, sell: Double, minStock: Double, notes: String) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var stockStr by remember { mutableStateOf("") }
  var unit by remember { mutableStateOf("Pcs") }
  var costStr by remember { mutableStateOf("") }
  var sellStr by remember { mutableStateOf("") }
  var minStockStr by remember { mutableStateOf("5") }
  var notes by remember { mutableStateOf("") }

  val isRawMaterial = initialType == ItemType.RAW_MATERIAL
  val title = if (isRawMaterial) "Tambah Bahan Baku Baru" else "Tambah Barang Dagangan"

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(title, fontWeight = FontWeight.Bold) },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text(if (isRawMaterial) "Nama Bahan (misal: Tepung Terigu)" else "Nama Barang (misal: Kopi Susu)") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_item_name")
        )

        // Quick unit picker
        Column {
          Text("Satuan Barang:", style = MaterialTheme.typography.labelSmall)
          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(vertical = 4.dp)
          ) {
            items(COMMON_UNITS) { u ->
              FilterChip(
                selected = unit.equals(u, ignoreCase = true),
                onClick = { unit = u },
                label = { Text(u, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = PrimaryGreenLight,
                  selectedLabelColor = Color.White
                )
              )
            }
          }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = stockStr,
            onValueChange = { stockStr = it.filter { char -> char.isDigit() || char == '.' } },
            label = { Text("Stok Awal ($unit)") },
            placeholder = { Text("0") },
            singleLine = true,
            modifier = Modifier.weight(1f)
          )
          OutlinedTextField(
            value = minStockStr,
            onValueChange = { minStockStr = it.filter { char -> char.isDigit() || char == '.' } },
            label = { Text("Batas Min") },
            placeholder = { Text("5") },
            singleLine = true,
            modifier = Modifier.weight(1f)
          )
        }

        OutlinedTextField(
          value = costStr,
          onValueChange = { costStr = it.filter { char -> char.isDigit() } },
          label = { Text(if (isRawMaterial) "Harga Beli per $unit (Rp)" else "Harga Modal Beli per $unit (Rp)") },
          placeholder = { Text("Contoh: 10000") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        if (!isRawMaterial) {
          OutlinedTextField(
            value = sellStr,
            onValueChange = { sellStr = it.filter { char -> char.isDigit() } },
            label = { Text("Harga Jual Konsumen per $unit (Rp)") },
            placeholder = { Text("Contoh: 15000") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }

        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text("Catatan Tambahan (Opsional)") },
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val stock = stockStr.toDoubleOrNull() ?: 0.0
          val cost = costStr.toDoubleOrNull() ?: 0.0
          val sell = sellStr.toDoubleOrNull() ?: 0.0
          val minStock = minStockStr.toDoubleOrNull() ?: 5.0
          onSave(name.trim(), initialType, stock, unit, cost, sell, minStock, notes)
          onDismiss()
        },
        enabled = name.isNotBlank(),
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreenLight)
      ) {
        Text("Simpan")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text("Batal")
      }
    }
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSaleModal(
  availableProducts: List<Item>,
  onDismiss: () -> Unit,
  onSave: (itemId: Long, qty: Double, price: Double, discount: Double, customer: String, payment: String, note: String) -> Unit
) {
  var selectedProduct by remember { mutableStateOf(availableProducts.firstOrNull()) }
  var qtyStr by remember { mutableStateOf("1") }
  var priceStr by remember { mutableStateOf(selectedProduct?.sellingPrice?.toInt()?.toString() ?: "") }
  var discountStr by remember { mutableStateOf("") }
  var customerName by remember { mutableStateOf("") }
  var paymentMethod by remember { mutableStateOf("Tunai") }
  var notes by remember { mutableStateOf("") }
  var dropdownExpanded by remember { mutableStateOf(false) }

  val qty = qtyStr.toDoubleOrNull() ?: 0.0
  val unitPrice = priceStr.toDoubleOrNull() ?: 0.0
  val discount = discountStr.toDoubleOrNull() ?: 0.0
  val totalPrice = ((qty * unitPrice) - discount).coerceAtLeast(0.0)
  val unitCost = selectedProduct?.costPrice ?: 0.0
  val totalCost = qty * unitCost
  val estimatedProfit = totalPrice - totalCost

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = Icons.Default.PointOfSale, contentDescription = null, tint = PrimaryGreenLight)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Catat Penjualan Baru", fontWeight = FontWeight.Bold)
      }
    },
    text = {
      if (availableProducts.isEmpty()) {
        Text("Belum ada produk atau barang dagangan yang dapat dijual. Tambahkan barang terlebih dahulu di menu Stok.")
      } else {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Dropdown Product Picker
          ExposedDropdownMenuBox(
            expanded = dropdownExpanded,
            onExpandedChange = { dropdownExpanded = !dropdownExpanded }
          ) {
            OutlinedTextField(
              value = selectedProduct?.name ?: "Pilih Produk...",
              onValueChange = {},
              readOnly = true,
              label = { Text("Pilih Produk / Barang") },
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
              modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
            )
            ExposedDropdownMenu(
              expanded = dropdownExpanded,
              onDismissRequest = { dropdownExpanded = false }
            ) {
              availableProducts.forEach { prod ->
                DropdownMenuItem(
                  text = {
                    Column {
                      Text(prod.name, fontWeight = FontWeight.Bold)
                      Text(
                        "Stok: ${prod.stock} ${prod.unit} • Harga: ${formatRupiah(prod.sellingPrice)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                      )
                    }
                  },
                  onClick = {
                    selectedProduct = prod
                    priceStr = prod.sellingPrice.toInt().toString()
                    dropdownExpanded = false
                  }
                )
              }
            }
          }

          selectedProduct?.let { prod ->
            Text(
              text = "Stok tersedia: ${prod.stock} ${prod.unit} (Modal: ${formatRupiah(prod.costPrice)})",
              style = MaterialTheme.typography.bodySmall,
              color = if (prod.isOutOfStock) ExpenseRed else PrimaryGreenLight,
              fontWeight = FontWeight.SemiBold
            )
          }

          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = qtyStr,
              onValueChange = { qtyStr = it.filter { char -> char.isDigit() || char == '.' } },
              label = { Text("Jumlah (${selectedProduct?.unit ?: "Pcs"})") },
              singleLine = true,
              modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
              value = priceStr,
              onValueChange = { priceStr = it.filter { char -> char.isDigit() } },
              label = { Text("Harga Jual (Rp)") },
              singleLine = true,
              modifier = Modifier.weight(1.3f)
            )
          }

          OutlinedTextField(
            value = discountStr,
            onValueChange = { discountStr = it.filter { char -> char.isDigit() } },
            label = { Text("Potongan Diskon (Rp, Opsional)") },
            placeholder = { Text("0") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          // Payment method picker
          Column {
            Text("Metode Pembayaran:", style = MaterialTheme.typography.labelSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              listOf("Tunai", "Transfer", "QRIS").forEach { method ->
                FilterChip(
                  selected = paymentMethod == method,
                  onClick = { paymentMethod = method },
                  label = { Text(method, fontSize = 12.sp) },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryGreenLight,
                    selectedLabelColor = Color.White
                  )
                )
              }
            }
          }

          OutlinedTextField(
            value = customerName,
            onValueChange = { customerName = it },
            label = { Text("Nama Pelanggan (Opsional)") },
            placeholder = { Text("Contoh: Bu Ani") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Catatan Transaksi (Opsional)") },
            modifier = Modifier.fillMaxWidth()
          )

          // Live Calculation Card
          Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Penjualan:", fontWeight = FontWeight.Bold)
                Text(formatRupiah(totalPrice), fontWeight = FontWeight.Bold, color = PrimaryGreenLight, fontSize = 16.sp)
              }
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Estimasi Keuntungan:", style = MaterialTheme.typography.bodySmall)
                Text(formatRupiah(estimatedProfit), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = PrimaryGreen)
              }
            }
          }
        }
      }
    },
    confirmButton = {
      if (availableProducts.isNotEmpty()) {
        val prod = selectedProduct
        val isValid = prod != null && qty > 0 && unitPrice >= 0

        Button(
          onClick = {
            if (prod != null) {
              onSave(prod.id, qty, unitPrice, discount, customerName, paymentMethod, notes)
              onDismiss()
            }
          },
          enabled = isValid,
          colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreenLight)
        ) {
          Text("Simpan Penjualan")
        }
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
fun AddExpenseModal(
  onDismiss: () -> Unit,
  onSave: (title: String, amount: Double, category: String, notes: String) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var amountStr by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf(DEFAULT_EXPENSE_CATEGORIES.first()) }
  var notes by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null, tint = ExpenseRed)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Catat Biaya Operasional", fontWeight = FontWeight.Bold)
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Helpful reminder
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = Color(0xFFE8F5E9),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              "Hanya untuk biaya usaha seperti listrik, gas, gaji, sewa, bensin. Belanja barang/bahan dicatat di menu Stok.",
              style = MaterialTheme.typography.labelSmall,
              fontSize = 11.sp,
              color = Color(0xFF1B5E20)
            )
          }
        }

        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Nama Pengeluaran") },
          placeholder = { Text("Contoh: Listrik kios / Bensin pengantaran") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = amountStr,
          onValueChange = { amountStr = it.filter { char -> char.isDigit() } },
          label = { Text("Nominal Pengeluaran (Rp)") },
          placeholder = { Text("Contoh: 50000") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        Column {
          Text("Kategori Pengeluaran:", style = MaterialTheme.typography.labelSmall)
          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(vertical = 4.dp)
          ) {
            items(DEFAULT_EXPENSE_CATEGORIES) { cat ->
              FilterChip(
                selected = selectedCategory == cat,
                onClick = { selectedCategory = cat },
                label = { Text(cat, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = ExpenseRed,
                  selectedLabelColor = Color.White
                )
              )
            }
          }
        }

        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text("Catatan Tambahan (Opsional)") },
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      val amount = amountStr.toDoubleOrNull() ?: 0.0
      Button(
        onClick = {
          onSave(title.trim(), amount, selectedCategory, notes.trim())
          onDismiss()
        },
        enabled = title.isNotBlank() && amount > 0,
        colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
      ) {
        Text("Simpan Pengeluaran")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text("Batal")
      }
    }
  )
}
