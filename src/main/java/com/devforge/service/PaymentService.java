package com.devforge.service;

import com.devforge.dto.billing.PaymentResponse;
import com.devforge.dto.common.PageResponse;
import org.springframework.data.domain.Pageable;

public interface PaymentService {

    PageResponse<PaymentResponse> getUserPayments(Long userId, Pageable pageable);
}
