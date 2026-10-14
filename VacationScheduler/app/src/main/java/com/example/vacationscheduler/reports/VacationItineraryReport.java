package com.example.vacationscheduler.reports;

import com.example.vacationscheduler.entities.Excursion;
import com.example.vacationscheduler.entities.Vacation;

import java.util.List;

public class VacationItineraryReport extends TravelReport {

    private final Vacation vacation;
    private final List<Excursion> excursions;

    public VacationItineraryReport(
            Vacation vacation,
            List<Excursion> excursions
    ) {
        super("Vacation Itinerary");
        this.vacation = vacation;
        this.excursions = excursions;
    }

    @Override
    public String generateReport() {

        StringBuilder report = new StringBuilder();

        report.append("VACATION ITINERARY\n\n");

        report.append("Vacation: ")
                .append(vacation.getVacationTitle())
                .append("\n");

        report.append("Hotel: ")
                .append(vacation.getHotel())
                .append("\n");

        report.append("Start Date: ")
                .append(vacation.getStartDate())
                .append("\n");

        report.append("End Date: ")
                .append(vacation.getEndDate())
                .append("\n\n");

        report.append("EXCURSIONS\n");

        if (excursions.isEmpty()) {
            report.append("No excursions scheduled.");
        } else {
            for (Excursion excursion : excursions) {
                report.append("\n")
                        .append(excursion.getExcursionTitle())
                        .append(" - ")
                        .append(excursion.getExcursionDate());
            }
        }

        return report.toString();
    }
}