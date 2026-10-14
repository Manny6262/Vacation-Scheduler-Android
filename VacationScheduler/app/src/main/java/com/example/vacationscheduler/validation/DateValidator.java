package com.example.vacationscheduler.validation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public class DateValidator {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MM/dd/uuuu")
                    .withResolverStyle(ResolverStyle.STRICT);

    private DateValidator() {
    }

    public static boolean isValidVacationRange(
            String startDate,
            String endDate
    ) {
        try {
            LocalDate start =
                    LocalDate.parse(startDate, DATE_FORMATTER);

            LocalDate end =
                    LocalDate.parse(endDate, DATE_FORMATTER);

            return end.isAfter(start);

        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static boolean isExcursionWithinVacation(
            String excursionDate,
            String vacationStart,
            String vacationEnd
    ) {
        try {
            LocalDate excursion =
                    LocalDate.parse(excursionDate, DATE_FORMATTER);

            LocalDate start =
                    LocalDate.parse(vacationStart, DATE_FORMATTER);

            LocalDate end =
                    LocalDate.parse(vacationEnd, DATE_FORMATTER);

            return !excursion.isBefore(start)
                    && !excursion.isAfter(end);

        } catch (DateTimeParseException e) {
            return false;
        }
    }
}