package com.capstone.emergency.pharmacy.core.item.service;

import com.capstone.emergency.pharmacy.core.error.NotFoundException;
import com.capstone.emergency.pharmacy.core.item.repository.ItemRepository;
import com.capstone.emergency.pharmacy.core.item.repository.ProductRepository;
import com.capstone.emergency.pharmacy.core.item.repository.model.Item;
import com.capstone.emergency.pharmacy.core.item.repository.model.Product;
import com.capstone.emergency.pharmacy.core.item.service.model.AddItemCommand;
import com.capstone.emergency.pharmacy.core.item.service.model.AddProductCommand;
import com.capstone.emergency.pharmacy.core.item.service.model.mapper.ItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ItemService {

    private final static String NON_EXISTENT_PRODUCT_MESSAGE = "is not present in table \"product\"";

    private final ItemRepository itemRepository;
    private final ProductRepository productRepository;
    private final ItemMapper mapper;

    public Item addItem(AddItemCommand addItemCommand) {
        final var item = Item
                .builder()
                .packSize(addItemCommand.packSize())
                .price(addItemCommand.price())
                .dose(mapper.toDoseEntity(addItemCommand.dose()))
                .type(addItemCommand.type())
                .product(
                        Product.builder().id(addItemCommand.productId()).build()
                )
                .build();

        try {
            return itemRepository.save(item);
        } catch (DataIntegrityViolationException e) {
            final var message = e.getMessage();
            if (message.contains(NON_EXISTENT_PRODUCT_MESSAGE)) {
                throw new NotFoundException("Product with id: " + addItemCommand.productId() + " not found");
            }
            throw new RuntimeException("Unknown error occurred");
        }
    }

    public Product addProduct(AddProductCommand addProductCommand) {
        final var product = Product
                .builder()
                .name(addProductCommand.name())
                .contraindication(addProductCommand.contraindication())
                .instruction(addProductCommand.instruction())
                .composition(addProductCommand.composition())
                .storageCondition(addProductCommand.storageCondition())
                .build();

        Product result;
        try {
            result = productRepository.save(product);
        } catch (DataIntegrityViolationException ex) {
            result = productRepository.findByName(addProductCommand.name())
                    .orElseThrow(() -> new RuntimeException("Unknown error occurred"));
        }
        return result;
    }
}
