package com.devforge.repository;

import com.devforge.entity.Subscription;
import com.devforge.entity.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    @Query("""
            SELECT s FROM Subscription s
            JOIN FETCH s.plan
            WHERE s.user.id = :userId
              AND s.status IN :statuses
            ORDER BY s.createdAt DESC
            LIMIT 1
            """)
    Optional<Subscription> findCurrentByUserId(@Param("userId") Long userId,
                                               @Param("statuses") Collection<SubscriptionStatus> statuses);

    @Query("""
            SELECT s FROM Subscription s
            JOIN FETCH s.plan
            WHERE s.stripeSubscriptionId = :stripeSubscriptionId
            """)
    Optional<Subscription> findByStripeSubscriptionId(@Param("stripeSubscriptionId") String stripeSubscriptionId);

    boolean existsByStripeSubscriptionId(String stripeSubscriptionId);
}
