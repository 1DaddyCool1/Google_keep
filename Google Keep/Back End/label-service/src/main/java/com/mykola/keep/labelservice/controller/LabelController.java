package com.mykola.keep.labelservice.controller;

import com.mykola.keep.labelservice.dto.CreateLabelRequest;
import com.mykola.keep.labelservice.dto.LabelDTO;
import com.mykola.keep.labelservice.dto.UpdateLabelRequest;
import com.mykola.keep.labelservice.service.impl.LabelServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Label API", description = "Label-related endpoints")
@RestController
@RequestMapping("/api/labels")
public class LabelController {

    private final LabelServiceImpl labelService;
    public LabelController(LabelServiceImpl labelService) {this.labelService = labelService;}

    @Operation(summary = "Get all labels for the current user", description = "Retrieve all labels associated with the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Labels retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public ResponseEntity<List<LabelDTO>> getAllLabels() {
        return ResponseEntity.ok().body(labelService.getAllLabelsForCurrentUser());
    }

    @Operation(summary = "Get a specific label by ID", description = "Retrieve a specific label by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Label retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Label not found")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<LabelDTO> createLabel(@RequestBody CreateLabelRequest request) {
        return ResponseEntity.ok().body(labelService.createLabel(request));
    }

    @Operation(summary = "Update a label", description = "Update an existing label")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Label updated successfully"),
            @ApiResponse(responseCode = "404", description = "Label not found")
    })
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<LabelDTO> updateLabel(
            @RequestBody UpdateLabelRequest request,
            @Parameter(description = "Label ID", required = true)
            @PathVariable Long id
    ) {
        return ResponseEntity.ok().body(labelService.updateLabel(request, id));
    }

    @Operation(summary = "Delete a label", description = "Delete an existing label")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Label deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Label not found")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteLabel(
            @Parameter(description = "Label ID", required = true)
            @PathVariable Long id
    ) {
        labelService.deleteLabel(id);
        return ResponseEntity.noContent().build();
    }
}
