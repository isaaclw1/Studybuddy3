package com.example.studybuddy3.loginsignup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studybuddy3.R;
import com.example.studybuddy3.datatype.Course;
import com.example.studybuddy3.datatype.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SignupActivity extends AppCompatActivity {

    private TextInputEditText emailInput, passwordInput, confirmPasswordInput;
    private ImageButton addCoursesButton;
    private MaterialButton registerButton;
    private ChipGroup courseChipGroup;
    private ProgressBar progressBar;

    private DatabaseReference mDatabase;
    private List<String> selectedCourses = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase Database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize views and setup listeners
        initializeViews();
        setupClickListeners();

        // Initialize sample courses if needed
        initializeSampleCourses();
    }

    private void initializeViews() {
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        addCoursesButton = findViewById(R.id.addCoursesButton);
        registerButton = findViewById(R.id.registerButton);
        courseChipGroup = findViewById(R.id.courseChipGroup);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        addCoursesButton.setOnClickListener(v -> showCourseSelectionDialog());
        registerButton.setOnClickListener(v -> validateAndRegister());
    }

    private void initializeSampleCourses() {
        mDatabase.child("courses").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // Add sample courses if none exist
                    List<Course> sampleCourses = Arrays.asList(
                            new Course("CS101", "Introduction to Programming", "CS101", "Basic programming concepts"),
                            new Course("CS201", "Data Structures", "CS201", "Advanced data structures"),
                            new Course("CS301", "Algorithms", "CS301", "Algorithm design and analysis"),
                            new Course("MATH101", "Calculus I", "MATH101", "Introduction to calculus"),
                            new Course("MATH201", "Linear Algebra", "MATH201", "Matrices and vector spaces")
                    );

                    for (Course course : sampleCourses) {
                        mDatabase.child("courses").child(course.getCourseId()).setValue(course);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    private void showCourseSelectionDialog() {
        mDatabase.child("courses").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Course> courses = new ArrayList<>();
                for (DataSnapshot courseSnapshot : snapshot.getChildren()) {
                    Course course = courseSnapshot.getValue(Course.class);
                    if (course != null) {
                        courses.add(course);
                    }
                }

                if (courses.isEmpty()) {
                    Toast.makeText(SignupActivity.this,
                            "No courses available", Toast.LENGTH_SHORT).show();
                    return;
                }

                String[] courseNames = new String[courses.size()];
                boolean[] checkedItems = new boolean[courses.size()];

                for (int i = 0; i < courses.size(); i++) {
                    courseNames[i] = courses.get(i).getCourseName();
                    checkedItems[i] = selectedCourses.contains(courses.get(i).getCourseId());
                }

                new MaterialAlertDialogBuilder(SignupActivity.this)
                        .setTitle("Select Your Courses")
                        .setMultiChoiceItems(courseNames, checkedItems, (dialog, which, isChecked) -> {
                            String courseId = courses.get(which).getCourseId();
                            String courseName = courses.get(which).getCourseName();

                            if (isChecked) {
                                selectedCourses.add(courseId);
                                addChipToGroup(courseId, courseName);
                            } else {
                                selectedCourses.remove(courseId);
                                removeChipFromGroup(courseName);
                            }
                        })
                        .setPositiveButton("Done", null)
                        .show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SignupActivity.this,
                        "Failed to load courses: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addChipToGroup(String courseId, String courseName) {
        for (int i = 0; i < courseChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) courseChipGroup.getChildAt(i);
            if (chip.getText().toString().equals(courseName)) {
                return;
            }
        }

        Chip chip = new Chip(this);
        chip.setText(courseName);
        chip.setCloseIconVisible(true);
        chip.setTag(courseId);
        chip.setOnCloseIconClickListener(v -> {
            courseChipGroup.removeView(chip);
            selectedCourses.remove(courseId);
        });
        courseChipGroup.addView(chip);
    }

    private void removeChipFromGroup(String courseName) {
        for (int i = 0; i < courseChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) courseChipGroup.getChildAt(i);
            if (chip.getText().toString().equals(courseName)) {
                courseChipGroup.removeView(chip);
                break;
            }
        }
    }

    private void validateAndRegister() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        String confirmPassword = confirmPasswordInput.getText().toString();

        if (!validateInputs(email, password, confirmPassword)) {
            return;
        }

        showProgress(true);
        checkIfUserExists(email, password);
    }

    private boolean validateInputs(String email, String password, String confirmPassword) {
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (selectedCourses.isEmpty()) {
            Toast.makeText(this, "Please select at least one course", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void checkIfUserExists(String email, String password) {
        mDatabase.child("users").orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            showProgress(false);
                            Toast.makeText(SignupActivity.this,
                                    "Email already registered", Toast.LENGTH_SHORT).show();
                        } else {
                            registerUser(email, password);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        showProgress(false);
                        Toast.makeText(SignupActivity.this,
                                "Registration failed: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void registerUser(String email, String password) {
        String userId = mDatabase.child("users").push().getKey();

        if (userId != null) {
            String hashedPassword = hashPassword(password);
            User user = new User(userId, email, hashedPassword);
            user.setEnrolledCourses(selectedCourses);

            // Save user data
            mDatabase.child("users").child(userId)
                    .setValue(user)
                    .addOnCompleteListener(task -> {
                        showProgress(false);
                        if (task.isSuccessful()) {
                            // Update each selected course with the new user's ID
                            for (String courseId : selectedCourses) {
                                DatabaseReference courseRef = mDatabase.child("courses").child(courseId).child("enrolledUserIds");
                                courseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        List<String> enrolledUserIds = (List<String>) snapshot.getValue();
                                        if (enrolledUserIds == null) {
                                            enrolledUserIds = new ArrayList<>();
                                        }
                                        enrolledUserIds.add(userId);

                                        // Update the course with the new enrolledUserIds list
                                        courseRef.setValue(enrolledUserIds);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(SignupActivity.this,
                                                "Failed to enroll in course: " + courseId,
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            // Save user session and navigate to MainActivity
                            saveUserSession(userId, email);
                            Toast.makeText(SignupActivity.this,
                                    "Registration successful", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SignupActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(SignupActivity.this,
                                    "Failed to save user data", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            showProgress(false);
            Toast.makeText(this, "Failed to generate user ID", Toast.LENGTH_SHORT).show();
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return android.util.Base64.encodeToString(hash, android.util.Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveUserSession(String userId, String email) {
        SharedPreferences prefs = getSharedPreferences("StudyBuddy", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("userId", userId);
        editor.putString("userEmail", email);
        editor.putBoolean("isLoggedIn", true);
        editor.apply();
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        registerButton.setEnabled(!show);
        addCoursesButton.setEnabled(!show);
    }
}