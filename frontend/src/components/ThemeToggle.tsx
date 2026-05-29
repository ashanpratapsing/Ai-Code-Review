import React from 'react';
import { Sun, Moon } from 'lucide-react';
import { useTheme } from '../context/ThemeContext';
import { motion } from 'framer-motion';

export const ThemeToggle: React.FC = () => {
  const { theme, toggleTheme } = useTheme();

  return (
    <button
      onClick={toggleTheme}
      className="p-2 rounded-lg border border-border bg-card/40 hover:bg-hover-subtle transition-all duration-300 flex items-center justify-center cursor-pointer text-muted-foreground hover:text-foreground"
      aria-label="Toggle Theme"
    >
      <motion.div
        initial={false}
        animate={{ rotate: theme === 'dark' ? 0 : 180, scale: 1 }}
        transition={{ type: 'spring', stiffness: 200, damping: 15 }}
      >
        {theme === 'dark' ? (
          <Moon className="w-4 h-4 text-primary" />
        ) : (
          <Sun className="w-4 h-4 text-primary" />
        )}
      </motion.div>
    </button>
  );
};
