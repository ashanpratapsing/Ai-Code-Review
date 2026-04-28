# 🚀 AI CODE ANALYZER: FAANG ARCHITECTURE
**Technical Guide for the Modular Review Engine**

This document details the high-fidelity implementation of the AI Code Analyzer suite, following standard industry design patterns for developer tools.

---

## 🏗 Component Architecture
The analyzer is built as a modular suite in `src/components/CodeAnalyzer/`:

1.  **CodeAnalyzer (Container)**
    - *Role*: Orchestrates the state machine (Idle -> Loading -> Results -> Error).
    - *States*: Handled via the `useCodeAnalysis` custom hook.

2.  **CodeInput (Editor Layer)**
    - *Engine*: Powered by `@monaco-editor/react`.
    - *Layout*: Implements `automaticLayout: true` and a fixed `min-h-[400px]` to prevent container collapse.
    - *Features*: Support for language switching and local storage persistence.

3.  **AnalysisResults (Presentation Layer)**
    - *Role*: Parses `Metrics` data into actionable insights.
    - *Visuals*: Custom Score Cards, Issue Lists (grouped by severity), and Refactor Code blocks.

4.  **LoadingState (UX Layer)**
    - *Role*: Provides visual feedback with Framer Motion animations during long-running AI calls.

---

## 🔗 Hook Integration: `useCodeAnalysis`
This custom hook isolates the business logic of analysis:
- **Temporary Upload**: Codes are uploaded via `codeService.uploadCode` to generate a database-backed ID.
- **Trigger**: Calls `aiService.analyzeFile(fileId)`.
- **Mapping**: Transforms backend `Metrics` into a structured `AnalysisResult` object.

```typescript
// Mapping logic example
const formattedResults = {
  issues: metrics.bugs + metrics.codeSmells,
  suggestions: metrics.optimization,
  refactor: metrics.refactoredCode
};
```

---

## 🛠 Troubleshooting the Editor
If the editor is **NOT rendering**, check these three layers:

1.  **CSS Parent**: The parent of `CodeInput` must have a defined height (we use `flex-1` plus `h-[calc(100vh-10rem)]`).
2.  **Monaco Overlay**: Ensure the Z-index of any loaders/overlays does not permanently block the editor mount.
3.  **HMR Mismatch**: If adding props frequently, perform a hard refresh (`Ctrl + F5`) to clear the Monaco instance cache.

---
*Reference the [MASTER_INDEX.md](./MASTER_INDEX.md) for related guides.*
