package com.capstone.emergency.pharmacy.rest.controller.vending;

import com.capstone.emergency.pharmacy.core.vending.repository.mongo.model.Reservation;
import com.capstone.emergency.pharmacy.core.vending.service.ReservationService;
import com.capstone.emergency.pharmacy.core.vending.service.model.AddReservationCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RequiredArgsConstructor
@CrossOrigin
@RestController
@RequestMapping("/api/reservation")
public class ReservationController {

    private final ReservationService service;

    @PostMapping
    public ResponseEntity<Reservation> addReservation(
            @RequestBody @Valid AddReservationCommand command
    ) {
        final var auth = SecurityContextHolder.getContext().getAuthentication();
        final var jwt = (Jwt) auth.getPrincipal();
        final var userId = jwt.getSubject();

        return ResponseEntity.ok(
                service.addReservation(userId, command)
        );
    }
}
