package com.example.conduit.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import com.example.conduit.entities.Vacation;

@Dao
public interface VacationDao {
    @Query("SELECT * FROM vacations")
    LiveData<List<Vacation>> getAllVacationsAsync();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Vacation vacation);

    @Update
    void update(Vacation vacation);

    @Delete
    void delete(Vacation vacation);

    @Query("SELECT * FROM vacations WHERE id = :vacationId")
    LiveData<Vacation> getVacationByIdAsync(int vacationId);

    @Query("SELECT * FROM vacations")
    List<Vacation> getAllVacations();

    @Query("SELECT * FROM vacations WHERE id = :vacationId")
    Vacation getVacationByIdSync(int vacationId);
}
