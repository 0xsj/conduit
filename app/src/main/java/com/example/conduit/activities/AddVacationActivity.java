package com.example.conduit.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.conduit.R;
import com.example.conduit.database.AppDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddVacationActivity extends AppCompatActivity {
    private EditText editTitle, editHotel, editStartDate, editEndDate;

    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vacation_add);
        initViews();
        initEventHandlers();
    }

    private void initViews() {
        editTitle = findViewById(R.id.editTitle);
        editHotel = findViewById(R.id.editHotel);
        editStartDate = findViewById(R.id.editStartDate);
        editEndDate = findViewById(R.id.editEndDate);
    }

    private void initEventHandlers() {
        Button saveVacationButton = findViewById(R.id.saveVacation);
        saveVacationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
            }
        });

        Button viewVacationButton = findViewById(R.id.viewVacations);
        viewVacationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
            }
        });

        Button setAlertButton = findViewById(R.id.setAlert);
        setAlertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
            }
        });
    }

    private void onAddEventToCalendar() {}

    private void onClearFields() {}

    private void onResetFields() {
    }

    private void onSetAlert() {}

    private void onSetAlarm() {}

    private void onSetAlarmForVacation() {}

    private void onShowNoChangesMade() {}

    private void onShowSaveSuccess() {}

    private void onShowValidationError() {}

    private void onUpdateUIVacationDetails() {}

    private void onSaveVacation() {}

    private void onLoadVacationDetails() {}

    private boolean onValidateInput() {
        return false;
    }
}
