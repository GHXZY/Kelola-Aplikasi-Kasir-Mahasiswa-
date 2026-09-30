import React, { useState } from 'react';
import {
  ArrowLeft,
  Wallet,
  QrCode,
  Hourglass,
  Check,
  AlertCircle,
  Phone,
  User,
  Users,
  ChevronRight,
  ShoppingBag,
  X
} from 'lucide-react';
import { CartSummary, BusinessSettings, CustomerEntity } from '../types';
import { formatRupiah } from '../utils/format';
import { CustomerPickerDialog, AddEditCustomerDialog } from '../dialogs/CustomerDialogs';

interface PaymentScreenProps {
  cart: CartSummary;
  settings?: BusinessSettings;
  customers?: CustomerEntity[];
  onCompletePayment: (
    paymentMethod: string,
    cashReceived: number,
    debtCustomerName?: string,
    debtPhone?: string,
    changePending?: { buyerName: string; note: string },
    customerId?: number | null
  ) => void;
  onNavigateBack: () => void;
  onSaveNewCustomer?: (customer: CustomerEntity) => void;
}

export const PaymentScreen: React.FC<PaymentScreenProps> = ({
  cart,
  settings,
  customers = [],
  onCompletePayment,
  onNavigateBack,
  onSaveNewCustomer
}) => {
  const [method, setMethod] = useState<'Tunai' | 'QRIS' | 'Transfer' | 'E-Wallet' | 'Bayar Nanti'>(
    settings?.defaultPaymentMethod === 'QRIS' ? 'QRIS' : 'Tunai'
  );
  const [cashReceivedInput, setCashReceivedInput] = useState(cart.total.toString());
  const [customerName, setCustomerName] = useState('');
  const [customerPhone, setCustomerPhone] = useState('');

  // Customer Linking
  const [selectedCustomer, setSelectedCustomer] = useState<CustomerEntity | null>(null);
  const [showCustomerPicker, setShowCustomerPicker] = useState(false);
  const [showAddCustomer, setShowAddCustomer] = useState(false);

  // Pending change states
  const [isChangePending, setIsChangePending] = useState(false);
  const [buyerNameForChange, setBuyerNameForChange] = useState('');
  const [changeNote, setChangeNote] = useState('');

  const cashReceived = Number(cashReceivedInput) || 0;
  const change = Math.max(0, cashReceived - cart.total);
  const isCashInsufficient = method === 'Tunai' && cashReceived < cart.total;

  const quickPresets = [
    { label: 'Uang Pas', amount: cart.total },
    { label: '20k', amount: 20000 },
    { label: '50k', amount: 50000 },
    { label: '100k', amount: 100000 }
  ].filter((p) => p.amount >= cart.total || p.label === 'Uang Pas');

  const paymentMethods = [
    { id: 'Tunai', icon: Wallet, label: 'Tunai' },
    { id: 'QRIS', icon: QrCode, label: 'QRIS' },
    { id: 'Transfer', icon: Wallet, label: 'Transfer' },
    { id: 'E-Wallet', icon: Wallet, label: 'E-Wallet' },
    { id: 'Bayar Nanti', icon: Hourglass, label: 'Bayar Nanti' }
  ];

  const handleSelectCustomer = (cust: CustomerEntity | null) => {
    setSelectedCustomer(cust);
    if (cust) {
      if (method === 'Bayar Nanti') {
        setCustomerName(cust.name);
        if (cust.phone) setCustomerPhone(cust.phone);
      }
      if (isChangePending) {
        setBuyerNameForChange(cust.name);
      }
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (method === 'Tunai') {
      if (isCashInsufficient) return;
      if (isChangePending && !buyerNameForChange.trim() && !selectedCustomer) return;
    }
    if (method === 'Bayar Nanti' && !customerName.trim() && !selectedCustomer) return;

    const finalDebtName = customerName.trim() || selectedCustomer?.name;
    const finalDebtPhone = customerPhone.trim() || selectedCustomer?.phone;
    const finalBuyerName = buyerNameForChange.trim() || selectedCustomer?.name;

    onCompletePayment(
      method,
      method === 'Tunai' ? cashReceived : cart.total,
      method === 'Bayar Nanti' ? finalDebtName : undefined,
      method === 'Bayar Nanti' ? finalDebtPhone : undefined,
      method === 'Tunai' && isChangePending && change > 0 && finalBuyerName
        ? { buyerName: finalBuyerName, note: changeNote.trim() }
        : undefined,
      selectedCustomer ? selectedCustomer.id : null
    );
  };

  const isSubmitDisabled =
    (method === 'Tunai' &&
      (isCashInsufficient || (isChangePending && !buyerNameForChange.trim() && !selectedCustomer))) ||
    (method === 'Bayar Nanti' && !customerName.trim() && !selectedCustomer);

  return (
    <div className="flex flex-col h-full bg-slate-50">
      {/* Top Bar */}
      <div className="px-4 py-3 bg-white border-b border-slate-200 flex-shrink-0">
        <div className="max-w-5xl mx-auto flex items-center justify-between">
          <div className="flex items-center gap-3">
            <button
              onClick={onNavigateBack}
              className="w-9 h-9 rounded-full bg-slate-100 flex items-center justify-center text-slate-600 hover:bg-slate-200 transition-colors"
            >
              <ArrowLeft className="w-5 h-5" />
            </button>
            <div>
              <h1 className="text-base font-bold text-slate-800">Pembayaran</h1>
              <p className="text-[11px] text-slate-400">Pilih metode dan selesaikan transaksi</p>
            </div>
          </div>
        </div>
      </div>

      {/* Scrollable Body: Responsive 2-column on tablet landscape (lg:) */}
      <div className="flex-1 overflow-y-auto p-4 pb-28 lg:pb-8">
        <div className="max-w-5xl mx-auto">
          <form onSubmit={handleSubmit} className="grid grid-cols-1 lg:grid-cols-12 gap-5">
            {/* Left Column: Customer Selector & Payment Methods */}
            <div className="lg:col-span-7 space-y-4">
              {/* Optional Customer Picker Section */}
              <div className="bg-white rounded-card p-3.5 border border-slate-200 shadow-soft">
                <div className="flex items-center justify-between mb-2">
                  <div className="flex items-center gap-1.5 text-xs font-semibold text-slate-700">
                    <User className="w-3.5 h-3.5 text-brand-primary" />
                    <span>Pelanggan (Opsional)</span>
                  </div>
                  {selectedCustomer && (
                    <button
                      type="button"
                      onClick={() => handleSelectCustomer(null)}
                      className="text-[11px] font-medium text-danger hover:underline flex items-center gap-0.5"
                    >
                      <X className="w-3 h-3" />
                      <span>Lepas</span>
                    </button>
                  )}
                </div>

                {selectedCustomer ? (
                  <div className="flex items-center justify-between p-2.5 rounded-input bg-blue-50/70 border border-brand-sky/40">
                    <div className="flex items-center gap-2.5">
                      <div className="w-8 h-8 rounded-full bg-brand-primary text-white text-xs font-bold flex items-center justify-center">
                        {selectedCustomer.name.slice(0, 1).toUpperCase()}
                      </div>
                      <div>
                        <div className="text-xs font-bold text-slate-800">{selectedCustomer.name}</div>
                        {selectedCustomer.phone && (
                          <div className="text-[11px] text-slate-500">{selectedCustomer.phone}</div>
                        )}
                      </div>
                    </div>

                    <button
                      type="button"
                      onClick={() => setShowCustomerPicker(true)}
                      className="px-2.5 py-1 text-[11px] font-semibold text-brand-primary bg-white rounded-md border border-brand-sky/40 hover:bg-blue-50 transition-colors"
                    >
                      Ganti
                    </button>
                  </div>
                ) : (
                  <button
                    type="button"
                    onClick={() => setShowCustomerPicker(true)}
                    className="w-full py-2.5 px-3 rounded-input border border-dashed border-slate-300 hover:border-brand-primary hover:bg-slate-50 text-slate-600 hover:text-brand-primary text-xs font-semibold flex items-center justify-between transition-all"
                  >
                    <div className="flex items-center gap-2">
                      <Users className="w-4 h-4 text-slate-400" />
                      <span>Pilih dari List Pelanggan</span>
                    </div>
                    <ChevronRight className="w-4 h-4 text-slate-400" />
                  </button>
                )}
              </div>

              {/* Total Banner on Mobile (hidden on lg, visible in right column on lg) */}
              <div className="lg:hidden bg-gradient-to-r from-blue-50 to-sky-50 border border-brand-sky/40 rounded-card p-4 text-center">
                <p className="text-xs font-semibold text-slate-500 mb-1">Total Tagihan</p>
                <p className="text-2xl font-extrabold text-brand-primary">{formatRupiah(cart.total)}</p>
                {cart.totalItemCount > 0 && (
                  <p className="text-[11px] text-slate-400 mt-0.5">{cart.totalItemCount} Item</p>
                )}
              </div>

              {/* Payment Method Selector */}
              <div>
                <p className="text-xs font-semibold text-slate-600 mb-2">Metode Pembayaran</p>
                <div className="flex gap-2 overflow-x-auto pb-1">
                  {paymentMethods.map((m) => {
                    const Icon = m.icon;
                    const isSelected = method === m.id;
                    return (
                      <button
                        type="button"
                        key={m.id}
                        onClick={() => {
                          setMethod(m.id as any);
                          if (m.id === 'Bayar Nanti' && selectedCustomer) {
                            setCustomerName(selectedCustomer.name);
                            if (selectedCustomer.phone) setCustomerPhone(selectedCustomer.phone);
                          }
                        }}
                        className={`flex-shrink-0 px-3.5 py-2.5 rounded-input flex flex-col items-center justify-center gap-1.5 border text-xs font-semibold transition-all ${
                          isSelected
                            ? 'bg-brand-primary border-brand-primary text-white shadow-xs'
                            : 'bg-white border-slate-200 text-slate-700 hover:bg-slate-100'
                        }`}
                      >
                        <Icon className="w-4 h-4" />
                        <span className="whitespace-nowrap">{m.label}</span>
                      </button>
                    );
                  })}
                </div>
              </div>

              {/* TUNAI MODE */}
              {method === 'Tunai' && (
                <div className="space-y-3 bg-white rounded-card p-4 border border-slate-200 shadow-soft">
                  <div>
                    <label className="text-xs font-semibold text-slate-700 block mb-1">
                      Uang Diterima (Rp)
                    </label>
                    <input
                      type="number"
                      value={cashReceivedInput}
                      onChange={(e) => setCashReceivedInput(e.target.value)}
                      className="w-full h-12 px-4 rounded-input bg-slate-50 text-slate-800 text-lg font-bold focus:ring-2 focus:ring-brand-primary focus:outline-none border border-slate-200"
                    />
                  </div>

                  {/* Quick Presets */}
                  <div className="flex flex-wrap gap-2">
                    {quickPresets.map((p, idx) => (
                      <button
                        type="button"
                        key={idx}
                        onClick={() => setCashReceivedInput(p.amount.toString())}
                        className="px-3 py-1.5 rounded-chip bg-slate-50 border border-slate-200 text-xs font-semibold text-slate-700 hover:bg-slate-100 transition-colors"
                      >
                        {p.label}
                      </button>
                    ))}
                  </div>

                  {/* Kembalian / Insufficient Warning */}
                  <div className="pt-3 border-t border-slate-200 space-y-2">
                    <div className="flex items-center justify-between">
                      <span className="text-xs font-semibold text-slate-600">Kembalian:</span>
                      <span
                        className={`text-lg font-bold ${
                          isCashInsufficient ? 'text-danger' : 'text-emerald-600'
                        }`}
                      >
                        {isCashInsufficient ? 'Uang kurang!' : formatRupiah(change)}
                      </span>
                    </div>

                    {!isCashInsufficient && change > 0 && (
                      <div className="mt-2 pt-2 border-t border-slate-200 space-y-2">
                        <label className="flex items-center gap-2 cursor-pointer">
                          <input
                            type="checkbox"
                            checked={isChangePending}
                            onChange={(e) => {
                              const checked = e.target.checked;
                              setIsChangePending(checked);
                              if (checked && selectedCustomer && !buyerNameForChange) {
                                setBuyerNameForChange(selectedCustomer.name);
                              }
                            }}
                            className="w-4 h-4 rounded text-brand-primary focus:ring-brand-primary"
                          />
                          <span className="text-xs font-semibold text-amber-800">
                            Uang kembalian belum diberikan ke pembeli?
                          </span>
                        </label>

                        {isChangePending && (
                          <div className="p-3 bg-amber-50 rounded-input border border-amber-200 space-y-2 animate-fadeIn">
                            <div>
                              <label className="text-[11px] font-semibold text-amber-900 block mb-1">
                                Nama Pembeli (Wajib) *
                              </label>
                              <input
                                type="text"
                                required={isChangePending}
                                value={buyerNameForChange}
                                onChange={(e) => setBuyerNameForChange(e.target.value)}
                                placeholder="Misal: Mas Danang"
                                className="w-full h-9 px-3 rounded-input bg-white text-slate-800 text-xs focus:ring-1 focus:ring-amber-500 focus:outline-none border border-amber-200"
                              />
                            </div>
                            <div>
                              <label className="text-[11px] font-medium text-amber-800 block mb-1">
                                Catatan Tambahan (Opsional)
                              </label>
                              <input
                                type="text"
                                value={changeNote}
                                onChange={(e) => setChangeNote(e.target.value)}
                                placeholder="Misal: Belum ada pecahan 5.000"
                                className="w-full h-9 px-3 rounded-input bg-white text-slate-800 text-xs focus:ring-1 focus:ring-amber-500 focus:outline-none border border-amber-200"
                              />
                            </div>
                            <p className="text-[10px] text-amber-700">
                              Sistem akan mencatat kembalian <b>{formatRupiah(change)}</b> sebagai <b>Belum Diberikan</b> di Beranda.
                            </p>
                          </div>
                        )}
                      </div>
                    )}
                  </div>
                </div>
              )}

              {/* QRIS MODE — LARGE DISPLAY */}
              {method === 'QRIS' && (
                <div className="bg-white rounded-card p-5 border border-slate-200 shadow-soft flex flex-col items-center text-center space-y-4">
                  <div className="text-sm font-bold text-slate-800">
                    {settings?.qrisMerchantName || settings?.businessName || 'QRIS TOKO KELOLA'}
                  </div>
                  <div className="text-base font-extrabold text-brand-primary">
                    Total: {formatRupiah(cart.total)}
                  </div>

                  {settings?.qrisImageUri ? (
                    <div className="p-3 bg-white rounded-2xl border-2 border-blue-200 shadow-lg w-72 h-72 sm:w-80 sm:h-80 aspect-square flex items-center justify-center">
                      <img
                        src={settings.qrisImageUri}
                        alt="QRIS Merchant"
                        className="w-full h-full object-contain rounded-xl"
                      />
                    </div>
                  ) : (
                    <div className="w-full p-4 bg-amber-50 rounded-card border border-amber-200 text-left space-y-1.5">
                      <div className="flex items-center gap-2 text-amber-800 font-bold text-xs">
                        <AlertCircle className="w-4 h-4 text-amber-600 flex-shrink-0" />
                        <span>Gambar QRIS Belum Diunggah</span>
                      </div>
                      <p className="text-[11px] text-amber-700 leading-relaxed">
                        Silakan upload gambar QRIS toko Anda di menu <b>Pengaturan</b> agar pelanggan dapat langsung melakukan scan pembayaran dari layar kasir ini.
                      </p>
                    </div>
                  )}

                  <p className="text-xs text-slate-500 leading-relaxed max-w-xs">
                    Arahkan pembeli untuk scan kode QR di atas, kemudian tekan tombol <b>Selesaikan Transaksi</b> setelah dana diterima.
                  </p>
                </div>
              )}

              {/* TRANSFER MODE */}
              {(method === 'Transfer' || method === 'E-Wallet') && (
                <div className="bg-white rounded-card p-4 border border-slate-200 shadow-soft flex flex-col items-center text-center space-y-3">
                  <div className="w-14 h-14 rounded-full bg-blue-50 flex items-center justify-center">
                    <Wallet className="w-7 h-7 text-brand-primary" />
                  </div>
                  <p className="text-sm font-bold text-slate-800">{method}</p>
                  <p className="text-base font-extrabold text-brand-primary">{formatRupiah(cart.total)}</p>
                  <p className="text-xs text-slate-500 leading-relaxed max-w-xs">
                    Pastikan pembeli sudah menyelesaikan pembayaran melalui {method}, kemudian tekan tombol <b>Selesaikan Transaksi</b>.
                  </p>
                </div>
              )}

              {/* BAYAR NANTI (KASBON) MODE */}
              {method === 'Bayar Nanti' && (
                <div className="space-y-3 bg-amber-50/70 rounded-card p-4 border border-amber-200 shadow-soft">
                  <div>
                    <label className="text-xs font-semibold text-amber-900 block mb-1">
                      <span className="flex items-center gap-1.5">
                        <User className="w-3.5 h-3.5" />
                        Nama Penghutang / Pelanggan *
                      </span>
                    </label>
                    <input
                      type="text"
                      required
                      value={customerName}
                      onChange={(e) => setCustomerName(e.target.value)}
                      placeholder="Misal: Mas Dimas Kost No. 4"
                      className="w-full h-11 px-3.5 rounded-input bg-white text-slate-800 text-xs focus:ring-1 focus:ring-amber-500 focus:outline-none border border-amber-200"
                    />
                  </div>

                  <div>
                    <label className="text-xs font-semibold text-amber-900 block mb-1">
                      <span className="flex items-center gap-1.5">
                        <Phone className="w-3.5 h-3.5" />
                        Nomor WhatsApp / HP (Opsional)
                      </span>
                    </label>
                    <input
                      type="text"
                      value={customerPhone}
                      onChange={(e) => setCustomerPhone(e.target.value)}
                      placeholder="Misal: 081234567890"
                      className="w-full h-11 px-3.5 rounded-input bg-white text-slate-800 text-xs focus:ring-1 focus:ring-amber-500 focus:outline-none border border-amber-200"
                    />
                  </div>

                  <p className="text-[11px] text-amber-700 leading-snug">
                    Transaksi ini akan otomatis dicatat ke menu <b>Kasbon / Hutang</b>.
                  </p>
                </div>
              )}
            </div>

            {/* Right Column (Tablet Landscape lg:col-span-5): Order Recap & Action */}
            <div className="lg:col-span-5 space-y-4">
              {/* Total Banner for Tablet Landscape */}
              <div className="hidden lg:block bg-gradient-to-r from-blue-50 to-sky-50 border border-brand-sky/40 rounded-card p-4 text-center">
                <p className="text-xs font-semibold text-slate-500 mb-1">Total Tagihan</p>
                <p className="text-3xl font-extrabold text-brand-primary">{formatRupiah(cart.total)}</p>
                {cart.totalItemCount > 0 && (
                  <p className="text-xs text-slate-400 mt-0.5">{cart.totalItemCount} Barang Dipilih</p>
                )}
              </div>

              {/* Order Items Breakdown */}
              <div className="bg-white rounded-card p-4 border border-slate-200 shadow-soft space-y-3">
                <div className="flex items-center gap-2 pb-2 border-b border-slate-100 text-xs font-bold text-slate-700">
                  <ShoppingBag className="w-4 h-4 text-brand-primary" />
                  <span>Rincian Pesanan ({cart.totalItemCount})</span>
                </div>

                <div className="max-h-56 overflow-y-auto space-y-2 pr-1">
                  {cart.items.map(({ product, quantity }) => (
                    <div key={product.id} className="flex justify-between items-start text-xs">
                      <div>
                        <div className="font-semibold text-slate-800">{product.name}</div>
                        <div className="text-[11px] text-slate-400">
                          {formatRupiah(product.sellingPrice)} × {quantity}
                        </div>
                      </div>
                      <div className="font-bold text-slate-700">
                        {formatRupiah(product.sellingPrice * quantity)}
                      </div>
                    </div>
                  ))}
                </div>

                <div className="pt-2 border-t border-slate-100 space-y-1 text-xs">
                  <div className="flex justify-between text-slate-500">
                    <span>Subtotal:</span>
                    <span className="font-medium text-slate-700">{formatRupiah(cart.subtotal)}</span>
                  </div>
                  {cart.promoDiscount > 0 && (
                    <div className="flex justify-between text-emerald-600 font-semibold">
                      <span>Diskon Promo:</span>
                      <span>-{formatRupiah(cart.promoDiscount)}</span>
                    </div>
                  )}
                  <div className="flex justify-between text-sm font-bold text-slate-800 pt-1.5 border-t border-slate-200">
                    <span>Total Pembayaran:</span>
                    <span className="text-brand-primary text-base">{formatRupiah(cart.total)}</span>
                  </div>
                </div>
              </div>

              {/* CTA button visible on tablet landscape */}
              <div className="hidden lg:block pt-1">
                <button
                  type="submit"
                  disabled={isSubmitDisabled}
                  className={`w-full h-12 rounded-input text-white text-sm font-bold flex items-center justify-center gap-2 shadow-md transition-all active:scale-[0.99] ${
                    isSubmitDisabled
                      ? 'bg-slate-300 text-slate-500 cursor-not-allowed'
                      : 'bg-brand-primary hover:bg-brand-deep cursor-pointer'
                  }`}
                >
                  <Check className="w-5 h-5" />
                  <span>Selesaikan Transaksi</span>
                </button>
              </div>
            </div>
          </form>
        </div>
      </div>

      {/* Fixed Bottom CTA for Mobile & Tablet Portrait */}
      <div className="lg:hidden fixed bottom-0 left-0 right-0 p-4 bg-white border-t border-slate-200 shadow-lg z-10">
        <button
          type="button"
          onClick={handleSubmit}
          disabled={isSubmitDisabled}
          className={`w-full h-12 rounded-input text-white text-sm font-bold flex items-center justify-center gap-2 shadow-md transition-all active:scale-[0.99] ${
            isSubmitDisabled
              ? 'bg-slate-300 text-slate-500 cursor-not-allowed'
              : 'bg-brand-primary hover:bg-brand-deep cursor-pointer'
          }`}
        >
          <Check className="w-5 h-5" />
          <span>Selesaikan Transaksi</span>
        </button>
      </div>

      {/* Customer Picker Dialog */}
      {showCustomerPicker && (
        <CustomerPickerDialog
          customers={customers}
          selectedCustomerId={selectedCustomer?.id}
          onSelectCustomer={handleSelectCustomer}
          onAddNewCustomer={() => setShowAddCustomer(true)}
          onDismiss={() => setShowCustomerPicker(false)}
        />
      )}

      {/* Add New Customer Dialog on the fly */}
      {showAddCustomer && (
        <AddEditCustomerDialog
          onSaveCustomer={(newCust) => {
            onSaveNewCustomer?.(newCust);
            handleSelectCustomer(newCust);
            setShowAddCustomer(false);
          }}
          onDismiss={() => setShowAddCustomer(false)}
        />
      )}
    </div>
  );
};
