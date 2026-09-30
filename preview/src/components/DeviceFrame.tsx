import React, { useState, useEffect } from 'react';
import { Smartphone, Monitor, Moon, Sun, RotateCw, Wifi, Battery, Signal, ArrowLeft, Tablet } from 'lucide-react';
import { KelolaLogoBadge } from './KelolaLogo';

export type DevicePreset = 'small' | 'standard' | 'large' | 'tablet-p' | 'tablet-l' | 'responsive';

interface DeviceSize {
  width: number | string;
  height: number | string;
  label: string;
}

const DEVICE_SIZES: Record<DevicePreset, DeviceSize> = {
  small: { width: 360, height: 740, label: 'Small Android (360×740)' },
  standard: { width: 412, height: 892, label: 'Standard Android (412×892)' },
  large: { width: 430, height: 932, label: 'Large Android (430×932)' },
  'tablet-p': { width: 768, height: 1024, label: 'Tablet Portrait (768×1024)' },
  'tablet-l': { width: 1024, height: 768, label: 'Tablet Landscape (1024×768)' },
  responsive: { width: '100%', height: '100%', label: 'Responsif Penuh' },
};

interface DeviceFrameProps {
  children: React.ReactNode;
  isDark: boolean;
  onToggleTheme: () => void;
  onNavigateBack?: () => void;
  canNavigateBack?: boolean;
  viewportWidth?: '360dp' | '412dp' | '430dp';
  onViewportWidthChange?: (width: '360dp' | '412dp' | '430dp') => void;
}

