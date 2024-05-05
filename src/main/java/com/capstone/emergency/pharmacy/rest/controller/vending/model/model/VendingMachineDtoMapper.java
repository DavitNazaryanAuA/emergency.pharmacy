package com.capstone.emergency.pharmacy.rest.controller.vending.model.model;

import com.capstone.emergency.pharmacy.core.vending.repository.model.VendingMachineItem;
import com.capstone.emergency.pharmacy.rest.controller.vending.model.VendingMachineItemResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = "spring"
)
public interface VendingMachineDtoMapper {
    VendingMachineItemResponse toVendingMachineItemResponse(VendingMachineItem vendingMachineItem);
}
