package com.capstone.emergency.pharmacy.core.vending.service.mapper;

import com.capstone.emergency.pharmacy.core.vending.repository.model.CartItem;
import com.capstone.emergency.pharmacy.core.vending.repository.model.VendingMachineEntity;
import com.capstone.emergency.pharmacy.core.vending.service.model.AddItemToCardCommand;
import com.capstone.emergency.pharmacy.core.vending.service.model.Location;
import com.capstone.emergency.pharmacy.core.vending.service.model.VendingMachine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = "spring"
)
public interface VMMapper {

    List<VendingMachine> toVendingMachines(List<VendingMachineEntity> vms);

    VendingMachine toVendingMachine(VendingMachineEntity vm);

    VendingMachineEntity.Location toLocationEntity(Location location);

    @Mapping(target = "vendingMachineId", source = "vendingMachineId")
    CartItem toCartItem(AddItemToCardCommand command, Long vendingMachineId);
}
