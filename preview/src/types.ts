export interface CategoryEntity {
  id: number;
  name: string;
  icon?: string;
}

export interface ProductEntity {
  id: number;
  categoryId: number;
  name: string;
  costPrice: number;
  sellingPrice: number;
  stock: number;
  minimumStock: number;
  unit: string;
  barcode?: string;
  expirationDate?: number | string | null;
}

export interface CartItem {
  product: ProductEntity;
  quantity: number;
}

export interface PromoRequirement {
  productId: number;
  quantity: number;
}

export interface PromoEntity {
  id: number;
  name: string;
  title?: string;
  description?: string;
  isActive: boolean;
  discountType: 'NOMINAL' | 'PERCENTAGE' | 'FREE_PRODUCT';
  discountValue: number;
  maxUsage: number; // 0 = unlimited
  requiredItemsJson: string; // JSON string of PromoRequirement[]
  freeProductId?: number | null;
  freeQuantity?: number;
  minPurchase?: number;
  createdAt?: number;
  updatedAt?: number;
}

export interface CartSummary {
  items: CartItem[];
  totalItemCount: number;
  subtotal: number;
  promoDiscount: number;
  total: number;
  appliedPromos: string[];
}

export interface TransactionEntity {
  id: number;
  transactionNumber: string;
  total: number;
  subtotal: number;
  discount: number;
  cashReceived: number;
  change: number;
  paymentMethod: string;
  customerName?: string;
  customerId?: number | null;
  status: 'COMPLETED' | 'CANCELLED';
  createdAt: number;
}

export interface TransactionItemEntity {
  id: number;
  transactionId: number;
  productId: number;
  productNameSnapshot: string;
  costPriceSnapshot: number;
  sellingPriceSnapshot: number;
  quantity: number;
  profit: number;
}

export interface DebtEntity {
  id: number;
  transactionId?: number;
  transactionNumber: string;
  customerName: string;
  customerPhone: string;
  customerId?: number | null;
  amount: number;
  remainingAmount: number;
  dueDate: string;
  note: string;
  status: 'UNPAID' | 'PARTIALLY_PAID' | 'PAID';
  createdAt: number;
}

export interface CustomerEntity {
  id: number;
  name: string;
  phone: string;
  notes: string;
  createdAt: number;
  updatedAt: number;
}

export interface CustomerWithStats {
  customer: CustomerEntity;
  totalPurchases: number;
  totalUnpaid: number;
  totalPendingChange: number;
}

export interface ExpenseEntity {
  id: number;
  category: string;
  amount: number;
  note: string;
  date: number;
}

export interface IncomeEntity {
  id: number;
  source: string;
  amount: number;
  note?: string;
  date: number;
}

export interface DailySalesStat {
  dayLabel: string;
  amount: number;
}

export interface TopProductStat {
  id: number;
  name: string;
  quantitySold: number;
  totalRevenue: number;
}

export interface ReportStats {
  totalSales: number;
  grossProfit: number;
  totalExpense: number;
  netCashflow: number;
  transactionCount: number;
  itemsSold: number;
  dailySales: DailySalesStat[];
  topProducts: TopProductStat[];
}

export interface ChangeRecordEntity {
  id: number;
  transactionId?: number | null;
  transactionNumber: string;
  buyerName: string;
  customerId?: number | null;
  amount: number;
  status: 'PENDING' | 'PAID';
  note: string;
  createdAt: number;
  paidAt?: number | null;
  cashierName?: string;
}

export interface LossRecordEntity {
  id: number;
  productId: number;
  productName: string;
  quantity: number;
  costPrice: number;
  totalLoss: number;
  reason: string;
  date: number;
  createdAt: number;
}

export interface NoteEntity {
  id: number;
  title: string;
  content: string;
  category: string; // 'Umum' | 'Transaksi' | 'Stok' | 'Kembalian' | 'Keuangan'
  relatedEntityId?: number | null;
  createdAt: number;
  updatedAt: number;
}

export interface BusinessSettings {
  businessName: string;
  address: string;
  phone: string;
  receiptFooter: string;
  defaultPaymentMethod: string;
  qrisImagePath: string;
  qrisImageUri?: string | null;
  qrisMerchantName: string;
  openingCapital: number;
  previousSales: number;
  previousSalesDate: string;
  previousSalesNote: string;
  themeMode: 'SYSTEM' | 'LIGHT' | 'DARK';
  viewportWidth?: '360dp' | '412dp' | '430dp';
}

