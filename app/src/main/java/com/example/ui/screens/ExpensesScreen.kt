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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Expense
import com.example.ui.components.EmptyStateView
import com.example.ui.components.formatRupiah
import com.example.ui.theme.ExpenseRed
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExpensesScreen(
  viewModel: MainViewModel,
  onOpenAddExpense: () -> Unit,
  onNavigateToStock: () -> Unit,
  modifier: Modifier = Modifier
) {
  val expenses by viewModel.expenses.collectAsState()
  var expenseToDelete by remember { mutableStateOf<Expense?>(null) }

  val totalExpenses = expenses.sumOf { it.amount }

  Column(
    modifier = modifier
      .fillMaxSize()
      .testTag("expenses_screen")
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
            text = "Pengeluaran Usaha",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1
          )
          Text(
            text = "Total: ${formatRupiah(totalExpenses)} (${expenses.size} transaksi)",
            style = MaterialTheme.typography.bodySmall,
            color = ExpenseRed,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Important Info Card to prevent double-cost
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = Color(0xFF2E7D32),
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "Tips Pencatatan Keuangan",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF1B5E20)
            )
            Text(
              text = "Catat biaya operasional (listrik, gaji, bensin, dll) di sini. Untuk belanja stok/bahan, catat lewat menu Stok agar modal tidak terhitung dua kali.",
              style = MaterialTheme.typography.bodySmall,
              fontSize = 11.sp,
              color = Color(0xFF2E7D32)
            )
          }
        }
      }
    }

    if (expenses.isEmpty()) {
      EmptyStateView(
        title = "Belum Ada Pengeluaran",
        description = "Catat biaya listrik, sewa, gaji, atau transportasi untuk memantau keuntungan bersih usaha Anda.",
        icon = Icons.Default.ReceiptLong,
        actionButtonText = "+ Catat Pengeluaran",
        onActionClick = onOpenAddExpense,
        modifier = Modifier.weight(1f)
      )
    } else {
      LazyColumn(
        modifier = Modifier
          .weight(1f)
          .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(expenses, key = { it.id }) { expense ->
          ExpenseItemCard(
            expense = expense,
            onDelete = { expenseToDelete = expense }
          )
        }
        item { Spacer(modifier = Modifier.height(72.dp)) }
      }
    }
  }

  // Delete Confirm Dialog
  expenseToDelete?.let { expense ->
    AlertDialog(
      onDismissRequest = { expenseToDelete = null },
      title = { Text("Hapus Catatan Pengeluaran?") },
      text = { Text("Pengeluaran \"${expense.title}\" sebesar ${formatRupiah(expense.amount)} akan dihapus.") },
      confirmButton = {
        Button(
          onClick = {
            viewModel.deleteExpense(expense)
            expenseToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Hapus")
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { expenseToDelete = null }) {
          Text("Batal")
        }
      }
    )
  }
}

@Composable
fun ExpenseItemCard(
  expense: Expense,
  onDelete: () -> Unit
) {
  val dateFormatted = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("in", "ID")).format(Date(expense.date))

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(42.dp)
          .clip(CircleShape)
          .background(Color(0xFFFFEBEE)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.ReceiptLong,
          contentDescription = null,
          tint = ExpenseRed,
          modifier = Modifier.size(20.dp)
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = expense.title,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "${expense.category} • $dateFormatted",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 11.sp
        )
        if (expense.notes.isNotBlank()) {
          Text(
            text = "\"${expense.notes}\"",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            fontSize = 11.sp
          )
        }
      }

      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = formatRupiah(expense.amount),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = ExpenseRed
        )
        IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
          Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus", tint = Color.Gray, modifier = Modifier.size(16.dp))
        }
      }
    }
  }
}
