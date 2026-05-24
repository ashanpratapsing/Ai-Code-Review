package com.student.demo.service;

import com.student.demo.config.RabbitMQConfig;
import com.student.demo.dto.CodeExecutionDTO.*;
import com.student.demo.entity.CodeExecution;
import com.student.demo.entity.User;
import com.student.demo.repository.CodeExecutionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CodeExecutionService {

    private static final Logger logger = LoggerFactory.getLogger(CodeExecutionService.class);
    private static final int MEMORY_LIMIT_MB = 128;
    private static final int TIMEOUT_SECONDS = 15;

    private final CodeExecutionRepository codeExecutionRepository;
    private final OwnershipService ownershipService;
    private final RabbitTemplate rabbitTemplate;

    public CodeExecutionService(CodeExecutionRepository codeExecutionRepository,
                                OwnershipService ownershipService,
                                RabbitTemplate rabbitTemplate) {
        this.codeExecutionRepository = codeExecutionRepository;
        this.ownershipService = ownershipService;
        this.rabbitTemplate = rabbitTemplate;
    }

    public ExecutionResponse executeForUser(ExecutionRequest request, User user) {
        logger.info("Initiating synchronous code execution request for user: {}", user.getId());

        // 1. Create initial PENDING execution record
        CodeExecution execution = new CodeExecution();
        execution.setUser(user);
        execution.setLanguage(request.getLanguage().toUpperCase());
        execution.setSourceCode(request.getCode());
        execution.setStatus("PENDING");
        execution.setMemoryLimitMb(MEMORY_LIMIT_MB);
        execution.setTimeoutMs(TIMEOUT_SECONDS * 1000);

        if (request.getCodeFileId() != null) {
            execution.setCodeFile(ownershipService.requireOwnedCodeFile(request.getCodeFileId(), user.getId()));
        }

        codeExecutionRepository.save(execution);
        
        // Populate the execution ID for the worker
        request.setExecutionId(execution.getId());

        // 2. Publish to RabbitMQ and block waiting for reply
        try {
            logger.info("Publishing execution job {} to RabbitMQ. Waiting for reply...", execution.getId());
            
            Object reply = rabbitTemplate.convertSendAndReceive(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.EXECUTION_ROUTING_KEY,
                    request
            );

            if (reply instanceof ExecutionResponse) {
                logger.info("Received execution response from worker for job ID: {}", execution.getId());
                return (ExecutionResponse) reply;
            } else {
                logger.error("Invalid reply received from RabbitMQ worker for job ID: {}. Reply: {}", execution.getId(), reply);
                return createErrorResponse(execution.getId(), "Received invalid reply format from Execution Worker.");
            }
        } catch (Exception e) {
            logger.error("Failed to perform request-reply execution via RabbitMQ for job ID: {}", execution.getId(), e);
            return createErrorResponse(execution.getId(), "Execution service communication failure: " + e.getMessage());
        }
    }

    private ExecutionResponse createErrorResponse(Long executionId, String errorMessage) {
        ExecutionResponse response = new ExecutionResponse();
        response.setExecutionId(executionId);
        response.setStatus("ERROR");
        response.setCompileError(errorMessage);
        return response;
    }
}
