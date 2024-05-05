package com.capstone.emergency.pharmacy.core.vending.repository.model;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.Date;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Table(
        name = "vending_machine",
        indexes = @Index(name = "idx_location", columnList = "longitude, latitude", unique = true)
)
public class VendingMachineEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "longitude", column = @Column(name = "longitude", nullable = false)),
            @AttributeOverride(name = "latitude", column = @Column(name = "latitude", nullable = false))
    })
    private Location location;

    @CreatedDate
    @Column(insertable = false)
    private Date createdAt;

    @LastModifiedDate
    @Column(insertable = false)
    private Date updatedAt;

    @Embeddable
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @ToString
    public static class Location {
        private Double longitude;
        private Double latitude;
    }
}
