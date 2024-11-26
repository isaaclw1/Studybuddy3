package com.example.studybuddy3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.studybuddy3.datatype.User;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class UserUnitTest {

    private User user;

    @Before
    public void setUp() {
        user = new User("testUserId", "test@example.com", "hashedPassword123");
    }

    @Test
    public void testUserCreation() {
        assertEquals("testUserId", user.getUserId());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("hashedPassword123", user.getPasswordHash());
        assertNotNull(user.getEnrolledCourses());
        assertTrue(user.getEnrolledCourses().isEmpty());
    }

    @Test
    public void testSettersAndGetters() {
        user.setUserId("newUserId");
        user.setEmail("new@example.com");
        user.setPasswordHash("newHashedPassword");

        assertEquals("newUserId", user.getUserId());
        assertEquals("new@example.com", user.getEmail());
        assertEquals("newHashedPassword", user.getPasswordHash());
    }

    @Test
    public void testCourseEnrollment() {
        List<String> courses = Arrays.asList("Math101", "CS201", "History202");
        user.setEnrolledCourses(courses);

        assertNotNull(user.getEnrolledCourses());
        assertEquals(3, user.getEnrolledCourses().size());
        assertTrue(user.getEnrolledCourses().contains("Math101"));
        assertTrue(user.getEnrolledCourses().contains("CS201"));
        assertTrue(user.getEnrolledCourses().contains("History202"));
    }

    @Test
    public void testAddCourse() {
        user.getEnrolledCourses().add("Physics301");

        assertEquals(1, user.getEnrolledCourses().size());
        assertTrue(user.getEnrolledCourses().contains("Physics301"));
    }

    @Test
    public void testInvalidEmail() {
        user.setEmail("invalid-email");
        assertEquals("invalid-email", user.getEmail()); // Application-level validation not included in this test.
    }
}
