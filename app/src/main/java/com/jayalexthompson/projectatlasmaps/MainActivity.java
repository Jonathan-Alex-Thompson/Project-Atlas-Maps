package com.jayalexthompson.projectatlasmaps;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    public FirebaseAuth userCheckAuth;
    Button loginSwitcher;
    Button registerSwitcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userCheckAuth = FirebaseAuth.getInstance();
        blastOff();
        loginSwitcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent loginPage = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(loginPage);
            }
        });
        registerSwitcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent registerPage = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(registerPage);
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = userCheckAuth.getCurrentUser();
        if (currentUser != null){
            //set logic to go to dashboard and get user document
            //Intent dashboardOpening = new Intent(MainActivity.this, DashboardActivity.class);
            //startActivity(dashboardOpening);
        }

    }

    public void blastOff(){
        loginSwitcher = findViewById(R.id.loginGo);
        registerSwitcher = findViewById(R.id.registerGo);

    }
}
