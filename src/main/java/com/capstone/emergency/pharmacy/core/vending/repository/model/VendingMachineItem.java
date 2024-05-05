package com.capstone.emergency.pharmacy.core.vending.repository.model;

import com.capstone.emergency.pharmacy.core.item.repository.model.Item;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Table(name = "vending_machine_item")
public class VendingMachineItem {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "vending_machine_id", nullable = false)
    private Long vendingMachineId;

    @ManyToOne
    @JoinColumn(name = "item", referencedColumnName = "id", nullable = false)
    private Item item;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;
}