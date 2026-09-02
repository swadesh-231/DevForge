package com.devforge.mapper;

import com.devforge.dto.billing.SubscriptionResponse;
import com.devforge.entity.Subscription;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class, uses = PlanMapper.class)
public interface SubscriptionMapper {

    SubscriptionResponse toSubscriptionResponse(Subscription subscription);
}
