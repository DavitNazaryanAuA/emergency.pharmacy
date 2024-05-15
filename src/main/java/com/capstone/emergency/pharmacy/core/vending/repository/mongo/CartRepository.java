package com.capstone.emergency.pharmacy.core.vending.repository.mongo;

import com.capstone.emergency.pharmacy.core.vending.repository.mongo.model.Cart;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends MongoRepository<Cart, String> {

    Cart findByUserIdAndAndVendingMachineId(String userId, Long vendingMachineId);

    Optional<Cart> findByUserId(String userId);

    Long deleteByUserId(String userId);
}
