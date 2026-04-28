import { useState, useCallback } from 'react';
import { aiService, codeService, historyService } from '../../../services/api';
import type { CodeFile } from '../../../types';

export const useCodeAnalysis = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [results, setResults] = useState<any>(null);

  const analyze = useCallback(async (code: string, language: string) => {
    setLoading(true);
    setError(null);
    try {
      // 1. Upload code
      const uploadRes = await codeService.uploadCode({
        fileName: `Analysis_${Date.now()}.${language === 'javascript' ? 'js' : language === 'python' ? 'py' : 'txt'}`,
        codeContent: code,
        language
      });
      
      const fileId = uploadRes.data.id;

      // 2. Trigger analysis - Backend now returns the exact structured format
      const analysisRes = await aiService.analyzeFile(fileId);
      const enhancedMetrics = analysisRes.data;

      // 3. Mapping is now trivial because backend matches the UI requirements
      const formattedResults = {
        issues: enhancedMetrics.issues.map((msg: string, idx: number) => ({
          type: 'warning',
          message: msg,
          severity: 'medium',
          line: idx + 1
        })),
        betterApproach: enhancedMetrics.betterApproach,
        faangInsights: enhancedMetrics.faangInsights,
        summary: {
          score: enhancedMetrics.score,
          timeComplexity: enhancedMetrics.timeComplexity,
          spaceComplexity: enhancedMetrics.spaceComplexity,
          quality: enhancedMetrics.score > 7 ? 'Clean' : enhancedMetrics.score > 4 ? 'Fair' : 'Needs Review',
          text: enhancedMetrics.summary
        },
        optimizedCode: enhancedMetrics.optimizedCode
      };

      setResults(formattedResults);

      // 4. Save to history
      try {
        await historyService.saveHistory({
          codeSnippet: code,
          resultJson: JSON.stringify(formattedResults),
          score: enhancedMetrics.score
        });
      } catch (historyErr) {
        console.warn('Failed to save history:', historyErr);
      }
    } catch (err: any) {
      console.error('Analysis failed:', err);
      setError(err.response?.data?.message || err.message || 'Analysis failed');
    } finally {
      setLoading(false);
    }
  }, []);

  return { analyze, loading, error, results, setResults };
};
