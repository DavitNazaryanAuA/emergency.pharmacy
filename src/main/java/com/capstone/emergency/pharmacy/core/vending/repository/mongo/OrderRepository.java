package com.capstone.emergency.pharmacy.core.vending.repository.mongo;

import com.capstone.emergency.pharmacy.core.vending.repository.mongo.model.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {

    Optional<Order> findByIdAndUserId(String id, String userId);

    List<Order> findByUserIdAndStatus(String userId, Order.Status status, Pageable pageable);
}
