package com.mykola.keep.auth.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "roles")
@Schema(description = "Role entity")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Role ID")
    private Long id;

    public Role(String name) {
        this.name = name;
    }

    @Column(unique = true, nullable = false)
    @Schema(description = "Role name")
    private String name; // e.g., USER, ADMIN

}

