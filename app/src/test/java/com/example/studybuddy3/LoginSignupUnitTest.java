package com.example.studybuddy3;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.studybuddy3.utils.TestClock;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class LoginSignupUnitTest {

    private LoginActivity loginActivity;
    private SignupActivity signupActivity;
    private TestClock testClock;
    private static final long INITIAL_TIME = 1635724800000L;

    @Before
    public void setUp() {
        testClock = new TestClock(INITIAL_TIME);
        loginActivity = new LoginActivity();
        signupActivity = new SignupActivity();
    }

    @Test
    public void testValidateInputs_Login_ValidInputs() {
        assertTrue(loginActivity.validateInputs("test@example.com", "password123"));
    }

    @Test
    public void testValidateInputs_Login_EmptyEmail() {
        assertFalse(loginActivity.validateInputs("", "password123"));
    }

    @Test
    public void testValidateInputs_Login_EmptyPassword() {
        assertFalse(loginActivity.validateInputs("test@example.com", ""));
    }

    @Test
    public void testValidateInputs_Login_InvalidEmail() {
        assertFalse(loginActivity.validateInputs("invalid-email", "password123"));
    }

    @Test
    public void testHashPassword() {
        String password = "password123";
        String hashedPassword = loginActivity.hashPassword(password);
        assertNotNull(hashedPassword);
        assertTrue(hashedPassword.length() > 0);
    }

    @Test
    public void testHashPassword_EmptyPassword() {
        String password = "";
        String hashedPassword = loginActivity.hashPassword(password);
        assertNotNull(hashedPassword);
        assertTrue(hashedPassword.length() > 0);
    }

    @Test
    public void testValidateInputs_Signup_MismatchedPasswords() {
        assertFalse(signupActivity.validateInputs(
                "test@example.com",
                "password123",
                "differentPassword"
        ));
    }

    @Test
    public void testValidateInputs_Signup_ShortPassword() {
        assertFalse(signupActivity.validateInputs(
                "test@example.com",
                "12345",
                "12345"
        ));
    }

    @Test
    public void testValidateInputs_Signup_EmptyEmail() {
        assertFalse(signupActivity.validateInputs(
                "",
                "password123",
                "password123"
        ));
    }

    @Test
    public void testHashPassword_Signup() {
        String password = "securePassword";
        String hashedPassword = signupActivity.hashPassword(password);
        assertNotNull(hashedPassword);
        assertTrue(hashedPassword.length() > 0);
    }
}