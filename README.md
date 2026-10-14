# Vacation Scheduler

## Project Description

Vacation Scheduler is an Android app that helps users organize vacations and excursions. Users can save trip details, search vacations, set date alerts, and generate itinerary reports.

## Tools Used

- Java
- Android Studio
- Room and SQlite for database storage
- GitLab for version control
- JUnit for testing

## How to Run the App

1. Open the 'VacationScheduler' folder in Android Studio.
2. Allow Gradle to sync.
3. Select an Android emulator or connected device.
4. Select the 'app' configuration and click Run.

## How to Update the Code

1. Open the project in Android Studio.
2. Make the needed changes to the Java files or XML layouts under 'app/src/main'.
3. Build and run the app to check the changes.
4. Run the unit tests to verify the validation features.
5. Commit and push the changes to the 'Working' branch in GitLab.

## Testing

Unit tests are located in 'app/src/test'. The 'DateValidatorTest.java' file contains seven tests for vacation and excursion date validation.

To run the tests, right-click 'DateValidatorTest.java' and select Run.

## Project Features

- Add, edit and delete vacations and excursions
- Search vacations
- Validate vacation and excursion dates
- Set date alerts
- Share vacation details
- Generate itinerary reports
- Store information using Room and SQLite