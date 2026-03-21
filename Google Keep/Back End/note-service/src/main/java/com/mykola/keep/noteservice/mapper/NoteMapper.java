package com.mykola.keep.noteservice.mapper;

import com.mykola.keep.noteservice.dto.NoteDTO;
import com.mykola.keep.noteservice.entity.Note;

import java.util.ArrayList;

public class NoteMapper {
    public static NoteDTO toDto(Note note) {
        return new NoteDTO(
                String.valueOf(note.getId()),
                note.getTitle(),
                note.getContent(),
                new ArrayList<>(note.getMediaIds()),
                new ArrayList<>(note.getLabelIds()),
                note.getCreatedAt(),
                note.getUpdatedAt(),
                note.isTrashed(),
                note.getTrashedAt(),
                note.isArchived(),
                note.getArchivedAt()
        );
    }
}
