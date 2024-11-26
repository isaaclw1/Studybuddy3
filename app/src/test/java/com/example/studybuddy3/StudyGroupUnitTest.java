package com.example.studybuddy3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.studybuddy3.datatype.StudyGroup;
import com.example.studybuddy3.datatype.StudySession;
import com.example.studybuddy3.datatype.ChatMessage;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StudyGroupUnitTest {
    private StudyGroup studyGroup;
    private static final String TEST_GROUP_ID = "test-group-123";
    private static final String TEST_COURSE_ID = "COMP-101";
    private static final String TEST_GROUP_NAME = "Test Study Group";

    @Mock
    private StudySession mockStudySession;
    @Mock
    private ChatMessage mockChatMessage;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        studyGroup = new StudyGroup();
        studyGroup.setGroupId(TEST_GROUP_ID);
        studyGroup.setCourseId(TEST_COURSE_ID);
        studyGroup.setGroupName(TEST_GROUP_NAME);
        studyGroup.setCreatedAt(System.currentTimeMillis());
    }

    @Test
    public void testEmptyConstructor() {
        StudyGroup newGroup = new StudyGroup();
        assertNotNull(newGroup);
        assertNotNull(newGroup.getMemberIds());
        assertNotNull(newGroup.getSessions());
        assertNotNull(newGroup.getMessages());
        assertTrue(newGroup.getMemberIds().isEmpty());
        assertTrue(newGroup.getSessions().isEmpty());
        assertTrue(newGroup.getMessages().isEmpty());
    }

    @Test
    public void testBasicProperties() {
        assertEquals(TEST_GROUP_ID, studyGroup.getGroupId());
        assertEquals(TEST_COURSE_ID, studyGroup.getCourseId());
        assertEquals(TEST_GROUP_NAME, studyGroup.getGroupName());
        assertTrue(studyGroup.getCreatedAt() > 0);
    }

    @Test
    public void testMemberManagement() {
        List<String> members = Arrays.asList("user1", "user2", "user3");
        studyGroup.setMemberIds(members);

        assertNotNull(studyGroup.getMemberIds());
        assertEquals(3, studyGroup.getMemberIds().size());
        assertTrue(studyGroup.getMemberIds().contains("user1"));
        assertTrue(studyGroup.getMemberIds().contains("user2"));
        assertTrue(studyGroup.getMemberIds().contains("user3"));
    }

    @Test
    public void testSessionManagement() {
        StudySession session1 = mock(StudySession.class);
        StudySession session2 = mock(StudySession.class);
        List<StudySession> sessions = Arrays.asList(session1, session2);

        studyGroup.setSessions(sessions);

        assertNotNull(studyGroup.getSessions());
        assertEquals(2, studyGroup.getSessions().size());
        assertTrue(studyGroup.getSessions().contains(session1));
        assertTrue(studyGroup.getSessions().contains(session2));
    }

    @Test
    public void testChatMessageManagement() {
        String messageId = "msg-123";
        String senderId = "user1";
        String receiverId = "user2";
        String senderEmail = "test@example.com";
        String content = "Test message";
        long timestamp = System.currentTimeMillis();

        ChatMessage message = new ChatMessage(messageId, senderId, receiverId,
                senderEmail, content, timestamp);
        List<ChatMessage> messages = Arrays.asList(message);

        studyGroup.setMessages(messages);

        assertNotNull(studyGroup.getMessages());
        assertEquals(1, studyGroup.getMessages().size());
        ChatMessage storedMessage = studyGroup.getMessages().get(0);
        assertEquals(messageId, storedMessage.getMessageId());
        assertEquals(senderId, storedMessage.getSenderId());
        assertEquals(receiverId, storedMessage.getReceiverId());
        assertEquals(senderEmail, storedMessage.getSenderEmail());
        assertEquals(content, storedMessage.getContent());
        assertEquals(timestamp, storedMessage.getTimestamp());
    }

    @Test
    public void testSettersAndGetters() {
        String newGroupId = "new-group-789";
        String newCourseId = "MATH-201";
        String newGroupName = "New Study Group";
        long newCreatedAt = System.currentTimeMillis();

        studyGroup.setGroupId(newGroupId);
        studyGroup.setCourseId(newCourseId);
        studyGroup.setGroupName(newGroupName);
        studyGroup.setCreatedAt(newCreatedAt);

        assertEquals(newGroupId, studyGroup.getGroupId());
        assertEquals(newCourseId, studyGroup.getCourseId());
        assertEquals(newGroupName, studyGroup.getGroupName());
        assertEquals(newCreatedAt, studyGroup.getCreatedAt());
    }

    @Test
    public void testGroupNameValidation() {
        String validName = "TestGroup-123";
        String validNameWithUnderscore = "Test_Group_123";

        studyGroup.setGroupName(validName);
        assertEquals(validName, studyGroup.getGroupName());

        studyGroup.setGroupName(validNameWithUnderscore);
        assertEquals(validNameWithUnderscore, studyGroup.getGroupName());
    }

    @Test
    public void testChatMessageCreation() {
        ChatMessage message = new ChatMessage();
        assertNotNull(message);

        String messageId = "msg-123";
        String senderId = "user1";
        String receiverId = "user2";
        String senderEmail = "test@example.com";
        String content = "Hello, world!";
        long timestamp = System.currentTimeMillis();

        message.setMessageId(messageId);
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setSenderEmail(senderEmail);
        message.setContent(content);
        message.setTimestamp(timestamp);

        assertEquals(messageId, message.getMessageId());
        assertEquals(senderId, message.getSenderId());
        assertEquals(receiverId, message.getReceiverId());
        assertEquals(senderEmail, message.getSenderEmail());
        assertEquals(content, message.getContent());
        assertEquals(timestamp, message.getTimestamp());
    }

    @Test
    public void testListManipulation() {
        // Test adding members
        List<String> initialMembers = new ArrayList<>(Arrays.asList("user1", "user2"));
        studyGroup.setMemberIds(initialMembers);
        studyGroup.getMemberIds().add("user3");
        assertEquals(3, studyGroup.getMemberIds().size());

        // Test adding sessions
        StudySession session = mock(StudySession.class);
        studyGroup.getSessions().add(session);
        assertEquals(1, studyGroup.getSessions().size());

        // Test adding messages
        ChatMessage message = mock(ChatMessage.class);
        studyGroup.getMessages().add(message);
        assertEquals(1, studyGroup.getMessages().size());
    }
}