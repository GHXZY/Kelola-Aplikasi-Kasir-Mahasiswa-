package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.draw.blur

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.DebtEntity
import com.example.data.local.entity.ExpenseEntity
import com.example.data.local.entity.ProductEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.TransactionItemEntity
import com.example.ui.MainViewModel
import com.example.util.PdfReceiptGenerator
import com.example.ui.cashier.CartSheet
import com.example.ui.cashier.CashierScreen
import com.example.ui.cashier.PaymentDialog
import com.example.ui.cashier.PaymentScreen
import com.example.ui.cashier.TransactionSuccessDialog
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.DonationDialog
import com.example.ui.components.KelolaLogoBadge
import com.example.ui.components.OpeningScreen
import com.example.ui.debts.DebtsScreen

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.example.ui.notes.NotesScreen
import com.example.ui.debts.DebtPaymentScreen

import com.example.ui.debts.SettleDebtDialog
import com.example.ui.home.HomeScreen
import com.example.ui.pending_change.PendingChangesScreen
import com.example.ui.products.AddCategoryDialog
import com.example.ui.products.AddEditProductDialog
import com.example.ui.products.AddEditProductScreen
import com.example.ui.products.ProductScreen
import com.example.ui.products.ReduceStockDialog
import com.example.ui.products.RestockDialog
import com.example.ui.promo.PromoScreen
import com.example.ui.reports.AddExpenseDialog
import com.example.ui.reports.ReportScreen
import com.example.ui.reports.TransactionDetailDialog
import com.example.ui.settings.AboutScreen
import com.example.ui.settings.GuideScreen
import com.example.ui.settings.SettingsDialog
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.BorderLight
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.BrandSky
import com.example.ui.theme.DangerRed
import com.example.ui.theme.KelolaSpacing
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.OnPrimaryBlueContainer
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueContainer
import com.example.ui.theme.SuccessDot
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber
import com.example.util.FormatUtils
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val title: String, val icon: ImageVector, val tag: String) {
    data object Home : Screen("home", "Beranda", Icons.Default.Home, "nav_home")
    data object Cashier : Screen("cashier", "Kasir", Icons.Default.PointOfSale, "nav_cashier")
    data object Debts : Screen("debts", "Hutang", Icons.Default.HourglassTop, "nav_debts")
    data object Products : Screen("products", "Produk", Icons.Default.Inventory2, "nav_products")
    data object Reports : Screen("reports", "Laporan", Icons.Default.BarChart, "nav_reports")
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val colorThemeKey by viewModel.colorTheme.collectAsState()
            val activeColorTheme = remember(colorThemeKey) {
                com.example.ui.theme.ColorTheme.fromKey(colorThemeKey)
            }
            val viewportWidth by viewModel.viewportWidth.collectAsState()
            val targetWidth = when (viewportWidth) {
                "360dp" -> 360f
                "430dp" -> 430f
                else -> 412f
            }
            val isDark = when (themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }

            val configuration = LocalConfiguration.current
            val screenWidthDp = configuration.screenWidthDp.toFloat()
            val currentDensity = LocalDensity.current

            // Responsive Viewport Density Scaling:
            // Scaled so targetWidth fills the device screen width proportionally
            val scaleFactor = (screenWidthDp / targetWidth).coerceIn(0.75f, 1.45f)
            val customDensity = Density(
                density = currentDensity.density * scaleFactor,
                fontScale = currentDensity.fontScale * scaleFactor
            )

            MyApplicationTheme(darkTheme = isDark, colorTheme = activeColorTheme) {
                CompositionLocalProvider(LocalDensity provides customDensity) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Box(
                            modifier = if (screenWidthDp > 600f) {
                                Modifier.fillMaxHeight().width(targetWidth.dp)
                            } else {
                                Modifier.fillMaxSize()
                            }
                        ) {
                            MainApp(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(viewModel: MainViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Navigation State
    val screens = listOf(Screen.Home, Screen.Cashier, Screen.Products, Screen.Reports)
    var selectedScreenIndex by remember { mutableIntStateOf(0) }
    val pagerState = rememberPagerState(initialPage = 0) { screens.size }
    var isShowingDebts by remember { mutableStateOf(false) }
    var isShowingPendingChanges by remember { mutableStateOf(false) }
    val currentScreen = screens[selectedScreenIndex]

    // Sync pager swipe to selectedScreenIndex
    LaunchedEffect(pagerState.currentPage) {
        if (!isShowingDebts) {
            selectedScreenIndex = pagerState.currentPage
        }
    }

    // Sync selectedScreenIndex changes (clicks, programmatic) to pagerState
    LaunchedEffect(selectedScreenIndex) {
        if (pagerState.currentPage != selectedScreenIndex) {
            pagerState.animateScrollToPage(selectedScreenIndex)
        }
    }

    // State from ViewModel
    val businessName by viewModel.businessName.collectAsState()
    val businessAddress by viewModel.businessAddress.collectAsState()
    val businessPhone by viewModel.businessPhone.collectAsState()
    val receiptFooter by viewModel.receiptFooter.collectAsState()
    val qrisMerchantName by viewModel.qrisMerchantName.collectAsState()
    val qrisImagePath by viewModel.qrisImagePath.collectAsState()
    val defaultPaymentMethod by viewModel.defaultPaymentMethod.collectAsState()
    val openingCapital by viewModel.openingCapital.collectAsState()
    val previousSales by viewModel.previousSales.collectAsState()
    val previousSalesDate by viewModel.previousSalesDate.collectAsState()
    val previousSalesNote by viewModel.previousSalesNote.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val colorTheme by viewModel.colorTheme.collectAsState()
    val viewportWidth by viewModel.viewportWidth.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val products by viewModel.products.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val incomes by viewModel.incomes.collectAsState()
    val debts by viewModel.debts.collectAsState()
    val changeRecords by viewModel.changeRecords.collectAsState()
    val promos by viewModel.promos.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val dashboardStats by viewModel.dashboardStats.collectAsState()
    val reportStats by viewModel.reportStats.collectAsState()
    val reportPeriod by viewModel.reportPeriod.collectAsState()
    val lastCompletedTx by viewModel.lastCompletedTx.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val bankAccounts by viewModel.bankAccounts.collectAsState()
    val showDonationDialog by viewModel.showDonationDialog.collectAsState()
    val hasEditedStoreProfile by viewModel.hasEditedStoreProfile.collectAsState()
    var isShowingOpeningScreen by rememberSaveable { mutableStateOf(true) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkAndTriggerDonationDialog()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var isShowingNotes by remember { mutableStateOf(false) }
    var isShowingGuide by rememberSaveable { mutableStateOf(false) }
    var isShowingAbout by rememberSaveable { mutableStateOf(false) }
    var debtForPaymentScreen by remember { mutableStateOf<DebtEntity?>(null) }

    // Dialog & Sheet States
    var showCartSheet by remember { mutableStateOf(false) }
    val cartSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var isShowingPromo by remember { mutableStateOf(false) }

    var showPaymentDialog by remember { mutableStateOf(false) }
    val paymentSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isShowingPaymentScreen by remember { mutableStateOf(false) }

    var debtToSettle by remember { mutableStateOf<DebtEntity?>(null) }
    var debtToDelete by remember { mutableStateOf<DebtEntity?>(null) }
    var debtToEditItems by remember { mutableStateOf<DebtEntity?>(null) }
    var debtItemsForEdit by remember { mutableStateOf<List<TransactionItemEntity>>(emptyList()) }

    var isShowingSettings by remember { mutableStateOf(false) }
    var isShowingAddEditProduct by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<ProductEntity?>(null) }

    var showRestockDialog by remember { mutableStateOf(false) }
    var productToRestock by remember { mutableStateOf<ProductEntity?>(null) }

    var showReduceStockDialog by remember { mutableStateOf(false) }
    var productToReduce by remember { mutableStateOf<ProductEntity?>(null) }

    var showDeleteProductConfirm by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<ProductEntity?>(null) }

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var expenseToEdit by remember { mutableStateOf<ExpenseEntity?>(null) }

    // Transaction Detail State
    var selectedTxForDetail by remember { mutableStateOf<TransactionEntity?>(null) }
    var selectedTxItems by remember { mutableStateOf<List<TransactionItemEntity>>(emptyList()) }

    // System Back Handlers
    BackHandler(enabled = isShowingSettings) {
        isShowingSettings = false
    }

    BackHandler(enabled = isShowingAddEditProduct) {
        isShowingAddEditProduct = false
        productToEdit = null
    }

    BackHandler(enabled = isShowingDebts) {
        isShowingDebts = false
    }

    BackHandler(enabled = isShowingPromo) {
        isShowingPromo = false
    }

    BackHandler(enabled = isShowingPendingChanges) {
        isShowingPendingChanges = false
    }

    BackHandler(enabled = isShowingPaymentScreen) {
        isShowingPaymentScreen = false
    }

    BackHandler(enabled = isShowingNotes) {
        isShowingNotes = false
    }

    BackHandler(enabled = debtForPaymentScreen != null) {
        debtForPaymentScreen = null
    }

    BackHandler(enabled = isShowingOpeningScreen) {
        isShowingOpeningScreen = false
    }

    BackHandler(enabled = isShowingGuide) {
        isShowingGuide = false
    }

    BackHandler(enabled = isShowingAbout) {
        isShowingAbout = false
    }

    // Notification listener for Snackbar
    LaunchedEffect(Unit) {
        viewModel.userMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    if (isShowingGuide) {
        GuideScreen(
            onNavigateBack = { isShowingGuide = false }
        )
    } else if (isShowingAbout) {
        AboutScreen(
            onNavigateBack = { isShowingAbout = false }
        )
    } else if (isShowingSettings) {
        val context = LocalContext.current
        SettingsScreen(
            currentBusinessName = businessName,
            currentAddress = businessAddress,
            currentPhone = businessPhone,
            currentReceiptFooter = receiptFooter,
            defaultPaymentMethod = defaultPaymentMethod,
            qrisImagePath = qrisImagePath,
            qrisMerchantName = qrisMerchantName,
            openingCapital = openingCapital,
            previousSales = previousSales,
            previousSalesDate = previousSalesDate,
            previousSalesNote = previousSalesNote,
            themeMode = themeMode,
            colorTheme = colorTheme,
            viewportWidth = viewportWidth,
            onSaveViewportWidth = { viewModel.updateViewportWidth(it) },
            bankAccounts = bankAccounts,
            onAddBankAccount = { bName, aName, aNum ->
                viewModel.addBankAccount(bName, aName, aNum)
            },
            onRemoveBankAccount = { id ->
                viewModel.removeBankAccount(id)
            },
            onSaveBusinessInfo = { name, address, phone ->
                viewModel.updateBusinessInfo(name, address, phone)
            },
            onSaveDefaultPaymentMethod = { method ->
                viewModel.updateDefaultPaymentMethod(method)
            },
            onUploadQris = { uri ->
                viewModel.saveQrisImage(context, uri, onSuccess = {}, onError = {})
            },
            onSaveCroppedQris = { croppedBmp ->
                viewModel.saveCroppedQrisBitmap(context, croppedBmp, onSuccess = {}, onError = {})
            },
            onDeleteQris = {
                viewModel.deleteQrisImage(context) {}
            },
            onSaveOpeningCapital = { capital ->
                viewModel.updateOpeningCapital(capital)
            },
            onSavePreviousSales = { amount, date, note ->
                viewModel.updatePreviousSales(amount, date, note)
            },
            onSaveReceiptFooter = { footer ->
                viewModel.updateReceiptSettings(footer)
            },
            onUpdateThemeMode = { mode ->
                viewModel.updateThemeMode(mode)
            },
            onUpdateColorTheme = { themeKey ->
                viewModel.updateColorTheme(themeKey)
            },
            onExportBackup = {
                viewModel.exportDataAsJson { jsonString ->
                    try {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, jsonString)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Cadangkan Data (JSON)")
                        context.startActivity(shareIntent)
                    } catch (e: Exception) {
                        // ignore
                    }
                }
            },
            onResetSettingsOnly = {
                viewModel.resetSettingsOnly(context) {}
            },
            onResetAllData = {
                viewModel.resetAllData {
                    // reset finished
                }
            },
            onPreviewOpeningScreen = {
                isShowingOpeningScreen = true
            },
            onOpenGuide = {
                isShowingGuide = true
            },
            onOpenAbout = {
                isShowingAbout = true
            },
            onNavigateBack = { isShowingSettings = false }
        )

    } else if (isShowingNotes) {
        NotesScreen(
            notes = notes,
            onSaveNote = { note -> viewModel.saveNote(note) },
            onDeleteNote = { noteId -> viewModel.deleteNote(noteId) },
            onNavigateBack = { isShowingNotes = false }
        )
    } else if (debtForPaymentScreen != null) {
        DebtPaymentScreen(
            debt = debtForPaymentScreen!!,
            qrisImagePath = qrisImagePath,
            qrisMerchantName = qrisMerchantName,
            bankAccounts = bankAccounts,
            onConfirmSettle = { debtId, amount, method, note ->
                viewModel.settleDebt(debtId, amount, method, note)
                debtForPaymentScreen = null
            },
            onNavigateBack = { debtForPaymentScreen = null }
        )
    } else if (isShowingPromo) {
        PromoScreen(
            promos = promos,
            products = products,
            onSavePromo = { promo -> viewModel.savePromo(promo) },
            onTogglePromoActive = { id, isActive -> viewModel.togglePromoActive(id, isActive) },
            onDeletePromo = { id -> viewModel.deletePromo(id) },
            onNavigateBack = { isShowingPromo = false }
        )
    } else if (isShowingPendingChanges) {
        PendingChangesScreen(
            changeRecords = changeRecords,
            onMarkChangeGiven = { id -> viewModel.markChangeAsPaid(id) },
            onNavigateBack = { isShowingPendingChanges = false }
        )
    } else if (isShowingPaymentScreen) {
        PaymentScreen(
            totalAmount = cart.total,
            totalItemCount = cart.totalItemCount,
            defaultPaymentMethod = defaultPaymentMethod,
            qrisImagePath = qrisImagePath,
            qrisMerchantName = qrisMerchantName,
            bankAccounts = bankAccounts,
            onConfirmSale = { method, cash, customerName, customerPhone, debtNote, isChangePending, buyerNameForChange, changeNote ->
                viewModel.processSale(
                    method, cash, customerName, customerPhone, debtNote,
                    isChangePending, buyerNameForChange, changeNote
                ) { completedTx ->
                    isShowingPaymentScreen = false
                    if (method == "Bayar Nanti") {
                        isShowingDebts = true
                    }
                }
            },
            onNavigateBack = { isShowingPaymentScreen = false }
        )
    } else if (isShowingAddEditProduct) {
        AddEditProductScreen(
            initialProduct = productToEdit,
            categories = categories,
            onSaveProduct = { name, catId, cost, sell, stock, minStock, unit, expDate, barcode ->
                if (productToEdit == null) {
                    viewModel.addProduct(name, catId, cost, sell, stock, minStock, unit, expDate, barcode)
                } else {
                    viewModel.updateProduct(
                        productToEdit!!.copy(
                            name = name,
                            categoryId = catId,
                            costPrice = cost,
                            sellingPrice = sell,
                            stock = stock,
                            minimumStock = minStock,
                            unit = unit,
                            expirationDate = expDate,
                            barcode = barcode
                        )
                    )
                }
                isShowingAddEditProduct = false
                productToEdit = null
            },
            onGenerateUniqueBarcode = {
                viewModel.generateUniqueBarcode(productToEdit?.id)
            },
            onCheckBarcodeAvailability = { candidate ->
                viewModel.checkBarcodeAvailability(candidate, productToEdit?.id)
            },
            onLookupProduct = { barcode ->
                viewModel.getProductByBarcode(barcode)
            },
            onSelectProductToEdit = { product ->
                productToEdit = product
            },
            onAddCategoryCustom = { catName, onCreated ->
                viewModel.addCategory(catName) { newId ->
                    onCreated(newId)
                }
            },
            onOpenAddCategory = { showAddCategoryDialog = true },
            onNavigateBack = {
                isShowingAddEditProduct = false
                productToEdit = null
            }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
                topBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = KelolaSpacing.ScreenMargin, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (isShowingDebts) {
                            IconButton(
                                onClick = { isShowingDebts = false },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .testTag("button_back_from_debts")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Kembali",
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Daftar Kasbon",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Orang Belum Bayar / Hutang",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            KelolaLogoBadge(
                                size = 36.dp,
                                iconSize = 20.dp
                            )
                            Column {
                                Text(
                                    text = if (currentScreen == Screen.Home) (businessName.ifEmpty { "Kelola" }) else currentScreen.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (currentScreen == Screen.Home) "Aplikasi Kasir Usaha" else businessName.ifEmpty { "Kelola" },
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Settings Button (Round button with border matching preview)
                    IconButton(
                        onClick = { isShowingSettings = true },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), CircleShape)
                            .testTag("button_open_settings")
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Pengaturan",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(0.dp))
            ) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp,
                    modifier = Modifier.height(64.dp)
                ) {
                    screens.forEachIndexed { index, screen ->
                        val isSelected = !isShowingDebts && selectedScreenIndex == index
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                isShowingDebts = false
                                selectedScreenIndex = index
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            icon = {
                                if (screen == Screen.Cashier && cart.totalItemCount > 0) {
                                    BadgedBox(
                                        badge = {
                                            Badge(
                                                containerColor = DangerRed,
                                                contentColor = Color.White
                                            ) {
                                                Text(
                                                    "${cart.totalItemCount}",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = screen.icon,
                                            contentDescription = screen.title,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = screen.icon,
                                        contentDescription = screen.title,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            alwaysShowLabel = true,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                indicatorColor = BrandSky.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier.testTag(screen.tag)
                        )
                    }
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isShowingDebts) {
                DebtsScreen(
                    debts = debts,
                    onSettleDebt = { debt -> debtForPaymentScreen = debt },
                    onDeleteDebt = { debt -> debtToDelete = debt },
                    onEditDebtItems = { debt ->
                        coroutineScope.launch {
                            selectedTxItems = viewModel.getTransactionItemsForDetail(debt.transactionId)
                            debtItemsForEdit = selectedTxItems
                            debtToEditItems = debt
                        }
                    },
                    onSelectTransactionNumber = { txNum ->
                        val tx = transactions.find { it.transactionNumber == txNum }
                        if (tx != null) {
                            coroutineScope.launch {
                                selectedTxItems = viewModel.getTransactionItemsForDetail(tx.id)
                                selectedTxForDetail = tx
                            }
                        }
                    }
                )
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = true
                ) { page ->
                    when (screens[page]) {
                        Screen.Home -> {
                            HomeScreen(
                                stats = dashboardStats,
                                businessName = businessName,
                                openingCapital = openingCapital,
                                onNavigateToCashier = { selectedScreenIndex = 1 },
                                onNavigateToDebts = { isShowingDebts = true },
                                onNavigateToPendingChanges = { isShowingPendingChanges = true },
                                onOpenAddProduct = {
                                    productToEdit = null
                                    isShowingAddEditProduct = true
                                },
                                onOpenAddExpense = { showAddExpenseDialog = true },
                                onOpenRestock = { prod ->
                                    productToRestock = prod
                                    showRestockDialog = true
                                },
                                onSelectTransaction = { tx ->
                                    coroutineScope.launch {
                                        selectedTxItems = viewModel.getTransactionItemsForDetail(tx.id)
                                        selectedTxForDetail = tx
                                    }
                                },
                                onMarkChangeGiven = { changeId ->
                                    viewModel.markChangeAsPaid(changeId)
                                },
                                onSettleDebt = { debt ->
                                    debtForPaymentScreen = debt
                                },
                                onOpenNotes = {
                                    isShowingNotes = true
                                },
                                onEditDebtItems = { debt ->
                                    coroutineScope.launch {
                                        selectedTxItems = viewModel.getTransactionItemsForDetail(debt.transactionId)
                                        debtItemsForEdit = selectedTxItems
                                        debtToEditItems = debt
                                    }
                                }
                            )
                        }
                        Screen.Cashier -> {
                            CashierScreen(
                                products = products,
                                categories = categories,
                                cart = cart,
                                onAddToCart = { viewModel.addToCart(it) },
                                onUpdateQuantity = { prodId, qty -> viewModel.updateCartQuantity(prodId, qty) },
                                onRemoveFromCart = { viewModel.removeFromCart(it) },
                                onClearCart = { viewModel.clearCart() },
                                onOpenCart = { showCartSheet = true }
                            )
                        }
                        Screen.Debts -> {
                            // Debts handled when isShowingDebts = true
                        }
                        Screen.Products -> {
                            ProductScreen(
                                products = products,
                                categories = categories,
                                onOpenAddProduct = {
                                    productToEdit = null
                                    isShowingAddEditProduct = true
                                },
                                onEditProduct = { prod ->
                                    productToEdit = prod
                                    isShowingAddEditProduct = true
                                },
                                onRestockProduct = { prod ->
                                    productToRestock = prod
                                    showRestockDialog = true
                                },
                                onReduceStockProduct = { prod ->
                                    productToReduce = prod
                                    showReduceStockDialog = true
                                },
                                onDeleteProduct = { prod ->
                                    productToDelete = prod
                                    showDeleteProductConfirm = true
                                },
                                onNavigateToCashier = { selectedScreenIndex = 1 },
                                onOpenAddExpense = { showAddExpenseDialog = true },
                                onOpenPromo = { isShowingPromo = true }
                            )
                        }
                        Screen.Reports -> {
                            ReportScreen(
                                reportStats = reportStats,
                                selectedPeriod = reportPeriod,
                                onSelectPeriod = { viewModel.setReportPeriod(it) },
                                transactions = transactions,
                                expenses = expenses,
                                incomes = incomes,
                                openingCapital = openingCapital,
                                previousSales = previousSales,
                                previousSalesDate = previousSalesDate,
                                previousSalesNote = previousSalesNote,
                                onSelectTransaction = { tx ->
                                    coroutineScope.launch {
                                        selectedTxItems = viewModel.getTransactionItemsForDetail(tx.id)
                                        selectedTxForDetail = tx
                                    }
                                },
                                onOpenAddExpense = {
                                    expenseToEdit = null
                                    showAddExpenseDialog = true
                                },
                                onEditExpense = { exp ->
                                    expenseToEdit = exp
                                    showAddExpenseDialog = true
                                },
                                onDeleteExpense = { viewModel.deleteExpense(it) },
                                onDeleteTransaction = { viewModel.deleteTransaction(it) },
                                onCancelLoss = { lossId, restoreStock ->
                                    viewModel.cancelLoss(lossId, restoreStock)
                                }
                            )
                        }
                    }
                }
            }
        }

    }

    // --- Modal Sheets & Dialogs ---

    // 1. Cart Sheet
    if (showCartSheet) {
        CartSheet(
            sheetState = cartSheetState,
            cart = cart,
            onIncreaseQty = { viewModel.addToCart(it) },
            onDecreaseQty = { viewModel.decreaseCart(it) },
            onRemoveItem = { viewModel.removeFromCart(it) },
            onClearCart = { viewModel.clearCart() },
            onApplyDiscount = { viewModel.setDiscount(it, false) },
            onProceedToPayment = {
                coroutineScope.launch {
                    cartSheetState.hide()
                    showCartSheet = false
                    isShowingPaymentScreen = true
                }
            },
            onDismiss = { showCartSheet = false }
        )
    }

    // 2. Payment Dialog / Sheet
    if (showPaymentDialog) {
        PaymentDialog(
            totalAmount = cart.total,
            sheetState = paymentSheetState,
            defaultPaymentMethod = defaultPaymentMethod,
            qrisImagePath = qrisImagePath,
            qrisMerchantName = qrisMerchantName,
            bankAccounts = bankAccounts,
            onConfirmSale = { method, cash, customerName, customerPhone, debtNote, isChangePending, buyerNameForChange, changeNote ->
                viewModel.processSale(
                    method,
                    cash,
                    customerName,
                    customerPhone,
                    debtNote,
                    isChangePending,
                    buyerNameForChange,
                    changeNote
                ) { completedTx ->
                    coroutineScope.launch {
                        paymentSheetState.hide()
                        showPaymentDialog = false
                        if (method == "Bayar Nanti") {
                            isShowingDebts = true
                        }
                    }
                }
            },
            onOpenDebtsScreen = {
                coroutineScope.launch {
                    paymentSheetState.hide()
                    showPaymentDialog = false
                    isShowingDebts = true
                }
            },
            onDismiss = { showPaymentDialog = false }
        )
    }

    // 3. Transaction Success Modal
    lastCompletedTx?.let { tx ->
        TransactionSuccessDialog(
            transaction = tx,
            onDismiss = {
                viewModel.clearLastCompletedTx()
            },
            onNewSale = {
                viewModel.clearLastCompletedTx()
            },
            onViewDetail = {
                viewModel.clearLastCompletedTx()
                coroutineScope.launch {
                    selectedTxItems = viewModel.getTransactionItemsForDetail(tx.id)
                    selectedTxForDetail = tx
                }
            },
            onShareReceipt = { targetTx ->
                coroutineScope.launch {
                    val items = viewModel.getTransactionItemsForDetail(targetTx.id)
                    PdfReceiptGenerator.shareReceiptPdf(
                        context = context,
                        transaction = targetTx,
                        items = items,
                        businessName = businessName,
                        businessAddress = businessAddress,
                        businessPhone = businessPhone,
                        receiptFooter = receiptFooter
                    )
                }
            }
        )
    }

    // 4. Transaction Detail Dialog
    selectedTxForDetail?.let { tx ->
        TransactionDetailDialog(
            transaction = tx,
            items = selectedTxItems,
            onCancelTransaction = {
                viewModel.cancelTransaction(tx.id)
                selectedTxForDetail = null
            },
            onDeleteTransaction = {
                viewModel.deleteTransaction(tx.id)
                selectedTxForDetail = null
            },
            onDismiss = { selectedTxForDetail = null },
            onShareReceipt = { targetTx, items ->
                PdfReceiptGenerator.shareReceiptPdf(
                    context = context,
                    transaction = targetTx,
                    items = items,
                    businessName = businessName,
                    businessAddress = businessAddress,
                    businessPhone = businessPhone,
                    receiptFooter = receiptFooter
                )
            }
        )
    }

    // 6. Restock Dialog
    productToRestock?.let { prod ->
        if (showRestockDialog) {
            RestockDialog(
                product = prod,
                onConfirmRestock = { addQty, note, recordExpense, totalCost ->
                    viewModel.restockProduct(prod.id, addQty, note, recordExpense, totalCost)
                    showRestockDialog = false
                    productToRestock = null
                },
                onDismiss = {
                    showRestockDialog = false
                    productToRestock = null
                }
            )
        }
    }

    // 7. Reduce Stock Dialog
    productToReduce?.let { prod ->
        if (showReduceStockDialog) {
            ReduceStockDialog(
                product = prod,
                onConfirmReduce = { reduceQty, reason ->
                    viewModel.reduceStock(prod.id, reduceQty, reason)
                    showReduceStockDialog = false
                    productToReduce = null
                },
                onDismiss = {
                    showReduceStockDialog = false
                    productToReduce = null
                }
            )
        }
    }

    // 8. Delete Product Confirmation
    if (showDeleteProductConfirm && productToDelete != null) {
        ConfirmationDialog(
            title = "Hapus Produk?",
            message = "Apakah Anda yakin ingin menghapus '${productToDelete!!.name}'? Produk ini tidak akan muncul lagi di kasir.",
            confirmText = "Hapus",
            isDestructive = true,
            onConfirm = {
                productToDelete?.let { viewModel.deleteProduct(it) }
                showDeleteProductConfirm = false
                productToDelete = null
            },
            onDismiss = {
                showDeleteProductConfirm = false
                productToDelete = null
            }
        )
    }

    // 9. Add Category Dialog
    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onSaveCategory = { catName ->
                viewModel.addCategory(catName)
                showAddCategoryDialog = false
            },
            onDismiss = { showAddCategoryDialog = false }
        )
    }

    // 10. Add Expense Dialog
    if (showAddExpenseDialog) {
        AddExpenseDialog(
            initialExpense = expenseToEdit,
            onSaveExpense = { cat, amt, note ->
                val currentExp = expenseToEdit
                if (currentExp != null) {
                    viewModel.updateExpense(currentExp.id, cat, amt, note)
                    expenseToEdit = null
                } else {
                    viewModel.addExpense(cat, amt, note)
                }
                showAddExpenseDialog = false
            },
            onDismiss = {
                showAddExpenseDialog = false
                expenseToEdit = null
            }
        )
    }

    // 12. Settle Debt Dialog (Pelunasan Hutang)
    debtToSettle?.let { debt ->
        SettleDebtDialog(
            debt = debt,
            onConfirmSettle = { debtId, amount, paymentMethod, note ->
                viewModel.settleDebt(debtId, amount, paymentMethod, note)
                debtToSettle = null
            },
            onDismiss = { debtToSettle = null }
        )
    }

    // 13. Delete Debt Confirmation
    debtToDelete?.let { debt ->
        ConfirmationDialog(
            title = "Hapus Catatan Hutang?",
            message = "Catatan hutang atas nama '${debt.customerName}' sebesar ${FormatUtils.formatRupiah(debt.remainingAmount)} akan dihapus dari sistem.",
            confirmText = "Hapus",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteDebt(debt.id)
                debtToDelete = null
            },
            onDismiss = { debtToDelete = null }
        )
    }

    // 14. Edit Debt Items Dialog (Edit Belanjaan Pembeli Kasbon)
    debtToEditItems?.let { debt ->
        com.example.ui.debts.EditDebtItemsDialog(
            debt = debt,
            initialItems = debtItemsForEdit,
            availableProducts = products,
            onSave = { updatedItems ->
                viewModel.updateDebtItems(debt.id, updatedItems) {
                    debtToEditItems = null
                    debtItemsForEdit = emptyList()
                }
            },
            onDismiss = {
                debtToEditItems = null
                debtItemsForEdit = emptyList()
            }
        )
    }

    // 15. Donation / Support Dialog (Tampil pertama kali download & tiap 5 jam)
    if (showDonationDialog && !isShowingOpeningScreen) {
        DonationDialog(
            onDismiss = { viewModel.dismissDonationDialog() }
        )
    }

    // 16. Opening Screen (Layar Pembuka Aplikasi saat dibuka)
    androidx.compose.animation.AnimatedVisibility(
        visible = isShowingOpeningScreen,
        enter = androidx.compose.animation.fadeIn(),
        exit = androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(400))
    ) {
        OpeningScreen(
            storeName = businessName,
            storeAddress = businessAddress,
            hasCustomProfile = hasEditedStoreProfile,
            onTimeoutOrDismiss = {
                isShowingOpeningScreen = false
            }
        )
    }
    }
}
