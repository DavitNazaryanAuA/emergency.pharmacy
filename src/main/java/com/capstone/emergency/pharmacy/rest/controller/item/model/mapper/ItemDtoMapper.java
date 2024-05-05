package com.capstone.emergency.pharmacy.rest.controller.item.model.mapper;

import com.capstone.emergency.pharmacy.core.item.repository.model.Item;
import com.capstone.emergency.pharmacy.core.item.repository.model.Product;
import com.capstone.emergency.pharmacy.rest.controller.item.model.response.ItemResponse;
import com.capstone.emergency.pharmacy.rest.controller.item.model.response.ProductResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = "spring"
)
public interface ItemDtoMapper {
    ItemResponse toItemResponse(Item item);

    ProductResponse toProductResponse(Product product);
}
