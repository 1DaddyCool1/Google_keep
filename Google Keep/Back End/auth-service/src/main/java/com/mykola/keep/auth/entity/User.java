package com.mykola.keep.auth.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"username"}),
        @UniqueConstraint(columnNames = {"email"})
})
@Schema(description = "User entity")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "User ID")
    private Long id;

    @Column(nullable = false, length = 32)
    @Schema(description = "Username")
    private String username;

    @Column(nullable = false, length = 128)
    @Schema(description = "Email")
    private String email;

    @Column(nullable = false)
    @Schema(description = "Password hash")
    private String passwordHash;

    @Column(nullable = false)
    @Schema(description = "User status")
    private boolean active = true;

    @CreationTimestamp
    @Schema(description = "User creation date")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Schema(description = "User last update date")
    private LocalDateTime updatedAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    @Schema(description = "User roles")
    private Set<Role> roles = new HashSet<>();
}

