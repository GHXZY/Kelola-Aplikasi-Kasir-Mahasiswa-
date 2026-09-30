import React, { useState, useMemo } from 'react';
import {
  ArrowLeft,
  Clock,
  CheckCircle2,
  AlertCircle,
  Receipt,
  Search,
  Check,
  Coins,
  Filter
} from 'lucide-react';
import { ChangeRecordEntity } from '../types';
import { formatRupiah, formatDateTime } from '../utils/format';
import { ConfirmationDialog } from '../components/CommonComponents';

interface PendingChangesScreenProps {
  changeRecords: ChangeRecordEntity[];
  onMarkChangeGiven: (changeId: number) => void;
  onNavigateBack: () => void;
}

export const PendingChangesScreen: React.FC<PendingChangesScreenProps> = ({
  changeRecords,
  onMarkChangeGiven,
  onNavigateBack
}) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedFilter, setSelectedFilter] = useState<'PENDING' | 'ALL' | 'PAID'>('PENDING');
  const [changeToGive, setChangeToGive] = useState<ChangeRecordEntity | null>(null);

  // Stats
  const pendingRecords = useMemo(() => changeRecords.filter((c) => c.status === 'PENDING'), [changeRecords]);
  const paidRecords = useMemo(() => changeRecords.filter((c) => c.status === 'PAID'), [changeRecords]);

  const totalPendingAmount = useMemo(
    () => pendingRecords.reduce((sum, c) => sum + c.amount, 0),
    [pendingRecords]
  );
  const totalPaidAmount = useMemo(
    () => paidRecords.reduce((sum, c) => sum + c.amount, 0),
    [paidRecords]
  );

  // Filtered List
  const filteredRecords = useMemo(() => {
    return changeRecords.filter((record) => {
      // Filter status
      if (selectedFilter === 'PENDING' && record.status !== 'PENDING') return false;
      if (selectedFilter === 'PAID' && record.status !== 'PAID') return false;

      // Search
      if (searchQuery.trim()) {
        const query = searchQuery.toLowerCase();
        const matchName = record.buyerName.toLowerCase().includes(query);
        const matchTx = record.transactionNumber.toLowerCase().includes(query);
        const matchNote = (record.note || '').toLowerCase().includes(query);
        if (!matchName && !matchTx && !matchNote) return false;
      }

      return true;
    });
  }, [changeRecords, selectedFilter, searchQuery]);

  return (
    <div className="flex-1 flex flex-col overflow-hidden bg-[#F7F9FF]">
      {/* Top Bar */}
      <div className="p-4 bg-white border-b border-slate-200/80 flex-shrink-0">
        <div className="max-w-6xl mx-auto flex items-center justify-between">
          <div className="flex items-center gap-3">
            <button
              onClick={onNavigateBack}
              data-testid="button_back_from_pending_changes"
              className="w-10 h-10 rounded-full bg-slate-100 hover:bg-slate-200 flex items-center justify-center text-slate-700 transition-all"
            >
              <ArrowLeft className="w-5 h-5 text-brand-primary" />
            </button>
            <div>
              <h1 className="text-base font-bold text-slate-800">Kembalian Belum Diberikan</h1>
              <p className="text-xs text-slate-500">Kelola pengembalian uang pelanggan kasir</p>
            </div>
          </div>

          <div className="w-9 h-9 rounded-full bg-amber-100/70 flex items-center justify-center text-amber-600">
            <Coins className="w-5 h-5" />
          </div>
        </div>
      </div>

      {/* Main Body */}
      <div className="flex-1 overflow-y-auto p-4 pb-20 md:p-6">
        <div className="max-w-6xl mx-auto space-y-4">
          {/* Stats Summary Cards */}
          <div className="grid grid-cols-2 gap-3">
          <div className="bg-white rounded-card p-3.5 border border-amber-200 shadow-soft bg-gradient-to-br from-amber-50/50 to-white">
            <div className="flex items-center justify-between text-amber-700 mb-1">
              <span className="text-xs font-semibold">Total Tertunda</span>
              <Clock className="w-4 h-4 text-amber-600" />
            </div>
            <div className="text-lg font-extrabold text-amber-800">
              {formatRupiah(totalPendingAmount)}
            </div>
            <div className="text-[11px] text-amber-600 font-medium mt-0.5">
              {pendingRecords.length} transaksi belum diserahkan
            </div>
          </div>

          <div className="bg-white rounded-card p-3.5 border border-slate-200 shadow-soft">
            <div className="flex items-center justify-between text-slate-500 mb-1">
              <span className="text-xs font-semibold">Sudah Diberikan</span>
              <CheckCircle2 className="w-4 h-4 text-emerald-600" />
            </div>
            <div className="text-lg font-bold text-slate-800">
              {formatRupiah(totalPaidAmount)}
            </div>
            <div className="text-[11px] text-slate-400 font-medium mt-0.5">
              {paidRecords.length} transaksi diselesaikan
            </div>
          </div>
        </div>

        {/* Search & Filter Bar */}
        <div className="space-y-2">
          {/* Search Box */}
          <div className="relative">
            <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2 pointer-events-none" />
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Cari nama pembeli, struk, atau catatan..."
              className="w-full h-10 pl-9.5 pr-3.5 rounded-input bg-white border border-slate-200 text-xs text-slate-800 focus:outline-none focus:ring-1 focus:ring-brand-primary placeholder:text-slate-400 shadow-2xs"
            />
          </div>

          {/* Filter Chips */}
          <div className="flex items-center gap-1.5 overflow-x-auto pb-1">
            {[
              { id: 'PENDING', label: `Tertunda (${pendingRecords.length})` },
              { id: 'ALL', label: `Semua (${changeRecords.length})` },
              { id: 'PAID', label: `Selesai (${paidRecords.length})` }
            ].map((tab) => (
              <button
                key={tab.id}
                onClick={() => setSelectedFilter(tab.id as any)}
                className={`h-8 px-3 rounded-full text-xs font-semibold whitespace-nowrap transition-all ${
                  selectedFilter === tab.id
                    ? 'bg-brand-primary text-white shadow-2xs'
                    : 'bg-white border border-slate-200 text-slate-600 hover:bg-slate-50'
                }`}
              >
                {tab.label}
              </button>
            ))}
          </div>
        </div>

        {/* Change Records List */}
        {filteredRecords.length === 0 ? (
          <div className="bg-white rounded-card p-8 border border-slate-200 shadow-soft text-center space-y-2">
            <div className="w-12 h-12 rounded-full bg-emerald-50 text-emerald-600 flex items-center justify-center mx-auto mb-2">
              <CheckCircle2 className="w-6 h-6" />
            </div>
            <div className="text-sm font-bold text-slate-800">
              {selectedFilter === 'PENDING'
                ? 'Tidak Ada Kembalian Tertunda 🎉'
                : 'Tidak Ada Data Kembalian'}
            </div>
            <p className="text-xs text-slate-500 max-w-xs mx-auto">
              {selectedFilter === 'PENDING'
                ? 'Semua uang kembalian pelanggan sudah diserahkan dengan baik.'
                : 'Tidak ditemukan transaksi kembalian yang cocok dengan pencarian.'}
            </p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-3 space-y-0">
            {filteredRecords.map((item) => {
              const isPending = item.status === 'PENDING';
              return (
                <div
                  key={item.id}
                  className={`bg-white rounded-card p-4 border shadow-soft transition-all ${
                    isPending
                      ? 'border-amber-200 hover:border-amber-300'
                      : 'border-slate-200 opacity-90'
                  }`}
                >
                  <div className="flex items-start justify-between gap-2">
                    <div className="space-y-1">
                      <div className="flex items-center gap-2">
                        <span className="text-sm font-bold text-slate-800">{item.buyerName}</span>
                        <span
                          className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                            isPending
                              ? 'bg-amber-100 text-amber-800'
                              : 'bg-emerald-100 text-emerald-800'
                          }`}
                        >
                          {isPending ? 'Belum Diberikan' : 'Sudah Diberikan'}
                        </span>
                      </div>

                      <div className="text-lg font-extrabold text-amber-800">
                        {formatRupiah(item.amount)}
                      </div>

                      <div className="flex items-center gap-1.5 text-[11px] text-slate-400">
                        <Receipt className="w-3 h-3 text-slate-400" />
                        <span>{item.transactionNumber}</span>
                        <span>•</span>
                        <span>{formatDateTime(item.createdAt)}</span>
                      </div>

                      {item.note && (
                        <div className="text-xs text-slate-600 bg-slate-50 rounded p-1.5 mt-1 border border-slate-100">
                          Catatan: {item.note}
                        </div>
                      )}

                      {!isPending && item.paidAt && (
                        <div className="text-[10px] text-emerald-600 font-medium">
                          ✓ Diberikan pada {formatDateTime(item.paidAt)}
                        </div>
                      )}
                    </div>

                    {isPending && (
                      <button
                        onClick={() => setChangeToGive(item)}
                        data-testid={`button_give_change_${item.id}`}
                        className="h-9 px-3 rounded-input bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-semibold flex items-center gap-1.5 shadow-xs transition-all flex-shrink-0"
                      >
                        <Check className="w-4 h-4" />
                        <span>Berikan</span>
                      </button>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        )}
        </div>
      </div>

      {/* Confirmation Dialog */}
      {changeToGive && (
        <ConfirmationDialog
          title="Konfirmasi Berikan Kembalian"
          message={`Apakah uang kembalian sebesar ${formatRupiah(
            changeToGive.amount
          )} sudah diserahkan ke pembeli "${changeToGive.buyerName}"?`}
          confirmText="Ya, Sudah Diberikan"
          isDestructive={false}
          onConfirm={() => {
            onMarkChangeGiven(changeToGive.id);
            setChangeToGive(null);
          }}
          onDismiss={() => setChangeToGive(null)}
        />
      )}
    </div>
  );
};
