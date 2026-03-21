package com.mykola.keep.noteservice.controller;

import com.mykola.keep.noteservice.dto.LabelIdsRequest;
import com.mykola.keep.noteservice.dto.NoteDTO;
import com.mykola.keep.noteservice.dto.CreateNoteRequest;
import com.mykola.keep.noteservice.dto.UpdateNoteRequest;
import com.mykola.keep.noteservice.service.impl.NoteServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Tag(name = "Note API", description = "Note-related endpoints")
@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteServiceImpl noteService;
    public NoteController(NoteServiceImpl noteService) {this.noteService = noteService;}

    @Operation(summary = "Get all notes for the current user", description = "Retrieve all notes associated with the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notes retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public ResponseEntity<List<NoteDTO>>getAllNotes(@RequestParam(name = "labelId", required = false) String labelId) {
        return ResponseEntity.ok(noteService.getAllNotes(labelId));
    }

    @Operation(summary = "Get a specific note by ID", description = "Retrieve a specific note by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Note retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Note not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Optional<NoteDTO>>getNoteById(
            @Parameter(description = "Note ID", required = true)
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(noteService.getNoteById(id));
    }

    @Operation(summary = "Create a new note", description = "Create a new note for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Note created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping
    public ResponseEntity<NoteDTO>createNote(@RequestBody CreateNoteRequest request) {
        return ResponseEntity.ok(noteService.createNote(request));
    }

    @Operation(summary = "Duplicate a note", description = "Duplicate an existing note")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Note duplicated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("/{id}/copy")
    public ResponseEntity<NoteDTO>duplicateNote(
            @Parameter(description = "Note ID", required = true)
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(noteService.duplicateNote(id));
    }

    @Operation(summary = "Update an existing note", description = "Update an existing note")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Note updated successfully"),
            @ApiResponse(responseCode = "404", description = "Note not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<NoteDTO>updateNote(
            @Parameter(description = "Note ID", required = true)
            @PathVariable Long id,
            @RequestBody UpdateNoteRequest request
    ) {
        return ResponseEntity.ok(noteService.updateNote(request, id));
    }

    @Operation(summary = "Delete a note", description = "Delete an existing note")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Note deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Note not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void>deleteNote(
            @Parameter(description = "Note ID", required = true)
            @PathVariable Long id
    ) {
        noteService.deleteNote(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Set labels for a note", description = "Set labels for an existing note")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Labels set successfully"),
            @ApiResponse(responseCode = "404", description = "Note not found")
    })
    @PutMapping("/{id}/labels")
    public ResponseEntity<NoteDTO> setNoteLabels(
            @Parameter(description = "Note ID", required = true)
            @PathVariable Long id,
            @RequestBody LabelIdsRequest request
    ) {
        return ResponseEntity.ok(noteService.setNoteLabels(id, request));
    }

    // Archive management endpoints
    @Operation(summary = "Get archived notes", description = "Retrieve all archived notes for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Archived notes retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/archived")
    public ResponseEntity<List<NoteDTO>> getArchivedNotes() {
        return ResponseEntity.ok(noteService.getArchivedNotes());
    }

    @Operation(summary = "Unarchive a note", description = "Unarchive an archived note")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Note unarchived successfully"),
            @ApiResponse(responseCode = "404", description = "Note not found")
    })
    @PostMapping("/{id}/archive")
    public ResponseEntity<NoteDTO> archiveNote(
            @Parameter(description = "Note ID", required = true)
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(noteService.archiveNote(id));
    }

    @Operation(summary = "Restore a note from the trash", description = "Restore a note from the trash")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Note restored successfully"),
            @ApiResponse(responseCode = "404", description = "Note not found")
    })
    @PostMapping("/{id}/unarchive")
    public ResponseEntity<NoteDTO> unarchiveNote(
            @Parameter(description = "Note ID", required = true)
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(noteService.unarchiveNote(id));
    }

    // Trash management endpoints
    @Operation(summary = "Get trashed notes", description = "Retrieve all trashed notes for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trashed notes retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/trash")
    public ResponseEntity<List<NoteDTO>> getTrashedNotes() {
        return ResponseEntity.ok(noteService.getTrashedNotes());
    }

    @Operation(summary = "Move a note to the trash", description = "Move a note to the trash")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Note moved to trash successfully"),
            @ApiResponse(responseCode = "404", description = "Note not found")
    })
    @PostMapping("/{id}/restore")
    public ResponseEntity<NoteDTO> restoreNote(
            @Parameter(description = "Note ID", required = true)
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(noteService.restoreNote(id));
    }

    @Operation(summary = "Empty the trash", description = "Empty the trash")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Trash emptied successfully")
    })
    @DeleteMapping("/trash/empty")
    public ResponseEntity<Void> emptyTrash() {
        noteService.emptyTrash();
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Permanently delete a note", description = "Permanently delete a note")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Note permanently deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Note not found")
    })
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<Void> permanentlyDeleteNote(
            @Parameter(description = "Note ID", required = true)
            @PathVariable Long id
    ) {
        noteService.permanentlyDeleteNote(id);
        return ResponseEntity.noContent().build();
    }

    // Internal endpoint for Labels Service to call when deleting a label
    @Operation(summary = "Remove a label from notes", description = "Remove a label from all notes", hidden = true)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Label removed from notes successfully"),
            @ApiResponse(responseCode = "404", description = "Label not found")
    })
    @DeleteMapping("/internal/remove-label/{labelId}")
    public ResponseEntity<Void> removeLabelFromNotes(
            @Parameter(description = "Label ID", required = true)
            @PathVariable Long labelId
    ) {
        noteService.removeLabelFromNotes(labelId);
        return ResponseEntity.noContent().build();
    }
}
