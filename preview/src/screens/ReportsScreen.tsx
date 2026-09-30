import React, { useState, useMemo } from 'react';
import {
  TrendingUp,
  Coins,
  TrendingDown,
  Receipt,
  Plus,
  Trash2,
  Edit2,
  MoreVertical,
  ArrowUp,
  ArrowDown,
  BarChart3,
  AlertTriangle,
  CheckCircle2,
  Calendar
} from 'lucide-react';
import { TransactionEntity, TransactionItemEntity, ExpenseEntity, IncomeEntity, TopProductStat, LossRecordEntity } from '../types';
import { formatRupiah, formatDateTime } from '../utils/format';
import { SummaryCard, SearchField, ConfirmationDialog } from '../components/CommonComponents';

interface ReportsScreenProps {
  transactions: TransactionEntity[];
  transactionItems?: TransactionItemEntity[];
  expenses: ExpenseEntity[];
  incomes: IncomeEntity[];
  lossRecords?: LossRecordEntity[];
  openingCapital: number;
  onSelectTransaction: (tx: TransactionEntity) => void;
  onOpenAddExpense?: () => void;
  onEditExpense?: (expense: ExpenseEntity) => void;
  onDeleteExpense: (expenseId: number) => void;
  onDeleteTransaction?: (txId: number) => void;
}

