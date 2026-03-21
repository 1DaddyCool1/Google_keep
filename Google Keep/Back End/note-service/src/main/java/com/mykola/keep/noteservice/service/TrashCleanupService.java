package com.mykola.keep.noteservice.service;

import com.mykola.keep.noteservice.entity.Note;
import com.mykola.keep.noteservice.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TrashCleanupService {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Runs daily at 2:00 AM to delete notes that have been in trash for more than 7 days
     */
    @Scheduled(cron = "0 0 2 * * ?") // Every day at 2:00 AM
    @Transactional
    public void cleanupOldTrashedNotes() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);
        List<Note> notesToDelete = noteRepository.findTrashedNotesBefore(cutoffDate);

        System.out.println("Trash cleanup: Found " + notesToDelete.size() + " notes to permanently delete");

        for (Note note : notesToDelete) {
            try {
                // Delete all images from Media Service
                for (Long mediaId : note.getMediaIds()) {
                    deleteImageFromMediaService(mediaId);
                }

                // Permanently delete the note
                noteRepository.delete(note);
                System.out.println("Permanently deleted note ID: " + note.getId() + " (trashed on: " + note.getTrashedAt() + ")");
            } catch (Exception e) {
                System.err.println("Error deleting note ID " + note.getId() + ": " + e.getMessage());
            }
        }

        System.out.println("Trash cleanup completed. Deleted " + notesToDelete.size() + " notes.");
    }

    private void deleteImageFromMediaService(Long mediaId) {
        try {
            String mediaServiceUrl = "http://media-service/api/media/" + mediaId;
            restTemplate.delete(mediaServiceUrl);
        } catch (Exception e) {
            System.err.println("Error deleting image from media service: " + e.getMessage());
        }
    }
}

