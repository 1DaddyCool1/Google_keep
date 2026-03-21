package com.mykola.keep.noteservice.repository;

import com.mykola.keep.noteservice.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByUsername(String username);
    Optional<Note> findByIdAndUsername(Long id, String username);

    // Find active (non-trashed) notes
    List<Note> findByUsernameAndIsTrashed(String username, boolean isTrashed);

    // Find trashed notes for a user
    @Query("SELECT n FROM Note n WHERE n.username = :username AND n.isTrashed = true")
    List<Note> findTrashedByUsername(@Param("username") String username);

    // Find active notes by label
    @Query("SELECT DISTINCT n FROM Note n JOIN n.labelIds l WHERE n.username = :username AND l = :labelId AND n.isTrashed = false AND n.isArchived = false")
    List<Note> findByUsernameAndLabelId(@Param("username") String username, @Param("labelId") Long labelId);

    // Find archived notes for a user
    @Query("SELECT n FROM Note n WHERE n.username = :username AND n.isArchived = true AND n.isTrashed = false")
    List<Note> findArchivedByUsername(@Param("username") String username);

    // Find active (non-trashed, non-archived) notes
    @Query("SELECT n FROM Note n WHERE n.username = :username AND n.isTrashed = false AND n.isArchived = false")
    List<Note> findActiveByUsername(@Param("username") String username);

    // Find notes trashed before a certain date
    @Query("SELECT n FROM Note n WHERE n.isTrashed = true AND n.trashedAt < :cutoffDate")
    List<Note> findTrashedNotesBefore(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Modifying
    @Query("UPDATE Note n SET n.labelIds = (SELECT l FROM n.labelIds l WHERE l <> :labelId) WHERE :labelId MEMBER OF n.labelIds")
    void removeLabelFromAllNotes(@Param("labelId") Long labelId);
}