export const DeviceFrame: React.FC<DeviceFrameProps> = ({
  children,
  isDark,
  onToggleTheme,
  onNavigateBack,
  canNavigateBack = false,
  viewportWidth,
  onViewportWidthChange
}) => {
  const [preset, setPreset] = useState<DevicePreset>('standard');
  const [time, setTime] = useState('12:00');

  // Sync with viewportWidth setting
  useEffect(() => {
    if (viewportWidth === '360dp') setPreset('small');
    else if (viewportWidth === '412dp') setPreset('standard');
    else if (viewportWidth === '430dp') setPreset('large');
  }, [viewportWidth]);

  useEffect(() => {
    const updateTime = () => {
      const now = new Date();
      setTime(now.toLocaleTimeString('id-ID', { hour: '2-digit', minute: '2-digit' }).replace(/\./g, ':'));
    };
    updateTime();
    const interval = setInterval(updateTime, 10000);
    return () => clearInterval(interval);
  }, []);

  const size = DEVICE_SIZES[preset];
  const isFullscreen = preset === 'responsive';

  return (
    <div className="flex flex-col h-screen w-screen bg-slate-950 text-slate-100 overflow-hidden select-none">
      {/* Top Companion Bar */}
      <header className="h-14 border-b border-slate-800 bg-slate-900/95 backdrop-blur-md px-4 flex items-center justify-between flex-shrink-0 z-40">
        <div className="flex items-center gap-3">
          <KelolaLogoBadge size={32} />
          <div>
            <div className="flex items-center gap-2">
              <span className="font-bold text-sm text-white tracking-tight">Kelola</span>
              <span className="text-[10px] font-semibold bg-brand-primary/30 text-brand-sky border border-brand-primary/40 px-1.5 py-0.5 rounded">
                Android UI Preview
              </span>
            </div>
            <p className="text-[11px] text-slate-400 hidden sm:block">
              Jetpack Compose Mirror • Localhost Companion
            </p>
          </div>
        </div>

        {/* Controls */}
        <div className="flex items-center gap-2">
          {/* Preset Buttons */}
          <div className="bg-slate-800 p-0.5 rounded-lg flex items-center gap-0.5 border border-slate-700/60 overflow-x-auto max-w-[500px]">
            {(['small', 'standard', 'large', 'tablet-p', 'tablet-l', 'responsive'] as DevicePreset[]).map((p) => (
              <button
                key={p}
                onClick={() => setPreset(p)}
                className={`px-2 py-1 text-[11px] font-medium rounded-md whitespace-nowrap transition-all ${
                  preset === p
                    ? 'bg-brand-primary text-white font-semibold shadow-xs'
                    : 'text-slate-400 hover:text-slate-200 hover:bg-slate-700/50'
                }`}
                title={DEVICE_SIZES[p].label}
              >
                {p === 'small' && '360dp'}
                {p === 'standard' && '412dp'}
                {p === 'large' && '430dp'}
                {p === 'tablet-p' && 'Tablet (P)'}
                {p === 'tablet-l' && 'Tablet (L)'}
                {p === 'responsive' && (
                  <span className="flex items-center gap-1">
                    <Monitor className="w-3.5 h-3.5" />
                    <span className="hidden sm:inline">Penuh</span>
                  </span>
                )}
              </button>
            ))}
          </div>

          {/* Theme Toggle */}
          <button
            onClick={onToggleTheme}
            className="w-8 h-8 rounded-lg bg-slate-800 border border-slate-700/60 flex items-center justify-center text-slate-300 hover:text-white hover:bg-slate-700 transition-all flex-shrink-0"
            title={isDark ? 'Ganti ke Mode Terang' : 'Ganti ke Mode Gelap'}
          >
            {isDark ? <Sun className="w-4 h-4 text-amber-400" /> : <Moon className="w-4 h-4 text-brand-sky" />}
          </button>

          {/* Refresh Page */}
          <button
            onClick={() => window.location.reload()}
            className="w-8 h-8 rounded-lg bg-slate-800 border border-slate-700/60 flex items-center justify-center text-slate-300 hover:text-white hover:bg-slate-700 transition-all flex-shrink-0"
            title="Muat Ulang Preview"
          >
            <RotateCw className="w-3.5 h-3.5" />
          </button>
        </div>
      </header>

      {/* Main Preview Canvas Area */}
      <main className="flex-1 flex items-center justify-center p-2 sm:p-4 md:p-6 overflow-auto bg-gradient-to-b from-slate-950 via-slate-900 to-slate-950">
        {isFullscreen ? (
          <div className={`w-full h-full flex flex-col shadow-2xl overflow-hidden relative ${isDark ? 'dark bg-slate-900' : 'bg-[#F7F9FF]'}`}>
            {children}
          </div>
        ) : (
          <div
            style={{ width: size.width, height: size.height }}
            className={`relative flex flex-col ${
              preset.startsWith('tablet') ? 'rounded-[28px] ring-[10px]' : 'rounded-[44px] ring-[12px]'
            } ring-slate-800/90 shadow-[0_25px_60px_-15px_rgba(0,0,0,0.7)] border-[4px] border-slate-700/80 overflow-hidden flex-shrink-0 transition-all duration-200 ${
              isDark ? 'dark bg-slate-900' : 'bg-[#F7F9FF]'
            }`}
          >
            {/* Android Status Bar */}
            <div className={`h-8 px-6 flex items-center justify-between text-[12px] font-medium z-30 select-none flex-shrink-0 ${
              isDark ? 'bg-slate-900 text-slate-200' : 'bg-[#F7F9FF] text-slate-800'
            }`}>
              <div className="font-semibold tracking-tight">{time}</div>
              {/* Camera cutout notch */}
              <div className="w-4 h-4 rounded-full bg-black/80 ring-1 ring-slate-700/40 -mt-1" />
              <div className="flex items-center gap-1.5 opacity-85">
                <Signal className="w-3 h-3" />
                <Wifi className="w-3 h-3" />
                <Battery className="w-3.5 h-3.5" />
              </div>
            </div>

            {/* Android Screen Body Viewport */}
            <div className="flex-1 flex flex-col overflow-hidden relative">
              {children}
            </div>

            {/* Android Bottom Gesture Navigation Bar */}
            <div className={`h-5 w-full flex items-center justify-center z-30 flex-shrink-0 ${
              isDark ? 'bg-slate-900' : 'bg-white'
            }`}>
              <div className={`w-32 h-1 rounded-full ${isDark ? 'bg-slate-600' : 'bg-slate-300'}`} />
            </div>
          </div>
        )}
      </main>
    </div>
  );
};
