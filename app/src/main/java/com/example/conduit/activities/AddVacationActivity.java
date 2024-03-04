package com.example.conduit.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.conduit.database.AppDatabase;

public class AddVacationActivity extends AppCompatActivity {

    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the database
        db = AppDatabase.getDatabase(getApplicationContext());
    }


}
