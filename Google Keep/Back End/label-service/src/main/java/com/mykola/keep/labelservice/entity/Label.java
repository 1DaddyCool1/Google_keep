package com.mykola.keep.labelservice.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "labels")
@Schema(description = "Label entity")
public class Label {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Label ID")
    private Long id;
    @Column(nullable = false)
    @Schema(description = "Label name")
    private String name;
    @Column(nullable = false)
    @Schema(description = "user->label")
    private String username; // denormalized
    @CreationTimestamp
    @Schema(description = "Label creation date")
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Schema(description = "Label last update date")
    private LocalDateTime updatedAt;
}
