import React, { useState } from 'react';
import {
  TrendingUp,
  Coins,
  Receipt,
  TrendingDown,
  Hourglass,
  AlertTriangle,
  Settings,
  Tag,
  ArrowRight,
  Plus,
  CheckCircle2,
  Check,
  FileText,
  Clock,
  Edit3,
  ChevronRight,
  Users
} from 'lucide-react';
import {
  ProductEntity,
  TransactionEntity,
  DebtEntity,
  ExpenseEntity,
  BusinessSettings,
  ChangeRecordEntity
} from '../types';
import { formatRupiah, formatDateTime } from '../utils/format';
import { KelolaLogoBadge } from '../components/KelolaLogo';
import { SummaryCard, StockBadge, ConfirmationDialog } from '../components/CommonComponents';

interface HomeScreenProps {
  settings: BusinessSettings;
  products: ProductEntity[];
  transactions: TransactionEntity[];
  debts: DebtEntity[];
  expenses: ExpenseEntity[];
  changeRecords: ChangeRecordEntity[];
  onOpenSettings: () => void;
  onOpenPromos: () => void;
  onOpenDebts: () => void;
  onOpenPendingChanges: () => void;
  onOpenNotes: () => void;
  onOpenCustomers?: () => void;
  onOpenCashier: () => void;
  onSelectTransaction: (tx: TransactionEntity) => void;
  onRestockProduct: (product: ProductEntity) => void;
  onMarkChangeGiven: (recordId: number) => void;
  onSettleDebt: (debt: DebtEntity) => void;
  onEditDebtItems: (debt: DebtEntity) => void;
}

