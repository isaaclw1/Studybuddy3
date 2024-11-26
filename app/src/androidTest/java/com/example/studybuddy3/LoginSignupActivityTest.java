package com.example.studybuddy3;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.not;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.database.FirebaseDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginSignupActivityTest {

    @Before
    public void setup() {
        Intents.init();
        // Clear any existing data
        FirebaseDatabase.getInstance().getReference();
    }

    @After
    public void cleanup() {
        Intents.release();
    }


    @Test
    public void testInvalidLogin() {
        ActivityScenario<LoginActivity> activityScenario = ActivityScenario.launch(LoginActivity.class);

        // Fill in login form with invalid credentials
        onView(withId(R.id.emailInput))
                .perform(typeText("invalid@example.com"), closeSoftKeyboard());
        onView(withId(R.id.passwordInput))
                .perform(typeText("wrongpassword"), closeSoftKeyboard());

        // Click login
        onView(withId(R.id.loginButton)).perform(click());

        // Verify we're still on LoginActivity (by checking if login button is still visible)
        onView(withId(R.id.loginButton)).check(matches(isDisplayed()));
    }

    @Test
    public void testInvalidSignup() {
        ActivityScenario<SignupActivity> activityScenario = ActivityScenario.launch(SignupActivity.class);

        // Fill in signup form with invalid data (missing courses)
        onView(withId(R.id.emailInput))
                .perform(typeText("test@example.com"), closeSoftKeyboard());
        onView(withId(R.id.passwordInput))
                .perform(typeText("pass"), closeSoftKeyboard()); // Too short password
        onView(withId(R.id.confirmPasswordInput))
                .perform(typeText("pass"), closeSoftKeyboard());

        // Click register
        onView(withId(R.id.registerButton)).perform(click());

        // Verify we're still on SignupActivity
        onView(withId(R.id.registerButton)).check(matches(isDisplayed()));
    }

    @Test
    public void testSuccessfulLogin() {
        ActivityScenario<LoginActivity> activityScenario = ActivityScenario.launch(LoginActivity.class);

        // Fill in login form
        onView(withId(R.id.emailInput))
                .perform(typeText("haha@gmail.com"), closeSoftKeyboard());
        onView(withId(R.id.passwordInput))
                .perform(typeText("123456"), closeSoftKeyboard());

        // Click login
        onView(withId(R.id.loginButton)).perform(click());

        // Verify navigation to PostLoginActivity
        intended(hasComponent(PostLoginActivity.class.getName()));
    }

    @Test
    public void testLoginFormValidation() {
        ActivityScenario<LoginActivity> activityScenario = ActivityScenario.launch(LoginActivity.class);

        // Test empty fields
        onView(withId(R.id.loginButton)).perform(click());
        onView(withId(R.id.loginButton)).check(matches(isDisplayed()));

        // Test invalid email format
        onView(withId(R.id.emailInput))
                .perform(typeText("invalidemail"), closeSoftKeyboard());
        onView(withId(R.id.passwordInput))
                .perform(typeText("password123"), closeSoftKeyboard());
        onView(withId(R.id.loginButton)).perform(click());
        onView(withId(R.id.loginButton)).check(matches(isDisplayed()));
    }

    @Test
    public void testSignupFormValidation() {
        ActivityScenario<SignupActivity> activityScenario = ActivityScenario.launch(SignupActivity.class);

        // Test password mismatch
        onView(withId(R.id.emailInput))
                .perform(typeText("test@example.com"), closeSoftKeyboard());
        onView(withId(R.id.passwordInput))
                .perform(typeText("password123"), closeSoftKeyboard());
        onView(withId(R.id.confirmPasswordInput))
                .perform(typeText("password456"), closeSoftKeyboard());
        onView(withId(R.id.registerButton)).perform(click());
        onView(withId(R.id.registerButton)).check(matches(isDisplayed()));

        // Verify progress indicators
        onView(withId(R.id.progressBar)).check(matches(not(isDisplayed())));
        onView(withId(R.id.registerButton)).check(matches(isEnabled()));
    }
}