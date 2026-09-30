import React from 'react';
import { X, Plus, Minus, Trash2, ArrowRight, ShoppingBag, Tag } from 'lucide-react';
import { CartSummary } from '../types';
import { formatRupiah } from '../utils/format';

interface CartSheetProps {
  cart: CartSummary;
  onUpdateQuantity: (productId: number, qty: number) => void;
  onRemoveFromCart: (productId: number) => void;
  onProceedToPayment: () => void;
  onDismiss: () => void;
}

export const CartSheet: React.FC<CartSheetProps> = ({
  cart,
  onUpdateQuantity,
  onRemoveFromCart,
  onProceedToPayment,
  onDismiss
}) => {
  return (
    <div className="fixed inset-0 z-50 flex flex-col justify-end bg-black/60 backdrop-blur-xs animate-fadeIn">
      {/* Backdrop tap to close */}
      <div className="flex-1" onClick={onDismiss} />

      {/* Modal Bottom Sheet Container (ShapeSheet 16px) */}
      <div className="bg-white rounded-t-sheet border-t border-slate-200 shadow-2xl flex flex-col max-h-[85vh] animate-slideUp w-full md:max-w-lg md:mx-auto md:rounded-card md:mb-6 md:border">
        {/* Sheet Drag Pill & Header */}
        <div className="p-4 pb-2 border-b border-slate-100 flex-shrink-0">
          <div className="w-12 h-1 bg-slate-300 rounded-full mx-auto mb-3" />
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 rounded-full bg-brand-sky/30 text-brand-primary flex items-center justify-center">
                <ShoppingBag className="w-4 h-4" />
              </div>
              <div>
                <h2 className="text-base font-bold text-slate-800">Keranjang Kasir</h2>
                <p className="text-xs text-slate-400">{cart.totalItemCount} barang dipilih</p>
              </div>
            </div>

            <button
              onClick={onDismiss}
              className="w-8 h-8 rounded-full bg-slate-100 flex items-center justify-center text-slate-500 hover:text-slate-800"
            >
              <X className="w-4 h-4" />
            </button>
          </div>
        </div>

        {/* Cart Items List */}
        <div className="flex-1 overflow-y-auto p-4 space-y-3">
          {cart.items.map(({ product, quantity }) => (
            <div
              key={product.id}
              className="flex items-center justify-between p-3 rounded-card bg-slate-50 border border-slate-200/80"
            >
              <div className="flex-1 min-w-0 pr-2">
                <h4 className="text-xs font-semibold text-slate-800 truncate">{product.name}</h4>
                <div className="text-[11px] text-slate-400 mt-0.5">
                  {formatRupiah(product.sellingPrice)} × {quantity}
                </div>
                <div className="text-xs font-bold text-brand-primary mt-1">
                  {formatRupiah(product.sellingPrice * quantity)}
                </div>
              </div>

              {/* Counter Buttons */}
              <div className="flex items-center gap-1.5 flex-shrink-0">
                <button
                  onClick={() => {
                    if (quantity <= 1) {
                      onRemoveFromCart(product.id);
                    } else {
                      onUpdateQuantity(product.id, quantity - 1);
                    }
                  }}
                  className="w-8 h-8 rounded-full bg-white border border-slate-200 flex items-center justify-center text-slate-700 hover:bg-slate-100 active:scale-95"
                >
                  {quantity <= 1 ? <Trash2 className="w-3.5 h-3.5 text-danger" /> : <Minus className="w-3.5 h-3.5" />}
                </button>

                <span className="w-6 text-center text-xs font-bold text-slate-800">{quantity}</span>

                <button
                  onClick={() => {
                    if (quantity < product.stock) {
                      onUpdateQuantity(product.id, quantity + 1);
                    }
                  }}
                  disabled={quantity >= product.stock}
                  className={`w-8 h-8 rounded-full flex items-center justify-center text-white active:scale-95 ${
                    quantity >= product.stock
                      ? 'bg-slate-200 text-slate-400 cursor-not-allowed'
                      : 'bg-brand-primary hover:bg-brand-deep'
                  }`}
                >
                  <Plus className="w-3.5 h-3.5" />
                </button>
              </div>
            </div>
          ))}
        </div>

        {/* Order Breakdown & CTA */}
        <div className="p-4 bg-slate-50 border-t border-slate-200 flex-shrink-0 space-y-3">
          <div className="space-y-1 text-xs">
            <div className="flex justify-between text-slate-500">
              <span>Subtotal:</span>
              <span className="font-semibold text-slate-700">{formatRupiah(cart.subtotal)}</span>
            </div>

            {cart.promoDiscount > 0 && (
              <div className="flex justify-between text-emerald-600 font-semibold">
                <span className="flex items-center gap-1">
                  <Tag className="w-3 h-3" />
                  <span>Promo Hemat:</span>
                </span>
                <span>-{formatRupiah(cart.promoDiscount)}</span>
              </div>
            )}

            <div className="flex justify-between text-sm font-bold text-slate-900 pt-1 border-t border-slate-200">
              <span>Total Pembayaran:</span>
              <span className="text-brand-primary text-base">{formatRupiah(cart.total)}</span>
            </div>
          </div>

          <button
            onClick={onProceedToPayment}
            className="w-full h-12 rounded-input bg-brand-primary hover:bg-brand-deep text-white text-sm font-bold flex items-center justify-center gap-2 shadow-md active:scale-[0.99] transition-all"
          >
            <span>Lanjut ke Pembayaran</span>
            <ArrowRight className="w-4 h-4" />
          </button>
        </div>
      </div>
    </div>
  );
};
