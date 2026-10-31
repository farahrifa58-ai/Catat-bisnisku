package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.BusinessProfile
import com.example.data.model.BusinessType
import com.example.data.model.Expense
import com.example.data.model.Item
import com.example.data.model.ItemType
import com.example.data.model.MovementType
import com.example.data.model.ProductionMaterialUsage
import com.example.data.model.ProductionRecord
import com.example.data.model.Sale
import com.example.data.model.StockMovement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BusinessRepository(private val db: AppDatabase) {

  val businessProfile: Flow<BusinessProfile?> = db.businessDao().getProfile()
  val allItems: Flow<List<Item>> = db.itemDao().getAllItems()
  val products: Flow<List<Item>> = db.itemDao().getItemsByType(ItemType.PRODUCT.name)
  val rawMaterials: Flow<List<Item>> = db.itemDao().getItemsByType(ItemType.RAW_MATERIAL.name)
  val stockMovements: Flow<List<StockMovement>> = db.stockMovementDao().getAllMovements()
  val allSales: Flow<List<Sale>> = db.saleDao().getAllSales()
  val allExpenses: Flow<List<Expense>> = db.expenseDao().getAllExpenses()
  val productionRecords: Flow<List<ProductionRecord>> = db.productionDao().getAllRecords()

  suspend fun saveProfile(profile: BusinessProfile) = withContext(Dispatchers.IO) {
    db.businessDao().upsertProfile(profile)
  }

  suspend fun getProfileSync(): BusinessProfile? = withContext(Dispatchers.IO) {
    db.businessDao().getProfileSync()
  }

  suspend fun addItem(item: Item): Long = withContext(Dispatchers.IO) {
    val id = db.itemDao().insertItem(item)
    if (item.stock > 0) {
      db.stockMovementDao().insertMovement(
        StockMovement(
          itemId = id,
          itemName = item.name,
          movementType = MovementType.IN_PURCHASE.name,
          qty = item.stock,
          costPerUnit = item.costPrice,
          note = "Stok awal",
          date = System.currentTimeMillis()
        )
      )
    }
    id
  }

  suspend fun updateItem(item: Item) = withContext(Dispatchers.IO) {
    db.itemDao().updateItem(item.copy(updatedAt = System.currentTimeMillis()))
  }

  suspend fun deleteItem(item: Item) = withContext(Dispatchers.IO) {
    db.itemDao().deleteItem(item)
  }

  suspend fun addStock(itemId: Long, qty: Double, costPrice: Double, note: String) = withContext(Dispatchers.IO) {
    val item = db.itemDao().getItemByIdSync(itemId) ?: return@withContext
    val newStock = item.stock + qty
    // Gunakan harga modal baru jika diisi > 0, atau pertahankan jika 0
    val newCost = if (costPrice > 0.0) costPrice else item.costPrice
    db.itemDao().updateStockAndCost(
      id = itemId,
      newStock = newStock,
      newCostPrice = newCost,
      updatedAt = System.currentTimeMillis()
    )
    db.stockMovementDao().insertMovement(
      StockMovement(
        itemId = itemId,
        itemName = item.name,
        movementType = MovementType.IN_PURCHASE.name,
        qty = qty,
        costPerUnit = newCost,
        note = if (note.isBlank()) "Penambahan stok masuk" else note,
        date = System.currentTimeMillis()
      )
    )
  }

  suspend fun reduceStock(itemId: Long, qty: Double, note: String) = withContext(Dispatchers.IO) {
    val item = db.itemDao().getItemByIdSync(itemId) ?: return@withContext
    val newStock = (item.stock - qty).coerceAtLeast(0.0)
    db.itemDao().updateStock(
      id = itemId,
      newStock = newStock,
      updatedAt = System.currentTimeMillis()
    )
    db.stockMovementDao().insertMovement(
      StockMovement(
        itemId = itemId,
        itemName = item.name,
        movementType = MovementType.OUT_ADJUSTMENT.name,
        qty = qty,
        costPerUnit = item.costPrice,
        note = if (note.isBlank()) "Penyesuaian stok keluar" else note,
        date = System.currentTimeMillis()
      )
    )
  }

  suspend fun recordSale(
    itemId: Long,
    qty: Double,
    sellingPrice: Double,
    discount: Double,
    customerName: String,
    paymentMethod: String,
    notes: String,
    date: Long = System.currentTimeMillis()
  ): Result<Sale> = withContext(Dispatchers.IO) {
    val item = db.itemDao().getItemByIdSync(itemId)
      ?: return@withContext Result.failure(Exception("Barang tidak ditemukan"))

    val totalPrice = ((qty * sellingPrice) - discount).coerceAtLeast(0.0)
    val unitCostPrice = item.costPrice
    val totalCost = qty * unitCostPrice
    val grossProfit = totalPrice - totalCost

    val dateFormat = SimpleDateFormat("yyMMdd-HHmm", Locale.getDefault())
    val invoice = "INV-${dateFormat.format(Date(date))}-${(100..999).random()}"

    val sale = Sale(
      invoiceNumber = invoice,
      itemId = itemId,
      itemName = item.name,
      qty = qty,
      unit = item.unit,
      sellingPrice = sellingPrice,
      discount = discount,
      totalPrice = totalPrice,
      unitCostPrice = unitCostPrice,
      totalCost = totalCost,
      grossProfit = grossProfit,
      customerName = customerName.trim(),
      paymentMethod = paymentMethod,
      notes = notes.trim(),
      date = date
    )

    val saleId = db.saleDao().insertSale(sale)

    // Kurangi stok barang secara otomatis
    val newStock = item.stock - qty
    db.itemDao().updateStock(
      id = itemId,
      newStock = newStock,
      updatedAt = System.currentTimeMillis()
    )

    // Catat riwayat pergerakan stok
    db.stockMovementDao().insertMovement(
      StockMovement(
        itemId = itemId,
        itemName = item.name,
        movementType = MovementType.SALE.name,
        qty = qty,
        costPerUnit = unitCostPrice,
        note = "Penjualan #$invoice ${if (customerName.isNotBlank()) "ke $customerName" else ""}",
        date = date
      )
    )

    Result.success(sale.copy(id = saleId))
  }

  suspend fun deleteSale(sale: Sale, restoreStock: Boolean = true) = withContext(Dispatchers.IO) {
    db.saleDao().deleteSale(sale)
    if (restoreStock) {
      val item = db.itemDao().getItemByIdSync(sale.itemId)
      if (item != null) {
        val newStock = item.stock + sale.qty
        db.itemDao().updateStock(item.id, newStock, System.currentTimeMillis())
        db.stockMovementDao().insertMovement(
          StockMovement(
            itemId = item.id,
            itemName = item.name,
            movementType = MovementType.IN_PURCHASE.name,
            qty = sale.qty,
            costPerUnit = item.costPrice,
            note = "Pengembalian stok pembatalan penjualan #${sale.invoiceNumber}",
            date = System.currentTimeMillis()
          )
        )
      }
    }
  }

  suspend fun recordProduction(
    outputItemId: Long,
    outputQty: Double,
    materials: List<Triple<Long, Double, Double>>, // rawItemId, qtyUsed, costPerUnit
    notes: String,
    date: Long = System.currentTimeMillis()
  ): Result<ProductionRecord> = withContext(Dispatchers.IO) {
    val outputItem = db.itemDao().getItemByIdSync(outputItemId)
      ?: return@withContext Result.failure(Exception("Produk tujuan tidak ditemukan"))

    if (outputQty <= 0) {
      return@withContext Result.failure(Exception("Jumlah produk harus lebih dari 0"))
    }

    var totalCost = 0.0
    val materialUsages = mutableListOf<ProductionMaterialUsage>()

    for ((rawId, qtyUsed, passedCost) in materials) {
      val rawItem = db.itemDao().getItemByIdSync(rawId)
        ?: return@withContext Result.failure(Exception("Bahan baku ID $rawId tidak ditemukan"))

      val actualCostPerUnit = if (passedCost > 0.0) passedCost else rawItem.costPrice
      val subtotal = qtyUsed * actualCostPerUnit
      totalCost += subtotal

      // Kurangi stok bahan baku
      val newRawStock = (rawItem.stock - qtyUsed).coerceAtLeast(0.0)
      db.itemDao().updateStock(rawId, newRawStock, System.currentTimeMillis())

      // Catat pergerakan bahan
      db.stockMovementDao().insertMovement(
        StockMovement(
          itemId = rawId,
          itemName = rawItem.name,
          movementType = MovementType.PRODUCTION_INPUT.name,
          qty = qtyUsed,
          costPerUnit = actualCostPerUnit,
          note = "Digunakan produksi ${outputItem.name} ($outputQty ${outputItem.unit})",
          date = date
        )
      )

      materialUsages.add(
        ProductionMaterialUsage(
          productionRecordId = 0, // diisi setelah insert record
          rawItemId = rawId,
          rawItemName = rawItem.name,
          qtyUsed = qtyUsed,
          unit = rawItem.unit,
          costPerUnit = actualCostPerUnit,
          totalCost = subtotal
        )
      )
    }

    val unitCost = totalCost / outputQty

    val record = ProductionRecord(
      outputItemId = outputItemId,
      outputItemName = outputItem.name,
      outputQty = outputQty,
      outputUnit = outputItem.unit,
      totalMaterialCost = totalCost,
      unitCost = unitCost,
      notes = notes.trim(),
      date = date
    )

    val recordId = db.productionDao().insertRecord(record)
    val usagesWithRecordId = materialUsages.map { it.copy(productionRecordId = recordId) }
    db.productionDao().insertMaterials(usagesWithRecordId)

    // Tambah stok produk jadi & perbarui perkiraan modal produksi per produk
    val newOutputStock = outputItem.stock + outputQty
    db.itemDao().updateStockAndCost(
      id = outputItemId,
      newStock = newOutputStock,
      newCostPrice = unitCost,
      updatedAt = System.currentTimeMillis()
    )

    // Catat pergerakan stok produk jadi masuk
    db.stockMovementDao().insertMovement(
      StockMovement(
        itemId = outputItemId,
        itemName = outputItem.name,
        movementType = MovementType.PRODUCTION_OUTPUT.name,
        qty = outputQty,
        costPerUnit = unitCost,
        note = "Hasil produksi #${recordId}",
        date = date
      )
    )

    Result.success(record.copy(id = recordId))
  }

  suspend fun addExpense(
    title: String,
    amount: Double,
    category: String,
    date: Long = System.currentTimeMillis(),
    notes: String = ""
  ): Long = withContext(Dispatchers.IO) {
    db.expenseDao().insertExpense(
      Expense(
        title = title.trim(),
        amount = amount,
        category = category,
        date = date,
        notes = notes.trim()
      )
    )
  }

  suspend fun deleteExpense(expense: Expense) = withContext(Dispatchers.IO) {
    db.expenseDao().deleteExpense(expense)
  }

  suspend fun exportBackupJson(): String = withContext(Dispatchers.IO) {
    val profile = db.businessDao().getProfileSync()
    val allItemsList = db.itemDao().getItemByIdSync(0)?.let { listOf(it) } ?: emptyList()
    // Read directly
    val root = JSONObject()
    root.put("appName", "Catat Bisnisku")
    root.put("version", 1)
    root.put("exportDate", System.currentTimeMillis())

    val profileJson = JSONObject()
    if (profile != null) {
      profileJson.put("name", profile.name)
      profileJson.put("type", profile.type)
      profileJson.put("phone", profile.phone)
      profileJson.put("address", profile.address)
      profileJson.put("isConfigured", profile.isConfigured)
    }
    root.put("profile", profileJson)

    // Items
    val itemsArray = JSONArray()
    val currentItems = db.itemDao().getAllItems()
    // For sync export we get all items from DB
    val dbItems = db.openHelper.readableDatabase.query("SELECT * FROM items")
    while (dbItems.moveToNext()) {
      val itemObj = JSONObject()
      itemObj.put("name", dbItems.getString(dbItems.getColumnIndexOrThrow("name")))
      itemObj.put("itemType", dbItems.getString(dbItems.getColumnIndexOrThrow("itemType")))
      itemObj.put("stock", dbItems.getDouble(dbItems.getColumnIndexOrThrow("stock")))
      itemObj.put("unit", dbItems.getString(dbItems.getColumnIndexOrThrow("unit")))
      itemObj.put("costPrice", dbItems.getDouble(dbItems.getColumnIndexOrThrow("costPrice")))
      itemObj.put("sellingPrice", dbItems.getDouble(dbItems.getColumnIndexOrThrow("sellingPrice")))
      itemObj.put("minStockAlert", dbItems.getDouble(dbItems.getColumnIndexOrThrow("minStockAlert")))
      itemObj.put("notes", dbItems.getString(dbItems.getColumnIndexOrThrow("notes")))
      itemsArray.put(itemObj)
    }
    dbItems.close()
    root.put("items", itemsArray)

    // Sales
    val salesArray = JSONArray()
    val allSalesList = db.saleDao().getAllSalesSync()
    for (s in allSalesList) {
      val saleObj = JSONObject()
      saleObj.put("invoiceNumber", s.invoiceNumber)
      saleObj.put("itemName", s.itemName)
      saleObj.put("qty", s.qty)
      saleObj.put("unit", s.unit)
      saleObj.put("sellingPrice", s.sellingPrice)
      saleObj.put("discount", s.discount)
      saleObj.put("totalPrice", s.totalPrice)
      saleObj.put("unitCostPrice", s.unitCostPrice)
      saleObj.put("totalCost", s.totalCost)
      saleObj.put("grossProfit", s.grossProfit)
      saleObj.put("customerName", s.customerName)
      saleObj.put("paymentMethod", s.paymentMethod)
      saleObj.put("notes", s.notes)
      saleObj.put("date", s.date)
      salesArray.put(saleObj)
    }
    root.put("sales", salesArray)

    // Expenses
    val expensesArray = JSONArray()
    val allExpensesList = db.expenseDao().getAllExpensesSync()
    for (e in allExpensesList) {
      val expObj = JSONObject()
      expObj.put("title", e.title)
      expObj.put("amount", e.amount)
      expObj.put("category", e.category)
      expObj.put("date", e.date)
      expObj.put("notes", e.notes)
      expensesArray.put(expObj)
    }
    root.put("expenses", expensesArray)

    root.toString(2)
  }

  suspend fun importBackupJson(jsonString: String): Result<Boolean> = withContext(Dispatchers.IO) {
    try {
      val root = JSONObject(jsonString)
      if (!root.has("appName") || root.getString("appName") != "Catat Bisnisku") {
        return@withContext Result.failure(Exception("Format file backup tidak valid untuk Catat Bisnisku"))
      }

      // Restore profile
      if (root.has("profile")) {
        val p = root.getJSONObject("profile")
        val restoredProfile = BusinessProfile(
          id = 1,
          name = p.optString("name", "Bisnis Saya"),
          type = p.optString("type", BusinessType.JUAL_BELI.name),
          phone = p.optString("phone", ""),
          address = p.optString("address", ""),
          isConfigured = true
        )
        db.businessDao().upsertProfile(restoredProfile)
      }

      // Restore items
      if (root.has("items")) {
        val itemsArray = root.getJSONArray("items")
        for (i in 0 until itemsArray.length()) {
          val itemObj = itemsArray.getJSONObject(i)
          val item = Item(
            name = itemObj.getString("name"),
            itemType = itemObj.optString("itemType", ItemType.PRODUCT.name),
            stock = itemObj.optDouble("stock", 0.0),
            unit = itemObj.optString("unit", "Pcs"),
            costPrice = itemObj.optDouble("costPrice", 0.0),
            sellingPrice = itemObj.optDouble("sellingPrice", 0.0),
            minStockAlert = itemObj.optDouble("minStockAlert", 5.0),
            notes = itemObj.optString("notes", "")
          )
          db.itemDao().insertItem(item)
        }
      }

      // Restore sales
      if (root.has("sales")) {
        val salesArray = root.getJSONArray("sales")
        for (i in 0 until salesArray.length()) {
          val s = salesArray.getJSONObject(i)
          val sale = Sale(
            invoiceNumber = s.optString("invoiceNumber", "INV-${System.currentTimeMillis()}"),
            itemId = 0,
            itemName = s.getString("itemName"),
            qty = s.getDouble("qty"),
            unit = s.optString("unit", "Pcs"),
            sellingPrice = s.getDouble("sellingPrice"),
            discount = s.optDouble("discount", 0.0),
            totalPrice = s.getDouble("totalPrice"),
            unitCostPrice = s.optDouble("unitCostPrice", 0.0),
            totalCost = s.optDouble("totalCost", 0.0),
            grossProfit = s.optDouble("grossProfit", 0.0),
            customerName = s.optString("customerName", ""),
            paymentMethod = s.optString("paymentMethod", "Tunai"),
            notes = s.optString("notes", ""),
            date = s.optLong("date", System.currentTimeMillis())
          )
          db.saleDao().insertSale(sale)
        }
      }

      // Restore expenses
      if (root.has("expenses")) {
        val expensesArray = root.getJSONArray("expenses")
        for (i in 0 until expensesArray.length()) {
          val e = expensesArray.getJSONObject(i)
          val exp = Expense(
            title = e.getString("title"),
            amount = e.getDouble("amount"),
            category = e.optString("category", "Operasional Lain"),
            date = e.optLong("date", System.currentTimeMillis()),
            notes = e.optString("notes", "")
          )
          db.expenseDao().insertExpense(exp)
        }
      }

      Result.success(true)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun resetAllData() = withContext(Dispatchers.IO) {
    db.itemDao().deleteAll()
    db.stockMovementDao().deleteAll()
    db.productionDao().deleteAllRecords()
    db.productionDao().deleteAllMaterials()
    db.saleDao().deleteAll()
    db.expenseDao().deleteAll()
    db.businessDao().upsertProfile(
      BusinessProfile(
        id = 1,
        name = "Bisnis Saya",
        type = BusinessType.JUAL_BELI.name,
        isConfigured = false
      )
    )
  }
}
