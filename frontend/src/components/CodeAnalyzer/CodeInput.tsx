import React, { useState, useEffect } from 'react';
import Editor from '@monaco-editor/react';
import { Button, Card } from '../ui/core';
import { Play, Code2, ClipboardList, Trash2 } from 'lucide-react';

interface CodeInputProps {
  onAnalyze: (code: string, language: string, model: string) => void;
  isLoading: boolean;
}

export const CodeInput: React.FC<CodeInputProps> = ({ onAnalyze, isLoading }) => {
  const [code, setCode] = useState(() => localStorage.getItem('last_code') || '// Paste your code here...\n\n');
  const [language, setLanguage] = useState('typescript');
  const [aiModel, setAiModel] = useState('AUTO');

  useEffect(() => {
    localStorage.setItem('last_code', code);
  }, [code]);

  const languages = [
    { label: 'TypeScript', value: 'typescript' },
    { label: 'JavaScript', value: 'javascript' },
    { label: 'Java', value: 'java' },
    { label: 'Python', value: 'python' },
    { label: 'C++', value: 'cpp' },
  ];

  const models = [
    { label: 'AUTO (Smart Fallback)', value: 'AUTO' },
    { label: 'GROQ (LLaMA-3 Fast)', value: 'GROQ' },
    { label: 'OPENAI (GPT-4o-mini)', value: 'OPENAI' },
  ];

  return (
    <div className="flex flex-col h-full space-y-4 animate-in fade-in duration-500">
      <div className="flex items-center justify-between bg-secondary/20 p-3 rounded-xl border border-white/5">
        <div className="flex items-center gap-4">
          <div className="flex items-center gap-2">
            <Code2 className="w-4 h-4 text-primary" />
            <select 
              value={language}
              onChange={(e) => setLanguage(e.target.value)}
              className="bg-transparent text-xs font-bold uppercase tracking-wider focus:outline-none cursor-pointer hover:text-primary transition-colors"
            >
              {languages.map(lang => (
                <option key={lang.value} value={lang.value} className="bg-[#1e1e1e]">{lang.label}</option>
              ))}
            </select>
          </div>
          <div className="h-4 w-px bg-white/10" />
          <div className="flex items-center gap-2">
            <select 
              value={aiModel}
              onChange={(e) => setAiModel(e.target.value)}
              className="bg-transparent text-xs font-bold uppercase tracking-wider focus:outline-none cursor-pointer text-purple-400 hover:text-purple-300 transition-colors"
            >
              {models.map(model => (
                <option key={model.value} value={model.value} className="bg-[#1e1e1e]">{model.label}</option>
              ))}
            </select>
          </div>
          <div className="h-4 w-px bg-white/10" />
          <div className="text-[10px] text-muted-foreground uppercase tracking-widest font-medium">
            {code.split('\n').length} Lines
          </div>
        </div>
        
        <div className="flex items-center gap-2">
          <Button 
            variant="ghost" 
            size="sm" 
            className="h-8 text-[10px] gap-2 hover:bg-red-500/10 hover:text-red-400"
            onClick={() => setCode('')}
          >
            <Trash2 className="w-3 h-3" />
            Clear
          </Button>
          <Button 
            variant="primary" 
            size="sm" 
            className="h-8 text-[10px] gap-2 px-6 rounded-full"
            disabled={code.length < 10 || isLoading}
            onClick={() => onAnalyze(code, language, aiModel)}
          >
            <Play className="w-3 h-3 fill-current" />
            Analyze Code
          </Button>
        </div>
      </div>

      <Card className="flex-1 p-0 overflow-hidden border-white/5 bg-[#0e0e11] shadow-2xl relative min-h-[400px]">
        <Editor
          height="100%"
          language={language}
          theme="vs-dark"
          value={code}
          onChange={(val) => setCode(val || '')}
          options={{
            fontSize: 14,
            fontFamily: 'JetBrains Mono, Fira Code, monospace',
            minimap: { enabled: false },
            scrollBeyondLastLine: false,
            automaticLayout: true,
            padding: { top: 20, bottom: 20 },
            smoothScrolling: true,
            cursorBlinking: 'smooth',
            cursorSmoothCaretAnimation: 'on',
            lineNumbers: 'on',
            renderLineHighlight: 'all',
          }}
        />
        {code.length < 10 && (
          <div className="absolute inset-0 flex items-center justify-center pointer-events-none opacity-20 flex-col gap-4">
            <ClipboardList className="w-16 h-16" />
            <p className="text-sm font-medium tracking-widest uppercase">Waiting for Input</p>
          </div>
        )}
      </Card>
    </div>
  );
};
