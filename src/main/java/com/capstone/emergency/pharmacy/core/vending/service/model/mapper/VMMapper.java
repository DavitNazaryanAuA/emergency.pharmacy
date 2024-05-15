package com.capstone.emergency.pharmacy.core.vending.service.model.mapper;

import com.capstone.emergency.pharmacy.core.vending.repository.model.VendingMachineEntity;
import com.capstone.emergency.pharmacy.core.vending.repository.mongo.model.Reservation;
import com.capstone.emergency.pharmacy.core.vending.service.model.Location;
import com.capstone.emergency.pharmacy.core.vending.service.model.VendingMachine;
import com.capstone.emergency.pharmacy.rest.controller.vending.model.ReservationResponse;
import org.mapstruct.Mapper;
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

    ReservationResponse toReservationResponse(Reservation reservation);
}
