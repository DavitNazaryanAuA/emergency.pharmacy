package com.capstone.emergency.pharmacy.core.item.repository.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(
            name = "name",
            nullable = false,
            unique = true,
            updatable = false,
            length = 100
    )
    private String name;

    @Column(
            name = "instruction",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String instruction;

    @Column(
            name = "storage_condition",
            nullable = false
    )
    private String storageCondition;

    @Column(
            name = "contraindication",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String contraindication;

    @Column(
            name = "composition",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String composition;

    @OneToMany(mappedBy = "product")
    @ToString.Exclude
    private List<Item> items;
}
