package com.example.conduit.database;

import android.content.Context;
import android.util.Log;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.conduit.dao.ExcursionDao;
import com.example.conduit.dao.VacationDao;
import com.example.conduit.entities.Excursion;
import com.example.conduit.entities.Vacation;

@Database(entities = {Vacation.class, Excursion.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract VacationDao vacationDao();
    public abstract ExcursionDao excursionDao();

    private static volatile AppDatabase DB_INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (DB_INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (DB_INSTANCE == null) {
                    DB_INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "D308").build();
                    Log.d("AppDatabase", "Database connected successfully");
                }
            }
        }
        return DB_INSTANCE;
    }
}
