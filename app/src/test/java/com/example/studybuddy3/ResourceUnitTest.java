package com.example.studybuddy3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import com.example.studybuddy3.datatype.Resource;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.gms.tasks.Task;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

public class ResourceUnitTest {
    private Resource resource;
    private final String TEST_RESOURCE_ID = "test-resource-123";
    private final String TEST_NAME = "Sample Lecture Notes";
    private final String TEST_DESCRIPTION = "Notes from Week 1";
    private final String TEST_CATEGORY = "Lecture Notes";
    private final String TEST_FILE_URL = "https://example.com/files/notes.pdf";
    private final String TEST_UPLOADER_ID = "user-123";
    private final String TEST_SESSION_ID = "session-456";
    private final long TEST_UPLOAD_TIME = System.currentTimeMillis();

    @Mock
    private FirebaseDatabase mockFirebaseDatabase;
    @Mock
    private DatabaseReference mockDatabaseReference;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        resource = new Resource(
                TEST_RESOURCE_ID,
                TEST_NAME,
                TEST_DESCRIPTION,
                TEST_CATEGORY,
                TEST_FILE_URL,
                TEST_UPLOADER_ID,
                TEST_UPLOAD_TIME,
                TEST_SESSION_ID
        );
    }

    @Test
    public void testResourceCreation() {
        assertNotNull(resource);
        assertEquals(TEST_RESOURCE_ID, resource.getResourceId());
        assertEquals(TEST_NAME, resource.getName());
        assertEquals(TEST_DESCRIPTION, resource.getDescription());
        assertEquals(TEST_CATEGORY, resource.getCategory());
        assertEquals(TEST_FILE_URL, resource.getFileUrl());
        assertEquals(TEST_UPLOADER_ID, resource.getUploaderId());
        assertEquals(TEST_SESSION_ID, resource.getSessionId());
    }

    @Test
    public void testEmptyConstructor() {
        Resource emptyResource = new Resource();
        assertNotNull(emptyResource);
    }

    @Test
    public void testSettersAndGetters() {
        String newResourceId = "new-resource-789";
        String newName = "Updated Notes";
        String newDescription = "Updated description";
        String newCategory = "Project Materials";
        String newFileUrl = "https://example.com/files/updated.pdf";
        String newUploaderId = "user-789";
        String newSessionId = "session-789";

        resource.setResourceId(newResourceId);
        resource.setName(newName);
        resource.setDescription(newDescription);
        resource.setCategory(newCategory);
        resource.setFileUrl(newFileUrl);
        resource.setUploaderId(newUploaderId);
        resource.setSessionId(newSessionId);

        assertEquals(newResourceId, resource.getResourceId());
        assertEquals(newName, resource.getName());
        assertEquals(newDescription, resource.getDescription());
        assertEquals(newCategory, resource.getCategory());
        assertEquals(newFileUrl, resource.getFileUrl());
        assertEquals(newUploaderId, resource.getUploaderId());
        assertEquals(newSessionId, resource.getSessionId());
    }

    @Test
    public void testResourceFilter() {
        List<Resource> resources = new ArrayList<>();
        resources.add(resource);
        resources.add(new Resource("id2", "Math Notes", "Calculus notes",
                "Study Materials", "url2", "user2", TEST_UPLOAD_TIME, "session2"));
        resources.add(new Resource("id3", "Physics Lab", "Lab report",
                "Lab Materials", "url3", "user3", TEST_UPLOAD_TIME, "session3"));

        // Test filtering by name
        List<Resource> filteredByName = filterResources(resources, "lecture");
        assertEquals(1, filteredByName.size());
        assertEquals(TEST_NAME, filteredByName.get(0).getName());

        List<Resource> filteredByCategory = filterResources(resources, "lab");
        assertEquals(1, filteredByCategory.size());
        assertEquals("Physics Lab", filteredByCategory.get(0).getName());

        List<Resource> filteredByDescription = filterResources(resources, "calculus");
        assertEquals(1, filteredByDescription.size());
        assertEquals("Math Notes", filteredByDescription.get(0).getName());
    }

    private List<Resource> filterResources(List<Resource> resources, String query) {
        List<Resource> filteredList = new ArrayList<>();
        String lowerQuery = query.toLowerCase().trim();

        for (Resource resource : resources) {
            if (resource.getName().toLowerCase().contains(lowerQuery) ||
                    resource.getDescription().toLowerCase().contains(lowerQuery) ||
                    resource.getCategory().toLowerCase().contains(lowerQuery)) {
                filteredList.add(resource);
            }
        }
        return filteredList;
    }


    @Test
    public void testResourceUpload() {
        // Mock Firebase behavior
        when(mockFirebaseDatabase.getReference()).thenReturn(mockDatabaseReference);
        when(mockDatabaseReference.child("resources")).thenReturn(mockDatabaseReference);
        when(mockDatabaseReference.child(TEST_SESSION_ID)).thenReturn(mockDatabaseReference);
        when(mockDatabaseReference.child(TEST_RESOURCE_ID)).thenReturn(mockDatabaseReference);

        DatabaseReference mockPush = mock(DatabaseReference.class);
        when(mockDatabaseReference.push()).thenReturn(mockPush);
        when(mockPush.getKey()).thenReturn(TEST_RESOURCE_ID);

        Task<Void> mockTask = mock(Task.class);
        when(mockDatabaseReference.setValue(resource)).thenReturn(mockTask);
        when(mockTask.isSuccessful()).thenReturn(true);


        mockDatabaseReference.child("resources")
                .child(TEST_SESSION_ID)
                .child(TEST_RESOURCE_ID)
                .setValue(resource);

        verify(mockDatabaseReference).setValue(resource);
    }

    @Test
    public void testResourceViewing() {
        // Mock data snapshot
        DataSnapshot mockSnapshot = mock(DataSnapshot.class);
        when(mockSnapshot.getValue(Resource.class)).thenReturn(resource);

        // Verify resource URL retrieval
        Resource retrievedResource = mockSnapshot.getValue(Resource.class);
        assertNotNull(retrievedResource);
        assertEquals(TEST_FILE_URL, retrievedResource.getFileUrl());
    }
}