package com.example.studybuddy3;

import android.content.Intent;

import androidx.test.espresso.action.ViewActions;
import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;
import static org.hamcrest.Matchers.*;

@RunWith(AndroidJUnit4.class)
public class ResourceActivityTest {

    @Rule
    public ActivityTestRule<ResourceActivity> mActivityRule = new ActivityTestRule<>(ResourceActivity.class, false, false);

    @Test
    public void testUploadResource() throws InterruptedException {
        // Start the activity with sessionId and groupId
        Intent intent = new Intent();
        intent.putExtra("sessionId", "test-session-123");
        intent.putExtra("groupId", "test-group-456");
        mActivityRule.launchActivity(intent);

        // Click on 'Upload' button
        onView(withId(R.id.uploadButton)).perform(click());

        // Wait for the upload activity to load
        TimeUnit.SECONDS.sleep(1);

        // Enter resource details
        onView(withId(R.id.resourceNameInput)).perform(typeText("Test Resource"), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.resourceDescriptionText)).perform(typeText("Test Description"), ViewActions.closeSoftKeyboard());
        onView(withId(R.id.resourceCategoryText)).perform(typeText("Notes"), ViewActions.closeSoftKeyboard());

        // Click on 'Upload' button
        onView(withId(R.id.uploadResourceButton)).perform(click());

        // Wait for the operation to complete
        TimeUnit.SECONDS.sleep(2);

        // Verify that the resource appears in the resource list
        onView(withText("Test Resource")).check(matches(isDisplayed()));
    }

    @Test
    public void testSearchResource() throws InterruptedException {
        // Start the activity with sessionId and groupId
        Intent intent = new Intent();
        intent.putExtra("sessionId", "test-session-123");
        intent.putExtra("groupId", "test-group-456");
        mActivityRule.launchActivity(intent);

        // Wait for resources to load
        TimeUnit.SECONDS.sleep(2);

        // Enter search query
        onView(withId(R.id.searchInput)).perform(typeText("Test"), ViewActions.closeSoftKeyboard());

        // Verify that the resource matching the query is displayed
        onView(withId(R.id.resourcesRecyclerView))
                .check(matches(hasDescendant(withText("Test Resource"))));
    }
}
