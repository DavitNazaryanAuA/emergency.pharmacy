package com.capstone.emergency.pharmacy.core.vending.repository.mongo;

import com.capstone.emergency.pharmacy.core.vending.repository.mongo.model.Reservation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ReservationRepository extends MongoRepository<Reservation, String> {

    List<Reservation> findByReservedItemAndUserIdAndExpDateGreaterThan(
            Reservation.ReservedItem item,
            String userId,
            Date currentDate
    );

    List<Reservation> findByReservedItem_VendingMachineIdAndUserIdAndExpDateGreaterThan(
            String itemId,
            String userId,
            Date currentDate
    );

    Long deleteAllByUserId(String userId);
}
