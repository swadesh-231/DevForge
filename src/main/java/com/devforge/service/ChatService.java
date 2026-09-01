package com.devforge.service;

import com.devforge.dto.chat.ChatMessageResponse;

import java.util.List;

public interface ChatService {
    List<ChatMessageResponse> getProjectChatHistory(Long projectId);
}
