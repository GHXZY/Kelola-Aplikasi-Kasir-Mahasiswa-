import {
  CategoryEntity,
  ProductEntity,
  PromoEntity,
  TransactionEntity,
  TransactionItemEntity,
  DebtEntity,
  ExpenseEntity,
  BusinessSettings,
  ChangeRecordEntity,
  LossRecordEntity,
  NoteEntity,
  CustomerEntity
} from '../types';

const NOW = Date.now();
const ONE_HOUR = 3600 * 1000;
const ONE_DAY = 24 * ONE_HOUR;

export const INITIAL_CATEGORIES: CategoryEntity[] = [
  { id: 1, name: 'Makanan' },
  { id: 2, name: 'Minuman' },
  { id: 3, name: 'Sembako' },
  { id: 4, name: 'Snack' },
  { id: 5, name: 'Alat Tulis' },
];

export const INITIAL_PRODUCTS: ProductEntity[] = [
  {
    id: 1,
    categoryId: 2,
    name: 'Kopi Susu Gula Aren 250ml',
    costPrice: 8000,
    sellingPrice: 15000,
    stock: 24,
    minimumStock: 5,
    unit: 'botol',
    barcode: '899123456781',
    expirationDate: NOW + 7 * ONE_DAY
  },
  {
    id: 2,
    categoryId: 1,
    name: 'Nasi Ayam Geprek Sambal Bawang',
    costPrice: 12000,
    sellingPrice: 20000,
    stock: 18,
    minimumStock: 4,
    unit: 'porsi',
    barcode: '899123456782'
  },
  {
    id: 3,
    categoryId: 2,
    name: 'Es Teh Manis Melati Jumbo',
    costPrice: 2000,
    sellingPrice: 5000,
    stock: 50,
    minimumStock: 10,
    unit: 'cup',
    barcode: '899123456783'
  },
  {
    id: 4,
    categoryId: 4,
    name: 'Keripik Singkong Pedas 150g',
    costPrice: 6000,
    sellingPrice: 10000,
    stock: 32,
    minimumStock: 6,
    unit: 'bungkus',
    barcode: '899123456784'
  },
  {
    id: 5,
    categoryId: 1,
    name: 'Mie Goreng Spesial Telur',
    costPrice: 9000,
    sellingPrice: 15000,
    stock: 15,
    minimumStock: 3,
    unit: 'porsi',
    barcode: '899123456785'
  },
  {
    id: 6,
    categoryId: 3,
    name: 'Beras Pandan Wangi 5kg',
    costPrice: 68000,
    sellingPrice: 78000,
    stock: 8,
    minimumStock: 2,
    unit: 'karung',
    barcode: '899123456786'
  },
  {
    id: 7,
    categoryId: 3,
    name: 'Minyak Goreng Pouch 2L',
    costPrice: 31000,
    sellingPrice: 35000,
    stock: 14,
    minimumStock: 4,
    unit: 'pouch',
    barcode: '899123456787'
  },
  {
    id: 8,
    categoryId: 4,
    name: 'Roti Panggang Coklat Keju',
    costPrice: 7000,
    sellingPrice: 12000,
    stock: 2,
    minimumStock: 5,
    unit: 'porsi',
    barcode: '899123456788',
    expirationDate: NOW + 2 * ONE_DAY
  },
  {
    id: 9,
    categoryId: 5,
    name: 'Buku Tulis Bergaris 58 Lembar',
    costPrice: 3500,
    sellingPrice: 6000,
    stock: 0,
    minimumStock: 5,
    unit: 'pcs',
    barcode: '899123456789'
  }
];

export const INITIAL_PROMOS: PromoEntity[] = [
  {
    id: 1,
    name: 'Paket Hemat Kopi + Keripik',
    title: 'Paket Hemat Kopi + Keripik',
    description: 'Beli 1 Kopi Susu + 1 Keripik Singkong dapat potongan Rp 5.000',
    discountType: 'NOMINAL',
    discountValue: 5000,
    maxUsage: 0,
    requiredItemsJson: JSON.stringify([
      { productId: 1, quantity: 1 },
      { productId: 4, quantity: 1 }
    ]),
    isActive: true
  },
  {
    id: 2,
    name: 'Paket Kenyang Geprek + Es Teh',
    title: 'Paket Kenyang Geprek + Es Teh',
    description: 'Beli 1 Nasi Ayam Geprek + 1 Es Teh Manis potongan Rp 3.000',
    discountType: 'NOMINAL',
    discountValue: 3000,
    maxUsage: 0,
    requiredItemsJson: JSON.stringify([
      { productId: 2, quantity: 1 },
      { productId: 3, quantity: 1 }
    ]),
    isActive: true
  },
  {
    id: 3,
    name: 'Diskon 10% Beli 2 Kopi Susu',
    title: 'Diskon 10% Beli 2 Kopi Susu',
    description: 'Beli minimal 2 botol Kopi Susu Aren dapat diskon 10%',
    discountType: 'PERCENTAGE',
    discountValue: 10,
    maxUsage: 1,
    requiredItemsJson: JSON.stringify([
      { productId: 1, quantity: 2 }
    ]),
    isActive: true
  }
];

