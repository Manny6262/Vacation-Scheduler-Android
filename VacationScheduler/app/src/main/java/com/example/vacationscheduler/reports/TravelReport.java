package com.example.vacationscheduler.reports;

public abstract class TravelReport {

    private final String reportTitle;

    public TravelReport(String reportTitle) {
        this.reportTitle = reportTitle;
    }

    public String getReportTitle() {
        return reportTitle;
    }

    public abstract String generateReport();
}