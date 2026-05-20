import React from 'react';
import { motion } from 'framer-motion';
import { Zap, Sparkles } from 'lucide-react';

interface LoadingStateProps {
  status?: string;
}

export const LoadingState: React.FC<LoadingStateProps> = ({ status = 'Analyzing Code Architecture' }) => {
  return (
    <div className="flex flex-col items-center justify-center p-12 space-y-6 animate-in fade-in duration-500">
      <div className="relative">
        <motion.div
          animate={{ rotate: 360 }}
          transition={{ duration: 2, repeat: Infinity, ease: "linear" }}
          className="w-24 h-24 border-4 border-primary/20 border-t-primary rounded-full shadow-[0_0_30px_rgba(59,130,246,0.3)]"
        />
        <div className="absolute inset-0 flex items-center justify-center">
          <Zap className="w-8 h-8 text-primary animate-pulse" />
        </div>
      </div>
      
      <div className="text-center">
        <h3 className="text-xl font-bold tracking-tight mb-2 flex items-center justify-center gap-2">
          <Sparkles className="w-5 h-5 text-yellow-500" />
          {status}
        </h3>
        <p className="text-sm text-muted-foreground max-w-xs mx-auto animate-pulse">
          Please wait while our FAANG engine compiles reviews, patterns, and complexity analysis...
        </p>
      </div>

      <div className="flex gap-1">
        {[0, 1, 2].map((i) => (
          <motion.div
            key={i}
            animate={{ scale: [1, 1.5, 1], opacity: [0.3, 1, 0.3] }}
            transition={{ duration: 1, repeat: Infinity, delay: i * 0.2 }}
            className="w-2 h-2 bg-primary rounded-full"
          />
        ))}
      </div>
    </div>
  );
};
