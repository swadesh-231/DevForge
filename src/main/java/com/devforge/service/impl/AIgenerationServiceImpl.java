package com.devforge.service.impl;

import com.devforge.dto.chat.StreamResponse;
import com.devforge.service.AIgenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class AIgenerationServiceImpl implements AIgenerationService {
    @Override
    public Flux<StreamResponse> streamResponse(String message, Long projectId) {
        return null;
    }
}
