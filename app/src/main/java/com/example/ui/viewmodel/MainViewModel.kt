package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.BusinessProfile
import com.example.data.model.BusinessType
import com.example.data.model.Expense
import com.example.data.model.Item
import com.example.data.model.ItemType
import com.example.data.model.ProductionRecord
import com.example.data.model.Sale
import com.example.data.model.StockMovement
import com.example.data.repository.BusinessRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class TimePeriod(val title: String) {
  HARI_INI("Hari Ini"),
  MINGGU_INI("Minggu Ini"),
  BULAN_INI("Bulan Ini"),
  TAHUN_INI("Tahun Ini"),
  SEMUA("Semua Waktu")
}

data class FinancialSummary(
  val totalSales: Double = 0.0,
  val costOfGoodsSold: Double = 0.0,
  val grossProfit: Double = 0.0,
  val operationalExpenses: Double = 0.0,
  val netProfit: Double = 0.0,
  val salesCount: Int = 0,
  val expensesCount: Int = 0
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

  private val repository: BusinessRepository

  init {
    val db = AppDatabase.getDatabase(application)
    repository = BusinessRepository(db)
  }

  val businessProfile: StateFlow<BusinessProfile?> = repository.businessProfile
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  val allItems: StateFlow<List<Item>> = repository.allItems
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val products: StateFlow<List<Item>> = repository.products
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val rawMaterials: StateFlow<List<Item>> = repository.rawMaterials
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val stockMovements: StateFlow<List<StockMovement>> = repository.stockMovements
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val sales: StateFlow<List<Sale>> = repository.allSales
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val expenses: StateFlow<List<Expense>> = repository.allExpenses
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val productionRecords: StateFlow<List<ProductionRecord>> = repository.productionRecords
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  private val _selectedPeriod = MutableStateFlow(TimePeriod.BULAN_INI)
  val selectedPeriod: StateFlow<TimePeriod> = _selectedPeriod.asStateFlow()

  fun setPeriod(period: TimePeriod) {
    _selectedPeriod.value = period
  }

  private val _snackbarMessage = MutableSharedFlow<String>()
  val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

  private fun getPeriodStartTimestamp(period: TimePeriod): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)

    return when (period) {
      TimePeriod.HARI_INI -> cal.timeInMillis
      TimePeriod.MINGGU_INI -> {
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.timeInMillis
      }
      TimePeriod.BULAN_INI -> {
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.timeInMillis
      }
      TimePeriod.TAHUN_INI -> {
        cal.set(Calendar.DAY_OF_YEAR, 1)
        cal.timeInMillis
      }
      TimePeriod.SEMUA -> 0L
    }
  }

  // Combined Financial Summary based on period
  val summary: StateFlow<FinancialSummary> = combine(
    sales,
    expenses,
    _selectedPeriod
  ) { salesList, expensesList, period ->
    val startTime = getPeriodStartTimestamp(period)

    val filteredSales = if (startTime == 0L) salesList else salesList.filter { it.date >= startTime }
    val filteredExpenses = if (startTime == 0L) expensesList else expensesList.filter { it.date >= startTime }

    val totalSales = filteredSales.sumOf { it.totalPrice }
    val cogs = filteredSales.sumOf { it.totalCost }
    val gross = totalSales - cogs
    val opExpenses = filteredExpenses.sumOf { it.amount }
    val net = gross - opExpenses

    FinancialSummary(
      totalSales = totalSales,
      costOfGoodsSold = cogs,
      grossProfit = gross,
      operationalExpenses = opExpenses,
      netProfit = net,
      salesCount = filteredSales.size,
      expensesCount = filteredExpenses.size
    )
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialSummary())

  // Total stock value
  val totalStockValue: StateFlow<Double> = allItems.combine(allItems) { items, _ ->
    items.sumOf { it.totalStockValue }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

  // Low stock items
  val lowStockItems: StateFlow<List<Item>> = allItems.combine(allItems) { items, _ ->
    items.filter { it.isLowStock }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  fun saveBusinessProfile(name: String, type: BusinessType, phone: String, address: String) {
    viewModelScope.launch {
      val existing = repository.getProfileSync() ?: BusinessProfile()
      repository.saveProfile(
        existing.copy(
          name = name.ifBlank { "Bisnis Saya" },
          type = type.name,
          phone = phone,
          address = address,
          isConfigured = true
        )
      )
      _snackbarMessage.emit("Profil usaha berhasil disimpan")
    }
  }

  fun saveBusinessProfile(name: String, phone: String, address: String) {
    viewModelScope.launch {
      val existing = repository.getProfileSync() ?: BusinessProfile()
      repository.saveProfile(
        existing.copy(
          name = name.ifBlank { "Bisnis Saya" },
          type = BusinessType.PRODUKSI.name,
          phone = phone,
          address = address,
          isConfigured = true
        )
      )
      _snackbarMessage.emit("Profil usaha berhasil disimpan")
    }
  }

  fun switchBusinessType(newType: BusinessType) {
    viewModelScope.launch {
      val existing = repository.getProfileSync() ?: BusinessProfile()
      repository.saveProfile(existing.copy(type = newType.name, isConfigured = true))
      _snackbarMessage.emit("Jenis usaha diubah ke ${newType.label}")
    }
  }

  fun setThemeSetting(setting: String) {
    viewModelScope.launch {
      val existing = repository.getProfileSync() ?: BusinessProfile()
      repository.saveProfile(existing.copy(themeSetting = setting))
      val msg = when (setting) {
        "LIGHT" -> "Tema terang aktif"
        "DARK" -> "Tema gelap aktif"
        else -> "Tema mengikuti pengaturan HP"
      }
      _snackbarMessage.emit(msg)
    }
  }

  fun addNewItem(
    name: String,
    itemType: ItemType,
    stock: Double,
    unit: String,
    costPrice: Double,
    sellingPrice: Double,
    minStockAlert: Double,
    notes: String
  ) {
    viewModelScope.launch {
      val item = Item(
        name = name.trim(),
        itemType = itemType.name,
        stock = stock,
        unit = unit.trim().ifBlank { "Pcs" },
        costPrice = costPrice,
        sellingPrice = if (itemType == ItemType.RAW_MATERIAL) 0.0 else sellingPrice,
        minStockAlert = minStockAlert,
        notes = notes.trim()
      )
      repository.addItem(item)
      val label = if (itemType == ItemType.RAW_MATERIAL) "Bahan baku" else "Barang"
      _snackbarMessage.emit("$label \"$name\" berhasil ditambahkan")
    }
  }

  fun updateItem(item: Item) {
    viewModelScope.launch {
      repository.updateItem(item)
      _snackbarMessage.emit("Data \"${item.name}\" berhasil diperbarui")
    }
  }

  fun deleteItem(item: Item) {
    viewModelScope.launch {
      repository.deleteItem(item)
      _snackbarMessage.emit("\"${item.name}\" dihapus")
    }
  }

  fun addStock(itemId: Long, qty: Double, costPrice: Double, note: String, recordExpense: Boolean = false) {
    viewModelScope.launch {
      repository.addStock(itemId, qty, costPrice, note)
      if (recordExpense && costPrice > 0 && qty > 0) {
        val totalCost = qty * costPrice
        val expenseNotes = if (note.isNotBlank()) note else "Pembelian stok bahan baku (+$qty)"
        repository.addExpense(
          title = "Beli Bahan Baku",
          amount = totalCost,
          category = "Bahan Baku",
          notes = expenseNotes
        )
      }
      _snackbarMessage.emit("Stok berhasil ditambah (+$qty)")
    }
  }

  fun reduceStock(itemId: Long, qty: Double, note: String) {
    viewModelScope.launch {
      repository.reduceStock(itemId, qty, note)
      _snackbarMessage.emit("Stok berhasil dikurangi (-$qty)")
    }
  }

  fun recordSale(
    itemId: Long,
    qty: Double,
    sellingPrice: Double,
    discount: Double,
    customerName: String,
    paymentMethod: String,
    notes: String,
    onSuccess: (Sale) -> Unit = {}
  ) {
    viewModelScope.launch {
      val result = repository.recordSale(
        itemId = itemId,
        qty = qty,
        sellingPrice = sellingPrice,
        discount = discount,
        customerName = customerName,
        paymentMethod = paymentMethod,
        notes = notes
      )
      result.onSuccess { sale ->
        _snackbarMessage.emit("Penjualan #${sale.invoiceNumber} berhasil dicatat!")
        onSuccess(sale)
      }.onFailure { err ->
        _snackbarMessage.emit("Gagal: ${err.message}")
      }
    }
  }

  fun deleteSale(sale: Sale) {
    viewModelScope.launch {
      repository.deleteSale(sale, restoreStock = true)
      _snackbarMessage.emit("Penjualan #${sale.invoiceNumber} dibatalkan & stok dikembalikan")
    }
  }

  fun recordProduction(
    outputItemId: Long,
    outputQty: Double,
    materials: List<Triple<Long, Double, Double>>,
    notes: String,
    onSuccess: () -> Unit = {}
  ) {
    viewModelScope.launch {
      val result = repository.recordProduction(outputItemId, outputQty, materials, notes)
      result.onSuccess {
        _snackbarMessage.emit("Produksi berhasil! Menghasilkan $outputQty unit")
        onSuccess()
      }.onFailure { err ->
        _snackbarMessage.emit("Gagal produksi: ${err.message}")
      }
    }
  }

  fun recordExpense(
    title: String,
    amount: Double,
    category: String,
    notes: String
  ) {
    viewModelScope.launch {
      repository.addExpense(title, amount, category, System.currentTimeMillis(), notes)
      _snackbarMessage.emit("Pengeluaran \"$title\" dicatat")
    }
  }

  fun deleteExpense(expense: Expense) {
    viewModelScope.launch {
      repository.deleteExpense(expense)
      _snackbarMessage.emit("Pengeluaran dihapus")
    }
  }

  fun getBackupJson(onResult: (String) -> Unit) {
    viewModelScope.launch {
      val json = repository.exportBackupJson()
      onResult(json)
    }
  }

  fun restoreFromJson(jsonString: String, onResult: (Boolean, String) -> Unit) {
    viewModelScope.launch {
      val result = repository.importBackupJson(jsonString)
      result.onSuccess {
        _snackbarMessage.emit("Data cadangan berhasil dipulihkan!")
        onResult(true, "Data berhasil dipulihkan!")
      }.onFailure { err ->
        _snackbarMessage.emit("Gagal memulihkan: ${err.message}")
        onResult(false, err.message ?: "Format data tidak valid")
      }
    }
  }

  fun resetAllData() {
    viewModelScope.launch {
      repository.resetAllData()
      _snackbarMessage.emit("Semua data usaha telah direset")
    }
  }
}
