package com.student.demo.dto;

import java.util.List;
import java.util.Map;

public class DashboardSummaryDTO {
    private long totalProjects;
    private long totalFilesAnalyzed;
    private long totalAnalyses;
    private long failedExecutions;
    private long passedExecutions;
    private double successRate;
    private int score;
    private List<Map<String, Object>> activityData;
    private List<Map<String, Object>> issueDistribution;
    private List<Map<String, Object>> recentActivity;

    public long getTotalProjects() { return totalProjects; }
    public void setTotalProjects(long totalProjects) { this.totalProjects = totalProjects; }
    public long getTotalFilesAnalyzed() { return totalFilesAnalyzed; }
    public void setTotalFilesAnalyzed(long totalFilesAnalyzed) { this.totalFilesAnalyzed = totalFilesAnalyzed; }
    public long getTotalAnalyses() { return totalAnalyses; }
    public void setTotalAnalyses(long totalAnalyses) { this.totalAnalyses = totalAnalyses; }
    public long getFailedExecutions() { return failedExecutions; }
    public void setFailedExecutions(long failedExecutions) { this.failedExecutions = failedExecutions; }
    public long getPassedExecutions() { return passedExecutions; }
    public void setPassedExecutions(long passedExecutions) { this.passedExecutions = passedExecutions; }
    public double getSuccessRate() { return successRate; }
    public void setSuccessRate(double successRate) { this.successRate = successRate; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public List<Map<String, Object>> getActivityData() { return activityData; }
    public void setActivityData(List<Map<String, Object>> activityData) { this.activityData = activityData; }
    public List<Map<String, Object>> getIssueDistribution() { return issueDistribution; }
    public void setIssueDistribution(List<Map<String, Object>> issueDistribution) { this.issueDistribution = issueDistribution; }
    public List<Map<String, Object>> getRecentActivity() { return recentActivity; }
    public void setRecentActivity(List<Map<String, Object>> recentActivity) { this.recentActivity = recentActivity; }
}
