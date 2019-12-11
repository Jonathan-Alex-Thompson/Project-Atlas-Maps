package com.jayalexthompson.projectatlasmaps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class HistroyActivity extends AppCompatActivity {

    //fix this, check why its not selecting from that file
    TextView tripShow;
    Button returnction;
    FirebaseFirestore database = FirebaseFirestore.getInstance();
    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_histroy);
        tripShow = findViewById(R.id.trip_display);

        returnction = findViewById(R.id.returnAction);
        returnction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mappingPage = new Intent(HistroyActivity.this, DashboardActivity.class);
                startActivity(mappingPage);
            }
        });

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            //locate specific user details for email and mode saving
            DocumentReference docRef = database.collection("app_users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());

            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {

                            document.getData();

                            //check if user has filled in all metrics
                            if (document.get("email") != null) {
                                email = document.get("email").toString();

                            } else {
                                tripShow.setText("No trips saved yet, please save a trip to see the history");
                            }
                        } else {
                            tripShow.setText("No trip history exists");
                        }
                    }
                }
            });
        }

        if (email != null) {

            String findmail = email;
            //locate specific user details

            CollectionReference myRef = database.collection("app_trips");
            Query findTrip = myRef.whereEqualTo("email", findmail);

            findTrip.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                    for (DocumentSnapshot ds : queryDocumentSnapshots) {
                        if (ds != null && ds.exists()) {
                            //Toast.makeText(RegisterActivity.this, "Username Exists!", Toast.LENGTH_SHORT).show();
                            String Display = ds.get("start").toString() + "\n" + ds.get("end").toString() + "\n" +
                                    ds.get("distance").toString() + "\n" + ds.get("time").toString() + "\n" + ds.get("date").toString() + "\n";
                            tripShow.setText(Display);
                        } else {
                            tripShow.setText("No trips saved yet, please save a trip to see the history");
                        }
                    }
                }
            });

        }
    }
}
