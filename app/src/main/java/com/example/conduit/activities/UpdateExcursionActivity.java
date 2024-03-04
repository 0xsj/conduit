package com.example.conduit.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.conduit.database.AppDatabase;
import com.example.conduit.entities.Excursion;

import java.util.concurrent.Executor;

public class UpdateExcursionActivity extends AppCompatActivity {
    private EditText editExcursionTitle, editExcrusionDate;
    private Button saveExcursion;
    private Excursion currentExcursion;
    private int vacationId;
    private AppDatabase db;

    private Executor executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {}
    private void initEventHandlers() {}

    private void initViews() {}
    private boolean isDateWithinRange() {
        return false;
    }



    private void observeExcursionDetails() {}

    private void persistExcursion() {}

    private void putExcursion() {}

    private boolean isValidDate() {
        return false;
    }
}
