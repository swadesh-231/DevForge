package com.devforge.ai.prompt;

import com.devforge.entity.ChatEvent;
import com.devforge.entity.ChatMessage;
import com.devforge.entity.enums.ChatEventType;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class LlmResponseParser {

    private static final Pattern TAG_PATTERN = Pattern.compile(
            "<(message|file|tool)([^>]*)>([\\s\\S]*?)</\\1>",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    private static final Pattern ATTRIBUTE_PATTERN = Pattern.compile("(\\w+)=\"([^\"]*)\"");

    private final ObjectMapper objectMapper;

    public List<ChatEvent> parseChatEvents(String fullResponse, ChatMessage parentMessage) {
        List<ChatEvent> events = new ArrayList<>();
        if (fullResponse == null || fullResponse.isBlank()) {
            return events;
        }

        Matcher matcher = TAG_PATTERN.matcher(fullResponse);
        int sequenceOrder = 1;

        while (matcher.find()) {
            Map<String, String> attributes = extractAttributes(matcher.group(2));
            ChatEvent event = toEvent(
                    matcher.group(1).toLowerCase(),
                    attributes,
                    matcher.group(3).trim(),
                    parentMessage,
                    sequenceOrder);

            if (event != null) {
                events.add(event);
                sequenceOrder++;
            }
        }
        return events;
    }

    private ChatEvent toEvent(String tagName,
                              Map<String, String> attributes,
                              String content,
                              ChatMessage parentMessage,
                              int sequenceOrder) {
        ChatEvent.ChatEventBuilder builder = ChatEvent.builder()
                .chatMessage(parentMessage)
                .content(content)
                .sequenceOrder(sequenceOrder);

        switch (tagName) {
            case "message" -> builder
                    .type(ChatEventType.MESSAGE)
                    .metadata(toJson(Map.of("phase", attributes.getOrDefault("phase", "unknown"))));
            case "file" -> {
                String filePath = attributes.get("path");
                if (filePath == null || filePath.isBlank()) {
                    log.warn("Skipping a file tag emitted without a path attribute");
                    return null;
                }
                builder.type(ChatEventType.FILE_EDIT).filePath(filePath);
            }
            case "tool" -> builder
                    .type(ChatEventType.TOOL_LOG)
                    .metadata(toJson(Map.of("paths", splitArgs(attributes.get("args")))));
            default -> {
                log.warn("Ignoring unsupported tag <{}>", tagName);
                return null;
            }
        }
        return builder.build();
    }

    private static List<String> splitArgs(String args) {
        if (args == null || args.isBlank()) {
            return List.of();
        }
        return Arrays.stream(args.split(","))
                .map(String::trim)
                .filter(argument -> !argument.isEmpty())
                .toList();
    }

    private String toJson(Map<String, Object> metadata) {
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JacksonException exception) {
            log.warn("Could not serialise chat event metadata", exception);
            return null;
        }
    }

    private static Map<String, String> extractAttributes(String attributeString) {
        Map<String, String> attributes = new HashMap<>();
        if (attributeString == null) {
            return attributes;
        }

        Matcher matcher = ATTRIBUTE_PATTERN.matcher(attributeString);
        while (matcher.find()) {
            attributes.put(matcher.group(1).toLowerCase(), matcher.group(2));
        }
        return attributes;
    }
}
