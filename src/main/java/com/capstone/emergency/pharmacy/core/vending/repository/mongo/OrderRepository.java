package com.capstone.emergency.pharmacy.core.vending.repository.mongo;

import com.capstone.emergency.pharmacy.core.vending.repository.mongo.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {

    Optional<Order> findByIdAndUserId(String id, String userId);
}
