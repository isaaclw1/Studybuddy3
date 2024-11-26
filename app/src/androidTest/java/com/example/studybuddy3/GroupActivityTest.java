package com.example.studybuddy3;

import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.*;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static androidx.test.espresso.assertion.ViewAssertions.*;

@RunWith(AndroidJUnit4.class)
public class GroupActivityTest {

    @Rule
    public ActivityTestRule<GroupActivity> mActivityRule = new ActivityTestRule<>(GroupActivity.class);

    @Test
    public void testViewGroupMembers() {
        // Verify that the member list is displayed
        onView(withId(R.id.memberRecyclerView)).check(matches(isDisplayed()));

        // Verify that at least one member is displayed
        onView(withId(R.id.memberRecyclerView))
                .check(matches(hasDescendant(withId(R.id.memberNameText))));
    }
}
