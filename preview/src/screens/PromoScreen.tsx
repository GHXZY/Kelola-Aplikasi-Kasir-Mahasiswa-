import React, { useState } from 'react';
import { ArrowLeft, Plus, Trash2, Tag, Percent, ShoppingBag, X, AlertCircle } from 'lucide-react';
import { PromoEntity, ProductEntity, PromoRequirement } from '../types';
import { formatRupiah } from '../utils/format';
import { parseRequirements } from '../utils/promoEngine';

interface PromoScreenProps {
  promos: PromoEntity[];
  products: ProductEntity[];
  onSavePromo: (promo: PromoEntity) => void;
  onTogglePromoActive: (promoId: number, isActive: boolean) => void;
  onDeletePromo: (promoId: number) => void;
  onNavigateBack: () => void;
}

export const PromoScreen: React.FC<PromoScreenProps> = ({
  promos,
  products,
  onSavePromo,
  onTogglePromoActive,
  onDeletePromo,
  onNavigateBack
}) => {
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [promoToDelete, setPromoToDelete] = useState<PromoEntity | null>(null);

  // Form State for New Promo
  const [promoName, setPromoName] = useState('');
  const [discountType, setDiscountType] = useState<'NOMINAL' | 'PERCENTAGE'>('NOMINAL');
  const [discountValue, setDiscountValue] = useState<number | ''>(5000);
  const [maxUsage, setMaxUsage] = useState<number>(0); // 0 = unlimited, 1 = 1x
  const [requirements, setRequirements] = useState<PromoRequirement[]>([]);
  const [selectedProductId, setSelectedProductId] = useState<number>(products[0]?.id || 0);

  const handleAddRequirement = () => {
    if (!selectedProductId) return;
    const existing = requirements.find((r) => r.productId === selectedProductId);
    if (existing) {
      setRequirements(
        requirements.map((r) =>
          r.productId === selectedProductId ? { ...r, quantity: r.quantity + 1 } : r
        )
      );
    } else {
      setRequirements([...requirements, { productId: selectedProductId, quantity: 1 }]);
    }
  };

  const handleUpdateReqQty = (productId: number, delta: number) => {
    setRequirements(
      requirements
        .map((r) => {
          if (r.productId === productId) {
            const next = r.quantity + delta;
            return next > 0 ? { ...r, quantity: next } : null;
          }
          return r;
        })
        .filter((r): r is PromoRequirement => r !== null)
    );
  };

  const handleRemoveReq = (productId: number) => {
    setRequirements(requirements.filter((r) => r.productId !== productId));
  };

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    if (!promoName.trim() || requirements.length === 0 || !discountValue || discountValue <= 0) {
      return;
    }

    const newPromo: PromoEntity = {
      id: Date.now(),
      name: promoName.trim(),
      title: promoName.trim(),
      description: `Bundling ${requirements.length} produk syarat`,
      isActive: true,
      discountType,
      discountValue: Number(discountValue),
      maxUsage,
      requiredItemsJson: JSON.stringify(requirements),
      createdAt: Date.now(),
      updatedAt: Date.now()
    };

    onSavePromo(newPromo);
    setShowCreateModal(false);

    // Reset Form
    setPromoName('');
    setDiscountType('NOMINAL');
    setDiscountValue(5000);
    setMaxUsage(0);
    setRequirements([]);
  };

  const productMap = new Map(products.map((p) => [p.id, p]));

  return (
    <div className="flex-1 flex flex-col overflow-hidden bg-[#F7F9FF] relative">
      {/* Top Bar */}
      <div className="p-4 bg-white border-b border-slate-200/80 flex-shrink-0">
        <div className="max-w-5xl mx-auto flex items-center justify-between">
          <div className="flex items-center gap-3">
            <button
              onClick={onNavigateBack}
              data-testid="button_back_promo"
              className="w-10 h-10 rounded-full bg-slate-100 hover:bg-slate-200 flex items-center justify-center text-slate-700 transition-all"
            >
              <ArrowLeft className="w-5 h-5 text-brand-primary" />
            </button>
            <div>
              <h1 className="text-base font-bold text-slate-800">Promo & Bundling</h1>
              <p className="text-xs text-slate-500">Otomatis deteksi diskon kombinasi produk di kasir</p>
            </div>
          </div>

          <button
            onClick={() => setShowCreateModal(true)}
            data-testid="button_create_promo"
            className="h-9 px-3 rounded-input bg-brand-primary hover:bg-brand-deep text-white text-xs font-semibold flex items-center gap-1.5 shadow-xs transition-all"
          >
            <Plus className="w-4 h-4" />
            <span>Buat Promo</span>
          </button>
        </div>
      </div>

      {/* Promos List */}
      <div className="flex-1 overflow-y-auto p-4 pb-20 md:p-6">
        <div className="max-w-5xl mx-auto">
          {promos.length === 0 ? (
            <div className="bg-white rounded-card p-8 text-center border border-slate-200 text-slate-400">
              <Tag className="w-10 h-10 mx-auto text-slate-300 mb-2" />
              <p className="text-sm font-semibold text-slate-600">Belum Ada Promo Aktif</p>
              <p className="text-xs text-slate-400 mt-1">Buat promo bundling paket hemat untuk meningkatkan penjualan kasir.</p>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-3.5 space-y-0">
              {promos.map((promo) => {
            const reqs = parseRequirements(promo.requiredItemsJson);
            const promoTitle = promo.name || promo.title || 'Promo Bundling';

            return (
              <div
                key={promo.id}
                data-testid={`promo_card_${promo.id}`}
                className={`bg-white rounded-card p-4 border transition-all ${
                  promo.isActive ? 'border-slate-200 shadow-soft' : 'border-slate-200/60 opacity-60 bg-slate-50'
                }`}
              >
                <div className="flex items-start justify-between gap-2">
                  <div className="flex items-start gap-3">
                    <div
                      className={`w-10 h-10 rounded-input flex items-center justify-center flex-shrink-0 ${
                        promo.discountType === 'PERCENTAGE'
                          ? 'bg-amber-100 text-amber-700'
                          : 'bg-brand-sky/30 text-brand-primary'
                      }`}
                    >
                      {promo.discountType === 'PERCENTAGE' ? <Percent className="w-5 h-5" /> : <Tag className="w-5 h-5" />}
                    </div>
                    <div>
                      <h3 className="text-sm font-bold text-slate-800">{promoTitle}</h3>
                      <div className="flex items-center gap-2 mt-1">
                        <span className="text-xs font-bold text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded">
                          {promo.discountType === 'PERCENTAGE'
                            ? `Diskon ${promo.discountValue}%`
                            : `Potongan ${formatRupiah(promo.discountValue)}`}
                        </span>
                        <span className="text-[11px] font-medium text-slate-400">
                          {promo.maxUsage === 1 ? 'Maks 1x per struk' : 'Kelipatan tanpa batas'}
                        </span>
                      </div>
                    </div>
                  </div>

                  {/* Toggle Active Switch */}
                  <div className="flex items-center gap-2">
                    <button
                      onClick={() => onTogglePromoActive(promo.id, !promo.isActive)}
                      className={`w-11 h-6 flex items-center rounded-full p-1 transition-colors ${
                        promo.isActive ? 'bg-brand-primary' : 'bg-slate-300'
                      }`}
                      title={promo.isActive ? 'Nonaktifkan Promo' : 'Aktifkan Promo'}
                    >
                      <div
                        className={`bg-white w-4 h-4 rounded-full shadow-md transform transition-transform ${
                          promo.isActive ? 'translate-x-5' : 'translate-x-0'
                        }`}
                      />
                    </button>

                    <button
                      onClick={() => setPromoToDelete(promo)}
                      className="w-8 h-8 rounded-full text-slate-400 hover:text-rose-600 hover:bg-rose-50 flex items-center justify-center transition-all"
                      title="Hapus Promo"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                </div>

                {/* Requirements List */}
                <div className="mt-3 pt-3 border-t border-slate-100">
                  <p className="text-[11px] font-semibold text-slate-500 mb-1.5 flex items-center gap-1">
                    <ShoppingBag className="w-3 h-3 text-slate-400" />
                    <span>Produk Syarat Bundling:</span>
                  </p>
                  <div className="flex flex-wrap gap-1.5">
                    {reqs.map((req, idx) => {
                      const prod = productMap.get(req.productId);
                      return (
                        <span
                          key={idx}
                          className="text-[11px] bg-slate-100 text-slate-700 px-2 py-1 rounded-input font-medium flex items-center gap-1"
                        >
                          <span className="font-bold text-brand-primary">{req.quantity}x</span>
                          <span>{prod ? prod.name : `Produk #${req.productId}`}</span>
                        </span>
                      );
                    })}
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  </div>

      {/* Create Promo Modal */}
      {showCreateModal && (
        <div className="absolute inset-0 bg-black/40 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4 backdrop-blur-xs">
          <div className="bg-white w-full max-w-lg rounded-t-2xl sm:rounded-2xl max-h-[90vh] flex flex-col shadow-2xl overflow-hidden animate-in slide-in-from-bottom-4 duration-200">
            {/* Modal Header */}
            <div className="p-4 border-b border-slate-200 flex items-center justify-between">
              <div>
                <h2 className="text-base font-bold text-slate-800">Buat Promo Bundling Baru</h2>
                <p className="text-xs text-slate-500">Tentukan kombinasi produk dan besaran potongan harga</p>
              </div>
              <button
                onClick={() => setShowCreateModal(false)}
                className="w-8 h-8 rounded-full bg-slate-100 hover:bg-slate-200 flex items-center justify-center text-slate-600"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            {/* Modal Form */}
            <form onSubmit={handleSave} className="flex-1 overflow-y-auto p-4 space-y-4">
              {/* Nama Promo */}
              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">Nama Promo</label>
                <input
                  type="text"
                  value={promoName}
                  onChange={(e) => setPromoName(e.target.value)}
                  placeholder="Contoh: Paket Hemat Kopi + Keripik"
                  required
                  className="w-full h-11 px-3 rounded-input border border-slate-200 focus:border-brand-primary focus:ring-2 focus:ring-brand-sky/20 outline-none text-sm text-slate-800"
                />
              </div>

              {/* Produk Syarat */}
              <div className="space-y-2">
                <label className="block text-xs font-bold text-slate-700">Produk yang Wajib Dibeli</label>
                
                {/* Requirements Picker */}
                <div className="flex items-center gap-2">
                  <select
                    value={selectedProductId}
                    onChange={(e) => setSelectedProductId(Number(e.target.value))}
                    className="flex-1 h-10 px-3 rounded-input border border-slate-200 text-xs text-slate-800 bg-white"
                  >
                    {products.map((p) => (
                      <option key={p.id} value={p.id}>
                        {p.name} ({formatRupiah(p.sellingPrice)})
                      </option>
                    ))}
                  </select>

                  <button
                    type="button"
                    onClick={handleAddRequirement}
                    className="h-10 px-3 rounded-input bg-brand-sky/20 hover:bg-brand-sky/30 text-brand-primary text-xs font-semibold flex items-center gap-1 transition-all"
                  >
                    <Plus className="w-4 h-4" />
                    <span>Tambah</span>
                  </button>
                </div>

                {/* Selected Requirements List */}
                <div className="space-y-1.5 mt-2">
                  {requirements.length === 0 ? (
                    <div className="p-3 bg-amber-50 rounded-input border border-amber-200/60 text-xs text-amber-800 flex items-center gap-2">
                      <AlertCircle className="w-4 h-4 text-amber-600 flex-shrink-0" />
                      <span>Tambahkan minimal 1 produk syarat ke dalam bundling ini.</span>
                    </div>
                  ) : (
                    requirements.map((req) => {
                      const prod = productMap.get(req.productId);
                      return (
                        <div
                          key={req.productId}
                          className="flex items-center justify-between p-2.5 bg-slate-50 rounded-input border border-slate-200"
                        >
                          <div className="min-w-0 pr-2">
                            <p className="text-xs font-semibold text-slate-800 truncate">{prod?.name}</p>
                            <p className="text-[11px] text-slate-400">{formatRupiah(prod?.sellingPrice || 0)} / {prod?.unit || 'pcs'}</p>
                          </div>

                          <div className="flex items-center gap-2">
                            <div className="flex items-center border border-slate-200 bg-white rounded-input overflow-hidden">
                              <button
                                type="button"
                                onClick={() => handleUpdateReqQty(req.productId, -1)}
                                className="w-7 h-7 flex items-center justify-center text-slate-600 hover:bg-slate-100 font-bold"
                              >
                                -
                              </button>
                              <span className="w-8 text-center text-xs font-bold text-slate-800">
                                {req.quantity}
                              </span>
                              <button
                                type="button"
                                onClick={() => handleUpdateReqQty(req.productId, 1)}
                                className="w-7 h-7 flex items-center justify-center text-slate-600 hover:bg-slate-100 font-bold"
                              >
                                +
                              </button>
                            </div>

                            <button
                              type="button"
                              onClick={() => handleRemoveReq(req.productId)}
                              className="w-7 h-7 text-slate-400 hover:text-rose-600 flex items-center justify-center"
                            >
                              <X className="w-4 h-4" />
                            </button>
                          </div>
                        </div>
                      );
                    })
                  )}
                </div>
              </div>

              {/* Jenis Diskon & Nilai */}
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">Jenis Potongan</label>
                  <div className="flex rounded-input border border-slate-200 p-1 bg-slate-50">
                    <button
                      type="button"
                      onClick={() => setDiscountType('NOMINAL')}
                      className={`flex-1 py-1.5 text-xs font-semibold rounded ${
                        discountType === 'NOMINAL' ? 'bg-white shadow-xs text-brand-primary font-bold' : 'text-slate-600'
                      }`}
                    >
                      Nominal (Rp)
                    </button>
                    <button
                      type="button"
                      onClick={() => setDiscountType('PERCENTAGE')}
                      className={`flex-1 py-1.5 text-xs font-semibold rounded ${
                        discountType === 'PERCENTAGE' ? 'bg-white shadow-xs text-brand-primary font-bold' : 'text-slate-600'
                      }`}
                    >
                      Persen (%)
                    </button>
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">
                    {discountType === 'NOMINAL' ? 'Besar Potongan (Rp)' : 'Persentase Diskon (%)'}
                  </label>
                  <input
                    type="number"
                    min="1"
                    max={discountType === 'PERCENTAGE' ? 100 : undefined}
                    value={discountValue}
                    onChange={(e) => setDiscountValue(e.target.value === '' ? '' : Number(e.target.value))}
                    required
                    className="w-full h-10 px-3 rounded-input border border-slate-200 focus:border-brand-primary outline-none text-xs font-semibold text-slate-800"
                  />
                </div>
              </div>

              {/* Batas Penggunaan */}
              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">Batas Penggunaan per Transaksi</label>
                <div className="flex gap-2">
                  <button
                    type="button"
                    onClick={() => setMaxUsage(0)}
                    className={`flex-1 p-2.5 rounded-input border text-left transition-all ${
                      maxUsage === 0
                        ? 'border-brand-primary bg-brand-sky/10 text-brand-primary font-bold'
                        : 'border-slate-200 text-slate-600 bg-white'
                    }`}
                  >
                    <div className="text-xs">Kelipatan Tanpa Batas</div>
                    <div className="text-[10px] text-slate-400 font-normal">Diskon bertambah jika beli berkali lipat</div>
                  </button>

                  <button
                    type="button"
                    onClick={() => setMaxUsage(1)}
                    className={`flex-1 p-2.5 rounded-input border text-left transition-all ${
                      maxUsage === 1
                        ? 'border-brand-primary bg-brand-sky/10 text-brand-primary font-bold'
                        : 'border-slate-200 text-slate-600 bg-white'
                    }`}
                  >
                    <div className="text-xs">Maksimal 1x</div>
                    <div className="text-[10px] text-slate-400 font-normal">Hanya berlaku 1x per struk transaksi</div>
                  </button>
                </div>
              </div>

              {/* Submit Buttons */}
              <div className="pt-2 flex items-center justify-end gap-2 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  className="h-10 px-4 rounded-input border border-slate-200 text-slate-600 text-xs font-semibold hover:bg-slate-50"
                >
                  Batal
                </button>
                <button
                  type="submit"
                  disabled={requirements.length === 0 || !promoName.trim() || !discountValue}
                  className="h-10 px-5 rounded-input bg-brand-primary hover:bg-brand-deep disabled:bg-slate-300 text-white text-xs font-bold shadow-sm transition-all"
                >
                  Simpan Promo
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Delete Promo Confirmation */}
      {promoToDelete && (
        <div className="absolute inset-0 bg-black/40 z-50 flex items-center justify-center p-4 backdrop-blur-xs">
          <div className="bg-white rounded-card p-5 max-w-sm w-full shadow-2xl border border-slate-200">
            <h3 className="text-sm font-bold text-slate-800">Hapus Promo Ini?</h3>
            <p className="text-xs text-slate-500 mt-1">
              Promo <span className="font-semibold text-slate-700">{promoToDelete.name || promoToDelete.title}</span> akan dihapus permanen.
            </p>
            <div className="flex items-center justify-end gap-2 mt-4">
              <button
                onClick={() => setPromoToDelete(null)}
                className="h-9 px-3 rounded-input border border-slate-200 text-xs font-semibold text-slate-600 hover:bg-slate-50"
              >
                Batal
              </button>
              <button
                onClick={() => {
                  onDeletePromo(promoToDelete.id);
                  setPromoToDelete(null);
                }}
                className="h-9 px-4 rounded-input bg-rose-600 hover:bg-rose-700 text-xs font-bold text-white shadow-xs"
              >
                Hapus
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
