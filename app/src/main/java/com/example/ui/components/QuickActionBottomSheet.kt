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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.PrimaryGreenLight
import com.example.ui.theme.SecondaryTeal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickActionBottomSheet(
  isProduksi: Boolean,
  onDismiss: () -> Unit,
  onActionTambahBarangOrBahan: () -> Unit,
  onActionTambahStokOrProduksi: () -> Unit,
  onActionCatatPenjualan: () -> Unit,
  onActionCatatPengeluaran: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 8.dp)
        .testTag("quick_action_sheet"),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Text(
        text = "Catat Cepat Usaha",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 6.dp)
      )

      if (!isProduksi) {
        // Usaha Jual Beli options
        QuickActionItem(
          title = "Tambah Barang Baru",
          subtitle = "Daftarkan produk atau barang dagangan baru",
          icon = Icons.Default.Inventory,
          tint = PrimaryGreenLight,
          bgColor = Color(0xFFE8F5E9),
          testTag = "action_tambah_barang",
          onClick = {
            onDismiss()
            onActionTambahBarangOrBahan()
          }
        )

        QuickActionItem(
          title = "Tambah Stok Barang",
          subtitle = "Catat barang masuk dari supplier & perbarui modal",
          icon = Icons.Default.Add,
          tint = SecondaryTeal,
          bgColor = Color(0xFFE0F2F1),
          testTag = "action_tambah_stok",
          onClick = {
            onDismiss()
            onActionTambahStokOrProduksi()
          }
        )
      } else {
        // Usaha Produksi options
        QuickActionItem(
          title = "Tambah Bahan Baku",
          subtitle = "Daftarkan tepung, telur, gula, kain, dll.",
          icon = Icons.Default.Inventory,
          tint = SecondaryTeal,
          bgColor = Color(0xFFE0F2F1),
          testTag = "action_tambah_bahan",
          onClick = {
            onDismiss()
            onActionTambahBarangOrBahan()
          }
        )

        QuickActionItem(
          title = "Mulai Produksi",
          subtitle = "Olah bahan baku jadi produk jadi & hitung modal unit",
          icon = Icons.Default.Build,
          tint = PrimaryGreen,
          bgColor = Color(0xFFE8F5E9),
          testTag = "action_mulai_produksi",
          onClick = {
            onDismiss()
            onActionTambahStokOrProduksi()
          }
        )
      }

      // Both Business Types have Penjualan & Pengeluaran
      QuickActionItem(
        title = "Catat Penjualan",
        subtitle = "Kurangi stok otomatis & hitung keuntungan langsung",
        icon = Icons.Default.PointOfSale,
        tint = PrimaryGreenLight,
        bgColor = Color(0xFFE8F5E9),
        testTag = "action_catat_penjualan",
        onClick = {
          onDismiss()
          onActionCatatPenjualan()
        }
      )

      QuickActionItem(
        title = "Catat Pengeluaran",
        subtitle = "Biaya operasional seperti listrik, gas, gaji, sewa, bensin",
        icon = Icons.Default.ReceiptLong,
        tint = ExpenseRed,
        bgColor = Color(0xFFFFEBEE),
        testTag = "action_catat_pengeluaran",
        onClick = {
          onDismiss()
          onActionCatatPengeluaran()
        }
      )

      Spacer(modifier = Modifier.height(28.dp))
    }
  }
}

@Composable
fun QuickActionItem(
  title: String,
  subtitle: String,
  icon: ImageVector,
  tint: Color,
  bgColor: Color,
  testTag: String,
  onClick: () -> Unit
) {
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .clickable { onClick() }
      .testTag(testTag),
    shape = RoundedCornerShape(14.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .background(bgColor),
        contentAlignment = Alignment.Center
      ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    }
  }
}
