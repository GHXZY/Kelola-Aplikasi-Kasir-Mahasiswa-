import React, { useState, useMemo } from 'react';
import { Plus, Minus, Edit, Trash2, PackagePlus, TrendingDown, Tag, X } from 'lucide-react';
import { ProductEntity, CategoryEntity } from '../types';
import { formatRupiah } from '../utils/format';
import { SearchField, CategoryChipGroup, StockBadge, EditorialCategoryTag, ConfirmationDialog } from '../components/CommonComponents';

interface ProductsScreenProps {
  products: ProductEntity[];
  categories: CategoryEntity[];
  onOpenAddProduct: () => void;
  onOpenEditProduct: (product: ProductEntity) => void;
  onOpenRestock: (product: ProductEntity) => void;
  onOpenReduceStock: (product: ProductEntity) => void;
  onOpenAddExpense: () => void;
  onOpenPromo: () => void;
  onDeleteProduct: (productId: number) => void;
}

export const ProductsScreen: React.FC<ProductsScreenProps> = ({
  products,
  categories,
  onOpenAddProduct,
  onOpenEditProduct,
  onOpenRestock,
  onOpenReduceStock,
  onOpenAddExpense,
  onOpenPromo,
  onDeleteProduct
}) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('Semua');
  const [productToDelete, setProductToDelete] = useState<ProductEntity | null>(null);
  const [isFabOpen, setIsFabOpen] = useState(false);

  const categoryNames = useMemo(() => {
    return ['Semua', ...categories.map((c) => c.name)];
  }, [categories]);

  const categoryMap = useMemo(() => {
    return new Map(categories.map((c) => [c.id, c.name]));
  }, [categories]);

  const filteredProducts = useMemo(() => {
    return products.filter((p) => {
      const catName = categoryMap.get(p.categoryId) || '';
      const matchSearch =
        !searchQuery ||
        p.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
        catName.toLowerCase().includes(searchQuery.toLowerCase());

      const matchCategory =
        selectedCategory === 'Semua' || catName.toLowerCase() === selectedCategory.toLowerCase();

      return matchSearch && matchCategory;
    });
  }, [products, searchQuery, selectedCategory, categoryMap]);

  return (
    <div className="flex-1 flex flex-col overflow-hidden bg-[#F7F9FF] relative">
      {/* Top Header */}
      <div className="p-4 pb-2 space-y-3 bg-white border-b border-slate-200/80 flex-shrink-0">
        <div className="max-w-6xl mx-auto space-y-3">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-base font-bold text-slate-800 leading-tight">Manajemen Produk</h1>
              <p className="text-xs text-slate-500 font-medium">Total {products.length} barang terdaftar</p>
            </div>
          </div>

          {/* Search & Category Filter */}
          <SearchField
            query={searchQuery}
            onQueryChange={setSearchQuery}
            placeholder="Cari nama barang atau kategori..."
          />
          <CategoryChipGroup
            categories={categoryNames}
            selectedCategory={selectedCategory}
            onSelectCategory={setSelectedCategory}
          />
        </div>
      </div>

      {/* Product List */}
      <div className="flex-1 overflow-y-auto p-4 pb-24 md:p-6">
        <div className="max-w-6xl mx-auto">
          {filteredProducts.length === 0 ? (
            <div className="bg-white rounded-card p-8 text-center border border-slate-200 text-slate-400 text-sm">
              Tidak ada produk yang cocok dengan filter saat ini.
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3 space-y-0">
              {filteredProducts.map((p) => {
            const catName = categoryMap.get(p.categoryId) || 'Umum';
            const initials = p.name.slice(0, 2).toUpperCase();

            return (
              <div
                key={p.id}
                data-testid={`product_item_${p.id}`}
                className="bg-white rounded-card p-3.5 border border-slate-200 shadow-soft flex flex-col gap-2.5 transition-all hover:border-slate-300"
              >
                <div className="flex items-start justify-between gap-2">
                  <div className="flex items-start gap-3">
                    <div className="w-11 h-11 rounded-input bg-slate-100 flex items-center justify-center flex-shrink-0 font-bold text-brand-primary text-sm">
                      {initials}
                    </div>
                    <div>
                      <div className="flex items-center gap-2 mb-0.5">
                        <EditorialCategoryTag text={catName} />
                      </div>
                      <h3 className="text-sm font-semibold text-slate-800 line-clamp-1">{p.name}</h3>
                      <div className="text-xs text-slate-400 mt-0.5">
                        Modal: {formatRupiah(p.costPrice)} • Jual:{' '}
                        <span className="font-bold text-brand-primary text-[13px]">
                          {formatRupiah(p.sellingPrice)}
                        </span>
                      </div>
                    </div>
                  </div>

                  <StockBadge stock={p.stock} minimumStock={p.minimumStock} unit={p.unit} />
                </div>

                {/* Bottom Actions Row (Tinggi 40dp) */}
                <div className="pt-2 border-t border-slate-100 flex items-center justify-between gap-1">
                  <div className="flex items-center gap-1.5">
                    <button
                      onClick={() => onOpenRestock(p)}
                      data-testid={`button_restock_${p.id}`}
                      className="w-9 h-9 rounded-input bg-emerald-50 hover:bg-emerald-100 text-emerald-700 flex items-center justify-center transition-all"
                      title="Tambah Stok"
                    >
                      <Plus className="w-4 h-4" />
                    </button>

                    <button
                      onClick={() => onOpenReduceStock(p)}
                      data-testid={`button_reduce_stock_${p.id}`}
                      className="w-9 h-9 rounded-input bg-amber-50 hover:bg-amber-100 text-amber-700 flex items-center justify-center transition-all"
                      title="Kurangi Stok"
                    >
                      <Minus className="w-4 h-4" />
                    </button>
                  </div>

                  <div className="flex items-center gap-1">
                    <button
                      onClick={() => onOpenEditProduct(p)}
                      data-testid={`button_edit_product_${p.id}`}
                      className="w-9 h-9 rounded-input text-slate-500 hover:text-brand-primary hover:bg-slate-100 flex items-center justify-center transition-all"
                      title="Edit Produk"
                    >
                      <Edit className="w-4 h-4" />
                    </button>

                    <button
                      onClick={() => setProductToDelete(p)}
                      data-testid={`button_delete_product_${p.id}`}
                      className="w-9 h-9 rounded-input text-slate-400 hover:text-danger hover:bg-red-50 flex items-center justify-center transition-all"
                      title="Hapus Produk"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  </div>

      {/* Delete Confirmation */}
      {productToDelete && (
        <ConfirmationDialog
          title={`Hapus ${productToDelete.name}?`}
          message="Produk ini akan dihapus dari daftar katalog toko Anda. Riwayat transaksi sebelumnya tetap tercatat."
          confirmText="Hapus Produk"
          isDestructive={true}
          onConfirm={() => {
            onDeleteProduct(productToDelete.id);
            setProductToDelete(null);
          }}
          onDismiss={() => setProductToDelete(null)}
        />
      )}

      {/* FAB Backdrop */}
      {isFabOpen && (
        <div
          className="absolute inset-0 bg-slate-900/40 z-30 transition-opacity backdrop-blur-[1px]"
          onClick={() => setIsFabOpen(false)}
        />
      )}

      {/* Floating Action Buttons (FABs) Speed Dial */}
      <div className="absolute right-4 bottom-5 z-40 flex flex-col items-end gap-2.5">
        {isFabOpen && (
          <div className="flex flex-col items-end gap-2.5 mb-1">
            {/* 1. Promo */}
            <div className="flex items-center gap-2">
              <span className="bg-white text-slate-800 text-xs font-semibold px-2.5 py-1 rounded-full shadow-md border border-slate-100">
                Promo & Bundling
              </span>
              <button
                onClick={() => {
                  setIsFabOpen(false);
                  onOpenPromo();
                }}
                data-testid="fab_action_promo"
                className="w-11 h-11 rounded-full bg-brand-primary text-white flex items-center justify-center shadow-lg hover:bg-brand-deep transition-all transform active:scale-95"
                title="Kelola Promo"
              >
                <Tag className="w-5 h-5" />
              </button>
            </div>

            {/* 2. Pengeluaran */}
            <div className="flex items-center gap-2">
              <span className="bg-white text-rose-700 text-xs font-semibold px-2.5 py-1 rounded-full shadow-md border border-slate-100">
                Catat Pengeluaran
              </span>
              <button
                onClick={() => {
                  setIsFabOpen(false);
                  onOpenAddExpense();
                }}
                data-testid="fab_action_expense"
                className="w-11 h-11 rounded-full bg-rose-500 text-white flex items-center justify-center shadow-lg hover:bg-rose-600 transition-all transform active:scale-95"
                title="Catat Pengeluaran"
              >
                <TrendingDown className="w-5 h-5" />
              </button>
            </div>

            {/* 3. Tambah Produk */}
            <div className="flex items-center gap-2">
              <span className="bg-white text-slate-800 text-xs font-semibold px-2.5 py-1 rounded-full shadow-md border border-slate-100">
                Tambah Produk
              </span>
              <button
                onClick={() => {
                  setIsFabOpen(false);
                  onOpenAddProduct();
                }}
                data-testid="fab_action_add_product"
                className="w-11 h-11 rounded-full bg-brand-primary text-white flex items-center justify-center shadow-lg hover:bg-brand-deep transition-all transform active:scale-95"
                title="Tambah Produk"
              >
                <PackagePlus className="w-5 h-5" />
              </button>
            </div>
          </div>
        )}

        {/* Main Floating Toggle Button */}
        <button
          onClick={() => setIsFabOpen(!isFabOpen)}
          data-testid="fab_main_toggle"
          className={`w-14 h-14 rounded-full text-white flex items-center justify-center shadow-xl transition-all transform active:scale-90 ${
            isFabOpen ? 'bg-slate-700 rotate-90' : 'bg-brand-primary hover:bg-brand-deep'
          }`}
          title="Menu Aksi Cepat"
        >
          {isFabOpen ? <X className="w-6 h-6" /> : <Plus className="w-6 h-6" />}
        </button>
      </div>
    </div>
  );
};
