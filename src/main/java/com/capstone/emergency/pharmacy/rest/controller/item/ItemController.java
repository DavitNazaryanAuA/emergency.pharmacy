package com.capstone.emergency.pharmacy.rest.controller.item;

import com.capstone.emergency.pharmacy.core.item.service.ItemService;
import com.capstone.emergency.pharmacy.core.item.service.model.AddItemCommand;
import com.capstone.emergency.pharmacy.core.item.service.model.AddProductCommand;
import com.capstone.emergency.pharmacy.rest.controller.item.model.mapper.ItemDtoMapper;
import com.capstone.emergency.pharmacy.rest.controller.item.model.response.ItemResponse;
import com.capstone.emergency.pharmacy.rest.controller.item.model.response.ProductResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RequiredArgsConstructor
@CrossOrigin
@RestController
@RequestMapping("/api/item")
public class ItemController {

    private final ItemService service;
    private final ItemDtoMapper mapper;

    @PostMapping
    public ResponseEntity<ItemResponse> addItem(
            @RequestBody @Valid AddItemCommand request
    ) {
        final var item = service.addItem(request);
        return ResponseEntity.ok(mapper.toItemResponse(item));
    }

    @PostMapping("/product")
    public ResponseEntity<ProductResponse> addProduct(
            @RequestBody @Valid AddProductCommand request
    ) {
        final var product = service.addProduct(request);
        return ResponseEntity.ok(mapper.toProductResponse(product));
    }
}
