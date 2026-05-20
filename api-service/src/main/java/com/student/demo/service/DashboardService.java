package com.student.demo.service;

import com.student.demo.dto.DashboardSummaryDTO;
import com.student.demo.entity.AnalysisHistory;
import com.student.demo.entity.AnalysisStatus;
import com.student.demo.entity.CodeExecution;
import com.student.demo.repository.CodeExecutionRepository;
import com.student.demo.repository.CodeFileRepository;
import com.student.demo.repository.HistoryRepository;
import com.student.demo.repository.MetricsRepository;
import com.student.demo.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class DashboardService {

    private final ProjectRepository projectRepository;
    private final CodeFileRepository codeFileRepository;
    private final MetricsRepository metricsRepository;
    private final CodeExecutionRepository codeExecutionRepository;
    private final HistoryRepository historyRepository;

    public DashboardService(ProjectRepository projectRepository,
                            CodeFileRepository codeFileRepository,
                            MetricsRepository metricsRepository,
                            CodeExecutionRepository codeExecutionRepository,
                            HistoryRepository historyRepository) {
        this.projectRepository = projectRepository;
        this.codeFileRepository = codeFileRepository;
        this.metricsRepository = metricsRepository;
        this.codeExecutionRepository = codeExecutionRepository;
        this.historyRepository = historyRepository;
    }

    public DashboardSummaryDTO getSummaryForUser(Long userId) {
        DashboardSummaryDTO summary = new DashboardSummaryDTO();

        long totalProjects = projectRepository.countByUserId(userId);
        long totalFiles = codeFileRepository.countByUserId(userId);
        long totalAnalyses = metricsRepository.countByUserId(userId);
        long completedAnalyses = metricsRepository.countByUserIdAndStatus(userId, AnalysisStatus.COMPLETED);

        long failedExecutions = codeExecutionRepository.countByUserIdAndStatus(userId, "COMPILE_ERROR")
                + codeExecutionRepository.countByUserIdAndStatus(userId, "ERROR")
                + codeExecutionRepository.countByUserIdAndStatus(userId, "RUNTIME_ERROR");
        long successExecutions = codeExecutionRepository.countByUserIdAndStatus(userId, "SUCCESS");
        long totalExecutions = codeExecutionRepository.countByUserId(userId);

        double successRate = totalExecutions == 0 ? 0.0 : (successExecutions * 100.0) / totalExecutions;
        Double avgScore = metricsRepository.averageComplexityScoreByUserId(userId);
        int healthScore = avgScore == null ? 0 : (int) Math.max(0, Math.min(100, 100 - avgScore));

        summary.setTotalProjects(totalProjects);
        summary.setTotalFilesAnalyzed(totalFiles);
        summary.setTotalAnalyses(totalAnalyses);
        summary.setFailedExecutions(failedExecutions);
        summary.setPassedExecutions(successExecutions);
        summary.setSuccessRate(Math.round(successRate * 10.0) / 10.0);
        summary.setScore(healthScore);
        summary.setActivityData(buildActivity(userId));
        summary.setIssueDistribution(buildIssueDistribution(userId, completedAnalyses, failedExecutions));
        summary.setRecentActivity(buildRecentActivity(userId));

        return summary;
    }

    private List<Map<String, Object>> buildActivity(Long userId) {
        LocalDateTime since = LocalDateTime.now().minusDays(6).withHour(0).withMinute(0).withSecond(0);
        List<AnalysisHistory> history = historyRepository.findRecentByUserId(userId, since);
        List<CodeExecution> executions = codeExecutionRepository.findRecentByUserId(userId, since);

        Map<LocalDate, int[]> buckets = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            buckets.put(LocalDate.now().minusDays(i), new int[]{0, 0});
        }

        for (AnalysisHistory h : history) {
            LocalDate day = h.getCreatedAt().toLocalDate();
            if (buckets.containsKey(day)) {
                buckets.get(day)[0]++;
            }
        }
        for (CodeExecution e : executions) {
            LocalDate day = e.getCreatedAt().toLocalDate();
            if (buckets.containsKey(day)) {
                buckets.get(day)[1]++;
            }
        }

        List<Map<String, Object>> activity = new ArrayList<>();
        for (Map.Entry<LocalDate, int[]> entry : buckets.entrySet()) {
            Map<String, Object> row = new HashMap<>();
            row.put("name", entry.getKey().getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
            row.put("issues", entry.getValue()[0]);
            row.put("files", entry.getValue()[1]);
            activity.add(row);
        }
        return activity;
    }

    private List<Map<String, Object>> buildIssueDistribution(Long userId, long completed, long failed) {
        List<Map<String, Object>> distribution = new ArrayList<>();
        distribution.add(Map.of("name", "Completed", "count", completed));
        distribution.add(Map.of("name", "Failed Exec", "count", failed));
        distribution.add(Map.of("name", "Pending", "count",
                Math.max(0, metricsRepository.countByUserId(userId) - completed)));
        return distribution;
    }

    private List<Map<String, Object>> buildRecentActivity(Long userId) {
        List<Map<String, Object>> recent = new ArrayList<>();
        historyRepository.findRecentByUserId(userId, LocalDateTime.now().minusDays(7)).stream()
                .limit(5)
                .forEach(h -> recent.add(Map.of(
                        "type", "analysis",
                        "title", "Code analysis",
                        "score", h.getScore() != null ? h.getScore() : 0,
                        "at", h.getCreatedAt().toString()
                )));
        codeExecutionRepository.findRecentByUserId(userId, LocalDateTime.now().minusDays(7)).stream()
                .limit(5)
                .forEach(e -> recent.add(Map.of(
                        "type", "execution",
                        "title", "Code execution (" + e.getLanguage() + ")",
                        "status", e.getStatus(),
                        "at", e.getCreatedAt().toString()
                )));
        return recent;
    }
}
