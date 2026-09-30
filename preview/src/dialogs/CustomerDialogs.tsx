import React, { useState } from 'react';
import { X, User, Phone, Edit2, Trash2, CheckCircle2, Clock, Hourglass, ShoppingBag, Plus, Search, ChevronRight } from 'lucide-react';
import { CustomerEntity, CustomerWithStats, DebtEntity, ChangeRecordEntity, TransactionEntity } from '../types';
import { formatRupiah, formatDateTime } from '../utils/format';

// =========================================================================
// 1. CUSTOMER PROFILE DIALOG
// =========================================================================
interface CustomerProfileDialogProps {
  customerWithStats: CustomerWithStats;
  debts: DebtEntity[];
  changeRecords: ChangeRecordEntity[];
  transactions: TransactionEntity[];
  onEditCustomer: (customer: CustomerEntity) => void;
  onDeleteCustomer: (customerId: number) => void;
  onSettleDebt: (debt: DebtEntity) => void;
  onMarkChangeGiven: (changeId: number) => void;
  onSelectTransaction: (tx: TransactionEntity) => void;
  onDismiss: () => void;
}

export const CustomerProfileDialog: React.FC<CustomerProfileDialogProps> = ({
  customerWithStats,
  debts,
  changeRecords,
  transactions,
  onEditCustomer,
  onDeleteCustomer,
  onSettleDebt,
  onMarkChangeGiven,
  onSelectTransaction,
  onDismiss
}) => {
  const { customer, totalPurchases, totalUnpaid, totalPendingChange } = customerWithStats;

  // Filter transactions, debts, and changes for this customer
  const customerDebts = debts.filter((d) => d.customerId === customer.id && d.status !== 'PAID');
  const customerChanges = changeRecords.filter((c) => c.customerId === customer.id && c.status === 'PENDING');
  const customerTransactions = transactions.filter((t) => t.customerId === customer.id);

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-3 sm:p-4 bg-slate-950/60 backdrop-blur-xs animate-fadeIn">
      <div className="bg-white rounded-card w-full max-w-lg md:max-w-xl max-h-[90vh] flex flex-col shadow-dialog border border-slate-200 overflow-hidden">
        {/* Header */}
        <div className="p-4 border-b border-slate-200 flex items-center justify-between bg-slate-50/70 flex-shrink-0">
          <div className="flex items-center gap-3">
            <div className="w-11 h-11 rounded-full bg-brand-primary text-white font-bold text-base flex items-center justify-center shadow-xs">
              {customer.name.slice(0, 1).toUpperCase()}
            </div>
            <div>
              <h2 className="text-base font-bold text-slate-800 leading-snug">{customer.name}</h2>
              {customer.phone ? (
                <p className="text-xs text-slate-500 flex items-center gap-1">
                  <Phone className="w-3 h-3 text-slate-400" />
                  <span>{customer.phone}</span>
                </p>
              ) : (
                <p className="text-xs text-slate-400">Tanpa nomor kontak</p>
              )}
            </div>
          </div>

          <div className="flex items-center gap-1">
            <button
              onClick={() => onEditCustomer(customer)}
              className="w-8 h-8 rounded-full hover:bg-slate-200 text-slate-600 flex items-center justify-center transition-colors"
              title="Edit Pelanggan"
            >
              <Edit2 className="w-4 h-4" />
            </button>
            <button
              onClick={() => onDeleteCustomer(customer.id)}
              className="w-8 h-8 rounded-full hover:bg-red-100 text-red-600 flex items-center justify-center transition-colors"
              title="Hapus Pelanggan"
            >
              <Trash2 className="w-4 h-4" />
            </button>
            <button
              onClick={onDismiss}
              className="w-8 h-8 rounded-full hover:bg-slate-200 text-slate-600 flex items-center justify-center transition-colors ml-1"
            >
              <X className="w-5 h-5" />
            </button>
          </div>
        </div>

        {/* Scrollable Content */}
        <div className="flex-1 overflow-y-auto p-4 space-y-4">
          {/* Notes if any */}
          {customer.notes && (
            <div className="p-2.5 rounded-input bg-blue-50/70 border border-blue-100 text-xs text-slate-700">
              <span className="font-semibold text-brand-primary">Catatan: </span>
              {customer.notes}
            </div>
          )}

          {/* 3 Core Stats Cards */}
          <div className="grid grid-cols-3 gap-2 text-center">
            <div className="bg-slate-50 rounded-card p-3 border border-slate-200">
              <span className="text-[11px] font-medium text-slate-500 block mb-0.5">Pembelian</span>
              <span className="text-base font-bold text-slate-800">{totalPurchases}</span>
              <span className="text-[10px] text-slate-400 block mt-0.5">Transaksi</span>
            </div>

            <div className={`rounded-card p-3 border ${
              totalUnpaid > 0 ? 'bg-red-50 border-red-200 text-red-800' : 'bg-slate-50 border-slate-200 text-slate-800'
            }`}>
              <span className="text-[11px] font-medium text-slate-500 block mb-0.5">Belum Bayar</span>
              <span className={`text-base font-bold ${totalUnpaid > 0 ? 'text-red-700' : 'text-slate-800'}`}>
                {formatRupiah(totalUnpaid)}
              </span>
              <span className="text-[10px] text-slate-400 block mt-0.5">Sisa Kasbon</span>
            </div>

            <div className={`rounded-card p-3 border ${
              totalPendingChange > 0 ? 'bg-amber-50 border-amber-200 text-amber-800' : 'bg-slate-50 border-slate-200 text-slate-800'
            }`}>
              <span className="text-[11px] font-medium text-slate-500 block mb-0.5">Kembalian</span>
              <span className={`text-base font-bold ${totalPendingChange > 0 ? 'text-amber-800' : 'text-slate-800'}`}>
                {formatRupiah(totalPendingChange)}
              </span>
              <span className="text-[10px] text-slate-400 block mt-0.5">Belum Diberikan</span>
            </div>
          </div>

          {/* Outstanding Debts Section */}
          {customerDebts.length > 0 && (
            <div className="space-y-2">
              <h3 className="text-xs font-bold text-slate-700 flex items-center gap-1.5">
                <Hourglass className="w-3.5 h-3.5 text-red-600" />
                <span>Kasbon yang Belum Lunas ({customerDebts.length})</span>
              </h3>
              <div className="space-y-1.5">
                {customerDebts.map((debt) => (
                  <div
                    key={debt.id}
                    className="p-3 bg-red-50/50 rounded-input border border-red-200 flex items-center justify-between text-xs"
                  >
                    <div>
                      <div className="font-semibold text-slate-800">{formatRupiah(debt.remainingAmount)}</div>
                      <div className="text-[11px] text-slate-500">{debt.note || 'Transaksi ' + debt.transactionNumber}</div>
                    </div>
                    <button
                      onClick={() => onSettleDebt(debt)}
                      className="px-3 py-1.5 bg-red-600 hover:bg-red-700 text-white font-semibold rounded-chip text-xs shadow-xs active:scale-95 transition-all"
                    >
                      Lunasi
                    </button>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Outstanding Change Records Section */}
          {customerChanges.length > 0 && (
            <div className="space-y-2">
              <h3 className="text-xs font-bold text-slate-700 flex items-center gap-1.5">
                <Clock className="w-3.5 h-3.5 text-amber-600" />
                <span>Kembalian Belum Diserahkan ({customerChanges.length})</span>
              </h3>
              <div className="space-y-1.5">
                {customerChanges.map((change) => (
                  <div
                    key={change.id}
                    className="p-3 bg-amber-50/50 rounded-input border border-amber-200 flex items-center justify-between text-xs"
                  >
                    <div>
                      <div className="font-semibold text-amber-900">{formatRupiah(change.amount)}</div>
                      <div className="text-[11px] text-slate-500">{change.note || 'Kembalian ' + change.transactionNumber}</div>
                    </div>
                    <button
                      onClick={() => onMarkChangeGiven(change.id)}
                      className="px-3 py-1.5 bg-amber-600 hover:bg-amber-700 text-white font-semibold rounded-chip text-xs shadow-xs active:scale-95 transition-all"
                    >
                      Tandai Sudah Diberikan
                    </button>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Recent Purchases Section */}
          <div className="space-y-2">
            <h3 className="text-xs font-bold text-slate-700 flex items-center gap-1.5">
              <ShoppingBag className="w-3.5 h-3.5 text-brand-primary" />
              <span>Riwayat Transaksi Pelanggan</span>
            </h3>
            {customerTransactions.length === 0 ? (
              <p className="text-xs text-slate-400 bg-slate-50 p-4 rounded-input text-center">
                Belum ada transaksi tersimpan untuk pelanggan ini.
              </p>
            ) : (
              <div className="space-y-1.5">
                {customerTransactions.slice(0, 5).map((tx) => (
                  <div
                    key={tx.id}
                    onClick={() => onSelectTransaction(tx)}
                    className="p-2.5 bg-white rounded-input border border-slate-200 hover:border-brand-sky flex items-center justify-between cursor-pointer transition-all text-xs"
                  >
                    <div>
                      <div className="font-semibold text-slate-800">{tx.transactionNumber}</div>
                      <div className="text-[10px] text-slate-400">
                        {formatDateTime(tx.createdAt)} • {tx.paymentMethod}
                      </div>
                    </div>
                    <div className="font-bold text-brand-primary flex items-center gap-1">
                      <span>{formatRupiah(tx.total)}</span>
                      <ChevronRight className="w-3.5 h-3.5 text-slate-400" />
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* Footer */}
        <div className="p-3 border-t border-slate-200 bg-slate-50 flex justify-end">
          <button
            onClick={onDismiss}
            className="px-4 py-2 bg-slate-200 hover:bg-slate-300 text-slate-700 font-semibold rounded-input text-xs transition-colors"
          >
            Tutup
          </button>
        </div>
      </div>
    </div>
  );
};

// =========================================================================
// 2. ADD / EDIT CUSTOMER DIALOG
// =========================================================================
interface AddEditCustomerDialogProps {
  initialCustomer?: CustomerEntity | null;
  customerToEdit?: CustomerEntity | null;
  onSave?: (name: string, phone: string, notes: string) => void;
  onSaveCustomer?: (customer: CustomerEntity) => void;
  onDismiss: () => void;
}

export const AddEditCustomerDialog: React.FC<AddEditCustomerDialogProps> = ({
  initialCustomer,
  customerToEdit,
  onSave,
  onSaveCustomer,
  onDismiss
}) => {
  const currentCustomer = initialCustomer || customerToEdit;
  const [name, setName] = useState(currentCustomer?.name || '');
  const [phone, setPhone] = useState(currentCustomer?.phone || '');
  const [notes, setNotes] = useState(currentCustomer?.notes || '');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) return;
    if (onSave) {
      onSave(name.trim(), phone.trim(), notes.trim());
    }
    if (onSaveCustomer) {
      const savedCust: CustomerEntity = {
        id: currentCustomer?.id || Date.now(),
        name: name.trim(),
        phone: phone.trim(),
        notes: notes.trim(),
        createdAt: currentCustomer?.createdAt || Date.now(),
        updatedAt: Date.now()
      };
      onSaveCustomer(savedCust);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/60 backdrop-blur-xs animate-fadeIn">
      <div className="bg-white rounded-card w-full max-w-md shadow-dialog border border-slate-200 overflow-hidden">
        <div className="p-4 border-b border-slate-200 flex items-center justify-between bg-slate-50/70">
          <h2 className="text-sm font-bold text-slate-800">
            {currentCustomer ? 'Edit Data Pelanggan' : 'Tambah Pelanggan Baru'}
          </h2>
          <button
            onClick={onDismiss}
            className="w-8 h-8 rounded-full hover:bg-slate-200 text-slate-600 flex items-center justify-center transition-colors"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-4 space-y-3.5">
          <div>
            <label className="text-xs font-semibold text-slate-700 block mb-1">
              Nama Pelanggan (Wajib) *
            </label>
            <input
              type="text"
              required
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="Misal: Ahmad / Bu Siti"
              className="w-full h-10 px-3 rounded-input bg-slate-50 border border-slate-200 text-xs text-slate-800 focus:outline-none focus:ring-2 focus:ring-brand-primary/20 focus:border-brand-primary"
            />
          </div>

          <div>
            <label className="text-xs font-semibold text-slate-700 block mb-1">
              Nomor Telepon / Kontak (Opsional)
            </label>
            <input
              type="tel"
              value={phone}
              onChange={(e) => setPhone(e.target.value)}
              placeholder="Misal: 08123456789"
              className="w-full h-10 px-3 rounded-input bg-slate-50 border border-slate-200 text-xs text-slate-800 focus:outline-none focus:ring-2 focus:ring-brand-primary/20 focus:border-brand-primary"
            />
          </div>

          <div>
            <label className="text-xs font-semibold text-slate-700 block mb-1">
              Catatan Pelanggan (Opsional)
            </label>
            <textarea
              rows={2}
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              placeholder="Misal: Pelanggan setia kopi susu / mahasiswa prodi SI"
              className="w-full p-2.5 rounded-input bg-slate-50 border border-slate-200 text-xs text-slate-800 focus:outline-none focus:ring-2 focus:ring-brand-primary/20 focus:border-brand-primary resize-none"
            />
          </div>

          <div className="pt-2 flex items-center justify-end gap-2">
            <button
              type="button"
              onClick={onDismiss}
              className="px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 font-semibold rounded-input text-xs transition-colors"
            >
              Batal
            </button>
            <button
              type="submit"
              disabled={!name.trim()}
              className="px-4 py-2 bg-brand-primary hover:bg-brand-deep disabled:bg-slate-200 text-white disabled:text-slate-400 font-semibold rounded-input text-xs transition-colors"
            >
              Simpan Pelanggan
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

// =========================================================================
// 3. CUSTOMER PICKER DIALOG (FOR CASHIER PAYMENT)
// =========================================================================
interface CustomerPickerDialogProps {
  customers: CustomerEntity[];
  selectedCustomerId?: number | null;
  onSelectCustomer: (customer: CustomerEntity | null) => void;
  onAddNewCustomer: () => void;
  onDismiss: () => void;
}

export const CustomerPickerDialog: React.FC<CustomerPickerDialogProps> = ({
  customers,
  selectedCustomerId,
  onSelectCustomer,
  onAddNewCustomer,
  onDismiss
}) => {
  const [search, setSearch] = useState('');

  const filtered = customers.filter(
    (c) =>
      c.name.toLowerCase().includes(search.toLowerCase()) ||
      c.phone.includes(search)
  );

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/60 backdrop-blur-xs animate-fadeIn">
      <div className="bg-white rounded-card w-full max-w-md max-h-[80vh] flex flex-col shadow-dialog border border-slate-200 overflow-hidden">
        {/* Header */}
        <div className="p-4 border-b border-slate-200 flex items-center justify-between bg-slate-50/70">
          <div>
            <h2 className="text-sm font-bold text-slate-800">Pilih Pelanggan</h2>
            <p className="text-[11px] text-slate-500">Hubungkan transaksi dengan data pelanggan</p>
          </div>
          <button
            onClick={onDismiss}
            className="w-8 h-8 rounded-full hover:bg-slate-200 text-slate-600 flex items-center justify-center transition-colors"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Search & Add New CTA */}
        <div className="p-3 border-b border-slate-100 space-y-2">
          <div className="relative">
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2 pointer-events-none" />
            <input
              type="text"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Cari pelanggan..."
              className="w-full h-9 pl-9 pr-3 rounded-input bg-slate-50 border border-slate-200 text-xs text-slate-800 focus:outline-none focus:ring-1 focus:ring-brand-primary"
            />
          </div>

          <button
            onClick={() => {
              onDismiss();
              onAddNewCustomer();
            }}
            className="w-full h-9 rounded-input border border-dashed border-brand-primary/60 text-brand-primary hover:bg-brand-primary/5 text-xs font-semibold flex items-center justify-center gap-1.5 transition-colors"
          >
            <Plus className="w-3.5 h-3.5" />
            <span>+ Tambah Pelanggan Baru</span>
          </button>
        </div>

        {/* Customer List */}
        <div className="flex-1 overflow-y-auto p-2 space-y-1">
          {/* Option: Tanpa Pelanggan */}
          <button
            onClick={() => {
              onSelectCustomer(null);
              onDismiss();
            }}
            className="w-full p-2.5 rounded-input text-left text-xs font-medium text-slate-600 hover:bg-slate-100 flex items-center justify-between transition-colors"
          >
            <span>Tanpa Pelanggan (Umum)</span>
            {!selectedCustomerId && <span className="text-brand-primary font-bold">✓</span>}
          </button>

          {filtered.map((cust) => {
            const isSelected = selectedCustomerId === cust.id;
            return (
              <button
                key={cust.id}
                onClick={() => {
                  onSelectCustomer(cust);
                  onDismiss();
                }}
                className={`w-full p-2.5 rounded-input text-left flex items-center justify-between transition-colors ${
                  isSelected ? 'bg-blue-50/80 border border-brand-sky/60' : 'hover:bg-slate-50'
                }`}
              >
                <div className="flex items-center gap-2.5">
                  <div className="w-7 h-7 rounded-full bg-brand-primary/10 text-brand-primary text-xs font-bold flex items-center justify-center">
                    {cust.name.slice(0, 1).toUpperCase()}
                  </div>
                  <div>
                    <div className="text-xs font-semibold text-slate-800">{cust.name}</div>
                    {cust.phone && <div className="text-[10px] text-slate-400">{cust.phone}</div>}
                  </div>
                </div>
                {isSelected && <span className="text-brand-primary font-bold text-xs">✓</span>}
              </button>
            );
          })}
        </div>
      </div>
    </div>
  );
};
