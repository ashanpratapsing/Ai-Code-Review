import React, { useState, useEffect } from 'react';
import Editor from '@monaco-editor/react';
import { Button, Card, Badge } from '../ui/core';
import { 
  Play, 
  Code2, 
  ClipboardList, 
  Trash2, 
  Plus, 
  Terminal, 
  CheckCircle2, 
  XCircle, 
  AlertTriangle, 
  Loader2 
} from 'lucide-react';
import { codeExecutionService } from '../../services/api';

import type { AnalysisContext, ExecutionResponse } from '../../types';

interface CodeInputProps {
  onAnalyze: (code: string, language: string, model: string, executionContext?: AnalysisContext) => void;
  isLoading: boolean;
  initialCode?: string;
  codeFileId?: number;
}

interface TestCase {
  id: number;
  input: string;
  expectedOutput: string;
}

const JAVA_DEFAULT = `import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        if (sc.hasNextInt()) {
            int a = sc.nextInt();
            int b = sc.nextInt();
            System.out.println(a + b);
        }
    }
}`;

const PYTHON_DEFAULT = `import sys

# Read space-separated values from standard input
lines = sys.stdin.read().split()
if len(lines) >= 2:
    a = int(lines[0])
    b = int(lines[1])
    print(a + b)
`;

const JS_DEFAULT = `const fs = require('fs');

// Read standard input
const input = fs.readFileSync(0, 'utf-8').trim().split(/\\s+/);
if (input.length >= 2) {
    const a = parseInt(input[0], 10);
    const b = parseInt(input[1], 10);
    console.log(a + b);
}
`;

const DEFAULT_TEST_CASES: TestCase[] = [
  { id: 1, input: "5 10", expectedOutput: "15" },
  { id: 2, input: "100 250", expectedOutput: "350" }
];

const detectsInput = (codeText: string, lang: string): boolean => {
  const normalized = codeText.toLowerCase();
  if (lang === 'java') {
    return normalized.includes('scanner') || 
           normalized.includes('system.in') || 
           normalized.includes('bufferedreader') || 
           normalized.includes('inputstreamreader');
  } else if (lang === 'python') {
    return normalized.includes('input(') || 
           normalized.includes('input ()') ||
           normalized.includes('sys.stdin') || 
           normalized.includes('open(0)');
  } else if (lang === 'javascript' || lang === 'typescript') {
    return normalized.includes('readfilesync(') || 
           normalized.includes('readline') || 
           normalized.includes('process.stdin');
  }
  return false;
};

