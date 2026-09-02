package com.devforge.service;

import com.devforge.dto.chat.SendMessageRequest;
import com.devforge.dto.chat.StreamResponse;
import reactor.core.publisher.Flux;

public interface AiGenerationService {

    Flux<StreamResponse> streamResponse(SendMessageRequest request);
}
