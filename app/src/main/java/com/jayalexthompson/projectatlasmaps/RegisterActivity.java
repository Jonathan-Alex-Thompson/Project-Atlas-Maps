package com.jayalexthompson.projectatlasmaps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    Button register;
    EditText newUserPassword;
    EditText newUserEmail;
    String passwordIn;
    String emailIn;
    private FirebaseAuth registerAuth;

    private static final String TAG = RegisterActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        registerAuth = FirebaseAuth.getInstance();
        register = findViewById(R.id.registerAction);
        newUserEmail = findViewById(R.id.registerEmail);
        newUserPassword = findViewById(R.id.registerPassword);
        blastOff();

        newUserPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasChanged) {
                /* When focus is lost check that the text field
                 * has valid values.
                 */
                if (!hasChanged) {
                    passwordIn = newUserPassword.getText().toString();
                    Toast.makeText(getApplicationContext(),passwordIn, Toast.LENGTH_SHORT).show();
                }
            }
        });

        newUserEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasChanged) {
                /* When focus is lost check that the text field
                 * has valid values.
                 */
                if (!hasChanged) {
                    emailIn = newUserEmail.getText().toString();
                    Toast.makeText(getApplicationContext(),emailIn, Toast.LENGTH_SHORT).show();
                }
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emailIn = newUserEmail.getText().toString();
                passwordIn = newUserPassword.getText().toString();
                registerAuth.createUserWithEmailAndPassword(emailIn, passwordIn)
                        .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "createUserWithEmail:success");
                                    Toast.makeText(RegisterActivity.this, "Sign-up Successful",
                                            Toast.LENGTH_SHORT).show();
                                    FirebaseUser newUser = registerAuth.getCurrentUser();
                                    Intent profile = new Intent(RegisterActivity.this, ProfileActivity.class);
                                    profile.putExtra("email", emailIn);
                                    startActivity(profile);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                    //updateUI(null);
                                }

                                // ...
                            }
                        });
            }
        });
    }

    public void blastOff(){


    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = registerAuth.getCurrentUser();
        //updateUI(currentUser);
    }
}
