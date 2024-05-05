package com.capstone.emergency.pharmacy.core.item.repository.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Table(name = "item")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "price", nullable = false)
    private Double price;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "amount", nullable = false)),
            @AttributeOverride(name = "unit", column = @Column(name = "unit", nullable = false))
    })
    private Dose dose;

    @Column(name = "pack_size", nullable = false)
    private Integer packSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private Type type;

    @ManyToOne
    @JoinColumn(name = "product_id", referencedColumnName = "id", nullable = false)
    private Product product;

    @Embeddable
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @ToString
    public static class Dose {
        private Double amount;
        @Enumerated(EnumType.STRING)
        private Unit unit;
    }

    public enum Type {
        TABLET,
        CAPSULE,
        SPRAY,
        DROP,
        LIQUID
    }
}
