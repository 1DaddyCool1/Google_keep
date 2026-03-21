package com.mykola.keep.noteservice.service;

import com.mykola.keep.noteservice.entity.Note;
import com.mykola.keep.noteservice.repository.NoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrashCleanupService Tests")
public class TrashCleanupServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private TrashCleanupService trashCleanupService;

    private Note oldTrashedNote1;
    private Note oldTrashedNote2;
    private Note recentTrashedNote;

    @BeforeEach
    void setUp() {
        // Create old trashed notes (more than 7 days)
        oldTrashedNote1 = createNote(1L, "user1", "Old Note 1", true,
                                     LocalDateTime.now().minusDays(10));
        oldTrashedNote1.setMediaIds(createMediaIds(1L, 2L));

        oldTrashedNote2 = createNote(2L, "user2", "Old Note 2", true,
                                     LocalDateTime.now().minusDays(8));
        oldTrashedNote2.setMediaIds(createMediaIds(3L));

        // Create recent trashed note (less than 7 days)
        recentTrashedNote = createNote(3L, "user1", "Recent Note", true,
                                       LocalDateTime.now().minusDays(3));
    }

    private Note createNote(Long id, String username, String title,
                           boolean isTrashed, LocalDateTime trashedAt) {
        Note note = new Note();
        note.setId(id);
        note.setUsername(username);
        note.setTitle(title);
        note.setContent("Content for " + title);
        note.setTrashed(isTrashed);
        note.setTrashedAt(trashedAt);
        note.setMediaIds(new HashSet<>());
        return note;
    }

    private Set<Long> createMediaIds(Long... ids) {
        Set<Long> mediaIds = new HashSet<>();
        for (Long id : ids) {
            mediaIds.add(id);
        }
        return mediaIds;
    }

    @Test
    @DisplayName("Should cleanup notes trashed more than 7 days ago")
    void shouldCleanupOldTrashedNotes() {
        List<Note> oldNotes = Arrays.asList(oldTrashedNote1, oldTrashedNote2);
        when(noteRepository.findTrashedNotesBefore(any(LocalDateTime.class)))
            .thenReturn(oldNotes);

        trashCleanupService.cleanupOldTrashedNotes();

        // Verify that findTrashedNotesBefore was called with appropriate cutoff
        ArgumentCaptor<LocalDateTime> dateCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(noteRepository).findTrashedNotesBefore(dateCaptor.capture());

        LocalDateTime cutoff = dateCaptor.getValue();
        assertTrue(cutoff.isBefore(LocalDateTime.now()));
        assertTrue(cutoff.isAfter(LocalDateTime.now().minusDays(8)));

        // Verify images were deleted from media service
        verify(restTemplate, times(3)).delete(anyString()); // 2 + 1 = 3 images
        verify(restTemplate).delete("http://media-service/api/media/1");
        verify(restTemplate).delete("http://media-service/api/media/2");
        verify(restTemplate).delete("http://media-service/api/media/3");

        // Verify notes were deleted
        verify(noteRepository).delete(oldTrashedNote1);
        verify(noteRepository).delete(oldTrashedNote2);
    }

    @Test
    @DisplayName("Should not cleanup notes trashed less than 7 days ago")
    void shouldNotCleanupRecentTrashedNotes() {
        when(noteRepository.findTrashedNotesBefore(any(LocalDateTime.class)))
            .thenReturn(Arrays.asList());

        trashCleanupService.cleanupOldTrashedNotes();

        verify(noteRepository).findTrashedNotesBefore(any(LocalDateTime.class));
        verify(restTemplate, never()).delete(anyString());
        verify(noteRepository, never()).delete(any(Note.class));
    }

    @Test
    @DisplayName("Should handle notes with no media")
    void shouldHandleNotesWithNoMedia() {
        Note noteWithoutMedia = createNote(4L, "user1", "No Media Note", true,
                                          LocalDateTime.now().minusDays(10));
        noteWithoutMedia.setMediaIds(new HashSet<>());

        when(noteRepository.findTrashedNotesBefore(any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(noteWithoutMedia));

        trashCleanupService.cleanupOldTrashedNotes();

        verify(restTemplate, never()).delete(anyString());
        verify(noteRepository).delete(noteWithoutMedia);
    }

    @Test
    @DisplayName("Should handle media service errors gracefully")
    void shouldHandleMediaServiceErrors() {
        when(noteRepository.findTrashedNotesBefore(any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(oldTrashedNote1));

        doThrow(new RuntimeException("Media service unavailable"))
            .when(restTemplate).delete(anyString());

        // Should not throw exception
        assertDoesNotThrow(() -> trashCleanupService.cleanupOldTrashedNotes());

        // Should still attempt to delete the note
        verify(noteRepository).delete(oldTrashedNote1);
    }

    @Test
    @DisplayName("Should handle note deletion errors")
    void shouldHandleNoteDeletionErrors() {
        when(noteRepository.findTrashedNotesBefore(any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(oldTrashedNote1, oldTrashedNote2));

        doThrow(new RuntimeException("Database error"))
            .when(noteRepository).delete(oldTrashedNote1);

        // Should not throw exception and should continue with other notes
        assertDoesNotThrow(() -> trashCleanupService.cleanupOldTrashedNotes());

        verify(noteRepository).delete(oldTrashedNote1);
        verify(noteRepository).delete(oldTrashedNote2);
    }

    @Test
    @DisplayName("Should handle empty result from repository")
    void shouldHandleEmptyResult() {
        when(noteRepository.findTrashedNotesBefore(any(LocalDateTime.class)))
            .thenReturn(Arrays.asList());

        assertDoesNotThrow(() -> trashCleanupService.cleanupOldTrashedNotes());

        verify(noteRepository).findTrashedNotesBefore(any(LocalDateTime.class));
        verify(restTemplate, never()).delete(anyString());
        verify(noteRepository, never()).delete(any(Note.class));
    }

    @Test
    @DisplayName("Should cleanup notes with multiple media files")
    void shouldCleanupNotesWithMultipleMedia() {
        Note noteWithManyMedia = createNote(5L, "user1", "Many Media", true,
                                           LocalDateTime.now().minusDays(10));
        noteWithManyMedia.setMediaIds(createMediaIds(1L, 2L, 3L, 4L, 5L));

        when(noteRepository.findTrashedNotesBefore(any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(noteWithManyMedia));

        trashCleanupService.cleanupOldTrashedNotes();

        verify(restTemplate, times(5)).delete(anyString());
        verify(noteRepository).delete(noteWithManyMedia);
    }

    @Test
    @DisplayName("Should use correct cutoff date calculation")
    void shouldUseCorrectCutoffDate() {
        when(noteRepository.findTrashedNotesBefore(any(LocalDateTime.class)))
            .thenReturn(Arrays.asList());

        trashCleanupService.cleanupOldTrashedNotes();

        ArgumentCaptor<LocalDateTime> dateCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(noteRepository).findTrashedNotesBefore(dateCaptor.capture());

        LocalDateTime cutoff = dateCaptor.getValue();
        LocalDateTime expectedCutoff = LocalDateTime.now().minusDays(7);

        // Allow 1 minute tolerance for test execution time
        assertTrue(Math.abs(cutoff.compareTo(expectedCutoff)) < 60);
    }

    @Test
    @DisplayName("Should process multiple users notes")
    void shouldProcessMultipleUsersNotes() {
        Note user1Note = createNote(1L, "user1", "User1 Note", true,
                                    LocalDateTime.now().minusDays(10));
        user1Note.setMediaIds(createMediaIds(1L));

        Note user2Note = createNote(2L, "user2", "User2 Note", true,
                                    LocalDateTime.now().minusDays(8));
        user2Note.setMediaIds(createMediaIds(2L));

        Note user3Note = createNote(3L, "user3", "User3 Note", true,
                                    LocalDateTime.now().minusDays(15));
        user3Note.setMediaIds(createMediaIds(3L));

        when(noteRepository.findTrashedNotesBefore(any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(user1Note, user2Note, user3Note));

        trashCleanupService.cleanupOldTrashedNotes();

        verify(noteRepository).delete(user1Note);
        verify(noteRepository).delete(user2Note);
        verify(noteRepository).delete(user3Note);
        verify(restTemplate, times(3)).delete(anyString());
    }

    @Test
    @DisplayName("Should handle partial media deletion failures")
    void shouldHandlePartialMediaDeletionFailures() {
        oldTrashedNote1.setMediaIds(createMediaIds(1L, 2L, 3L));

        when(noteRepository.findTrashedNotesBefore(any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(oldTrashedNote1));

        // Fail on second media deletion
        doNothing().when(restTemplate).delete("http://media-service/api/media/1");
        doThrow(new RuntimeException("Media not found"))
            .when(restTemplate).delete("http://media-service/api/media/2");
        doNothing().when(restTemplate).delete("http://media-service/api/media/3");

        assertDoesNotThrow(() -> trashCleanupService.cleanupOldTrashedNotes());

        // Should still delete the note despite media deletion failure
        verify(noteRepository).delete(oldTrashedNote1);
    }

    @Test
    @DisplayName("Should call repository method only once per cleanup")
    void shouldCallRepositoryOnce() {
        when(noteRepository.findTrashedNotesBefore(any(LocalDateTime.class)))
            .thenReturn(Arrays.asList());

        trashCleanupService.cleanupOldTrashedNotes();

        verify(noteRepository, times(1)).findTrashedNotesBefore(any(LocalDateTime.class));
    }
}

