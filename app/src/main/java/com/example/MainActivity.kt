package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExtendedFloatingActionButton
import com.example.ui.theme.ExpenseRed
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.BusinessType
import com.example.data.model.Item
import com.example.data.model.ItemType
import com.example.ui.components.AddExpenseModal
import com.example.ui.components.AddItemModal
import com.example.ui.components.AddSaleModal
import com.example.ui.components.QuickActionBottomSheet
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExpensesScreen
import com.example.ui.screens.ProductionScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SalesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StockAdjustModal
import com.example.ui.screens.StockScreen
import com.example.ui.theme.CatatBisniskuTheme
import com.example.ui.theme.PrimaryGreenLight
import com.example.ui.viewmodel.MainViewModel

enum class MainNavScreen(val title: String) {
  DASHBOARD("Dashboard"),
  STOK("Stok"),
  PENJUALAN("Penjualan"),
  PENGELUARAN("Pengeluaran"),
  LAPORAN("Laporan")
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val viewModel: MainViewModel = viewModel()
      val profile by viewModel.businessProfile.collectAsState()
      val systemInDark = isSystemInDarkTheme()
      val isDarkTheme = when (profile?.themeSetting) {
        "DARK" -> true
        "LIGHT" -> false
        else -> systemInDark
      }
      CatatBisniskuTheme(darkTheme = isDarkTheme) {
        CatatBisniskuApp(viewModel = viewModel)
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatatBisniskuApp(
  viewModel: MainViewModel = viewModel()
) {
  val profile by viewModel.businessProfile.collectAsState()
  val products by viewModel.products.collectAsState()
  val snackbarHostState = remember { SnackbarHostState() }

  // App Navigation States
  var isSplashVisible by remember { mutableStateOf(true) }
  var currentTab by remember { mutableStateOf(MainNavScreen.DASHBOARD) }
  var isSettingsOpen by remember { mutableStateOf(false) }
  var isProductionOpen by remember { mutableStateOf(false) }
  var productionTargetProduct by remember { mutableStateOf<Item?>(null) }

  // Creation Modals
  var showAddItemModal by remember { mutableStateOf(false) }
  var addItemTypeTarget by remember { mutableStateOf(ItemType.PRODUCT) }
  var showAddSaleModal by remember { mutableStateOf(false) }
  var showAddExpenseModal by remember { mutableStateOf(false) }
  var quickAddStockTarget by remember { mutableStateOf<Item?>(null) }

  // Snackbar Listener
  LaunchedEffect(Unit) {
    viewModel.snackbarMessage.collect { msg ->
      snackbarHostState.showSnackbar(msg)
    }
  }

  val isProduksi = profile?.type == BusinessType.PRODUKSI.name

  if (isSplashVisible) {
    SplashScreen(
      onAnimationFinished = { isSplashVisible = false }
    )
  } else {
    // Back Handler
    BackHandler(enabled = isSettingsOpen || isProductionOpen) {
      if (isProductionOpen) isProductionOpen = false
      else if (isSettingsOpen) isSettingsOpen = false
    }

    Scaffold(
      modifier = Modifier.fillMaxSize(),
      contentWindowInsets = WindowInsets(0, 0, 0, 0),
      snackbarHost = { SnackbarHost(snackbarHostState) },
      topBar = {
        if (!isSettingsOpen && !isProductionOpen) {
          CenterAlignedTopAppBar(
            title = {
              Text(
                text = "Catat Bisnisku",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  fontSize = 18.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
              )
            },
            navigationIcon = {
              Box(modifier = Modifier.padding(start = 12.dp)) {
                Image(
                  painter = painterResource(id = R.drawable.app_logo),
                  contentDescription = "Logo",
                  modifier = Modifier.size(34.dp)
                )
              }
            },
            actions = {
              IconButton(
                onClick = { isSettingsOpen = true },
                modifier = Modifier.testTag("btn_settings")
              ) {
                Icon(
                  imageVector = Icons.Default.Settings,
                  contentDescription = "Pengaturan Usaha",
                  tint = MaterialTheme.colorScheme.onSurface
                )
              }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
              containerColor = MaterialTheme.colorScheme.surface
            )
          )
        }
      },
      bottomBar = {
        if (!isSettingsOpen && !isProductionOpen) {
          NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
          ) {
            NavigationBarItem(
              selected = currentTab == MainNavScreen.DASHBOARD,
              onClick = { currentTab = MainNavScreen.DASHBOARD },
              icon = { Icon(Icons.Default.Dashboard, contentDescription = "Beranda") },
              label = {
                Text(
                  text = "Beranda",
                  fontSize = 10.sp,
                  maxLines = 1,
                  softWrap = false
                )
              },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryGreenLight,
                indicatorColor = PrimaryGreenLight.copy(alpha = 0.15f)
              ),
              modifier = Modifier.testTag("tab_dashboard")
            )
            NavigationBarItem(
              selected = currentTab == MainNavScreen.STOK,
              onClick = { currentTab = MainNavScreen.STOK },
              icon = { Icon(Icons.Default.Inventory, contentDescription = "Stok") },
              label = {
                Text(
                  text = "Stok",
                  fontSize = 10.sp,
                  maxLines = 1,
                  softWrap = false
                )
              },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryGreenLight,
                indicatorColor = PrimaryGreenLight.copy(alpha = 0.15f)
              ),
              modifier = Modifier.testTag("tab_stok")
            )
            NavigationBarItem(
              selected = currentTab == MainNavScreen.PENJUALAN,
              onClick = { currentTab = MainNavScreen.PENJUALAN },
              icon = { Icon(Icons.Default.PointOfSale, contentDescription = "Penjualan") },
              label = {
                Text(
                  text = "Penjualan",
                  fontSize = 10.sp,
                  maxLines = 1,
                  softWrap = false
                )
              },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryGreenLight,
                indicatorColor = PrimaryGreenLight.copy(alpha = 0.15f)
              ),
              modifier = Modifier.testTag("tab_penjualan")
            )
            NavigationBarItem(
              selected = currentTab == MainNavScreen.PENGELUARAN,
              onClick = { currentTab = MainNavScreen.PENGELUARAN },
              icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Biaya") },
              label = {
                Text(
                  text = "Biaya",
                  fontSize = 10.sp,
                  maxLines = 1,
                  softWrap = false
                )
              },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryGreenLight,
                indicatorColor = PrimaryGreenLight.copy(alpha = 0.15f)
              ),
              modifier = Modifier.testTag("tab_pengeluaran")
            )
            NavigationBarItem(
              selected = currentTab == MainNavScreen.LAPORAN,
              onClick = { currentTab = MainNavScreen.LAPORAN },
              icon = { Icon(Icons.Default.Assessment, contentDescription = "Laporan") },
              label = {
                Text(
                  text = "Laporan",
                  fontSize = 10.sp,
                  maxLines = 1,
                  softWrap = false
                )
              },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PrimaryGreenLight,
                indicatorColor = PrimaryGreenLight.copy(alpha = 0.15f)
              ),
              modifier = Modifier.testTag("tab_laporan")
            )
          }
        }
      },
      floatingActionButton = {
        if (!isSettingsOpen && !isProductionOpen) {
          when (currentTab) {
            MainNavScreen.DASHBOARD -> {
              ExtendedFloatingActionButton(
                onClick = { showAddSaleModal = true },
                icon = { Icon(Icons.Default.PointOfSale, contentDescription = null, modifier = Modifier.size(20.dp)) },
                text = { Text("+ Catat Penjualan", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                containerColor = PrimaryGreenLight,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_dashboard_sale")
              )
            }
            MainNavScreen.STOK -> {
              ExtendedFloatingActionButton(
                onClick = {
                  addItemTypeTarget = if (isProduksi) ItemType.RAW_MATERIAL else ItemType.PRODUCT
                  showAddItemModal = true
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp)) },
                text = { Text(if (isProduksi) "+ Bahan Baku" else "+ Tambah Barang", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                containerColor = PrimaryGreenLight,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_stock_add")
              )
            }
            MainNavScreen.PENJUALAN -> {
              ExtendedFloatingActionButton(
                onClick = { showAddSaleModal = true },
                icon = { Icon(Icons.Default.PointOfSale, contentDescription = null, modifier = Modifier.size(20.dp)) },
                text = { Text("+ Catat Penjualan", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                containerColor = PrimaryGreenLight,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_sale_add")
              )
            }
            MainNavScreen.PENGELUARAN -> {
              ExtendedFloatingActionButton(
                onClick = { showAddExpenseModal = true },
                icon = { Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(20.dp)) },
                text = { Text("+ Catat Biaya", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                containerColor = ExpenseRed,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_expense_add")
              )
            }
            MainNavScreen.LAPORAN -> {
              // Di menu laporan tidak perlu tombol melayang agar tampilan grafik & ringkasan bersih
            }
          }
        }
      }
    ) { innerPadding ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
      ) {
        Crossfade(
          targetState = when {
            isSettingsOpen -> "SETTINGS"
            isProductionOpen -> "PRODUCTION"
            else -> currentTab.name
          },
          label = "MainScreenTransition"
        ) { screenState ->
          when (screenState) {
            "SETTINGS" -> {
              SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { isSettingsOpen = false }
              )
            }
            "PRODUCTION" -> {
              ProductionScreen(
                viewModel = viewModel,
                initialTargetProduct = productionTargetProduct,
                onNavigateBack = {
                  isProductionOpen = false
                  productionTargetProduct = null
                }
              )
            }
            MainNavScreen.DASHBOARD.name -> {
              DashboardScreen(
                viewModel = viewModel,
                onNavigateToStock = { currentTab = MainNavScreen.STOK },
                onNavigateToSales = { currentTab = MainNavScreen.PENJUALAN },
                onNavigateToExpenses = { currentTab = MainNavScreen.PENGELUARAN },
                onQuickAddStock = { item -> quickAddStockTarget = item },
                onOpenQuickAction = { showAddSaleModal = true }
              )
            }
            MainNavScreen.STOK.name -> {
              StockScreen(
                viewModel = viewModel,
                onOpenAddDialog = { type ->
                  addItemTypeTarget = type
                  showAddItemModal = true
                },
                onOpenProduction = { targetProd ->
                  productionTargetProduct = targetProd
                  isProductionOpen = true
                }
              )
            }
            MainNavScreen.PENJUALAN.name -> {
              SalesScreen(
                viewModel = viewModel,
                onOpenAddSale = { showAddSaleModal = true }
              )
            }
            MainNavScreen.PENGELUARAN.name -> {
              ExpensesScreen(
                viewModel = viewModel,
                onOpenAddExpense = { showAddExpenseModal = true },
                onNavigateToStock = { currentTab = MainNavScreen.STOK }
              )
            }
            MainNavScreen.LAPORAN.name -> {
              ReportsScreen(viewModel = viewModel)
            }
          }
        }
      }
    }

    // Modal: Add Item (Barang / Bahan)
    if (showAddItemModal) {
      AddItemModal(
        initialType = addItemTypeTarget,
        onDismiss = { showAddItemModal = false },
        onSave = { name, type, stock, unit, cost, sell, minStock, notes ->
          viewModel.addNewItem(name, type, stock, unit, cost, sell, minStock, notes)
        }
      )
    }

    // Modal: Add Sale
    if (showAddSaleModal) {
      AddSaleModal(
        availableProducts = products,
        onDismiss = { showAddSaleModal = false },
        onSave = { itemId, qty, price, discount, customer, payment, note ->
          viewModel.recordSale(itemId, qty, price, discount, customer, payment, note)
        }
      )
    }

    // Modal: Add Expense
    if (showAddExpenseModal) {
      AddExpenseModal(
        onDismiss = { showAddExpenseModal = false },
        onSave = { title, amount, category, notes ->
          viewModel.recordExpense(title, amount, category, notes)
        }
      )
    }

    // Modal: Quick Add Stock from Dashboard Alert
    quickAddStockTarget?.let { item ->
      StockAdjustModal(
        item = item,
        isAddMode = true,
        onDismiss = { quickAddStockTarget = null },
        onConfirm = { qty, newCost, note ->
          viewModel.addStock(item.id, qty, newCost, note)
          quickAddStockTarget = null
        }
      )
    }
  }
}
