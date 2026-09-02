package com.devforge.ai.advisor;

import com.devforge.dto.file.FileNode;
import com.devforge.service.ProjectFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileTreeAdvisor implements StreamAdvisor {

    public static final String PROJECT_ID_PARAM = "projectId";

    private static final String ADVISOR_NAME = "fileTreeAdvisor";

    private final ProjectFileService projectFileService;

    @Override
    public @NonNull Flux<ChatClientResponse> adviseStream(@NonNull ChatClientRequest chatClientRequest,
                                                          @NonNull StreamAdvisorChain streamAdvisorChain) {
        Object projectId = chatClientRequest.context().get(PROJECT_ID_PARAM);
        if (projectId == null) {
            log.warn("{} invoked without a {} context parameter", ADVISOR_NAME, PROJECT_ID_PARAM);
            return streamAdvisorChain.nextStream(chatClientRequest);
        }
        return streamAdvisorChain.nextStream(
                withFileTree(chatClientRequest, Long.valueOf(projectId.toString())));
    }

    private ChatClientRequest withFileTree(ChatClientRequest request, Long projectId) {
        List<Message> incoming = request.prompt().getInstructions();
        List<Message> messages = new ArrayList<>();

        incoming.stream()
                .filter(message -> message.getMessageType() == MessageType.SYSTEM)
                .findFirst()
                .ifPresent(messages::add);

        messages.add(new SystemMessage(renderFileTree(projectId)));

        incoming.stream()
                .filter(message -> message.getMessageType() != MessageType.SYSTEM)
                .forEach(messages::add);

        return request.mutate()
                .prompt(new Prompt(messages, request.prompt().getOptions()))
                .build();
    }

    private String renderFileTree(Long projectId) {
        String paths = projectFileService.getFileTree(projectId).files().stream()
                .map(FileNode::path)
                .collect(Collectors.joining("\n"));

        return """
                ---- FILE_TREE ----
                %s
                ---- END FILE_TREE ----""".formatted(paths.isEmpty() ? "(empty project)" : paths);
    }

    @Override
    public @NonNull String getName() {
        return ADVISOR_NAME;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 100;
    }
}
