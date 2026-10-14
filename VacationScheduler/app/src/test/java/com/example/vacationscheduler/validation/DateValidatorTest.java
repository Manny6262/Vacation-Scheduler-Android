package com.example.vacationscheduler.validation;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DateValidatorTest {

    @Test
    public void validVacationRange_returnsTrue() {
        assertTrue(DateValidator.isValidVacationRange(
                "10/10/2026",
                "10/15/2026"
        ));
    }

    @Test
    public void endBeforeStart_returnsFalse() {
        assertFalse(DateValidator.isValidVacationRange(
                "10/15/2026",
                "10/10/2026"
        ));
    }

    @Test
    public void invalidDateFormat_returnsFalse() {
        assertFalse(DateValidator.isValidVacationRange(
                "2026-10-10",
                "10/15/2026"
        ));
    }

    @Test
    public void excursionInsideVacation_returnsTrue() {
        assertTrue(DateValidator.isExcursionWithinVacation(
                "10/12/2026",
                "10/10/2026",
                "10/15/2026"
        ));
    }

    @Test
    public void excursionBeforeVacation_returnsFalse() {
        assertFalse(DateValidator.isExcursionWithinVacation(
                "10/05/2026",
                "10/10/2026",
                "10/15/2026"
        ));
    }

    @Test
    public void excursionAfterVacation_returnsFalse() {
        assertFalse(DateValidator.isExcursionWithinVacation(
                "10/20/2026",
                "10/10/2026",
                "10/15/2026"
        ));
    }
    @Test
    public void invalidCalendarDate_returnsFalse() {
        assertFalse(DateValidator.isValidVacationRange(
                "02/30/2026",
                "03/05/2026"
        ));
    }
}