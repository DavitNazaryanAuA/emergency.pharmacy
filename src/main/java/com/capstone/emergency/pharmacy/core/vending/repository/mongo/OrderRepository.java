package com.capstone.emergency.pharmacy.core.vending.repository.mongo;

import com.capstone.emergency.pharmacy.core.vending.repository.model.Order;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends CrudRepository<Order, String> {
}
