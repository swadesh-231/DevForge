package com.devforge.service.impl;

import com.devforge.dto.chat.ChatMessageResponse;
import com.devforge.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    @Override
    public List<ChatMessageResponse> getProjectChatHistory(Long projectId) {
        return List.of();
    }
}
