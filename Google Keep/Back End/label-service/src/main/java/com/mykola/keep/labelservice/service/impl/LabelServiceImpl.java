package com.mykola.keep.labelservice.service.impl;

import com.mykola.keep.labelservice.dto.CreateLabelRequest;
import com.mykola.keep.labelservice.dto.LabelDTO;
import com.mykola.keep.labelservice.dto.UpdateLabelRequest;
import com.mykola.keep.labelservice.entity.Label;
import com.mykola.keep.labelservice.mapper.LabelMapper;
import com.mykola.keep.labelservice.repository.LabelRepository;
import com.mykola.keep.labelservice.service.LabelService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LabelServiceImpl implements LabelService {

    private final LabelRepository labelRepository;
    private final RestTemplate restTemplate;
    public LabelServiceImpl(LabelRepository labelRepository, RestTemplate restTemplate) {
        this.labelRepository = labelRepository;
        this.restTemplate = restTemplate;
    }

    public String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? String.valueOf(auth.getPrincipal()) : null;
    }

    @Override
    public List<LabelDTO> getAllLabelsForCurrentUser() {
        String username = currentUsername();
        return labelRepository.findByUsernameOrderByCreatedAtDesc(username).stream()
                .map(LabelMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public LabelDTO createLabel(CreateLabelRequest request) {
        String username = currentUsername();
        if(request == null || request.getName() == null || request.getName().isBlank()) {
            throw new RuntimeException("Label name cannot be null or empty");
        }
        if(labelRepository.existsByUsernameAndNameIgnoreCase(username, request.getName().trim())) {
            throw new RuntimeException("Label already exists");
        }
        Label label = new Label();
        label.setName(request.getName().trim());
        label.setUsername(username);
        label = labelRepository.save(label);
        return LabelMapper.toDto(label);
    }

    @Override
    public LabelDTO updateLabel(UpdateLabelRequest request, Long id) {
        String username = currentUsername();
        Label label = labelRepository.findByIdAndUsername(id, username).orElse(null);
        if(label == null) {
            throw new RuntimeException("Label not found or not authorized");
        }
        String newName = request.getName();
        if(newName == null || newName.isBlank()) {
            throw new RuntimeException("Label name cannot be null or empty");
        }
        newName = newName.trim();
        if(!label.getName().equalsIgnoreCase(newName) && labelRepository.existsByUsernameAndNameIgnoreCase(username, newName)) {
            throw new RuntimeException("Label already exists");
        }
        label.setName(newName);
        label = labelRepository.save(label);
        return LabelMapper.toDto(label);
    }

    @Override
    @Transactional
    public void deleteLabel(Long id) {
        String username = currentUsername();
        Label label = labelRepository.findByIdAndUsername(id, username).orElse(null);
        if(label == null) {
            throw new RuntimeException("Label not found or not authorized");
        }

        // Call Notes Service to remove label associations
        try {
            String notesServiceUrl = "http://notes-service/api/notes/internal/remove-label/" + id;
            restTemplate.delete(notesServiceUrl);
        } catch (Exception e) {
            // Log error but continue with deletion
            System.err.println("Error calling notes service: " + e.getMessage());
        }

        // Delete the label
        labelRepository.delete(label);
    }
}
