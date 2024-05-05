package com.capstone.emergency.pharmacy.core.vending.repository;

import com.capstone.emergency.pharmacy.core.vending.repository.model.VendingMachineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendingMachineRepository extends JpaRepository<VendingMachineEntity, Long> {
    @Query(
            "SELECT vm FROM VendingMachineEntity vm WHERE " +
                    "(" +
                    "CASE WHEN :swLong < :neLong " +
                    "THEN (vm.location.longitude BETWEEN :swLong AND :neLong)" +
                    "ELSE (vm.location.longitude BETWEEN :neLong AND :swLong) END" +
                    ")" +
                    "AND " +
                    "(" +
                    "CASE WHEN :swLat < :neLat " +
                    "THEN (vm.location.latitude BETWEEN :swLat AND :neLat)" +
                    "ELSE (vm.location.latitude BETWEEN :neLat AND :swLat) END" +
                    ")"
    )
    List<VendingMachineEntity> getVendingMachinesInLocation(
            double swLong,
            double swLat,
            double neLong,
            double neLat
    );

    Optional<VendingMachineEntity> findByLocation(VendingMachineEntity.Location location);
}
