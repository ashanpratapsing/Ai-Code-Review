import { useState, useCallback } from 'react';
import { api, aiService, codeService, historyService } from '../../../services/api';
import type { AnalysisContext, AnalysisResult } from '../../../types';

const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost/api';

export const useCodeAnalysis = (projectId?: number, existingFileId?: number) => {
  const [loading, setLoading] = useState(false);
  const [status, setStatus] = useState<string>('Initializing...');
  const [error, setError] = useState<string | null>(null);
  const [results, setResults] = useState<Record<string, unknown> | null>(null);

  const analyze = useCallback(
    async (code: string, language: string, model: string = 'AUTO', executionContext?: AnalysisContext) => {
      if (!projectId && !existingFileId) {
        setError('Select a project before analyzing code.');
        return;
      }

      setLoading(true);
      setStatus('Uploading Code...');
      setError(null);

      try {
        let fileId = existingFileId;

        if (!fileId) {
          const uploadRes = await codeService.uploadCode({
            name: `Analysis_${Date.now()}.${language === 'javascript' ? 'js' : language === 'python' ? 'py' : 'java'}`,
            content: code,
            language,
            project: { id: projectId! },
          });
          fileId = uploadRes.data.id;
        }

        await aiService.analyzeFile(fileId, model, executionContext);
        setStatus('Waiting for worker...');

        const enhancedMetrics = await new Promise<AnalysisResult>((resolve, reject) => {
          const url = `${API_BASE}/analyze/${fileId}/stream`;
          const eventSource = new EventSource(url, { withCredentials: true });

          eventSource.addEventListener('message', async (event) => {
            const statusMsg = event.data;
            setStatus(statusMsg);

            if (statusMsg === 'COMPLETED') {
              eventSource.close();
              try {
                const resultRes = await api.get<AnalysisResult>(`/analyze/${fileId}`);
                resolve(resultRes.data);
              } catch {
                reject(new Error('Failed to fetch final analysis results.'));
              }
            } else if (statusMsg === 'FAILED') {
              eventSource.close();
              reject(new Error('AI Analysis failed to process the request.'));
            }
          });

          eventSource.onerror = () => {
            eventSource.close();
            reject(new Error('Lost connection to analysis server. Please try again.'));
          };
        });

        const issuesList = enhancedMetrics.issues ?? enhancedMetrics.bugsDetected ?? [];
        const formattedResults = {
          issues: issuesList.map((msg: string, idx: number) => ({
            type: 'warning',
            message: msg,
            severity: 'medium',
            line: idx + 1,
          })),
          betterApproach: enhancedMetrics.betterApproach,
          faangInsights: enhancedMetrics.faangInsights,
          rootCause: enhancedMetrics.rootCause,
          summary: {
            score: enhancedMetrics.score,
            timeComplexity: enhancedMetrics.timeComplexity,
            spaceComplexity: enhancedMetrics.spaceComplexity,
            quality:
              (enhancedMetrics.score ?? 0) > 70 ? 'Clean' : (enhancedMetrics.score ?? 0) > 40 ? 'Fair' : 'Needs Review',
            text: typeof enhancedMetrics.summary === 'string' ? enhancedMetrics.summary : '',
          },
          optimizedCode: enhancedMetrics.optimizedCode,
          securityIssues: enhancedMetrics.securityIssues ?? [],
          suggestions: enhancedMetrics.suggestions ?? [],
          designPattern: enhancedMetrics.designPattern,
          edgeCases: enhancedMetrics.edgeCases ?? [],
          performanceIssues: enhancedMetrics.performanceIssues ?? [],
          bestPractices: enhancedMetrics.bestPractices ?? [],
          scalabilityAnalysis: enhancedMetrics.scalabilityAnalysis,
          concurrencyAnalysis: enhancedMetrics.concurrencyAnalysis,
          collectionAnalysis: enhancedMetrics.collectionAnalysis,
          graphAnalysis: enhancedMetrics.graphAnalysis,
          runtimeAnalysis: enhancedMetrics.runtimeAnalysis,
          readabilityScore: enhancedMetrics.readabilityScore,
          maintainabilityScore: enhancedMetrics.maintainabilityScore,
          explanation: enhancedMetrics.explanation,
        };

        setResults(formattedResults);

        await historyService.saveHistory({
          codeSnippet: code,
          resultJson: JSON.stringify(formattedResults),
          score: enhancedMetrics.score,
          codeFileId: fileId,
        });
      } catch (err: unknown) {
        const message =
          err && typeof err === 'object' && 'response' in err
            ? (err as { response?: { data?: { message?: string } } }).response?.data?.message
            : err instanceof Error
              ? err.message
              : 'Analysis failed';
        setError(message || 'Analysis failed');
      } finally {
        setLoading(false);
      }
    },
    [projectId, existingFileId]
  );

  return { analyze, loading, status, error, results, setResults };
};
