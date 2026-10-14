package com.example.vacationscheduler;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vacationscheduler.entities.Vacation;
import com.example.vacationscheduler.repository.VacationRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.time.ZoneId;
import androidx.appcompat.app.AlertDialog;

import com.example.vacationscheduler.repository.ExcursionRepository;
import com.example.vacationscheduler.reports.TravelReport;
import com.example.vacationscheduler.reports.VacationItineraryReport;
import com.example.vacationscheduler.validation.DateValidator;

import android.app.DatePickerDialog;
import java.util.Calendar;

public class VacationDetailActivity extends AppCompatActivity {

    private EditText titleEditText;
    private EditText hotelEditText;
    private EditText startDateEditText;
    private EditText endDateEditText;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MM/dd/yyyy");

    private VacationRepository repository;
    private ExcursionRepository excursionRepository;
    private Vacation currentVacation;

    private int vacationID = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vacation_detail);

        titleEditText = findViewById(R.id.vacationTitleEditText);
        hotelEditText = findViewById(R.id.hotelEditText);
        startDateEditText = findViewById(R.id.startDateEditText);
        endDateEditText = findViewById(R.id.endDateEditText);

        startDateEditText.setOnClickListener(v -> showDatePicker(startDateEditText));
        endDateEditText.setOnClickListener(v -> showDatePicker(endDateEditText));

        Button saveButton = findViewById(R.id.saveVacationButton);
        Button deleteButton = findViewById(R.id.deleteVacationButton);
        Button alertButton = findViewById(R.id.setVacationAlertsButton);
        Button shareButton = findViewById(R.id.shareVacationButton);
        Button excursionsButton = findViewById(R.id.viewExcursionsButton);
        Button reportButton = findViewById(R.id.generateReportButton);

        repository = new VacationRepository(getApplication());
        excursionRepository = new ExcursionRepository(getApplication());

        vacationID = getIntent().getIntExtra("vacationID", -1);

        if (vacationID != -1) {
            loadVacation();
        } else {
            deleteButton.setEnabled(false);
            excursionsButton.setEnabled(false);
            reportButton.setEnabled(false);
        }

        saveButton.setOnClickListener(v -> saveVacation());

        deleteButton.setOnClickListener(v -> deleteVacation());
        alertButton.setOnClickListener(v -> setVacationAlerts());
        shareButton.setOnClickListener(v -> shareVacation());
        reportButton.setOnClickListener(v -> generateVacationReport());
        excursionsButton.setOnClickListener(v -> {

            Intent intent = new Intent(
                    VacationDetailActivity.this,
                    ExcursionListActivity.class
            );

            intent.putExtra("vacationID", vacationID);

            startActivity(intent);
        });
    }

    private void loadVacation() {
        repository.getVacationById(vacationID, vacation -> {
            currentVacation = vacation;

            runOnUiThread(() -> {
                if (vacation != null) {
                    titleEditText.setText(vacation.getVacationTitle());
                    hotelEditText.setText(vacation.getHotel());
                    startDateEditText.setText(vacation.getStartDate());
                    endDateEditText.setText(vacation.getEndDate());
                }
            });
        });
    }

    private void saveVacation() {

        String title = titleEditText.getText().toString().trim();
        String hotel = hotelEditText.getText().toString().trim();
        String startDate = startDateEditText.getText().toString().trim();
        String endDate = endDateEditText.getText().toString().trim();

        if (title.isEmpty() || hotel.isEmpty()
                || startDate.isEmpty() || endDate.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please complete all fields.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }
        if (!DateValidator.isValidVacationRange(startDate, endDate)) {
            Toast.makeText(
                    this,
                    "Please enter valid dates. End date must be after start date.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (vacationID == -1) {

            Vacation vacation =
                    new Vacation(title, hotel, startDate, endDate);

            repository.insert(vacation);

            Toast.makeText(
                    this,
                    "Vacation added.",
                    Toast.LENGTH_SHORT
            ).show();

        } else {

            currentVacation.setVacationTitle(title);
            currentVacation.setHotel(hotel);
            currentVacation.setStartDate(startDate);
            currentVacation.setEndDate(endDate);

            repository.update(currentVacation);

            Toast.makeText(
                    this,
                    "Vacation updated.",
                    Toast.LENGTH_SHORT
            ).show();
        }

        finish();
    }

    private void deleteVacation() {

        if (currentVacation == null) {
            return;
        }

        repository.canDeleteVacation(vacationID, canDelete -> {

            if (canDelete) {

                repository.delete(currentVacation);

                runOnUiThread(() -> {
                    Toast.makeText(
                            VacationDetailActivity.this,
                            "Vacation deleted.",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();
                });

            } else {

                runOnUiThread(() ->
                        Toast.makeText(
                                VacationDetailActivity.this,
                                "Delete the associated excursions first.",
                                Toast.LENGTH_LONG
                        ).show()
                );
            }
        });
    }
    private void setVacationAlerts() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    100
            );

            Toast.makeText(
                    this,
                    "Allow notifications, then tap Set Vacation Alerts again.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        String title = titleEditText.getText().toString().trim();
        String startDateText = startDateEditText.getText().toString().trim();
        String endDateText = endDateEditText.getText().toString().trim();

        if (title.isEmpty() || startDateText.isEmpty() || endDateText.isEmpty()) {
            Toast.makeText(
                    this,
                    "Enter the vacation title and dates first.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        try {

            LocalDate startDate =
                    LocalDate.parse(startDateText, DATE_FORMATTER);

            LocalDate endDate =
                    LocalDate.parse(endDateText, DATE_FORMATTER);

            long startTime = startDate
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();

            long endTime = endDate
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();

            scheduleVacationAlert(
                    startTime,
                    title,
                    title + " is starting today.",
                    Math.abs((title + startDateText + "start").hashCode())
            );

            scheduleVacationAlert(
                    endTime,
                    title,
                    title + " is ending today.",
                    Math.abs((title + endDateText + "end").hashCode())
            );

            Toast.makeText(
                    this,
                    "Vacation alerts have been set.",
                    Toast.LENGTH_SHORT
            ).show();

        } catch (DateTimeParseException e) {

            Toast.makeText(
                    this,
                    "Please enter dates as MM/DD/YYYY.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void scheduleVacationAlert(
            long triggerTime,
            String title,
            String message,
            int requestCode
    ) {

        Intent intent =
                new Intent(this, NotificationReceiver.class);

        intent.putExtra("title", title);
        intent.putExtra("message", message);

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
    private void shareVacation() {

        String title = titleEditText.getText().toString().trim();
        String hotel = hotelEditText.getText().toString().trim();
        String startDate = startDateEditText.getText().toString().trim();
        String endDate = endDateEditText.getText().toString().trim();

        if (title.isEmpty() || hotel.isEmpty()
                || startDate.isEmpty() || endDate.isEmpty()) {

            Toast.makeText(
                    this,
                    "Complete the vacation details before sharing.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String vacationDetails =
                "Vacation: " + title
                        + "\nHotel: " + hotel
                        + "\nStart Date: " + startDate
                        + "\nEnd Date: " + endDate;

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);
        shareIntent.putExtra(Intent.EXTRA_TEXT, vacationDetails);

        startActivity(
                Intent.createChooser(
                        shareIntent,
                        "Share Vacation Details"
                )
        );
    }
    private void generateVacationReport() {

        if (currentVacation == null) {
            Toast.makeText(
                    this,
                    "Save the vacation before generating a report.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        excursionRepository.getExcursionsForVacation(
                vacationID,
                excursions -> {

                    TravelReport report =
                            new VacationItineraryReport(
                                    currentVacation,
                                    excursions
                            );

                    String reportText = report.generateReport();

                    runOnUiThread(() ->
                            new AlertDialog.Builder(
                                    VacationDetailActivity.this
                            )
                                    .setTitle(report.getReportTitle())
                                    .setMessage(reportText)
                                    .setPositiveButton("Close", null)
                                    .show()
                    );
                }
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