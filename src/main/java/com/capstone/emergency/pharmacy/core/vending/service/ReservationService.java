package com.capstone.emergency.pharmacy.core.vending.service;

import com.capstone.emergency.pharmacy.core.error.BadRequestException;
import com.capstone.emergency.pharmacy.core.error.NotFoundException;
import com.capstone.emergency.pharmacy.core.vending.repository.VendingMachineItemRepository;
import com.capstone.emergency.pharmacy.core.vending.repository.mongo.ReservationRepository;
import com.capstone.emergency.pharmacy.core.vending.repository.mongo.model.Reservation;
import com.capstone.emergency.pharmacy.core.vending.service.model.AddReservationCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final VendingMachineItemRepository vendingMachineItemRepository;

    public Reservation addReservation(
            String userId,
            AddReservationCommand command
    ) {
        final var machineId = command.vendingMachineId();
        final var itemId = command.itemId();
        final var requestedQuantity = command.quantity();

        final var itemFuture = CompletableFuture.supplyAsync(() ->
                vendingMachineItemRepository.findByVendingMachineIdAndItem_Id(machineId, itemId)
                        .orElseThrow(() -> new NotFoundException("Item not found in machine"))
        );
        final var reservationsFuture = CompletableFuture.supplyAsync(() ->
                reservationRepository.findByReservedItemAndUserIdAndExpDateGreaterThan(
                        Reservation.ReservedItem.builder().itemId(itemId.toString()).vendingMachineId(machineId.toString()).build(),
                        userId,
                        Date.from(Instant.now())
                )
        );

        final var reservations = itemFuture.thenCombine(reservationsFuture, (vendingMachineItem, itemReservations) -> {
            final var totalReservations = itemReservations
                    .stream()
                    .mapToInt(Reservation::getQuantity)
                    .sum();
            final var available = vendingMachineItem.getQuantity() - totalReservations;

            if (available < requestedQuantity) {
                throw new BadRequestException(
                        "Not enough of item: " + itemId + " in machine." +
                                " Requested: " + requestedQuantity + ", available: " + available
                );
            }
            return itemReservations;
        }).join();

        System.out.println(reservations);

        final var existing = reservations.stream()
                .filter(reservation -> reservation.getUserId().equals(userId))
                .findFirst();

        System.out.println(existing);

        Reservation updatedReservation;
        if (existing.isPresent()) {
            updatedReservation = existing.get();
            updatedReservation.setQuantity(
                    updatedReservation.getQuantity() + requestedQuantity
            );
        } else {
            final var expDate = Date.from(
                    Instant.now().plus(15L, ChronoUnit.MINUTES)
            );
            updatedReservation = Reservation.builder()
                    .userId(userId)
                    .reservedItem(
                            Reservation.ReservedItem
                                    .builder()
                                    .itemId(itemId.toString())
                                    .vendingMachineId(machineId.toString())
                                    .build()
                    )
                    .quantity(command.quantity())
                    .expDate(expDate)
                    .build();
        }
        return reservationRepository.save(updatedReservation);
    }
}
