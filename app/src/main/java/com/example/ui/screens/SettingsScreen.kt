package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Storefront
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
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.BusinessType
import com.example.ui.components.ConfirmationDialog
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.PrimaryGreenLight
import com.example.ui.viewmodel.MainViewModel

@Composable
fun SettingsScreen(
  viewModel: MainViewModel,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val clipboard = LocalClipboardManager.current
  val profile by viewModel.businessProfile.collectAsState()

  // Business profile form
  var businessName by remember(profile) { mutableStateOf(profile?.name ?: "Bisnis Saya") }
  var phone by remember(profile) { mutableStateOf(profile?.phone ?: "") }
  var address by remember(profile) { mutableStateOf(profile?.address ?: "") }

  // Dialogs
  var showBackupDialog by remember { mutableStateOf(false) }
  var backupJsonContent by remember { mutableStateOf("") }
  var showRestoreDialog by remember { mutableStateOf(false) }
  var restoreInputText by remember { mutableStateOf("") }
  var showResetConfirm by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("settings_screen")
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(8.dp))
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onNavigateBack) {
          Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Kembali")
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "Pengaturan Usaha",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
      }
    }

    // 1. Profil Usaha
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Storefront, contentDescription = null, tint = PrimaryGreenLight)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Profil Usaha",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = businessName,
            onValueChange = { businessName = it },
            label = { Text("Nama Usaha / Toko") },
            placeholder = { Text("Contoh: Warung Berkah / Dapur Kue Enak") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Nomor WhatsApp (Opsional)") },
            placeholder = { Text("08123456789") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Alamat Usaha (Opsional)") },
            placeholder = { Text("Contoh: Jl. Merdeka No. 12") },
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(14.dp))

          Button(
            onClick = {
              viewModel.saveBusinessProfile(businessName, phone, address)
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreenLight),
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(imageVector = Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Simpan Pengaturan")
          }
        }
      }
    }

    // 2. Tema Tampilan (Terang / Gelap)
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Palette, contentDescription = null, tint = PrimaryGreenLight)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Tema Tampilan Aplikasi",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          val currentTheme = profile?.themeSetting ?: "SYSTEM"

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .clickable { viewModel.setThemeSetting("LIGHT") }
              .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            RadioButton(
              selected = currentTheme == "LIGHT",
              onClick = { viewModel.setThemeSetting("LIGHT") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("☀️ Tema Terang", fontWeight = FontWeight.Medium)
          }

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .clickable { viewModel.setThemeSetting("DARK") }
              .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            RadioButton(
              selected = currentTheme == "DARK",
              onClick = { viewModel.setThemeSetting("DARK") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("🌙 Tema Gelap", fontWeight = FontWeight.Medium)
          }

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .clickable { viewModel.setThemeSetting("SYSTEM") }
              .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            RadioButton(
              selected = currentTheme == "SYSTEM",
              onClick = { viewModel.setThemeSetting("SYSTEM") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("📱 Ikuti Pengaturan HP", fontWeight = FontWeight.Medium)
          }
        }
      }
    }

    // 3. Cadangan & Pemulihan Data
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Backup, contentDescription = null, tint = PrimaryGreenLight)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Cadangan & Pemulihan Data",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Data tersimpan offline di HP ini. Anda dapat mencadangkan seluruh data usaha dan memulihkannya kapan saja.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(14.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Button(
              onClick = {
                viewModel.getBackupJson { json ->
                  backupJsonContent = json
                  showBackupDialog = true
                }
              },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreenLight)
            ) {
              Icon(imageVector = Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Cadangkan", fontSize = 12.sp)
            }

            OutlinedButton(
              onClick = {
                restoreInputText = ""
                showRestoreDialog = true
              },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(10.dp)
            ) {
              Icon(imageVector = Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Pulihkan", fontSize = 12.sp)
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          OutlinedButton(
            onClick = { showResetConfirm = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ExpenseRed)
          ) {
            Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Reset Semua Data Usaha", fontSize = 12.sp)
          }
        }
      }
    }

    // 3. Tentang Aplikasi
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Image(
            painter = painterResource(id = R.drawable.app_logo),
            contentDescription = "Logo Catat Bisnisku",
            modifier = Modifier.size(80.dp)
          )

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = "Catat Bisnisku",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = PrimaryGreen
          )

          Text(
            text = "Catat Usaha, Pantau Untungnya.",
            style = MaterialTheme.typography.bodyMedium,
            color = PrimaryGreenLight,
            fontWeight = FontWeight.Medium
          )

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = "Versi 1.0.0 • Offline First",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
          )

          Spacer(modifier = Modifier.height(8.dp))

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFE8F5E9),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
          ) {
            Text(
              text = "by Rafline Dev",
              style = MaterialTheme.typography.labelMedium,
              color = PrimaryGreen,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(64.dp))
    }
  }

  // Backup Share Dialog
  if (showBackupDialog) {
    AlertDialog(
      onDismissRequest = { showBackupDialog = false },
      title = { Text("File Cadangan Usaha Siap") },
      text = {
        Column {
          Text("Salin teks cadangan ini atau bagikan ke WhatsApp / Google Drive sebagai arsip data usaha Anda.")
          Spacer(modifier = Modifier.height(10.dp))
          OutlinedTextField(
            value = backupJsonContent,
            onValueChange = {},
            readOnly = true,
            maxLines = 6,
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val sendIntent = Intent().apply {
              action = Intent.ACTION_SEND
              putExtra(Intent.EXTRA_TEXT, backupJsonContent)
              type = "application/json"
            }
            context.startActivity(Intent.createChooser(sendIntent, "Simpan File Cadangan"))
            showBackupDialog = false
          }
        ) {
          Text("Bagikan / Simpan")
        }
      },
      dismissButton = {
        OutlinedButton(
          onClick = {
            clipboard.setText(AnnotatedString(backupJsonContent))
            showBackupDialog = false
          }
        ) {
          Text("Salin ke Clipboard")
        }
      }
    )
  }

  // Restore Dialog
  if (showRestoreDialog) {
    AlertDialog(
      onDismissRequest = { showRestoreDialog = false },
      title = { Text("Pulihkan Cadangan Data") },
      text = {
        Column {
          Text("Tempel teks data cadangan Catat Bisnisku yang pernah Anda simpan.")
          Spacer(modifier = Modifier.height(10.dp))
          OutlinedTextField(
            value = restoreInputText,
            onValueChange = { restoreInputText = it },
            placeholder = { Text("Tempel format JSON cadangan di sini...") },
            maxLines = 6,
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (restoreInputText.isNotBlank()) {
              viewModel.restoreFromJson(restoreInputText) { success, _ ->
                if (success) {
                  showRestoreDialog = false
                }
              }
            }
          },
          enabled = restoreInputText.isNotBlank()
        ) {
          Text("Pulihkan Sekarang")
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { showRestoreDialog = false }) {
          Text("Batal")
        }
      }
    )
  }

  // Reset Confirmation Dialog
  ConfirmationDialog(
    show = showResetConfirm,
    title = "Reset Semua Data Usaha?",
    message = "Seluruh data stok, penjualan, pengeluaran, dan produksi akan dihapus secara permanen. Pastikan Anda telah mencadangkan data penting terlebih dahulu.",
    confirmButtonText = "Ya, Hapus Semua",
    isDanger = true,
    onConfirm = { viewModel.resetAllData() },
    onDismiss = { showResetConfirm = false }
  )
}
