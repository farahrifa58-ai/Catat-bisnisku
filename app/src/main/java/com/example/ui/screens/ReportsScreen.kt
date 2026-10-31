package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CalculationRow
import com.example.ui.components.PeriodFilterBar
import com.example.ui.components.formatRupiah
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.PrimaryGreenLight
import com.example.ui.theme.SecondaryTeal
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportsScreen(
  viewModel: MainViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val profile by viewModel.businessProfile.collectAsState()
  val summary by viewModel.summary.collectAsState()
  val selectedPeriod by viewModel.selectedPeriod.collectAsState()
  val sales by viewModel.sales.collectAsState()
  val expenses by viewModel.expenses.collectAsState()

  // Top Selling Products Calculation
  val topProducts = sales
    .groupBy { it.itemName }
    .map { (name, list) ->
      Triple(name, list.sumOf { it.qty }, list.sumOf { it.totalPrice })
    }
    .sortedByDescending { it.second }
    .take(5)

  val maxQty = topProducts.maxOfOrNull { it.second } ?: 1.0

  // Largest Expense Categories
  val topExpenseCategories = expenses
    .groupBy { it.category }
    .map { (cat, list) -> Pair(cat, list.sumOf { it.amount }) }
    .sortedByDescending { it.second }
    .take(5)

  val maxExpenseAmount = topExpenseCategories.maxOfOrNull { it.second } ?: 1.0

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("reports_screen")
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Header
    item {
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Laporan Keuangan",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Pantau laba bersih & perkembangan usaha",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Button(
          modifier = Modifier.height(40.dp),
          onClick = {
            val reportText = """
              *LAPORAN USAHA: ${profile?.name ?: "Catat Bisnisku"}*
              Periode: ${selectedPeriod.title}
              Tanggal Export: ${SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("in", "ID")).format(Date())}
              
              *RINGKASAN KEUANGAN*
              - Total Penjualan: ${formatRupiah(summary.totalSales)} (${summary.salesCount} transaksi)
              - Modal Barang Terjual: ${formatRupiah(summary.costOfGoodsSold)}
              - Keuntungan Kotor: ${formatRupiah(summary.grossProfit)}
              - Biaya Operasional: ${formatRupiah(summary.operationalExpenses)} (${summary.expensesCount} transaksi)
              -------------------------------------
              *KEUNTUNGAN BERSIH: ${formatRupiah(summary.netProfit)}*
              
              *PRODUK TERLARIS:*
              ${topProducts.mapIndexed { idx, (name, qty, rev) -> "${idx + 1}. $name: $qty terjual (${formatRupiah(rev)})" }.joinToString("\n")}
              
              Dibuat dengan aplikasi Catat Bisnisku by Rafline Dev
              Catat Usaha, Pantau Untungnya.
            """.trimIndent()

            val sendIntent = Intent().apply {
              action = Intent.ACTION_SEND
              putExtra(Intent.EXTRA_TEXT, reportText)
              type = "text/plain"
            }
            context.startActivity(Intent.createChooser(sendIntent, "Ekspor Laporan Usaha"))
          },
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreenLight)
        ) {
          Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Ekspor", fontSize = 13.sp)
        }
      }
    }

    // Period selector
    item {
      PeriodFilterBar(
        selectedPeriod = selectedPeriod,
        onPeriodSelected = { viewModel.setPeriod(it) },
        modifier = Modifier.padding(horizontal = 0.dp)
      )
    }

    // Summary Card (Rincian Usaha)
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Assessment, contentDescription = null, tint = PrimaryGreenLight)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Ringkasan Laba Rugi (${selectedPeriod.title})",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          CalculationRow(label = "Total Penjualan", amount = summary.totalSales, color = PrimaryGreenLight, prefix = "+")
          CalculationRow(label = "Modal Barang Terjual", amount = summary.costOfGoodsSold, color = Color.Gray, prefix = "-")
          CalculationRow(label = "Keuntungan Kotor", amount = summary.grossProfit, color = MaterialTheme.colorScheme.primary, isBold = true)
          CalculationRow(label = "Biaya Operasional", amount = summary.operationalExpenses, color = ExpenseRed, prefix = "-")

          Spacer(modifier = Modifier.height(10.dp))
          Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.LightGray))
          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("KEUNTUNGAN BERSIH", fontWeight = FontWeight.Bold, fontSize = 14.sp)
              Text("Laba riil masuk kas", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            Text(
              text = formatRupiah(summary.netProfit),
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.ExtraBold,
              color = if (summary.netProfit >= 0) PrimaryGreen else ExpenseRed
            )
          }
        }
      }
    }

    // Visual Comparison Bar
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Grafik Perbandingan Keuangan",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(12.dp))

          val maxVal = maxOf(summary.totalSales, summary.costOfGoodsSold + summary.operationalExpenses, 1.0)

          ChartBarItem("Penjualan", summary.totalSales, maxVal, PrimaryGreenLight)
          ChartBarItem("Modal Terjual", summary.costOfGoodsSold, maxVal, Color(0xFF78909C))
          ChartBarItem("Biaya Operasional", summary.operationalExpenses, maxVal, ExpenseRed)
          ChartBarItem("Laba Bersih", summary.netProfit.coerceAtLeast(0.0), maxVal, PrimaryGreen)
        }
      }
    }

    // Top Selling Products
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFFBC02D))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Produk Terlaris",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          if (topProducts.isEmpty()) {
            Text("Belum ada penjualan di periode ini.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
          } else {
            topProducts.forEachIndexed { index, (name, qty, rev) ->
              Column(modifier = Modifier.padding(vertical = 4.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(
                    text = "${index + 1}. $name",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                  )
                  Text(
                    text = "$qty unit • ${formatRupiah(rev)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = PrimaryGreenLight
                  )
                }
                Spacer(modifier = Modifier.height(3.dp))
                LinearProgressIndicator(
                  progress = { (qty / maxQty).toFloat() },
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                  color = PrimaryGreenLight,
                  trackColor = Color(0xFFE8F5E9)
                )
              }
            }
          }
        }
      }
    }

    // Largest Expense Categories
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Category, contentDescription = null, tint = ExpenseRed)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Pengeluaran Terbesar",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          if (topExpenseCategories.isEmpty()) {
            Text("Belum ada pengeluaran operasional.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
          } else {
            topExpenseCategories.forEachIndexed { index, (cat, amount) ->
              Column(modifier = Modifier.padding(vertical = 4.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(
                    text = "${index + 1}. $cat",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                  )
                  Text(
                    text = formatRupiah(amount),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = ExpenseRed
                  )
                }
                Spacer(modifier = Modifier.height(3.dp))
                LinearProgressIndicator(
                  progress = { (amount / maxExpenseAmount).toFloat() },
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                  color = ExpenseRed,
                  trackColor = Color(0xFFFFEBEE)
                )
              }
            }
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(72.dp))
    }
  }
}

@Composable
fun ChartBarItem(
  label: String,
  value: Double,
  maxValue: Double,
  color: Color
) {
  val fraction = if (maxValue > 0) (value / maxValue).toFloat().coerceIn(0f, 1f) else 0f

  Column(modifier = Modifier.padding(vertical = 4.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(label, style = MaterialTheme.typography.bodySmall)
      Text(formatRupiah(value), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = color)
    }
    Spacer(modifier = Modifier.height(4.dp))
    LinearProgressIndicator(
      progress = { fraction },
      modifier = Modifier
        .fillMaxWidth()
        .height(8.dp)
        .clip(RoundedCornerShape(4.dp)),
      color = color,
      trackColor = Color(0xFFF0F0F0)
    )
  }
}
