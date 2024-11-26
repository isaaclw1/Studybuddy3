package com.example.studybuddy3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.studybuddy3.datatype.StudySession;
import com.example.studybuddy3.datatype.Resource;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class StudySessionUnitTest {
    private StudySession studySession;
    private final String TEST_SESSION_ID = "test-session-123";
    private final String TEST_TITLE = "Test Study Session";
    private final String TEST_LOCATION = "Library Room 101";
    private final String TEST_GROUP_ID = "test-group-456";
    private Calendar testCalendar;

    @Before
    public void setUp() {
        testCalendar = Calendar.getInstance();
        testCalendar.set(2024, Calendar.NOVEMBER, 15, 14, 30);

        long testDate = testCalendar.getTimeInMillis();
        testCalendar.set(Calendar.HOUR_OF_DAY, 14);
        long testStartTime = testCalendar.getTimeInMillis();
        testCalendar.set(Calendar.HOUR_OF_DAY, 16);
        long testEndTime = testCalendar.getTimeInMillis();

        studySession = new StudySession(
                TEST_SESSION_ID,
                TEST_TITLE,
                testDate,
                testStartTime,
                testEndTime,
                TEST_LOCATION,
                TEST_GROUP_ID
        );
    }

    @Test
    public void testStudySessionCreation() {
        assertNotNull(studySession);
        assertEquals(TEST_SESSION_ID, studySession.getSessionId());
        assertEquals(TEST_TITLE, studySession.getTitle());
        assertEquals(TEST_LOCATION, studySession.getLocation());
        assertEquals(TEST_GROUP_ID, studySession.getGroupId());
        assertNotNull(studySession.getAttendeeIds());
        assertTrue(studySession.getAttendeeIds().isEmpty());
        assertNotNull(studySession.getResources());
        assertTrue(studySession.getResources().isEmpty());
    }

    @Test
    public void testEmptyConstructor() {
        StudySession emptySession = new StudySession();
        assertNotNull(emptySession);
        assertNotNull(emptySession.getAttendeeIds());
        assertNotNull(emptySession.getResources());
        assertTrue(emptySession.getAttendeeIds().isEmpty());
        assertTrue(emptySession.getResources().isEmpty());
    }

    @Test
    public void testSettersAndGetters() {
        String newSessionId = "new-session-789";
        String newTitle = "Updated Study Session";
        String newLocation = "Online Zoom Meeting";
        String newGroupId = "new-group-789";

        studySession.setSessionId(newSessionId);
        studySession.setTitle(newTitle);
        studySession.setLocation(newLocation);
        studySession.setGroupId(newGroupId);

        assertEquals(newSessionId, studySession.getSessionId());
        assertEquals(newTitle, studySession.getTitle());
        assertEquals(newLocation, studySession.getLocation());
        assertEquals(newGroupId, studySession.getGroupId());
    }

    @Test
    public void testAttendeeManagement() {
        List<String> attendees = Arrays.asList("user1", "user2", "user3");
        studySession.setAttendeeIds(attendees);

        assertNotNull(studySession.getAttendeeIds());
        assertEquals(3, studySession.getAttendeeIds().size());
        assertTrue(studySession.getAttendeeIds().contains("user1"));
        assertTrue(studySession.getAttendeeIds().contains("user2"));
        assertTrue(studySession.getAttendeeIds().contains("user3"));
    }

    @Test
    public void testResourceManagement() {
        Resource resource1 = mock(Resource.class);
        Resource resource2 = mock(Resource.class);
        List<Resource> resources = Arrays.asList(resource1, resource2);

        studySession.setResources(resources);

        assertNotNull(studySession.getResources());
        assertEquals(2, studySession.getResources().size());
        assertTrue(studySession.getResources().contains(resource1));
        assertTrue(studySession.getResources().contains(resource2));
    }

    @Test
    public void testTimeManagement() {
        Calendar newTime = Calendar.getInstance();
        newTime.set(2024, Calendar.NOVEMBER, 16, 10, 0); // Nov 16, 2024, 10:00 AM

        long newDate = newTime.getTimeInMillis();
        studySession.setDate(newDate);
        assertEquals(newDate, studySession.getDate());

        newTime.set(Calendar.HOUR_OF_DAY, 10);
        long newStartTime = newTime.getTimeInMillis();
        studySession.setStartTime(newStartTime);
        assertEquals(newStartTime, studySession.getStartTime());

        newTime.set(Calendar.HOUR_OF_DAY, 12);
        long newEndTime = newTime.getTimeInMillis();
        studySession.setEndTime(newEndTime);
        assertEquals(newEndTime, studySession.getEndTime());
    }

    @Test
    public void testValidTimeRange() {
        long startTime = studySession.getStartTime();
        long endTime = studySession.getEndTime();
        assertTrue("End time should be after start time", endTime > startTime);
    }
}