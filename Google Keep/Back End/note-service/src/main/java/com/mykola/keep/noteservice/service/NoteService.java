package com.mykola.keep.noteservice.service;

import com.mykola.keep.noteservice.dto.CreateNoteRequest;
import com.mykola.keep.noteservice.dto.LabelIdsRequest;
import com.mykola.keep.noteservice.dto.NoteDTO;
import com.mykola.keep.noteservice.dto.UpdateNoteRequest;

import java.util.List;
import java.util.Optional;

public interface NoteService {
    List<NoteDTO> getAllNotes(String username);
    Optional<NoteDTO> getNoteById(Long id);
    NoteDTO createNote(CreateNoteRequest request);
    NoteDTO updateNote(UpdateNoteRequest request, Long id);
    NoteDTO duplicateNote(Long id);
    void deleteNote(Long id);
    NoteDTO setNoteLabels(Long id, LabelIdsRequest request);
    List<NoteDTO> getArchivedNotes();
    NoteDTO archiveNote(Long id);
    NoteDTO unarchiveNote(Long id);
    List<NoteDTO> getTrashedNotes();
    NoteDTO restoreNote(Long id);
    void emptyTrash();
    void permanentlyDeleteNote(Long id);
    void removeLabelFromNotes(Long labelId);
}
