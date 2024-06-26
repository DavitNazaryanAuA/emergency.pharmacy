package com.capstone.emergency.pharmacy.rest.controller.vending;

import com.capstone.emergency.pharmacy.core.vending.service.VendingMachineService;
import com.capstone.emergency.pharmacy.core.vending.service.model.LoadItemsCommand;
import com.capstone.emergency.pharmacy.core.vending.service.model.Location;
import com.capstone.emergency.pharmacy.core.vending.service.model.RegisterMachineCommand;
import com.capstone.emergency.pharmacy.core.vending.service.model.VendingMachine;
import com.capstone.emergency.pharmacy.rest.controller.vending.model.model.VendingMachineDtoMapper;
import com.capstone.emergency.pharmacy.rest.controller.vending.model.model.VendingMachineLoadedItemsResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Range;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RequiredArgsConstructor
@CrossOrigin
@RestController
@RequestMapping("/api/vm")
public class VendingMachineController {

    private final VendingMachineService service;
    private final VendingMachineDtoMapper mapper;

    @GetMapping("/items")
    public ResponseEntity<List<VendingMachine>> search(
            @RequestParam("long")
            @Range(min = -180, max = 180, message = "Longitude must be in a value between -180 and 180")
                    double curLong,
            @RequestParam("lat")
            @Range(min = -180, max = 180, message = "Latitude must be in a value between -90 and 90")
                    double curLat,
            @RequestParam("productName") @NotBlank String productName
    ) {
        return ResponseEntity.ok(
                service.searchByProduct(curLong, curLat, productName)
        );
    }

    @GetMapping
    public ResponseEntity<List<VendingMachine>> getVmsInLocation(
            @RequestParam("swLong")
            @Range(min = -180, max = 180, message = "Longitude must be in a value between -180 and 180")
                    double swLong,
            @RequestParam("swLat")
            @Range(min = -90, max = 90, message = "Latitude must be in a value between -90 and 90")
                    double swLat,
            @RequestParam("neLong")
            @Range(min = -180, max = 180, message = "Longitude must be in a value between -180 and 180")
                    double neLong,
            @RequestParam("neLat")
            @Range(min = -90, max = 90, message = "Latitude must be in a value between -90 and 90")
                    double neLat
    ) {
        return ResponseEntity.ok(
                service.getVendingMachinesInLocation(swLong, swLat, neLong, neLat)
        );
    }

    @PostMapping
    public ResponseEntity<VendingMachine> registerVendingMachine(
            @RequestBody @Valid RegisterMachineCommand registerMachineCommand
    ) {
        return ResponseEntity.ok(
                service.registerVendingMachine(registerMachineCommand)
        );
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<VendingMachineLoadedItemsResponse> loadItem(
            @PathVariable("id") Long id,
            @RequestBody @Valid LoadItemsCommand loadItemCommand
    ) {
        final var itemsResponse = service
                .loadItems(
                        id,
                        loadItemCommand.loadedItems()
                )
                .stream()
                .map(mapper::toVendingMachineItemResponse)
                .toList();
        return ResponseEntity.ok(
                new VendingMachineLoadedItemsResponse(id, itemsResponse)
        );
    }

    @GetMapping("/{id}/items")
    public ResponseEntity<VendingMachineLoadedItemsResponse> getItems(@PathVariable("id") Long id) {
        final var auth = SecurityContextHolder.getContext().getAuthentication();
        final var jwt = (Jwt) auth.getPrincipal();
        final var userId = jwt.getSubject();

        final var itemsResponse = service
                .getMachineItems(id, userId)
                .stream()
                .map(mapper::toVendingMachineItemResponse)
                .toList();

        return ResponseEntity.ok(
                new VendingMachineLoadedItemsResponse(id, itemsResponse)
        );
    }

    @PostMapping("/{id}/lock")
    public ResponseEntity<Void> lockMachine(
            @PathVariable("id") Long id
    ) {
        final var auth = SecurityContextHolder.getContext().getAuthentication();
        final var jwt = (Jwt) auth.getPrincipal();
        final var userId = jwt.getSubject();

        System.out.println("Locking");
        service.lockVendingMachine(id, userId);
        System.out.println("Locked");
        return ResponseEntity.ok().build();
    }
}
