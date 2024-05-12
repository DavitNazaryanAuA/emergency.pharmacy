package com.capstone.emergency.pharmacy.core.vending.repository.mongo;

import com.capstone.emergency.pharmacy.core.vending.repository.model.Order;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends CrudRepository<Order, String> {

    Optional<Order> findByIdAndUserId(String id, String userId);
}
