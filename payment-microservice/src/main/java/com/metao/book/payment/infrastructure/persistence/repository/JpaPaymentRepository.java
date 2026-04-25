package com.metao.book.payment.infrastructure.persistence.repository;

import com.metao.book.payment.domain.model.valueobject.PaymentId;
import com.metao.book.payment.infrastructure.persistence.entity.PaymentEntity;
import io.micrometer.core.annotation.Timed;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * JPA repository for PaymentEntity
 */
@Repository
public interface JpaPaymentRepository extends JpaRepository<PaymentEntity, PaymentId> {

    @Timed(value = "payment.db.find-by-id-for-update")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PaymentEntity p WHERE p.id = :paymentId")
    Optional<PaymentEntity> findByIdForUpdate(@Param("paymentId") PaymentId paymentId);

    @Timed(value = "payment.db.find-by-order-id")
    Optional<PaymentEntity> findByOrderId(String orderId);

    @Timed(value = "payment.db.find-by-status")
    List<PaymentEntity> findByStatus(PaymentEntity.PaymentStatusEntity status);

    @Timed(value = "payment.db.find-by-status-paged")
    List<PaymentEntity> findByStatus(PaymentEntity.PaymentStatusEntity status, Pageable pageable);

    @Timed(value = "payment.db.exists-by-order-id")
    boolean existsByOrderId(String orderId);

    @Timed(value = "payment.db.count-by-status")
    long countByStatus(PaymentEntity.PaymentStatusEntity status);
}
