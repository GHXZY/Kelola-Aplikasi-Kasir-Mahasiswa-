import React, { useState, useMemo } from 'react';
import { Plus, Minus, X, ArrowRight, ShoppingCart, ShoppingBag, PackageOpen, Trash2 } from 'lucide-react';
import { ProductEntity, CategoryEntity, CartSummary } from '../types';
import { formatRupiah } from '../utils/format';
import { SearchField, CategoryChipGroup, StockBadge, ConfirmationDialog } from '../components/CommonComponents';

interface CashierScreenProps {
  products: ProductEntity[];
  categories: CategoryEntity[];
  cart: CartSummary;
  onAddToCart: (product: ProductEntity) => void;
  onUpdateQuantity: (productId: number, qty: number) => void;
  onRemoveFromCart: (productId: number) => void;
  onClearCart: () => void;
  onOpenCart: () => void;
  onProceedToPayment?: () => void;
}

export const CashierScreen: React.FC<CashierScreenProps> = ({
  products,
  categories,
  cart,
  onAddToCart,
  onUpdateQuantity,
  onRemoveFromCart,
  onClearCart,
  onOpenCart,
  onProceedToPayment
}) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('Semua');
  const [showCancelDialog, setShowCancelDialog] = useState(false);

  const categoryNames = useMemo(() => {
    return ['Semua', ...categories.map((c) => c.name)];
  }, [categories]);

  const categoryMap = useMemo(() => {
    return new Map(categories.map((c) => [c.id, c.name]));
  }, [categories]);

  const cartQtyMap = useMemo(() => {
    const map = new Map<number, number>();
    cart.items.forEach((item) => map.set(item.product.id, item.quantity));
    return map;
  }, [cart]);

  const filteredProducts = useMemo(() => {
    return products.filter((prod) => {
      const catName = categoryMap.get(prod.categoryId) || '';
      const matchSearch =
        !searchQuery ||
        prod.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
        catName.toLowerCase().includes(searchQuery.toLowerCase());

      const matchCategory =
        selectedCategory === 'Semua' || catName.toLowerCase() === selectedCategory.toLowerCase();

      return matchSearch && matchCategory;
    });
  }, [products, searchQuery, selectedCategory, categoryMap]);

  return (
    <div className="flex-1 flex flex-col lg:flex-row overflow-hidden relative bg-[#F7F9FF]">
      {/* Left Column: Product Catalog Area */}
      <div className="flex-1 flex flex-col overflow-hidden relative min-w-0">
        {/* Header Search & Category Filter */}
        <div className="p-4 pb-2 space-y-2.5 bg-white border-b border-slate-200/80 flex-shrink-0">
          <SearchField
            query={searchQuery}
            onQueryChange={setSearchQuery}
            placeholder="Cari produk atau kategori..."
          />
          <CategoryChipGroup
            categories={categoryNames}
            selectedCategory={selectedCategory}
            onSelectCategory={setSelectedCategory}
          />
        </div>

        {/* Product Grid Area */}
        <div className="flex-1 overflow-y-auto p-4 pb-36 lg:pb-6">
          {filteredProducts.length === 0 ? (
            <div className="flex flex-col items-center justify-center p-12 text-center text-slate-400">
              <PackageOpen className="w-12 h-12 mb-2 stroke-1" />
              <h3 className="font-semibold text-slate-700 text-sm">Produk Tidak Ditemukan</h3>
              <p className="text-xs text-slate-400 mt-1">Coba kata kunci atau kategori lain.</p>
            </div>
          ) : (
            <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-3 xl:grid-cols-4 gap-3">
              {filteredProducts.map((product) => {
                const qtyInCart = cartQtyMap.get(product.id) || 0;
                const isOutOfStock = product.stock <= 0;
                const catName = categoryMap.get(product.categoryId) || 'Umum';
                const initials = product.name.slice(0, 2).toUpperCase();

                return (
                  <div
                    key={product.id}
                    data-testid={`product_pos_card_${product.id}`}
                    className={`rounded-card p-3.5 border transition-all flex flex-col justify-between ${
                      qtyInCart > 0
                        ? 'bg-blue-50/60 border-brand-sky/70 shadow-sm ring-1 ring-brand-sky/40'
                        : 'bg-white border-slate-200 shadow-soft hover:border-slate-300'
                    }`}
                  >
                    <div>
                      {/* Top Row: Category + Cancel Badge if in cart */}
                      <div className="flex items-center justify-between gap-1 mb-2">
                        <span className="text-[11px] font-medium text-slate-400 truncate">
                          {catName}
                        </span>
                        {qtyInCart > 0 && (
                          <button
                            onClick={() => onRemoveFromCart(product.id)}
                            data-testid={`button_cancel_product_${product.id}`}
                            className="h-6 px-1.5 rounded-chip bg-danger-container hover:bg-red-200 text-danger text-[10px] font-semibold flex items-center gap-0.5 transition-all"
                            title="Hapus dari keranjang"
                          >
                            <X className="w-3 h-3" />
                            <span>Batal</span>
                          </button>
                        )}
                      </div>

                      {/* Product visual container */}
                      <div className="w-full h-16 rounded-input bg-slate-100 flex items-center justify-center mb-2.5">
                        <span className="text-base font-bold text-brand-primary">{initials}</span>
                      </div>

                      {/* Product Info */}
                      <h3 className="text-[13px] font-semibold text-slate-800 line-clamp-1 leading-snug mb-0.5">
                        {product.name}
                      </h3>
                      <div className="text-[14px] font-bold text-brand-primary mb-1.5">
                        {formatRupiah(product.sellingPrice)}
                      </div>
                      <StockBadge stock={product.stock} minimumStock={product.minimumStock} unit={product.unit} />
                    </div>

                    {/* Inline Cart Action (Tinggi 40dp) */}
                    <div className="mt-3">
                      {qtyInCart > 0 ? (
                        <div className="flex items-center justify-between bg-white rounded-full p-1 border border-brand-sky/60 shadow-xs">
                          <button
                            onClick={() => onUpdateQuantity(product.id, qtyInCart - 1)}
                            data-testid={`button_decrease_cart_${product.id}`}
                            className="w-8 h-8 rounded-full bg-slate-100 hover:bg-slate-200 flex items-center justify-center text-slate-700 active:scale-95 transition-all"
                          >
                            <Minus className="w-3.5 h-3.5" />
                          </button>
                          <span className="text-sm font-bold text-brand-primary px-1">
                            {qtyInCart}
                          </span>
                          <button
                            onClick={() => {
                              if (qtyInCart < product.stock) {
                                onUpdateQuantity(product.id, qtyInCart + 1);
                              }
                            }}
                            disabled={qtyInCart >= product.stock}
                            data-testid={`button_increase_cart_${product.id}`}
                            className={`w-8 h-8 rounded-full flex items-center justify-center text-white active:scale-95 transition-all ${
                              qtyInCart >= product.stock
                                ? 'bg-slate-300 text-slate-500 cursor-not-allowed'
                                : 'bg-brand-primary hover:bg-brand-deep'
                            }`}
                          >
                            <Plus className="w-3.5 h-3.5" />
                          </button>
                        </div>
                      ) : (
                        <button
                          onClick={() => onAddToCart(product)}
                          disabled={isOutOfStock}
                          data-testid={`button_add_to_cart_${product.id}`}
                          className={`w-full h-10 rounded-input text-xs font-semibold flex items-center justify-center gap-1 transition-all ${
                            isOutOfStock
                              ? 'bg-slate-100 text-slate-400 cursor-not-allowed'
                              : 'bg-brand-primary/10 hover:bg-brand-primary text-brand-primary hover:text-white active:scale-[0.98]'
                          }`}
                        >
                          <Plus className="w-3.5 h-3.5" />
                          <span>{isOutOfStock ? 'Habis' : 'Pilih'}</span>
                        </button>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>

        {/* Sticky Bottom Cart Bar for Mobile & Tablet Portrait (ShapeSheet 16px, CTA 52dp) */}
        {cart.totalItemCount > 0 && (
          <div className="lg:hidden absolute bottom-2 left-3 right-3 z-30 animate-slideUp">
            <div className="bg-white rounded-sheet p-3.5 border border-slate-200 shadow-sheet flex flex-col gap-3 max-w-xl mx-auto">
              <div className="flex items-center justify-between">
                <div>
                  <div className="flex items-center gap-1.5">
                    <span className="w-2 h-2 rounded-full bg-emerald-500" />
                    <span className="text-xs font-semibold text-slate-500">Total Pesanan</span>
                  </div>
                  <div className="text-xs text-slate-500 mt-0.5 font-medium">
                    {cart.totalItemCount} barang dipilih
                  </div>
                  {cart.promoDiscount > 0 && (
                    <span className="inline-block mt-0.5 px-1.5 py-0.5 rounded text-[10px] font-bold bg-success-container text-success-text">
                      Hemat: -{formatRupiah(cart.promoDiscount)}
                    </span>
                  )}
                </div>

                <div className="text-right">
                  {cart.promoDiscount > 0 && (
                    <div className="text-[11px] text-slate-400 line-through">
                      {formatRupiah(cart.subtotal)}
                    </div>
                  )}
                  <div className="text-lg font-bold text-brand-primary">
                    {formatRupiah(cart.total)}
                  </div>
                </div>
              </div>

              {/* Actions: Secondary (48dp) + CTA (52dp) */}
              <div className="flex items-center gap-2">
                <button
                  onClick={() => setShowCancelDialog(true)}
                  data-testid="button_cancel_order_bar"
                  className="h-12 flex-1 rounded-input border border-red-200 bg-danger-container hover:bg-red-100 text-danger text-xs font-semibold flex items-center justify-center gap-1 transition-all"
                >
                  <X className="w-4 h-4" />
                  <span>Batal Beli</span>
                </button>

                <button
                  onClick={onOpenCart}
                  data-testid="button_open_cart_bar"
                  className="h-[52px] flex-[1.4] rounded-input bg-brand-primary hover:bg-brand-deep text-white text-sm font-semibold flex items-center justify-center gap-2 shadow-md active:scale-[0.98] transition-all"
                >
                  <span>Bayar</span>
                  <ArrowRight className="w-4 h-4" />
                </button>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Right Column: Tablet Landscape Persistent Cart Sidebar (lg:flex) */}
      <div className="hidden lg:flex w-80 xl:w-96 flex-col bg-white border-l border-slate-200 shadow-sm z-20 flex-shrink-0">
        {/* Cart Header */}
        <div className="p-4 border-b border-slate-100 flex items-center justify-between flex-shrink-0">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-full bg-brand-sky/30 text-brand-primary flex items-center justify-center">
              <ShoppingCart className="w-4 h-4" />
            </div>
            <div>
              <h2 className="text-sm font-bold text-slate-800">Keranjang Kasir</h2>
              <p className="text-[11px] text-slate-400">{cart.totalItemCount} barang dipilih</p>
            </div>
          </div>
          {cart.totalItemCount > 0 && (
            <button
              onClick={() => setShowCancelDialog(true)}
              className="text-[11px] font-semibold text-danger hover:text-red-700 hover:bg-danger-container px-2 py-1 rounded transition-colors"
            >
              Kosongkan
            </button>
          )}
        </div>

        {/* Cart Items List */}
        <div className="flex-1 overflow-y-auto p-3 space-y-2.5">
          {cart.items.length === 0 ? (
            <div className="h-full flex flex-col items-center justify-center p-6 text-center text-slate-400">
              <ShoppingBag className="w-10 h-10 mb-2 stroke-1 text-slate-300" />
              <p className="text-xs font-semibold text-slate-600">Keranjang Kosong</p>
              <p className="text-[11px] text-slate-400 mt-0.5">Pilih produk di katalog untuk menambahkan.</p>
            </div>
          ) : (
            cart.items.map(({ product, quantity }) => (
              <div
                key={product.id}
                className="flex items-center justify-between p-2.5 rounded-card bg-slate-50 border border-slate-200/80 hover:border-slate-300 transition-all"
              >
                <div className="flex-1 min-w-0 pr-2">
                  <h4 className="text-xs font-semibold text-slate-800 truncate">{product.name}</h4>
                  <div className="text-[11px] text-slate-400 mt-0.5">
                    {formatRupiah(product.sellingPrice)} × {quantity}
                  </div>
                  <div className="text-xs font-bold text-brand-primary mt-0.5">
                    {formatRupiah(product.sellingPrice * quantity)}
                  </div>
                </div>

                {/* Increment / Decrement Counter */}
                <div className="flex items-center gap-1 flex-shrink-0">
                  <button
                    onClick={() => {
                      if (quantity <= 1) {
                        onRemoveFromCart(product.id);
                      } else {
                        onUpdateQuantity(product.id, quantity - 1);
                      }
                    }}
                    className="w-7 h-7 rounded-full bg-white border border-slate-200 flex items-center justify-center text-slate-700 hover:bg-slate-100 active:scale-95"
                  >
                    {quantity <= 1 ? <Trash2 className="w-3 h-3 text-danger" /> : <Minus className="w-3 h-3" />}
                  </button>
                  <span className="w-5 text-center text-xs font-bold text-slate-800">{quantity}</span>
                  <button
                    onClick={() => {
                      if (quantity < product.stock) onUpdateQuantity(product.id, quantity + 1);
                    }}
                    disabled={quantity >= product.stock}
                    className={`w-7 h-7 rounded-full flex items-center justify-center text-white active:scale-95 ${
                      quantity >= product.stock
                        ? 'bg-slate-200 text-slate-400 cursor-not-allowed'
                        : 'bg-brand-primary hover:bg-brand-deep'
                    }`}
                  >
                    <Plus className="w-3 h-3" />
                  </button>
                </div>
              </div>
            ))
          )}
        </div>

        {/* Order Breakdown & Bayar CTA */}
        {cart.totalItemCount > 0 && (
          <div className="p-4 bg-slate-50 border-t border-slate-200 flex-shrink-0 space-y-3">
            <div className="space-y-1 text-xs">
              <div className="flex justify-between text-slate-500">
                <span>Subtotal:</span>
                <span className="font-semibold text-slate-700">{formatRupiah(cart.subtotal)}</span>
              </div>
              {cart.promoDiscount > 0 && (
                <div className="flex justify-between text-emerald-600 font-semibold">
                  <span>Promo Hemat:</span>
                  <span>-{formatRupiah(cart.promoDiscount)}</span>
                </div>
              )}
              <div className="flex justify-between text-sm font-bold text-slate-900 pt-1.5 border-t border-slate-200">
                <span>Total:</span>
                <span className="text-brand-primary text-base">{formatRupiah(cart.total)}</span>
              </div>
            </div>

            <div className="flex gap-2">
              <button
                onClick={() => setShowCancelDialog(true)}
                className="h-11 px-3 rounded-input border border-red-200 bg-danger-container hover:bg-red-100 text-danger text-xs font-semibold flex items-center justify-center gap-1 transition-all"
                title="Batalkan Seluruh Pembelian"
              >
                <X className="w-4 h-4" />
              </button>
              <button
                onClick={onProceedToPayment || onOpenCart}
                className="flex-1 h-11 rounded-input bg-brand-primary hover:bg-brand-deep text-white text-xs font-bold flex items-center justify-center gap-1.5 shadow-md active:scale-[0.98] transition-all"
              >
                <span>Bayar Sekarang</span>
                <ArrowRight className="w-4 h-4" />
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Cancel Order Confirmation Modal */}
      {showCancelDialog && (
        <ConfirmationDialog
          title="Batalkan Seluruh Pembelian?"
          message="Semua barang yang telah Anda masukkan ke keranjang kasir akan dikosongkan."
          confirmText="Kosongkan Keranjang"
          isDestructive={true}
          onConfirm={() => {
            onClearCart();
            setShowCancelDialog(false);
          }}
          onDismiss={() => setShowCancelDialog(false)}
        />
      )}
    </div>
  );
};
