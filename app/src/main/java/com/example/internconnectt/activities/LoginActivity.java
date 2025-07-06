package com.example.internconnectt.activities;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.internconnectt.R;

public class LoginActivity extends AppCompatActivity{
    EditText editTextEmail,editTextPassword;
    Button buttonLogin;
    TextView textRegister;
    RadioGroup roleRadioGroup;
    RadioButton radioStudent, radioFaculty;

    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth=FirebaseAuth.getInstance();
        editTextEmail= findViewById(R.id.editTextEmail);
        editTextPassword=findViewById(R.id.editTextPassword);
        buttonLogin= (Button) findViewById(R.id.buttonLogin);
        textRegister= (TextView) findViewById(R.id.textRegister);
        roleRadioGroup = findViewById(R.id.roleRadioGroup);
        radioStudent = findViewById(R.id.radioStudent);
        radioFaculty = findViewById(R.id.radioFaculty);
        textRegister.setOnClickListener(view -> {
            Toast.makeText(LoginActivity.this, "Register", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
        buttonLogin.setOnClickListener(view ->loginUser());

    }
    private void loginUser(){
        String email=editTextEmail.getText().toString().trim();
        String password=editTextPassword.getText().toString().trim();
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Enter a valid email");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Enter password");
            return;
        }
        if (roleRadioGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select Student or Faculty", Toast.LENGTH_SHORT).show();
            return;
        }
        // Sign in with Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

                        // Redirect based on role
                        Intent intent;
                        if (radioStudent.isChecked()) {
                            intent = new Intent(LoginActivity.this, StudentDashboardActivity.class);
                        } else {
                            intent = new Intent(LoginActivity.this, FacultyDashboardActivity.class);
                        }
                        startActivity(intent);
                    } else {
                        Toast.makeText(this,
                                "Login failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
