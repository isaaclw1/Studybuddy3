package com.example.studybuddy3;

import androidx.test.espresso.action.ViewActions;
import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.database.FirebaseDatabase;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.action.ViewActions.*;
import static org.hamcrest.Matchers.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;

@RunWith(AndroidJUnit4.class)
public class AddGroupActivityTest {

    @Rule
    public ActivityTestRule<AddGroupActivity> mActivityRule = new ActivityTestRule<>(AddGroupActivity.class);

    @Before
    public void setUp() {
        FirebaseDatabase.getInstance().setPersistenceEnabled(false);
    }

    @Test
    public void testSuccessfulGroupCreation() throws InterruptedException {
        // Wait for the courses to load
        TimeUnit.SECONDS.sleep(2);

        // Select a course from the dropdown
        onView(withId(R.id.courseDropdown)).perform(click());
        // Assuming the course list is populated, select the first item
        onData(anything()).atPosition(0).perform(click());

        // Enter a valid group name
        onView(withId(R.id.groupNameInput)).perform(typeText("TestGroup"), ViewActions.closeSoftKeyboard());

        // Click on 'Create Group' button
        onView(withId(R.id.createGroupButton)).perform(click());

        // Wait for the operation to complete
        TimeUnit.SECONDS.sleep(2);

        // Verify that a success message is displayed
        onView(withText("Group created successfully"))
                .inRoot(withDecorView(not(is(mActivityRule.getActivity().getWindow().getDecorView()))))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testDuplicateGroupName() throws InterruptedException {
        // Create a group first
        testSuccessfulGroupCreation();

        // Attempt to create the same group again
        onView(withId(R.id.groupNameInput)).perform(clearText(), typeText("TestGroup"), ViewActions.closeSoftKeyboard());

        // Click on 'Create Group' button
        onView(withId(R.id.createGroupButton)).perform(click());

        // Wait for the operation to complete
        TimeUnit.SECONDS.sleep(2);

        // Verify that an error message is displayed
        onView(withText("Group name already exists for this course"))
                .inRoot(withDecorView(not(is(mActivityRule.getActivity().getWindow().getDecorView()))))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testGroupNameValidation() {
        // Enter an invalid group name
        onView(withId(R.id.groupNameInput)).perform(typeText("Invalid@Name!"), ViewActions.closeSoftKeyboard());

        // Click on 'Create Group' button
        onView(withId(R.id.createGroupButton)).perform(click());

        // Verify that an error message is displayed
        onView(withText("Group name can only contain letters, numbers, hyphens, and underscores"))
                .inRoot(withDecorView(not(is(mActivityRule.getActivity().getWindow().getDecorView()))))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testAddingMembersToGroup() throws InterruptedException {
        // Wait for the courses to load
        TimeUnit.SECONDS.sleep(2);

        // Select a course from the dropdown
        onView(withId(R.id.courseDropdown)).perform(click());
        onData(anything()).atPosition(0).perform(click());

        // Click on 'Select Members' button
        onView(withId(R.id.selectMembersButton)).perform(click());

        // Wait for the dialog to appear
        TimeUnit.SECONDS.sleep(1);

        // Select the first member in the list
        onData(anything()).atPosition(0).perform(click());

        // Click 'Done'
        onView(withText("Done")).perform(click());

        // Verify that the member chip is added
        onView(allOf(withId(R.id.selectedMembersChipGroup), hasDescendant(withText(containsString("@")))))
                .check(matches(isDisplayed()));
    }
}
