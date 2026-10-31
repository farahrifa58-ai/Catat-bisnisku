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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.data.model.MovementType
import com.example.data.model.StockMovement
import com.example.ui.components.CalculationRow
import com.example.ui.components.EmptyStateView
import com.example.ui.components.OfflineStatusBadge
import com.example.ui.components.PeriodFilterBar
import com.example.ui.components.SummaryCard
import com.example.ui.components.formatRupiah
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.PrimaryGreenLight
import com.example.ui.theme.SecondaryTeal
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
  viewModel: MainViewModel,
  onNavigateToStock: () -> Unit,
  onNavigateToSales: () -> Unit,
  onNavigateToExpenses: () -> Unit,
  onQuickAddStock: (Item) -> Unit,
  onOpenQuickAction: () -> Unit,
  modifier: Modifier = Modifier
) {
  val summary by viewModel.summary.collectAsState()
  val selectedPeriod by viewModel.selectedPeriod.collectAsState()
  val totalStockValue by viewModel.totalStockValue.collectAsState()
  val lowStockItems by viewModel.lowStockItems.collectAsState()
  val recentMovements by viewModel.stockMovements.collectAsState()
  val profile by viewModel.businessProfile.collectAsState()

  val isProduksi = profile?.type == BusinessType.PRODUKSI.name

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("dashboard_screen"),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Header Greeting & Business Mode
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = profile?.name ?: "Bisnis Saya",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1
            )
            Text(
              text = "Pencatatan Bahan, Produk & Keuntungan",
              style = MaterialTheme.typography.bodySmall,
              color = PrimaryGreenLight,
              fontWeight = FontWeight.SemiBold,
              maxLines = 1
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          OfflineStatusBadge()
        }
      }
    }

    // Period Filter
    item {
      PeriodFilterBar(
        selectedPeriod = selectedPeriod,
        onPeriodSelected = { viewModel.setPeriod(it) }
      )
    }

    // 4 Main Summary Cards in 2x2 Grid
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          SummaryCard(
            title = "Penjualan",
            amount = summary.totalSales,
            icon = Icons.Default.TrendingUp,
            accentColor = PrimaryGreenLight,
            subtitle = "${summary.salesCount} transaksi",
            modifier = Modifier
              .weight(1f)
              .clickable { onNavigateToSales() },
            testTag = "card_penjualan"
          )
          SummaryCard(
            title = "Pengeluaran",
            amount = summary.operationalExpenses,
            icon = Icons.Default.ReceiptLong,
            accentColor = ExpenseRed,
            subtitle = "Biaya operasional",
            modifier = Modifier
              .weight(1f)
              .clickable { onNavigateToExpenses() },
            testTag = "card_pengeluaran"
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          val isProfit = summary.netProfit >= 0
          SummaryCard(
            title = "Keuntungan Bersih",
            amount = summary.netProfit,
            icon = Icons.Default.MonetizationOn,
            accentColor = if (isProfit) PrimaryGreen else ExpenseRed,
            subtitle = "Laba setelah modal & biaya",
            modifier = Modifier.weight(1f),
            testTag = "card_keuntungan"
          )
          SummaryCard(
            title = "Nilai Stok",
            amount = totalStockValue,
            icon = Icons.Default.Inventory,
            accentColor = SecondaryTeal,
            subtitle = "Aset barang & bahan",
            modifier = Modifier
              .weight(1f)
              .clickable { onNavigateToStock() },
            testTag = "card_nilai_stok"
          )
        }
      }
    }

    // Breakdown Perhitungan Keuntungan (Simple & Transparent)
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Rincian Keuntungan Usaha (${selectedPeriod.title})",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(10.dp))

          CalculationRow(
            label = "Total Penjualan",
            amount = summary.totalSales,
            color = PrimaryGreenLight,
            prefix = "+"
          )
          CalculationRow(
            label = "Modal Barang Terjual",
            amount = summary.costOfGoodsSold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            prefix = "-"
          )
          CalculationRow(
            label = "Keuntungan Kotor",
            amount = summary.grossProfit,
            color = MaterialTheme.colorScheme.primary,
            isBold = true
          )
          CalculationRow(
            label = "Pengeluaran Operasional",
            amount = summary.operationalExpenses,
            color = ExpenseRed,
            prefix = "-"
          )

          Spacer(modifier = Modifier.height(8.dp))
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(1.dp)
              .background(MaterialTheme.colorScheme.outlineVariant)
          )
          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "Keuntungan Bersih",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = formatRupiah(summary.netProfit),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = if (summary.netProfit >= 0) PrimaryGreen else ExpenseRed
            )
          }
        }
      }
    }

    // Peringatan Stok Menipis
    if (lowStockItems.isNotEmpty()) {
      item {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = AccentAmber,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Peringatan Stok Menipis (${lowStockItems.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
              )
            }
            Text(
              text = "Lihat Semua",
              style = MaterialTheme.typography.bodySmall,
              color = PrimaryGreenLight,
              modifier = Modifier.clickable { onNavigateToStock() }
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            lowStockItems.take(3).forEach { item ->
              Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                  containerColor = if (item.isOutOfStock) Color(0xFFFFEBEE) else Color(0xFFFFF8E1)
                ),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = item.name,
                      fontWeight = FontWeight.Bold,
                      fontSize = 14.sp
                    )
                    Text(
                      text = if (item.isOutOfStock) "Stok HABIS (0 ${item.unit})" else "Tersisa ${item.stock} ${item.unit} (Min: ${item.minStockAlert})",
                      fontSize = 12.sp,
                      color = if (item.isOutOfStock) ExpenseRed else AccentAmber,
                      fontWeight = FontWeight.Medium
                    )
                  }
                  Button(
                    onClick = { onQuickAddStock(item) },
                    contentPadding = ButtonDefaults.TextButtonContentPadding,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreenLight)
                  ) {
                    Text("+ Stok", fontSize = 12.sp)
                  }
                }
              }
            }
          }
        }
      }
    }

    // Aktivitas Terbaru
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp)
      ) {
        Text(
          text = "Aktivitas Terakhir",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
      }
    }

    if (recentMovements.isEmpty()) {
      item {
        EmptyStateView(
          title = "Belum Ada Aktivitas Usaha",
          description = "Tekan tombol (+) di bawah untuk mencatat stok, penjualan, atau pengeluaran pertama Anda.",
          icon = Icons.Default.ReceiptLong,
          actionButtonText = "+ Catat Aktivitas",
          onActionClick = onOpenQuickAction,
          modifier = Modifier.padding(bottom = 24.dp)
        )
      }
    } else {
      items(recentMovements.take(5)) { movement ->
        MovementRowItem(
          movement = movement,
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
      }
      item {
        Spacer(modifier = Modifier.height(48.dp))
      }
    }
  }
}


