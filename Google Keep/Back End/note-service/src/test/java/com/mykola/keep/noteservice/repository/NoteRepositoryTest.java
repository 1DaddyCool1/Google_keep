package com.mykola.keep.noteservice.repository;

import com.mykola.keep.noteservice.entity.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("NoteRepository Tests")
public class NoteRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private NoteRepository noteRepository;

    private Note testNote1;
    private Note testNote2;
    private Note testNote3;

    @BeforeEach
    void setUp() {
        // Clear the database
        noteRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // Create test notes
        testNote1 = createNote("user1", "Note 1", "Content 1", false, false);
        testNote2 = createNote("user1", "Note 2", "Content 2", false, false);
        testNote3 = createNote("user2", "Note 3", "Content 3", false, false);
    }

    private Note createNote(String username, String title, String content,
                           boolean isTrashed, boolean isArchived) {
        Note note = new Note();
        note.setUsername(username);
        note.setTitle(title);
        note.setContent(content);
        note.setTrashed(isTrashed);
        note.setArchived(isArchived);
        if (isTrashed) {
            note.setTrashedAt(LocalDateTime.now());
        }
        if (isArchived) {
            note.setArchivedAt(LocalDateTime.now());
        }
        return entityManager.persist(note);
    }

    @Test
    @DisplayName("Should find notes by username")
    void shouldFindByUsername() {
        List<Note> user1Notes = noteRepository.findByUsername("user1");

        assertEquals(2, user1Notes.size());
        assertTrue(user1Notes.stream().allMatch(n -> n.getUsername().equals("user1")));
    }

    @Test
    @DisplayName("Should find note by id and username")
    void shouldFindByIdAndUsername() {
        Optional<Note> found = noteRepository.findByIdAndUsername(testNote1.getId(), "user1");

        assertTrue(found.isPresent());
        assertEquals("Note 1", found.get().getTitle());
    }

    @Test
    @DisplayName("Should not find note with wrong username")
    void shouldNotFindWithWrongUsername() {
        Optional<Note> found = noteRepository.findByIdAndUsername(testNote1.getId(), "user2");

        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Should find active notes by username")
    void shouldFindActiveByUsername() {
        // Create trashed and archived notes
        createNote("user1", "Trashed", "Content", true, false);
        createNote("user1", "Archived", "Content", false, true);
        entityManager.flush();

        List<Note> activeNotes = noteRepository.findActiveByUsername("user1");

        assertEquals(2, activeNotes.size());
        assertTrue(activeNotes.stream().noneMatch(Note::isTrashed));
        assertTrue(activeNotes.stream().noneMatch(Note::isArchived));
    }

    @Test
    @DisplayName("Should find trashed notes by username")
    void shouldFindTrashedByUsername() {
        Note trashedNote = createNote("user1", "Trashed Note", "Content", true, false);
        entityManager.flush();

        List<Note> trashedNotes = noteRepository.findTrashedByUsername("user1");

        assertEquals(1, trashedNotes.size());
        assertEquals("Trashed Note", trashedNotes.get(0).getTitle());
        assertTrue(trashedNotes.get(0).isTrashed());
    }

    @Test
    @DisplayName("Should find archived notes by username")
    void shouldFindArchivedByUsername() {
        Note archivedNote = createNote("user1", "Archived Note", "Content", false, true);
        entityManager.flush();

        List<Note> archivedNotes = noteRepository.findArchivedByUsername("user1");

        assertEquals(1, archivedNotes.size());
        assertEquals("Archived Note", archivedNotes.get(0).getTitle());
        assertTrue(archivedNotes.get(0).isArchived());
        assertFalse(archivedNotes.get(0).isTrashed());
    }

    @Test
    @DisplayName("Should find notes by username and label")
    void shouldFindByUsernameAndLabelId() {
        Set<Long> labelIds = new HashSet<>();
        labelIds.add(100L);
        testNote1.setLabelIds(labelIds);
        entityManager.persist(testNote1);
        entityManager.flush();

        List<Note> notesWithLabel = noteRepository.findByUsernameAndLabelId("user1", 100L);

        assertEquals(1, notesWithLabel.size());
        assertEquals("Note 1", notesWithLabel.get(0).getTitle());
        assertTrue(notesWithLabel.get(0).getLabelIds().contains(100L));
    }

    @Test
    @DisplayName("Should not return trashed notes when finding by label")
    void shouldNotReturnTrashedNotesWhenFindingByLabel() {
        Set<Long> labelIds = new HashSet<>();
        labelIds.add(100L);

        testNote1.setLabelIds(labelIds);
        testNote1.setTrashed(true);
        testNote1.setTrashedAt(LocalDateTime.now());
        entityManager.persist(testNote1);
        entityManager.flush();

        List<Note> notesWithLabel = noteRepository.findByUsernameAndLabelId("user1", 100L);

        assertEquals(0, notesWithLabel.size());
    }

    @Test
    @DisplayName("Should not return archived notes when finding by label")
    void shouldNotReturnArchivedNotesWhenFindingByLabel() {
        Set<Long> labelIds = new HashSet<>();
        labelIds.add(100L);

        testNote1.setLabelIds(labelIds);
        testNote1.setArchived(true);
        testNote1.setArchivedAt(LocalDateTime.now());
        entityManager.persist(testNote1);
        entityManager.flush();

        List<Note> notesWithLabel = noteRepository.findByUsernameAndLabelId("user1", 100L);

        assertEquals(0, notesWithLabel.size());
    }

    @Test
    @DisplayName("Should find notes trashed before cutoff date")
    void shouldFindTrashedNotesBefore() {
        LocalDateTime oldDate = LocalDateTime.now().minusDays(10);
        LocalDateTime recentDate = LocalDateTime.now().minusDays(2);

        Note oldTrashedNote = createNote("user1", "Old Trashed", "Content", true, false);
        oldTrashedNote.setTrashedAt(oldDate);
        entityManager.persist(oldTrashedNote);

        Note recentTrashedNote = createNote("user1", "Recent Trashed", "Content", true, false);
        recentTrashedNote.setTrashedAt(recentDate);
        entityManager.persist(recentTrashedNote);

        entityManager.flush();

        LocalDateTime cutoff = LocalDateTime.now().minusDays(5);
        List<Note> oldNotes = noteRepository.findTrashedNotesBefore(cutoff);

        assertEquals(1, oldNotes.size());
        assertEquals("Old Trashed", oldNotes.get(0).getTitle());
    }

    @Test
    @DisplayName("Should find notes by username and trashed status")
    void shouldFindByUsernameAndIsTrashed() {
        createNote("user1", "Trashed", "Content", true, false);
        entityManager.flush();

        List<Note> notTrashed = noteRepository.findByUsernameAndIsTrashed("user1", false);
        List<Note> trashed = noteRepository.findByUsernameAndIsTrashed("user1", true);

        assertEquals(2, notTrashed.size());
        assertEquals(1, trashed.size());
    }

    @Test
    @DisplayName("Should save note with media IDs")
    void shouldSaveNoteWithMediaIds() {
        Note note = new Note();
        note.setUsername("user1");
        note.setTitle("Note with Media");
        note.setContent("Content");

        Set<Long> mediaIds = new HashSet<>();
        mediaIds.add(1L);
        mediaIds.add(2L);
        note.setMediaIds(mediaIds);

        Note saved = noteRepository.save(note);
        entityManager.flush();
        entityManager.clear();

        Note found = noteRepository.findById(saved.getId()).orElseThrow();
        assertEquals(2, found.getMediaIds().size());
        assertTrue(found.getMediaIds().contains(1L));
        assertTrue(found.getMediaIds().contains(2L));
    }

    @Test
    @DisplayName("Should save note with label IDs")
    void shouldSaveNoteWithLabelIds() {
        Note note = new Note();
        note.setUsername("user1");
        note.setTitle("Note with Labels");
        note.setContent("Content");

        Set<Long> labelIds = new HashSet<>();
        labelIds.add(10L);
        labelIds.add(20L);
        labelIds.add(30L);
        note.setLabelIds(labelIds);

        Note saved = noteRepository.save(note);
        entityManager.flush();
        entityManager.clear();

        Note found = noteRepository.findById(saved.getId()).orElseThrow();
        assertEquals(3, found.getLabelIds().size());
        assertTrue(found.getLabelIds().contains(10L));
        assertTrue(found.getLabelIds().contains(20L));
        assertTrue(found.getLabelIds().contains(30L));
    }

    @Test
    @DisplayName("Should update note")
    void shouldUpdateNote() {
        testNote1.setTitle("Updated Title");
        testNote1.setContent("Updated Content");
        noteRepository.save(testNote1);
        entityManager.flush();
        entityManager.clear();

        Note found = noteRepository.findById(testNote1.getId()).orElseThrow();
        assertEquals("Updated Title", found.getTitle());
        assertEquals("Updated Content", found.getContent());
    }

    @Test
    @DisplayName("Should delete note")
    void shouldDeleteNote() {
        Long id = testNote1.getId();
        noteRepository.delete(testNote1);
        entityManager.flush();

        Optional<Note> found = noteRepository.findById(id);
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Should return empty list when no notes found")
    void shouldReturnEmptyListWhenNoNotesFound() {
        List<Note> notes = noteRepository.findByUsername("nonexistent");

        assertNotNull(notes);
        assertTrue(notes.isEmpty());
    }

    @Test
    @DisplayName("Should handle multiple labels on same note")
    void shouldHandleMultipleLabelsOnSameNote() {
        Set<Long> labelIds = new HashSet<>();
        labelIds.add(100L);
        labelIds.add(200L);
        labelIds.add(300L);

        testNote1.setLabelIds(labelIds);
        entityManager.persist(testNote1);
        entityManager.flush();

        List<Note> notes100 = noteRepository.findByUsernameAndLabelId("user1", 100L);
        List<Note> notes200 = noteRepository.findByUsernameAndLabelId("user1", 200L);
        List<Note> notes300 = noteRepository.findByUsernameAndLabelId("user1", 300L);

        assertEquals(1, notes100.size());
        assertEquals(1, notes200.size());
        assertEquals(1, notes300.size());
        assertEquals(testNote1.getId(), notes100.get(0).getId());
        assertEquals(testNote1.getId(), notes200.get(0).getId());
        assertEquals(testNote1.getId(), notes300.get(0).getId());
    }
}

