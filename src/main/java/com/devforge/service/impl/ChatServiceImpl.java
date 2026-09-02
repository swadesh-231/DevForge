package com.devforge.service.impl;

import com.devforge.dto.chat.ChatMessageResponse;
import com.devforge.dto.chat.ChatSessionResponse;
import com.devforge.dto.chat.CreateChatSessionRequest;
import com.devforge.entity.ChatSession;
import com.devforge.entity.Project;
import com.devforge.entity.User;
import com.devforge.entity.enums.ProjectPermission;
import com.devforge.exception.ResourceNotFoundException;
import com.devforge.mapper.ChatMapper;
import com.devforge.repository.ChatMessageRepository;
import com.devforge.repository.ChatSessionRepository;
import com.devforge.repository.UserRepository;
import com.devforge.security.CurrentUserProvider;
import com.devforge.security.access.ProjectAccessGuard;
import com.devforge.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatServiceImpl implements ChatService {

    private static final String DEFAULT_SESSION_TITLE = "New chat";

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final ChatMapper chatMapper;
    private final ProjectAccessGuard projectAccessGuard;
    private final CurrentUserProvider currentUserProvider;

    @Override
    public List<ChatSessionResponse> getProjectSessions(Long projectId) {
        projectAccessGuard.require(projectId, ProjectPermission.VIEW);
        return chatMapper.toSessionResponses(activeSessions(projectId));
    }

    @Override
    @Transactional
    public ChatSessionResponse createSession(Long projectId, CreateChatSessionRequest request) {
        Project project = projectAccessGuard.requireProject(projectId, ProjectPermission.EDIT);
        Long userId = currentUserProvider.requireUserId();

        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        ChatSession session = chatSessionRepository.save(ChatSession.builder()
                .project(project)
                .user(user)
                .title(request.title() == null || request.title().isBlank()
                        ? DEFAULT_SESSION_TITLE
                        : request.title().trim())
                .build());

        return chatMapper.toSessionResponse(session);
    }

    @Override
    public List<ChatMessageResponse> getSessionMessages(Long projectId, Long sessionId) {
        projectAccessGuard.require(projectId, ProjectPermission.VIEW);
        ChatSession session = requireSession(projectId, sessionId);
        return chatMapper.toMessageResponses(
                chatMessageRepository.findBySessionIdWithEvents(session.getId()));
    }

    @Override
    public List<ChatMessageResponse> getProjectChatHistory(Long projectId) {
        projectAccessGuard.require(projectId, ProjectPermission.VIEW);

        return activeSessions(projectId).stream()
                .findFirst()
                .map(session -> chatMapper.toMessageResponses(
                        chatMessageRepository.findBySessionIdWithEvents(session.getId())))
                .orElseGet(List::of);
    }

    @Override
    @Transactional
    public void deleteSession(Long projectId, Long sessionId) {
        projectAccessGuard.require(projectId, ProjectPermission.EDIT);
        requireSession(projectId, sessionId).setDeletedAt(Instant.now());
    }

    private List<ChatSession> activeSessions(Long projectId) {
        return chatSessionRepository.findActiveByProjectAndUser(
                projectId, currentUserProvider.requireUserId());
    }

    private ChatSession requireSession(Long projectId, Long sessionId) {
        return chatSessionRepository
                .findActiveByIdAndProjectAndUser(sessionId, projectId, currentUserProvider.requireUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Chat session", sessionId));
    }
}
