package com.mariocastro.reservas.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "business")
public class Business {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "uuid", updatable = false)
    private UUID uuid;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "cif", nullable = false, unique = true, length = 9)
    private String cif;

    @Column(name = "address", nullable = false, length = 150)
    private String address;

    @Column(name = "active", nullable = false, columnDefinition = "boolean default true")
    private boolean active = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_uuid", nullable = false)
    private User owner;

    @ToString.Exclude
    @OneToMany(mappedBy = "business")
    private List<Booking> bookings;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (cif != null) {
            cif = cif.trim().toUpperCase();
        }
    }

    public boolean isOwnedBy(UUID userUuid) {
        return owner != null && owner.getUuid().equals(userUuid);
    }
}
