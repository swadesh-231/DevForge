package com.devforge.service.impl;

import com.devforge.config.BillingProperties;
import com.devforge.dto.billing.PlanLimitsResponse;
import com.devforge.dto.billing.SubscriptionResponse;
import com.devforge.entity.Plan;
import com.devforge.entity.Subscription;
import com.devforge.entity.User;
import com.devforge.entity.enums.SubscriptionStatus;
import com.devforge.exception.ResourceNotFoundException;
import com.devforge.mapper.PlanMapper;
import com.devforge.mapper.SubscriptionMapper;
import com.devforge.repository.PlanRepository;
import com.devforge.repository.ProjectMemberRepository;
import com.devforge.repository.SubscriptionRepository;
import com.devforge.repository.UserRepository;
import com.devforge.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final PlanMapper planMapper;
    private final BillingProperties billingProperties;

    @Override
    public Optional<SubscriptionResponse> getCurrentSubscription(Long userId) {
        return findEntitlingSubscription(userId).map(subscriptionMapper::toSubscriptionResponse);
    }

    @Override
    public PlanLimitsResponse getEffectiveLimits(Long userId) {
        return findEntitlingSubscription(userId)
                .map(Subscription::getPlan)
                .or(() -> planRepository.findByNameIgnoreCase(billingProperties.freePlanName()))
                .map(planMapper::toPlanLimitsResponse)
                .orElseGet(billingProperties::freeLimits);
    }

    @Override
    public boolean canCreateProject(Long userId) {
        return projectMemberRepository.countProjectsOwnedByUser(userId) < getEffectiveLimits(userId).maxProjects();
    }

    @Override
    @Transactional
    public void activateSubscription(Long userId, Long planId, String stripeSubscriptionId, String stripeCustomerId) {
        if (subscriptionRepository.existsByStripeSubscriptionId(stripeSubscriptionId)) {
            log.debug("Subscription {} already recorded", stripeSubscriptionId);
            return;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        Plan plan = requirePlan(planId);

        if (stripeCustomerId != null && user.getStripeCustomerId() == null) {
            user.setStripeCustomerId(stripeCustomerId);
        }

        subscriptionRepository.save(Subscription.builder()
                .user(user)
                .plan(plan)
                .status(SubscriptionStatus.INCOMPLETE)
                .stripeSubscriptionId(stripeSubscriptionId)
                .stripeCustomerId(stripeCustomerId)
                .stripePriceId(plan.getStripePriceId())
                .build());

        log.info("Recorded subscription {} for user {} on plan {}", stripeSubscriptionId, userId, planId);
    }

    @Override
    @Transactional
    public void updateSubscription(String stripeSubscriptionId,
                                   SubscriptionStatus status,
                                   Instant periodStart,
                                   Instant periodEnd,
                                   Boolean cancelAtPeriodEnd,
                                   Long planId) {
        Subscription subscription = requireSubscription(stripeSubscriptionId);

        if (status != null) {
            subscription.setStatus(status);
            if (status == SubscriptionStatus.CANCELED && subscription.getEndedAt() == null) {
                subscription.setEndedAt(Instant.now());
            }
        }
        if (periodStart != null) {
            subscription.setCurrentPeriodStart(periodStart);
        }
        if (periodEnd != null) {
            subscription.setCurrentPeriodEnd(periodEnd);
        }
        if (cancelAtPeriodEnd != null) {
            subscription.setCancelAtPeriodEnd(cancelAtPeriodEnd);
        }
        if (planId != null && !planId.equals(subscription.getPlan().getId())) {
            Plan plan = requirePlan(planId);
            subscription.setPlan(plan);
            subscription.setStripePriceId(plan.getStripePriceId());
        }
    }

    @Override
    @Transactional
    public void renewSubscriptionPeriod(String stripeSubscriptionId, Instant periodStart, Instant periodEnd) {
        Subscription subscription = requireSubscription(stripeSubscriptionId);

        subscription.setCurrentPeriodStart(
                periodStart != null ? periodStart : subscription.getCurrentPeriodEnd());
        subscription.setCurrentPeriodEnd(periodEnd);

        if (subscription.getStatus() == SubscriptionStatus.PAST_DUE
                || subscription.getStatus() == SubscriptionStatus.INCOMPLETE
                || subscription.getStatus() == SubscriptionStatus.UNPAID) {
            subscription.setStatus(SubscriptionStatus.ACTIVE);
        }
    }

    @Override
    @Transactional
    public void markSubscriptionPastDue(String stripeSubscriptionId) {
        Subscription subscription = requireSubscription(stripeSubscriptionId);

        if (subscription.getStatus() == SubscriptionStatus.PAST_DUE) {
            return;
        }
        subscription.setStatus(SubscriptionStatus.PAST_DUE);
        log.info("Subscription {} marked past due", stripeSubscriptionId);
    }

    @Override
    @Transactional
    public void cancelSubscription(String stripeSubscriptionId) {
        Subscription subscription = requireSubscription(stripeSubscriptionId);

        subscription.setStatus(SubscriptionStatus.CANCELED);
        subscription.setCanceledAt(Instant.now());
        subscription.setEndedAt(Instant.now());
        subscription.setCancelAtPeriodEnd(false);
        log.info("Subscription {} canceled", stripeSubscriptionId);
    }

    private Optional<Subscription> findEntitlingSubscription(Long userId) {
        return subscriptionRepository.findCurrentByUserId(userId, Subscription.ENTITLING_STATUSES);
    }

    private Plan requirePlan(Long planId) {
        return planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan", planId));
    }

    private Subscription requireSubscription(String stripeSubscriptionId) {
        return subscriptionRepository.findByStripeSubscriptionId(stripeSubscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", stripeSubscriptionId));
    }
}
