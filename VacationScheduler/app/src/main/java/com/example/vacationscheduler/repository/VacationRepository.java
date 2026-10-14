package com.example.vacationscheduler.repository;

import android.app.Application;

import com.example.vacationscheduler.dao.ExcursionDAO;
import com.example.vacationscheduler.dao.VacationDAO;
import com.example.vacationscheduler.database.AppDatabase;
import com.example.vacationscheduler.entities.Vacation;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VacationRepository {

    private final VacationDAO vacationDAO;
    private final ExcursionDAO excursionDAO;

    private final ExecutorService executorService =
            Executors.newSingleThreadExecutor();

    public VacationRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        vacationDAO = database.vacationDAO();
        excursionDAO = database.excursionDAO();
    }

    public void insert(Vacation vacation) {
        executorService.execute(() -> vacationDAO.insert(vacation));
    }

    public void update(Vacation vacation) {
        executorService.execute(() -> vacationDAO.update(vacation));
    }

    public void delete(Vacation vacation) {
        executorService.execute(() -> vacationDAO.delete(vacation));
    }

    public void getAllVacations(DatabaseCallback<List<Vacation>> callback) {
        executorService.execute(() -> {
            List<Vacation> vacations = vacationDAO.getAllVacations();
            callback.onComplete(vacations);
        });
    }

    public void getVacationById(int vacationID, DatabaseCallback<Vacation> callback) {
        executorService.execute(() -> {
            Vacation vacation = vacationDAO.getVacationById(vacationID);
            callback.onComplete(vacation);
        });
    }

    public void canDeleteVacation(
            int vacationID,
            DatabaseCallback<Boolean> callback
    ) {
        executorService.execute(() -> {
            int excursionCount =
                    excursionDAO.countExcursionsForVacation(vacationID);

            callback.onComplete(excursionCount == 0);
        });
    }

    public interface DatabaseCallback<T> {
        void onComplete(T result);
    }
}