export const CodeInput: React.FC<CodeInputProps> = ({ onAnalyze, isLoading, initialCode, codeFileId }) => {
  const [code, setCode] = useState(() => initialCode || localStorage.getItem('last_code') || JAVA_DEFAULT);
  const [language, setLanguage] = useState('java');
  const [aiModel, setAiModel] = useState('AUTO');

  // Sandbox Testcase & Console states
  const [testCases, setTestCases] = useState<TestCase[]>(DEFAULT_TEST_CASES);
  const [selectedTestCaseId, setSelectedTestCaseId] = useState<number>(1);
  const [activeConsoleTab, setActiveConsoleTab] = useState<'testcases' | 'console'>('testcases');
  const [runResults, setRunResults] = useState<ExecutionResponse | null>(null);
  const [running, setRunning] = useState<boolean>(false);
  const [runError, setRunError] = useState<string | null>(null);

  // Synchronize dynamic code templates when swapping language
  useEffect(() => {
    const currentCode = code.trim();
    if (currentCode === '' || currentCode === JAVA_DEFAULT.trim() || currentCode === PYTHON_DEFAULT.trim() || currentCode === JS_DEFAULT.trim() || currentCode === '// Paste your code here...') {
      if (language === 'java') {
        setCode(JAVA_DEFAULT);
      } else if (language === 'python') {
        setCode(PYTHON_DEFAULT);
      } else if (language === 'javascript' || language === 'typescript') {
        setCode(JS_DEFAULT);
      }
    }
  }, [language]);

  // Handle dynamic testcase resetting based on input detection
  useEffect(() => {
    const hasInput = detectsInput(code, language);
    const isDefaultCode = code.trim() === JAVA_DEFAULT.trim() || 
                          code.trim() === PYTHON_DEFAULT.trim() || 
                          code.trim() === JS_DEFAULT.trim();

    const isDefaultTestCases = 
      testCases.length === 2 &&
      testCases[0].input === "5 10" &&
      testCases[0].expectedOutput === "15" &&
      testCases[1].input === "100 250" &&
      testCases[1].expectedOutput === "350";

    if (isDefaultCode) {
      setTestCases(DEFAULT_TEST_CASES);
    } else if (!hasInput && isDefaultTestCases) {
      // Reset to a single empty test case so code runs without failing on math testcases
      setTestCases([{ id: 1, input: '', expectedOutput: '' }]);
    }
  }, [code, language]);

  useEffect(() => {
    localStorage.setItem('last_code', code);
  }, [code]);

  const languages = [
    { label: 'Java (Temurin 17)', value: 'java' },
    { label: 'Python (3.10)', value: 'python' },
    { label: 'JavaScript (Node 18)', value: 'javascript' },
  ];

  const models = [
    { label: 'AUTO (Smart Fallback)', value: 'AUTO' },
    { label: 'GROQ (LLaMA-3 Fast)', value: 'GROQ' },
    { label: 'OPENAI (GPT-4o-mini)', value: 'OPENAI' },
  ];

  const handleRunCode = async () => {
    setRunning(true);
    setRunError(null);
    setActiveConsoleTab('console');
    try {
      // Map frontend typescript/javascript target value to JS or language expected by backend
      const mappedLang = language === 'javascript' || language === 'typescript' ? 'javascript' : language;
      const res = await codeExecutionService.execute({
        code,
        language: mappedLang,
        testCases,
        codeFileId,
      });
      setRunResults(res.data);
    } catch (e: any) {
      console.error(e);
      setRunError(e.response?.data?.message || 'Failed to establish connection to Sandbox Sandbox Execution Service');
    } finally {
      setRunning(false);
    }
  };

  const handleAddTestCase = () => {
    const nextId = testCases.length > 0 ? Math.max(...testCases.map(tc => tc.id)) + 1 : 1;
    const newCase: TestCase = {
      id: nextId,
      input: '',
      expectedOutput: ''
    };
    setTestCases([...testCases, newCase]);
    setSelectedTestCaseId(nextId);
    setActiveConsoleTab('testcases');
  };

  const handleDeleteTestCase = (id: number) => {
    const filtered = testCases.filter(tc => tc.id !== id);
    setTestCases(filtered);
    if (selectedTestCaseId === id && filtered.length > 0) {
      setSelectedTestCaseId(filtered[0].id);
    }
  };

  const updateTestCase = (id: number, key: 'input' | 'expectedOutput', value: string) => {
    setTestCases(testCases.map(tc => {
      if (tc.id === id) {
        return { ...tc, [key]: value };
      }
      return tc;
    }));
  };

  const activeTestCase = testCases.find(tc => tc.id === selectedTestCaseId);

  return (
    <div className="flex flex-col h-full space-y-4 animate-in fade-in duration-500">
      
      {/* 1. Header Toolbar */}
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

          {/* Interactive Sandbox Run Button */}
          <Button 
            variant="outline" 
            size="sm" 
            className="h-8 text-[10px] gap-2 px-5 rounded-full border-primary/20 hover:bg-primary/10 hover:text-primary"
            disabled={code.length < 10 || running}
            onClick={handleRunCode}
          >
            {running ? <Loader2 className="w-3 h-3 animate-spin" /> : <Play className="w-3 h-3 text-primary fill-primary/20" />}
            Run Code
          </Button>

          {/* Primary AI Review Button */}
          <Button 
            variant="primary" 
            size="sm" 
            className="h-8 text-[10px] gap-2 px-6 rounded-full"
            disabled={code.length < 10 || isLoading}
            onClick={() =>
              onAnalyze(code, language, aiModel, runResults
                ? {
                    executionStatus: runResults.status,
                    compileError: runResults.compileError,
                    executionResults: runResults.results,
                  }
                : undefined)
            }
          >
            <Code2 className="w-3 h-3" />
            Analyze Code
          </Button>
        </div>
      </div>

      {/* 2. Monaco Editor Container */}
      <Card className="flex-1 p-0 overflow-hidden border-white/5 bg-[#0e0e11] shadow-2xl relative min-h-[300px]">
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

      {/* 3. Competitive-Coding Execution Drawer */}
      <div className="h-[280px] border border-white/5 bg-[#0d0d11]/80 backdrop-blur-md rounded-2xl overflow-hidden flex flex-col shadow-2xl">
        
        {/* Terminal Drawer Header */}
        <div className="h-12 border-b border-white/5 bg-white/5 px-4 flex items-center justify-between">
          <div className="flex gap-4">
            <button 
              onClick={() => setActiveConsoleTab('testcases')}
              className={`text-xs font-bold uppercase tracking-wider h-12 flex items-center border-b-2 transition-all gap-2 ${activeConsoleTab === 'testcases' ? 'border-primary text-primary' : 'border-transparent text-muted-foreground hover:text-foreground'}`}
            >
              <ClipboardList className="w-3.5 h-3.5" />
              Testcases ({testCases.length})
            </button>
            <button 
              onClick={() => setActiveConsoleTab('console')}
              className={`text-xs font-bold uppercase tracking-wider h-12 flex items-center border-b-2 transition-all gap-2 ${activeConsoleTab === 'console' ? 'border-primary text-primary' : 'border-transparent text-muted-foreground hover:text-foreground'}`}
            >
              <Terminal className="w-3.5 h-3.5" />
              Console Output
              {running && <Loader2 className="w-3 h-3 animate-spin text-primary" />}
            </button>
          </div>

          {activeConsoleTab === 'testcases' && (
            <Button 
              variant="ghost" 
              size="sm" 
              className="h-7 text-[9px] gap-1 px-3 border border-white/5 hover:bg-white/5 text-primary"
              onClick={handleAddTestCase}
            >
              <Plus className="w-3 h-3" />
              Add Case
            </Button>
          )}
        </div>

        {/* Terminal Body */}
        <div className="flex-1 overflow-hidden flex">
          
          {/* Tab 1: Test Cases Management */}
          {activeConsoleTab === 'testcases' && (
            <div className="flex-1 flex overflow-hidden">
              {/* Cases Sidebar */}
              <div className="w-40 border-r border-white/5 p-2 overflow-y-auto space-y-1 bg-black/10">
                {testCases.map((tc, index) => (
                  <div 
                    key={tc.id}
                    onClick={() => setSelectedTestCaseId(tc.id)}
                    className={`group px-3 py-2 rounded-lg text-xs font-semibold flex items-center justify-between cursor-pointer transition-all ${selectedTestCaseId === tc.id ? 'bg-primary/10 text-primary border border-primary/20' : 'text-muted-foreground hover:text-foreground hover:bg-white/5'}`}
                  >
                    <span>Case {index + 1}</span>
                    <button 
                      onClick={(e) => {
                        e.stopPropagation();
                        handleDeleteTestCase(tc.id);
                      }}
                      className="opacity-0 group-hover:opacity-100 hover:text-red-400 p-0.5 rounded transition-all"
                    >
                      <Trash2 className="w-3 h-3" />
                    </button>
                  </div>
                ))}
                {testCases.length === 0 && (
                  <div className="text-center py-10 text-[10px] italic text-muted-foreground opacity-30">
                    No cases
                  </div>
                )}
              </div>

              {/* Case Editor Fields */}
              <div className="flex-1 p-4 overflow-y-auto bg-black/5">
                {activeTestCase ? (
                  <div className="grid grid-cols-2 gap-4 h-full">
                    {!detectsInput(code, language) && (
                      <div className="col-span-2 px-3 py-2 bg-yellow-500/10 border border-yellow-500/20 text-yellow-400 rounded-lg text-[10px] flex items-center gap-2">
                        <AlertTriangle className="w-3.5 h-3.5 shrink-0" />
                        <span>No standard input usage (like Scanner or sys.stdin) detected in your code. Inputs provided below will be ignored by your program.</span>
                      </div>
                    )}
                    <div className="flex flex-col space-y-2">
                      <label className="text-[10px] uppercase tracking-widest text-muted-foreground font-semibold">Standard Input (stdin)</label>
                      <textarea 
                        value={activeTestCase.input}
                        onChange={(e) => updateTestCase(activeTestCase.id, 'input', e.target.value)}
                        placeholder="Provide standard inputs here..."
                        className="flex-1 w-full bg-[#070709] border border-white/5 rounded-xl p-3 text-xs font-mono focus:outline-none focus:border-primary/55 resize-none text-foreground"
                      />
                    </div>
                    <div className="flex flex-col space-y-2">
                      <label className="text-[10px] uppercase tracking-widest text-muted-foreground font-semibold">Expected Output (stdout)</label>
                      <textarea 
                        value={activeTestCase.expectedOutput}
                        onChange={(e) => updateTestCase(activeTestCase.id, 'expectedOutput', e.target.value)}
                        placeholder="Provide expected matching stdout (leave empty to skip validation)..."
                        className="flex-1 w-full bg-[#070709] border border-white/5 rounded-xl p-3 text-xs font-mono focus:outline-none focus:border-primary/55 resize-none text-foreground"
                      />
                    </div>
                  </div>
                ) : (
                  <div className="h-full flex items-center justify-center flex-col text-muted-foreground opacity-30 gap-2">
                    <ClipboardList className="w-8 h-8" />
                    <p className="text-xs">Add a testcase to configure standard inputs</p>
                  </div>
                )}
              </div>
            </div>
          )}

          {/* Tab 2: Console stdout/stderr Output */}
          {activeConsoleTab === 'console' && (
            <div className="flex-1 p-4 overflow-y-auto bg-[#070709] font-mono text-xs flex flex-col">
              {running && (
                <div className="flex-1 flex flex-col items-center justify-center space-y-3 text-muted-foreground">
                  <Loader2 className="w-8 h-8 animate-spin text-primary" />
                  <p className="text-xs tracking-wider uppercase">Executing code inside secure Docker sandbox...</p>
                </div>
              )}

              {runError && (
                <div className="bg-red-500/10 border border-red-500/20 text-red-400 p-4 rounded-xl flex items-start gap-3">
                  <AlertTriangle className="w-5 h-5 shrink-0" />
                  <div className="space-y-1">
                    <h5 className="font-bold">Sandbox Execution Error</h5>
                    <p className="text-xs">{runError}</p>
                  </div>
                </div>
              )}

              {!running && !runError && runResults && (
                <div className="space-y-4 flex-1">
                  
                  {/* Class Compilation Failure Output (Terminal Style) */}
                  {runResults.status === 'COMPILE_ERROR' && (
                    <div className="flex-1 border border-red-500/20 rounded-xl bg-red-950/20 overflow-hidden">
                      <div className="bg-red-950/40 px-4 py-2 border-b border-red-500/20 text-red-400 font-bold uppercase tracking-widest text-[9px] flex items-center gap-2">
                        <AlertTriangle className="w-3.5 h-3.5" />
                        Compilation Error Output
                      </div>
                      <pre className="p-4 overflow-x-auto text-[11px] text-red-200/90 leading-relaxed font-mono whitespace-pre-wrap">
                        {runResults.compileError}
                      </pre>
                    </div>
                  )}

                  {/* Standard Success Executions */}
                  {runResults.status === 'SUCCESS' && (
                    <div className="space-y-4">
                      {runResults.results?.map((res: any, idx: number) => {
                        const isPassed = res.status === 'PASSED';
                        const isTimeout = res.status === 'TIMEOUT';
                        const isRuntimeError = res.status === 'RUNTIME_ERROR';

                        return (
                          <div key={res.id} className="border border-white/5 rounded-xl bg-[#0f0f13] overflow-hidden">
                            <div className="px-4 py-2 bg-white/5 border-b border-white/5 flex items-center justify-between">
                              <span className="font-bold text-muted-foreground text-[10px] uppercase">Test Case {idx + 1}</span>
                              <div className="flex items-center gap-3">
                                <span className="text-[10px] text-muted-foreground font-semibold">Duration: {res.executionTimeMs} ms</span>
                                {isPassed && <Badge variant="success"><CheckCircle2 className="w-3 h-3 mr-1" /> Passed</Badge>}
                                {res.status === 'FAILED' && <Badge variant="error"><XCircle className="w-3 h-3 mr-1" /> Wrong Answer</Badge>}
                                {isTimeout && <Badge variant="warning"><AlertTriangle className="w-3 h-3 mr-1" /> Timeout</Badge>}
                                {isRuntimeError && <Badge variant="error"><XCircle className="w-3 h-3 mr-1" /> Runtime Error</Badge>}
                              </div>
                            </div>

                            <div className="p-4 grid grid-cols-3 gap-4 text-[11px]">
                              <div>
                                <span className="text-muted-foreground text-[9px] uppercase tracking-wider block mb-1">Standard Input</span>
                                <pre className="bg-black/40 p-2 rounded border border-white/5 font-mono min-h-8 max-h-20 overflow-y-auto">{testCases[idx]?.input || '(empty)'}</pre>
                              </div>
                              <div>
                                <span className="text-muted-foreground text-[9px] uppercase tracking-wider block mb-1">Expected Output</span>
                                <pre className="bg-black/40 p-2 rounded border border-white/5 font-mono min-h-8 max-h-20 overflow-y-auto text-green-400">{res.expectedOutput || '(empty)'}</pre>
                              </div>
                              <div>
                                <span className="text-muted-foreground text-[9px] uppercase tracking-wider block mb-1">Actual Output</span>
                                <pre className={`bg-black/40 p-2 rounded border border-white/5 font-mono min-h-8 max-h-20 overflow-y-auto ${isPassed ? 'text-green-400 font-bold' : isRuntimeError ? 'text-red-400' : 'text-red-400 font-bold'}`}>
                                  {isTimeout ? 'Time Limit Exceeded' : res.actualOutput || (res.error ? res.error : '(empty)')}
                                </pre>
                              </div>
                            </div>
                          </div>
                        );
                      })}
                    </div>
                  )}
                </div>
              )}

              {!running && !runError && !runResults && (
                <div className="flex-1 flex flex-col items-center justify-center space-y-2 text-muted-foreground opacity-30">
                  <Terminal className="w-8 h-8" />
                  <p className="text-xs">Console is empty. Click "Run Code" to compile and run your program against testcases.</p>
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
