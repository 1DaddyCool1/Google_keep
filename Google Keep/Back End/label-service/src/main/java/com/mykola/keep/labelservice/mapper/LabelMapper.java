package com.mykola.keep.labelservice.mapper;

import com.mykola.keep.labelservice.dto.LabelDTO;
import com.mykola.keep.labelservice.entity.Label;

public class LabelMapper {
    public static LabelDTO toDto(Label label) {
        return new LabelDTO(
                String.valueOf(label.getId()),
                label.getName(),
                label.getCreatedAt()
        );
    }
}
