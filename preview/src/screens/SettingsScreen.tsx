import React, { useState } from 'react';
import {
  ArrowLeft,
  Store,
  CreditCard,
  Wallet,
  Receipt,
  Sun,
  Moon,
  Monitor,
  Check,
  Shield,
  Download,
  RotateCcw,
  AlertTriangle,
  QrCode,
  Upload,
  Trash2,
  Crop,
  Smartphone,
  Image as ImageIcon,
  Info
} from 'lucide-react';
import { BusinessSettings } from '../types';
import { KelolaLogoBadge } from '../components/KelolaLogo';
import { ConfirmationDialog } from '../components/CommonComponents';
import { QrisCropDialog } from '../dialogs/QrisCropDialog';

interface SettingsScreenProps {
  settings: BusinessSettings;
  onSaveSettings: (newSettings: BusinessSettings) => void;
  onResetAllData: () => void;
  onNavigateBack: () => void;
}

export const SettingsScreen: React.FC<SettingsScreenProps> = ({
  settings,
  onSaveSettings,
  onResetAllData,
  onNavigateBack
}) => {
  const [formData, setFormData] = useState<BusinessSettings>({ ...settings });
  const [savedSuccess, setSavedSuccess] = useState(false);
  const [showResetConfirm, setShowResetConfirm] = useState(false);
  const [uploadError, setUploadError] = useState<string | null>(null);
  const [rawImageForCrop, setRawImageForCrop] = useState<string | null>(null);
  const [isCropOpen, setIsCropOpen] = useState(false);
  const [originalRawImage, setOriginalRawImage] = useState<string | null>(null);

  const handleQrisFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const validTypes = ['image/jpeg', 'image/png', 'image/webp'];
    if (!validTypes.includes(file.type)) {
      setUploadError('Format tidak didukung. Harap gunakan gambar JPG, PNG, atau WebP.');
      return;
    }

    if (file.size > 2 * 1024 * 1024) {
      setUploadError('Ukuran file terlalu besar. Maksimal ukuran gambar adalah 2MB.');
      return;
    }

    setUploadError(null);
    const reader = new FileReader();
    reader.onload = () => {
      if (typeof reader.result === 'string') {
        const rawUrl = reader.result as string;
        setRawImageForCrop(rawUrl);
        setOriginalRawImage(rawUrl);
        setIsCropOpen(true);
      }
    };
    reader.readAsDataURL(file);
    // Reset file input value so selecting the same file triggers change
    e.target.value = '';
  };

  const handleSave = () => {
    onSaveSettings(formData);
    setSavedSuccess(true);
    setTimeout(() => setSavedSuccess(false), 2000);
  };

  return (
    <div className="flex-1 flex flex-col overflow-hidden bg-[#F7F9FF]">
      {/* Top Bar */}
      <div className="p-4 bg-white border-b border-slate-200/80 flex-shrink-0">
        <div className="max-w-3xl mx-auto flex items-center justify-between">
          <div className="flex items-center gap-3">
            <button
              onClick={onNavigateBack}
              data-testid="button_back_from_settings"
              className="w-10 h-10 rounded-full bg-slate-100 hover:bg-slate-200 flex items-center justify-center text-slate-700 transition-all"
            >
              <ArrowLeft className="w-5 h-5 text-brand-primary" />
            </button>
            <div>
              <h1 className="text-base font-bold text-slate-800">Pengaturan</h1>
              <p className="text-xs text-slate-500">Kelola data usaha & preferensi kasir</p>
            </div>
          </div>

          <KelolaLogoBadge size={34} />
        </div>
      </div>

      {/* Settings Form Container */}
      <div className="flex-1 overflow-y-auto p-4 pb-20 md:p-6">
        <div className="max-w-3xl mx-auto space-y-4">
          {savedSuccess && (
            <div className="p-3 bg-emerald-50 border border-emerald-200 text-emerald-700 rounded-input text-xs font-semibold flex items-center gap-2 animate-fadeIn">
              <Check className="w-4 h-4" />
              <span>Pengaturan berhasil disimpan!</span>
            </div>
          )}

        {/* 1. Identitas Usaha */}
        <div className="bg-white rounded-card p-4 border border-slate-200 shadow-soft space-y-3">
          <div className="flex items-center gap-2 text-brand-primary pb-2 border-b border-slate-100 font-bold text-xs tracking-wider uppercase">
            <Store className="w-4 h-4" />
            <span>Identitas Usaha & Toko</span>
          </div>

          <div className="space-y-2">
            <div>
              <label className="text-xs font-medium text-slate-700 block mb-1">Nama Usaha / Toko</label>
              <input
                type="text"
                value={formData.businessName}
                onChange={(e) => setFormData({ ...formData, businessName: e.target.value })}
                className="w-full h-11 px-3.5 rounded-input bg-slate-100 text-slate-800 text-xs focus:bg-white focus:ring-1 focus:ring-brand-primary focus:outline-none"
              />
            </div>

            <div>
              <label className="text-xs font-medium text-slate-700 block mb-1">Alamat Usaha</label>
              <input
                type="text"
                value={formData.address}
                onChange={(e) => setFormData({ ...formData, address: e.target.value })}
                className="w-full h-11 px-3.5 rounded-input bg-slate-100 text-slate-800 text-xs focus:bg-white focus:ring-1 focus:ring-brand-primary focus:outline-none"
              />
            </div>

            <div>
              <label className="text-xs font-medium text-slate-700 block mb-1">Nomor Telepon / WhatsApp</label>
              <input
                type="text"
                value={formData.phone}
                onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                className="w-full h-11 px-3.5 rounded-input bg-slate-100 text-slate-800 text-xs focus:bg-white focus:ring-1 focus:ring-brand-primary focus:outline-none"
              />
            </div>
          </div>
        </div>

        {/* 2. Metode Pembayaran & QRIS */}
        <div className="bg-white rounded-card p-4 border border-slate-200 shadow-soft space-y-3">
          <div className="flex items-center gap-2 text-brand-primary pb-2 border-b border-slate-100 font-bold text-xs tracking-wider uppercase">
            <CreditCard className="w-4 h-4" />
            <span>Metode Pembayaran Kasir</span>
          </div>

          <div>
            <label className="text-xs font-semibold text-slate-700 block mb-1">Metode Bawaan</label>
            <div className="flex items-center gap-2">
              {['Tunai', 'QRIS'].map((method) => (
                <button
                  key={method}
                  onClick={() => setFormData({ ...formData, defaultPaymentMethod: method })}
                  className={`h-10 px-4 rounded-input text-xs font-semibold flex items-center gap-2 border transition-all ${
                    formData.defaultPaymentMethod === method
                      ? 'bg-brand-primary border-brand-primary text-white shadow-xs'
                      : 'bg-slate-100 border-transparent text-slate-700 hover:bg-slate-200'
                  }`}
                >
                  {method === 'QRIS' ? <QrCode className="w-4 h-4" /> : <Wallet className="w-4 h-4" />}
                  <span>{method}</span>
                </button>
              ))}
            </div>
          </div>

          {/* QRIS Merchant & Image Upload */}
          <div className="pt-2 border-t border-slate-100 space-y-3">
            <div>
              <label className="text-xs font-medium text-slate-700 block mb-1">
                Nama Merchant QRIS (Tampil di Kasir)
              </label>
              <input
                type="text"
                value={formData.qrisMerchantName || ''}
                onChange={(e) => setFormData({ ...formData, qrisMerchantName: e.target.value })}
                placeholder="Misal: WARUNG KELOLA BERKAH"
                className="w-full h-11 px-3.5 rounded-input bg-slate-100 text-slate-800 text-xs focus:bg-white focus:ring-1 focus:ring-brand-primary focus:outline-none uppercase font-semibold"
              />
            </div>

            <div>
              <label className="text-xs font-medium text-slate-700 block mb-1.5">
                Foto / Gambar QRIS Toko
              </label>

              {uploadError && (
                <div className="p-2 mb-2 bg-red-50 border border-red-200 text-red-700 rounded-input text-[11px] font-medium flex items-center gap-1.5">
                  <AlertTriangle className="w-3.5 h-3.5 flex-shrink-0" />
                  <span>{uploadError}</span>
                </div>
              )}

              {formData.qrisImageUri ? (
                <div className="bg-slate-50 rounded-input p-3 border border-slate-200 space-y-2.5">
                  <div className="flex items-center justify-center p-2 bg-white rounded-lg border border-slate-200">
                    <img
                      src={formData.qrisImageUri}
                      alt="QRIS Merchant"
                      className="max-h-48 max-w-full object-contain rounded"
                    />
                  </div>

                  <div className="flex items-center gap-2">
                    <button
                      type="button"
                      onClick={() => {
                        const srcToCrop = originalRawImage || formData.qrisImageUri;
                        if (srcToCrop) {
                          setRawImageForCrop(srcToCrop);
                          setIsCropOpen(true);
                        }
                      }}
                      className="h-9 px-3 rounded-input bg-brand-primary/10 hover:bg-brand-primary/20 text-brand-primary text-xs font-semibold flex items-center gap-1.5 transition-all"
                      title="Sesuaikan potongan kotak QRIS"
                    >
                      <Crop className="w-3.5 h-3.5" />
                      <span>Potong Ulang</span>
                    </button>

                    <label className="flex-1 h-9 px-3 rounded-input bg-slate-200 hover:bg-slate-300 text-slate-700 text-xs font-semibold flex items-center justify-center gap-1.5 cursor-pointer transition-all">
                      <Upload className="w-3.5 h-3.5" />
                      <span>Ganti Gambar</span>
                      <input
                        type="file"
                        accept="image/jpeg,image/png,image/webp"
                        onChange={handleQrisFileChange}
                        className="hidden"
                      />
                    </label>

                    <button
                      type="button"
                      onClick={() => {
                        setFormData((prev) => ({
                          ...prev,
                          qrisImageUri: null,
                          qrisImagePath: ''
                        }));
                        setOriginalRawImage(null);
                      }}
                      className="h-9 px-3 rounded-input bg-red-50 hover:bg-red-100 text-red-600 text-xs font-semibold flex items-center gap-1.5 border border-red-200 transition-all"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                      <span>Hapus</span>
                    </button>
                  </div>
                </div>
              ) : (
                <label className="border-2 border-dashed border-slate-300 hover:border-brand-primary rounded-input p-4 flex flex-col items-center justify-center text-center cursor-pointer transition-all hover:bg-blue-50/30">
                  <div className="w-10 h-10 rounded-full bg-brand-sky/20 flex items-center justify-center text-brand-primary mb-1.5">
                    <Upload className="w-5 h-5" />
                  </div>
                  <span className="text-xs font-semibold text-slate-800">
                    Upload Kode QRIS Toko
                  </span>
                  <span className="text-[11px] text-slate-400 mt-0.5">
                    Format PNG, JPG, WebP (Maks. 2MB)
                  </span>
                  <input
                    type="file"
                    accept="image/jpeg,image/png,image/webp"
                    onChange={handleQrisFileChange}
                    className="hidden"
                  />
                </label>
              )}
            </div>
          </div>
        </div>

        {/* 3. Modal Awal Kasir */}
        <div className="bg-white rounded-card p-4 border border-slate-200 shadow-soft space-y-3">
          <div className="flex items-center gap-2 text-brand-primary pb-2 border-b border-slate-100 font-bold text-xs tracking-wider uppercase">
            <Wallet className="w-4 h-4" />
            <span>Modal Awal Kasir</span>
          </div>

          <div>
            <label className="text-xs font-medium text-slate-700 block mb-1">Uang Kas Awal di Laci (Rp)</label>
            <input
              type="number"
              value={formData.openingCapital || ''}
              onChange={(e) => setFormData({ ...formData, openingCapital: Number(e.target.value) || 0 })}
              className="w-full h-11 px-3.5 rounded-input bg-slate-100 text-slate-800 text-xs focus:bg-white focus:ring-1 focus:ring-brand-primary focus:outline-none"
              placeholder="0"
            />
          </div>
        </div>

        {/* 4. Pesan Footer Struk */}
        <div className="bg-white rounded-card p-4 border border-slate-200 shadow-soft space-y-3">
          <div className="flex items-center gap-2 text-brand-primary pb-2 border-b border-slate-100 font-bold text-xs tracking-wider uppercase">
            <Receipt className="w-4 h-4" />
            <span>Catatan Struk Pembelian</span>
          </div>

          <div>
            <label className="text-xs font-medium text-slate-700 block mb-1">Pesan Bawah Struk (Footer)</label>
            <input
              type="text"
              value={formData.receiptFooter}
              onChange={(e) => setFormData({ ...formData, receiptFooter: e.target.value })}
              className="w-full h-11 px-3.5 rounded-input bg-slate-100 text-slate-800 text-xs focus:bg-white focus:ring-1 focus:ring-brand-primary focus:outline-none"
            />
          </div>
        </div>

        {/* 5. Tema Aplikasi */}
        <div className="bg-white rounded-card p-4 border border-slate-200 shadow-soft space-y-3">
          <div className="flex items-center gap-2 text-brand-primary pb-2 border-b border-slate-100 font-bold text-xs tracking-wider uppercase">
            <Sun className="w-4 h-4" />
            <span>Tema Tampilan</span>
          </div>

          <div className="space-y-2">
            {[
              { id: 'LIGHT', label: 'Mode Terang', icon: Sun },
              { id: 'DARK', label: 'Mode Gelap', icon: Moon },
              { id: 'SYSTEM', label: 'Ikuti Sistem', icon: Monitor }
            ].map((t) => {
              const Icon = t.icon;
              const isSelected = formData.themeMode === t.id;
              return (
                <button
                  key={t.id}
                  onClick={() => setFormData({ ...formData, themeMode: t.id as any })}
                  className={`w-full p-3 rounded-input flex items-center justify-between text-xs font-semibold transition-all border ${
                    isSelected
                      ? 'bg-blue-50/80 border-brand-primary text-brand-primary'
                      : 'bg-slate-50 border-slate-200 text-slate-700 hover:bg-slate-100'
                  }`}
                >
                  <div className="flex items-center gap-3">
                    <Icon className="w-4 h-4" />
                    <span>{t.label}</span>
                  </div>
                  <div className={`w-4 h-4 rounded-full border flex items-center justify-center ${isSelected ? 'border-brand-primary bg-brand-primary text-white' : 'border-slate-300'}`}>
                    {isSelected && <span className="w-1.5 h-1.5 rounded-full bg-white" />}
                  </div>
                </button>
              );
            })}
          </div>
        </div>

        {/* 5. Ukuran Tampilan Layar (Viewport) */}
        <div className="bg-white rounded-card p-4 border border-slate-200 shadow-soft space-y-3">
          <div className="flex items-center gap-2 text-brand-primary pb-2 border-b border-slate-100 font-bold text-xs tracking-wider uppercase">
            <Smartphone className="w-4 h-4" />
            <span>Ukuran Tampilan Layar (Viewport)</span>
          </div>
          <p className="text-[11px] text-slate-500">
            Pilih lebar tampilan layar Android untuk menyesuaikan simulasi tampilan perangkat:
          </p>
          <div className="grid grid-cols-3 gap-2">
            {[
              { id: '360dp', label: '360dp', sub: 'Ringkas', desc: 'HP Kecil' },
              { id: '412dp', label: '412dp', sub: 'Standar', desc: 'Pixel/Galaxy' },
              { id: '430dp', label: '430dp', sub: 'Lega', desc: 'Layar Lebar' }
            ].map((opt) => {
              const isSelected = (formData.viewportWidth || '412dp') === opt.id;
              return (
                <button
                  key={opt.id}
                  type="button"
                  onClick={() => setFormData({ ...formData, viewportWidth: opt.id as any })}
                  className={`p-3 rounded-input text-left border transition-all flex flex-col justify-between ${
                    isSelected
                      ? 'bg-blue-50/80 border-brand-primary text-brand-primary shadow-xs'
                      : 'bg-slate-50 border-slate-200 text-slate-700 hover:bg-slate-100'
                  }`}
                >
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-bold">{opt.label}</span>
                    <div
                      className={`w-3.5 h-3.5 rounded-full border flex items-center justify-center ${
                        isSelected ? 'border-brand-primary bg-brand-primary text-white' : 'border-slate-300'
                      }`}
                    >
                      {isSelected && <span className="w-1 h-1 rounded-full bg-white" />}
                    </div>
                  </div>
                  <div className="mt-1">
                    <div className="text-[11px] font-semibold">{opt.sub}</div>
                    <div className="text-[10px] text-slate-400 font-normal">{opt.desc}</div>
                  </div>
                </button>
              );
            })}
          </div>
        </div>

        {/* Save button (CTA 48dp) */}
        <button
          onClick={handleSave}
          className="w-full h-12 rounded-input bg-brand-primary hover:bg-brand-deep text-white text-xs font-bold flex items-center justify-center gap-2 shadow-md transition-all active:scale-[0.99]"
        >
          <Check className="w-4 h-4" />
          <span>Simpan Seluruh Pengaturan</span>
        </button>

        {/* 6. Manajemen Data */}
        <div className="bg-white rounded-card p-4 border border-slate-200 shadow-soft space-y-3">
          <div className="flex items-center gap-2 text-brand-primary pb-2 border-b border-slate-100 font-bold text-xs tracking-wider uppercase">
            <Shield className="w-4 h-4" />
            <span>Manajemen Data</span>
          </div>

          <div className="space-y-2">
            <button
              onClick={() => {
                const blob = new Blob([JSON.stringify(formData, null, 2)], { type: 'application/json' });
                const url = URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = `backup-kelola-${new Date().toISOString().slice(0, 10)}.json`;
                a.click();
              }}
              className="w-full h-11 rounded-input border border-slate-200 bg-slate-50 hover:bg-slate-100 text-slate-700 text-xs font-semibold flex items-center justify-center gap-2 transition-all"
            >
              <Download className="w-4 h-4" />
              <span>Cadangkan Data (Export JSON)</span>
            </button>

            <button
              onClick={() => setShowResetConfirm(true)}
              className="w-full h-11 rounded-input border border-red-200 bg-danger-container hover:bg-red-200 text-danger text-xs font-semibold flex items-center justify-center gap-2 transition-all"
            >
              <RotateCcw className="w-4 h-4" />
              <span>Reset Database ke Bawaan</span>
            </button>

            {/* Version Footer */}
            <div className="pt-3 pb-1 flex items-center justify-center gap-1.5 text-slate-400 text-xs">
              <Info className="w-3.5 h-3.5" />
              <span>Kelola • Versi 2.5 (Offline Local POS)</span>
            </div>
          </div>
        </div>
      </div>
    </div>

      {/* Reset Confirmation */}
      {showResetConfirm && (
        <ConfirmationDialog
          title="Reset Database ke Bawaan?"
          message="PERINGATAN: Semua data transaksi, produk, dan kasbon akan dikembalikan ke data contoh bawaan."
          confirmText="Hapus & Reset"
          isDestructive={true}
          onConfirm={() => {
            onResetAllData();
            setShowResetConfirm(false);
          }}
          onDismiss={() => setShowResetConfirm(false)}
        />
      )}

      {/* QRIS Crop Modal */}
      {isCropOpen && rawImageForCrop && (
        <QrisCropDialog
          imageSrc={rawImageForCrop}
          onCropComplete={(croppedDataUrl) => {
            setFormData((prev) => ({
              ...prev,
              qrisImageUri: croppedDataUrl,
              qrisImagePath: prev.qrisImagePath || 'qris_merchant.png'
            }));
            setIsCropOpen(false);
            setRawImageForCrop(null);
          }}
          onCancel={() => {
            setIsCropOpen(false);
            setRawImageForCrop(null);
          }}
        />
      )}
    </div>
  );
};
