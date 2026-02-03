package com.travel.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data // Generates getters, setters, toString, etc.
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;
    
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String resetToken;
    private LocalDateTime resetTokenExpiry;

    private String country;
    private String cityState;
    private String postalCode;

    private String imageUrl;
}


