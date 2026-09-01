package com.devforge.service;

import com.devforge.dto.chat.StreamResponse;
import reactor.core.publisher.Flux;

public interface AIgenerationService {
    Flux<StreamResponse> streamResponse(String message, Long projectId);
}
