package com.student.demo.service;

import com.student.demo.entity.AnalysisHistory;
import com.student.demo.entity.CodeFile;
import com.student.demo.entity.User;
import com.student.demo.repository.HistoryRepository;
import com.student.demo.security.SecurityUtil;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HistoryService {

    private final HistoryRepository historyRepository;
    private final SecurityUtil securityUtil;
    private final OwnershipService ownershipService;

    public HistoryService(HistoryRepository historyRepository,
                          SecurityUtil securityUtil,
                          OwnershipService ownershipService) {
        this.historyRepository = historyRepository;
        this.securityUtil = securityUtil;
        this.ownershipService = ownershipService;
    }

    public List<AnalysisHistory> getUserHistory() {
        Long userId = securityUtil.requireCurrentUserId();
        return historyRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public AnalysisHistory saveHistory(String codeSnippet, String resultJson, Integer score, Long codeFileId) {
        User user = securityUtil.requireCurrentUser();

        AnalysisHistory history = new AnalysisHistory();
        history.setUser(user);
        history.setCodeSnippet(codeSnippet);
        history.setResultJson(resultJson);
        history.setScore(score);

        if (codeFileId != null) {
            CodeFile file = ownershipService.requireOwnedCodeFile(codeFileId, user.getId());
            history.setCodeFileId(file.getId());
            if (file.getProject() != null) {
                history.setProjectId(file.getProject().getId());
            }
        }

        return historyRepository.save(history);
    }
}
