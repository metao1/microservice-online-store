package com.metao.book.payment.infrastructure.persistence.repository;

import com.metao.book.payment.domain.model.valueobject.PaymentId;
import com.metao.book.payment.infrastructure.persistence.entity.PaymentEntity;
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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PaymentEntity p WHERE p.id = :paymentId")
    Optional<PaymentEntity> findByIdForUpdate(@Param("paymentId") PaymentId paymentId);

    Optional<PaymentEntity> findByOrderId(String orderId);

    List<PaymentEntity> findByStatus(PaymentEntity.PaymentStatusEntity status);

    List<PaymentEntity> findByStatus(PaymentEntity.PaymentStatusEntity status, Pageable pageable);

    boolean existsByOrderId(String orderId);

    long countByStatus(PaymentEntity.PaymentStatusEntity status);
}
