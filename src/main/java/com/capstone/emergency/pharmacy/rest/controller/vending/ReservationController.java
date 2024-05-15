package com.capstone.emergency.pharmacy.rest.controller.vending;

import com.capstone.emergency.pharmacy.core.vending.service.ReservationService;
import com.capstone.emergency.pharmacy.core.vending.service.model.AddReservationCommand;
import com.capstone.emergency.pharmacy.core.vending.service.model.mapper.VMMapper;
import com.capstone.emergency.pharmacy.rest.controller.vending.model.ReservationResponse;
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
    private final VMMapper mapper;

    @PostMapping
    public ResponseEntity<ReservationResponse> addReservation(
            @RequestBody @Valid AddReservationCommand command
    ) {
        final var auth = SecurityContextHolder.getContext().getAuthentication();
        final var jwt = (Jwt) auth.getPrincipal();
        final var userId = jwt.getSubject();

        return ResponseEntity.ok(
                mapper.toReservationResponse(
                        service.addReservation(userId, command)
                )
        );
    }
}
