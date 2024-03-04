package com.example.conduit.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.conduit.entities.Excursion;
import com.example.conduit.entities.Vacation;

@Database(entities = {Vacation.class, Excursion.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
}
