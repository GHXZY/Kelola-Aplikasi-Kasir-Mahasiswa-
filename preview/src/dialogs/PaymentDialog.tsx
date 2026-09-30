import React, { useState } from 'react';
import { X, Wallet, QrCode, Hourglass, Check, AlertCircle } from 'lucide-react';
import { CartSummary, BusinessSettings } from '../types';
import { formatRupiah } from '../utils/format';

interface PaymentDialogProps {
  cart: CartSummary;
  settings?: BusinessSettings;
  onCompletePayment: (
    paymentMethod: string,
    cashReceived: number,
    debtCustomerName?: string,
    debtPhone?: string,
    changePending?: { buyerName: string; note: string }
  ) => void;
  onDismiss: () => void;
}

export const PaymentDialog: React.FC<PaymentDialogProps> = ({
  cart,
  settings,
  onCompletePayment,
  onDismiss
}) => {
  const [method, setMethod] = useState<'Tunai' | 'QRIS' | 'Bayar Nanti'>(
    (settings?.defaultPaymentMethod === 'QRIS' ? 'QRIS' : 'Tunai')
  );
  const [cashReceivedInput, setCashReceivedInput] = useState(cart.total.toString());
  const [customerName, setCustomerName] = useState('');
  const [customerPhone, setCustomerPhone] = useState('');

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

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (method === 'Tunai') {
      if (isCashInsufficient) return;
      if (isChangePending && !buyerNameForChange.trim()) return;
    }
    if (method === 'Bayar Nanti' && !customerName.trim()) return;

    onCompletePayment(
      method,
      method === 'Tunai' ? cashReceived : cart.total,
      method === 'Bayar Nanti' ? customerName : undefined,
      method === 'Bayar Nanti' ? customerPhone : undefined,
      method === 'Tunai' && isChangePending && change > 0
        ? { buyerName: buyerNameForChange.trim(), note: changeNote.trim() }
        : undefined
    );
  };

  return (
    <div className="fixed inset-0 z-50 flex flex-col justify-end bg-black/60 backdrop-blur-xs animate-fadeIn">
      <div className="flex-1" onClick={onDismiss} />

      <div className="bg-white rounded-t-sheet border-t border-slate-200 shadow-2xl flex flex-col max-h-[90vh] animate-slideUp w-full md:max-w-xl md:mx-auto md:rounded-card md:mb-6 md:border">
        {/* Header */}
        <div className="p-4 pb-2 border-b border-slate-100 flex items-center justify-between flex-shrink-0">
          <div>
            <h2 className="text-base font-bold text-slate-800">Metode Pembayaran</h2>
            <p className="text-xs text-slate-400">Pilih cara bayar transaksi pelanggan</p>
          </div>
          <button
            onClick={onDismiss}
            className="w-8 h-8 rounded-full bg-slate-100 flex items-center justify-center text-slate-500 hover:text-slate-800"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Form Body */}
        <form onSubmit={handleSubmit} className="flex-1 overflow-y-auto p-4 space-y-4">
          {/* Total Banner */}
          <div className="bg-blue-50/80 border border-brand-sky/40 rounded-card p-3.5 flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-600">Total Tagihan:</span>
            <span className="text-xl font-bold text-brand-primary">{formatRupiah(cart.total)}</span>
          </div>

          {/* Payment Method Selector Tabs */}
          <div className="grid grid-cols-3 gap-2">
            {[
              { id: 'Tunai', icon: Wallet },
              { id: 'QRIS', icon: QrCode },
              { id: 'Bayar Nanti', icon: Hourglass }
            ].map((m) => {
              const Icon = m.icon;
              const isSelected = method === m.id;
              return (
                <button
                  type="button"
                  key={m.id}
                  onClick={() => setMethod(m.id as any)}
                  className={`p-2.5 rounded-input flex flex-col items-center justify-center gap-1.5 border text-xs font-semibold transition-all ${
                    isSelected
                      ? 'bg-brand-primary border-brand-primary text-white shadow-xs'
                      : 'bg-slate-50 border-slate-200 text-slate-700 hover:bg-slate-100'
                  }`}
                >
                  <Icon className="w-4 h-4" />
                  <span>{m.id}</span>
                </button>
              );
            })}
          </div>

          {/* TUNAI MODE */}
          {method === 'Tunai' && (
            <div className="space-y-3 bg-slate-50 rounded-card p-3.5 border border-slate-200">
              <div>
                <label className="text-xs font-semibold text-slate-700 block mb-1">Uang Diterima (Rp)</label>
                <input
                  type="number"
                  value={cashReceivedInput}
                  onChange={(e) => setCashReceivedInput(e.target.value)}
                  className="w-full h-11 px-3.5 rounded-input bg-white text-slate-800 text-base font-bold focus:ring-1 focus:ring-brand-primary focus:outline-none border border-slate-200"
                />
              </div>

              {/* Quick Presets */}
              <div className="flex flex-wrap gap-1.5">
                {quickPresets.map((p, idx) => (
                  <button
                    type="button"
                    key={idx}
                    onClick={() => setCashReceivedInput(p.amount.toString())}
                    className="px-2.5 py-1 rounded-chip bg-white border border-slate-200 text-[11px] font-semibold text-slate-700 hover:bg-slate-100"
                  >
                    {p.label}
                  </button>
                ))}
              </div>

              {/* Kembalian / Insufficient Warning */}
              <div className="pt-2 border-t border-slate-200 space-y-2">
                <div className="flex items-center justify-between">
                  <span className="text-xs font-semibold text-slate-600">Kembalian:</span>
                  <span
                    className={`text-base font-bold ${
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
                        onChange={(e) => setIsChangePending(e.target.checked)}
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

          {/* QRIS MODE */}
          {method === 'QRIS' && (
            <div className="bg-slate-50 rounded-card p-4 border border-slate-200 flex flex-col items-center text-center space-y-3">
              <div className="text-xs font-bold text-slate-800">
                {settings?.qrisMerchantName || settings?.businessName || 'QRIS TOKO KELOLA'}
              </div>
              <div className="text-sm font-extrabold text-brand-primary">
                Total Tagihan: {formatRupiah(cart.total)}
              </div>

              {settings?.qrisImageUri ? (
                <div className="p-2 bg-white rounded-xl border border-slate-200 shadow-xs w-44 h-44 sm:w-48 sm:h-48 aspect-square flex items-center justify-center">
                  <img
                    src={settings.qrisImageUri}
                    alt="QRIS Merchant"
                    className="w-full h-full object-contain rounded-lg"
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

              <p className="text-[11px] text-slate-500">
                Arahkan pembeli untuk scan kode QR, kemudian tekan tombol selesaikan transaksi di bawah setelah dana diterima.
              </p>
            </div>
          )}

          {/* BAYAR NANTI (KASBON) MODE */}
          {method === 'Bayar Nanti' && (
            <div className="space-y-3 bg-amber-50/70 rounded-card p-3.5 border border-amber-200">
              <div>
                <label className="text-xs font-semibold text-amber-900 block mb-1">Nama Penghutang / Pelanggan *</label>
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
                <label className="text-xs font-semibold text-amber-900 block mb-1">Nomor WhatsApp / HP (Opsional)</label>
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

          {/* Submit CTA */}
          <button
            type="submit"
            disabled={method === 'Tunai' && (isCashInsufficient || (isChangePending && !buyerNameForChange.trim()))}
            className={`w-full h-12 rounded-input text-white text-sm font-bold flex items-center justify-center gap-2 shadow-md transition-all active:scale-[0.99] ${
              method === 'Tunai' && (isCashInsufficient || (isChangePending && !buyerNameForChange.trim()))
                ? 'bg-slate-300 text-slate-500 cursor-not-allowed'
                : 'bg-brand-primary hover:bg-brand-deep'
            }`}
          >
            <Check className="w-4 h-4" />
            <span>Selesaikan Transaksi</span>
          </button>
        </form>
      </div>
    </div>
  );
};

