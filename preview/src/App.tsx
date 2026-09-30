import React, { useState, useMemo, useEffect } from 'react';
import {
  INITIAL_CATEGORIES,
  INITIAL_PRODUCTS,
  INITIAL_PROMOS,
  INITIAL_TRANSACTIONS,
  INITIAL_TRANSACTION_ITEMS,
  INITIAL_DEBTS,
  INITIAL_EXPENSES,
  INITIAL_SETTINGS,
  INITIAL_CHANGE_RECORDS,
  INITIAL_LOSS_RECORDS,
  INITIAL_NOTES,
  INITIAL_CUSTOMERS
} from './data/mockData';
import {
  ProductEntity,
  CategoryEntity,
  PromoEntity,
  TransactionEntity,
  TransactionItemEntity,
  DebtEntity,
  ExpenseEntity,
  BusinessSettings,
  CartItem,
  CartSummary,
  ChangeRecordEntity,
  LossRecordEntity,
  NoteEntity,
  CustomerEntity,
  CustomerWithStats
} from './types';
import { DeviceFrame } from './components/DeviceFrame';
import { BottomNavBar, TabScreen } from './components/BottomNavBar';
import { HomeScreen } from './screens/HomeScreen';
import { CashierScreen } from './screens/CashierScreen';
import { ProductsScreen } from './screens/ProductsScreen';
import { ReportsScreen } from './screens/ReportsScreen';
import { DebtsScreen } from './screens/DebtsScreen';
import { PendingChangesScreen } from './screens/PendingChangesScreen';
import { PromoScreen } from './screens/PromoScreen';
import { SettingsScreen } from './screens/SettingsScreen';
import { AddEditProductScreen } from './screens/AddEditProductScreen';
import { PaymentScreen } from './screens/PaymentScreen';
import { CustomerListScreen } from './screens/CustomerListScreen';

// Dialogs
import { CartSheet } from './dialogs/CartSheet';
import { PaymentDialog } from './dialogs/PaymentDialog';
import { TransactionSuccessDialog } from './dialogs/TransactionSuccessDialog';
import { TransactionDetailDialog } from './dialogs/TransactionDetailDialog';
import { SettleDebtDialog } from './dialogs/SettleDebtDialog';
import { EditDebtItemsDialog } from './dialogs/EditDebtItemsDialog';
import { AddExpenseDialog } from './dialogs/AddExpenseDialog';
import { NotesDialog } from './dialogs/NotesDialog';
import { RestockDialog, ReduceStockDialog, AddCategoryDialog } from './dialogs/ProductModals';
import { CustomerProfileDialog, AddEditCustomerDialog } from './dialogs/CustomerDialogs';
import { evaluatePromos } from './utils/promoEngine';