export const INITIAL_TRANSACTIONS: TransactionEntity[] = [
  {
    id: 1,
    transactionNumber: 'TRX-20260914-001',
    total: 35000,
    subtotal: 35000,
    discount: 0,
    cashReceived: 50000,
    change: 15000,
    paymentMethod: 'Tunai',
    customerId: 1,
    customerName: 'Ahmad',
    status: 'COMPLETED',
    createdAt: NOW - 2 * ONE_HOUR
  },
  {
    id: 2,
    transactionNumber: 'TRX-20260914-002',
    total: 45000,
    subtotal: 50000,
    discount: 5000,
    cashReceived: 45000,
    change: 0,
    paymentMethod: 'QRIS',
    customerId: 2,
    customerName: 'Budi Santoso',
    status: 'COMPLETED',
    createdAt: NOW - 45 * 60 * 1000
  },
  {
    id: 3,
    transactionNumber: 'TRX-20260913-001',
    total: 20000,
    subtotal: 20000,
    discount: 0,
    cashReceived: 20000,
    change: 0,
    paymentMethod: 'Tunai',
    customerId: 1,
    customerName: 'Ahmad',
    status: 'COMPLETED',
    createdAt: NOW - ONE_DAY - 3 * ONE_HOUR
  },
  {
    id: 4,
    transactionNumber: 'TRX-20260913-002',
    total: 15000,
    subtotal: 15000,
    discount: 0,
    cashReceived: 0,
    change: 0,
    paymentMethod: 'Bayar Nanti',
    status: 'COMPLETED',
    createdAt: NOW - ONE_DAY - 5 * ONE_HOUR
  }
];

export const INITIAL_TRANSACTION_ITEMS: TransactionItemEntity[] = [
  {
    id: 1,
    transactionId: 1,
    productId: 1,
    productNameSnapshot: 'Kopi Susu Gula Aren 250ml',
    costPriceSnapshot: 8000,
    sellingPriceSnapshot: 15000,
    quantity: 1,
    profit: 7000
  },
  {
    id: 2,
    transactionId: 1,
    productId: 2,
    productNameSnapshot: 'Nasi Ayam Geprek Sambal Bawang',
    costPriceSnapshot: 12000,
    sellingPriceSnapshot: 20000,
    quantity: 1,
    profit: 8000
  },
  {
    id: 3,
    transactionId: 2,
    productId: 3,
    productNameSnapshot: 'Es Teh Manis Melati Jumbo',
    costPriceSnapshot: 2000,
    sellingPriceSnapshot: 5000,
    quantity: 2,
    profit: 6000
  },
  {
    id: 4,
    transactionId: 2,
    productId: 4,
    productNameSnapshot: 'Keripik Singkong Pedas 150g',
    costPriceSnapshot: 6000,
    sellingPriceSnapshot: 10000,
    quantity: 4,
    profit: 16000
  },
  {
    id: 5,
    transactionId: 3,
    productId: 2,
    productNameSnapshot: 'Nasi Ayam Geprek Sambal Bawang',
    costPriceSnapshot: 12000,
    sellingPriceSnapshot: 20000,
    quantity: 1,
    profit: 8000
  },
  {
    id: 6,
    transactionId: 4,
    productId: 5,
    productNameSnapshot: 'Mie Goreng Spesial Telur',
    costPriceSnapshot: 9000,
    sellingPriceSnapshot: 15000,
    quantity: 1,
    profit: 6000
  }
];

export const INITIAL_DEBTS: DebtEntity[] = [
  {
    id: 1,
    transactionNumber: 'TRX-20260913-002',
    customerName: 'Ahmad',
    customerPhone: '081234567890',
    customerId: 1,
    amount: 25000,
    remainingAmount: 25000,
    dueDate: '2026-09-20',
    note: 'Ambil kopi & makan siang',
    status: 'UNPAID',
    createdAt: NOW - ONE_DAY - 5 * ONE_HOUR
  },
  {
    id: 2,
    transactionNumber: 'TRX-20260912-005',
    customerName: 'Siti Rahma',
    customerPhone: '087890123456',
    customerId: 3,
    amount: 50000,
    remainingAmount: 50000,
    dueDate: '2026-09-25',
    note: 'Beras & cemilan tugas kelompok',
    status: 'UNPAID',
    createdAt: NOW - 2 * ONE_DAY
  },
  {
    id: 3,
    transactionNumber: 'TRX-20260910-001',
    customerName: 'Budi Santoso',
    customerPhone: '085712345678',
    customerId: 2,
    amount: 30000,
    remainingAmount: 0,
    dueDate: '2026-09-12',
    note: 'Snack rapat kantin - lunas',
    status: 'PAID',
    createdAt: NOW - 4 * ONE_DAY
  }
];

