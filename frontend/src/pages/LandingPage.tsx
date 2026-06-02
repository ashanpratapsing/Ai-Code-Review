import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { useTheme } from '../context/ThemeContext';
import { useAuth } from '../context/AuthContext';
import { ThemeToggle } from '../components/ThemeToggle';
import { AxiomSplash } from '../components/AxiomSplash';
import { AxiomLogo } from '../components/AxiomLogo';
import { 
  Terminal as TerminalIcon, Cpu, Layers, Activity, Check, Code, 
  ArrowRight, Lock, Shield, RefreshCw, Play, Zap, BarChart2, 
  X, Flame, Calendar, Sparkles
} from 'lucide-react';
import { Button, Card, Badge } from '../components/ui/core';

// Simulated terminal run logs per language
const SIMULATED_RUNS = [
  {
    language: 'JAVA',
    file: 'Solution.java',
    code: 'public class Solution {\n    public static void main(String[] args) {\n        System.out.println("Java execution: Hello Java");\n    }\n}',
    logs: [
      '>> Initialize sandbox environment (temurin:17-alpine)...',
      '>> Mounting isolated workspace at /sandbox/exec_48...',
      '>> Compiling Solution.java using javac...',
      '>> Class binary generated successfully (size: 412 bytes).',
      '>> Executing class Main with 256MB memory limit...',
      '>> Stdin: "Hello Java" | Expected: "Java execution: Hello Java"',
      '>> Actual: "Java execution: Hello Java"',
      '>> [RESULT] Test case 1/1: PASSED (Execution time: 24ms)'
    ]
  },
  {
    language: 'PYTHON',
    file: 'main.py',
    code: 'import sys\ninput_data = sys.stdin.read().strip()\nprint(f"Python execution: {input_data}")',
    logs: [
      '>> Spawning sandboxed sub-container (python:3.10-alpine)...',
      '>> Mounting workspace workspace_983...',
      '>> Launching python main.py...',
      '>> Stdin: "Hello Python" | Expected: "Python execution: Hello Python"',
      '>> Actual: "Python execution: Hello Python"',
      '>> [RESULT] Test case 1/1: PASSED (Execution time: 14ms)'
    ]
  },
  {
    language: 'CPP',
    file: 'solution.cpp',
    code: '#include <iostream>\nint main() {\n    std::cout << "C++ execution: sum is 15\\n";\n    return 0;\n}',
    logs: [
      '>> Initializing compilation worker (gcc:alpine)...',
      '>> Executing g++ -O3 -std=c++17 solution.cpp -o solution...',
      '>> Native binary solution compiled successfully (size: 16.4KB).',
      '>> Spawning execution sandbox...',
      '>> Actual: "C++ execution: sum is 15"',
      '>> [RESULT] Test case 1/1: PASSED (Execution time: 4ms)'
    ]
  },
  {
    language: 'GO',
    file: 'main.go',
    code: 'package main\nimport "fmt"\nfunc main() {\n    fmt.Println("Go execution: Golang")\n}',
    logs: [
      '>> Spawning Go compiler container (golang:alpine)...',
      '>> Executing go build -ldflags="-s -w" main.go...',
      '>> Static binary main compiled successfully (size: 1.1MB).',
      '>> Invoking native sandbox execution...',
      '>> Actual: "Go execution: Golang"',
      '>> [RESULT] Test case 1/1: PASSED (Execution time: 6ms)'
    ]
  }
];

