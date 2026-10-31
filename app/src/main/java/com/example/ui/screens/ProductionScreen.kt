package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.model.ProductionRecord
import com.example.ui.components.EmptyStateView
import com.example.ui.components.formatRupiah
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.PrimaryGreenLight
import com.example.ui.theme.SecondaryTeal
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SelectedMaterialInput(
  var rawItemId: Long = 0L,
  var rawItemName: String = "",
  var unit: String = "",
  var qtyUsedStr: String = "",
  var costPerUnit: Double = 0.0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductionScreen(
  viewModel: MainViewModel,
  initialTargetProduct: Item? = null,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val products by viewModel.products.collectAsState()
  val rawMaterials by viewModel.rawMaterials.collectAsState()
  val productionRecords by viewModel.productionRecords.collectAsState()

  var selectedTab by remember { mutableIntStateOf(0) } // 0: Form Produksi, 1: Riwayat Produksi

  // Form State
  var selectedProduct by remember {
    mutableStateOf(initialTargetProduct ?: products.firstOrNull())
  }
  var outputQtyStr by remember { mutableStateOf("") }
  var notesStr by remember { mutableStateOf("") }

  // Dynamic ingredient rows
  val materialsInputs = remember {
    mutableStateListOf<SelectedMaterialInput>().apply {
      if (rawMaterials.isNotEmpty()) {
        val first = rawMaterials.first()
        add(SelectedMaterialInput(first.id, first.name, first.unit, "", first.costPrice))
      }
    }
  }

  // Calculate live cost
  val totalCalculatedMaterialCost = materialsInputs.sumOf { input ->
    val q = input.qtyUsedStr.toDoubleOrNull() ?: 0.0
    q * input.costPerUnit
  }
  val outQty = outputQtyStr.toDoubleOrNull() ?: 0.0
  val estimatedUnitCost = if (outQty > 0) totalCalculatedMaterialCost / outQty else 0.0

  Column(
    modifier = modifier
      .fillMaxSize()
      .testTag("production_screen")
  ) {
    // Top Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(onClick = onNavigateBack) {
        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Kembali")
      }
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = "Mulai Produksi Barang",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )
    }

    TabRow(selectedTabIndex = selectedTab) {
      Tab(
        selected = selectedTab == 0,
        onClick = { selectedTab = 0 },
        text = { Text("Form Produksi", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
      )
      Tab(
        selected = selectedTab == 1,
        onClick = { selectedTab = 1 },
        text = { Text("Riwayat (${productionRecords.size})", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
      )
    }

    if (selectedTab == 0) {
      if (products.isEmpty() || rawMaterials.isEmpty()) {
        EmptyStateView(
          title = "Data Belum Lengkap",
          description = "Untuk melakukan produksi, Anda harus memiliki minimal 1 Bahan Baku dan 1 Produk Jadi pada menu Stok.",
          icon = Icons.Default.Build,
          actionButtonText = "Kembali ke Stok",
          onActionClick = onNavigateBack,
          modifier = Modifier.weight(1f)
        )
      } else {
        LazyColumn(
          modifier = Modifier
            .weight(1f)
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          // 1. Output Product Section
          item {
            Card(
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Text(
                  text = "1. Produk yang Dihasilkan",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = PrimaryGreenLight
                )
                Spacer(modifier = Modifier.height(10.dp))

                var productDropdownExpanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                  expanded = productDropdownExpanded,
                  onExpandedChange = { productDropdownExpanded = !productDropdownExpanded }
                ) {
                  OutlinedTextField(
                    value = selectedProduct?.name ?: "Pilih produk...",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Pilih Produk Jadi") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productDropdownExpanded) },
                    modifier = Modifier
                      .menuAnchor()
                      .fillMaxWidth()
                  )
                  ExposedDropdownMenu(
                    expanded = productDropdownExpanded,
                    onDismissRequest = { productDropdownExpanded = false }
                  ) {
                    products.forEach { prod ->
                      DropdownMenuItem(
                        text = { Text("${prod.name} (Stok: ${prod.stock} ${prod.unit})") },
                        onClick = {
                          selectedProduct = prod
                          productDropdownExpanded = false
                        }
                      )
                    }
                  }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                  value = outputQtyStr,
                  onValueChange = { outputQtyStr = it.filter { char -> char.isDigit() || char == '.' } },
                  label = { Text("Jumlah yang Dihasilkan (${selectedProduct?.unit ?: "Unit"})") },
                  placeholder = { Text("Contoh: 20") },
                  singleLine = true,
                  modifier = Modifier.fillMaxWidth()
                )
              }
            }
          }

          // 2. Ingredients Section
          item {
            Card(
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "2. Bahan Baku yang Digunakan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreenLight
                  )

                  OutlinedButton(
                    onClick = {
                      val candidate = rawMaterials.firstOrNull { raw -> materialsInputs.none { it.rawItemId == raw.id } }
                        ?: rawMaterials.first()
                      materialsInputs.add(
                        SelectedMaterialInput(candidate.id, candidate.name, candidate.unit, "", candidate.costPrice)
                      )
                    },
                    shape = RoundedCornerShape(8.dp)
                  ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tambah Bahan", fontSize = 12.sp)
                  }
                }

                Spacer(modifier = Modifier.height(12.dp))

                materialsInputs.forEachIndexed { index, input ->
                  var exp by remember { mutableStateOf(false) }

                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    Box(modifier = Modifier.weight(1.5f)) {
                      ExposedDropdownMenuBox(
                        expanded = exp,
                        onExpandedChange = { exp = !exp }
                      ) {
                        OutlinedTextField(
                          value = input.rawItemName,
                          onValueChange = {},
                          readOnly = true,
                          label = { Text("Bahan #${index + 1}") },
                          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = exp) },
                          modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                          expanded = exp,
                          onDismissRequest = { exp = false }
                        ) {
                          rawMaterials.forEach { raw ->
                            DropdownMenuItem(
                              text = { Text("${raw.name} (Stok: ${raw.stock} ${raw.unit})") },
                              onClick = {
                                materialsInputs[index] = input.copy(
                                  rawItemId = raw.id,
                                  rawItemName = raw.name,
                                  unit = raw.unit,
                                  costPerUnit = raw.costPrice
                                )
                                exp = false
                              }
                            )
                          }
                        }
                      }
                    }

                    OutlinedTextField(
                      value = input.qtyUsedStr,
                      onValueChange = { newVal ->
                        materialsInputs[index] = input.copy(
                          qtyUsedStr = newVal.filter { char -> char.isDigit() || char == '.' }
                        )
                      },
                      label = { Text("Jumlah (${input.unit})") },
                      placeholder = { Text("0") },
                      singleLine = true,
                      modifier = Modifier.weight(1f)
                    )

                    IconButton(
                      onClick = {
                        if (materialsInputs.size > 1) {
                          materialsInputs.removeAt(index)
                        }
                      },
                      enabled = materialsInputs.size > 1
                    ) {
                      Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus Baris", tint = ExpenseRed)
                    }
                  }
                }
              }
            }
          }

          // 3. Live Cost Calculation Preview
          item {
            Card(
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Text(
                  text = "3. Perhitungan Modal Produksi Otomatis",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text("Total Biaya Bahan Baku:")
                  Text(formatRupiah(totalCalculatedMaterialCost), fontWeight = FontWeight.Bold)
                }

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text("Jumlah Produk Jadi:")
                  Text("$outQty ${selectedProduct?.unit ?: ""}", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(6.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.LightGray))
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text("Modal per Unit Produk:", fontWeight = FontWeight.Bold)
                  Text(
                    formatRupiah(estimatedUnitCost),
                    fontWeight = FontWeight.Bold,
                    color = PrimaryGreenLight,
                    fontSize = 16.sp
                  )
                }
              }
            }
          }

          // Notes
          item {
            OutlinedTextField(
              value = notesStr,
              onValueChange = { notesStr = it },
              label = { Text("Catatan Produksi (Opsional)") },
              placeholder = { Text("Contoh: Batch pagi pesanan bu Siti") },
              modifier = Modifier.fillMaxWidth()
            )
          }

          // Submit Button
          item {
            val canSubmit = selectedProduct != null &&
              outQty > 0 &&
              materialsInputs.all { (it.qtyUsedStr.toDoubleOrNull() ?: 0.0) > 0 }

            Button(
              onClick = {
                val prod = selectedProduct ?: return@Button
                val list = materialsInputs.map {
                  Triple(it.rawItemId, it.qtyUsedStr.toDoubleOrNull() ?: 0.0, it.costPerUnit)
                }
                viewModel.recordProduction(
                  outputItemId = prod.id,
                  outputQty = outQty,
                  materials = list,
                  notes = notesStr,
                  onSuccess = {
                    onNavigateBack()
                  }
                )
              },
              enabled = canSubmit,
              modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
              Icon(imageVector = Icons.Default.Build, contentDescription = null)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Simpan & Perbarui Stok Usaha", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(48.dp))
          }
        }
      }
    } else {
      // Riwayat Produksi
      if (productionRecords.isEmpty()) {
        EmptyStateView(
          title = "Belum Ada Riwayat Produksi",
          description = "Riwayat hasil produksi beserta modal dan bahan yang dipakai akan tersimpan di sini.",
          icon = Icons.Default.History,
          modifier = Modifier.weight(1f)
        )
      } else {
        LazyColumn(
          modifier = Modifier
            .weight(1f)
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(productionRecords) { record ->
            ProductionRecordCard(record)
          }
        }
      }
    }
  }
}

@Composable
fun ProductionRecordCard(record: ProductionRecord) {
  val dateStr = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("in", "ID")).format(Date(record.date))

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = record.outputItemName,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "+${record.outputQty} ${record.outputUnit}",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = PrimaryGreenLight
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = "Total Modal Bahan: ${formatRupiah(record.totalMaterialCost)}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = "Modal: ${formatRupiah(record.unitCost)} / ${record.outputUnit}",
          style = MaterialTheme.typography.bodySmall,
          fontWeight = FontWeight.SemiBold,
          color = PrimaryGreen
        )
      }

      if (record.notes.isNotBlank()) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "\"${record.notes}\"",
          style = MaterialTheme.typography.bodySmall,
          color = Color.Gray
        )
      }

      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = dateStr,
        style = MaterialTheme.typography.labelSmall,
        color = Color.Gray
      )
    }
  }
}
