package com.mykola.keep.noteservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mykola.keep.noteservice.dto.CreateNoteRequest;
import com.mykola.keep.noteservice.dto.LabelIdsRequest;
import com.mykola.keep.noteservice.dto.UpdateNoteRequest;
import com.mykola.keep.noteservice.entity.Note;
import com.mykola.keep.noteservice.repository.NoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("NotesController Integration Tests")
public class NotesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NoteRepository noteRepository;

    @MockBean
    private RestTemplate restTemplate;

    private Note testNote;
    private List<Note> testNotes;

    @BeforeEach
    void setUp() {
        testNote = createNote(1L, "testuser", "Test Note", "Test Content", false, false);

        Note note2 = createNote(2L, "testuser", "Note 2", "Content 2", false, false);
        Note note3 = createNote(3L, "testuser", "Note 3", "Content 3", false, false);

        testNotes = Arrays.asList(testNote, note2, note3);
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
    @DisplayName("Should get all active notes for current user")
    @WithMockUser(username = "testuser")
    void shouldGetAllActiveNotes() throws Exception {
        when(noteRepository.findActiveByUsername("testuser")).thenReturn(testNotes);

        mockMvc.perform(get("/api/notes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].title").value("Test Note"))
                .andExpect(jsonPath("$[1].title").value("Note 2"))
                .andExpect(jsonPath("$[2].title").value("Note 3"));

        verify(noteRepository).findActiveByUsername("testuser");
    }

    @Test
    @DisplayName("Should get notes filtered by label")
    @WithMockUser(username = "testuser")
    void shouldGetNotesFilteredByLabel() throws Exception {
        Note labeledNote = createNote(1L, "testuser", "Labeled Note", "Content", false, false);
        when(noteRepository.findByUsernameAndLabelId("testuser", 100L))
            .thenReturn(Arrays.asList(labeledNote));

        mockMvc.perform(get("/api/notes")
                .param("labelId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Labeled Note"));

        verify(noteRepository).findByUsernameAndLabelId("testuser", 100L);
    }

    @Test
    @DisplayName("Should create note successfully")
    @WithMockUser(username = "testuser")
    void shouldCreateNote() throws Exception {
        CreateNoteRequest request = new CreateNoteRequest();
        request.setTitle("New Note");
        request.setContent("<p>New Content</p>");
        request.setImages(Arrays.asList());

        Note savedNote = createNote(1L, "testuser", "New Note", "<p>New Content</p>", false, false);
        when(noteRepository.save(any(Note.class))).thenReturn(savedNote);

        mockMvc.perform(post("/api/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Note"))
                .andExpect(jsonPath("$.content").value("<p>New Content</p>"));

        verify(noteRepository).save(any(Note.class));
    }

    @Test
    @DisplayName("Should create note with images")
    @WithMockUser(username = "testuser")
    void shouldCreateNoteWithImages() throws Exception {
        CreateNoteRequest request = new CreateNoteRequest();
        request.setTitle("Note with Images");
        request.setContent("Content");
        request.setImages(Arrays.asList("base64image1", "base64image2"));

        Map<String, Object> mediaResponse = new HashMap<>();
        mediaResponse.put("ids", Arrays.asList(1, 2));
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
            .thenReturn(mediaResponse);

        Note savedNote = createNote(1L, "testuser", "Note with Images", "Content", false, false);
        savedNote.setMediaIds(new HashSet<>(Arrays.asList(1L, 2L)));
        when(noteRepository.save(any(Note.class))).thenReturn(savedNote);

        mockMvc.perform(post("/api/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mediaIds", hasSize(2)));

        verify(restTemplate).postForObject(anyString(), any(), eq(Map.class));
        verify(noteRepository).save(any(Note.class));
    }

    @Test
    @DisplayName("Should get note by id")
    @WithMockUser(username = "testuser")
    void shouldGetNoteById() throws Exception {
        when(noteRepository.findByIdAndUsername(1L, "testuser"))
            .thenReturn(Optional.of(testNote));

        mockMvc.perform(get("/api/notes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Note"))
                .andExpect(jsonPath("$.content").value("Test Content"));

        verify(noteRepository).findByIdAndUsername(1L, "testuser");
    }

    @Test
    @DisplayName("Should return 404 when note not found")
    @WithMockUser(username = "testuser")
    void shouldReturn404WhenNoteNotFound() throws Exception {
        when(noteRepository.findByIdAndUsername(999L, "testuser"))
            .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/notes/999"))
                .andExpect(status().isNotFound());

        verify(noteRepository).findByIdAndUsername(999L, "testuser");
    }

    @Test
    @DisplayName("Should update note")
    @WithMockUser(username = "testuser")
    void shouldUpdateNote() throws Exception {
        UpdateNoteRequest request = new UpdateNoteRequest();
        request.setTitle("Updated Title");
        request.setContent("Updated Content");
        request.setImageIds(Arrays.asList());
        request.setNewImages(Arrays.asList());

        when(noteRepository.findByIdAndUsername(1L, "testuser"))
            .thenReturn(Optional.of(testNote));
        when(noteRepository.save(any(Note.class))).thenReturn(testNote);

        mockMvc.perform(put("/api/notes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(noteRepository).findByIdAndUsername(1L, "testuser");
        verify(noteRepository).save(any(Note.class));
    }

    @Test
    @DisplayName("Should delete note (move to trash)")
    @WithMockUser(username = "testuser")
    void shouldDeleteNote() throws Exception {
        when(noteRepository.findByIdAndUsername(1L, "testuser"))
            .thenReturn(Optional.of(testNote));
        when(noteRepository.save(any(Note.class))).thenReturn(testNote);

        mockMvc.perform(delete("/api/notes/1"))
                .andExpect(status().isNoContent());

        verify(noteRepository).findByIdAndUsername(1L, "testuser");
        verify(noteRepository).save(argThat(note ->
            note.isTrashed() && note.getTrashedAt() != null
        ));
    }

    @Test
    @DisplayName("Should set note labels")
    @WithMockUser(username = "testuser")
    void shouldSetNoteLabels() throws Exception {
        LabelIdsRequest request = new LabelIdsRequest();
        request.setLabelIds(Arrays.asList("10", "20", "30"));

        when(noteRepository.findByIdAndUsername(1L, "testuser"))
            .thenReturn(Optional.of(testNote));
        when(noteRepository.save(any(Note.class))).thenReturn(testNote);

        mockMvc.perform(put("/api/notes/1/labels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(noteRepository).findByIdAndUsername(1L, "testuser");
        verify(noteRepository).save(argThat(note ->
            note.getLabelIds().size() == 3 &&
            note.getLabelIds().contains(10L) &&
            note.getLabelIds().contains(20L) &&
            note.getLabelIds().contains(30L)
        ));
    }

    @Test
    @DisplayName("Should copy note")
    @WithMockUser(username = "testuser")
    void shouldCopyNote() throws Exception {
        testNote.setMediaIds(new HashSet<>(Arrays.asList(1L, 2L)));
        testNote.setLabelIds(new HashSet<>(Arrays.asList(10L, 20L)));

        when(noteRepository.findByIdAndUsername(1L, "testuser"))
            .thenReturn(Optional.of(testNote));

        Map<String, Object> copyResponse = new HashMap<>();
        copyResponse.put("id", 100);
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
            .thenReturn(copyResponse);

        Note copiedNote = createNote(2L, "testuser", "Test Note", "Test Content", false, false);
        when(noteRepository.save(any(Note.class))).thenReturn(copiedNote);

        mockMvc.perform(post("/api/notes/1/copy"))
                .andExpect(status().isCreated());

        verify(noteRepository).findByIdAndUsername(1L, "testuser");
        verify(restTemplate, times(2)).postForObject(anyString(), any(), eq(Map.class));
        verify(noteRepository).save(any(Note.class));
    }

    @Test
    @DisplayName("Should get archived notes")
    @WithMockUser(username = "testuser")
    void shouldGetArchivedNotes() throws Exception {
        Note archivedNote = createNote(1L, "testuser", "Archived", "Content", false, true);
        when(noteRepository.findArchivedByUsername("testuser"))
            .thenReturn(Arrays.asList(archivedNote));

        mockMvc.perform(get("/api/notes/archived"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Archived"));

        verify(noteRepository).findArchivedByUsername("testuser");
    }

    @Test
    @DisplayName("Should archive note")
    @WithMockUser(username = "testuser")
    void shouldArchiveNote() throws Exception {
        when(noteRepository.findByIdAndUsername(1L, "testuser"))
            .thenReturn(Optional.of(testNote));
        when(noteRepository.save(any(Note.class))).thenReturn(testNote);

        mockMvc.perform(post("/api/notes/1/archive"))
                .andExpect(status().isOk());

        verify(noteRepository).save(argThat(note ->
            note.isArchived() && note.getArchivedAt() != null
        ));
    }

    @Test
    @DisplayName("Should not archive trashed note")
    @WithMockUser(username = "testuser")
    void shouldNotArchiveTrashedNote() throws Exception {
        testNote.setTrashed(true);
        when(noteRepository.findByIdAndUsername(1L, "testuser"))
            .thenReturn(Optional.of(testNote));

        mockMvc.perform(post("/api/notes/1/archive"))
                .andExpect(status().isBadRequest());

        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    @DisplayName("Should unarchive note")
    @WithMockUser(username = "testuser")
    void shouldUnarchiveNote() throws Exception {
        testNote.setArchived(true);
        testNote.setArchivedAt(LocalDateTime.now());

        when(noteRepository.findByIdAndUsername(1L, "testuser"))
            .thenReturn(Optional.of(testNote));
        when(noteRepository.save(any(Note.class))).thenReturn(testNote);

        mockMvc.perform(post("/api/notes/1/unarchive"))
                .andExpect(status().isOk());

        verify(noteRepository).save(argThat(note ->
            !note.isArchived() && note.getArchivedAt() == null
        ));
    }

    @Test
    @DisplayName("Should get trashed notes")
    @WithMockUser(username = "testuser")
    void shouldGetTrashedNotes() throws Exception {
        Note trashedNote = createNote(1L, "testuser", "Trashed", "Content", true, false);
        when(noteRepository.findTrashedByUsername("testuser"))
            .thenReturn(Arrays.asList(trashedNote));

        mockMvc.perform(get("/api/notes/trash"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Trashed"));

        verify(noteRepository).findTrashedByUsername("testuser");
    }

    @Test
    @DisplayName("Should restore note from trash")
    @WithMockUser(username = "testuser")
    void shouldRestoreNote() throws Exception {
        testNote.setTrashed(true);
        testNote.setTrashedAt(LocalDateTime.now());

        when(noteRepository.findByIdAndUsername(1L, "testuser"))
            .thenReturn(Optional.of(testNote));
        when(noteRepository.save(any(Note.class))).thenReturn(testNote);

        mockMvc.perform(post("/api/notes/1/restore"))
                .andExpect(status().isOk());

        verify(noteRepository).save(argThat(note ->
            !note.isTrashed() && note.getTrashedAt() == null &&
            !note.isArchived() && note.getArchivedAt() == null
        ));
    }

    @Test
    @DisplayName("Should empty trash")
    @WithMockUser(username = "testuser")
    void shouldEmptyTrash() throws Exception {
        Note trashedNote1 = createNote(1L, "testuser", "Trashed1", "Content", true, false);
        trashedNote1.setMediaIds(new HashSet<>(Arrays.asList(1L)));

        Note trashedNote2 = createNote(2L, "testuser", "Trashed2", "Content", true, false);
        trashedNote2.setMediaIds(new HashSet<>(Arrays.asList(2L, 3L)));

        when(noteRepository.findTrashedByUsername("testuser"))
            .thenReturn(Arrays.asList(trashedNote1, trashedNote2));

        mockMvc.perform(delete("/api/notes/trash/empty"))
                .andExpect(status().isNoContent());

        verify(restTemplate, times(3)).delete(anyString());
        verify(noteRepository).delete(trashedNote1);
        verify(noteRepository).delete(trashedNote2);
    }

    @Test
    @DisplayName("Should permanently delete note")
    @WithMockUser(username = "testuser")
    void shouldPermanentlyDeleteNote() throws Exception {
        testNote.setMediaIds(new HashSet<>(Arrays.asList(1L, 2L)));

        when(noteRepository.findByIdAndUsername(1L, "testuser"))
            .thenReturn(Optional.of(testNote));

        mockMvc.perform(delete("/api/notes/1/permanent"))
                .andExpect(status().isNoContent());

        verify(restTemplate, times(2)).delete(anyString());
        verify(noteRepository).delete(testNote);
    }

    @Test
    @DisplayName("Should remove label from all notes (internal endpoint)")
    @WithMockUser(username = "testuser")
    void shouldRemoveLabelFromNotes() throws Exception {
        when(noteRepository.findAll()).thenReturn(testNotes);
        when(noteRepository.saveAll(anyList())).thenReturn(testNotes);

        mockMvc.perform(delete("/api/notes/internal/remove-label/100"))
                .andExpect(status().isNoContent());

        verify(noteRepository).findAll();
        verify(noteRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should handle invalid label id in query param")
    @WithMockUser(username = "testuser")
    void shouldHandleInvalidLabelId() throws Exception {
        when(noteRepository.findActiveByUsername("testuser")).thenReturn(testNotes);

        mockMvc.perform(get("/api/notes")
                .param("labelId", "invalid"))
                .andExpect(status().isOk());

        verify(noteRepository).findActiveByUsername("testuser");
        verify(noteRepository, never()).findByUsernameAndLabelId(anyString(), anyLong());
    }
}

