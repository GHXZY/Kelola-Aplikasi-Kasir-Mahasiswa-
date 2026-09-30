import React, { useState, useMemo } from 'react';
import { ArrowLeft, Search, Plus, User, Phone, ChevronRight, ShoppingBag, AlertCircle, Coins, Clock } from 'lucide-react';
import { CustomerWithStats } from '../types';
import { formatRupiah } from '../utils/format';

interface CustomerListScreenProps {
  customersWithStats: CustomerWithStats[];
  onOpenAddCustomer?: () => void;
  onAddNewCustomer?: () => void;
  onSelectCustomer: (customerWithStats: CustomerWithStats) => void;
  onNavigateBack: () => void;
}

export const CustomerListScreen: React.FC<CustomerListScreenProps> = ({
  customersWithStats,
  onOpenAddCustomer,
  onAddNewCustomer,
  onSelectCustomer,
  onNavigateBack
}) => {
  const handleAddCustomerClick = onOpenAddCustomer || onAddNewCustomer;
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedFilter, setSelectedFilter] = useState<'ALL' | 'HAS_DEBT' | 'HAS_CHANGE' | 'NAME_AZ'>('ALL');

  const filteredCustomers = useMemo(() => {
    let result = [...customersWithStats];

    // Filter status
    if (selectedFilter === 'HAS_DEBT') {
      result = result.filter((c) => c.totalUnpaid > 0);
    } else if (selectedFilter === 'HAS_CHANGE') {
      result = result.filter((c) => c.totalPendingChange > 0);
    }

    // Search query
    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase();
      result = result.filter(
        (c) =>
          c.customer.name.toLowerCase().includes(q) ||
          c.customer.phone.includes(q) ||
          c.customer.notes.toLowerCase().includes(q)
      );
    }

    // Sort
    if (selectedFilter === 'NAME_AZ') {
      result.sort((a, b) => a.customer.name.localeCompare(b.customer.name));
    }

    return result;
  }, [customersWithStats, searchQuery, selectedFilter]);

  return (
    <div className="flex-1 flex flex-col overflow-hidden bg-[#F7F9FF]">
      {/* Top Header */}
      <div className="p-4 pb-3 bg-white border-b border-slate-200/80 flex-shrink-0 space-y-3">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <button
              onClick={onNavigateBack}
              data-testid="button_back_from_customers"
              className="w-10 h-10 rounded-full bg-slate-100 hover:bg-slate-200 flex items-center justify-center text-slate-700 transition-all"
            >
              <ArrowLeft className="w-5 h-5 text-brand-primary" />
            </button>
            <div>
              <h1 className="text-base font-bold text-slate-800 leading-tight">List Pelanggan</h1>
              <p className="text-xs text-slate-500 font-medium">
                {customersWithStats.length} pelanggan terdaftar
              </p>
            </div>
          </div>

          <button
            onClick={handleAddCustomerClick}
            data-testid="button_add_customer"
            className="h-9 px-3.5 rounded-input bg-brand-primary hover:bg-brand-deep text-white text-xs font-semibold flex items-center gap-1.5 shadow-sm active:scale-95 transition-all"
          >
            <Plus className="w-4 h-4" />
            <span>Tambah</span>
          </button>
        </div>

        {/* Search Input */}
        <div className="relative">
          <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2 pointer-events-none" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Cari nama atau nomor HP pelanggan..."
            className="w-full h-10 pl-10 pr-4 rounded-input bg-slate-50 border border-slate-200 text-xs text-slate-800 focus:outline-none focus:ring-2 focus:ring-brand-primary/20 focus:border-brand-primary transition-all"
          />
        </div>

        {/* Filter Chips */}
        <div className="flex items-center gap-1.5 overflow-x-auto no-scrollbar pb-0.5">
          {[
            { id: 'ALL', label: 'Semua' },
            { id: 'HAS_DEBT', label: 'Ada Kasbon' },
            { id: 'HAS_CHANGE', label: 'Ada Kembalian' },
            { id: 'NAME_AZ', label: 'Nama A-Z' }
          ].map((chip) => {
            const isSelected = selectedFilter === chip.id;
            return (
              <button
                key={chip.id}
                onClick={() => setSelectedFilter(chip.id as any)}
                className={`px-3 py-1.5 rounded-chip text-xs font-medium whitespace-nowrap transition-all border ${
                  isSelected
                    ? 'bg-brand-primary text-white border-brand-primary shadow-xs font-semibold'
                    : 'bg-white text-slate-600 border-slate-200 hover:bg-slate-50'
                }`}
              >
                {chip.label}
              </button>
            );
          })}
        </div>
      </div>

      {/* Main Content Area: Responsive Grid (1 col on mobile, 2-3 cols on tablet) */}
      <div className="flex-1 overflow-y-auto p-4 pb-20">
        <div className="max-w-6xl mx-auto">
          {filteredCustomers.length === 0 ? (
            <div className="bg-white rounded-card p-8 text-center border border-slate-200 text-slate-400 text-sm mt-4">
              <User className="w-10 h-10 mx-auto mb-2 text-slate-300 stroke-1" />
              <p className="font-semibold text-slate-600">Tidak ada pelanggan yang cocok</p>
              <p className="text-xs text-slate-400 mt-1">
                Coba ubah kata kunci pencarian atau filter yang dipilih.
              </p>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
              {filteredCustomers.map((item) => {
                const { customer, totalPurchases, totalUnpaid, totalPendingChange } = item;
                const initial = customer.name.slice(0, 1).toUpperCase();

                return (
                  <div
                    key={customer.id}
                    onClick={() => onSelectCustomer(item)}
                    data-testid={`customer_card_${customer.id}`}
                    className="bg-white rounded-card p-3.5 border border-slate-200 shadow-soft hover:border-brand-sky hover:shadow-md cursor-pointer transition-all flex flex-col justify-between group active:scale-[0.99]"
                  >
                    <div>
                      {/* Top Row: Avatar & Name */}
                      <div className="flex items-start justify-between gap-2 mb-2">
                        <div className="flex items-center gap-2.5">
                          <div className="w-10 h-10 rounded-full bg-brand-primary/10 text-brand-primary font-bold text-sm flex items-center justify-center flex-shrink-0 group-hover:bg-brand-primary group-hover:text-white transition-colors">
                            {initial}
                          </div>
                          <div>
                            <h3 className="text-sm font-semibold text-slate-800 leading-snug line-clamp-1">
                              {customer.name}
                            </h3>
                            {customer.phone ? (
                              <p className="text-[11px] text-slate-500 flex items-center gap-1 mt-0.5">
                                <Phone className="w-3 h-3 text-slate-400" />
                                <span>{customer.phone}</span>
                              </p>
                            ) : (
                              <p className="text-[11px] text-slate-400 mt-0.5">Tanpa nomor kontak</p>
                            )}
                          </div>
                        </div>

                        <div className="w-7 h-7 rounded-full bg-slate-50 flex items-center justify-center text-slate-400 group-hover:text-brand-primary group-hover:bg-brand-primary/10 transition-colors flex-shrink-0">
                          <ChevronRight className="w-4 h-4" />
                        </div>
                      </div>

                      {/* Notes snippet if exists */}
                      {customer.notes && (
                        <p className="text-[11px] text-slate-500 bg-slate-50 rounded-md px-2 py-1 mb-2 line-clamp-1 italic">
                          "{customer.notes}"
                        </p>
                      )}
                    </div>

                    {/* Bottom Row: Stats Pills */}
                    <div className="pt-2 border-t border-slate-100 flex flex-wrap items-center gap-1.5 text-[11px]">
                      <span className="px-2 py-0.5 rounded-full bg-slate-100 text-slate-600 font-medium">
                        {totalPurchases} Pembelian
                      </span>

                      {totalUnpaid > 0 ? (
                        <span className="px-2 py-0.5 rounded-full bg-red-100 text-red-700 font-semibold">
                          Kasbon: {formatRupiah(totalUnpaid)}
                        </span>
                      ) : (
                        <span className="px-2 py-0.5 rounded-full bg-emerald-50 text-emerald-600 font-medium">
                          Lunas
                        </span>
                      )}

                      {totalPendingChange > 0 && (
                        <span className="px-2 py-0.5 rounded-full bg-amber-100 text-amber-800 font-semibold">
                          Kembalian: {formatRupiah(totalPendingChange)}
                        </span>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
