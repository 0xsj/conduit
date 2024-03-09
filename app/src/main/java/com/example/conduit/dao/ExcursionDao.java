package com.example.conduit.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import com.example.conduit.entities.Excursion;

@Dao
public interface ExcursionDao {
    @Query("SELECT * FROM excursions WHERE vacationId = :vacationId")
    LiveData<List<Excursion>> getExcursionsForVacationAsync(int vacationId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Excursion excursion);

    @Update
    void update(Excursion excursion);

    @Delete
    void delete(Excursion excursion);

    @Query("SELECT * FROM excursions WHERE vacationId = :vacationId")
    List<Excursion> getExcursionsForVacation(int vacationId);

    @Query("SELECT * FROM excursions WHERE id = :excursionId")
    LiveData<Excursion> getExcursionById(int excursionId);

}
