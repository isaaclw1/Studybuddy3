package com.example.studybuddy3.utils;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;

import android.view.View;
import android.widget.ListView;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.matcher.BoundedMatcher;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class DialogTestUtils {

    public static ViewAction clickItemAtPosition(final int position) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return allOf(isAssignableFrom(ListView.class), isDisplayed());
            }

            @Override
            public String getDescription() {
                return "Click item at position " + position + " in dialog list";
            }

            @Override
            public void perform(UiController uiController, View view) {
                ListView listView = (ListView) view;
                View item = listView.getChildAt(position);
                if (item != null) {
                    item.performClick();
                }
            }
        };
    }

    public static Matcher<View> withListSize(final int size) {
        return new BoundedMatcher<View, ListView>(ListView.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("ListView with size: " + size);
            }

            @Override
            protected boolean matchesSafely(ListView listView) {
                return listView.getCount() == size;
            }
        };
    }

    public static ViewInteraction onDialogList() {
        return onView(allOf(isAssignableFrom(ListView.class), isDisplayed()));
    }
}