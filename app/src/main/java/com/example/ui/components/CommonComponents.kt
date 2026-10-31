package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrimaryGreenLight
import com.example.ui.viewmodel.TimePeriod
import java.text.NumberFormat
import java.util.Locale

fun formatRupiah(amount: Double): String {
  val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
  formatter.maximumFractionDigits = 0
  val formatted = formatter.format(amount)
  // Format standard: Rp10.000 (menghapus spasi jika ada)
  return formatted.replace("Rp ", "Rp").replace("Rp", "Rp")
}

@Composable
fun SummaryCard(
  title: String,
  amount: Double,
  icon: ImageVector,
  accentColor: Color,
  modifier: Modifier = Modifier,
  subtitle: String? = null,
  testTag: String = "summary_card"
) {
  Card(
    modifier = modifier.testTag(testTag),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontWeight = FontWeight.Medium
        )
        Box(
          modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(accentColor.copy(alpha = 0.14f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(18.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = formatRupiah(amount),
        style = MaterialTheme.typography.titleLarge.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 18.sp
        ),
        color = MaterialTheme.colorScheme.onSurface
      )

      if (!subtitle.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontSize = 11.sp
        )
      }
    }
  }
}

@Composable
fun PeriodFilterBar(
  selectedPeriod: TimePeriod,
  onPeriodSelected: (TimePeriod) -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .horizontalScroll(rememberScrollState())
      .padding(horizontal = 16.dp, vertical = 6.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    TimePeriod.values().forEach { period ->
      FilterChip(
        selected = selectedPeriod == period,
        onClick = { onPeriodSelected(period) },
        label = {
          Text(
            text = period.title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selectedPeriod == period) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            softWrap = false
          )
        },
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = PrimaryGreenLight,
          selectedLabelColor = Color.White
        )
      )
    }
  }
}

@Composable
fun OfflineStatusBadge(modifier: Modifier = Modifier) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(16.dp),
    color = PrimaryGreenLight.copy(alpha = 0.12f)
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Icons.Default.CheckCircle,
        contentDescription = "Status Offline",
        tint = PrimaryGreenLight,
        modifier = Modifier.size(13.dp)
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = "Offline",
        style = MaterialTheme.typography.labelSmall,
        color = PrimaryGreenLight,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        softWrap = false
      )
    }
  }
}

@Composable
fun EmptyStateView(
  title: String,
  description: String,
  icon: ImageVector,
  actionButtonText: String? = null,
  onActionClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(32.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Box(
      modifier = Modifier
        .size(68.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.surfaceVariant),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(36.dp),
        tint = MaterialTheme.colorScheme.primary
      )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = title,
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(6.dp))

    Text(
      text = description,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center
    )

    if (actionButtonText != null && onActionClick != null) {
      Spacer(modifier = Modifier.height(18.dp))
      Button(
        onClick = onActionClick,
        shape = RoundedCornerShape(12.dp)
      ) {
        Text(actionButtonText)
      }
    }
  }
}

@Composable
fun ConfirmationDialog(
  show: Boolean,
  title: String,
  message: String,
  confirmButtonText: String = "Lanjutkan",
  cancelButtonText: String = "Batal",
  isDanger: Boolean = false,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit
) {
  if (show) {
    AlertDialog(
      onDismissRequest = onDismiss,
      icon = {
        Icon(
          imageVector = Icons.Default.Info,
          contentDescription = null,
          tint = if (isDanger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )
      },
      title = {
        Text(text = title, fontWeight = FontWeight.Bold)
      },
      text = {
        Text(text = message)
      },
      confirmButton = {
        Button(
          onClick = {
            onConfirm()
            onDismiss()
          },
          colors = if (isDanger) {
            ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
          } else {
            ButtonDefaults.buttonColors()
          }
        ) {
          Text(confirmButtonText)
        }
      },
      dismissButton = {
        OutlinedButton(onClick = onDismiss) {
          Text(cancelButtonText)
        }
      }
    )
  }
}

@Composable
fun CalculationRow(
  label: String,
  amount: Double,
  color: Color,
  prefix: String = "",
  isBold: Boolean = false
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 3.dp),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
    )
    Text(
      text = "$prefix ${formatRupiah(amount)}",
      style = MaterialTheme.typography.bodyMedium,
      color = color,
      fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium
    )
  }
}