export const HomeScreen: React.FC<HomeScreenProps> = ({
  settings,
  products,
  transactions,
  debts,
  expenses,
  changeRecords,
  onOpenSettings,
  onOpenPromos,
  onOpenDebts,
  onOpenPendingChanges,
  onOpenNotes,
  onOpenCashier,
  onSelectTransaction,
  onRestockProduct,
  onMarkChangeGiven,
  onSettleDebt,
  onEditDebtItems,
  onOpenCustomers
}) => {
  // Calculations
  const completedTx = transactions.filter((t) => t.status === 'COMPLETED');
  const todaySales = completedTx.reduce((sum, t) => sum + t.total, 0);
  const totalExpense = expenses.reduce((sum, e) => sum + e.amount, 0);
  const currentCash = settings.openingCapital + todaySales - totalExpense;

  const unpaidDebts = debts.filter((d) => d.status !== 'PAID');
  const totalUnpaidAmount = unpaidDebts.reduce((sum, d) => sum + d.remainingAmount, 0);

  const pendingChanges = changeRecords.filter((c) => c.status === 'PENDING');
  const totalPendingChange = pendingChanges.reduce((sum, c) => sum + c.amount, 0);

  const lowStockProducts = products.filter((p) => p.stock <= p.minimumStock);

  return (
    <div className="flex-1 overflow-y-auto bg-[#F7F9FF] px-4 py-3 md:px-6 lg:px-8">
      <div className="max-w-6xl mx-auto space-y-4">
        {/* Top App Bar */}
        <div className="flex items-center justify-between pt-1">
          <div className="flex items-center gap-2.5">
            <KelolaLogoBadge size={36} />
            <div>
              <h1 className="text-[16px] font-bold text-slate-900 leading-tight">
                {settings.businessName || 'Kelola'}
              </h1>
              <p className="text-[11px] text-slate-500 font-medium">Aplikasi Kasir Usaha</p>
            </div>
          </div>

          <div className="flex items-center gap-1.5">
            <button
              onClick={onOpenSettings}
              data-testid="button_open_settings"
              className="w-9 h-9 rounded-full bg-white border border-slate-200 flex items-center justify-center text-slate-600 hover:text-brand-primary hover:border-brand-sky shadow-xs transition-all cursor-pointer"
              title="Pengaturan"
            >
              <Settings className="w-4 h-4" />
            </button>
          </div>
        </div>

        {/* Top Financial Section: Hero Card Saldo Kas + Ringkasan Hari Ini (Responsive Grid on Tablet) */}
        <div className="grid grid-cols-1 md:grid-cols-12 gap-3.5 items-stretch">
          {/* Hero Card Saldo Kas (36sp / displayMedium) */}
          <div className="md:col-span-7 bg-gradient-to-br from-[#004974] via-[#006199] to-[#0B658A] rounded-card p-5 text-white shadow-md relative overflow-hidden flex flex-col justify-between">
            <div className="absolute -right-6 -bottom-6 w-32 h-32 rounded-full bg-white/5 pointer-events-none" />
            <div className="absolute right-12 -top-6 w-20 h-20 rounded-full bg-white/5 pointer-events-none" />

            <div>
              <div className="flex items-center gap-2 mb-1 text-slate-100/90 text-[12px] font-medium">
                <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
                <span>Saldo Kas Toko Saat Ini</span>
              </div>

              <div className="text-[32px] sm:text-[36px] font-bold tracking-tight text-white mb-3">
                {formatRupiah(currentCash)}
              </div>
            </div>

            <div className="pt-3 border-t border-white/15 flex items-center justify-between text-[12px]">
              <div>
                <span className="text-slate-200/80 block text-[11px]">Penjualan Hari Ini</span>
                <span className="font-semibold text-emerald-300">+{formatRupiah(todaySales)}</span>
              </div>
              <div className="text-right">
                <span className="text-slate-200/80 block text-[11px]">Pengeluaran</span>
                <span className="font-semibold text-red-300">-{formatRupiah(totalExpense)}</span>
              </div>
            </div>
          </div>

          {/* Ringkasan Metrik Finansial */}
          <div className="md:col-span-5 flex flex-col justify-between">
            <h2 className="text-[14px] font-bold text-slate-800 mb-2.5">Ringkasan Hari Ini</h2>
            <div className="grid grid-cols-2 gap-3 flex-1">
              <SummaryCard
                title="Omset Kasir"
                value={formatRupiah(todaySales)}
                subtitle={`${completedTx.length} transaksi selesai`}
                icon={TrendingUp}
                iconColorClass="text-brand-primary"
                iconBgClass="bg-brand-sky/20"
              />
              <SummaryCard
                title="Pengeluaran"
                value={formatRupiah(totalExpense)}
                subtitle={`${expenses.length} mutasi dicatat`}
                icon={TrendingDown}
                iconColorClass="text-danger"
                iconBgClass="bg-danger-container"
              />
            </div>
          </div>
        </div>

        {/* Section: Rekap Kembalian & Kasbon (1 col mobile, 2 cols tablet) */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
          {/* SECTION: RINGKASAN KEMBALIAN BELUM DIBERIKAN */}
          <button
            onClick={onOpenPendingChanges}
            data-testid="card_pending_change_summary"
            className="w-full bg-white rounded-card p-4 border border-slate-200 shadow-soft hover:border-amber-300 flex items-center justify-between transition-all group text-left cursor-pointer"
          >
            <div className="flex items-center gap-3.5">
              <div
                className={`w-11 h-11 rounded-full flex items-center justify-center flex-shrink-0 transition-transform group-hover:scale-105 ${
                  pendingChanges.length > 0 ? 'bg-amber-100 text-amber-700' : 'bg-emerald-100 text-emerald-600'
                }`}
              >
                <Coins className="w-5 h-5" />
              </div>
              <div>
                <div className="text-[12px] font-semibold text-slate-500">
                  Kembalian Belum Diberikan
                </div>
                <div
                  className={`text-[17px] font-extrabold leading-tight ${
                    pendingChanges.length > 0 ? 'text-amber-800' : 'text-slate-800'
                  }`}
                >
                  {formatRupiah(totalPendingChange)}
                </div>
                <div className="text-[11px] text-slate-500 font-medium mt-0.5">
                  {pendingChanges.length > 0
                    ? `${pendingChanges.length} Transaksi Tertunda`
                    : 'Semua kembalian beres diberikan 👏'}
                </div>
              </div>
            </div>

            <div className="w-8 h-8 rounded-full bg-slate-50 flex items-center justify-center text-slate-400 group-hover:text-brand-primary group-hover:bg-brand-primary/10 transition-colors">
              <ChevronRight className="w-4 h-4" />
            </div>
          </button>

          {/* SECTION: RINGKASAN ORANG BELUM BAYAR / KASBON */}
          <button
            onClick={onOpenDebts}
            data-testid="card_unpaid_debts_summary"
            className="w-full bg-white rounded-card p-4 border border-slate-200 shadow-soft hover:border-brand-sky flex items-center justify-between transition-all group text-left cursor-pointer"
          >
            <div className="flex items-center gap-3.5">
              <div
                className={`w-11 h-11 rounded-full flex items-center justify-center flex-shrink-0 transition-transform group-hover:scale-105 ${
                  unpaidDebts.length > 0 ? 'bg-red-100 text-red-600' : 'bg-emerald-100 text-emerald-600'
                }`}
              >
                <Hourglass className="w-5 h-5" />
              </div>
              <div>
                <div className="text-[12px] font-semibold text-slate-500">
                  Orang Belum Bayar (Kasbon)
                </div>
                <div
                  className={`text-[17px] font-extrabold leading-tight ${
                    unpaidDebts.length > 0 ? 'text-red-700' : 'text-slate-800'
                  }`}
                >
                  {formatRupiah(totalUnpaidAmount)}
                </div>
                <div className="text-[11px] text-slate-500 font-medium mt-0.5">
                  {unpaidDebts.length > 0
                    ? `${unpaidDebts.length} Orang Belum Lunas`
                    : 'Semua kasbon lunas 👏'}
                </div>
              </div>
            </div>

            <div className="w-8 h-8 rounded-full bg-slate-50 flex items-center justify-center text-slate-400 group-hover:text-brand-primary group-hover:bg-brand-primary/10 transition-colors">
              <ChevronRight className="w-4 h-4" />
            </div>
          </button>
        </div>

        {/* Section: Shortcuts (Catatan & List Pelanggan) - 1 col mobile, 2 cols tablet */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
          {/* Catatan & Riwayat Toko */}
          <button
            onClick={onOpenNotes}
            data-testid="button_open_notes_home"
            className="w-full bg-white rounded-card p-3.5 border border-slate-200 shadow-soft hover:border-brand-sky flex items-center justify-between transition-all group text-left cursor-pointer"
          >
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-full bg-brand-primary/10 flex items-center justify-center text-brand-primary flex-shrink-0 group-hover:scale-105 transition-transform">
                <FileText className="w-5 h-5" />
              </div>
              <div>
                <div className="text-[13px] font-semibold text-slate-800">
                  Catatan & Riwayat Toko
                </div>
                <div className="text-[11px] text-slate-500">
                  Pengingat stok, catatan transaksi & aktivitas toko
                </div>
              </div>
            </div>
            <div className="w-8 h-8 rounded-full bg-slate-50 flex items-center justify-center text-slate-400 group-hover:text-brand-primary group-hover:bg-brand-primary/10 transition-colors">
              <ChevronRight className="w-4 h-4" />
            </div>
          </button>

          {/* List Pelanggan */}
          <button
            onClick={onOpenCustomers}
            data-testid="button_open_customers_home"
            className="w-full bg-white rounded-card p-3.5 border border-slate-200 shadow-soft hover:border-brand-sky flex items-center justify-between transition-all group text-left cursor-pointer"
          >
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-full bg-brand-primary/10 flex items-center justify-center text-brand-primary flex-shrink-0 group-hover:scale-105 transition-transform">
                <Users className="w-5 h-5" />
              </div>
              <div>
                <div className="text-[13px] font-semibold text-slate-800">
                  List Pelanggan
                </div>
                <div className="text-[11px] text-slate-500">
                  Kelola pelanggan setia, kasbon & kembalian
                </div>
              </div>
            </div>
            <div className="w-8 h-8 rounded-full bg-slate-50 flex items-center justify-center text-slate-400 group-hover:text-brand-primary group-hover:bg-brand-primary/10 transition-colors">
              <ChevronRight className="w-4 h-4" />
            </div>
          </button>
        </div>

        {/* Section: Perlu Restock & Transaksi Terkini (1 col mobile, 2 cols on landscape tablet/desktop) */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-4 pb-6">
          {/* Peringatan Stok Menipis */}
          <div>
            <div className="flex items-center justify-between mb-2">
              <h2 className="text-[14px] font-bold text-slate-800 flex items-center gap-1.5">
                <AlertTriangle className="w-4 h-4 text-warning" />
                <span>Perlu Restock ({lowStockProducts.length})</span>
              </h2>
            </div>
            {lowStockProducts.length === 0 ? (
              <div className="bg-white rounded-card p-5 text-center border border-slate-200 text-slate-400 text-xs">
                Semua stok produk masih aman 👍
              </div>
            ) : (
              <div className="space-y-2">
                {lowStockProducts.slice(0, 3).map((prod) => (
                  <div
                    key={prod.id}
                    className="bg-white rounded-card p-3 border border-slate-200 shadow-soft flex items-center justify-between"
                  >
                    <div>
                      <div className="text-[13px] font-semibold text-slate-800">{prod.name}</div>
                      <StockBadge stock={prod.stock} minimumStock={prod.minimumStock} unit={prod.unit} />
                    </div>
                    <button
                      onClick={() => onRestockProduct(prod)}
                      data-testid={`button_restock_${prod.id}`}
                      className="w-10 h-10 rounded-input bg-brand-primary/10 hover:bg-brand-primary/20 text-brand-primary flex items-center justify-center transition-all cursor-pointer"
                      title="Tambah Stok"
                    >
                      <Plus className="w-5 h-5" />
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Transaksi Terkini */}
          <div>
            <div className="flex items-center justify-between mb-2">
              <h2 className="text-[14px] font-bold text-slate-800">Transaksi Terkini</h2>
              <button
                onClick={onOpenCashier}
                className="text-[12px] font-semibold text-brand-primary hover:underline flex items-center gap-0.5 cursor-pointer"
              >
                <span>Buka Kasir</span>
                <ArrowRight className="w-3.5 h-3.5" />
              </button>
            </div>

            {transactions.length === 0 ? (
              <div className="bg-white rounded-card p-6 text-center border border-slate-200 text-slate-400 text-sm">
                Belum ada transaksi hari ini
              </div>
            ) : (
              <div className="space-y-2">
                {transactions.slice(0, 4).map((tx) => (
                  <div
                    key={tx.id}
                    onClick={() => onSelectTransaction(tx)}
                    className="bg-white rounded-card p-3 border border-slate-200 shadow-soft flex items-center justify-between hover:border-brand-sky cursor-pointer transition-all active:scale-[0.99]"
                  >
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="text-[13px] font-semibold text-slate-800">{tx.transactionNumber}</span>
                        <span
                          className={`text-[10px] font-bold px-1.5 py-0.5 rounded ${
                            tx.status === 'COMPLETED'
                              ? 'bg-success-container text-success-text'
                              : 'bg-danger-container text-danger-text'
                          }`}
                        >
                          {tx.status === 'COMPLETED' ? 'Selesai' : 'Batal'}
                        </span>
                      </div>
                      <div className="text-[11px] text-slate-500 mt-0.5">
                        {formatDateTime(tx.createdAt)} • {tx.paymentMethod}
                      </div>
                    </div>
                    <div className="text-right">
                      <div className="text-[14px] font-bold text-brand-primary">{formatRupiah(tx.total)}</div>
                      <span className="text-[11px] text-slate-400">Lihat struk →</span>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

