package com.mykola.keep.noteservice.controller;

import com.mykola.keep.noteservice.dto.CreateNoteRequest;
import com.mykola.keep.noteservice.dto.LabelIdsRequest;
import com.mykola.keep.noteservice.dto.UpdateNoteRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NotesController Request DTOs Tests")
public class NotesControllerRequestDTOsTest {

    @Test
    @DisplayName("CreateNoteRequest - Should set and get all properties")
    void createNoteRequestShouldSetAndGetAllProperties() {
        CreateNoteRequest request = new CreateNoteRequest();

        request.setTitle("Test Title");
        request.setContent("Test Content");
        List<String> images = Arrays.asList("image1", "image2");
        request.setImages(images);

        assertEquals("Test Title", request.getTitle());
        assertEquals("Test Content", request.getContent());
        assertEquals(images, request.getImages());
        assertEquals(2, request.getImages().size());
    }

    @Test
    @DisplayName("CreateNoteRequest - Should handle null values")
    void createNoteRequestShouldHandleNullValues() {
        CreateNoteRequest request = new CreateNoteRequest();

        request.setTitle(null);
        request.setContent(null);
        request.setImages(null);

        assertNull(request.getTitle());
        assertNull(request.getContent());
        assertNull(request.getImages());
    }

    @Test
    @DisplayName("CreateNoteRequest - Should handle empty images list")
    void createNoteRequestShouldHandleEmptyImages() {
        CreateNoteRequest request = new CreateNoteRequest();

        request.setImages(Arrays.asList());

        assertNotNull(request.getImages());
        assertTrue(request.getImages().isEmpty());
    }

    @Test
    @DisplayName("UpdateNoteRequest - Should set and get all properties")
    void updateNoteRequestShouldSetAndGetAllProperties() {
        UpdateNoteRequest request = new UpdateNoteRequest();

        request.setTitle("Updated Title");
        request.setContent("Updated Content");

        List<String> imageIds = Arrays.asList("1", "2", "3");
        request.setImageIds(imageIds);

        List<String> newImages = Arrays.asList("newImage1", "newImage2");
        request.setNewImages(newImages);

        assertEquals("Updated Title", request.getTitle());
        assertEquals("Updated Content", request.getContent());
        assertEquals(imageIds, request.getImageIds());
        assertEquals(3, request.getImageIds().size());
        assertEquals(newImages, request.getNewImages());
        assertEquals(2, request.getNewImages().size());
    }

    @Test
    @DisplayName("UpdateNoteRequest - Should handle null values")
    void updateNoteRequestShouldHandleNullValues() {
        UpdateNoteRequest request = new UpdateNoteRequest();

        request.setTitle(null);
        request.setContent(null);
        request.setImageIds(null);
        request.setNewImages(null);

        assertNull(request.getTitle());
        assertNull(request.getContent());
        assertNull(request.getImageIds());
        assertNull(request.getNewImages());
    }

    @Test
    @DisplayName("UpdateNoteRequest - Should handle partial updates")
    void updateNoteRequestShouldHandlePartialUpdates() {
        UpdateNoteRequest request = new UpdateNoteRequest();

        // Only update title, leave others null
        request.setTitle("Only Title Update");

        assertEquals("Only Title Update", request.getTitle());
        assertNull(request.getContent());
        assertNull(request.getImageIds());
        assertNull(request.getNewImages());
    }

    @Test
    @DisplayName("UpdateNoteRequest - Should handle empty collections")
    void updateNoteRequestShouldHandleEmptyCollections() {
        UpdateNoteRequest request = new UpdateNoteRequest();

        request.setImageIds(Arrays.asList());
        request.setNewImages(Arrays.asList());

        assertNotNull(request.getImageIds());
        assertNotNull(request.getNewImages());
        assertTrue(request.getImageIds().isEmpty());
        assertTrue(request.getNewImages().isEmpty());
    }

    @Test
    @DisplayName("LabelIdsRequest - Should set and get label IDs")
    void labelIdsRequestShouldSetAndGetLabelIds() {
        LabelIdsRequest request = new LabelIdsRequest();

        List<String> labelIds = Arrays.asList("1", "2", "3", "4", "5");
        request.setLabelIds(labelIds);

        assertEquals(labelIds, request.getLabelIds());
        assertEquals(5, request.getLabelIds().size());
    }

    @Test
    @DisplayName("LabelIdsRequest - Should handle null label IDs")
    void labelIdsRequestShouldHandleNullLabelIds() {
        LabelIdsRequest request = new LabelIdsRequest();

        request.setLabelIds(null);

        assertNull(request.getLabelIds());
    }

    @Test
    @DisplayName("LabelIdsRequest - Should handle empty label IDs")
    void labelIdsRequestShouldHandleEmptyLabelIds() {
        LabelIdsRequest request = new LabelIdsRequest();

        request.setLabelIds(Arrays.asList());

        assertNotNull(request.getLabelIds());
        assertTrue(request.getLabelIds().isEmpty());
    }

    @Test
    @DisplayName("LabelIdsRequest - Should handle large number of labels")
    void labelIdsRequestShouldHandleLargeNumberOfLabels() {
        LabelIdsRequest request = new LabelIdsRequest();

        List<String> labelIds = Arrays.asList(
            "1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
            "11", "12", "13", "14", "15", "16", "17", "18", "19", "20"
        );
        request.setLabelIds(labelIds);

        assertEquals(20, request.getLabelIds().size());
    }

    @Test
    @DisplayName("CreateNoteRequest - Should handle large content")
    void createNoteRequestShouldHandleLargeContent() {
        CreateNoteRequest request = new CreateNoteRequest();

        String largeContent = "a".repeat(10000);
        request.setContent(largeContent);

        assertEquals(10000, request.getContent().length());
    }

    @Test
    @DisplayName("CreateNoteRequest - Should handle HTML content")
    void createNoteRequestShouldHandleHtmlContent() {
        CreateNoteRequest request = new CreateNoteRequest();

        String htmlContent = "<div><p>Hello <strong>World</strong></p><ul><li>Item 1</li></ul></div>";
        request.setContent(htmlContent);

        assertEquals(htmlContent, request.getContent());
    }

    @Test
    @DisplayName("UpdateNoteRequest - Should handle multiple new images")
    void updateNoteRequestShouldHandleMultipleNewImages() {
        UpdateNoteRequest request = new UpdateNoteRequest();

        List<String> newImages = Arrays.asList(
            "base64image1",
            "base64image2",
            "base64image3",
            "base64image4",
            "base64image5"
        );
        request.setNewImages(newImages);

        assertEquals(5, request.getNewImages().size());
    }

    @Test
    @DisplayName("UpdateNoteRequest - Should keep existing images and add new ones")
    void updateNoteRequestShouldKeepExistingAndAddNew() {
        UpdateNoteRequest request = new UpdateNoteRequest();

        List<String> existingImageIds = Arrays.asList("1", "2", "3");
        List<String> newImages = Arrays.asList("newImage1", "newImage2");

        request.setImageIds(existingImageIds);
        request.setNewImages(newImages);

        assertEquals(3, request.getImageIds().size());
        assertEquals(2, request.getNewImages().size());
    }
}

