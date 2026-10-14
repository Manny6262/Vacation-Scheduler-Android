package com.example.vacationscheduler.repository;

import android.app.Application;

import com.example.vacationscheduler.dao.ExcursionDAO;
import com.example.vacationscheduler.database.AppDatabase;
import com.example.vacationscheduler.entities.Excursion;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExcursionRepository {

    private final ExcursionDAO excursionDAO;

    private final ExecutorService executorService =
            Executors.newSingleThreadExecutor();

    public ExcursionRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        excursionDAO = database.excursionDAO();
    }

    public void insert(Excursion excursion) {
        executorService.execute(() -> excursionDAO.insert(excursion));
    }

    public void update(Excursion excursion) {
        executorService.execute(() -> excursionDAO.update(excursion));
    }

    public void delete(Excursion excursion) {
        executorService.execute(() -> excursionDAO.delete(excursion));
    }

    public void getExcursionsForVacation(
            int vacationID,
            DatabaseCallback<List<Excursion>> callback
    ) {
        executorService.execute(() -> {
            List<Excursion> excursions =
                    excursionDAO.getExcursionsForVacation(vacationID);

            callback.onComplete(excursions);
        });
    }

    public void getExcursionById(
            int excursionID,
            DatabaseCallback<Excursion> callback
    ) {
        executorService.execute(() -> {
            Excursion excursion =
                    excursionDAO.getExcursionById(excursionID);

            callback.onComplete(excursion);
        });
    }

    public interface DatabaseCallback<T> {
        void onComplete(T result);
    }
}
