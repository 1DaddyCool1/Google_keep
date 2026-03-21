package com.mykola.keep.mediaservice.controller;

import com.mykola.keep.mediaservice.dto.UploadBatchRequest;
import com.mykola.keep.mediaservice.dto.UploadRequest;
import com.mykola.keep.mediaservice.entity.Media;
import com.mykola.keep.mediaservice.service.impl.MediaServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Tag(name = "Media API", description = "Media-related endpoints")
@RestController
@RequestMapping("/api/media")
public class MediaController {

    private final MediaServiceImpl mediaService;
    public MediaController(MediaServiceImpl mediaService) {this.mediaService = mediaService;}

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? String.valueOf(auth.getPrincipal()) : null;
    }

    @Operation(summary = "Upload an image", description = "Upload an image to the server")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Image uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestBody UploadRequest request) {
        try {
            String username = currentUsername();
            Media media = mediaService.uploadImage(username, request.getImageData());
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", media.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Upload multiple images", description = "Upload multiple images to the server")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Images uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("/upload-batch")
    public ResponseEntity<?> uploadBatch(@RequestBody UploadBatchRequest request) {
        try {
            String username = currentUsername();
            List<Long> ids = request.getImages().stream()
                    .map(img -> {
                        try {
                            return mediaService.uploadImage(username, img).getId();
                        } catch (IOException e) {
                            return null;
                        }
                    })
                    .filter(id -> id != null)
                    .toList();
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("ids", ids));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Get an image", description = "Get an image by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Image not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getImage(
            @Parameter(description = "Image ID", required = true)
            @PathVariable Long id
    ) {
        try {
            String username = currentUsername();
            byte[] imageBytes = mediaService.getImageData(id, username);
            if (imageBytes == null) {
                return ResponseEntity.notFound().build();
            }
            String contentType = mediaService.getContentType(id, username);
            return ResponseEntity.ok()
                    .header("Content-Type", contentType != null ? contentType : "application/octet-stream")
                    .body(imageBytes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Copy an image", description = "Copy an image to the server")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Image copied successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("/{id}/copy")
    public ResponseEntity<?> copyImage(
            @Parameter(description = "Image ID", required = true)
            @PathVariable Long id
    ) {
        try {
            String username = currentUsername();
            Media media = mediaService.copyImage(id, username);
            return ResponseEntity.ok(Map.of("id", media.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Delete an image", description = "Delete an image by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Image deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Image not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImage(
            @Parameter(description = "Image ID", required = true)
            @PathVariable Long id
    ) {
        String username = currentUsername();
        mediaService.deleteImage(id, username);
        return ResponseEntity.noContent().build();
    }
}