@Composable
fun MovementRowItem(
  movement: StockMovement,
  modifier: Modifier = Modifier
) {
  val dateFormatted = SimpleDateFormat("dd MMM, HH:mm", Locale("in", "ID")).format(Date(movement.date))

  val (icon, tint, badgeBg) = when (movement.movementType) {
    MovementType.SALE.name -> Triple(Icons.Default.ArrowUpward, PrimaryGreenLight, Color(0xFFE8F5E9))
    MovementType.IN_PURCHASE.name -> Triple(Icons.Default.ArrowDownward, SecondaryTeal, Color(0xFFE0F2F1))
    MovementType.PRODUCTION_OUTPUT.name -> Triple(Icons.Default.Build, PrimaryGreen, Color(0xFFE8F5E9))
    MovementType.PRODUCTION_INPUT.name -> Triple(Icons.Default.Build, AccentAmber, Color(0xFFFFF8E1))
    else -> Triple(Icons.Default.Inventory, Color.Gray, Color(0xFFF5F5F5))
  }

  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(badgeBg),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = tint,
          modifier = Modifier.size(18.dp)
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = movement.itemName,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "${movement.note} • $dateFormatted",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 11.sp
        )
      }

      val sign = when (movement.movementType) {
        MovementType.IN_PURCHASE.name, MovementType.PRODUCTION_OUTPUT.name -> "+"
        else -> "-"
      }
      Text(
        text = "$sign${movement.qty}",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold,
        color = tint
      )
    }
  }
}
