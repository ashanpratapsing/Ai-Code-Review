package com.student.demo.service;

import com.student.demo.entity.AnalysisHistory;
import com.student.demo.entity.User;
import com.student.demo.repository.HistoryRepository;
import com.student.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HistoryService {

    @Autowired
    private HistoryRepository historyRepository;

    @Autowired
    private UserRepository userRepository;

    public List<AnalysisHistory> getUserHistory() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        return historyRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    public AnalysisHistory saveHistory(String codeSnippet, String resultJson, Integer score) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        AnalysisHistory history = new AnalysisHistory();
        history.setUser(user);
        history.setCodeSnippet(codeSnippet);
        history.setResultJson(resultJson);
        history.setScore(score);

        return historyRepository.save(history);
    }
}
