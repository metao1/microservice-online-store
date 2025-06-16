package com.metao.book.payment.infrastructure.persistence.repository;

import com.metao.book.payment.domain.model.valueobject.PaymentId;
import com.metao.book.payment.infrastructure.persistence.entity.PaymentEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA repository for PaymentEntity
 */
@Repository
public interface JpaPaymentRepository extends JpaRepository<PaymentEntity, PaymentId> {

    Optional<PaymentEntity> findByOrderId(String orderId);

    List<PaymentEntity> findByStatus(PaymentEntity.PaymentStatusEntity status);

    boolean existsByOrderId(String orderId);

    long countByStatus(PaymentEntity.PaymentStatusEntity status);
}
