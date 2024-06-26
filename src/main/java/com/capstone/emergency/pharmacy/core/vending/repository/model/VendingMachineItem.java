package com.capstone.emergency.pharmacy.core.vending.repository.model;

import com.capstone.emergency.pharmacy.core.item.repository.model.Item;
import jakarta.persistence.*;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "vending_machine_item", indexes = @Index(name = "idx_machineId_itemId", columnList = "vendingMachineId, item_id", unique = true))
public class VendingMachineItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "vending_machine_id", nullable = false)
    private Long vendingMachineId;

    @ManyToOne
    @JoinColumn(name = "item_id", referencedColumnName = "id", nullable = false)
    private Item item;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;
}
