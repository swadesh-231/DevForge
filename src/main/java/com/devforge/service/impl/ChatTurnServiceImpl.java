package com.devforge.service.impl;

import com.devforge.ai.prompt.LlmResponseParser;
import com.devforge.entity.ChatEvent;
import com.devforge.entity.ChatMessage;
import com.devforge.entity.ChatSession;
import com.devforge.entity.Project;
import com.devforge.entity.User;
import com.devforge.entity.enums.ChatEventType;
import com.devforge.entity.enums.MessageRole;
import com.devforge.exception.ResourceNotFoundException;
import com.devforge.repository.ChatMessageRepository;
import com.devforge.repository.ChatSessionRepository;
import com.devforge.repository.ProjectRepository;
import com.devforge.repository.UserRepository;
import com.devforge.service.ChatTurnService;
import com.devforge.service.ProjectFileService;
import com.devforge.service.UsageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChatTurnServiceImpl implements ChatTurnService {

    private static final int MAX_TITLE_LENGTH = 200;

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final LlmResponseParser llmResponseParser;
    private final ProjectFileService projectFileService;
    private final UsageService usageService;

    @Override
    public Long openSession(Long projectId, Long userId, Long sessionId, String firstMessage) {
        if (sessionId != null) {
            return chatSessionRepository.findActiveByIdAndProjectAndUser(sessionId, projectId, userId)
                    .map(ChatSession::getId)
                    .orElseThrow(() -> new ResourceNotFoundException("Chat session", sessionId));
        }

        Project project = projectRepository.findByIdAndDeletedAtIsNull(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", projectId));
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        ChatSession session = chatSessionRepository.save(ChatSession.builder()
                .project(project)
                .user(user)
                .title(titleFrom(firstMessage))
                .build());

        log.debug("Opened chat session {} for project {}", session.getId(), projectId);
        return session.getId();
    }

    @Override
    public void recordAssistantTurn(AssistantTurn turn) {
        ChatSession session = chatSessionRepository.findById(turn.sessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Chat session", turn.sessionId()));

        int nextSequenceOrder = chatMessageRepository.findMaxSequenceOrder(session.getId()).orElse(0) + 1;

        chatMessageRepository.save(ChatMessage.builder()
                .chatSession(session)
                .role(MessageRole.USER)
                .sequenceOrder(nextSequenceOrder)
                .content(turn.userMessage())
                .tokensIn(turn.promptTokens())
                .tokensOut(0)
                .build());

        ChatMessage assistantMessage = ChatMessage.builder()
                .chatSession(session)
                .role(MessageRole.ASSISTANT)
                .sequenceOrder(nextSequenceOrder + 1)
                .content(turn.assistantResponse())
                .tokensIn(0)
                .tokensOut(turn.completionTokens())
                .build();

        List<ChatEvent> events = new ArrayList<>();
        events.add(ChatEvent.builder()
                .chatMessage(assistantMessage)
                .type(ChatEventType.THOUGHT)
                .sequenceOrder(0)
                .content("Thought for " + turn.thinkingSeconds() + "s")
                .build());
        events.addAll(llmResponseParser.parseChatEvents(turn.assistantResponse(), assistantMessage));

        assistantMessage.getEvents().addAll(events);
        chatMessageRepository.save(assistantMessage);

        applyFileEdits(session.getProject().getId(), events);

        session.setUpdatedAt(Instant.now());
        usageService.recordUsage(session.getUser().getId(), turn.promptTokens(), turn.completionTokens());
    }

    private void applyFileEdits(Long projectId, List<ChatEvent> events) {
        events.stream()
                .filter(event -> event.getType() == ChatEventType.FILE_EDIT)
                .forEach(event -> projectFileService.saveFile(projectId, event.getFilePath(), event.getContent()));
    }

    private static String titleFrom(String firstMessage) {
        if (firstMessage == null || firstMessage.isBlank()) {
            return "New chat";
        }
        String title = firstMessage.strip().replaceAll("\\s+", " ");
        return title.length() <= MAX_TITLE_LENGTH ? title : title.substring(0, MAX_TITLE_LENGTH - 1) + "…";
    }
}
