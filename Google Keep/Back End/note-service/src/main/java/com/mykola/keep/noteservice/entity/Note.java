package com.mykola.keep.noteservice.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "notes")
@Schema(description = "Note entity")
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Note ID")
    private Long id;

    @Column(nullable = false)
    @Schema(description = "Note title")
    private String title;

    @Column(nullable = false)
    @Schema(description = "Note content")
    private String content;

    @Column(nullable = false)
    @Schema(description = "user->note")
    private String username;// denormalized

    @CreationTimestamp
    @Schema(description = "Note creation date")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Schema(description = "Note last update date")
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    @Schema(description = "Note status")
    private boolean isTrashed = false;

    @Schema(description = "Note trashed date")
    private LocalDateTime trashedAt;

    @Column(nullable = false)
    @Schema(description = "Note archived status")
    private boolean isArchived = false;

    @Schema(description = "Note archived date")
    private LocalDateTime archivedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "note_images", joinColumns = @JoinColumn(name = "note_id"))
    @Column(name = "media_id")
    @Schema(description = "Note images")
    private Set<Long> mediaIds = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "note_labels", joinColumns = @JoinColumn(name = "note_id"))
    @Column(name = "label_id")
    @Schema(description = "Note labels")
    private Set<Long> labelIds = new HashSet<>();
}
