package com.example.vacationscheduler;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vacationscheduler.entities.Excursion;
import com.example.vacationscheduler.repository.ExcursionRepository;

import java.util.ArrayList;
import java.util.List;

public class ExcursionListActivity extends AppCompatActivity {

    private ListView excursionListView;
    private ExcursionRepository repository;

    private final List<Excursion> excursions = new ArrayList<>();
    private final List<String> excursionDetails = new ArrayList<>();

    private ArrayAdapter<String> adapter;

    private int vacationID = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_excursion_list);

        excursionListView = findViewById(R.id.excursionListView);
        Button addExcursionButton = findViewById(R.id.addExcursionButton);
        TextView headerTextView = findViewById(R.id.excursionHeaderTextView);

        repository = new ExcursionRepository(getApplication());

        vacationID = getIntent().getIntExtra("vacationID", -1);

        if (vacationID == -1) {
            Toast.makeText(
                    this,
                    "Unable to determine the associated vacation.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        headerTextView.setText("Excursions");

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                excursionDetails
        );

        excursionListView.setAdapter(adapter);

        addExcursionButton.setOnClickListener(v -> {

            Intent intent = new Intent(
                    ExcursionListActivity.this,
                    ExcursionDetailActivity.class
            );

            intent.putExtra("vacationID", vacationID);

            startActivity(intent);
        });

        excursionListView.setOnItemClickListener(
                (parent, view, position, id) -> {

                    Excursion selectedExcursion =
                            excursions.get(position);

                    Intent intent = new Intent(
                            ExcursionListActivity.this,
                            ExcursionDetailActivity.class
                    );

                    intent.putExtra(
                            "vacationID",
                            vacationID
                    );

                    intent.putExtra(
                            "excursionID",
                            selectedExcursion.getExcursionID()
                    );

                    startActivity(intent);
                }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadExcursions();
    }

    private void loadExcursions() {

        repository.getExcursionsForVacation(
                vacationID,
                result -> {

                    runOnUiThread(() -> {

                        excursions.clear();
                        excursionDetails.clear();

                        excursions.addAll(result);

                        for (Excursion excursion : excursions) {

                            String details =
                                    "Excursion: " + excursion.getExcursionTitle()
                                            + "\nDate: "
                                            + excursion.getExcursionDate();

                            excursionDetails.add(details);
                        }

                        adapter.notifyDataSetChanged();
                    });
                }
        );
    }
}