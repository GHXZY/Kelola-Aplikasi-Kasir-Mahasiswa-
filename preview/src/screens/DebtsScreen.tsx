import React, { useState, useMemo } from 'react';
import {
  Hourglass,
  CheckCircle2,
  Phone,
  Receipt,
  Trash2,
  Edit3,
  CreditCard,
  ArrowLeft,
  User
} from 'lucide-react';
import { DebtEntity } from '../types';
import { formatRupiah, formatDateTime } from '../utils/format';
import { SummaryCard, SearchField, ConfirmationDialog } from '../components/CommonComponents';

interface DebtsScreenProps {
  debts: DebtEntity[];
  onSettleDebt: (debt: DebtEntity) => void;
  onEditDebtItems: (debt: DebtEntity) => void;
  onViewTransaction: (transactionNumber: string) => void;
  onDeleteDebt: (debt: DebtEntity) => void;
  onNavigateBack?: () => void;
}

export const DebtsScreen: React.FC<DebtsScreenProps> = ({
  debts,
  onSettleDebt,
  onEditDebtItems,
  onViewTransaction,
  onDeleteDebt,
  onNavigateBack
}) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedFilter, setSelectedFilter] = useState('Semua');
  const [debtToDelete, setDebtToDelete] = useState<DebtEntity | null>(null);

  const filterOptions = ['Semua', 'Belum Lunas', 'Dicicil', 'Lunas'];

  // Stats
  const totalUnpaid = debts.filter((d) => d.status !== 'PAID').reduce((sum, d) => sum + d.remainingAmount, 0);
  const unpaidCount = debts.filter((d) => d.status !== 'PAID').length;
  const totalPaid = debts.reduce((sum, d) => sum + (d.amount - d.remainingAmount), 0);

  const filteredDebts = useMemo(() => {
    return debts.filter((d) => {
      const matchSearch =
        !searchQuery ||
        d.customerName.toLowerCase().includes(searchQuery.toLowerCase()) ||
        d.customerPhone.includes(searchQuery) ||
        d.transactionNumber.toLowerCase().includes(searchQuery.toLowerCase());

      const matchFilter =
        selectedFilter === 'Semua' ||
        (selectedFilter === 'Belum Lunas' && d.status === 'UNPAID') ||
        (selectedFilter === 'Dicicil' && d.status === 'PARTIALLY_PAID') ||
        (selectedFilter === 'Lunas' && d.status === 'PAID');

      return matchSearch && matchFilter;
    });
  }, [debts, searchQuery, selectedFilter]);

  return (
    <div className="flex-1 flex flex-col overflow-hidden bg-[#F7F9FF]">
      {/* Top Header */}
      <div className="p-4 pb-2 bg-white border-b border-slate-200/80 flex-shrink-0 space-y-3">
        <div className="max-w-6xl mx-auto space-y-3">
          <div className="flex items-center gap-3">
            {onNavigateBack && (
              <button
                onClick={onNavigateBack}
                className="w-10 h-10 rounded-full bg-slate-100 hover:bg-slate-200 flex items-center justify-center text-slate-700 transition-all"
              >
                <ArrowLeft className="w-5 h-5 text-brand-primary" />
              </button>
            )}
            <div>
              <h1 className="text-base font-bold text-slate-800 leading-tight">Daftar Kasbon</h1>
              <p className="text-xs text-slate-500 font-medium">Kelola tagihan bayar nanti & pelunasan</p>
            </div>
          </div>

          {/* Stats summary row */}
          <div className="grid grid-cols-2 gap-2.5">
            <SummaryCard
              title="Sisa Kasbon"
              value={formatRupiah(totalUnpaid)}
              subtitle={`${unpaidCount} orang belum lunas`}
              icon={Hourglass}
              iconColorClass="text-amber-600"
              iconBgClass="bg-amber-100/60"
            />
            <SummaryCard
              title="Total Dilunasi"
              value={formatRupiah(totalPaid)}
              subtitle="Sudah diterima"
              icon={CheckCircle2}
              iconColorClass="text-emerald-600"
              iconBgClass="bg-emerald-100/60"
            />
          </div>

          {/* Search & Filters */}
          <SearchField
            query={searchQuery}
            onQueryChange={setSearchQuery}
            placeholder="Cari nama atau no. transaksi..."
          />

          <div className="flex items-center gap-2 overflow-x-auto no-scrollbar py-0.5">
            {filterOptions.map((f) => {
              const isSelected = selectedFilter === f;
              return (
                <button
                  key={f}
                  onClick={() => setSelectedFilter(f)}
                  className={`h-9 px-3.5 rounded-chip text-xs font-medium whitespace-nowrap transition-all border ${
                    isSelected
                      ? 'bg-brand-primary border-brand-primary text-white font-semibold shadow-xs'
                      : 'bg-white border-slate-200 text-slate-600 hover:bg-slate-50'
                  }`}
                >
                  {f}
                </button>
              );
            })}
          </div>
        </div>
      </div>

      {/* Debt Cards List */}
      <div className="flex-1 overflow-y-auto p-4 pb-20 md:p-6">
        <div className="max-w-6xl mx-auto">
          {filteredDebts.length === 0 ? (
            <div className="bg-white rounded-card p-8 text-center border border-slate-200 text-slate-400 text-sm">
              Tidak ada data kasbon yang sesuai.
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-3 space-y-0">
              {filteredDebts.map((debt) => {
            const isPaid = debt.status === 'PAID';
            const isPartiallyPaid = debt.status === 'PARTIALLY_PAID';
            const paidAmount = debt.amount - debt.remainingAmount;
            const progress = debt.amount > 0 ? Math.min(100, Math.round((paidAmount / debt.amount) * 100)) : 0;

            return (
              <div
                key={debt.id}
                data-testid={`debt_card_${debt.id}`}
                className="bg-white rounded-card p-4 border border-slate-200 shadow-soft flex flex-col gap-3"
              >
                {/* Header Row: Person Icon, Name, Status Badge */}
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2.5">
                    <div
                      className={`w-10 h-10 rounded-full flex items-center justify-center flex-shrink-0 ${
                        isPaid
                          ? 'bg-success-container text-emerald-600'
                          : isPartiallyPaid
                          ? 'bg-blue-100 text-brand-primary'
                          : 'bg-warning-container text-amber-600'
                      }`}
                    >
                      <User className="w-5 h-5" />
                    </div>
                    <div>
                      <h3 className="text-sm font-bold text-slate-800">{debt.customerName}</h3>
                      <p className="text-[11px] text-slate-400">
                        {debt.transactionNumber} • {formatDateTime(debt.createdAt)}
                      </p>
                    </div>
                  </div>

                  <span
                    className={`px-2.5 py-1 rounded-chip text-[11px] font-bold ${
                      isPaid
                        ? 'bg-success-container text-success-text'
                        : isPartiallyPaid
                        ? 'bg-blue-100 text-brand-primary'
                        : 'bg-warning-container text-warning-text'
                    }`}
                  >
                    {isPaid ? 'Lunas' : isPartiallyPaid ? 'Dicicil' : 'Belum lunas'}
                  </span>
                </div>

                {/* Optional Phone & Contact Button */}
                {debt.customerPhone && (
                  <div className="flex items-center justify-between pt-1 border-t border-slate-100">
                    <div className="flex items-center gap-1.5 text-xs text-slate-500">
                      <Phone className="w-3.5 h-3.5 text-slate-400" />
                      <span>{debt.customerPhone}</span>
                    </div>
                    <a
                      href={`https://wa.me/${debt.customerPhone.replace(/^0/, '62')}`}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="h-9 px-3 rounded-input border border-slate-200 bg-white hover:bg-slate-50 text-brand-primary text-xs font-semibold flex items-center gap-1 transition-all"
                    >
                      Hubungi
                    </a>
                  </div>
                )}

                {/* Note if any */}
                {debt.note && (
                  <div className="bg-slate-50 p-2 rounded-md text-xs text-slate-600 border border-slate-100">
                    Catatan: {debt.note}
                  </div>
                )}

                {/* Amount details */}
                <div className="flex items-end justify-between pt-1">
                  <div>
                    <span className={`text-[11px] font-medium block ${isPaid ? 'text-emerald-600' : 'text-danger'}`}>
                      {isPaid ? 'Total Tagihan Lunas' : 'Sisa Kasbon'}
                    </span>
                    <span className={`text-lg font-bold ${isPaid ? 'text-emerald-600' : 'text-danger'}`}>
                      {formatRupiah(isPaid ? debt.amount : debt.remainingAmount)}
                    </span>
                  </div>

                  {!isPaid && (
                    <div className="text-right">
                      <span className="text-[11px] text-slate-400 block">Total Awal</span>
                      <span className="text-xs font-medium text-slate-500">{formatRupiah(debt.amount)}</span>
                    </div>
                  )}
                </div>

                {/* Progress bar if partially paid */}
                {isPartiallyPaid && (
                  <div className="space-y-1">
                    <div className="w-full h-1.5 bg-slate-100 rounded-full overflow-hidden">
                      <div style={{ width: `${progress}%` }} className="h-full bg-brand-primary rounded-full transition-all" />
                    </div>
                    <div className="flex justify-between text-[10px] text-slate-400">
                      <span>Sudah dicicil: {formatRupiah(paidAmount)}</span>
                      <span className="font-semibold text-brand-primary">{progress}%</span>
                    </div>
                  </div>
                )}

                {/* Bottom Actions Row (Tinggi 40dp) */}
                <div className="pt-2 border-t border-slate-100 flex items-center justify-between">
                  <div className="flex items-center gap-1">
                    <button
                      onClick={() => onViewTransaction(debt.transactionNumber)}
                      className="w-10 h-10 rounded-input text-slate-400 hover:text-brand-primary hover:bg-slate-100 flex items-center justify-center transition-all"
                      title="Lihat Struk"
                    >
                      <Receipt className="w-4 h-4" />
                    </button>

                    <button
                      onClick={() => setDebtToDelete(debt)}
                      className="w-10 h-10 rounded-input text-slate-400 hover:text-danger hover:bg-red-50 flex items-center justify-center transition-all"
                      title="Hapus Kasbon"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>

                  {!isPaid && (
                    <div className="flex items-center gap-2">
                      <button
                        onClick={() => onEditDebtItems(debt)}
                        data-testid={`button_edit_items_debt_${debt.id}`}
                        className="h-10 px-3 rounded-input border border-slate-200 bg-white hover:bg-slate-50 text-brand-primary text-xs font-semibold flex items-center gap-1 transition-all"
                      >
                        <Edit3 className="w-3.5 h-3.5" />
                        <span>Edit Barang</span>
                      </button>

                      <button
                        onClick={() => onSettleDebt(debt)}
                        data-testid={`button_settle_debt_${debt.id}`}
                        className={`h-10 px-3.5 rounded-input text-white text-xs font-semibold flex items-center gap-1 shadow-xs transition-all ${
                          isPartiallyPaid
                            ? 'bg-brand-primary hover:bg-brand-deep'
                            : 'bg-emerald-600 hover:bg-emerald-700'
                        }`}
                      >
                        <CreditCard className="w-3.5 h-3.5" />
                        <span>{isPartiallyPaid ? 'Cicil' : 'Lunasi'}</span>
                      </button>
                    </div>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  </div>

      {/* Delete Confirmation */}
      {debtToDelete && (
        <ConfirmationDialog
          title={`Hapus Data Kasbon ${debtToDelete.customerName}?`}
          message="Catatan kasbon ini akan dihapus dari daftar. Transaksi kasir asli tetap tersimpan di riwayat."
          confirmText="Hapus Kasbon"
          isDestructive={true}
          onConfirm={() => {
            onDeleteDebt(debtToDelete);
            setDebtToDelete(null);
          }}
          onDismiss={() => setDebtToDelete(null)}
        />
      )}
    </div>
  );
};
