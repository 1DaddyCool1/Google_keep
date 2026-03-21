package com.mykola.keep.noteservice.service.impl;

import com.mykola.keep.noteservice.dto.CreateNoteRequest;
import com.mykola.keep.noteservice.dto.LabelIdsRequest;
import com.mykola.keep.noteservice.dto.NoteDTO;
import com.mykola.keep.noteservice.dto.UpdateNoteRequest;
import com.mykola.keep.noteservice.entity.Note;
import com.mykola.keep.noteservice.mapper.NoteMapper;
import com.mykola.keep.noteservice.repository.NoteRepository;
import com.mykola.keep.noteservice.service.NoteService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.mykola.keep.noteservice.mapper.NoteMapper.toDto;

@Service
public class NoteServiceImpl implements NoteService {

    private final RestTemplate restTemplate;
    private final NoteRepository noteRepository;
    public NoteServiceImpl(RestTemplate restTemplate, NoteRepository noteRepository) {
        this.restTemplate = restTemplate;
        this.noteRepository = noteRepository;}

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth == null) {
            return null;
        }

        Object principal = auth.getPrincipal();
        if(principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            return ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        } else {
            return String.valueOf(principal);
        }
    }

    @Override
    public List<NoteDTO> getAllNotes(String labelId) {
        String username = currentUsername();
        List<Note> notes;
        if(labelId != null && !labelId.isBlank()) {
            try {
                Long lId = Long.parseLong(labelId);
                notes = noteRepository.findByUsernameAndLabelId(username,lId);
            } catch (NumberFormatException e) {
                notes = noteRepository.findActiveByUsername(username);
            }
        } else {
            // Only return active (non-trashed, non-archived) notes
            notes = noteRepository.findActiveByUsername(username);
        }

        return notes.stream().map(NoteMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public Optional<NoteDTO> getNoteById(Long id) {
        String username = currentUsername();
        Optional<Note> noteOpt = noteRepository.findByIdAndUsername(id, username);
        return noteOpt.map(NoteMapper::toDto);
    }

    @Override
    @Transactional
    public NoteDTO createNote(CreateNoteRequest request) {
        String username = currentUsername();

        Note note = new Note();
        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        note.setUsername(username);

        // Upload images to Media Service
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            List<Long> mediaIds = uploadImagesToMediaService(request.getImages());
            note.setMediaIds(new HashSet<>(mediaIds));
        }

        note = noteRepository.save(note);
        return toDto(note);
    }

    // Helper methods for inter-service communication
    private List<Long> uploadImagesToMediaService(List<String> base64Images) {
        try {
            String mediaServiceUrl = "http://media-service/api/media/upload-batch";
            Map<String, Object> requestBody = Map.of("images", base64Images);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(
                    mediaServiceUrl, requestBody, Map.class);

            if (response != null && response.containsKey("ids")) {
                @SuppressWarnings("unchecked")
                List<Integer> ids = (List<Integer>) response.get("ids");
                return ids.stream().map(Long::valueOf).collect(Collectors.toList());
            }
        } catch (Exception e) {
            System.err.println("Error uploading images to media service: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    @Override
    @Transactional
    public NoteDTO updateNote(UpdateNoteRequest request, Long id) {
        String username = currentUsername();
        Optional<Note> noteOpt = noteRepository.findByIdAndUsername(id, username);
        if (noteOpt.isEmpty()) {
            throw new RuntimeException("Note not found or not authorized");
        }

        Note note = noteOpt.get();
        if (request.getTitle() != null) {
            note.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            note.setContent(request.getContent());
        }

        // Handle image updates
        if (request.getImageIds() != null) {
            Set<Long> keepIds = request.getImageIds().stream()
                    .map(Long::parseLong)
                    .collect(Collectors.toSet());

            // Delete removed images from Media Service
            Set<Long> toDelete = new HashSet<>(note.getMediaIds());
            toDelete.removeAll(keepIds);
            for (Long mediaId : toDelete) {
                deleteImageFromMediaService(mediaId);
            }

            note.setMediaIds(keepIds);
        }

        // Upload new images
        if (request.getNewImages() != null && !request.getNewImages().isEmpty()) {
            List<Long> newMediaIds = uploadImagesToMediaService(request.getNewImages());
            note.getMediaIds().addAll(newMediaIds);
        }

        note = noteRepository.save(note);
        return toDto(note);
    }

    private void deleteImageFromMediaService(Long mediaId) {
        try {
            String mediaServiceUrl = "http://media-service/api/media/" + mediaId;
            restTemplate.delete(mediaServiceUrl);
        } catch (Exception e) {
            System.err.println("Error deleting image from media service: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public NoteDTO duplicateNote(Long id) {
        String username = currentUsername();
        Optional<Note> srcOpt = noteRepository.findByIdAndUsername(id, username);
        if (srcOpt.isEmpty()) {
            throw new RuntimeException("Note not found or not authorized");
        }

        Note src = srcOpt.get();
        Note dst = new Note();
        dst.setTitle(src.getTitle());
        dst.setContent(src.getContent());
        dst.setUsername(username);

        // Copy labels
        dst.setLabelIds(new HashSet<>(src.getLabelIds()));

        // Copy images via Media Service
        Set<Long> copiedMediaIds = new HashSet<>();
        for (Long mediaId : src.getMediaIds()) {
            try {
                Long copiedId = copyImageInMediaService(mediaId);
                if (copiedId != null) {
                    copiedMediaIds.add(copiedId);
                }
            } catch (Exception e) {
                // Log and continue
                System.err.println("Failed to copy image: " + mediaId);
            }
        }
        dst.setMediaIds(copiedMediaIds);

        dst = noteRepository.save(dst);
        return NoteMapper.toDto(dst);
    }

    private Long copyImageInMediaService(Long mediaId) {
        try {
            String mediaServiceUrl = "http://media-service/api/media/" + mediaId + "/copy";
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(
                    mediaServiceUrl, Map.of(), Map.class);

            if (response != null && response.containsKey("id")) {
                return Long.valueOf(response.get("id").toString());
            }
        } catch (Exception e) {
            System.err.println("Error copying image in media service: " + e.getMessage());
        }
        return null;
    }

    @Override
    @Transactional
    public void deleteNote(Long id) {
        String username = currentUsername();
        Optional<Note> noteOpt = noteRepository.findByIdAndUsername(id, username);
        if (noteOpt.isEmpty()) {
            throw new RuntimeException("Note not found or not authorized");
        }
        Note note = noteOpt.get();
        // Move to trash instead of permanent delete
        note.setTrashed(true);
        note.setTrashedAt(LocalDateTime.now());
        noteRepository.save(note);
    }

    @Override
    @Transactional
    public NoteDTO setNoteLabels(Long id, LabelIdsRequest request) {
        String username = currentUsername();
        Optional<Note> noteOpt = noteRepository.findByIdAndUsername(id, username);
        if (noteOpt.isEmpty()) {
            throw new RuntimeException("Note not found or not authorized");
        }

        Note note = noteOpt.get();
        Set<Long> labelIds = new HashSet<>();
        if (request != null && request.getLabelIds() != null) {
            labelIds = request.getLabelIds().stream()
                    .map(Long::parseLong)
                    .collect(Collectors.toSet());
        }

        note.setLabelIds(labelIds);
        note = noteRepository.save(note);

        return NoteMapper.toDto(note);
    }

    @Override
    public List<NoteDTO> getArchivedNotes() {
        String username = currentUsername();
        List<Note> archivedNotes = noteRepository.findArchivedByUsername(username);
        return archivedNotes.stream().map(NoteMapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public NoteDTO archiveNote(Long id) {
        String username = currentUsername();
        Optional<Note> noteOpt = noteRepository.findByIdAndUsername(id, username);
        if (noteOpt.isEmpty()) throw new RuntimeException("Note not found or not authorized");
        Note note = noteOpt.get();
        if (note.isTrashed()) {
            throw new RuntimeException("Cannot archive a trashed note");
        }

        note.setArchived(true);
        note.setArchivedAt(LocalDateTime.now());
        note = noteRepository.save(note);

        return NoteMapper.toDto(note);
    }

    @Override
    @Transactional
    public NoteDTO unarchiveNote(Long id) {
        String username = currentUsername();
        Optional<Note> noteOpt = noteRepository.findByIdAndUsername(id, username);
        if (noteOpt.isEmpty()) {
            throw new RuntimeException("Note not found or not authorized");
        }

        Note note = noteOpt.get();
        if (!note.isArchived()) {
            throw new RuntimeException("Note is not archived");
        }

        note.setArchived(false);
        note.setArchivedAt(null);
        note = noteRepository.save(note);

        return NoteMapper.toDto(note);
    }

    @Override
    public List<NoteDTO> getTrashedNotes() {
        String username = currentUsername();
        List<Note> trashedNotes = noteRepository.findTrashedByUsername(username);
        return trashedNotes.stream().map(NoteMapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public NoteDTO restoreNote(Long id) {
        String username = currentUsername();
        Optional<Note> noteOpt = noteRepository.findByIdAndUsername(id, username);
        if (noteOpt.isEmpty()) {
            throw new RuntimeException("Note not found or not authorized");
        }

        Note note = noteOpt.get();
        if (!note.isTrashed()) {
            throw new RuntimeException("Note is not trashed");
        }

        note.setTrashed(false);
        note.setTrashedAt(null);
        // Also unarchive when restoring
        note.setArchived(false);
        note.setArchivedAt(null);
        note = noteRepository.save(note);

        return NoteMapper.toDto(note);
    }

    @Override
    @Transactional
    public void emptyTrash() {
        String username = currentUsername();
        List<Note> trashedNotes = noteRepository.findTrashedByUsername(username);

        // Permanently delete all trashed notes and their images
        for (Note note : trashedNotes) {
            // Delete all images from Media Service
            for (Long mediaId : note.getMediaIds()) {
                deleteImageFromMediaService(mediaId);
            }
            noteRepository.delete(note);
        }
    }

    @Override
    @Transactional
    public void permanentlyDeleteNote(Long id) {
        String username = currentUsername();
        Optional<Note> noteOpt = noteRepository.findByIdAndUsername(id, username);
        if (noteOpt.isEmpty()) {
            throw new RuntimeException("Note not found or not authorized");
        }

        Note note = noteOpt.get();

        // Delete all images from Media Service
        for (Long mediaId : note.getMediaIds()) {
            deleteImageFromMediaService(mediaId);
        }

        noteRepository.delete(note);
    }

    @Override
    @Transactional
    public void removeLabelFromNotes(Long labelId) {
        List<Note> notes = noteRepository.findAll();
        for (Note note : notes) {
            note.getLabelIds().remove(labelId);
        }
        noteRepository.saveAll(notes);
    }
}
