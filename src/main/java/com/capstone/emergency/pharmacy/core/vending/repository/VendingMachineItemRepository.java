package com.capstone.emergency.pharmacy.core.vending.repository;

import com.capstone.emergency.pharmacy.core.vending.repository.model.VendingMachineItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VendingMachineItemRepository extends JpaRepository<VendingMachineItem, Long> {

    Optional<VendingMachineItem> findByVendingMachineId(Long vendingMachineId);
}
