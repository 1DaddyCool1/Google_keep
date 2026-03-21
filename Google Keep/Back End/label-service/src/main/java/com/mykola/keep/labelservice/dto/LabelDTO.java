package com.mykola.keep.labelservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LabelDTO {
    private String id;
    private String name;
    private LocalDateTime createdAt;
}
