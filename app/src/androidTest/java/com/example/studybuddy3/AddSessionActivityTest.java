package com.example.studybuddy3;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;

import androidx.test.espresso.action.ViewActions;
import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static androidx.test.espresso.contrib.PickerActions.*;
import static org.hamcrest.Matchers.*;

import static androidx.test.espresso.contrib.PickerActions.*;
import static org.hamcrest.Matchers.*;
import android.widget.DatePicker;
import android.widget.TimePicker;


@RunWith(AndroidJUnit4.class)
public class AddSessionActivityTest {

    @Rule
    public ActivityTestRule<AddSessionActivity> mActivityRule = new ActivityTestRule<>(AddSessionActivity.class);

    @Test
    public void testSuccessfulSessionCreation() throws InterruptedException {
        // Enter a valid title
        onView(withId(R.id.titleInput)).perform(typeText("Study Session"), ViewActions.closeSoftKeyboard());

        // Select a date
        onView(withId(R.id.dateInput)).perform(click());

        onView(withClassName(equalTo(DatePicker.class.getName())))
                .perform(setDate(2024, 11, 15));
        onView(withText("OK")).perform(click());

        // Select start time
        onView(withId(R.id.startTimeInput)).perform(click());
        onView(withClassName(equalTo(TimePicker.class.getName())))
                .perform(setTime(10, 0));
        onView(withText("OK")).perform(click());

        // Select end time
        onView(withId(R.id.endTimeInput)).perform(click());
        onView(withClassName(equalTo(TimePicker.class.getName())))
                .perform(setTime(12, 0));
        onView(withText("OK")).perform(click());

        // Enter location
        onView(withId(R.id.locationInput)).perform(typeText("Library"), ViewActions.closeSoftKeyboard());

        // Select attendees
        onView(withId(R.id.selectMembersButton)).perform(click());
        TimeUnit.SECONDS.sleep(1);
        onData(anything()).atPosition(0).perform(click());
        onView(withText("Done")).perform(click());

        // Create session
        onView(withId(R.id.createSessionButton)).perform(click());

        // Wait for the operation to complete
        TimeUnit.SECONDS.sleep(2);

        // Verify success message
        onView(withText("Study session created successfully"))
                .inRoot(withDecorView(not(is(mActivityRule.getActivity().getWindow().getDecorView()))))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testSessionTimeValidation() throws InterruptedException {
        // Enter a valid title
        onView(withId(R.id.titleInput)).perform(typeText("Invalid Time Session"), ViewActions.closeSoftKeyboard());

        // Select a date
        onView(withId(R.id.dateInput)).perform(click());

        onView(withClassName(equalTo(DatePicker.class.getName())))
                .perform(setDate(2024, 11, 15));
        onView(withText("OK")).perform(click());

        // Select start time
        onView(withId(R.id.startTimeInput)).perform(click());
        onView(withClassName(equalTo(TimePicker.class.getName())))
                .perform(setTime(12, 0));
        onView(withText("OK")).perform(click());

        // Select end time before start time
        onView(withId(R.id.endTimeInput)).perform(click());
        onView(withClassName(equalTo(TimePicker.class.getName())))
                .perform(setTime(10, 0));
        onView(withText("OK")).perform(click());

        // Enter location
        onView(withId(R.id.locationInput)).perform(typeText("Library"), ViewActions.closeSoftKeyboard());

        // Create session
        onView(withId(R.id.createSessionButton)).perform(click());

        // Verify error message
        onView(withText("End time must be after start time"))
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