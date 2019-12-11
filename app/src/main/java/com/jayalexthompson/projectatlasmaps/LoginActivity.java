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

public class LoginActivity extends AppCompatActivity {


    Button login;
    EditText currentUserPassword;
    EditText currentUserEmail;
    String passwordIn = "";
    String emailIn = "";
    private FirebaseAuth loginAuth;

    private static final String TAG = LoginActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginAuth = FirebaseAuth.getInstance();
        blastOff();
        currentUserPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasChanged) {
                /* When focus is lost check that the text field
                 * has valid values.
                 */
                if (!hasChanged) {
                    passwordIn = currentUserPassword.getText().toString();
                }
            }
        });

        currentUserEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasChanged) {
                /* When focus is lost check that the text field
                 * has valid values.
                 */
                if (!hasChanged) {
                    emailIn = currentUserEmail.getText().toString();
                }
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                passwordIn = currentUserPassword.getText().toString();
                emailIn = currentUserEmail.getText().toString();
                loginAuth.signInWithEmailAndPassword(emailIn, passwordIn)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                //Rename error messages and create actions.
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "signInWithEmail:success");
                                    Toast.makeText(LoginActivity.this,"logged in",Toast.LENGTH_SHORT).show();
                                    FirebaseUser currentUser = loginAuth.getCurrentUser();

                                    Intent mapOpen = new Intent(LoginActivity.this, DashboardActivity.class);
                                    mapOpen.putExtra("email",emailIn);
                                    startActivity(mapOpen);

                                    /*Intent profile = new Intent(LoginActivity.this, ProfileActivity.class);
                                    profile.putExtra("email", emailIn);
                                    startActivity(profile);*/
                                    //updateUI(currentUser);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    Toast.makeText(LoginActivity.this, "Authentication failed.",
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
        login = findViewById(R.id.loginAction);
        currentUserEmail = findViewById(R.id.loginEmail);
        currentUserPassword = findViewById(R.id.loginPassword);

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = loginAuth.getCurrentUser();
        //updateUI(currentUser);
    }
}