export const INITIAL_EXPENSES: ExpenseEntity[] = [
  {
    id: 1,
    category: 'Bahan Baku & Dapur',
    amount: 45000,
    note: 'Beli es batu kristal & plastik cup',
    date: NOW - 3 * ONE_HOUR
  },
  {
    id: 2,
    category: 'Operasional & Listrik',
    amount: 25000,
    note: 'Token listrik darurat kasir',
    date: NOW - ONE_DAY - 2 * ONE_HOUR
  }
];

export const INITIAL_SETTINGS: BusinessSettings = {
  businessName: 'Warung Kelola Berkah',
  address: 'Jl. Raya Kampus No. 45, Gedung A',
  phone: '0812-3456-7890',
  receiptFooter: 'Terima kasih atas kunjungan Anda! Semoga harimu menyenangkan.',
  defaultPaymentMethod: 'Tunai',
  qrisImagePath: '',
  qrisImageUri: null,
  qrisMerchantName: 'WARUNG KELOLA BERKAH',
  openingCapital: 200000,
  previousSales: 0,
  previousSalesDate: '',
  previousSalesNote: '',
  themeMode: 'LIGHT',
  viewportWidth: '412dp'
};

export const INITIAL_CHANGE_RECORDS: ChangeRecordEntity[] = [
  {
    id: 1,
    transactionNumber: 'TRX-20260914-001',
    buyerName: 'Ahmad',
    customerId: 1,
    amount: 5000,
    status: 'PENDING',
    note: 'Belum ada pecahan Rp 5.000',
    createdAt: NOW - 2 * ONE_HOUR,
    paidAt: null
  },
  {
    id: 2,
    transactionNumber: 'TRX-20260914-002',
    buyerName: 'Budi Santoso',
    customerId: 2,
    amount: 10000,
    status: 'PENDING',
    note: 'Kembalian tertunda pecahan 10k',
    createdAt: NOW - ONE_HOUR,
    paidAt: null
  }
];

export const INITIAL_LOSS_RECORDS: LossRecordEntity[] = [
  {
    id: 1,
    productId: 8,
    productName: 'Roti Panggang Coklat Keju',
    quantity: 2,
    costPrice: 7000,
    totalLoss: 14000,
    reason: 'Kadaluarsa',
    date: NOW - ONE_DAY,
    createdAt: NOW - ONE_DAY
  }
];

export const INITIAL_NOTES: NoteEntity[] = [
  {
    id: 1,
    title: 'Pesan Beras Grosir Jaya',
    content: 'Stok beras sisa 8 karung, pesan 15 karung lagi hari Kamis sebelum jam 10 pagi ke Toko Grosir Jaya.',
    category: 'Stok',
    createdAt: NOW - 2 * ONE_DAY,
    updatedAt: NOW - 2 * ONE_DAY
  },
  {
    id: 2,
    title: 'Catatan Titipan Kembalian Mas Danang',
    content: 'Kembalian Rp 5.000 transaksi pagi tadi, tolong berikan kalau orangnya mampir ke toko.',
    category: 'Kembalian',
    createdAt: NOW - 2 * ONE_HOUR,
    updatedAt: NOW - 2 * ONE_HOUR
  }
];

export const INITIAL_CUSTOMERS: CustomerEntity[] = [
  {
    id: 1,
    name: 'Ahmad',
    phone: '081234567890',
    notes: 'Sering pesan Kopi Gula Aren pagi hari',
    createdAt: NOW - 10 * ONE_DAY,
    updatedAt: NOW - 10 * ONE_DAY
  },
  {
    id: 2,
    name: 'Budi Santoso',
    phone: '085712345678',
    notes: 'Kantin gedung B lantai 2',
    createdAt: NOW - 7 * ONE_DAY,
    updatedAt: NOW - 7 * ONE_DAY
  },
  {
    id: 3,
    name: 'Siti Rahma',
    phone: '087890123456',
    notes: 'Mahasiswa prodi Sistem Informasi',
    createdAt: NOW - 5 * ONE_DAY,
    updatedAt: NOW - 5 * ONE_DAY
  }
];

