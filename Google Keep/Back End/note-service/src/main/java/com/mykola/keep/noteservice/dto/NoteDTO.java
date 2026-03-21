package com.mykola.keep.noteservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NoteDTO {
    private String id;
    private String title;
    private String content;
    private List<Long> mediaIds;
    private List<Long> labelIds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isTrashed;
    private LocalDateTime trashedAt;
    private boolean isArchived;
    private LocalDateTime archivedAt;

    public NoteDTO(
            String id,
            String title,
            String content,
            List<Long> mediaIds,
            List<Long> labelIds,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.mediaIds = mediaIds;
        this.labelIds = labelIds;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isTrashed = false;
        this.trashedAt = null;
        this.isArchived = false;
        this.archivedAt = null;
    }

    public NoteDTO(
            String id,
            String title,
            String content,
            List<Long> mediaIds,
            List<Long> labelIds,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            boolean isTrashed,
            LocalDateTime trashedAt
    ) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.mediaIds = mediaIds;
        this.labelIds = labelIds;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isTrashed = isTrashed;
        this.trashedAt = trashedAt;
        this.isArchived = false;
        this.archivedAt = null;
    }
}
