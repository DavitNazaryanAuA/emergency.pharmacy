package com.capstone.emergency.pharmacy.core.vending.repository.mongo;

import com.capstone.emergency.pharmacy.core.vending.repository.mongo.model.Reservation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends MongoRepository<Reservation, String> {
}
