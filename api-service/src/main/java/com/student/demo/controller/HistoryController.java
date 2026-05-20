package com.student.demo.controller;

import com.student.demo.entity.AnalysisHistory;
import com.student.demo.service.HistoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/history")
public class HistoryController {

    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping
    public List<AnalysisHistory> getHistory() {
        return historyService.getUserHistory();
    }

    @PostMapping("/save")
    public AnalysisHistory saveHistory(@RequestBody Map<String, Object> payload) {
        String codeSnippet = (String) payload.get("codeSnippet");
        String resultJson = (String) payload.get("resultJson");
        Integer score = payload.get("score") instanceof Number n ? n.intValue() : null;
        Long codeFileId = payload.get("codeFileId") instanceof Number n ? n.longValue() : null;
        return historyService.saveHistory(codeSnippet, resultJson, score, codeFileId);
    }
}
