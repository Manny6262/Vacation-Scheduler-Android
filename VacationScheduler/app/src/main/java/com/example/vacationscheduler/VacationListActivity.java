package com.example.vacationscheduler;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vacationscheduler.entities.Vacation;
import com.example.vacationscheduler.repository.VacationRepository;

import java.util.ArrayList;
import java.util.List;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import com.example.vacationscheduler.repository.ExcursionRepository;

import java.util.Locale;


import android.widget.Toast;

import com.example.vacationscheduler.entities.Excursion;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;


public class VacationListActivity extends AppCompatActivity {

    private ListView vacationListView;
    private EditText searchVacationEditText;
    private VacationRepository repository;
    private ExcursionRepository excursionRepository;

    private final List<Vacation> vacations = new ArrayList<>();
    private final List<Vacation> displayedVacations = new ArrayList<>();
    private final List<String> vacationTitles = new ArrayList<>();

    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vacation_list);

        vacationListView = findViewById(R.id.vacationListView);
        searchVacationEditText = findViewById(R.id.searchVacationEditText);
        Button addVacationButton = findViewById(R.id.addVacationButton);
        Button backupToCloudButton = findViewById(R.id.backupToCloudButton);

        repository = new VacationRepository(getApplication());
        excursionRepository = new ExcursionRepository(getApplication());

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                vacationTitles
        );

        vacationListView.setAdapter(adapter);
        searchVacationEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterVacations(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

        });
        backupToCloudButton.setOnClickListener(v -> backupToCloud());

        addVacationButton.setOnClickListener(v -> {
            Intent intent = new Intent(
                    VacationListActivity.this,
                    VacationDetailActivity.class
            );

            startActivity(intent);
        });

        vacationListView.setOnItemClickListener((parent, view, position, id) -> {

            Vacation selectedVacation = displayedVacations.get(position);

            Intent intent = new Intent(
                    VacationListActivity.this,
                    VacationDetailActivity.class
            );

            intent.putExtra(
                    "vacationID",
                    selectedVacation.getVacationID()
            );

            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadVacations();
    }

    private void loadVacations() {

        repository.getAllVacations(result -> {

            runOnUiThread(() -> {

                vacations.clear();
                vacations.addAll(result);

                filterVacations(searchVacationEditText.getText().toString());
            });
        });
    }

    private void filterVacations(String query) {
            displayedVacations.clear();
            vacationTitles.clear();

            String searchText = query.trim().toLowerCase(Locale.ROOT);

            for (Vacation vacation : vacations) {
                String title = vacation.getVacationTitle();
                String hotel = vacation.getHotel();

                if (searchText.isEmpty()
                        || title.toLowerCase(Locale.ROOT).contains(searchText)
                        || hotel.toLowerCase(Locale.ROOT).contains(searchText)) {

                    displayedVacations.add(vacation);

                    String vacationDetails =
                            vacation.getVacationTitle()
                                    + "\nHotel: " + vacation.getHotel()
                                    + "\nStart: " + vacation.getStartDate()
                                    + "\nEnd: " + vacation.getEndDate();

                    vacationTitles.add(vacationDetails);
                }
            }

            adapter.notifyDataSetChanged();
        }

    private void backupToCloud() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "Firebase is not signed in.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        repository.getAllVacations(savedVacations -> {
            if (savedVacations == null) {
                runOnUiThread(() -> Toast.makeText(this,
                        "Unable to read vacations.",
                        Toast.LENGTH_SHORT).show());
                return;
            }

            List<Map<String, Object>> backupData =
                    Collections.synchronizedList(new ArrayList<>());

            if (savedVacations.isEmpty()) {
                uploadCloudBackup(user.getUid(), backupData);
                return;
            }

            AtomicInteger remaining =
                    new AtomicInteger(savedVacations.size());

            for (Vacation vacation : savedVacations) {
                excursionRepository.getExcursionsForVacation(
                        vacation.getVacationID(),
                        excursions -> {

                            Map<String, Object> vacationData = new HashMap<>();
                            vacationData.put("vacationID", vacation.getVacationID());
                            vacationData.put("title", vacation.getVacationTitle());
                            vacationData.put("hotel", vacation.getHotel());
                            vacationData.put("startDate", vacation.getStartDate());
                            vacationData.put("endDate", vacation.getEndDate());

                            List<Map<String, Object>> excursionData =
                                    new ArrayList<>();

                            if (excursions != null) {
                                for (Excursion excursion : excursions) {
                                    Map<String, Object> item = new HashMap<>();
                                    item.put("excursionID", excursion.getExcursionID());
                                    item.put("title", excursion.getExcursionTitle());
                                    item.put("date", excursion.getExcursionDate());
                                    excursionData.add(item);
                                }
                            }

                            vacationData.put("excursions", excursionData);
                            backupData.add(vacationData);

                            if (remaining.decrementAndGet() == 0) {
                                uploadCloudBackup(user.getUid(), backupData);
                            }
                        }
                );
            }
        });
    }

    private void uploadCloudBackup(
            String userId,
            List<Map<String, Object>> vacationsData
    ) {
        Map<String, Object> backup = new HashMap<>();
        backup.put("vacations", new ArrayList<>(vacationsData));
        backup.put("backedUpAt", FieldValue.serverTimestamp());

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("vacations")
                .document("backup")
                .set(backup)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this,
                                "Cloud backup successful!",
                                Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Cloud backup failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }

}


