import React, { useEffect, useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';

interface AxiomSplashProps {
  onComplete: () => void;
}

const EASE_OUT_EXPO = [0.16, 1, 0.3, 1] as [number, number, number, number];

export const AxiomSplash: React.FC<AxiomSplashProps> = ({ onComplete }) => {
  const [isVisible, setIsVisible] = useState(true);

  useEffect(() => {
    // 1.5s animation sequence + 350ms exit transition = ~1.85s total
    const timer = setTimeout(() => {
      setIsVisible(false);
    }, 1500);

    return () => clearTimeout(timer);
  }, []);

  const letterContainerVariants = {
    hidden: { opacity: 0 },
    visible: {
      opacity: 1,
      transition: {
        staggerChildren: 0.08,
        delayChildren: 0.5
      }
    }
  };

  const letterVariants = {
    hidden: { opacity: 0, y: 5, scale: 0.9 },
    visible: { 
      opacity: 1, 
      y: 0, 
      scale: 1,
      transition: { duration: 0.4, ease: EASE_OUT_EXPO } 
    }
  };

  return (
    <AnimatePresence onExitComplete={onComplete}>
      {isVisible && (
        <motion.div
          initial={{ opacity: 1 }}
          exit={{ opacity: 0, scale: 1.01 }}
          transition={{ duration: 0.35, ease: EASE_OUT_EXPO }}
          className="fixed inset-0 z-[100] flex flex-col items-center justify-center bg-[#020617] overflow-hidden"
        >
          {/* Central Radial Glows */}
          <div className="absolute w-[450px] h-[450px] rounded-full bg-cyan-500/10 blur-[130px] pointer-events-none" />
          <div className="absolute w-[350px] h-[350px] rounded-full bg-violet-500/5 blur-[110px] pointer-events-none" />

          {/* Shimmer sweep effect */}
          <motion.div
            initial={{ x: '-150%' }}
            animate={{ x: '150%' }}
            transition={{ duration: 1.3, delay: 0.2, ease: 'easeInOut' }}
            className="absolute top-0 bottom-0 w-[40%] bg-gradient-to-r from-transparent via-cyan-500/5 to-transparent skew-x-12 pointer-events-none"
          />

          <div className="flex flex-col items-center gap-7 relative z-10">
            {/* Animated Logo Symbol */}
            <div className="relative">
              <svg
                width="140"
                height="70"
                viewBox="0 0 124 60"
                fill="none"
                xmlns="http://www.w3.org/2000/svg"
                className="drop-shadow-[0_0_20px_rgba(6,182,212,0.45)] text-[#06B6D4]"
              >
                {/* Left Bracket */}
                <motion.path
                  d="M20 12 L8 30 L20 48"
                  stroke="currentColor"
                  strokeWidth="4.5"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  initial={{ pathLength: 0 }}
                  animate={{ pathLength: 1 }}
                  transition={{ duration: 0.75, ease: EASE_OUT_EXPO }}
                />

                {/* Left Colon */}
                <motion.circle
                  cx="28"
                  cy="22"
                  r="3.5"
                  fill="currentColor"
                  initial={{ scale: 0 }}
                  animate={{ scale: 1 }}
                  transition={{ delay: 0.3, type: 'spring', stiffness: 200 }}
                />
                <motion.circle
                  cx="28"
                  cy="38"
                  r="3.5"
                  fill="currentColor"
                  initial={{ scale: 0 }}
                  animate={{ scale: 1 }}
                  transition={{ delay: 0.35, type: 'spring', stiffness: 200 }}
                />

                {/* Central A Main Legs */}
                <motion.path
                  d="M38 48 L60 12 L82 48"
                  stroke="currentColor"
                  strokeWidth="4.5"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  initial={{ pathLength: 0 }}
                  animate={{ pathLength: 1 }}
                  transition={{ delay: 0.2, duration: 0.85, ease: EASE_OUT_EXPO }}
                />

                {/* Central A Crossbar */}
                <motion.path
                  d="M49 33 L71 33"
                  stroke="currentColor"
                  strokeWidth="4.5"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  initial={{ pathLength: 0, opacity: 0 }}
                  animate={{ pathLength: 1, opacity: 1 }}
                  transition={{ delay: 0.45, duration: 0.5, ease: 'easeOut' }}
                />

                {/* Right Colon */}
                <motion.circle
                  cx="92"
                  cy="22"
                  r="3.5"
                  fill="currentColor"
                  initial={{ scale: 0 }}
                  animate={{ scale: 1 }}
                  transition={{ delay: 0.3, type: 'spring', stiffness: 200 }}
                />
                <motion.circle
                  cx="92"
                  cy="38"
                  r="3.5"
                  fill="currentColor"
                  initial={{ scale: 0 }}
                  animate={{ scale: 1 }}
                  transition={{ delay: 0.35, type: 'spring', stiffness: 200 }}
                />

                {/* Right Bracket */}
                <motion.path
                  d="M100 12 L112 30 L100 48"
                  stroke="currentColor"
                  strokeWidth="4.5"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  initial={{ pathLength: 0 }}
                  animate={{ pathLength: 1 }}
                  transition={{ duration: 0.75, ease: EASE_OUT_EXPO }}
                />

                {/* Cursor Block */}
                <motion.rect
                  x="116"
                  y="25"
                  width="4.5"
                  height="10"
                  rx="0.5"
                  fill="currentColor"
                  initial={{ opacity: 0 }}
                  animate={{ opacity: [0, 1, 0.3, 1] }}
                  transition={{ delay: 0.6, duration: 0.6, repeat: Infinity, repeatType: 'reverse' }}
                />
              </svg>
            </div>

            {/* Cinematic Letter-by-Letter Reveal */}
            <div className="text-center space-y-2 select-none">
              <motion.h1
                variants={letterContainerVariants}
                initial="hidden"
                animate="visible"
                className="flex justify-center gap-1.5 font-black tracking-[0.35em] text-2xl uppercase pl-[0.35em] bg-gradient-to-b from-[#E2E8F0] to-[#94A3B8] bg-clip-text text-transparent"
              >
                {['A', 'X', 'I', 'O', 'M'].map((char, index) => (
                  <motion.span key={index} variants={letterVariants}>
                    {char}
                  </motion.span>
                ))}
              </motion.h1>

              {/* Tagline Fade-In */}
              <motion.p
                initial={{ opacity: 0, y: 4 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.9, duration: 0.7, ease: 'easeOut' }}
                className="text-[9px] text-[#06B6D4] font-mono tracking-[0.25em] uppercase pl-[0.25em]"
              >
                Analyze. Execute. Evolve.
              </motion.p>
            </div>
          </div>
        </motion.div>
      )}
    </AnimatePresence>
  );
};
