# 📖 QUICK REFERENCE: AI CODE ANALYZER
**Architecture Overview & Success Metrics**

This is a one-page summary of the AI Code Analyzer's core logic and requirements.

---

## 🔍 The Situation
The analyzer is the primary value-add of the platform. It must be stable, visible, and provide high-fidelity AI feedback from the backend.

## 🧱 Component Blueprint
- **Input**: Uses Monaco for multi-language syntax support.
- **Hook**: `useCodeAnalysis` handles the temporary upload and analysis trigger sequence.
- **Results**: Displays Score, Issues (Severity-based), and Suggested Refactors.

---

## ✅ Success Criteria
| Category | Requirement | Verified |
|----------|-------------|----------|
| **Visibility** | Editor must be visible on the `/review` page instantly. | ✅ |
| **Logic** | "Check Code Now" must trigger backend AI calls. | ✅ |
| **UX** | Analysis must show a visible Loading state. | ✅ |
| **State** | Results panel must clear correctly on "Reset". | ✅ |

---

## 🛠 File Template Structure
If adding new analyzer features, follow this file structure:
```
src/components/CodeAnalyzer/
├── CodeAnalyzer.tsx (Main)
├── CodeInput.tsx (Editor)
├── AnalysisResults.tsx (Display)
├── LoadingState.tsx (Feedback)
└── hooks/
    └── useCodeAnalysis.ts (Logic)
```

---
*Reference the [MASTER_INDEX.md](./MASTER_INDEX.md) for related guides.*
