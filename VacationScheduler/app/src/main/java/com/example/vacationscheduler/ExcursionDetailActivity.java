package com.example.vacationscheduler;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vacationscheduler.entities.Excursion;
import com.example.vacationscheduler.repository.ExcursionRepository;

import com.example.vacationscheduler.repository.VacationRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.ZoneId;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.vacationscheduler.validation.DateValidator;

import android.app.DatePickerDialog;
import java.util.Calendar;

public class ExcursionDetailActivity extends AppCompatActivity {

    private EditText titleEditText;
    private EditText dateEditText;

    private VacationRepository vacationRepository;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MM/dd/yyyy");

    private ExcursionRepository repository;
    private Excursion currentExcursion;

    private int vacationID = -1;
    private int excursionID = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_excursion_detail);

        titleEditText = findViewById(R.id.excursionTitleEditText);
        dateEditText = findViewById(R.id.excursionDateEditText);
        dateEditText.setOnClickListener(v -> showDatePicker(dateEditText));

        Button saveButton = findViewById(R.id.saveExcursionButton);
        Button deleteButton = findViewById(R.id.deleteExcursionButton);
        Button alertButton = findViewById(R.id.setExcursionAlertButton);

        repository = new ExcursionRepository(getApplication());
        vacationRepository = new VacationRepository(getApplication());

        vacationID = getIntent().getIntExtra("vacationID", -1);
        excursionID = getIntent().getIntExtra("excursionID", -1);

        if (excursionID != -1) {
            loadExcursion();
        } else {
            deleteButton.setEnabled(false);
        }

        saveButton.setOnClickListener(v -> saveExcursion());
        deleteButton.setOnClickListener(v -> deleteExcursion());
        alertButton.setOnClickListener(v -> setExcursionAlert());
    }

    private void loadExcursion() {

        repository.getExcursionById(excursionID, excursion -> {

            currentExcursion = excursion;

            runOnUiThread(() -> {

                if (excursion != null) {
                    titleEditText.setText(
                            excursion.getExcursionTitle()
                    );

                    dateEditText.setText(
                            excursion.getExcursionDate()
                    );
                }
            });
        });
    }

    private void saveExcursion() {

        String title =
                titleEditText.getText().toString().trim();

        String date =
                dateEditText.getText().toString().trim();

        if (title.isEmpty() || date.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please complete all fields.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (vacationID == -1) {

            Toast.makeText(
                    this,
                    "Unable to determine the associated vacation.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }



        vacationRepository.getVacationById(
                vacationID,
                vacation -> {

                    if (vacation == null) {
                        return;
                    }

                    try {

                        if (!DateValidator.isExcursionWithinVacation(
                                date,
                                vacation.getStartDate(),
                                vacation.getEndDate()
                        )) {
                            runOnUiThread(() ->
                                    Toast.makeText(
                                            ExcursionDetailActivity.this,
                                            "Excursion date must be during the vacation.",
                                            Toast.LENGTH_LONG
                                    ).show()
                            );
                            return;
                        }

                        if (excursionID == -1) {

                            Excursion excursion =
                                    new Excursion(
                                            vacationID,
                                            title,
                                            date
                                    );

                            repository.insert(excursion);

                            runOnUiThread(() -> {
                                Toast.makeText(
                                        ExcursionDetailActivity.this,
                                        "Excursion added.",
                                        Toast.LENGTH_SHORT
                                ).show();

                                finish();
                            });

                        } else {

                            if (currentExcursion == null) {
                                return;
                            }

                            currentExcursion.setExcursionTitle(title);
                            currentExcursion.setExcursionDate(date);
                            currentExcursion.setVacationID(vacationID);

                            repository.update(currentExcursion);

                            runOnUiThread(() -> {
                                Toast.makeText(
                                        ExcursionDetailActivity.this,
                                        "Excursion updated.",
                                        Toast.LENGTH_SHORT
                                ).show();

                                finish();
                            });
                        }

                    } catch (DateTimeParseException e) {

                        runOnUiThread(() ->
                                Toast.makeText(
                                        ExcursionDetailActivity.this,
                                        "Unable to validate vacation dates.",
                                        Toast.LENGTH_LONG
                                ).show()
                        );
                    }
                }
        );
    }

    private void deleteExcursion() {

        if (currentExcursion == null) {
            return;
        }

        repository.delete(currentExcursion);

        Toast.makeText(
                this,
                "Excursion deleted.",
                Toast.LENGTH_SHORT
        ).show();

        finish();
    }
    private void setExcursionAlert() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    101
            );

            Toast.makeText(
                    this,
                    "Allow notifications, then tap Set Excursion Alert again.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        String title =
                titleEditText.getText().toString().trim();

        String dateText =
                dateEditText.getText().toString().trim();

        if (title.isEmpty() || dateText.isEmpty()) {

            Toast.makeText(
                    this,
                    "Enter the excursion title and date first.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        try {

            LocalDate excursionDate =
                    LocalDate.parse(dateText, DATE_FORMATTER);

            long triggerTime = excursionDate
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();

            scheduleExcursionAlert(
                    triggerTime,
                    title,
                    title + " is scheduled today."
            );

            Toast.makeText(
                    this,
                    "Excursion alert has been set.",
                    Toast.LENGTH_SHORT
            ).show();

        } catch (DateTimeParseException e) {

            Toast.makeText(
                    this,
                    "Please enter the excursion date as MM/DD/YYYY.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void scheduleExcursionAlert(
            long triggerTime,
            String title,
            String message
    ) {

        Intent intent =
                new Intent(this, NotificationReceiver.class);

        intent.putExtra("title", title);
        intent.putExtra("message", message);

        int requestCode =
                Math.abs((title + triggerTime).hashCode());

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(
                        this,
                        requestCode,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT |
                                PendingIntent.FLAG_IMMUTABLE
                );

        AlarmManager alarmManager =
                (AlarmManager) getSystemService(ALARM_SERVICE);

        alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
        );
    }
    private void showDatePicker(EditText dateField) {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {

                    String selectedDate = String.format(
                            java.util.Locale.US,
                            "%02d/%02d/%04d",
                            month + 1,
                            dayOfMonth,
                            year
                    );

                    dateField.setText(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }
}