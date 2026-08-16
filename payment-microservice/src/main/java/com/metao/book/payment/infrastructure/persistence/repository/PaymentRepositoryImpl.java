package com.metao.book.payment.infrastructure.persistence.repository;

import com.metao.book.payment.domain.model.aggregate.PaymentAggregate;
import com.metao.book.payment.domain.model.valueobject.OrderId;
import com.metao.book.payment.domain.model.valueobject.PaymentId;
import com.metao.book.payment.domain.model.valueobject.PaymentStatus;
import com.metao.book.payment.application.port.PaymentCreationLockPort;
import com.metao.book.payment.application.port.PaymentUpdateLockPort;
import com.metao.book.payment.domain.repository.PaymentRepository;
import com.metao.book.payment.infrastructure.persistence.entity.PaymentEntity;
import com.metao.book.payment.infrastructure.persistence.mapper.PaymentEntityMapper;
import com.metao.book.shared.spring.persistence.OffsetBasedPageRequest;
import com.metao.book.shared.architecture.OutboundAdapter;
import io.micrometer.observation.annotation.Observed;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

/**
 * Infrastructure implementation of PaymentRepository
 */
@Repository
@OutboundAdapter
@RequiredArgsConstructor
@Observed(name = "payment.persistence.repository", contextualName = "payment-repository")
public class PaymentRepositoryImpl implements PaymentRepository, PaymentCreationLockPort, PaymentUpdateLockPort {

    private final JpaPaymentRepository jpaPaymentRepository;
    private final PaymentEntityMapper paymentEntityMapper;

    @Override
    public PaymentAggregate save(PaymentAggregate payment) {
        PaymentEntity entity = paymentEntityMapper.toEntity(payment);
        jpaPaymentRepository.save(entity);
        // Domain events are in-memory concerns on the aggregate instance; do not reconstruct here.
        return payment;
    }

    @Override
    public PaymentAggregate saveAndFlush(PaymentAggregate payment) {
        PaymentEntity entity = paymentEntityMapper.toEntity(payment);
        jpaPaymentRepository.saveAndFlush(entity);
        // Domain events are in-memory concerns on the aggregate instance; do not reconstruct here.
        return payment;
    }

    public void lockOrderForCreation(OrderId orderId) {
        jpaPaymentRepository.lockOrderForCreation(orderId.value());
    }

    @Override
    public void lock(OrderId orderId) {
        lockOrderForCreation(orderId);
    }

    @Override
    public Optional<PaymentAggregate> findById(PaymentId paymentId) {
        return jpaPaymentRepository.findById(paymentId.value())
            .map(paymentEntityMapper::toDomain);
    }

    @Override
    public Optional<PaymentAggregate> findByIdForUpdate(PaymentId paymentId) {
        return jpaPaymentRepository.findByIdForUpdate(paymentId.value())
            .map(paymentEntityMapper::toDomain);
    }

    @Override
    public Optional<PaymentAggregate> findByOrderId(OrderId orderId) {
        return jpaPaymentRepository.findByOrderId(orderId.value())
            .map(paymentEntityMapper::toDomain);
    }

    @Override
    public List<PaymentAggregate> findByStatus(PaymentStatus status) {
        PaymentEntity.PaymentStatusEntity entityStatus = mapToEntityStatus(status);
        return jpaPaymentRepository.findByStatus(entityStatus)
            .stream()
            .map(paymentEntityMapper::toDomain)
            .toList();
    }

    @Override
    public List<PaymentAggregate> findByStatus(PaymentStatus status, int offset, int limit) {
        PaymentEntity.PaymentStatusEntity entityStatus = mapToEntityStatus(status);
        Pageable pageable = new OffsetBasedPageRequest(offset, limit);
        return jpaPaymentRepository.findByStatus(entityStatus, pageable)
            .stream()
            .map(paymentEntityMapper::toDomain)
            .toList();
    }

    @Override
    public List<PaymentAggregate> findAll(int offset, int limit) {
        Pageable pageable = new OffsetBasedPageRequest(offset, limit);
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
    public void delete(PaymentAggregate payment) {
        jpaPaymentRepository.deleteById(payment.getId().value());
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
