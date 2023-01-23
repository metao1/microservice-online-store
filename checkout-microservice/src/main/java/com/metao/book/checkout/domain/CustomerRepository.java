package com.metao.book.checkout.domain;

import com.metao.book.checkout.application.model.CustomerId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<CustomerEntity, CustomerId> {

    Optional<CustomerEntity> findCustomerEntityByName(String name);
}