package com.metao.book.payment.infrastructure.persistence.repository;

import com.metao.book.payment.domain.model.aggregate.Payment;
import com.metao.book.payment.domain.model.valueobject.OrderId;
import com.metao.book.payment.domain.model.valueobject.PaymentId;
import com.metao.book.payment.domain.model.valueobject.PaymentStatus;
import com.metao.book.payment.domain.repository.PaymentRepository;
import com.metao.book.payment.infrastructure.persistence.entity.PaymentEntity;
import com.metao.book.payment.infrastructure.persistence.mapper.PaymentEntityMapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

/**
 * Infrastructure implementation of PaymentRepository
 */
@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {

    private final JpaPaymentRepository jpaPaymentRepository;
    private final PaymentEntityMapper paymentEntityMapper;

    @Override
    public Payment save(Payment payment) {
        PaymentEntity entity = paymentEntityMapper.toEntity(payment);
        PaymentEntity savedEntity = jpaPaymentRepository.save(entity);
        return paymentEntityMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Payment> findById(PaymentId paymentId) {
        return jpaPaymentRepository.findById(paymentId)
            .map(paymentEntityMapper::toDomain);
    }

    @Override
    public Optional<Payment> findByOrderId(OrderId orderId) {
        return jpaPaymentRepository.findByOrderId(orderId.value())
            .map(paymentEntityMapper::toDomain);
    }

    @Override
    public List<Payment> findByStatus(PaymentStatus status) {
        PaymentEntity.PaymentStatusEntity entityStatus = mapToEntityStatus(status);
        return jpaPaymentRepository.findByStatus(entityStatus)
            .stream()
            .map(paymentEntityMapper::toDomain)
            .toList();
    }

    @Override
    public List<Payment> findAll(int offset, int limit) {
        Pageable pageable = PageRequest.of(offset / limit, limit);
        return jpaPaymentRepository.findAll(pageable)
            .stream()
            .map(paymentEntityMapper::toDomain)
            .toList();
    }

    @Override
    public boolean existsByOrderId(OrderId orderId) {
        return jpaPaymentRepository.existsByOrderId(orderId.value());
    }

    @Override
    public void delete(Payment payment) {
        jpaPaymentRepository.deleteById(payment.getId());
    }

    @Override
    public long count() {
        return jpaPaymentRepository.count();
    }

    @Override
    public long countByStatus(PaymentStatus status) {
        PaymentEntity.PaymentStatusEntity entityStatus = mapToEntityStatus(status);
        return jpaPaymentRepository.countByStatus(entityStatus);
    }

    /**
     * Map domain PaymentStatus to entity PaymentStatusEntity
     */
    private PaymentEntity.PaymentStatusEntity mapToEntityStatus(PaymentStatus status) {
        return switch (status) {
            case PENDING -> PaymentEntity.PaymentStatusEntity.PENDING;
            case SUCCESSFUL -> PaymentEntity.PaymentStatusEntity.SUCCESSFUL;
            case FAILED -> PaymentEntity.PaymentStatusEntity.FAILED;
            case CANCELLED -> PaymentEntity.PaymentStatusEntity.CANCELLED;
        };
    }
}
