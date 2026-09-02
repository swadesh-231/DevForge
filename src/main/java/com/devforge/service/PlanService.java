package com.devforge.service;

import com.devforge.dto.billing.PlanResponse;

import java.util.List;

public interface PlanService {

    List<PlanResponse> getActivePlans();

    PlanResponse getPlan(Long planId);
}
