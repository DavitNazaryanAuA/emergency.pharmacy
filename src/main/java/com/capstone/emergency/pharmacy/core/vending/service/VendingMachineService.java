package com.capstone.emergency.pharmacy.core.vending.service;

import com.capstone.emergency.pharmacy.core.error.NotFoundException;
import com.capstone.emergency.pharmacy.core.item.repository.ItemRepository;
import com.capstone.emergency.pharmacy.core.item.repository.model.Item;
import com.capstone.emergency.pharmacy.core.vending.repository.VendingMachineItemRepository;
import com.capstone.emergency.pharmacy.core.vending.repository.VendingMachineRedisRepository;
import com.capstone.emergency.pharmacy.core.vending.repository.VendingMachineRepository;
import com.capstone.emergency.pharmacy.core.vending.repository.model.VendingMachineEntity;
import com.capstone.emergency.pharmacy.core.vending.repository.model.VendingMachineItem;
import com.capstone.emergency.pharmacy.core.vending.repository.mongo.ReservationRepository;
import com.capstone.emergency.pharmacy.core.vending.repository.mongo.model.Reservation;
import com.capstone.emergency.pharmacy.core.vending.service.model.LoadItemsCommand;
import com.capstone.emergency.pharmacy.core.vending.service.model.RegisterMachineCommand;
import com.capstone.emergency.pharmacy.core.vending.service.model.VendingMachine;
import com.capstone.emergency.pharmacy.core.vending.service.model.mapper.VMMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RequiredArgsConstructor
@Service
public class VendingMachineService {
    private final VendingMachineRepository repository;
    private final VendingMachineItemRepository vendingMachineItemRepository;
    private final ItemRepository itemRepository;
    private final ReservationRepository reservationRepository;
    private final VendingMachineRedisRepository vendingMachineRedisRepository;
    private final VMMapper mapper;

    public List<VendingMachine> searchByProduct(
            double curLong,
            double curLat,
            String productName
    ) {
        return repository.getNearByVMsHavingProduct(
                        productName,
                        curLong, curLat
                )
                .stream()
                .map(mapper::toVendingMachine)
                .limit(5)
                .toList();
    }

    public VendingMachine registerVendingMachine(RegisterMachineCommand registerCommand) {
        final var locationEntity = mapper.toLocationEntity(registerCommand.location());
        final var machine = VendingMachineEntity
                .builder()
                .address(
                        VendingMachineEntity.Address.builder()
                                .country(registerCommand.country()).city(registerCommand.city()).address(registerCommand.address()).build()
                )
                .location(locationEntity)
                .build();

        VendingMachineEntity result;
        try {
            result = repository.save(machine);
        } catch (DataIntegrityViolationException ex) {
            result = repository.findByLocation(locationEntity)
                    .orElseThrow(() -> new RuntimeException("Unknown error occurred"));
        }
        return mapper.toVendingMachine(result);
    }

    public List<VendingMachine> getVendingMachinesInLocation(
            double swLong,
            double swLat,
            double neLong,
            double neLat
    ) {
        final var vms = repository.getVendingMachinesInLocation(swLong, swLat, neLong, neLat);
        return mapper.toVendingMachines(vms);
    }

    public List<VendingMachineItem> loadItems(
            Long vendingMachineId,
            List<LoadItemsCommand.LoadedItem> loadedItems
    ) {
        final var machine = repository.findById(vendingMachineId)
                .orElseThrow(() -> new NotFoundException("Vending Machine not found"));

        final var futures = loadedItems
                .stream()
                .map(item ->
                        CompletableFuture.supplyAsync(() ->
                                itemRepository
                                        .findById(item.itemId())
                                        .orElseThrow(() -> new RuntimeException("Item with id: " + item.itemId() + " not found"))
                        )
                )
                .toList();

        List<Item> items;
        try {
            items = futures.stream().map(CompletableFuture::join).toList();
        } catch (CompletionException ex) {
            throw new NotFoundException(ex.getCause().getMessage());
        }

        final var machineItems = IntStream.range(0, items.size()).mapToObj(i ->
                VendingMachineItem.builder()
                        .item(items.get(i))
                        .vendingMachineId(machine.getId())
                        .quantity(loadedItems.get(i).quantity())
                        .build()
        ).toList();

        return vendingMachineItemRepository.saveAll(machineItems);
    }

    public List<VendingMachineItem> getMachineItems(
            Long vendingMachineId,
            String userId
    ) {
        repository.findById(vendingMachineId).orElseThrow(() -> new NotFoundException("Vending machine with id: " + vendingMachineId + " not found"));

        final var itemsFuture = CompletableFuture.supplyAsync(() ->
                vendingMachineItemRepository.findAllByVendingMachineId(vendingMachineId)
        );
        final var reservationsFuture = CompletableFuture.supplyAsync(() ->
                reservationRepository.findByReservedItem_VendingMachineIdAndUserIdAndExpDateGreaterThan(
                        vendingMachineId.toString(),
                        userId,
                        Date.from(Instant.now())
                )
        );

        return itemsFuture.thenCombine(reservationsFuture, (items, itemReservations) -> {
            final var reservationIdsToQuantity = itemReservations.stream()
                    .collect(Collectors.toMap(reservation -> reservation.getReservedItem().getItemId(), Reservation::getQuantity));

            items.forEach(item -> {
                var reserved = reservationIdsToQuantity.get(item.getItem().getId().toString());
                reserved = reserved == null ? 0 : reserved;
                item.setQuantity(item.getQuantity() - reserved);
            });

            return items;
        }).join();
    }

    public void lockVendingMachine(Long vendingMachineId, String userId) {
        repository.findById(vendingMachineId).orElseThrow(() ->
                new NotFoundException("Vending machine: " + vendingMachineId + " not found")
        );

        vendingMachineRedisRepository.lockMachine(vendingMachineId, userId);
    }

    public void validateMachineLock(Long vendingMachineId, String userId) {
//        vendingMachineRedisRepository.validateMachineLock(vendingMachineId, userId);
    }
}
