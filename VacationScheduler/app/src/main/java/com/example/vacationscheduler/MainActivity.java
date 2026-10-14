package com.example.vacationscheduler;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() == null) {
            firebaseAuth.signInAnonymously()
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this,
                                    "Firebase sign-in successful",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this,
                                    "Firebase sign-in failed",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            Toast.makeText(this,
                    "Firebase already signed in",
                    Toast.LENGTH_SHORT).show();
        }

        Button viewVacationsButton =
                findViewById(R.id.viewVacationsButton);

        viewVacationsButton.setOnClickListener(v -> {

            Intent intent = new Intent(
                    MainActivity.this,
                    VacationListActivity.class
            );

            startActivity(intent);
        });
    }
}