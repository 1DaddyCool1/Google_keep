package com.mykola.keep.mediaservice.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "media")
@Schema(description = "Media entity")
public class Media {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Media ID")
    private Long id;
    @Column(nullable = false)
    @Schema(description = "user->media")
    private String username; // denormalized
    @Column(nullable = false)
    @Schema(description = "Media type")
    private String imagePath;
    @CreationTimestamp
    @Schema(description = "Media upload date")
    private LocalDateTime uploadedAt;
}
