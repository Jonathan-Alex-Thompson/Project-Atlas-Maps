package com.jayalexthompson.projectatlasmaps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class UpdateProfileActivity extends AppCompatActivity {

    ImageButton car;
    ImageButton bus;
    ImageButton walk;
    ImageButton cycle;
    ToggleButton styleIn;
    EditText userName;
    Button returnAction;
    Button save;
    String email;
    FirebaseFirestore database = FirebaseFirestore.getInstance();

    String style = "metric";
    String mode = "car";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);
        car = findViewById(R.id.carSelect);
        save = findViewById(R.id.saveAction);
        car.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                car.setBackground(getDrawable(R.drawable.button_inactive));
                bus.setBackground(getDrawable(R.drawable.button_active));
                walk.setBackground(getDrawable(R.drawable.button_active));
                cycle.setBackground(getDrawable(R.drawable.button_active));
                mode = "car";
            }
        });
        bus = findViewById(R.id.busSelect);
        bus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bus.setBackground(getDrawable(R.drawable.button_inactive));
                car.setBackground(getDrawable(R.drawable.button_active));
                walk.setBackground(getDrawable(R.drawable.button_active));
                cycle.setBackground(getDrawable(R.drawable.button_active));
                mode = "public transport";
            }
        });
        walk = findViewById(R.id.walkingSelect);
        walk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bus.setBackground(getDrawable(R.drawable.button_active));
                walk.setBackground(getDrawable(R.drawable.button_inactive));
                car.setBackground(getDrawable(R.drawable.button_active));
                cycle.setBackground(getDrawable(R.drawable.button_active));
                mode = "walking";
            }
        });
        cycle = findViewById(R.id.cyclingSelect);
        cycle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bus.setBackground(getDrawable(R.drawable.button_active));
                walk.setBackground(getDrawable(R.drawable.button_active));
                car.setBackground(getDrawable(R.drawable.button_active));
                cycle.setBackground(getDrawable(R.drawable.button_inactive));
                mode = "cycle";
            }
        });
        userName = findViewById(R.id.newUserNameIn);
        styleIn = findViewById(R.id.metricStyleIn);
        if(styleIn.isChecked()){
            style="imperial";
        }else{
            style="metric";
        }


        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            //locate specific user details
            DocumentReference docRef = database.collection("app_users").document(FirebaseAuth.getInstance().getCurrentUser().getUid());

            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {

                            document.getData();

                            //check if user has filled in all metrics
                            if (document.get("email") != null || document.get("username") != null ||
                                    document.get("metrics") != null || document.get("mode") != null) {

                                String email = document.get("email").toString();
                                userName.setText(document.get("username").toString());
                                String transport_style = document.get("mode").toString();



                                if (transport_style.equals("car")){

                                    car.setBackground(getDrawable(R.drawable.button_inactive));
                                    bus.setBackground(getDrawable(R.drawable.button_active));
                                    walk.setBackground(getDrawable(R.drawable.button_active));
                                    cycle.setBackground(getDrawable(R.drawable.button_active));
                                    mode = "car";

                                } else if (transport_style.equals("public transport")){

                                    bus.setBackground(getDrawable(R.drawable.button_inactive));
                                    car.setBackground(getDrawable(R.drawable.button_active));
                                    walk.setBackground(getDrawable(R.drawable.button_active));
                                    cycle.setBackground(getDrawable(R.drawable.button_active));
                                    mode = "public transport";

                                }else if(transport_style.equals("walking")){

                                    bus.setBackground(getDrawable(R.drawable.button_active));
                                    walk.setBackground(getDrawable(R.drawable.button_inactive));
                                    car.setBackground(getDrawable(R.drawable.button_active));
                                    cycle.setBackground(getDrawable(R.drawable.button_active));
                                    mode = "walking";
                                }else if(transport_style.equals("cycle")){

                                    bus.setBackground(getDrawable(R.drawable.button_active));
                                    walk.setBackground(getDrawable(R.drawable.button_active));
                                    car.setBackground(getDrawable(R.drawable.button_active));
                                    cycle.setBackground(getDrawable(R.drawable.button_inactive));
                                    mode = "cycle";
                                }

                                String meassure_style = document.get("metrics").toString();

                                if(meassure_style.equals("imperial")){
                                    styleIn.setChecked(true);
                                }else{
                                    styleIn.setChecked(false);
                                }


                            }
                        }
                    }
                }
            });
        }

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, Object> user = new HashMap<>();
                user.put("email", email);
                user.put("username", userName.getText().toString());
                user.put("metrics",style);
                user.put("mode",mode);

                database.collection("app_users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .update(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Intent map = new Intent(UpdateProfileActivity.this, DashboardActivity.class);
                        startActivity(map);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UpdateProfileActivity.this, "Broken instance", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });


        returnAction = findViewById(R.id.returnAction);
        returnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(UpdateProfileActivity.this);
                builder.setTitle("Exit Profile Page!");
                builder.setMessage("You are returning to map, any unsaved profile changes will not be saved. Do you still want to return?");
                builder.setCancelable(false);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent mappingPage = new Intent(UpdateProfileActivity.this, DashboardActivity.class);
                        startActivity(mappingPage);
                    }
                });

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.show();
            }
        });

    }

}
