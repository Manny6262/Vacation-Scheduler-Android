package com.example.vacationscheduler.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.vacationscheduler.entities.Excursion;

import java.util.List;

@Dao
public interface ExcursionDAO {

    @Insert
    void insert(Excursion excursion);

    @Update
    void update(Excursion excursion);

    @Delete
    void delete(Excursion excursion);

    @Query("SELECT * FROM excursions WHERE vacationID = :vacationID ORDER BY excursionID ASC")
    List<Excursion> getExcursionsForVacation(int vacationID);

    @Query("SELECT COUNT(*) FROM excursions WHERE vacationID = :vacationID")
    int countExcursionsForVacation(int vacationID);
    @Query("SELECT * FROM excursions WHERE excursionID = :excursionID LIMIT 1")
    Excursion getExcursionById(int excursionID);
}