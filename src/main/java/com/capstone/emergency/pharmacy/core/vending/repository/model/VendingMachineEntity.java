package com.capstone.emergency.pharmacy.core.vending.repository.model;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.Date;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Entity
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

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "country", column = @Column(name = "country", nullable = false)),
            @AttributeOverride(name = "city", column = @Column(name = "city", nullable = false)),
            @AttributeOverride(name = "address", column = @Column(name = "address", nullable = false))
    })
    private Address address;

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

    @Embeddable
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @ToString
    public static class Address {
        private String country;
        private String city;
        private String address;
    }
}
