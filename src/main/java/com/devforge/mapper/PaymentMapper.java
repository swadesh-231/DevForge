package com.devforge.mapper;

import com.devforge.dto.billing.PaymentResponse;
import com.devforge.entity.Payment;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface PaymentMapper {

    PaymentResponse toPaymentResponse(Payment payment);
}
