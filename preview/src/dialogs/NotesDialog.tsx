import React, { useState, useMemo } from 'react';
import {
  X,
  Plus,
  Search,
  FileText,
  Trash2,
  Edit2,
  Check,
  Calendar,
  Tag
} from 'lucide-react';
import { NoteEntity } from '../types';
import { formatDateTime } from '../utils/format';
import { ConfirmationDialog } from '../components/CommonComponents';

interface NotesDialogProps {
  notes: NoteEntity[];
  onSaveNote: (noteData: Partial<NoteEntity>) => void;
  onDeleteNote: (noteId: number) => void;
  onDismiss: () => void;
}

const CATEGORIES = ['Semua', 'Umum', 'Transaksi', 'Stok', 'Kembalian', 'Keuangan'];

export const NotesDialog: React.FC<NotesDialogProps> = ({
  notes,
  onSaveNote,
  onDeleteNote,
  onDismiss
}) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('Semua');
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [editingNote, setEditingNote] = useState<NoteEntity | null>(null);

  // Form Fields
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [category, setCategory] = useState('Umum');

  const [noteToDelete, setNoteToDelete] = useState<NoteEntity | null>(null);

  const handleOpenAdd = () => {
    setEditingNote(null);
    setTitle('');
    setContent('');
    setCategory('Umum');
    setIsFormOpen(true);
  };

  const handleOpenEdit = (note: NoteEntity) => {
    setEditingNote(note);
    setTitle(note.title);
    setContent(note.content);
    setCategory(note.category || 'Umum');
    setIsFormOpen(true);
  };

  const handleSubmitForm = (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim() || !content.trim()) return;

    onSaveNote({
      ...(editingNote ? { id: editingNote.id } : {}),
      title: title.trim(),
      content: content.trim(),
      category
    });

    setIsFormOpen(false);
    setEditingNote(null);
    setTitle('');
    setContent('');
  };

  const filteredNotes = useMemo(() => {
    return notes
      .filter((n) => {
        const matchesCategory =
          selectedCategory === 'Semua' || n.category === selectedCategory;
        const matchesSearch =
          n.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
          n.content.toLowerCase().includes(searchQuery.toLowerCase());
        return matchesCategory && matchesSearch;
      })
      .sort((a, b) => b.updatedAt - a.updatedAt);
  }, [notes, selectedCategory, searchQuery]);

  return (
    <div className="fixed inset-0 z-50 flex flex-col justify-end bg-black/60 backdrop-blur-xs animate-fadeIn">
      <div className="flex-1" onClick={onDismiss} />

      <div className="bg-white rounded-t-sheet border-t border-slate-200 shadow-2xl flex flex-col max-h-[92vh] animate-slideUp w-full md:max-w-xl md:mx-auto md:rounded-card md:mb-6 md:border">
        {/* Header */}
        <div className="p-4 pb-3 border-b border-slate-100 flex items-center justify-between flex-shrink-0">
          <div className="flex items-center gap-2.5">
            <div className="w-9 h-9 rounded-full bg-brand-sky/20 flex items-center justify-center text-brand-primary">
              <FileText className="w-4 h-4" />
            </div>
            <div>
              <h2 className="text-base font-bold text-slate-800">Catatan & Riwayat Toko</h2>
              <p className="text-[11px] text-slate-400">Pengingat operasional, pesanan, dan titipan</p>
            </div>
          </div>

          <div className="flex items-center gap-1">
            {!isFormOpen && (
              <button
                onClick={handleOpenAdd}
                className="h-8 px-3 rounded-input bg-brand-primary hover:bg-brand-deep text-white text-xs font-semibold flex items-center gap-1 shadow-xs transition-all"
              >
                <Plus className="w-3.5 h-3.5" />
                <span>Catatan</span>
              </button>
            )}
            <button
              onClick={onDismiss}
              className="w-8 h-8 rounded-full bg-slate-100 flex items-center justify-center text-slate-500 hover:text-slate-800"
            >
              <X className="w-4 h-4" />
            </button>
          </div>
        </div>

        {/* Content Body */}
        <div className="flex-1 overflow-y-auto p-4 space-y-3">
          {isFormOpen ? (
            /* Form Add/Edit */
            <form onSubmit={handleSubmitForm} className="space-y-3 bg-slate-50 rounded-card p-4 border border-slate-200 animate-fadeIn">
              <div className="flex items-center justify-between pb-2 border-b border-slate-200">
                <span className="text-xs font-bold text-slate-800">
                  {editingNote ? 'Edit Catatan Toko' : 'Tulis Catatan Baru'}
                </span>
                <button
                  type="button"
                  onClick={() => setIsFormOpen(false)}
                  className="text-xs text-slate-500 hover:text-slate-700"
                >
                  Batal
                </button>
              </div>

              <div>
                <label className="text-[11px] font-semibold text-slate-700 block mb-1">
                  Judul Catatan *
                </label>
                <input
                  type="text"
                  required
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  placeholder="Misal: Pesan Beras ke Toko Jaya"
                  className="w-full h-10 px-3 rounded-input bg-white text-slate-800 text-xs focus:ring-1 focus:ring-brand-primary focus:outline-none border border-slate-200"
                />
              </div>

              <div>
                <label className="text-[11px] font-semibold text-slate-700 block mb-1">
                  Kategori
                </label>
                <div className="flex flex-wrap gap-1.5">
                  {CATEGORIES.filter((c) => c !== 'Semua').map((c) => (
                    <button
                      type="button"
                      key={c}
                      onClick={() => setCategory(c)}
                      className={`px-2.5 py-1 rounded-chip text-[11px] font-semibold border transition-all ${
                        category === c
                          ? 'bg-brand-primary border-brand-primary text-white shadow-xs'
                          : 'bg-white border-slate-200 text-slate-600 hover:bg-slate-100'
                      }`}
                    >
                      {c}
                    </button>
                  ))}
                </div>
              </div>

              <div>
                <label className="text-[11px] font-semibold text-slate-700 block mb-1">
                  Isi Catatan *
                </label>
                <textarea
                  required
                  rows={4}
                  value={content}
                  onChange={(e) => setContent(e.target.value)}
                  placeholder="Tuliskan isi catatan penting di sini..."
                  className="w-full p-3 rounded-input bg-white text-slate-800 text-xs focus:ring-1 focus:ring-brand-primary focus:outline-none border border-slate-200 resize-none"
                />
              </div>

              <div className="flex gap-2 pt-1">
                <button
                  type="button"
                  onClick={() => setIsFormOpen(false)}
                  className="flex-1 h-10 rounded-input border border-slate-200 text-slate-600 text-xs font-semibold hover:bg-white"
                >
                  Batal
                </button>
                <button
                  type="submit"
                  className="flex-1 h-10 rounded-input bg-brand-primary hover:bg-brand-deep text-white text-xs font-bold flex items-center justify-center gap-1.5 shadow-xs"
                >
                  <Check className="w-3.5 h-3.5" />
                  <span>Simpan Catatan</span>
                </button>
              </div>
            </form>
          ) : (
            <>
              {/* Search & Category Filter */}
              <div className="space-y-2">
                <div className="relative">
                  <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
                  <input
                    type="text"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    placeholder="Cari catatan toko..."
                    className="w-full h-10 pl-9 pr-3 rounded-input bg-slate-100 text-slate-800 text-xs focus:bg-white focus:ring-1 focus:ring-brand-primary focus:outline-none"
                  />
                  {searchQuery && (
                    <button
                      onClick={() => setSearchQuery('')}
                      className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
                    >
                      <X className="w-3.5 h-3.5" />
                    </button>
                  )}
                </div>

                <div className="flex items-center gap-1.5 overflow-x-auto no-scrollbar py-0.5">
                  {CATEGORIES.map((c) => (
                    <button
                      key={c}
                      onClick={() => setSelectedCategory(c)}
                      className={`px-2.5 py-1 rounded-chip text-[11px] whitespace-nowrap border transition-all ${
                        selectedCategory === c
                          ? 'bg-brand-primary border-brand-primary text-white font-semibold shadow-xs'
                          : 'bg-white border-slate-200 text-slate-600 hover:bg-slate-50'
                      }`}
                    >
                      {c}
                    </button>
                  ))}
                </div>
              </div>

              {/* Notes List */}
              {filteredNotes.length === 0 ? (
                <div className="p-8 text-center bg-white rounded-card border border-slate-200 space-y-2">
                  <div className="w-12 h-12 rounded-full bg-slate-100 flex items-center justify-center mx-auto text-slate-400">
                    <FileText className="w-6 h-6" />
                  </div>
                  <h4 className="text-xs font-bold text-slate-700">Belum Ada Catatan</h4>
                  <p className="text-[11px] text-slate-400 max-w-xs mx-auto">
                    {searchQuery
                      ? `Tidak ada catatan cocok dengan "${searchQuery}"`
                      : 'Buat catatan pertama Anda untuk mencatat pengingat barang, kembalian, atau keperluan toko.'}
                  </p>
                  <button
                    onClick={handleOpenAdd}
                    className="mt-2 h-9 px-4 rounded-input bg-brand-primary text-white text-xs font-semibold inline-flex items-center gap-1.5"
                  >
                    <Plus className="w-3.5 h-3.5" />
                    <span>Tulis Catatan</span>
                  </button>
                </div>
              ) : (
                <div className="space-y-2.5">
                  {filteredNotes.map((note) => (
                    <div
                      key={note.id}
                      className="bg-white rounded-card p-3.5 border border-slate-200 shadow-soft space-y-2"
                    >
                      <div className="flex items-start justify-between gap-2">
                        <div>
                          <div className="flex items-center gap-2">
                            <h3 className="text-xs font-bold text-slate-900 leading-tight">
                              {note.title}
                            </h3>
                            <span className="px-2 py-0.5 rounded text-[10px] font-semibold bg-brand-sky/20 text-brand-primary">
                              {note.category}
                            </span>
                          </div>
                          <div className="text-[10px] text-slate-400 flex items-center gap-1 mt-0.5">
                            <Calendar className="w-3 h-3" />
                            <span>{formatDateTime(note.updatedAt)}</span>
                          </div>
                        </div>

                        <div className="flex items-center gap-1 flex-shrink-0">
                          <button
                            onClick={() => handleOpenEdit(note)}
                            className="w-7 h-7 rounded-full bg-slate-100 hover:bg-slate-200 flex items-center justify-center text-slate-600 transition-all"
                            title="Edit catatan"
                          >
                            <Edit2 className="w-3.5 h-3.5" />
                          </button>
                          <button
                            onClick={() => setNoteToDelete(note)}
                            className="w-7 h-7 rounded-full bg-red-50 hover:bg-red-100 flex items-center justify-center text-red-600 transition-all"
                            title="Hapus catatan"
                          >
                            <Trash2 className="w-3.5 h-3.5" />
                          </button>
                        </div>
                      </div>

                      <p className="text-xs text-slate-700 whitespace-pre-wrap leading-relaxed bg-slate-50/70 p-2.5 rounded-md border border-slate-100">
                        {note.content}
                      </p>
                    </div>
                  ))}
                </div>
              )}
            </>
          )}
        </div>
      </div>

      {/* Confirmation Delete Dialog */}
      {noteToDelete && (
        <ConfirmationDialog
          title="Hapus Catatan Toko?"
          message={`Apakah Anda yakin ingin menghapus catatan "${noteToDelete.title}"? Catatan yang dihapus tidak dapat dipulihkan.`}
          confirmText="Hapus Catatan"
          isDestructive={true}
          onConfirm={() => {
            onDeleteNote(noteToDelete.id);
            setNoteToDelete(null);
          }}
          onDismiss={() => setNoteToDelete(null)}
        />
      )}
    </div>
  );
};
