package com.capstone.emergency.pharmacy.core.vending.service;

import com.capstone.emergency.pharmacy.core.error.ApiException;
import com.capstone.emergency.pharmacy.core.error.BadRequestException;
import com.capstone.emergency.pharmacy.core.error.NotFoundException;
import com.capstone.emergency.pharmacy.core.item.repository.model.Item;
import com.capstone.emergency.pharmacy.core.vending.repository.CartItemRedisOperations;
import com.capstone.emergency.pharmacy.core.vending.repository.VendingMachineItemRepository;
import com.capstone.emergency.pharmacy.core.vending.repository.VendingMachineRepository;
import com.capstone.emergency.pharmacy.core.vending.repository.model.VendingMachineEntity;
import com.capstone.emergency.pharmacy.core.vending.repository.model.VendingMachineItem;
import com.capstone.emergency.pharmacy.core.vending.service.mapper.VMMapper;
import com.capstone.emergency.pharmacy.core.vending.service.model.AddItemToCardCommand;
import com.capstone.emergency.pharmacy.core.vending.service.model.LoadItemsCommand;
import com.capstone.emergency.pharmacy.core.vending.service.model.Location;
import com.capstone.emergency.pharmacy.core.vending.service.model.VendingMachine;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class VendingMachineService {
    private final static String NON_EXISTENT_ITEM_MESSAGE = "is not present in table \"item\"";

    private final VendingMachineRepository repository;
    private final VendingMachineItemRepository itemRepository;
    private final CartItemRedisOperations cartItemRedisOperations;
    private final VMMapper mapper;

    public VendingMachine registerVendingMachine(Location location) {
        final var locationEntity = mapper.toLocationEntity(location);
        final var machine = VendingMachineEntity
                .builder()
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

        final var machineItems = loadedItems
                .stream()
                .map(loadedItem ->
                        VendingMachineItem.builder()
                                .item(
                                        Item.builder().id(loadedItem.itemId()).build()
                                )
                                .vendingMachineId(machine.getId())
                                .quantity(loadedItem.quantity())
                                .build()
                )
                .toList();

        try {
            return itemRepository.saveAll(machineItems);
        } catch (DataIntegrityViolationException ex) {
            final var message = ex.getMostSpecificCause().getMessage();
            if (message.contains(NON_EXISTENT_ITEM_MESSAGE)) {
                int id_start = message.indexOf('=') + 2;
                int id_end = message.lastIndexOf(')');
                String id = message.substring(id_start, id_end);

                throw new NotFoundException("Item with id: " + id + " not found");
            }
            throw new RuntimeException("Unknown error occurred");
        }
    }

    public void addItemToCart(
            String userId,
            Long vendingMachineId,
            AddItemToCardCommand addItemToCardCommand
    ) {
        final var machineItem = itemRepository
                .findByVendingMachineId(vendingMachineId)
                .orElseThrow(() -> new NotFoundException(
                        "Item" + addItemToCardCommand.itemId() + " not found in machine: " + vendingMachineId
                ));

        if (machineItem.getQuantity() < addItemToCardCommand.quantity()) {
            throw new BadRequestException(
                    "Not enough items: " + machineItem.getQuantity() + ", requested: " + addItemToCardCommand.quantity(),
                    ApiException.Reason.NOT_ENOUGH_ITEMS
            );
        }

        cartItemRedisOperations.addItemToCart(
                userId,
                mapper.toCartItem(addItemToCardCommand, vendingMachineId)
        );
    }
}
