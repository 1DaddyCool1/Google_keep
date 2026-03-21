package com.mykola.keep.noteservice.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NoteDto Tests")
public class NoteDtoTest {

    @Test
    @DisplayName("Should create NoteDto with no-args constructor")
    void shouldCreateWithNoArgsConstructor() {
        NoteDTO dto = new NoteDTO();
        assertNotNull(dto);
    }

    @Test
    @DisplayName("Should create NoteDto with basic constructor")
    void shouldCreateWithBasicConstructor() {
        List<Long> mediaIds = Arrays.asList(1L, 2L);
        List<Long> labelIds = Arrays.asList(10L, 20L);
        LocalDateTime now = LocalDateTime.now();

        NoteDTO dto = new NoteDTO("1", "Title", "Content", mediaIds, labelIds, now, now);

        assertEquals("1", dto.getId());
        assertEquals("Title", dto.getTitle());
        assertEquals("Content", dto.getContent());
        assertEquals(mediaIds, dto.getMediaIds());
        assertEquals(labelIds, dto.getLabelIds());
        assertEquals(now, dto.getCreatedAt());
        assertEquals(now, dto.getUpdatedAt());
        assertFalse(dto.isTrashed());
        assertNull(dto.getTrashedAt());
        assertFalse(dto.isArchived());
        assertNull(dto.getArchivedAt());
    }

    @Test
    @DisplayName("Should create NoteDto with trashed status constructor")
    void shouldCreateWithTrashedConstructor() {
        List<Long> mediaIds = Arrays.asList(1L);
        List<Long> labelIds = Arrays.asList(10L);
        LocalDateTime createdAt = LocalDateTime.now().minusDays(5);
        LocalDateTime updatedAt = LocalDateTime.now().minusDays(3);
        LocalDateTime trashedAt = LocalDateTime.now().minusDays(1);

        NoteDTO dto = new NoteDTO("1", "Title", "Content", mediaIds, labelIds,
                                  createdAt, updatedAt, true, trashedAt);

        assertEquals("1", dto.getId());
        assertTrue(dto.isTrashed());
        assertEquals(trashedAt, dto.getTrashedAt());
        assertFalse(dto.isArchived());
        assertNull(dto.getArchivedAt());
    }

    @Test
    @DisplayName("Should create NoteDto with full constructor")
    void shouldCreateWithFullConstructor() {
        List<Long> mediaIds = Arrays.asList(1L, 2L, 3L);
        List<Long> labelIds = Arrays.asList(10L, 20L);
        LocalDateTime createdAt = LocalDateTime.now().minusDays(10);
        LocalDateTime updatedAt = LocalDateTime.now().minusDays(5);
        LocalDateTime trashedAt = LocalDateTime.now().minusDays(2);
        LocalDateTime archivedAt = LocalDateTime.now().minusDays(3);

        NoteDTO dto = new NoteDTO("1", "Title", "Content", mediaIds, labelIds,
                                  createdAt, updatedAt, true, trashedAt, true, archivedAt);

        assertEquals("1", dto.getId());
        assertEquals("Title", dto.getTitle());
        assertEquals("Content", dto.getContent());
        assertEquals(3, dto.getMediaIds().size());
        assertEquals(2, dto.getLabelIds().size());
        assertEquals(createdAt, dto.getCreatedAt());
        assertEquals(updatedAt, dto.getUpdatedAt());
        assertTrue(dto.isTrashed());
        assertEquals(trashedAt, dto.getTrashedAt());
        assertTrue(dto.isArchived());
        assertEquals(archivedAt, dto.getArchivedAt());
    }

    @Test
    @DisplayName("Should set and get all properties")
    void shouldSetAndGetAllProperties() {
        NoteDTO dto = new NoteDTO();

        dto.setId("123");
        dto.setTitle("Test Title");
        dto.setContent("<p>Test Content</p>");

        List<Long> mediaIds = Arrays.asList(1L, 2L);
        dto.setMediaIds(mediaIds);

        List<Long> labelIds = Arrays.asList(10L, 20L);
        dto.setLabelIds(labelIds);

        LocalDateTime now = LocalDateTime.now();
        dto.setTrashed(true);
        dto.setTrashedAt(now);
        dto.setArchived(true);
        dto.setArchivedAt(now);

        assertEquals("123", dto.getId());
        assertEquals("Test Title", dto.getTitle());
        assertEquals("<p>Test Content</p>", dto.getContent());
        assertEquals(mediaIds, dto.getMediaIds());
        assertEquals(labelIds, dto.getLabelIds());
        assertTrue(dto.isTrashed());
        assertEquals(now, dto.getTrashedAt());
        assertTrue(dto.isArchived());
        assertEquals(now, dto.getArchivedAt());
    }

    @Test
    @DisplayName("Should handle null values")
    void shouldHandleNullValues() {
        NoteDTO dto = new NoteDTO();

        dto.setId(null);
        dto.setTitle(null);
        dto.setContent(null);
        dto.setMediaIds(null);
        dto.setLabelIds(null);
        dto.setTrashedAt(null);
        dto.setArchivedAt(null);

        assertNull(dto.getId());
        assertNull(dto.getTitle());
        assertNull(dto.getContent());
        assertNull(dto.getMediaIds());
        assertNull(dto.getLabelIds());
        assertNull(dto.getTrashedAt());
        assertNull(dto.getArchivedAt());
    }

    @Test
    @DisplayName("Should handle empty collections")
    void shouldHandleEmptyCollections() {
        NoteDTO dto = new NoteDTO();

        dto.setMediaIds(Arrays.asList());
        dto.setLabelIds(Arrays.asList());

        assertNotNull(dto.getMediaIds());
        assertNotNull(dto.getLabelIds());
        assertTrue(dto.getMediaIds().isEmpty());
        assertTrue(dto.getLabelIds().isEmpty());
    }

    @Test
    @DisplayName("Should handle large collections")
    void shouldHandleLargeCollections() {
        NoteDTO dto = new NoteDTO();

        List<Long> mediaIds = Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L);
        List<Long> labelIds = Arrays.asList(100L, 200L, 300L, 400L, 500L);

        dto.setMediaIds(mediaIds);
        dto.setLabelIds(labelIds);

        assertEquals(10, dto.getMediaIds().size());
        assertEquals(5, dto.getLabelIds().size());
    }
}