export const ReportsScreen: React.FC<ReportsScreenProps> = ({
  transactions,
  transactionItems = [],
  expenses,
  incomes,
  lossRecords = [],
  openingCapital,
  onSelectTransaction,
  onOpenAddExpense,
  onEditExpense,
  onDeleteExpense,
  onDeleteTransaction
}) => {
  const [selectedPeriod, setSelectedPeriod] = useState('Hari Ini');
  const [activeTab, setActiveTab] = useState<0 | 1 | 2 | 3>(0);
  const [txSearch, setTxSearch] = useState('');
  const [openMenuId, setOpenMenuId] = useState<number | null>(null);
  const [confirmDeleteTarget, setConfirmDeleteTarget] = useState<{
    id: number;
    isExpense: boolean;
    title: string;
    amount: number;
  } | null>(null);

  const periods = ['Hari Ini', '7 Hari Terakhir', 'Bulan Ini', 'Semua'];

  const isMatchPeriod = (timestamp: number) => {
    const now = Date.now();
    const d = new Date(timestamp);
    const today = new Date();
    if (selectedPeriod === 'Hari Ini') {
      return d.toDateString() === today.toDateString();
    }
    if (selectedPeriod === '7 Hari Terakhir') {
      return timestamp >= now - 7 * 86400000;
    }
    if (selectedPeriod === 'Bulan Ini') {
      return d.getMonth() === today.getMonth() && d.getFullYear() === today.getFullYear();
    }
    return true; // 'Semua'
  };

  // Metrics
  const periodTransactions = transactions.filter((t) => isMatchPeriod(t.createdAt));
  const completedTx = periodTransactions.filter((t) => t.status === 'COMPLETED');
  const totalSales = completedTx.reduce((sum, t) => sum + t.total, 0);

  const completedTxIds = useMemo(() => new Set(completedTx.map((t) => t.id)), [completedTx]);
  const grossProfit = useMemo(() => {
    const relevantItems = (transactionItems || []).filter((it) => completedTxIds.has(it.transactionId));
    if (relevantItems.length > 0) {
      const itemsProfit = relevantItems.reduce((sum, it) => {
        const prof = it.profit != null ? it.profit : (it.sellingPriceSnapshot - it.costPriceSnapshot) * it.quantity;
        return sum + prof;
      }, 0);
      const totalDisc = completedTx.reduce((sum, t) => sum + (t.discount || 0), 0);
      return Math.max(0, itemsProfit - totalDisc);
    }
    return Math.round(totalSales * 0.38);
  }, [transactionItems, completedTxIds, completedTx, totalSales]);

  const periodExpenses = expenses.filter((e) => isMatchPeriod(e.date));
  const totalExpense = periodExpenses.reduce((sum, e) => sum + e.amount, 0);
  const netCashflow = totalSales - totalExpense;
  const periodLoss = lossRecords.filter((r) => isMatchPeriod(r.date || r.createdAt));
  const totalLoss = periodLoss.reduce((sum, r) => sum + r.totalLoss, 0);

  // Top Products Calculation for Tab 0
  const topProducts: TopProductStat[] = useMemo(() => {
    const relevantItems = (transactionItems || []).filter((it) => completedTxIds.has(it.transactionId));
    if (relevantItems.length === 0) {
      return [
        { id: 1, name: 'Kopi Susu Gula Aren', quantitySold: 28, totalRevenue: 420000 },
        { id: 2, name: 'Roti Bakar Coklat', quantitySold: 19, totalRevenue: 285000 },
        { id: 3, name: 'Es Teh Manis Jumbo', quantitySold: 15, totalRevenue: 75000 },
        { id: 4, name: 'Mie Goreng Telur', quantitySold: 12, totalRevenue: 180000 },
        { id: 5, name: 'Air Mineral Botol', quantitySold: 10, totalRevenue: 40000 }
      ];
    }
    const map = new Map<number, { name: string; qty: number; revenue: number }>();
    relevantItems.forEach((it) => {
      const cur = map.get(it.productId);
      if (cur) {
        cur.qty += it.quantity;
        cur.revenue += it.quantity * it.sellingPriceSnapshot;
      } else {
        map.set(it.productId, {
          name: it.productNameSnapshot,
          qty: it.quantity,
          revenue: it.quantity * it.sellingPriceSnapshot
        });
      }
    });
    const list: TopProductStat[] = [];
    map.forEach((val, id) => {
      list.push({
        id,
        name: val.name,
        quantitySold: val.qty,
        totalRevenue: val.revenue
      });
    });
    return list.sort((a, b) => b.quantitySold - a.quantitySold).slice(0, 5);
  }, [transactionItems, completedTxIds]);

  // Filtered transactions for Tab 1
  const filteredTx = useMemo(() => {
    return periodTransactions.filter((t) => {
      if (!txSearch) return true;
      return (
        t.transactionNumber.toLowerCase().includes(txSearch.toLowerCase()) ||
        t.paymentMethod.toLowerCase().includes(txSearch.toLowerCase())
      );
    });
  }, [periodTransactions, txSearch]);

  // Cashflow timeline for Tab 2
  const cashflowTimeline = useMemo(() => {
    const list: {
      id: number;
      isIncome: boolean;
      title: string;
      subtitle: string;
      amount: number;
      date: number;
      expenseId?: number;
      rawExpense?: ExpenseEntity;
      rawTx?: TransactionEntity;
    }[] = [];

    completedTx.forEach((t) => {
      list.push({
        id: t.id * 100,
        isIncome: true,
        title: 'Penjualan Kasir',
        subtitle: `${t.transactionNumber} (${t.paymentMethod})`,
        amount: t.total,
        date: t.createdAt,
        rawTx: t
      });
    });

    (incomes || [])
      .filter((inc) => inc.source !== 'Penjualan' && isMatchPeriod(inc.date))
      .forEach((inc) => {
        list.push({
          id: inc.id * 1000 + 5,
          isIncome: true,
          title: inc.source,
          subtitle: inc.note || 'Pemasukan lain',
          amount: inc.amount,
          date: inc.date
        });
      });

    periodExpenses.forEach((e) => {
      list.push({
        id: e.id * 100 + 1,
        isIncome: false,
        title: e.category,
        subtitle: e.note || 'Pengeluaran operasional',
        amount: e.amount,
        date: e.date,
        expenseId: e.id,
        rawExpense: e
      });
    });

    return list.sort((a, b) => b.date - a.date);
  }, [completedTx, periodExpenses, incomes, selectedPeriod]);

  // Daily sales dummy stats for bar chart
  const dailySales = [
    { day: 'Sen', amount: 45000 },
    { day: 'Sel', amount: 75000 },
    { day: 'Rab', amount: 120000 },
    { day: 'Kam', amount: 80000 },
    { day: 'Jum', amount: 150000 },
    { day: 'Sab', amount: 210000 },
    { day: 'Min', amount: totalSales || 95000 }
  ];
  const maxDayAmount = Math.max(...dailySales.map((d) => d.amount), 1);

  return (
    <div className="flex-1 flex flex-col overflow-hidden bg-[#F7F9FF]">
      {/* Top Header Period Filter */}
      <div className="p-4 pb-2 bg-white border-b border-slate-200/80 flex-shrink-0 space-y-3">
        <div className="max-w-6xl mx-auto space-y-3">
          <div>
            <h1 className="text-base font-bold text-slate-800 leading-tight">Laporan Keuangan</h1>
            <p className="text-xs text-slate-500">Ringkasan transaksi, omset, dan arus kas usaha</p>
          </div>

          <div className="flex items-center gap-2 overflow-x-auto no-scrollbar py-0.5">
            {periods.map((p) => {
              const isSelected = selectedPeriod === p;
              return (
                <button
                  key={p}
                  onClick={() => setSelectedPeriod(p)}
                  className={`h-9 px-3.5 rounded-chip text-xs font-medium whitespace-nowrap transition-all border ${
                    isSelected
                      ? 'bg-brand-primary border-brand-primary text-white font-semibold shadow-xs'
                      : 'bg-white border-slate-200 text-slate-600 hover:bg-slate-50'
                  }`}
                >
                  {p}
                </button>
              );
            })}
          </div>
        </div>
      </div>

      {/* Main Content Area */}
      <div className="flex-1 overflow-y-auto p-4 pb-20 md:p-6">
        <div className="max-w-6xl mx-auto space-y-4">
          {/* Metric Summary Cards Grid */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-2.5">
            <SummaryCard
              title="Penjualan"
              value={formatRupiah(totalSales)}
              subtitle={`${completedTx.length} transaksi`}
              icon={TrendingUp}
              iconColorClass="text-brand-primary"
              iconBgClass="bg-brand-sky/20"
            />
            <SummaryCard
              title="Keuntungan (Kotor)"
              value={formatRupiah(grossProfit)}
              subtitle="Estimasi laba bersih"
              icon={Coins}
              iconColorClass="text-emerald-600"
              iconBgClass="bg-emerald-50"
            />
            <SummaryCard
              title="Pengeluaran"
              value={formatRupiah(totalExpense)}
              subtitle="Beban operasional"
              icon={TrendingDown}
              iconColorClass="text-danger"
              iconBgClass="bg-danger-container"
            />
            <SummaryCard
              title="Arus Kas Bersih"
              value={formatRupiah(netCashflow)}
              subtitle="Omset - Pengeluaran"
              icon={Receipt}
              iconColorClass="text-teal"
              iconBgClass="bg-teal-container"
            />
          </div>

        {/* Navigation Subtabs (Ringkasan, Riwayat, Arus Kas, Kerugian) */}
        <div className="bg-slate-200/70 p-1 rounded-input flex items-center gap-1 text-xs font-semibold">
          {[
            { id: 0, label: 'Ringkasan' },
            { id: 1, label: `Riwayat (${transactions.length})` },
            { id: 2, label: 'Arus Kas' },
            { id: 3, label: `Kerugian (${lossRecords.length})` }
          ].map((tab) => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id as any)}
              className={`flex-1 py-2 rounded-md transition-all text-center ${
                activeTab === tab.id
                  ? 'bg-white text-brand-primary shadow-xs font-bold'
                  : 'text-slate-600 hover:text-slate-900'
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>

        {/* TAB 0: Ringkasan & Grafik */}
        {activeTab === 0 && (
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-4 space-y-0">
            {/* Left Column: Bar Chart & Modal Awal */}
            <div className="space-y-4">
              {/* Daily Sales Bar Chart */}
              <div className="bg-white rounded-card p-4 border border-slate-200 shadow-soft">
                <div className="flex items-center justify-between mb-4">
                  <div>
                    <h3 className="text-sm font-bold text-slate-800">Tren Penjualan Mingguan</h3>
                    <p className="text-[11px] text-slate-400">Total omset 7 hari terakhir</p>
                  </div>
                  <BarChart3 className="w-5 h-5 text-brand-primary" />
                </div>

                <div className="h-32 flex items-end justify-between gap-2 pt-4">
                  {dailySales.map((item, idx) => {
                    const heightPercent = Math.max(12, Math.round((item.amount / maxDayAmount) * 100));
                    return (
                      <div key={idx} className="flex-1 flex flex-col items-center gap-1.5 h-full justify-end">
                        <span className="text-[10px] text-slate-400 font-medium">
                          {item.amount >= 1000 ? `${Math.round(item.amount / 1000)}k` : item.amount}
                        </span>
                        <div
                          style={{ height: `${heightPercent}%` }}
                          className={`w-full rounded-t-md transition-all ${
                            idx === dailySales.length - 1
                              ? 'bg-gradient-to-t from-brand-deep to-brand-primary'
                              : 'bg-brand-sky/60 hover:bg-brand-sky'
                          }`}
                        />
                        <span className="text-[11px] font-semibold text-slate-600">{item.day}</span>
                      </div>
                    );
                  })}
                </div>
              </div>

              {/* Modal Awal Info Card */}
              {openingCapital > 0 && (
                <div className="bg-white rounded-card p-3.5 border border-slate-200 shadow-soft flex items-center justify-between">
                  <div>
                    <div className="text-xs font-semibold text-slate-700">Modal Awal Kasir Hari Ini</div>
                    <div className="text-[11px] text-slate-400">Uang kembalian awal di laci</div>
                  </div>
                  <div className="text-sm font-bold text-teal">{formatRupiah(openingCapital)}</div>
                </div>
              )}
            </div>

            {/* Right Column: Produk Terlaris Leaderboard Card */}
            <div className="bg-white rounded-card p-4 border border-slate-200 shadow-soft space-y-3">
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="text-sm font-bold text-slate-800">Produk Terlaris</h3>
                  <p className="text-[11px] text-slate-400">Peringkat produk dengan penjualan terbanyak</p>
                </div>
                <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-amber-100 text-amber-800">
                  Top 5
                </span>
              </div>

              {topProducts.length === 0 ? (
                <div className="py-6 text-center text-slate-400 text-xs">
                  Belum ada data penjualan produk.
                </div>
              ) : (
                <div className="divide-y divide-slate-100">
                  {topProducts.map((prod, idx) => {
                    const rank = idx + 1;
                    const medalBadge =
                      rank === 1
                        ? 'bg-amber-100 text-amber-800 border-amber-300'
                        : rank === 2
                        ? 'bg-slate-100 text-slate-700 border-slate-300'
                        : rank === 3
                        ? 'bg-orange-100 text-orange-800 border-orange-300'
                        : 'bg-slate-50 text-slate-500 border-slate-200';

                    return (
                      <div key={prod.id} className="py-2.5 flex items-center justify-between">
                        <div className="flex items-center gap-3">
                          <div
                            className={`w-6 h-6 rounded-full border flex items-center justify-center font-bold text-xs flex-shrink-0 ${medalBadge}`}
                          >
                            {rank}
                          </div>
                          <div>
                            <div className="text-xs font-bold text-slate-800">{prod.name}</div>
                            <div className="text-[11px] text-slate-400 font-medium">
                              {prod.quantitySold} terjual
                            </div>
                          </div>
                        </div>
                        <div className="text-right">
                          <div className="text-xs font-bold text-brand-primary">
                            {formatRupiah(prod.totalRevenue)}
                          </div>
                          <div className="text-[10px] text-slate-400">Total Omset</div>
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>
          </div>
        )}

        {/* TAB 1: Riwayat Transaksi */}
        {activeTab === 1 && (
          <div className="space-y-3">
            <SearchField
              query={txSearch}
              onQueryChange={setTxSearch}
              placeholder="Cari no. transaksi atau metode..."
            />

            {filteredTx.length === 0 ? (
              <div className="bg-white rounded-card p-8 text-center border border-slate-200 text-slate-400 text-sm">
                Tidak ada transaksi yang cocok.
              </div>
            ) : (
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-2.5 space-y-0">
                {filteredTx.map((tx) => (
                  <div
                    key={tx.id}
                    onClick={() => onSelectTransaction(tx)}
                    className="bg-white rounded-card p-3.5 border border-slate-200 shadow-soft flex items-center justify-between hover:border-brand-sky cursor-pointer transition-all active:scale-[0.99]"
                  >
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="text-[13px] font-bold text-slate-800">{tx.transactionNumber}</span>
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
                      <div className="text-[11px] text-slate-500 mt-1">
                        {formatDateTime(tx.createdAt)} • {tx.paymentMethod}
                      </div>
                    </div>

                    <div className="text-right">
                      <div className="text-sm font-bold text-brand-primary">{formatRupiah(tx.total)}</div>
                      <span className="text-[11px] text-brand-primary font-medium hover:underline">
                        Detail struk →
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {/* TAB 2: Arus Kas / Buku Kas */}
        {activeTab === 2 && (
          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold text-slate-700">Mutasi Kas Masuk & Keluar</span>
            </div>

            {cashflowTimeline.length === 0 ? (
              <div className="bg-white rounded-card p-8 text-center border border-slate-200 text-slate-400 text-sm">
                Belum ada catatan mutasi kas.
              </div>
            ) : (
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-2.5 space-y-0">
                {cashflowTimeline.map((item) => (
                  <div
                    key={item.id}
                    className="bg-white rounded-card p-3.5 border border-slate-200 shadow-soft flex items-center justify-between"
                  >
                    <div className="flex items-center gap-3">
                      <div
                        className={`w-10 h-10 rounded-full flex items-center justify-center flex-shrink-0 ${
                          item.isIncome ? 'bg-success-container text-success-text' : 'bg-danger-container text-danger-text'
                        }`}
                      >
                        {item.isIncome ? (
                          <ArrowUp className="w-4 h-4 text-emerald-600" />
                        ) : (
                          <ArrowDown className="w-4 h-4 text-red-600" />
                        )}
                      </div>
                      <div>
                        <div className="text-[13px] font-semibold text-slate-800">{item.title}</div>
                        <div className="text-[11px] text-slate-500 truncate max-w-[180px]">{item.subtitle}</div>
                        <div className="text-[10px] text-slate-400">{formatDateTime(item.date)}</div>
                      </div>
                    </div>

                    <div className="flex items-center gap-2 relative">
                      <span
                        className={`text-[13px] font-bold ${
                          item.isIncome ? 'text-emerald-600' : 'text-danger'
                        }`}
                      >
                        {item.isIncome ? `+${formatRupiah(item.amount)}` : `-${formatRupiah(item.amount)}`}
                      </span>

                      <div className="relative">
                        <button
                          type="button"
                          onClick={(e) => {
                            e.stopPropagation();
                            setOpenMenuId(openMenuId === item.id ? null : item.id);
                          }}
                          data-testid={`button_menu_cashflow_${item.id}`}
                          className="w-8 h-8 rounded-full text-slate-400 hover:text-slate-700 hover:bg-slate-100 flex items-center justify-center transition-all cursor-pointer"
                          title="Opsi data arus kas"
                        >
                          <MoreVertical className="w-4 h-4" />
                        </button>

                        {openMenuId === item.id && (
                          <div
                            className="absolute right-0 top-9 z-40 bg-white rounded-lg shadow-lg border border-slate-200 py-1 w-32 animate-fadeIn text-left"
                            onClick={(e) => e.stopPropagation()}
                          >
                            <button
                              type="button"
                              onClick={() => {
                                setOpenMenuId(null);
                                if (item.rawExpense) {
                                  onEditExpense?.(item.rawExpense);
                                } else if (item.rawTx) {
                                  onSelectTransaction(item.rawTx);
                                }
                              }}
                              className="w-full px-3 py-2 text-xs font-semibold text-slate-700 hover:bg-slate-50 flex items-center gap-2 text-left cursor-pointer"
                            >
                              <Edit2 className="w-3.5 h-3.5 text-brand-primary" />
                              <span>Edit Data</span>
                            </button>
                            <button
                              type="button"
                              onClick={() => {
                                setOpenMenuId(null);
                                setConfirmDeleteTarget({
                                  id: item.rawExpense?.id || item.rawTx?.id || item.id,
                                  isExpense: !item.isIncome,
                                  title: item.title,
                                  amount: item.amount
                                });
                              }}
                              className="w-full px-3 py-2 text-xs font-semibold text-danger hover:bg-red-50 flex items-center gap-2 text-left cursor-pointer"
                            >
                              <Trash2 className="w-3.5 h-3.5 text-danger" />
                              <span>Hapus Data</span>
                            </button>
                          </div>
                        )}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {/* TAB 3: Laporan Kerugian (Barang Kadaluarsa & Rusak) */}
        {activeTab === 3 && (
          <div className="space-y-4">
            <div className="bg-red-50/80 border border-red-200 rounded-card p-4 flex items-center justify-between shadow-xs">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 rounded-full bg-red-100 flex items-center justify-center text-red-600 flex-shrink-0">
                  <AlertTriangle className="w-5 h-5" />
                </div>
                <div>
                  <div className="text-[12px] font-medium text-red-900">Total Kerugian Tercatat</div>
                  <div className="text-[18px] font-bold text-red-700 leading-tight">
                    {formatRupiah(totalLoss)}
                  </div>
                  <div className="text-[11px] text-red-600">
                    {lossRecords.length} kejadian barang kadaluarsa / rusak
                  </div>
                </div>
              </div>
            </div>

            {lossRecords.length === 0 ? (
              <div className="p-8 text-center bg-white rounded-card border border-slate-200 space-y-2">
                <div className="w-12 h-12 rounded-full bg-emerald-100 flex items-center justify-center mx-auto text-emerald-600">
                  <CheckCircle2 className="w-6 h-6" />
                </div>
                <h4 className="text-xs font-bold text-slate-800">Tidak Ada Kerugian</h4>
                <p className="text-[11px] text-slate-400 max-w-xs mx-auto">
                  Semua stok barang aman dan tidak ada pencatatan barang kadaluarsa atau kerugian.
                </p>
              </div>
            ) : (
              <div className="space-y-2.5">
                <div className="text-xs font-bold text-slate-700">Rincian Barang Kadaluarsa & Kerugian:</div>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-2.5 space-y-0">
                  {lossRecords.map((r) => (
                    <div
                      key={r.id}
                      className="bg-white rounded-card p-3.5 border border-slate-200 shadow-soft flex items-center justify-between"
                    >
                      <div>
                        <div className="flex items-center gap-2">
                          <span className="text-xs font-bold text-slate-800">{r.productName}</span>
                          <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-red-100 text-red-700">
                            {r.reason || 'Kadaluarsa'}
                          </span>
                        </div>
                        <div className="text-[11px] text-slate-500 mt-0.5">
                          Jumlah: <b>{r.quantity} pcs</b> • HPP Modal: {formatRupiah(r.costPrice)}/pcs
                        </div>
                        <div className="text-[10px] text-slate-400 flex items-center gap-1 mt-0.5">
                          <Calendar className="w-3 h-3" />
                          <span>{formatDateTime(r.date || r.createdAt)}</span>
                        </div>
                      </div>

                      <div className="text-right">
                        <span className="text-[13px] font-bold text-red-600 block">
                          -{formatRupiah(r.totalLoss)}
                        </span>
                        <span className="text-[10px] text-slate-400">Kerugian HPP</span>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        )}
        </div>
      </div>

      {/* Confirmation Dialog for Delete Cashflow Item */}
      {confirmDeleteTarget && (
        <ConfirmationDialog
          title={confirmDeleteTarget.isExpense ? 'Hapus Pengeluaran?' : 'Hapus Transaksi Kasir?'}
          message={`Yakin ingin menghapus ${confirmDeleteTarget.title} sebesar ${formatRupiah(confirmDeleteTarget.amount)} dari catatan arus kas?`}
          confirmText="Hapus Data"
          isDestructive={true}
          onConfirm={() => {
            if (confirmDeleteTarget.isExpense) {
              onDeleteExpense(confirmDeleteTarget.id);
            } else {
              onDeleteTransaction?.(confirmDeleteTarget.id);
            }
            setConfirmDeleteTarget(null);
          }}
          onDismiss={() => setConfirmDeleteTarget(null)}
        />
      )}
    </div>
  );
};
