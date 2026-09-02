package com.devforge.controller;

import com.devforge.dto.chat.ChatMessageResponse;
import com.devforge.dto.chat.ChatSessionResponse;
import com.devforge.dto.chat.CreateChatSessionRequest;
import com.devforge.dto.chat.SendMessageRequest;
import com.devforge.dto.chat.StreamResponse;
import com.devforge.dto.common.ApiResponse;
import com.devforge.service.AiGenerationService;
import com.devforge.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final AiGenerationService aiGenerationService;
    private final ChatService chatService;

    @PostMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<StreamResponse>> streamChat(@Valid @RequestBody SendMessageRequest request) {
        return aiGenerationService.streamResponse(request)
                .map(data -> ServerSentEvent.<StreamResponse>builder().data(data).build());
    }

    @GetMapping("/projects/{projectId}/sessions")
    public ResponseEntity<ApiResponse<List<ChatSessionResponse>>> getSessions(@PathVariable Long projectId) {
        return ResponseEntity.ok(ApiResponse.ok(chatService.getProjectSessions(projectId)));
    }

    @PostMapping("/projects/{projectId}/sessions")
    public ResponseEntity<ApiResponse<ChatSessionResponse>> createSession(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateChatSessionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(
                chatService.createSession(projectId, request), "Chat session created"));
    }

    @GetMapping("/projects/{projectId}/sessions/{sessionId}/messages")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getSessionMessages(
            @PathVariable Long projectId,
            @PathVariable Long sessionId) {
        return ResponseEntity.ok(ApiResponse.ok(chatService.getSessionMessages(projectId, sessionId)));
    }

    @DeleteMapping("/projects/{projectId}/sessions/{sessionId}")
    public ResponseEntity<ApiResponse<Void>> deleteSession(
            @PathVariable Long projectId,
            @PathVariable Long sessionId) {
        chatService.deleteSession(projectId, sessionId);
        return ResponseEntity.ok(ApiResponse.message("Chat session deleted"));
    }

    @GetMapping("/projects/{projectId}")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getChatHistory(@PathVariable Long projectId) {
        return ResponseEntity.ok(ApiResponse.ok(chatService.getProjectChatHistory(projectId)));
    }
}
