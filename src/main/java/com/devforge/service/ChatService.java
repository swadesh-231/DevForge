package com.devforge.service;

import com.devforge.dto.chat.ChatMessageResponse;
import com.devforge.dto.chat.ChatSessionResponse;
import com.devforge.dto.chat.CreateChatSessionRequest;

import java.util.List;

public interface ChatService {

    List<ChatSessionResponse> getProjectSessions(Long projectId);

    ChatSessionResponse createSession(Long projectId, CreateChatSessionRequest request);

    List<ChatMessageResponse> getSessionMessages(Long projectId, Long sessionId);

    List<ChatMessageResponse> getProjectChatHistory(Long projectId);

    void deleteSession(Long projectId, Long sessionId);
}
