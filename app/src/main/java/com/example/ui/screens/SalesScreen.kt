package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Item
import com.example.data.model.Sale
import com.example.ui.components.EmptyStateView
import com.example.ui.components.formatRupiah
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.PrimaryGreenLight
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SalesScreen(
  viewModel: MainViewModel,
  onOpenAddSale: () -> Unit,
  modifier: Modifier = Modifier
) {
  val sales by viewModel.sales.collectAsState()
  var searchQuery by remember { mutableStateOf("") }
  var selectedSaleDetail by remember { mutableStateOf<Sale?>(null) }
  var saleToDelete by remember { mutableStateOf<Sale?>(null) }

  val filteredSales = sales.filter {
    it.itemName.contains(searchQuery, ignoreCase = true) ||
      it.invoiceNumber.contains(searchQuery, ignoreCase = true) ||
      it.customerName.contains(searchQuery, ignoreCase = true)
  }

  val totalPeriodSales = filteredSales.sumOf { it.totalPrice }
  val totalGrossProfit = filteredSales.sumOf { it.grossProfit }

  Column(
    modifier = modifier
      .fillMaxSize()
      .testTag("sales_screen")
  ) {
    // Header
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
            text = "Catatan Penjualan",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1
          )
          Text(
            text = "Total: ${formatRupiah(totalPeriodSales)} • Untung: ${formatRupiah(totalGrossProfit)}",
            style = MaterialTheme.typography.bodySmall,
            color = PrimaryGreenLight,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text("Cari produk, invoice, atau pelanggan...") },
        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
      )
    }

    if (filteredSales.isEmpty()) {
      EmptyStateView(
        title = "Belum Ada Transaksi Penjualan",
        description = if (searchQuery.isNotEmpty()) "Tidak ditemukan transaksi sesuai pencarian" else "Catat penjualan pertama Anda. Stok akan berkurang otomatis dan keuntungan langsung dihitung.",
        icon = Icons.Default.PointOfSale,
        actionButtonText = "+ Catat Penjualan",
        onActionClick = onOpenAddSale,
        modifier = Modifier.weight(1f)
      )
    } else {
      LazyColumn(
        modifier = Modifier
          .weight(1f)
          .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(filteredSales, key = { it.id }) { sale ->
          SaleItemCard(
            sale = sale,
            onClick = { selectedSaleDetail = sale },
            onDelete = { saleToDelete = sale }
          )
        }
        item { Spacer(modifier = Modifier.height(72.dp)) }
      }
    }
  }

  // Detail / Struk Dialog
  selectedSaleDetail?.let { sale ->
    SaleReceiptDialog(
      sale = sale,
      onDismiss = { selectedSaleDetail = null }
    )
  }

  // Delete Confirm Dialog
  saleToDelete?.let { sale ->
    AlertDialog(
      onDismissRequest = { saleToDelete = null },
      title = { Text("Batalkan Penjualan?") },
      text = {
        Text("Penjualan #${sale.invoiceNumber} (${sale.itemName}) akan dibatalkan dan stok barang (${sale.qty} ${sale.unit}) akan dikembalikan.")
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.deleteSale(sale)
            saleToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
        ) {
          Text("Batalkan & Kembalikan Stok")
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { saleToDelete = null }) {
          Text("Tutup")
        }
      }
    )
  }
}

@Composable
fun SaleItemCard(
  sale: Sale,
  onClick: () -> Unit,
  onDelete: () -> Unit
) {
  val dateFormatted = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("in", "ID")).format(Date(sale.date))

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("sale_card_${sale.id}"),
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
            text = sale.itemName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "${sale.invoiceNumber} • $dateFormatted",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
          )
        }

        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
          Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus", tint = ExpenseRed, modifier = Modifier.size(16.dp))
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "${sale.qty} ${sale.unit} @ ${formatRupiah(sale.sellingPrice)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          if (sale.discount > 0) {
            Text(
              text = "Diskon: -${formatRupiah(sale.discount)}",
              style = MaterialTheme.typography.bodySmall,
              color = ExpenseRed,
              fontSize = 11.sp
            )
          }
        }

        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = formatRupiah(sale.totalPrice),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = PrimaryGreenLight
          )
          Text(
            text = "Untung: +${formatRupiah(sale.grossProfit)}",
            style = MaterialTheme.typography.bodySmall,
            color = PrimaryGreen,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp
          )
        }
      }

      if (sale.customerName.isNotBlank() || sale.notes.isNotBlank()) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = listOfNotNull(
            if (sale.customerName.isNotBlank()) "Pelanggan: ${sale.customerName}" else null,
            if (sale.notes.isNotBlank()) sale.notes else null
          ).joinToString(" • "),
          style = MaterialTheme.typography.bodySmall,
          color = Color.Gray,
          fontSize = 11.sp
        )
      }
    }
  }
}

@Composable
fun SaleReceiptDialog(
  sale: Sale,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val dateFormatted = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("in", "ID")).format(Date(sale.date))

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = Icons.Default.Receipt, contentDescription = null, tint = PrimaryGreenLight)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Struk Penjualan", fontWeight = FontWeight.Bold)
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Text("Nomor: ${sale.invoiceNumber}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        Text("Waktu: $dateFormatted", fontSize = 12.sp, color = Color.Gray)
        if (sale.customerName.isNotBlank()) {
          Text("Pelanggan: ${sale.customerName}", fontSize = 12.sp)
        }
        Text("Metode: ${sale.paymentMethod}", fontSize = 12.sp)

        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.LightGray))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Text(sale.itemName, fontWeight = FontWeight.Bold)
          Text("${sale.qty} ${sale.unit}")
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Text("Harga Satuan")
          Text(formatRupiah(sale.sellingPrice))
        }
        if (sale.discount > 0) {
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Potongan Diskon")
            Text("-${formatRupiah(sale.discount)}", color = ExpenseRed)
          }
        }

        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.LightGray))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Text("TOTAL PENJUALAN", fontWeight = FontWeight.Bold)
          Text(formatRupiah(sale.totalPrice), fontWeight = FontWeight.Bold, color = PrimaryGreenLight, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(4.dp))
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Text("Perhitungan Keuntungan Transaksi:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text("Modal Terjual: ${formatRupiah(sale.totalCost)} (${formatRupiah(sale.unitCostPrice)} / unit)", style = MaterialTheme.typography.bodySmall)
            Text("Keuntungan Transaksi: ${formatRupiah(sale.grossProfit)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = PrimaryGreen)
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val shareText = """
            *STRUK PENJUALAN*
            Invoice: ${sale.invoiceNumber}
            Tanggal: $dateFormatted
            ${if (sale.customerName.isNotBlank()) "Pelanggan: ${sale.customerName}\n" else ""}
            Barang: ${sale.itemName}
            Jumlah: ${sale.qty} ${sale.unit}
            Harga: ${formatRupiah(sale.sellingPrice)}
            ${if (sale.discount > 0) "Diskon: -${formatRupiah(sale.discount)}\n" else ""}
            ----------------------
            *TOTAL: ${formatRupiah(sale.totalPrice)}*
            Pembayaran: ${sale.paymentMethod}
            
            Dicatat melalui Catat Bisnisku
            Catat Usaha, Pantau Untungnya.
          """.trimIndent()

          val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
          }
          val shareIntent = Intent.createChooser(sendIntent, "Bagikan Struk Penjualan")
          context.startActivity(shareIntent)
        },
        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreenLight)
      ) {
        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Bagikan Struk")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text("Tutup")
      }
    }
  )
}
