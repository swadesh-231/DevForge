package com.devforge.service.impl;

import com.devforge.dto.billing.PlanResponse;
import com.devforge.exception.ResourceNotFoundException;
import com.devforge.mapper.PlanMapper;
import com.devforge.repository.PlanRepository;
import com.devforge.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlanServiceImpl implements PlanService {

    private final PlanRepository planRepository;
    private final PlanMapper planMapper;

    @Override
    public List<PlanResponse> getActivePlans() {
        return planMapper.toPlanResponses(
                planRepository.findByIsActiveTrueOrderByDisplayOrderAscAmountMinorAsc());
    }

    @Override
    public PlanResponse getPlan(Long planId) {
        return planRepository.findById(planId)
                .map(planMapper::toPlanResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Plan", planId));
    }
}
