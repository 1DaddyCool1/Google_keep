package com.mykola.keep.labelservice.service;

import com.mykola.keep.labelservice.dto.CreateLabelRequest;
import com.mykola.keep.labelservice.dto.LabelDTO;
import com.mykola.keep.labelservice.dto.UpdateLabelRequest;

import java.util.List;

public interface LabelService {
    List<LabelDTO> getAllLabelsForCurrentUser();
    LabelDTO createLabel(CreateLabelRequest request);
    LabelDTO updateLabel(UpdateLabelRequest request, Long id);
    void deleteLabel(Long id);
}
