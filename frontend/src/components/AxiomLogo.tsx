import React from 'react';

interface AxiomLogoProps {
  className?: string;
  iconClassName?: string;
  size?: 'xs' | 'sm' | 'md' | 'lg' | 'xl';
  showText?: boolean;
  textPosition?: 'right' | 'bottom';
  showSubtitle?: boolean;
  glow?: boolean;
}

export const AxiomLogo: React.FC<AxiomLogoProps> = ({
  className = '',
  iconClassName = '',
  size = 'md',
  showText = true,
  textPosition = 'right',
  showSubtitle = false,
  glow = true
}) => {
  // Sizing maps
  const iconSizes = {
    xs: 'w-6 h-3',
    sm: 'w-10 h-5',
    md: 'w-16 h-8',
    lg: 'w-24 h-12',
    xl: 'w-36 h-18'
  };

  const textSizes = {
    xs: 'text-xs tracking-wider',
    sm: 'text-sm tracking-widest',
    md: 'text-lg tracking-[0.25em]',
    lg: 'text-2xl tracking-[0.35em]',
    xl: 'text-4xl tracking-[0.45em]'
  };

  const subtitleSizes = {
    xs: 'text-[7px] tracking-[0.1em]',
    sm: 'text-[9px] tracking-[0.15em]',
    md: 'text-[10px] tracking-[0.2em]',
    lg: 'text-xs tracking-[0.25em]',
    xl: 'text-sm tracking-[0.3em]'
  };

  const glowStyle = glow 
    ? 'drop-shadow-[0_0_12px_rgba(6,182,212,0.65)]' 
    : '';

  const renderIcon = () => (
    <svg
      viewBox="0 0 124 60"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      className={`${iconSizes[size]} ${glowStyle} text-[#06B6D4] transition-all duration-300 ${iconClassName}`}
    >
      {/* Left bracket: < */}
      <path
        d="M20 12 L8 30 L20 48"
        stroke="currentColor"
        strokeWidth="4.5"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      {/* Left colon: : */}
      <circle cx="28" cy="22" r="3" fill="currentColor" />
      <circle cx="28" cy="38" r="3" fill="currentColor" />

      {/* Central "A" */}
      <path
        d="M38 48 L60 12 L82 48"
        stroke="currentColor"
        strokeWidth="4.5"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <path
        d="M49 33 L71 33"
        stroke="currentColor"
        strokeWidth="4.5"
        strokeLinecap="round"
        strokeLinejoin="round"
      />

      {/* Right colon: : */}
      <circle cx="92" cy="22" r="3" fill="currentColor" />
      <circle cx="92" cy="38" r="3" fill="currentColor" />

      {/* Right bracket: > */}
      <path
        d="M100 12 L112 30 L100 48"
        stroke="currentColor"
        strokeWidth="4.5"
        strokeLinecap="round"
        strokeLinejoin="round"
      />

      {/* Cursor block attached to the right of the right bracket */}
      <rect
        x="116"
        y="25"
        width="4"
        height="10"
        rx="0.5"
        fill="currentColor"
      />
    </svg>
  );

  if (textPosition === 'bottom') {
    return (
      <div className={`flex flex-col items-center justify-center gap-4 text-center ${className}`}>
        {renderIcon()}
        {showText && (
          <div className="flex flex-col items-center gap-1.5">
            <h1 className={`font-black text-[#06B6D4] uppercase select-none ${textSizes[size]}`}>
              AXIOM
            </h1>
            {showSubtitle && (
              <span className={`font-mono text-slate-400 select-none ${subtitleSizes[size]}`}>
                &gt; CODE.TRUTH.REPEAT
              </span>
            )}
          </div>
        )}
      </div>
    );
  }

  return (
    <div className={`flex items-center gap-3 ${className}`}>
      {renderIcon()}
      {showText && (
        <div className="flex flex-col justify-center">
          <span className={`font-black text-foreground hover:text-[#06B6D4] transition-colors select-none ${textSizes[size]}`}>
            AXIOM
          </span>
          {showSubtitle && (
            <span className={`font-mono text-slate-400 text-left select-none ${subtitleSizes[size]}`}>
              &gt; CODE.TRUTH.REPEAT
            </span>
          )}
        </div>
      )}
    </div>
  );
};
