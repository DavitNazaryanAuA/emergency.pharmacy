package com.capstone.emergency.pharmacy.core.vending.service;

import com.capstone.emergency.pharmacy.core.error.NotFoundException;
import com.capstone.emergency.pharmacy.core.vending.repository.mongo.ReservationRepository;
import com.capstone.emergency.pharmacy.core.vending.repository.mongo.model.Reservation;
import com.capstone.emergency.pharmacy.core.vending.service.model.AddReservationCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@RequiredArgsConstructor
@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;

    public Reservation addReservation(
            String userId,
            AddReservationCommand command
    ) {
        final var saved = reservationRepository.insert(
                Reservation.builder()
                        .itemId(command.itemId().toString())
                        .userId(userId)
                        .quantity(command.quantity())
                        .date(new Date())
                        .build()
        );

        return reservationRepository.findById(saved.getId()).orElseThrow(() -> new NotFoundException("Not Found"));
    }
}
