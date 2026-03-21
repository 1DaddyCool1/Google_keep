package com.mykola.keep.noteservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateNoteRequest {
    private String title;
    private String content;
    private List<String> imageIds;
    private List<String> newImages;
}
