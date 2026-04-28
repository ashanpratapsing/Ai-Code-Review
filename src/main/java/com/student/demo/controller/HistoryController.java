package com.student.demo.controller;

import com.student.demo.entity.AnalysisHistory;
import com.student.demo.service.HistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/history")
public class HistoryController {

    @Autowired
    private HistoryService historyService;

    @GetMapping
    public List<AnalysisHistory> getHistory() {
        return historyService.getUserHistory();
    }

    @PostMapping("/save")
    public AnalysisHistory saveHistory(@RequestBody Map<String, Object> payload) {
        String codeSnippet = (String) payload.get("codeSnippet");
        String resultJson = (String) payload.get("resultJson");
        Integer score = (Integer) payload.get("score");
        return historyService.saveHistory(codeSnippet, resultJson, score);
    }
}
