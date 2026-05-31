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
    private int currentStreak;
    private int longestStreak;
    private long totalContributions;
    private double weeklyConsistency;
    private Map<String, Map<String, Object>> contributionCalendar;

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
    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }
    public int getLongestStreak() { return longestStreak; }
    public void setLongestStreak(int longestStreak) { this.longestStreak = longestStreak; }
    public long getTotalContributions() { return totalContributions; }
    public void setTotalContributions(long totalContributions) { this.totalContributions = totalContributions; }
    public double getWeeklyConsistency() { return weeklyConsistency; }
    public void setWeeklyConsistency(double weeklyConsistency) { this.weeklyConsistency = weeklyConsistency; }
    public Map<String, Map<String, Object>> getContributionCalendar() { return contributionCalendar; }
    public void setContributionCalendar(Map<String, Map<String, Object>> contributionCalendar) { this.contributionCalendar = contributionCalendar; }
}