export const LandingPage = () => {
  const { theme } = useTheme();
  const navigate = useNavigate();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const { user } = useAuth();
  const [showToast, setShowToast] = useState(false);
  const [showSplash, setShowSplash] = useState(() => {
    return sessionStorage.getItem('axiom_splash_shown') !== 'true';
  });

  // Terminal state
  const [langIndex, setLangIndex] = useState(0);
  const [typedCode, setTypedCode] = useState('');
  const [logLines, setLogLines] = useState<string[]>([]);
  const [step, setStep] = useState<'typing' | 'compiling' | 'finished'>('typing');

  // Trigger login reminder toast after 12 seconds, reset on scroll/interaction to prioritize exploring
  useEffect(() => {
    if (user || sessionStorage.getItem('axiom_toast_dismissed') === 'true' || showToast) {
      return;
    }

    let showTimeout: any;
    const delayDuration = 12000; // 12 seconds initial delay

    const resetTimer = () => {
      if (showTimeout) {
        clearTimeout(showTimeout);
      }
      showTimeout = setTimeout(() => {
        if (!sessionStorage.getItem('axiom_toast_dismissed') && !user) {
          setShowToast(true);
        }
      }, delayDuration);
    };

    // Start initial timer
    resetTimer();

    const handleInteraction = () => {
      resetTimer();
    };

    window.addEventListener('scroll', handleInteraction, { passive: true });
    window.addEventListener('mousemove', handleInteraction, { passive: true });
    window.addEventListener('keydown', handleInteraction, { passive: true });
    window.addEventListener('click', handleInteraction, { passive: true });

    return () => {
      if (showTimeout) {
        clearTimeout(showTimeout);
      }
      window.removeEventListener('scroll', handleInteraction);
      window.removeEventListener('mousemove', handleInteraction);
      window.removeEventListener('keydown', handleInteraction);
      window.removeEventListener('click', handleInteraction);
    };
  }, [user, showToast]);

  const handleDismissToast = () => {
    setShowToast(false);
    sessionStorage.setItem('axiom_toast_dismissed', 'true');
  };

  const handleCtaClick = () => {
    sessionStorage.setItem('axiom_toast_dismissed', 'true');
  };

  // Simulator typing & logging loops
  useEffect(() => {
    let active = true;
    const currentRun = SIMULATED_RUNS[langIndex];
    let codeCharIndex = 0;
    setStep('typing');
    setLogLines([]);

    let typeCodeInterval: any;
    let logInterval: any;
    let nextLangTimeout: any;

    // 1. Simulate typing of code
    typeCodeInterval = setInterval(() => {
      if (!active) return;
      if (currentRun && codeCharIndex <= currentRun.code.length) {
        setTypedCode(currentRun.code.slice(0, codeCharIndex));
        codeCharIndex++;
      } else {
        clearInterval(typeCodeInterval);
        setStep('compiling');
        
        // 2. Simulate printing compilation logs
        let logIndex = 0;
        logInterval = setInterval(() => {
          if (!active) return;
          if (currentRun && currentRun.logs && logIndex < currentRun.logs.length) {
            const nextLine = currentRun.logs[logIndex];
            if (nextLine !== undefined) {
              setLogLines(prev => [...prev, nextLine]);
            }
            logIndex++;
          } else {
            clearInterval(logInterval);
            setStep('finished');
            
            // 3. Pause and advance to next language
            nextLangTimeout = setTimeout(() => {
              if (active) {
                setLangIndex(prev => (prev + 1) % SIMULATED_RUNS.length);
              }
            }, 5000);
          }
        }, 800);
      }
    }, 25);

    return () => {
      active = false;
      clearInterval(typeCodeInterval);
      clearInterval(logInterval);
      clearTimeout(nextLangTimeout);
    };
  }, [langIndex]);

  const scrollToSection = (id: string) => {
    setMobileMenuOpen(false);
    const element = document.getElementById(id);
    if (element) {
      element.scrollIntoView({ behavior: 'smooth' });
    }
  };

  return (
    <div className="min-h-screen bg-background text-foreground relative overflow-hidden selection:bg-primary/30 selection:text-foreground">
      {showSplash && (
        <AxiomSplash
          onComplete={() => {
            setShowSplash(false);
            sessionStorage.setItem('axiom_splash_shown', 'true');
          }}
        />
      )}
      {/* Decorative Radial Background Glows */}
      <div className="absolute top-0 left-0 w-full h-full pointer-events-none z-0">
        <div className="absolute top-0 left-1/4 w-[500px] h-[500px] bg-primary/10 rounded-full blur-[150px] dark:bg-primary/5" />
        <div className="absolute top-1/3 right-1/4 w-[600px] h-[600px] bg-violet-500/10 rounded-full blur-[180px] dark:bg-violet-500/5" />
        <div className="absolute bottom-10 left-10 w-[400px] h-[400px] bg-primary/10 rounded-full blur-[140px] dark:bg-primary/5" />
      </div>

      {/* Sticky Premium Navbar */}
      <header className="sticky top-0 z-50 w-full border-b border-border/40 bg-background/60 backdrop-blur-xl transition-all">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
          <div className="flex items-center gap-8">
            {/* Platform Brand Logo */}
            <Link to="/" className="hover:opacity-90 transition-opacity">
              <AxiomLogo size="sm" showText={true} />
            </Link>

            {/* Desktop Navbar Links */}
            <nav className="hidden md:flex items-center gap-6">
              <button onClick={() => scrollToSection('hero')} className="text-sm font-medium text-muted-foreground hover:text-foreground transition-colors cursor-pointer">Home</button>
              <button onClick={() => scrollToSection('metrics')} className="text-sm font-medium text-muted-foreground hover:text-foreground transition-colors cursor-pointer">Metrics</button>
              <button onClick={() => scrollToSection('languages')} className="text-sm font-medium text-muted-foreground hover:text-foreground transition-colors cursor-pointer">Languages</button>
              <button onClick={() => scrollToSection('architecture')} className="text-sm font-medium text-muted-foreground hover:text-foreground transition-colors cursor-pointer">Architecture</button>
              <button onClick={() => scrollToSection('features')} className="text-sm font-medium text-muted-foreground hover:text-foreground transition-colors cursor-pointer">Features</button>
            </nav>
          </div>

          {/* Right Nav Options */}
          <div className="hidden md:flex items-center gap-4">
            <ThemeToggle />
            <Link to="/login" onClick={handleCtaClick}>
              <Button variant="ghost" className="text-sm cursor-pointer">Sign In</Button>
            </Link>
            <Link to="/signup" onClick={handleCtaClick}>
              <Button size="sm" className="text-sm font-semibold rounded-lg bg-primary hover:bg-primary/90 text-white cursor-pointer">
                Get Started
              </Button>
            </Link>
          </div>

          {/* Mobile Menu Button */}
          <div className="flex items-center gap-3 md:hidden">
            <ThemeToggle />
            <button 
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className="p-2 text-muted-foreground hover:text-foreground rounded-lg transition-colors cursor-pointer"
            >
              {mobileMenuOpen ? <X className="w-6 h-6" /> : <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16m-7 6h7" /></svg>}
            </button>
          </div>
        </div>

        {/* Mobile Navigation Dropdown */}
        <AnimatePresence>
          {mobileMenuOpen && (
            <motion.div 
              initial={{ opacity: 0, height: 0 }}
              animate={{ opacity: 1, height: 'auto' }}
              exit={{ opacity: 0, height: 0 }}
              className="md:hidden border-b border-border/40 bg-background/95 backdrop-blur-md"
            >
              <div className="px-4 pt-2 pb-6 space-y-3">
                <button onClick={() => scrollToSection('hero')} className="block w-full text-left py-2 font-medium text-muted-foreground hover:text-foreground">Home</button>
                <button onClick={() => scrollToSection('metrics')} className="block w-full text-left py-2 font-medium text-muted-foreground hover:text-foreground">Metrics</button>
                <button onClick={() => scrollToSection('languages')} className="block w-full text-left py-2 font-medium text-muted-foreground hover:text-foreground">Languages</button>
                <button onClick={() => scrollToSection('architecture')} className="block w-full text-left py-2 font-medium text-muted-foreground hover:text-foreground">Architecture</button>
                <button onClick={() => scrollToSection('features')} className="block w-full text-left py-2 font-medium text-muted-foreground hover:text-foreground">Features</button>
                <div className="pt-4 border-t border-border/40 flex flex-col gap-3">
                  <Link to="/login" onClick={handleCtaClick} className="w-full">
                    <Button variant="outline" className="w-full">Sign In</Button>
                  </Link>
                  <Link to="/signup" onClick={handleCtaClick} className="w-full">
                    <Button className="w-full bg-primary text-white">Get Started</Button>
                  </Link>
                </div>
              </div>
            </motion.div>
          )}
        </AnimatePresence>
      </header>

      {/* Main Page Layout Wrapper */}
      <main className="relative z-10 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        
        {/* SECTION 1 — HERO SECTION */}
        <section id="hero" className="min-h-[calc(100vh-4rem)] flex flex-col lg:flex-row items-center justify-center gap-12 py-16">
          {/* Hero Details (Left Side) */}
          <div className="flex-1 space-y-8 text-center lg:text-left">
            <motion.div
              initial={{ opacity: 0, y: -20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6 }}
            >
              <Badge variant="success" className="px-3.5 py-1.5 rounded-full font-bold uppercase tracking-wider text-[10px] gap-1.5 shadow-inner">
                <Sparkles className="w-3 h-3 text-primary animate-pulse" />
                Engineering Intelligence Platform
              </Badge>
            </motion.div>

            <motion.h1 
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6, delay: 0.1 }}
              className="text-4xl sm:text-5xl lg:text-6xl font-extrabold tracking-tight leading-[1.1] bg-gradient-to-r from-foreground via-foreground to-muted-foreground bg-clip-text text-transparent"
            >
              Analyze. Execute.<br />
              <span className="bg-gradient-to-r from-primary to-violet-500 bg-clip-text text-transparent">
                Optimize Everywhere.
              </span>
            </motion.h1>

            <motion.p
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6, delay: 0.2 }}
              className="text-muted-foreground text-lg sm:text-xl max-w-2xl mx-auto lg:mx-0 leading-relaxed font-normal"
            >
              A high-performance codebase hardening platform. Sandbox and review your code across 6 runtimes with distributed queue validation, real-time observability telemetry, and streak analytics.
            </motion.p>

            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6, delay: 0.3 }}
              className="flex flex-col sm:flex-row items-center justify-center lg:justify-start gap-4"
            >
              <Link to="/signup">
                <Button size="lg" className="gap-2.5 font-bold shadow-xl shadow-primary/20 bg-primary hover:bg-primary/95 text-white w-full sm:w-auto rounded-xl">
                  Start Optimizing
                  <ArrowRight className="w-5 h-5" />
                </Button>
              </Link>
              <Link to="/login">
                <Button size="lg" variant="outline" className="w-full sm:w-auto font-bold rounded-xl bg-card/10 border-border/80">
                  Interactive Demo
                </Button>
              </Link>
            </motion.div>
          </div>

          {/* Interactive Code Execution Terminal (Right Side) */}
          <div className="flex-1 w-full max-w-xl lg:max-w-none">
            <motion.div
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              transition={{ duration: 0.8 }}
              className="w-full border border-border/80 rounded-2xl shadow-2xl bg-card-bg-subtle backdrop-blur-2xl overflow-hidden shadow-black/40"
            >
              {/* Terminal Window Header */}
              <div className="px-4 py-3 bg-muted/40 border-b border-border/40 flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <div className="w-3 h-3 rounded-full bg-red-500/80" />
                  <div className="w-3 h-3 rounded-full bg-yellow-500/80" />
                  <div className="w-3 h-3 rounded-full bg-green-500/80" />
                  <span className="text-xs font-semibold text-muted-foreground ml-2 font-mono">
                    sandbox_runtime@{SIMULATED_RUNS[langIndex].language.toLowerCase()}
                  </span>
                </div>
                <div className="flex items-center gap-1.5">
                  <Badge variant="outline" className="text-[10px] font-mono tracking-wider bg-black/20 text-primary border-primary/20 py-0.5">
                    {SIMULATED_RUNS[langIndex].language}
                  </Badge>
                </div>
              </div>

              {/* Terminal Screen Body */}
              <div className="p-5 font-mono text-xs sm:text-sm space-y-4 min-h-[340px] flex flex-col justify-between bg-black/40 text-green-400">
                {/* Typed Code block */}
                <div className="space-y-1">
                  <div className="text-muted-foreground flex items-center justify-between border-b border-border/10 pb-1.5 mb-2">
                    <span>Source: {SIMULATED_RUNS[langIndex].file}</span>
                    <span className="text-[10px] uppercase font-bold text-primary animate-pulse flex items-center gap-1">
                      <Play className="w-2.5 h-2.5 fill-primary text-primary" /> {step}
                    </span>
                  </div>
                  <pre className="text-sky-300 overflow-x-auto select-none leading-relaxed">
                    {typedCode}
                    {step === 'typing' && <span className="animate-pulse bg-sky-300 text-sky-300">|</span>}
                  </pre>
                </div>

                {/* Simulated execution logs block */}
                <div className="space-y-1.5 pt-4 border-t border-border/10 flex-1 min-h-[140px] overflow-y-auto">
                  {logLines.map((line, idx) => (
                    <div 
                      key={idx} 
                      className={`leading-relaxed ${
                        line?.includes('[RESULT]') ? 'text-yellow-300 font-bold border-l-2 border-yellow-400 pl-2 mt-1' :
                        line?.includes('PASSED') ? 'text-green-300 font-semibold' : 'text-zinc-400'
                      }`}
                    >
                      {line}
                    </div>
                  ))}
                  {step === 'compiling' && (
                    <div className="text-zinc-500 animate-pulse flex items-center gap-2">
                      <RefreshCw className="w-3.5 h-3.5 animate-spin" /> Fetching sub-runtimes...
                    </div>
                  )}
                </div>
              </div>
            </motion.div>
          </div>
        </section>

        {/* SECTION 2 — TRUST / ENGINEERING METRICS */}
        <section id="metrics" className="py-20 border-t border-border/40 scroll-mt-16">
          <div className="text-center max-w-3xl mx-auto space-y-4 mb-16">
            <h2 className="text-3xl font-extrabold tracking-tight">Built for Production Scale</h2>
            <p className="text-muted-foreground">Advanced telemetry, sub-millisecond execution boundaries, and absolute sandboxed security.</p>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
            <Card className="hover:border-primary/40 transition-all group duration-300 p-6 flex flex-col justify-between">
              <div className="w-12 h-12 bg-primary/10 rounded-xl flex items-center justify-center border border-primary/20 mb-6 group-hover:scale-110 transition-transform">
                <Cpu className="text-primary w-6 h-6" />
              </div>
              <div className="space-y-2">
                <h3 className="text-xl font-bold">6 Language Runtimes</h3>
                <p className="text-muted-foreground text-sm leading-relaxed">Secure micro-sandbox compilation and run support for Java, Python, JavaScript, C, C++, and Go.</p>
              </div>
            </Card>

            <Card className="hover:border-primary/40 transition-all group duration-300 p-6 flex flex-col justify-between">
              <div className="w-12 h-12 bg-violet-500/10 rounded-xl flex items-center justify-center border border-violet-500/20 mb-6 group-hover:scale-110 transition-transform">
                <Layers className="text-violet-400 w-6 h-6" />
              </div>
              <div className="space-y-2">
                <h3 className="text-xl font-bold">Async Message Queues</h3>
                <p className="text-muted-foreground text-sm leading-relaxed">RabbitMQ-driven task workers process heavy compiler operations concurrently without blocking APIs.</p>
              </div>
            </Card>

            <Card className="hover:border-primary/40 transition-all group duration-300 p-6 flex flex-col justify-between">
              <div className="w-12 h-12 bg-green-500/10 rounded-xl flex items-center justify-center border border-green-500/20 mb-6 group-hover:scale-110 transition-transform">
                <Activity className="text-green-400 w-6 h-6" />
              </div>
              <div className="space-y-2">
                <h3 className="text-xl font-bold">SSE Real-Time Telemetry</h3>
                <p className="text-muted-foreground text-sm leading-relaxed">Server-Sent Events stream live code execution steps and review statistics instantly to the client UI.</p>
              </div>
            </Card>
          </div>
        </section>

        {/* SECTION 3 — SUPPORTED LANGUAGES */}
        <section id="languages" className="py-20 border-t border-border/40 scroll-mt-16">
          <div className="text-center max-w-3xl mx-auto space-y-4 mb-16">
            <h2 className="text-3xl font-extrabold tracking-tight">Unified Language Sandboxing</h2>
            <p className="text-muted-foreground">Standardized compilation targets and performance monitoring across multiple backend engines.</p>
          </div>

          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-5">
            {[
              { name: 'Java', color: 'from-orange-500/20 to-red-500/10', glow: 'group-hover:shadow-orange-500/10', text: 'JDK 17 Runtime' },
              { name: 'Python', color: 'from-blue-500/20 to-yellow-500/10', glow: 'group-hover:shadow-blue-500/10', text: 'v3.10 Engine' },
              { name: 'JavaScript', color: 'from-yellow-400/20 to-orange-500/10', glow: 'group-hover:shadow-yellow-400/10', text: 'Node18 Sandbox' },
              { name: 'C', color: 'from-blue-600/20 to-sky-500/10', glow: 'group-hover:shadow-blue-600/10', text: 'GCC Compile' },
              { name: 'C++', color: 'from-indigo-500/20 to-blue-500/10', glow: 'group-hover:shadow-indigo-500/10', text: 'G++ std=17' },
              { name: 'Go', color: 'from-sky-400/20 to-teal-500/10', glow: 'group-hover:shadow-sky-400/10', text: 'Native Compiler' }
            ].map((lang, idx) => (
              <motion.div 
                key={idx}
                whileHover={{ y: -6 }}
                className={`group cursor-pointer border border-border/60 rounded-2xl bg-card/30 p-5 text-center transition-all duration-300 hover:border-primary/50 relative overflow-hidden ${lang.glow}`}
              >
                <div className={`absolute top-0 left-0 w-full h-1.5 bg-gradient-to-r ${lang.color}`} />
                <h4 className="text-xl font-bold tracking-tight mb-1">{lang.name}</h4>
                <p className="text-[10px] text-muted-foreground uppercase font-mono tracking-wider">{lang.text}</p>
              </motion.div>
            ))}
          </div>
        </section>

        {/* SECTION 4 — HOW IT WORKS (ARCHITECTURE FLOW) */}
        <section id="architecture" className="py-20 border-t border-border/40 scroll-mt-16">
          <div className="text-center max-w-3xl mx-auto space-y-4 mb-16">
            <h2 className="text-3xl font-extrabold tracking-tight">Distributed Runtime Architecture</h2>
            <p className="text-muted-foreground">The journey of a code execution requests through our multi-layered network.</p>
          </div>

          {/* Graphical Pipeline Cards */}
          <div className="relative border border-border/60 rounded-2xl bg-card-bg-subtle p-8 overflow-x-auto shadow-inner">
            <div className="flex items-center justify-between min-w-[900px] gap-4 py-4">
              {[
                { step: '01', title: 'React Client', icon: Code, desc: 'Code submitted / SSE listener bound' },
                { step: '02', title: 'API Gateway', icon: Shield, desc: 'OAuth verification / Rate limits' },
                { step: '03', title: 'RabbitMQ', icon: Layers, desc: 'Async payload serialization & queue' },
                { step: '04', title: 'Worker Node', icon: Cpu, desc: 'Workspace setup & docker wrapper' },
                { step: '05', title: 'Isolated Sandbox', icon: Lock, desc: 'Cooperative limits & test runner' },
                { step: '06', title: 'DB / Redis', icon: BarChart2, desc: 'Result storage & real-time cache' }
              ].map((node, idx) => (
                <div key={idx} className="flex items-center flex-1">
                  <div className="flex flex-col items-center text-center space-y-3 relative z-10 w-full">
                    <div className="w-12 h-12 rounded-xl bg-background border border-border/80 flex items-center justify-center shadow-lg relative group hover:border-primary/50 transition-colors">
                      <node.icon className="text-primary w-5 h-5 group-hover:scale-110 transition-transform" />
                      <span className="absolute -top-2 -right-2 text-[9px] font-bold px-1.5 py-0.5 rounded-full bg-secondary border border-border text-muted-foreground">
                        {node.step}
                      </span>
                    </div>
                    <div className="space-y-0.5">
                      <h4 className="text-xs font-bold text-foreground">{node.title}</h4>
                      <p className="text-[9px] text-muted-foreground px-1 leading-normal max-w-[120px] mx-auto">{node.desc}</p>
                    </div>
                  </div>
                  {idx < 5 && (
                    <div className="w-full h-0.5 bg-gradient-to-r from-primary/30 to-violet-500/30 flex items-center justify-center relative mx-1">
                      <div className="w-1.5 h-1.5 rounded-full bg-primary animate-ping absolute" />
                    </div>
                  )}
                </div>
              ))}
            </div>
          </div>
        </section>

        {/* SECTION 5 — FEATURE MATRIX GRID */}
        <section id="features" className="py-20 border-t border-border/40 scroll-mt-16">
          <div className="text-center max-w-3xl mx-auto space-y-4 mb-16">
            <h2 className="text-3xl font-extrabold tracking-tight">Full System Capabilities</h2>
            <p className="text-muted-foreground">Every component optimized to support clean codes, verified performance, and security gates.</p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {[
              { icon: TerminalIcon, title: 'Multi-Language Sandbox', desc: 'Securely run code inside containers matching standard compilers (Java, Go, Python, C++).' },
              { icon: Sparkles, title: 'AI Code Reviewer', desc: 'Get automated structural code insights and complexity feedback using dynamic LLM triggers.' },
              { icon: Layers, title: 'Event-Driven Async Workers', desc: 'Separate compilation workloads into execution workers to preserve HTTP responsiveness.' },
              { icon: Activity, title: 'Prometheus Monitoring', desc: 'Monitor API health, queue sizes, and compilation latency with Prometheus and Grafana dashboards.' },
              { icon: Shield, title: 'Secure Virtual Isolation', desc: 'Limit execution containers using CPU, RAM, and syscall restrictions (read-only directories).' },
              { icon: RefreshCw, title: 'Real-Time SSE Channels', desc: 'Leverage Server-Sent Events to push execution logs to the UI instantly without API polling.' }
            ].map((feat, idx) => (
              <Card key={idx} className="hover:border-primary/30 transition-all duration-300 p-6 flex items-start gap-4">
                <div className="w-10 h-10 rounded-lg bg-primary/15 border border-primary/20 flex items-center justify-center shrink-0">
                  <feat.icon className="text-primary w-5 h-5" />
                </div>
                <div className="space-y-1.5">
                  <h4 className="text-base font-bold">{feat.title}</h4>
                  <p className="text-muted-foreground text-xs leading-relaxed">{feat.desc}</p>
                </div>
              </Card>
            ))}
          </div>
        </section>

        {/* SECTION 6 — STREAK & ANALYTICS SHOWCASE */}
        <section className="py-20 border-t border-border/40">
          <div className="flex flex-col lg:flex-row items-center gap-12">
            <div className="flex-1 space-y-6">
              <Badge variant="outline" className="text-primary font-bold uppercase tracking-wider text-[9px] border-primary/20">
                Developer Activity
              </Badge>
              <h2 className="text-3xl sm:text-4xl font-extrabold tracking-tight">
                Maintain Your Engineering Discipline.
              </h2>
              <p className="text-muted-foreground text-sm sm:text-base leading-relaxed">
                Log consecutive code reviews, track language breakdown, and watch your activity metrics accumulate. Designed like leading developer tools to reward daily optimization.
              </p>
              
              <div className="flex items-center gap-6 pt-2">
                <div className="flex items-center gap-2">
                  <Flame className="w-6 h-6 text-orange-500 fill-orange-500" />
                  <div>
                    <h4 className="text-base font-extrabold leading-none">12 Days</h4>
                    <span className="text-[10px] text-muted-foreground uppercase font-mono tracking-wider">Current Streak</span>
                  </div>
                </div>
                <div className="flex items-center gap-2">
                  <Calendar className="w-6 h-6 text-primary" />
                  <div>
                    <h4 className="text-base font-extrabold leading-none">24 Runs</h4>
                    <span className="text-[10px] text-muted-foreground uppercase font-mono tracking-wider">Total Executions</span>
                  </div>
                </div>
              </div>
            </div>

            {/* Simulated Git-Style Heatmap Card */}
            <div className="flex-1 w-full max-w-xl">
              <Card className="p-6 border-border/80 bg-card-bg-subtle backdrop-blur-2xl">
                <h4 className="text-xs font-bold font-mono text-muted-foreground mb-4 uppercase tracking-wider">
                  Contribution Heatmap
                </h4>
                
                {/* Heatmap Grid Simulation */}
                <div className="grid grid-cols-12 gap-1.5">
                  {Array.from({ length: 48 }).map((_, idx) => {
                    const intensities = ['bg-zinc-800/40 dark:bg-zinc-800/10', 'bg-primary/20', 'bg-primary/45', 'bg-primary/75', 'bg-primary'];
                    const color = intensities[Math.floor(Math.sin(idx * 0.4) * 2.2 + 2.2)];
                    return (
                      <div 
                        key={idx} 
                        className={`aspect-square rounded-[3px] border border-border/10 ${color} hover:ring-2 hover:ring-ring hover:scale-110 transition-all`}
                      />
                    );
                  })}
                </div>

                <div className="mt-4 pt-4 border-t border-border/10 flex items-center justify-between text-[10px] text-zinc-500 font-mono">
                  <span>Less</span>
                  <div className="flex items-center gap-1">
                    <div className="w-2.5 h-2.5 rounded-[1px] bg-zinc-800/40 dark:bg-zinc-800/10 border border-border/10" />
                    <div className="w-2.5 h-2.5 rounded-[1px] bg-primary/20" />
                    <div className="w-2.5 h-2.5 rounded-[1px] bg-primary/45" />
                    <div className="w-2.5 h-2.5 rounded-[1px] bg-primary/75" />
                    <div className="w-2.5 h-2.5 rounded-[1px] bg-primary" />
                  </div>
                  <span>More</span>
                </div>
              </Card>
            </div>
          </div>
        </section>

        {/* SECTION 7 — FINAL CONVERSION CTA */}
        <section className="py-20 border-t border-border/40 text-center relative overflow-hidden rounded-3xl bg-gradient-to-tr from-card-bg-subtle to-background border-2 border-border/30 mb-20">
          <div className="relative z-10 max-w-2xl mx-auto space-y-8">
            <h2 className="text-3xl sm:text-4xl lg:text-5xl font-extrabold tracking-tight">
              Build Smarter Engineering Workflows
            </h2>
            <p className="text-muted-foreground text-sm sm:text-base leading-relaxed">
              Spawns isolated runtimes, receives compiler outputs, and hardening results instantly. Connect your repositories and request review limits.
            </p>
            <div className="flex items-center justify-center gap-4">
              <Link to="/signup" onClick={handleCtaClick}>
                <Button size="lg" className="rounded-xl px-8 font-bold bg-primary text-white cursor-pointer">
                  Get Started
                </Button>
              </Link>
              <Link to="/login" onClick={handleCtaClick}>
                <Button size="lg" variant="outline" className="rounded-xl px-8 font-bold border-border bg-card/10">
                  Login
                </Button>
              </Link>
            </div>
          </div>
        </section>

      </main>

      {/* Landing Page Footer */}
      <footer className="border-t border-border/40 bg-muted/20 py-8 relative z-10 text-center text-xs text-muted-foreground">
        <div className="max-w-7xl mx-auto px-4 flex flex-col sm:flex-row items-center justify-between gap-4">
          <div className="flex items-center gap-2">
            <AxiomLogo size="xs" showText={true} glow={false} />
          </div>
          <p>© 2026 Axiom. Hardened compile & runtime execution sandbox. All rights reserved.</p>
          <div className="flex items-center gap-4">
            <a href="https://github.com/ashanpratapsing/Ai-Code-Review" target="_blank" rel="noopener noreferrer" className="hover:text-foreground transition-colors text-muted-foreground">
              <svg className="w-5 h-5 fill-current" viewBox="0 0 24 24">
                <path fillRule="evenodd" clipRule="evenodd" d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.531 1.032 1.531 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0112 6.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0022 12.017C22 6.484 17.522 2 12 2z" />
              </svg>
            </a>
          </div>
        </div>
      </footer>

      {/* Floating Activation Toast Modal */}
      <AnimatePresence>
        {showToast && (
          <motion.div 
            initial={{ opacity: 0, y: 40, scale: 0.95 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: 30, scale: 0.95 }}
            transition={{ type: 'spring', stiffness: 300, damping: 25 }}
            className="fixed z-50 bottom-4 left-1/2 -translate-x-1/2 w-[calc(100%-2rem)] max-w-sm md:bottom-6 md:right-6 md:left-auto md:translate-x-0 md:w-full md:max-w-sm"
          >
            <Card className={`p-5 border shadow-2xl relative rounded-2xl backdrop-blur-xl transition-all duration-300 ${
              theme === 'dark' 
                ? 'border-violet-500/25 bg-zinc-950/90 shadow-[0_8px_30px_rgb(0,0,0,0.5),0_0_20px_rgba(139,92,246,0.12)]' 
                : 'border-amber-500/15 bg-[#fafaf9]/95 shadow-[0_8px_30px_rgba(197,160,89,0.12),0_0_15px_rgba(197,160,89,0.06)]'
            }`}>
              <button 
                onClick={handleDismissToast}
                className="absolute top-2.5 right-2.5 p-2 rounded-lg text-muted-foreground hover:text-foreground hover:bg-hover-subtle transition-colors cursor-pointer touch-manipulation focus:outline-none"
                aria-label="Dismiss message"
              >
                <X className="w-5 h-5" />
              </button>
              
              <div className="flex gap-4">
                <div className={`w-10 h-10 rounded-xl border flex items-center justify-center shrink-0 ${
                  theme === 'dark' ? 'bg-violet-500/10 border-violet-500/20' : 'bg-amber-500/10 border-amber-500/20'
                }`}>
                  <Sparkles className={`w-5 h-5 animate-pulse ${
                    theme === 'dark' ? 'text-violet-400' : 'text-amber-600'
                  }`} />
                </div>
                <div className="space-y-1.5 pr-4">
                  <h4 className="text-sm font-bold text-foreground">Ready to explore deeper?</h4>
                  <p className="text-xs text-muted-foreground leading-relaxed">
                    Unlock the full engineering platform. Sign in to start analyzing and executing code.
                  </p>
                  <div className="flex items-center gap-3 pt-1">
                    <Link to="/signup" onClick={handleCtaClick} className="shrink-0">
                      <Button size="sm" className="text-xs font-bold bg-primary text-white cursor-pointer rounded-lg">
                        Get Started
                      </Button>
                    </Link>
                    <Link to="/login" onClick={handleCtaClick} className="shrink-0">
                      <Button size="sm" variant="ghost" className="text-xs font-bold cursor-pointer rounded-lg">
                        Login
                      </Button>
                    </Link>
                  </div>
                </div>
              </div>
            </Card>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
};
