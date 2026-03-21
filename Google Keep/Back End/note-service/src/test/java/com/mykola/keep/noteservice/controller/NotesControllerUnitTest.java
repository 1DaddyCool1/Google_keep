package com.mykola.keep.noteservice.controller;

import com.mykola.keep.noteservice.dto.CreateNoteRequest;
import com.mykola.keep.noteservice.dto.LabelIdsRequest;
import com.mykola.keep.noteservice.dto.UpdateNoteRequest;
import com.mykola.keep.noteservice.entity.Note;
import com.mykola.keep.noteservice.repository.NoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotesController Unit Tests")
class NotesControllerUnitTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private NoteController noteController;

    private Note testNote;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("testuser");

        testNote = createNote(1L, "testuser", "Test Note", "Content", false, false);
    }

    private Note createNote(Long id, String username, String title, String content,
                            boolean isTrashed, boolean isArchived) {
        Note note = new Note();
        note.setId(id);
        note.setUsername(username);
        note.setTitle(title);
        note.setContent(content);
        note.setTrashed(isTrashed);
        note.setArchived(isArchived);
        note.setMediaIds(new HashSet<>());
        note.setLabelIds(new HashSet<>());
        if (isTrashed) {
            note.setTrashedAt(LocalDateTime.now());
        }
        if (isArchived) {
            note.setArchivedAt(LocalDateTime.now());
        }
        return note;
    }

    @Test
    @DisplayName("Should list active notes for current user")
    void shouldListActiveNotes() {
        List<Note> notes = List.of(testNote);
        when(noteRepository.findActiveByUsername("testuser")).thenReturn(notes);

        List<?> result = noteController.getAllNotes(null).getBody();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(noteRepository).findActiveByUsername("testuser");
    }

    @Test
    @DisplayName("Should list notes filtered by label")
    void shouldListNotesFilteredByLabel() {
        when(noteRepository.findByUsernameAndLabelId("testuser", 100L))
                .thenReturn(List.of(testNote));

        List<?> result = noteController.getAllNotes("100").getBody();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(noteRepository).findByUsernameAndLabelId("testuser", 100L);
    }

    @Test
    @DisplayName("Should create note without images")
    void shouldCreateNoteWithoutImages() {
        CreateNoteRequest request = new CreateNoteRequest();
        request.setTitle("New Note");
        request.setContent("Content");
        request.setImages(List.of());

        when(noteRepository.save(any(Note.class))).thenReturn(testNote);

        ResponseEntity<?> response = noteController.createNote(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(noteRepository).save(any(Note.class));
    }

    @Test
    @DisplayName("Should create note with images")
    void shouldCreateNoteWithImages() {
        CreateNoteRequest request = new CreateNoteRequest();
        request.setTitle("Note with Images");
        request.setContent("Content");
        request.setImages(List.of("image1", "image2"));

        Map<String, Object> mediaResponse = new HashMap<>();
        mediaResponse.put("ids", List.of(1, 2));
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenReturn(mediaResponse);
        when(noteRepository.save(any(Note.class))).thenReturn(testNote);

        ResponseEntity<?> response = noteController.createNote(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(restTemplate).postForObject(anyString(), any(), eq(Map.class));
        verify(noteRepository).save(any(Note.class));
    }

    @Test
    @DisplayName("Should get note by id")
    void shouldGetNoteById() {
        when(noteRepository.findByIdAndUsername(1L, "testuser"))
                .thenReturn(Optional.of(testNote));

        ResponseEntity<?> response = noteController.getNoteById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(noteRepository).findByIdAndUsername(1L, "testuser");
    }

    @Test
    @DisplayName("Should return 404 when note not found")
    void shouldReturn404WhenNoteNotFound() {
        when(noteRepository.findByIdAndUsername(999L, "testuser"))
                .thenReturn(Optional.empty());

        ResponseEntity<?> response = noteController.getNoteById(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(noteRepository).findByIdAndUsername(999L, "testuser");
    }

    @Test
    @DisplayName("Should update note title and content")
    void shouldUpdateNote() {
        UpdateNoteRequest request = new UpdateNoteRequest();
        request.setTitle("Updated Title");
        request.setContent("Updated Content");
        request.setImageIds(List.of());
        request.setNewImages(List.of());

        when(noteRepository.findByIdAndUsername(1L, "testuser"))
                .thenReturn(Optional.of(testNote));
        when(noteRepository.save(any(Note.class))).thenReturn(testNote);

        ResponseEntity<?> response = noteController.updateNote(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(noteRepository).findByIdAndUsername(1L, "testuser");
        verify(noteRepository).save(any(Note.class));
    }

    @Test
    @DisplayName("Should move note to trash on delete")
    void shouldMoveNoteToTrash() {
        when(noteRepository.findByIdAndUsername(1L, "testuser"))
                .thenReturn(Optional.of(testNote));
        when(noteRepository.save(any(Note.class))).thenReturn(testNote);

        ResponseEntity<Void> response = noteController.deleteNote(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(noteRepository).save(argThat(note -> note.isTrashed()));
    }

    @Test
    @DisplayName("Should set note labels")
    void shouldSetNoteLabels() {
        LabelIdsRequest request = new LabelIdsRequest();
        request.setLabelIds(List.of("10", "20"));

        when(noteRepository.findByIdAndUsername(1L, "testuser"))
                .thenReturn(Optional.of(testNote));
        when(noteRepository.save(any(Note.class))).thenReturn(testNote);

        ResponseEntity<?> response = noteController.setNoteLabels(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(noteRepository).save(argThat(note -> note.getLabelIds().size() == 2));
    }

    @Test
    @DisplayName("Should copy note")
    void shouldCopyNote() {
        testNote.setLabelIds(new HashSet<>(Set.of(10L)));
        testNote.setMediaIds(new HashSet<>(Set.of(1L)));

        when(noteRepository.findByIdAndUsername(1L, "testuser"))
                .thenReturn(Optional.of(testNote));

        Map<String, Object> copyResponse = new HashMap<>();
        copyResponse.put("id", 100);
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
                .thenReturn(copyResponse);

        when(noteRepository.save(any(Note.class))).thenReturn(testNote);

        ResponseEntity<?> response = noteController.duplicateNote(1L);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(noteRepository).save(any(Note.class));
    }

    @Test
    @DisplayName("Should get archived notes")
    void shouldGetArchivedNotes() {
        Note archivedNote = createNote(2L, "testuser", "Archived", "Content", false, true);
        when(noteRepository.findArchivedByUsername("testuser"))
                .thenReturn(List.of(archivedNote));

        List<?> result = noteController.getArchivedNotes().getBody();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(noteRepository).findArchivedByUsername("testuser");
    }

    @Test
    @DisplayName("Should archive note")
    void shouldArchiveNote() {
        when(noteRepository.findByIdAndUsername(1L, "testuser"))
                .thenReturn(Optional.of(testNote));
        when(noteRepository.save(any(Note.class))).thenReturn(testNote);

        ResponseEntity<?> response = noteController.archiveNote(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(noteRepository).save(argThat(Note::isArchived));
    }

    @Test
    @DisplayName("Should not archive trashed note")
    void shouldNotArchiveTrashedNote() {
        testNote.setTrashed(true);
        when(noteRepository.findByIdAndUsername(1L, "testuser"))
                .thenReturn(Optional.of(testNote));

        ResponseEntity<?> response = noteController.archiveNote(1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    @DisplayName("Should unarchive note")
    void shouldUnarchiveNote() {
        testNote.setArchived(true);
        when(noteRepository.findByIdAndUsername(1L, "testuser"))
                .thenReturn(Optional.of(testNote));
        when(noteRepository.save(any(Note.class))).thenReturn(testNote);

        ResponseEntity<?> response = noteController.unarchiveNote(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(noteRepository).save(argThat(note -> !note.isArchived()));
    }

    @Test
    @DisplayName("Should get trashed notes")
    void shouldGetTrashedNotes() {
        Note trashedNote = createNote(2L, "testuser", "Trashed", "Content", true, false);
        when(noteRepository.findTrashedByUsername("testuser"))
                .thenReturn(List.of(trashedNote));

        List<?> result = noteController.getTrashedNotes().getBody();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(noteRepository).findTrashedByUsername("testuser");
    }

    @Test
    @DisplayName("Should restore note from trash")
    void shouldRestoreNote() {
        testNote.setTrashed(true);
        testNote.setTrashedAt(LocalDateTime.now());

        when(noteRepository.findByIdAndUsername(1L, "testuser"))
                .thenReturn(Optional.of(testNote));
        when(noteRepository.save(any(Note.class))).thenReturn(testNote);

        ResponseEntity<?> response = noteController.restoreNote(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(noteRepository).save(argThat(note -> !note.isTrashed()));
    }

    @Test
    @DisplayName("Should empty trash and delete all trashed notes")
    void shouldEmptyTrash() {
        Note trashedNote = createNote(2L, "testuser", "Trashed", "Content", true, false);
        trashedNote.setMediaIds(new HashSet<>(Set.of(1L, 2L)));

        when(noteRepository.findTrashedByUsername("testuser"))
                .thenReturn(List.of(trashedNote));

        ResponseEntity<Void> response = noteController.emptyTrash();

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(restTemplate, times(2)).delete(anyString());
        verify(noteRepository).delete(trashedNote);
    }

    @Test
    @DisplayName("Should permanently delete note")
    void shouldPermanentlyDeleteNote() {
        testNote.setMediaIds(new HashSet<>(Set.of(1L, 2L)));

        when(noteRepository.findByIdAndUsername(1L, "testuser"))
                .thenReturn(Optional.of(testNote));

        ResponseEntity<Void> response = noteController.permanentlyDeleteNote(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(restTemplate, times(2)).delete(anyString());
        verify(noteRepository).delete(testNote);
    }

    @Test
    @DisplayName("Should handle invalid label id gracefully")
    void shouldHandleInvalidLabelId() {
        when(noteRepository.findActiveByUsername("testuser"))
                .thenReturn(List.of(testNote));

        List<?> result = noteController.getAllNotes("invalid").getBody();

        assertNotNull(result);
        verify(noteRepository).findActiveByUsername("testuser");
        verify(noteRepository, never()).findByUsernameAndLabelId(anyString(), anyLong());
    }

    @Test
    @DisplayName("Should handle null images list in create request")
    void shouldHandleNullImagesInCreateRequest() {
        CreateNoteRequest request = new CreateNoteRequest();
        request.setTitle("Note");
        request.setContent("Content");
        request.setImages(null);

        when(noteRepository.save(any(Note.class))).thenReturn(testNote);

        ResponseEntity<?> response = noteController.createNote(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(noteRepository).save(any(Note.class));
    }
}

