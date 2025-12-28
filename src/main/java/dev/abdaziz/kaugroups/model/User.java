package dev.abdaziz.kaugroups.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;

import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, unique = true)
    private UUID id;

    @Column(nullable = false, updatable = false, unique = true)
    private String name;

    @Email
    @Column(nullable = false, updatable = false, unique = true)
    private String email;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Gender gender = Gender.UNKNOWN;
}

