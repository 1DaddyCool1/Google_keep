package com.mykola.keep.noteservice.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Note Entity Tests")
public class NoteTest {

    private Note note;

    @BeforeEach
    void setUp() {
        note = new Note();
    }

    @Test
    @DisplayName("Should create note with default values")
    void shouldCreateNoteWithDefaults() {
        assertNotNull(note);
        assertFalse(note.isTrashed());
        assertFalse(note.isArchived());
        assertNull(note.getTrashedAt());
        assertNull(note.getArchivedAt());
    }

    @Test
    @DisplayName("Should set and get id")
    void shouldSetAndGetId() {
        Long id = 1L;
        note.setId(id);
        assertEquals(id, note.getId());
    }

    @Test
    @DisplayName("Should set and get title")
    void shouldSetAndGetTitle() {
        String title = "Test Note Title";
        note.setTitle(title);
        assertEquals(title, note.getTitle());
    }

    @Test
    @DisplayName("Should set and get content")
    void shouldSetAndGetContent() {
        String content = "<p>Test note content</p>";
        note.setContent(content);
        assertEquals(content, note.getContent());
    }

    @Test
    @DisplayName("Should set and get username")
    void shouldSetAndGetUsername() {
        String username = "testuser";
        note.setUsername(username);
        assertEquals(username, note.getUsername());
    }

    @Test
    @DisplayName("Should set and get trashed status")
    void shouldSetAndGetTrashedStatus() {
        note.setTrashed(true);
        assertTrue(note.isTrashed());

        LocalDateTime trashedAt = LocalDateTime.now();
        note.setTrashedAt(trashedAt);
        assertEquals(trashedAt, note.getTrashedAt());
    }

    @Test
    @DisplayName("Should set and get archived status")
    void shouldSetAndGetArchivedStatus() {
        note.setArchived(true);
        assertTrue(note.isArchived());

        LocalDateTime archivedAt = LocalDateTime.now();
        note.setArchivedAt(archivedAt);
        assertEquals(archivedAt, note.getArchivedAt());
    }

    @Test
    @DisplayName("Should handle media IDs collection")
    void shouldHandleMediaIds() {
        Set<Long> mediaIds = new HashSet<>();
        mediaIds.add(1L);
        mediaIds.add(2L);
        mediaIds.add(3L);

        note.setMediaIds(mediaIds);
        assertEquals(3, note.getMediaIds().size());
        assertTrue(note.getMediaIds().contains(1L));
        assertTrue(note.getMediaIds().contains(2L));
        assertTrue(note.getMediaIds().contains(3L));
    }

    @Test
    @DisplayName("Should handle label IDs collection")
    void shouldHandleLabelIds() {
        Set<Long> labelIds = new HashSet<>();
        labelIds.add(10L);
        labelIds.add(20L);

        note.setLabelIds(labelIds);
        assertEquals(2, note.getLabelIds().size());
        assertTrue(note.getLabelIds().contains(10L));
        assertTrue(note.getLabelIds().contains(20L));
    }

    @Test
    @DisplayName("Should allow empty media IDs")
    void shouldAllowEmptyMediaIds() {
        note.setMediaIds(new HashSet<>());
        assertNotNull(note.getMediaIds());
        assertTrue(note.getMediaIds().isEmpty());
    }

    @Test
    @DisplayName("Should allow empty label IDs")
    void shouldAllowEmptyLabelIds() {
        note.setLabelIds(new HashSet<>());
        assertNotNull(note.getLabelIds());
        assertTrue(note.getLabelIds().isEmpty());
    }

    @Test
    @DisplayName("Should handle complete note lifecycle")
    void shouldHandleCompleteNoteLifecycle() {
        // Create active note
        note.setTitle("Lifecycle Note");
        note.setContent("Content");
        note.setUsername("user1");
        assertFalse(note.isTrashed());
        assertFalse(note.isArchived());

        // Archive note
        note.setArchived(true);
        note.setArchivedAt(LocalDateTime.now());
        assertTrue(note.isArchived());
        assertFalse(note.isTrashed());

        // Unarchive note
        note.setArchived(false);
        note.setArchivedAt(null);
        assertFalse(note.isArchived());

        // Trash note
        note.setTrashed(true);
        note.setTrashedAt(LocalDateTime.now());
        assertTrue(note.isTrashed());

        // Restore note
        note.setTrashed(false);
        note.setTrashedAt(null);
        assertFalse(note.isTrashed());
    }

    @Test
    @DisplayName("Should handle null title and content")
    void shouldHandleNullTitleAndContent() {
        note.setTitle(null);
        note.setContent(null);
        assertNull(note.getTitle());
        assertNull(note.getContent());
    }

    @Test
    @DisplayName("Should handle long content")
    void shouldHandleLongContent() {
        String longContent = "a".repeat(10000);
        note.setContent(longContent);
        assertEquals(10000, note.getContent().length());
    }
}

