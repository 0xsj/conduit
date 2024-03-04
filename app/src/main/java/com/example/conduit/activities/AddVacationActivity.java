package com.example.conduit.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.conduit.R;
import com.example.conduit.database.AppDatabase;

import android.widget.Button;
import android.widget.EditText;

public class AddVacationActivity extends AppCompatActivity {
    private EditText editTitle, editHotel, editStartDate, editEndDate;

    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vacation_add);
        editTitle = findViewById(R.id.editTitle);
        editHotel = findViewById(R.id.editHotel);
        editStartDate = findViewById(R.id.editStartDate);
        editEndDate = findViewById(R.id.editEndDate);

        Button saveVacationButton = findViewById(R.id.saveVacation);
        Button viewVacationButton = findViewById(R.id.viewVacations);




        // Initialize the database
        db = AppDatabase.getDatabase(getApplicationContext());
    }


}
