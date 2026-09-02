package com.devforge.service.impl;

import com.devforge.ai.advisor.FileTreeAdvisor;
import com.devforge.ai.prompt.CodeGenerationPrompt;
import com.devforge.ai.tools.CodeGenerationTools;
import com.devforge.dto.chat.SendMessageRequest;
import com.devforge.dto.chat.StreamResponse;
import com.devforge.entity.enums.ProjectPermission;
import com.devforge.security.CurrentUserProvider;
import com.devforge.security.access.ProjectAccessGuard;
import com.devforge.service.AiGenerationService;
import com.devforge.service.ChatTurnService;
import com.devforge.service.ChatTurnService.AssistantTurn;
import com.devforge.service.ProjectFileService;
import com.devforge.service.UsageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiGenerationServiceImpl implements AiGenerationService {

    private static final String GENERATION_FAILED = "Generation failed. Please try again.";

    private final ChatClient chatClient;
    private final ChatTurnService chatTurnService;
    private final ProjectFileService projectFileService;
    private final FileTreeAdvisor fileTreeAdvisor;
    private final ProjectAccessGuard projectAccessGuard;
    private final CurrentUserProvider currentUserProvider;
    private final UsageService usageService;

    @Override
    public Flux<StreamResponse> streamResponse(SendMessageRequest request) {
        Long projectId = request.projectId();
        Long userId = currentUserProvider.requireUserId();

        projectAccessGuard.require(projectId, userId, ProjectPermission.EDIT);
        usageService.assertWithinDailyTokenLimit(userId);

        Long sessionId = chatTurnService.openSession(projectId, userId, request.sessionId(), request.content());

        StringBuilder responseBuffer = new StringBuilder();
        AtomicReference<Usage> usage = new AtomicReference<>();
        AtomicLong firstTokenAt = new AtomicLong();
        long startedAt = System.currentTimeMillis();

        Flux<StreamResponse> deltas = chatClient.prompt()
                .system(CodeGenerationPrompt.systemPrompt())
                .user(request.content())
                .tools(new CodeGenerationTools(projectFileService, projectId))
                .advisors(advisors -> advisors
                        .advisors(fileTreeAdvisor)
                        .param(FileTreeAdvisor.PROJECT_ID_PARAM, projectId))
                .stream()
                .chatResponse()
                .doOnNext(response -> {
                    if (response.getMetadata().getUsage() != null) {
                        usage.set(response.getMetadata().getUsage());
                    }
                })
                .mapNotNull(response -> response.getResult() == null
                        ? null
                        : response.getResult().getOutput().getText())
                .filter(text -> !text.isEmpty())
                .doOnNext(text -> {
                    firstTokenAt.compareAndSet(0L, System.currentTimeMillis());
                    responseBuffer.append(text);
                })
                .map(StreamResponse::delta);

        Mono<StreamResponse> completion = record(sessionId, request, responseBuffer, usage, firstTokenAt, startedAt)
                .thenReturn(StreamResponse.complete());

        return Flux.concat(Flux.just(StreamResponse.started(sessionId)), deltas, completion)
                .onErrorResume(error -> {
                    log.error("AI generation failed for project {}", projectId, error);
                    return record(sessionId, request, responseBuffer, usage, firstTokenAt, startedAt)
                            .thenReturn(StreamResponse.failed(GENERATION_FAILED))
                            .onErrorReturn(StreamResponse.failed(GENERATION_FAILED));
                });
    }

    private Mono<Void> record(Long sessionId,
                              SendMessageRequest request,
                              StringBuilder responseBuffer,
                              AtomicReference<Usage> usage,
                              AtomicLong firstTokenAt,
                              long startedAt) {
        return Mono.<Void>fromRunnable(() -> chatTurnService.recordAssistantTurn(new AssistantTurn(
                        sessionId,
                        request.content(),
                        responseBuffer.toString(),
                        tokens(usage.get(), Usage::getPromptTokens),
                        tokens(usage.get(), Usage::getCompletionTokens),
                        thinkingSeconds(firstTokenAt.get(), startedAt))))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private static long thinkingSeconds(long firstTokenAt, long startedAt) {
        long reference = firstTokenAt > 0 ? firstTokenAt : System.currentTimeMillis();
        return Math.max(0, (reference - startedAt) / 1000);
    }

    private static int tokens(Usage usage, java.util.function.Function<Usage, Integer> extractor) {
        if (usage == null) {
            return 0;
        }
        Integer value = extractor.apply(usage);
        return value == null ? 0 : value;
    }
}
