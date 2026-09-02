package com.devforge.service.impl;

import com.devforge.dto.billing.PaymentResponse;
import com.devforge.dto.common.PageResponse;
import com.devforge.mapper.PaymentMapper;
import com.devforge.repository.PaymentRepository;
import com.devforge.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    @Override
    public PageResponse<PaymentResponse> getUserPayments(Long userId, Pageable pageable) {
        return PageResponse.of(
                paymentRepository.findByUserId(userId, pageable), paymentMapper::toPaymentResponse);
    }
}
