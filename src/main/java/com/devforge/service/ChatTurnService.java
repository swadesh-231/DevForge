package com.devforge.service;

public interface ChatTurnService {

    Long openSession(Long projectId, Long userId, Long sessionId, String firstMessage);

    void recordAssistantTurn(AssistantTurn turn);

    record AssistantTurn(
            Long sessionId,
            String userMessage,
            String assistantResponse,
            int promptTokens,
            int completionTokens,
            long thinkingSeconds
    ) {
    }
}
