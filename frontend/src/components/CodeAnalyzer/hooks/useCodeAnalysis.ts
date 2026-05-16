import { useState, useCallback } from 'react';
import { api, aiService, codeService, historyService } from '../../../services/api';
import type { CodeFile } from '../../../types';

export const useCodeAnalysis = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [results, setResults] = useState<any>(null);

  const analyze = useCallback(async (code: string, language: string, model: string = 'AUTO') => {
    setLoading(true);
    setError(null);
    try {
      // 1. Upload code
      const uploadRes = await codeService.uploadCode({
        name: `Analysis_${Date.now()}.${language === 'javascript' ? 'js' : language === 'python' ? 'py' : 'txt'}`,
        content: code,
        language
      });
      
      const fileId = uploadRes.data.id;

      // 2. Trigger async analysis
      await aiService.analyzeFile(fileId, model);

      // 3. Wait for Real-Time SSE Updates
      const enhancedMetrics = await new Promise<any>((resolve, reject) => {
        const url = `${import.meta.env.VITE_API_URL || 'http://localhost:8088'}/analyze/${fileId}/stream`;
        const eventSource = new EventSource(url, { withCredentials: true });

        eventSource.addEventListener('message', async (event) => {
          const status = event.data;
          
          if (status === 'COMPLETED') {
            eventSource.close();
            try {
              const resultRes = await api.get(`/analyze/${fileId}`);
              resolve(resultRes.data);
            } catch (e) {
              reject(new Error('Failed to fetch final analysis results.'));
            }
          } else if (status === 'FAILED') {
            eventSource.close();
            reject(new Error('AI Analysis failed to process the request.'));
          }
        });

        eventSource.onerror = () => {
          eventSource.close();
          reject(new Error('Lost connection to analysis server. Please try again.'));
        };
      });

      // 4. Mapping
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

      // 5. Save to history
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
