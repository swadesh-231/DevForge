package com.devforge.mapper;

import com.devforge.dto.billing.PlanLimitsResponse;
import com.devforge.dto.billing.PlanResponse;
import com.devforge.entity.Plan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = MapperConfiguration.class)
public interface PlanMapper {

    PlanResponse toPlanResponse(Plan plan);

    List<PlanResponse> toPlanResponses(List<Plan> plans);

    @Mapping(target = "planName", source = "name")
    PlanLimitsResponse toPlanLimitsResponse(Plan plan);
}
