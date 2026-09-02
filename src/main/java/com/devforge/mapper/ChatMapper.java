package com.devforge.mapper;

import com.devforge.dto.chat.ChatEventResponse;
import com.devforge.dto.chat.ChatMessageResponse;
import com.devforge.dto.chat.ChatSessionResponse;
import com.devforge.entity.ChatEvent;
import com.devforge.entity.ChatMessage;
import com.devforge.entity.ChatSession;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = MapperConfiguration.class)
public interface ChatMapper {

    @Mapping(target = "projectId", source = "project.id")
    ChatSessionResponse toSessionResponse(ChatSession chatSession);

    List<ChatSessionResponse> toSessionResponses(List<ChatSession> chatSessions);

    @Mapping(target = "sessionId", source = "chatSession.id")
    ChatMessageResponse toMessageResponse(ChatMessage chatMessage);

    List<ChatMessageResponse> toMessageResponses(List<ChatMessage> chatMessages);

    ChatEventResponse toEventResponse(ChatEvent chatEvent);
}