export const App: React.FC = () => {
  // Master Store Data
  const [categories, setCategories] = useState<CategoryEntity[]>(INITIAL_CATEGORIES);
  const [products, setProducts] = useState<ProductEntity[]>(INITIAL_PRODUCTS);
  const [promos, setPromos] = useState<PromoEntity[]>(INITIAL_PROMOS);
  const [transactions, setTransactions] = useState<TransactionEntity[]>(INITIAL_TRANSACTIONS);
  const [transactionItems, setTransactionItems] = useState<TransactionItemEntity[]>(INITIAL_TRANSACTION_ITEMS);
  const [debts, setDebts] = useState<DebtEntity[]>(INITIAL_DEBTS);
  const [expenses, setExpenses] = useState<ExpenseEntity[]>(INITIAL_EXPENSES);
  const [settings, setSettings] = useState<BusinessSettings>(INITIAL_SETTINGS);

  // New Offline Feature States: Kembalian, Kerugian, Catatan
  const [changeRecords, setChangeRecords] = useState<ChangeRecordEntity[]>(INITIAL_CHANGE_RECORDS);
  const [lossRecords, setLossRecords] = useState<LossRecordEntity[]>(INITIAL_LOSS_RECORDS);
  const [notes, setNotes] = useState<NoteEntity[]>(INITIAL_NOTES);

  // Customer Management States
  const [customers, setCustomers] = useState<CustomerEntity[]>(INITIAL_CUSTOMERS);
  const [isShowingCustomers, setIsShowingCustomers] = useState(false);
  const [selectedCustomerForProfile, setSelectedCustomerForProfile] = useState<CustomerWithStats | null>(null);
  const [customerToEdit, setCustomerToEdit] = useState<CustomerEntity | null>(null);
  const [showAddEditCustomerModal, setShowAddEditCustomerModal] = useState(false);

  // Cart State
  const [cartItems, setCartItems] = useState<CartItem[]>([]);

  // Navigation State
  const [activeTab, setActiveTab] = useState<TabScreen>('home');
  const [isShowingDebts, setIsShowingDebts] = useState(false);
  const [isShowingPendingChanges, setIsShowingPendingChanges] = useState(false);
  const [isShowingPromo, setIsShowingPromo] = useState(false);
  const [isShowingSettings, setIsShowingSettings] = useState(false);
  const [isShowingAddEditProduct, setIsShowingAddEditProduct] = useState(false);
  const [productToEdit, setProductToEdit] = useState<ProductEntity | null>(null);

  // Modal Dialog States
  const [showCartSheet, setShowCartSheet] = useState(false);
  const [showPaymentDialog, setShowPaymentDialog] = useState(false);
  const [isShowingPaymentScreen, setIsShowingPaymentScreen] = useState(false);
  const [showNotesDialog, setShowNotesDialog] = useState(false);
  const [lastCompletedTx, setLastCompletedTx] = useState<TransactionEntity | null>(null);
  const [selectedTxForDetail, setSelectedTxForDetail] = useState<TransactionEntity | null>(null);
  const [debtToSettle, setDebtToSettle] = useState<DebtEntity | null>(null);
  const [debtToEditItems, setDebtToEditItems] = useState<DebtEntity | null>(null);
  const [productToRestock, setProductToRestock] = useState<ProductEntity | null>(null);
  const [productToReduce, setProductToReduce] = useState<ProductEntity | null>(null);
  const [showAddCategory, setShowAddCategory] = useState(false);
  const [showAddExpense, setShowAddExpense] = useState(false);
  const [expenseToEdit, setExpenseToEdit] = useState<ExpenseEntity | null>(null);

  // Automated Idempotent Expired Products Processing
  useEffect(() => {
    const now = Date.now();
    let hasExpiredUpdates = false;
    const newLosses: LossRecordEntity[] = [];

    const updatedProducts = products.map((p) => {
      if (p.expirationDate && Number(p.expirationDate) < now && p.stock > 0) {
        hasExpiredUpdates = true;
        const lossQty = p.stock;
        const lossAmount = lossQty * p.costPrice;
        newLosses.push({
          id: Date.now() + Math.floor(Math.random() * 1000),
          productId: p.id,
          productName: p.name,
          quantity: lossQty,
          costPrice: p.costPrice,
          totalLoss: lossAmount,
          reason: 'Kadaluarsa',
          date: now,
          createdAt: now
        });
        return { ...p, stock: 0 };
      }
      return p;
    });

    if (hasExpiredUpdates) {
      setProducts(updatedProducts);
      setLossRecords((prev) => [...newLosses, ...prev]);
      showToast(`${newLosses.length} produk kadaluarsa otomatis dicatat ke Laporan Kerugian.`);
    }
  }, [products]);

  // Snackbar Toast
  const [toastMessage, setToastMessage] = useState<string | null>(null);
  const showToast = (msg: string) => {
    setToastMessage(msg);
    setTimeout(() => setToastMessage(null), 3000);
  };

  // Dark Theme Mode
  const [isDark, setIsDark] = useState(false);

  // Cart Summary Computation
  const cart: CartSummary = useMemo(() => {
    const totalItemCount = cartItems.reduce((sum, item) => sum + item.quantity, 0);
    const subtotal = cartItems.reduce((sum, item) => sum + item.product.sellingPrice * item.quantity, 0);

    const cartMap: { [productId: number]: number } = {};
    const priceMap: { [productId: number]: number } = {};
    const nameMap: { [productId: number]: string } = {};

    cartItems.forEach((item) => {
      cartMap[item.product.id] = item.quantity;
      priceMap[item.product.id] = item.product.sellingPrice;
      nameMap[item.product.id] = item.product.name;
    });

    const evalResult = evaluatePromos(cartMap, priceMap, promos, nameMap);
    const promoDiscount = evalResult.totalDiscount;
    const appliedPromos = evalResult.appliedPromos.map(
      (ap) => `${ap.promoName} (-${ap.description})`
    );

    const total = Math.max(0, subtotal - promoDiscount);

    return {
      items: cartItems,
      totalItemCount,
      subtotal,
      promoDiscount,
      total,
      appliedPromos
    };
  }, [cartItems, promos]);

  // Cart Handlers
  const handleAddToCart = (product: ProductEntity) => {
    const existing = cartItems.find((i) => i.product.id === product.id);
    if (existing) {
      if (existing.quantity < product.stock) {
        setCartItems(
          cartItems.map((i) =>
            i.product.id === product.id ? { ...i, quantity: i.quantity + 1 } : i
          )
        );
      }
    } else {
      setCartItems([...cartItems, { product, quantity: 1 }]);
    }
  };

  const handleUpdateCartQty = (productId: number, qty: number) => {
    if (qty <= 0) {
      handleRemoveFromCart(productId);
    } else {
      setCartItems(
        cartItems.map((i) => (i.product.id === productId ? { ...i, quantity: qty } : i))
      );
    }
  };

  const handleRemoveFromCart = (productId: number) => {
    setCartItems(cartItems.filter((i) => i.product.id !== productId));
  };

  const handleClearCart = () => {
    setCartItems([]);
    showToast('Keranjang belanja dikosongkan');
  };

  // Compute Customer Stats
  const customersWithStats: CustomerWithStats[] = useMemo(() => {
    return customers.map((c) => {
      const customerDebts = debts.filter((d) => d.customerId === c.id && d.status !== 'PAID');
      const totalUnpaid = customerDebts.reduce((sum, d) => sum + d.remainingAmount, 0);

      const customerChanges = changeRecords.filter((ch) => ch.customerId === c.id && ch.status === 'PENDING');
      const totalPendingChange = customerChanges.reduce((sum, ch) => sum + ch.amount, 0);

      const totalPurchases = transactions.filter((t) => t.customerId === c.id && t.status === 'COMPLETED').length;

      return {
        customer: c,
        totalPurchases,
        totalUnpaid,
        totalPendingChange
      };
    });
  }, [customers, debts, changeRecords, transactions]);

  const handleSaveCustomer = (custData: CustomerEntity) => {
    const existing = customers.find((c) => c.id === custData.id);
    if (existing) {
      setCustomers((prev) => prev.map((c) => (c.id === custData.id ? custData : c)));
      showToast(`Data pelanggan "${custData.name}" diperbarui`);
    } else {
      setCustomers((prev) => [custData, ...prev]);
      showToast(`Pelanggan "${custData.name}" berhasil ditambahkan!`);
    }
  };

  const handleDeleteCustomer = (customerId: number) => {
    setCustomers((prev) => prev.filter((c) => c.id !== customerId));
    showToast('Data pelanggan dihapus');
  };

  // Transaction & Payment Completion
  const handleCompletePayment = (
    paymentMethod: string,
    cashReceived: number,
    debtCustomerName?: string,
    debtPhone?: string,
    changePending?: { buyerName: string; note: string },
    customerId?: number | null
  ) => {
    const now = Date.now();
    const trxNumber = `TRX-${new Date().toISOString().slice(0, 10).replace(/-/g, '')}-${String(
      transactions.length + 1
    ).padStart(3, '0')}`;

    const newTx: TransactionEntity = {
      id: Date.now(),
      transactionNumber: trxNumber,
      total: cart.total,
      subtotal: cart.subtotal,
      discount: cart.promoDiscount,
      cashReceived,
      change: Math.max(0, cashReceived - cart.total),
      paymentMethod,
      customerId: customerId || null,
      customerName: debtCustomerName || changePending?.buyerName || undefined,
      status: 'COMPLETED',
      createdAt: now
    };

    // Transaction items snapshot
    const newItems: TransactionItemEntity[] = cart.items.map((ci, idx) => ({
      id: now + idx,
      transactionId: newTx.id,
      productId: ci.product.id,
      productNameSnapshot: ci.product.name,
      costPriceSnapshot: ci.product.costPrice,
      sellingPriceSnapshot: ci.product.sellingPrice,
      quantity: ci.quantity,
      profit: (ci.product.sellingPrice - ci.product.costPrice) * ci.quantity
    }));

    // Deduct stock from products
    setProducts((prev) =>
      prev.map((p) => {
        const itemInCart = cart.items.find((ci) => ci.product.id === p.id);
        return itemInCart ? { ...p, stock: Math.max(0, p.stock - itemInCart.quantity) } : p;
      })
    );

    // If Bayar Nanti, create a debt record
    if (paymentMethod === 'Bayar Nanti' && debtCustomerName) {
      const newDebt: DebtEntity = {
        id: Date.now(),
        transactionNumber: trxNumber,
        customerId: customerId || null,
        customerName: debtCustomerName,
        customerPhone: debtPhone || '',
        amount: cart.total,
        remainingAmount: cart.total,
        dueDate: new Date(now + 7 * 86400000).toISOString().slice(0, 10),
        note: 'Transaksi Bayar Nanti Kasir',
        status: 'UNPAID',
        createdAt: now
      };
      setDebts((prev) => [newDebt, ...prev]);
    }

    // If Change Pending recorded
    if (changePending && newTx.change > 0) {
      const newChangeRecord: ChangeRecordEntity = {
        id: Date.now() + 10,
        transactionId: newTx.id,
        transactionNumber: trxNumber,
        customerId: customerId || null,
        buyerName: changePending.buyerName,
        amount: newTx.change,
        status: 'PENDING',
        note: changePending.note || '',
        createdAt: now,
        paidAt: null
      };
      setChangeRecords((prev) => [newChangeRecord, ...prev]);
    }

    setTransactions((prev) => [newTx, ...prev]);
    setTransactionItems((prev) => [...newItems, ...prev]);
    setCartItems([]);
    setShowPaymentDialog(false);
    setShowCartSheet(false);
    setLastCompletedTx(newTx);
    showToast(`Transaksi ${trxNumber} berhasil!`);
  };

  // Change Records Handler
  const handleMarkChangeGiven = (recordId: number) => {
    setChangeRecords((prev) =>
      prev.map((c) => (c.id === recordId ? { ...c, status: 'PAID', paidAt: Date.now() } : c))
    );
    showToast('Uang kembalian ditandai sudah diberikan!');
  };

  // Debt Settle Handler
  const handleConfirmSettleDebt = (
    debtId: number,
    amount: number,
    paymentMethod: string,
    note: string
  ) => {
    setDebts((prev) =>
      prev.map((d) => {
        if (d.id !== debtId) return d;
        const newRemaining = Math.max(0, d.remainingAmount - amount);
        const newStatus = newRemaining === 0 ? 'PAID' : 'PARTIALLY_PAID';
        return {
          ...d,
          remainingAmount: newRemaining,
          status: newStatus,
          note: note || d.note
        };
      })
    );
    setDebtToSettle(null);
    showToast(`Pembayaran kasbon sebesar Rp ${amount.toLocaleString('id-ID')} berhasil dicatat!`);
  };

  const handleSaveDebtItems = (
    newAmount: number,
    updatedItems: { id: number; name: string; price: number; quantity: number }[]
  ) => {
    if (!debtToEditItems) return;
    setDebts((prev) =>
      prev.map((d) => {
        if (d.id !== debtToEditItems.id) return d;
        const diff = newAmount - d.amount;
        return {
          ...d,
          amount: newAmount,
          remainingAmount: Math.max(0, d.remainingAmount + diff),
          status: d.remainingAmount + diff === 0 ? 'PAID' : d.status
        };
      })
    );
    setTransactions((prev) =>
      prev.map((t) =>
        t.transactionNumber === debtToEditItems.transactionNumber ||
        (debtToEditItems.transactionId != null && t.id === debtToEditItems.transactionId)
          ? { ...t, subtotal: newAmount, total: newAmount }
          : t
      )
    );
    setDebtToEditItems(null);
    showToast('Bon hutang berhasil diperbarui!');
  };

  // Note CRUD Handlers
  const handleSaveNote = (data: Partial<NoteEntity>) => {
    if (data.id) {
      setNotes((prev) =>
        prev.map((n) =>
          n.id === data.id ? ({ ...n, ...data, updatedAt: Date.now() } as NoteEntity) : n
        )
      );
      showToast('Catatan toko diperbarui!');
    } else {
      const newNote: NoteEntity = {
        id: Date.now(),
        title: data.title || 'Catatan',
        content: data.content || '',
        category: data.category || 'Umum',
        createdAt: Date.now(),
        updatedAt: Date.now()
      };
      setNotes((prev) => [newNote, ...prev]);
      showToast('Catatan baru berhasil disimpan!');
    }
  };

  const handleDeleteNote = (id: number) => {
    setNotes((prev) => prev.filter((n) => n.id !== id));
    showToast('Catatan berhasil dihapus');
  };

  // Expense Handlers
  const handleAddExpense = (category: string, amount: number, note: string) => {
    const newExp: ExpenseEntity = {
      id: Date.now(),
      category,
      amount,
      note,
      date: Date.now()
    };
    setExpenses((prev) => [newExp, ...prev]);
    showToast(`Pengeluaran ${category} sebesar Rp ${amount.toLocaleString('id-ID')} disimpan.`);
  };

  const handleDeleteExpense = (expId: number) => {
    setExpenses((prev) => prev.filter((e) => e.id !== expId));
    showToast('Pengeluaran dihapus');
  };

  const handleUpdateExpense = (id: number, category: string, amount: number, note: string) => {
    setExpenses((prev) =>
      prev.map((e) => (e.id === id ? { ...e, category, amount, note } : e))
    );
    setExpenseToEdit(null);
    showToast('Pengeluaran berhasil diperbarui.');
  };

  const handleDeleteTransaction = (txId: number) => {
    setTransactions((prev) => prev.filter((t) => t.id !== txId));
    showToast('Transaksi penjualan dihapus dari arus kas.');
  };

  // Product CRUD Handlers
  const handleSaveProduct = (data: Partial<ProductEntity>) => {
    if (data.id) {
      setProducts((prev) => prev.map((p) => (p.id === data.id ? ({ ...p, ...data } as ProductEntity) : p)));
      showToast(`Produk ${data.name} diperbarui!`);
    } else {
      const newProd: ProductEntity = {
        id: Date.now(),
        categoryId: data.categoryId || 1,
        name: data.name || 'Produk Baru',
        costPrice: data.costPrice || 0,
        sellingPrice: data.sellingPrice || 0,
        stock: data.stock || 0,
        minimumStock: data.minimumStock || 3,
        unit: data.unit || 'pcs'
      };
      setProducts((prev) => [newProd, ...prev]);
      showToast(`Produk ${newProd.name} berhasil ditambahkan!`);
    }
    setIsShowingAddEditProduct(false);
    setProductToEdit(null);
  };

  const handleDeleteProduct = (productId: number) => {
    setProducts((prev) => prev.filter((p) => p.id !== productId));
    showToast('Produk berhasil dihapus');
  };

  const handleRestockProduct = (productId: number, addQty: number) => {
    setProducts((prev) =>
      prev.map((p) => (p.id === productId ? { ...p, stock: p.stock + addQty } : p))
    );
    showToast(`Stok berhasil ditambah +${addQty}`);
  };

  const handleReduceStock = (productId: number, reduceQty: number, reason: string) => {
    setProducts((prev) =>
      prev.map((p) => (p.id === productId ? { ...p, stock: Math.max(0, p.stock - reduceQty) } : p))
    );
    showToast(`Stok dikurangi -${reduceQty} (${reason})`);
  };

  const handleAddCategory = (name: string): number => {
    const newId = Date.now();
    const newCat: CategoryEntity = {
      id: newId,
      name
    };
    setCategories((prev) => [...prev, newCat]);
    showToast(`Kategori '${name}' berhasil dibuat!`);
    return newId;
  };

  const handleSavePromo = (newPromo: PromoEntity) => {
    setPromos((prev) => [newPromo, ...prev]);
    showToast(`Promo "${newPromo.name}" berhasil dibuat!`);
  };

  const handleTogglePromoActive = (promoId: number, isActive: boolean) => {
    setPromos((prev) =>
      prev.map((p) => (p.id === promoId ? { ...p, isActive } : p))
    );
    showToast(isActive ? 'Promo diaktifkan' : 'Promo dinonaktifkan');
  };

  const handleDeletePromo = (promoId: number) => {
    setPromos((prev) => prev.filter((p) => p.id !== promoId));
    showToast('Promo berhasil dihapus');
  };

  // Back Navigation Handler for subviews
  const canNavigateBack =
    isShowingSettings ||
    isShowingAddEditProduct ||
    isShowingDebts ||
    isShowingPendingChanges ||
    isShowingPromo ||
    isShowingCustomers ||
    isShowingPaymentScreen;

  const handleNavigateBack = () => {
    if (isShowingSettings) setIsShowingSettings(false);
    else if (isShowingAddEditProduct) {
      setIsShowingAddEditProduct(false);
      setProductToEdit(null);
    } else if (isShowingDebts) setIsShowingDebts(false);
    else if (isShowingPendingChanges) setIsShowingPendingChanges(false);
    else if (isShowingPromo) setIsShowingPromo(false);
    else if (isShowingCustomers) setIsShowingCustomers(false);
    else if (isShowingPaymentScreen) setIsShowingPaymentScreen(false);
  };

  return (
    <DeviceFrame
      isDark={isDark}
      onToggleTheme={() => setIsDark(!isDark)}
      canNavigateBack={canNavigateBack}
      onNavigateBack={handleNavigateBack}
      viewportWidth={settings.viewportWidth || '412dp'}
      onViewportWidthChange={(newW) => setSettings({ ...settings, viewportWidth: newW })}
    >
      {/* View Router based on Subscreen Flags */}
      <div className="flex-1 flex flex-col overflow-hidden relative">
        {isShowingSettings ? (
          <SettingsScreen
            settings={settings}
            onSaveSettings={(newSettings) => {
              setSettings(newSettings);
              showToast('Pengaturan toko diperbarui');
            }}
            onResetAllData={() => {
              setProducts(INITIAL_PRODUCTS);
              setCategories(INITIAL_CATEGORIES);
              setTransactions(INITIAL_TRANSACTIONS);
              setDebts(INITIAL_DEBTS);
              setExpenses(INITIAL_EXPENSES);
              setSettings(INITIAL_SETTINGS);
              setChangeRecords(INITIAL_CHANGE_RECORDS);
              setLossRecords(INITIAL_LOSS_RECORDS);
              setNotes(INITIAL_NOTES);
              setCartItems([]);
              showToast('Database toko direset ke data bawaan.');
            }}
            onNavigateBack={() => setIsShowingSettings(false)}
          />
        ) : isShowingAddEditProduct ? (
          <AddEditProductScreen
            product={productToEdit}
            categories={categories}
            onSave={handleSaveProduct}
            onAddCategory={handleAddCategory}
            onNavigateBack={() => {
              setIsShowingAddEditProduct(false);
              setProductToEdit(null);
            }}
          />
        ) : isShowingDebts ? (
          <DebtsScreen
            debts={debts}
            onSettleDebt={(d) => setDebtToSettle(d)}
            onEditDebtItems={(d) => setDebtToEditItems(d)}
            onViewTransaction={(trxNum) => {
              const tx = transactions.find((t) => t.transactionNumber === trxNum);
              if (tx) setSelectedTxForDetail(tx);
              else showToast(`Struk ${trxNum} tidak ditemukan`);
            }}
            onDeleteDebt={(d) => {
              setDebts((prev) => prev.filter((item) => item.id !== d.id));
              showToast('Data kasbon dihapus');
            }}
            onNavigateBack={() => setIsShowingDebts(false)}
          />
        ) : isShowingPendingChanges ? (
          <PendingChangesScreen
            changeRecords={changeRecords}
            onMarkChangeGiven={handleMarkChangeGiven}
            onNavigateBack={() => setIsShowingPendingChanges(false)}
          />
        ) : isShowingPromo ? (
          <PromoScreen
            promos={promos}
            products={products}
            onSavePromo={handleSavePromo}
            onTogglePromoActive={handleTogglePromoActive}
            onDeletePromo={handleDeletePromo}
            onNavigateBack={() => setIsShowingPromo(false)}
          />
        ) : isShowingCustomers ? (
          <CustomerListScreen
            customersWithStats={customersWithStats}
            onSelectCustomer={(cws) => setSelectedCustomerForProfile(cws)}
            onAddNewCustomer={() => {
              setCustomerToEdit(null);
              setShowAddEditCustomerModal(true);
            }}
            onNavigateBack={() => setIsShowingCustomers(false)}
          />
        ) : isShowingPaymentScreen ? (
          <PaymentScreen
            cart={cart}
            settings={settings}
            customers={customers}
            onCompletePayment={(method, cash, debtName, debtPhone, changePending, custId) => {
              handleCompletePayment(method, cash, debtName, debtPhone, changePending, custId);
              setIsShowingPaymentScreen(false);
            }}
            onSaveNewCustomer={handleSaveCustomer}
            onNavigateBack={() => setIsShowingPaymentScreen(false)}
          />
        ) : (
          <>
            {/* Primary Tab Screens */}
            {activeTab === 'home' && (
              <HomeScreen
                settings={settings}
                products={products}
                transactions={transactions}
                debts={debts}
                expenses={expenses}
                changeRecords={changeRecords}
                onOpenSettings={() => setIsShowingSettings(true)}
                onOpenPromos={() => setIsShowingPromo(true)}
                onOpenDebts={() => setIsShowingDebts(true)}
                onOpenPendingChanges={() => setIsShowingPendingChanges(true)}
                onOpenNotes={() => setShowNotesDialog(true)}
                onOpenCustomers={() => setIsShowingCustomers(true)}
                onOpenCashier={() => setActiveTab('cashier')}
                onSelectTransaction={(tx) => setSelectedTxForDetail(tx)}
                onRestockProduct={(prod) => setProductToRestock(prod)}
                onMarkChangeGiven={handleMarkChangeGiven}
                onSettleDebt={(debt) => setDebtToSettle(debt)}
                onEditDebtItems={(debt) => setDebtToEditItems(debt)}
              />
            )}

            {activeTab === 'cashier' && (
              <CashierScreen
                products={products}
                categories={categories}
                cart={cart}
                onAddToCart={handleAddToCart}
                onUpdateQuantity={handleUpdateCartQty}
                onRemoveFromCart={handleRemoveFromCart}
                onClearCart={handleClearCart}
                onOpenCart={() => setShowCartSheet(true)}
                onProceedToPayment={() => setIsShowingPaymentScreen(true)}
              />
            )}

            {activeTab === 'products' && (
              <ProductsScreen
                products={products}
                categories={categories}
                onOpenAddProduct={() => {
                  setProductToEdit(null);
                  setIsShowingAddEditProduct(true);
                }}
                onOpenEditProduct={(prod) => {
                  setProductToEdit(prod);
                  setIsShowingAddEditProduct(true);
                }}
                onOpenRestock={(prod) => setProductToRestock(prod)}
                onOpenReduceStock={(prod) => setProductToReduce(prod)}
                onOpenAddExpense={() => setShowAddExpense(true)}
                onOpenPromo={() => setIsShowingPromo(true)}
                onDeleteProduct={handleDeleteProduct}
              />
            )}

            {activeTab === 'reports' && (
              <ReportsScreen
                transactions={transactions}
                transactionItems={transactionItems}
                expenses={expenses}
                incomes={[]}
                lossRecords={lossRecords}
                openingCapital={settings.openingCapital}
                onSelectTransaction={(tx) => setSelectedTxForDetail(tx)}
                onOpenAddExpense={() => {
                  setExpenseToEdit(null);
                  setShowAddExpense(true);
                }}
                onEditExpense={(exp) => {
                  setExpenseToEdit(exp);
                  setShowAddExpense(true);
                }}
                onDeleteExpense={handleDeleteExpense}
                onDeleteTransaction={handleDeleteTransaction}
              />
            )}

            {/* Android Navigation Bar */}
            <BottomNavBar
              activeTab={activeTab}
              onSelectTab={setActiveTab}
              cartCount={cart.totalItemCount}
            />
          </>
        )}

        {/* Global Toast / Snackbar Component */}
        {toastMessage && (
          <div className="absolute top-2 left-4 right-4 z-50 animate-bounce">
            <div className="bg-slate-900/95 text-white text-xs font-semibold px-4 py-2.5 rounded-full shadow-lg border border-slate-700/60 text-center flex items-center justify-center gap-2">
              <span className="w-2 h-2 rounded-full bg-brand-sky" />
              <span>{toastMessage}</span>
            </div>
          </div>
        )}
      </div>

      {/* Cart Bottom Sheet */}
      {showCartSheet && (
        <CartSheet
          cart={cart}
          onUpdateQuantity={handleUpdateCartQty}
          onRemoveFromCart={handleRemoveFromCart}
          onProceedToPayment={() => {
            setShowCartSheet(false);
            setIsShowingPaymentScreen(true);
          }}
          onDismiss={() => setShowCartSheet(false)}
        />
      )}

      {/* Payment Dialog */}
      {showPaymentDialog && (
        <PaymentDialog
          cart={cart}
          settings={settings}
          onCompletePayment={handleCompletePayment}
          onDismiss={() => setShowPaymentDialog(false)}
        />
      )}

      {/* Transaction Success Dialog */}
      {lastCompletedTx && (
        <TransactionSuccessDialog
          transaction={lastCompletedTx}
          onNewSale={() => setLastCompletedTx(null)}
          onViewDetail={(tx) => {
            setLastCompletedTx(null);
            setSelectedTxForDetail(tx);
          }}
          onDismiss={() => setLastCompletedTx(null)}
        />
      )}

      {/* Transaction Detail Receipt Dialog */}
      {selectedTxForDetail && (
        <TransactionDetailDialog
          transaction={selectedTxForDetail}
          items={transactionItems.filter((i) => i.transactionId === selectedTxForDetail.id)}
          settings={settings}
          onCancelTransaction={(txId) => {
            setTransactions((prev) =>
              prev.map((t) => (t.id === txId ? { ...t, status: 'CANCELLED' } : t))
            );
            showToast('Transaksi dibatalkan & retur dicatat');
          }}
          onDeleteTransaction={(txId) => {
            setTransactions((prev) => prev.filter((t) => t.id !== txId));
            setTransactionItems((prev) => prev.filter((i) => i.transactionId !== txId));
            showToast('Struk transaksi dihapus');
          }}
          onDismiss={() => setSelectedTxForDetail(null)}
        />
      )}

      {/* Settle Debt Dialog */}
      {debtToSettle && (
        <SettleDebtDialog
          debt={debtToSettle}
          onConfirmSettle={handleConfirmSettleDebt}
          onDismiss={() => setDebtToSettle(null)}
        />
      )}

      {/* Edit Debt Items Dialog */}
      {debtToEditItems && (
        <EditDebtItemsDialog
          debt={debtToEditItems}
          availableProducts={products}
          onSave={handleSaveDebtItems}
          onDismiss={() => setDebtToEditItems(null)}
        />
      )}

      {/* Add Expense Dialog */}
      {showAddExpense && (
        <AddExpenseDialog
          initialExpense={expenseToEdit}
          onAddExpense={handleAddExpense}
          onUpdateExpense={handleUpdateExpense}
          onDismiss={() => {
            setShowAddExpense(false);
            setExpenseToEdit(null);
          }}
        />
      )}

      {/* Product Modals */}
      {productToRestock && (
        <RestockDialog
          product={productToRestock}
          onConfirmRestock={handleRestockProduct}
          onDismiss={() => setProductToRestock(null)}
        />
      )}

      {productToReduce && (
        <ReduceStockDialog
          product={productToReduce}
          onConfirmReduce={handleReduceStock}
          onDismiss={() => setProductToReduce(null)}
        />
      )}

      {showAddCategory && (
        <AddCategoryDialog
          onAddCategory={handleAddCategory}
          onDismiss={() => setShowAddCategory(false)}
        />
      )}

      {/* Notes Dialog */}
      {showNotesDialog && (
        <NotesDialog
          notes={notes}
          onSaveNote={handleSaveNote}
          onDeleteNote={handleDeleteNote}
          onDismiss={() => setShowNotesDialog(false)}
        />
      )}

      {/* Customer Profile Dialog */}
      {selectedCustomerForProfile && (
        <CustomerProfileDialog
          customerWithStats={selectedCustomerForProfile}
          debts={debts}
          changeRecords={changeRecords}
          transactions={transactions}
          onEditCustomer={(cust) => {
            setSelectedCustomerForProfile(null);
            setCustomerToEdit(cust);
            setShowAddEditCustomerModal(true);
          }}
          onDeleteCustomer={(id) => {
            handleDeleteCustomer(id);
            setSelectedCustomerForProfile(null);
          }}
          onSettleDebt={(d) => {
            setSelectedCustomerForProfile(null);
            setDebtToSettle(d);
          }}
          onMarkChangeGiven={(recordId) => {
            handleMarkChangeGiven(recordId);
          }}
          onSelectTransaction={(tx) => {
            setSelectedCustomerForProfile(null);
            setSelectedTxForDetail(tx);
          }}
          onDismiss={() => setSelectedCustomerForProfile(null)}
        />
      )}

      {/* Add / Edit Customer Dialog */}
      {showAddEditCustomerModal && (
        <AddEditCustomerDialog
          customerToEdit={customerToEdit}
          onSaveCustomer={handleSaveCustomer}
          onDismiss={() => {
            setShowAddEditCustomerModal(false);
            setCustomerToEdit(null);
          }}
        />
      )}
    </DeviceFrame>
  );
};
